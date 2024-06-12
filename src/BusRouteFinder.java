import dbTables.BusRoute;
import dbTables.BusStop;
import dbTables.PostAddress;
import dbTables.Stops;

import java.util.List;

import static dbTables.dbManager.*;

public class BusRouteFinder {
    PostAddress start;
    PostAddress end;

    public BusRouteFinder(PostAddress start, PostAddress end) {
        this.start = start;
        this.end = end;
    }

    public BusRoute findShortestBusRoute() {
        // Find bus stops near start and end
        List<Stops> closeToStartStops = findNearestBusStop(start);
        List<Stops> closeToEndStops = findNearestBusStop(end);

        if(closeToStartStops.isEmpty() || closeToEndStops.isEmpty()) {
            System.out.println("No bus stops found near start or end");
            return null;
        }


        // Find the shortest route
        RouteDetails routeDetails = findShortestRoute(closeToStartStops, closeToEndStops);
        if (routeDetails != null) {
            BusRoute route = getAllStopsFromTripId(routeDetails.getTripId(), routeDetails.getStartStopSequence(), routeDetails.getEndStopSequence());

            assert route != null;

            return route;
        }
        else {
            System.out.println("No route found");
            return null;
        }
    }

    public static List<Stops> findNearestBusStop(PostAddress address) {
        // Find nearest bus stop
        return fetchStopsByCoords(address.getLat(), address.getLon());
    }

    public static void main(String[] args) {
        PostAddress start = AddressFinder.getAddress("6229EN");
        PostAddress end = AddressFinder.getAddress("6229HD");

        BusRouteFinder busRouteFinder = new BusRouteFinder(start, end);
        BusRoute shortestRoute = busRouteFinder.findShortestBusRoute();

        if (shortestRoute != null) {
            System.out.println("Shortest route found");
            System.out.println("Trip time: " + shortestRoute.calculateTripTime() + " minutes");

            for (BusStop busStop : shortestRoute.getBusStops()) {
                System.out.println(busStop.getStopName());
            }
        }
    }
}
