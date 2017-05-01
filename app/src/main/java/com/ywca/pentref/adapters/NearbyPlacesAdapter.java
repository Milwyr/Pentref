package com.ywca.pentref.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ywca.pentref.R;
import com.ywca.pentref.models.Poi;

import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by happy on 1/5/2017.
 */

public class NearbyPlacesAdapter extends RecyclerView.Adapter<NearbyPlacesAdapter.ViewHolder> {
    private NearbyPlacesAdapter.OnItemClickListener mOnItemClickListener;
    private List<Pair<Poi, Float>> mPoiPairList;
    private Context mContext;

    /**
     * Interface definition for a callback to be invoked when an item in this {@link RecyclerView} has been clicked.
     */
    public interface OnItemClickListener {
        void onItemClick(Poi poi);
    }

    /**
     *  Constructor
     * @param poiPairList A list of pairs, where first={@link Poi}, second=distance
     */
    public NearbyPlacesAdapter(@NonNull List<Pair<Poi, Float>> poiPairList) {
        mPoiPairList = poiPairList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.nearby_places_row_layout, parent, false);
        mContext = parent.getContext();
        return new NearbyPlacesAdapter.ViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Pair<Poi, Float> poiPair = mPoiPairList.get(position);

        Locale locale = mContext.getResources().getConfiguration().locale;
        if (locale.getLanguage().contains("zh")) {
            holder.placeNameTextView.setText(poiPair.first.getChineseName());
        } else {
            holder.placeNameTextView.setText(poiPair.first.getName());
        }

        int distance = Math.round(poiPair.second); // in metres
        String distanceMessage = distance + "m";
        holder.distanceTextView.setText(distanceMessage);
    }

    @Override
    public int getItemCount() {
        return mPoiPairList == null ? 0 : mPoiPairList.size();
    }

    /**
     * Registers a callback to be invoked when an item in this {@link RecyclerView} has been clicked.
     * @param onItemClickListener The callback that will be invoked
     */
    public void setOnItemClickListener(NearbyPlacesAdapter.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView placeNameTextView;
        private TextView distanceTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            placeNameTextView = (TextView) itemView.findViewById(R.id.nearby_place_text_view);
            distanceTextView = (TextView) itemView.findViewById(R.id.distance_text_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mOnItemClickListener.onItemClick(mPoiPairList.get(getAdapterPosition()).first);
        }
    }
}