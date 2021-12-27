package com.techtown.bbfindingfriends;

public class FriendLocation {
    private double Latitude;
    private double Longtitude;
    private double Altitude;

    private double exception() {
        return R.integer.code_location_null;
    }

    public FriendLocation() {
        Latitude = exception();
        Longtitude = exception();
        Altitude = exception();
    }

    public FriendLocation(double latitude, double longtitude, double altitude) {
        Latitude = latitude;
        Longtitude = longtitude;
        Altitude = altitude;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public double getLongtitude() {
        return Longtitude;
    }

    public void setLongtitude(double longtitude) {
        Longtitude = longtitude;
    }

    public double getAltitude() {
        return Altitude;
    }

    public void setAltitude(double altitude) {
        Altitude = altitude;
    }
}
