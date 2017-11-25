package com.urbanlabs.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import org.json.JSONObject;

import java.util.List;

public abstract class JsonAdapter extends ArrayAdapter<JSONObject> {
    protected final Context context;
    protected List<JSONObject> values;

    public JsonAdapter(Context context, int resource, List<JSONObject> values) {
        super(context, resource, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);

    @Override
    public int getCount() {
        return values == null ? 0 : values.size();
    }

    /**
     *
     * @param i
     * @return
     */
    public JSONObject getValueAt(int i) {
        if(i >= 0 && i < values.size())
            return values.get(i);
        Log.e("[JsonAdapter]", "Index out of bounds");
        return null;
    }
}
