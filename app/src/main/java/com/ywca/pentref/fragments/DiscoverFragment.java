package com.ywca.pentref.fragments;

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ywca.pentref.R;
import com.ywca.pentref.activities.PoiDetailsActivity;
import com.ywca.pentref.adapters.BookmarksAdapter;
import com.ywca.pentref.adapters.CategoryAdapter;
import com.ywca.pentref.common.CategoryItem;
import com.ywca.pentref.common.Contract;
import com.ywca.pentref.common.PentrefProvider;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.models.Poi;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a {@link GoogleMap} instance with the predefined Points of Interest and categories.
 */
// Reference: https://github.com/googlemaps/android-samples/blob/master/ApiDemos/app/src/main/java/com/example/mapdemo/RawMapViewDemoActivity.java
public class DiscoverFragment extends Fragment implements
        OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMarkerClickListener {

    private final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    // Request code to launch PoiDetailsActivity
    private final int RC_POI_ACTIVITY_DETAILS = 9000;

    // Request code for requesting for location permission
    private final int RC_LOCATION_PERMISSION = 10000;

    private GoogleMap mGoogleMap;

    private CardView mPoiSummaryCardView;
    private MapView mMapView;
    private RelativeLayout mBottomSheet;
    private Poi mSelectedPoi;

    public DiscoverFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discover, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.map_view);

        // TODO: Deal with the potential crash
        try {
            mMapView.onCreate(savedInstanceState);
        } catch (Exception e) {
            Log.e("DiscoverFragment", e.getMessage());
        }

        mMapView.getMapAsync(this);

        mBottomSheet = (RelativeLayout) rootView.findViewById(R.id.bottom_sheet);

        GridView gridView = (GridView) rootView.findViewById(R.id.category_grid_view);
        List<CategoryItem> categories = new ArrayList<>();
        categories.add(new CategoryItem(0, "Points of Interest", R.drawable.ic_menu_camera));
        categories.add(new CategoryItem(1, "Public Facilities", R.drawable.ic_bus_black_36dp));
        categories.add(new CategoryItem(2, "Restaurants", R.drawable.ic_menu_share));
        categories.add(new CategoryItem(3, "Miscellaneous", R.drawable.ic_bookmark_black_36dp));
        gridView.setAdapter(new CategoryAdapter(getActivity(), categories));

        // TODO: Potentially create a new layout for this
        RecyclerView bottomRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        bottomRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        bottomRecyclerView.setLayoutManager(layoutManager);
        // TODO: Has to use read data
        List<Poi> pois = new ArrayList<>();
        pois.add(new Poi(1, "Temp", "Description", "www.yahoo.com", "Somewhere in Tai O", new LatLng(1, 2)));
        pois.add(new Poi(2, "Tai O YWCA", "Description", "www.yahoo.com", "Tai O YWCA, New Territories", new LatLng(1, 2)));
        pois.add(new Poi(2, "Tai O YWCA", "Description", "www.yahoo.com", "Tai O YWCA, New Territories", new LatLng(1, 2)));
        pois.add(new Poi(2, "Tai O YWCA", "Description", "www.yahoo.com", "Tai O YWCA, New Territories", new LatLng(1, 2)));
        pois.add(new Poi(2, "Tai O YWCA", "Description", "www.yahoo.com", "Tai O YWCA, New Territories", new LatLng(1, 2)));
        bottomRecyclerView.setAdapter(new BookmarksAdapter(R.layout.bookmark_row_layout, pois));

        mPoiSummaryCardView = (CardView) rootView.findViewById(R.id.poi_summary_card_view);
        mPoiSummaryCardView.setOnClickListener(this);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_POI_ACTIVITY_DETAILS) {
            mPoiSummaryCardView.setVisibility(View.GONE);
            mBottomSheet.setVisibility(View.VISIBLE);
        }
    }

    //    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        mMapView.onSaveInstanceState(outState);
//    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mGoogleMap = googleMap;

        //region Enable locate me button
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Check if coarse location and fine location permissions has been granted
            if ((ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

                // Request coarse location and fine location permissions if not granted
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, RC_LOCATION_PERMISSION);
            }
        } else {
            // Location permissions have been granted prior to installation before Marshmallow (API 23)
            googleMap.setMyLocationEnabled(true);
        }
        //endregion

        // Radius: 500m
        googleMap.addCircle(new CircleOptions()
                .center(new LatLng(22.2574336, 113.8620642))
                .radius(500)
                .strokeWidth(5));

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mBottomSheet.setVisibility(View.VISIBLE);
                mPoiSummaryCardView.setVisibility(View.GONE);
            }
        });

        new AsyncTask<Void, Void, List<Poi>>() {
            @Override
            protected List<Poi> doInBackground(Void... params) {
                // Retrieve a list of Points of Interest from the local database
                Cursor cursor = getActivity().getContentResolver().query(
                        Contract.Poi.CONTENT_URI, Contract.Poi.PROJECTION_ALL, null, null, null);
                List<Poi> pois = PentrefProvider.convertToPois(cursor);
                cursor.close();
                return pois;
            }

            @Override
            protected void onPostExecute(List<Poi> pois) {
                super.onPostExecute(pois);

                // Add a list of Points of Interest to the map
                for (Poi poi : pois) {
                    googleMap.addMarker(new MarkerOptions()
                            .title(poi.getName())
                            .position(poi.getLatLng())).setTag(poi);
                }

                // Set a marker click listener
                googleMap.setOnMarkerClickListener(DiscoverFragment.this);
            }
        }.execute();

//        // TODO: Load data offline when available
//        String poiUrl = "https://raw.githubusercontent.com/Milwyr/Temporary/master/pois.json";
//        JsonArrayRequest PoiJsonArrayRequest = new JsonArrayRequest(
//                Request.Method.GET, poiUrl, null, new Response.Listener<JSONArray>() {
//            @Override
//            public void onResponse(JSONArray response) {
//                Gson gson = new Gson();
//                List<Poi> pois = Arrays.asList(gson.fromJson(response.toString(), Poi[].class));
//
//                for (Poi poi : pois) {
//                    googleMap.addMarker(new MarkerOptions()
//                            .title(poi.getName())
//                            .position(poi.getLatLng())).setTag(poi);
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.e("MainActivity", error.getMessage());
//            }
//        });
//
//        RequestQueue queue = Volley.newRequestQueue(getActivity());
//        queue.add(PoiJsonArrayRequest);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.poi_summary_card_view:
                Intent intent = new Intent(getActivity(), PoiDetailsActivity.class);
                intent.putExtra(Utility.SELECTED_POI_EXTRA_KEY, mSelectedPoi);
                startActivityForResult(intent, RC_POI_ACTIVITY_DETAILS);
                break;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mBottomSheet.setVisibility(View.GONE);
        mSelectedPoi = (Poi) marker.getTag();
        mPoiSummaryCardView.setVisibility(View.VISIBLE);
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RC_LOCATION_PERMISSION) {

            // Enable locate me button if permissions are granted
            if ((ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                mGoogleMap.setMyLocationEnabled(true);
            }
        }
    }

    // Map view requires these lifecycle methods to be forwarded to itself
    //region Lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mMapView != null) {
            mMapView.onStart();
        }
    }

    @Override
    public void onPause() {
        if (mMapView != null) {
            mMapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mMapView != null) {
            mMapView.onStop();
        }
    }

    @Override
    public void onDestroy() {
        if (mMapView != null) {
            mMapView.onDestroy();
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
}