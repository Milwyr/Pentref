package com.ywca.pentref.common;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.ywca.pentref.models.Poi;
import com.ywca.pentref.models.Transport;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class to manage the local SQLite database.
 */

public class LocalDatabaseHelper extends SQLiteOpenHelper {
    // If the database schema is changed, the database version must be incremented
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Pentref.db";

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
        final String CREATE_POI_TABLE_SQL_QUERY = "CREATE TABLE IF NOT EXISTS " +
                Poi.TABLE_NAME + " (" +
                Poi.COLUMN_ID + " LONG PRIMARY KEY, " +
                Poi.COLUMN_NAME + " VARCHAR(255), " +
                Poi.COLUMN_DESCRIPTION + " VARCHAR(255), " +
                Poi.COLUMN_WEBSITE_URI + " VARCHAR(255), " +
                Poi.COLUMN_ADDRESS + " VARCHAR(255), " +
                Poi.COLUMN_LATITUDE + " DOUBLE, " +
                Poi.COLUMN_LONGITUDE + " DOUBLE, " +
                Poi.COLUMN_TIMESTAMP + " VARCHAR(255));";
        sqLiteDatabase.execSQL(CREATE_POI_TABLE_SQL_QUERY);

        final String CREATE_TRAN_TABLE_SQL_QUERY = "CREATE TABLE IF NOT EXISTS " +
                Transport.TABLE_NAME + " (" +
                Transport.COLUMN_ID + " LONG PRIMARY KEY, " +
                Transport.COLUMN_ROUTENUM + " VARCHAR(255), " +
                Transport.COLUMN_TYPE + " VARCHAR(255), " +
                Transport.COLUMN_ADULTPRICE + " FLOAT, " +
                Transport.COLUMN_CHILDPRICE + " FLOAT, " +
                Transport.COLUMN_DEP_STATION + " VARCHAR(255), " +
                Transport.COLUMN_DES_STATION + " VARCHAR(255), ";
        sqLiteDatabase.execSQL(CREATE_TRAN_TABLE_SQL_QUERY);
        // TODO: Create a Transport table
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    //region Point of Interest
    public List<Poi> getPois() {
        List<Poi> pois = new ArrayList<>();

        String[] columns = {
                Poi.COLUMN_ID,
                Poi.COLUMN_NAME,
                Poi.COLUMN_DESCRIPTION,
                Poi.COLUMN_WEBSITE_URI,
                Poi.COLUMN_ADDRESS,
                Poi.COLUMN_LATITUDE,
                Poi.COLUMN_LONGITUDE
        };

        Cursor cursor = getWritableDatabase().query(
                Poi.TABLE_NAME, columns, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            long id = cursor.getLong(cursor.getColumnIndex(Poi.COLUMN_ID));
            String name = cursor.getString(cursor.getColumnIndex(Poi.COLUMN_NAME));
            String description = cursor.getString(cursor.getColumnIndex(Poi.COLUMN_DESCRIPTION));
            String websiteUri = cursor.getString(cursor.getColumnIndex(Poi.COLUMN_WEBSITE_URI));
            String address = cursor.getString(cursor.getColumnIndex(Poi.COLUMN_ADDRESS));
            double latitude = cursor.getDouble(cursor.getColumnIndex(Poi.COLUMN_LATITUDE));
            double longitude = cursor.getDouble(cursor.getColumnIndex(Poi.COLUMN_LONGITUDE));

            pois.add(new Poi(id, name, description, websiteUri, address, new LatLng(latitude, longitude)));

            cursor.moveToNext();
        }
        cursor.close();
        return pois;
    }

    /**
     * Inserts a Point of Interest to the SQLite database.
     *
     * @param poi A Point of Interest to be inserted
     * @return True if the Point of Interest is inserted successfully
     */
    public boolean insertPoi(Poi poi) {
        ContentValues values = new ContentValues();

        values.put(Poi.COLUMN_ID, poi.getId());
        values.put(Poi.COLUMN_NAME, poi.getName());
        values.put(Poi.COLUMN_DESCRIPTION, poi.getDescription());
        values.put(Poi.COLUMN_WEBSITE_URI, poi.getWebsiteUri());
        values.put(Poi.COLUMN_ADDRESS, poi.getAddress());
        values.put(Poi.COLUMN_LATITUDE, poi.getLatLng().latitude);
        values.put(Poi.COLUMN_LONGITUDE, poi.getLatLng().longitude);

        // TODO: Get the timestamp from server
        values.put(Poi.COLUMN_TIMESTAMP, "To be implemented");

        return getWritableDatabase().insertWithOnConflict(
                Poi.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ROLLBACK) > 0;
    }

