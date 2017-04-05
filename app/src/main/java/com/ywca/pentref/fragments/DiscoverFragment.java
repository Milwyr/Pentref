package com.ywca.pentref.fragments;

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ywca.pentref.R;
import com.ywca.pentref.activities.BaseActivity;
import com.ywca.pentref.activities.PoiDetailsActivity;
import com.ywca.pentref.adapters.CategoryAdapter;
import com.ywca.pentref.adapters.SpinnerCategoryAdapter;
import com.ywca.pentref.common.Category;
import com.ywca.pentref.common.Contract;
import com.ywca.pentref.common.PentrefProvider;
import com.ywca.pentref.common.UpdateBookmarkAsyncTask;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.models.Poi;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.ywca.pentref.common.UpdateBookmarkAsyncTask;

import static android.app.Activity.RESULT_OK;

/**
 * Displays a {@link GoogleMap} instance with the predefined Points of Interest and categories.
 */
// Reference: https://github.com/googlemaps/android-samples/blob/master/ApiDemos/app/src/main/java/com/example/mapdemo/RawMapViewDemoActivity.java
public class DiscoverFragment extends BaseFragment implements LocationListener,
        OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMarkerClickListener {

    //region Constants
    // Request code to launch PoiDetailsActivity
    private final int REQUEST_POI_ACTIVITY_DETAILS = 9000;

    // Request code for requesting for location permission
    private final int REQUEST_LOCATION_PERMISSION = 10000;

    // Request code for checking whether GPS is turned on
    private final int REQUEST_CHECK_GPS_SETTINGS = 10001;
    //endregion

    //region Fields
    private Circle mCircle;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mGoogleMap;
    private Marker mPreviousMarker;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;

    private CardView mPoiSummaryCardView;
    private MapView mMapView;
    private RelativeLayout mBottomSheet;
    private TextView mSummaryCardTitleTextView;
    private Poi mSelectedPoi;
    private FloatingActionButton mBookmarkBtn;
    private Toast mToast;
    private Spinner mSpinner;


    // All the Points of Interest read from the local database
    private List<Poi> mPois;
    //endregion

    public DiscoverFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPois = new ArrayList<>();

        // Build Google Api client and connect to it
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
//        ImageButton imgbtn = (ImageButton) getActivity().findViewById(R.id.f_discover_imgbtn_refresh);
//        imgbtn.setOnClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discover, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.map_view);

        mBookmarkBtn = (FloatingActionButton) rootView.findViewById(R.id.f_discover_fab_bookmark);
        mBookmarkBtn.setOnClickListener(this);

        // Avoid unexpected crash
        try {
            mMapView.onCreate(savedInstanceState);
        } catch (Exception e) {
            Log.e("DiscoverFragment", e.getMessage());
        }

        mMapView.getMapAsync(this);

        mBottomSheet = (RelativeLayout) rootView.findViewById(R.id.bottom_sheet);

        // Initialises category spinner view that is embedded in the bottom sheet
        mSpinner = (Spinner) rootView.findViewById(R.id.f_discover_spinner);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Remove all the existing markers
                mGoogleMap.clear();
                mPreviousMarker = null;

                //Add All poi if position = 0
                if(position == 0){
                    for(Poi poi : mPois){
                        MarkerOptions markerOptions = new MarkerOptions().position(poi.getLatLng());
                        mGoogleMap.addMarker(markerOptions).setTag(poi);
                    }
                }else if(position == mSpinner.getAdapter().getCount() - 1) {
                    //Show bookmarked POI
                    Toast.makeText(getActivity(),"bookmark selected",Toast.LENGTH_SHORT);
                    new AsyncTask<Void, Void, List<Long>>() {
                        @Override
                        protected List<Long> doInBackground(Void... params) {
                            //Retrvive all ids from local bookmark
                            Cursor cursor = getActivity().getContentResolver().query(
                                    Contract.Bookmark.CONTENT_URI, Contract.Bookmark.PROJECTION_ALL, null, null, null);

                            // This line is used to get rid of the warning
                            if (cursor == null) {
                                return new ArrayList<>();
                            }
                            List<Long> idList = PentrefProvider.convertToBookmarkIds(cursor);
                            cursor.close();
                            return idList;
                        }

                        @Override
                        protected void onPostExecute(List<Long> longs) {
                            super.onPostExecute(longs);
                            //Add only the markers that match the bookmark
                            for(Poi poi : mPois){
                                if(longs.contains(poi.getId())){
                                    MarkerOptions markerOptions = new MarkerOptions().position(poi.getLatLng());
                                    mGoogleMap.addMarker(markerOptions).setTag(poi);
                                }
                            }
                        }
                    }.execute();
                }else{
                        // Add only the markers that match the selected category
                        for (Poi poi : mPois) {
                            if (poi.getCategoryId() == (position)) {
                                MarkerOptions markerOptions = new MarkerOptions().position(poi.getLatLng());
                                mGoogleMap.addMarker(markerOptions).setTag(poi);
                            }
                        }
                    }
                }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Initialises category grid view that is embedded in the bottom sheet
        final GridView gridView = (GridView) rootView.findViewById(R.id.category_grid_view);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Remove all the existing markers
                mGoogleMap.clear();
                mPreviousMarker = null;

                // Add only the markers that match the selected category
                for (Poi poi : mPois) {
                    if (poi.getCategoryId() == (i + 1)) {
                        MarkerOptions markerOptions = new MarkerOptions().position(poi.getLatLng());
                        mGoogleMap.addMarker(markerOptions).setTag(poi);
                    }
                }
            }
        });
        new AsyncTask<Void, Void, List<Category>>() {
            @Override
            protected List<Category> doInBackground(Void... voids) {
                // Retrieve a list of POI categories from the local database
                Cursor cursor = getActivity().getContentResolver().query(
                        Contract.Category.CONTENT_URI, Contract.Category.PROJECTION_ALL, null, null, null);

                // This line is used to get rid of the warning
                if (cursor == null) {
                    return new ArrayList<>();
                }

                List<Category> categories = PentrefProvider.convertToCategories(cursor);
                cursor.close();
                return categories;
            }

            @Override
            protected void onPostExecute(List<Category> categories) {
                // Add the categories to the grid view at the bottom
                gridView.setAdapter(new CategoryAdapter(getActivity(), categories));
                mSpinner.setAdapter(new SpinnerCategoryAdapter(getActivity(),categories));

            }
        }.execute();


        mPoiSummaryCardView = (CardView) rootView.findViewById(R.id.poi_summary_card_view);
        mPoiSummaryCardView.setOnClickListener(this);

        mSummaryCardTitleTextView = (TextView) rootView.findViewById(R.id.discover_poi_name_text_view);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_POI_ACTIVITY_DETAILS:
                mPoiSummaryCardView.setVisibility(View.GONE);
                mBottomSheet.setVisibility(View.VISIBLE);
                break;
            case REQUEST_CHECK_GPS_SETTINGS:
                if (resultCode == RESULT_OK) {
                    // All required changes were successfully made
                    startLocationUpdates();
                }
                break;
        }
    }

    //    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        mMapView.onSaveInstanceState(outState);
