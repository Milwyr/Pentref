package com.ywca.pentref.common;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ywca.pentref.R;
import com.ywca.pentref.activities.TimetableActivity;
import com.ywca.pentref.models.Transport;

import java.util.ArrayList;
import java.util.List;

/**
 * An adapter that provides a displays a data set on a RecyclerView using the given layout.
 */
public class TransportRecyclerViewAdapter extends
        RecyclerView.Adapter<TransportRecyclerViewAdapter.ViewHolder> {

    private Context mContext;
    private int mLayoutId;
    private List<Transport> mTransports;

    static class ViewHolder extends RecyclerView.ViewHolder {
        private View rootView;
        private TextView routeNumberTextView;
        private TextView departureStationTextView;
        private TextView destinationStationTextView;
        private TextView firstTransportTimeTextView;
        private TextView lastTransportTimeTextView;

        ViewHolder(View view) {
            super(view);
            rootView = view;
            routeNumberTextView = (TextView) view.findViewById(R.id.route_number_text_view);
            departureStationTextView = (TextView) view.findViewById(R.id.departure_station_text_view);
            destinationStationTextView = (TextView) view.findViewById(R.id.destination_station_text_view);
            firstTransportTimeTextView = (TextView) view.findViewById(R.id.first_transport_time_text_view);
            lastTransportTimeTextView = (TextView) view.findViewById(R.id.last_transport_time_text_view);
        }
    }

    /**
     * Constructor
     *
     * @param layoutId   Layout file id of each row of the RecyclerView
     * @param transports A list of transport items to be displayed on the given layout
     */
    public TransportRecyclerViewAdapter(int layoutId, List<Transport> transports) {
        mLayoutId = layoutId;
        mTransports = transports;

        if (mTransports == null) {
            mTransports = new ArrayList<>();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View rootView = LayoutInflater.from(mContext).inflate(mLayoutId, parent, false);
        return new ViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Transport transport = mTransports.get(position);

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, TimetableActivity.class);
                intent.putExtra("Transport", transport);
                mContext.startActivity(intent);
            }
        });

        holder.routeNumberTextView.setText(transport.getRouteNumber());
        holder.departureStationTextView.setText(transport.getDepartureStation());
        holder.destinationStationTextView.setText(transport.getDestinationStation());

        // TODO: Add fields in the transport object
        holder.firstTransportTimeTextView.setText("First bus: 5am");
        holder.lastTransportTimeTextView.setText("Last bus: 11pm");
    }

    @Override
    public int getItemCount() {
        return (mTransports == null) ? 0 : mTransports.size();
    }
}