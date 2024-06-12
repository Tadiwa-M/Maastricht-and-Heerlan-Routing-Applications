
import dbTables.PostAddress;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class Dijkstra implements IShortestPath {
    final double threshold = 0.23;
    ArrayList<ArrayList<Integer>> adj;
    ArrayList<PostAddress> addresses;
    int N;
    double[] dist;
    int[] parent;

    public Dijkstra(Graph graph) {
        // Copy from graph
        this.N = graph.numVertices;
        this.addresses = graph.addresses;
        this.adj = graph.adj;

        // Initialize distance and parent arrays
        dist = new double[N];
        parent = new int[N];
    }

    public void runAlgorithm(int src) {
        //Initialize distance array
        for (int i = 0; i < N; i++) {
            dist[i] = Double.MAX_VALUE;
            parent[i] = -1;
        }
        dist[src] = 0;


        // Initialize priority queue
        PriorityQueue<Pair> pq = new PriorityQueue<>();

        // Add source node to the priority queue
        pq.add(new Pair(0, src));

        // While the priority queue is not empty
        while (!pq.isEmpty()) {
            // Extract the minimum distance node from the priority queue
            int u = pq.poll().second;

            // Traverse through all the adjacent nodes of u
            for (int i = 0; i < adj.get(u).size(); i++) {
                // Get node value and weight of the current adjacent of u
                int v = adj.get(u).get(i);

                double weight = PostAddress.basicDistances(addresses.get(u), addresses.get(v));

                // If the distance to v is shorter by going through u
                if (dist[v] > dist[u] + weight) {
                    // Update the distance of v
                    dist[v] = dist[u] + weight;
                    pq.add(new Pair(dist[v], v));
                    parent[v] = u;
                }
            }
        }
    }

    public double getDistance(int dest) {
        return dist[dest];
    }

    void printDistances() {
        for (int i = 0; i < N; i++) {
            System.out.println("Distance from source to " + addresses.get(i).getPostalCode() + " is " + dist[i]);
        }
    }

    public ArrayList<PostAddress> findPath(int dest) {
        if (!foundPath(dest)) {
            return new ArrayList<PostAddress>();
        }

        ArrayList <PostAddress> path = new ArrayList<PostAddress>();
        int i = dest;

        while (i != -1) {
            path.add(addresses.get(i));
            i = parent[i];
        }

        //Reverse the path
        ArrayList<PostAddress> reversedPath = new ArrayList<>();
        for (int j = path.size() - 1; j >= 0; j--) {
            reversedPath.add(path.get(j));
        }

        return reversedPath;
    }

    boolean foundPath(int dest) {
        return dist[dest] != Double.MAX_VALUE;
    }
}
