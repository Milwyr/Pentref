package com.ywca.pentref.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ywca.pentref.R;

/**
 * Reference: https://github.com/googlesamples/google-services/blob/master/android/signin/app/src/main/java/com/google/samples/quickstart/signin/SignInActivity.java#L51-L55.
 */
public class ProfileFragment extends Fragment implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private final int RC_SIGN_IN = 9000;

    //region Instance Variables

    // Used for Facebook login
    private CallbackManager mCallbackManager;
    private ProfileTracker mProfileTracker;

    // Used for Google login
    private GoogleApiClient mGoogleApiClient;

    private ImageView mProfilePicture;
    private TextView mUserNameTextView;
    private SignInButton mGoogleSignInButton;
    private Button mGoogleSignOutButton;
    private Button mAdminSignInBtn;
    private Button mAdminSignoutBtn;

    //Used for Firebase login
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    //endregion

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initialises componments for Firebase admin login
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if( user != null){
                    //User is signed in
                    getActivity().findViewById(R.id.f_admin_sign_in_btn).setVisibility(View.GONE);
                    getActivity().findViewById(R.id.f_admin_sign_out_btn).setVisibility(View.VISIBLE);
                }else{
                    //User is signed out
                    getActivity().findViewById(R.id.f_admin_sign_in_btn).setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.f_admin_sign_out_btn).setVisibility(View.GONE);
                }
            }
        };

        // Initialises components for Facebook login
        FacebookSdk.sdkInitialize(getActivity());
        mCallbackManager = CallbackManager.Factory.create();
        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                if (currentProfile != null) {
                    // Download user's profile picture and display it
                    ImageRequest imageRequest = new ImageRequest(
                            Profile.getCurrentProfile().getProfilePictureUri(96, 96).toString(),
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap response) {
                                    mProfilePicture.setImageBitmap(response);
                                }
                            }, 96, 96, ImageView.ScaleType.CENTER_CROP, null,
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e("SignInFragment", error.getMessage());
                                }
                            }
                    );
                    Volley.newRequestQueue(getActivity(), null).add(imageRequest);
                }

                if (currentProfile == null || currentProfile.getName().isEmpty()) {
                    mUserNameTextView.setText(getResources().getString(R.string.visitor));
                } else {
                    mUserNameTextView.setText(currentProfile.getName());
                }
            }
        };

        // Initialise components for Google login
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .enableAutoManage((AppCompatActivity) getActivity(), this)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_in, container, false);

        // Initialise the two widgets
        mProfilePicture = (ImageView) rootView.findViewById(R.id.profile_picture);
        mUserNameTextView = (TextView) rootView.findViewById(R.id.user_name_text_view);

        // The user is signed in with Facebook
        if (Profile.getCurrentProfile() != null) {
            // Download user's profile picture and display it
            ImageRequest imageRequest = new ImageRequest(
                    Profile.getCurrentProfile().getProfilePictureUri(96, 96).toString(),
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            mProfilePicture.setImageBitmap(response);
                        }
                    }, 96, 96, ImageView.ScaleType.CENTER_CROP, null,
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("SignInFragment", error.getMessage());
                        }
                    }
            );
            Volley.newRequestQueue(getActivity(), null).add(imageRequest);

            // Initialise the text view with Facebook's user name
            String userName = Profile.getCurrentProfile().getName();
            if (!userName.isEmpty()) {
                mUserNameTextView.setText(userName);
            }
        }

        LoginButton loginButton = (LoginButton) rootView.findViewById(R.id.facebook_sign_in_button);
        loginButton.setFragment(this);
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        mGoogleSignInButton = (SignInButton) rootView.findViewById(R.id.sign_in_button);
        mGoogleSignInButton.setOnClickListener(this);

        mGoogleSignOutButton = (Button) rootView.findViewById(R.id.sign_out_button);
        mGoogleSignOutButton.setOnClickListener(this);

        //Buttons for admin login/logout
        mAdminSignInBtn = (Button) rootView.findViewById(R.id.f_admin_sign_in_btn);
        mAdminSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
                View mView = getActivity().getLayoutInflater().inflate(R.layout.fragment_profile_admin_login, null);
                final EditText mEmail = (EditText) mView.findViewById(R.id.f_profile_admin_et_email);
                final EditText mPassword = (EditText) mView.findViewById(R.id.f_profile_admin_login_et_password);
                Button mLogin = (Button) mView.findViewById(R.id.f_profile_admin_btn_login);

                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();

                mLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setCancelable(false);
                        progressDialog.setMessage(getActivity().getResources().getString(R.string.signining));
                        if(!mEmail.getText().toString().isEmpty() && !mPassword.getText().toString().isEmpty()){
                            progressDialog.show();
                            mAuth.signInWithEmailAndPassword(mEmail.getText().toString(),mPassword.getText().toString())
                                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    Toast.makeText(getActivity(), "Login Success", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    dialog.dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            });

                        }else{
                            Toast.makeText(getActivity(),
                                    "Please enter correct email/password",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });



            }
        });
        mAdminSignoutBtn = (Button) rootView.findViewById(R.id.f_admin_sign_out_btn);
        mAdminSignoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Toast.makeText(getActivity(), "Sign out", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

        OptionalPendingResult<GoogleSignInResult> opr =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult
            // will be "done" and the GoogleSignInResult will be available instantly.
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // TODO: progress dialog
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Facebook login
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Google login
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mProfileTracker.stopTracking();
        mGoogleApiClient.stopAutoManage((AppCompatActivity) getActivity());
        mGoogleApiClient.disconnect();
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();

            if (account != null) {
                // Download user's Google's profile picture and display it
                if (account.getPhotoUrl() != null) {
                    ImageRequest imageRequest = new ImageRequest(
                            account.getPhotoUrl().toString(),
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap response) {
                                    mProfilePicture.setImageBitmap(response);
                                }
                            }, 96, 96, ImageView.ScaleType.CENTER_CROP, null,
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e("SignInFragment", error.getMessage());
                                }
                            }
                    );
                    Volley.newRequestQueue(getActivity(), null).add(imageRequest);
                }

                mUserNameTextView.setText(account.getDisplayName());
            }

            updateUserInterface(true);
        } else {
            updateUserInterface(false);
        }
    }

    // Changes visibility of buttons
    private void updateUserInterface(boolean signedIn) {
        if (signedIn) {
            mGoogleSignInButton.setVisibility(View.GONE);
            mGoogleSignOutButton.setVisibility(View.VISIBLE);
        } else {
            mGoogleSignInButton.setVisibility(View.VISIBLE);
            mGoogleSignOutButton.setVisibility(View.GONE);
            mProfilePicture.setImageResource(R.drawable.ic_person_black);
            mUserNameTextView.setText(getResources().getString(R.string.visitor));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;
            case R.id.sign_out_button:
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                updateUserInterface(false);
                            }
                        });
                break;
        }
    }

    // Called when there is an error while connecting the Google API client to Google service.
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getActivity(), "Connection failed", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}