package com.ywca.pentref.common;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ywca.pentref.models.Poi;
import com.ywca.pentref.models.Transport;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link ContentProvider} that exposes the local {@link SQLiteDatabase} to other applications via content uri.
 */
public class PentrefProvider extends ContentProvider {
    //region URI matcher
    private static final int POI_TABLE = 1;
    private static final int POI_ROW = 2;
    private static final int TRANSPORT_TABLE = 3;
    private static final int TRANSPORT_ROW = 4;

    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        mUriMatcher.addURI(Contract.AUTHORITY, Contract.Poi.TABLE_NAME, POI_TABLE);
        mUriMatcher.addURI(Contract.AUTHORITY, Contract.Poi.TABLE_NAME + "/#", POI_ROW);
        mUriMatcher.addURI(Contract.AUTHORITY, Contract.Transport.TABLE_NAME, TRANSPORT_TABLE);
        mUriMatcher.addURI(Contract.AUTHORITY, Contract.Transport.TABLE_NAME + "/#", TRANSPORT_ROW);
    }
    //endregion

    private LocalDatabaseHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new LocalDatabaseHelper(getContext());
        return true;
    }

    //region CRUD operations (create, read, update, delete)
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection,
                        String selection, String[] selectionArgs, String sortOrder) {
        String tableName;
        switch (mUriMatcher.match(uri)) {
            case POI_TABLE:
                tableName = Contract.Poi.TABLE_NAME;
                break;
            case POI_ROW:
                tableName = Contract.Poi.TABLE_NAME;
                selection += Contract.Poi.COLUMN_ID + " = " + uri.getLastPathSegment();
                break;
            case TRANSPORT_TABLE:
                tableName = Contract.Transport.TABLE_NAME;
                break;
            case TRANSPORT_ROW:
                tableName = Contract.Transport.TABLE_NAME;
                selection += Contract.Transport.COLUMN_ID + " = " + uri.getLastPathSegment();
                break;
            default:
                throw new IllegalArgumentException("Illegal uri: " + uri);
        }

        Cursor cursor = mDbHelper.getWritableDatabase().query(
                tableName, projection, selection, selectionArgs, null, null, sortOrder);

        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        String tableName = convertToTableName(uri);

        long id = mDbHelper.getWritableDatabase().insertWithOnConflict(
                tableName, null, values, SQLiteDatabase.CONFLICT_ROLLBACK);

        if (id >= 0) {
            Uri newUri = ContentUris.withAppendedId(uri, id);

            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(newUri, null);
            }
            return newUri;
        }
        throw new SQLException("Problem while inserting into uri: " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        String tableName = convertToTableName(uri);
        int count = mDbHelper.getWritableDatabase().delete(tableName, selection, selectionArgs);

        if (count > 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values,
                      String selection, String[] selectionArgs) {
        String tableName = convertToTableName(uri);
        int count = mDbHelper.getWritableDatabase()
                .update(tableName, values, selection, selectionArgs);

        if (count > 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;
    }
    //endregion

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    //region Helper methods

    /**
     * A helper method that initialises a {@link ContentValues}
     * with the values of all columns of a {@link Poi} instance.
     *
     * @param poi An object instance of {@link Poi}
     * @return A {@link ContentValues} with the values of a {@link Poi} instance
     */
    public static ContentValues getContentValues(Poi poi) {
        ContentValues values = new ContentValues();
        values.put(Contract.Poi.COLUMN_ID, poi.getId());
        values.put(Contract.Poi.COLUMN_NAME, poi.getName());
        values.put(Contract.Poi.COLUMN_DESCRIPTION, poi.getDescription());
        values.put(Contract.Poi.COLUMN_WEBSITE_URI, poi.getWebsiteUri());
        values.put(Contract.Poi.COLUMN_ADDRESS, poi.getAddress());
        values.put(Contract.Poi.COLUMN_LATITUDE, poi.getLatLng().latitude);
        values.put(Contract.Poi.COLUMN_LONGITUDE, poi.getLatLng().longitude);
        values.put(Contract.Poi.COLUMN_TIMESTAMP, "To be implemented");
        return values;
    }

    /**
     * A helper method that initialises a {@link ContentValues}
     * with the values of all columns of a {@link Transport} instance.
     *
     * @param transport An object instance of {@link Transport}
     * @return A {@link ContentValues} with the values of a {@link Transport} instance
     */
    public static ContentValues getContentValues(Transport transport) {
        ContentValues values = new ContentValues();
        values.put(Contract.Transport.COLUMN_ID, transport.getId());
        values.put(Contract.Transport.COLUMN_ROUTE_NUMBER, transport.getRouteNumber());
        values.put(Contract.Transport.COLUMN_TYPE, transport.getTypeEnum().getValue());
        values.put(Contract.Transport.COLUMN_ADULT_PRICE, transport.getAdultPrice());
        values.put(Contract.Transport.COLUMN_CHILD_PRICE, transport.getChildPrice());
        values.put(Contract.Transport.COLUMN_DEPARTURE_STATION, transport.getDepartureStation());
        values.put(Contract.Transport.COLUMN_DESTINATION_STATION, transport.getDestinationStation());
        return values;
    }
    //endregion

    private String convertToTableName(Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case POI_TABLE:
                return Contract.Poi.TABLE_NAME;
            case POI_ROW:
                return Contract.Poi.TABLE_NAME;
            case TRANSPORT_TABLE:
                return Contract.Transport.TABLE_NAME;
            case TRANSPORT_ROW:
                return Contract.Transport.TABLE_NAME;
            default:
                throw new IllegalArgumentException("Illegal uri: " + uri);
        }
    }

    //region SQLite database
    private final int DATABASE_VERSION = 1;
    private final String DATABASE_NAME = "Pentref.db";

    class LocalDatabaseHelper extends SQLiteOpenHelper {

        LocalDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Create a table for point of interest
            final String CREATE_POI_TABLE_SQL_QUERY = "CREATE TABLE IF NOT EXISTS " +
                    Contract.Poi.TABLE_NAME + " (" +
                    Contract.Poi.COLUMN_ID + " LONG PRIMARY KEY, " +
                    Contract.Poi.COLUMN_NAME + " VARCHAR(255), " +
                    Contract.Poi.COLUMN_DESCRIPTION + " VARCHAR(255), " +
                    Contract.Poi.COLUMN_WEBSITE_URI + " VARCHAR(255), " +
                    Contract.Poi.COLUMN_ADDRESS + " VARCHAR(255), " +
                    Contract.Poi.COLUMN_LATITUDE + " DOUBLE, " +
                    Contract.Poi.COLUMN_LONGITUDE + " DOUBLE, " +
                    Contract.Poi.COLUMN_TIMESTAMP + " VARCHAR(255));";
            db.execSQL(CREATE_POI_TABLE_SQL_QUERY);

            // Create a table for transportation
            final String CREATE_TRANSPORT_TABLE_SQL_QUERY = "CREATE TABLE IF NOT EXISTS " +
                    Contract.Transport.TABLE_NAME + " (" +
                    Contract.Transport.COLUMN_ID + " LONG PRIMARY KEY, " +
                    Contract.Transport.COLUMN_ROUTE_NUMBER + " VARCHAR(255), " +
                    Contract.Transport.COLUMN_TYPE + " INTEGER, " +
                    Contract.Transport.COLUMN_ADULT_PRICE + " FLOAT, " +
                    Contract.Transport.COLUMN_CHILD_PRICE + " FLOAT, " +
                    Contract.Transport.COLUMN_DEPARTURE_STATION + " VARCHAR(255), " +
                    Contract.Transport.COLUMN_DESTINATION_STATION + " VARCHAR(255)); ";
            db.execSQL(CREATE_TRANSPORT_TABLE_SQL_QUERY);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // The upgrade policy is to simply remove all tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + Contract.Poi.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + Contract.Transport.TABLE_NAME);
            onCreate(db);
        }
    }
    //endregion
}