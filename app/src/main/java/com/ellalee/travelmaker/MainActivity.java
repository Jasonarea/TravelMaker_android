package com.ellalee.travelmaker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void menuMain(View v) {
        startActivity(new Intent(getApplicationContext(), MenuMain.class));
}

    public void calenderMain(View v) {
        startActivity(new Intent(getApplicationContext(), CalendarMain.class));
    }
   /* public void mapMain(View v){
        startActivity(new Intent(getApplicationContext(),MapMain.class));
    }
    */

}
