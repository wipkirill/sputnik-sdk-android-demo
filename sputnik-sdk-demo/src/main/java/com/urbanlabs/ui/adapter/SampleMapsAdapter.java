package com.urbanlabs.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.urbanlabs.R;

import org.json.JSONObject;

import java.util.List;

/**
 *
 */
public class SampleMapsAdapter extends ArrayAdapter<JSONObject> {
    private final LayoutInflater inflater_;

    public SampleMapsAdapter(Context context, int resource, List<JSONObject> objects) {
        super(context, resource, objects);
        inflater_ = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View rowView = inflater_.inflate(R.layout.sample_map_item, parent, false);
        TextView nameTextView = (TextView) rowView.findViewById(R.id.recentMapArea);
        TextView countryTextView = (TextView) rowView.findViewById(R.id.secondLine);
        TextView fileSize = (TextView) rowView.findViewById(R.id.fileSize);
        JSONObject item = getItem(position);

        try {
            nameTextView.setText(item.getString("areaName"));
            countryTextView.setText(item.getString("country"));
            fileSize.setText(item.getString("fileSize"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rowView;
    }
}
