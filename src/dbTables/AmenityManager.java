package dbTables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static dbTables.dbManager.getSqlConnection;

public class AmenityManager {
    public static List<Amenity> fetchAmenitiesByCords() {
        Connection conn = getSqlConnection();
        if (conn == null)
            return null;

        List<Amenity> nearbyAmenities = new ArrayList<Amenity>();

        String query = "SELECT lat, lon, amenity " +
                "FROM amenities ";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);

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

    public static List<Tourism> fetchAttractionsByCoords() {
        Connection conn = getSqlConnection();
        if (conn == null)
            return null;

        List<Tourism> nearbyAttractions = new ArrayList<Tourism>();

        String query = "SELECT lat, lon, name, attraction_type FROM tourism ";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
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

    public static List<Shop> fetchShopsByCoords() {
        Connection conn = getSqlConnection();
        if (conn == null)
            return null;

        List<Shop> nearbyShops = new ArrayList<>();

        String query = "SELECT lat, lon, name, shop FROM shop ";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);

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
        return nearbyShops;
    }

    public static double fetchAddressScore(String postalCode) {
        Connection conn = getSqlConnection();
        if (conn == null)
            return -1;

        String query = "SELECT * FROM amenity_score WHERE postal_address = ?";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, postalCode);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                double score = resultSet.getDouble("score");
                resultSet.close();
                stmt.close();
                conn.close();
                return score;
            } else {
                resultSet.close();
                stmt.close();
                conn.close();
                return -1;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving postal code: " + e.getMessage());
            return -1;
        }
    }

    public static boolean insertAddressScore(String postalCode, double score) {
        Connection conn = getSqlConnection();
        if (conn == null)
            return false;

        String query = "INSERT INTO amenity_score (postal_address, score) VALUES (?, ?)";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, postalCode);
            stmt.setDouble(2, score);
            int rowsInserted = stmt.executeUpdate();

            stmt.close();
            conn.close();

            return rowsInserted > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting postal code and score: " + e.getMessage());
            return false;
        }
    }
}
