import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    final static String path = "data/distances.csv";
    static HashMap<String, PostAddress> postAdresses = Utilities.initPostAddressMap(path);

    public static void main(String[] args) {
        PostAddress start = postAdresses.get("6216EG");
        PostAddress end = postAdresses.get("6211EF");
        calculateDistance(start, end, 0);

    }

    public static void calculateDistance(PostAddress start, PostAddress end, int vehicleType) {
/*        Graph graph = new Graph();
//            graph.loadGraphFromCSV("data/graph100.csv");
        graph.createGraph();

        // Initialize the PathFinder with the graph
        PathFinder pathFinder = new PathFinder(graph);

        // Find and print the path from start to end
        List<PostAddress> path = pathFinder.findPath(start, end);
        if (path.isEmpty()) {
            System.out.println("No path could be found between the specified addresses.");
        } else {
            System.out.println("Path found: " + path.stream()
                    .map(PostAddress::getPostalCode)
                    .collect(Collectors.joining(" -> ")));
        }

 */
        ShortestPathFinder shortestPathFinder = new ShortestPathFinder();

        List<PostAddress> path = shortestPathFinder.findPath(start, end);
        double distance = shortestPathFinder.getDistance(start, end);

        if (path.isEmpty()) {
            System.out.println("No path could be found between the specified addresses.");
        } else {
            System.out.println("Path found: " + path.stream()
                    .map(PostAddress::getPostalCode)
                    .collect(Collectors.joining(" -> ")));

            System.out.println("Distance: " + distance);
        }
    }
}
