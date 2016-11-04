package com.ywca.pentref.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

/**
 * Encapsulates the information of a transportation (bus or ferry) such as
 * price, departure station and destination station.
 */

public class Transport extends BaseObservable {
    public static final String TABLE_NAME = "Transport";
    private String routeNumber;
    private TypeEnum typeEnum;
    private float adultPrice;
    private float childPrice;
    private String departureStation;
    private String destinationStation;
    public Transport(String routeNumber, TypeEnum typeEnum, float adultPrice,
                     float childPrice, String departureStation, String destinationStation) {
        this.routeNumber = routeNumber;
        this.typeEnum = typeEnum;
        this.adultPrice = adultPrice;
        this.childPrice = childPrice;
        this.departureStation = departureStation;
        this.destinationStation = destinationStation;
    }

    public TypeEnum getTypeEnum() {
        return this.typeEnum;
    }

    @Bindable
    public float getAdultPrice() {
        return this.adultPrice;
    }

    @Bindable
    public float getChildPrice() {
        return this.childPrice;
    }

    @Bindable
    public String getDepartureStation() {
        return this.departureStation;
    }

    @Bindable
    public String getDestinationStation() {
        return this.destinationStation;
    }

    public enum TypeEnum {BUS, FERRY}
}