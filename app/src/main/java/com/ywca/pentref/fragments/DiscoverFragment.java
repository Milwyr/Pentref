package com.ywca.pentref.fragments;

import android.Manifest;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.ywca.pentref.R;
import com.ywca.pentref.activities.PoiDetailsActivity;
import com.ywca.pentref.adapters.BookmarksRecyclerViewAdapter;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.models.Poi;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Use the {@link DiscoverFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
// Reference: https://github.com/googlemaps/android-samples/blob/master/ApiDemos/app/src/main/java/com/example/mapdemo/RawMapViewDemoActivity.java
public class DiscoverFragment extends Fragment implements
        OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMarkerClickListener {
    // TODO: Rename parameter arguments, choose names that match
    private static final String ARG_PARAM1 = "param1";

    private final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    // TODO: Rename and change types of parameters
    private String mParam1;

    private CardView mPoiSummaryCardView;
    private MapView mMapView;
    private RelativeLayout mBottomSheetRelativeLayout;
    private Poi mSelectedPoi;

    public DiscoverFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment DiscoverFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DiscoverFragment newInstance(String param1) {
        DiscoverFragment fragment = new DiscoverFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discover, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.map_view);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        mBottomSheetRelativeLayout = (RelativeLayout) rootView.findViewById(R.id.bottom_sheet_relative_layout);

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
        bottomRecyclerView.setAdapter(new BookmarksRecyclerViewAdapter(R.layout.bookmark_row_layout, pois));

        mPoiSummaryCardView = (CardView) rootView.findViewById(R.id.poi_summary_card_view);
        mPoiSummaryCardView.setOnClickListener(this);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getActivity().getComponentName()));
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        //region Request coarse location and fine location permissions if not granted
        if ((ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 10000);
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
                mBottomSheetRelativeLayout.setVisibility(View.VISIBLE);
                mPoiSummaryCardView.setVisibility(View.GONE);
            }
        });

        // TODO: Load data offline when available
        String poiUrl = "https://raw.githubusercontent.com/Milwyr/Temporary/master/pois.json";
        JsonArrayRequest PoiJsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, poiUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Gson gson = new Gson();
                List<Poi> pois = Arrays.asList(gson.fromJson(response.toString(), Poi[].class));

                for (Poi poi : pois) {
                    googleMap.addMarker(new MarkerOptions()
                            .title(poi.getName())
                            .position(poi.getLatLng())).setTag(poi);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("MainActivity", error.getMessage());
            }
        });

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(PoiJsonArrayRequest);

        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMarkerClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.poi_summary_card_view:
                Intent intent = new Intent(getActivity(), PoiDetailsActivity.class);
                intent.putExtra(Utility.SELECTED_POI_EXTRA_NAME, mSelectedPoi);
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mBottomSheetRelativeLayout.setVisibility(View.GONE);
        mSelectedPoi = (Poi) marker.getTag();
        mPoiSummaryCardView.setVisibility(View.VISIBLE);
        return false;
    }

    // Map view requires these lifecycle methods to be forwarded to itself
    //region Lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
    //endregion
}