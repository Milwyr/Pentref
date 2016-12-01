package com.ywca.pentref.common;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.ywca.pentref.models.Poi;

/**
 * Inserts or deletes the {@link Poi} with the given poi id from the local SQLite database
 * depending on the boolean parameter in the doInBackGround() method.
 */
public class UpdateBookmarkAsyncTask extends AsyncTask<Boolean, Void, Void> {
    private Context mContext;
    private long mPoiId;

    /**
     * Constructor
     *
     * @param context Context
     * @param poiId   {@link Poi} id
     */
    protected UpdateBookmarkAsyncTask(Context context, long poiId) {
        mContext = context;
        mPoiId = poiId;
    }

    /**
     * Inserts or deletes the {@link Poi} with the given poi id from the local SQLite database.
     *
     * @param booleans boolean[0] is true if the given {@link Poi} was bookmarked before executing this method
     */
    @Override
    protected Void doInBackground(Boolean... booleans) {
        boolean isBookmarked = booleans[0];

        if (!isBookmarked) {
            // Add the poi to the bookmark table
            ContentValues values = new ContentValues();
            values.put(Contract.Bookmark.COLUMN_POI_ID, mPoiId);
            mContext.getContentResolver().insert(Contract.Bookmark.CONTENT_URI, values);
        } else {
            // Delete the bookmarked poi from the bookmark table
            Uri uri = Contract.Bookmark.CONTENT_URI;
            String selection = Contract.Bookmark.COLUMN_POI_ID + " = ?";
            String[] selectionArgs = {Long.toString(mPoiId)};

            mContext.getContentResolver().delete(uri, selection, selectionArgs);
        }

        return null;
    }
}