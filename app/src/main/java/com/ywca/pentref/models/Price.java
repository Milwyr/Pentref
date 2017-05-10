package com.ywca.pentref.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Includes a child price and an adult price.
 */
public class Price implements Parcelable {
    public static final Creator<Price> CREATOR = new Creator<Price>() {
        @Override
        public Price createFromParcel(Parcel source) {
            return new Price(source);
        }

        @Override
        public Price[] newArray(int size) {
            return new Price[size];
        }
    };
    //region Fields
    private double childPrice;
    //endregion
    private double adultPrice;

    public Price() {
    }

    private Price(Parcel source) {
        this.childPrice = source.readDouble();
        this.adultPrice = source.readDouble();
    }
    //end

    //getter function
    public double getChildPrice() {
        return childPrice;
    }

    public double getAdultPrice() {
        return adultPrice;
    }
    //endregion

    //region Comparison methods
    @Override
    public boolean equals(Object other) {
        if (other instanceof Price) {
            Price otherPrice = (Price) other;
            double epsilon = 0.000001;

            return Math.abs(this.childPrice - otherPrice.childPrice) < epsilon &&
                    Math.abs(this.adultPrice - otherPrice.adultPrice) < epsilon;
        }
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return Double.valueOf(this.childPrice).hashCode() * Double.valueOf(this.adultPrice).hashCode();
    }

    //region Code to implement Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel destination, int flags) {
        destination.writeDouble(this.childPrice);
        destination.writeDouble(this.adultPrice);
    }
    //endregion
}