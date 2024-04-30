public class Query {
    PostAddress startingPoint;
    PostAddress endPoint;
    int vehicleCode;

    public Query(String startingPoint, String endPoint, int vehicleCode) {
        this.startingPoint = AddressFinder.getAddress(startingPoint);
        this.endPoint = AddressFinder.getAddress(endPoint);
        this.vehicleCode = vehicleCode;

        if (this.startingPoint == null || this.endPoint == null) {
            throw new IllegalArgumentException("Invalid postal code");
        }
        else {
            Graph graph = new GraphCreator().createGraph();
            ShortestPathFinder shortestPathFinder = new ShortestPathFinder(graph);

            //TODO wrap this in a factory
            IShortestPath shortestPathAlgorithm = new Dijkstra(graph);

            shortestPathFinder.setShortestPathAlgorithm(shortestPathAlgorithm);

            shortestPathFinder.findPath(this.startingPoint, this.endPoint);
        }
    }
}
