package com.gueg.browser.web.history.sql;


import android.provider.BaseColumns;

final class SQLReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private SQLReaderContract() {
    }

    /* Inner class that defines the table contents */
    class SQLEntry implements BaseColumns {
        static final String _ID = "id";
        static final String DB_TABLE_NAME = "history";
        static final String DB_COLUMN_TITLE = "title";
        static final String DB_COLUMN_URL = "url";
        static final String DB_COLUMN_DATE = "date";
    }

}