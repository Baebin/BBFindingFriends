package com.techtown.bbfindingfriends;

import android.util.Log;

public class FriendLocation {
    private static final String TAG = "Location";

    private double exception = R.integer.code_location_null;

    private double Latitude;
    private double Longtitude;
    private double Altitude;

    public FriendLocation() {
        Latitude = exception;
        Longtitude = exception;
        Altitude = exception;
    }

    public boolean checkException() {
        if (Latitude == exception) return false;
        if (Longtitude == exception) return false;
        if (Altitude == exception) return false;

        return true;
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
        Log.d(TAG, "setLatitude(" + latitude + ")");
        Latitude = latitude;
    }

    public double getLongtitude() {
        return Longtitude;
    }

    public void setLongtitude(double longtitude) {
        Log.d(TAG, "setLongtitude(" + longtitude + ")");
        Longtitude = longtitude;
    }

    public double getAltitude() {
        return Altitude;
    }

    public void setAltitude(double altitude) {
        Log.d(TAG, "setAltitude(" + altitude + ")");
        Altitude = altitude;
    }
}
