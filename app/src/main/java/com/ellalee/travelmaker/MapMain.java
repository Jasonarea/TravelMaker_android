package com.ellalee.travelmaker;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.TypedArray;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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

    private int edit_mode = 0; // 1:editMarker 2: editRoute
    private int routeIndex = 0; // day 1,2 ....
    private int newIndex;
    private Geocoder geocoder;
    private EditText editAddress;
    private GoogleMap googleMap;
    private Button btnSearch;
    private Button btnRoute;
    private Button btnPlace;

    //private PolylineOptions polylineOptions; //1�
    //private ArrayList<Marker> markersList = new ArrayList<>(); //�러�
    //private ArrayList<ArrayList<Marker>> routesList = new ArrayList<ArrayList<Marker>>();

    private ArrayList<LatLng> markerLatLng; //1�
    private ContextMenu contextMenu;
    private ArrayList<Route> routes = new ArrayList<>(); //�러�

    String[] routeColor;

    private class Route{
        public int index;
        public PolylineOptions polylineOptions = new PolylineOptions();
        public ArrayList<Marker> markerList;
        public Polyline polyline;

        Route(int idx){
            index = idx;
            markerList = new ArrayList<>();
            setPolylineOptions();
            polyline = googleMap.addPolyline(polylineOptions);
            polyline.setClickable(true);
        }
        boolean add(Marker marker){
            return markerList.add(marker);
        }
        boolean remove(Marker marker){
            return markerList.remove(marker);
        }
        boolean contains(Marker marker){
            Iterator<Marker> iterator = markerList.iterator();
            while(iterator.hasNext()){
                if(iterator.equals(marker))
                    return true;
            }
            return false;
        }
        public void setMarkerList(ArrayList<Marker> markerList) {
            this.markerList = markerList;
        }
        public void setPolylineOptions(){
            polylineOptions.color(Color.parseColor(routeColor[index]));

            this.polylineOptions.width(10);
            this.polylineOptions.addAll(toLatLng(this.markerList));
        }
        public void setPoints(ArrayList<LatLng> latLng){
            polyline.setPoints(latLng);
        }
        public void setPoints(){
            polyline.setPoints(toLatLng(markerList));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_main);

        geocoder = new Geocoder(this);
        editAddress = findViewById(R.id.editAddress);
        btnSearch = findViewById(R.id.btnSearch);
        btnRoute = findViewById(R.id.btnRoute);
        btnPlace = findViewById(R.id.btnPlace);

        routeColor = getResources().getStringArray(R.array.routeColor);
        registerForContextMenu(btnRoute);

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        googleMap = map;
        geocoder = new Geocoder(this);

        Route day1= new Route(routeIndex);
        Route day2 = new Route(1);
        Route day3 = new Route(2);

        routes.add(routeIndex,day1);
        routes.add(1,day2);
        routes.add(2,day3);


        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {    // 검�해마커추�
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
                        // 마커 추�
                        googleMap.addMarker(mOptions);
                        // �당 좌표롔면 �
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SearchPoint, 15));

                    }
                }
            }
        });

        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDragStart(Marker marker) {}

            @Override
            public void onMarkerDrag(Marker marker) { }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                //Marker가 �함line� �당 �인 지�기, 마커가 �러 루트�함경우�고려루트 모두 검
                Iterator<Route> route_iterator = routes.iterator(); //route iterator
                Route cur;
                while (route_iterator.hasNext()) {
                    cur = route_iterator.next();
                    cur.setPoints(); //if(route_iterator.contains(marker)
                }
            }
        });

        LatLng SEOUL = new LatLng(37.56, 126.97);
        //main activity �서 �시�름 받아�동�도롘정�기

        map.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));
        map.animateCamera(CameraUpdateFactory.zoomTo(10));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if(edit_mode==1){  //marker 지�기
            marker.remove();

            Iterator<Route> route_iterator = routes.iterator();
            Route cur;
            marker.remove();

            while (route_iterator.hasNext()){
                cur = route_iterator.next();
                if(cur.remove(marker))
                    cur.setPoints();
            }
        }
        else if(edit_mode==2) {  // marker 추�기
            routes.get(routeIndex).add(marker);
            routes.get(routeIndex).setPoints();
        }
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        //menu.add(0,-1,0,"add another day");
//        menu.add(0,routeIndex,0,"Day"+routeIndex);

        menu.add(0,0,0,"Day1");
        menu.add(0,1,1,"Day2");
        menu.add(0,2,2,"Day3");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if(routes.get(routeIndex).markerList.size()<2){ //route가 �그�진 marker�나지�기
            routes.get(routeIndex).markerList.clear();
        }

        routeIndex=item.getItemId();
        Toast.makeText(this, "Color:"+routeColor[routeIndex], Toast.LENGTH_SHORT).show();

        btnRoute.setTextColor(Color.parseColor(routeColor[routeIndex]));
        edit_mode=2; //context메뉴루트륌러�도 루트�정 모드 on
        btnPlace.setTextColor(Color.BLACK); //placebtn deactivate;

        return super.onContextItemSelected(item);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //  -------------> menu 추� 만들�
//        for (int i=0;i<=routeIndex;i++){
//            menu.add(0,i,i,"Day"+i);
//        }
        return super.onPrepareOptionsMenu(menu);
    }

    PolylineOptions setPolylineOptions(int index, ArrayList<LatLng> markerLatLng){
        PolylineOptions polylineOption = new PolylineOptions();
        switch (index){
            case 1:
                polylineOption.color(Color.BLUE);
                break;
            case 2:
                polylineOption.color(Color.RED);
                break;
            case 3:
                polylineOption.color(Color.GREEN);
                break;
        }
        polylineOption.width(10);
        polylineOption.addAll(markerLatLng);
        return polylineOption;
    }

    ArrayList<LatLng> toLatLng(ArrayList<Marker> markers){
        Iterator<Marker> iterator = markers.iterator();
        ArrayList<LatLng> LatLngs = new ArrayList<>();

        while(iterator.hasNext()){
            LatLngs.add(iterator.next().getPosition());
        }
        return LatLngs;
    }
/*
    Polyline DrawPolyRoute(int routeIdx){
        setPolylineOptions(routeIdx,toLatLng(routesList.get(routeIdx)));

        //�처음�로 루트 �정 - BLUE
        polylineOptions.color(Color.BLUE);

        Polyline curPolyline = googleMap.addPolyline(polylineOptions);
        return curPolyline;
    }
*/
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
            //DrawPolyRoute();
            if(routes.get(routeIndex).markerList.size()<2){
                routes.get(routeIndex).markerList.clear();
            }
            btnRoute.setTextColor(Color.BLACK);
            btnPlace.setTextColor(Color.BLACK);
            Toast.makeText(this,"Show the route",Toast.LENGTH_SHORT).show();
            edit_mode = 0;

  /*          if(line.get(routeIndex) == null) //처음 만들
                line.add(routeIndex,DrawPolyRoute(routeIndex));
            else{ //�번�만들
   */
//�깐1            line.get(routeIndex).setPoints(toLatLng(markerPoint.get(routeIndex)));
        }
    }
}