package com.ywca.pentref.common;

import android.app.SearchManager;
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
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.ywca.pentref.models.Poi;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A {@link ContentProvider} that exposes the local {@link SQLiteDatabase} to other applications via content uri.
 */
public class PentrefProvider extends ContentProvider {
    //region URI matcher
    private static final int POI_TABLE = 1;
    private static final int POI_ROW = 2;
    private static final int CATEGORY_TABLE = 3;
    private static final int CATEGORY_ROW = 4;
    private static final int BOOKMARK_TABLE = 5;
    private static final int BOOKMARK_ROW = 6;
    private static final int SEARCH_SUGGESTIONS = 7;

    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        mUriMatcher.addURI(Contract.AUTHORITY, Contract.Poi.TABLE_NAME, POI_TABLE);
        mUriMatcher.addURI(Contract.AUTHORITY, Contract.Poi.TABLE_NAME + "/#", POI_ROW);
        mUriMatcher.addURI(Contract.AUTHORITY, Contract.Category.TABLE_NAME, CATEGORY_TABLE);
        mUriMatcher.addURI(Contract.AUTHORITY, Contract.Category.TABLE_NAME + "/#", CATEGORY_TABLE);
        mUriMatcher.addURI(Contract.AUTHORITY, Contract.Bookmark.TABLE_NAME, BOOKMARK_TABLE);
        mUriMatcher.addURI(Contract.AUTHORITY, Contract.Bookmark.TABLE_NAME + "/#", BOOKMARK_ROW);
        mUriMatcher.addURI(Contract.AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGESTIONS);
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
        String tableName = convertToTableName(uri);
        selection = convertToSelection(uri, selection);

        if (mUriMatcher.match(uri) == SEARCH_SUGGESTIONS) {
            /*
                The column names "_id", "SUGGEST_COLUMN_TEXT_1", "SUGGEST_COLUMN_TEXT_2"
                are used to build a suggestion table and show a list of suggestions when
                the user searches for Points of Interest.

                The column "SUGGEST_COLUMN_INTENT_DATA_ID" records the POI id of each row.
            */
            projection = new String[]{
                    Contract.Poi._ID + " AS " + BaseColumns._ID,
                    Contract.Poi.COLUMN_NAME + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1,
                    Contract.Poi.COLUMN_ADDRESS + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_2,
                    Contract.Poi._ID + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID};

            // The two percent signs are used to match the 'LIKE' statements specified in searchable.xml
            if (selectionArgs.length > 0) {
                selectionArgs = new String[]{"%" + selectionArgs[0] + "%"};
            }
        }

        Cursor cursor = mDbHelper.getReadableDatabase().query(
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
        selection = convertToSelection(uri, selection);

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
        selection = convertToSelection(uri, selection);
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
        values.put(Contract.Poi._ID, poi.getId());
        values.put(Contract.Poi.COLUMN_NAME, poi.getName(Locale.ENGLISH));
        values.put(Contract.Poi.COLUMN_CHINESE_NAME, poi.getName(Locale.CHINESE));
        values.put(Contract.Poi.COLUMN_HEADER_IMAGE_FILE_NAME, poi.getHeaderImageFileName());
        values.put(Contract.Poi.COLUMN_CATEGORY_ID, poi.getCategoryId());
        values.put(Contract.Poi.COLUMN_WEBSITE_URI, poi.getWebsiteUri());
        values.put(Contract.Poi.COLUMN_ADDRESS, poi.getAddress(Locale.ENGLISH));
        values.put(Contract.Poi.COLUMN_CHINESE_ADDRESS, poi.getAddress(Locale.CHINESE));
        values.put(Contract.Poi.COLUMN_PHONE_NUMBER, poi.getPhoneNumber());
        values.put(Contract.Poi.COLUMN_LATITUDE, poi.getLatLng().latitude);
        values.put(Contract.Poi.COLUMN_LONGITUDE, poi.getLatLng().longitude);
        return values;
    }

