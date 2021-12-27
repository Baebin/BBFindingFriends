package com.techtown.bbfindingfriends;

import java.util.ArrayList;

public class FriendLocationExceptionManger {
    FriendLocation friendLocation;

    private double exception = R.integer.code_location_null;

    private double Latitude;
    private double Longtitude;
    private double Altitude;

    public FriendLocationExceptionManger(FriendLocation friendLocation) {
        this.friendLocation = friendLocation;

        Latitude = friendLocation.getLatitude();
        Longtitude = friendLocation.getLongtitude();
        Altitude = friendLocation.getAltitude();
    }

    public boolean checkException() {
        if (Latitude == exception) return true;
        if (Longtitude == exception) return true;
        if (Altitude == exception) return true;

        return false;
    }

    public String getException() {
        ArrayList<Double> temp = new ArrayList<Double>(3);
        temp.add(Latitude);
        temp.add(Longtitude);
        temp.add(Altitude);

        int stack = 0;

        for (double value : temp) {
            if (value == exception) {
                stack++;
            }
        }

        if (stack == 1) {
            if (Latitude == exception) {
                return "L111";
            } else if (Longtitude == exception) {
                return "L112";
            } else {
                // Longtitude Exception
                return "L113";
            }
        } else if (stack == 2) {
            if (Latitude == exception) {
                if (Longtitude == exception) {
                    return "L114";
                } else {
                    // Altitude Exception
                    return "L116";
                }
            } else {
                // Longtitude, Altitude Exception
                return "L115";
            }
        }

        // Latitude, Longtitude, Altitude Exceptiom
        return "L117";
    }
}
