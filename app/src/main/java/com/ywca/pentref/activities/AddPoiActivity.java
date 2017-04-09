package com.ywca.pentref.activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.gson.GsonBuilder;
import com.ywca.pentref.R;
import com.ywca.pentref.common.Contract;
import com.ywca.pentref.common.PentrefProvider;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.models.Poi;

import org.json.JSONException;
import org.json.JSONObject;

public class AddPoiActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView poiLatitude;
    private TextView poiLongitude;

    private EditText idEt;
    private EditText nameEt;
    private EditText chineseNameEt;
    private EditText headerImageFileNameEt;
    private EditText categoryIdEt;
    private EditText websiteUriEt;
    private EditText addressEt;
    private EditText chineseAddressEt;
    private EditText phoneNumberEt;

    private double latitude;
    private double longitude;

    private ProgressDialog progress;

    private DatabaseReference mDatabaseRef;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_poi);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        poiLatitude = (TextView) findViewById(R.id.PoiLatitude);
        poiLongitude = (TextView) findViewById(R.id.PoiLongitude);
        if(getIntent() != null){
            poiLatitude.setText(""+getIntent().getDoubleExtra(Utility.ADMIN_SELECTED_LOCATION_LATITUDE,0));
            latitude = getIntent().getDoubleExtra(Utility.ADMIN_SELECTED_LOCATION_LATITUDE,0);
            poiLongitude.setText(""+getIntent().getDoubleExtra(Utility.ADMIN_SELECTED_LOCATION_LONGITUDE,0));
            longitude = getIntent().getDoubleExtra(Utility.ADMIN_SELECTED_LOCATION_LONGITUDE,0);
        }


        idEt = (EditText) findViewById(R.id.a_add_poi_et_id);
        nameEt = (EditText) findViewById(R.id.a_add_poi_et_name);
        chineseNameEt = (EditText) findViewById(R.id.a_add_poi_et_chinese_name);
        headerImageFileNameEt = (EditText) findViewById(R.id.a_add_poi_et_header_image_file_name);
        categoryIdEt = (EditText) findViewById(R.id.a_add_poi_et_category_id);
        websiteUriEt = (EditText) findViewById(R.id.a_add_poi_et_website_uri);
        addressEt = (EditText) findViewById(R.id.a_add_poi_et_address);
        chineseAddressEt = (EditText) findViewById(R.id.a_add_poi_et_chinese_address);
        phoneNumberEt = (EditText) findViewById(R.id.a_add_poi_et_phone_number);

        Button submit = (Button) findViewById(R.id.a_add_poi_btn_submit);
        submit.setOnClickListener(this);

        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Adding new POI");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog

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
                    progress.dismiss();
                    Log.d("AddPoActivity",poiKey);
                    addPoi.setId(poiKey);
                    ContentValues poiValues = PentrefProvider.getContentValues(addPoi);
                    try {
                        getContentResolver().insert(Contract.Poi.CONTENT_URI, poiValues);
                    }catch (Exception e){
                        Log.e("AddPoiActivity",e.getMessage());
                    }
                    Toast.makeText(AddPoiActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                }
                  }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddPoiActivity.this, "Fail to add poi", Toast.LENGTH_SHORT).show();
                    progress.dismiss();
                }});







                break;
        }

    }
}
