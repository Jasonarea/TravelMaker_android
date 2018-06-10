package com.ellalee.travelmaker;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by jiwon on 2018-06-03.
 */

public class CalendarAddSche extends AppCompatActivity{
    private DatePicker date;
    private TextView viewDate;
    private EditText sche;
    private EditText memo;
    private Button complete;
    private Button cancel;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_sche_add);

//        date = (DatePicker)findViewById(R.id.datePicker);
        viewDate = (TextView)findViewById(R.id.DateSetting);
        sche = (EditText)findViewById(R.id.addSche);
        memo = (EditText)findViewById(R.id.addMemo);
        complete = (Button)findViewById(R.id.add_complete);
        cancel = (Button)findViewById(R.id.cancel);

//        date.init(date.getYear(), date.getMonth(), date.getDayOfMonth(), new DatePicker.OnDateChangedListener() {
//            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//                viewDate.setText(String.format("%d년 %d월 %d일", year, monthOfYear+1, dayOfMonth));
//            }
//        });

        Calendar c = Calendar.getInstance();

        final DatePickerDialog datePickerDialog = new DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, new DatePickerDialog.OnDateSetListener() {

            @Override

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                try {

                    Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(year+"-"+(monthOfYear+1)+"-"+dayOfMonth);
                    viewDate.setText("선택된 날짜 " + year + "년"+ (monthOfYear+1) + "월" + dayOfMonth + "일");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setCalendarViewShown(false);

        datePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        datePickerDialog.getDatePicker().setHorizontalFadingEdgeEnabled(true);

        datePickerDialog.show();

        viewDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                invalidateOptionsMenu();
            }
        });


        complete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(sche.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "스케줄을 입력해주세요!", Toast.LENGTH_LONG).show();
                }
                else {
                    Intent intent = new Intent();
                    intent.putExtra("year", String.valueOf(datePickerDialog.getDatePicker().getYear()));
                    intent.putExtra("month", String.valueOf(datePickerDialog.getDatePicker().getMonth() + 1));
                    intent.putExtra("day", String.valueOf(datePickerDialog.getDatePicker().getDayOfMonth()));
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
