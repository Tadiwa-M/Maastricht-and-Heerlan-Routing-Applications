import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import dbTables.PostAddress;
import dbTables.DirectRoute;
import dbTables.BusRoute;

//SOS!!!!!!!!!!!!!!! CAREFUL WHEN RUNNING IT, IT MIGHT BREAK EVERYTHING :,(
//P.S. IF ANYTHING ELSE BREAKS I APOLOGIZE IN ADVANCE
public class BusRouteFinderTest {

    private PostAddress startAddress;
    private PostAddress endAddress;

    @Before
    public void setUp() {
        // Initialize the start and end addresses for the tests
        startAddress = AddressFinder.getAddress("6213GA"); // Replace with actual postal code for testing
        endAddress = AddressFinder.getAddress("6211JH");   // Replace with actual postal code for testing
    }

    @Test
    public void testFindShortestDirectBusRoute() {
        BusRouteFinder finder = new BusRouteFinder(startAddress, endAddress);
        DirectRoute route = finder.findShortestDirectBusRoute();
        assertNotNull("Should find a direct route between the given addresses.", route);
    }


    @Test
    public void testFindOverallShortestRoute() {
        BusRouteFinder finder = new BusRouteFinder(startAddress, endAddress);
        BusRoute route = finder.findOverallShortestRoute();
        assertNotNull("Should find the overall shortest route between the given addresses.", route);
    }

    @Test
    public void testNoRouteBetweenStops() {
        // Initialize with postal codes that do not have a direct or transfer route between them
        PostAddress remoteStart = AddressFinder.getAddress("9999ZZ"); // Replace with an invalid or remote postal code
        PostAddress remoteEnd = AddressFinder.getAddress("8888YY");   // Replace with an invalid or remote postal code

        BusRouteFinder finder = new BusRouteFinder(remoteStart, remoteEnd);
        BusRoute route = finder.findOverallShortestRoute();
        assertNull("Should not find a route between the given remote addresses.", route);
    }
}