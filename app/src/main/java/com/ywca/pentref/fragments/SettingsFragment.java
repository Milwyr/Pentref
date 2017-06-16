package com.ywca.pentref.fragments;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.ywca.pentref.R;
import com.ywca.pentref.common.Category;
import com.ywca.pentref.common.Contract;
import com.ywca.pentref.common.PentrefProvider;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.models.Poi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Displays options for the user to choose and saves user's preferences.
 */
public class SettingsFragment extends PreferenceFragment implements
        Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    private static final String TAG = "SettingsFragment";
    private ProgressDialog mProgressDialog;
    private ListPreference mNotificationPreference;
    private SharedPreferences mSharedPreferences;
    private FirebaseDatabase mDatabase;

    private boolean mIsPoiDownloaded;
    private boolean mIsCategoryDownloaded;
    private boolean mIsTransportDownloaded;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        mDatabase = FirebaseDatabase.getInstance();

        mSharedPreferences = getActivity()
                .getSharedPreferences(getResources().getString(R.string.pref_file_name_user_settings), Context.MODE_PRIVATE);

        Preference synchroniseWithServerPreference = findPreference("synchronise_with_server");
        synchroniseWithServerPreference.setOnPreferenceClickListener(this);

        // Add listener to notification preference, and update the summary
        mNotificationPreference = (ListPreference)
                findPreference(Utility.PREF_KEY_NOTIFICATION_PREFERENCE);
        mNotificationPreference.setOnPreferenceChangeListener(this);
        updateNotificationPreferenceSummary();


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

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            downloadDataFromServer();
                        }
                    }.execute();


                    return true;
                } else {
                    if (getView() != null) {
                        Snackbar.make(getView(), R.string.error_network_unavailable, Snackbar.LENGTH_LONG).show();
                    }
                }
        }

        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        if (preference.getKey().equals(Utility.PREF_KEY_NOTIFICATION_PREFERENCE)) {
            // Save the selected value for notification preference,
            // i.e. (how many minutes notification is displayed before departure of transportation)
            mSharedPreferences.edit().putInt(
                    Utility.PREF_KEY_NOTIFICATION_PREFERENCE, Integer.valueOf(value.toString())).apply();
            updateNotificationPreferenceSummary();
            return true;
        }
        return false;
    }

    // Downloads Points of Interest, categories and transport schedule from server via json files
    private void downloadDataFromServer() {
        //Read all Points of Interest from the firebase and add them to SQLite database
        DatabaseReference poiRef = mDatabase.getReference("POI");
        poiRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //dataSnapshot should contains a list of poi
                for (DataSnapshot poiSnapshot : dataSnapshot.getChildren()) {
                    // TODO: handle the poiSnapshot
                    //try firebase getvalue function
                    Poi poi = poiSnapshot.getValue(Poi.class);
                    poi.setId(poiSnapshot.getKey());
                    ContentValues values = PentrefProvider.getContentValues(poi);
                    try {
                        getActivity().getContentResolver().insert(Contract.Poi.CONTENT_URI, values);
                        mIsPoiDownloaded = true;
                    } catch (Exception e) {
                        Log.e("TutorialActivity:poi", e.getMessage());
                    }
                }
                dismissDialogIfFinished();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                mIsPoiDownloaded = true;
                dismissDialogIfFinished();
                Toast.makeText(getActivity(), "POI download canceled", Toast.LENGTH_SHORT).show();
                Log.d(TAG, databaseError.getMessage());
            }
        });

        //Read all poiCategories from firebase
        DatabaseReference categoryRef = mDatabase.getReference("Categories");
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //dataSnapshot containes a list of categories\

                for (DataSnapshot categoryData : dataSnapshot.getChildren()) {
                    int k = 0;
                    Category category = categoryData.getValue(Category.class);
                    String test = category.getName();
                    ContentValues values = new ContentValues();
                    values.put(Contract.Category._ID, category.getId());
                    values.put(Contract.Category.COLUMN_NAME, category.getName());

                    try {
                        getActivity().getContentResolver().insert(Contract.Category.CONTENT_URI, values);
                        mIsCategoryDownloaded = true;
                    } catch (Exception e) {
                        Log.e("TutorialActivity:cat", e.getMessage());
                    }
                }
                dismissDialogIfFinished();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("CategoryFirebase", databaseError.getMessage());
                mIsCategoryDownloaded  = true;
                dismissDialogIfFinished();
                Toast.makeText(getActivity(), "Categories canceled", Toast.LENGTH_SHORT).show();
            }
        });

        //Get the teansports from firebase
        DatabaseReference transportRef = mDatabase.getReference("Transport");
        transportRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                JSONArray transJson = new JSONArray();

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Map<String, String> value = (Map<String, String>) data.getValue();
                    transJson.put(new JSONObject(value));
                }
                int i = 0;

                File transportsFile = new File(getActivity().getFilesDir(), Utility.TRANSPORTATION_JSON_FILE_NAME);
                if (transportsFile.exists()) {
                    transportsFile.delete();
                }
                // Create the transportation json file, and write the response json array
                // that is read from server to the newly created local json file
                try {
                    boolean isFileCreated = transportsFile.createNewFile();
                    if (isFileCreated) {
                        FileWriter fileWriter = new FileWriter(transportsFile);
                        fileWriter.write(transJson.toString());
                        fileWriter.flush();
                        fileWriter.close();
                        mIsTransportDownloaded = true;
                        dismissDialogIfFinished();
                    }
                } catch (IOException e) {
                    mIsTransportDownloaded = true;
                    dismissDialogIfFinished();
                    Toast.makeText(getActivity(), "Unable to write transport schedule", Toast.LENGTH_SHORT).show();
                    Log.e("TutorialActivity:trans", e.getMessage());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(),"Tansport download canceled",Toast.LENGTH_SHORT).show();
                mIsTransportDownloaded = true;
                dismissDialogIfFinished();
                Log.i("FireTrans", databaseError.getMessage());
            }
        });

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

    private void updateNotificationPreferenceSummary() {
        int minutes = mSharedPreferences.getInt(Utility.PREF_KEY_NOTIFICATION_PREFERENCE, 30);
        mNotificationPreference.setSummary(String.format(getResources().getString(R.string.pref_notification_summary), minutes));
    }

    // Called to inform the app that a download process is complete
    private interface VolleyFinishCallback {
        void onPoiFinish();

        void onCategoryFinish();

        void onTransportFinish();
    }
}