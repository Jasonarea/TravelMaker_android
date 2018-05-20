package com.ellalee.travelmaker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class RouteInfoSliding extends View {
    /*    private int index;
        private ArrayList<Marker> markerList;
        private String routeColor = new String();
        private TextView textView;
    */
    public RouteInfoSliding(Context context) {
        super(context);
    }

    public RouteInfoSliding(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /*
        public RouteInfoSliding(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        public void setRoute(Route r){
            index = r.getIndex();
            markerList = r.getMarkerList();
            routeColor = r.getRouteColor();
            textView = findViewById(R.id.slidingTitle);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Toast.makeText(getContext(), "route info custom", Toast.LENGTH_SHORT).show();
            textView.setText("Route info of Day "+index+" in custom view");
            Paint pnt = new Paint();
            pnt.setColor(Color.parseColor(routeColor));
            canvas.drawCircle(500,500,20,pnt);
        }
        */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint pnt = new Paint();
        pnt.setColor(Color.RED);
        pnt.setStrokeWidth(20);

        canvas.drawColor(Color.WHITE);
        canvas.drawLine(0,0,4000,700,pnt);
/*        for (int x = 0; x < 200; x += 5) {
            canvas.drawLine(x, 0, 200 - x, 100, pnt);
        }*/
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 300 means that our View must have minimum height=300. Defind your value for height.

//        super.onMeasure(4000 ,heightMeasureSpec);
        super.onMeasure(MeasureSpec.makeMeasureSpec(widthMeasureSpec,MeasureSpec.EXACTLY),heightMeasureSpec);
    }
}
