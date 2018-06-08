package com.ellalee.travelmaker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class sharePractice extends Fragment {

    private static final String LOG_TAG = sharePractice.class.getSimpleName();

    private ShareActionProvider mShareActionProvider;
    private String forecast;
    private Intent mShareIntent;

    public sharePractice() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_sharing, menu);

        MenuItem shareItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(mShareIntent);
        }
        else {
            Log.d(LOG_TAG, "Share action provider is null");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Intent intent = getActivity().getIntent();

        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            forecast = intent.getStringExtra(Intent.EXTRA_TEXT);
            TextView forecastTextview = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
            forecastTextview.setText(forecast);
            setupShareIntent();
        }

        return rootView;
    }

    private void setupShareIntent() {
        String textToShare = forecast + " #SunshineApp";
        mShareIntent = new Intent(Intent.ACTION_SEND);
        mShareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        mShareIntent.setType("text/plain");
        mShareIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
    }
}
