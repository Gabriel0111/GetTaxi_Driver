package gabriel.gettaxi_driver.controller;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import java.util.Random;

import gabriel.gettaxi_driver.R;
import gabriel.gettaxi_driver.models.backend.GetTaxiConst;

public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        final String NOTIFICATION_CHANNEL_ID = "NewOrder";
        Random r = new Random();

        NotificationChannel notificationChannel;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        final PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, Welcome.class), PendingIntent.FLAG_UPDATE_CURRENT);

        String destinationClient = intent.getStringExtra(GetTaxiConst.ClientConst.DESTINATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notification New Client", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Notification New Client");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.icon_driver)
                .setContentTitle("New Trip Available")
                .setContentText("Destination: " + destinationClient)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setContentIntent(contentIntent)
                .setContentInfo("Info");

        notificationManager.notify(r.nextInt(3000), notificationBuilder.build());
    }
}

