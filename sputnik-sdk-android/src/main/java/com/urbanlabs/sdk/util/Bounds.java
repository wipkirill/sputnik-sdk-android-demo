package com.urbanlabs.sdk.util;

import com.urbanlabs.sdk.dict.TUtil;
import org.json.JSONObject;

/**
 * Created by kirill on 12.02.15.
 */
public class Bounds {
    private double [][] bounds_;
    public Bounds(double [][] bounds) {
        bounds_ = new double [bounds.length][];
        for(int i = 0; i < bounds.length; i++)
            bounds_[i] = bounds[i].clone();
    }

    private double getMin(int idx) {
        return bounds_[idx][0];
    }

    private double getMax(int idx) {
        return bounds_[idx][1];
    }

    private double getCenter(int idx) {
        return (bounds_[idx][0]+bounds_[idx][1])/2;
    }

    public double centerLat() {
        return getCenter(1);
    }

    public double centerLon() {
        return getCenter(0);
    }

    public LatLon getNorthWest() {
        if(bounds_.length > 0)
            return new LatLon(bounds_[0][0], bounds_[0][1]);
        return null;
    }

    public LatLon getSouthEast() {
        if(bounds_.length > 0)
            return new LatLon(bounds_[1][0], bounds_[1][1]);
        return null;
    }

    public static Bounds readJson(JSONObject bRoot) throws Exception {
        double[] coord1 = TUtil.readDoubleArray(bRoot.getJSONArray(TUtil.BBOX_NORTH_WEST));
        double[] coord2 = TUtil.readDoubleArray(bRoot.getJSONArray(TUtil.BBOX_SOUTH_EAST));
        double[][] res = {coord1, coord2};
        return new Bounds(res);
    }
}
