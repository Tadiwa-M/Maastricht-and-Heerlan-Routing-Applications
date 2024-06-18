package dbTables;

public class IntermediateStop {
    private String stopName;
    private String departureTime;
    private String arrivalTime;
    private float lat;
    private float lon;

    public IntermediateStop(String stopName, String departureTime, String arrivalTime, float lat, float lon) {
        this.stopName = stopName;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.lat = lat;
        this.lon = lon;
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

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}
