package com.urbanlabs.pane;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.urbanlabs.R;
import com.urbanlabs.mapview.MapView;
import com.urbanlabs.mapview.gcs.crs.EPSG_3857;
import com.urbanlabs.mapview.layer.ILayerDebuggable;
import com.urbanlabs.mapview.layer.StubLayerControllable;
import com.urbanlabs.mapview.layer.TileLayer;
import com.urbanlabs.mapview.pane.BubbleMarker;
import com.urbanlabs.mapview.pane.BubbleMarkerOverlay;
import com.urbanlabs.mapview.primitive.IPoint;
import com.urbanlabs.mapview.primitive.Pixel;
import com.urbanlabs.mapview.util.NumUtils;
import com.urbanlabs.sdk.Sputnik;
import com.urbanlabs.sdk.SputnikConsts;
import com.urbanlabs.sdk.SputnikException;
import com.urbanlabs.sdk.callback.GetCallback;
import com.urbanlabs.sdk.callback.ListCallback;
import com.urbanlabs.sdk.response.GraphNearest;
import com.urbanlabs.sdk.response.SearchResult;
import com.urbanlabs.sdk.util.LatLon;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SputnikBubbleMarkerOverlay extends BubbleMarkerOverlay implements ILayerDebuggable {
    private StubLayerControllable control_;
    private String mapName_;
    private LayoutInflater inflater_;

    public SputnikBubbleMarkerOverlay(String mapName, StubLayerControllable control) {
        control_ = control;
        mapName_ = mapName;
    }

    @Override
    public void setView(MapView view) {
        inflater_ = (LayoutInflater)view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view_ = view;
    }

    @Override
    public boolean onSingleTapUp(final Pixel.Double pos) {
        IPoint loc = control_.toLayer(pos);
        List<LatLon> lList = new ArrayList<>();
        lList.add(new LatLon(loc.y(), loc.x()));

        final BubbleMarker bubble = new SputnikBubbleMarker(view_.getContext());
        bubble.setGlobalPos(map_.toGlobal(pos));
        bubble.setMarkerView((ViewGroup)inflater_.inflate(R.layout.default_marker, null));
        
        final BalloonOverlayView bv = new BalloonOverlayView(view_.getContext(), 0);
        bubble.setBubbleView(bv);
        add(bubble);

        Sputnik.searchNearest(mapName_, lList, null, true, new ListCallback<SearchResult>() {
            @Override
            public void done(List<SearchResult> list, SputnikException e) {
                // set data for bubble
                if(e == null && list.size() > 0) {
                    bv.setData(list.get(0));
                }
            }
        });

        return true;
    }

    public void addBubble(SearchResult item) {
        Pixel.Double pos = new Pixel.Double(item.getCoords().lon(), item.getCoords().lat());
        BubbleMarker bubble = new SputnikBubbleMarker(view_.getContext());
        BalloonOverlayView baloonView = new BalloonOverlayView(view_.getContext(), 0);
        
        baloonView.setData(item);
        bubble.setBubbleView(baloonView);
        bubble.setMarkerView((ViewGroup)inflater_.inflate(R.layout.default_marker, null));
        
        IPoint canvasPos = control_.toCanvas(pos);
        bubble.setGlobalPos(map_.toGlobal((Pixel.Double)canvasPos));
        add(bubble);
    }

    // BubbleMarker overrides

    @Override
    public boolean onLongPress(Pixel.Double e) {
        return false;
    }

    @Override
    public boolean onShowPress(Pixel.Double e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(Pixel.Double e) {
        return false;
    }

    @Override
    public String toDebugString() {
        String res = "Bubbles:";
        for(BubbleMarker marker : markers_) {
            IPoint gl = marker.getGlobalPos();
            IPoint pt = map_.toCanvas(marker.getGlobalPos());
            res = res+" "+ NumUtils.toString(pt.x(), 7)+":"+NumUtils.toString(pt.y(), 7)+"\n";
            res = res+" "+ NumUtils.toString(gl.x(), 7)+":"+NumUtils.toString(gl.y(), 7);
            res += "\n";
        }
        return res;
    }
}
