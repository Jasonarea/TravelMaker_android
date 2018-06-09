package com.ellalee.travelmaker;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Cap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.util.ArrayList;
import java.util.Iterator;

public class Route{
    private long id;
    private int index;
    private PolylineOptions polylineOptions;
    private ArrayList<Marker> markerList;
    private ArrayList<MarkerOptions> mOpts;
    private Polyline polyline;
    private String routeColor = new String();

//  Route(int idx,String color, GoogleMap map){
    Route(int idx,String color){
        index = idx;
        markerList = new ArrayList<>();
        routeColor = color;
        polylineOptions = new PolylineOptions();
        setPolylineOptions();
        polyline = null;
    }

    public void setId(long id){
        this.id =id;
    }
    public void setIndex(int idk){
        this.index = idk;
    }
    public void setRouteColor(String c){
        this.routeColor = c;
    }
    public void setPolyline(Polyline p){
        this.polyline =   p;
    }

    public void setMarkerList(GoogleMap map,ArrayList<Marker> markerList) {
        this.markerList = markerList;
        init(map);
        setPoints(map);
    }
    public void setMarkerList(ArrayList<Marker> markerList) {
        this.markerList = markerList;
    }


    public void setMarkerOptions(ArrayList<MarkerOptions> mopt){this.mOpts = mopt;}
    public long getId(){
        return id;
    }
    public int getIndex(){
        return index;
    }
    public String getRouteColor(){
        return routeColor;
    }
    public Polyline getPolyline(){return polyline;}
    public PolylineOptions getPolylineOptions(){setPolylineOptions(); return this.polylineOptions;}
    public ArrayList<Marker> getMarkerList(){
        return markerList;
    }
    public ArrayList<MarkerOptions> getMarkerOptions(){return mOpts; }
    public boolean add(Marker marker){
        return markerList.add(marker);
    }
    public void init(GoogleMap map){
        if(polyline==null){
        //    if(mOpts!=null&&markerList.isEmpty()) setMarkerList(map);
            polyline = map.addPolyline(polylineOptions);
            polyline.setClickable(true);
            polyline.setTag(index);
        }
    }
    /*
    public void setMarkerList(GoogleMap map){ //디비에서 읽어올때 한번만 콜
        setMarkerOptions(mOpts);

        ArrayList<Marker> list = new ArrayList<>();
        Iterator<MarkerOptions> iterator = mOpts.iterator();
        MarkerOptions cur;

        while (iterator.hasNext()){
            cur=iterator.next();
            Marker m = map.addMarker(cur);

            int icon_idx = Integer.parseInt(cur.getSnippet().toString());
            switch (icon_idx){
                case 1:
                    m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_dining));
                    m.setTag(1);
                    break;
                case 2:
                    m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_residence));
                    m.setTag(2);
                    break;
                case 3:
                    m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_shopping));
                    m.setTag(3);
                    break;
                case 0:
                    m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_default));
                    m.setTag(0);
                    break;
            }
            m.setSnippet("");

            list.add(m);
        }
        setMarkerList(list);
    }

    public void drawPolyline(GoogleMap map){
        setPolylineOptions();
        polyline = map.addPolyline(polylineOptions);
        polyline.setClickable(true);
        polyline.setTag(index);
    }
    */

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
    int contains(Polyline line,GoogleMap map){
        init(map);
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

    public void setPolylineWidth(float width,GoogleMap map){
        init(map);
        polyline.setWidth(width);
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
    public void setPoints(GoogleMap map){
        init(map);
        polyline.setPoints(toLatLng(markerList));
    }
}
