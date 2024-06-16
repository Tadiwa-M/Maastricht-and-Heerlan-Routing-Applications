package dbTables;

public class IntermediateStop {
    private String stopName;
    private String departureTime;
    private String arrivalTime;

    public IntermediateStop(String stopName, String departureTime, String arrivalTime) {
        this.stopName = stopName;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    public String getStopName() {
        return stopName;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }
}
