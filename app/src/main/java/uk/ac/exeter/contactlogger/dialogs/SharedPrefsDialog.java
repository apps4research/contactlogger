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

/**
 * Created by apps4research on 2015-11-12.
 */
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
