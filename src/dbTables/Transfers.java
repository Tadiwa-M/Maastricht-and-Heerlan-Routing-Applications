package dbTables;

import java.sql.*;

public class Transfers {
    private int fromStopId;
    private int toStopId;
    private int fromRouteId;
    private int toRouteId;
    private int fromTripId;
    private int toTripId;
    private int transferType;

    public int getFromTripId() {
        return fromTripId;
    }

    public void setFromTripId(int fromTripId) {
        this.fromTripId = fromTripId;
    }

    public int getFromStopId() {
        return fromStopId;
    }

    public void setFromStopId(int fromStopId) {
        this.fromStopId = fromStopId;
    }

    public int getToStopId() {
        return toStopId;
    }

    public void setToStopId(int toStopId) {
        this.toStopId = toStopId;
    }

    public int getTransferType() {
        return transferType;
    }

    public void setTransferType(int transferType) {
        this.transferType = transferType;
    }

    public int getFromRouteId() {
        return fromRouteId;
    }

    public void setFromRouteId(int fromRouteId) {
        this.fromRouteId = fromRouteId;
    }

    public int getToRouteId() {
        return toRouteId;
    }

    public void setToRouteId(int toRouteId) {
        this.toRouteId = toRouteId;
    }

    public int getToTripId() {
        return toTripId;
    }

    public void setToTripId(int toTripId) {
        this.toTripId = toTripId;
    }

    @Override
    public String toString() {
        return "Transfers{" +
                "fromStopId=" + fromStopId +
                ", toStopId=" + toStopId +
                ", fromRouteId=" + fromRouteId +
                ", toRouteId=" + toRouteId +
                ", fromTripId=" + fromTripId +
                ", toTripId=" + toTripId +
                ", transferType=" + transferType +
                '}';
    }
}

