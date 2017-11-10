package com.gueg.browser.rss.sql;


import android.provider.BaseColumns;

final class SQLReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private SQLReaderContract() {
    }

    /* Inner class that defines the table contents */
    class SQLEntry implements BaseColumns {
        static final String _ID = "id";
        static final String DB_TABLE_NAME = "rssitems";
        static final String DB_COLUMN_FEED = "feed";
        static final String DB_COLUMN_FEED_URL = "feed_url";
        static final String DB_COLUMN_TITLE = "title";
        static final String DB_COLUMN_LINK = "link";
        static final String DB_COLUMN_PUBLISHED = "published";
        static final String DB_COLUMN_DESCRIPTION = "description";
    }

}