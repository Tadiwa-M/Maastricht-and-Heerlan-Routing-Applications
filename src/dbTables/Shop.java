package dbTables;

public class Shop {
    private String name;
    private double lat;
    private double lon;
    private String type;

    public Shop(String name, double lat, double lon, String type){
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.type = type;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
}
