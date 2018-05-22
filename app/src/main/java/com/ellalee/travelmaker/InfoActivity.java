package com.ellalee.travelmaker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by User on 4/29/2015.
 */
public class InfoActivity extends Thread {

    TextView header;
    TextView body;
    public InfoActivity() {
        String s = "";
        String b = "";
        /*Bundle extras = getIntent().getExtras();
        if (extras != null) {
            b = extras.getString("body");
            s = extras.getString("subject");
        }*/

        body.setText(b);
        header.setText(s);
    }
}