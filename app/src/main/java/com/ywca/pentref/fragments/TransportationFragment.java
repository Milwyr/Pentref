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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ywca.pentref.R;
import com.ywca.pentref.adapters.TransportAdapter;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.models.Transport;

import org.joda.time.LocalTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * A {@link Fragment} that displays a list of {@link android.support.v7.widget.CardView} objects
 * that show the schedule of buses/ferries that depart from and arrive at Tai O.
 */
public class TransportationFragment extends Fragment {
    private RecyclerView mRecyclerView;

    public TransportationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootVIew = inflater.inflate(R.layout.fragment_transportation, container, false);

        mRecyclerView = (RecyclerView) rootVIew.findViewById(R.id.transport_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Insert the transport items into the recycler view
        new AsyncTask<Void, Void, List<Transport>>() {
            @Override
            protected List<Transport> doInBackground(Void... params) {
                return parseTransportationFile();
            }

            @Override
            protected void onPostExecute(List<Transport> transports) {
                TransportAdapter adapter =
                        new TransportAdapter(R.layout.transport_card_layout, transports);
                mRecyclerView.setAdapter(adapter);
            }
        }.execute();

        // Inflate the layout for this fragment
        return rootVIew;
    }

    // Parses the local transportation json file and returns the result as
    // a list of Transport objects. Returns null if any error occurs.
    private List<Transport> parseTransportationFile() {
        File transportsFile = new File(
                getActivity().getFilesDir(), Utility.TRANSPORTATION_JSON_FILE_NAME);

        if (transportsFile.exists()) {

            try {
                // Parse the input stream of the file into buffer (byte array)
                FileInputStream fileInputStream = new FileInputStream(transportsFile);
                int size = fileInputStream.available();
                byte[] buffer = new byte[size];
                fileInputStream.read(buffer);
                fileInputStream.close();

                // Convert the byte array into a string
                String transportationString = new String(buffer, "UTF-8");

                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(LocalTime.class, new Utility.LocalTimeSerializer())
                        .create();

                return Arrays.asList(
                        gson.fromJson(transportationString, Transport[].class));
            } catch (IOException e) {
                Log.e("TransportationFragment", e.getMessage());
            }
        }

        return null;
    }
}