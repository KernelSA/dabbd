package ua.kernel.dabbd.commons.entity;

import lombok.Data;
import org.springframework.data.geo.Point;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity(name = "DM_TRACKER")
public class DmTrackerEntity {

    @Id
    @Column(name = "TrackerID", unique = true, nullable = false, length = 50) //(50) primary key,
    private String trackerId;
    @Column(name = "Fuel", nullable = false)            //NOT NULL,
    private Integer fuel;
    @Column(name = "Power", nullable = false)           //NOT NULL,
    private Integer power;
    @Column(name = "Speed", nullable = false)           //NOT NULL,
    private Integer speed;
    @Column(name = "Coordinates", nullable = false, columnDefinition = "point")     //NOT NULL,
    private Point coordinates;
    //     Double[] coordinates;
    @Column(name = "GsmSignal", nullable = false)       //NOT NULL,
    private Integer gsmSignal;
    @Column(name = "GpsSattelites", nullable = false)   //NOT NULL,
    private Integer gpsSatellites;
    @Column(name = "Online")
    private Boolean online;
    @Column(name = "LastEventDt")                       //without Time zone,
    private LocalDateTime lastEventDt;
    @Column(name = "LastEventID", nullable = false)     //NOT NULL,
    private Integer lastEventID;
    @Column(name = "InsDt", nullable = false)           //without Time zone NOT null default current_timestamp,
    private LocalDateTime insDt;
    @Column(name = "UpdDt")                             //without Time zone
    private LocalDateTime updDt;

}
