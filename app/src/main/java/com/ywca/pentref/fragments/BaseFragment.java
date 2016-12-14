package com.ywca.pentref.fragments;

import android.app.Fragment;
import android.os.Build;

import java.util.Locale;

/**
 * Provides methods that are used in several Fragments.
 */
public abstract class BaseFragment extends Fragment {
    /**
     * @return The current locale settings of the device
     */
    protected Locale getDeviceLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            return getResources().getConfiguration().locale;
        }
    }
}