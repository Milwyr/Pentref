package com.ywca.pentref.activities;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ywca.pentref.R;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.models.Poi;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ReviewActivity extends BaseActivity implements View.OnClickListener {
    // The request code to open photo gallery
    private final int RC_PHOTO_GALLERY = 7000;

    private Intent mIncomingIntent;
    private List<Uri> mSelectedPhotoUris;

    private Poi mCurrentPoi;
    private float mRating;
    private String mUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        mSelectedPhotoUris = new ArrayList<>();

        if (getIntent() != null) {
            // Retrieve Point of Interest from PoiDetailsActivity, and put it in an intent
            mCurrentPoi = getIntent().getParcelableExtra(Utility.SELECTED_POI_EXTRA_KEY);
            mIncomingIntent = new Intent(this, PoiDetailsActivity.class);
            mIncomingIntent.putExtra(Utility.SELECTED_POI_EXTRA_KEY, mCurrentPoi);

            //region Customise the actionbar
            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(mCurrentPoi.getName(getDeviceLocale()));
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeAsUpIndicator(R.drawable.ic_cross_white_24dp);
            }
            //endregion

            //region Initialise widgets
            mUserName = getIntent().getStringExtra(Utility.USER_PROFILE_NAME_EXTRA_KEY);
            TextView userNameTextView = (TextView) findViewById(R.id.user_name_text_view);
            userNameTextView.setText(mUserName);

            mRating = getIntent().getFloatExtra(Utility.USER_REVIEW_RATING_EXTRA_KEY, 0);
            RatingBar userReviewRatingBar = (RatingBar) findViewById(R.id.user_review_rating_bar);
            userReviewRatingBar.setRating(mRating);

            ImageView importFromPhotoGallery = (ImageView) findViewById(R.id.import_from_photo_gallery);
            importFromPhotoGallery.setOnClickListener(this);

            Button submitButton = (Button) findViewById(R.id.submit_button);
            submitButton.setOnClickListener(this);
            //endregion
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_PHOTO_GALLERY && resultCode == RESULT_OK) {
            // The user selects one photo
            if (data.getData() != null) {
                Uri photoUri = data.getData();
                mSelectedPhotoUris.add(photoUri);

                ImageView photo1 = (ImageView) findViewById(R.id.photo1);
                photo1.setImageURI(photoUri);
            } else if (data.getClipData() != null) {
                // The user selects multiple photos
                ClipData clipData = data.getClipData();

                for (int index = 0; index < clipData.getItemCount(); index++) {
                    Uri photoUri = clipData.getItemAt(index).getUri();
                    mSelectedPhotoUris.add(photoUri);
                    ImageView photo1 = (ImageView) findViewById(R.id.photo1);
                    photo1.setImageURI(photoUri);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.dialog_message_confirm_discard_changes)
                    .setPositiveButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Does nothing, and the dialog will be dismissed
                        }
                    })
                    .setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Pass the incoming Poi instance to PoiDetailsActivity
                            setResult(RESULT_CANCELED, mIncomingIntent);
                            finish();
                        }
                    }).show();
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.import_from_photo_gallery:
                // Launch the system gallery to allow the user to choose multiple photos
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, "Select Pictures"), RC_PHOTO_GALLERY);
                break;
            case R.id.submit_button:
                if (isConnectedToInternet()) {
                    try {
                        EditText titleEditText = (EditText) findViewById(R.id.review_title_edit_text);
                        EditText descriptionEditText = (EditText) findViewById(R.id.review_description_edit_text);

                        String title = null;
                        if (titleEditText.getText() != null && titleEditText.getText().length() > 0) {
                            title = titleEditText.getText().toString();
                        }

                        String description = null;
                        if (descriptionEditText.getText() != null && descriptionEditText.getText().length() > 0) {
                            description = descriptionEditText.getText().toString();
                        }

                        if (title == null || title.isEmpty() || description == null || description.isEmpty()) {
                            View view = findViewById(R.id.relative_layout);
                            Snackbar.make(view, "Not empty", Snackbar.LENGTH_LONG).show();
                        } else {
                            // Pass the incoming Poi instance to PoiDetailsActivity
                            postReview(title, description);
                            setResult(RESULT_OK, mIncomingIntent);
                            finish();
                        }
                    } catch (JSONException e) {
                        Log.e("ReviewActivity", e.getMessage());
                    }
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.error_network_unavailable)
                            .setMessage(R.string.error_message_connect_to_internet_before_posting_review)
                            .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                }
                break;
        }
    }

    // Post review to the server
    private void postReview(String title, String description) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("poiId", mCurrentPoi.getId());
        jsonObject.put("userId", getIntent().getStringExtra(Utility.USER_PROFILE_ID_EXTRA_KEY));
        jsonObject.put("userName", mUserName);
        jsonObject.put("rating", mRating);
        jsonObject.put("title", title);
        jsonObject.put("description", description);
        jsonObject.put("timestamp", ISODateTimeFormat.dateTimeNoMillis().print(DateTime.now()));

        String url = Utility.SERVER_URL + "/PostReq.php?METHOD=INS&PATH=reviews&";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        Volley.newRequestQueue(this).add(request);
    }
}