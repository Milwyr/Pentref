package com.ywca.pentref.common;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
}