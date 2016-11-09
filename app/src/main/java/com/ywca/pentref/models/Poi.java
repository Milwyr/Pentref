package com.ywca.pentref.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

/**
 * A Poi (Point of Interest) encapsulates information about a physical location,
 * including its name, address, and other relevant information.
 */
public class Poi implements Comparable, Parcelable {
    //region Constants
    public static final String TABLE_NAME = "Poi";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_WEBSITE_URI = "website_uri";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    //endregion

    //region Instance variables
    private long id;
    private String name;
    private String description;
    private String websiteUri;
    private String address;
    private double latitude;
    private double longitude;
    //endregion

    public Poi(long id, String name, String description, String websiteUri, String address, LatLng latLng) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.websiteUri = websiteUri;
        this.address = address;
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getWebsiteUri() {
        return this.websiteUri;
    }

    public String getAddress() {
        return this.address;
    }

    public LatLng getLatLng() {
        return new LatLng(this.latitude, this.longitude);
    }

    //region Comparison methods
    @Override
    public boolean equals(Object other) {
        if (other instanceof Poi) {
            return this.id == ((Poi) other).getId();
        }
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return Long.valueOf(this.id).hashCode();
    }

    @Override
    public int compareTo(@NonNull Object other) {
        if (other instanceof Poi) {
            return Long.valueOf(this.id).compareTo(((Poi) other).getId());
        }
        return 0;
    }

    //endregion

    //region Code to implement Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeLong(this.id);
        out.writeString(this.name);
        out.writeString(this.description);
        out.writeString(this.websiteUri);
        out.writeString(this.address);
        out.writeDouble(this.latitude);
        out.writeDouble(this.longitude);
    }

    private Poi(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.description = in.readString();
        this.websiteUri = in.readString();
        this.address = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
    }

    public static final Creator<Poi> CREATOR = new Creator<Poi>() {
        @Override
        public Poi createFromParcel(Parcel in) {
            return new Poi(in);
        }

        @Override
        public Poi[] newArray(int size) {
            return new Poi[size];
        }
    };
    //endregion
}