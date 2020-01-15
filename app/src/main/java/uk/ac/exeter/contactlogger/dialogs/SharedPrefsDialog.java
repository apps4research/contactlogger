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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.FrameLayout;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import uk.ac.exeter.contactlogger.R;

public class SharedPrefsDialog extends DialogFragment {

    private static final String TAG = SharedPrefsDialog.class.getName();
    String pref_details = "";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //shared preferences
        String prefs_name = getResources().getString(R.string.PREFS_NAME);
        SharedPreferences settings = getActivity().getSharedPreferences(prefs_name, 0);

        //get shared preferences and show them
        Map<String, ?> allEntries = settings.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {

            String key = entry.getKey();
            String value = entry.getValue().toString();

            if (key.equals("last_known_lat")) {
                value = String.valueOf(Double.longBitsToDouble(
                        settings.getLong("last_known_lat", 0)));
            }
            if (key.equals("last_known_lng")) {
                value = String.valueOf(Double.longBitsToDouble(
                        settings.getLong("last_known_lng", 0)));
            }

            if (key.equals("daily_last_shown")) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault());
                value = dateFormat.format(settings.getLong("daily_last_shown", 0));
            }

            pref_details = pref_details + key + ": " + value + "\n";
        }

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        FrameLayout container = new FrameLayout(getActivity());

        builder.setTitle(R.string.title_sharedprefs);
        builder.setMessage(pref_details.trim());

        builder.setView(container);
        builder.setNegativeButton(R.string.action_close, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SharedPrefsDialog.this.getDialog().cancel();
            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
