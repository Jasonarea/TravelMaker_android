package com.ellalee.travelmaker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class PlanSQLiteHelper extends SQLiteOpenHelper /*extends SQLiteOpenHelper*/ {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "planDB";

//    private static final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + DATABASE_NAME;

    // Table Names
    private static final String TABLE_PLAN = "plans";
    //private static final String TABLE_ROUTELIST = "route_lists";
    private static final String TABLE_ROUTE = "routes";
    private static final String TABLE_MARKERLIST = "marker_lists";
    private static final String TABLE_MARKER = "markers";

    // Common column names
    private static final String KEY_ID = "id";

    // PLANS Table - column names
    private static final String KEY_TITLE = "title";
    private static final String KEY_CITY = "city";
//    private static final String KEY_LATITUDE = "latitude"; duplicate
//    private static final String KEY_LONGITUDE =  "longitude";
    private static final String KEY_MONTH = "month";
    private static final String KEY_YEAR = "year";
    private static final String KEY_DAY = "day";
//    private static final String KEY_ROUTELIST_ID = "route_list_id"; //

    private static final String KEY_PLAN_ID ="plan_id";
    private static final String KEY_ROUTE_ID="route_id";

    // ROUTE Table - column names
    private static final String KEY_INDEX = "routeIndex";
//    private static final String K
//    private static final String KEY_PLAN_ID = "plan_id";
//    private static final String KEY_MARKERLIST_ID = "marker_list_id"; //

    // MARKERLIST Table - column names
    private static final String KEY_MARKER_ID = "marker_id";
//    private static final String KEY_ROUTE_ID = "route_id"; //

    // MARKER Table - column names
//    private static final String KEY_PLAN_ID = "plan_id";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE =  "longitude";
    private static final String KEY_GET_ID = "getId";
//  private static final String KEY_TITLE =  "title"; //duplicate field
    private static final String KEY_ICON =  "icon";

    // Table Create Statements
    // Plan table create statement
    private static final String CREATE_TABLE_PLAN = "CREATE TABLE " + TABLE_PLAN + "( "
            + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_TITLE + " TEXT, "
            + KEY_CITY + " TEXT, "
            + KEY_LATITUDE + " REAL, "
            + KEY_LONGITUDE + " REAL, "
            + KEY_MONTH + " INTEGER, "
            + KEY_YEAR + " INTEGER, "
            + KEY_DAY + " INTEGER )"; // foreign key

    // Route table create statement
    private static final String CREATE_TABLE_ROUTE = "CREATE TABLE " + TABLE_ROUTE + "("
            + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_PLAN_ID + " INTEGER, "
            + KEY_YEAR + " INTEGER, "
            + KEY_MONTH + " INTEGER, "
            + KEY_DAY + " INTEGER, "
            + KEY_INDEX + " INTEGER )"; //fk

    // MarkerList table create statement
    private static final String CREATE_TABLE_MARKERLIST = "CREATE TABLE " + TABLE_MARKERLIST + "("
            + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_MARKER_ID + " INTEGER, "   //fk
            + KEY_ROUTE_ID + " INTEGER" + ")";  //fk

    // Route table create statement
    private static final String CREATE_TABLE_MARKER = "CREATE TABLE " + TABLE_MARKER + "("
            + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_PLAN_ID+ " INTEGER, "
            + KEY_GET_ID + " TEXT, "
            + KEY_LATITUDE + " REAL, "
            + KEY_LONGITUDE + " REAL, "
            + KEY_TITLE + " TEXT, "
            + KEY_ICON + " INTEGER" + ")"; // enum 4

    public PlanSQLiteHelper(Context context) {
        super(context,DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PLAN);
        db.execSQL(CREATE_TABLE_ROUTE);
        db.execSQL(CREATE_TABLE_MARKERLIST);
        db.execSQL(CREATE_TABLE_MARKER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MARKERLIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MARKER);

        onCreate(db);
    }

    public String getDateString(int y,int m,int d,int n){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = new GregorianCalendar(y,m,d);
        cal.add(Calendar.DAY_OF_MONTH,n);

        return sdf.format(cal.getTime());
    }
    public ArrayList<String> getAllPlanSchedule() {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> resultList = new ArrayList<>();
        Cursor c = db.query(TABLE_PLAN, null, null, null, null, null, null);

        if (c!=null && c.getCount()!=0) {
            c.moveToFirst();
            do {
                int year = c.getInt(c.getColumnIndex(KEY_YEAR));
                int days;
                long plan_id;
                String output;

                if(year!=0 && year!=-1){ //date has been set
                    plan_id = c.getLong(c.getColumnIndex(KEY_ID));
                    days = getRouteListCount(plan_id);
                    for(int i=0;i<days;i++){
                        output ="date:"+getDateString(c.getInt(c.getColumnIndex(KEY_YEAR)),
                                c.getInt(c.getColumnIndex(KEY_MONTH)),
                                c.getInt(c.getColumnIndex(KEY_DAY)),i)+
                                ",sche:"+c.getString(c.getColumnIndex(KEY_TITLE)) + ",memo: ";
                        resultList.add(output);
                    }
                }
            } while (c.moveToNext());
        }
        return resultList;
    }
    public ArrayList<String> getPlanDocumentation(long plan_id){ //for sharing & documentation
        SQLiteDatabase db = this.getWritableDatabase();

        ArrayList<String> resultList= new ArrayList<>();
        String result,tmp;

        String selectQuery = "SELECT * FROM "+ TABLE_ROUTE  + " WHERE "+KEY_PLAN_ID+ " = '"+plan_id+"'";
        Cursor c = db.rawQuery(selectQuery,null);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(0,0,0);

        if(c!=null && c.getCount()!=0) {
            c.moveToFirst();
            do{
                date = new Date(c.getInt(c.getColumnIndex(KEY_YEAR)),
                        c.getInt(c.getColumnIndex(KEY_MONTH)),
                        c.getInt(c.getColumnIndex(KEY_DAY)));

                tmp = getRouteDocumentation(c.getInt(c.getColumnIndex(KEY_ID)));

                result = sdf.format(date) + tmp ;
                resultList.add(result);

            }while(c.moveToNext());
        }
        return resultList;
    }

    public String getRouteDocumentation(long route_id){
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM "+ TABLE_MARKERLIST  + " WHERE "+KEY_ROUTE_ID+ " = '"+route_id+"'";
        Cursor c = db.rawQuery(selectQuery,null);

        String result="",tmp ;
        if(c!=null && c.getCount()!=0) {
            c.moveToFirst();
            do{
                tmp = getMarkerDocumentation(c.getInt(c.getColumnIndex(KEY_MARKER_ID)));
                if(tmp.trim().length()>0){
                    result = result + " -> " + tmp; //get markers which has a title.
                }
            }while(c.moveToNext());
        }
        return result;
    }
    public String getMarkerDocumentation(long marker_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_MARKER + " WHERE " + KEY_ID + " = '" + marker_id + "'";
        Cursor c = db.rawQuery(selectQuery, null);
        String title="";

        if (c != null && c.getCount() != 0) {
            c.moveToFirst();
            title = c.getString(c.getColumnIndex(KEY_TITLE));
        }
        if(title.equals("Click to edit!") || title.isEmpty()){
            return " ";
        }else return title;
    }

    public long createPlan(Plan plan){ // make another create without date //default plan name is city name.
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TITLE,plan.getTitle());
        values.put(KEY_CITY,plan.getCity());
        values.put(KEY_LATITUDE,plan.getCentre().latitude);
        values.put(KEY_LONGITUDE,plan.getCentre().longitude);
        values.put(KEY_YEAR,plan.getYear());
        values.put(KEY_MONTH,plan.getMonth());
        values.put(KEY_DAY,plan.getDay());
        //put(.getMarker) info

        //insert a row to table plan
        long plan_id = db.insert(TABLE_PLAN,null,values);
        plan.setId(plan_id);
        Log.d("create plan init : ",plan.init()+"");


        //assigning route
        ArrayList<Route> routes = plan.getRoutesList();

        if(!routes.isEmpty()){
            Iterator<Route> iterator = routes.iterator();
            Route cur;

            while(iterator.hasNext()){
                cur = iterator.next();
                createRoute(plan_id,cur);
            }
            Log.d("INITIAL ROUTE NUM : ",routes.size()+"*******");
/*
            for (long route_id : route_id_List){
                createRouteList(plan_id,route_id); //link routes to the plan
            }
*/
        }

        return plan_id; //call by ref
    }

    public Date getDateAfterNdays(Date date,int n){
        Calendar cal = new GregorianCalendar(date.getYear(),date.getMonth()+1,date.getDate());
        cal.add(Calendar.DAY_OF_MONTH,n);
        return cal.getTime();
    }
    public long createRoute(long plan_id,Route route){
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM "+ TABLE_PLAN + " WHERE "+KEY_ID+ " = '"+plan_id+"'";
        Date date = new Date(0,0,0);
        Cursor c = db.rawQuery(selectQuery,null);

        if(c!=null && c.getCount()!=0) {
            c.moveToFirst();
            date = new Date(c.getInt(c.getColumnIndex(KEY_YEAR)),
                    c.getInt(c.getColumnIndex(KEY_MONTH)),
                    c.getInt(c.getColumnIndex(KEY_DAY)));
            date = getDateAfterNdays(date,route.getIndex());
        }

        ContentValues values = new ContentValues();
        values.put(KEY_INDEX,route.getIndex());
        values.put(KEY_PLAN_ID,plan_id);
        values.put(KEY_YEAR,date.getYear());
        values.put(KEY_MONTH,date.getMonth()+1);
        values.put(KEY_DAY,date.getDate());

        long route_id = db.insert(TABLE_ROUTE,null,values);
        route.setId(route_id);

        return route_id;
    }

