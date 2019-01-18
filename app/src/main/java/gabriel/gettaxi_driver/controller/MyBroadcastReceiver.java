package gabriel.gettaxi_driver.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int MY_NOTFICATION_ID;

        final String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

        //final String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

      /*  @SuppressLint("WrongConstant")

        NotificationChannel notificationChannel;

        NotificationManager notificationManager;



        final PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, DriverActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        String destination = intent.getStringExtra("dest");

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notification {s", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Channel description");

            notificationChannel.enableLights(true);

            notificationChannel.setLightColor(Color.CYAN);

            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});

            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);

        }

        final NotificationCompat.Builder b = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);




        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.logo7_hdpi)
                .setContentTitle("New Waiting Order")
                .setContentText("You have a  new Waiting Order !!!  "  +  destination )
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setContentIntent(contentIntent)
                .setContentInfo("Info");


        notificationManager.notify(1, b.build());
    }


*/

}}

