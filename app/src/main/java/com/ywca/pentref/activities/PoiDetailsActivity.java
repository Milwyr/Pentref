package com.ywca.pentref.activities;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.ywca.pentref.R;
import com.ywca.pentref.adapters.ReviewsAdapter;
import com.ywca.pentref.common.Contract;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.models.Poi;
import com.ywca.pentref.models.Review;

import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PoiDetailsActivity extends AppCompatActivity implements RatingBar.OnRatingBarChangeListener, View.OnClickListener {
    private final int REQUEST_CODE_REVIEW_ACTIVITY = 9000;
    private final int REQUEST_CODE_GOOGLE_SIGN_IN = 9001;

    private Poi mSelectedPoi;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInResult mGoogleSignInResult;

    private FloatingActionButton mBookmarkFab;

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
        } else if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
            // Retrieve user's Google result (account)
            mGoogleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bookmark_fab:
                boolean isPreviouslyBookmarked = (boolean) mBookmarkFab.getTag();
                boolean isNowBookmarked = !isPreviouslyBookmarked;

                // Insert or delete the bookmark after the user clicks on the bookmark fab
                new AsyncTask<Boolean, Void, Boolean>() {
                    @Override
                    protected void onPreExecute() {
                        mBookmarkFab.setEnabled(false);
                    }

                    @Override
                    protected Boolean doInBackground(Boolean... booleans) {
                        boolean isBookmarked = booleans[0];

                        long poiId = mSelectedPoi.getId();

                        if (isBookmarked) {
                            // Add the poi to the bookmark table
                            ContentValues values = new ContentValues();
                            values.put(Contract.Bookmark.COLUMN_POI_ID, poiId);
                            getContentResolver().insert(Contract.Bookmark.CONTENT_URI, values);
                        } else {
                            // Delete the poi from the bookmark table
//                            Uri uriWithPoiId = Uri.withAppendedPath(
//                                    Contract.Bookmark.CONTENT_URI, Long.toString(poiId));
                            Uri uri = Contract.Bookmark.CONTENT_URI;
                            String selection = Contract.Bookmark.COLUMN_POI_ID + " = ?";
                            String[] selectionArgs = {Long.toString(poiId)};

                            getContentResolver().delete(uri, selection, selectionArgs);
                        }

                        return isBookmarked;
                    }

                    @Override
                    protected void onPostExecute(Boolean isBookmarked) {
                        super.onPostExecute(isBookmarked);
                        mBookmarkFab.setTag(isBookmarked);
                        mBookmarkFab.setImageResource(isBookmarked ? R.drawable.ic_bookmarked_black_36dp : R.drawable.ic_bookmark_black_36dp);
                        mBookmarkFab.setEnabled(true);
                    }
                }.execute(isNowBookmarked);
                break;
        }
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
                ratingBar.setRating(0);
                new AlertDialog.Builder(this)
                        .setTitle(R.string.error_not_signed_in)
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

    private void initialiseComponents() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // TODO: Dynamically choose the url
        String baseUrl = Utility.SERVER_URL + "/poi_photos/";
        // Download the header image from server
        ImageRequest imageRequest = new ImageRequest(
                baseUrl + mSelectedPoi.getHeaderImageFileName(),
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
                        Log.e("PoiDetailsActivity", "Failed to download header image.");
                    }
                });
        Volley.newRequestQueue(this).add(imageRequest);

        mBookmarkFab = (FloatingActionButton) findViewById(R.id.bookmark_fab);
        mBookmarkFab.setOnClickListener(this);
        new updateBookmarkFabAsyncTask().execute(mSelectedPoi.getId());

        // TODO: Set data for views (address, website uri, phone number...)

        // Download reviews from server
        String url = "http://comp4521p1.cse.ust.hk/reviews/review_30.json";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                        .create();
                List<Review> reviews = Arrays.asList(gson.fromJson(response.toString(), Review[].class));

                RecyclerView recyclerView = (RecyclerView) findViewById(R.id.review_recycler_view);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(PoiDetailsActivity.this));

                ReviewsAdapter adapter = new ReviewsAdapter(reviews);
                recyclerView.setAdapter(adapter);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("PoiDetailsActivity", error.getMessage());
            }
        });
        Volley.newRequestQueue(this).add(jsonArrayRequest);

        RatingBar userReviewRatingBar = (RatingBar) findViewById(R.id.user_review_rating_bar);
        userReviewRatingBar.setOnRatingBarChangeListener(this);

        // Initialise components for Google login
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Start the Google sign in intent to retrieve user's Google's profile
//        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
//        startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN);
    }

    // Returns true if the user has signed in with either Facebook or Google
    private boolean isSignedIn() {
        return Profile.getCurrentProfile() != null /*|| mGoogleSignInResult != null*/;
    }

    /**
     * Finds if the given poi id is in table Bookmark of the local SQLite database.
     * After that, updates the image for bookmark fab according to the bookmarked state.
     */
    private class updateBookmarkFabAsyncTask extends AsyncTask<Long, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Long... longs) {
            long poiId = longs[0];

            // Query the bookmark table with the given poi id
            Uri uriWithPoiId = Uri.withAppendedPath(
                    Contract.Bookmark.CONTENT_URI, Long.toString(poiId));
            Cursor cursor = getContentResolver().query(uriWithPoiId, null, null, null, null);

            // Return true if the given poi id is found in the bookmark table
            if (cursor != null) {
                cursor.close();
                return cursor.getCount() > 0;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean isBookmarked) {
            super.onPostExecute(isBookmarked);
            mBookmarkFab.setTag(isBookmarked);
            mBookmarkFab.setImageResource(isBookmarked ? R.drawable.ic_bookmarked_black_36dp : R.drawable.ic_bookmark_black_36dp);
        }
    }

    // Serialize and deserialize a ISO 8601 DateTime formatted string to a LocalDateTime object
    private class LocalDateTimeSerializer implements
            JsonDeserializer<LocalDateTime>, JsonSerializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            final String dateString = json.getAsString();

            if (dateString.length() == 0) {
                return null;
            }
            return ISODateTimeFormat.dateTimeNoMillis().parseLocalDateTime(dateString);
        }

        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            String dateString = "";
            if (src != null) {
                dateString = ISODateTimeFormat.dateTimeNoMillis().print(src);
            }
            return new JsonPrimitive(dateString);
        }
    }
}