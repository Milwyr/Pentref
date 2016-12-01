package com.ywca.pentref.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

/**
 * A {@link Poi} (Point of Interest) encapsulates information about a physical location,
 * including its name, address, and other relevant information.
 */
public class Poi implements Comparable, Parcelable {
    //region Fields
    private long id;
    private String name;
    private String headerImageFileName;
    private int categoryId;
    private String description;
    private String websiteUri;
    private String address;
    private double latitude;
    private double longitude;
    //endregion

    public Poi(long id, String name, String headerImageFileName, int categoryId,
               String description, String websiteUri, String address, LatLng latLng) {
        this.id = id;
        this.name = name;
        this.headerImageFileName = headerImageFileName;
        this.categoryId = categoryId;
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

    public String getHeaderImageFileName() {
        return this.headerImageFileName;
    }

    public int getCategoryId() {
        return this.categoryId;
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
    public void writeToParcel(Parcel destination, int i) {
        destination.writeLong(this.id);
        destination.writeString(this.name);
        destination.writeString(this.headerImageFileName);
        destination.writeInt(this.categoryId);
        destination.writeString(this.description);
        destination.writeString(this.websiteUri);
        destination.writeString(this.address);
        destination.writeDouble(this.latitude);
        destination.writeDouble(this.longitude);
    }

    private Poi(Parcel source) {
        this.id = source.readLong();
        this.name = source.readString();
        this.headerImageFileName = source.readString();
        this.categoryId = source.readInt();
        this.description = source.readString();
        this.websiteUri = source.readString();
        this.address = source.readString();
        this.latitude = source.readDouble();
        this.longitude = source.readDouble();
    }

    public static final Creator<Poi> CREATOR = new Creator<Poi>() {
        @Override
        public Poi createFromParcel(Parcel source) {
            return new Poi(source);
        }

        @Override
        public Poi[] newArray(int size) {
            return new Poi[size];
        }
    };
    //endregion
}