package com.ywca.pentref.models;

import java.util.Locale;

/**
 * Created by Ronald on 2017/6/16.
 */

public class Tour {
    private String mTourName;
    private String mTourChineseName;
    private String mImagePath;
    //Date Should follow the ISO standard
    private String mStartingDateTime;
    private double mLength;
    private String mDescription;
    private String mChineseDescription;
    private String mContactNo;

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

    

}
