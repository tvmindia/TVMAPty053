package com.tech.thrithvam.partyec;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class AppFirebaseMessagingService extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
       /* // ...
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }*/

        String title;
        String description;
        if(remoteMessage.getData()!=null) {
            title = (remoteMessage.getData().get("title") == null || remoteMessage.getData().get("title").equals("")) ? "null" : remoteMessage.getData().get("title");
            description = (remoteMessage.getData().get("body") == null || remoteMessage.getData().get("body").equals("")) ? "null" : remoteMessage.getData().get("body");

            //Notification----------------------
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(AppFirebaseMessagingService.this);
            mBuilder.setSmallIcon(R.mipmap.ic_launcher);
            mBuilder.setContentTitle(title);
            mBuilder.setContentText(description);
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mBuilder.setSound(alarmSound);
            Intent resultIntent = new Intent(AppFirebaseMessagingService.this, Home.class);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(AppFirebaseMessagingService.this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            mBuilder.setAutoCancel(true);
            mNotificationManager.notify((int) remoteMessage.getSentTime(), mBuilder.build());
        }
    }
}
