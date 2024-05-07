public class Query {
    PostAddress startingPoint;
    PostAddress endPoint;
    VehicleType vehicleType;

    public Query(String startingPoint, String endPoint, VehicleType vehicleType) {
        this.startingPoint = AddressFinder.getAddress(startingPoint);
        this.endPoint = AddressFinder.getAddress(endPoint);
        this.vehicleType = vehicleType;

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
