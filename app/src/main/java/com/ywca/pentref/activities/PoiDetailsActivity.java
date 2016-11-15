package com.ywca.pentref.activities;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.ywca.pentref.R;
import com.ywca.pentref.adapters.ReviewsRecyclerViewAdapter;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.models.Poi;
import com.ywca.pentref.models.Review;

import java.util.ArrayList;
import java.util.List;

public class PoiDetailsActivity extends AppCompatActivity {

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

    private void initialiseComponents() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(mSelectedPoi.getName());

        // TODO: Set data for views (address, website uri, phone number...)

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.review_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // TODO: Use live data
        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review(5, "Milton", "Very Good", "Worth visiting", null));
        reviews.add(new Review(3, "Moses", "Very Good", "Worth visiting", null));
        reviews.add(new Review(2, "Peter", "Very Good", "Worth visiting", null));
        ReviewsRecyclerViewAdapter adapter = new ReviewsRecyclerViewAdapter();
        adapter.setReviews(reviews);
        recyclerView.setAdapter(adapter);
    }
}