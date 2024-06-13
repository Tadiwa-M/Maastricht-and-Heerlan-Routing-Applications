package dbTables;

import cn.hutool.core.date.DateTime;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class BusRoute {
    private List<BusStop> busStops;
    private String startTime;
    private String endTime;

    public BusRoute(List<BusStop> busStops) {
        this.busStops = busStops;

        if (busStops.isEmpty()) {
            System.out.println("Route is empty");
            return;
        }

        this.startTime = busStops.get(0).getDepartureTime();
        this.endTime = busStops.get(busStops.size() - 1).getArrivalTime();
    }

    public BusRoute(List<BusStop> busStops, String startTime) {
        this.busStops = busStops;
        this.startTime = startTime;

        if (busStops.isEmpty()) {
            System.out.println("Route is empty");
            return;
        }

        this.endTime = busStops.get(busStops.size() - 1).getArrivalTime();
    }

    public List<BusStop> getBusStops() {
        return busStops;
    }

    public int calculateTripTime() {
        Duration duration = getDuration(startTime, endTime);

        // Return the duration in minutes
        return (int) duration.toMinutes();
    }

    public static Duration getDuration(String firstDepartureTime, String lastArrivalTime) {
        // Define the date-time format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        // Parse the strings into LocalDateTime objects
        LocalTime dateTime1 = LocalTime.parse(firstDepartureTime, formatter);
        LocalTime dateTime2 = LocalTime.parse(lastArrivalTime, formatter);

        // Calculate the time difference
        return Duration.between(dateTime1, dateTime2);
    }
}
