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
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.common.collect.MapMaker;

import java.util.ArrayList;
import java.util.Iterator;

public class RouteInfoSliding extends View{
    private ArrayList<Marker> markerList;
    private int mIndex;
    private String routeColor;
    private int num;
    private ArrayList<Integer> circle = new ArrayList<>();
    private ArrayList<Rect> area = new ArrayList<>();
    private int x=450,y=200,rad=40;

    public RouteInfoSliding(Context context) {
        super(context);
    }

    public RouteInfoSliding(Context context, AttributeSet attrs) {
        super(context, attrs);
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

        int cur=200;
        for(int i=0;i<num;i++){
            circle.add(i,cur);
            area.add(new Rect(cur,y-rad,cur+rad*2,y+rad));

            cur+=x;
        }
    }
    public ArrayList<Marker> getModified(){
        return markerList;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint linePnt = new Paint();
        Paint circlePnt = new Paint();
        Paint textPnt = new Paint();

        linePnt.setColor(Color.parseColor(routeColor));
        linePnt.setStrokeWidth(20);
        textPnt.setColor(getResources().getColor(R.color.colorRouteInfoText));
        textPnt.setTextSize(50);


        canvas.drawLine(circle.get(0),y,circle.get(num-1),y,linePnt);


        for(int i=0;i<num;i++){
/*            placeBtn = new ImageButton(cnxt);
            placeBtn.setImageResource(R.drawable.marker_residence);
            placeBtn.setClickable(true);
            placeBtn.setOnDragListener();
            placeBtn.setX(circle.get(i));
            placeBtn.setY(y);
            placeBtn.setLayoutParams(lp);
            ((HorizontalScrollView)this.getParent()).addView(placeBtn,70,70);
*/

            circlePnt.setColor(Color.WHITE);
            circlePnt.setStyle(Paint.Style.FILL);
            canvas.drawCircle(circle.get(i),y,rad,circlePnt);

            circlePnt.setColor(getResources().getColor(R.color.colorRouteInfoPlace));
            circlePnt.setStrokeWidth(30);
            circlePnt.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(circle.get(i),y,rad,circlePnt);

            canvas.drawText(markerList.get(i).getTitle(),area.get(i).left-rad,area.get(i).bottom+(rad*2),textPnt);

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int x=(int)event.getX();
        int y=(int)event.getY();
        Log.d("NUM","TOTAL NUM :"+num);

        for(mIndex=0;mIndex<num;mIndex++){
            if (area.get(mIndex).contains(x,y)  && event.getAction()==MotionEvent.ACTION_DOWN)
            {
                Log.d("BITMAP", "Marker "+mIndex+" touched ");

                MenuBuilder popMenu = new MenuBuilder(getContext());
                final MenuInflater inflater = new MenuInflater(getContext());
                inflater.inflate(R.menu.menu_marker,popMenu);
                MenuPopupHelper opt = new MenuPopupHelper(getContext(),popMenu,findViewById(R.id.routeInfoDraw));
                opt.setForceShowIcon(true);

                popMenu.setCallback(new MenuBuilder.Callback() {
                    @Override
                    public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) { 
                        Log.d("INDEX",": +++++++++++"+mIndex);
                        switch (item.getItemId()){
                            case R.id.option_delete:
                                Iterator<Marker> iterator = markerList.iterator();
                                while(iterator.hasNext())
                                {
                                    Marker m = iterator.next();
                                    if(m.equals(markerList.get(mIndex)))
                                        m.remove();
                                }
                                break;
                            case R.id.option_residence:
                                markerList.get(mIndex).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_residence));
                                break;
                        }
                        return false;
                    }

                    @Override
                    public void onMenuModeChange(MenuBuilder menu) {}
                });
                opt.show(circle.get(mIndex),0);
                return true;
            }

        }
        return false;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(4000, MeasureSpec.AT_MOST), heightMeasureSpec);
//      super.onMeasure(MeasureSpec.makeMeasureSpec(widthMeasureSpec,MeasureSpec.EXACTLY),heightMeasureSpec);
    }
}
