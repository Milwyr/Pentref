package com.ywca.pentref.models;

import android.databinding.Observable;
import android.databinding.ObservableField;
import android.databinding.PropertyChangeRegistry;

/**
 * Encapsulates the weather information such as temperature,
 * weather condition, and other relevant information.
 */
public class Weather implements Observable {
    public static final String TABLE_NAME = "Weather";

    // Reference: https://developer.android.com/topic/libraries/data-binding/index.html
    private PropertyChangeRegistry mOnPropertyChangedCallbacks;

    // Observable fields of a POI
    public final ObservableField<String> name = new ObservableField<>();

    public Weather() {
        mOnPropertyChangedCallbacks = new PropertyChangeRegistry();
    }

    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback onPropertyChangedCallback) {
        mOnPropertyChangedCallbacks.add(onPropertyChangedCallback);
    }

    @Override
    public void removeOnPropertyChangedCallback(OnPropertyChangedCallback onPropertyChangedCallback) {
        mOnPropertyChangedCallbacks.remove(onPropertyChangedCallback);
    }
}