package ua.kernel.dabbd.commons.model;


import lombok.Data;

@Data
public class KernelRoute {

    // Id
    private String routeId;
    // KM
    private Double distance;
    // route polygon
    private KernelGisPolygon pol;
    // comments
    private String through;
    private String descr;


}
