package ua.kernel.dabbd.commons.entity;

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
    @Column(name = "event_id", unique = true, insertable = false, updatable = false)           // primary key,
    private Long eventId;
    @Column(name = "tracker_id", length = 50)         // (50) NOT NULL,
    private String trackerId;
    @Column(name = "source_type", length = 50)        // (50) NOT NULL,
    private String sourceType;
    @Column(name = "event_dt")           // without Time zone NOT NULL,
    private LocalDateTime eventDt;
    @Column(name = "latitude")
    private Double latitude;
    @Column(name = "longitude")
    private Double longitude;
    @Column(name = "speed")
    private Integer speed;
    @Column(name = "fuel")
    private Integer fuel;
    @Column(name = "power")
    private Integer power;
    @Column(name = "gsm_signal")
    private Integer gsmSignal;
    @Column(name = "gps_satellites")
    private Integer gpsSatellites;
    @Column(name = "ins_dt", insertable = false, updatable = false)             // without Time zone NOT null DEFAULT CURRENT_TIMESTAMP
    private LocalDateTime insDt;
    @Column(name = "profile_updated")
    private Boolean Profile_Updated;
    @Column(name = "kafka_timestamp")
    private LocalDateTime kafkaTimestamp;


}
