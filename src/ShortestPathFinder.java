import java.util.ArrayList;

public class ShortestPathFinder {
    final double threshold = 0.37;
    ArrayList <PostAddress> addresses = new ArrayList<>();
    Dijkstra dijkstra;
    public ShortestPathFinder() {
        // Array of adj list
        ArrayList<ArrayList<Integer>> adj = new ArrayList<ArrayList<Integer>>();
        DataManager.getDataManager().postAddresses.forEach((k, v) -> {
            adj.add(new ArrayList<Integer>());
        });

        for (PostAddress address : DataManager.getDataManager().postAddresses.values()) {
            if(ValidPostalCode(address.getPostalCode())) {
                addresses.add(address);
            }
        }


        //Create graph
        CreateGraph(addresses, adj);

        //Dijkstra algorithm + Path printing
        dijkstra = new Dijkstra(adj, addresses);
    }

    public ArrayList<PostAddress> findPath(PostAddress start, PostAddress end) {
        int indexStart = addresses.indexOf(start);
        int indexEnd = addresses.indexOf(end);

        if (indexStart == -1 || indexEnd == -1) {
            return new ArrayList<PostAddress>();
        }

        dijkstra.runAlgorithm(indexStart);
        return dijkstra.findPath(indexEnd);
    }

    //Get distance from start to end
    public double getDistance(PostAddress start, PostAddress end) {
        int indexStart = addresses.indexOf(start);
        int indexEnd = addresses.indexOf(end);

        if (indexStart == -1 || indexEnd == -1) {
            return -1;
        }

        dijkstra.runAlgorithm(indexStart);
        return dijkstra.dist[indexEnd];
    }

    public boolean ValidPostalCode(String postalCode) {

        String number = postalCode.substring(0, 4);
        try {
            int num = Integer.parseInt(number);

            if (num < 6211 || num > 6229)
                return false;
        }
        catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    public void CreateGraph(ArrayList<PostAddress> addresses, ArrayList<ArrayList<Integer>> adj) {
        for (int i = 0; i < addresses.size(); i++) {
            double minDist = Double.MAX_VALUE;
            int minIndex = -1;
            for (int j = 0; j < addresses.size(); j++) {
                if (i != j) {
                    double dist = PostAddress.basicDistances(addresses.get(i), addresses.get(j));
                    if (dist < minDist) {
                        minDist = dist;
                        minIndex = j;
                    }
                }
                if (i != j && PostAddress.basicDistances(addresses.get(i), addresses.get(j)) < threshold) {
                    adj.get(i).add(j);
                }
            }
            if (adj.get(i).isEmpty())
                adj.get(i).add(minIndex);
        }
    }
}
