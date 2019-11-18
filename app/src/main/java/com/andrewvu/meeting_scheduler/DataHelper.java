package com.andrewvu.meeting_scheduler;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;
    public static final String DB_NAME = "schedule";    // database
    public static final String DB_TABLE = "meetings";   // table for meetings
    public static final int DB_VERSION = 1;

    private static final String CREATE_TABLE = "CREATE TABLE " + DB_TABLE +
            " (id INTEGER PRIMARY KEY, date TEXT NOT NULL, " +
            "time TEXT NOT NULL, name TEXT, phone TEXT);";


    DataHelper(Context context) {
        super(context,DB_NAME,null,DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //How to migrate or reconstruct data from old version to new on upgrade
    }
}
