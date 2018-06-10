package com.ellalee.travelmaker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.res.ResourcesCompat;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.common.collect.MapMaker;

import java.util.ArrayList;
import java.util.Iterator;

import javax.mail.Quota;

public class RouteInfoSliding extends View {
    private ArrayList<Marker> markerList;
    private String routeColor;
    private int num;

    private Route route;

    private GoogleMap map;
    private PlanSQLiteHelper db;

    private ArrayList<Integer> circle = new ArrayList<>();
    private ArrayList<Rect> area = new ArrayList<>();
    private final int x=450,y=200,rad=50,start=200;

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
    public void setRoute(Route r,GoogleMap gMap){
        route = r;
        markerList = route.getMarkerList();
        num= route.getMarkerList().size();
        routeColor = route.getRouteColor();
        map = gMap;
        db = new PlanSQLiteHelper(getContext());

        int cur=start;
        for(int i=0;i<num;i++){
            circle.add(i,cur);
            area.add(new Rect(cur-rad,y-rad,cur+rad,y+rad));

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
        textPnt.setTypeface(ResourcesCompat.getFont(getContext(),R.font.nanumbarunpen_b));

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

            String printTitle = markerList.get(i).getTitle();
            if(printTitle.length()>8){
                if(printTitle.equals("Click to edit!")){
                    printTitle = " ";
                }else{
                    printTitle = printTitle.substring(0,7);
                }
            }
            canvas.drawText(printTitle,area.get(i).left-rad,area.get(i).bottom+(rad*2),textPnt);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int posX=(int)event.getX();
        int posY=(int)event.getY();
        final int idx;
        Log.d("NUM","TOTAL NUM :"+num);

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            if( y-rad <= posY && posY <= y+rad){
                if( start-rad <= posX && posX <= start+(x*(num-1))+rad){ //in clickable area
                    idx = (posX-start)/x;
                    if( (posX-start) % x <= Math.abs(rad)){ //circle
                        Log.d("CLICK","CIRCLE "+idx);

                        MenuBuilder popMenu = new MenuBuilder(getContext());
                        final MenuInflater inflater = new MenuInflater(getContext());
                        inflater.inflate(R.menu.menu_marker,popMenu);
                        MenuPopupHelper opt = new MenuPopupHelper(getContext(),popMenu,findViewById(R.id.routeInfoDraw));
                        opt.setForceShowIcon(true);
                        opt.setGravity(TEXT_ALIGNMENT_CENTER);
                        opt.show(circle.get(idx),0);

                        popMenu.setCallback(new MenuBuilder.Callback() {
                            @Override
                            public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                                MarkerTag tag;
                                switch (item.getItemId()){
                                    case R.id.option_delete:
                                        db.deleteMarkerList(route.getId(),Long.valueOf(route.getMarkerList().get(idx).getTag().toString()));
                                        markerList.remove(idx);
                                        if(markerList.size()==1){
                                            markerList.clear();
                                        }
                                        route.setMarkerList(map,getModified());
                                        setRoute(route,map);
                                        invalidate();
                                        break;
                                    case R.id.option_residence:
                                        route.getMarkerList().get(idx).setIcon(MapMain.Micon.getMarkerIcon(1));
                                        tag = (MarkerTag) route.getMarkerList().get(idx).getTag();
                                        tag.setIcon(1);
                                        route.getMarkerList().get(idx).setTag(tag);
                                        db.updateMarker(markerList.get(idx),1);
                                        break;
                                    case R.id.option_restaurant:
                                        route.getMarkerList().get(idx).setIcon(MapMain.Micon.getMarkerIcon(2));
                                        tag = (MarkerTag) route.getMarkerList().get(idx).getTag();
                                        tag.setIcon(2);
                                        route.getMarkerList().get(idx).setTag(tag);
                                        db.updateMarker(markerList.get(idx),2);
                                        break;
                                    case R.id.option_shopping:
                                        route.getMarkerList().get(idx).setIcon(MapMain.Micon.getMarkerIcon(3));
                                        tag = (MarkerTag) route.getMarkerList().get(idx).getTag();
                                        tag.setIcon(3);
                                        route.getMarkerList().get(idx).setTag(tag);
                                        db.updateMarker(markerList.get(idx),3);
                                        break;
                                    case R.id.option_transport:
                                        route.getMarkerList().get(idx).setIcon(MapMain.Micon.getMarkerIcon(4));
                                        tag = (MarkerTag) route.getMarkerList().get(idx).getTag();
                                        tag.setIcon(4);
                                        route.getMarkerList().get(idx).setTag(tag);
                                        db.updateMarker(markerList.get(idx),4);
                                        break;
                                    case R.id.option_default:
                                        route.getMarkerList().get(idx).setIcon(MapMain.Micon.getMarkerIcon(0));
                                        tag = (MarkerTag) route.getMarkerList().get(idx).getTag();
                                        tag.setIcon(0);
                                        route.getMarkerList().get(idx).setTag(tag);
                                        db.updateMarker(markerList.get(idx),0);
                                        break;
                                }
                                return true;
                            }

                            @Override
                            public void onMenuModeChange(MenuBuilder menu) {
                            }
                        });

                    }
                    else{ //line
                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                        alert.setMessage("이동경로를 알아볼까요?");
                        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                String uri ="http://maps.google.com/maps?saddr="
                                        +markerList.get(idx).getPosition().latitude+","
                                        +markerList.get(idx).getPosition().longitude
                                        +"&daddr="
                                        +markerList.get(idx+1).getPosition().latitude+","
                                        +markerList.get(idx+1).getPosition().longitude;

                                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,Uri.parse(uri));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addCategory(Intent.CATEGORY_LAUNCHER );
                                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                                (getContext()).startActivity(intent);

                            }
                        });
                        alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {}
                        });
                        alert.show();
                    }
                }
            }
        }
        /*
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

        }*/
        return false;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(6000, MeasureSpec.AT_MOST), heightMeasureSpec);
//      super.onMeasure(MeasureSpec.makeMeasureSpec(widthMeasureSpec,MeasureSpec.EXACTLY),heightMeasureSpec);
    }
}
