package com.ywca.pentref.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates a timetable for a bus/ferry, as well as a {@link LinkedHashMap} in which
 * the station is the key and the price (when a passenger boards in that station) is the value.
 */
public class Direction implements Parcelable {
    private Timetable timetable;
    private LinkedHashMap<String, Price> stationPriceMap;

    public Direction(Timetable timetable, LinkedHashMap<String, Price> stationPriceMap) {
        this.timetable = timetable;
        this.stationPriceMap = stationPriceMap;
    }

    public Timetable getTimetable() {
        return this.timetable;
    }

    public LinkedHashMap<String, Price> getStationPriceMap() {
        return this.stationPriceMap;
    }

    /**
     * Converts the station price map to a list of stations with the original sorting order.
     *
     * @return A list of stations
     */
    public List<String> getStations() {
        return new ArrayList<>(stationPriceMap.keySet());
    }

    /**
     * Returns the price object of the given station.
     *
     * @param stationName The station to look for
     * @return The price object of the given station
     */
    public Price getPrice(String stationName) {
        if (this.stationPriceMap.containsKey(stationName)) {
            return this.stationPriceMap.get(stationName);
        }
        return null;
    }

    //region Code to implement Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel destination, int flags) {
        destination.writeParcelable(this.timetable, flags);

        // Convert the station price map to two lists, and write each list to parcel
        List<String> stations = new ArrayList<>();
        List<Price> prices = new ArrayList<>();
        for (Map.Entry<String, Price> entry : this.stationPriceMap.entrySet()) {
            stations.add(entry.getKey());
            prices.add(entry.getValue());
        }
        destination.writeStringArray(stations.toArray(new String[stations.size()]));
        destination.writeParcelableArray(prices.toArray(new Price[prices.size()]), flags);
    }

    private Direction(Parcel source) {
        this.timetable = source.readParcelable(Timetable.class.getClassLoader());

        List<String> stations = new ArrayList<>();
        source.readStringList(stations);

        // Read the price array from the Parcelable object
        Parcelable[] parcelableArray = source.readParcelableArray(Price.class.getClassLoader());
        Price[] priceArray = Arrays.copyOf(parcelableArray, parcelableArray.length, Price[].class);

        // Instantiate the station price map
        if (this.stationPriceMap == null) {
            this.stationPriceMap = new LinkedHashMap<>();
        }

        // Assume the number of elements of stations and prices are the same,
        // station and price is added in pair.
        for (int index = 0; index < stations.size(); index++) {
            this.stationPriceMap.put(stations.get(index), priceArray[index]);
        }
    }

    public static final Creator<Direction> CREATOR = new Creator<Direction>() {
        @Override
        public Direction createFromParcel(Parcel source) {
            return new Direction(source);
        }

        @Override
        public Direction[] newArray(int size) {
            return new Direction[size];
        }
    };
    //endregion
}