package com.ellalee.travelmaker;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.icu.text.LocaleDisplayNames;
import android.media.Image;
import android.media.ImageReader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.Inflater;

import static android.view.View.INVISIBLE;
import static android.view.View.LAYER_TYPE_HARDWARE;
import static android.view.View.VISIBLE;

public class planListActivity extends AppCompatActivity {

    SQLiteDatabase db;
    PlanSQLiteHelper helper;
    ArrayList<planListItem> plans;
    GridView planListView;
    Button activateDelete;


    boolean delete_mode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_list);

        helper = new PlanSQLiteHelper(getApplicationContext());
        planListView = findViewById(R.id.planListView);
        plans = new ArrayList<>();

        activateDelete = findViewById(R.id.btnActivateDelete);
        activateDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(delete_mode){
                    delete_mode=false;
                    planListView.setAdapter(new PlanGridAdapter(delete_mode));
                    activateDelete.setBackground(getDrawable(R.drawable.close_bin));
                }else{
                    delete_mode=true;
                    planListView.setAdapter(new PlanGridAdapter(delete_mode));
                    activateDelete.setBackground(getDrawable(R.drawable.open_bin));
                }
            }
        });
        invalidate();
    }
    public void select(){
        db =helper.getReadableDatabase();
        Cursor cursor = db.query("plans",null,null,null,null,null,null);
        while(cursor.moveToNext()){

            plans.add(new planListItem(cursor.getString(cursor.getColumnIndex("title")),
                    new Date( cursor.getInt(cursor.getColumnIndex("year")),
                            cursor.getInt(cursor.getColumnIndex("month")),
                            cursor.getInt(cursor.getColumnIndex("day"))),
                    helper.getRouteListCount(cursor.getInt(cursor.getColumnIndex("id"))),
                    cursor.getInt(cursor.getColumnIndex("id"))));

          /*  planList[i++]="*"+cursor.getInt(cursor.getColumnIndex("id"))+"* "
                    +cursor.getString(cursor.getColumnIndex("title"))+"\n"
                    +helper.getRouteListCount(cursor.getInt(cursor.getColumnIndex("id")))+"days plan "
                    +cursor.getInt(cursor.getColumnIndex("year"))+"/ "
                    +cursor.getInt(cursor.getColumnIndex("month"))+"/ "
                    +cursor.getInt(cursor.getColumnIndex("day"));
          */
        }
        cursor.close();
    }

    public void invalidate(){
        select();
        PlanGridAdapter adapter =new PlanGridAdapter(delete_mode);
        adapter.notifyDataSetChanged();
        planListView.setAdapter(adapter);
    }
    public void refresh(int id){
        //plans.remove(id);
        planListView.setAdapter(new PlanGridAdapter(delete_mode));
    }

    public class planListItem{
        String title;
        Date startDate;
        int routeNum;
        long plan_id;
        int imgSrc;

        planListItem(){
            title = " ";
            startDate = new Date(00,00,00);
            routeNum = 0;
            plan_id = -1;
            imgSrc = R.drawable.plan_img_default;
        }

        planListItem(Plan p){
            title = p.getTitle();
            startDate = new Date(p.getYear(),p.getMonth(),p.getDay());
            routeNum = p.getRoutesList().size();
            plan_id = p.getId();
            imgSrc = R.drawable.plan_img_default;
        }

        planListItem(String t,Date d,int n,long id){
            title = t;
            startDate = d;
            routeNum = n;
            plan_id = id;
            imgSrc = R.drawable.plan_img_default;
        }

        public String getTitle(){
            return this.title;
        }
        public String getDate(){
            int m=this.startDate.getMonth()+1;
            return this.startDate.getYear()+"/ "+m+"/ "+this.startDate.getDate();
        }
        public int getImgSrc(){
            return this.imgSrc;
        }
        public long getPlan_id(){
            return this.plan_id;
        }
        public void setTitle(String newTitle){
            this.title = newTitle;
        }
        public void setDate(Date newDate){
            this.startDate = newDate;
        }
        public String toString() {
            String msg;
//            msg = "*"+this.plan_id+"* "+this.title+"\n"+routeNum+"days plan "+startDate.toString();
            msg = "*"+this.plan_id+"* "+this.title+"\n"+routeNum+"days plan";
            return msg;
        }
    }

    public class PlanGridAdapter extends BaseAdapter{
        LayoutInflater inflater;
        boolean mode =false;

        TextView title ;
        TextView date ;
        Button btnDelete ;
        ImageView imgPlan ;

        public PlanGridAdapter(boolean delete_mode){
            inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mode = delete_mode;
        }

        @Override
        public int getCount() {
            return plans.size();
        }

        @Override
        public Object getItem(int i) {
            return plans.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            final int viewId =i;

            if(view == null){
                view=inflater.inflate(R.layout.plan_grid_item,viewGroup,false);
            }
            title = view.findViewById(R.id.txtPlanTitle);
            date = view.findViewById(R.id.txtPlanDate);
            btnDelete = view.findViewById(R.id.btnPlanDelete);
            imgPlan = view.findViewById(R.id.imgPlanNote);

            if(mode){
                btnDelete.setVisibility(VISIBLE);
            }else{
                btnDelete.setVisibility(View.GONE);
            }

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(planListActivity.this);
                    alert.setMessage("정말 삭제할까요?");
                    alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                     //       Toast.makeText(planListActivity.this, "delete plan "+plans.get(viewId).getPlan_id(), Toast.LENGTH_SHORT).show();
                            db = helper.getWritableDatabase();
                            helper.deletePlan(plans.get(viewId).getPlan_id());
                            plans.remove(viewId);
                            refresh(viewId);
                            //invalidate();
                        }
                    });
                    alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    alert.show();
                }
            });

            title.setText(plans.get(i).getTitle());
            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(planListActivity.this);
                    alert.setTitle("일정 제목을 알려주세요.");

                    final EditText editTitle = new EditText(planListActivity.this);
                    alert.setView(editTitle);

                    alert.setPositiveButton("저장", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            db = helper.getWritableDatabase();
                            String newTitle = editTitle.getText().toString();
                            plans.get(viewId).setTitle(newTitle);
                            helper.updatePlan(plans.get(viewId).getPlan_id(),newTitle); //plan_id starts from 1.
                            planListView.invalidateViews();
                        }
                    });

                    alert.setNegativeButton("취소",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    });
                    alert.show();
                }
            });
            if(plans.get(i).startDate.toString().equals(new Date(0,0,0).toString())){
                date.setText("날짜 설정하기!");
            }else{
                date.setText(plans.get(i).getDate());
            }
            date.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(planListActivity.this);

                    final DatePicker datePicker = new DatePicker(planListActivity.this);
                    datePicker.setBackgroundColor(Color.TRANSPARENT);
                    alert.setView(datePicker);
                    
                    alert.setPositiveButton("저장", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton){
                            db = helper.getWritableDatabase();
                            Date newDate = new Date(datePicker.getYear(),datePicker.getMonth(),datePicker.getDayOfMonth());
                            plans.get(viewId).setDate(newDate);
                            helper.updatePlan(plans.get(viewId).getPlan_id(),newDate); //plan_id starts from 1.
                            planListView.invalidateViews();
                        }
                    });

                    alert.setNegativeButton("취소",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    });
                    alert.show();
                }
            });
            imgPlan.setImageResource(plans.get(viewId).getImgSrc());
            imgPlan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(),MapMain.class);
                    Log.d("INTENT PLAN_ID",plans.get(viewId).plan_id+" ");
                    intent.putExtra("plan_id",plans.get(viewId).plan_id);
                    startActivity(intent);
                }
            });
            return view;
        }
    }
}
