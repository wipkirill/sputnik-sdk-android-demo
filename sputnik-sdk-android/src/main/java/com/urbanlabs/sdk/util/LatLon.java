package com.urbanlabs.sdk.util;

/**
 * Created by kirill on 2/9/14.
 */
public class LatLon {
    private double lat_;
    private double lon_;

    public LatLon(double lat_, double lon_) {
        this.lat_ = lat_;
        this.lon_ = lon_;
    }

    public double lat() {
        return lat_;
    }

    public double lon() {
        return lon_;
    }

    @Override
    public String toString() {
        return String.valueOf(lat_) + "," + String.valueOf(lon_);
    }

    public String toBracketString() {
        return "[" + String.valueOf(lat_) + "," + String.valueOf(lon_) + "]";
    }
}
