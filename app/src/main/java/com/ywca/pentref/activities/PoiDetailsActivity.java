package com.ywca.pentref.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.ywca.pentref.R;
import com.ywca.pentref.models.Poi;

public class PoiDetailsActivity extends AppCompatActivity {

//    public static final String EXTRA_NAME = "POIName";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_detail);

        Intent intent = getIntent();
//        final String POIName = intent.getStringExtra(EXTRA_NAME);


        initialiseComponents();

        loadBackdrop();
    }

    private void initialiseComponents() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Poi poi = null;
        if (getIntent() != null) {
            poi = getIntent().getParcelableExtra("SelectedPoi");
        }

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("POIName");
    }

    private void loadBackdrop() {
        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.poi30, null));
    }
}