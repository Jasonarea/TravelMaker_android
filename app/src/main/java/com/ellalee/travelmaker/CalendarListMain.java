package com.ellalee.travelmaker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CalendarListMain extends AppCompatActivity {
    ListView listView;
    ArrayAdapter<String> adapter;
    private String date;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_list_main);
    }
}
