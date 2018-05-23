package com.ellalee.travelmaker;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Cap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.util.ArrayList;
import java.util.Iterator;

public class Route{
    private int index;
    private PolylineOptions polylineOptions = new PolylineOptions();
    private ArrayList<Marker> markerList;
    private Polyline polyline;
    private String routeColor = new String();


    Route(int idx,String color, GoogleMap map){
        index = idx;
        markerList = new ArrayList<>();
        routeColor = color;
        setPolylineOptions();
        polyline = map.addPolyline(polylineOptions);
        polyline.setClickable(true);
        polyline.setTag(index);
    }
    boolean add(Marker marker){
        return markerList.add(marker);
    }

    boolean remove(Marker marker){
        return markerList.remove(marker);
    }

    int getIndex(){
        return index;
    }

    ArrayList<Marker> getMarkerList(){
        return markerList;
    }

    String getRouteColor(){
        return routeColor;
    }

    boolean contains(Marker marker){
        Iterator<Marker> iterator = markerList.iterator();
        while(iterator.hasNext()){
            if(iterator.equals(marker))
                return true;
        }
        return false;
    }
    int contains(Polyline line){
        if(polyline.equals(line)){
            return index;
        }
        else return -1;
    }
    boolean isLastMarker(Marker m){
        if(markerList.size()==0)
            return false;

        Iterator<Marker> iterator = markerList.iterator();
        Marker cur=null;
        while(iterator.hasNext()){
            cur=iterator.next();
        }
        return cur.equals(m);
    }

    public void setPolylineWidth(float width){
        polyline.setWidth(width);
    }

    public void setMarkerList(ArrayList<Marker> markerList) {
        this.markerList = markerList;
    }

    ArrayList<LatLng> toLatLng(ArrayList<Marker> markers){
        Iterator<Marker> iterator = markers.iterator();
        ArrayList<LatLng> LatLngs = new ArrayList<>();

        while(iterator.hasNext()){
            LatLngs.add(iterator.next().getPosition());
        }
        return LatLngs;
    }
    public void setPolylineOptions(){
        polylineOptions.color(Color.parseColor(routeColor));
        this.polylineOptions.width(10);
        this.polylineOptions.startCap(new RoundCap());
        this.polylineOptions.endCap(new RoundCap());
        this.polylineOptions.addAll(toLatLng(this.markerList));
    }
    public void setPoints(ArrayList<LatLng> latLng){
        polyline.setPoints(latLng);
    }
    public void setPoints(){
        polyline.setPoints(toLatLng(markerList));
    }
}
