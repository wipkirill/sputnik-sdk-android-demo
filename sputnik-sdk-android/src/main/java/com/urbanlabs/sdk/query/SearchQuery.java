package com.urbanlabs.sdk.query;

import android.util.Log;
import android.util.Pair;
import com.urbanlabs.sdk.Sputnik;
import com.urbanlabs.sdk.SputnikConsts;
import com.urbanlabs.sdk.SputnikException;
import com.urbanlabs.sdk.callback.ListCallback;
import com.urbanlabs.sdk.dict.TUtil;
import com.urbanlabs.sdk.response.SearchResult;
import com.urbanlabs.sdk.response.json.JsonHandler;
import com.urbanlabs.sdk.util.KV;
import com.urbanlabs.sdk.util.LatLon;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Search API class
 */
public class SearchQuery extends AbstractQuery<ListCallback<SearchResult>>{
    private String mapName_;
    private String source_ = SputnikConsts.MAP_TYPE_OSM;
    private String query_ = "";
    private List<Pair<String, String>> tagsVals_ = new ArrayList<>();
    private Set<String> tags_ = new HashSet<>();
    private LatLon near_;
    private int limit_ = -1;
    private int offset_ = -1;
    private List<String> ids_ = new ArrayList<>();

    public SearchQuery(String mapName) {
        mapName_ = mapName;
    }

    /**
     * Actual search call
     * @param callback
     */
    @Override
    public void find(final ListCallback<SearchResult> callback) {
        Sputnik.search(getArgs(), new JsonHandler(new String[]{}) {
            @Override
            public void onResult(JSONObject data) {
                List<SearchResult> res = new ArrayList<>();
                try {
                    JSONArray resp = data.getJSONArray(TUtil.RESPONSE);
                    for(int i = 0; i < resp.length(); ++i) {
                        res.add(new SearchResult(resp.getJSONObject(i)));
                    }
                    callback.done(res, null);
                } catch (Exception e) {
                    callback.done(null, new SputnikException(e.getMessage()));
                }
            }

            @Override
            public void onError(String message) {
                Log.d("ERROR", message);
                callback.done(null, new SputnikException(message));
            }
        });
    }

    /**
     * Query string that should appear in tags of each output object.
     * @param query
     */
    public void searchTerm(String query) {
        this.query_ = query;
    }

    /**
     * Adds tag that have to be contained in each output object.
     * For example, to find all shops simply write q.addTag("shop")
     * @param tag
     */
    public void addTag(String tag) {
        tags_.add(tag);
    }

    /**
     * Tags that have to be contained in each output object. Are specified
     * by an array of OSM tags. For example, to find all shops simply
     * write q.addTags({"shop", ""})
     * @param tag
     */
    public void addTags(Set<String> tag) {
        tags_.addAll(tag);
    }

    /**
     * Key/value pairs from OSM tags that are required to be present in each output
     * object. For example, to find all schools call q.addTagVal("amenity","school")
     * @param tag
     * @param tagValue
     */
    public void addTagVal(String tag, String tagValue) {
        tagsVals_.add(new Pair<>(tag, tagValue));
    }

    /**
     * Search in proximity of this point.
     * @param searchNear
     */
    public void near(LatLon searchNear) {
        near_ = searchNear;
    }

    /**
     * Maximum number of results to output. Defaults to 10.
     * @param limit
     */
    public void limit(int limit) {
        limit_ = limit;
    }
    /**
     * The number of results to skip. Defaults to 0.
     * @param offset
     */
    public void offset(int offset) {
        offset_ = offset;
    }

    /**
     * Search only among given ids.
     * @param ids
     */
    public void setIds(List<String> ids) {
        ids_ = ids;
    }

    private List<KV> getArgs() {
        List<KV> args = new ArrayList<KV>();
        args.add(new KV(TUtil.MAP_NAME, mapName_));
        args.add(new KV(TUtil.SOURCE, source_));
        if(query_ == null)
            query_ = "";
        args.add(new KV(TUtil.QUERY, query_));
        if(offset_ >= 0)
            args.add(new KV(TUtil.OFFSET, String.valueOf(offset_)));
        if(limit_ >= 0)
            args.add(new KV(TUtil.LIMIT, String.valueOf(limit_)));
        if(near_ != null)
            args.add(new KV(TUtil.NEAR, near_.toBracketString()));

        if(tagsVals_.size() > 0) {
            StringBuilder tv = new StringBuilder();
            tv.append(tagsVals_.get(0).first).append(",").append(tagsVals_.get(0).second);
            for(int i = 1; i < tagsVals_.size(); ++i) {
                tv.append(",").append(tagsVals_.get(i).first).append(",").append(tagsVals_.get(i).second);
            }
            args.add(new KV(TUtil.TAGS_VALS, "["+tv.toString()+"]"));
        }

        if(tags_.size() > 0) {
            Object[] tagArr = tags_.toArray();
            StringBuilder tg = new StringBuilder();
            tg.append((String)tagArr[0]);
            for(int i = 1; i < tagArr.length; ++i)
                tg.append(",").append((String)tagArr[i]);

            args.add(new KV(TUtil.TAGS, "[" + tg.toString() + "]"));
        }

        if(ids_.size() > 0) {
            StringBuilder idB = new StringBuilder();
            idB.append(ids_.get(0));
            for(int i = 1; i < ids_.size(); ++i)
                idB.append(",").append(ids_.get(i));
            args.add(new KV(TUtil.IDS, "[" + idB.toString() + "]"));
        }

        return args;
    }
}
