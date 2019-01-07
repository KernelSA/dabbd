package ua.kernel.dabbd.commons.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ResultCheckStyle;
import org.hibernate.annotations.SQLInsert;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "LOG_TRIGGERS")
public class TriggerLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trigger_id", unique = true, nullable = false, insertable = false, updatable = false)     //primary key,
    private Long triggerId;
    @Column(name = "tracker_id", length = 50, nullable = false)      //(50),
    private String trackerId;
    @Column(name = "trigger_type", length = 20)                      //(20),
    private String triggerType;
    @Column(name = "trigger_dt", nullable = false)     //without Time zone NOT null default current_timestamp,
    private LocalDateTime triggerDt;
    @Column(name = "event_dt")       //without Time zone
    private LocalDateTime eventDt;
    @Column(name = "trigger_info", length = 3000)                      //(20),
    private String triggerInfo;


}
