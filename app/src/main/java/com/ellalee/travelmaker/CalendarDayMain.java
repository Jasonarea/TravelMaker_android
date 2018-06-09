package com.ellalee.travelmaker;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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
    SQLiteDatabase db;
    CalendarDBHelper helper;


    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calendar_day);
        helper = new CalendarDBHelper(CalendarDayMain.this, "calendar.db", null, 1);

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
                update(String.valueOf(year) + "-" + String.valueOf(month) + "-" + String.valueOf(day),
                        sched, memo);
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete(String.valueOf(year) + "-" + String.valueOf(month) + "-" + String.valueOf(day),
                        sched);
            }
        });


    }

    /*
     * DB에 있는 값 업데이트 method
     */
    public void update(String date, String sched, String memo) {
        db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("schedule", sched);
        db.update("calendar", values, "date=?", new String[]{date});
    }

    /*
    * 원하는 날짜의 스케줄 삭제
    */
    public void delete(String date, String sched) {
        db = helper.getWritableDatabase();
        db.execSQL("DELETE FROM calendar WHERE (date=" + date + ") AND (schedule=" + sched + ");");
    }
}
