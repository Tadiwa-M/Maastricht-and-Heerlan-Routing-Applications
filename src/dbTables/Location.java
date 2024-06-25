package dbTables;

public abstract class Location {
    private double lat;
    private double lon;
    private String type;

    public Location(double lat, double lon, String type) {
        this.lat = lat;
        this.lon = lon;
        this.type = type;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getType() {
        return type;
    }
}
