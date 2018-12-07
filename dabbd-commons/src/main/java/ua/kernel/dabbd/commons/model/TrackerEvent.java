package ua.kernel.dabbd.commons.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
public class TrackerEvent {

    private String trackerId;
    private String sourceType;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "dd.MM.yyyy-HH:mm:ss")
    private LocalDateTime eventDt;
    private ArrayList<Double> coordinates;
    private Integer speed;
    private Integer fuelLevel;
    private Integer powerLevel;
    private Short gsmSignal;
    private Short sattelites;

}
