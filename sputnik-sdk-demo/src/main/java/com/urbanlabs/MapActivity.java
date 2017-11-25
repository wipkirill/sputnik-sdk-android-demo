package com.urbanlabs;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.urbanlabs.mapview.MapView;
import com.urbanlabs.mapview.SputnikControlTileLayer;
import com.urbanlabs.mapview.SputnikMapView;
import com.urbanlabs.mapview.SputnikSVGUrlRewrite;
import com.urbanlabs.mapview.SputnikUrlRewrite;
import com.urbanlabs.mapview.gcs.crs.EPSG_3857;
import com.urbanlabs.mapview.layer.TileLayer;
import com.urbanlabs.mapview.layer.TileLayerOptions;
import com.urbanlabs.mapview.layer.tile.BitmapTileSource;
import com.urbanlabs.mapview.layer.tile.SvgTileSource;
import com.urbanlabs.mapview.primitive.Pixel;
import com.urbanlabs.pane.SputnikBubbleMarkerOverlay;
import com.urbanlabs.sdk.Sputnik;
import com.urbanlabs.sdk.SputnikConsts;
import com.urbanlabs.sdk.SputnikException;
import com.urbanlabs.sdk.callback.BasicCallback;
import com.urbanlabs.sdk.callback.GetCallback;
import com.urbanlabs.sdk.query.RouteQuery;
import com.urbanlabs.sdk.response.GraphList;
import com.urbanlabs.sdk.response.MapInfo;
import com.urbanlabs.sdk.response.RouteResponse;
import com.urbanlabs.sdk.response.SearchResult;
import com.urbanlabs.sdk.util.Bounds;
import com.urbanlabs.sdk.util.LatLon;
import com.urbanlabs.ui.adapter.MapSearchAdapter;
import com.urbanlabs.ui.widget.ClearableEditText;
import com.urbanlabs.ui.widget.MatchedTagsContainer;
import com.urbanlabs.ui.widget.SputnikDialogStatus;

import java.util.ArrayList;

/**
 * Created by kirill on 1/25/14.
 */
public class MapActivity extends Activity implements MatchedTagsContainer.Refreshable {
    // mapview controllers
    private SputnikMapView mapView_;
    // search box
    private ClearableEditText searchTerm_;
    // search results
    private ListView listResults_;
    private MapSearchAdapter searchAdapter_;
    private MatchedTagsContainer matchedTags_;

    long idle_min = 200; // after user stops typing
    long last_text_edit = 0;
    Handler h = new Handler();
    private String mapName_;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_map);
        matchedTags_ = (MatchedTagsContainer)findViewById(R.id.matchedTags);
        matchedTags_.setListener(this);
        listResults_ = (ListView)findViewById(R.id.listViewResults);
        searchAdapter_ = new MapSearchAdapter(listResults_.getContext(), R.layout.search_item, new ArrayList<SearchResult>());
        listResults_.setAdapter(searchAdapter_);
        listResults_.setOnItemClickListener(clk);
        Intent intent = getIntent();

        if(intent != null) {
            mapName_ = intent.getStringExtra("mapName");
            searchAdapter_.setMapName(mapName_);
            matchedTags_.setMapName(mapName_);
            if(mapName_ != null && !mapName_.isEmpty()) {
                Sputnik.getMapInfo(mapName_, initHandler);
            }
        }

        // set uncaught exception handler
        Thread.UncaughtExceptionHandler mUEHandler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Log.e("[UNCAUGHT]", "exception", e);
            }
        };
        Thread.setDefaultUncaughtExceptionHandler(mUEHandler);

        mapView_ = (SputnikMapView)findViewById(R.id.MapView);

        searchTerm_ = (ClearableEditText)findViewById(R.id.searchTerm);
        searchTerm_.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                last_text_edit = System.currentTimeMillis();
                h.postDelayed(input_finish_checker, idle_min);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}
        });
        searchTerm_.setListener(new ClearableEditText.Listener() {
            public void didClearText() {
                searchAdapter_.clear();
                matchedTags_.clear();
                searchAdapter_.notifyDataSetChanged();
            }
        });
    }

    private GetCallback<MapInfo> initHandler = new GetCallback<MapInfo>() {
        @Override
        public void done(MapInfo mapInfo, SputnikException parseException) {
            try {
                if(mapInfo.hasFeature(SputnikConsts.FEATURE_TILES)) {
                    mapView_.setMapInfo(mapInfo);
                } else {
                    // report error
                    reportError();
                }
            } catch (Exception e) {
                e.printStackTrace();
                reportError();
            }
        }
    };

    /**
     *
     */
    private Runnable input_finish_checker = new Runnable() {
        public void run() {
            if (System.currentTimeMillis() > (last_text_edit + idle_min - 500)) {
                refreshSearchResults();
            }
        }
    };

    @Override
    public void refreshSearchResults() {
        // user hasn't changed the EditText for longer than
        // the min delay (with half second buffer window)
        String term = searchTerm_.getText().toString();
        if(matchedTags_.getSelectedTags().size() > 0 || term.length() > 1) {
            searchAdapter_.query(term, matchedTags_.getSelectedTags());
            matchedTags_.refresh(term);
            return;
        }
        if(term.length() == 0) {
            searchAdapter_.clear();
            searchAdapter_.notifyDataSetChanged();
            matchedTags_.clear();
        }
    }

    private AdapterView.OnItemClickListener clk = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            SearchResult item = searchAdapter_.getItem(i);
            if(item != null) {
                mapView_.addBubble(item);
            }
        }
    };

    private void reportError() {
        Toast.makeText(this, getString(R.string.loadMapFailed), Toast.LENGTH_LONG).show();
    }
}

