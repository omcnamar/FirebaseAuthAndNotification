package com.olegsagenadatrytwo.firebaseauthandnotification.view.mainactivity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TwitterAuthProvider;
import com.olegsagenadatrytwo.firebaseauthandnotification.R;
import com.olegsagenadatrytwo.firebaseauthandnotification.view.secondactivity.SecondActivity;
import com.google.android.gms.auth.api.Auth;

//face book imports
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

public class MainActivity extends AppCompatActivity implements MainActivityContract.View {

    private static final String TAG = "MainActivity";
    private MainActivityPresenter presenter;

    private EditText etUserName;
    private EditText etPassword;

    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private static final int RC_SIGN_IN = 9001;
    private String name;
    private String email;
    private SignInButton mSignInButton;

    //facebook
    private LoginButton loginButton;
    private CallbackManager callbackManager;

    //twitter
    private TwitterLoginButton mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(getString(R.string.twitterCONSUMER_KEY), getString(R.string.twitterCONSUMER_SECRET)))
                .debug(true)
                .build();
        Twitter.initialize(config);
        setContentView(R.layout.activity_main);

        //_________________________________
        mLoginButton = (TwitterLoginButton) findViewById(R.id.button_twitter_login);
        mLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d(TAG, "twitterLogin:success" + result);
                handleTwitterSession(result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.w(TAG, "twitterLogin:failure", exception);
                //updateUI(null);
            }
        });
        //________________________________

        //google sign in button
        mSignInButton = (SignInButton) findViewById(R.id.login_with_google);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "authenticateUser: " + "button clicked to sign in with google");
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        //this intent is for Firebase messaging
        Intent intent = getIntent();
        String action = intent.getStringExtra("action1");
        if (action != null) {
            switch (action) {
                case "value1":
                    toToSecondActivity();
                    break;
            }
        }

        //initialize presenter
        firebaseAuth = FirebaseAuth.getInstance();
        presenter = new MainActivityPresenter();
        presenter.init(this, firebaseAuth, authStateListener);
        presenter.attachView(this);

        //bind edit text
        etUserName = (EditText) findViewById(R.id.etUserName);
        etPassword = (EditText) findViewById(R.id.etPassword);

        //google authentication process
        presenter.loginWithGoogleSetUp(this, firebaseAuth);

        //facebook authentication process
        AppEventsLogger.activateApp(this);
        loginButton = (LoginButton) findViewById(R.id.fb_login_button);
        loginButton.setReadPermissions("email");
        callbackManager = CallbackManager.Factory.create();
        presenter.logInWithFacebookSetUp(loginButton, callbackManager, this);

    }

    private void handleTwitterSession(TwitterSession session) {
        Log.d(TAG, "handleTwitterSession:" + session);

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            toToSecondActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            toToSecondActivity();
        }
    }

    //onClick for log in and register
    public void authenticateUser(View view) {
        String userName = etUserName.getText().toString();
        String password = etPassword.getText().toString();

        //make sure user entered at least something into the email and password fields
        if (userName.length() == 0 || password.length() == 0) {
            if (userName.length() == 0) {
                Toast.makeText(this, "email missing", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "password missing", Toast.LENGTH_SHORT).show();
            }
        } else {

            //switch to determine if it is a log in or a sign up
            switch (view.getId()) {
                case R.id.btnLogIn:
                    presenter.logIn(userName, password, this);
                    break;
                case R.id.btnRegister:
                    presenter.signUp(userName, password, this);
                    break;
            }
        }
    }

    // This IS the method where the result of clicking the signIn button will be handled
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, save Token and a state then authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();

                name = account.getDisplayName();
                email = account.getEmail();
                Log.d(TAG, "onActivityResult: " + name + ", " + email);
                Toast.makeText(this, "successful", Toast.LENGTH_SHORT).show();
                presenter.firebaseAuthWithGoogle(account, this);
                //photoUri = account.getPhotoUrl();
                //photo = photoUri.toString();

            } else {
                // Google Sign In failed, update UI appropriately
                Log.d(TAG, "onActivityResult: " + "log in failed");
                Toast.makeText(this, "Login Unsuccessful", Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
            // Pass the activity result to the Twitter login button.
            mLoginButton.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void toToSecondActivity() {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }

    @Override
    public void showError(String e) {

    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.removeAuthStateListener();
    }

    @Override
    public void googleApiClientReady(GoogleApiClient googleApiClient) {
        this.mGoogleApiClient = googleApiClient;
    }
}
