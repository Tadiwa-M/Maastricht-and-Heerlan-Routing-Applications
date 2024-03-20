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
            Main.calculateDistance(this.startingPoint, this.endPoint, this.vehicleCode);
        }
    }
}
