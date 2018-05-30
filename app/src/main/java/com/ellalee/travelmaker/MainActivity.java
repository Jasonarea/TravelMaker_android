package com.ellalee.travelmaker;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final String[] navItems = {"Gmail Login", "Budget"};
    private ListView lvNavList;
    private FrameLayout flContainer;
    private DrawerLayout dlDrawer;
    private ImageButton btn;

    @Override

    public void onBackPressed() {

        if (dlDrawer.isDrawerOpen(lvNavList)) {

            dlDrawer.closeDrawer(lvNavList);

        } else {

            super.onBackPressed();

        }
    }

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);



        lvNavList = (ListView)findViewById(R.id.lv_activity_main_nav_list);

        flContainer = (FrameLayout)findViewById(R.id.fl_activity_main_container);

        btn = (ImageButton)findViewById(R.id.menu_action_button);



        btn.setOnClickListener(new OnClickListener() {



            @Override

            public void onClick(View v) {

                Toast.makeText(getApplicationContext(), "open", Toast.LENGTH_SHORT).show();

                dlDrawer.openDrawer(lvNavList);

            }

        });

        dlDrawer = (DrawerLayout)findViewById(R.id.dl_activity_main_drawer);


        lvNavList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, navItems));

        lvNavList.setOnItemClickListener(new MainActivity.DrawerItemClickListener());

    }


    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override

        public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {

            switch (position) {

                case 0:

                   // startActivity(new Intent(getApplicationContext(), GmailSync.class))

                    break;

                case 1:

                    flContainer.setBackgroundColor(Color.parseColor("#5F9EA0"));

                    break;

            }

            dlDrawer.closeDrawer(lvNavList);

        }

    }

    public void calenderMain(View v) {
        startActivity(new Intent(getApplicationContext(), CalendarMain.class));
    }

    public void mapMain(View v){
        startActivity(new Intent(getApplicationContext(),MapMain.class));
    }
}