    /**
     * A helper method that returns a list of Points of interest from the given cursor.
     *
     * @param cursor A cursor returned by the query method in this class
     * @return A list of {@link Poi}
     */
    public static List<Poi> convertToPois(@NonNull Cursor cursor) {
        List<Poi> pois = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String id = cursor.getString(cursor.getColumnIndex(Contract.Poi._ID));
            String name = cursor.getString(cursor.getColumnIndex(Contract.Poi.COLUMN_NAME));
            String chineseName = cursor.getString(cursor.getColumnIndex(Contract.Poi.COLUMN_CHINESE_NAME));
            String headerImageFileName = cursor.getString(cursor.getColumnIndex(Contract.Poi.COLUMN_HEADER_IMAGE_FILE_NAME));
            int categoryId = cursor.getInt(cursor.getColumnIndex(Contract.Poi.COLUMN_CATEGORY_ID));
            String websiteUri = cursor.getString(cursor.getColumnIndex(Contract.Poi.COLUMN_WEBSITE_URI));
            String address = cursor.getString(cursor.getColumnIndex(Contract.Poi.COLUMN_ADDRESS));
            String chineseAddress = cursor.getString(cursor.getColumnIndex(Contract.Poi.COLUMN_CHINESE_ADDRESS));
            double latitude = cursor.getDouble(cursor.getColumnIndex(Contract.Poi.COLUMN_LATITUDE));
            double longitude = cursor.getDouble(cursor.getColumnIndex(Contract.Poi.COLUMN_LONGITUDE));
            String phoneNumber = cursor.getString(cursor.getColumnIndex(Contract.Poi.COLUMN_PHONE_NUMBER));
            pois.add(new Poi(id, name, chineseName, headerImageFileName, categoryId, websiteUri,
                    address, chineseAddress, phoneNumber, new LatLng(latitude, longitude)));

            cursor.moveToNext();
        }
        cursor.close();
        return pois;
    }

    public static List<Category> convertToCategories(@NonNull Cursor cursor) {
        List<Category> categories = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int id = cursor.getInt(cursor.getColumnIndex(Contract.Category._ID));
            String name = cursor.getString(cursor.getColumnIndex(Contract.Category.COLUMN_NAME));
            categories.add(new Category(id, name));
            cursor.moveToNext();
        }
        cursor.close();
        return categories;
    }
    //endregion

    public static List<String> convertToBookmarkIds(@NonNull Cursor cursor){
        List<String> idList = new ArrayList<>();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            idList.add(cursor.getString(cursor.getColumnIndex(Contract.Bookmark.COLUMN_POI_ID)));
            cursor.moveToNext();
        }
        cursor.close();
        return idList;
    }

    private String convertToTableName(Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case POI_TABLE:
            case POI_ROW:
            case SEARCH_SUGGESTIONS:
                return Contract.Poi.TABLE_NAME;
            case CATEGORY_TABLE:
            case CATEGORY_ROW:
                return Contract.Category.TABLE_NAME;
            case BOOKMARK_TABLE:
            case BOOKMARK_ROW:
                return Contract.Bookmark.TABLE_NAME;
            default:
                throw new IllegalArgumentException("Illegal uri: " + uri);
        }
    }

    // Returns the appropriate selection statement if the uri specifies row number,
    // or returns the given selection otherwise.
    private String convertToSelection(Uri uri, String selection) {
        switch (mUriMatcher.match(uri)) {
            case POI_ROW:
                return Contract.Poi._ID + " = " + uri.getLastPathSegment();
            case CATEGORY_ROW:
                return Contract.Category._ID + " = " + uri.getLastPathSegment();
            case BOOKMARK_ROW:
                return Contract.Bookmark.COLUMN_POI_ID + " = " + uri.getLastPathSegment();
            default:
                return selection;
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
                    Contract.Poi._ID + " TEXT PRIMARY KEY, " +
                    Contract.Poi.COLUMN_NAME + " TEXT, " +
                    Contract.Poi.COLUMN_CHINESE_NAME + " TEXT, " +
                    Contract.Poi.COLUMN_HEADER_IMAGE_FILE_NAME + " TEXT, " +
                    Contract.Poi.COLUMN_CATEGORY_ID + " LONG, " +
                    Contract.Poi.COLUMN_WEBSITE_URI + " TEXT, " +
                    Contract.Poi.COLUMN_ADDRESS + " TEXT, " +
                    Contract.Poi.COLUMN_CHINESE_ADDRESS + " TEXT, " +
                    Contract.Poi.COLUMN_PHONE_NUMBER + " TEXT, " +
                    Contract.Poi.COLUMN_LATITUDE + " DOUBLE, " +
                    Contract.Poi.COLUMN_LONGITUDE + " DOUBLE);";
            db.execSQL(CREATE_POI_TABLE_SQL_QUERY);

            // Create a table for categories
            final String CREATE_CATEGORY_TABLE_SQL_QUERY = "CREATE TABLE IF NOT EXISTS " +
                    Contract.Category.TABLE_NAME + " (" +
                    Contract.Category._ID + " INTEGER PRIMARY KEY, " +
                    Contract.Category.COLUMN_NAME + " TEXT);";
            db.execSQL(CREATE_CATEGORY_TABLE_SQL_QUERY);

            // Create a table for bookmarks
            final String CREATE_BOOKMARK_TABLE_SQL_QUERY = "CREATE TABLE IF NOT EXISTS " +
                    Contract.Bookmark.TABLE_NAME + " (" +
                    Contract.Bookmark._ID + " INTEGER PRIMARY KEY, " +
                    Contract.Bookmark.COLUMN_POI_ID + " TEXT)";
            db.execSQL(CREATE_BOOKMARK_TABLE_SQL_QUERY);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // The upgrade policy is to simply remove all tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + Contract.Poi.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + Contract.Category.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + Contract.Bookmark.TABLE_NAME);
            onCreate(db);
        }
    }
    //endregion
}