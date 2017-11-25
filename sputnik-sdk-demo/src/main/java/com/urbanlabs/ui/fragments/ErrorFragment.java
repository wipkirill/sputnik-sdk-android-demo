package com.urbanlabs.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.urbanlabs.R;

/**
 * A placeholder fragment containing error view.
 */
@SuppressLint("ValidFragment")
public class ErrorFragment extends Fragment {
    private String errorMessage_;

    public ErrorFragment(String message) {
        errorMessage_ = message;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_error, container, false);
        TextView t = (TextView) rootView.findViewById(R.id.errorMessage);
        t.setText(errorMessage_);
        return rootView;
    }
}
