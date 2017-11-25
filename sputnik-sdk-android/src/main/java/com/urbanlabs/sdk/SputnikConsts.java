package com.urbanlabs.sdk;

import java.util.Arrays;
import java.util.List;

/**
 * Created by kirill on 10.02.15.
 */
public class SputnikConsts {
    public static final String LOG_TAG = "[Sputnik]";

    public static final String HOME_FOLDER = "Sputnik";
    public static final String LOCALHOST   = "localhost";

    // URLs
    public static final String LIST_MAPS         = "listmaps";
    public static final String MAP_INFO          = "mapinfo";
    public static final String MAP_ICON          = "mapicon";

    public static final String SEARCH            = "search/query";
    public static final String NEAREST_OBJECT    = "search/nearest";
    public static final String LOAD_TILES        = "loadtiles";
    public static final String SET_ZOOM          = "setzoom";
    public static final String GET_TILES         = "gettiles";
    public static final String MATCH_TAGS        = "matchtags";

    public static final String LOAD_GRAPH        = "graph/load";
    public static final String UNLOAD_GRAPH      = "graph/unload";
    public static final String GET_LOADED_GRAPHS = "graph/list";
    public static final String NEAREST_NEIGHBOUR = "graph/nearest";
    public static final String ROUTE             = "graph/route";

    // features
    public static final String FEATURE_TILES             = "tiles";
    public static final String FEATURE_SEARCH            = "search";
    public static final String FEATURE_ROUTING           = "routing";
    public static final String FEATURE_TURN_RESTRICTIONS = "turnrestrictions";
    public static final String FEATURE_OSM_TAGS          = "tags";
    public static final String FEATURE_ADDRESS_DECODER   = "addressdecoder";

    // map types
    public static final String MAP_TYPE_OSM = "osm";

    public static final int MIN_STORAGE_SIZE_MB = 40;

    public static final List<String> ERRORS = Arrays.asList(
            "",
            "Failed to load Sputnik system libraries",
            "SD card on your device is not available",
            "Your SD card is not writable",
            "Your SD card should have at least 50MB of free space",
            "An error occurred while storing data on your SD card",
            "An error occurred while we were starting Sputnik on your device",
            "Your system architecture is incompatible with this version of Sputnik"
    );
}
