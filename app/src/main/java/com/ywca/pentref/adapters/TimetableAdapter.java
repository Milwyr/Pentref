package com.ywca.pentref.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ywca.pentref.R;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

/**
 * An adapter that displays the timetable of any transport in a particular direction
 * on a {@link RecyclerView} using the given layout.
 */
public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.ViewHolder> {
    private int mLayoutId;
    private List<LocalTime> mLocalTimes;

    /**
     * Constructor
     *
     * @param layoutId   The layout resource id
     * @param localTimes A list of {@link LocalTime} objects
     */
    public TimetableAdapter(int layoutId, @NonNull List<LocalTime> localTimes) {
        mLayoutId = layoutId;
        mLocalTimes = localTimes;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.timetable_row_layout, parent, false);
        return new TimetableAdapter.ViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");

        LocalTime localTime = mLocalTimes.get(position);
        holder.timeTextView.setText(formatter.print(localTime));
    }

    @Override
    public int getItemCount() {
        return (mLocalTimes == null) ? 0 : mLocalTimes.size();
    }

    /**
     * Updates the timetable, i.e. a list of {@link LocalTime} objects.
     * The method notifyDataSetChanged will be called as well.
     *
     * @param localTimes A list of {@link LocalTime} objects
     */
    public void updateLocalTimes(List<LocalTime> localTimes) {
        mLocalTimes = localTimes;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView timeTextView;

        ViewHolder(View view) {
            super(view);
            timeTextView = (TextView) view.findViewById(R.id.timetable_time_text_view);
        }
    }
}