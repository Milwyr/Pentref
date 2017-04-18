package com.ywca.pentref.activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.GsonBuilder;
import com.ywca.pentref.R;
import com.ywca.pentref.common.Contract;
import com.ywca.pentref.common.PentrefProvider;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.models.Poi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddPoiActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView poiLatitude;
    private TextView poiLongitude;

    private EditText nameEt;
    private EditText chineseNameEt;
    private EditText headerImageFileNameEt;
    private EditText categoryIdEt;
    private EditText websiteUriEt;
    private EditText addressEt;
    private EditText chineseAddressEt;
    private EditText phoneNumberEt;
    private ImageView mPreviewImage;

    private double latitude;
    private double longitude;

    private boolean mPoiFlag = false;
    private boolean mPictureFlag = false;

    private ProgressDialog progress;

    private DatabaseReference mDatabaseRef;
    private StorageReference mStorageRef;

    private Bitmap mImageBitmap;

    private int onCompleteCount = 0;

    String mCurrentPhotoPath;


    static final int REQUEST_IMAGE_CAPTURE = 1;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_poi);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        poiLatitude = (TextView) findViewById(R.id.PoiLatitude);
        poiLongitude = (TextView) findViewById(R.id.PoiLongitude);
        if(getIntent() != null){
            poiLatitude.setText(""+getIntent().getDoubleExtra(Utility.ADMIN_SELECTED_LOCATION_LATITUDE,0));
            latitude = getIntent().getDoubleExtra(Utility.ADMIN_SELECTED_LOCATION_LATITUDE,0);
            poiLongitude.setText(""+getIntent().getDoubleExtra(Utility.ADMIN_SELECTED_LOCATION_LONGITUDE,0));
            longitude = getIntent().getDoubleExtra(Utility.ADMIN_SELECTED_LOCATION_LONGITUDE,0);
        }


        nameEt = (EditText) findViewById(R.id.a_add_poi_et_name);
        chineseNameEt = (EditText) findViewById(R.id.a_add_poi_et_chinese_name);
        headerImageFileNameEt = (EditText) findViewById(R.id.a_add_poi_et_header_image_file_name);
        categoryIdEt = (EditText) findViewById(R.id.a_add_poi_et_category_id);
        websiteUriEt = (EditText) findViewById(R.id.a_add_poi_et_website_uri);
        addressEt = (EditText) findViewById(R.id.a_add_poi_et_address);
        chineseAddressEt = (EditText) findViewById(R.id.a_add_poi_et_chinese_address);
        phoneNumberEt = (EditText) findViewById(R.id.a_add_poi_et_phone_number);
        mPreviewImage = (ImageView) findViewById(R.id.a_add_poi_preview_image);

        ImageButton cameraButton = (ImageButton) findViewById(R.id.a_add_poi_img_btn_camera);
        cameraButton.setOnClickListener(this);

        Button submit = (Button) findViewById(R.id.a_add_poi_btn_submit);
        submit.setOnClickListener(this);

        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Adding new POI");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //This function will startActivityForResult to the user camera
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Log.i("camera","inDispatch");
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.ywca.pentref.file.provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
    private void setPic() {
        // Get the dimensions of the View
        int targetW = mPreviewImage.getWidth();
        int targetH = mPreviewImage.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mPreviewImage.setImageBitmap(bitmap);
    }



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
           setPic();
        }
    }

    //Check if both poi and picture is completed the upload
    private void onComplete(){
        onCompleteCount++;
        if(mPoiFlag && mPictureFlag){
            Toast.makeText(AddPoiActivity.this, "Both Success!", Toast.LENGTH_SHORT).show();
            progress.dismiss();
            finish();
        }else if(onCompleteCount >= 2){
            progress.dismiss();
            Toast.makeText(AddPoiActivity.this,"Fail",Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.a_add_poi_btn_submit:
                //Create all input
                Log.i("AddPoiActivity","submit clicked");
                String name = nameEt.getText().toString();
                String chineseName = chineseNameEt.getText().toString();
                String headerImageFileName = headerImageFileNameEt.getText().toString();
                Integer categoryId = Integer.parseInt(categoryIdEt.getText().toString());
                String websiteUri = websiteUriEt.getText().toString();
                String address = addressEt.getText().toString();
                String chineseAddress = chineseAddressEt.getText().toString();
                String phoneNumber = phoneNumberEt.getText().toString();


                //TODO: Implements the data checking
                if(name == null || chineseName ==null || headerImageFileName == null || categoryId == null ||
                        websiteUri == null || address == null || chineseAddress == null || phoneNumber == null){
                    Toast.makeText(AddPoiActivity.this,"Please enter all data",Toast.LENGTH_SHORT).show();
                }


                //add poi to firebase
                progress.show();
                //Create tha poi to be add
                final Poi addPoi = new Poi(null,name,chineseName,headerImageFileName,categoryId.intValue()
                        ,websiteUri,address,chineseAddress,phoneNumber,new LatLng(latitude,longitude));
                DatabaseReference newPoiRef =  mDatabaseRef.child("POI").push();
                //get the push key of the poi
                final String poiKey = newPoiRef.getKey();
                newPoiRef.setValue(addPoi).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("AddPoActivity",poiKey);
                    addPoi.setId(poiKey);
                    ContentValues poiValues = PentrefProvider.getContentValues(addPoi);
                    try {
                        getContentResolver().insert(Contract.Poi.CONTENT_URI, poiValues);
                        mPoiFlag = true;
                    }catch (Exception e){
                        Log.e("AddPoiActivity",e.getMessage());
                    }
                    Toast.makeText(AddPoiActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                    onComplete();
                }
                  }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddPoiActivity.this, "Fail to add poi", Toast.LENGTH_SHORT).show();
                    onComplete();
                }});
                //endregion

                //Add picture to firebase storage
                StorageReference poisRef = mStorageRef.child("images/"+headerImageFileName);
                try {
                    InputStream stream = new FileInputStream(new File(mCurrentPhotoPath));
                    UploadTask uploadTask = poisRef.putStream(stream);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            onComplete();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                            mPictureFlag = true;
                            onComplete();
                        }
                    });
                }catch(FileNotFoundException e){
                    //For java compiler
                }

                break;

            case R.id.a_add_poi_img_btn_camera:
                dispatchTakePictureIntent();
                break;
            default:
                break;
        }

    }
}
