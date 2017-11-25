package com.urbanlabs.sdk.response;

import com.urbanlabs.sdk.dict.TUtil;
import com.urbanlabs.sdk.util.LatLon;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class represents a typical search result
 * output of a @link SearchQuery.find
 */
public class SearchResult extends AbstractResponse {
    private Long id_ = -1L;
    private LatLon coords_;
    private Map<String, String> tags_ = new HashMap<>();
    public enum OSM_TYPE {
        UNKNOWN,
        NODE,
        WAY,
        RELATION
    }

    /**
     *
     * @param data
     * @throws Exception
     */
    public SearchResult(JSONObject data) throws Exception {
        id_ = TUtil.getId(data);
        coords_ = TUtil.getLatLon(data);
        JSONObject tags = TUtil.getTags(data);
        Iterator<String> it = tags.keys();
        while (it.hasNext()) {
            String k = it.next();
            tags_.put(k, tags.getString(k));
        }
    }

    /**
     * OSM id
     * @return
     */
    public Long getId() {
        return id_;
    }

    /**
     * OSM tags
     * @return
     */
    public Map<String, String> getTags() {
        return tags_;
    }

    /**
     * Geographical coordinates object
     * @return
     */
    public LatLon getCoords() {
        return coords_;
    }

    /**
     * Checks whether this OSM object has "name" tag
     * @return
     */
    public boolean hasName() {
        return tags_.containsKey(TUtil.OSM_NAME);
    }

    /**
     * Returns value of tagName for this object
     * @param tagName
     * @return
     */
    public String getTag(String tagName) {
        return tags_.get(tagName);
    }

    /**
     * Add OSM tags to object
     * @param tag
     * @param tagValue
     */
    public void putTagVal(String tag, String tagValue) {
        tags_.put(tag, tagValue);
    }

    /**
     * Returns OSM type
     * @return
     */
    public OSM_TYPE getType() {
        String type = getTag(TUtil.OSM_TYPE);
        if(type == null)
            return OSM_TYPE.UNKNOWN;
        if(type.equals(TUtil.OSM_NODE))
            return OSM_TYPE.NODE;
        if(type.equals(TUtil.OSM_WAY))
            return OSM_TYPE.WAY;
        if(type.equals(TUtil.OSM_REL))
            return OSM_TYPE.RELATION;
        return OSM_TYPE.UNKNOWN;
    }
}
