package com.ywca.pentref.fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.gson.Gson;
import com.ywca.pentref.R;
import com.ywca.pentref.activities.AddPoiActivity;
import com.ywca.pentref.common.Contract;
import com.ywca.pentref.common.PentrefProvider;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.models.Poi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PoiAdminFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.


 */
public class PoiAdminFragment extends BaseFragment implements OnMapReadyCallback, View.OnClickListener, LocationListener,GoogleMap.OnMarkerClickListener{

    //region Constants
    // Request code to launch PoiDetailsActivity
    private final int REQUEST_POI_ACTIVITY_DETAILS = 9000;

    // Request code for requesting for location permission
    private final int REQUEST_LOCATION_PERMISSION = 10000;

    // Request code for checking whether GPS is turned on
    private final int REQUEST_CHECK_GPS_SETTINGS = 10001;
    //endregion

    private Circle mCircle;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mGoogleMap;
    private Marker mCurrentMarker;
    private Poi mSelectedDeletePoi;
    private Marker mSelectedDeleteMarker;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private MapView mMapView;
    private CardView mPoiAddCardView;
    private CardView mPoiDelCardView;
    private ArrayList<Poi> mPois;
    private ProgressDialog mProgress;
    private RequestQueue mQueue;

    public static final String TAG = "PoiAdminFragment";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mPois = new ArrayList<>();
        mProgress = new ProgressDialog(getActivity());
        mProgress.setTitle("Loading");
        mProgress.setMessage("Deleting POI");
        mProgress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        // Build Google Api client and connect to it
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_poi_admin, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.mapView);

        try {
            mMapView.onCreate(savedInstanceState);
        } catch (Exception e) {
            Log.e("PoiAdminFragment", e.getMessage());
        }
        mMapView.getMapAsync(this);
        mPoiAddCardView = (CardView) rootView.findViewById(R.id.poi_add_card_view);
        Button okBtn = (Button) rootView.findViewById(R.id.okBtn);
        okBtn.setOnClickListener(this);

        mPoiDelCardView = (CardView) rootView.findViewById(R.id.poi_del_card_view);
        Button delBtn = (Button) rootView.findViewById(R.id.poi_btn_del);
        delBtn.setOnClickListener(this);


        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

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
               mGoogleMap.setOnMarkerClickListener(PoiAdminFragment.this);

            }
        }.execute();


        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(mCurrentMarker != null){
                    mCurrentMarker.remove();
                    mCurrentMarker  = null;
                }
                MarkerOptions makerOption = new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                mCurrentMarker = mGoogleMap.addMarker(makerOption);
                mPoiAddCardView.setVisibility(View.VISIBLE);
                mPoiDelCardView.setVisibility(View.GONE);
            }
        });
        Boolean test = mGoogleMap.getUiSettings().isZoomGesturesEnabled();
        mGoogleMap.getUiSettings().setScrollGesturesEnabled(true);


        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                initiateLocationRequest();
                return false;
            }
        });





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
                            Log.e("PoiAdminFragment", e.getMessage());
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
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
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
    public void onClick(View v) {
        Log.i("PoiAdminFragment","Onclick()");
        switch(v.getId()){
            case R.id.okBtn:
                Log.i("PoiAdminFragment","OKpressed");
                Intent intent = new Intent(getActivity(), AddPoiActivity.class);
               // String eee = getCompleteAddressString(mCurrentMarker.getPosition().latitude,mCurrentMarker.getPosition().longitude);
                intent.putExtra(Utility.ADMIN_SELECTED_LOCATION_LATITUDE,mCurrentMarker.getPosition().latitude);
                intent.putExtra(Utility.ADMIN_SELECTED_LOCATION_LONGITUDE,mCurrentMarker.getPosition().longitude);
                startActivity(intent);
                break;
            case R.id.poi_btn_del:

                Log.i("PoiAdminFragment","DELpressed");
                Log.i("PoiAdminFragment",""+mSelectedDeletePoi.getId());


                //Delete the selected poi from server
                if(mQueue == null) {
                    mQueue = Volley.newRequestQueue(getActivity());
                }
                String delUrl = Utility.SERVER_URL + "/PostReq.php?Method=DEL&PATH=pois&UID=20161217";
                JSONObject delPoijsonObject = new JSONObject();
                try {
                    delPoijsonObject.put("id",mSelectedDeletePoi.getId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.POST, delUrl, delPoijsonObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int a = 1;
                    }
                }, new Response.ErrorListener() {
                    @Override
                    //Server side error
                    public void onErrorResponse(VolleyError error) {
                        int b = 1;
                    }

                });

                jsonObjectRequest.setTag(TAG);
                mQueue.add(jsonObjectRequest);

//                //Delete the selected poi from local table
//                // Defines selection criteria for the rows you want to delete
//                String mSelectionClause = Contract.Poi._ID + " = ?";
//                String[] mSelectionArgs = {mSelectedDeletePoi.getId()+""};
//                getActivity().getContentResolver().delete(Contract.Poi.CONTENT_URI,mSelectionClause,mSelectionArgs);

                syncWithServer();

                //delete the SelectedDeleteMarker marker from the map
                mSelectedDeleteMarker.remove();
                mSelectedDeleteMarker = null;


                break;
        }

    }
    //Delete the local poi table and get the new one from the server
    private void syncWithServer() {
        //Create requestQueue if not exist
        if(mQueue == null){
            mQueue = Volley.newRequestQueue(getActivity());
        }

        //Start the progressDialog
        mProgress.show();

        // Remove all poi from the local database
        ContentResolver contentResolver = getActivity().getContentResolver();
        contentResolver.delete(Contract.Poi.CONTENT_URI, null, null);

        //Download poi from server
        String poiUrl = Utility.SERVER_URL + "/PostReq.php?Method=GET&PATH=pois";
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
                        getActivity().getContentResolver().insert(Contract.Poi.CONTENT_URI, values);

                    } catch (Exception e) {
                        Log.e("Poi_admin_Fragment", e.getMessage());
                    }
                }



                mProgress.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error != null) {
                    mProgress.dismiss();
                    Log.e("Poi_admmn_Fragment", error.getMessage());
                }
            }
        });
        mQueue.add(poiJsonArrayRequest);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if (marker == mCurrentMarker){
            mPoiAddCardView.setVisibility(View.VISIBLE);
            mPoiDelCardView.setVisibility(View.GONE);
        }else{
            mPoiAddCardView.setVisibility(View.GONE);
            mPoiDelCardView.setVisibility(View.VISIBLE);
            mSelectedDeletePoi = (Poi) marker.getTag();
            mSelectedDeleteMarker = marker;
        }


        return false;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
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
                Log.e("PoiAdminFragment", e.getMessage());
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
                Log.e("PoiAdminFragment", e.getMessage());
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
                Log.e("PoiAdminFragment", e.getMessage());
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
                Log.e("PoiAdminFragment", e.getMessage());
            }
        }
        mGoogleApiClient.disconnect();
        if (mQueue != null) {
            mQueue.cancelAll(TAG);
        }
    }

    @Override
    public void onDestroy() {
        if (mMapView != null) {
            try {
                // NullPointerException is thrown in certain circumstances
                mMapView.onDestroy();
            } catch (Exception e) {
                Log.e("PoiAdminFragment", e.getMessage());
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

    //get full address
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
               //Log.w("My Current loction address", "" + strReturnedAddress.toString());
            } else {
                //Log.i("My Current loction address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Log.w("My Current loction address", "Canont get Address!");
        }
        return strAdd;
    }
}
