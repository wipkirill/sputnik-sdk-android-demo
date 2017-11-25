package com.urbanlabs;

import android.app.Application;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.urbanlabs.sdk.Sputnik;
import com.urbanlabs.sdk.SputnikException;
import com.urbanlabs.sdk.callback.BasicCallback;

/**
 * This is a small example on how to initialize Sputnik
 * library in an Android Application
 */
public class SputnikApp extends Application {
    public static final String TAG = "[SputnikApp]";
    public static final String APP_STATE_MESSAGE = "SputnikStateMessage";
    public static final String SHOW_MAP_MESSAGE = "SputnikShowMapMessage";
    public static final String DOWNLOAD_MAP_MESSAGE = "SputnikDownloadMapMessage";

    private LocalBroadcastManager locBroadCastMgr_ = null;

    @Override
    public final void onCreate() {
        super.onCreate();
        locBroadCastMgr_ = LocalBroadcastManager.getInstance(this);
        Log.v("", "Starting app");
        // call init and use our callback mechanism
        Sputnik.init(getApplicationContext(), new BasicCallback() {
            @Override
            public void done(SputnikException e) {
                if(e == null) {
                    Log.v(TAG, "Started successfully");
                    notifySuccess();
                } else {
                    Log.e(TAG, "Error while starting");
                    notifyError(e.getMessage());
                }
            }
        });
    }

    /**
     * Actions in case of a failure
     */
    private void notifyError(String errMessage) {
        Intent errInt = new Intent(APP_STATE_MESSAGE);
        errInt.putExtra("ERROR", true);
        errInt.putExtra("MESSAGE", errMessage);
        locBroadCastMgr_.sendBroadcast(errInt);
    }

    /**
     * Notify other activities on success
     */
    private void notifySuccess() {
        Intent sInt = new Intent(APP_STATE_MESSAGE);
        sInt.putExtra("ERROR",false);
        locBroadCastMgr_.sendBroadcast(sInt);
    }
}
