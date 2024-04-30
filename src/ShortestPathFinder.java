import java.util.ArrayList;

public class ShortestPathFinder {
    ArrayList <PostAddress> addresses = new ArrayList<>();
    IShortestPath shortestPathAlgorithm;
    Graph graph;

    public ShortestPathFinder(Graph graph) {
        this.addresses = graph.addresses;
        this.graph = graph;
    }

    public void setShortestPathAlgorithm(IShortestPath shortestPathAlgorithm) {
        this.shortestPathAlgorithm = shortestPathAlgorithm;
    }

    public ArrayList<PostAddress> findPath(PostAddress start, PostAddress end) {
        int indexStart = addresses.indexOf(start);
        int indexEnd = addresses.indexOf(end);

        if (indexStart == -1 || indexEnd == -1) {
            return new ArrayList<PostAddress>();
        }

        shortestPathAlgorithm.runAlgorithm(indexStart);
        return shortestPathAlgorithm.findPath(indexEnd);
    }

    //Get distance from start to end
    public double getDistance(PostAddress start, PostAddress end) {
        int indexStart = addresses.indexOf(start);
        int indexEnd = addresses.indexOf(end);

        if (indexStart == -1 || indexEnd == -1) {
            return -1;
        }

        shortestPathAlgorithm.runAlgorithm(indexStart);
        return shortestPathAlgorithm.getDistance(indexEnd);
    }


}
