package com.ywca.pentref.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.ywca.pentref.R;
import com.ywca.pentref.adapters.ReviewsAdapter;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.models.Poi;
import com.ywca.pentref.models.Review;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

public class PoiDetailsActivity extends AppCompatActivity implements RatingBar.OnRatingBarChangeListener {
    private final int REQUEST_CODE_REVIEW_ACTIVITY = 9000;

    private Poi mSelectedPoi;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_details);
        FacebookSdk.sdkInitialize(this);

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

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    private void initialiseComponents() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // TODO: Dynamically choose the url
        // Download the header image from server
        ImageRequest imageRequest = new ImageRequest(
                "https://github.com/Milwyr/Temporary/blob/master/poi_photos/29_hwa_kong_temple_landscape.jpg?raw=true",
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        ImageView headerImageView = (ImageView) findViewById(R.id.header_image);
                        headerImageView.setImageBitmap(response);
                    }
                }, 1280, 720, ImageView.ScaleType.CENTER_CROP, null,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("PoiDetailsActivity", error.getMessage());
                    }
                });
        Volley.newRequestQueue(this).add(imageRequest);

        // TODO: Set data for views (address, website uri, phone number...)

        RatingBar userReviewRatingBar = (RatingBar) findViewById(R.id.user_review_rating_bar);
        userReviewRatingBar.setOnRatingBarChangeListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.review_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // TODO: Use live data
        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review(5, "Milton", "Very Good", "Worth visiting", null, LocalDateTime.now()));
        reviews.add(new Review(3, "Moses", "Very Good", "Worth visiting", null, LocalDateTime.now()));
        reviews.add(new Review(2, "Peter", "Very Good", "Worth visiting", null, LocalDateTime.now()));
        ReviewsAdapter adapter = new ReviewsAdapter();
        adapter.setReviews(reviews);
        recyclerView.setAdapter(adapter);

        // Initialise components for Google login
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        // Only navigate to ReviewActivity if the rating bar is clicked by the user
        if (fromUser) {
            if (isSignedIn()) {
                Intent intent = new Intent(this, ReviewActivity.class);
                intent.putExtra(Utility.SELECTED_POI_EXTRA_KEY, mSelectedPoi);
                intent.putExtra(Utility.USER_REVIEW_RATING_EXTRA_KEY, rating);
                startActivityForResult(intent, REQUEST_CODE_REVIEW_ACTIVITY);
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.error_network_unavailable)
                        .setMessage(R.string.error_message_sign_in_before_posting_review)
                        .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        }
    }

    // Returns true if the user has signed in with either Facebook or Google
    private boolean isSignedIn() {
        return Profile.getCurrentProfile() != null || isSignedInWithGoogle();
    }

    private boolean isSignedInWithGoogle() {
        OptionalPendingResult<GoogleSignInResult> opr =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);

        if (opr.isDone()) {
            GoogleSignInResult result = opr.get();
            return result != null;
        }
        return false;
    }
}