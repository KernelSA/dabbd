package ua.kernel.dabbd.commons.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrackerEvent {

    private String trackerId;
    private String sourceType;
    private LocalDateTime eventDt;
    private Double[] coordinates;
    private Integer speed;
    private Integer fuelLevel;
    private Integer powerLevel;
    private Integer gsmSignal;
    private Integer gpsSattelites;
}
