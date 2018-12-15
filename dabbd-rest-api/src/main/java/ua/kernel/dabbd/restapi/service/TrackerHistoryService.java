package ua.kernel.dabbd.restapi.service;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.kernel.dabbd.commons.entity.EventsEntity;
import ua.kernel.dabbd.commons.model.Feature;
import ua.kernel.dabbd.commons.model.FeatureCollection;
import ua.kernel.dabbd.commons.repository.EventsRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TrackerHistoryService {

    private GeometryFactory geometryFactory = new GeometryFactory();

    private final EventsRepository eventsRepository;

    public FeatureCollection getTrack(String trackerId) {
        log.info("=> Going to prepare tracker history for tracker '{}'", trackerId);
//        List<EventsEntity> allByTrackerId = eventsRepository.findAllByTrackerId(trackerId);
        LocalDateTime today = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        List<EventsEntity> allByTrackerId
                = eventsRepository.findByTrackerIdAndEveAndEventDtGreaterThan(trackerId, today);

        log.info("=> Found {} records for tracker {} for date {}", allByTrackerId.size(), trackerId, today);

        List<Feature> collect = allByTrackerId.stream().sorted(Comparator.comparing(EventsEntity::getEventDt))
                .map(eventsEntity ->
                        Feature.builder()
                                .properties(eventsEntity)
                                .geometry(geometryFactory
                                        .createPoint(new Coordinate(eventsEntity.getLatitude(), eventsEntity.getLongitude())))
                                .build())
                .collect(Collectors.toList());

        log.trace("=> History track for tracker '{}' => {}", trackerId, collect);

        return FeatureCollection.builder().features(collect).build();
    }


}
