import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    final static String path = "data/distances.csv";
    static HashMap<String, PostAddress> postAdresses = Utilities.initPostAddressMap(path);

    public static void main(String[] args) {
        Graph graph = new Graph();
        try {
            graph.loadGraphFromCSV("data/graph100.csv");

            // Initialize the PathFinder with the graph
            PathFinder pathFinder = new PathFinder(graph);

            // Find and print the path from start to end
            List<PostAddress> path = pathFinder.findPath(postAdresses.get("6211RE"), postAdresses.get("6219NW"));
            if (path.isEmpty()) {
                System.out.println("No path could be found between the specified addresses.");
            } else {
                System.out.println("Path found: " + path.stream()
                        .map(PostAddress::getPostalCode)
                        .collect(Collectors.joining(" -> ")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
