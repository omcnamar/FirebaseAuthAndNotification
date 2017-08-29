package com.olegsagenadatrytwo.firebaseauthandnotification.view.mainactivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.olegsagenadatrytwo.firebaseauthandnotification.R;
import com.olegsagenadatrytwo.firebaseauthandnotification.view.secondactivity.SecondActivity;

/**
 * Created by omcna on 8/28/2017.
 */

public class MainActivityPresenter implements MainActivityContract.Presenter, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private MainActivityContract.View view;
    private static final String TAG = "MainActivityPresenter";
    private Context context;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;

    //facebook
    private CallbackManager callbackManager;

    //google
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth firebaseAuth;

    @Override
    public void attachView(MainActivityContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    //this method will log user in through Firebase
    @Override
    public void logIn(String email, String password, MainActivity activity) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        if (task.isSuccessful()) {
                            Toast.makeText(context, "successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(context, SecondActivity.class);
                            context.startActivity(intent);
                        }
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(context, "failed", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }

    //this method will sign up user to Firebase
    @Override
    public void signUp(String email, String password, final MainActivity mainActivity) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(mainActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        if (task.isSuccessful()) {
                            Toast.makeText(mainActivity, "Successful",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(mainActivity, "Failed",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    //this method will initialize the FirebaseAuth and Listener
    @Override
    public void init(Context context, FirebaseAuth mAuth, FirebaseAuth.AuthStateListener mAuthListener) {
        this.mAuth = mAuth;
        this.mAuthListener = mAuthListener;
        this.context = context;
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
        mAuth.addAuthStateListener(mAuthListener);
    }

    //this method will setUp the facebook log in
    @Override
    public void logInWithFacebookSetUp(LoginButton loginButton, CallbackManager callbackManager, final MainActivity mainActivity) {
        this.callbackManager = callbackManager;
        // Callback registration

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        Log.d(TAG, "onSuccess: " + "manager");
                        handleFacebookAccessToken(loginResult.getAccessToken(), mainActivity);
                        goToSecondActivity(mainActivity);
                    }

                    @Override
                    public void onCancel() {
                        // App code
                        Log.d(TAG, "onCancel: " + "manager");
                        Toast.makeText(mainActivity, "failed cancel", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        Log.d(TAG, "onError: " + "manager");
                        Toast.makeText(mainActivity, "failed error", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void goToSecondActivity(MainActivity mainActivity) {
        Toast.makeText(mainActivity, "successful", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(mainActivity, SecondActivity.class);
        mainActivity.startActivity(intent);
    }

    // This method configures Google SignIn
    @Override
    public void loginWithGoogleSetUp(MainActivity mainActivity, FirebaseAuth firebaseAuth) {
        // Configure sign-in to request the user’s basic profile like name and email
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to GoogleSignIn.API and the options above.
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .enableAutoManage(mainActivity /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, options)
                .build();
        view.googleApiClientReady(mGoogleApiClient);
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    public void firebaseAuthWithGoogle(GoogleSignInAccount acct, final MainActivity mainActivity) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getIdToken());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(mainActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            goToSecondActivity(mainActivity);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(context, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token, final MainActivity mainActivity) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(mainActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            user = mAuth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    @Override
    public void removeAuthStateListener() {
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