/*
    public long createRoute(long plan_id,Route route){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_INDEX,route.getIndex());
        values.put(KEY_PLAN_ID,plan_id);

        long route_id = db.insert(TABLE_ROUTE,null,values);
        route.setId(route_id);

        return route_id;
    }
*/    public long createMarkerList(long plan_id,long route_id,Marker marker){
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM "+ TABLE_MARKER + " WHERE "+KEY_PLAN_ID+ " = '"+plan_id+"'"
                +" AND "+KEY_LATITUDE + " = '" +marker.getPosition().latitude+"'"
                +" AND "+KEY_LONGITUDE+ " = '" +marker.getPosition().longitude+"'";
        Cursor c = db.rawQuery(selectQuery,null);

        if(c!=null && c.getCount()!=0) {
            c.moveToFirst();

            ContentValues values = new ContentValues();
            values.put(KEY_ROUTE_ID, route_id);
            values.put(KEY_MARKER_ID, c.getLong(c.getColumnIndex(KEY_ID)));

            return db.insert(TABLE_MARKERLIST, null, values);
        }
        return -1;
    }
/*
    public long createMarkerList(long route_id,long marker_id){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ROUTE_ID,route_id);
        values.put(KEY_MARKER_ID,marker_id);

        return db.insert(TABLE_MARKERLIST,null,values);
    }

    public long createMarkerList(long route_id,Marker marker){
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT * FROM "+TABLE_MARKER+ " WHERE "+ KEY_GET_ID + " = '" +marker.getId()+"' ";
        Cursor c = db.rawQuery(selectQuery,null);

        if(c!=null && c.getCount()!=0){

            c.moveToFirst();
            ContentValues values = new ContentValues();
            values.put(KEY_ROUTE_ID,route_id);
            values.put(KEY_MARKER_ID,c.getLong(c.getColumnIndex(KEY_ID)));

            return db.insert(TABLE_MARKERLIST,null,values);
        }
        return 0;
    }
*/
    public long createMarker(Marker marker,long plan_id){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_GET_ID,marker.getId());
        values.put(KEY_PLAN_ID,plan_id);
        LatLng position = marker.getPosition();
        values.put(KEY_LATITUDE,position.latitude);
        values.put(KEY_LONGITUDE,position.longitude);
        values.put(KEY_TITLE,marker.getTitle());
        values.put(KEY_ICON,marker.getTag().hashCode()); //0-3

        long marker_id = db.insert(TABLE_MARKER,null,values);
        marker.setTag(new MarkerTag(marker.getTag().hashCode(),marker_id));
        //   marker.setSnippet(marker.getTag().toString());

        return marker_id;
    }

    /////////read DB
    public Plan getPlan(long plan_id){ //light version
        SQLiteDatabase db = getReadableDatabase();

        String selectQuery = "SELECT * FROM "+TABLE_PLAN+ " WHERE "+ KEY_ID + " = '" +plan_id+"' ";
        Cursor c = db.rawQuery(selectQuery,null);

        if(c!=null&&c.getCount()!=0){
            c.moveToFirst();
            Plan plan = new Plan();
            plan.setId(c.getInt(c.getColumnIndex(KEY_ID)));
            plan.setTitle(c.getString(c.getColumnIndex(KEY_TITLE)));
            plan.setCity(c.getString(c.getColumnIndex(KEY_CITY)));

            double lat = c.getDouble(c.getColumnIndex(KEY_LATITUDE));
            double log = c.getDouble(c.getColumnIndex(KEY_LONGITUDE));
            plan.setCentre(new LatLng(lat,log));

            plan.setY(c.getInt(c.getColumnIndex(KEY_YEAR)));
            plan.setM(c.getInt(c.getColumnIndex(KEY_MONTH)));
            plan.setD(c.getInt(c.getColumnIndex(KEY_DAY)));
            plan.init();

            return plan;
        }
        return null;
    }

    public Plan getPlan(long plan_id,GoogleMap map){ //full version
        SQLiteDatabase db = getReadableDatabase();

        String selectQuery = "SELECT * FROM "+TABLE_PLAN+ " WHERE "+ KEY_ID + " = '" +plan_id+"' ";
        Cursor c = db.rawQuery(selectQuery,null);

        if(c!=null && c.getCount()!=0){
            c.moveToFirst();
        Plan plan = new Plan();
        plan.setId(c.getInt(c.getColumnIndex(KEY_ID)));
        plan.setTitle(c.getString(c.getColumnIndex(KEY_TITLE)));
        plan.setCity(c.getString(c.getColumnIndex(KEY_CITY)));

        double lat = c.getDouble(c.getColumnIndex(KEY_LATITUDE));
        double log = c.getDouble(c.getColumnIndex(KEY_LONGITUDE));
        plan.setCentre(new LatLng(lat,log));

        plan.setAllMarkers(getALLMarkers(plan.getId(),map));

        plan.setRoutesList(getRouteList(plan.getAllMarkers(),plan_id,map));
        plan.setY(c.getInt(c.getColumnIndex(KEY_YEAR)));
        plan.setM(c.getInt(c.getColumnIndex(KEY_MONTH)));
        plan.setD(c.getInt(c.getColumnIndex(KEY_DAY)));
        Log.d("get plan init : ",plan.init()+"");

        return plan;
        }
        return null;
    }
    public ArrayList<Route> getRouteList(ArrayList<Marker> allMarkers,long plan_id,GoogleMap map){
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Route> list = new ArrayList<>();

        String selectQuery = "SELECT * FROM "+TABLE_ROUTE+ " WHERE "+ KEY_PLAN_ID + " = '" +plan_id+"'";
        Cursor c = db.rawQuery(selectQuery,null);

        // looping through all rows and adding to list
        if (c!=null && c.getCount()!=0) {
            c.moveToFirst();
            do {
                Route r =  getRoute(allMarkers,c.getLong(c.getColumnIndex(KEY_ID)),map);
                list.add(r);
            } while (c.moveToNext());
        }
        return list;
    }

    public int getRouteListCount(long plan_id){
        SQLiteDatabase db = getReadableDatabase();

        String selectQuery = "SELECT * FROM "+TABLE_ROUTE+ " WHERE "+ KEY_PLAN_ID + " = '" +plan_id+"'";
        Cursor c = db.rawQuery(selectQuery,null);

        return c.getCount();
    }

    public Route getRoute(ArrayList<Marker> allMarkers,long route_id,GoogleMap map){
        SQLiteDatabase db = getReadableDatabase();

        String selectQuery = "SELECT * FROM "+TABLE_ROUTE+ " WHERE "+ KEY_ID + " = '" +route_id+"'";
        Cursor c = db.rawQuery(selectQuery,null);

        if(c!=null && c.getCount()!=0) {
            c.moveToFirst();
            Route route = new Route(-1, "#3C989E");
            route.setId(c.getInt(c.getColumnIndex(KEY_ID)));
            route.setRouteColor(MapMain.Rcolor.getRouteColor(c.getInt(c.getColumnIndex(KEY_INDEX))));
            route.setIndex(c.getInt(c.getColumnIndex(KEY_INDEX)));
            route.setMarkerList(getMarkerList(allMarkers,route_id));
            Log.d(route_id+"루트마커는?! : ",route.getMarkerList().size()+" ");

            Polyline p = map.addPolyline(route.getPolylineOptions());
            p.setClickable(true);
            p.setTag(route.getIndex());
            route.setPolyline(p);

            return route;
        }
        return null;
    }
    /*
    public ArrayList<MarkerOptions> getMarkerListOpt(long route_id){
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<MarkerOptions> list = new ArrayList<>();

        String selectQuery = "SELECT * FROM "+ TABLE_MARKERLIST + " ml, " + TABLE_MARKER + " tm "
                + "WHERE ml."+ KEY_ROUTE_ID + " = '" +route_id+ "' "
                + "AND ml."+KEY_MARKER_ID + " = tm." +KEY_ID;
        Cursor c = db.rawQuery(selectQuery,null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                MarkerOptions opt = getMarkerOption(c.getString(c.getColumnIndex(KEY_GET_ID)));
                list.add(opt);
            } while (c.moveToNext());
        }
        return list;
    }*/
