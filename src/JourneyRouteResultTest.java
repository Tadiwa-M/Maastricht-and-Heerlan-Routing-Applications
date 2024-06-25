package dbTables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

public class JourneyRouteResultTest {

    private JourneyRoute route;
    private List<AStarWithTime.PathNode> path;
    private int travelTime;
    private String startTime;

    @BeforeEach
    public void setUp() {
        route = new JourneyRoute("startStop", "endStop");
        path = Arrays.asList(
                new AStarWithTime.PathNode("stop1", "trip1", "08:00:00", "08:05:00", "route1"),
                new AStarWithTime.PathNode("stop2", "trip2", "08:10:00", "08:15:00", "route2")
        );
        travelTime = 15;  // in minutes
        startTime = "08:00:00";
    }

    @Test
    public void testConstructorAndGetters() {
        JourneyRouteResult journeyRouteResult = new JourneyRouteResult(route, path, travelTime, startTime);

        assertEquals(route, journeyRouteResult.route);
        assertEquals(path, journeyRouteResult.path);
        assertEquals(travelTime, journeyRouteResult.travelTime);
        assertEquals(startTime, journeyRouteResult.startTime);
    }
}