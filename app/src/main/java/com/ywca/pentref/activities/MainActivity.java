package com.ywca.pentref.activities;

import android.app.Fragment;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ywca.pentref.R;
import com.ywca.pentref.common.Contract;
import com.ywca.pentref.common.PentrefProvider;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.fragments.BookmarksFragment;
import com.ywca.pentref.fragments.DiscoverFragment;
import com.ywca.pentref.fragments.SettingsFragment;
import com.ywca.pentref.fragments.SignInFragment;
import com.ywca.pentref.fragments.TransportationFragment;
import com.ywca.pentref.fragments.WeatherFragment;
import com.ywca.pentref.models.Poi;
import com.ywca.pentref.models.Transport;

import org.joda.time.LocalTime;
import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // The search view that is inflated as a menu item on the Action Bar
    private MenuItem mActionSearchMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialiseComponents();

        //TODO: Only execute this method when the items have not been added in the local database
        if (isOnline()) {
            fetchJsonFromServer();
        }

        // Display the discover fragment only when the app launches as
        // savedInstanceState != null when orientation changes
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(
                    R.id.frame, DiscoverFragment.newInstance("")).commit();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu with a search view
        getMenuInflater().inflate(R.menu.main, menu);
        mActionSearchMenuItem = menu.findItem(R.id.action_search);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) mActionSearchMenuItem.getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    /**
     * This event is fired by the search view on the Action Bar, i.e. either
     * the user clicks search or the user selects an item from the suggestion list.
     *
     * @param intent Incoming intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    // Handles the event from method onNewIntent()
    private void handleIntent(Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //TODO: use the query to search your data somehow
        } else if (intent.getAction().equals(Intent.ACTION_VIEW)) {
            // Handle a click event on the suggestion list
            final Uri uri = intent.getData();
            if (uri != null) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();

                        // Collapse the search view on the Action Bar after
                        // on the suggestion list is clicked
                        mActionSearchMenuItem.collapseActionView();
                    }

                    @Override
                    protected Void doInBackground(Void... params) {
                        // Read from local database and get the cursor with the POI data
                        // specified by the uri
                        Cursor cursor = getContentResolver().query(
                                uri, Contract.Poi.PROJECTION_ALL, null, null, null);

                        if (cursor == null) {
                            return null;
                        }

                        // Retrieve the Point of Interest from the cursor
                        Poi selectedPoi = PentrefProvider.convertToPois(cursor).get(0);
                        cursor.close();

                        // Launch POIDetailsActivity
                        Intent poiDetailsIntent = new Intent(MainActivity.this, PoiDetailsActivity.class);
                        poiDetailsIntent.putExtra(Utility.SELECTED_POI_EXTRA_KEY, selectedPoi);
                        startActivity(poiDetailsIntent);

                        return null;
                    }
                }.execute();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_discover:
                changeFragment(R.string.discover, new DiscoverFragment());
                break;
            case R.id.nav_bookmarks:
                changeFragment(R.string.bookmarks, new BookmarksFragment());
                break;
            case R.id.nav_weather:
                changeFragment(R.string.weather, new WeatherFragment());
                break;
            case R.id.nav_transportation:
                changeFragment(R.string.transportation, new TransportationFragment());
                break;
            case R.id.nav_login:
                changeFragment(R.string.transportation, new SignInFragment());
                break;
            case R.id.nav_settings:
                changeFragment(R.string.settings, new SettingsFragment());
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Changes the title to the given string resource and
    // replaces the current fragment by the given one.
    private void changeFragment(int resourceId, Fragment fragment) {
        setTitle(resourceId);

        getFragmentManager().beginTransaction().replace(R.id.frame, fragment).commit();

        // The search view is only visible when the current fragment is discover fragment
        if (fragment instanceof DiscoverFragment) {
            mActionSearchMenuItem.setVisible(true);
        } else {
            mActionSearchMenuItem.setVisible(false);
        }
    }

    // Initialises components when onCreate() method is called
    private void initialiseComponents() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Set the first item (Discover) to be checked by default
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    private void fetchJsonFromServer() {
        RequestQueue queue = Volley.newRequestQueue(this);

        String poiUrl = "https://raw.githubusercontent.com/Milwyr/Temporary/master/pois.json";
        String transportUrl = "https://raw.githubusercontent.com/Milwyr/Temporary/master/transports.json";

        // Read all Points of Interest from the server and add them to SQLite database
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
                    } catch (Exception e) {
                        Log.e("MainActivity", e.getMessage());
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("MainActivity", error.getMessage());
            }
        });

        // Fetch the transports json on the server and save it to a local json file
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
                    }
                } catch (IOException e) {
                    Log.e("MainActivity", e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("MainActivity", error.getMessage());
            }
        });

        queue.add(poiJsonArrayRequest);
        queue.add(transportJsonArrayRequest);
    }
}