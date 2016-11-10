package com.ywca.pentref.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ywca.pentref.R;
import com.ywca.pentref.models.Transport;

import java.util.ArrayList;
import java.util.List;

/**
 * An adapter that displays a list of POIs (Points of Interest) on a RecyclerView using the given layout.
 */
public class TimetableRecyclerAdapter extends
        RecyclerView.Adapter<TimetableRecyclerAdapter.ViewHolder> {

    private int mLayoutId;
    private List<Transport> mTransportItems;

    public TimetableRecyclerAdapter(int layoutId, List<Transport> transportItems) {
        mLayoutId = layoutId;
        mTransportItems = transportItems;
        if (mTransportItems == null) {
            mTransportItems = new ArrayList<>();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // TODO: should use dynamic data
        holder.column1.setText("15:00");
        holder.column2.setText("15:30");
        holder.column3.setText("16:00");
        holder.column4.setText("16:30");
        holder.column5.setText("17:00");
    }

    @Override
    public int getItemCount() {
//        return mTransportItems.size();
        return 5;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView column1;
        private TextView column2;
        private TextView column3;
        private TextView column4;
        private TextView column5;

        ViewHolder(View view) {
            super(view);
            column1 = (TextView) view.findViewById(R.id.column1);
            column2 = (TextView) view.findViewById(R.id.column2);
            column3 = (TextView) view.findViewById(R.id.column3);
            column4 = (TextView) view.findViewById(R.id.column4);
            column5 = (TextView) view.findViewById(R.id.column5);
        }
    }
}