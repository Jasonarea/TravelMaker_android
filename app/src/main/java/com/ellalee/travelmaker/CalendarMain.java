package com.ellalee.travelmaker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

public class CalendarMain extends Activity {

//    ListAdapter adapter;

    static SQLiteDatabase db;
    static CalendarDBHelper helper;

    /**
     * 연/월 텍스트뷰
     */
    private TextView tvDate;


    /**
     * 그리드뷰 어댑터
     */
    private static GridAdapter gridAdapter;


    /**
     * 일 저장 할 리스트 -> 날짜 저장할 리스트로 변경해야함
     */
    private ArrayList<Day> dayList;

    /**
     * 스케줄 저장되어있는 리스트
     */
    private static List<String> doList;


    /**
     * 그리드뷰
     */
    private static GridView gridView;


    /**
     * 캘린더 변수
     */
    private static Calendar mCal;


    /*
     * 이전달 버튼
     */
    private Button leftBtn;


    /*
     * 다음달 버튼
     */
    private Button rightBtn;

    /*
     * 스케줄 추가를 위한 floating 버튼
     */
    private FloatingActionButton addSche;

    //dayList counting 변수
    private int count = 0;

    //month back, month next를 위한 변수
    private int back_month_count = 0;
    private int next_month_count = 0;

    private static Context context;

    // 오늘에 날짜를 세팅

    long now = System.currentTimeMillis();

    final Date date = new Date(now);

    //연,월,일을 따로 저장

    final SimpleDateFormat curYearFormat = new SimpleDateFormat("yyyy", Locale.KOREA);

    final SimpleDateFormat curMonthFormat = new SimpleDateFormat("MM", Locale.KOREA);

