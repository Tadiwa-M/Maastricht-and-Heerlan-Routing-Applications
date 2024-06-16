import dbTables.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RoutingApplication {
    public static void main(String[] args) {
        BusGraph graph = GTFSLoader.loadGraph();
        if (graph == null) {
            System.err.println("Failed to load graph");
            return;
        }

        Map<String, Stop> addressMap = GTFSLoader.fetchAllAddresses();
        if (addressMap.isEmpty()) {
            System.err.println("Failed to load addresses");
            return;
        }

        try {
            List<Stops> startStops = getStopsByPostalCode("6216EG");
            List<Stops> endStops = getStopsByPostalCode("6229EN");
            String startTime = "08:00:00";

            Route bestRoute = null;
            List<AStarWithTime.PathNode> bestPath = null;

            for (Stops start : startStops) {
                for (Stops end : endStops) {
                    if (!addressMap.containsKey(start.getStopId())) {
                        System.err.println("Missing address data for start stop: " + start.getStopId());
                        continue;
                    }

                    if (!addressMap.containsKey(end.getStopId())) {
                        System.err.println("Missing address data for end stop: " + end.getStopId());
                        continue;
                    }

                    List<AStarWithTime.PathNode> path = AStarWithTime.findShortestPath(graph, start.getStopId(), end.getStopId(), startTime, addressMap);
                    if (!path.isEmpty()) {
                        if (bestRoute == null || getTotalTravelTime(path, startTime) < getTotalTravelTime(bestPath, startTime)) {
                            bestRoute = new Route(start.getStopId(), end.getStopId());
                            bestPath = path;
                        }
                    }
                }
            }

            if (bestPath != null) {
                printPathDetails(bestPath, bestRoute.startStopId, bestRoute.endStopId, startTime);
            } else {
                System.out.println("No path found between the given stops.");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
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
        int totalTravelTimeInSeconds = 0;

        for (AStarWithTime.PathNode node : path) {
            Stop stop = GTFSLoader.getStopDetails(node.previousStopId);
            String transferInfo = "";

            if (previousTripId != null && !previousTripId.equals(node.tripId)) {
                transferInfo = " [Transfer to Route " + node.routeId + "]";
            }

            System.out.println("Stop: " + (stop != null ? stop.getStopName() : node.previousStopId) +
                    ", Departure: " + node.departureTime +
                    ", Arrival: " + node.arrivalTime +
                    ", Route: " + node.routeId +
                    transferInfo);

            finalArrivalTime = node.arrivalTime;
            previousTripId = node.tripId;
        }

        // Calculate total travel time from the startTime to the final arrival time
        if (finalArrivalTime != null) {
            totalTravelTimeInSeconds = AStarWithTime.timeToSeconds(finalArrivalTime) - AStarWithTime.timeToSeconds(startTime);

            int hours = totalTravelTimeInSeconds / 3600;
            int minutes = (totalTravelTimeInSeconds % 3600) / 60;
            int seconds = totalTravelTimeInSeconds % 60;

            System.out.println("Total travel time: " + String.format("%02d:%02d:%02d", hours, minutes, seconds));
        }
    }

    private static int getTotalTravelTime(List<AStarWithTime.PathNode> path, String startTime) {
        String finalArrivalTime = path.get(path.size() - 1).arrivalTime;
        return AStarWithTime.timeToSeconds(finalArrivalTime) - AStarWithTime.timeToSeconds(startTime);
    }

    public static class Route {
        String startStopId;
        String endStopId;

        Route(String startStopId, String endStopId) {
            this.startStopId = startStopId;
            this.endStopId = endStopId;
        }
    }
}