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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import uk.ac.exeter.contactlogger.R;

public class missingValues extends DialogFragment {

    private static final String TAG = missingValues.class.getName();
    private static final String ERR_MESSAGE = "message";
    private static final String ERR_NUMS = "errors";

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
        String positive_btn = getString(R.string.change);

        String negative_btn;
        String title;
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
