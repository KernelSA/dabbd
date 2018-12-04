package ua.kernel.dabbd.commons.entity;

import com.vividsolutions.jts.geom.Point;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "EVENTS")
public class EventsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EventID", unique = true, insertable = false, updatable = false)           // primary key,
    private Long eventId;
    @Column(name = "TrackerID", length = 50, nullable = false)         // (50) NOT NULL,
    private String trackerId;
    @Column(name = "SourceType", length = 50)        // (50) NOT NULL,
    private String sourceType;
    @Column(name = "EventDt")           // without Time zone NOT NULL,
    private LocalDateTime eventDt;
    @Column(name = "lat")       // NOT NULL,
    private Double lat;
    @Column(name = "long")
    private Double lng;
    @Column(name = "Speed")             // NOT NULL,
    private Integer speed;
    @Column(name = "Fuel")              // NOT NULL,
    private Integer fuel;
    @Column(name = "Power")             // NOT NULL,
    private Integer power;
    @Column(name = "GsmSignal")         // NOT NULL,
    private Integer gsmSignal;
    @Column(name = "GpsSattelites")     // NOT NULL,
    private Integer gpsSatellites;
    @Column(name = "InsDt")             // without Time zone NOT null DEFAULT CURRENT_TIMESTAMP
    private LocalDateTime insDt;


}
