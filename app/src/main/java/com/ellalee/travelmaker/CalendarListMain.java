package com.ellalee.travelmaker;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class CalendarListMain extends AppCompatActivity {
    ListView listView;
    ArrayAdapter<String> adapter;
    ArrayList<String> sched = new ArrayList<>();
    ArrayList<String> memo = new ArrayList<>();
    private int day, month, year;
    private FloatingActionButton addSche;
    SQLiteDatabase db;
    CalendarDBHelper helper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_list_main);

        helper = new CalendarDBHelper(CalendarListMain.this, "calendar.db", null, 1);
        addSche = (FloatingActionButton)findViewById(R.id.listAdd_FAB);
        init();

        addSche.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent send = new Intent();
                startActivityForResult(new Intent(getApplicationContext(), CalendarAddSche.class), 1);
            }
        });
    }

    protected void init() {

        Intent received = getIntent();
        day = received.getIntExtra("day", 0);
        year = received.getIntExtra("year", 0);
        month = received.getIntExtra("month", 0);
        sched = received.getStringArrayListExtra("sche");
        memo =received.getStringArrayListExtra("memo");

        listView = (ListView)findViewById(R.id.listview);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, sched);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CalendarListMain.this, CalendarDayMain.class);
                intent.putExtra("year", year);
                intent.putExtra("month", month);
                intent.putExtra("day", day);
                intent.putExtra("sche", sched.get(position));
                intent.putExtra("memo", memo.get(position));

                startActivity(intent);
            }
        });
    }

    /*
     *일정을 추가할 때 받은 값들 / DB에 insert
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_CANCELED) {

            String year = data.getStringExtra("year");
            String month = data.getStringExtra("month");
            String day = data.getStringExtra("day");
            String schedule = data.getStringExtra("schedule");
            String memo = data.getStringExtra("memo");

            String date = year + "-" + month + "-" + day;
            ContentValues values = new ContentValues();
            values.put("date", date);
            values.put("schedule", schedule);
            values.put("memo", memo);

            db.insert("calendar", null, values);
        }
//        gridAdapter = new GridAdapter(getApplicationContext(), dayList);
//        gridView.setAdapter(gridAdapter);
    }

}
