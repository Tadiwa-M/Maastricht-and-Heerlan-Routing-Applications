package dbTables;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JourneyRouteTest {

    @Test
    public void testConstructorAndGetters() {
        String startStopId = "startStop";
        String endStopId = "endStop";
        JourneyRoute journeyRoute = new JourneyRoute(startStopId, endStopId);

        assertEquals(startStopId, journeyRoute.startStopId);
        assertEquals(endStopId, journeyRoute.endStopId);
    }
}