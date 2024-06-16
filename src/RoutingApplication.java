import dbTables.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RoutingApplication {

    public static void main(String[] args) {
        try {
            RouteResult result = findBestRoute("6218BK", "6229EN", "08:00:00");
            if (result != null) {
                printPathDetails(result.path, result.route.startStopId, result.route.endStopId, "08:00:00");
            } else {
                System.out.println("No path found between the given stops.");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static RouteResult findBestRoute(String startPostalCode, String endPostalCode, String startTime) throws Exception {
        BusGraph graph = GTFSLoader.loadGraph();
        if (graph == null) {
            throw new Exception("Failed to load graph");
        }

        Map<String, Stop> addressMap = GTFSLoader.fetchAllAddresses();
        if (addressMap.isEmpty()) {
            throw new Exception("Failed to load addresses");
        }

        Map<String, String> routeNames = GTFSLoader.fetchAllRouteNames();
        if (routeNames.isEmpty()) {
            throw new Exception("Failed to load route names");
        }

        List<Stops> startStops = getStopsByPostalCode(startPostalCode);
        List<Stops> endStops = getStopsByPostalCode(endPostalCode);

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<RouteResult>> results = new ArrayList<>();

        for (Stops start : startStops) {
            for (Stops end : endStops) {
                results.add(executor.submit(new PathFinderTask(graph, addressMap, routeNames, start, end, startTime)));
            }
        }

        Route bestRoute = null;
        List<AStarWithTime.PathNode> bestPath = null;
        int bestTime = Integer.MAX_VALUE;

        for (Future<RouteResult> result : results) {
            try {
                RouteResult routeResult = result.get();
                if (routeResult != null && routeResult.travelTime < bestTime) {
                    bestRoute = routeResult.route;
                    bestPath = routeResult.path;
                    bestTime = routeResult.travelTime;
                }
            } catch (Exception e) {
                System.err.println("Error processing routing result: " + e.getMessage());
            }
        }

        executor.shutdown();

        if (bestPath != null) {
            return new RouteResult(bestRoute, bestPath, bestTime);
        } else {
            return null;
        }
    }

    private static List<Stops> getStopsByPostalCode(String postalCode) {
        PostAddress address = AddressFinder.getAddress(postalCode);
        if (address == null) {
            System.err.println("No address found for postal code: " + postalCode);
            return Collections.emptyList();
        }
        return BusRouteFinder.findNearestBusStop(address);
    }

    private static void printPathDetails(List<AStarWithTime.PathNode> path, String startStopId, String endStopId, String startTime) {
        System.out.println("Shortest path from " + startStopId + " to " + endStopId + " starting at " + startTime + ":");

        String previousTripId = null;
        String firstDepartureTime = startTime;
        String finalArrivalTime = null;
        int totalTravelTimeInSeconds;

        for (int i = 0; i < path.size(); i++) {
            AStarWithTime.PathNode node = path.get(i);
            Stop stop = GTFSLoader.getStopDetails(node.previousStopId);

            if (previousTripId == null || !previousTripId.equals(node.tripId)) {
                if (previousTripId != null) {
                    System.out.println("Transfer to Route " + node.routeId + " at Stop: " + (stop != null ? stop.getStopName() : node.previousStopId));
                }
                previousTripId = node.tripId;
            }

            String nextStopId = (i + 1 < path.size()) ? path.get(i + 1).previousStopId : endStopId;

            // Print all intermediate stops for this segment using GTFS times
            List<IntermediateStop> segmentStops = GTFSLoader.getIntermediateStopsForTrip(node.tripId, node.previousStopId, nextStopId);
            for (IntermediateStop segmentStop : segmentStops) {
                System.out.println("Stop: " + segmentStop.getStopName() +
                        ", Arrival: " + segmentStop.getArrivalTime() +
                        ", Departure: " + segmentStop.getDepartureTime() +
                        ", Route: " + node.routeId +
                        ", Trip id: " + node.tripId);
            }

            finalArrivalTime = node.arrivalTime;
        }

        // Print final stop name
        Stop stop = GTFSLoader.getStopDetails(endStopId);
        System.out.println("Stop: " + (stop != null ? stop.getStopName() : endStopId) + ", Arrival: " + finalArrivalTime);

        // Calculate total travel time from the startTime to the final arrival time
        if (finalArrivalTime != null) {
            totalTravelTimeInSeconds = AStarWithTime.timeToSeconds(finalArrivalTime) - AStarWithTime.timeToSeconds(firstDepartureTime);

            int hours = totalTravelTimeInSeconds / 3600;
            int minutes = (totalTravelTimeInSeconds % 3600) / 60;
            int seconds = totalTravelTimeInSeconds % 60;

            System.out.println("Total travel time: " + String.format("%02d:%02d:%02d", hours, minutes, seconds));
        } else {
            System.out.println("Total travel time: N/A");
        }
    }

    private static int getTotalTravelTime(List<AStarWithTime.PathNode> path, String startTime) {
        String finalArrivalTime = path.get(path.size() - 1).arrivalTime;
        return AStarWithTime.timeToSeconds(finalArrivalTime) - AStarWithTime.timeToSeconds(startTime);
    }

    public static class Route {
        String startStopId;
        String endStopId;

        public Route(String startStopId, String endStopId) {
            this.startStopId = startStopId;
            this.endStopId = endStopId;
        }
    }

    public static class RouteResult {
        Route route;
        List<AStarWithTime.PathNode> path;
        int travelTime;

        public RouteResult(Route route, List<AStarWithTime.PathNode> path, int travelTime) {
            this.route = route;
            this.path = path;
            this.travelTime = travelTime;
        }
    }

    static class PathFinderTask implements Callable<RouteResult> {
        private final BusGraph graph;
        private final Map<String, Stop> addressMap;
        private final Map<String, String> routeNames;
        private final Stops start;
        private final Stops end;
        private final String startTime;

        public PathFinderTask(BusGraph graph, Map<String, Stop> addressMap, Map<String, String> routeNames, Stops start, Stops end, String startTime) {
            this.graph = graph;
            this.addressMap = addressMap;
            this.routeNames = routeNames;
            this.start = start;
            this.end = end;
            this.startTime = startTime;
        }

        @Override
        public RouteResult call() throws Exception {
            if (!addressMap.containsKey(start.getStopId())) {
                System.err.println("Missing address data for start stop: " + start.getStopId());
                return null;
            }

            if (!addressMap.containsKey(end.getStopId())) {
                System.err.println("Missing address data for end stop: " + end.getStopId());
                return null;
            }

            List<AStarWithTime.PathNode> path = AStarWithTime.findShortestPath(graph, start.getStopId(), end.getStopId(), startTime, addressMap);
            if (!path.isEmpty()) {
                for (AStarWithTime.PathNode node : path) {
                    Stop stop = GTFSLoader.getStopDetails(node.previousStopId);
                    if (stop == null) {
                        System.err.println("Stop details not found for stopId: " + node.previousStopId);
                        return null;
                    }
                    if (!routeNames.containsKey(node.routeId)) {
                        System.err.println("Route details not found for routeId: " + node.routeId);
                        return null;
                    }
                }
                int travelTime = getTotalTravelTime(path, startTime);
                return new RouteResult(new Route(start.getStopId(), end.getStopId()), path, travelTime);
            }
            return null;
        }
    }
}