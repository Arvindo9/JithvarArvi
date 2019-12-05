package org.ptindia.jithvar.handler;

/**
 * Created by root on 21/6/17.
 */

public class DataBaseHandler {

    private String userId;
    private String contact;
    private String status;
    private String longitude;
    private String latitude;
    private String location;
    private String tracking_TD;

    public DataBaseHandler(String tracking_TD){

        this.tracking_TD = tracking_TD;
    }

    public DataBaseHandler(String userId, String status){
        this.userId = userId;
        this.status = status;
    }

    public DataBaseHandler( String status, boolean ok){
        this.status = status;
    }

    public DataBaseHandler(String userId, String contact, int tmp){
        this.userId = userId;
        this.contact = contact;
    }

    public DataBaseHandler(String userId, String longitude, String latitude, String location){

        this.userId = userId;
        this.longitude = longitude;
        this.latitude = latitude;
        this.location = location;
    }

    public String getUserId() {
        return userId;
    }

    public String getStatus() {
        return status;
    }

    public String getContact() {
        return contact;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getTracking_TD() {
        return tracking_TD;
    }

    public void setTracking_TD(String tracking_TD) {
        this.tracking_TD = tracking_TD;
    }

    public String getLocation() {
        return location;
    }
}
