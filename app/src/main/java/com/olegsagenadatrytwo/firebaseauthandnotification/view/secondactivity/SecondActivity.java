package com.olegsagenadatrytwo.firebaseauthandnotification.view.secondactivity;

import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.olegsagenadatrytwo.firebaseauthandnotification.R;
import com.olegsagenadatrytwo.firebaseauthandnotification.view.mainactivity.MainActivity;

public class SecondActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        mAuth = FirebaseAuth.getInstance();
    }

    //This method is overridden to make sure that user goes to Home screen instead of LogIn page
    //when already logged in
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    public void logOut(View view) {
        LoginManager.getInstance().logOut();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
