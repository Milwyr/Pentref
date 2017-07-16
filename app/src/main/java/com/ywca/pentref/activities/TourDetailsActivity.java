package com.ywca.pentref.activities;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ywca.pentref.R;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.models.Tour;

public class TourDetailsActivity extends BaseActivity {
    public Tour mTour;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.a_tour_details_toolbar);
        setSupportActionBar(toolbar);
        if(getIntent() != null){
            mTour = getIntent().getParcelableExtra(Utility.TOUR_EXTRA_KEY);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.a_tour_details_collapsing_toolbar);
        toolbarLayout.setTitle(mTour.getTourName(getDeviceLocale()));
    }
}
