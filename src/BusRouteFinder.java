import dbTables.*;

import java.util.List;

import static dbTables.BusRoute.getDuration;
import static dbTables.dbManager.*;

public class BusRouteFinder {
    private final PostAddress start;
    private final PostAddress end;

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
        String startPostCode = "6216EG";
        String endPostCode = "6229EN";

        PostAddress start = AddressFinder.getAddress(startPostCode);
        PostAddress end = AddressFinder.getAddress(endPostCode);

        List<String> startStops = findNearestBusStop(start).stream().map(Stops::getStopId).toList();
        List<String> endStops = findNearestBusStop(end).stream().map(Stops::getStopId).toList();

        String startTime = "11:00:00";

        // Assume we pick the first stop from the list of nearest stops for simplicity
        if (!startStops.isEmpty() && !endStops.isEmpty()) {
            Route bestRoute = findBestRoute(startStops, endStops, startTime);
            if (bestRoute != null) {
                System.out.println("Best route found");

                System.out.println("Total Time: " + getDuration(startTime, bestRoute.getArrivalTime()).toMinutes());
                System.out.println("Trip time: " + bestRoute.getTotalTravelTime() + " minutes");
                BusRoute fromRoute = getStopsFromRoute(bestRoute.getFromRoute());
                BusRoute toRoute = getStopsFromRoute(bestRoute.getToRoute());
                for (BusStop busStop : fromRoute.getBusStops()) {
                    System.out.println(busStop.getStopName() + " " + busStop.getRouteName() + " " + busStop.getDepartureTime());
                }
                System.out.println("Change at: " + (toRoute.getBusStops().get(0)).getStopName());
                for (BusStop busStop : toRoute.getBusStops()) {
                    System.out.println(busStop.getStopName() + " " + busStop.getRouteName() + " " + busStop.getDepartureTime());
                }
            }
            else {
                System.out.println("No route found");
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
