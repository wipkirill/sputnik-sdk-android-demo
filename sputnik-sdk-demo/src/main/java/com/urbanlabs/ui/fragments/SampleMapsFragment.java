package com.urbanlabs.ui.fragments;

import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.urbanlabs.R;
import com.urbanlabs.sdk.Sputnik;
import com.urbanlabs.sdk.response.json.JsonHandler;
import com.urbanlabs.sdk.util.NetworkUtil;
import com.urbanlabs.ui.adapter.SampleMapsAdapter;
import com.urbanlabs.util.LayoutUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a menu view.
 */
public class SampleMapsFragment extends Fragment {
    private SampleMapsAdapter defaultAdapter_;
    private ListView cityList_;
    private TextView noItems_;
    private ProgressBar progress_;

    public SampleMapsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_sample_maps, container, false);
        cityList_ = (ListView) rootView.findViewById(R.id.defaultCities);
        noItems_ = (TextView)rootView.findViewById(R.id.noItemsInListView);
        progress_ = (ProgressBar)rootView.findViewById(R.id.listViewProgress);
        cityList_.setEmptyView(progress_);
        if(NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {
            try {
                Sputnik.makeRequest(new URL(getString(R.string.sample_maps_list_url)), new JsonHandler(new String[]{}) {
                    @Override
                    public void onResult(JSONObject jsonObject) {
                        Log.v("[DEBUG]", "Loaded json " + jsonObject.toString());
                        try {
                            if(jsonObject.has("response")) {
                                JSONArray cities = jsonObject.getJSONArray("response");
                                List<JSONObject> objs = new ArrayList<>();
                                for(int i=0; i < cities.length();++i)
                                    objs.add(cities.getJSONObject(i));
                                defaultAdapter_ = new SampleMapsAdapter(rootView.getContext(),
                                        R.layout.sample_map_item, objs);
                                cityList_.setAdapter(defaultAdapter_);
                                LayoutUtil.setListViewHeightBasedOnChildren(cityList_);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        cityList_.setEmptyView(noItems_);
                    }

                    @Override
                    public void onError(String message) {
                        Log.e("[ERROR]","Error loading json " + message);
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            cityList_.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    JSONObject clickedCity = defaultAdapter_.getItem(i);
                    if(clickedCity != null) {
                        try {
                            String mapFileUrl = clickedCity.getString("downloadLink");
                            String cityName = clickedCity.getString("areaName");
                            String size = clickedCity.getString("fileSize");
                            String country = clickedCity.getString("country");
                            if(NetworkUtil.isNetworkAvailable(getActivity()))
                                showDownloadDialog(mapFileUrl, cityName, country, size);
                            else
                                Toast.makeText(getActivity(), getString(R.string.noInternetConnection),
                                        Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } else {
            cityList_.setEmptyView(noItems_);
            progress_.setVisibility(LinearLayout.GONE);
            Toast.makeText(getActivity(), getString(R.string.noInternetConnection), Toast.LENGTH_LONG).show();
        }

        return rootView;
    }

    /**
     *
     * @param mapFileUrl
     * @param areaName
     * @param country
     * @param size
     */
    void showDownloadDialog(String mapFileUrl, String areaName, String country, String size) {
        DialogFragment newFragment = DownloadDialogFragment.newInstance(mapFileUrl, areaName, country, size);
        newFragment.show(getFragmentManager(), "dialog");
    }
}

