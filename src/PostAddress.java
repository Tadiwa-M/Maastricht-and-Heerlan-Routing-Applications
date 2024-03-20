import java.util.Objects;

public class PostAddress {
    private String postalCode;
    private double lat;
    private double lon;
    private static final double EARTH_RADIUS_KM = 6371.0;

    public String getPostalCode() {
        return postalCode;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public PostAddress(String postalCode, double lat, double lon) {
        this.postalCode = postalCode;
        this.lat = lat;
        this.lon = lon;
    }

    // This function converts decimal degrees to radians
    private static double degToRad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /***
     * A method for finding the birds flight distance between two points
     * 
     * @param start
     * @param end
     * @return The kilometers between those points
     */
    public static double basicDistances(PostAddress start, PostAddress end) {
        // Convert the latitudes and longitudes from decimal degrees to radians
        double lat1Rad = degToRad(start.getLat());
        double lon1Rad = degToRad(start.getLon());
        double lat2Rad = degToRad(end.getLat());
        double lon2Rad = degToRad(end.getLon());

        // Haversine formula
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        PostAddress other = (PostAddress) obj;
        return Objects.equals(postalCode, other.postalCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postalCode);
    }

}
