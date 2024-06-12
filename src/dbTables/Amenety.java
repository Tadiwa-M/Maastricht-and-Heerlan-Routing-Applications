package dbTables;

public class Amenety {
    private double lat;
    private double lon;
    private String type;

    public Amenety(double lat, double lon, String type) {
        this.lat = lat;
        this.lon = lon;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
