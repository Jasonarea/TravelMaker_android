package com.ellalee.travelmaker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by tyu54 on 2018-05-30.
 */

public class PlanSQLiteHelper extends SQLiteOpenHelper /*extends SQLiteOpenHelper*/ {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "planDB";

    // Table Names
    private static final String TABLE_PLAN = "plans";
    private static final String TABLE_ROUTELIST = "route_lists";
    private static final String TABLE_ROUTE = "routes";
    private static final String TABLE_MARKERLIST = "marker_lists";
    private static final String TABLE_MARKER = "markers";

    // Common column names
    private static final String KEY_ID = "id";

    // PLANS Table - column names
    private static final String KEY_TITLE = "title";
    private static final String KEY_CITY = "city";
    private static final String KEY_MONTH = "month";
    private static final String KEY_YEAR = "year";
    private static final String KEY_DAY = "day";
    private static final String KEY_ROUTELIST_ID = "route_list"; //

    // ROUTELIST Table - column names
    private static final String KEY_PLAN_ID = "plan_id";
    private static final String KEY_ROUTE_ID = "route_id";

    // ROUTE Table - column names
    private static final String KEY_INDEX = "routeIndex";
    private static final String KEY_MARKERLIST_ID = "marker_list"; //

    // MARKERLIST Table - column names
    private static final String KEY_MARKER_ID = "marker_id";
//    private static final String KEY_ROUTE_ID = "route_id"; //duplicate field

    // MARKER Table - column names
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE =  "longitude";
//    private static final String KEY_TITLE =  "title"; //duplicate field
    private static final String KEY_ICON =  "icon";


    // Table Create Statements
    // Plan table create statement
    private static final String CREATE_TABLE_PLAN = "CREATE TABLE " + TABLE_PLAN + "("
            + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_TITLE + " TEXT, "
            + KEY_CITY + "TEXT, "
            + KEY_MONTH + " INTEGER, "
            + KEY_YEAR + " INTEGER, "
            + KEY_DAY + " INTEGER, "
            + KEY_ROUTELIST_ID + " INTEGER"+")"; // foreign key

    // RouteList table create statement
    private static final String CREATE_TABLE_ROUTELIST = "CREATE TABLE " + TABLE_ROUTELIST + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_PLAN_ID + " INTEGER, "   //fk
            + KEY_ROUTE_ID + " INTEGER" + ")";  //fk

    // Route table create statement
    private static final String CREATE_TABLE_ROUTE = "CREATE TABLE " + TABLE_ROUTE + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_INDEX + " INTEGER,"
            + KEY_MARKERLIST_ID + " INTEGER" + ")"; //fk

    // MarkerList table create statement
    private static final String CREATE_TABLE_MARKERLIST = "CREATE TABLE " + TABLE_MARKERLIST + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_MARKER_ID + " INTEGER,"   //fk
            + KEY_ROUTE_ID + " INTEGER" + ")";  //fk

    // Route table create statement
    private static final String CREATE_TABLE_MARKER = "CREATE TABLE " + TABLE_MARKER + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_LATITUDE + " DOUBLE,"
            + KEY_LONGITUDE + " DOUBLE,"
            + KEY_TITLE + " TEXT,"
            + KEY_ICON + " INTEGER" + ")"; // enum 4

    public PlanSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PLAN);
        db.execSQL(CREATE_TABLE_ROUTELIST);
        db.execSQL(CREATE_TABLE_ROUTE);
        db.execSQL(CREATE_TABLE_MARKERLIST);
        db.execSQL(CREATE_TABLE_MARKER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTELIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MARKERLIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MARKER);

        onCreate(db);
    }

    public long createPlan(Plan plan){ // make another create without date //default plan name is city name.
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TITLE,plan.getTitle());
        values.put(KEY_YEAR,plan.getYear());
        values.put(KEY_MONTH,plan.getMonth());
        values.put(KEY_DAY,plan.getDay());

        //insert a row to table plan
        long plan_id = db.insert(TABLE_PLAN,null,values);
        plan.setId(plan_id);

        //assigning route
        ArrayList<Route> routes = plan.getRoutesList();

        if(!routes.isEmpty()){
            Iterator<Route> iterator = routes.iterator();
            Route cur;
            int i=0;

            long[] route_id_List = new long[routes.size()];
            while(iterator.hasNext()){
                cur = iterator.next();
                route_id_List[i++]=createRoute(cur);
            }

            for (long route_id : route_id_List){
                createRouteList(plan_id,route_id); //link routes to the plan
            }
        }

        return plan_id;
    }
    public long createRouteList(long plan_id,long route_id){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PLAN_ID,plan_id);
        values.put(KEY_ROUTE_ID,route_id);

        return db.insert(TABLE_ROUTELIST,null,values);
    }

    public long createRoute(Route route){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_INDEX,route.getIndex());

        long route_id = db.insert(TABLE_ROUTE,null,values);

        //assigning markerlist
        ArrayList<Marker> markers = route.getMarkerList();
        Iterator<Marker> iterator = markers.iterator();
        Marker cur;
        int i=0;

        long[] marker_id_List = new long[markers.size()];
        while(iterator.hasNext()){
            cur = iterator.next();
            marker_id_List[i++]=createMarker(cur);
        }

        for (long marker_id : marker_id_List){
            createMarkerList(route_id,marker_id); //link routes to the plan
        }

        return route_id;
    }

    public long createMarkerList(long route_id,long marker_id){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ROUTE_ID,route_id);
        values.put(KEY_MARKER_ID,marker_id);

        return db.insert(TABLE_MARKERLIST,null,values);
    }

    public long createMarker(Marker marker){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        LatLng position = marker.getPosition();
        values.put(KEY_LATITUDE,position.latitude);
        values.put(KEY_LONGITUDE,position.longitude);
        values.put(KEY_TITLE,marker.getTitle());
        values.put(KEY_ICON,Integer.parseInt(marker.getTag().toString())); //0-3

        return db.insert(TABLE_MARKER,null,values);
    }

    public Plan getPlan(long plan_id){
        SQLiteDatabase db = getReadableDatabase();

        String selectQuery = "SELECT * FROM "+TABLE_PLAN+ "WHERE"+ KEY_ID + "=" +plan_id;
        Cursor c = db.rawQuery(selectQuery,null);

        if(c!=null)
            c.moveToFirst();
        Plan plan = new Plan();
        plan.setId(c.getInt(c.getColumnIndex(KEY_ID)));
        plan.setTitle(c.getString(c.getColumnIndex(KEY_TITLE)));
       //plan.setRoutesList(getRouteList(plan_id));
        plan.setY(c.getInt(c.getColumnIndex(KEY_YEAR)));
        plan.setM(c.getInt(c.getColumnIndex(KEY_MONTH)));
        plan.setD(c.getInt(c.getColumnIndex(KEY_DAY)));

        return plan;
    }

    /*public ArrayList<Route> getRouteList(long plan_id){
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Route> list = new ArrayList<>();

        String selectQuery = "SELECT * FROM "+TABLE_ROUTELIST+ "WHERE"+ KEY_ID + "=" +plan_id;
        Cursor c = db.rawQuery(selectQuery,null);


    }*/

    public int updatePlan(Plan plan) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, plan.getTitle());
        values.put(KEY_YEAR, plan.getYear());
        values.put(KEY_MONTH, plan.getMonth());
        values.put(KEY_DAY, plan.getDay());

        // updating row
        return db.update(TABLE_PLAN, values, KEY_ID + " = ?",
                new String[] { String.valueOf(plan.getId()) });
    }
}
