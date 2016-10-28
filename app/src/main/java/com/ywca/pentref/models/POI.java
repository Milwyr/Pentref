package com.ywca.pentref.models;

import android.databinding.Observable;
import android.databinding.ObservableField;
import android.databinding.PropertyChangeRegistry;

/**
 * A POI (Point of Interest) encapsulates information about a physical location,
 * including its name, address, and other relevant information.
 */
public class POI implements Observable {
    public static final String TABLE_NAME = "POI";

    private PropertyChangeRegistry mOnPropertyChangedCallbacks;

    // Observable fields of a POI
    public final ObservableField<String> name = new ObservableField<>();
    public final ObservableField<String> description = new ObservableField<>();

    public POI(String name, String description) {
        mOnPropertyChangedCallbacks = new PropertyChangeRegistry();

        this.name.set(name);
        this.description.set(description);
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