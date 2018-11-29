package ua.kernel.dabbd.commons.model.entity;

import lombok.Data;

import javax.persistence.Entity;
import java.time.LocalDateTime;

@Data
@Entity
public class TrackerEventEntity {


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
