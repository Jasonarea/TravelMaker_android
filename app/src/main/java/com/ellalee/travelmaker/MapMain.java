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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.common.collect.MapMaker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
//import static com.ellalee.travelmaker.MapMain.Rcolor.getRouteColor;

public class MapMain extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener{

    private PlanSQLiteHelper db;
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
    private Button btnSlideClose;

    private ContextMenu contextMenu;

    private ArrayList<Route> routes = new ArrayList<>(); //multi
//   private ArrayList<Route> routes; //multi
//    public String[] routeColor;
//    private BitmapDescriptor[] markerIcon;

    Animation slidingOpen;
    Animation slidingClose;
    RelativeLayout slidingLayout;
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

        db = new PlanSQLiteHelper(getApplicationContext());
        plan = new Plan();
        plan.setId(plan_id);
/*
        Iterator<Route> iterator = routes.iterator();
        Route c;
        while(iterator.hasNext()){
            c = iterator.next();
            c.init(googleMap);
        }  //get routes info from db and initiate all
*/
     //   Toast.makeText(getApplicationContext(),"plan_id:"+plan_id, Toast.LENGTH_SHORT).show();

        geocoder = new Geocoder(this);
        btnSearch = findViewById(R.id.btnSearch);
        editAddress = findViewById(R.id.editAddress);
        editAddress.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(i == KeyEvent.KEYCODE_ENTER){
                    btnSearch.callOnClick();
                }
                return false;
            }
        });
        btnRoute = findViewById(R.id.btnRoute);
        btnPlace = findViewById(R.id.btnPlace);
        btnSlideClose = findViewById(R.id.btnSlideClose);
        btnSlideClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Iterator<Route> iterator = routes.iterator();
                Route cur;
                while(iterator.hasNext()){
                    cur=iterator.next();
                    openPage=false;
                    slidingLayout.startAnimation(slidingClose);
                    cur.setPolylineWidth(10,googleMap); //normal
                }
            }
        });

//        routeColor = getResources().getStringArray(R.array.routeColor);
        registerForContextMenu(btnRoute);
  /*      markerIcon = new BitmapDescriptor[4];
        markerIcon[0] = BitmapDescriptorFactory.fromResource(R.drawable.marker_default);
        markerIcon[1] = BitmapDescriptorFactory.fromResource(R.drawable.marker_restaurant);
        markerIcon[2] = BitmapDescriptorFactory.fromResource(R.drawable.marker_hotel);
        markerIcon[3] = BitmapDescriptorFactory.fromResource(R.drawable.marker_shopping);
*/
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

        //db = new PlanSQLiteHelper(getApplicationContext());
        plan = db.getPlan(plan.getId(),googleMap);
        db.getRouteListCount(plan.getId());

        Log.d("MAPMAIN ROUTES NUM: ",plan.getRoutesList().size()+"********");

        routes = plan.getRoutesList();
        newIndex = routes.size()-1;
//        db.getALLMarkers(plan.getId(),map,markerIcon);
/*
        //initialize when plan is created
        //ini tial route setting (default route)
        if(routes.isEmpty()) {
//          Route day1 = new Route(0, routeColor[0], googleMap);
            Route day1 = new Route(0, routeColor[0]);
            routes.add(0, day1);
        }
*/
        //main activity에서 valid한 로케이션 확인 후 넘겨주기
//        LatLng center = new LatLng(37.56, 126.97);

        LatLng center = plan.getCentre();
        if(!plan.getAllMarkers().isEmpty()){
            center = plan.getAllMarkers().get(0).getPosition(); //first marker as center
        }
        map.moveCamera(CameraUpdateFactory.newLatLng(center));
        map.animateCamera(CameraUpdateFactory.zoomTo(14));

        /*
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
                        mOptions.icon(Micon.getMarkerIcon(0));

                        //add marker
                        Marker m =googleMap.addMarker(mOptions);
                        MarkerTag tag = new MarkerTag(0,0);
                        m.setTag(tag);
                        //m.setSnippet(m.getId());
                        db.createMarker(m,plan.getId());

                        //zoom camera view
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SearchPoint, 15));
                    }
                }
            }
        });
        */

        //adding a marker by longclick
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                MarkerOptions option =new MarkerOptions();
                option.draggable(true);
                option.position(latLng);
                option.title(getString(R.string.marker_default_title));
                option.icon(Micon.getMarkerIcon(0));

                //add marker
                Marker m = googleMap.addMarker(option);
                MarkerTag tag = new MarkerTag(0,0);
                m.setTag(tag);
