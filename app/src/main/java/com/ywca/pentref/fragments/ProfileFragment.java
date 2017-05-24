package com.ywca.pentref.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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

import com.amulyakhare.textdrawable.TextDrawable;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ywca.pentref.R;
import com.ywca.pentref.common.Utility;

/**
 * Reference: https://github.com/googlesamples/google-services/blob/master/android/signin/app/src/main/java/com/google/samples/quickstart/signin/SignInActivity.java#L51-L55.
 */
public class ProfileFragment extends Fragment implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String TAG = "ProfileFragment" ;
    private final int RC_SIGN_IN = 9000;

    //region Instance Variables

    // Used for Facebook login
    private CallbackManager mCallbackManager;
   // private ProfileTracker mProfileTracker;

    // Used for Google login
    private GoogleApiClient mGoogleApiClient;

    private ImageView mProfilePicture;
    private TextView mUserNameTextView;
    private SignInButton mGoogleSignInButton;
    private Button mAdminSignInBtn;
    private Button mFirebaseSignOutBtn;

    //Used for Firebase login
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mDatabase;
    //endregion
    private ProgressDialog progressDialog;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getActivity().getResources().getString(R.string.signining));
        //initialises componments for Firebase admin login
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //true when user signed in
                    updateFirebaseSignInUI(true,user);
                } else {
                   //false when user is not sign in
                    updateFirebaseSignInUI(false,null);
                }
            }
        };

        // Initialises components for Facebook login
        FacebookSdk.sdkInitialize(getActivity());
        mCallbackManager = CallbackManager.Factory.create();
//        mProfileTracker = new ProfileTracker() {
//            @Override
//            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
//                if (currentProfile != null) {
//                    // Download user's profile picture and display it
//                    ImageRequest imageRequest = new ImageRequest(
//                            Profile.getCurrentProfile().getProfilePictureUri(96, 96).toString(),
//                            new Response.Listener<Bitmap>() {
//                                @Override
//                                public void onResponse(Bitmap response) {
//                                    mProfilePicture.setImageBitmap(response);
//                                }
//                            }, 96, 96, ImageView.ScaleType.CENTER_CROP, null,
//                            new Response.ErrorListener() {
//                                @Override
//                                public void onErrorResponse(VolleyError error) {
//                                    Log.e("SignInFragment", error.getMessage());
//                                }
//                            }
//                    );
//                    Volley.newRequestQueue(getActivity(), null).add(imageRequest);
//                }
//
//                if (currentProfile == null || currentProfile.getName().isEmpty()) {
//                    mUserNameTextView.setText(getResources().getString(R.string.visitor));
//                } else {
//                    mUserNameTextView.setText(currentProfile.getName());
//                }
//            }
//        };

        // Initialise components for Google login

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1090547724167-jb7bc3191eko1v0pkuete2425curaj80.apps.googleusercontent.com")
                .requestEmail()
                .build();
        if(mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .enableAutoManage((AppCompatActivity) getActivity(), this)
                    .build();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_in, container, false);

        // Initialise the two widgets
        mProfilePicture = (ImageView) rootView.findViewById(R.id.profile_picture);
        mUserNameTextView = (TextView) rootView.findViewById(R.id.user_name_text_view);

        // The user is signed in with Facebook
