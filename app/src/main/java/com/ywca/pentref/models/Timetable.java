package com.ywca.pentref.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates timetables at two different types of date:
 * (Monday to Saturday) and (Sunday and public holiday).
 */
public class Timetable implements Parcelable {
    //region Fields
    // Created to parse the incoming json file using Gson
//    @SerializedName("monToSat")
//    private List<String> monToSatTimeStrings;
//    @SerializedName("sunAndPublicHoliday")
//    private List<String> sunAndPublicHolidayTimeStrings;

    @SerializedName("monToSat")
    private List<LocalTime> monToSatTimes;
    @SerializedName("sunAndPublicHoliday")
    private List<LocalTime> sunAndPublicHolidayTimes;
    //endregion

    public Timetable(List<LocalTime> monToSatTimes, List<LocalTime> sunAndPublicHolidayTimes) {
        this.monToSatTimes = monToSatTimes;
        this.sunAndPublicHolidayTimes = sunAndPublicHolidayTimes;
    }

    public List<LocalTime> getMonToSatTimes() {
        return this.monToSatTimes;
    }

    public List<LocalTime> getSunAndPublicHolidayTimes() {
        return this.sunAndPublicHolidayTimes;
    }

    private List<String> convertToStringList(List<LocalTime> localTimes) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
        List<String> timeStrings = new ArrayList<>();
        for (LocalTime time : localTimes) {
            timeStrings.add(formatter.print(time));
        }
        return timeStrings;
    }

    //region Code to implement Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel destination, int flags) {
        // Convert a list of LocalTime objects to a list of strings, and write them as Parcelable array
        List<String> monToSatTimeStrings = convertToStringList(this.monToSatTimes);
        destination.writeStringArray(monToSatTimeStrings.toArray(new String[monToSatTimeStrings.size()]));

        // Convert a list of LocalTime objects to a list of strings, and write them as Parcelable array
        List<String> sunAndPublicHolidayTimeStrings = convertToStringList(this.sunAndPublicHolidayTimes);
        destination.writeStringArray(sunAndPublicHolidayTimeStrings.toArray(new String[sunAndPublicHolidayTimeStrings.size()]));
    }

    private Timetable(Parcel source) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");

        // Instantiates the two lists
        if (this.monToSatTimes == null) {
            this.monToSatTimes = new ArrayList<>();
        }
        if (this.sunAndPublicHolidayTimes == null) {
            this.sunAndPublicHolidayTimes = new ArrayList<>();
        }

        // Read a list of Monday to Saturday times in string format, and convert them back to LocalTime objects
        List<String> monToSatTimeStrings = new ArrayList<>();
        source.readStringList(monToSatTimeStrings);
        for (String s : monToSatTimeStrings) {
            this.monToSatTimes.add(formatter.parseLocalTime(s));
        }

        // Read a list of Sunday and public holiday times in string format, and convert them back to LocalTime objects
        List<String> sunAndPublicHolidayTimeStrings = new ArrayList<>();
        source.readStringList(sunAndPublicHolidayTimeStrings);
        for (String s : sunAndPublicHolidayTimeStrings) {
            this.sunAndPublicHolidayTimes.add(formatter.parseLocalTime(s));
        }
    }

    public static final Creator<Timetable> CREATOR = new Creator<Timetable>() {
        @Override
        public Timetable createFromParcel(Parcel source) {
            return new Timetable(source);
        }

        @Override
        public Timetable[] newArray(int size) {
            return new Timetable[size];
        }
    };
    //endregion
}