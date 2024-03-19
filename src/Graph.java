import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Graph {
    private Map<PostAdress, HashSet<PostAdress>> adjList; 

    public Graph() {
        this.adjList = new HashMap<>();
    }

    public void addEdge(PostAdress source, PostAdress destination) {
        this.adjList.computeIfAbsent(source, k -> new HashSet<>()).add(destination);
        this.adjList.computeIfAbsent(destination, k -> new HashSet<>()).add(source);
    }

    public void loadGraphFromCSV(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        HashMap<String, PostAdress> addresses = Utilities.initPostAdressMap("data/distances.csv");
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(",");
            if (tokens.length > 1) {
                PostAdress source = addresses.get(tokens[0]);
                for (int i = 1; i < tokens.length; i++) {
                    PostAdress destination = addresses.get(tokens[i]);
                    addEdge(source, destination);
                }
            }
        }
        reader.close();
    }

    public Map<PostAdress, HashSet<PostAdress>> getAdjList() {
        return adjList;
    }

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
}
