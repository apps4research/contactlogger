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
public class missingValues extends DialogFragment {

    private static final String TAG = missingValues.class.getName();
    private static final String ERR_MESSAGE = "message";
    private static final String ERR_NUMS = "errors";
    private String positive_btn, negative_btn, title;

    public interface onMissingValuesResultListener {
        void onMissingResult(int result);
    }

    public void setMessage(final String message, final int num_errors) {
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        args.putString(ERR_MESSAGE, message);
        args.putInt(ERR_NUMS, num_errors);
        setArguments(args);
    }

    private onMissingValuesResultListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (onMissingValuesResultListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement onMissingValButtonsResultListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();
        String message = args.getString(ERR_MESSAGE);
        int errors = args.getInt(ERR_NUMS);
        positive_btn = getString(R.string.change);

        if (errors > 1) {
            //plural
            negative_btn = getString(R.string.decline_all);
            title = getString(R.string.dialog_novalues_title);
        } else {
            //singular
            negative_btn = getString(R.string.decline_one);
            title = getString(R.string.dialog_novalue_title);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message);

        //cancel and change value(s)
        builder.setPositiveButton(positive_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                mListener.onMissingResult(Activity.RESULT_CANCELED); //Change Value
                dismiss();
            }
        });

        //no response ... does not wish to answer
        builder.setNegativeButton(negative_btn, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mListener.onMissingResult(Activity.RESULT_OK); //Decline response
            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}