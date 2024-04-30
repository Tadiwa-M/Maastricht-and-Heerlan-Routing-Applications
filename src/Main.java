import Transport.*;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void calculateDistance(PostAddress start, PostAddress end, int vehicleType) {
        Graph graph = new GraphCreator().createGraph();
        ShortestPathFinder shortestPathFinder = new ShortestPathFinder(graph);
        shortestPathFinder.setShortestPathAlgorithm(new Dijkstra(graph));

        List<PostAddress> path = shortestPathFinder.findPath(start, end);
        double distance = shortestPathFinder.getDistance(start, end);

        if (path.isEmpty()) {
            System.out.println("No path could be found between the specified addresses.");
        } else {
            System.out.println("Path found: " + path.stream()
                    .map(PostAddress::getPostalCode)
                    .collect(Collectors.joining(" -> ")));

            Vehicle vehicle;
            if (vehicleType == 0)
                vehicle = new Foot(distance);
            else
                vehicle = new Bike(distance);

            double time = vehicle.calculateTime();

            //Round to 2 decimal places
            distance = Math.round(distance * 100.0) / 100.0;
            System.out.println("Distance: " + distance + " Kilometers");

            //Round to 2 decimal places
            time = Math.round(time * 100.0) / 100.0;
            System.out.println("Time: " + time + " Minutes");
        }
    }

    public static void main(String[] args) {

       PostAddress start = DataManager.getDataManager().postAddresses.get("6216EG");
       PostAddress end = DataManager.getDataManager().postAddresses.get("6221CR");

        calculateDistance(start, end, 1);

        for (PostAddress address : DataManager.getDataManager().postAddresses.values()) {
            //address valid postal code
            if (address.getPostalCode().startsWith("621")) {
                //print distance
                end = AddressFinder.getAddress(address.getPostalCode());
                calculateDistance(start, end, 0);
            }
        }
    }
}
