package com.ellalee.travelmaker;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jiwon on 2018-06-06.
 */

public class CalendarDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MyDB";
    static final String DATABASE_TABLE = "contacts";
    static final int DATABASE_VERSION = 1;
    static final String DATABASE_CREATE = "create table contacts (_id integer primary key autoin";
    public CalendarDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, null, version);
    }

    public void onCreate(SQLiteDatabase db) {
        try {
            String sql = "create table calendar (" + "date text, " + "sche text, " + "memo text);";
            db.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        String sql = "drop table if exists calendar";
        db.execSQL(sql);
        onCreate(db);
    }

}
//    public void insert(String date, String sche, String memo) {
//        SQLiteDatabase db = getWritableDatabase();
//        db.execSQL("INSERT INTO CALENDAR VALUES('" + date + "', " + sche + ", '" + memo + "');");
//        db.close();
//    }
//
//    public void update(String date, String sche, String memo) {
//        SQLiteDatabase db = getWritableDatabase();
//        db.execSQL("UPDATE CALENDAR SET memo=" + memo + " WHERE date='" + date + "' AND sche='" + sche +"';" );
//        db.close();
//    }
//
//    public void delete(String date, String sche) {
//        SQLiteDatabase db = getWritableDatabase();
//
//        db.execSQL("DELETE FROM CALENDAR WHERE date='" + date + "' AND sche='" + sche + "';");
//        db.close();
//    }
//
//    public Cursor getResult() {
//        SQLiteDatabase db = getReadableDatabase();
//       // String result  = "";
//
//        Cursor cursor = db.rawQuery("SELECT * FROM CALENDAR", null);
////        while(cursor.moveToNext()) {
////            String Date = cursor.getString(cursor.getColumnIndex("date"));
////            String Schedule = cursor.getString(cursor.getColumnIndex("schedule"));
////            String Memo = cursor.getString(cursor.getColumnIndex("memo"));
////
////            result += Date + " " + Schedule + " " + Memo;
////        }
//        return cursor;
//    }
//}
