package com.urbanlabs.mapview;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.WindowManager;

import com.urbanlabs.mapview.gcs.crs.EPSG_3857;
import com.urbanlabs.mapview.layer.TileLayer;
import com.urbanlabs.mapview.layer.TileLayerOptions;
import com.urbanlabs.mapview.layer.tile.BitmapTileSource;
import com.urbanlabs.mapview.layer.tile.SvgTileSource;
import com.urbanlabs.mapview.primitive.Pixel;
import com.urbanlabs.pane.SputnikBubbleMarkerOverlay;
import com.urbanlabs.sdk.Sputnik;
import com.urbanlabs.sdk.response.MapInfo;
import com.urbanlabs.sdk.response.SearchResult;
import com.urbanlabs.sdk.util.Bounds;
import com.urbanlabs.ui.widget.SputnikDialogStatus;

/**
 * Created by kirill 02.04.2015.
 */
public class SputnikMapView extends MapView {
    private Activity parentActivity_;
    private TileLayer label_;
    private SputnikDialogStatus dialog_;
    private SputnikControlTileLayer control_;
    private TileLayer background_;
    private SputnikBubbleMarkerOverlay spBm_;
    public SputnikMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parentActivity_ = (Activity) context;
    }

    public void setMapInfo(MapInfo info) {
        dialog_ = new SputnikDialogStatus(getContext());
        register(dialog_);

        // choose layer options based on dpi
        TileLayerOptions options = null;
        float density = getResources().getDisplayMetrics().density;
        if (density >= 4.0) { // xxxhdpi
            options = new TileLayerOptions.DefaultXXHDPI();
        } else if(density >= 4.0) { // xxhdpi
            options = new TileLayerOptions.DefaultXXHDPI();
        } else if (density >= 4.0) { // xhdpi
            options = new TileLayerOptions.DefaultXHDPI();
        } else if (density >= 1.5) { // hdpi
            options = new TileLayerOptions.DefaultHDPI();
        } else if(density >= 1.0) { // mdpi
            options = new TileLayerOptions.DefaultMDPI();
        } else { // ldpi
            options = new TileLayerOptions.DefaultLDPI();
        }

        // initialize map view
        String spUrl = Sputnik.getLocalUrl();
        String mapName = info.getMapFileName();

        // initialize map view
        background_ = new TileLayer(options, new BitmapTileSource(
                new SputnikUrlRewrite(spUrl, "background", mapName)
        ),
                new EPSG_3857()
        );

        label_ = new TileLayer(options, new SvgTileSource(
                new SputnikSVGUrlRewrite(spUrl, "label", mapName)
        ),
                new EPSG_3857()
        );

        control_ = new SputnikControlTileLayer(info.getMapFileName());
        add(control_);

        spBm_ = new SputnikBubbleMarkerOverlay(mapName, background_);
        add(spBm_);
        forceWindowFormat();

        Bounds bounds = info.getBoundingBox();
        // TODO: order of lat, lon is clearly broken somewhere (possibly in sdk)
        Pixel.Double pos = new Pixel.Double(bounds.centerLat(), bounds.centerLon());

        background_.setPos(pos);
        background_.setZoom(14);

//        label_.setPos(pos);
//        label_.setZoom(14);

        add(background_);
//        add(label_);
        control_.init();
    }
    private void forceWindowFormat() {
        if(parentActivity_ != null) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(parentActivity_.getWindow().getAttributes());
            lp.format = PixelFormat.RGBA_8888;
            parentActivity_.getWindow().setBackgroundDrawable(new ColorDrawable(0xff000000));
            parentActivity_.getWindow().setAttributes(lp);
        }
    }

    public void addBubble(SearchResult item) {
        spBm_.addBubble(item);
    }
}
