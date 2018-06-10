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
import android.widget.Toast;

import java.util.ArrayList;

import static java.sql.Types.NULL;

/**
 * Created by jiwon on 2018-06-09.
 */

public class CalendarDayMain extends Activity {
    String sched, memo;
    int day, month, year;
    private TextView date;
    private TextView scheView, memoView,category_sche, category_memo;
    private EditText scheEdit, memoEdit;
    private Button changeBtn, deleteBtn, completeBtn;
    private String mode = "View";
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
        scheEdit = (EditText)findViewById(R.id.change_editsche);
        memoEdit = (EditText)findViewById(R.id.change_editmemo);
        completeBtn = (Button)findViewById(R.id.complete_change);
        category_sche = (TextView)findViewById(R.id.category3);
        category_memo = (TextView)findViewById(R.id.category4);

        date.setText(String.valueOf(year) + "." + String.valueOf(month) + "." + String.valueOf(day));
        scheView.setText(sched);
        memoView.setText(memo);

        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode = "Edit";
                scheEdit.setVisibility(View.VISIBLE);
                memoEdit.setVisibility(View.VISIBLE);
                completeBtn.setVisibility(View.VISIBLE);
                category_memo.setVisibility(View.VISIBLE);
                category_sche.setVisibility(View.VISIBLE);
                scheView.setVisibility(View.INVISIBLE);

                completeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newSche = scheEdit.getText().toString();
                        memo = memoEdit.getText().toString();
                        if(!newSche.replace(" ", "").equals("")) {
                            update(String.valueOf(year) + "-" + String.valueOf(month) + "-" + String.valueOf(day),
                                    sched, newSche,  memo);

                            scheView.setText(sched);
                            scheEdit.setVisibility(View.INVISIBLE);
                            memoEdit.setVisibility(View.INVISIBLE);
                            completeBtn.setVisibility(View.INVISIBLE);
                            category_memo.setVisibility(View.INVISIBLE);
                            category_sche.setVisibility(View.INVISIBLE);
                            scheView.setVisibility(View.VISIBLE);

                            Toast.makeText(getApplicationContext(), newSche + " 스케줄 수정완료!", Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(CalendarDayMain.this, CalendarListMain.class);
                            intent.putExtra("year", year);
                            intent.putExtra("month", month);
                            intent.putExtra("day", day);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                        else {
                            setResult(RESULT_CANCELED);
                            Toast.makeText(getApplicationContext(), "스케줄이 입력되지 않았습니다!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete(String.valueOf(year) + "-" + String.valueOf(month) + "-" + String.valueOf(day),
                        sched);
                Toast.makeText(getApplicationContext(), "스케줄 삭제완료!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(CalendarDayMain.this, CalendarListMain.class);
                intent.putExtra("year", year);
                intent.putExtra("month", month);
                intent.putExtra("day", day);
                setResult(RESULT_OK, intent);
                finish();
            }
        });


    }

    /*
     * DB에 있는 값 업데이트 method
     */
    public void update(String date, String sched, String newSche, String memo) {
        db = helper.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put("date", date);
//        values.put("schedule", sched);
//        db.update("calendar", values, "date=?", new String[]{date});
        db.execSQL("UPDATE calendar SET schedule = '"+ newSche + "', memo = '" + memo + "' WHERE (date = '" + date + "') AND (schedule = '" + sched + "');");
    }

    /*
    * 원하는 날짜의 스케줄 삭제
    */
    public void delete(String date, String sched) {
        db = helper.getWritableDatabase();
        db.execSQL("DELETE FROM calendar WHERE (date='" + date + "') AND (schedule='" + sched + "');");
    }
}
