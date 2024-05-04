import java.util.ArrayList;
import java.util.PriorityQueue;

public class A_STAR {
    ArrayList<ArrayList<Integer>> adj;
    ArrayList<PostAddress> addresses;
    int N;
    double[] dist;
    int[] parent;
    double[] heuristic;
    final double threshold = 0.23;

    public A_STAR(ArrayList<ArrayList<Integer>> adj, ArrayList<PostAddress> addresses) {
        this.adj = adj;
        this.addresses = addresses;
        this.N = addresses.size();
        dist = new double[N];
        parent = new int[N];
        heuristic = new double[N];
    }

    public void runAlgorithm(int src, int dest) {
        for (int i = 0; i < N; i++) {
            dist[i] = Double.MAX_VALUE;
            parent[i] = -1;
            heuristic[i] = PostAddress.basicDistances(addresses.get(i), addresses.get(dest));
        }

        dist[src] = 0;

        PriorityQueue<Pair> pq = new PriorityQueue<>((a, b) -> Double.compare(a.first, b.first));
        pq.add(new Pair(0 + heuristic[src], src));

        while (!pq.isEmpty()) {
            int u = pq.poll().second;

            if (u == dest)
                break;

            for (int i = 0; i < adj.get(u).size(); i++) {
                int v = adj.get(u).get(i);

                double weight = PostAddress.basicDistances(addresses.get(u), addresses.get(v));

                if (weight > threshold)
                    weight *= 5;

                double newDist = dist[u] + weight;

                if (newDist < dist[v]) {
                    dist[v] = newDist;
                    parent[v] = u;
                    pq.add(new Pair(newDist + heuristic[v], v));
                }
            }
        }
    }

    void printDistances() {
        for (int i = 0; i < N; i++) {
            System.out.println("Distance from source to " + addresses.get(i).getPostalCode() + " is " + dist[i]);
        }
    }

    public ArrayList<PostAddress> findPath(int dest) {
        if (!foundPath(dest)) {
            return new ArrayList<>();
        }

        ArrayList<PostAddress> path = new ArrayList<>();
        int i = dest;

        while (i != -1) {
            path.add(addresses.get(i));
            i = parent[i];
        }

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