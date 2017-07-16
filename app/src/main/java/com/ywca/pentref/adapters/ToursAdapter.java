package com.ywca.pentref.adapters;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ywca.pentref.R;
import com.ywca.pentref.activities.TourDetailsActivity;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.models.Tour;

import java.util.List;
import java.util.Locale;

/**
 * Created by Ronald on 2017/6/16.
 */

public class ToursAdapter extends RecyclerView.Adapter<ToursAdapter.ViewHolder> {
    public Context mContext;

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView tourImageView;
        public TextView tourName;
        public TextView tourDescription;
        public CardView cardView;

        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            tourImageView = (ImageView) itemView.findViewById(R.id.tourImage);
            tourName = (TextView) itemView.findViewById(R.id.tourName);
            tourDescription = (TextView) itemView.findViewById(R.id.tourDescription);
            cardView = (CardView) itemView.findViewById(R.id.tour_card_view);
        }
    }

    // Store a member variable for the contacts
    private List<Tour> mTours;


    public ToursAdapter(List<Tour> tours){
        mTours = tours;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        mContext = parent.getContext();

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.tour_row_layout, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Tour tour = mTours.get(position);
        //Get photo From firebase
        holder.tourName.setText(tour.getTourName(Locale.getDefault()));
        holder.tourDescription.setText(tour.getDescription(Locale.getDefault()));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, TourDetailsActivity.class);
                intent.putExtra(Utility.TOUR_EXTRA_KEY,tour);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTours.size();
    }
}
