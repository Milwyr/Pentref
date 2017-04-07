package com.ywca.pentref.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.icu.text.StringSearch;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
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
import com.ywca.pentref.common.UpdateBookmarkAsyncTask;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.models.Poi;
import com.ywca.pentref.models.Review;

import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class PoiDetailsActivity extends BaseActivity implements RatingBar.OnRatingBarChangeListener, View.OnClickListener {
    private final int REQUEST_CODE_REVIEW_ACTIVITY = 9000;

    private Locale mLocale;
    private Poi mSelectedPoi;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInAccount mGoogleSignInAccount;

    private FloatingActionButton mBookmarkFab;
    private RatingBar mUserReviewRatingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_details);
        FacebookSdk.sdkInitialize(this);

        if (getIntent() != null) {
            mSelectedPoi = getIntent().getParcelableExtra(Utility.SELECTED_POI_EXTRA_KEY);
        }

        initialiseComponents();

        // Retrieve Google account if the user has signed in with Google
        OptionalPendingResult<GoogleSignInResult> opr =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            GoogleSignInResult googleSignInResult = opr.get();
            if (googleSignInResult != null) {
                mGoogleSignInAccount = googleSignInResult.getSignInAccount();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_REVIEW_ACTIVITY) {
            mSelectedPoi = data.getParcelableExtra(Utility.SELECTED_POI_EXTRA_KEY);

            if (resultCode == RESULT_OK) {
                View coordinatorLayout = findViewById(R.id.coordinator_layout);
                Snackbar.make(coordinatorLayout, getResources().getText(R.string.review_submitted), Snackbar.LENGTH_LONG).show();
            } else {
                mUserReviewRatingBar.setRating(0);
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
        collapsingToolbar.setTitle(mSelectedPoi.getName(mLocale));
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
                final boolean isPreviouslyBookmarked = (boolean) mBookmarkFab.getTag();

                // Insert or delete the bookmark from database after the user clicks on the bookmark fab
                new UpdateBookmarkAsyncTask(this, mSelectedPoi.getId()) {
                    @Override
                    protected void onPreExecute() {
                        mBookmarkFab.setEnabled(false);
                    }

                    @Override
                    protected void onPostExecute(Void v) {
                        boolean isNowBookmarked = !isPreviouslyBookmarked;
                        mBookmarkFab.setTag(isNowBookmarked);
                        mBookmarkFab.setImageResource(isNowBookmarked ?
                                R.drawable.ic_bookmarked_black_36dp : R.drawable.ic_bookmark_black_36dp);
                        mBookmarkFab.setEnabled(true);
                    }
                }.execute(isPreviouslyBookmarked);
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

                // Save user's id and name in the intent's extra
                if (Profile.getCurrentProfile() != null) {
                    intent.putExtra(Utility.USER_PROFILE_ID_EXTRA_KEY, Profile.getCurrentProfile().getId());
                    intent.putExtra(Utility.USER_PROFILE_NAME_EXTRA_KEY, Profile.getCurrentProfile().getName());
                } else if (mGoogleSignInAccount != null) {
                    intent.putExtra(Utility.USER_PROFILE_ID_EXTRA_KEY, mGoogleSignInAccount.getIdToken());
                    intent.putExtra(Utility.USER_PROFILE_NAME_EXTRA_KEY, mGoogleSignInAccount.getDisplayName());
                }

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

        mLocale = super.getDeviceLocale();

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

        // Read category name that matches the id from database,
        // and set the name to the category text view
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String[] projection = {Contract.Category.COLUMN_NAME};
                String selection = Contract.Category._ID + "= ?";
                String[] arguments = {Integer.toString(mSelectedPoi.getCategoryId())};

                Cursor cursor = getContentResolver().query(Contract.Category.CONTENT_URI,
                        projection, selection, arguments, null);

                // This case should not happen at all
                if (cursor == null) {
                    return "Not categorised";
                }

                cursor.moveToFirst();
                String categoryName = cursor.getString(cursor.getColumnIndex(Contract.Category.COLUMN_NAME));
                cursor.close();
                return categoryName;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                TextView categoryTextView = (TextView) findViewById(R.id.category_text_view);
                categoryTextView.setText(s);
            }
        }.execute();

        // Set the image of the bookmark fab depending on whether the poi is bookmarked
        mBookmarkFab = (FloatingActionButton) findViewById(R.id.bookmark_fab);
        mBookmarkFab.setOnClickListener(this);
        new InitialiseBookmarkFabAsyncTask().execute(mSelectedPoi.getId());

        // Initialise the address text view of Point of Interest
        TextView poiAddressTextView = (TextView) findViewById(R.id.poi_address_text_view);
        String address = mSelectedPoi.getAddress(mLocale);
        if (address == null || address.isEmpty()) {
            findViewById(R.id.poi_address_image_view).setVisibility(View.GONE);
            poiAddressTextView.setVisibility(View.INVISIBLE);
        } else {
            poiAddressTextView.setVisibility(View.VISIBLE);
            poiAddressTextView.setText(address);
        }

        // Initialise the website url text view of Point of Interest
        TextView poiWebsiteTextView = (TextView) findViewById(R.id.poi_website_text_view);
        String websiteUrl = mSelectedPoi.getWebsiteUri();
        if (websiteUrl == null || websiteUrl.isEmpty()) {
            findViewById(R.id.poi_website_image_view).setVisibility(View.GONE);
            poiWebsiteTextView.setVisibility(View.INVISIBLE);
        } else {
            poiWebsiteTextView.setVisibility(View.VISIBLE);

            Uri uri = Uri.parse(websiteUrl);

            if (uri.getAuthority() != null && !uri.getAuthority().isEmpty()) {
                String formattedUrl = uri.getAuthority().replace("http://", "").replace("https://", "").replace("www.", "");
                poiWebsiteTextView.setText(formattedUrl);
            }
        }

        // Initialise the phone number text view of Point of Interest
        TextView poiPhoneNumberTextView = (TextView) findViewById(R.id.poi_phone_number_text_view);
        String phoneNumber = mSelectedPoi.getPhoneNumber();
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            findViewById(R.id.poi_phone_number_image_view).setVisibility(View.GONE);
            poiPhoneNumberTextView.setVisibility(View.INVISIBLE);
        } else {
            poiPhoneNumberTextView.setVisibility(View.VISIBLE);
            poiPhoneNumberTextView.setText(phoneNumber.replace("+852 ", ""));
        }

        // Download reviews from server
        String url = Utility.SERVER_URL + "/PostReq.php?METHOD=GET&PATH=reviews&poiId=" + mSelectedPoi.getId();
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

        mUserReviewRatingBar = (RatingBar) findViewById(R.id.user_review_rating_bar);
        mUserReviewRatingBar.setOnRatingBarChangeListener(this);

        // Initialise Google api client for Google login
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso).enableAutoManage(this, null).build();
    }

    // Returns true if the user has signed in with either Facebook or Google
    private boolean isSignedIn() {
        return Profile.getCurrentProfile() != null || mGoogleSignInAccount != null;
    }

    /**
     * Finds if the given poi id is in table Bookmark of the local SQLite database.
     * After that, updates the image for bookmark fab according to the bookmarked state.
     */
    private class InitialiseBookmarkFabAsyncTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            String poiId = strings[0];

            // Query the bookmark table with the given poi id
            Uri uriWithPoiId = Uri.withAppendedPath(
                    Contract.Bookmark.CONTENT_URI, poiId);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}