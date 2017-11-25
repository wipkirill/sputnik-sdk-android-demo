package com.urbanlabs.sdk.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.urbanlabs.sdk.util.UnzipUtility;
import com.urbanlabs.sdk.R;

/**
 * Created by kirill on 2/13/14.
 */
public class UnzipTask extends AsyncTask<String, Void, Boolean> {
    private String mapName_;

    public UnzipTask(Activity activity) {
        this.activity_ = activity;
        dialog = new ProgressDialog(activity);
    }

    /**
     * progress dialog to show user that the backup is processing.
     */
    private ProgressDialog dialog;
    /**
     * application context.
     */
    private Activity activity_;

    protected void onPreExecute() {
        this.dialog.setMessage(activity_.getString(R.string.unzipInProgress));
        this.dialog.show();
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        if (!success)
            Toast.makeText(activity_, activity_.getString(R.string.unpackingMapFailed), Toast.LENGTH_LONG).show();
        else {
            ((UnzipListener)activity_).done(mapName_);
        }
    }

    /**
     *
     * @param args
     * @return
     */
    protected Boolean doInBackground(final String... args) {
        try {
            mapName_ = args[0].replace(".zip", "");
            String zipFilePath = args[1];
            String destinationPath = args[2];
            boolean removeZip = Boolean.valueOf(args[3]);
            UnzipUtility ut = new UnzipUtility();
            ut.unzip(zipFilePath, destinationPath, removeZip);
            return true;
        } catch (Exception e) {
            Log.e("[UnzipTask]",  e.getMessage());
            return false;
        }
    }
    public interface UnzipListener {
        void done(String mapName);
    }
}
