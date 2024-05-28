package dbTables;

public class BusStop {
    private final int stopId;
    private final int stopSequence;
    private final String stopName;
    private final String arrivalTime;
    private final String departureTime;
    private final float stopLat;
    private final float stopLon;
    private final String routeColor;


    public BusStop(int stopId, int stopSequence, String stopName, String arrivalTime, String departureTime, float stopLat, float stopLon, String routeColor) {
        this.stopId = stopId;
        this.stopSequence = stopSequence;
        this.stopName = stopName;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.stopLat = stopLat;
        this.stopLon = stopLon;
        this.routeColor = routeColor;
    }

    public String getRouteColor() {
        return routeColor;
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

    public float getStopLat() {
        return stopLat;
    }

    public float getStopLon() {
        return stopLon;
    }
}