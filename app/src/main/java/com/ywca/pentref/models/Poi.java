package com.ywca.pentref.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;

import java.util.Locale;

/**
 * A {@link Poi} (Point of Interest) encapsulates information about a physical location,
 * including its name, address, and other relevant information.
 */
public class Poi implements Comparable, Parcelable {
    //region Fields
    private long id;
    private String name;
    private String chineseName;
    private String headerImageFileName;
    private int categoryId;
    private String websiteUri;
    private String address;
    private String chineseAddress;
    private String phoneNumber;
    private double latitude;
    private double longitude;
    //endregion

    //for firebase
    public Poi(){}

    public Poi(DataSnapshot poiDataSnapshot){
        this.id = (long) poiDataSnapshot.child("id").getValue();
        this.name = (String) poiDataSnapshot.child("chineseName").getValue();
        this.headerImageFileName = (String) poiDataSnapshot.child("headerImageFileName").getValue();
        //this.categoryId = (long) poiDataSnapshot.child("categoryId").getValue();
        this.websiteUri = (String) poiDataSnapshot.child("websiteUri").getValue();
        this.address = (String) poiDataSnapshot.child("address").getValue();
        this.chineseAddress = (String) poiDataSnapshot.child("chineseAddress").getValue();
        this.phoneNumber = (String) poiDataSnapshot.child("phoneNumber").getValue();
        this.latitude = (double) poiDataSnapshot.child("latitude").getValue();
        this.longitude = (double) poiDataSnapshot.child("longitude").getValue();
    }

    public Poi(long id, String name, String chineseName, String headerImageFileName, int categoryId,
               String websiteUri, String address, String chineseAddress, String phoneNumber, LatLng latLng) {
        this.id = id;
        this.name = name;
        this.chineseName = chineseName;
        this.headerImageFileName = headerImageFileName;
        this.categoryId = categoryId;
        this.websiteUri = websiteUri;
        this.address = address;
        this.chineseAddress = chineseAddress;
        this.phoneNumber = phoneNumber;
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }

    public long getId() {
        return this.id;
    }

    /**
     * Returns Accessor method for the attribute name
     *
     * @param locale Current locale settings of the device
     * @return Chinese name if the locale is Chinese (i.e. zh_XX),
     * otherwise returns the name in English.
     */
    public String getName(Locale locale) {
        if (locale.getLanguage().equals("zh")) {
            return this.chineseName;
        } else {
            return this.name;
        }
    }

    public String getName(){ return this.name; }

    public String getChineseName(){ return this.chineseName; }

    public String getHeaderImageFileName() {
        return this.headerImageFileName;
    }

    public int getCategoryId() {
        return this.categoryId;
    }

    public String getWebsiteUri() {
        return this.websiteUri;
    }

    /**
     * Returns Accessor method for the attribute address
     *
     * @param locale Current locale settings of the device
     * @return Chinese address if the locale is Chinese (i.e. zh_XX),
     * otherwise returns the address in English.
     */
    public String getAddress(Locale locale) {
        if (locale.getLanguage().equals("zh")) {
            return this.chineseAddress;
        } else {
            return this.address;
        }
    }

    public String getAddress(){
        return this.address;
    }

    public String getChineseAddress(){
        return this.chineseAddress;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public LatLng getLatLng() {
        return new LatLng(this.latitude, this.longitude);
    }

    public double getLatitude(){
        return this.latitude;
    }

    public double getLongitude(){
        return this.longitude;
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
        destination.writeString(this.chineseName);
        destination.writeString(this.headerImageFileName);
        destination.writeInt(this.categoryId);
        destination.writeString(this.websiteUri);
        destination.writeString(this.address);
        destination.writeString(this.chineseAddress);
        if (this.phoneNumber == null) {
            this.phoneNumber = "";
        }
        destination.writeString(this.phoneNumber);
        destination.writeDouble(this.latitude);
        destination.writeDouble(this.longitude);
    }

    private Poi(Parcel source) {
        this.id = source.readLong();
        this.name = source.readString();
        this.chineseName = source.readString();
        this.headerImageFileName = source.readString();
        this.categoryId = source.readInt();
        this.websiteUri = source.readString();
        this.address = source.readString();
        this.chineseAddress = source.readString();
        this.phoneNumber = source.readString();
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