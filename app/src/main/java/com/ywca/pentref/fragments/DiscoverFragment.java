package com.ywca.pentref.fragments;

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.ywca.pentref.R;
import com.ywca.pentref.activities.PoiDetailsActivity;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.models.Poi;

import org.json.JSONArray;

import java.util.Arrays;
import java.util.List;

/**
 * Use the {@link DiscoverFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
// Reference: https://github.com/googlemaps/android-samples/blob/master/ApiDemos/app/src/main/java/com/example/mapdemo/RawMapViewDemoActivity.java
public class DiscoverFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMarkerClickListener {
    // TODO: Rename parameter arguments, choose names that match
    private static final String ARG_PARAM1 = "param1";

    private final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    // TODO: Rename and change types of parameters
    private String mParam1;

    private CardView mPoiSummaryCardView;
    private MapView mMapView;
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
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }

//        mSelectedPoi = new Poi(12345, "Tai O", "Beautiful", "www.google.com.hk", "Address", null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discover, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.map_view);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);

        mPoiSummaryCardView = (CardView) rootView.findViewById(R.id.poi_summary_card_view);
        mPoiSummaryCardView.setOnClickListener(this);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        // Request location permissions if not granted
        if ((ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, 10000);
        }

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
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

                for (Poi poi: pois) {
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