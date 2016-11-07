package com.ywca.pentref.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import com.ywca.pentref.R;
import com.ywca.pentref.adapters.TimetableRecyclerAdapter;
import com.ywca.pentref.models.Transport;

import java.util.Calendar;

public class TimetableActivity extends AppCompatActivity {

    private Transport mSelectedTransportItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        // The selected transport item is passed by an intent in TransportRecyclerViewAdapter
        if (getIntent() != null) {
            mSelectedTransportItem = getIntent().getParcelableExtra("Transport");
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.timetable_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new TimetableRecyclerAdapter(R.layout.timetable_row_layout, null));

        Button notifyMeButton = (Button) findViewById(R.id.timetable_notification_button);
        notifyMeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(TimetableActivity.this, new TimePickerDialog.OnTimeSetListener() {
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
        });
    }
}