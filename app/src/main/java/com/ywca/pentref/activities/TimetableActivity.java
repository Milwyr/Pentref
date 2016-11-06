package com.ywca.pentref.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ywca.pentref.R;
import com.ywca.pentref.models.Transport;

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
    }
}