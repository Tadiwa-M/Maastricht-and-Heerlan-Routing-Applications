import dbTables.*;

import java.util.List;

import static dbTables.BusRoute.getDuration;
import static dbTables.dbManager.*;

public class BusRouteFinder {
    private final PostAddress start;
    private final PostAddress end;
    private String startTime;

    public BusRouteFinder(PostAddress start, PostAddress end) {
        this.start = start;
        this.end = end;
    }

    public BusRouteFinder(PostAddress start, PostAddress end, String startTime) {
        this.start = start;
        this.end = end;
        this.startTime = startTime;
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

    public TransferRoute findShortestTransferRoute() {
        // Find bus stops near start and end
        List<Stops> startStops = findNearestBusStop(start);
        List<Stops> endStops = findNearestBusStop(end);

        if(startStops.isEmpty() || endStops.isEmpty()) {
            System.out.println("No bus stops found near start or end");
            return null;
        }

        // Find the shortest route
        Route bestRoute = findBestRoute(startStops, endStops, startTime);

        if (bestRoute != null) {
            BusRoute fromRoute = getStopsFromRoute(bestRoute.getFromRoute());
            BusRoute toRoute = getStopsFromRoute(bestRoute.getToRoute());
            return new TransferRoute(fromRoute, toRoute, startTime);
        }
        else {
            System.out.println("No transfer route found");
            return null;
        }
    }

    public static void main(String[] args) {
        BusRouteFinder busRouteFinder = new BusRouteFinder(AddressFinder.getAddress("6216EG"), AddressFinder.getAddress("6229EN"), "11:00:00");
        TransferRoute shortestTransferRoute = busRouteFinder.findShortestTransferRoute();

        if (shortestTransferRoute != null) {
            System.out.println("Shortest transfer route found");
            System.out.println("Trip time: " + shortestTransferRoute.calculateTripTime() + " minutes");

            for (BusStop busStop : shortestTransferRoute.getStartRoute().getBusStops()) {
                System.out.println(busStop.getStopName() + " " + busStop.getDepartureTime());
            }
            System.out.println("Transfer line: " + shortestTransferRoute.getTransferLine());
            for (BusStop busStop : shortestTransferRoute.getEndRoute().getBusStops()) {
                System.out.println(busStop.getStopName() + " " + busStop.getDepartureTime());
            }
        }
    }

    public static void test() {
        PostAddress start = AddressFinder.getAddress("6216EG");
        PostAddress end = AddressFinder.getAddress("6229EN");

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
