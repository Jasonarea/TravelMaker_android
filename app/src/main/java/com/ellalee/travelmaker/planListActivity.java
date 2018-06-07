package com.ellalee.travelmaker;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

public class planListActivity extends AppCompatActivity {

    SQLiteDatabase db;
    PlanSQLiteHelper helper;
    String[] planList;
    ArrayList<planListItem> plans;
    ListView planListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_list);

        helper = new PlanSQLiteHelper(getApplicationContext());
        planListView = findViewById(R.id.planListView);
        plans = new ArrayList<>();

        invalidate();

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
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,planList);
        planListView.setAdapter(adapter);
    }

    public class planListItem{
        String title;
        Date startDate;
        int routeNum;
        long plan_id;

        planListItem(){
            title = " ";
            startDate = new Date(00,00,00);
            routeNum = 0;
            plan_id = -1;
        }

        planListItem(Plan p){
            title = p.getTitle();
            startDate = new Date(p.getYear(),p.getMonth(),p.getDay());
            routeNum = p.getRoutesList().size();
            plan_id=p.getId();
        }

        planListItem(String t,Date d,int n,long id){
            title = t;
            startDate = d;
            routeNum = n;
            plan_id = id;
        }

        public String toString() {
            String msg;
            msg = "*"+this.plan_id+"* "+this.title+"\n"+routeNum+"days plan "+startDate.toString();
            return msg;
        }
    }


}
