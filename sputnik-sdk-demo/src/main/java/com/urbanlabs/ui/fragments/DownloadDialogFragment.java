package com.urbanlabs.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.urbanlabs.R;
import com.urbanlabs.SputnikApp;
import com.urbanlabs.sdk.util.NetworkUtil;


public class DownloadDialogFragment extends DialogFragment {
    private LocalBroadcastManager locBroadCastMgr_;
    public static DownloadDialogFragment newInstance(String mapFileUrl, String cityName,  String country, String downloadSize) {
        DownloadDialogFragment frag = new DownloadDialogFragment();
        Bundle args = new Bundle();
        args.putString("mapFileUrl", mapFileUrl);
        args.putString("cityName", cityName);
        args.putString("country", country);
        args.putString("size", downloadSize);
        frag.setArguments(args);
        return frag;
    }

    public DownloadDialogFragment() {
        locBroadCastMgr_ = LocalBroadcastManager.getInstance(getActivity());
    }

    /**
     *
     * @param savedInstanceState
     * @return
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dView = inflater.inflate(R.layout.fragment_download_dialog, null);
        TextView cityName = (TextView)dView.findViewById(R.id.cityToDownload);
        TextView countryName = (TextView)dView.findViewById(R.id.countryToDownload);
        TextView size = (TextView)dView.findViewById(R.id.fileSize);
        cityName.setText(cityName.getText()+getArguments().getString("cityName"));
        countryName.setText(countryName.getText()+getArguments().getString("country"));
        size.setText(size.getText()+getArguments().getString("size"));

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.downloadDialogTitle)
                .setView(dView)
                .setPositiveButton(R.string.okDownloadButton,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                sendDownloadIntent();
                            }
                        }
                )
                .setNegativeButton(R.string.cancelButton,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Log.d("DownloadDialog", "Negative result");
                        }
                    }
                )
                .create();
    }

    /**
     *
     */
    private void sendDownloadIntent() {
        Intent dIntent = new Intent(SputnikApp.DOWNLOAD_MAP_MESSAGE);
        dIntent.putExtra("mapFileUrl", getArguments().getString("mapFileUrl"));
        dIntent.putExtra("cityName", getArguments().getString("cityName"));
        if(NetworkUtil.isNetworkAvailable(getActivity()))
            locBroadCastMgr_.sendBroadcast(dIntent);
        else
            Toast.makeText(getActivity(), getString(R.string.noInternetConnection), Toast.LENGTH_LONG).show();
    }
}