    /**
     * Inserts a list of Point of Interests to the SQLite database.
     *
     * @param pois A list of Point of Interests
     */
    public void insertPois(List<Poi> pois) {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransactionNonExclusive();

        String sql = "INSERT OR ROLLBACK INTO " + Poi.TABLE_NAME + " (" +
                Poi.COLUMN_ID + ", " +
                Poi.COLUMN_NAME + ", " +
                Poi.COLUMN_DESCRIPTION + ", " +
                Poi.COLUMN_WEBSITE_URI + ", " +
                Poi.COLUMN_ADDRESS + ", " +
                Poi.COLUMN_LATITUDE + ", " +
                Poi.COLUMN_LONGITUDE + ", " +
                Poi.COLUMN_TIMESTAMP + ")  VALUES(?, ?, ?, ?, ?, ?, ?, ?);";

        try {
            SQLiteStatement statement = database.compileStatement(sql);

            for (Poi poi : pois) {
                statement.bindLong(1, poi.getId());
                statement.bindString(2, poi.getName());
                statement.bindString(3, poi.getDescription());
                statement.bindString(4, poi.getWebsiteUri());
                statement.bindString(5, poi.getAddress());
                statement.bindDouble(6, poi.getLatLng().latitude);
                statement.bindDouble(7, poi.getLatLng().longitude);

                // TODO: Get the timestamp from server
                statement.bindString(8, "To be implemented");

                statement.execute();
                statement.clearBindings();
            }

            database.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e("LocalDatabaseHelper", e.getMessage());
        } finally {
            if (database.inTransaction()) {
                database.endTransaction();
            }
        }
    }

    public void updatePoi() {
        // TODO: To be implemented
    }
    //endregion

    //region Transport

    public List<Transport> getTransports() {
        List<Transport> transports = new ArrayList<>();

        String[] columns = {
                Transport.COLUMN_ID,
                Transport.COLUMN_ROUTENUM,
                Transport.COLUMN_TYPE,
                Transport.COLUMN_ADULTPRICE,
                Transport.COLUMN_CHILDPRICE,
                Transport.COLUMN_DEP_STATION,
                Transport.COLUMN_DES_STATION
        };

        //not sure
        Cursor cursor = getWritableDatabase().query(
                Transport.TABLE_NAME, columns, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            long id = cursor.getLong(cursor.getColumnIndex(Transport.COLUMN_ID));
            String routeNum = cursor.getString(cursor.getColumnIndex(Transport.COLUMN_ROUTENUM));
            long typeIndex = cursor.getLong(cursor.getColumnIndex(Transport.COLUMN_TYPE));

            Transport.TypeEnum typeEnum;
            if (typeIndex == 0) {
                typeEnum = Transport.TypeEnum.BUS;
            } else {
                typeEnum = Transport.TypeEnum.FERRY;
            }

            float adultPrice = cursor.getFloat(cursor.getColumnIndex(Transport.COLUMN_ADULTPRICE));
            float childPrice = cursor.getFloat(cursor.getColumnIndex(Transport.COLUMN_CHILDPRICE));
            String depStation = cursor.getString(cursor.getColumnIndex(Transport.COLUMN_DEP_STATION));
            String desStation = cursor.getString(cursor.getColumnIndex(Transport.COLUMN_DES_STATION));


            transports.add(new Transport(id, routeNum, typeEnum, adultPrice, childPrice, depStation, desStation));

            cursor.moveToNext();
        }
        cursor.close();
        return transports;
    }

    /**
     * Inserts a transport (bus or ferry) to the SQLite database.
     *
     * @param transport A transport (bus or ferry)
     * @return True if the transport is inserted successfully
     */
    public boolean insertTransport(Transport transport) {
        // TODO: To be implemented
        ContentValues values = new ContentValues();

        values.put(Transport.COLUMN_ID, transport.getId());
        values.put(Transport.COLUMN_ROUTENUM, transport.getRouteNum());
        values.put(Transport.COLUMN_TYPE, transport.getTypeEnum().getValue());
        values.put(Transport.COLUMN_ADULTPRICE, transport.getAdultPrice());
        values.put(Transport.COLUMN_CHILDPRICE, transport.getChildPrice());
        values.put(Transport.COLUMN_DEP_STATION, transport.getDepartureStation());
        values.put(Transport.COLUMN_DES_STATION, transport.getDestinationStation());


        return getWritableDatabase().insertWithOnConflict(
                Transport.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ROLLBACK) > 0;
    }

    /**
     * Inserts a list of transports (bus or ferry) to the SQLite database.
     *
     * @param transports A list of transports (bus or ferry)
     */
    public void insertTransports(List<Transport> transports) {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransactionNonExclusive();

        String sql = "INSERT OR ROLLBACK INTO " + Transport.TABLE_NAME + " (" +
                Transport.COLUMN_ID + ", " +
                Transport.COLUMN_ROUTENUM + ", " +
                Transport.COLUMN_TYPE + ", " +
                Transport.COLUMN_ADULTPRICE + ", " +
                Transport.COLUMN_CHILDPRICE + ", " +
                Transport.COLUMN_DEP_STATION + ", " +
                Transport.COLUMN_DES_STATION + ")  VALUES(?, ?, ?, ?, ?, ?, ?);";

        try {
            SQLiteStatement statement = database.compileStatement(sql);

            for (Transport transport : transports) {
                statement.bindLong(1, transport.getId());
                statement.bindString(2, transport.getRouteNum());
                statement.bindLong(3, transport.getTypeEnum().getValue());
                statement.bindDouble(4, transport.getAdultPrice());
                statement.bindDouble(5, transport.getChildPrice());
                statement.bindString(6, transport.getDepartureStation());
                statement.bindString(7, transport.getDestinationStation());

                // TODO: Get the timestamp from server
                statement.bindString(8, "To be implemented");

                statement.execute();
                statement.clearBindings();
            }

            database.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e("LocalDatabaseHelper", e.getMessage());
        } finally {
            if (database.inTransaction()) {
                database.endTransaction();
            }
        }
    }

    public void updateTransport() {
        // TODO: To be implemented
    }
    //endregion
}