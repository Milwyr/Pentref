package com.ywca.pentref.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
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
import com.ywca.pentref.R;


/**
 * Reference: https://github.com/googlesamples/google-services/blob/master/android/signin/app/src/main/java/com/google/samples/quickstart/signin/SignInActivity.java#L51-L55.
 */
public class SignInFragment extends Fragment implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private final int RC_SIGN_IN = 9000;

    //region Instance Variables

    // Used for Facebook login
    private CallbackManager mCallbackManager;
    private AccessTokenTracker mAccessTokenTracker;

    // Used for Google login
    private GoogleApiClient mGoogleApiClient;

    private TextView mNameTextView;
    private SignInButton mGoogleSignInButton;
    private Button mGoogleSignOutButton;
    //endregion

    public SignInFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialises components for Facebook login
        FacebookSdk.sdkInitialize(getActivity());
        mCallbackManager = CallbackManager.Factory.create();
        mAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken, AccessToken currentAccessToken) {

            }
        };
        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        // Initialises components for Google login
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
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

        LoginButton loginButton = (LoginButton) rootView.findViewById(R.id.facebook_sign_in_button);
        loginButton.setReadPermissions("email");
        loginButton.setFragment(this);
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(getActivity(), "Logged in with Facebook", Toast.LENGTH_SHORT).show();
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

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

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
        mAccessTokenTracker.stopTracking();
        mGoogleApiClient.stopAutoManage((AppCompatActivity) getActivity());
        mGoogleApiClient.disconnect();
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();

            if (account != null) {
                mNameTextView.setText(account.getDisplayName());
                Toast.makeText(getActivity(), "Logged in with Google", Toast.LENGTH_SHORT).show();
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
            mNameTextView.setText("Signed Out");
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

}