package uk.ac.exeter.contactlogger.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;

import uk.ac.exeter.contactlogger.R;

/**
 * Created by apps4research on 2015-11-12.
 */
public class OnBootAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = OnBootAlarmReceiver.class.getName();
    private long interval_ms;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            Log.d(TAG, "finished booting");

            //shared preferences
            final String prefs_name = context.getResources().getString(R.string.PREFS_NAME);

            // Get the AlarmManager Service
            AlarmManager alarmManager = (AlarmManager)
                    context.getSystemService(Activity.ALARM_SERVICE);

            // Create an Start Intent to broadcast to the AlarmService BroadcastReceiver
            Intent alarmStartIntent = new Intent(context, AlarmService.class);

            // Create an PendingIntent that holds the AlarmService starting Intent
            PendingIntent alarmStartPendingIntent = PendingIntent.
                    getBroadcast(context, 0, alarmStartIntent, 0);

            //get the shared prefs
            SharedPreferences settings = context.getSharedPreferences(prefs_name, 0);
            int alarm_hour_start = settings.getInt("alarm_hour_start", 0);
            int alarm_min_start = settings.getInt("alarm_min_start", 0);
            int alarm_hour_int = settings.getInt("alarm_hour_int", 0);
            int alarm_min_int = settings.getInt("alarm_min_int", 0);
            boolean alarm_switch = settings.getBoolean("alarm_switch", false);

            //if alarm was activated
            if (alarm_switch) {

                if (alarm_hour_int != 0 || alarm_min_int != 0) {
                    interval_ms = (alarm_hour_int * 3600000) + (alarm_min_int * 60000);
                }

                //make sure a start time is set
                if (alarm_hour_start != 0 && alarm_min_start != 0) {

                    Calendar alarmStartTime = Calendar.getInstance();
                    alarmStartTime.setTimeInMillis(System.currentTimeMillis());
                    alarmStartTime.set(Calendar.HOUR_OF_DAY, alarm_hour_start);
                    alarmStartTime.set(Calendar.MINUTE, alarm_min_start);

                    //if an interval is set choose repeating alarm
                    if (interval_ms > 0) {
                        //set daily alarm with interval
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmStartTime.getTimeInMillis(),
                                interval_ms, alarmStartPendingIntent);
                        Log.d(TAG, "set onboot interval alarm");
                    } else {
                        //set daily alarm
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmStartTime.getTimeInMillis(),
                                AlarmManager.INTERVAL_DAY, alarmStartPendingIntent);
                        Log.d(TAG, "set onboot daily alarm");
                    }
                }
            }
        }
    }
}