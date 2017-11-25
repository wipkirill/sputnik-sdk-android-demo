package com.urbanlabs.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.urbanlabs.R;
import com.urbanlabs.sdk.Sputnik;
import com.urbanlabs.sdk.SputnikException;
import com.urbanlabs.sdk.callback.ListCallback;
import com.urbanlabs.sdk.dict.TUtil;
import com.urbanlabs.sdk.dict.TagDictItem;
import com.urbanlabs.sdk.dict.TagDictionary;
import com.urbanlabs.sdk.query.SearchQuery;
import com.urbanlabs.sdk.response.SearchResult;
import com.urbanlabs.sdk.util.KV;
import com.urbanlabs.sdk.util.LatLon;
import com.urbanlabs.ui.widget.TagItemContainer;
import com.urbanlabs.ui.widget.TagItemView;
import com.urbanlabs.sdk.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class MapSearchAdapter extends ArrayAdapter<SearchResult> {
    private TagDictionary dict_;

    protected String mapName_;

    public MapSearchAdapter(Context context, int resource, List<SearchResult> values) {
        super(context, resource, values);
        dict_ = TagDictionary.getInstance(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.search_item, parent, false);
        TextView nameView = (TextView) rowView.findViewById(R.id.searchItemName);
        final TextView addrView = (TextView) rowView.findViewById(R.id.addressView);
        TagItemContainer tagCont = (TagItemContainer) rowView.findViewById(R.id.tagsContainer);
        List<KV> props = new ArrayList<>();
        try {
            SearchResult currentItem = getItem(position);

            // extract name if exists
            if(currentItem.hasName()) {
                String objName = currentItem.getTag(TUtil.OSM_NAME);
                objName = StringUtil.toUpperFirst(objName);
                nameView.setText(objName);
            } else {
                Log.v("[SEARCH]", "No name field in tags");
            }

            if(TUtil.hasAddress(currentItem)) {
                addrView.setText(TUtil.extractAddress(currentItem));
            }

            Map<String, String> tags = currentItem.getTags();
            for(Map.Entry<String, String > e : tags.entrySet()) {
                props.add(new KV(e.getKey(), e.getValue()));
            }

            List<TagDictItem> useFul = dict_.matchUsefulTags(props);
            for(TagDictItem t:useFul) {
                View tagItemView = inflater.inflate(R.layout.tag_item, parent, false);
                TagItemView tagItem = (TagItemView)tagItemView.findViewById(R.id.tagItemElement);
                tagItem.setText(t.getText());
                tagCont.addTag(tagItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rowView;
    }

    public String getMapName() {
        return mapName_;
    }

    public void setMapName(String mapName) {
        this.mapName_ = mapName;
    }

    public void query(String term, Set<String> requiredTags) {
        clear();

        SearchQuery q = Sputnik.getSearchQuery(mapName_);
        q.searchTerm(term);
        if(requiredTags != null && requiredTags.size() > 0) {
            for(String t : requiredTags) {
                TagDictItem tg = dict_.getLocalizedTag(t);
                // this is tag name, like "shop" or "railway"
                if(tg.getParent() == -1) {
                    q.addTag(t);
                } else {
                    // this is tag value, like "supermarket"(shop=supermarket) or
                    // "bar"(amenity=bar)
                    TagDictItem parent = dict_.findTagById(tg.getParent());
                    if(parent != null) {
                        q.addTagVal(parent.getOriginalOsm(), tg.getOriginalOsm());
                    }
                }
            }
        }
            //q.addTags(requiredTags);

        q.find(new ListCallback<SearchResult>() {
            @Override
            public void done(List<SearchResult> list, SputnikException e) {
                if(e == null) {
                    clear();
                    addAll(list);
                    fetchAddress();
                    notifyDataSetChanged();
                } else {
                    Log.v("[ERROR]", "Error while searching:"+e.getMessage());
                }
            }
        });
    }

    private void fetchAddress() {
        try {
            final List<Integer> indexToUpdate = new ArrayList<Integer>();
            List<LatLon> latlons = new ArrayList<LatLon>();
            for (int i = 0; i < getCount(); ++i) {
                if (!TUtil.hasAddress(getItem(i))) {
                    indexToUpdate.add(i);
                    latlons.add(getItem(i).getCoords());
                }
            }
            if(latlons.size() > 0)
                Sputnik.searchNearest(mapName_, latlons, null, true, new ListCallback<SearchResult>() {
                    @Override
                    public void done(List<SearchResult> list, SputnikException e) {
                        if(e == null) {
                            for(int i = 0; i < list.size(); ++i) {
                                int id = indexToUpdate.get(i);
                                if(id >= 0 && id < getCount()) {
                                    SearchResult rOld = getItem(id);
                                    SearchResult rNearest = list.get(i);
                                    for (String addrKey : TUtil.ADDR_TAGS) {
                                        if(rNearest.getTag(addrKey) != null && rOld.getTag(addrKey) == null)
                                            rOld.putTagVal(addrKey, rNearest.getTag(addrKey));
                                    }
                                }
                            }
                            notifyDataSetChanged();
                        } else {
                            Log.v("[ERROR]", "Error while fetching addresses: " + e.getMessage());
                        }
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
