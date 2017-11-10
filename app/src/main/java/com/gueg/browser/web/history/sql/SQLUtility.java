package com.gueg.browser.web.history.sql;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.gueg.browser.web.history.HistoryItem;
import com.gueg.browser.web.history.sql.SQLReaderContract.SQLEntry;

import java.util.ArrayList;

public class SQLUtility {

    private ArrayList<HistoryItem> savedHistory = new ArrayList<>();
    private Context context;

    private int lastPos = 0;

    private SQLReader helper;
    private static String[] projection = {
            SQLEntry._ID,
            SQLEntry.DB_COLUMN_TITLE,
            SQLEntry.DB_COLUMN_URL,
            SQLEntry.DB_COLUMN_DATE
    };

    public SQLUtility(Context context) {
        helper = new SQLReader(context);
        this.context = context;
    }

    public ArrayList<HistoryItem> read20HistoryItems() {

        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query(
                SQLEntry.DB_TABLE_NAME,                     // The table to query
                projection,                                 // The columns to return
                null,                                       // The columns for the WHERE clause
                null,                                       // The values for the WHERE clause
                null,                                       // don't group the rows
                null,                                       // don't filter by row groups
                SQLEntry.DB_COLUMN_DATE +" DESC"        // The sort order
        );

        ArrayList<HistoryItem> items = new ArrayList<>();
        while(cursor.moveToNext()&&cursor.getPosition()<lastPos);
        while(cursor.moveToNext()&&cursor.getPosition()<lastPos+20) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_TITLE));
            String url = cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_URL));
            Long date = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_DATE)));

            items.add(new HistoryItem(title,url,date));
        }
        cursor.close();

        lastPos+=20;

        savedHistory.addAll(items);
        return items;
    }


    public void write(ArrayList<HistoryItem> items) {
        for (HistoryItem item : items) {
            boolean found = false;
            for (HistoryItem i : savedHistory) {
                if (i.title.equals(item.title) && i.url.equals(item.url) && i.date.equals(item.date))
                    found = true;
            }
            if (!found)
                write(item);
        }
    }

    private void write(HistoryItem item) {
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SQLEntry.DB_COLUMN_TITLE, item.title);
        values.put(SQLEntry.DB_COLUMN_URL, item.url);
        values.put(SQLEntry.DB_COLUMN_DATE, item.date.toString());

        db.insert(SQLEntry.DB_TABLE_NAME, null, values);

        savedHistory.add(item);
    }


    public void removeItem(int pos) {
        SQLiteDatabase db = helper.getWritableDatabase();
        // Define 'where' part of query.
        String selection = SQLEntry.DB_COLUMN_TITLE + " LIKE ? AND " + SQLEntry.DB_COLUMN_URL + " LIKE ? AND " + SQLEntry.DB_COLUMN_DATE + " LIKE ?" ;
        // Specify arguments in placeholder order.
        if(pos<savedHistory.size()) {
            HistoryItem item = savedHistory.get(pos);
            String[] selectionArgs = {item.title, item.url, item.date.toString()};
            // Issue SQL statement.
            db.delete(SQLEntry.DB_TABLE_NAME, selection, selectionArgs);
        }
    }

    public void clearHistory() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(SQLEntry.DB_TABLE_NAME, null, null);
        Toast.makeText(context, "Historique effacé", Toast.LENGTH_SHORT).show();
    }

    public void onDestroy() {
        helper.close();
    }

}
