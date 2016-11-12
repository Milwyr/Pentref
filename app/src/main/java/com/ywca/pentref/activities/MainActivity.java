package com.ywca.pentref.activities;

import android.app.Fragment;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.ywca.pentref.R;
import com.ywca.pentref.common.Contract;
import com.ywca.pentref.common.LocalDatabaseHelper;
import com.ywca.pentref.fragments.BookmarksFragment;
import com.ywca.pentref.fragments.DiscoverFragment;
import com.ywca.pentref.fragments.SettingsFragment;
import com.ywca.pentref.fragments.SignInFragment;
import com.ywca.pentref.fragments.TransportationFragment;
import com.ywca.pentref.fragments.WeatherFragment;
import com.ywca.pentref.models.Poi;
import com.ywca.pentref.models.Transport;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private LocalDatabaseHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialiseComponents();
        fetchJsonFromServer();

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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_discover:
                changeFragment(R.string.discover, DiscoverFragment.newInstance(""));
                break;
            case R.id.nav_bookmarks:
                changeFragment(R.string.bookmarks, BookmarksFragment.newInstance(""));
                break;
            case R.id.nav_weather:
                changeFragment(R.string.weather, WeatherFragment.newInstance(""));
                break;
            case R.id.nav_transportation:
                changeFragment(R.string.transportation, TransportationFragment.newInstance(""));
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

//        mDbHelper = LocalDatabaseHelper.getInstance(this);
    }

    private void fetchJsonFromServer() {
        RequestQueue queue = Volley.newRequestQueue(this);

        String poiUrl = "https://raw.githubusercontent.com/Milwyr/Temporary/master/pois.json";
        String transportUrl = "https://raw.githubusercontent.com/Milwyr/Temporary/master/transports.json";

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                Cursor cursor = getContentResolver().query(Contract.Poi.CONTENT_URI, Contract.Poi.PROJECTION_ALL, null, null, null);
                List<Poi> pois = new ArrayList<>();

                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    long id = cursor.getLong(cursor.getColumnIndex(Contract.Poi.COLUMN_ID));
                    String name = cursor.getString(cursor.getColumnIndex(Contract.Poi.COLUMN_NAME));
                    String description = cursor.getString(cursor.getColumnIndex(Contract.Poi.COLUMN_DESCRIPTION));
                    String websiteUri = cursor.getString(cursor.getColumnIndex(Contract.Poi.COLUMN_WEBSITE_URI));
                    String address = cursor.getString(cursor.getColumnIndex(Contract.Poi.COLUMN_ADDRESS));
                    double latitude = cursor.getDouble(cursor.getColumnIndex(Contract.Poi.COLUMN_LATITUDE));
                    double longitude = cursor.getDouble(cursor.getColumnIndex(Contract.Poi.COLUMN_LONGITUDE));

                    pois.add(new Poi(id, name, description, websiteUri, address, new LatLng(latitude, longitude)));

                    cursor.moveToNext();
                }
                cursor.close();

                return null;
            }
        }.execute();

//        getLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
//            @Override
//            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//                return new CursorLoader(MainActivity.this, Contract.Poi.CONTENT_URI, Contract.Poi.PROJECTION_ALL, null, null, null);
//            }
//
//            @Override
//            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
////                Cursor c = getContentResolver().query(Contract.Poi.CONTENT_URI, Contract.Poi.PROJECTION_ALL, null, null, null);
//                List<Poi> pois = new ArrayList<>();
//
//                String[] columns = {
//                        Poi.COLUMN_ID,
//                        Poi.COLUMN_NAME,
//                        Poi.COLUMN_DESCRIPTION,
//                        Poi.COLUMN_WEBSITE_URI,
//                        Poi.COLUMN_ADDRESS,
//                        Poi.COLUMN_LATITUDE,
//                        Poi.COLUMN_LONGITUDE
//                };
//
//                cursor.moveToFirst();
//
//                while (!cursor.isAfterLast()) {
//                    long id = cursor.getLong(cursor.getColumnIndex(Contract.Poi.COLUMN_ID));
//                    String name = cursor.getString(cursor.getColumnIndex(Contract.Poi.COLUMN_NAME));
//                    String description = cursor.getString(cursor.getColumnIndex(Contract.Poi.COLUMN_DESCRIPTION));
//                    String websiteUri = cursor.getString(cursor.getColumnIndex(Contract.Poi.COLUMN_WEBSITE_URI));
//                    String address = cursor.getString(cursor.getColumnIndex(Contract.Poi.COLUMN_ADDRESS));
//                    double latitude = cursor.getDouble(cursor.getColumnIndex(Contract.Poi.COLUMN_LATITUDE));
//                    double longitude = cursor.getDouble(cursor.getColumnIndex(Contract.Poi.COLUMN_LONGITUDE));
//
//                    pois.add(new Poi(id, name, description, websiteUri, address, new LatLng(latitude, longitude)));
//
//                    cursor.moveToNext();
//                }
//                cursor.close();
//            }
//
//            @Override
//            public void onLoaderReset(Loader<Cursor> loader) {
//
//            }
//        });

//        JsonArrayRequest poiJsonArrayRequest = new JsonArrayRequest(
//                Request.Method.GET, poiUrl, null, new Response.Listener<JSONArray>() {
//            @Override
//            public void onResponse(JSONArray response) {
//                Gson gson = new Gson();
//                final List<Poi> pois = Arrays.asList(gson.fromJson(response.toString(), Poi[].class));
//
//                new AsyncTask<Void, Void, Void>() {
//
//                    @Override
//                    protected Void doInBackground(Void... params) {
//                        for (final Poi poi : pois) {
//                            ContentValues values = new ContentValues();
//                                values.put(Contract.Poi.COLUMN_ID, poi.getId());
//                            values.put(Contract.Poi.COLUMN_NAME, poi.getName());
//                            values.put(Contract.Poi.COLUMN_DESCRIPTION, poi.getDescription());
//                            values.put(Contract.Poi.COLUMN_WEBSITE_URI, poi.getWebsiteUri());
//                            values.put(Contract.Poi.COLUMN_ADDRESS, poi.getAddress());
//                            values.put(Contract.Poi.COLUMN_LATITUDE, poi.getLatLng().latitude);
//                            values.put(Contract.Poi.COLUMN_LONGITUDE, poi.getLatLng().longitude);
//                            values.put(Contract.Poi.COLUMN_TIMESTAMP, "To be implemented");
//
//                            try {
//                                getContentResolver().insert(Contract.Poi.CONTENT_URI, values);
//                            } catch (Exception e) {
//                                Log.e("MainActivity", e.getMessage());
//                            }
//                        }
//
//                        return null;
//                    }
//                }.execute();
//                }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.e("MainActivity", error.getMessage());
//            }
//        });

//        JsonArrayRequest transportJsonArrayRequest = new JsonArrayRequest(
//                Request.Method.GET, transportUrl, null, new Response.Listener<JSONArray>() {
//            @Override
//            public void onResponse(JSONArray response) {
//                Gson gson = new Gson();
//                List<Transport> transports = Arrays.asList(gson.fromJson(response.toString(), Transport[].class));
//                for (Transport transport : transports) {
////                    mDbHelper.insertTransport(transport);
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.e("MainActivity", error.getMessage());
//            }
//        });

//        queue.add(poiJsonArrayRequest);
//        queue.add(transportJsonArrayRequest);
    }
}