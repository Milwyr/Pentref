package com.ywca.pentref.activities;

import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ywca.pentref.R;
import com.ywca.pentref.common.Contract;
import com.ywca.pentref.common.PentrefProvider;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.fragments.AboutFragment;
import com.ywca.pentref.fragments.BookmarksFragment;
import com.ywca.pentref.fragments.DiscoverFragment;
import com.ywca.pentref.fragments.PoiAdminFragment;
import com.ywca.pentref.fragments.ProfileFragment;
import com.ywca.pentref.fragments.SettingsFragment;
import com.ywca.pentref.fragments.TourFragment;
import com.ywca.pentref.fragments.TransportationFragment;
import com.ywca.pentref.fragments.WeatherFragment;
import com.ywca.pentref.models.Poi;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // The search view that is inflated as a menu item on the Action Bar
    private MenuItem mActionSearchMenuItem;
    private MenuItem mPoiAddItem;
    private MenuItem mPoiDeleteItem;

    private NavigationView mNavigationView;
    //Firebase Auth instance
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mDatabase;
    private int mCurrentFragmentId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialiseComponents();
        Menu menu = mNavigationView.getMenu();
        menu.findItem(R.id.nav_admin).setVisible(false);
        mDatabase = FirebaseDatabase.getInstance();

        //Get a firebaseAuth instance
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("MainFireBaseAuth", "onAuthStateChanged:signed_in:" + user.getUid());
                    //Check if user is admin
                    DatabaseReference adminRef = mDatabase.getReference().child(Utility.FIREBASE_TABLE_ADMIN).child(user.getUid());
                    adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //dataSnapshot.getValue is true when the user is admin
                            Menu menu = mNavigationView.getMenu();
                            if (dataSnapshot.getValue() != null && (boolean) dataSnapshot.getValue()) {
                                menu.findItem(R.id.nav_admin).setVisible(true);
                            }else{
                                menu.findItem(R.id.nav_admin).setVisible(false);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    // User is signed out
                    Log.d("MainFireBaseAuth", "onAuthStateChanged:signed_out");
                    Menu menu = mNavigationView.getMenu();
                    menu.findItem(R.id.nav_admin).setVisible(false);
                }
            }
        };


        // Display the discover fragment only when the app launches as
        // savedInstanceState != null when orientation changes
        if (savedInstanceState == null) {
            int fragmentIndex = getIntent().getIntExtra(Utility.FRAGMENT_INDEX_EXTRA_KEY, -1);

            switch (fragmentIndex) {
                case 0:
                    changeFragment(R.string.discover, new DiscoverFragment());
                    break;
                case 1:
                    changeFragment(R.string.bookmarks, new BookmarksFragment());
                    break;
                case 2:
                    changeFragment(R.string.weather, new WeatherFragment());
                    break;
                case 3:
                    changeFragment(R.string.transport_schedule, new TransportationFragment());
                    break;
                case 4:
                    changeFragment(R.string.settings, new SettingsFragment());
                    break;
            }
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
        // Inflate the options menu with a search view and a poi delete btn
        getMenuInflater().inflate(R.menu.main, menu);
        mActionSearchMenuItem = menu.findItem(R.id.action_search);
        mPoiAddItem = menu.findItem(R.id.admin_add_poi_item);
        mPoiDeleteItem = menu.findItem(R.id.admin_delete_poi_item);


        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) mActionSearchMenuItem.getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        //

        // The search view is only visible when the current fragment is discover fragment
        Fragment currentFragment = getFragmentManager().findFragmentById(R.id.frame);
        updateSearchViewVisibility(currentFragment);

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
        //Do nothing when the action is null
        if (intent.getAction() == null) {
            Log.d("MainActivity", "intent null");
            return;
        }
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
                changeFragment(R.string.transport_schedule, new TransportationFragment());
                break;
            case R.id.nav_tour:
                changeFragment(R.string.tour, new TourFragment());
                break;
            case R.id.nav_profile:
                changeFragment(R.string.profile, new ProfileFragment());
                break;
            case R.id.nav_settings:
                changeFragment(R.string.settings, new SettingsFragment());
                break;
            case R.id.nav_about:
//                changeFragment(R.string.about, new AboutFragment());
                changeFragment(R.string.about, new AboutFragment());
                break;
            case R.id.nav_admin:
                changeFragment(R.string.admin, new PoiAdminFragment());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Changes the title to the given string resource and
    // replaces the current fragment by the given one.
    private void changeFragment(int resourceId, Fragment fragment) {
        //Check if it's the current fragment
        if(mCurrentFragmentId != resourceId){
            mCurrentFragmentId = resourceId;
            setTitle(resourceId);
            getFragmentManager().beginTransaction().replace(R.id.frame, fragment).commit();
        }else{
            //Do nth
        }



        // Highlight the selected item in the Navigation Drawer
        if (fragment instanceof DiscoverFragment) {
            mNavigationView.setCheckedItem(R.id.nav_discover);
        } else if (fragment instanceof BookmarksFragment) {
            mNavigationView.setCheckedItem(R.id.nav_bookmarks);
        } else if (fragment instanceof WeatherFragment) {
            mNavigationView.setCheckedItem(R.id.nav_weather);
        } else if (fragment instanceof TransportationFragment) {
            mNavigationView.setCheckedItem(R.id.nav_transportation);
        } else if (fragment instanceof SettingsFragment) {
            mNavigationView.setCheckedItem(R.id.nav_settings);
        } else if (fragment instanceof PoiAdminFragment) {
            mNavigationView.setCheckedItem(R.id.nav_admin);
        } else if (fragment instanceof TourFragment){
            mNavigationView.setCheckedItem(R.id.nav_tour);
        }

        // The search view is only visible when the current fragment is discover fragment
        if (mActionSearchMenuItem != null) {
            updateSearchViewVisibility(fragment);
        }
    }

    // Initialises components when onCreate() method is called
    private void initialiseComponents() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    private void updateSearchViewVisibility(Fragment currentFragment) {
        if (currentFragment instanceof DiscoverFragment) {
            mActionSearchMenuItem.setVisible(true);
        } else {
            mActionSearchMenuItem.setVisible(false);
        }
        //Set mPoiDeleteItem invisible for all fragment
        mPoiDeleteItem.setVisible(false);
        mPoiAddItem.setVisible(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


}