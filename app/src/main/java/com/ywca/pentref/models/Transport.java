package com.ywca.pentref.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Encapsulates the information of a transportation (bus or ferry) such as
 * price, departure station and destination station.
 */
public class Transport implements Comparable, Parcelable {
    //region Constants
    public static final String TABLE_NAME = "TRANSPORTATION";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_ROUTE_NUMBER = "route_number";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_ADULT_PRICE = "adult_price";
    public static final String COLUMN_CHILD_PRICE = "child_price";
    public static final String COLUMN_DEPARTURE_STATION = "departure_station";
    public static final String COLUMN_DESTINATION_STATION = "destination_station";
    //endregion

    //region Instance variables
    private long id;
    private String routeNumber;
    private TypeEnum typeEnum;
    private float adultPrice;
    private float childPrice;
    private String departureStation;
    private String destinationStation;
    //endregion

    public Transport(long id, String routeNumber, TypeEnum typeEnum, float adultPrice,
                     float childPrice, String departureStation, String destinationStation) {
        this.id = id;
        this.routeNumber = routeNumber;
        this.typeEnum = typeEnum;
        this.adultPrice = adultPrice;
        this.childPrice = childPrice;
        this.departureStation = departureStation;
        this.destinationStation = destinationStation;
    }

    //region Getter methods
    public long getId() {
        return this.id;
    }

    public TypeEnum getTypeEnum() {
        return this.typeEnum;
    }

    public String getRouteNumber() {
        return this.routeNumber;
    }

    public float getAdultPrice() {
        return this.adultPrice;
    }

    public float getChildPrice() {
        return this.childPrice;
    }

    public String getDepartureStation() {
        return this.departureStation;
    }

    public String getDestinationStation() {
        return this.destinationStation;
    }
    //endregion

    //region Setter methods
    public void setAdultPrice(float adultPrice) {
        this.adultPrice = adultPrice;
    }

    public void setChildPrice(float childPrice) {
        this.childPrice = childPrice;
    }
    //endregion

    //region Comparison methods
    @Override
    public boolean equals(Object other) {
        if (other instanceof Transport) {
            return Long.valueOf(this.id).equals(((Transport) other).getId());
        }
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return Long.valueOf(this.id).hashCode();
    }

    @Override
    public int compareTo(Object other) {
        if (other instanceof Transport) {
            return Long.valueOf(this.id).compareTo(((Transport) other).getId());
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
        out.writeString(this.routeNumber);
        out.writeInt(this.typeEnum.getValue());
        out.writeFloat(this.adultPrice);
        out.writeFloat(this.childPrice);
        out.writeString(this.departureStation);
        out.writeString(this.destinationStation);
    }

    public static final Parcelable.Creator<Transport> CREATOR = new Parcelable.Creator<Transport>() {

        @Override
        public Transport createFromParcel(Parcel parcel) {
            return new Transport(parcel);
        }

        @Override
        public Transport[] newArray(int size) {
            return new Transport[size];
        }
    };

    private Transport(Parcel in) {
        this.id = in.readLong();
        this.routeNumber = in.readString();

        int typeIndex = in.readInt();
        if (typeIndex == 0) {
            this.typeEnum = TypeEnum.BUS;
        } else {
            this.typeEnum = TypeEnum.FERRY;
        }

        this.adultPrice = in.readFloat();
        this.childPrice = in.readFloat();
        this.departureStation = in.readString();
        this.destinationStation = in.readString();

    }
    //endregion

    public enum TypeEnum {
        @SerializedName("0")
        BUS(0),

        @SerializedName("1")
        FERRY(1);

        private int value;

        TypeEnum(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
}