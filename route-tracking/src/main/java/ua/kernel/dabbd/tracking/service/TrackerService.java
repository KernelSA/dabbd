package ua.kernel.dabbd.tracking.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ua.kernel.dabbd.commons.model.EventTrigger;
import ua.kernel.dabbd.commons.model.KernelRoute;
import ua.kernel.dabbd.commons.model.TrackerEvent;
import ua.kernel.dabbd.commons.model.TriggerType;
import ua.kernel.dabbd.commons.model.WaybillRequest;
import ua.kernel.dabbd.tracking.config.RouteTrackingProperties;
import ua.kernel.dabbd.tracking.model.TrackingEntity;
import ua.kernel.dabbd.tracking.model.TrackingEntity.TrackingState;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class TrackerService {

    private static final long OUT_OF_ROUTE_THRESHOLD_MINUTES = 1L;
    private static final long WAYBILL_TIMEOUT_THRESHOLD_HOURS = 8L;

    private final RouteTrackingProperties properties;
    private final KafkaTemplate<String, EventTrigger> kafkaTemplate;

    private Map<String, TrackingEntity> trackingMap = new HashMap<>();

    @PostConstruct
    private void init() {
        new Thread(() -> {
            new ArrayList<>(trackingMap.values()).forEach((trackingEntity) -> {
                // check tracking state
                TrackerEvent lastEvent = trackingEntity.getLastEvent();
                switch (trackingEntity.getState()) {
                    case OUT_OF_ROUTE:
                        TrackerEvent firstOutOfRouteEvent = trackingEntity.getFirstOutOfRouteEvent();


                        if (firstOutOfRouteEvent.getEventDt().plusMinutes(OUT_OF_ROUTE_THRESHOLD_MINUTES).isBefore(LocalDateTime.now())) {
                            log.info("Tracker is OUT OF ROUTE more then a {} minutes. Going to send trigger event.", OUT_OF_ROUTE_THRESHOLD_MINUTES);
                            EventTrigger eventTrigger = new EventTrigger();
                            eventTrigger.setTrackerId(firstOutOfRouteEvent.getTrackerId());
                            eventTrigger.setTriggerDt(LocalDateTime.now());
                            eventTrigger.setTriggerInfo("Tracker is out of route, Waybill(TTN) docNo: "
                                    + trackingEntity.getWaybillRequest().getDocNo()
                                    + " docGuid: " + trackingEntity.getWaybillRequest().getDocGuid());
                            eventTrigger.setTriggerEvents(Arrays.asList(firstOutOfRouteEvent, lastEvent));
                            eventTrigger.setTriggerType(TriggerType.OUT_OF_ROUTE);
                            eventTrigger.setEventDt(lastEvent.getEventDt());

                            kafkaTemplate.send(new ProducerRecord<>(properties.getTriggersTopic(), eventTrigger));
                        }
                        break;
                    case UNLOAD_ZONE:
                        trackingMap.remove(trackingEntity.getWaybillRequest().getTrackerId());
                        break;
                    default:
                        if (lastEvent != null &&
                                lastEvent.getEventDt().plusHours(WAYBILL_TIMEOUT_THRESHOLD_HOURS).isBefore(LocalDateTime.now())) {

                            //TODO send event about abnormal Waybill termination?
                            trackingMap.remove(trackingEntity.getWaybillRequest().getTrackerId());
                        }
                        break;
                }
            });
        }).start();
    }

    public void addWaybill(WaybillRequest waybillRequest) {

        if (trackingMap.containsKey(waybillRequest.getTrackerId())) {
            //TODO cancel or update tracking
            TrackingEntity trackingEntity = trackingMap.get(waybillRequest.getTrackerId());
            if (waybillRequest.getTrackerId().equals(trackingEntity.getWaybillRequest().getTrackerId())) {
                // do nothing? request is the same
            } else {
                // what the difference?
                trackingEntity.getWaybillRequest().getEventType().equals(1); // change route
                //TODO  do something?
            }
        } else {
            trackingMap.put(waybillRequest.getTrackerId(), new TrackingEntity(waybillRequest));
        }

    }

    public void trackEvent(TrackerEvent event) {
        String trackerId = event.getTrackerId();
        if (trackingMap.containsKey(trackerId)) {
            TrackingEntity trackingEntity = trackingMap.get(trackerId);
            switch (trackingEntity.getState()) {

                case INIT:
                    // check event in load zone or on one of routes
                    if (checkEventInLoadingZone(event, trackingEntity)) {
                        updateTrackingState(event, trackingEntity, TrackingState.LOAD_ZONE);
                    }
                    break;
                case LOAD_ZONE:
                    // check event in load zone or on one of routes
                    if (checkEventInLoadingZone(event, trackingEntity)) {
                        updateTrackingState(event, trackingEntity, TrackingState.LOAD_ZONE);
                    } else {
                        if (checkEventIsOnRoute(event, trackingEntity).isPresent()) {
                            updateTrackingState(event, trackingEntity, TrackingState.ON_ROUTE);
                        } else {
                            //TODO check rare case, jump from load zone to unload zone(is it possible?)
                            // otherwise  off route
                            updateTrackingState(event, trackingEntity, TrackingState.OUT_OF_ROUTE);
                            trackingEntity.setFirstOutOfRouteEvent(event);
                        }
                    }
                    break;
                case ON_ROUTE:
                    // check event on one of routes or in unload_zone
                    if (checkEventIsOnRoute(event, trackingEntity).isPresent()) {
                        updateTrackingState(event, trackingEntity, TrackingState.ON_ROUTE);
                    } else {
                        if (checkEventInUnloadingZone(event, trackingEntity)) {
                            updateTrackingState(event, trackingEntity, TrackingState.UNLOAD_ZONE);
                            //TODO stop tracking??
                        } else {
                            // off route
                            updateTrackingState(event, trackingEntity, TrackingState.OUT_OF_ROUTE);
                            trackingEntity.setFirstOutOfRouteEvent(event);
                        }
                    }

                    break;
                case OUT_OF_ROUTE:
                    // check event still off route or on route
                    //TODO  check that from prev state pass more than N seconds and fire trigger?
                    if (checkEventIsOnRoute(event, trackingEntity).isPresent()) {
                        // back on route
                        updateTrackingState(event, trackingEntity, TrackingState.ON_ROUTE);
                    } else {
                        if (checkEventInUnloadingZone(event, trackingEntity)) {
                            updateTrackingState(event, trackingEntity, TrackingState.UNLOAD_ZONE);
                            //TODO stop tracking??
                        }
                        //nothing changed, still off route
                    }

                    break;
                case UNLOAD_ZONE:
                    // stop tracking
                    // or should not reach here
                    log.warn("Event received while in Unloading zone, this state should be already processed in separate thread. " +
                            "Evt: {}, TrackingEntity: {}", event, trackingEntity);
                    break;
                default:
                    log.warn("TrackingEntity in a wrong state: {}", trackingEntity);
            }
        }
    }

    private void updateTrackingState(TrackerEvent event, TrackingEntity trackingEntity, TrackingState state) {
        trackingEntity.setState(state);
        trackingEntity.setLastEvent(event);
        log.debug("Tracker event: '{}' is '{}'. Waybill: '{}'", event, state, trackingEntity);
    }

    private boolean checkEventInLoadingZone(TrackerEvent event, TrackingEntity trackingEntity) {
        ArrayList<ArrayList<Double>> loadingZonePolygon = trackingEntity.getWaybillRequest().getFromPol().getCoordinates().get(0);
        ArrayList<Double> eventCoords = event.getCoordinates();
        return checkPointInPolygon(eventCoords, loadingZonePolygon);
    }

    private boolean checkEventInUnloadingZone(TrackerEvent event, TrackingEntity trackingEntity) {
        ArrayList<ArrayList<Double>> loadingZonePolygon = trackingEntity.getWaybillRequest().getDestPol().getCoordinates().get(0);
        ArrayList<Double> eventCoords = event.getCoordinates();
        return checkPointInPolygon(eventCoords, loadingZonePolygon);
    }

    private Optional<String> checkEventIsOnRoute(TrackerEvent event, TrackingEntity trackingEntity) {
        Set<KernelRoute> routes = trackingEntity.getWaybillRequest().getRoutes();
        for (KernelRoute route : routes) {
            ArrayList<ArrayList<Double>> routePol = route.getPol().getCoordinates().get(0);
            ArrayList<Double> eventCoords = event.getCoordinates();
            if (checkPointInPolygon(eventCoords, routePol)) {
                return Optional.of(route.getRouteId());
            }
        }
        return Optional.empty();
    }

    private boolean checkPointInPolygon(ArrayList<Double> coordinates, ArrayList<ArrayList<Double>> polygonCoordinates) {

        GeometryFactory gf = new GeometryFactory();
        // create polygon
        int numPoints = polygonCoordinates.size();
        System.out.println("numPoints " + numPoints);

        Coordinate[] points = new Coordinate[numPoints + 1];
        int i = 0;
        for (ArrayList<Double> node : polygonCoordinates) {
            points[i++] = new Coordinate(node.get(0), node.get(1));

        }
        System.out.println("i= " + i);
        // close the polygon
        points[numPoints] = points[0];
        System.out.println(points);

        LinearRing jtsRing = gf.createLinearRing(points);
        Polygon poly = gf.createPolygon(jtsRing, null);

        Coordinate coord = new Coordinate(coordinates.get(0), coordinates.get(1));

        Point pt = gf.createPoint(coord);
        long start = System.currentTimeMillis();
        if (poly.contains(pt)) {
            // point is contained within bounds of polygon
            // do something here
            System.out.println(">>> INSIDE " + (System.currentTimeMillis() - start));
            return true;
        } else {
            System.out.println(">>> OUTSIDE " + (System.currentTimeMillis() - start));
            return false;
        }
    }

}
