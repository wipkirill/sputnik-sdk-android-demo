package com.urbanlabs.sdk.query;

import android.util.Log;
import com.urbanlabs.sdk.Sputnik;
import com.urbanlabs.sdk.SputnikException;
import com.urbanlabs.sdk.callback.GetCallback;
import com.urbanlabs.sdk.dict.TUtil;
import com.urbanlabs.sdk.response.RouteResponse;
import com.urbanlabs.sdk.response.json.JsonHandler;
import com.urbanlabs.sdk.util.KV;
import com.urbanlabs.sdk.util.LatLon;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kirill on 17.02.15.
 */
public class RouteQuery extends AbstractQuery<GetCallback<RouteResponse>> {
    private String mapName_;
    private String mapType_;
    private List<LatLon> wayPoints_ = new ArrayList<LatLon>();
    private String travelMode_ = "0";
    private String metric_ = "0";

    /**
     *
     * @param mapName
     * @param mapType
     */
    public RouteQuery(String mapName, String mapType) {
        mapName_ = mapName;
        mapType_ = mapType;
    }

    /**
     *
     * @param pt
     * @throws SputnikException
     */
    public void addWayPoint(LatLon pt) {
        if(pt != null)
            wayPoints_.add(pt);
    }

    /**
     *
     * @param pts
     */
    public void setWayPoints(List<LatLon> pts) {
        wayPoints_.clear();
        wayPoints_.addAll(pts);
    }

    /**
     *
     * @param callback
     */
    @Override
    public void find(final GetCallback<RouteResponse> callback) {
        Sputnik.route(getArgs(), new JsonHandler(new String[]{}) {
            @Override
            public void onResult(JSONObject data) {
                Log.d("DEBUG", data.toString());
                try {
                    RouteResponse r = new RouteResponse(data);
                    callback.done(r, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.done(null, new SputnikException(e.getMessage()));
                }
            }

            @Override
            public void onError(String message) {
                Log.d("ERROR", message);
                callback.done(null, new SputnikException(message));
            }
        });
    }

    /**
     *
     * @return
     */
    private List<KV> getArgs() {
        List<KV> args = new ArrayList<>();
        args.add(new KV(TUtil.MAP_NAME, mapName_));
        args.add(new KV(TUtil.MAP_TYPE, mapType_));
        args.add(new KV(TUtil.METRIC, metric_));
        args.add(new KV(TUtil.TRAVELMODE, travelMode_));

        StringBuilder wps = new StringBuilder();
        wps.append(wayPoints_.get(0).toString());
        for(int i = 1; i < wayPoints_.size(); ++i) {
            wps.append("|").append(wayPoints_.get(i).toString());
        }

        args.add(new KV(TUtil.WAYPOINTS, wps.toString()));

        return args;
    }
}
