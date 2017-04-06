package com.ywca.pentref.activities;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.AsyncTask;
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
import android.widget.RadioGroup;

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
import com.ywca.pentref.models.Transport;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * The tutorial only appears to the user if the app is installed for the first time.
 * When this Activity is active, it downloads data from server via json files.
 */
public class LaunchingActivity extends BaseActivity {

    private final String PREF_KEY_IS_FIRST_TIME_INSTALLED = Utility.PREF_KEY_IS_FIRST_TIME_INSTALLED;

    private SharedPreferences mSharedPreferences;

    private boolean mArePoiCategoriesDownloaded = false;
    private boolean mArePoisDownloaded = false;
    private boolean mIsScheduleFileDownloaded = false;
    private FirebaseDatabase mDatabase;
    private String TAG = "FirebaseLaunch";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read this flag stored in SplashScreenActivity as it is more efficient
        // than reading from shared preferences
        boolean isFirstTimeInstalled = getIntent().getBooleanExtra(PREF_KEY_IS_FIRST_TIME_INSTALLED, true);

        //Try out firebase : get list of poi from firebase
        mDatabase = FirebaseDatabase.getInstance();




        // Show this activity to user if this app is installed for the first time
        if (isFirstTimeInstalled) {
            if (isConnectedToInternet()) {
                // Hide action bar and set content view
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }
                setContentView(R.layout.activity_tutorial);

                mSharedPreferences = getSharedPreferences(getResources().getString(R.string.pref_file_name_local), MODE_PRIVATE);

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
            // Navigate to ChoosePageActivity if the app has been launched before
            startActivity(new Intent(this, ChoosePageActivity.class));
            finish();
        }
    }

    // Download Points of Interest, categories and transport schedule from server via json files
    private void downloadDataFromServer() {
        RequestQueue queue = Volley.newRequestQueue(this);

        //Read all Points of Interest from the firebase and add them to SQLite database
        DatabaseReference poiRef = mDatabase.getReference("POI");
        poiRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //dataSnapshot should contains a list of poi
                for (DataSnapshot poiSnapshot: dataSnapshot.getChildren()) {
                    // TODO: handle the poiSnapshot
                    //try read one first
                    String test = (String) poiSnapshot.child("name").getValue();
//                    Log.i(TAG,test);
                    //try firebase getvalue function
                    Poi poi = poiSnapshot.getValue(Poi.class);
                    poi.setId(poiSnapshot.getKey());
                    int i = 5;
                    ContentValues values = PentrefProvider.getContentValues(poi);
                    try {
                        getContentResolver().insert(Contract.Poi.CONTENT_URI, values);
                        mArePoisDownloaded = true;
                        updateIsFirstTimeInstalledFlag();
                    } catch (Exception e) {
                        Log.e("TutorialActivity:poi", e.getMessage());
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG,databaseError.getMessage());
                int j = 0;
            }
        });

        /*// Read all Points of Interest from the server and add them to SQLite database
        String poiUrl = Utility.SERVER_URL + "/PostReq.php?Method=GET&PATH=pois";
        JsonArrayRequest poiJsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, poiUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                *//*try {
                    response.get(0).toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }*//*

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
                        Log.e("TutorialActivity:poi", e.getMessage());
                    }

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error != null) {
                    Log.e("TutorialActivity", error.getMessage());
                }
            }
        });
        queue.add(poiJsonArrayRequest);


*/
        //Read all poiCategories from firebase
        DatabaseReference categoryRef = mDatabase.getReference("Categories");
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //dataSnapshot containes a list of categories\
                Log.i("Categoriesfirebase","hi");
                for(DataSnapshot categoryData : dataSnapshot.getChildren()){
                    int k = 0;
                    Category category = categoryData.getValue(Category.class);
                    String test = category.getName();
                    ContentValues values = new ContentValues();
                    values.put(Contract.Category._ID, category.getId());
                    values.put(Contract.Category.COLUMN_NAME, category.getName());

                    try {
                        getContentResolver().insert(Contract.Category.CONTENT_URI, values);
                        mArePoiCategoriesDownloaded = true;
                        updateIsFirstTimeInstalledFlag();
                    } catch (Exception e) {
                        Log.e("TutorialActivity:cat", e.getMessage());
                    }
                    Log.i(TAG,test);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String test = databaseError.getMessage();
                Log.i("CategoryFirebase",databaseError.getMessage());
                int j = 0;
            }
        });
       /* // Read all Point of Interest categories from the server and add them to SQLite database
        String poiCategoriesUrl = Utility.SERVER_URL + "/PostReq.php?Method=GET&PATH=poi_categories";
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
                        Log.e("TutorialActivity:cat", e.getMessage());
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error != null) {
                    Log.e("TutorialActivity", error.getMessage());
                }
            }
        });
        queue.add(poiCategoryArrayRequest);*/

        //Get the teansports from firebase
        DatabaseReference transportRef = mDatabase.getReference("Transport");
        transportRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               String test =  dataSnapshot.toString();
                int i = 0 ;

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("FireTrans",databaseError.getMessage());
            }
        });

        // Fetch the transports json on the server and save it to a local json file
        String transportUrl = Utility.SERVER_URL + "/PostReq.php?Method=GET&PATH=transport_schedule";
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
                    Log.e("TutorialActivity:trans", e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error != null) {
                    Log.e("TutorialActivity", error.getMessage());
                }
            }
        });
        queue.add(transportJsonArrayRequest);
    }

    // Save the app is not installed for the first time in SharedPreferences
    // if all data has been stored from server
    private void updateIsFirstTimeInstalledFlag() {
        boolean isDownloadFinished = mArePoisDownloaded &&
                mArePoiCategoriesDownloaded && mIsScheduleFileDownloaded;
        if (isDownloadFinished) {
            mSharedPreferences.edit().putBoolean(PREF_KEY_IS_FIRST_TIME_INSTALLED, false).apply();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements View.OnClickListener {

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
            Button loginButton = (Button) rootView.findViewById(R.id.tutorial_login_button);
            Button getStartedButton = (Button) rootView.findViewById(R.id.tutorial_get_started_button);

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

                        loginButton.setVisibility(View.VISIBLE);
                        loginButton.setOnClickListener(this);

                        getStartedButton.setVisibility(View.VISIBLE);
                        getStartedButton.setOnClickListener(this);
                        break;
                }
            }

            return rootView;
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.tutorial_login_button:
                    // Navigate to settings fragment (index 4 indicates settings fragment)
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.putExtra(Utility.FRAGMENT_INDEX_EXTRA_KEY, 4);
                    startActivity(intent);
                    getActivity().finish();
                    break;
                case R.id.tutorial_get_started_button:
                    startActivity(new Intent(getActivity(), ChoosePageActivity.class));
                    getActivity().finish();
                    break;
            }
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