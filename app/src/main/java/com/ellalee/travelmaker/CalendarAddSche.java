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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_sche_add);

        year = (EditText)findViewById(R.id.setYear);
        month = (EditText)findViewById(R.id.setMonth);
        day = (EditText)findViewById(R.id.setDay);
        sche = (EditText)findViewById(R.id.addSche);
        memo = (EditText)findViewById(R.id.addMemo);
        complete = (Button)findViewById(R.id.add_complete);

        complete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent =  new Intent();
                intent.putExtra("year", year.getText());
                intent.putExtra("month", month.getText());
                intent.putExtra("day", day.getText());
                intent.putExtra("schedule", sche.getText());
                intent.putExtra("memo", memo.getText());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
