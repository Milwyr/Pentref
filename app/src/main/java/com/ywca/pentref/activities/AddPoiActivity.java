package com.ywca.pentref.activities;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.GsonBuilder;
import com.ywca.pentref.R;
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_poi);
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
                int id = Integer.parseInt(idEt.getText().toString());
                String name = nameEt.getText().toString();
                String chineseName = chineseNameEt.getText().toString();
                String headerImageFileName = headerImageFileNameEt.getText().toString();
                int categoryId = Integer.parseInt(categoryIdEt.getText().toString());
                String websiteUri = websiteUriEt.getText().toString();
                String address = addressEt.getText().toString();
                String chineseAddress = chineseAddressEt.getText().toString();
                String phoneNumber = phoneNumberEt.getText().toString();

                //TODO: Implements the data checking


                //Send Request
                progress.show();
                String poiUrl = Utility.SERVER_URL + "/PostReq.php?Method=INS&PATH=pois&UID=20161217";
                JSONObject poiJsonObject = new JSONObject();

                Poi newPoi = new Poi(id,name,chineseName,headerImageFileName,
                        categoryId,websiteUri,address,chineseAddress,phoneNumber,new LatLng(latitude,longitude));

                String test = new GsonBuilder().create().toJson(newPoi);
                try {
                    poiJsonObject = new JSONObject(test);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.POST, poiUrl, poiJsonObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int a = 1;
                        progress.dismiss();

                    }
                }, new Response.ErrorListener() {
                    @Override
                    //Server side error
                    public void onErrorResponse(VolleyError error) {
                        progress.dismiss();
                        int b = 1;
                    }
                });
                Volley.newRequestQueue(this).add(jsonObjectRequest);



                break;
        }

    }
}