//        if (Profile.getCurrentProfile() != null) {
//            // Download user's profile picture and display it
//            ImageRequest imageRequest = new ImageRequest(
//                    Profile.getCurrentProfile().getProfilePictureUri(96, 96).toString(),
//                    new Response.Listener<Bitmap>() {
//                        @Override
//                        public void onResponse(Bitmap response) {
//                            mProfilePicture.setImageBitmap(response);
//                        }
//                    }, 96, 96, ImageView.ScaleType.CENTER_CROP, null,
//                    new Response.ErrorListener() {
//                        @Override
//                        public void onErrorResponse(VolleyError error) {
//                            Log.e("SignInFragment", error.getMessage());
//                        }
//                    }
//            );
//            Volley.newRequestQueue(getActivity(), null).add(imageRequest);
//
//            // Initialise the text view with Facebook's user name
//            String userName = Profile.getCurrentProfile().getName();
//            if (!userName.isEmpty()) {
//                mUserNameTextView.setText(userName);
//            }
//        }

        LoginButton loginButton = (LoginButton) rootView.findViewById(R.id.facebook_sign_in_button);
        loginButton.setFragment(this);
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");

            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });

        mGoogleSignInButton = (SignInButton) rootView.findViewById(R.id.sign_in_button);
        mGoogleSignInButton.setOnClickListener(this);

        //mGoogleSignOutButton = (Button) rootView.findViewById(R.id.sign_out_button);
        //mGoogleSignOutButton.setOnClickListener(this);

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
                        if (!mEmail.getText().toString().isEmpty() && !mPassword.getText().toString().isEmpty()) {
                            progressDialog.show();
                            mAuth.signInWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
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
                                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            });

                        } else {
                            Toast.makeText(getActivity(),
                                    "Please enter correct email/password",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });
        mFirebaseSignOutBtn = (Button) rootView.findViewById(R.id.f_firebase_sign_out_btn);
        mFirebaseSignOutBtn.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Facebook login
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Google login
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
                Toast.makeText(getActivity(),"Google Sign In failed",Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mProfileTracker.stopTracking();
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
        } else {
            mGoogleSignInButton.setVisibility(View.VISIBLE);
            mProfilePicture.setImageResource(R.drawable.ic_person_black);
            mUserNameTextView.setText(getResources().getString(R.string.visitor));
        }
    }

    private void updateFirebaseSignInUI(boolean signedIn, final FirebaseUser user){
        if(signedIn){
            //User is signed in
            getActivity().findViewById(R.id.f_admin_sign_in_btn).setVisibility(View.GONE);
            mGoogleSignInButton.setVisibility(View.GONE);
            getActivity().findViewById(R.id.facebook_sign_in_button).setVisibility(View.GONE);
            //TODO: All sign in btn set visiblilty gone
            getActivity().findViewById(R.id.f_firebase_sign_out_btn).setVisibility(View.VISIBLE);

            //Check if user is admin
            DatabaseReference adminRef = mDatabase.getReference().child(Utility.FIREBASE_TABLE_ADMIN).child(user.getUid());
            adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //dataSnapshot.getValue is true when the user is admin
                    if(dataSnapshot.getValue() != null && (boolean) dataSnapshot.getValue()){
                        mUserNameTextView.setText(getResources().getString(R.string.admin));
                    }else{
                        String userName = user.getProviderData().get(1).getDisplayName();
                        if(userName != null) {
                            mUserNameTextView.setText(userName);
                            String firstLetter = userName.substring(0, 1);
                            TextDrawable letterDrawable = TextDrawable.builder()
                                    .buildRound(firstLetter, Color.RED);
                            mProfilePicture.setImageDrawable(letterDrawable);
                        }else{
                            mUserNameTextView.setText("null");
                            TextDrawable letterDrawable = TextDrawable.builder()
                                    .buildRound("N", Color.RED);
                            mProfilePicture.setImageDrawable(letterDrawable);
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getActivity(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            //User is signed out
            mProfilePicture.setImageResource(R.drawable.ic_person_black);
            mUserNameTextView.setText(R.string.visitor);
            mAdminSignInBtn.setVisibility(View.VISIBLE);
            mGoogleSignInButton.setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.facebook_sign_in_button).setVisibility(View.VISIBLE);
            mFirebaseSignOutBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                progressDialog.show();
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;
            case R.id.f_firebase_sign_out_btn:
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                Toast.makeText(getActivity(), "Sign out", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("ProfileFragment", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            progressDialog.dismiss();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }


    // Called when there is an error while connecting the Google API client to Google service.
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getActivity(), "Connection failed", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}