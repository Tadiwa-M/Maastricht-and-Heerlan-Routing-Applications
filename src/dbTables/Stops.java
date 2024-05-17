package dbTables;

import java.sql.*;
//XXX: incorrect variables.
public class Stops {
    private int stopId;
    private String stopCode;
    private String stopName;
    private float stopLat;
    private float stopLon;
    private int locationType;
    private String parentStation;
    private String stopTimezone;
    private int wheelchairBoarding;
    private String platformCode;
    private String zoneId;
    private String stopDesc;
    private String stopUrl;

    public String getPlatformCode() {
        return platformCode;
    }

    public void setPlatformCode(String platformCode) {
        this.platformCode = platformCode;
    }

    public void setStopCode(String stopCode) {
        this.stopCode = stopCode;
    }

    public String getStopCode() {
        return stopCode;
    }

    public int getStopId() {
        return stopId;
    }

    public void setStopId(int stopId) {
        this.stopId = stopId;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public String getStopDesc() {
        return stopDesc;
    }

    public void setStopDesc(String stopDesc) {
        this.stopDesc = stopDesc;
    }

    public float getStopLat() {
        return stopLat;
    }

    public void setStopLat(float stopLat) {
        this.stopLat = stopLat;
    }

    public float getStopLon() {
        return stopLon;
    }

    public void setStopLon(float stopLon) {
        this.stopLon = stopLon;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public String getStopUrl() {
        return stopUrl;
    }

    public void setStopUrl(String stopUrl) {
        this.stopUrl = stopUrl;
    }

    public int getLocationType() {
        return locationType;
    }

    public void setLocationType(int locationType) {
        this.locationType = locationType;
    }

    public String getParentStation() {
        return parentStation;
    }

    public void setParentStation(String parentStation) {
        this.parentStation = parentStation;
    }

    public String getStopTimezone() {
        return stopTimezone;
    }

    public void setStopTimezone(String stopTimezone) {
        this.stopTimezone = stopTimezone;
    }

    public int getWheelchairBoarding() {
        return wheelchairBoarding;
    }

    public void setWheelchairBoarding(int wheelchairBoarding) {
        this.wheelchairBoarding = wheelchairBoarding;
    }
}

