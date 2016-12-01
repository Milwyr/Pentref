package com.ywca.pentref.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ywca.pentref.R;
import com.ywca.pentref.common.UpdateBookmarkAsyncTask;
import com.ywca.pentref.models.Poi;

import java.util.ArrayList;
import java.util.List;

/**
 * An adapter that a displays a list of {@link Poi} objects (Points of Interest) on a {@link RecyclerView} using the given layout.
 */
public class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.ViewHolder> {

    private Context mContext;
    private int mLayoutId;
    private List<Poi> mPois;

    public BookmarksAdapter(int layoutId, List<Poi> pois) {
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
                new AlertDialog.Builder(mContext)
                        .setMessage(R.string.dialog_message_confirm_remove_bookmark)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new UpdateBookmarkAsyncTask(mContext, poi.getId()) {
                                    @Override
                                    protected void onPostExecute(Void v) {
                                        removePoi(poi.getId());
                                    }
                                }.execute(true);
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return (mPois == null) ? 0 : mPois.size();
    }

    private void removePoi(long poiId) {
        int position = 0;
        for (Poi poi : mPois) {
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