package com.ellalee.travelmaker;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class CalendarListMain extends AppCompatActivity {
    ListView listView;
    ArrayAdapter<String> adapter;
    ArrayList<String> sched = new ArrayList<>();
    ArrayList<String> memo = new ArrayList<>();
    private int day, month, year;
    private TextView title;
    private TextView schedTitle, empty;
    SQLiteDatabase db;
    CalendarDBHelper helper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_list_main);

        helper = new CalendarDBHelper(CalendarListMain.this, "calendar.db", null, 1);
        init();

    }

    protected void init() {

        Intent received = getIntent();
        day = received.getIntExtra("day", 0);
        year = received.getIntExtra("year", 0);
        month = received.getIntExtra("month", 0);
        sched = received.getStringArrayListExtra("sche");
        memo =received.getStringArrayListExtra("memo");

        listView = (ListView)findViewById(R.id.listview);
        title = (TextView)findViewById(R.id.setTitle);
        schedTitle = (TextView)findViewById(R.id.notice_sche);
        empty = (TextView)findViewById(R.id.empty_sche);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, sched);
        listView.setAdapter(adapter);
        title.setText(year + "." + month + "." + day);
        schedTitle.setText("* 등록된 스케줄 *");

        if(sched.size() > 0)
            empty.setVisibility(View.INVISIBLE);
        else {
            empty.setText("등록된 스케줄이 없습니다.");
            empty.setVisibility(View.VISIBLE);
        }


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CalendarListMain.this, CalendarDayMain.class);
                intent.putExtra("year", year);
                intent.putExtra("month", month);
                intent.putExtra("day", day);
                intent.putExtra("sche", sched.get(position));
                intent.putExtra("memo", memo.get(position));

                startActivityForResult(intent, RESULT_OK);
            }
        });
    }

    /*
     * DB에서 삭제나 수정하고 넘어온 날짜정보들
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_CANCELED) {
            String year = data.getStringExtra("year");
            String month = data.getStringExtra("month");
            String day = data.getStringExtra("day");

            Intent sendToMain = new Intent(CalendarListMain.this, CalendarMain.class);
            sendToMain.putExtra("year", year);
            sendToMain.putExtra("month", month);
            sendToMain.putExtra("day", day);
            startActivity(sendToMain);
            //setResult(1, sendToMain);
        }
        finish();
    }

}
