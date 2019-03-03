package com.locationservice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper mInstance;
    private static final String DATABASE_NAME = "LocationStore.db";
    private static final String TABLE_NAME = "location_table";
    private static final String COL_1 = "ID";
    private static final String COL_2 = "LAT";
    private static final String COL_3 = "LNG";

    public static DatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return mInstance;
    }


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME +" (LAT TEXT,LNG TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String lat,String lng) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2,lat);
        contentValues.put(COL_3,lng);
        long result = db.insert(TABLE_NAME,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from "+TABLE_NAME,null);
    }


    public Integer deleteData (String id) {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(TABLE_NAME, "ID = ?",new String[] {id});
//        return db.delete(TABLE_NAME, "ID = ?",new String[] {id});
    }
}
