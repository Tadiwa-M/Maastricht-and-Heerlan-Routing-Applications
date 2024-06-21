package dbTables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static dbTables.dbManager.getSqlConnection;

public class Tourism {
    private String name;
    private double lat;
    private double lon;
    private String type;

    public Tourism(String name, double lat, double lon, String type) {
        this.name = name;
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

    /**
     * Returns a sorted(closest first) list of tourism attraction close to the lat, lon in radius
     * @param lat
     * @param lon
     * @param radius
     * @return
     */
    public static List<Tourism> fetchAttractionsByCoords(double lat, double lon, double radius) {
        Connection conn = getSqlConnection();
        if (conn == null)
            return null;

        List<Tourism> nearbyAttractions = new ArrayList<Tourism>();

        String query = "SELECT lat, lon, name, attraction_type, " +
                "(6378 * acos(cos(radians(?)) * cos(radians(lat)) * cos(radians(lon) - radians(?)) + sin(radians(?)) * sin(radians(lat)))) AS distance "
                +
                "FROM tourism " +
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
                Tourism attraction = new Tourism(resultSet.getString("name"), resultSet.getDouble("lat"),
                        resultSet.getDouble("lon"), resultSet.getString("attraction_type"));
                nearbyAttractions.add(attraction);
            }
            resultSet.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return nearbyAttractions;
    }

}
