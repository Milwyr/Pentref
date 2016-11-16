package com.ywca.pentref.fragments;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ywca.pentref.R;
import com.ywca.pentref.adapters.TransportRecyclerViewAdapter;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.models.Transport;

import org.joda.time.LocalTime;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A {@link Fragment} that displays a list of {@link android.support.v7.widget.CardView} objects
 * that show the buses/ferries that depart from and arrive at Tai O.
 */
public class TransportationFragment extends Fragment {
    public TransportationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootVIew = inflater.inflate(R.layout.fragment_transportation, container, false);
        final RecyclerView recyclerView = (RecyclerView) rootVIew.findViewById(R.id.transport_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        String transportUrl = "https://raw.githubusercontent.com/Milwyr/Temporary/master/transports.json";
        JsonArrayRequest transportJsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, transportUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(LocalTime.class, new Utility.LocalTimeSerializer())
                        .create();
                final List<Transport> transports = Arrays.asList(
                        gson.fromJson(response.toString(), Transport[].class));

                TransportRecyclerViewAdapter adapter =
                        new TransportRecyclerViewAdapter(R.layout.transport_card_layout, transports);
                recyclerView.setAdapter(adapter);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TransportationFragment", error.getMessage());
            }
        });
        Volley.newRequestQueue(getActivity()).add(transportJsonArrayRequest);

        // Inflate the layout for this fragment
        return rootVIew;
    }

}