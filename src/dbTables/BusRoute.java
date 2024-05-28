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

    public BusRoute(List<BusStop> busStops) {
        this.busStops = busStops;
    }

    public List<BusStop> getBusStops() {
        return busStops;
    }

    public int calculateTripTime() {
        BusStop firstStop = busStops.get(0);
        BusStop lastStop = busStops.get(busStops.size() - 1);

        String firstDepartureTime = firstStop.getDepartureTime();
        String lastArrivalTime = lastStop.getArrivalTime();

        // Define the date-time format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        // Parse the strings into LocalDateTime objects
        LocalTime dateTime1 = LocalTime.parse(firstDepartureTime, formatter);
        LocalTime dateTime2 = LocalTime.parse(lastArrivalTime, formatter);

        // Calculate the time difference
        Duration duration = Duration.between(dateTime1, dateTime2);

        // Return the duration in minutes
        return (int) duration.toMinutes();
    }
}