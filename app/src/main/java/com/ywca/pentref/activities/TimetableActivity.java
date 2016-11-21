package com.ywca.pentref.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.ywca.pentref.R;
import com.ywca.pentref.adapters.TimetableAdapter;
import com.ywca.pentref.models.Transport;

import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TimetableActivity extends AppCompatActivity implements
        View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private Transport mSelectedTransportItem;
    private TimetableAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        // The selected transport item is passed by an intent in TransportRecyclerViewAdapter
        if (getIntent() != null) {
            mSelectedTransportItem = getIntent().getParcelableExtra("Transport");
        }

        TextView routeNumberTextView = (TextView) findViewById(R.id.route_number_text_view);
        routeNumberTextView.setText(mSelectedTransportItem.getRouteNumber());

        // TODO: Let user dynamically chooses which timetable to show
        List<LocalTime> localTimes = mSelectedTransportItem.getFromTaiO().getTimetable().getMonToSatTimes();
        List<LocalTime> timesAfterNow = new ArrayList<>();

        // Only select the times that are later than now
        for (LocalTime localTime: localTimes) {
            if (localTime.isAfter(LocalTime.now())) {
                timesAfterNow.add(localTime);
            }
        }

        // Insert a list of times into the adapter for the recycler view, with three columns per row
        mAdapter = new TimetableAdapter(R.layout.timetable_row_layout, timesAfterNow);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.timetable_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(mAdapter);

        Switch showFullTimetableSwitch = (Switch) findViewById(R.id.show_full_timetable_switch);
        showFullTimetableSwitch.setOnCheckedChangeListener(this);

        Button notifyMeButton = (Button) findViewById(R.id.timetable_notification_button);
        notifyMeButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.timetable_notification_button) {
            // Show a time picker to allow the user to choose the time of notification
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int i, int i1) {
                    Notification.Builder builder = new Notification.Builder(TimetableActivity.this)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Title")
                            .setContentText("Content")
                            .setAutoCancel(true);

                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(1, builder.build());
                }
            }, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), true);
            timePickerDialog.show();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        List<LocalTime> localTimes = mSelectedTransportItem.getFromTaiO().getTimetable().getMonToSatTimes();

        if (isChecked) {
            // Display the full timetable with time slots of the whole day
            mAdapter.updateLocalTimes(localTimes);
        } else {
            // Only select the times that are later than now
            List<LocalTime> timesAfterNow = new ArrayList<>();
            for (LocalTime localTime: localTimes) {
                if (localTime.isAfter(LocalTime.now())) {
                    timesAfterNow.add(localTime);
                }
            }
            mAdapter.updateLocalTimes(timesAfterNow);
        }
    }
}