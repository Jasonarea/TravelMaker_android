package com.ellalee.travelmaker;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.common.collect.MapMaker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MapMain extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener{

    private boolean route_edit = false;
    private Geocoder geocoder;
    private EditText editAddress;
    private GoogleMap googleMap;
    private Button btnSearch;
    private Button btnAddRoute;
    private PolylineOptions polylineOptions;
    private ArrayList<Marker> markerPoints = new ArrayList<>();
    private ArrayList<LatLng> markerLatLng = new ArrayList<>();

//    private int DEFAULT_ZOOM_LEVEL = 13;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_main);

        geocoder = new Geocoder(this);
        editAddress = findViewById(R.id.editAddress);
        btnSearch = findViewById(R.id.btnSearch);
        btnAddRoute = findViewById(R.id.btnAddRoute);

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(final GoogleMap map) {
        googleMap = map;
        geocoder = new Geocoder(this);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {    // 검색해서 마커추가
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

                        MarkerOptions mOptions2 = new MarkerOptions();
                        mOptions2.title(str);
                        mOptions2.draggable(true);
                        mOptions2.position(SearchPoint);
                        // 마커 추가
                        googleMap.addMarker(mOptions2);
                        // 해당 좌표로 화면 줌
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SearchPoint, 15));

                    }
                }
            }
        });

        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

            }
        });

        LatLng SEOUL = new LatLng(37.56, 126.97);
        //main activity 에서 도시이름 받아서 이동하도록 수정하기

        map.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));
        map.animateCamera(CameraUpdateFactory.zoomTo(13));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        markerPoints.add(marker);
        return false;
    }

    void DrawPolyRoute(){
        Iterator<Marker> iterator = markerPoints.iterator();
        while(iterator.hasNext()){
            markerLatLng.add(iterator.next().getPosition());
        }

        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        polylineOptions.width(10);
        polylineOptions.addAll(markerLatLng);
        googleMap.addPolyline(polylineOptions);
    }

    public void AddRoute(View v){
        if(!route_edit){
            //v.setClickable(true);
            Toast.makeText(this, "Make a route", Toast.LENGTH_SHORT).show();
        }
        else {
            //v.setClickable(false);
            DrawPolyRoute();
            Toast.makeText(this, "Show the route", Toast.LENGTH_SHORT).show();
        }
        route_edit = !route_edit;
        //*************버튼 색 변경하는 것 추가하기 !!
    }

}