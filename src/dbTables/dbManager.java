package dbTables;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class dbManager {

    public static void main(String[] args) {
        fetchShopsByCoords(5.66, 50.86, 0.5);
    }

    private static Connection getSqlConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String USERNAME = dbCredentials.USERNAME;
            String PORT = dbCredentials.PORT;
            String HOST = dbCredentials.HOST;
            String DATABASE_NAME = dbCredentials.databaseName;
            String PASSWORD = dbCredentials.PASSWORD;
            String DATABASE_URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE_NAME
                    + "?autoReconnect=true&useSSL=true&requireSSL=true";

            return DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error establishing database connection: " + e.getMessage());
            return null;
        }
    }

    private List<String> findPotentialTransferStops(String startStopId, String endStopId) {
        Connection conn = getSqlConnection();
        if (conn == null)
            return null;
        String sql = "SELECT DISTINCT st2.stop_id " +
                "FROM stop_times st1 " +
                "JOIN stop_times st2 ON st1.trip_id = st2.trip_id " +
                "WHERE st1.stop_id = ? AND st2.stop_id != ? AND st1.stop_sequence < st2.stop_sequence";

        List<String> transferStops = new ArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, startStopId);
            stmt.setString(2, endStopId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transferStops.add(rs.getString("stop_id"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return transferStops;
    }




    public static BusRoute getAllStopsFromTripId(String tripID, int startSequence, int endSequence) {
        Connection conn = getSqlConnection();
        if (conn == null)
            return null;

        List<BusStop> busStops = new ArrayList<>();

        String query = "SELECT " +
                "    st.stop_id, " +
                "    st.stop_sequence, " +
                "    s.stop_name, " +
                "    st.arrival_time, " +
                "    st.departure_time, " +
                "    s.stop_lat, " +
                "    s.stop_lon, " +
                "    r.route_short_name " +
                "FROM " +
                "    stop_times st " +
                "JOIN " +
                "    stops s ON st.stop_id = s.stop_id " +
                "JOIN " +
                "    trips t ON st.trip_id = t.trip_id " +
                "JOIN " +
                "    routes r ON t.route_id = r.route_id " +
                "WHERE " +
                "    st.trip_id = ? AND " +
                "    st.stop_sequence BETWEEN ? AND ? " +
                "ORDER BY " +
                "    st.stop_sequence;";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, tripID);
            stmt.setInt(2, startSequence);
            stmt.setInt(3, endSequence);
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                BusStop busStop = new BusStop(
                        resultSet.getInt("stop_id"),
                        resultSet.getInt("stop_sequence"),
                        resultSet.getString("stop_name"),
                        resultSet.getString("arrival_time"),
                        resultSet.getString("departure_time"),
                        resultSet.getFloat("stop_lat"),
                        resultSet.getFloat("stop_lon"),
                        resultSet.getString("route_short_name"));
                busStops.add(busStop);
            }

            resultSet.close();
            stmt.close();
            conn.close();

            return new BusRoute(busStops);
        } catch (SQLException e) {
            System.err.println("Error retrieving stops: " + e.getMessage());
            return null;
        }
    }

    public static class RouteDetails {
        private final String tripId;
        private final int startStopSequence;
        private final int endStopSequence;

        public RouteDetails(String tripId, int startStopSequence, int endStopSequence) {
            this.tripId = tripId;
            this.startStopSequence = startStopSequence;
            this.endStopSequence = endStopSequence;
        }

        public String getTripId() {
            return tripId;
        }

        public int getStartStopSequence() {
            return startStopSequence;
        }

        public int getEndStopSequence() {
            return endStopSequence;
        }
    }

    public static RouteDetails findShortestRoute(List<Stops> startStopIds, List<Stops> endStopIds) {
        Connection conn = getSqlConnection();
        if (conn == null)
            return null;

        String query = getRouteQuery(startStopIds, endStopIds);

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                String tripId = resultSet.getString("trip_id");
                int startStopSequence = resultSet.getInt("start_stop_sequence");
                int endStopSequence = resultSet.getInt("end_stop_sequence");
                resultSet.close();
                stmt.close();
                conn.close();
                return new RouteDetails(tripId, startStopSequence, endStopSequence);
            } else {
                resultSet.close();
                stmt.close();
                conn.close();
                System.out.println("No trip found");
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving trip id: " + e.getMessage());
            return null;
        }
    }

    private static @NotNull String getRouteQuery(List<Stops> startStopIds, List<Stops> endStopIds) {
        StringBuilder startStopsBuilder = new StringBuilder();
        StringBuilder endStopsBuilder = new StringBuilder();

        for (int i = 0; i < startStopIds.size(); i++) {
            startStopsBuilder.append(startStopIds.get(i).getStopId());
            if (i < startStopIds.size() - 1) {
                startStopsBuilder.append(",");
            }
        }

        for (int i = 0; i < endStopIds.size(); i++) {
            endStopsBuilder.append(endStopIds.get(i).getStopId());
            if (i < endStopIds.size() - 1) {
                endStopsBuilder.append(",");
            }
        }

        return "SELECT " +
                "    st1.trip_id, " +
                "    st1.stop_sequence AS start_stop_sequence, " +
                "    st2.stop_sequence AS end_stop_sequence, " +
                "    TIMESTAMPDIFF(SECOND, st1.departure_time, st2.arrival_time) AS travel_time " +
                "FROM " +
                "    stop_times st1 " +
                "JOIN " +
                "    stop_times st2 ON st1.trip_id = st2.trip_id " +
                "WHERE " +
                "    st1.stop_id IN (" + startStopsBuilder + ") AND " +
                "    st2.stop_id IN (" + endStopsBuilder + ") AND " +
                "    st1.stop_sequence < st2.stop_sequence " +
                "ORDER BY " +
                "    travel_time " +
                "LIMIT 1;";
    }
    
    public static List<Stops> fetchStopsByCoords(double lat, double lon) {
        Connection conn = getSqlConnection();
        if (conn == null)
            return null;

        List<Stops> stopsList = new ArrayList<>();

        String query = "SELECT *, " +
                "(6371 * acos(cos(radians(?)) * cos(radians(stop_lat)) * cos(radians(stop_lon) - radians(?)) + sin(radians(?)) * sin(radians(stop_lat)))) AS distance "
                +
                "FROM stops " +
                "HAVING distance <= 0.4 " +
                "ORDER BY distance " +
                "LIMIT 7;";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setDouble(1, lat);
            stmt.setDouble(2, lon);
            stmt.setDouble(3, lat);

            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                Stops stop = new Stops();
                stop.setStopId(resultSet.getInt("stop_id"));
                stop.setStopCode(resultSet.getString("stop_code"));
                stop.setStopName(resultSet.getString("stop_name"));
                stop.setStopLat(resultSet.getFloat("stop_lat"));
                stop.setStopLon(resultSet.getFloat("stop_lon"));
                stop.setLocationType(resultSet.getInt("location_type"));
                stop.setParentStation(resultSet.getString("parent_station"));
                stop.setStopTimezone(resultSet.getString("stop_timezone"));
                stop.setWheelchairBoarding(resultSet.getInt("wheelchair_boarding"));
                stop.setPlatformCode(resultSet.getString("platform_code"));
                stop.setZoneId(resultSet.getString("zone_id"));
                stopsList.add(stop);
            }
            resultSet.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return stopsList;
    }
    /**
     * Returns a sorted(closest first) list of tourism shops and malls close to the lat, lon in radius
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

        String query = "SELECT lat, lon, `properties/name`, `properties/shop`, " +
                "(6371 * acos(cos(radians(?)) * cos(radians(lat)) * cos(radians(lon) - radians(?)) + sin(radians(?)) * sin(radians(lat)))) AS distance " +
                "FROM shop " +
                "HAVING distance <= ? " +
                "ORDER BY distance";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setDouble(1, lon);
            stmt.setDouble(2, lat);
            stmt.setDouble(3, lon);
            stmt.setDouble(4, radius);

            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                Shop shop = new Shop(resultSet.getString("`properties/name`"), resultSet.getDouble("lat"), resultSet.getDouble("lon"), resultSet.getString("`properties/shop`"));
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

        String query = "SELECT lat, lon, `properties/name`, `properties/tourism`, " +
                "(6371 * acos(cos(radians(?)) * cos(radians(lat)) * cos(radians(lon) - radians(?)) + sin(radians(?)) * sin(radians(lat)))) AS distance "
                +
                "FROM tourism " +
                "HAVING distance <= ? " +
                "ORDER BY distance";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setDouble(1, lon);
            stmt.setDouble(2, lat);
            stmt.setDouble(3, lon);
            stmt.setDouble(4, radius);

            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                Tourism attraction = new Tourism(resultSet.getString("`properties/name`"), resultSet.getDouble("lat"),
                        resultSet.getDouble("lon"), resultSet.getString("`properties/tourism`"));
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

    public static List<Amenity> fetchAmenitiesByCoords(double lat, double lon, double radius) {
        Connection conn = getSqlConnection();
        if (conn == null)
            return null;

        List<Amenity> nearbyAmenities = new ArrayList<Amenity>();

        String query = "SELECT lat, lon, propertiesamenity, " +
                "(6371 * acos(cos(radians(?)) * cos(radians(lat)) * cos(radians(lon) - radians(?)) + sin(radians(?)) * sin(radians(lat)))) AS distance "
                +
                "FROM amenities " +
                "HAVING distance <= ? " +
                "ORDER BY distance";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setDouble(1, lon);
            stmt.setDouble(2, lat);
            stmt.setDouble(3, lon);
            stmt.setDouble(4, radius);

            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                Amenity Amenity = new Amenity(resultSet.getDouble("lat"),
                        resultSet.getDouble("lon"), resultSet.getString("`propertiesamenety`"));
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

    public static PostAddress fetchAddress(String postalCode) {
        Connection conn = getSqlConnection();
        if (conn == null)
            return null;

        String query = "SELECT * FROM postal_codes WHERE postal_code = ?";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, postalCode);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                PostAddress address = new PostAddress(resultSet.getString("postal_code"), resultSet.getDouble("latitude"), resultSet.getDouble("longitude"));
                resultSet.close();
                stmt.close();
                conn.close();
                return address;
            } else {
                resultSet.close();
                stmt.close();
                conn.close();
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving postal code: " + e.getMessage());
            return null;
        }
    }

    public static List<PostAddress> fetchAllAddresses() {
        Connection conn = getSqlConnection();
        if (conn == null)
            return null;

        List<PostAddress> addresses = new ArrayList<>();

        String query = "SELECT * FROM postal_codes";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                PostAddress address = new PostAddress(resultSet.getString("postal_code"), resultSet.getDouble("latitude"), resultSet.getDouble("longitude"));
                addresses.add(address);
            }
            resultSet.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving postal codes: " + e.getMessage());
        }
        return addresses;
    }
}
