package com.ywca.pentref.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

/**
 * Encapsulates the information of a transportation (bus or ferry) such as
 * price, departure station and destination station.
 */

public class Transport extends BaseObservable {
    public static final String TABLE_NAME = "TRANSPORTATION";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_ROUTENUM = "Routnum";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_ADULTPRICE = "adultPrice";
    public static final String COLUMN_CHILDPRICE = "childPrice";
    public static final String COLUMN_DEP_STATION = "departureStation";
    public static final String COLUMN_DES_STATION = "longitude";

    //region Instance variables
    private long id;
    private String routeNumber;
    private TypeEnum typeEnum;   //typeenum in sql???
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

    public long getId() {
        return this.id;
    }

    public TypeEnum getTypeEnum() {
        return this.typeEnum;
    }

    public String getRouteNum() {
        return this.routeNumber;
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

    public enum TypeEnum {
        BUS(0), FERRY(1);

        private int value;

        TypeEnum(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
}