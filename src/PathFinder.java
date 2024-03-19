import java.util.*;

//This is an implementation of Dijkstra's alg

public class PathFinder {
    private Graph graph;

    public PathFinder(Graph graph) {
        this.graph = graph;
    }

    // Method to find the closest PostAdress in the graph to a given PostAdress
    private PostAdress findClosestAddressInGraph(PostAdress target) {
        PostAdress closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (PostAdress node : graph.getAdjList().keySet()) {
            double distance = PostAdress.basicDistances(target, node);
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = node;
            }
        }
        return closest; // May return null if the graph is empty
    }

    public List<PostAdress> findPath(PostAdress start, PostAdress end) {
        PostAdress actualStart = start;
        PostAdress actualEnd = end;

        if (!graph.getAdjList().containsKey(start)) {
            actualStart = findClosestAddressInGraph(start);
            System.out.println("Original start address not in graph. Using closest address: " + actualStart.getPostalCode());
        }
        if (!graph.getAdjList().containsKey(end)) {
            actualEnd = findClosestAddressInGraph(end);
            System.out.println("Original end address not in graph. Using closest address: " + actualEnd.getPostalCode());
        }

        // Rest of the pathfinding algorithm remains unchanged
        Set<PostAdress> settledNodes = new HashSet<>();
        Set<PostAdress> unsettledNodes = new HashSet<>();
        Map<PostAdress, PostAdress> predecessors = new HashMap<>();
        Map<PostAdress, Double> distance = new HashMap<>();

        distance.put(actualStart, 0.0);
        unsettledNodes.add(actualStart);

        while (!unsettledNodes.isEmpty()) {
            PostAdress currentNode = getLowestDistanceNode(unsettledNodes, distance);
            unsettledNodes.remove(currentNode);
            for (PostAdress neighbor : graph.getAdjList().getOrDefault(currentNode, new HashSet<>())) {
                double edgeDistance = PostAdress.basicDistances(currentNode, neighbor);
                if (!settledNodes.contains(neighbor)) {
                    calculateMinimumDistance(neighbor, edgeDistance, currentNode, distance, predecessors);
                    unsettledNodes.add(neighbor);
                }
            }
            settledNodes.add(currentNode);
        }

        return getPath(actualEnd, predecessors);
    }

    private PostAdress getLowestDistanceNode(Set<PostAdress> unsettledNodes, Map<PostAdress, Double> distances) {
        PostAdress lowestDistanceNode = null;
        double lowestDistance = Double.MAX_VALUE;
        for (PostAdress node : unsettledNodes) {
            double nodeDistance = distances.get(node);
            if (nodeDistance < lowestDistance) {
                lowestDistance = nodeDistance;
                lowestDistanceNode = node;
            }
        }
        return lowestDistanceNode;
    }

    private void calculateMinimumDistance(PostAdress evaluationNode, double edgeDistance, PostAdress sourceNode, Map<PostAdress, Double> distance, Map<PostAdress, PostAdress> predecessors) {
        Double sourceDistance = distance.get(sourceNode);
        if (sourceDistance + edgeDistance < distance.getOrDefault(evaluationNode, Double.MAX_VALUE)) {
            distance.put(evaluationNode, sourceDistance + edgeDistance);
            predecessors.put(evaluationNode, sourceNode);
        }
    }

    private List<PostAdress> getPath(PostAdress target, Map<PostAdress, PostAdress> predecessors) {
        List<PostAdress> path = new ArrayList<>();
        PostAdress step = target; 
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
