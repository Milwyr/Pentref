package com.ywca.pentref.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ywca.pentref.R;
import com.ywca.pentref.activities.TimetableActivity;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.models.Transport;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * An adapter that displays a list of {@link Transport} objects on a {@link RecyclerView} using the given layout.
 */
public class TransportAdapter extends RecyclerView.Adapter<TransportAdapter.ViewHolder> {

    private Context mContext;
    private int mLayoutId;
    private List<Transport> mTransports;

    /**
     * Constructor
     *
     * @param layoutId   Layout file id of each row of the RecyclerView
     * @param transports A list of transport items to be displayed on the given layout
     */
    public TransportAdapter(int layoutId, List<Transport> transports) {
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
        holder.departureStationTextView.setText(transport.getFromTaiO().getStations().get(0));
        holder.destinationStationTextView.setText(transport.getToTaiO().getStations().get(0));

        List<LocalTime> localTimes;
        // TODO: Assume the direction is from Tai O
        if (LocalDate.now().getDayOfWeek() >= DateTimeConstants.MONDAY
                && LocalDate.now().getDayOfWeek() <= DateTimeConstants.SATURDAY) {
            localTimes = transport.getFromTaiO().getTimetable().getMonToSatTimes();
        } else {
            localTimes = transport.getFromTaiO().getTimetable().getSunAndPublicHolidayTimes();
        }

        holder.nextTwoTransportsTimeTextView.setText(getNextTwoTransportsMessage(localTimes));
    }

    // Convert the given list of LocalTime objects to a readable text
    private String getNextTwoTransportsMessage(List<LocalTime> localTimes) {
        // Get the next two departure times later than the current time
        List<LocalTime> nextTwoDepartureTimes = Utility.getTimesAfterNow(localTimes);

        // Convert the list of LocalTime objects to a readable text
        String message;
        if (nextTwoDepartureTimes.isEmpty()) {
            message = "No available transport";
        } else {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
            message = formatter.print(nextTwoDepartureTimes.get(0));

            if (nextTwoDepartureTimes.size() == 2) {
                message += ", " + formatter.print(nextTwoDepartureTimes.get(1));
            }
        }

        return message;
    }

    @Override
    public int getItemCount() {
        return (mTransports == null) ? 0 : mTransports.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private View rootView;
        private TextView routeNumberTextView;
        private TextView departureStationTextView;
        private TextView destinationStationTextView;
        private TextView nextTwoTransportsTimeTextView;

        ViewHolder(View view) {
            super(view);
            rootView = view;
            routeNumberTextView = (TextView) view.findViewById(R.id.route_number_text_view);
            departureStationTextView = (TextView) view.findViewById(R.id.departure_station_text_view);
            destinationStationTextView = (TextView) view.findViewById(R.id.destination_station_text_view);
            nextTwoTransportsTimeTextView = (TextView) view.findViewById(R.id.next_two_transports_time_text_view);
        }
    }
}