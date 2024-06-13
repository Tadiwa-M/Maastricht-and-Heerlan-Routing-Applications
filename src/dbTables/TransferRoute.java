package dbTables;

import java.time.Duration;

import static dbTables.BusRoute.getDuration;

public class TransferRoute {
    private BusRoute startRoute;
    private BusRoute endRoute;
    private String startTime;
    private String endTime;

    public TransferRoute(BusRoute startRoute, BusRoute endRoute) {
        this.startRoute = startRoute;
        this.endRoute = endRoute;

        if(startRoute.getBusStops().isEmpty() || endRoute.getBusStops().isEmpty()) {
            System.out.println("Route is empty");
            return;
        }
        this.startTime = startRoute.getBusStops().get(0).getDepartureTime();
        this.endTime = endRoute.getBusStops().get(endRoute.getBusStops().size() - 1).getArrivalTime();
    }

    public TransferRoute(BusRoute startRoute, BusRoute endRoute, String startTime) {
        this.startRoute = startRoute;
        this.endRoute = endRoute;

        if(startRoute.getBusStops().isEmpty() || endRoute.getBusStops().isEmpty()) {
            System.out.println("Route is empty");
            return;
        }

        this.startTime = startTime;
        this.endTime = endRoute.getBusStops().get(endRoute.getBusStops().size() - 1).getArrivalTime();
    }

    public String getTransferLine() {
        return startRoute.getBusStops().get(0).getRouteName() + " -> " + endRoute.getBusStops().get(0).getRouteName();
    }

    public BusRoute getStartRoute() {
        return startRoute;
    }

    public BusRoute getEndRoute() {
        return endRoute;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public int calculateTripTime() {
        Duration duration = getDuration(startTime, endTime);

        // Return the duration in minutes
        return (int) duration.toMinutes();
    }
}
