package ua.kernel.dabbd.triggers.functions;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import jdk.nashorn.internal.objects.Global;
import jdk.nashorn.internal.parser.JSONParser;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import scala.util.parsing.json.JSON;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class PointInPolygonTest {
    public static final Path RESOURCE_DIRECTORY = Paths.get("src", "test", "resources");


    @Test
    public void test() throws URISyntaxException, IOException {

//        URL routeItem = this.getClass().getClassLoader().getResource("/data/routeItem.json");
        Path path = RESOURCE_DIRECTORY.resolve("data").resolve("routeItem.json");
        byte[] bytes = Files.readAllBytes(path);
        Map<String, Object> myMap = new HashMap<String, Object>();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(bytes);
        JsonNode jsonNode1 = jsonNode.get("pol").withArray("coordinates").get(0);

        GeometryFactory gf = new GeometryFactory();
        // create polygon
        int numPoints = jsonNode1.size();
        System.out.println("numPoints " + numPoints);
        Coordinate[] points = new Coordinate[numPoints+1];
// set points
//...
        int i = 0;
        for (JsonNode node : jsonNode1) {
//            System.out.println(node.get(0)+">>"+node.get(1));
            points[i++] = new Coordinate(node.get(0).asDouble(), node.get(1).asDouble());

        }
        System.out.println("i= " + i);
        points[numPoints] = points[0];
        System.out.println(points);

        LinearRing jtsRing = gf.createLinearRing(points);
        Polygon poly = gf.createPolygon(jtsRing, null);

// now create point to test if contained inside polygon
//        34.09497057132573>>49.227120148309716
//        34.09497504836257>>49.22711483075883

        // inside
//        Coordinate coord = new Coordinate(34.094975048362_6, 49.22711483075883);
//        Coordinate coord = new Coordinate(34.5119727, 49.5488114);

//      OUTSIDE
        Coordinate coord = new Coordinate(34.5118654,
                49.5484842);


        Point pt = gf.createPoint(coord);
        long start = System.currentTimeMillis();
        if (poly.contains(pt)) {
            // point is contained within bounds of polygon
            // do something here
            System.out.println(">>> INSIDE " + (System.currentTimeMillis() - start));
        } else {
            System.out.println(">>> OUTSIDE " + (System.currentTimeMillis() - start));
        }
    }

}
