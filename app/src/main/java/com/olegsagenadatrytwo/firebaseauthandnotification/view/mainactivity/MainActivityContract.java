package com.olegsagenadatrytwo.firebaseauthandnotification.view.mainactivity;

import android.content.Context;

import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.olegsagenadatrytwo.firebaseauthandnotification.BasePresenter;
import com.olegsagenadatrytwo.firebaseauthandnotification.BaseView;

/**
 * Created by omcna on 8/28/2017.
 */

public interface MainActivityContract {

    interface View extends BaseView {

        void googleApiClientReady(GoogleApiClient googleApiClient);

    }

    interface Presenter extends BasePresenter<View> {

        void logIn(String email, String password, MainActivity mainActivity);
        void signUp(String email, String password, MainActivity mainActivity);
        void removeAuthStateListener();
        void init(Context context, FirebaseAuth mAuth, FirebaseAuth.AuthStateListener mAuthListener);
        void logInWithFacebookSetUp(LoginButton loginButton, CallbackManager callbackManager, final MainActivity mainActivity);
        void loginWithGoogleSetUp(MainActivity mainActivity, FirebaseAuth firebaseAuth);
        void firebaseAuthWithGoogle(GoogleSignInAccount acct, MainActivity mainActivity);
    }

}
