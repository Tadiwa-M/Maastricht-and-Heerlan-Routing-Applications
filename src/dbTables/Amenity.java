package dbTables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static dbTables.dbManager.getSqlConnection;

public class Amenity {
    private double lat;
    private double lon;
    private String type;

    public Amenity(double lat, double lon, String type) {
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

    public static List<Amenity> fetchAmenitiesByCords(double lat, double lon, double radius) {
        Connection conn = getSqlConnection();
        if (conn == null)
            return null;

        List<Amenity> nearbyAmenities = new ArrayList<Amenity>();

        String query = "SELECT lat, lon, amenity, " +
                "(6378 * acos(cos(radians(?)) * cos(radians(lat)) * cos(radians(lon) - radians(?)) + sin(radians(?)) * sin(radians(lat)))) AS distance "
                +
                "FROM amenities " +
                "HAVING distance <= ? " +
                "ORDER BY distance";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setDouble(1, lat);
            stmt.setDouble(2, lon);
            stmt.setDouble(3, lat);
            stmt.setDouble(4, radius);

            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                Amenity Amenity = new Amenity(resultSet.getDouble("lat"),
                        resultSet.getDouble("lon"), resultSet.getString("amenity"));
                nearbyAmenities.add(Amenity);
            }
            resultSet.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return nearbyAmenities;
    }

}
