package ua.kernel.dabbd.commons.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "DM_TRACKER")
public class DmTrackerEntity {

    @Id
    @Column(name = "tracker_id", unique = true, length = 50) //(50) primary key,
    private String trackerId;
    @Column(name = "Fuel")
    private Integer fuel;
    @Column(name = "Power")
    private Integer power;
    @Column(name = "Speed")
    private Integer speed;
    @Column(name = "Latitude")
    private Double latitude;
    @Column(name = "Longitude")
    private Double longitude;
    @Column(name = "GSM_Signal")
    private Integer gsmSignal;
    @Column(name = "GPS_Satellites")
    private Integer gpsSatellites;
    @Column(name = "Online")
    private Boolean online;
    @Column(name = "Last_event_dt")          //without Time zone,
    private LocalDateTime lastEventDt;
    @Column(name = "last_event_id")
    private Integer lastEventID;
    @Column(name = "Ins_dt")                 //without Time zone NOT null default current_timestamp,
    private LocalDateTime insDt;
    @Column(name = "Upd_dt")                 //without Time zone
    private LocalDateTime updDt;

}
