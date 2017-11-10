package com.gueg.browser.rss.sql;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.gueg.browser.rss.RssItem;
import com.gueg.browser.rss.sql.SQLReaderContract.SQLEntry;
import java.util.ArrayList;

public class SQLUtility {

    private ArrayList<RssItem> savedRss = new ArrayList<>();

    private SQLReader helper;
    private static String[] projection = {
            SQLEntry._ID,
            SQLEntry.DB_COLUMN_FEED,
            SQLEntry.DB_COLUMN_FEED_URL,
            SQLEntry.DB_COLUMN_TITLE,
            SQLEntry.DB_COLUMN_LINK,
            SQLEntry.DB_COLUMN_PUBLISHED,
            SQLEntry.DB_COLUMN_DESCRIPTION,
    };

    public SQLUtility(Context context) {
        helper = new SQLReader(context);
    }

    public ArrayList<RssItem> readAllRssItems() {

        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query(
                SQLEntry.DB_TABLE_NAME,                     // The table to query
                projection,                                 // The columns to return
                null,                                       // The columns for the WHERE clause
                null,                                       // The values for the WHERE clause
                null,                                       // don't group the rows
                null,                                       // don't filter by row groups
                SQLEntry.DB_COLUMN_PUBLISHED +" DESC"       // The sort order
        );

        ArrayList<RssItem> items = new ArrayList<>();
        while(cursor.moveToNext()) {
            String feed = cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_FEED));
            String feedUrl = cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_FEED_URL));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_TITLE));
            String link = cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_LINK));
            String published = cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_PUBLISHED));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(SQLEntry.DB_COLUMN_DESCRIPTION));

            items.add(new RssItem(feed, feedUrl, title, link, description, Long.parseLong(published)));
        }
        cursor.close();

        savedRss.addAll(items);
        return items;
    }


    public void write(ArrayList<RssItem> items) {
        for (RssItem item : items) {
            boolean found = false;
            for (RssItem i : savedRss) {
                if (i.title.equals(item.title) && i.link.equals(item.link))
                    found = true;
            }
            if (!found)
                write(item);
        }
    }

    private void write(RssItem item) {
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SQLEntry.DB_COLUMN_FEED, item.feed);
        values.put(SQLEntry.DB_COLUMN_FEED_URL, item.feedUrl);
        values.put(SQLEntry.DB_COLUMN_TITLE, item.title);
        values.put(SQLEntry.DB_COLUMN_LINK, item.link);
        values.put(SQLEntry.DB_COLUMN_PUBLISHED, item.published);
        values.put(SQLEntry.DB_COLUMN_DESCRIPTION, item.description);

        db.insert(SQLEntry.DB_TABLE_NAME, null, values);

        savedRss.add(item);
    }


    public void removeFeed(String feed) {
        SQLiteDatabase db = helper.getWritableDatabase();
        // Define 'where' part of query.
        String selection = SQLEntry.DB_COLUMN_FEED + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = {feed};
        // Issue SQL statement.
        db.delete(SQLEntry.DB_TABLE_NAME, selection, selectionArgs);
    }

    public void clear() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(SQLEntry.DB_TABLE_NAME, null, null);
    }

    public void onDestroy() {
        helper.close();
    }

}
