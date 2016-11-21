package com.ywca.pentref.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RatingBar;

import com.ywca.pentref.R;
import com.ywca.pentref.adapters.ReviewsAdapter;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.models.Poi;
import com.ywca.pentref.models.Review;

import java.util.ArrayList;
import java.util.List;

public class PoiDetailsActivity extends AppCompatActivity implements RatingBar.OnRatingBarChangeListener {
    private final int REQUEST_CODE_REVIEW_ACTIVITY = 9000;

    private Poi mSelectedPoi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_details);

        if (getIntent() != null) {
            mSelectedPoi = getIntent().getParcelableExtra(Utility.SELECTED_POI_EXTRA_KEY);
        }

        initialiseComponents();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_REVIEW_ACTIVITY) {
            mSelectedPoi = data.getParcelableExtra(Utility.SELECTED_POI_EXTRA_KEY);

            if (resultCode == RESULT_OK) {
                View rootVIew = findViewById(R.id.main_content);
                Snackbar.make(rootVIew, getResources().getText(R.string.review_submitted), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // This code is put here as the selectedPoi instance might only be returned
        // from onActivityResult() method, in which onCreate() method is not called.
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(mSelectedPoi.getName());
    }

    private void initialiseComponents() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // TODO: Set data for views (address, website uri, phone number...)

        RatingBar userReviewRatingBar = (RatingBar) findViewById(R.id.user_review_rating_bar);
        userReviewRatingBar.setOnRatingBarChangeListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.review_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // TODO: Use live data
        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review(5, "Milton", "Very Good", "Worth visiting", null));
        reviews.add(new Review(3, "Moses", "Very Good", "Worth visiting", null));
        reviews.add(new Review(2, "Peter", "Very Good", "Worth visiting", null));
        ReviewsAdapter adapter = new ReviewsAdapter();
        adapter.setReviews(reviews);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        // Only navigate to ReviewActivity if the rating bar is clicked by the user
        if (fromUser) {
            Intent intent = new Intent(this, ReviewActivity.class);
            intent.putExtra(Utility.SELECTED_POI_EXTRA_KEY, mSelectedPoi);
            intent.putExtra(Utility.USER_REVIEW_RATING_EXTRA_KEY, rating);
            startActivityForResult(intent, REQUEST_CODE_REVIEW_ACTIVITY);
        }
    }
}