//                m.setSnippet(m.getId());
                db.createMarker(m,plan.getId());
                plan.addMarker(m);

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
                db.updateMarker(marker);

                Iterator<Route> route_iterator = routes.iterator(); //route iterator
                Route cur;
                while (route_iterator.hasNext()) {
                    cur = route_iterator.next();
                    cur.setPoints(googleMap); //polyline.setPoints
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
                        int n = cur.contains(polyline,googleMap);
                        if(cur.contains(polyline,googleMap)!=-1){
                            if(!plan.doesDateSet()){
                                title.setText("Route info of Day "+(n+1));
                            }else{
                                title.setText(plan.getDateString(n));
                            }
                            RouteInfoSliding indicator = findViewById(R.id.routeInfoDraw);
                            indicator.setRoute(cur,googleMap);
                            cur.setMarkerList(googleMap,indicator.getModified()); //get the modified route info
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
                //final Marker cMarker =marker;
                final PlanSQLiteHelper db = new PlanSQLiteHelper(MapMain.this);

                AlertDialog.Builder alert = new AlertDialog.Builder(MapMain.this);
                LayoutInflater inflater = getLayoutInflater();
                final View inputView = inflater.inflate(R.layout.map_pop_input,null);

                if(marker.getTitle().equals(getString(R.string.marker_default_title)) || marker.getTitle().trim().length()<1){
                    alert.setTitle("마커 편집");
                }
                else{
                    alert.setTitle(marker.getTitle());
                }
                alert.setView(inputView);
                alert.setPositiveButton("저장", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText titleInput = (inputView).findViewById(R.id.markerTitleInput);
                        RadioGroup iconInput = (inputView).findViewById(R.id.iconCategory);
                        String mTitle = titleInput.getText().toString();
                        int checkedIcon;
                        switch (iconInput.getCheckedRadioButtonId()){
                            case R.id.opt_default:
                                checkedIcon=0;
                                break;
                            case R.id.opt_residence:
                                checkedIcon=1;
                                break;
                            case R.id.opt_dining:
                                checkedIcon=2;
                                break;
                            case R.id.opt_shopping:
                                checkedIcon=3;
                                break;
                            case R.id.opt_transportation:
                                checkedIcon=4;
                                break;
                            default:
                                    checkedIcon=0;
                        }
                        Log.d("ICON ID",checkedIcon+" ");

                        marker.setIcon(Micon.getMarkerIcon(checkedIcon));
                        if(mTitle.trim().length()>0){
                            marker.setTitle(mTitle);
                        }
                        db.updateMarker(marker,checkedIcon);
                    }
                });
                alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                });
                alert.show();
                /*
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
                                marker.setIcon(markerIcon[1]);
                                marker.setTag(1);
                                break;
                            case R.id.opt_residence:
                                marker.setIcon(markerIcon[2]);
                                marker.setTag(2);
                                break;
                            case R.id.opt_shopping:
                                marker.setIcon(markerIcon[3]);
                                marker.setTag(3);
                                break;
                            case R.id.opt_default:
                                marker.setIcon(markerIcon[4]);
                                marker.setTag(0);
                                break;
                        }
                    }
                });
                popInput.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                }  );
                popInput.show();*/
            }
        });
    }

    public void searchMarker(View v) {  // add a marker
        String str = editAddress.getText().toString();
        List<Address> list = null;
        double latitude, longitude;

        try {
            list = geocoder.getFromLocationName(str, 10);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MapMain.this, "장소 이름을 입력해주세요!", Toast.LENGTH_SHORT).show();
        }

        if (list != null) {
            if (list.size() == 0) {
                Toast.makeText(MapMain.this, "해당 정보가 없네요ㅠ 다른 이름으로 검색해주세요.", Toast.LENGTH_SHORT).show();
            }
            else {
                latitude = list.get(0).getLatitude();
                longitude = list.get(0).getLongitude();

                LatLng SearchPoint = new LatLng(latitude, longitude);

                MarkerOptions mOptions = new MarkerOptions();
                mOptions.title(str);
                mOptions.draggable(true);
                mOptions.position(SearchPoint);
                mOptions.icon(Micon.getMarkerIcon(0));

                if(!isThere(SearchPoint)) {
                    //add marker
                    Marker m = googleMap.addMarker(mOptions);
                    MarkerTag tag = new MarkerTag(0, 0);
                    m.setTag(tag);
                    db.createMarker(m, plan.getId());
                    plan.addMarker(m);
                }
                //zoom camera view
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SearchPoint, 15));
            }
        }
    }

    public boolean isThere(LatLng pos){
        Iterator<Marker> iterator =plan.getAllMarkers().iterator();
        Marker cur;
        while(iterator.hasNext()){
            cur = iterator.next();
            if(cur.getPosition().equals(pos))
                return true;
        }
        return false;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if(edit_mode==1){  //remove a marker
            Iterator<Route> route_iterator = routes.iterator();
            Route cur;

            db.deleteMarker(marker);
            plan.deleteMarker(marker);
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
                Toast.makeText(this, "여긴 이미 추가되었으니 다음 장소를 정해주세요!", Toast.LENGTH_SHORT).show();
            }else{
                routes.get(routeIndex).add(marker);
                routes.get(routeIndex).setPoints(googleMap);

                db.createMarkerList(plan.getId(),routes.get(routeIndex).getId(),marker);
            }

        }
        /*
        else{
            Toast.makeText(this, "ID: "+marker.getId(), Toast.LENGTH_SHORT).show();
        }*/
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        contextMenu = menu;

        menu.add(0,-1,0,"New Day");
        if(plan.doesDateSet()){
            menu.add(0,0,0,plan.getDateString());

            int day;
            for (int i=1;i<=newIndex;i++){
                day= i+1;
                contextMenu.add(0,i,0,plan.getDateString(i));
            }
        }else {
            menu.add(0,0,0,"Day1");

            int day;
            for (int i=1;i<=newIndex;i++){
                day= i+1;
                contextMenu.add(0,i,0,"Day"+day);
            }
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
                Route route = new Route(newIndex, Rcolor.getRouteColor(newIndex));
                routes.add(newIndex, route);
                db.createRoute(plan.getId(),route);

                openContextMenu(btnRoute); //show reorganized context menu
            }else{
                Toast.makeText(this, "최대 루트 갯수는 20개에요.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            routeIndex = item.getItemId();
            //Toast.makeText(this, "Color:" + routeColor[routeIndex], Toast.LENGTH_SHORT).show();

            btnRoute.setTextColor(Color.parseColor(Rcolor.getRouteColor(newIndex)));
            int day = routeIndex+1;
            btnRoute.setText("DAY"+day);
            edit_mode = 2;  //route edit mode on through the context menu
            btnPlace.setBackground(getDrawable(R.drawable.close_bin_big));
        }
        return super.onContextItemSelected(item);
    }

    public void editMarker(View v){
        if(edit_mode!= 1){ //marker delete mode
            btnPlace.setBackground(getDrawable(R.drawable.open_bin_big));
            Toast.makeText(this,"마커를 눌러 제거하세요.",Toast.LENGTH_SHORT).show();
            changeAllMakerIcon(plan,BitmapDescriptorFactory.fromResource(R.drawable.delete));
            edit_mode = 1;
        }
        else if(edit_mode==1){ //view mode
            btnPlace.setBackground(getDrawable(R.drawable.close_bin_big));
            btnRoute.setTextColor(Color.BLACK);
            btnRoute.setText("DAY");
  //          Toast.makeText(this,"marker delete mode off",Toast.LENGTH_SHORT).show();
            changeAllMakerIcon(plan);
            edit_mode = 0;
        }
    }

    public void editRoute(View v){

        if(edit_mode!=2){
            Toast.makeText(this,"마커를 순서대로 클릭해서 루트를 그려보세요!",Toast.LENGTH_SHORT).show();
            edit_mode = 2;
            btnRoute.setTextColor(Color.parseColor(Rcolor.getRouteColor(newIndex)));
            int day = routeIndex+1;
            btnRoute.setText("DAY"+day);
            btnPlace.setBackground(getDrawable(R.drawable.close_bin_big));
            changeAllMakerIcon(plan);
        }
        else if(edit_mode==2){

            if(routes.get(routeIndex).getMarkerList().size()==1){
                routes.get(routeIndex).getMarkerList().clear();
            }

            btnRoute.setTextColor(Color.BLACK);
            btnRoute.setText("DAY");
            btnPlace.setBackground(getDrawable(R.drawable.close_bin_big));
            changeAllMakerIcon(plan);
//            Toast.makeText(this,"Show the route",Toast.LENGTH_SHORT).show();
            edit_mode = 0;
        }
    }
    public void changeAllMakerIcon(Plan p,BitmapDescriptor icon){
        ArrayList<Marker> mList = p.getAllMarkers();
        Iterator<Marker> iterator = mList.iterator();
        Marker cur;
        while(iterator.hasNext()){
            cur = iterator.next();
            cur.setIcon(icon);
        }
    }
    public void changeAllMakerIcon(Plan p){
        ArrayList<Marker> mList = p.getAllMarkers();
        Iterator<Marker> iterator = mList.iterator();
        Marker cur;
        int icon;
        while(iterator.hasNext()){
            cur = iterator.next();
            icon = cur.getTag().hashCode();
            cur.setIcon(Micon.getMarkerIcon(icon));
        }
    }
