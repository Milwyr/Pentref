package com.ywca.pentref.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;


import com.ywca.pentref.R;

public class PoiDetailActiviy extends AppCompatActivity {

//    public static final String EXTRA_NAME = "POIName";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_detail);

        Intent intent = getIntent();
//        final String POIName = intent.getStringExtra(EXTRA_NAME);
        
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("POIName");

        loadBackdrop();
    }

    private void loadBackdrop() {
        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
//        Glide.with(this).load("tianleecourt").centerCrop().into(imageView);
//        imageView.setImageBitmap();
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.poi30, null));
    }

}
