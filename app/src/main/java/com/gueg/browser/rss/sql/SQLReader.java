package com.gueg.browser.rss.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.gueg.browser.rss.sql.SQLReaderContract.SQLEntry;


class SQLReader extends SQLiteOpenHelper {

    private static int DB_VERSION = 1;

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " +
                    SQLEntry.DB_TABLE_NAME + " (" +
                    SQLEntry._ID + " INTEGER PRIMARY KEY," +
                    SQLEntry.DB_COLUMN_FEED + " TEXT," +
                    SQLEntry.DB_COLUMN_FEED_URL + " TEXT," +
                    SQLEntry.DB_COLUMN_TITLE + " TEXT," +
                    SQLEntry.DB_COLUMN_LINK + " TEXT," +
                    SQLEntry.DB_COLUMN_DESCRIPTION + " TEXT," +
                    SQLEntry.DB_COLUMN_PUBLISHED + " TEXT" +
                    ")";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SQLEntry.DB_TABLE_NAME;


    SQLReader(Context context) {
        super(context, SQLEntry.DB_TABLE_NAME, null, DB_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }





}
