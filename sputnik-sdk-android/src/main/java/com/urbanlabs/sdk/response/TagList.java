package com.urbanlabs.sdk.response;

import com.urbanlabs.sdk.dict.TUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by днс on 20.02.2015.
 */
public class TagList extends AbstractResponse {
    private Set<String> tags_ = new HashSet<>();
    public TagList(JSONObject data) throws Exception {
        JSONObject result = data.getJSONObject(TUtil.RESPONSE);
        JSONArray tags = result.getJSONArray(TUtil.TAGS);
        for(int i = 0; i < tags.length(); ++i) {
            tags_.add(tags.getString(i));
        }
    }

    public Set<String> getTags() {
        return tags_;
    }
}
