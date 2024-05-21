package dbTables;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class dbManager {
    private static String USERNAME = dbCredentials.USERNAME;
    private static String PORT = dbCredentials.PORT;
    private static String HOST = dbCredentials.HOST;
    private static String DATABASE_NAME = dbCredentials.databaseName;
    private static String PASSWORD = dbCredentials.PASSWORD;
    private static String DATABASE_URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE_NAME + "?autoReconnect=true&useSSL=true&requireSSL=true";

    public static void main(String[] args) {
//        Connection conn = getSqlConnection();
//        try {//example query
//            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM trips");
//            ResultSet resultSet = stmt.executeQuery();
//            while (resultSet.next()) {
//                System.out.println(resultSet.getString("service_id")+ "   " + resultSet.getString("trip_id")+ "   "+ resultSet.getString("trip_headsign"));
//            }
//            resultSet.close();
//            stmt.close();
            System.out.println(fetchRoutesById("1069"));
//            conn.close();
//        } catch (SQLException e) {
//            System.err.println("Error: " + e.getMessage());
//        }
    }

    private static Connection getSqlConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error establishing database connection: " + e.getMessage());
            return null;
        }
    }

    public static Agency fetchAgencyById(String agencyId) {
        Connection conn = getSqlConnection();
        if (conn == null) return null;
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM agency WHERE agency_id = ?");
            stmt.setString(1, agencyId);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                Agency agency = new Agency();
                agency.setAgencyId(resultSet.getString("agency_id"));
                agency.setAgencyName(resultSet.getString("agency_name"));
                agency.setAgencyUrl(resultSet.getString("agency_url"));
                agency.setAgencyTimezone(resultSet.getString("agency_timezone"));
                agency.setAgencyLang(resultSet.getString("agency_lang"));
                agency.setAgencyPhone(resultSet.getString("agency_phone"));
                agency.setAgencyFareUrl(resultSet.getString("agency_fare_url"));
                resultSet.close();
                stmt.close();
                conn.close();
                return agency;
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return null;
    }

    public static boolean setAgency(Agency agency) {
        Connection conn = getSqlConnection();
        if (conn == null) return false;
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO agency (agency_id, agency_name, agency_url, agency_timezone, agency_lang, agency_phone, agency_fare_url) VALUES (?, ?, ?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE agency_name = VALUES(agency_name), agency_url = VALUES(agency_url), agency_timezone = VALUES(agency_timezone), agency_lang = VALUES(agency_lang), agency_phone = VALUES(agency_phone), agency_fare_url = VALUES(agency_fare_url)"
            );
            stmt.setString(1, agency.getAgencyId());
            stmt.setString(2, agency.getAgencyName());
            stmt.setString(3, agency.getAgencyUrl());
            stmt.setString(4, agency.getAgencyTimezone());
            stmt.setString(5, agency.getAgencyLang());
            stmt.setString(6, agency.getAgencyPhone());
            stmt.setString(7, agency.getAgencyFareUrl());
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            conn.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return false;
    }

    public static CalendarDates fetchCalendarDatesByServiceId(int serviceId) {
        Connection conn = getSqlConnection();
        if (conn == null) return null;
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM calendar_dates WHERE service_id = ?");
            stmt.setInt(1, serviceId);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                CalendarDates calendarDates = new CalendarDates();
                calendarDates.setServiceId(resultSet.getInt("service_id"));
                calendarDates.setDate(resultSet.getDate("date"));
                calendarDates.setExceptionType(resultSet.getInt("exception_type"));
                resultSet.close();
                stmt.close();
                conn.close();
                return calendarDates;
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return null;
    }

    public static boolean setCalendarDates(CalendarDates calendarDates) {
        Connection conn = getSqlConnection();
        if (conn == null) return false;
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO calendar_dates (service_id, date, exception_type) VALUES (?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE date = VALUES(date), exception_type = VALUES(exception_type)"
            );
            stmt.setInt(1, calendarDates.getServiceId());
            stmt.setDate(2, calendarDates.getDate());
            stmt.setInt(3, calendarDates.getExceptionType());
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            conn.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return false;
    }

    public static FeedInfo fetchFeedInfoByPublisherName(String feedPublisherName) {
        Connection conn = getSqlConnection();
        if (conn == null) return null;
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM feed_info WHERE feed_publisher_name = ?");
            stmt.setString(1, feedPublisherName);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                FeedInfo feedInfo = new FeedInfo();
                feedInfo.setFeedPublisherName(resultSet.getString("feed_publisher_name"));
                feedInfo.setFeedPublisherUrl(resultSet.getString("feed_publisher_url"));
                feedInfo.setFeedLang(resultSet.getString("feed_lang"));
                feedInfo.setFeedStartDate(resultSet.getDate("feed_start_date"));
                feedInfo.setFeedEndDate(resultSet.getDate("feed_end_date"));
                feedInfo.setFeedVersion(resultSet.getString("feed_version"));
                resultSet.close();
                stmt.close();
                conn.close();
                return feedInfo;
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return null;
    }

    public static boolean setFeedInfo(FeedInfo feedInfo) {
        Connection conn = getSqlConnection();
        if (conn == null) return false;
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO feed_info (feed_publisher_name, feed_publisher_url, feed_lang, feed_start_date, feed_end_date, feed_version) VALUES (?, ?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE feed_publisher_url = VALUES(feed_publisher_url), feed_lang = VALUES(feed_lang), feed_start_date = VALUES(feed_start_date), feed_end_date = VALUES(feed_end_date), feed_version = VALUES(feed_version)"
            );
            stmt.setString(1, feedInfo.getFeedPublisherName());
            stmt.setString(2, feedInfo.getFeedPublisherUrl());
            stmt.setString(3, feedInfo.getFeedLang());
            stmt.setDate(4, feedInfo.getFeedStartDate());
            stmt.setDate(5, feedInfo.getFeedEndDate());
            stmt.setString(6, feedInfo.getFeedVersion());
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            conn.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return false;
    }

    public static Routes fetchRoutesById(String routeId) {
        Connection conn = getSqlConnection();
        if (conn == null) return null;
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM routes WHERE route_id = ?");
            stmt.setString(1, routeId);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                Routes route = new Routes();
                route.setRouteId(resultSet.getString("route_id"));
                route.setAgencyId(resultSet.getString("agency_id"));
                route.setRouteShortName(resultSet.getString("route_short_name"));
                route.setRouteLongName(resultSet.getString("route_long_name"));
                route.setRouteDesc(resultSet.getString("route_desc"));
                route.setRouteType(resultSet.getInt("route_type"));
                route.setRouteUrl(resultSet.getString("route_url"));
                route.setRouteColor(resultSet.getString("route_color"));
                route.setRouteTextColor(resultSet.getString("route_text_color"));
                resultSet.close();
                stmt.close();
                conn.close();
                return route;
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return null;
    }

    public static boolean setRoutes(Routes route) {
        Connection conn = getSqlConnection();
        if (conn == null) return false;
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO routes (route_id, agency_id, route_short_name, route_long_name, route_desc, route_type, route_url, route_color, route_text_color) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE agency_id = VALUES(agency_id), route_short_name = VALUES(route_short_name), route_long_name = VALUES(route_long_name), route_desc = VALUES(route_desc), route_type = VALUES(route_type), route_url = VALUES(route_url), route_color = VALUES(route_color), route_text_color = VALUES(route_text_color)"
            );
            stmt.setString(1, route.getRouteId());
            stmt.setString(2, route.getAgencyId());
            stmt.setString(3, route.getRouteShortName());
            stmt.setString(4, route.getRouteLongName());
            stmt.setString(5, route.getRouteDesc());
            stmt.setInt(6, route.getRouteType());
            stmt.setString(7, route.getRouteUrl());
            stmt.setString(8, route.getRouteColor());
            stmt.setString(9, route.getRouteTextColor());
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            conn.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return false;
    }

    public static Shapes fetchShapesById(int shapeId) {
        Connection conn = getSqlConnection();
        if (conn == null) return null;
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM shapes WHERE shape_id = ?");
            stmt.setInt(1, shapeId);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                Shapes shape = new Shapes();
                shape.setShapeId(resultSet.getInt("shape_id"));
                shape.setShapePtSequence(resultSet.getInt("shape_pt_sequence"));
                shape.setShapePtLat(resultSet.getFloat("shape_pt_lat"));
                shape.setShapePtLon(resultSet.getFloat("shape_pt_lon"));
                shape.setShapeDistTraveled(resultSet.getFloat("shape_dist_traveled"));
                resultSet.close();
                stmt.close();
                conn.close();
                return shape;
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return null;
    }

    public static boolean setShapes(Shapes shape) {
        Connection conn = getSqlConnection();
        if (conn == null) return false;
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO shapes (shape_id, shape_pt_sequence, shape_pt_lat, shape_pt_lon, shape_dist_traveled) VALUES (?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE shape_pt_sequence = VALUES(shape_pt_sequence), shape_pt_lat = VALUES(shape_pt_lat), shape_pt_lon = VALUES(shape_pt_lon), shape_dist_traveled = VALUES(shape_dist_traveled)"
            );
            stmt.setInt(1, shape.getShapeId());
            stmt.setInt(2, shape.getShapePtSequence());
            stmt.setFloat(3, shape.getShapePtLat());
            stmt.setFloat(4, shape.getShapePtLon());
            stmt.setFloat(5, shape.getShapeDistTraveled());
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            conn.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return false;
    }

    public static StopTimes fetchStopTimesByTripId(int tripId) {
        Connection conn = getSqlConnection();
        if (conn == null) return null;
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM stop_times WHERE trip_id = ?");
            stmt.setInt(1, tripId);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                StopTimes stopTimes = new StopTimes();
                stopTimes.setTripId(resultSet.getInt("trip_id"));
                stopTimes.setStopSequence(resultSet.getInt("stop_sequence"));
                stopTimes.setStopHeadsign(resultSet.getString("stop_headsign"));
                stopTimes.setArrivalTime(resultSet.getTime("arrival_time"));
                stopTimes.setDepartureTime(resultSet.getTime("departure_time"));
                stopTimes.setPickupType(resultSet.getInt("pickup_type"));
                stopTimes.setDropOffType(resultSet.getInt("drop_off_type"));
                stopTimes.setTimepoint(resultSet.getInt("timepoint"));
                stopTimes.setShapeDistTraveled(resultSet.getFloat("shape_dist_traveled"));
                resultSet.close();
                stmt.close();
                conn.close();
                return stopTimes;
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return null;
    }

    public static boolean setStopTimes(StopTimes stopTimes) {
        Connection conn = getSqlConnection();
        if (conn == null) return false;
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO stop_times (trip_id, stop_sequence, stop_headsign, arrival_time, departure_time, pickup_type, drop_off_type, timepoint, shape_dist_traveled) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE stop_sequence = VALUES(stop_sequence), stop_headsign = VALUES(stop_headsign), arrival_time = VALUES(arrival_time), departure_time = VALUES(departure_time), pickup_type = VALUES(pickup_type), drop_off_type = VALUES(drop_off_type), timepoint = VALUES(timepoint), shape_dist_traveled = VALUES(shape_dist_traveled)"
            );
            stmt.setInt(1, stopTimes.getTripId());
            stmt.setInt(2, stopTimes.getStopSequence());
            stmt.setString(3, stopTimes.getStopHeadsign());
            stmt.setTime(4, stopTimes.getArrivalTime());
            stmt.setTime(5, stopTimes.getDepartureTime());
            stmt.setInt(6, stopTimes.getPickupType());
            stmt.setInt(7, stopTimes.getDropOffType());
            stmt.setInt(8, stopTimes.getTimepoint());
            stmt.setFloat(9, stopTimes.getShapeDistTraveled());
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            conn.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return false;
    }

    public static Stops fetchStopsById(int stopId) {
        Connection conn = getSqlConnection();
        if (conn == null) return null;
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM stops WHERE stop_id = ?");
            stmt.setInt(1, stopId);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
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
                resultSet.close();
                stmt.close();
                conn.close();
                return stop;
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return null;
    }

    public static boolean setStops(Stops stop) {
        Connection conn = getSqlConnection();
        if (conn == null) return false;
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO stops (stop_id, stop_code, stop_name, stop_lat, stop_lon, location_type, parent_station, stop_timezone, wheelchair_boarding, platform_code, zone_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE stop_code = VALUES(stop_code), stop_name = VALUES(stop_name), stop_lat = VALUES(stop_lat), stop_lon = VALUES(stop_lon), location_type = VALUES(location_type), parent_station = VALUES(parent_station), stop_timezone = VALUES(stop_timezone), wheelchair_boarding = VALUES(wheelchair_boarding), platform_code = VALUES(platform_code), zone_id = VALUES(zone_id)"
            );
            stmt.setInt(1, stop.getStopId());
            stmt.setString(2, stop.getStopCode());
            stmt.setString(3, stop.getStopName());
            stmt.setFloat(4, stop.getStopLat());
            stmt.setFloat(5, stop.getStopLon());
            stmt.setInt(6, stop.getLocationType());
            stmt.setString(7, stop.getParentStation());
            stmt.setString(8, stop.getStopTimezone());
            stmt.setInt(9, stop.getWheelchairBoarding());
            stmt.setString(10, stop.getPlatformCode());
            stmt.setString(11, stop.getZoneId());
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            conn.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return false;
    }

    public static Transfers fetchTransfersByFromStopId(int fromStopId) {
        Connection conn = getSqlConnection();
        if (conn == null) return null;
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM transfers WHERE from_stop_id = ?");
            stmt.setInt(1, fromStopId);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                Transfers transfer = new Transfers();
                transfer.setFromStopId(resultSet.getInt("from_stop_id"));
                transfer.setToStopId(resultSet.getInt("to_stop_id"));
                transfer.setFromRouteId(resultSet.getInt("from_route_id"));
                transfer.setToRouteId(resultSet.getInt("to_route_id"));
                transfer.setFromTripId(resultSet.getInt("from_trip_id"));
                transfer.setToTripId(resultSet.getInt("to_trip_id"));
                transfer.setTransferType(resultSet.getInt("transfer_type"));
                resultSet.close();
                stmt.close();
                conn.close();
                return transfer;
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return null;
    }

    public static boolean setTransfers(Transfers transfer) {
        Connection conn = getSqlConnection();
        if (conn == null) return false;
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO transfers (from_stop_id, to_stop_id, from_route_id, to_route_id, from_trip_id, to_trip_id, transfer_type) VALUES (?, ?, ?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE to_stop_id = VALUES(to_stop_id), from_route_id = VALUES(from_route_id), to_route_id = VALUES(to_route_id), from_trip_id = VALUES(from_trip_id), to_trip_id = VALUES(to_trip_id), transfer_type = VALUES(transfer_type)"
            );
            stmt.setInt(1, transfer.getFromStopId());
            stmt.setInt(2, transfer.getToStopId());
            stmt.setInt(3, transfer.getFromRouteId());
            stmt.setInt(4, transfer.getToRouteId());
            stmt.setInt(5, transfer.getFromTripId());
            stmt.setInt(6, transfer.getToTripId());
            stmt.setInt(7, transfer.getTransferType());
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            conn.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return false;
    }

    public static Trips fetchTripsById(String tripId) {
        Connection conn = getSqlConnection();
        if (conn == null) return null;
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM trips WHERE trip_id = ?");
            stmt.setString(1, tripId);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                Trips trip = new Trips();
                trip.setRouteId(resultSet.getString("route_id"));
                trip.setServiceId(resultSet.getString("service_id"));
                trip.setTripId(resultSet.getString("trip_id"));
                trip.setTripHeadsign(resultSet.getString("trip_headsign"));
                trip.setTripShortName(resultSet.getString("trip_short_name"));
                trip.setDirectionId(resultSet.getInt("direction_id"));
                trip.setBlockId(resultSet.getString("block_id"));
                trip.setShapeId(resultSet.getString("shape_id"));
                trip.setWheelchairAccessible(resultSet.getInt("wheelchair_accessible"));
                trip.setBikesAllowed(resultSet.getInt("bikes_allowed"));
                resultSet.close();
                stmt.close();
                conn.close();
                return trip;
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return null;
    }

    public static boolean setTrips(Trips trip) {
        Connection conn = getSqlConnection();
        if (conn == null) return false;
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO trips (route_id, service_id, trip_id, trip_headsign, trip_short_name, direction_id, block_id, shape_id, wheelchair_accessible, bikes_allowed) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE service_id = VALUES(service_id), trip_headsign = VALUES(trip_headsign), trip_short_name = VALUES(trip_short_name), direction_id = VALUES(direction_id), block_id = VALUES(block_id), shape_id = VALUES(shape_id), wheelchair_accessible = VALUES(wheelchair_accessible), bikes_allowed = VALUES(bikes_allowed)"
            );
            stmt.setString(1, trip.getRouteId());
            stmt.setString(2, trip.getServiceId());
            stmt.setString(3, trip.getTripId());
            stmt.setString(4, trip.getTripHeadsign());
            stmt.setString(5, trip.getTripShortName());
            stmt.setInt(6, trip.getDirectionId());
            stmt.setString(7, trip.getBlockId());
            stmt.setString(8, trip.getShapeId());
            stmt.setInt(9, trip.getWheelchairAccessible());
            stmt.setInt(10, trip.getBikesAllowed());
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            conn.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return false;
    }
}

