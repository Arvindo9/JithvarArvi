package org.ptindia.jithvar.handler;

/**
 * Created by Arvindo Mondal on 9/8/17.
 * Company name Jithvar
 * Email arvindo@jithvar.com
 */
public class TrackData {

    private String employeeId;
    private String empNameToTrack;
    private String latitude;
    private String longitude;
    private String location;
    private String employeeName;
    private String status;
    private String date;
    private String time;

    public TrackData(String employeeId, String employeeName, String Status){

        this.employeeId = employeeId;
        this.employeeName = employeeName;
        status = Status;
    }

    public TrackData(String employeeId, String empNameToTrack,
                     String latitude, String longitude, String location,
                     String status, String date, String time) {

        this.employeeId = employeeId;
        this.empNameToTrack = empNameToTrack;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;
        this.status = status;
        this.date = date;
        this.time = time;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getStatus() {
        return status;
    }

    public String getEmpNameToTrack() {
        return empNameToTrack;
    }

    public String getLattitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLocation() {
        return location;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}
