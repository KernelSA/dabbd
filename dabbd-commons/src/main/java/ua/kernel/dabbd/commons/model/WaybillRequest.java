package ua.kernel.dabbd.commons.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class WaybillRequest {

    private Integer eventType;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss")
    private LocalDateTime dateR; //25.07.2018 14:58:04

    private String trackerId; //"867844001422677"
    private String docNo;   //"0292-000092"
    private String docGuid; //"4ddcab5f-968d-4eba-a516-c60e6af1b6cb"

    private Integer fromId; //394
    private String fromType; //"c"
    private String fromDescr; //"60.20.17.02.06.33"
    private KernelGisPolygon fromPol;

    private Integer destId; //63
    private String destType; //"w"
    private String destDescr;
    private KernelGisPolygon destPol;

    private Set<KernelRoute> routes;


}
