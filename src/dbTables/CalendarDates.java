package dbTables;

import java.sql.*;

public class CalendarDates {
    private int serviceId;
    private Date date;
    private int exceptionType;

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(int exceptionType) {
        this.exceptionType = exceptionType;
    }

}

