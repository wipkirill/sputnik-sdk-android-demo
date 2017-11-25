package com.urbanlabs.mapview;


import android.util.Log;
import com.urbanlabs.mapview.layer.ILayer;
import com.urbanlabs.mapview.layer.ILayerMapControllerNotifiable;
import com.urbanlabs.sdk.Sputnik;
import com.urbanlabs.sdk.SputnikException;
import com.urbanlabs.sdk.callback.BasicCallback;
import java.util.LinkedList;

/**
 *
 */
public class SputnikControlTileLayer implements ILayerMapControllerNotifiable, ILayer {
    // is there an outstanding load maps request
    private LinkedList<Integer> zoomReqs_;
    private boolean loadMapsInProgress_;
    private MapController map_;
    private String mapName_;

    public SputnikControlTileLayer(String mapName) {
        zoomReqs_ = new LinkedList<Integer>();
        loadMapsInProgress_ = false;
        mapName_ = mapName;
    }

    public void init() {
        if(!loadMapsInProgress_)  {
            loadMapsInProgress_ = true;
            Sputnik.loadTiles(mapName_, new BasicCallback() {
                @Override
                public void done(SputnikException e) {
                    if(e == null) {
                        if(zoomReqs_.size() > 0) {
                            // change the zoom level on the server
                            Sputnik.setZoom(mapName_, zoomReqs_.getFirst(), new BasicCallback() {
                                    @Override
                                    public void done(SputnikException e) {
                                        if(e == null) {
                                            map_.notifyReady();
                                        } else {
                                            Log.v("[DEBUG]", e.getMessage());
                                            map_.notifyError("Couldn't set zoom");
                                        }
                                    }
                                });
                            zoomReqs_.removeFirst();
                        } else {
                            map_.notifyReady();
                        }
                    loadMapsInProgress_ = false;
                    } else {
                        loadMapsInProgress_ = false;
                        map_.notifyError(e.getMessage());
                    }
                }
            });
        }
    }

    public boolean setZoom(int zoom) {
        if(loadMapsInProgress_) {
            zoomReqs_.add(zoom);
            if(zoomReqs_.size() > 1)
                zoomReqs_.removeFirst();
        } else {
            // change the zoom level on the server
            Sputnik.setZoom(mapName_, zoom, new BasicCallback() {
                @Override
                public void done(SputnikException e) {
                    if(e == null) {
                        map_.notifyRedraw();
                    } else {
                        Log.v("[DEBUG]", e.getMessage());
                        map_.notifyError("Couldn't set zoom");
                    }
                }
            });
        }

        return true;
    }

    @Override
    public void setMapController(MapController map) {
        map_ = map;
    }
}
