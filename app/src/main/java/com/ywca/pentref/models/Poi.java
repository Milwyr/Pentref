package com.ywca.pentref.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.Observable;
import android.databinding.ObservableField;
import android.databinding.PropertyChangeRegistry;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;

/**
 * A POI (Point of Interest) encapsulates information about a physical location,
 * including its name, address, and other relevant information.
 */
public class Poi extends BaseObservable {
    //region Constants
    public static final String TABLE_NAME = "POI";
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

    @Bindable
    public String getName() {
        return this.name;
    }

    @Bindable
    public String getDescription() {
        return this.description;
    }

    @Bindable
    public String getWebsiteUri() {
        return this.websiteUri;
    }

    @Bindable
    public String getAddress() {
        return this.address;
    }

    public LatLng getLatLng() {
        return new LatLng(this.latitude, this.longitude);
    }
}