//    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.clear();

        //region Enable locate me button
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check if both coarse location and fine location permissions has been granted
            if ((ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

                // Request coarse location and fine location permissions if not granted
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            }
        } else {
            // Location permissions have been granted prior to installation before Marshmallow (API 23)
            googleMap.setMyLocationEnabled(true);
        }
        //endregion

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // Reset the previously selected marker to the default colour
                if (mPreviousMarker != null) {
                    mPreviousMarker.setIcon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }

                mBottomSheet.setVisibility(View.VISIBLE);
                mPoiSummaryCardView.setVisibility(View.GONE);
            }
        });

        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                initiateLocationRequest();
                return false;
            }
        });

        new AsyncTask<Void, Void, List<Poi>>() {
            @Override
            protected List<Poi> doInBackground(Void... params) {
                // Retrieve a list of Points of Interest from the local database
                Cursor cursor = getActivity().getContentResolver().query(
                        Contract.Poi.CONTENT_URI, Contract.Poi.PROJECTION_ALL, null, null, null);

                // This line is used to get rid of the warning
                if (cursor == null) {
                    return new ArrayList<>();
                }

                List<Poi> pois = PentrefProvider.convertToPois(cursor);
                cursor.close();
                return pois;
            }

            @Override
            protected void onPostExecute(List<Poi> pois) {
                super.onPostExecute(pois);

                // Add a list of Points of Interest to the map
                for (Poi poi : pois) {
                    MarkerOptions markerOptions = new MarkerOptions().position(poi.getLatLng());
                    mGoogleMap.addMarker(markerOptions).setTag(poi);
                }

                // Save all the Points of Interest to the instance variable
                mPois.addAll(pois);

                // Set a marker click listener
                mGoogleMap.setOnMarkerClickListener(DiscoverFragment.this);
            }
        }.execute();
    }

    // Request for continuous location update using fused location provider
    private void initiateLocationRequest() {
        // Instantiate LocationRequest and LocationSettingsRequest instances
        // if they have not been created
        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest()
                    .setInterval(5000) // milliseconds
                    .setFastestInterval(1000) // milliseconds
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationSettingsRequest = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest).build();
        }

        // Check whether GPS is turned on, and show a confirmation dialog to let user
        // turn on GPS it is currently off.
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient, mLocationSettingsRequest);
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult()
                            status.startResolutionForResult(getActivity(), REQUEST_CHECK_GPS_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.e("DiscoverFragment", e.getMessage());
                        }
                        break;
                }
            }
        });

        startLocationUpdates();
    }

    //region Geo-fencing API
    private LatLng mLastLatLng;

    @Override
    public void onLocationChanged(Location location) {
        // Remove the circle that has been previously added
        if (mCircle != null) {
            mCircle.remove();
        }

        // Draw a circle with radius 500m from the current location
        mCircle = mGoogleMap.addCircle(new CircleOptions()
                .center(new LatLng(location.getLatitude(), location.getLongitude()))
                .radius(500)
                .strokeWidth(5));

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(
                new LatLng(location.getLatitude(), location.getLongitude())));

        // Draw a line of the route the user has travelled
        if (mLastLatLng != null) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .visible(true).width(5).color(ContextCompat.getColor(getActivity(), R.color.black))
                    .add(mLastLatLng, new LatLng(location.getLatitude(), location.getLongitude()));
            mGoogleMap.addPolyline(polylineOptions);
        }
        mLastLatLng = new LatLng(location.getLatitude(), location.getLongitude());
    }

    private void startLocationUpdates() {
        if ((ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            // Request location updates only when both course and fine location permissions
            // have been granted
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    private void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(mGoogleApiClient, this);
        }
    }
    //endregion

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.poi_summary_card_view:
                // Deselect the currently selected marker (although named previous marker)
                // by setting the icon to be the default one (which is red)
                if (mPreviousMarker != null) {
                    mPreviousMarker.setIcon(BitmapDescriptorFactory.defaultMarker());
                }

                // Launch PoiDetailsActivity
                Intent intent = new Intent(getActivity(), PoiDetailsActivity.class);
                intent.putExtra(Utility.SELECTED_POI_EXTRA_KEY, mSelectedPoi);
                startActivityForResult(intent, REQUEST_POI_ACTIVITY_DETAILS);
                break;
            case R.id.f_discover_fab_bookmark:
                //
                final boolean isPreviouslyBookmarked = (boolean) mBookmarkBtn.getTag();

                // Insert or delete the bookmark from database after the user clicks on the bookmark fab
                new UpdateBookmarkAsyncTask(getActivity(),mSelectedPoi.getId()) {
                    @Override
                    protected void onPreExecute() {
                        mBookmarkBtn.setEnabled(false);
                    }

                    @Override
                    protected void onPostExecute(Void v) {
                        boolean isNowBookmarked = !isPreviouslyBookmarked;
                        mBookmarkBtn.setTag(isNowBookmarked);
                        mBookmarkBtn.setImageResource(isNowBookmarked ?
                                R.drawable.ic_bookmarked_black_36dp : R.drawable.ic_bookmark_black_36dp);

                        //Show isBookmarked msg
                        if(isNowBookmarked){
                            if(mToast != null){
                                mToast.cancel();
                            }
                            mToast =  Toast.makeText(getActivity(), "Bookmark added", Toast.LENGTH_SHORT);
                            mToast.show();
                        }else{
                            if(mToast != null) {
                                mToast.cancel();
                            }
                            mToast = Toast.makeText(getActivity(), "Bookmark removed", Toast.LENGTH_SHORT);
                            mToast.show();
                        }


                        mBookmarkBtn.setEnabled(true);
                    }
                }.execute(isPreviouslyBookmarked);
                break;
//            case R.id.f_discover_imgbtn_refresh:
//                Log.i("DiscoverFragment","Refresh clicked");
//
//                //delete all old local poi
//                break;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // Highlight the selected marker
        if (mPreviousMarker != null) {
            mPreviousMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        mPreviousMarker = marker;

        mBottomSheet.setVisibility(View.GONE);
        mSelectedPoi = (Poi) marker.getTag();
        mPoiSummaryCardView.setVisibility(View.VISIBLE);
        new InitialiseBookmarkFabAsyncTask().execute(mSelectedPoi.getId());


        Locale locale = super.getDeviceLocale();
        mSummaryCardTitleTextView.setText(mSelectedPoi.getName(locale));
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {

            // Enable locate me button if permissions are granted
            if ((ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                mGoogleMap.setMyLocationEnabled(true);
            }
        }
    }

    //region Lifecycle methods for MapView
    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            // NullPointerException is thrown in certain circumstances
            try {
                mMapView.onResume();
            } catch (Exception e) {
                Log.e("DiscoverFragment", e.getMessage());
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mMapView != null) {
            // NullPointerException is thrown in certain circumstances
            try {
                mMapView.onStart();
            } catch (Exception e) {
                Log.e("DiscoverFragment", e.getMessage());
            }
        }
    }

    @Override
    public void onPause() {
        if (mMapView != null) {
            try {
                // NullPointerException is thrown in certain circumstances
                mMapView.onPause();
            } catch (Exception e) {
                Log.e("DiscoverFragment", e.getMessage());
            }
        }
        stopLocationUpdates();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mMapView != null) {
            try {
                // NullPointerException is thrown in certain circumstances
                mMapView.onStop();
            } catch (Exception e) {
                Log.e("DiscoverFragment", e.getMessage());
            }
        }
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onDestroy() {
        if (mMapView != null) {
            try {
                // NullPointerException is thrown in certain circumstances
                mMapView.onDestroy();
            } catch (Exception e) {
                Log.e("DiscoverFragment", e.getMessage());
            }

        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }
    //endregion

    private class InitialiseBookmarkFabAsyncTask extends AsyncTask<Long, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Long... longs) {
            long poiId = longs[0];

            // Query the bookmark table with the given poi id
            Uri uriWithPoiId = Uri.withAppendedPath(
                    Contract.Bookmark.CONTENT_URI, Long.toString(poiId));
            Cursor cursor = getActivity().getContentResolver().query(uriWithPoiId, null, null, null, null);

            // Return true if the given poi id is found in the bookmark table
            if (cursor != null) {
                cursor.close();
                return cursor.getCount() > 0;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean isBookmarked) {
            super.onPostExecute(isBookmarked);
            mBookmarkBtn.setTag(isBookmarked);
            mBookmarkBtn.setImageResource(isBookmarked ? R.drawable.ic_bookmarked_black_36dp : R.drawable.ic_bookmark_black_36dp);
        }
    }
}