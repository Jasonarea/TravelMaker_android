package com.ellalee.travelmaker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class MenuMain extends AppCompatActivity {

    LinearLayout menuPage = null;
    ImageButton menu_button;

    Animation translateLeft = null;
    Animation translateRight = null;

    boolean isPageState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_main);

        menuPage = (LinearLayout)findViewById(R.id.menuPage);
        translateRight = AnimationUtils.loadAnimation(this, R.anim.translate_left);
        translateLeft = AnimationUtils.loadAnimation(this, R.anim.translate_right);

        SlidingAnimationListener listener = new SlidingAnimationListener();
        translateLeft.setAnimationListener(listener);
        translateRight.setAnimationListener(listener);

        menu_button = (ImageButton)findViewById(R.id.menu_action_button);
        menu_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(isPageState) {
                    menuPage.startAnimation(translateRight);
                }
                else {
                    menuPage.setVisibility(View.VISIBLE);
                    menuPage.startAnimation(translateLeft);
                }
            }
        });

    }

    class SlidingAnimationListener implements Animation.AnimationListener {
        public void onAnimationStart(Animation anim) { }
        public void onAnimationEnd(Animation anim) {
            if (isPageState) {
                menuPage.setVisibility(View.INVISIBLE);
                isPageState = false;
            }else {
                isPageState = true;
            }
        }

        public void onAnimationRepeat(Animation anim) { }
    }
}
