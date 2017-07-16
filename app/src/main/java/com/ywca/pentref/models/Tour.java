package com.ywca.pentref.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;

/**
 * Created by Ronald on 2017/6/16.
 */

public class Tour implements Parcelable {
    private String mTourName;
    private String mTourChineseName;
    private String mImagePath;
    //Date Should follow the ISO standard
    private String mStartingDateTime;
    private double mLength;
    private String mDescription;
    private String mChineseDescription;
    private String mContactNo;

    public static final Parcelable.Creator<Tour> CREATOR = new Creator<Tour>() {
        @Override
        public Tour createFromParcel(Parcel source) {
            return new Tour(source);
        }

        @Override
        public Tour[] newArray(int size) {
            return new Tour[size];
        }
    };

    public Tour(String tourName, String tourChineseName, String imagePath, String StartingDateTime
    , double length, String Description, String chineseDescription, String contactNo){
        mTourName = tourName;
        mTourChineseName = tourChineseName;
        mImagePath = imagePath;
        mStartingDateTime = StartingDateTime;
        mLength = length;
        mDescription = Description;
        mChineseDescription = chineseDescription;
        mContactNo = contactNo;
    }
    //for parceable
    public Tour(Parcel source){
        mTourName = source.readString();
        mTourChineseName = source.readString();
        mImagePath = source.readString();
        mStartingDateTime = source.readString();
        mLength = source.readDouble();
        mDescription = source.readString();
        mChineseDescription = source.readString();
        mContactNo = source.readString();
    }

    public Tour(){
        //For firebase
    }
    public String getTourName(Locale locale){
        if(locale.getLanguage().equals("zh")){
            return mTourChineseName;
        }else{
            return mTourName;
        }
    }

    public String getTourName(){
        return mTourName;
    }
    public String getTourChineseName(){
        return mTourChineseName;
    }
    public String getImagePath(){
        return mImagePath;
    }
    public String getStartingDateTime(){
        return mStartingDateTime;
    }
    public double getLength(){
        return mLength;
    }
    public String getDescription(Locale locale){
        if(locale.getLanguage().equals("zh")){
            return mChineseDescription;
        }else{
            return mDescription;
        }
    }
    public String getDescription(){
        return mDescription;
    }
    public String getChineseDescription(){
        return mChineseDescription;
    }
    public String getContactNo(){
        return mContactNo;
    }

    //region Code to implement Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTourName);
        dest.writeString(mTourChineseName);
        dest.writeString(mImagePath);
        dest.writeString(mStartingDateTime);
        dest.writeDouble(mLength);
        dest.writeString(mDescription);
        dest.writeString(mChineseDescription);
        dest.writeString(mContactNo);
    }
    ///end region
}
