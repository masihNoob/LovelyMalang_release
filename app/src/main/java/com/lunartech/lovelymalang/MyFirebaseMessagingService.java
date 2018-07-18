package com.lunartech.lovelymalang;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by: aryo on 12/2/16.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final int NOTIFICATION_ID = 1;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message){

        String from = message.getFrom();
        Map<String, String> data = message.getData();

        String msg;

        if (from.startsWith("/topics/")) {
            //title =  data.get("title");
            msg =  data.get("message");
        } else {
            //title =  data.get("title");
            msg =  data.get("message");
        }

        String jumlah = data.get("jumlah");
        if (jumlah != null)
        {
            int j = Integer.valueOf(jumlah);
            ShortcutBadger.applyCount(getApplicationContext(), j);
        }

        if (msg!=null) // insert into database
        {
            sendNotification(msg);
            Intent ref = new Intent(Utils.TAG);
            ref.putExtra("command", "refreshmsg");
            LocalBroadcastManager.getInstance(this).sendBroadcast(ref);
        }
    }


    private void sendNotification(String msg) {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, SplashscreenActivity.class);
        intent.putExtra("openbc", "1");
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.topeng2);

        String text = Utils.stripHtml(msg);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.topeng)
                        .setLargeIcon(icon)
                        .setContentTitle(getString(R.string.app_name))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(text))
                        .setAutoCancel(true)
                        .setContentText(text);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
