import java.util.ArrayList;

public class GraphCreator {
    final double threshold = 0.3;
    ArrayList<ArrayList<Integer>> adj;
    ArrayList<PostAddress> addresses;

    public GraphCreator() {
        // Array of adj list
        this.adj = new ArrayList<ArrayList<Integer>>();
        this.addresses = new ArrayList<>();

        for (PostAddress address : DataManager.getDataManager().postAddresses.values()) {
            if(ValidPostalCode(address.getPostalCode())) {
                addresses.add(address);
            }
        }

        DataManager.getDataManager().postAddresses.forEach((k, v) -> {
            adj.add(new ArrayList<Integer>());
        });
    }

    public Graph createGraph() {
        // Create graph
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
        return new Graph(addresses, adj);
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
}
