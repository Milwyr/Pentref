package com.ywca.pentref.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.ywca.pentref.R;

/**
 * Created by Milton on 28/10/2016.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}