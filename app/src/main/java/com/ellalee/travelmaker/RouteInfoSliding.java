package com.ellalee.travelmaker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ScrollingView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class RouteInfoSliding extends View{
    private ArrayList<Marker> markerList;
    private String routeColor;
    private int num;
    private ArrayList<Integer> circle = new ArrayList<>();

    public RouteInfoSliding(Context context) {
        super(context);
    }

    public RouteInfoSliding(Context context, AttributeSet attrs) {
        super(context, attrs);
    //    init(context,attrs);
    }
/*
    public void init(Context context,AttributeSet attrs){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        HorizontalScrollView root = findViewById(R.id.routeInfoHorizontalScroll);
        inflater.inflate(R.layout.activity_route_info_sliding,root,true);
        if(attrs!=null)
        {
            TypedArray arr = context.obtainStyledAttributes(attrs,R.styleable.RouteInfoSliding);
            routeColor = arr.getString(R.styleable.RouteInfoSliding_routeColor);
            num = arr.getInteger(R.styleable.RouteInfoSliding_placeNum,0);
            arr.recycle();
        }
    }
*/
    public void setRoute(Route r){
        markerList = r.getMarkerList();
        num= r.getMarkerList().size();
        routeColor = r.getRouteColor();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int x=450,y=200,rad=80;

        Paint linePnt = new Paint();
        Paint circlePnt = new Paint();
        linePnt.setColor(Color.parseColor(routeColor));
        linePnt.setStrokeWidth(30);

        circlePnt.setColor(Color.YELLOW);
        circlePnt.setStrokeWidth(10);
        Log.d("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%place num",": "+num);

        int cur=200;
        for(int i=0;i<num;i++){
            circle.add(i,cur);
            cur+=x;
        }
        canvas.drawLine(circle.get(0),y,circle.get(num-1),y,linePnt);

        for(int i=0;i<num;i++){
            canvas.drawCircle(circle.get(i),y,rad,circlePnt);
        }
        }
    @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
          super.onMeasure(MeasureSpec.makeMeasureSpec(4000, MeasureSpec.AT_MOST), heightMeasureSpec);
//        super.onMeasure(MeasureSpec.makeMeasureSpec(widthMeasureSpec,MeasureSpec.EXACTLY),heightMeasureSpec);
        }

}
