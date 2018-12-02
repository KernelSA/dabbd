package ua.kernel.dabbd.commons.entity;

import lombok.Data;
import org.springframework.data.geo.Point;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity(name = "EVENTS")
public class EventsEntity {

    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EventID", unique = true, insertable = false, updatable = false, nullable = false)           // primary key,
    private Long eventId;
    @Column(name = "TrackerID", length = 50, nullable = false)         // (50) NOT NULL,
    private String trackerId;
    @Column(name = "SourceType", length = 50, nullable = false)        // (50) NOT NULL,
    private String sourceType;
    @Column(name = "EventDt", nullable = false)           // without Time zone NOT NULL,
    private LocalDateTime eventDt;
    @Column(name = "Coordinates", nullable = false, columnDefinition = "point")       // NOT NULL,
    private Point coordinates;
    @Column(name = "Speed", nullable = false)             // NOT NULL,
    private Integer speed;
    @Column(name = "Fuel", nullable = false)              // NOT NULL,
    private Integer fuel;
    @Column(name = "Power", nullable = false)             // NOT NULL,
    private Integer power;
    @Column(name = "GsmSignal", nullable = false)         // NOT NULL,
    private Integer gsmSignal;
    @Column(name = "GpsSattelites", nullable = false)     // NOT NULL,
    private Integer gpsSatellites;
    @Column(name = "InsDt", nullable = false)             // without Time zone NOT null default current_timestamp
    private LocalDateTime insDt;


}
