package com.ellalee.travelmaker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by jiwon on 2018-06-09.
 */

public class CalendarDayMain extends Activity {
    String sched, memo;
    int day, month, year;
    private TextView date;
    private TextView scheView, memoView;
    private Button changeBtn, deleteBtn;


    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calendar_day);

        Intent intent = getIntent();
        day = intent.getIntExtra("day", 0);
        year = intent.getIntExtra("year", 0);
        month = intent.getIntExtra("month", 0);
        sched = intent.getStringExtra("sche");
        memo = intent.getStringExtra("memo");

        date = (TextView)findViewById(R.id.dayMain_date);
        scheView = (TextView)findViewById(R.id.dayMain_sche);
        memoView = (TextView)findViewById(R.id.dayMain_memo);
        changeBtn = (Button)findViewById(R.id.dayMain_change);
        deleteBtn = (Button)findViewById(R.id.dayMain_delete);

        date.setText(String.valueOf(year) + "." + String.valueOf(month) + "." + String.valueOf(day));
        scheView.setText(sched);
        memoView.setText(memo);

        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


    }
}
