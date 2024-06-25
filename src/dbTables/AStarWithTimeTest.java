package dbTables;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
//SOS SOS SOS Doesnt work currently
public class AStarWithTimeTest {
    // !!!!!!!!gives error, expected T1 actual is null ,also there is like a 5 minute mistake, try to fix later!!!!!!!
    @Test
    public void testFindShortestPath() {
        // Setup the graph
        BusGraph graph = new BusGraph();
        graph.addEdge("A", "B", 600, "T1", "08:00:00", "08:10:00", "R1");
        graph.addEdge("B", "C", 600, "T1", "08:15:00", "08:25:00", "R1");
        graph.addEdge("A", "C", 1800, "T2", "08:00:00", "08:30:00", "R2");

        // Setup parent stops
        graph.setParentStop("A", "P1");
        graph.setParentStop("B", "P1");
        graph.setParentStop("C", "P2");

        // Setup addresses
        Map<String, Stop> addressMap = new HashMap<>();
        addressMap.put("A", new Stop("A", "Stop A", 50.850346, 5.688889));
        addressMap.put("B", new Stop("B", "Stop B", 50.850346, 5.688889));
        addressMap.put("C", new Stop("C", "Stop C", 50.850346, 5.688889));

        // Setup travel times
        Map<String, Map<String, Double>> travelTimeMap = new HashMap<>();
        Map<String, Double> fromA = new HashMap<>();
        fromA.put("B", 600.0);
        fromA.put("C", 1800.0);
        travelTimeMap.put("A", fromA);

        Map<String, Double> fromB = new HashMap<>();
        fromB.put("C", 600.0);
        travelTimeMap.put("B", fromB);

        // Setup start times
        Map<String, String> startTimes = new HashMap<>();
        startTimes.put("A", "08:00:00");

        // Find the shortest path
        List<AStarWithTime.PathNode> path = AStarWithTime.findShortestPath(graph, Collections.singletonList("A"), "C", startTimes, addressMap, travelTimeMap);

        // Verify the path
        assertNotNull(path);
        assertFalse(path.isEmpty());
        assertEquals(2, path.size());

        AStarWithTime.PathNode node1 = path.get(0);
        AStarWithTime.PathNode node2 = path.get(1);

        assertEquals("A", node1.previousStopId);
        assertEquals("T1", node1.tripId);
        assertEquals("08:00:00", node1.departureTime);
        assertEquals("08:10:00", node1.arrivalTime);
        assertEquals("R1", node1.routeId);

        assertEquals("B", node2.previousStopId);
        assertEquals("T1", node2.tripId);
        assertEquals("08:15:00", node2.departureTime);
        assertEquals("08:25:00", node2.arrivalTime);
        assertEquals("R1", node2.routeId);
    }
}