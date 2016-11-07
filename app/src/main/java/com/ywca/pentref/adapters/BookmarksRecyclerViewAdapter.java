package com.ywca.pentref.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ywca.pentref.R;
import com.ywca.pentref.models.Poi;

import java.util.ArrayList;
import java.util.List;

/**
 * An adapter that provides a displays a list of POIs (Points of Interest) on a RecyclerView using the given layout.
 */
public class BookmarksRecyclerViewAdapter extends
        RecyclerView.Adapter<BookmarksRecyclerViewAdapter.ViewHolder> {

    private Context mContext;
    private int mLayoutId;
    private List<Poi> mPois;

    public BookmarksRecyclerViewAdapter(int layoutId, List<Poi> pois) {
        mLayoutId = layoutId;
        mPois = pois;
        if (mPois == null) {
            mPois = new ArrayList<>();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View rootView = LayoutInflater.from(mContext).inflate(mLayoutId, parent, false);
        return new ViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Poi poi = mPois.get(position);
        holder.itemView.setTag(position);
        holder.placeNameTextView.setText(poi.getName());
        holder.addressTextView.setText(poi.getAddress());
        holder.bookmarkedIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removePoi(poi.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPois.size();
    }

    private void removePoi(long poiId) {
        int position = 0;
        for (Poi poi: mPois) {
            if (poiId == poi.getId()) {
                mPois.remove(position);
                notifyItemRemoved(position);
                return;
            }
            position++;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView poiTypeImageView;
        private TextView placeNameTextView;
        private TextView addressTextView;
        private ImageView bookmarkedIconImageView;

        ViewHolder(View view) {
            super(view);
            poiTypeImageView = (ImageView) view.findViewById(R.id.poi_type_row_icon);
            placeNameTextView = (TextView) view.findViewById(R.id.place_name_text_view);
            addressTextView = (TextView) view.findViewById(R.id.address_text_view);
            bookmarkedIconImageView = (ImageView) view.findViewById(R.id.bookmarked_image_view);
        }
    }
}