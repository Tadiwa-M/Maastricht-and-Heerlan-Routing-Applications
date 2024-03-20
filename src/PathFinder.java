import java.util.*;

//This is an implementation of Dijkstra's alg

public class PathFinder {
    private Graph graph;

    public PathFinder(Graph graph) {
        this.graph = graph;
    }

    // Method to find the closest PostAdress in the graph to a given PostAdress
    private PostAddress findClosestAddressInGraph(PostAddress target) {
        PostAddress closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (PostAddress node : graph.getAdjList().keySet()) {
            double distance = PostAddress.basicDistances(target, node);
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = node;
            }
        }
        return closest; // May return null if the graph is empty
    }

    public List<PostAddress> findPath(PostAddress start, PostAddress end) {
        PostAddress actualStart = start;
        PostAddress actualEnd = end;

        if (!graph.getAdjList().containsKey(start)) {
            actualStart = findClosestAddressInGraph(start);
            System.out.println("Original start address not in graph. Using closest address: " + actualStart.getPostalCode());
        }
        if (!graph.getAdjList().containsKey(end)) {
            actualEnd = findClosestAddressInGraph(end);
            System.out.println("Original end address not in graph. Using closest address: " + actualEnd.getPostalCode());
        }

        // Rest of the pathfinding algorithm remains unchanged
        Set<PostAddress> settledNodes = new HashSet<>();
        Set<PostAddress> unsettledNodes = new HashSet<>();
        Map<PostAddress, PostAddress> predecessors = new HashMap<>();
        Map<PostAddress, Double> distance = new HashMap<>();

        distance.put(actualStart, 0.0);
        unsettledNodes.add(actualStart);

        while (!unsettledNodes.isEmpty()) {
            PostAddress currentNode = getLowestDistanceNode(unsettledNodes, distance);
            unsettledNodes.remove(currentNode);
            for (PostAddress neighbor : graph.getAdjList().getOrDefault(currentNode, new HashSet<>())) {
                double edgeDistance = PostAddress.basicDistances(currentNode, neighbor);
                if (!settledNodes.contains(neighbor)) {
                    calculateMinimumDistance(neighbor, edgeDistance, currentNode, distance, predecessors);
                    unsettledNodes.add(neighbor);
                }
            }
            settledNodes.add(currentNode);
        }

        return getPath(actualEnd, predecessors);
    }

    private PostAddress getLowestDistanceNode(Set<PostAddress> unsettledNodes, Map<PostAddress, Double> distances) {
        PostAddress lowestDistanceNode = null;
        double lowestDistance = Double.MAX_VALUE;
        for (PostAddress node : unsettledNodes) {
            double nodeDistance = distances.get(node);
            if (nodeDistance < lowestDistance) {
                lowestDistance = nodeDistance;
                lowestDistanceNode = node;
            }
        }
        return lowestDistanceNode;
    }

    private void calculateMinimumDistance(PostAddress evaluationNode, double edgeDistance, PostAddress sourceNode, Map<PostAddress, Double> distance, Map<PostAddress, PostAddress> predecessors) {
        Double sourceDistance = distance.get(sourceNode);
        if (sourceDistance + edgeDistance < distance.getOrDefault(evaluationNode, Double.MAX_VALUE)) {
            distance.put(evaluationNode, sourceDistance + edgeDistance);
            predecessors.put(evaluationNode, sourceNode);
        }
    }

    private List<PostAddress> getPath(PostAddress target, Map<PostAddress, PostAddress> predecessors) {
        List<PostAddress> path = new ArrayList<>();
        PostAddress step = target;
        if (predecessors.get(step) == null) {
            return path;
        }
        path.add(step);
        while (predecessors.get(step) != null) {
            step = predecessors.get(step);
            path.add(step);
        }
        Collections.reverse(path);
        return path;
    }
}
