package dbTables;

import java.util.*;

public class BusGraph {
    final Map<String, List<Edge>> adjList = new HashMap<>();

    // Method to add an edge to the graph
    public void addEdge(String fromStopId, String toStopId, int weight, String tripId, String departureTime, String arrivalTime) {
        adjList.computeIfAbsent(fromStopId, k -> new ArrayList<>()).add(new Edge(toStopId, weight, tripId, departureTime, arrivalTime));
    }

    // Method to get edges from a given stop
    public List<Edge> getEdges(String stopId) {
        return adjList.getOrDefault(stopId, new ArrayList<>());
    }

    // Edge class representing the connection between stops
    public static class Edge {
        String toStopId;
        int weight; // Travel time in seconds
        String tripId;
        String departureTime;
        String arrivalTime;

        public Edge(String toStopId, int weight, String tripId, String departureTime, String arrivalTime) {
            this.toStopId = toStopId;
            this.weight = weight;
            this.tripId = tripId;
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
        }
    }
}