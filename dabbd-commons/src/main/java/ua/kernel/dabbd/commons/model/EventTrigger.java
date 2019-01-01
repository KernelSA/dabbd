package ua.kernel.dabbd.commons.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Data
public class EventTrigger {

    private String trackerId;

    private String triggerId;
    private TriggerType triggerType;
    private String triggerInfo;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "dd.MM.yyyy-HH:mm:ss")
    private LocalDateTime triggerDt;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "dd.MM.yyyy-HH:mm:ss")
    private LocalDateTime eventDt; //?? last  Event DT caused the trigger event

    public EventTrigger() {
        this.triggerId = UUID.randomUUID().toString();
    }
}
