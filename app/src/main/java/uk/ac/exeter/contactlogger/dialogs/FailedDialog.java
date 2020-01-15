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
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import uk.ac.exeter.contactlogger.R;

public class FailedDialog extends DialogFragment {

    private static final String TAG = FailedDialog.class.getName();
    private static final String MESSAGE = "message";

    public void setMessage(final String message) {
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        args.putString(MESSAGE, message);
        setArguments(args);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();
        String message = args.getString(MESSAGE);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View dialogView = View.inflate(getActivity(),R.layout.failed_layout, null);
        TextView failed_message = (TextView) dialogView.findViewById(R.id.success_txt);
        failed_message.setText(message);

        builder.setTitle(R.string.sorry);

        builder.setView(dialogView);
        builder.setPositiveButton(R.string.action_close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //dismiss dialog
                dismiss();
            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
