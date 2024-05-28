import dbTables.BusRoute;
import dbTables.BusStop;
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

        BusRoute shortestRoute = null;
        int shortestTime = Integer.MAX_VALUE;
        int bestStartStopId = -1;

        // Find the shortest route
        for (Stops startStop : closeToStartStops) {
            for (Stops endStop : closeToEndStops) {

                System.out.println("Start: " + startStop.getStopName());
                System.out.println("End: " + endStop.getStopName());
                System.out.println();

                String tripId = findShortestRoute(startStop.getStopId(), endStop.getStopId());
                if (tripId != null) {
                    BusRoute route = getAllStopsFromTripId(tripId);

                    assert route != null;

                    int tripTime = route.calculateTripTime();

                    if (tripTime < shortestTime) {
                        shortestTime = tripTime;
                        shortestRoute = route;
                        bestStartStopId = startStop.getStopId();
                    }
                }
            }
        }

        if (shortestRoute == null) {
            System.out.println("No route found");
        }

        // Remove all stops before the best start stop
        for (int i = 0; i < shortestRoute.getBusStops().size(); i++) {
            if(shortestRoute.getBusStops().get(i).getStopId() == bestStartStopId) {
                //Remove all stops before the best start stop
                shortestRoute.getBusStops().subList(0, i).clear();
                break;
            }
        }

        return shortestRoute;

    }

    public static List<Stops> findNearestBusStop(PostAddress address) {
        // Find nearest bus stop
        return fetchStopsByCoords(address.getLat(), address.getLon());
    }

    public static void main(String[] args) {
        PostAddress start = AddressFinder.getAddress("6229EN");
        PostAddress end = AddressFinder.getAddress("6216EG");

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