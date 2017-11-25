package com.urbanlabs;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.urbanlabs.sdk.Sputnik;
import com.urbanlabs.sdk.SputnikConsts;
import com.urbanlabs.ui.fragments.ErrorFragment;
import com.urbanlabs.ui.fragments.RecentMapsFragment;
import com.urbanlabs.ui.fragments.SampleMapsFragment;
import com.urbanlabs.sdk.tasks.UnzipTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a sample activity which lists downloaded map files
 * and allows to browse them
 */
public class MenuActivity extends Activity implements UnzipTask.UnzipListener{
    // Broadcast mgr
    private LocalBroadcastManager locBroadCastMgr_ = null;
    private DownloadManager mgr = null;
    private long lastDownload = -1L;
    private Map<Long, String> downloadData_ = new HashMap<>();
    private RecentMapsFragment rmf_;
    private SampleMapsFragment saf_;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        locBroadCastMgr_ = LocalBroadcastManager.getInstance(this);
        mgr = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        switch (Sputnik.getCurrentState()) {
            case ERROR:
                // show error screen
                uiErrorState(SputnikConsts.ERRORS.get(Sputnik.getCurrentError()));
                break;
            case INIT:
                // show loading screen before Sputnik library is initializing
                uiLoadingState();
                break;
            case RUNNING:
                // everything is fine
                uiRunningState();
                break;
        }
        registerEvents();
    }

    /**
     *
     */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterEvents();
    }
    
    /**
     *
     */
    private void registerEvents() {
        Log.d("", "Registering for events");
        locBroadCastMgr_.registerReceiver(mMessageReceiver, new IntentFilter(SputnikApp.APP_STATE_MESSAGE));
        locBroadCastMgr_.registerReceiver(mMessageReceiver, new IntentFilter(SputnikApp.SHOW_MAP_MESSAGE));
        locBroadCastMgr_.registerReceiver(mMessageReceiver, new IntentFilter(SputnikApp.DOWNLOAD_MAP_MESSAGE));
        registerReceiver(onDownloadComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        registerReceiver(onDownloadNotificationClick,
                new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
    }

    /**
     *
     */
    private void unregisterEvents() {
        Log.d("", "Registering for events");
        locBroadCastMgr_.unregisterReceiver(mMessageReceiver);
        unregisterReceiver(onDownloadComplete);
        unregisterReceiver(onDownloadNotificationClick);
    }

    /**
     * Shows loading screen
     */
    private void uiLoadingState() {
        setContentView(R.layout.fragment_loading);
    }

    /**
     * Renders menu with maps
     */
    private void uiRunningState() {
        if(saf_ == null && rmf_ == null) {
            setContentView(R.layout.menu);
            rmf_ = new RecentMapsFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.container, rmf_)
                    .commit();

            saf_ = new SampleMapsFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.container, saf_)
                    .commit();
        }
    }

    /**
     * @param error
     */
    private void uiErrorState(String error) {
        setContentView(R.layout.menu);
        getFragmentManager().beginTransaction()
                .add(R.id.container, new ErrorFragment(error))
                .commit();
    }

    /**
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    /**
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_about:
                Uri webpage = Uri.parse(getString(R.string.sputnik_url));
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Starts mapview for a selected map file
     * @param mapName
     */
    private void runMapActivity(String mapName) {
        Intent rMap = new Intent(this, MapActivity.class);
        rMap.putExtra("mapName", mapName);
        startActivity(rMap);
    }

    /**
     *
     * @param intent
     */
    private void handleShowMap(Intent intent) {
        String mapName = intent.getStringExtra("mapName");
        if(mapName != null && !mapName.isEmpty()) {
            runMapActivity(mapName);
        } else {
            Log.e("Menu", "Mapname hasn't been specified");
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(SputnikApp.APP_STATE_MESSAGE)) {
                 handleAppStateMessage(intent);
            }
            if(intent.getAction().equals(SputnikApp.SHOW_MAP_MESSAGE)) {
                handleShowMap(intent);
            }
            if(intent.getAction().equals(SputnikApp.DOWNLOAD_MAP_MESSAGE))  {
                handleDownload(intent);
            }
        }
    };

    /**
     *
     * @param intent
     */
    private void handleAppStateMessage(Intent intent) {
        boolean err = intent.getBooleanExtra("ERROR", true);
        if (err) {
            // handle fragment_error, show fail screen
            String eMess = intent.getStringExtra("MESSAGE");
            uiErrorState(eMess);
        } else {
            uiRunningState();
        }
    }

    /**
     *
     * @param intent
     */
    private void handleDownload(Intent intent) {
        Log.d("[MenuActivity]", "Handling download intent");
        String mapFileUrl = intent.getStringExtra("mapFileUrl");
        String fileName = mapFileUrl.substring(mapFileUrl.lastIndexOf('/') + 1, mapFileUrl.length());
        String cityName = intent.getStringExtra("cityName");
        if(mapFileUrl != null && cityName != null) {
            startDownload(mapFileUrl, fileName, cityName);
        } else
            Log.e("[MenuActivity]", "Wrong arguments for download, no mapFile and mapName");
    }

    BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            // Get the dId of the completed download.
            long dId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            Cursor c = mgr.query(new DownloadManager.Query().setFilterById(dId));
            int status = DownloadManager.STATUS_FAILED;
            if(c.moveToFirst())
                status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            String zipPath = Sputnik.getWorkDir() +
                    downloadData_.get(dId);
            if(status == DownloadManager.STATUS_SUCCESSFUL) {
                Toast.makeText(ctxt, getString(R.string.downloadComplete), Toast.LENGTH_LONG).show();
                if(downloadData_.containsKey(dId)) {
                    String destPath = Sputnik.getWorkDir();
                    new UnzipTask(MenuActivity.this).execute(downloadData_.get(dId), zipPath, destPath, "true");
                } else
                    Log.e("UnzipTask", dId +" not found in downloads");
            } else {
                Toast.makeText(ctxt, getString(R.string.downloadFailed), Toast.LENGTH_LONG).show();
                File zipFile = new File(zipPath);
                if(zipFile.exists())
                    zipFile.delete();
            }
        }
    };

    BroadcastReceiver onDownloadNotificationClick = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            Toast.makeText(ctxt, getString(R.string.downloadInProgress), Toast.LENGTH_LONG).show();
        }
    };

    /**
     *
     * @param url
     * @param destFileName
     * @param description
     */
    public void startDownload(String url, String destFileName, String description) {
        Uri uri = Uri.parse(url);
        Uri destFileUri = Uri.fromFile(new File(Sputnik.getWorkDir(), destFileName));
        lastDownload = mgr.enqueue(new DownloadManager.Request(uri)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                        DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(description)
                .setDescription(description)
                .setDestinationUri(destFileUri));
        downloadData_.put(lastDownload, destFileName);
    }

    @Override
    public void done(String mapName) {
        rmf_.refresh();
        runMapActivity(mapName);
    }
}
