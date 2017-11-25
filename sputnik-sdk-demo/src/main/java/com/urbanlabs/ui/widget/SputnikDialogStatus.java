package com.urbanlabs.ui.widget;


import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;
import com.urbanlabs.R;
import com.urbanlabs.mapview.IMapViewCallback;

/**
 * Created with IntelliJ IDEA.
 * User: paprika
 * Date: 6/25/14
 * Time: 11:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class SputnikDialogStatus implements IMapViewCallback {
    // notification dialog
    private ProgressDialog dialog_;
    private Context context_;

    public SputnikDialogStatus(Context context) {
        context_ = context;
        // initialize the dialog
        dialog_ = new ProgressDialog(context, ProgressDialog.THEME_HOLO_LIGHT);
        dialog_.setMessage(context.getString(R.string.loadMapInProgress));
        dialog_.setCancelable(false);
        dialog_.setCanceledOnTouchOutside(false);
        dialog_.show();
    }

    @Override
    public void onNotifyError(String error) {
        dialog_.dismiss();
        Toast.makeText(context_, context_.getString(R.string.loadMapFailed), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNotifyReady() {
        dialog_.dismiss();
    }
}