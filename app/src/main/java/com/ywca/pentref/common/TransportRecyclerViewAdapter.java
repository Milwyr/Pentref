package com.ywca.pentref.common;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ywca.pentref.BR;
import com.ywca.pentref.activities.MainActivity;
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
        private ViewDataBinding binding;

        ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        ViewDataBinding getBinding() {
            return this.binding;
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
        ViewDataBinding binding = DataBindingUtil.inflate(LayoutInflater.from(
                parent.getContext()), mLayoutId, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Transport transport = mTransports.get(position);
        holder.getBinding().setVariable(BR.transport, transport);
        holder.getBinding().executePendingBindings();
        holder.getBinding().getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, TimetableActivity.class);
                intent.putExtra("Transport", transport);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (mTransports == null) ? 0 : mTransports.size();
    }
}