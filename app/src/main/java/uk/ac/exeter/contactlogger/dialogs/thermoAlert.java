package uk.ac.exeter.contactlogger.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import uk.ac.exeter.contactlogger.R;

/**
 * Created by apps4research on 2015-11-12.
 */
public class thermoAlert extends DialogFragment {

    public interface OnThermoDialogResultListener {
        void onThermoDialogResult(int resultCode);
    }

    private OnThermoDialogResultListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //Verify that the host activity implements the callback interface
        try {
            //Instantiate the DialogListener so we can send events to the host
            mListener = (OnThermoDialogResultListener) activity;
        } catch (ClassCastException e) {
            //activity doesn't implement the interface, so throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement OnThermoDialogResultListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.dialog_novalue_title);
        builder.setMessage(R.string.dialog_thermo_text);
        //accept default value
        builder.setPositiveButton(R.string.accept_default, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mListener.onThermoDialogResult(Activity.RESULT_FIRST_USER);
            }
        });
        //no response ... does not wish to answer
        builder.setNeutralButton(R.string.decline_response, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mListener.onThermoDialogResult(Activity.RESULT_OK);
            }
        });
        //cancel and change value of thermometer
        builder.setNegativeButton(R.string.change_value, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mListener.onThermoDialogResult(Activity.RESULT_CANCELED);
                dismiss();
            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}