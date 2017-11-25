package com.urbanlabs.sdk.response;

import com.urbanlabs.sdk.dict.TUtil;
import com.urbanlabs.sdk.util.Bounds;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Holds meta information about a Sputnik map file
 */
public class MapInfo extends AbstractResponse {
    /**
     * Name of map file
     */
    private String mapFileName_ = "";
    /**
     * List of map features included in map file
     */
    private Set<String> mapFeatures_ = new HashSet<>();
    /**
     * Geographical bounds of area represented by this map file
     */
    private Bounds bounds_;
    /**
     * Name of area
     */
    private String areaName_;
    /**
     * Set of international country codes valid for this area
     */
    private Set<String> countryCodes_;

    public MapInfo(JSONObject data) throws JSONException {
        // extract
        JSONObject result = data.getJSONObject(TUtil.RESPONSE);
        JSONObject cTags = TUtil.getTags(result);
        mapFeatures_ = TUtil.getStringSet(TUtil.MAP_FEATURES, cTags);
        mapFileName_ = TUtil.getString(TUtil.MAP_FILE, cTags);
        areaName_ = TUtil.getString(TUtil.AREA_NAME, cTags);
        countryCodes_ = TUtil.getStringSet(TUtil.COUNTRY_CODES, cTags);
        bounds_ = TUtil.getBounds(cTags);
    }

    /**
     * Check for a feature, see SputnikConsts.FEATURE_* for details
     * @param feature
     * @return
     */
    public boolean hasFeature(String feature) {
        return mapFeatures_.contains(feature);
    }

    public Set<String> getMapFeatures() {
        return mapFeatures_;
    }

    public Bounds getBoundingBox() {
        return bounds_;
    }

    public String getMapFileName() {
        return mapFileName_;
    }

    public String getAreaName() {
        return areaName_;
    }

    public Set<String> getCountryCodes() {
        return countryCodes_;
    }
}
