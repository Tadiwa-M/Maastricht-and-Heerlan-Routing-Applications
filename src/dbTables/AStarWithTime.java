package dbTables;

import java.util.*;

import static dbTables.PostAddress.EARTH_RADIUS_KM;
import static dbTables.PostAddress.degToRad;

public class AStarWithTime {
    private static final int TRANSFER_PENALTY = 300; // 5 minutes penalty for transfer

    public static List<PathNode> findShortestPath(BusGraph graph, String startStopId, String endStopId, String startTime, Map<String, Stop> addressMap) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fScore));
        Map<String, Double> gScore = new HashMap<>();
        Map<String, Double> fScore = new HashMap<>();
        Map<String, PathNode> cameFrom = new HashMap<>();
        Map<String, String> arrivalTimes = new HashMap<>();
        Map<String, String> tripIds = new HashMap<>();

        // Initialize start node
        gScore.put(startStopId, 0.0);
        fScore.put(startStopId, heuristic(startStopId, endStopId, addressMap));
        arrivalTimes.put(startStopId, startTime);
        tripIds.put(startStopId, null); // No trip ID at the start

        openSet.add(new Node(startStopId, fScore.get(startStopId), startTime));

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            // Check if we've reached the goal
            if (current.stopId.equals(endStopId)) {
                return reconstructPath(cameFrom, current.stopId);
            }

            for (BusGraph.Edge neighbor : graph.getEdges(current.stopId)) {
                if (neighbor.toStopId == null) {
                    System.err.println("Encountered null neighbor toStopId for current stop: " + current.stopId);
                    continue;
                }

                String currentArrivalTime = arrivalTimes.get(current.stopId);

                // Skip if we arrive after the neighbor's departure time
                if (currentArrivalTime != null && timeToSeconds(currentArrivalTime) > timeToSeconds(neighbor.departureTime)) {
                    continue;
                }

                double tentativeGScore = gScore.getOrDefault(current.stopId, Double.POSITIVE_INFINITY) + neighbor.weight;

                // Apply transfer penalty if the trip ID changes
                if (tripIds.get(current.stopId) != null && !tripIds.get(current.stopId).equals(neighbor.tripId)) {
                    tentativeGScore += TRANSFER_PENALTY;
                }

                // Check if 'neighbor.toStopId' is not null and ensure it is initialized in the gScore map
                if (tentativeGScore < gScore.getOrDefault(neighbor.toStopId, Double.POSITIVE_INFINITY)) {
                    cameFrom.put(neighbor.toStopId, new PathNode(current.stopId, neighbor.tripId, neighbor.departureTime, neighbor.arrivalTime));
                    gScore.put(neighbor.toStopId, tentativeGScore);
                    fScore.put(neighbor.toStopId, tentativeGScore + heuristic(neighbor.toStopId, endStopId, addressMap));
                    arrivalTimes.put(neighbor.toStopId, neighbor.arrivalTime);
                    tripIds.put(neighbor.toStopId, neighbor.tripId);
                    openSet.add(new Node(neighbor.toStopId, fScore.get(neighbor.toStopId), neighbor.arrivalTime));
                }
            }
        }

        return Collections.emptyList(); // Return an empty path if no path is found
    }

    private static double heuristic(String fromStopId, String toStopId, Map<String, Stop> addressMap) {
        Stop fromAddress = addressMap.get(fromStopId);
        Stop toAddress = addressMap.get(toStopId);

        if (fromAddress == null) {
            System.err.println("Missing address data for start stop: " + fromStopId);
            return Double.POSITIVE_INFINITY;
        }

        if (toAddress == null) {
            System.err.println("Missing address data for end stop: " + toStopId);
            return Double.POSITIVE_INFINITY;
        }

        return basicDistances(fromAddress, toAddress);
    }

    public static double basicDistances(Stop start, Stop end) {
        // Convert the latitudes and longitudes from decimal degrees to radians
        double lat1Rad = degToRad(start.getStopLat());
        double lon1Rad = degToRad(start.getStopLon());
        double lat2Rad = degToRad(end.getStopLat());
        double lon2Rad = degToRad(end.getStopLon());

        // Haversine formula
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    private static List<PathNode> reconstructPath(Map<String, PathNode> cameFrom, String current) {
        List<PathNode> path = new LinkedList<>();
        while (cameFrom.containsKey(current)) {
            PathNode node = cameFrom.get(current);
            path.add(node);
            current = node.previousStopId;
        }
        Collections.reverse(path);
        return path;
    }

    private static int timeToSeconds(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        return hours * 3600 + minutes * 60 + seconds;
    }

    private static class Node {
        String stopId;
        double fScore;
        String arrivalTime;

        Node(String stopId, double fScore, String arrivalTime) {
            this.stopId = stopId;
            this.fScore = fScore;
            this.arrivalTime = arrivalTime;
        }
    }

    public static class PathNode {
        public String previousStopId;
        public String tripId;
        public String departureTime;
        public String arrivalTime;

        public PathNode(String previousStopId, String tripId, String departureTime, String arrivalTime) {
            this.previousStopId = previousStopId;
            this.tripId = tripId;
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
        }

        @Override
        public String toString() {
            return "From " + previousStopId + " to " + tripId + " (Departs at: " + departureTime + ", Arrives at: " + arrivalTime + ")";
        }
    }
}