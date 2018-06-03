package com.ellalee.travelmaker;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.common.collect.MapMaker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MapMain extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener{

    private SQLiteDatabase db ;
    private PlanSQLiteHelper helper;
    private Plan plan;
                      //marker 0:default 1:residence 2: ....
    private int edit_mode = 0;  // 0:onlyView 1:editMarker 2: editRoute
    private int routeIndex = 0; // day 1,2 ....
    private int newIndex = 0;   // recently added index
    private Geocoder geocoder;
    private EditText editAddress;
    private GoogleMap googleMap;
    private Button btnSearch;
    private Button btnRoute;
    private Button btnPlace;

    private ContextMenu contextMenu;

    private ArrayList<Route> routes = new ArrayList<>(); //multi
//   private ArrayList<Route> routes; //multi
    String[] routeColor;

    Animation slidingOpen;
    Animation slidingClose;
    LinearLayout slidingLayout;
    RouteInfoSliding infoSliding;
    HorizontalScrollView routeHS;
    RouteInfoSliding routeInfoDraw;
    boolean openPage=false;


    private class SlidingPageAnimationListener implements Animation.AnimationListener{
    @Override
    public void onAnimationStart(Animation animation) {}

    @Override
    public void onAnimationRepeat(Animation animation) {}

    @Override
    public void onAnimationEnd(Animation animation) {
        slidingLayout.setVisibility(View.INVISIBLE);
        routeInfoDraw.setVisibility(View.INVISIBLE);
        routeHS.setVisibility(View.INVISIBLE);

        if(openPage){
            slidingLayout.setVisibility(View.VISIBLE);
            routeInfoDraw.setVisibility(View.VISIBLE);
            routeHS.setVisibility(View.VISIBLE);
        }
    }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_main);

        Intent intent = getIntent();
        long plan_id = intent.getExtras().getLong("plan_id");
        /*
        plan_id를 통해 db에서 plan불러오기
        plan  = (Plan)intent.getSerializableExtra("newPlan"); ///intent 넘기기
        routes = plan.getRoutesList();
*/
        Toast.makeText(getApplicationContext(),"plan_id:"+plan_id, Toast.LENGTH_SHORT).show();

        geocoder = new Geocoder(this);
        editAddress = findViewById(R.id.editAddress);
        btnSearch = findViewById(R.id.btnSearch);
        btnRoute = findViewById(R.id.btnRoute);
         btnPlace = findViewById(R.id.btnPlace);

        routeColor = getResources().getStringArray(R.array.routeColor);
        registerForContextMenu(btnRoute);

        slidingLayout = findViewById(R.id.slidingLayout);
        infoSliding = new RouteInfoSliding(this);
        routeHS = findViewById(R.id.routeInfoHorizontalScroll);
        routeInfoDraw = findViewById(R.id.routeInfoDraw);

        slidingOpen = AnimationUtils.loadAnimation(this,R.anim.sliding_open);
        slidingClose = AnimationUtils.loadAnimation(this,R.anim.sliding_close);

        SlidingPageAnimationListener animationListener = new SlidingPageAnimationListener();
        slidingOpen.setAnimationListener(animationListener);
        slidingClose.setAnimationListener(animationListener);

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(final GoogleMap map) {
        googleMap = map;
        geocoder = new Geocoder(this);

        //initialize when plan is created
        //initial route setting (default route)
        if(routes.isEmpty()) {
//          Route day1 = new Route(0, routeColor[0], googleMap);
            Route day1 = new Route(0, routeColor[0]);
            routes.add(0, day1);
        }

        //main activity에서 valid한 로케이션 확인 후 넘겨주기
        LatLng center = new LatLng(37.56, 126.97);
//        LatLng center = plan.getCentre();
        //************modify to get position point from main activity

        map.moveCamera(CameraUpdateFactory.newLatLng(center));
        map.animateCamera(CameraUpdateFactory.zoomTo(10));

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {    // add a marker
                String str = editAddress.getText().toString();
                List<Address> list = null;
                double latitude, longitude;

                try {
                    list = geocoder.getFromLocationName(str, 10);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MapMain.this, "I/O Error", Toast.LENGTH_SHORT).show();
                }

                if (list != null) {
                    if (list.size() == 0) {
                        Toast.makeText(MapMain.this, "No matching address info", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        latitude = list.get(0).getLatitude();
                        longitude = list.get(0).getLongitude();

                        LatLng SearchPoint = new LatLng(latitude, longitude);

                        MarkerOptions mOptions = new MarkerOptions();
                        mOptions.title(str);
                        mOptions.draggable(true);
                        mOptions.position(SearchPoint);
                        mOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.heart));

                        //add marker
                        googleMap.addMarker(mOptions).setTag(0);
                        //zoom camera view
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SearchPoint, 15));
                    }
                }
            }
        });

        //adding a marker by longclick
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                MarkerOptions option =new MarkerOptions();
                option.draggable(true);
                option.position(latLng);
                option.title(" ");
                option.icon(BitmapDescriptorFactory.fromResource(R.drawable.heart));
                googleMap.addMarker(option).setTag(0);
                //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
            }
        });

        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            float alpha;

            @Override
            public void onMarkerDragStart(Marker marker) {
                alpha = marker.getAlpha();
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                marker.setAlpha(10);
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                //find every route which include the marker
                marker.setAlpha(alpha);

                Iterator<Route> route_iterator = routes.iterator(); //route iterator
                Route cur;
                while (route_iterator.hasNext()) {
                    cur = route_iterator.next();
                    cur.setPoints(googleMap); //if(route_iterator.contains(marker)
                }
            }
        });
        googleMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                if(edit_mode==2){ //if the edit_mode is on
                    editRoute(btnRoute); //turn off the edit mode
                }
                TextView title = findViewById(R.id.slidingTitle);

                if(polyline.getWidth()==30){ //currently page is opened
                    openPage=false;
                    slidingLayout.startAnimation(slidingClose);
                    polyline.setWidth(10); //normal
                }
                else{
                    polyline.setWidth(30); //highlight
                    Iterator<Route> iterator = routes.iterator();
                    Route cur;
                    while(iterator.hasNext()){
                        cur=iterator.next();
                        if(cur.contains(polyline,googleMap)!=-1){
                            title.setText("Route info of Day "+cur.contains(polyline,googleMap));
                            RouteInfoSliding indicator = findViewById(R.id.routeInfoDraw);
                            indicator.setRoute(cur);
                            cur.setMarkerList(indicator.getModified()); //get the modified route info

                        }else{
                            cur.setPolylineWidth(10,googleMap);
                        }
                    }
                    openPage=true;
                    slidingLayout.startAnimation(slidingOpen);
                }
            }
        });
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {
                final View innerView = getLayoutInflater().inflate(R.layout.map_pop_input,null);
                AlertDialog.Builder popInput = new AlertDialog.Builder(getApplicationContext());
                final RadioGroup iconOpt = innerView.findViewById(R.id.IconCategory);

                if(marker.getTitle().equals(" ")){
                    popInput.setTitle("Edit Maker");
                }
                else{
                    popInput.setTitle(marker.getTitle());
                }
                popInput.setView(innerView);

                popInput.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        marker.setTitle(innerView.findViewById(R.id.markerTitleInput).toString());

                        switch (iconOpt.getCheckedRadioButtonId()){
                            case R.id.opt_dining:
                                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_dining));
                                marker.setTag(1);
                                break;
                            case R.id.opt_residence:
                                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_residence));
                                marker.setTag(2);
                                break;
                            case R.id.opt_shopping:
                                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_shopping));
                                marker.setTag(3);
                                break;
                            case R.id.opt_default:
                                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_default));
                                marker.setTag(0);
                                break;
                        }

                    }
                });
                popInput.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                popInput.show();

            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if(edit_mode==1){  //remove a marker
            marker.remove();

            Iterator<Route> route_iterator = routes.iterator();
            Route cur;
            marker.remove();

            while (route_iterator.hasNext()){
                cur = route_iterator.next();
                if(cur.remove(marker)){
                    cur.setPoints(googleMap);
                    if(cur.getMarkerList().size()==1){
                        cur.getMarkerList().clear();
                    }
                }
            }
        }
        else if(edit_mode==2) {  //add a marker
            if(routes.get(routeIndex).isLastMarker(marker)){ //avoid duplication
                Toast.makeText(this, "This place was just added.", Toast.LENGTH_SHORT).show();
            }else{
                routes.get(routeIndex).add(marker);
                routes.get(routeIndex).setPoints(googleMap);
//                routes.get(routeIndex).drawPolyline(googleMap);
            }

        }
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        contextMenu = menu;

        menu.add(0,-1,0,"New Day");
        menu.add(0,0,0,"Day1");

        int day;
        for (int i=1;i<=newIndex;i++){
            day= i+1;
            contextMenu.add(0,i,0,"Day"+day);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (routes.get(routeIndex).getMarkerList().size()==1) { //didnt draw any route
            routes.get(routeIndex).getMarkerList().clear();
        }
        if (item.getItemId() == -1) {

            //maximum route number is 20
            if(newIndex<20) {
                newIndex = routes.size();
//                Route route = new Route(newIndex, routeColor[newIndex], googleMap);
                Route route = new Route(newIndex, routeColor[newIndex]);
                routes.add(newIndex, route);

                openContextMenu(btnRoute); //show reorganized context menu
            }else{
                Toast.makeText(this, "It's a max route number", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            routeIndex = item.getItemId();
            Toast.makeText(this, "Color:" + routeColor[routeIndex], Toast.LENGTH_SHORT).show();

            btnRoute.setTextColor(Color.parseColor(routeColor[routeIndex]));
            edit_mode = 2;  //route edit mode on through the context menu
            btnPlace.setTextColor(Color.BLACK);  //deactivate placebtn
        }
        return super.onContextItemSelected(item);
    }

    public void editMarker(View v){
        if(edit_mode!= 1){
            btnPlace.setTextColor(Color.RED);
            btnRoute.setTextColor(Color.BLACK);
            Toast.makeText(this,"remove a marker",Toast.LENGTH_SHORT).show();
            edit_mode = 1;
        }
        else if(edit_mode==1){
            btnPlace.setTextColor(Color.BLACK);
            btnRoute.setTextColor(Color.BLACK);
            Toast.makeText(this,"Done",Toast.LENGTH_SHORT).show();
            edit_mode = 0;
        }
    }

    public void editRoute(View v){

        if(edit_mode!=2){
            Toast.makeText(this,"Make a route",Toast.LENGTH_SHORT).show();
            edit_mode = 2;
            btnRoute.setTextColor(Color.parseColor(routeColor[routeIndex]));
            btnPlace.setTextColor(Color.BLACK);
        }
        else if(edit_mode==2){

            if(routes.get(routeIndex).getMarkerList().size()==1){
                routes.get(routeIndex).getMarkerList().clear();
            }

            btnRoute.setTextColor(Color.BLACK);
            btnPlace.setTextColor(Color.BLACK);
            Toast.makeText(this,"Show the route",Toast.LENGTH_SHORT).show();
            edit_mode = 0;
        }
    }
    public void save(View v){
/*
        helper = new PlanSQLiteHelper(getApplicationContext());

        Plan p = plan;
        p.setRoutesList(routes);
/*        p.setTitle(plan.getTitle());
        p.setCity(plan.getCity());
        p.setCentre(plan.getCentre());
    //    p.setId(plan.getId());
        p.setY(plan.getYear());
        p.setM(plan.getMonth());
        p.setD(plan.getDay());

        long plan_id = helper.createPlan(p);
        db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("KEY_ID",plan_id);
        db.insert("TABLE_PLAN",null,values);
        */
    }
}