/*
 * Copyright (C) 2016 Tina Keil (apps4research) & Miriam Koschate-Reis.
 * All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.exeter.contactlogger.dialogs;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import java.util.Calendar;

import uk.ac.exeter.contactlogger.R;
import uk.ac.exeter.contactlogger.utils.AlarmService;
import uk.ac.exeter.contactlogger.utils.InputFilterMinMax;

public class AlarmDialog extends DialogFragment {

    private static final String TAG = AlarmDialog.class.getName();
    private EditText hour_start, hour_stop, hour_int, minute_start, minute_stop, minute_int;
    private int set_hour_start, set_min_start, set_hour_int, set_min_int;
    private SharedPreferences settings;
    private boolean val_alarm_switch;
    private AlarmManager alarmManager;
    private PendingIntent alarmStartPendingIntent;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //shared preferences
        final String prefs_name = getResources().getString(R.string.PREFS_NAME);

        // Get the AlarmManager Service
        alarmManager = (AlarmManager) getActivity().getSystemService(Activity.ALARM_SERVICE);

        // Create an Start Intent to broadcast to the AlarmService BroadcastReceiver
        Intent alarmStartIntent = new Intent(getActivity(), AlarmService.class);

        // Create an PendingIntent that holds the AlarmService starting Intent
        alarmStartPendingIntent = PendingIntent.getBroadcast(getActivity(), 0, alarmStartIntent, 0);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View dialogView = View.inflate(getActivity(),R.layout.alarm_layout, null);

        //clear start alarm input values
        Button btn_alarm_start = (Button) dialogView.findViewById(R.id.clear_start);
        Button btn_alarm_stop = (Button) dialogView.findViewById(R.id.clear_stop);
        Button btn_alarm_int = (Button) dialogView.findViewById(R.id.clear_int);
        RadioGroup alarm_active = (RadioGroup) dialogView.findViewById(R.id.alarm_active);

        //set numerical input limits to text fields
        hour_start = (EditText) dialogView.findViewById(R.id.hour_start);
        hour_start.setFilters(new InputFilter[]{new InputFilterMinMax("0", "23")});
        hour_stop = (EditText) dialogView.findViewById(R.id.hour_stop);
        hour_stop.setFilters(new InputFilter[]{new InputFilterMinMax("0", "23")});
        hour_int = (EditText) dialogView.findViewById(R.id.hour_int);
        hour_int.setFilters(new InputFilter[]{new InputFilterMinMax("0", "23")});

        minute_start = (EditText) dialogView.findViewById(R.id.minute_start);
        minute_start.setFilters(new InputFilter[]{new InputFilterMinMax("0", "59")});
        minute_stop = (EditText) dialogView.findViewById(R.id.minute_stop);
        minute_stop.setFilters(new InputFilter[]{new InputFilterMinMax("0", "59")});
        minute_int = (EditText) dialogView.findViewById(R.id.minute_int);
        minute_int.setFilters(new InputFilter[]{new InputFilterMinMax("0", "59")});

        //set hint text for text fields
        hour_start.setHint(Html.fromHtml(getString(R.string.hour_hint)));
        hour_stop.setHint(Html.fromHtml(getString(R.string.hour_hint)));
        hour_int.setHint(Html.fromHtml(getString(R.string.hour_hint)));
        minute_start.setHint(Html.fromHtml(getString(R.string.min_hint)));
        minute_stop.setHint(Html.fromHtml(getString(R.string.min_hint)));
        minute_int.setHint(Html.fromHtml(getString(R.string.min_hint)));

        //get the shared prefs
        settings = getActivity().getSharedPreferences(prefs_name, 0);
        String val_hour_start = settings.getString("alarm_hour_start", "");
        String val_min_start = settings.getString("alarm_min_start", "");
        String val_hour_stop = settings.getString("alarm_hour_stop", "");
        String val_min_stop = settings.getString("alarm_min_stop", "");
        String val_hour_int = settings.getString("alarm_hour_int", "");
        String val_min_int = settings.getString("alarm_min_int", "");
        val_alarm_switch = settings.getBoolean("alarm_switch", false);

        //add a leading 0 if length of string is one
        if (!val_hour_start.isEmpty() && val_hour_start.length() == 1) {
            val_hour_start = "0" + val_hour_start;
        }
        if (!val_min_start.isEmpty() && val_min_start.length() == 1) {
            val_min_start = "0" + val_min_start;
        }
        if (!val_hour_stop.isEmpty() && val_hour_stop.length() == 1) {
            val_hour_stop = "0" + val_hour_stop;
        }
        if (!val_min_stop.isEmpty() && val_min_stop.length() == 1) {
            val_min_stop = "0" + val_min_stop;
        }
        if (!val_hour_int.isEmpty() && val_hour_int.length() == 1) {
            val_hour_int = "0" + val_hour_int;
        }
        if (!val_min_int.isEmpty() && val_min_int.length() == 1) {
            val_min_int = "0" + val_min_int;
        }

        //set values of input fields
        if (!val_hour_start.isEmpty()) hour_start.setText(val_hour_start);
        if (!val_min_start.isEmpty()) minute_start.setText(val_min_start);
        if (!val_hour_stop.isEmpty()) hour_stop.setText(val_hour_stop);
        if (!val_min_stop.isEmpty()) minute_stop.setText(val_min_stop);
        if (!val_hour_int.isEmpty()) hour_int.setText(val_hour_int);
        if (!val_min_int.isEmpty()) minute_int.setText(val_min_int);
        if (val_alarm_switch) alarm_active.check(R.id.set_alarm_on);

        btn_alarm_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear_alarm("start");
            }
        });

        btn_alarm_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear_alarm("stop");
            }
        });

        btn_alarm_int.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear_alarm("int");
            }
        });

        alarm_active.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.set_alarm_off) {
                    val_alarm_switch = false; //off
                } else if (checkedId == R.id.set_alarm_on) {
                    val_alarm_switch = true; //on
                }
            }
        });

        builder.setTitle(R.string.title_alarm);
        builder.setMessage(R.string.alarm_message);

        builder.setView(dialogView);
        builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                //save the inputs to shared prefs
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("alarm_hour_start", hour_start.getText().toString());
                editor.putString("alarm_min_start", minute_start.getText().toString());
                editor.putString("alarm_hour_stop", hour_stop.getText().toString());
                editor.putString("alarm_min_stop", minute_stop.getText().toString());
                editor.putString("alarm_hour_int", hour_int.getText().toString());
                editor.putString("alarm_min_int", minute_int.getText().toString());
                editor.putBoolean("alarm_switch", val_alarm_switch);
                editor.apply();

                //get values from input fields and convert them to integers
                if (!hour_start.getText().toString().isEmpty()) {
                    set_hour_start = Integer.parseInt(hour_start.getText().toString());
                }
                if (!minute_start.getText().toString().isEmpty()) {
                    set_min_start = Integer.parseInt(minute_start.getText().toString());
                }
                if (!hour_int.getText().toString().isEmpty()) {
                    set_hour_int = Integer.parseInt(hour_int.getText().toString());
                }
                if (!minute_int.getText().toString().isEmpty()) {
                    set_min_int = Integer.parseInt(minute_int.getText().toString());
                }

                if (val_alarm_switch) {
                    setAlarm();
                } else {
                    cancelAlarm();
                }

            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                AlarmDialog.this.getDialog().cancel();
            }
        });

        return builder.create();
    }

    private void clear_alarm(String type) {
        switch (type) {
            case "start":
                hour_start.setText("");
                minute_start.setText("");
                break;
            case "stop":
                hour_stop.setText("");
                minute_stop.setText("");
                break;
            case "int":
                hour_int.setText("");
                minute_int.setText("");
                break;
        }
    }

    private void setAlarm() {

        //make sure a start time is set
        if (set_hour_start != 0 || set_min_start != 0) {

            long interval_ms = (set_hour_int * 3600000) + (set_min_int * 60000);

            Calendar alarmStartTime = Calendar.getInstance();
            alarmStartTime.setTimeInMillis(System.currentTimeMillis());
            alarmStartTime.set(Calendar.HOUR_OF_DAY, set_hour_start);
            alarmStartTime.set(Calendar.MINUTE, set_min_start);

            //if an interval is set choose repeating alarm
            if (interval_ms > 0) {
                //set daily alarm with interval
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmStartTime.getTimeInMillis(),
                        interval_ms, alarmStartPendingIntent);
            } else {
                //set daily alarm
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmStartTime.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, alarmStartPendingIntent);
            }
        }
    }

    private void cancelAlarm() {
        if (alarmManager != null) {
            alarmManager.cancel(alarmStartPendingIntent);
        }
    }
}
