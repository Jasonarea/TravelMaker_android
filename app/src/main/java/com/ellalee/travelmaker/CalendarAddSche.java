package com.ellalee.travelmaker;

import android.app.Fragment;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by jiwon on 2018-06-03.
 */

public class CalendarAddSche extends AppCompatActivity{
    private EditText year;
    private EditText month;
    private EditText day;
    private EditText sche;
    private EditText memo;
    private Button complete;
    private Button cancel;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_sche_add);

        year = (EditText)findViewById(R.id.setYear);
        month = (EditText)findViewById(R.id.setMonth);
        day = (EditText)findViewById(R.id.setDay);
        sche = (EditText)findViewById(R.id.addSche);
        memo = (EditText)findViewById(R.id.addMemo);
        complete = (Button)findViewById(R.id.add_complete);
        cancel = (Button)findViewById(R.id.cancel);

        complete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(year.getText().toString().equals("") || month.getText().toString().equals("") || day.getText().toString().equals("") || sche.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "날짜와 스케줄 전부 입력해주세요!", Toast.LENGTH_LONG).show();
                }
                else {
                    Intent intent = new Intent();
                    intent.putExtra("year", year.getText().toString());
                    intent.putExtra("month", month.getText().toString());
                    intent.putExtra("day", day.getText().toString());
                    intent.putExtra("schedule", sche.getText().toString());
                    intent.putExtra("memo", memo.getText().toString());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }
}
