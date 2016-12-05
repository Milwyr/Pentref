package com.ywca.pentref.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * Encapsulates the information of a transportation (bus or ferry) such as
 * price, departure station and destination station.
 */
public class Transport implements Comparable, Parcelable {
    //region Instance variables
    private String routeNumber;
    private TypeEnum typeEnum;
    private String nonTaiODestinationStation;
    private Price price;
    private Timetable fromTaiO;
    private Timetable toTaiO;
    //endregion

    //region Getter methods
    public TypeEnum getTypeEnum() {
        return this.typeEnum;
    }

    public String getRouteNumber() {
        return this.routeNumber;
    }

    public String getNonTaiODestinationStation() {
        return this.nonTaiODestinationStation;
    }

    public Timetable getFromTaiO() {
        return this.fromTaiO;
    }

    public Timetable getToTaiO() {
        return this.toTaiO;
    }
    //endregion

    //region Setter methods

    //endregion

    //region Comparison methods
    @Override
    public boolean equals(Object other) {
        if (other instanceof Transport) {
            return this.routeNumber.equals(((Transport) other).routeNumber);
        }
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return this.routeNumber.hashCode();
    }

    @Override
    public int compareTo(@NonNull Object other) {
        if (other instanceof Transport) {
            return this.routeNumber.compareTo(((Transport) other).routeNumber);
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
    public void writeToParcel(Parcel destination, int flags) {
        destination.writeString(this.routeNumber);
        destination.writeInt(this.typeEnum.getValue());
        destination.writeString(this.nonTaiODestinationStation);
        destination.writeParcelable(this.price, flags);
        destination.writeParcelable(this.fromTaiO, flags);
        destination.writeParcelable(this.toTaiO, flags);
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

    private Transport(Parcel source) {
        this.routeNumber = source.readString();

        int typeIndex = source.readInt();
        if (typeIndex == 0) {
            this.typeEnum = TypeEnum.BUS;
        } else {
            this.typeEnum = TypeEnum.FERRY;
        }

        this.nonTaiODestinationStation = source.readString();
        this.price = source.readParcelable(getClass().getClassLoader());
        this.fromTaiO = source.readParcelable(getClass().getClassLoader());
        this.toTaiO = source.readParcelable(getClass().getClassLoader());
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