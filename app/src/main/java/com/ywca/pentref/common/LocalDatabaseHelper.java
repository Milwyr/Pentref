package com.ywca.pentref.common;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * A helper class to manage the local SQLite database.
 */

public class LocalDatabaseHelper extends SQLiteOpenHelper {
    // If the database schema is changed, the database version must be incremented
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Pentref.db";

    // An instance of LocalDatabaseHelper
    private static LocalDatabaseHelper mDbInstance;

    private LocalDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creates a new instance of LocalDatabaseHelper if it is null.
     *
     * @param context Context
     * @return A LocalDatabaseHelper instance
     */
    public static synchronized LocalDatabaseHelper getInstance(Context context) {
        if (mDbInstance == null) {
            mDbInstance = new LocalDatabaseHelper(context);
        }
        return mDbInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
//        final String CREATE_POI_TABLE_SQL_QUERY = "CREATE TABLE IF NOT EXISTS " + + tableName);
//        sqLiteDatabase.execSQL(CREATE_POI_TABLE_SQL_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}