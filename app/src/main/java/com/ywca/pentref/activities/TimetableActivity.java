package com.ywca.pentref.activities;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.ywca.pentref.R;
import com.ywca.pentref.adapters.TimetableAdapter;
import com.ywca.pentref.common.NotificationReceiver;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.models.Timetable;
import com.ywca.pentref.models.Transport;

import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TimetableActivity extends AppCompatActivity implements
        CompoundButton.OnCheckedChangeListener, TimetableAdapter.OnItemClickListener {

    private boolean mIsDirectionFromTaiO;

    private Transport mSelectedTransportItem;
    private TimetableAdapter mAdapter;

    private RecyclerView mTimetableRecyclerView;
    private Switch mShowFullTimetableSwitch;
    private TextView mDepartureStationTextView;
    private TextView mDestinationStationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        // The selected transport item is passed by an intent in TransportRecyclerViewAdapter
        if (getIntent() != null) {
            mSelectedTransportItem = getIntent().getParcelableExtra(Utility.TRANSPORT_EXTRA_KEY);
        }

        // The default type is bus, and so the icon is set to ferry if that is the case
        if (mSelectedTransportItem.getTypeEnum() == Transport.TypeEnum.FERRY) {
            ImageView transportTypeIcon = (ImageView) findViewById(R.id.transport_type_icon);
            transportTypeIcon.setImageResource(R.drawable.ic_ferry_black_36dp);
        }

        TextView routeNumberTextView = (TextView) findViewById(R.id.route_number_text_view);
        routeNumberTextView.setText(mSelectedTransportItem.getRouteNumber());

        mDepartureStationTextView = (TextView) findViewById(R.id.departure_station_text_view);
        mDepartureStationTextView.setText(getResources().getString(R.string.tai_o));

        mDestinationStationTextView = (TextView) findViewById(R.id.destination_station_text_view);
        mDestinationStationTextView.setText(mSelectedTransportItem.getNonTaiODestinationStation());

        ImageButton changeDirectionButton = (ImageButton) findViewById(R.id.change_direction_image_view);
        changeDirectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Swap the text between the departure station and destination station text view
                CharSequence temp = mDepartureStationTextView.getText();
                mDepartureStationTextView.setText(mDestinationStationTextView.getText());
                mDestinationStationTextView.setText(temp);
                // Negate the flag
                mIsDirectionFromTaiO = !mIsDirectionFromTaiO;
                updateTimetableAdapter();


                mShowFullTimetableSwitch.setChecked(false);
            }
        });

        mTimetableRecyclerView = (RecyclerView) findViewById(R.id.timetable_recycler_view);
        mTimetableRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));

        // Initialise the recycler view adapter with the timetable of direction 'from Tai O'
        mIsDirectionFromTaiO = true;
        mAdapter = new TimetableAdapter(R.layout.timetable_row_layout, null);
        mAdapter.setOnItemClickListener(this);
        updateTimetableAdapter();
        mTimetableRecyclerView.setAdapter(mAdapter);

        mShowFullTimetableSwitch = (Switch) findViewById(R.id.show_full_timetable_switch);
        mShowFullTimetableSwitch.setOnCheckedChangeListener(this);
    }

    // Changes the direction of the chosen transportation
    private void updateTimetableAdapter() {
        // Select the timetable based on the direction (either 'from Tai O' or 'to Tai O)
        Timetable currentTimetable = mIsDirectionFromTaiO ?
                mSelectedTransportItem.getFromTaiO() : mSelectedTransportItem.getToTaiO();
        if(currentTimetable != null) {
            List<LocalTime> localTimes = currentTimetable.getMonToSatTimes();

            // Only select the times that are later than now
            List<LocalTime> timesAfterNow = Utility.getTimesAfterNow(localTimes);
            mAdapter.updateLocalTimes(timesAfterNow);
        }else{
            mAdapter.updateLocalTimes(new ArrayList<LocalTime>());
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Timetable currentTimetable = mIsDirectionFromTaiO ?
                mSelectedTransportItem.getFromTaiO() : mSelectedTransportItem.getToTaiO();
        if(currentTimetable != null) {
            List<LocalTime> localTimes = currentTimetable.getMonToSatTimes();

            if (isChecked) {
                // Display the full timetable with time slots of the whole day
                mAdapter.updateLocalTimes(localTimes);
            } else {
                // Only select the times that are later than now
                mAdapter.updateLocalTimes(Utility.getTimesAfterNow(localTimes));
            }
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

        // Read how many minutes the notification is displayed before the transportation departs
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.pref_file_name_user_settings), MODE_PRIVATE);
        int minutes = sharedPreferences.getInt(Utility.PREF_KEY_NOTIFICATION_PREFERENCE, 30);

        // Ensure the notification will display at the specified time before the bus/ferry departs
        localTime = localTime.minusMinutes(minutes);

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