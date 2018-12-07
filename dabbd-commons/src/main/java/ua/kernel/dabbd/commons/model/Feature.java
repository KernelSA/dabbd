package ua.kernel.dabbd.commons.model;

import com.vividsolutions.jts.geom.Geometry;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import ua.kernel.dabbd.commons.entity.EventsEntity;

@Data
@Builder
public class Feature {
    @Getter
    private final String type = "Feature";
    private Geometry geometry;
    private EventsEntity properties;

}