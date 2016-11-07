package com.ywca.pentref.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ywca.pentref.R;
import com.ywca.pentref.adapters.TransportRecyclerViewAdapter;
import com.ywca.pentref.models.Transport;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TransportationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransportationFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String mParam1;


    public TransportationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment TransportationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TransportationFragment newInstance(String param1) {
        TransportationFragment fragment = new TransportationFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootVIew = inflater.inflate(R.layout.fragment_transportation, container, false);
        RecyclerView recyclerView = (RecyclerView) rootVIew.findViewById(R.id.transport_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        List<Transport> transportList = new ArrayList<>();
        transportList.add(new Transport(123, "1A", Transport.TypeEnum.BUS, 20f, 1.0f, "Tai O", "Tsim Sha Tsui"));
        transportList.add(new Transport(456, "1S", Transport.TypeEnum.BUS, 10f, 5.0f, "Tai O", "Tsing Yi"));
        transportList.add(new Transport(789, "2C", Transport.TypeEnum.BUS, 15f, 7.5f, "Tai O", "Central"));
        TransportRecyclerViewAdapter adapter =
                new TransportRecyclerViewAdapter(R.layout.transport_card_layout, transportList);
        recyclerView.setAdapter(adapter);

        // Inflate the layout for this fragment
        return rootVIew;
    }

}