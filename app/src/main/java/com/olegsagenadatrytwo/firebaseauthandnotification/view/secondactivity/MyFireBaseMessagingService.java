package com.olegsagenadatrytwo.firebaseauthandnotification.view.secondactivity;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFireBaseMessagingService extends FirebaseMessagingService {

    public static final String TAG = "MyFireBaseMessaging";

    public MyFireBaseMessagingService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        for(Map.Entry<String, String> entry : remoteMessage.getData().entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            Log.d(TAG, "onMessageReceived: Key:" + key + " " + "Value:" + value);
        }


        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }
}
