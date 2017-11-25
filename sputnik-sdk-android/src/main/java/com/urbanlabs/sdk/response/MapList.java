package com.urbanlabs.sdk.response;
import com.urbanlabs.sdk.dict.TUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response for listmaps function
 */
public class MapList extends AbstractResponse {
    private List<String> maps_;
    public MapList(JSONObject data) throws JSONException {
        JSONArray areas = data.getJSONArray(TUtil.RESPONSE);
        maps_ = new ArrayList<>();
        for (int i = 0; i < areas.length(); ++i) {
            maps_.add(areas.getString(i));
        }
    }

    /**
     * List of map file names stored on device
     * @return
     */
    public List<String> getMaps() {
        return maps_;
    }
}
