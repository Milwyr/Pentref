package com.ywca.pentref.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;

import com.ywca.pentref.R;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.models.Poi;

public class ReviewActivity extends AppCompatActivity implements View.OnClickListener {
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        if (getIntent() != null) {
            // Retrieve Point of Interest from PoiDetailsActivity, and put it in an intent
            Poi poi = getIntent().getParcelableExtra(Utility.SELECTED_POI_EXTRA_KEY);
            mIntent = new Intent(this, PoiDetailsActivity.class);
            mIntent.putExtra(Utility.SELECTED_POI_EXTRA_KEY, poi);

            // Retrieve user's rating from PoiDetailsActivity
            float rating = getIntent().getFloatExtra(Utility.USER_REVIEW_RATING_EXTRA_KEY, 0);

            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(poi.getName());
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeAsUpIndicator(R.drawable.ic_cross_white_24dp);
            }

            RatingBar userReviewRatingBar = (RatingBar) findViewById(R.id.user_review_rating_bar);
            userReviewRatingBar.setRating(rating);

            Button submitButton = (Button) findViewById(R.id.submit_button);
            submitButton.setOnClickListener(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // This intent carries the incoming Poi instance so PoiDetailsActivity can receive it
            setResult(RESULT_CANCELED, mIntent);
            finish();
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        // This intent carries the incoming Poi instance so PoiDetailsActivity can receive it
        setResult(RESULT_OK, mIntent);
        finish();
    }
}