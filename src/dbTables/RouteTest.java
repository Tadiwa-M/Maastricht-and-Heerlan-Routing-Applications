package dbTables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RouteTest {

    private Route directRoute;
    private Route fromRoute;
    private Route toRoute;
    private Route transferRoute;

    @BeforeEach
    public void setUp() {
        directRoute = new Route("trip1", "route1", "R1", "Route 1", "08:00:00", "08:30:00", "stop1", "stop2");

        fromRoute = new Route("trip2", "route2", "R2", "Route 2", "09:00:00", "09:20:00", "stop3", "stop4");
        toRoute = new Route("trip3", "route3", "R3", "Route 3", "09:30:00", "10:00:00", "stop5", "stop6");
        transferRoute = new Route(fromRoute, toRoute);
    }

    @Test
    public void testDirectRouteConstructorAndGetters() {
        assertEquals("trip1", directRoute.getTripId());
        assertEquals("route1", directRoute.getRouteId());
        assertEquals("R1", directRoute.getRouteShortName());
        assertEquals("Route 1", directRoute.getRouteLongName());
        assertEquals("08:00:00", directRoute.getDepartureTime());
        assertEquals("08:30:00", directRoute.getArrivalTime());
        assertEquals("stop1", directRoute.getStartStopId());
        assertEquals("stop2", directRoute.getEndStopId());
    }

    @Test
    public void testTransferRouteConstructorAndGetters() {
        assertEquals("trip2", transferRoute.getTripId());
        assertEquals("route2", transferRoute.getRouteId());
        assertEquals("R2", transferRoute.getRouteShortName());
        assertEquals("Route 2", transferRoute.getRouteLongName());
        assertEquals("09:00:00", transferRoute.getDepartureTime());
        assertEquals("10:00:00", transferRoute.getArrivalTime()); // From the second segment
        assertEquals("stop3", transferRoute.getStartStopId());
        assertEquals("stop6", transferRoute.getEndStopId());
    }

    @Test
    public void testGetTotalTravelTime() {
        long travelTime = transferRoute.getTotalTravelTime();
        assertEquals(60, travelTime); // 60 minutes from 09:00:00 to 10:00:00
    }

    @Test
    public void testToString() {
        String expectedDirectRouteString = "Direct Route{tripId='trip1', routeId='route1', routeShortName='R1', routeLongName='Route 1', departureTime='08:00:00', arrivalTime='08:30:00'}";
        assertEquals(expectedDirectRouteString, directRoute.toString());

        String expectedTransferRouteString = "Route with transfer: \n  From: Direct Route{tripId='trip2', routeId='route2', routeShortName='R2', routeLongName='Route 2', departureTime='09:00:00', arrivalTime='09:20:00'}\n  To: Direct Route{tripId='trip3', routeId='route3', routeShortName='R3', routeLongName='Route 3', departureTime='09:30:00', arrivalTime='10:00:00'}";
        assertEquals(expectedTransferRouteString, transferRoute.toString());
    }
}