    final SimpleDateFormat curDayFormat = new SimpleDateFormat("dd", Locale.KOREA);


    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calendar_main);

        CalendarMain.context = getApplicationContext();
        helper = new CalendarDBHelper(CalendarMain.this, "calendar.db", null, 1);
        tvDate = (TextView) findViewById(R.id.tv_date);

        gridView = (GridView) findViewById(R.id.gridview);

        leftBtn = (Button) findViewById(R.id.calendar_month_back);
        rightBtn = (Button) findViewById(R.id.calendar_month_next);
        addSche = (FloatingActionButton) findViewById(R.id.scheFAB);

        //gridview 요일 표시

        dayList = new ArrayList<Day>();
        doList = new ArrayList();

        Day sun = new Day();
        sun.setDay("일");
        dayList.add(sun);

        Day mon = new Day();
        mon.setDay("월");
        dayList.add(mon);

        Day tue = new Day();
        tue.setDay("화");
        dayList.add(tue);

        Day wed = new Day();
        wed.setDay("수");
        dayList.add(wed);

        Day thu = new Day();
        thu.setDay("목");
        dayList.add(thu);

        Day fri = new Day();
        fri.setDay("금");
        dayList.add(fri);

        Day sat = new Day();
        sat.setDay("토");
        dayList.add(sat);

        mCal = Calendar.getInstance();


        //현재 날짜 텍스트뷰에 뿌려줌

        tvDate.setText(mCal.get(Calendar.YEAR) + " / " + (mCal.get(Calendar.MONTH) + 1));

        //이번달 1일 무슨요일인지 판단 mCal.set(Year,Month,Day)
        mCal.set(Integer.parseInt(curYearFormat.format(date)), Integer.parseInt(curMonthFormat.format(date)) - 1, 1);
        int dayNum = mCal.get(Calendar.DAY_OF_WEEK);
        Log.d("dayNum", String.valueOf(dayNum));

        //1일 - 요일 매칭 시키기 위해 공백 add
        for (int i = 1; i < dayNum; i++) {
            Day empty = new Day();
            empty.setDay("");
            dayList.add(empty);

        }

        setCalendarDate(mCal.get(Calendar.YEAR), mCal.get(Calendar.MONTH) + 1);

        gridAdapter = new GridAdapter(getApplicationContext(), dayList);

        gridView.setAdapter(gridAdapter);

        for (int i = 0; i < doList.size(); i++) {
            String date = doList.get(i).substring(doList.get(i).replace(" ", "").length() - 29, doList.get(i).length() - 19);
            String sched = doList.get(i).substring(8, doList.get(i).length() - 29);
            Log.d("DB에 들어가는 doList", date + " " + sched);
            insert(date, sched, "");
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCal.add(Calendar.MONTH, -back_month_count + next_month_count);
                mCal.set(mCal.get(Calendar.YEAR), mCal.get(Calendar.MONTH), 1);
                Intent intent = new Intent(CalendarMain.this, CalendarListMain.class);
                intent.putExtra("day", position - 5 - mCal.get(Calendar.DAY_OF_WEEK));
                intent.putExtra("year", mCal.get(Calendar.YEAR));
                intent.putExtra("month", mCal.get(Calendar.MONTH) + 1);

                String date = String.valueOf(mCal.get(Calendar.YEAR)) + "-" + String.valueOf(mCal.get(Calendar.MONTH) + 1) + "-" + String.valueOf(position - mCal.get(Calendar.DAY_OF_WEEK) - 5);
                ArrayList<String> s = new ArrayList<>();
                ArrayList<String> m = new ArrayList<>();

                Cursor c = db.rawQuery("SELECT schedule, memo FROM calendar WHERE date='" + date + "'", null);
                while(c.moveToNext()) {
                    s.add(c.getString(c.getColumnIndex("schedule")));
                    m.add(c.getString(c.getColumnIndex("memo")));
                }

                intent.putStringArrayListExtra("sche", s);
                intent.putStringArrayListExtra("memo", m);

                startActivityForResult(intent, 1);
            }
        });

        //back button 눌렀을 때
        leftBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                mCal = Calendar.getInstance();
                Log.d("삭제할 월", String.valueOf(mCal.get(Calendar.MONTH) - back_month_count + next_month_count));
                mCal.add(Calendar.MONTH, -back_month_count + next_month_count);
                int dayNum;

                Log.d("삭제된 달의 크기", String.valueOf(dayList.size() - 1));

                for (int i = dayList.size() - 1; i >= 7; i--) {
                    dayList.remove(i);
                }

                back_month_count += 1;
                mCal.add(Calendar.MONTH, -1);
                mCal.set(mCal.get(Calendar.YEAR), mCal.get(Calendar.MONTH), 1);

                dayNum = mCal.get(Calendar.DAY_OF_WEEK);
                Log.d("이번달 채워질 공백의 수", String.valueOf(dayNum));
                Log.d("새로 세팅될 월", String.valueOf(mCal.get(Calendar.MONTH)));

                //1일 - 요일 매칭 시키기 위해 공백 add
                for (int i = 1; i < dayNum; i++) {
                    Day empty = new Day();
                    empty.setDay("");
                    dayList.add(empty);
                }
                tvDate.setText(mCal.get(Calendar.YEAR) + " / " + (mCal.get(Calendar.MONTH) + 1));
                setCalendarDate(mCal.get(Calendar.YEAR), mCal.get(Calendar.MONTH) + 1);
                gridAdapter = new GridAdapter(getApplicationContext(), dayList);
                gridView.setAdapter(gridAdapter);

            }
        });


        //next button 눌렀을 때
        rightBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // 지워야할 달 날짜 세팅
                Log.d("삭제할 월", String.valueOf(mCal.get(Calendar.MONTH) - back_month_count + next_month_count));
                mCal.add(Calendar.MONTH, -back_month_count + next_month_count);
                int dayNum = mCal.get(Calendar.DAY_OF_WEEK) - 1;

                Log.d("새로 세팅된 달의 크기", String.valueOf(dayList.size() - 1));
                Log.d("dayNum", String.valueOf(dayNum));

                for (int i = dayList.size() - 1; i >= 7; i--) {
                    dayList.remove(i);
                }
                next_month_count += 1;

                mCal.add(Calendar.MONTH, 1);
                mCal.set(mCal.get(Calendar.YEAR), mCal.get(Calendar.MONTH), 1);
                dayNum = mCal.get(Calendar.DAY_OF_WEEK);

                Log.d("새로 세팅될 월", String.valueOf(mCal.get(Calendar.MONTH)));

                //1일 - 요일 매칭 시키기 위해 공백 add
                for (int i = 1; i < dayNum; i++) {
                    Day empty = new Day();
                    empty.setDay("");
                    dayList.add(empty);
                }
                tvDate.setText(mCal.get(Calendar.YEAR) + " / " + (mCal.get(Calendar.MONTH) + 1));
                setCalendarDate(mCal.get(Calendar.YEAR), mCal.get(Calendar.MONTH) + 1);
                gridAdapter = new GridAdapter(getApplicationContext(), dayList);
                gridView.setAdapter(gridAdapter);
            }
        });

        addSche.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), CalendarAddSche.class), 1);
            }
        });
    }

    public static Context getAppContext() {
        return CalendarMain.context;
    }

    /*
     * DB에 값 insert하는 method
     */
    public static void insert(String date, String sched, String memo) {
        helper = new CalendarDBHelper(MainActivity.mContext, "calendar.db", null, 1);
        db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("date", date);
        values.put("schedule", sched);
        values.put("memo", memo);

        Cursor c = db.rawQuery("SELECT date, schedule FROM calendar WHERE (date='"  + date + "') AND (schedule='" + sched + "')", null);
        c.moveToFirst();

        if(c.getCount() == 0) {
            db.insert("calendar", null, values);
        }
        else {
           // Toast.makeText(MainActivity.mContext, "이미 저장되어 있는 스케줄입니다!", Toast.LENGTH_LONG).show();
        }
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

    /*
     *DB 읽기
     */
    public List<String> select() {
        db = helper.getReadableDatabase();
        List<String> read = new ArrayList<String>();
        Cursor c = db.query("calendar", null, null, null, null, null, null);

        while(c.moveToNext()) {
            String date = c.getString(c.getColumnIndex("date"));
            String sched = c.getString(c.getColumnIndex("schedule"));
            String memo = c.getString(c.getColumnIndex("memo"));

            read.add("date:"+date+ ",sche:" + sched + ",memo:" + memo);
        }
        return read;
    }

    /*
     *일정을 추가할 때 받은 값들 / DB에 insert
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        String year, month, day, schedule, memo;

        if(resultCode != RESULT_CANCELED) {

            year = data.getStringExtra("year");
            month = data.getStringExtra("month");
            day = data.getStringExtra("day");
            schedule = data.getStringExtra("schedule");
            memo = data.getStringExtra("memo");

            if(resultCode == RESULT_OK) {
                insert(year + "-" + month + "-" + day, schedule, memo);
            }
            mCal.set(mCal.get(Calendar.YEAR), mCal.get(Calendar.MONTH), 1);
            int dayNum = mCal.get(Calendar.DAY_OF_WEEK);
            Log.d("인서트 될때 채워질 공백의 수", String.valueOf(dayNum));
            Log.d("인서트 새로 세팅될 월", String.valueOf(mCal.get(Calendar.MONTH)));

            if(mCal.get(Calendar.YEAR) == Integer.parseInt(year) && mCal.get(Calendar.MONTH) + 1 == Integer.parseInt(month)) {
                for (int i = dayList.size() - 1; i >= 7; i--) {
                    dayList.remove(i);
                }

                //1일 - 요일 매칭 시키기 위해 공백 add
                for (int i = 1; i < dayNum; i++) {
                    Day empty = new Day();
                    empty.setDay("");
                    dayList.add(empty);
                }
                setCalendarDate(mCal.get(Calendar.YEAR), mCal.get(Calendar.MONTH) + 1);
                gridAdapter = new GridAdapter(getApplicationContext(), dayList);
                gridView.setAdapter(gridAdapter);
            }
        }
    }


    /**
     * 해당 월에 표시할 스케줄 세팅
     *
     * @param month
     */

    private void setCalendarDate(int year, int month) {

        mCal.set(Calendar.YEAR, year);
        mCal.set(Calendar.MONTH, month - 1);

        Log.d("스케줄 넣을 월", String.valueOf(month));

        for (int i = 0; i < mCal.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
            Day d = new Day();
            List<String> dbList = new ArrayList<String>();
            count = 0;
            d.setDay("" + String.valueOf(i + 1));
            dbList = select();
            if(new PlanSQLiteHelper(getApplicationContext()).getAllPlanSchedule().size() > 0)
                dbList.addAll(new PlanSQLiteHelper(getApplicationContext()).getAllPlanSchedule());

            for(int a = 0; a < dbList.size(); a++) {
                Log.d("디비에 들어간 list", dbList.get(a));
            }

            for(int j = 0; j < dbList.size(); j++) {
                //date, sche, memo 분리
                Log.d("디비리스트", dbList.get(j));
                String []temp = dbList.get(j).split(",");
                String[] dt = temp[0].substring(temp[0].indexOf(":")+1).split("-");

                if(Integer.parseInt(dt[0]) == mCal.get(Calendar.YEAR)) {
                    if(Integer.parseInt(dt[1]) == month) {
                        if(Integer.parseInt(dt[2]) == Integer.parseInt(d.getDay())) {
                            Log.d("DB에서 얻어온 정보로 통과되는 일", dt[1]);
                            count += 1;
                            d.setSche(temp[1].substring(temp[1].indexOf(":")+1));
                            d.setMemo(temp[2].substring(temp[2].indexOf(":")+1));
                        }
                    }

                }
            }
            dayList.add(d);
        }
    }

    /*
     * 기존에 캘린더에 있던 daylist 객체를 지우는 함수
     */
//    private void deleteCalendarDate(int month, int year) {
//        int useDay = 6;
//
//        // 지워야할 달 날짜 세팅
//        Log.d("delete함수 월 세팅", String.valueOf(month));
//        mCal.set(year, month, 1);
//        int dayNum = mCal.get(Calendar.DAY_OF_WEEK) - 1;
//
//        Log.d( "새로 세팅된 달의 크기",String.valueOf(mCal.getActualMaximum(Calendar.DAY_OF_MONTH) +  dayNum + useDay));
//        Log.d( "useDay",String.valueOf(useDay));
//        Log.d( "dayNum",String.valueOf(dayNum));
//
//        for(int i = mCal.getActualMaximum(Calendar.DAY_OF_MONTH) +  dayNum + useDay; i >= 7; i--) {
//            dayList.remove(i);
//        }
//    }

    public static void setDoList(List<String> d) {
        doList = d;
        String[] doLi = new String[4];
        for (int i = 1; i < doList.size(); i++) {
            Log.d("Dolist debug", doList.get(i));

            doLi = doList.get(i).split(" ");
            String date = doLi[3].substring(1, doLi[3].indexOf('T'));
            String sched = doLi[2];
            Log.d("DB에 들어가는 doList", date + " " + sched);

            insert(date, sched, "");
        }
    }

    public static List<String> getDoList() { return doList; }


    /** 그리드뷰 어댑터 */

    private class GridAdapter extends BaseAdapter {

        private final ArrayList<Day> daylist;


        private final LayoutInflater inflater;



        /**
         * 생성자
         * @param context
         * @param list
         */

        public GridAdapter(Context context, ArrayList<Day> list) {

            this.daylist = list;

            this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }


        @Override

        public int getCount() {

            return daylist.size();

        }


        @Override

        public String getItem(int position) {

            return daylist.get(position).getDay();

        }


        public String getScheItem(int day, int position) {
            if(daylist.size() > day)
                return daylist.get(day).getSche().get(position).toString();
            else
                return "";
        }


        public int getScheTotal(int position) {
            return daylist.get(position).getSche().size();
        }

        @Override

        public long getItemId(int position) {

            return position;

        }


        @Override

        public View getView(int position, View convertView, ViewGroup parent) {



            ViewHolder holder = null;



            if (convertView == null) {

                convertView = inflater.inflate(R.layout.activity_calendar_sub, parent, false);

                holder = new ViewHolder();
                holder.tvItemGridView = (TextView)convertView.findViewById(R.id.tv_item_gridview);
                holder.firScheGridView = (TextView)convertView.findViewById(R.id.first_schedule);
                holder.secScheGridView = (TextView)convertView.findViewById(R.id.second_schedule);
                holder.totalScheGridView = (TextView)convertView.findViewById(R.id.total_shedule);


                convertView.setTag(holder);

            } else {

                holder = (ViewHolder)convertView.getTag();

            }

            holder.tvItemGridView.setText("" + getItem(position));
            if(daylist.get(position).getSche().size() != 0) {
                holder.firScheGridView.setText("" + getScheItem(position, 0));
                holder.firScheGridView.setBackgroundColor(Color.parseColor("#fbc2eb"));
                holder.firScheGridView.setSingleLine();
            }
            if(daylist.get(position).getSche().size() > 1) {
                holder.secScheGridView.setText("" + getScheItem(position, 1));
                holder.secScheGridView.setBackgroundColor(Color.parseColor("#c2e9fb"));
                holder.secScheGridView.setSingleLine();
            }
            if(getScheTotal(position)> 0)
                holder.totalScheGridView.setText("Total: " + getScheTotal(position));



            //해당 날짜 텍스트 컬러,배경 변경

            mCal = Calendar.getInstance();

            //오늘 day 가져옴

            Integer today = mCal.get(Calendar.DAY_OF_MONTH);

            String sToday = String.valueOf(today);

            if (sToday.equals(getItem(position))) { //오늘 day 텍스트 컬러 변경

                holder.tvItemGridView.setTextColor(getResources().getColor(R.color.black));

            }

            ViewGroup.LayoutParams param = convertView.getLayoutParams();
            if(param == null) {
                param = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            param.height = 300; //48dp
            convertView.setLayoutParams(param);

            return convertView;

        }

    }

    private class ViewHolder {

        TextView tvItemGridView;
        TextView firScheGridView;
        TextView secScheGridView;
        TextView totalScheGridView;
    }

    private class Day {
        private String day;
        private ArrayList<String> sche = new ArrayList<String>();
        private ArrayList<String> memo = new ArrayList<String>();

        public void setDay(String d) {
            day = d;
        }

        public void setSche(String s) {
            sche.add(s);
        }

        public String getDay() {
            return day;
        }

        public ArrayList<String> getSche() {
            return sche;
        }

        public void setMemo(String m) {
            memo.add(m);
        }

        public ArrayList<String> getMemo() {
            return memo;
        }
    }
}