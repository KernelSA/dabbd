package ua.kernel.dabbd.commons.model;


import lombok.Data;

import java.util.ArrayList;

@Data
public class KernelGisPolygon {

    // Always expected "Polygon" for now
    private String type;

    // [[[a,b],[c,d],...,[x,y]]]
    private ArrayList<ArrayList<ArrayList<Double>>> coordinates;

    // 4 points array
    private ArrayList<Double> bbox;

}
