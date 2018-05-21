package com.ellalee.travelmaker;

import android.graphics.Color;
import android.os.Bundle;

import java.text.SimpleDateFormat;

import java.util.ArrayList;

import java.util.Calendar;

import java.util.Date;

import java.util.List;

import java.util.Locale;

import android.app.Activity;

import android.content.Context;

import android.view.Gravity;
import android.view.LayoutInflater;

import android.view.View;

import android.view.ViewGroup;

import android.widget.BaseAdapter;

import android.widget.GridView;

import android.widget.LinearLayout;
import android.widget.TextView;

public class CalendarMain extends Activity {

    /**

     * 연/월 텍스트뷰

     */

    private TextView tvDate;

    /**

     * 그리드뷰 어댑터

     */

    private GridAdapter gridAdapter;

    /**

     * 일 저장 할 리스트

     */

    private ArrayList<DayVo> dayList;

    /**

     * 그리드뷰

     */

    private GridView gridView;

    /**

     * 캘린더 변수

     */

    private Calendar mCal;

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calendar_main);


        tvDate = (TextView)findViewById(R.id.tv_date);

        gridView = (GridView)findViewById(R.id.gridview);

        // 오늘에 날짜를 세팅 해준다.

        long now = System.currentTimeMillis();

        final Date date = new Date(now);

        //연,월,일을 따로 저장

        final SimpleDateFormat curYearFormat = new SimpleDateFormat("yyyy", Locale.KOREA);

        final SimpleDateFormat curMonthFormat = new SimpleDateFormat("MM", Locale.KOREA);

        final SimpleDateFormat curDayFormat = new SimpleDateFormat("dd", Locale.KOREA);

        //현재 날짜 텍스트뷰에 뿌려줌

        tvDate.setText(curYearFormat.format(date) + "/" + curMonthFormat.format(date));

        //gridview 요일 표시

        dayList = new ArrayList<DayVo>();


        mCal = Calendar.getInstance();


        //이번달 1일 무슨요일인지 판단 mCal.set(Year,Month,Day)

        mCal.set(Integer.parseInt(curYearFormat.format(date)), Integer.parseInt(curMonthFormat.format(date)) - 1, 1);

        int dayNum = mCal.get(Calendar.DAY_OF_WEEK);

        //1일 - 요일 매칭 시키기 위해 공백 add

        for (int i = 1; i < dayNum; i++) {
            DayVo vo = new DayVo();
            vo.setDay("");
            dayList.add(vo);
        }

        ArrayList<DayVo> scheduleList = getSchedule();
        for(DayVo schedule : scheduleList) {
            int sDay = Integer.parseInt(schedule.getDay());

            for(DayVo vo : dayList) {
                int day;
                try {
                    day = Integer.parseInt(vo.getDay());
                }catch(NumberFormatException e) {
                    continue;
                }
                if(sDay == day) {
                    vo.setScheduleList(new ArrayList<ScheduleVo>());
                    for(int i = 0; i < schedule.getScheduleList().size(); i++) {
                        ScheduleVo sv = schedule.getScheduleList().get(i);
                        vo.getScheduleList().add(sv);
                    }
                }
            }
        }

        setCalendarDate(mCal.get(Calendar.MONTH) + 1);

        gridAdapter = new GridAdapter(getApplicationContext(),dayList);

        gridView.setAdapter(gridAdapter);
    }
    /**

     * 해당 월에 표시할 일 수 구함

     *

     * @param month

     */

    private void setCalendarDate(int month) {

        mCal.set(Calendar.MONTH, month - 1);

        for (int i = 0; i < mCal.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {

            DayVo vo = new DayVo();
            vo.setDay(String.valueOf(i+1));
            dayList.add(vo);

        }
    }

    private ArrayList<DayVo> getSchedule() {
        ArrayList<DayVo> list = new ArrayList<>();
        /* 스케줄 읽어오는 부분 */
        return list;
    }


    /**

     * 그리드뷰 어댑터

     *

     */

    private class GridAdapter extends BaseAdapter {

        private final ArrayList<DayVo> list;
        private final LayoutInflater inflater;

        /**

         * 생성자

         *

         * @param context

         * @param list

         */

        public GridAdapter(Context context, ArrayList<DayVo> list) {

            this.list = list;

            this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override

        public int getCount() {

            return list.size();

        }

        @Override

        public Object getItem(int position) {

            return list.get(position);

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

                convertView.setTag(holder);

            } else {

                holder = (ViewHolder)convertView.getTag();

            }

            DayVo vo = dayList.get(position);
            holder.tvItemGridView.setText("" + getItem(position));
            if(vo.getDay().equals("")) {
                convertView.setClickable(false);
            }

            //해당 날짜 텍스트 컬러,배경 변경

            mCal = Calendar.getInstance();

            //오늘 day 가져옴

            Integer today = mCal.get(Calendar.DAY_OF_MONTH);

            String sToday = String.valueOf(today);

            if (sToday.equals(getItem(position))) { //오늘 day 텍스트 컬러 변경

                holder.tvItemGridView.setTextColor(getResources().getColor(R.color.black));

            }

            if(vo.getScheduleList() != null) {
                LinearLayout linearLayout = (LinearLayout)convertView;
                int scheduleCnt = vo.getScheduleList().size();
                for(int i = 0; i < scheduleCnt; i++) {
                    ScheduleVo sv = vo.getScheduleList().get(i);
                    TextView scheduleTv = new TextView(convertView.getContext());
                    scheduleTv.setGravity(Gravity.CENTER_VERTICAL);

                    if(i == 2) {
                        scheduleTv.setText("TOTAL: " + String.valueOf(scheduleCnt+1));
                        break;
                    }
                    scheduleTv.setTextColor(Color.WHITE);
                    scheduleTv.setSingleLine();

                    if(sv.getType().equals("PM")) {
                        scheduleTv.setBackgroundColor(Color.parseColor("@color/babypink"));
                    }else if(sv.getType().equals("BM")) {
                        scheduleTv.setBackgroundColor(Color.parseColor("@color/violetblue"));
                    }
                    scheduleTv.setText(sv.getName());
                }
            }

            return convertView;

        }
    }


    private class ViewHolder {

        TextView tvItemGridView;

    }

    private class DayVo {
        private String day;
        private ArrayList<ScheduleVo> scheduleList;

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public ArrayList<ScheduleVo>getScheduleList() {
            return scheduleList;
        }

        public void setScheduleList(ArrayList<ScheduleVo>scheduleList) {
            this.scheduleList = scheduleList;
        }
    }

    private class ScheduleVo {
        private String schedule_name;
        private String type;

        public String getName() {
            return schedule_name;
        }

        public void setName(String sName) {
            schedule_name = sName;
        }

        public String getType() {
            return type;
        }

        public void setType(String sType) {
            type = sType;
        }
    }

}
