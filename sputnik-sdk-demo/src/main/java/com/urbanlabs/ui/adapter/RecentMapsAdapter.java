package com.urbanlabs.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.urbanlabs.R;
import com.urbanlabs.sdk.Sputnik;
import com.urbanlabs.sdk.SputnikConsts;
import com.urbanlabs.sdk.SputnikException;
import com.urbanlabs.sdk.callback.GetCallback;
import com.urbanlabs.sdk.response.MapInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by kirill on 2/1/14.
 */
public class RecentMapsAdapter extends ArrayAdapter<String> {
    private LayoutInflater inflater_;
    private Map<Integer, View> holder_ = new HashMap<>();
    public RecentMapsAdapter(Context context, int resource, List<String> values) {
        super(context, resource, values);
        inflater_ = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        if(holder_.containsKey(position))
            return holder_.get(position);
        View rowView = inflater_.inflate(R.layout.recent_map_item, parent, false);
        final TextView areaName = (TextView) rowView.findViewById(R.id.recentMapAreaName);
        final TextView countryCodes = (TextView) rowView.findViewById(R.id.countryCodes);
        final LinearLayout featureCont = (LinearLayout)rowView.findViewById(R.id.featureContainer);
        holder_.put(position, rowView);
        Sputnik.getMapInfo(getItem(position), new GetCallback<MapInfo>() {
            @Override
            public void done(MapInfo mapInfo, SputnikException e) {
                if(e == null) {
                    if(mapInfo.getAreaName() != null)
                        areaName.setText(mapInfo.getAreaName());
                    else
                        areaName.setText("Not specified");
                    Object[] cCodes = mapInfo.getCountryCodes().toArray();
                    if(cCodes.length > 0)
                        countryCodes.append((String)cCodes[0]);
                    for(int i = 1; i < cCodes.length; ++i)
                        countryCodes.append(", " + (String)cCodes[i]);
                    Set<String> sFeatures = mapInfo.getMapFeatures();
                    List<String> locFt = extractLocalizedFeatures(sFeatures);
                    for(String lFt : locFt) {
                        LinearLayout lView = (LinearLayout)inflater_.inflate(R.layout.map_feature_item, parent, false);
                        TextView fView = (TextView) lView.findViewById(R.id.tagItemElement);
                        fView.setText(lFt);
                        featureCont.addView(lView);
                    }
                } else {
                    Log.v("[ERROR]", "Failed to get mapinfo for " + getItem(position));
                }
            }
        });
        return rowView;
    }

    /**
     *
     * @param ft
     * @return
     */
    private List<String> extractLocalizedFeatures(Set<String> ft) {
        List<String> locFt = new ArrayList<>();
        for(String f : ft) {
            if(f.equals(SputnikConsts.FEATURE_TILES))
                locFt.add(getContext().getString(R.string.tilesFeature));
            if(f.equals(SputnikConsts.FEATURE_SEARCH))
                locFt.add(getContext().getString(R.string.searchFeature));
            if(f.equals(SputnikConsts.FEATURE_ROUTING))
                locFt.add(getContext().getString(R.string.routingFeature));
            if(f.equals(SputnikConsts.FEATURE_ADDRESS_DECODER))
                locFt.add(getContext().getString(R.string.addrDecoderFeature));
        }
        return locFt;
    }
}
