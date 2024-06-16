package dbTables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class GTFSLoader {
    public static BusGraph loadGraph() {
        BusGraph graph = new BusGraph();
        Connection conn = getSqlConnection();

        if (conn == null) {
            return null;
        }

        String sql = "SELECT st1.stop_id, st1.trip_id, TIME_FORMAT(st1.departure_time, '%H:%i:%s') AS departure_time_str, " +
                "TIME_FORMAT(st2.arrival_time, '%H:%i:%s') AS arrival_time_str, st2.stop_id AS next_stop_id " +
                "FROM stop_times st1 " +
                "JOIN stop_times st2 ON st1.trip_id = st2.trip_id AND st1.stop_sequence < st2.stop_sequence";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String fromStopId = rs.getString("stop_id");
                String toStopId = rs.getString("next_stop_id");
                String tripId = rs.getString("trip_id");
                String departureTime = rs.getString("departure_time_str");
                String arrivalTime = rs.getString("arrival_time_str");
                int weight = computeTimeDifferenceInSeconds(departureTime, arrivalTime);

                graph.addEdge(fromStopId, toStopId, weight, tripId, departureTime, arrivalTime);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }

        return graph;
    }

    private static int computeTimeDifferenceInSeconds(String departureTime, String arrivalTime) {
        return timeToSeconds(arrivalTime) - timeToSeconds(departureTime);
    }

    private static int timeToSeconds(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        return hours * 3600 + minutes * 60 + seconds;
    }

    public static Map<String, Stop> fetchAllAddresses() {
        Map<String, Stop> addressMap = new HashMap<>();
        Connection conn = getSqlConnection();
        if (conn == null) {
            return Collections.emptyMap();
        }

        String sql = "SELECT stop_id, stop_name, stop_lat, stop_lon FROM stops";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String stopId = rs.getString("stop_id");
                String stopName = rs.getString("stop_name");
                double lat = rs.getDouble("stop_lat");
                double lon = rs.getDouble("stop_lon");
                addressMap.put(stopId, new Stop(stopId, stopName, lat, lon));
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }

        return addressMap;
    }

    public static Stop getStopDetails(String stopId) {
        Connection conn = getSqlConnection();
        if (conn == null) {
            return null;
        }

        String sql = "SELECT stop_id, stop_name, stop_lat, stop_lon FROM stops WHERE stop_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, stopId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String stopName = rs.getString("stop_name");
                    double lat = rs.getDouble("stop_lat");
                    double lon = rs.getDouble("stop_lon");
                    return new Stop(stopId, stopName, lat, lon);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }

        return null;
    }

    private static Connection getSqlConnection() {
        return dbManager.getSqlConnection();
    }
}