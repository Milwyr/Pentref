package com.ywca.pentref.fragments;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.ywca.pentref.R;
import com.ywca.pentref.activities.BaseActivity;
import com.ywca.pentref.common.Category;
import com.ywca.pentref.common.Contract;
import com.ywca.pentref.common.PentrefProvider;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.models.Poi;

import org.json.JSONArray;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Displays options for the user to choose and saves user's preferences.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    private ProgressDialog mProgressDialog;
    private VolleyFinishCallback mVolleyFinishCallback;

    private boolean mIsPoiDownloaded;
    private boolean mIsCategoryDownloaded;
    private boolean mIsTransportDownloaded;

    private interface VolleyFinishCallback {
        void onPoiFinish();

        void onCategoryFinish();

        void onTransportFinish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference synchroniseWithServerPreference = findPreference("synchronise_with_server");
        synchroniseWithServerPreference.setOnPreferenceClickListener(this);

        mVolleyFinishCallback = new VolleyFinishCallback() {
            @Override
            public void onPoiFinish() {
                mIsPoiDownloaded = true;
                dismissDialogIfFinished();
            }

            @Override
            public void onCategoryFinish() {
                mIsCategoryDownloaded = true;
                dismissDialogIfFinished();
            }

            @Override
            public void onTransportFinish() {
                mIsTransportDownloaded = true;
                dismissDialogIfFinished();
            }
        };
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case "synchronise_with_server":
                if (isConnectedToInternet()) {
                    // Initialise the three boolean values
                    mIsPoiDownloaded = false;
                    mIsCategoryDownloaded = false;
                    mIsTransportDownloaded = false;

                    // Show progress dialog
                    if (mProgressDialog == null) {
                        mProgressDialog = new ProgressDialog(getActivity());
                        mProgressDialog.setTitle(getResources().getString(R.string.loading));
                        mProgressDialog.setMessage(getResources().getString(R.string.dialog_message_synchronising));
                    }
                    mProgressDialog.show();

                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            // Remove all records from the database
                            ContentResolver contentResolver = getActivity().getContentResolver();
                            contentResolver.delete(Contract.Poi.CONTENT_URI, null, null);
                            contentResolver.delete(Contract.Category.CONTENT_URI, null, null);
                            return null;
                        }
                    }.execute();

                    downloadDataFromServer();

                    return true;
                } else {
                    if (getView() != null) {
                        Snackbar.make(getView(), R.string.error_network_unavailable, Snackbar.LENGTH_LONG).show();
                    }
                }
        }

        return false;
    }

    // Downloads Points of Interest, categories and transport schedule from server via json files
    private void downloadDataFromServer() {
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        // Read all Points of Interest from the server and add them to SQLite database
        String poiUrl = Utility.SERVER_URL + "/pois.json";
        JsonArrayRequest poiJsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, poiUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                // Parse the response array into a list of Points of Interest
                Gson gson = new Gson();
                List<Poi> pois = Arrays.asList(gson.fromJson(response.toString(), Poi[].class));

                // Insert the pois into the local database
                for (final Poi poi : pois) {
                    ContentValues values = PentrefProvider.getContentValues(poi);

                    try {
                        getActivity().getContentResolver().insert(Contract.Poi.CONTENT_URI, values);
                        mVolleyFinishCallback.onPoiFinish();
                    } catch (Exception e) {
                        Log.e("SettingsFragment", e.getMessage());
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error != null) {
                    Log.e("SettingsFragment", error.getMessage());
                }
            }
        });
        queue.add(poiJsonArrayRequest);

        // Read all Point of Interest categories from the server and add them to SQLite database
        String poiCategoriesUrl = Utility.SERVER_URL + "/poi_categories.json";
        JsonArrayRequest poiCategoryArrayRequest = new JsonArrayRequest(
                Request.Method.GET, poiCategoriesUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Gson gson = new Gson();
                List<Category> categories = Arrays.asList(gson.fromJson(response.toString(), Category[].class));

                for (Category item : categories) {
                    ContentValues values = new ContentValues();
                    values.put(Contract.Category._ID, item.getId());
                    values.put(Contract.Category.COLUMN_NAME, item.getName());

                    try {
                        getActivity().getContentResolver().insert(Contract.Category.CONTENT_URI, values);
                        mVolleyFinishCallback.onCategoryFinish();
                    } catch (Exception e) {
                        Log.e("SettingsFragment", e.getMessage());
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error != null) {
                    Log.e("SettingsFragment", error.getMessage());
                }
            }
        });
        queue.add(poiCategoryArrayRequest);

        // Fetch the transports json on the server and save it to a local json file
        String transportUrl = Utility.SERVER_URL + "/transport_schedule.json";
        JsonArrayRequest transportJsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, transportUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                // Delete the transportation json file if it has been stored locally before
                File transportsFile = new File(
                        getActivity().getFilesDir(), Utility.TRANSPORTATION_JSON_FILE_NAME);
                if (transportsFile.exists()) {
                    transportsFile.delete();
                }

                // Create the transportation json file, and write the response json array
                // that is read from server to the newly created local json file
                try {
                    boolean isFileCreated = transportsFile.createNewFile();
                    if (isFileCreated) {
                        FileWriter fileWriter = new FileWriter(transportsFile);
                        fileWriter.write(response.toString());
                        fileWriter.flush();
                        fileWriter.close();
                        mVolleyFinishCallback.onTransportFinish();
                    }
                } catch (IOException e) {
                    Log.e("SettingsFragment", e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error != null) {
                    Log.e("SettingsFragment", error.getMessage());
                }
            }
        });
        queue.add(transportJsonArrayRequest);
    }

    private void dismissDialogIfFinished() {
        if (mIsPoiDownloaded && mIsCategoryDownloaded && mIsTransportDownloaded) {
            mProgressDialog.dismiss();
        }
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager manager = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}