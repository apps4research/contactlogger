package uk.ac.exeter.contactlogger.utils;

import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;

import uk.ac.exeter.contactlogger.MainActivity;
import uk.ac.exeter.contactlogger.R;

/**
 * Created by apps4research on 2015-11-12.
 */
public class AlarmService extends BroadcastReceiver {

    // Notification ID to allow for future updates
    private static final int CL_NOTIFICATION_ID = 1;
    private static final String TAG = AlarmService.class.getName();

    // Notification Sound and Vibration on Arrival
    private final Uri soundURI = Uri
            .parse("android.resource://uk.ac.exeter.contactlogger/" + R.raw.mindful);
    private final long[] mVibratePattern = {0, 200, 200, 300};
    private int set_hour_stop, set_min_stop;

    @Override
    public void onReceive(Context context, Intent intent) {

        //shared preferences
        final String prefs_name = context.getResources().getString(R.string.PREFS_NAME);

        //get the shared prefs
        SharedPreferences settings = context.getSharedPreferences(prefs_name, 0);
        String alarm_hour_stop = settings.getString("alarm_hour_stop", "");
        String alarm_min_stop = settings.getString("alarm_min_stop", "");
        long currentTime = System.currentTimeMillis();
        long stopTime;

        if ((!alarm_hour_stop.isEmpty() && !alarm_hour_stop.equals("00")) ||
                (!alarm_min_stop.isEmpty() && !alarm_min_stop.equals("00"))) {

            //would be equal to 0 minutes or 0/24 hours
            if (!alarm_hour_stop.isEmpty()) {
                set_hour_stop = Integer.parseInt(alarm_hour_stop);
            }
            if (!alarm_min_stop.isEmpty()) {
                set_min_stop = Integer.parseInt(alarm_min_stop);
            }

            Calendar alarmStopTime = Calendar.getInstance();
            alarmStopTime.setTimeInMillis(System.currentTimeMillis());
            alarmStopTime.set(Calendar.HOUR_OF_DAY, set_hour_stop);
            alarmStopTime.set(Calendar.MINUTE, set_min_stop);
            stopTime = alarmStopTime.getTimeInMillis();
        } else {
            //override stop time by setting it higher than current time
            stopTime = System.currentTimeMillis() + 1;
        }

        if ((currentTime < stopTime)) {

            // Notification Text Elements
            final CharSequence tickerText = context.getString(R.string.alarm_ticker);
            final CharSequence contentTitle = context.getString(R.string.alarm_title);
            final CharSequence contentText = context.getString(R.string.alarm_reminder);

            // Get the NotificationManager to update notifications
            NotificationManager mNotificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            // The Intent to be used when the user clicks on the Notification View
            Intent mNotificationIntent = new Intent(context, MainActivity.class);
            mNotificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // The PendingIntent that wraps the underlying Intent
            PendingIntent mContentIntent = PendingIntent.getActivity(context, 0, mNotificationIntent, 0);

            // Build the Notification
            Notification.Builder notificationBuilder = new Notification.Builder(context)
                    .setSmallIcon(android.R.drawable.stat_sys_warning)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                    .setLights(Color.YELLOW, 500, 500)
                    .setAutoCancel(true)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setTicker(tickerText)
                    .setContentIntent(mContentIntent)
                    .setSound(soundURI)
                    .setVibrate(mVibratePattern);

            // Pass the Notification to the NotificationManager:
            mNotificationManager.notify(CL_NOTIFICATION_ID, notificationBuilder.build());
        }
    }
}
