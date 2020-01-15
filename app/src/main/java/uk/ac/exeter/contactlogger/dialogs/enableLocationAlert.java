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
public class enableLocationAlert extends DialogFragment {

    public interface OnLocationDialogResultListener {
        void onLocationDialogResult(int resultCode);
    }

    private OnLocationDialogResultListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (OnLocationDialogResultListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, so throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement OnGpsDialogResultListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.dialog_location_off_title);
        builder.setMessage(R.string.dialog_location_dialog_text);
        builder.setPositiveButton(R.string.location_services, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mListener.onLocationDialogResult(Activity.RESULT_OK);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mListener.onLocationDialogResult(Activity.RESULT_CANCELED);
                dismiss();
            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}