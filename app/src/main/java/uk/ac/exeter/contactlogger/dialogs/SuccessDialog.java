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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import uk.ac.exeter.contactlogger.MainActivity;
import uk.ac.exeter.contactlogger.R;

public class SuccessDialog extends DialogFragment {

    private static final String TAG = SuccessDialog.class.getName();
    private static final String MESSAGE = "message";
    private static final String BTN = "button";

    @Override
    //hide navigation buttons at bottom of screen
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        window.getDecorView().setSystemUiVisibility(uiOptions);
    }

    public void setMessage(final String message, final int which_btn) {
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        args.putString(MESSAGE, message);
        args.putInt(BTN, which_btn);
        setArguments(args);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();
        String message = args.getString(MESSAGE);
        int which_btn = args.getInt(BTN);
        int button_type;

        if (which_btn == 0) {
            button_type = R.string.btn_next_contact;
        } else {
            button_type = R.string.btn_log_contact;
        }

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View dialogView = View.inflate(getActivity(),R.layout.success_layout, null);
        TextView success_message = (TextView) dialogView.findViewById(R.id.success_txt);
        success_message.setText(message);

        builder.setTitle(R.string.thanks);

        builder.setView(dialogView);
        builder.setPositiveButton(button_type, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //continue to main activity
                dismiss();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                getActivity().finish();
                startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.action_exit_app, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //close app
                dismiss();
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().finish();
                startActivity(intent);
            }
        });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}

