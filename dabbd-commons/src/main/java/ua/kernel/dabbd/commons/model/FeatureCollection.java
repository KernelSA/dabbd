package ua.kernel.dabbd.commons.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FeatureCollection {
    private final String type = "FeatureCollection";
    private List<Feature> features;
}

    