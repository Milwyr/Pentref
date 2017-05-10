package com.ywca.pentref.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ywca.pentref.R;
import com.ywca.pentref.models.Poi;

/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {
    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;

    public AboutFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mProgress = new ProgressDialog(getActivity());
        mProgress.setMessage("Sign In");
        mProgress.setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root_view = inflater.inflate(R.layout.fragment_about, container, false);
        Button test_button = (Button) root_view.findViewById(R.id.test_btn);

        test_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("POI");

                //addPoi should have null id
                Poi addPoi = new Poi(null, "test poi2", "chineseName", "header", 2, "uri", "address", "chineseAddress", "34223233", new LatLng(0, 0));

                //Add new POI example
                myRef.push().setValue(addPoi).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("About", "Add Success!");
                        Toast.makeText(getActivity(), "New POI added", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("About", e.getMessage());
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                //update a poi with id(string) "2240"
                Poi updatePoi = new Poi("2240", "my ld name", "test2", "ff.png",
                        2, "ddd", "eee", "fff", "1234234", new LatLng(0, 0));
                //Update Example
                myRef.child(updatePoi.getId()).setValue(updatePoi)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i("About", "Update Success!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("About", e.getMessage());
                            }
                        });

            }
        });

        /*test_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //poiUrl for delete
                String poiUrl = Utility.SERVER_URL + "/PostReq.php?Method=DEL&PATH=pois&UID=20161217";
//                /
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
                    poiJsonObject.put("id",4444);

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




*/
        Button loginBtn = (Button) root_view.findViewById(R.id.f_about_btn_login);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress.show();
                mAuth.signInWithEmailAndPassword("test@gmail.com", "123456")
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Log.d("Loginfirebase", "Success!");
                                mProgress.dismiss();
                                Toast.makeText(getActivity(), "Success!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mProgress.dismiss();
                                Toast.makeText(getActivity(), "Fail to login", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        Button signoutBtn = (Button) root_view.findViewById(R.id.f_about_btn_signout);
        signoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getActivity(), "SignOut!", Toast.LENGTH_SHORT).show();
            }
        });


        // Inflate the layout for this fragment
        return root_view;
    }


}