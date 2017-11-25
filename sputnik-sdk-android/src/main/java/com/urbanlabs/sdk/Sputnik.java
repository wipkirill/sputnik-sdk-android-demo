package com.urbanlabs.sdk;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.urbanlabs.sdk.callback.BasicCallback;
import com.urbanlabs.sdk.callback.GetCallback;
import com.urbanlabs.sdk.callback.ListCallback;
import com.urbanlabs.sdk.dict.TUtil;
import com.urbanlabs.sdk.query.RouteQuery;
import com.urbanlabs.sdk.query.SearchQuery;
import com.urbanlabs.sdk.response.*;
import com.urbanlabs.sdk.response.json.JsonHandler;
import com.urbanlabs.sdk.response.json.ResponseHandler;
import com.urbanlabs.sdk.util.FileUtil;
import com.urbanlabs.sdk.util.KV;
import com.urbanlabs.sdk.util.LatLon;
import com.urbanlabs.sdk.util.NetworkUtil;
import com.urbanlabs.sdk.util.RequestTool;

import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class Sputnik {
    public static final String LTAG = SputnikConsts.LOG_TAG;
    private static Sputnik instance_ = null;

    private static String host_;
    private static int port_;
    // Storage
    private static boolean mExternalStorageAvailable_ = false;
    private static boolean mExternalStorageWritable_ = false;

    // Errors
    public enum ERROR {
        OK(0),
        LOADING_LIBS_FAILED(1),
        STORAGE_NOT_AVAILABLE(2),
        STORAGE_NOT_WRITABLE(3),
        STORAGE_NO_SPACE(4),
        WRITING_FAILED(5),
        SERVER_START_FAILED(6),
        SYSTEM_TEST_FAILED(7);
        private final int value;
        private ERROR(int value) {
            this.value = value;
        }
        public int getValue() {
            return this.value;
        }
    }

    private static ERROR currentError_ = ERROR.OK;

    // State
    public enum STATE {
        INIT,
        RUNNING,
        STOPPED,
        ERROR
    }
    private static STATE currentState_ = STATE.INIT;

    public static STATE getCurrentState() {
        return currentState_;
    }

    // Thread pooling
    private static ExecutorService execService_ = Executors.newCachedThreadPool();
    private static Handler handler_ = new Handler();

    // JNI
    private static native void initSputnikServer(int port, String workDir);

    private static native void stopSputnikServer();

    private static native boolean ready();

    // Context
    private static Context appContext_;
    private static BasicCallback callback_;


    private Sputnik() {
        ;
    }

    /**
     * Initialize Sputnik framework
     * @param applicationContext current Android app context
     * @param init Callback, which can handle start status
     */
    public static synchronized void init(Context applicationContext, BasicCallback init) {
        RequestTool.init(applicationContext);
        if (instance_ == null) {
            instance_ = new Sputnik();
            appContext_ = applicationContext;
            callback_ = init;
            start();
        } else {
            init.done(new SputnikException("Already running"));
        }
    }

    /**
     * Executes all intermediate steps to start the library
     */
    private static void start() {
        execService_.submit(new Runnable() {
            public void run() {
                if (loadLibraries()) {
                    if (systemCheck()) {
                        if (deployData()) {
                            startSputnik();
                        }
                    }
                }
                if (currentError_ != ERROR.OK) {
                    handler_.post(new Runnable() {
                        public void run() {
                            notifyError();
                        }
                    });
                }
                Log.v(LTAG, "Starter thread exited");
            }
        });
    }

    /**
     * Loading of system dynamic libs
     * @return true if loaded
     */
    private static boolean loadLibraries() {
        Log.v(LTAG, "Loading system libraries");
        try {
            System.loadLibrary("gnustl_shared");

            System.loadLibrary("sp_sqlite");
            System.loadLibrary("sp_png");
            System.loadLibrary("sp_xml2");
            System.loadLibrary("sp_ft2");
            System.loadLibrary("sp_mapnik");

            System.loadLibrary("sputnik");
        } catch (Throwable e) {
            currentError_ = ERROR.LOADING_LIBS_FAILED;
            currentState_ = STATE.ERROR;
            e.printStackTrace();
            return false;
        }
        Log.v(LTAG, "Done loading system libraries");
        return true;
    }

    /**
     * System storage check
     * @return true if can read/write
     */
    private static boolean systemCheck() {
        Log.v(LTAG, "System storage check");
        initStorageState();
        if (mExternalStorageAvailable_ == false) {
            currentError_ = ERROR.STORAGE_NOT_AVAILABLE;
            currentState_ = STATE.ERROR;
            return false;
        }
        if (mExternalStorageAvailable_ && !mExternalStorageWritable_) {
            currentError_ = ERROR.STORAGE_NOT_WRITABLE;
            currentState_ = STATE.ERROR;
            return false;
        }
        int minStorageMB = SputnikConsts.MIN_STORAGE_SIZE_MB;
        if (FileUtil.getAvailableSpaceInMB() < minStorageMB) {
            currentError_ = ERROR.STORAGE_NO_SPACE;
            currentState_ = STATE.ERROR;
            return false;
        }
        Log.v(LTAG, "Done system storage check");
        return true;
    }

    /**
     *
     */
    private static void initStorageState() {
        mExternalStorageAvailable_ = false;
        mExternalStorageWritable_ = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable_ = mExternalStorageWritable_ = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable_ = true;
            mExternalStorageWritable_ = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            mExternalStorageAvailable_ = mExternalStorageWritable_ = false;
        }
    }

    /**
     * Extract assets to disk drive
     * @return true if went well
     */
    private static boolean deployData() {
        Log.v(LTAG, "Deploying resources");
        File workDir = new File(getWorkDir());
        // if working directory already exist just copy common and config. Clear tiles folder
        if (!workDir.exists()) {
            if(!workDir.mkdir())
                return false;
            // if it is a new installation, deploy everything
            Log.v(LTAG, "This is new installation, deploy assets");
            try {
                FileUtil.copyFileOrDir(appContext_.getAssets(), getWorkDir(), FileUtil.ASSETS);
                //FileUtil.copyFileOrDir(appContext_.getAssets(), getWorkDir(), FileUtil.FONTS);
            } catch (Exception e) {
                Log.e(LTAG, e.getMessage());
                currentError_ = ERROR.WRITING_FAILED;
                currentState_ = STATE.ERROR;
                return false;
            }
        }
        Log.v(LTAG, "Done deploying resources");
        return true;
    }

    /**
     * Library working directory
     * @return Library working directory
     */
    public static String getWorkDir() {
        File storageDir = Environment.getExternalStorageDirectory();
        return storageDir.getAbsolutePath() + File.separator + SputnikConsts.HOME_FOLDER + File.separator;
    }

    /**
     *
     */
    public static void stop() {
        currentState_ = STATE.STOPPED;
        stopSputnikServer();
    }

    /**
     * Starts local server
     * @return true if started
     */
    private static boolean startSputnik() {
        port_ = 0;
        try {
            port_ = NetworkUtil.findFreePort();
            host_ = SputnikConsts.LOCALHOST;
        } catch (Exception e) {
            currentError_ = ERROR.SERVER_START_FAILED;
            currentState_ = STATE.ERROR;
            return false;
        }

        final String workDir = getWorkDir();
        Log.v(LTAG, "Working directory is " + workDir + ", port is " + port_);
        execService_.submit(new Runnable() {
            public void run() {
                Log.v(LTAG, "Starting Sputnik server thread...");
                initSputnikServer(port_, workDir);
                Log.v(LTAG, "Sputnik server thread exited");
            }
        });
        execService_.submit(new Runnable() {
            public void run() {
                int attempts = 0;
                boolean success = false;
                while (!(success = ready()) && attempts < 15) {
                    Log.v(LTAG, "Asking for server ready");
                    try {
                        Thread.sleep(1000);
                        attempts++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (success) {
                    Log.v(LTAG, "Server is ready");
                    handler_.post(new Runnable() {
                        public void run() {
                            currentState_ = STATE.RUNNING;
                            notifySuccess();
                        }
                    });
                } else {
                    Log.v(LTAG, "Server hasn't been started, reporting error");
                    handler_.post(new Runnable() {
                        public void run() {
                            currentState_ = STATE.ERROR;
                            currentError_ = ERROR.SERVER_START_FAILED;
                            notifyError();
                        }
                    });
                }
                Log.v(LTAG, "UI initializer thread exited");
            }
        });
        return true;
    }

    /**
     * Server port
     * @return port number
     */
    public static int getPort() {
        return port_;
    }

    /**
     * Error callback
     */
    private static void notifyError() {
        SputnikException e = new SputnikException(SputnikConsts.ERRORS.get(currentError_.getValue()));
        if(callback_ != null)
            callback_.done(e);
    }

    /**
     *
     * @return
     */
    public static int getCurrentError() {
        return currentError_.getValue();
    }

    /**
     * Success callback
     */
    private static void notifySuccess() {
        if(callback_ != null)
            callback_.done(null);
    }

    /**
     * Makes network request to a localhost
     * @param resource resource name
     * @param args HTTP arguments
     * @param handler Response handler
     */
    private static void makeRequest(String resource, List<KV> args, ResponseHandler handler) {
        URL url;
        try {
            url = new URL("http", host_, port_, resource + urlEncode(args));
        } catch (MalformedURLException e) {
            handler.onError("Failed to initialise URL host:" + host_ + ",port:" + port_ + ",res:" + resource);
            return;
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url.toString(), null,
                handler, handler);
        RequestTool.add(jsonObjectRequest, resource);
    }

    /**
     *
     * @param url
     * @param handler
     */
    public static void makeRequest(URL url, ResponseHandler handler) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url.toString(), null,
                handler, handler);
        RequestTool.add(jsonObjectRequest, url.toString());
    }

    /**
     * Lists maps stored on device
     * @param callback Handler
     */
    public static void listMaps(final GetCallback<MapList> callback) {
        makeRequest(SputnikConsts.LIST_MAPS, null, new JsonHandler(new String[]{}) {
            @Override
            public void onResult(JSONObject data) {
                Log.d(LTAG, data.toString());
                try {
                    MapList resp = new MapList(data);
                    callback.done(resp, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.done(null, new SputnikException("An error occurred while getting map list"));
                }
            }

            @Override
            public void onError(String message) {
                Log.e(LTAG, message);
                callback.done(null, new SputnikException(message));
            }
        });
    }

    /**
     * Returns @MapInfo object for a mapName
     * @param mapName name of a map file
     * @param callback Response handler
     */
    public static void getMapInfo(String mapName, final GetCallback<MapInfo> callback) {
        List<KV> args = new ArrayList<>();
        args.add(new KV(TUtil.MAP_NAME, mapName));
        makeRequest(SputnikConsts.MAP_INFO, args, new JsonHandler(new String[]{}) {
            @Override
            public void onResult(JSONObject data) {
                Log.d(LTAG, data.toString());
                try {
                    MapInfo m = new MapInfo(data);
                    callback.done(m, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.done(null, new SputnikException("An error occurred while getting mapinfo"));
                }
            }

            @Override
            public void onError(String message) {
                Log.e(LTAG, message);
                callback.done(null, new SputnikException(message));
            }
        });
    }

    /**
     * Loads graph for a given map file
     * @param mapName map file name
     * @param mapType type
     * @param callback
     */
    public static void loadGraph(String mapName, String mapType, BasicCallback callback) {
        List<KV> args = new ArrayList<>();
        args.add(new KV(TUtil.MAP_NAME, mapName));
        args.add(new KV(TUtil.MAP_TYPE, mapType));
        basicRequest(SputnikConsts.LOAD_GRAPH, args, callback);
    }

    /**
     * Unloads graph and releases resources
     * @param mapName
     * @param mapType
     * @param callback
     */
    public static void unloadGraph(String mapName, String mapType, BasicCallback callback) {
        List<KV> args = new ArrayList<KV>();
        args.add(new KV(TUtil.MAP_NAME, mapName));
        args.add(new KV(TUtil.MAP_TYPE, mapType));
        basicRequest(SputnikConsts.UNLOAD_GRAPH, args, callback);
    }

    /**
     * List of loaded graphs
     * @param mapType
     * @param callback
     */
    public static void getLoadedGraphs(String mapType, final GetCallback<GraphList> callback) {
        List<KV> args = new ArrayList<>();
        args.add(new KV(TUtil.MAP_TYPE, mapType));
        makeRequest(SputnikConsts.GET_LOADED_GRAPHS, args, new JsonHandler(new String[]{}) {
            @Override
            public void onResult(JSONObject data) {
                Log.d(LTAG, data.toString());
                try {
                    GraphList g = new GraphList(data);
                    callback.done(g, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.done(null, new SputnikException("An error occurred while getting loaded graphs"));
                }
            }

            @Override
            public void onError(String message) {
                Log.e(LTAG, message);
                callback.done(null, new SputnikException(message));
            }
        });
    }

    /**
     * Routing method. Use RouteQuery for detailed set up
     * @param args
     * @param handler
     */
    public static void route(List<KV> args, final ResponseHandler handler) {
        makeRequest(SputnikConsts.ROUTE, args, handler);
    }

    /**
     * Finds nearest graph node to given lat and lon
     * @param latLon query
     * @param mapName map file name
     * @param mapType type
     * @param callback
     */
    public static void graphNearest(LatLon latLon, String mapName, String mapType, final GetCallback<GraphNearest> callback) {
        List<KV> args = new ArrayList<KV>();
        args.add(new KV(TUtil.MAP_NAME, mapName));
        args.add(new KV(TUtil.MAP_TYPE, mapType));
        args.add(new KV(TUtil.LAT, String.valueOf(latLon.lat())));
        args.add(new KV(TUtil.LON, String.valueOf(latLon.lon())));
        makeRequest(SputnikConsts.NEAREST_NEIGHBOUR, args, new JsonHandler(new String[]{}) {
            @Override
            public void onResult(JSONObject data) {
                Log.d(LTAG, data.toString());
                try {
                    GraphNearest n = new GraphNearest(data);
                    callback.done(n, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.done(null, new SputnikException("An error occurred while finding nearest neighbour"));
                }
            }

            @Override
            public void onError(String message) {
                Log.e(LTAG, message);
                callback.done(null, new SputnikException(message));
            }
        });
    }

    /**
     * Init a new route query
     * @param mapName
     * @param mapType
     * @return
     */
    public static RouteQuery getRouteQuery(String mapName, String mapType) {
        return new RouteQuery(mapName, mapType);
    }


    /**
     *
     * @param function
     * @param args
     * @param callback
     */
    private static void basicRequest(String function, List<KV> args, final BasicCallback callback) {
        makeRequest(function, args, new JsonHandler(new String[]{}) {
            @Override
            public void onResult(JSONObject data) {
                Log.d(LTAG, data.toString());
                callback.done(null);
            }

            @Override
            public void onError(String message) {
                Log.e(LTAG, message);
                callback.done(new SputnikException(message));
            }
        });
    }

    /**
     * @param args
     * @param handler
     */
    public static void search(List<KV> args, ResponseHandler handler) {
        makeRequest(SputnikConsts.SEARCH, args, handler);
    }


    /**
     * @param mapName
     * @return
     */
    public static SearchQuery getSearchQuery(String mapName) {
        return new SearchQuery(mapName);
    }

    /**
     * @param requestTag
     */
    public static void cancelByTag(String requestTag) {
        Log.d(LTAG, "Cancelling search in progress");
        RequestTool.cancelPendingRequests(requestTag);
    }

    /**
     *
     * @param mapName
     * @param latLons
     * @param objIds
     * @param fetchAddress
     * @param callback
     */
    public static void searchNearest(String mapName, List<LatLon> latLons, List<Long> objIds,
                                         boolean fetchAddress, final ListCallback<SearchResult> callback) {
        List<KV> args = new ArrayList<>();
        args.add(new KV(TUtil.MAP_NAME, mapName));
        args.add(new KV(TUtil.SOURCE, SputnikConsts.MAP_TYPE_OSM));
        args.add(new KV(TUtil.DECODE, String.valueOf(fetchAddress)));
        if(latLons != null && latLons.size() > 0) {
            Log.v(LTAG, "Finding nearest objects by latlons");
            StringBuilder st = new StringBuilder(latLons.get(0).toBracketString());
            for (int i = 1; i < latLons.size(); ++i)
                st.append(",").append(latLons.get(i).toBracketString());
            args.add(new KV(TUtil.POINTS, "[" + st.toString() + "]"));
        } else {
            if (objIds != null && objIds.size() > 0) {
                Log.v(LTAG, "Finding nearest objects by object ids");
                JSONArray oIds = new JSONArray(objIds);
                args.add(new KV(TUtil.IDS, oIds.toString()));
            } else {
                Log.v(LTAG, "Finding nearest objects skipped");
            }
        }

        makeRequest(SputnikConsts.NEAREST_OBJECT, args, new JsonHandler(new String[]{}) {
            @Override
            public void onResult(JSONObject data) {
                Log.d(LTAG, data.toString());
                try {
                    List<SearchResult> res = new ArrayList<SearchResult>();
                    JSONArray resp = data.getJSONArray(TUtil.RESPONSE);
                    for(int i = 0; i < resp.length(); ++i) {
                        res.add(new SearchResult(resp.getJSONObject(i)));
                    }
                    callback.done(res, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.done(null, new SputnikException("An error occurred while finding nearest neighbour"));
                }
            }

            @Override
            public void onError(String message) {
                Log.e(LTAG, message);
                callback.done(null, new SputnikException(message));
            }
        });
    }

    /**
     * @param mapName
     * @return
     */
    public static String getMapIconUrl(String mapName) {
        List<KV> args = new ArrayList<KV>();
        args.add(new KV(TUtil.MAP_NAME, mapName));
        URL url;
        try {
            url = new URL("http", host_, port_, SputnikConsts.MAP_ICON + urlEncode(args));
        } catch (MalformedURLException e) {
            Log.e(LTAG, "Failed to initialize URL host:" + host_ + ",port:" + port_ + ",res:" + SputnikConsts.MAP_ICON);
            return null;
        }
        return url.toString();
    }

    /**
     *
     * @param mapName
     * @param callback
     */
    public static void loadTiles(String mapName, final BasicCallback callback) {
        List<KV> args = new ArrayList<KV>();
        args.add(new KV(TUtil.MAP_NAME, mapName));
        basicRequest(SputnikConsts.LOAD_TILES, args, callback);
    }

    /**
     *
     * @param mapName
     * @param zoom
     * @param callback
     */
    public static void setZoom(String mapName, int zoom, BasicCallback callback) {
        List<KV> args = new ArrayList<>();
        args.add(new KV(TUtil.MAP_NAME, mapName));
        args.add(new KV(TUtil.ZOOM, String.valueOf(zoom)));
        basicRequest(SputnikConsts.SET_ZOOM, args, callback);
    }

    /**
     *
     * @param mapName
     * @param query
     * @param callback
     */
    public static void matchTags(String mapName, String query, final GetCallback<TagList> callback) {
        List<KV> args = new ArrayList<>();
        args.add(new KV(TUtil.MAP_NAME, mapName));
        args.add(new KV(TUtil.TAG, query));
        makeRequest(SputnikConsts.MATCH_TAGS, args, new JsonHandler(new String[]{}) {
            @Override
            public void onResult(JSONObject jsonObject) {
                try {
                    TagList t = new TagList(jsonObject);
                    callback.done(t, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.done(null, new SputnikException(e.getMessage()));
                }
            }

            @Override
            public void onError(String message) {
                Log.e(LTAG, message);
                callback.done(null, new SputnikException(message));
            }
        });
    }

    private static String urlEncode(List<KV> args) {
        StringBuilder sb = new StringBuilder();
        if (args != null)
            sb.append(URLEncodedUtils.format(args, "utf8"));
        return sb.toString() == "" ? "" : "?" + sb.toString();
    }

    public static String getLocalUrl() {
        return "http://"+ SputnikConsts.LOCALHOST+":"+String.valueOf(Sputnik.getPort());
    }
}

