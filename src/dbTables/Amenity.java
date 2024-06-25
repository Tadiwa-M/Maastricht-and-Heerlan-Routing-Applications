package dbTables;

public record Amenity(double lat, double lon, String type) {
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