/*
    public void save(View v){
        plan.setRoutesList(routes);
        Toast.makeText(this, "complete save a plan!", Toast.LENGTH_SHORT).show();
     /*       File dbFile = new File(db.getPath());
            File recv = new File("/mnt/sdcard/mmyplan.db");

            try {
                FileInputStream inp = new FileInputStream(dbFile);
                FileOutputStream out = new FileOutputStream(recv);

                int bytesRead = 0;
                byte[] buffer = new byte[1024];
                while ((bytesRead = inp.read(buffer, 0, 1024)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.close();
                inp.close();

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

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

    }*/
    public static class Rcolor {
        static final String[] routeColor = new String[]{
                "#3C989E" ,
                "#ED5276" ,
                "#fcc244" ,
                "#978ddf" ,
                "#2e98d1" ,
                "#F4CDA5" ,
                "#fdf5654b" ,
                "#f9c571d6" ,
                "#0d97ea" ,
                "#7caf7f" ,
                "#57039c" ,
                "#e2a628" ,
                "#1d2e39" ,
                "#c2435c" ,
                "#f5557316" ,
                "#dd0ee4f9" ,
                "#9b564a" ,
                "#f7dad96c" ,
                "#fa576cc1" ,
                "#ff05d5" ,
};

        public static String getRouteColor(int idx){
            return routeColor[idx];
        }
    }

    public static class Micon {
        static final BitmapDescriptor[] mIcon = new BitmapDescriptor[]{
                BitmapDescriptorFactory.fromResource(R.drawable.marker_default),
                BitmapDescriptorFactory.fromResource(R.drawable.marker_hotel),
                BitmapDescriptorFactory.fromResource(R.drawable.marker_restaurant),
                BitmapDescriptorFactory.fromResource(R.drawable.marker_shopping),
                BitmapDescriptorFactory.fromResource(R.drawable.marker_transport)
        };

        public static BitmapDescriptor getMarkerIcon(int idx){
            return mIcon[idx];
        }
    }
}