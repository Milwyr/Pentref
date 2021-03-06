package com.ywca.pentref.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

        // The default icon is bus, and hence the icon is set to ferry only when necessary
        if (transport.getTypeEnum() == Transport.TypeEnum.FERRY) {
            holder.typeIcon.setImageResource(R.drawable.ic_ferry_black_36dp);
        }

        // Launch Timetable Activity when the CardView is clicked
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, TimetableActivity.class);
                intent.putExtra(Utility.TRANSPORT_EXTRA_KEY, transport);
                mContext.startActivity(intent);
            }
        });

        holder.routeNumberTextView.setText(transport.getRouteNumber());
        holder.departureStationTextView.setText(mContext.getString(R.string.tai_o));
        holder.destinationStationTextView.setText(transport.getNonTaiODestinationStation());

        List<LocalTime> localTimes;
        // TODO: Assume the direction is from Tai O
        if (LocalDate.now().getDayOfWeek() >= DateTimeConstants.MONDAY
                && LocalDate.now().getDayOfWeek() <= DateTimeConstants.SATURDAY) {
            if (transport.getFromTaiO() != null) {
                localTimes = transport.getFromTaiO().getMonToSatTimes();
            } else {
                localTimes = transport.getToTaiO().getMonToSatTimes();
            }
        } else {
            if (transport.getFromTaiO() != null) {
                localTimes = transport.getFromTaiO().getSunAndPublicHolidayTimes();
            } else {
                localTimes = transport.getToTaiO().getSunAndPublicHolidayTimes();
            }
        }

        List<LocalTime> nextTwoDepartureTimes = Utility.getTimesAfterNow(localTimes, 2);
        if (nextTwoDepartureTimes.isEmpty()) {
            holder.nextTransportTimeTextView.setText("N/A");
            holder.secondNextTransportTimeTextView.setText("N/A");
        } else {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
            holder.nextTransportTimeTextView.setText(formatter.print(nextTwoDepartureTimes.get(0)));

            if (nextTwoDepartureTimes.size() > 1) {
                String message = mContext.getResources().getString(R.string.next_transport) + " " +
                        formatter.print(nextTwoDepartureTimes.get(1));
                holder.secondNextTransportTimeTextView.setText(message);
            } else {
                holder.secondNextTransportTimeTextView.setText("N/A");
            }
        }
    }

    @Override
    public int getItemCount() {
        return (mTransports == null) ? 0 : mTransports.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView typeIcon;
        private CardView cardView;
        private TextView routeNumberTextView;
        private TextView departureStationTextView;
        private TextView destinationStationTextView;
        private TextView nextTransportTimeTextView;
        private TextView secondNextTransportTimeTextView;

        ViewHolder(View view) {
            super(view);
            typeIcon = (ImageView) view.findViewById(R.id.transport_type_icon);
            cardView = (CardView) view.findViewById(R.id.transport_card_view);
            routeNumberTextView = (TextView) view.findViewById(R.id.route_number_text_view);
            departureStationTextView = (TextView) view.findViewById(R.id.departure_station_text_view);
            destinationStationTextView = (TextView) view.findViewById(R.id.destination_station_text_view);
            nextTransportTimeTextView = (TextView) view.findViewById(R.id.next_transport_time_text_view);
            secondNextTransportTimeTextView = (TextView) view.findViewById(R.id.second_next_transport_time_text_view);
        }
    }
}