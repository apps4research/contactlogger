package uk.ac.exeter.contactlogger.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import uk.ac.exeter.contactlogger.AdminActivity;
import uk.ac.exeter.contactlogger.R;

/**
 * Created by apps4research on 2015-11-12.
 */
public class LoginDialog extends DialogFragment {

    private static final String TAG = LoginDialog.class.getName();
    //Replace with own password
    private static final String PWD = "password";
    private String password, prefs_name;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //shared preferences
        prefs_name = getResources().getString(R.string.PREFS_NAME);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText input = new EditText(getActivity());
        input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(25)}); //limit chars
        input.setHint(R.string.password);
        input.setSingleLine();
        input.setTransformationMethod(PasswordTransformationMethod.getInstance());

        FrameLayout container = new FrameLayout(getActivity());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = convertDpToPx(10);
        params.leftMargin = convertDpToPx(20);
        params.rightMargin = convertDpToPx(20);
        input.setLayoutParams(params);
        container.addView(input);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setTitle(R.string.title_login);
        builder.setMessage(R.string.login_message);

        builder.setView(container);
        builder.setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                password = input.getText().toString();
                if (password.equals(PWD)) {

                    //save successful login state in shared prefs
                    SharedPreferences settings = getActivity().getSharedPreferences(prefs_name, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("admin_login", true).apply();

                    //launch admin activity
                    Intent launchAdminActivity = new Intent(getActivity(), AdminActivity.class);
                    startActivity(launchAdminActivity);
                } else {
                    Toast.makeText(getActivity(), R.string.login_failed,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                LoginDialog.this.getDialog().cancel();
            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private int convertDpToPx(int dp) {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
