package com.ellalee.travelmaker;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class Plan {

    private long id;
    private String title;
    private LatLng centre;
    private String city;
    private ArrayList<Route> routesList;
    private int y,m,d;

    public String firstRouteColor = "#3C989E";

    public Plan(){
        title = " ";
        city = " ";
        centre = new LatLng(37.56, 126.97); //default center is seoul

        routesList = new ArrayList<>();
        Route day1 = new Route(0,firstRouteColor);
        routesList.add(0, day1);

        y=0;d=0;m=0;
    }

    public ArrayList<Route> getRoutesList() {
        return routesList;
    }
    public String getTitle(){
        return title;
    }
    public String getCity(){
        return city;
    }
    public LatLng getCentre(){ return this.centre;}
    public int getYear(){
        return y;
    }
    public int getMonth(){
        return m;
    }
    public int getDay(){
        return d;
    }
    public long getId(){
        return id;
    }

    public void setId(long ID){
        this.id=ID;
    }
    public void setTitle(String t){
        this.title = t;
    }
    public void setRoutesList(ArrayList<Route> r) {
        this.routesList = r;
    }
    public void setY(int y){
        this.y=y;
    }
    public void setM(int m){
        this.m = m;
    }
    public void setD(int d){
        this.d = d;
    }
    public void setCentre(LatLng pos){
        this.centre = pos;
    }
    public void setCity(String city){
        this.city = city;
    }

    public boolean init(){
        if(routesList==null|| routesList.isEmpty()){
            routesList = new ArrayList<>();
            Route day1 = new Route(0,firstRouteColor);
            routesList.add(0, day1);
            return true;
        }return false;
    }
}
