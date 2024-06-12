import dbTables.PostAddress;

import java.util.ArrayList;

public class Graph {
    int numVertices;
    ArrayList<ArrayList<Integer>> adj;
    ArrayList<PostAddress> addresses;

    public Graph(ArrayList<PostAddress> addresses, ArrayList<ArrayList<Integer>> adj) {
        this.numVertices = addresses.size();
        this.adj = adj;
        this.addresses = addresses;
    }
}
