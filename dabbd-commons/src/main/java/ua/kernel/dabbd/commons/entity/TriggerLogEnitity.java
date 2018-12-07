package ua.kernel.dabbd.commons.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@Entity(name = "LOG_TRIGGERS")
public class TriggerLogEnitity {

    @Id
    @Column(name = "TriggerID", unique = true, nullable = false, insertable = false, updatable = false)     //primary key,
    private Long triggerId;
    @Column(name = "TrackerId", length = 50, nullable = false)      //(50),
    private String trackerId;
    @Column(name = "TriggerType", length = 20)                      //(20),
    private String triggerType;
    @Column(name = "TriggerDt", nullable = false)     //without Time zone NOT null default current_timestamp,
    private LocalDateTime triggerDt;
    @Column(name = "EventDt")       //without Time zone
    private LocalDateTime eventDt;


}
