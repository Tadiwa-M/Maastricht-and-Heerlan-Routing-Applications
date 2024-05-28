package dbTables;

public class BusStop {
    private final int stopId;
    private final int stopSequence;
    private final String stopName;
    private final String arrivalTime;
    private final String departureTime;

//    private String routeColor;

    public BusStop(int stopId, int stopSequence, String stopName, String arrivalTime, String departureTime) {
        this.stopId = stopId;
        this.stopSequence = stopSequence;
        this.stopName = stopName;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
    }

    public int getStopId() {
        return stopId;
    }

    public int getStopSequence() {
        return stopSequence;
    }

    public String getStopName() {
        return stopName;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public String getDepartureTime() {
        return departureTime;
    }
}
