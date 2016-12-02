package com.ywca.pentref.activities;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.ywca.pentref.common.NotificationReceiver;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.models.Transport;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TimetableActivity extends AppCompatActivity implements
        CompoundButton.OnCheckedChangeListener, TimetableAdapter.OnItemClickListener {

    private Transport mSelectedTransportItem;
    private TimetableAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        // The selected transport item is passed by an intent in TransportRecyclerViewAdapter
        if (getIntent() != null) {
            mSelectedTransportItem = getIntent().getParcelableExtra(Utility.TRANSPORT_EXTRA_KEY);
        }

        TextView routeNumberTextView = (TextView) findViewById(R.id.route_number_text_view);
        routeNumberTextView.setText(mSelectedTransportItem.getRouteNumber());

        // TODO: Let user dynamically chooses which timetable to show
        List<LocalTime> localTimes = mSelectedTransportItem.getFromTaiO().getTimetable().getMonToSatTimes();
        // Only select the times that are later than now
        List<LocalTime> timesAfterNow = Utility.getTimesAfterNow(localTimes);

        // Insert a list of times into the adapter for the recycler view, with three columns per row
        mAdapter = new TimetableAdapter(R.layout.timetable_row_layout, timesAfterNow);
        mAdapter.setOnItemClickListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.timetable_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        recyclerView.setAdapter(mAdapter);

        Switch showFullTimetableSwitch = (Switch) findViewById(R.id.show_full_timetable_switch);
        showFullTimetableSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        List<LocalTime> localTimes = mSelectedTransportItem.getFromTaiO().getTimetable().getMonToSatTimes();

        if (isChecked) {
            // Display the full timetable with time slots of the whole day
            mAdapter.updateLocalTimes(localTimes);
        } else {
            // Only select the times that are later than now
            mAdapter.updateLocalTimes(Utility.getTimesAfterNow(localTimes));
        }
    }

    @Override
    public void onItemClick(final LocalTime localTime) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.dialog_message_confirm_issue_notification)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setAlarmToScheduleNotification(localTime);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    private void setAlarmToScheduleNotification(LocalTime localTime) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // Initialise intent and put the necessary data in it
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra(NotificationReceiver.HOUR_OF_DAY, localTime.getHourOfDay());
        intent.putExtra(NotificationReceiver.MINUTE_OF_HOUR, localTime.getMinuteOfHour());
        intent.putExtra(NotificationReceiver.ROUTE_NUMBER, mSelectedTransportItem.getRouteNumber());

        PendingIntent alarmIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        // Ensure the notification displays 30 minutes before the bus/ferry departs
        localTime = localTime.minusMinutes(30);

        // Convert LocalTime to a Calendar object
        Calendar calendar = Calendar.getInstance();
        calendar.set(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                localTime.getHourOfDay(),
                localTime.getMinuteOfHour(),
                0);

        // Set the alarm so the notification will be issued in NotificationReceiver
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
    }
}