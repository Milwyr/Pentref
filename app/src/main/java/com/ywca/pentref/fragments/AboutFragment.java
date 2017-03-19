package com.ywca.pentref.fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.util.JsonWriter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonToken;
import com.ywca.pentref.R;
import com.ywca.pentref.common.Utility;
import com.ywca.pentref.models.Poi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root_view = inflater.inflate(R.layout.fragment_about, container, false);
        Button test_button = (Button) root_view.findViewById(R.id.test_btn);
        test_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //poiUrl for delete
                String poiUrl = Utility.SERVER_URL + "/PostReq.php?Method=DEL&PATH=pois&UID=20161217";
//                //TODO: implement json object
                JSONObject poiJsonObject = new JSONObject();
//                try {
//                    poiJsonObject = new JSONObject("{\"id\": \"34\",\n" +
//                            "        \"name\": \"Tai O Cultural And Ecological Integrated Resource Centre\"}");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    poiJsonObject.put("id",1000);
//                    poiJsonObject.put("name","1234");
//                    poiJsonObject.put("chineseName","ddddd");
//                    poiJsonObject.put("headerImageFileName","123");
//                    poiJsonObject.put("categoryId",1);
//                    poiJsonObject.put("websiteUri","12345");
//                    poiJsonObject.put("address","testing address");
//                    poiJsonObject.put("chineseAdress","lkf");
//                    poiJsonObject.put("phoneNumber","2223123");
//                    poiJsonObject.put("latitiude",0);
//                    poiJsonObject.put("longitude",0);
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
                try {
                    poiJsonObject.put("id","34");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.POST, poiUrl, poiJsonObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int a = 1;
                    }
                }, new Response.ErrorListener() {
                    @Override
                    //Server side error
                    public void onErrorResponse(VolleyError error) {
                        int b = 1;
                    }
                });
//                StringRequest post = new StringRequest(Request.Method.POST, poiUrl, new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        int a = 1;
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        int b = 1;
//                    }
//                }) {
//                    @Override
//                    protected Map<String, String> getParams() throws AuthFailureError {
//
//                        Map<String, String>  poiJsonObject = new HashMap<String, String>();
//                        poiJsonObject.put("id","1000");
//                        poiJsonObject.put("name","1234");
//                        poiJsonObject.put("chineseName","ddddd");
//                        poiJsonObject.put("headerImageFileName","123");
//                        poiJsonObject.put("categoryId","1");
//                        poiJsonObject.put("websiteUri","12345");
//                        poiJsonObject.put("address","testing address");
//                        poiJsonObject.put("chineseAdress","lkf");
//                        poiJsonObject.put("phoneNumber","123123");
//                        poiJsonObject.put("latitiude","0");
//                        poiJsonObject.put("longitude","0");
//                        return poiJsonObject;
//                    }
//                };
                Volley.newRequestQueue(getActivity()).add(jsonObjectRequest);
            }
        });


        // Inflate the layout for this fragment
        return root_view;
    }


}