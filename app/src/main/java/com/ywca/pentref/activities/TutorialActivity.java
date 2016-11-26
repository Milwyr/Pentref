package com.ywca.pentref.activities;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.ywca.pentref.R;
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
 * Only appears to the user if the app is installed for the first time.
 * When this Activity is active, it downloads data from server via json files.
 */
public class TutorialActivity extends BaseActivity {

    private final String PREF_KEY_IS_FIRST_TIME_INSTALLED = "IsFirstTimeInstalled";

    private SharedPreferences mSharedPreferences;

    private boolean mArePoiCategoriesDownloaded = false;
    private boolean mArePoisDownloaded = false;
    private boolean mIsScheduleFileDownloaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = getSharedPreferences(getResources().getString(R.string.pref_file_name_local), MODE_PRIVATE);

        // Show this activity to user if this app is installed for the first time
        if (mSharedPreferences.getBoolean(PREF_KEY_IS_FIRST_TIME_INSTALLED, true)) {
            if (isConnectedToInternet()) {
                // Hide action bar and set content view
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }
                setContentView(R.layout.activity_tutorial);

                downloadDataFromServer();

                // The adapter returns a fragment for each of the three sections
                SectionsPagerAdapter sectionsPagerAdapter =
                        new SectionsPagerAdapter(getSupportFragmentManager());

                // Set up the ViewPager with the sections adapter
                ViewPager viewPager = (ViewPager) findViewById(R.id.container);
                viewPager.setAdapter(sectionsPagerAdapter);
            } else {
                // Display a dialog box to remind the user to connect to the internet
                new AlertDialog.Builder(this)
                        .setMessage(R.string.error_connect_to_internet_to_initialise)
                        .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                finish();
                            }
                        }).show();
            }
        } else {
            // Navigate to MainActivity if the app has been launched before
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    // Download Points of Interest, categories and transport schedule from server via json files
    private void downloadDataFromServer() {
        RequestQueue queue = Volley.newRequestQueue(this);

        // Server root directory
        String baseUrl = "http://comp4521p1.cse.ust.hk/";

        // Read all Points of Interest from the server and add them to SQLite database
        String poiUrl = baseUrl + "pois.json";
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
                        getContentResolver().insert(Contract.Poi.CONTENT_URI, values);
                        mArePoisDownloaded = true;
                        updateIsFirstTimeInstalledFlag();
                    } catch (Exception e) {
                        Log.e("TutorialActivity", e.getMessage());
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TutorialActivity", error.getMessage());
            }
        });
        queue.add(poiJsonArrayRequest);

        // Read all Point of Interest categories from the server and add them to SQLite database
        String poiCategoriesUrl = baseUrl + "poi_categories.json";
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
                        getContentResolver().insert(Contract.Category.CONTENT_URI, values);
                        mArePoiCategoriesDownloaded = true;
                        updateIsFirstTimeInstalledFlag();
                    } catch (Exception e) {
                        Log.e("TutorialActivity", e.getMessage());
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(poiCategoryArrayRequest);

        // Fetch the transports json on the server and save it to a local json file
        String transportUrl = baseUrl + "transport_schedule.json";
        JsonArrayRequest transportJsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, transportUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                // Terminate if the transportation json file has been stored locally before
                File transportsFile = new File(getFilesDir(), Utility.TRANSPORTATION_JSON_FILE_NAME);
                if (transportsFile.exists()) {
                    return;
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
                        mIsScheduleFileDownloaded = true;
                        updateIsFirstTimeInstalledFlag();
                    }
                } catch (IOException e) {
                    Log.e("TutorialActivity", e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TutorialActivity", error.getMessage());
            }
        });
        queue.add(transportJsonArrayRequest);
    }

    // Save the app is not installed for the first time in SharedPreferences
    // if all data has been stored from server
    private void updateIsFirstTimeInstalledFlag() {
        boolean isFirstTimeInstalled = mArePoisDownloaded &&
                mArePoiCategoriesDownloaded && mIsScheduleFileDownloaded;
        if (isFirstTimeInstalled) {
            mSharedPreferences.edit().putBoolean(PREF_KEY_IS_FIRST_TIME_INSTALLED, false).apply();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        // The argument representing the section number for this fragment
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_tutorial, container, false);
            ImageView imageView = (ImageView) rootView.findViewById(R.id.tutorial_image_view);
            RadioGroup radioGroup = (RadioGroup) rootView.findViewById(R.id.radio_group);
            Button getStartedButton = (Button) rootView.findViewById(R.id.tutorial_button);

            if (getArguments() != null) {
                switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                    case 1:
                        imageView.setImageResource(R.drawable.tutorial_section_1);
                        radioGroup.check(R.id.radio_button_1);
                        break;
                    case 2:
                        imageView.setImageResource(R.drawable.ic_person_black);
                        radioGroup.check(R.id.radio_button_2);
                        break;
                    case 3:
                        imageView.setImageResource(R.drawable.ic_bus_black_36dp);
                        radioGroup.check(R.id.radio_button_3);

                        getStartedButton.setVisibility(View.VISIBLE);
                        getStartedButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(getActivity(), MainActivity.class));
                            }
                        });
                        break;
                }
            }

            return rootView;
        }
    }

    /**
     * Returns a fragment corresponding to one of the three sections.
     */
    class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }
}