import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Graph {
    private Map<PostAddress, HashSet<PostAddress>> adjList;

    public Graph() {
        this.adjList = new HashMap<>();
    }

    public void addEdge(PostAddress source, PostAddress destination) {
        this.adjList.computeIfAbsent(source, k -> new HashSet<>()).add(destination);
        this.adjList.computeIfAbsent(destination, k -> new HashSet<>()).add(source);
    }

    public void loadGraphFromCSV(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        HashMap<String, PostAddress> addresses = Utilities.initPostAddressMap("data/distances.csv");
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(",");
            if (tokens.length > 1) {
                PostAddress source = addresses.get(tokens[0]);
                for (int i = 1; i < tokens.length; i++) {
                    PostAddress destination = addresses.get(tokens[i]);
                    addEdge(source, destination);
                }
            }
        }
        reader.close();
    }

    public void createGraph() {
        HashMap<String, PostAddress> addresses = DataManager.getDataManager().postAddresses;
        for (PostAddress source : addresses.values()) {
            for (PostAddress destination : addresses.values()) {
                if (source != destination && PostAddress.basicDistances(source, destination) < 0.7) {
                    addEdge(source, destination);
                }
            }
        }
    }

    public Map<PostAddress, HashSet<PostAddress>> getAdjList() {
        return adjList;
    }

    public void printGraph() {
        for (PostAddress node : this.adjList.keySet()) {
            System.out.print(node.getPostalCode() + " -> ");
            HashSet<PostAddress> edges = this.adjList.get(node);
            for (PostAddress edge : edges) {
                System.out.print(edge.getPostalCode() + " ");
            }
            System.out.println();
        }
    }
}
