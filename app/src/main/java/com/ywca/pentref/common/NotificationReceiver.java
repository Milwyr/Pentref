package com.ywca.pentref.common;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import com.ywca.pentref.R;

/**
 * Displays a notification when time reaches the alarm time.
 * For example, if the alarm is set at 10am, then a notification will be displayed at 10am.
 */
public class NotificationReceiver extends BroadcastReceiver {
    public static final String HOUR_OF_DAY = "hourOfDay";
    public static final String MINUTE_OF_HOUR = "minuteOfHour";
    public static final String ROUTE_NUMBER = "routeNumber";

    public NotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // This should never happen
        if (intent == null) {
            return;
        }

        // Retrieve the intent data that was set before
        int hourOfDay = intent.getIntExtra(HOUR_OF_DAY, -1);
        int minuteOfHour = intent.getIntExtra(MINUTE_OF_HOUR, -1);
        String routeNumber = intent.getStringExtra(ROUTE_NUMBER);

        String scheduledTime = hourOfDay + ":" + minuteOfHour;

        // Initialise the notification builder
        String bigTextMessage = String.format(
                context.getResources().getString(R.string.dialog_message_scheduled_bus), routeNumber) + " " + scheduledTime + ".";
        Notification.Builder builder = new Notification.Builder(context)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.dialog_title_reminder))
                .setContentText("Content")
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(new Notification.BigTextStyle().bigText(bigTextMessage))
                .setAutoCancel(true);

        // Display the notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }
}