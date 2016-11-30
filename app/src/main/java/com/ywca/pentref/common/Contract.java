package com.ywca.pentref.common;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Encapsulates constants when the CRUD operations are performed by {@link PentrefProvider}.
 */
public final class Contract {
    static final String AUTHORITY = "com.ywca.pentref.provider";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final class Poi implements BaseColumns {
        static final String TABLE_NAME = "poi";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_HEADER_IMAGE_FILE_NAME = "header_image";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_WEBSITE_URI = "website_uri";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_TIMESTAMP = "timestamp";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_NAME);

        public static final String[] PROJECTION_ALL = {
                _ID,
                COLUMN_NAME,
                COLUMN_HEADER_IMAGE_FILE_NAME,
                COLUMN_DESCRIPTION,
                COLUMN_WEBSITE_URI,
                COLUMN_ADDRESS,
                COLUMN_LATITUDE,
                COLUMN_LONGITUDE,
                COLUMN_TIMESTAMP
        };
    }

    public static final class Category implements BaseColumns {
        static final String TABLE_NAME = "category";

        public static final String COLUMN_NAME = "category_name";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_NAME);

        public static final String[] PROJECTION_ALL = {
                _ID, COLUMN_NAME
        };
    }

    public static final class Bookmark implements BaseColumns {
        static final String TABLE_NAME = "bookmark";

        public static final String COLUMN_POI_ID = "poi_id";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_NAME);

        public static final String[] PROJECTION_ALL = {
                _ID, COLUMN_POI_ID
        };
    }

    public static final class BookmarkedPois {
        public static final String PATH = "bookmarked_pois";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH);
    }

    public static final class Transport implements BaseColumns {
        public static final String TABLE_NAME = "transportation";

        public static final String COLUMN_ROUTE_NUMBER = "route_number";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_ADULT_PRICE = "adult_price";
        public static final String COLUMN_CHILD_PRICE = "child_price";
        public static final String COLUMN_DEPARTURE_STATION = "departure_station";
        public static final String COLUMN_DESTINATION_STATION = "destination_station";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_NAME);

        public static final String[] PROJECTION_ALL = {
                _ID,
                COLUMN_ROUTE_NUMBER,
                COLUMN_TYPE,
                COLUMN_ADULT_PRICE,
                COLUMN_CHILD_PRICE,
                COLUMN_DEPARTURE_STATION,
                COLUMN_DESTINATION_STATION
        };
    }
}