/*
    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    public ArrayList<Marker> getMarkerList(long route_id,GoogleMap map,BitmapDescriptor[] icon){
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Marker> mlist = new ArrayList<>();

        String selectQuery = "SELECT * FROM "+TABLE_MARKERLIST+ " WHERE "+ KEY_ROUTE_ID + " = '" +route_id+"'";
        Cursor c = db.rawQuery(selectQuery,null);

        // looping through all rows and adding to list
        if (c!=null && c.getCount()!=0) {
            c.moveToFirst();
            do {
                mlist.add(getMarker(c.getLong(c.getColumnIndex(KEY_MARKER_ID))));
            } while (c.moveToNext());
        }
        return mlist;
    }
*/
    public ArrayList<Marker> getMarkerList(ArrayList<Marker> allMarkers,long route_id){
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Marker> mlist = new ArrayList<>();

        String selectQuery = "SELECT * FROM "+TABLE_MARKERLIST+ " WHERE "+ KEY_ROUTE_ID + " = '" +route_id+"'";
        Cursor c = db.rawQuery(selectQuery,null);

        // looping through all rows and adding to list
        if (c!=null && c.getCount()!=0) {
            c.moveToFirst();
            do {
                mlist.add(getMarker(allMarkers,c.getLong(c.getColumnIndex(KEY_MARKER_ID))));
            } while (c.moveToNext());
        }
        return mlist;
    }

    //XXXXXXXXXXXXXXXXXXXXxxx
    /*public Marker getMarker(long marker_id,GoogleMap map,BitmapDescriptor[] icon){
        SQLiteDatabase db = getReadableDatabase();

        String selectQuery = "SELECT * FROM "+ TABLE_MARKER + " WHERE "+KEY_ID+ " = '"+marker_id+"'";
        Cursor c = db.rawQuery(selectQuery,null);

        if(c!=null && c.getCount()!=0) {
            c.moveToFirst();

            MarkerOptions opt = new MarkerOptions();
            opt.draggable(true);
            opt.position(new LatLng(c.getDouble(c.getColumnIndex(KEY_LATITUDE)),c.getDouble(c.getColumnIndex(KEY_LONGITUDE))));
            opt.title(c.getString(c.getColumnIndex(KEY_TITLE)));
            opt.icon(icon[c.getInt(c.getColumnIndex(KEY_ICON))]);

            Marker m =map.addMarker(opt);
            m.setTag(c.getInt(c.getColumnIndex(KEY_ICON)));
            m.setSnippet(c.getString(c.getColumnIndex(KEY_ID)));
            return m;
        }
        return null;
    }*/
    public Marker getMarker(ArrayList<Marker> mList,LatLng pos){
        Iterator<Marker> iterator = mList.iterator();
        Marker cur;
        while (iterator.hasNext()){
            cur=iterator.next();
            if(cur.getPosition().equals(pos))
                return cur;
        }
        return null;
    }
    public Marker getMarker(ArrayList<Marker> mList,long marker_id){
        Iterator<Marker> iterator = mList.iterator();
        Marker cur;
        while (iterator.hasNext()){
            cur=iterator.next();
            if(cur.getTag().equals(marker_id))
                return cur;
        }
        return null;
    }
    public ArrayList<Marker> getALLMarkers(long plan_id, GoogleMap map){ //include even if not assign with any route.
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Marker> mList = new ArrayList<>();

        String selectQuery = "SELECT * FROM "+ TABLE_MARKER +" WHERE "+KEY_PLAN_ID+" = '"+plan_id+"'";
        Cursor c = db.rawQuery(selectQuery,null);

        if(c!=null && c.getCount()!=0) {
            c.moveToFirst();
            do{
                MarkerOptions opt = new MarkerOptions();
                opt.draggable(true);
                opt.position(new LatLng(c.getDouble(c.getColumnIndex(KEY_LATITUDE)),c.getDouble(c.getColumnIndex(KEY_LONGITUDE))));
                opt.title(c.getString(c.getColumnIndex(KEY_TITLE)));
                opt.icon(MapMain.Micon.getMarkerIcon(c.getInt(c.getColumnIndex(KEY_ICON))));

                Marker m =map.addMarker(opt);
                m.setTag(new MarkerTag(c.getInt(c.getColumnIndex(KEY_ICON)),c.getLong(c.getColumnIndex(KEY_ID))));
                //    m.setSnippet(m.getTag().toString());

                mList.add(m);
            } while (c.moveToNext());
        }
        return mList;
    }

    //update
    public int updatePlan(Plan plan) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TITLE,plan.getTitle());
    /*  values.put(KEY_CITY,plan.getCity());
        values.put(KEY_LATITUDE,plan.getCentre().latitude);
        values.put(KEY_LONGITUDE,plan.getCentre().longitude);
    */  values.put(KEY_YEAR,plan.getYear());
        values.put(KEY_MONTH,plan.getMonth());
        values.put(KEY_DAY,plan.getDay());

        // updating plan date
        return db.update(TABLE_PLAN, values, KEY_ID + " = ?", new String[] { String.valueOf(plan.getId()) });
    }
    public int updatePlan(long plan_id, String newTitle){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TITLE,newTitle);

        Log.d("UPDATE","PLAN "+plan_id);
        // updating plan title
        return db.update(TABLE_PLAN, values, KEY_ID + " = ? ", new String[] { String.valueOf(plan_id) });
    }
    public int updatePlan(long plan_id, Date newDate){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_YEAR,newDate.getYear());
        values.put(KEY_MONTH,newDate.getMonth());
        values.put(KEY_DAY,newDate.getDate());

        Log.d("UPDATE","PLAN "+plan_id+" :"+newDate.getMonth()+"/"+newDate.getDate());
        // updating plan date
        return db.update(TABLE_PLAN, values, KEY_ID + " = ? ", new String[] { String.valueOf(plan_id) });
    }

    public int updateMarker(Marker marker){ //update position
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM "+ TABLE_MARKER + " WHERE " + KEY_ID +" = '"+marker.getTag().toString()+"'";
        Cursor c = db.rawQuery(selectQuery,null);

        if(c!=null && c.getCount()!=0){
            c.moveToFirst();

            Log.d("마커위치변경",marker.getTag().toString()+"***********");
            ContentValues values = new ContentValues();
            values.put(KEY_LATITUDE,marker.getPosition().latitude);
            values.put(KEY_LONGITUDE,marker.getPosition().longitude);

            return db.update(TABLE_MARKER,values,KEY_ID + " = ?",new String[]{ String.valueOf(c.getLong(c.getColumnIndex(KEY_ID))) });
        }
        return -1;
    }
    public int updateMarker(Marker marker,int icon){ //update Icon & title
        SQLiteDatabase db = this.getWritableDatabase();
        MarkerTag tag = (MarkerTag) marker.getTag();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE,marker.getTitle());
        values.put(KEY_ICON,icon);
        tag.setIcon(icon);
        marker.setTag(tag);

        return db.update(TABLE_MARKER,values,KEY_ID + " = ?",new String[]{ String.valueOf(tag.getId())});
    }


    //delete
    /*public void deleteMarker(long plan_id,Marker marker){
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM "+ TABLE_MARKER + " WHERE " + KEY_PLAN_ID +" = '"+plan_id+"'"
                +" AND "+KEY_LATITUDE + " = '" +marker.getPosition().latitude+"'"
                +" AND "+KEY_LONGITUDE+ " = '" +marker.getPosition().longitude+"'";
        Cursor c = db.rawQuery(selectQuery,null);

        if(c!=null && c.getCount()!=0) {
            c.moveToFirst();
            deleteMarkerList(plan_id, c.getLong(c.getColumnIndex(KEY_ID)));
            db.delete(TABLE_MARKER, KEY_ID + " = ?", new String[]{String.valueOf(c.getLong(c.getColumnIndex(KEY_ID)))});
        }
    }*/
    public void deleteMarker(Marker marker){
        SQLiteDatabase db = this.getWritableDatabase();
        MarkerTag tag = (MarkerTag) marker.getTag();

        Log.d("마커삭제",marker.getTag()+"***********");
        deleteMarkerList(tag.getId());
        //deleteMarkerList(marker);
        db.delete(TABLE_MARKER, KEY_ID + " = ?", new String[]{ String.valueOf(tag.getId()) });
    }
    public void deleteALLMarker(long plan_id){
        SQLiteDatabase db = this.getWritableDatabase();
        while (db.delete(TABLE_MARKER, KEY_PLAN_ID + " = ?", new String[]{ String.valueOf(plan_id) })>0){
        }
    }
    public void deleteMarkerList(Marker marker){ //because the marker was removed
        SQLiteDatabase db = this.getWritableDatabase();
        MarkerTag tag = (MarkerTag) marker.getTag();

        while(db.delete(TABLE_MARKERLIST,KEY_MARKER_ID+" = ?",new String[]{ String.valueOf(tag.getId()) })>0){
        }
    }
    public void deleteMarkerList(long marker_id){ //because the marker was removed
        SQLiteDatabase db = this.getWritableDatabase();
        while(db.delete(TABLE_MARKERLIST,KEY_MARKER_ID+" = ?",new String[]{ String.valueOf(marker_id) })>0){
            }
    }
    public void deleteMarkerListR(long route_id){ //because the route was removed
        SQLiteDatabase db = this.getWritableDatabase();
        while(db.delete(TABLE_MARKERLIST,KEY_ROUTE_ID+" = ?",new String[]{String.valueOf(route_id)})>0){
        }
    }
    public int deleteMarkerList(long route_id,long marker_id){ // the marker was removed just from the route.
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_MARKERLIST + " WHERE " +KEY_ROUTE_ID + " = '" +route_id +"'"
                +" AND "+KEY_MARKER_ID + " = '" +marker_id+"'";

        Cursor c = db.rawQuery(selectQuery,null);
        if(c!=null && c.getCount()!=0 ){
            c.moveToFirst();
            return db.delete(TABLE_MARKERLIST,KEY_ID+" = ?",new String[]{ String.valueOf(c.getInt(c.getColumnIndex(KEY_ID)))});
        }
        return 0;
    }

    public int deleteRoute(Route route){
        SQLiteDatabase db = this.getWritableDatabase();
        deleteMarkerListR(route.getId());
        return db.delete(TABLE_ROUTE,KEY_ROUTE_ID+" = ?",new String[]{String.valueOf(route.getId())});
    }

    public void deleteRoute(long plan_id){ //because the plan was deleted
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_ROUTE + " WHERE " +KEY_PLAN_ID + " = '" +plan_id+"' ";

        Cursor c = db.rawQuery(selectQuery,null);
        if(c!=null && c.getCount()!=0 ){
            c.moveToFirst();
            do{
                db.delete(TABLE_ROUTE,KEY_ID+" = ?",new String[]{ String.valueOf(c.getInt(c.getColumnIndex(KEY_ID)))});
                deleteMarkerListR(c.getInt(c.getColumnIndex(KEY_ID)));
            }while (c.moveToNext());
        }
    }

    public int deletePlan(long plan_id){
        SQLiteDatabase db = this.getWritableDatabase();
        deleteRoute(plan_id);
        deleteALLMarker(plan_id);
        return db.delete(TABLE_PLAN,KEY_ID+" = ?",new String[] {String.valueOf(plan_id)});
    }

}
