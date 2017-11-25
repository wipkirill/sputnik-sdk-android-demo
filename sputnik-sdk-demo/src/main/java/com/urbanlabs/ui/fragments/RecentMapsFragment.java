package com.urbanlabs.ui.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.urbanlabs.R;
import com.urbanlabs.SputnikApp;
import com.urbanlabs.sdk.Sputnik;
import com.urbanlabs.sdk.SputnikException;
import com.urbanlabs.sdk.callback.GetCallback;
import com.urbanlabs.sdk.response.MapList;
import com.urbanlabs.ui.adapter.RecentMapsAdapter;
import com.urbanlabs.util.LayoutUtil;

/**
 * A placeholder fragment containing a menu view.
 */
public class RecentMapsFragment extends Fragment {

    private RecentMapsAdapter adapter;
    private ListView rm;
    private ProgressBar progress;
    private TextView noItems;

    public RecentMapsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_recent_maps, container, false);

        rm = (ListView) rootView.findViewById(R.id.recentMaps);
        progress = (ProgressBar)rootView.findViewById(R.id.listViewProgress);
        rm.setEmptyView(progress);
        noItems = (TextView)rootView.findViewById(R.id.noItemsInListView);

        return rootView;
    }

    public void refresh() {
        Sputnik.listMaps(new GetCallback<MapList>() {
            @Override
            public void done(MapList mapList, SputnikException e) {
                if(e == null) {
                    if(mapList.getMaps().size() > 0) {
                        adapter = new RecentMapsAdapter(getView().getContext(),
                                R.layout.recent_map_item, mapList.getMaps());
                        rm.setAdapter(adapter);
                        LayoutUtil.setListViewHeightBasedOnChildren(rm);
                    } else {
                        progress.setVisibility(LinearLayout.GONE);
                        rm.setEmptyView(noItems);
                    }
                } else {
                    progress.setVisibility(LinearLayout.GONE);
                    rm.setEmptyView(noItems);
                }
            }
        });

        rm.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String data = adapter.getItem(i);
                if (data != null) {
                    notifyRecentMapSelected(data);
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        refresh();
    }

    /**
     *
     * @param mapName
     */
    private void notifyRecentMapSelected(String mapName) {
        Intent intent = new Intent(SputnikApp.SHOW_MAP_MESSAGE);
        intent.putExtra("mapName", mapName);
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
                .sendBroadcast(intent);
    }
}

