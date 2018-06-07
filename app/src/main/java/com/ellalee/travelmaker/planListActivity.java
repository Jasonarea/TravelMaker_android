package com.ellalee.travelmaker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.zip.Inflater;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class planListActivity extends AppCompatActivity {

    SQLiteDatabase db;
    PlanSQLiteHelper helper;
    String[] planList;
    ArrayList<planListItem> plans;
    GridView planListView;
    ImageButton activateDelete;
    ImageButton btnDelete;

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
                    activateDelete.setImageResource(R.drawable.close_bin);
                }else{
                    delete_mode=true;
                    activateDelete.setImageResource(R.drawable.open_bin);
                }
            }
        });

        invalidate();
/*
        planListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                db = helper.getReadableDatabase();
                String msg = planList[i].substring(1,3);

                Toast.makeText(planListActivity.this,msg, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(),MapMain.class);
                intent.putExtra("plan_id",plans.get(i).plan_id);
                startActivity(intent);
            }
        });
*/
    }
    public void select(){
        db =helper.getReadableDatabase();
        Cursor cursor = db.query("plans",null,null,null,null,null,null);
         planList = new String[cursor.getCount()];
        int i=0;
        while(cursor.moveToNext()){

            plans.add(new planListItem(cursor.getString(cursor.getColumnIndex("title")),
                    new Date( cursor.getInt(cursor.getColumnIndex("year")),
                            cursor.getInt(cursor.getColumnIndex("month")),
                            cursor.getInt(cursor.getColumnIndex("day"))),
                    helper.getRouteListCount(cursor.getInt(cursor.getColumnIndex("id"))),
                    cursor.getInt(cursor.getColumnIndex("id"))));

            planList[i]= plans.get(i).toString();
            i++;
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
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,planList);
        Log.d("PLAN NUM : ",plans.size()+"*******");
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
            plan_id=p.getId();
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
            return startDate.getYear()+"/ "+startDate.getMonth()+"/ "+startDate.getDay();
        }
        public int getImgSrc(){
            return this.imgSrc;
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
        boolean mode;

        TextView title ;
        TextView date ;
        ImageButton btnDelete ;
        ImageView imgPlan ;

        public PlanGridAdapter(boolean m){
            inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mode = m;
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

            Log.d("DELETE MODE "," "+mode);
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mode){
                        delete_mode=false;
                        btnDelete.setVisibility(INVISIBLE);
                    }else{
                        delete_mode=true;
                        btnDelete.setVisibility(VISIBLE);
                    }
                }
            });

            title.setText(plans.get(i).getTitle());
/*
            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getApplicationContext());
                    alert.setTitle("Edit plan title");

                    final EditText editTitle = new EditText(getApplicationContext());
                    alert.setView(editTitle);

                    alert.setPositiveButton("저장", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String newTitle = editTitle.getText().toString();
                            title.setText(newTitle);
                            //db작업필요
                        }
                    });

                    alert.setNegativeButton("취소",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    });
                    alert.show();
                }
            });
*/
            date.setText(plans.get(i).getDate());
            imgPlan.setImageResource(plans.get(i).getImgSrc());
            imgPlan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    TextView tv = view.findViewById(R.id.getViewId);
//                    int id = Integer.parseInt(String.valueOf(tv.getText()));

                    Intent intent = new Intent(getApplicationContext(),MapMain.class);
                    intent.putExtra("plan_id",plans.get(viewId).plan_id);
                    startActivity(intent);
                }
            });
            //btnDelete.//img로 이후에 변경 //onclick도 추가

            return view;
        }


    }


}
