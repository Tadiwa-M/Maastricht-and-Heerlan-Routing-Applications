import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;

public class Graph {
    private Map<PostAdress, HashSet<PostAdress>> adjList; // Graph represented as an adjacency list

    public Graph() {
        this.adjList = new HashMap<>();
    }

    public void addEdge(PostAdress source, PostAdress destination) {
        // Add the edge from source to destination
        this.adjList.computeIfAbsent(source, k -> new HashSet<>()).add(destination);
        // Since this is an undirected graph, also add the edge from destination to source
        this.adjList.computeIfAbsent(destination, k -> new HashSet<>()).add(source);
    }

    public void loadGraphFromCSV(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        HashMap<String, PostAdress> Addresses = Utilities.initPostAdressMap("data\\distances.csv");
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(","); // Assuming CSV is comma-separated
            if (tokens.length > 1) {
                PostAdress source = Addresses.get(tokens[0]);
                for (int i = 1; i < tokens.length; i++) {
                    PostAdress destination =Addresses.get(tokens[i]);
                    addEdge(source, destination);
                }
            }
        }
        reader.close();
    }

    // Method to print the graph (for debugging purposes)
    public void printGraph() {
        for (PostAdress node : this.adjList.keySet()) {
            System.out.print(node.getPostalCode() + " -> ");
            HashSet<PostAdress> edges = this.adjList.get(node);
            for (PostAdress edge : edges) {
                System.out.print(edge.getPostalCode() + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Graph graph = new Graph();
        try {
            graph.loadGraphFromCSV("data\\graph100.csv"); // Replace "path_to_your_file.csv" with your file's path
            graph.printGraph(); // Print the graph to check if it's loaded correctly
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
