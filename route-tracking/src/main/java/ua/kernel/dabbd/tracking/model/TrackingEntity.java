package ua.kernel.dabbd.tracking.model;


import lombok.Data;
import ua.kernel.dabbd.commons.model.TrackerEvent;
import ua.kernel.dabbd.commons.model.WaybillRequest;

@Data
public class TrackingEntity {

    private final WaybillRequest waybillRequest;
    private TrackerEvent lastEvent;
    private TrackerEvent firstOutOfRouteEvent;

    private TrackingState state = TrackingState.INIT;


    public static enum TrackingState {
        INIT, LOAD_ZONE, ON_ROUTE, OUT_OF_ROUTE, UNLOAD_ZONE
    }

}
