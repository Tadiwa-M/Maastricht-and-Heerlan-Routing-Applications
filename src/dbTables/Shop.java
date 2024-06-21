package dbTables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static dbTables.dbManager.getSqlConnection;

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


    /**
     * Returns a sorted(closest first) list of shops close to the lat, lon in radius
     * @param lat
     * @param lon
     * @param radius
     * @return
     */
    public static List<Shop> fetchShopsByCoords(double lat, double lon, double radius) {
        Connection conn = getSqlConnection();
        if (conn == null)
            return null;

        List<Shop> nearbyShops = new ArrayList<>();

        String query = "SELECT lat, lon, name, shop, " +
                "(6378 * acos(cos(radians(?)) * cos(radians(lat)) * cos(radians(lon) - radians(?)) + sin(radians(?)) * sin(radians(lat)))) AS distance " +
                "FROM shop " +
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
                Shop shop = new Shop(resultSet.getString("name"), resultSet.getDouble("lat"), resultSet.getDouble("lon"), resultSet.getString("shop"));
                nearbyShops.add(shop);
            }
            resultSet.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        System.out.println(nearbyShops.size());
        return nearbyShops;
    }
    
}
