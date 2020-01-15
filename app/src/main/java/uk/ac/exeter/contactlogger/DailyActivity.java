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

package uk.ac.exeter.contactlogger;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Set;

import uk.ac.exeter.contactlogger.dialogs.FailedDialog;
import uk.ac.exeter.contactlogger.dialogs.SuccessDialog;
import uk.ac.exeter.contactlogger.utils.DBAdapter;
import uk.ac.exeter.contactlogger.dialogs.missingValues;
import uk.ac.exeter.contactlogger.utils.Utils;

public class DailyActivity extends Activity implements
        missingValues.onMissingValuesResultListener,
        View.OnClickListener {

    private final static String TAG = DailyActivity.class.getName();

    FragmentManager fm = getFragmentManager();

    private final static int missing_val = 999;
    private boolean val_missing = true;
    private int item1_val, item2_val, item3_val;
    private static TextView info_item1;
    private static TextView info_item2;
    private static TextView info_item3;
    private String prefs_name, device_id;

    //Arrays
    private final LinkedHashMap<Integer, String> ButtonCatMap_Items = new LinkedHashMap<>();
    private final LinkedHashMap<Integer, String> ButtonKeyMap_Items = new LinkedHashMap<>();
    private ArrayList<String> buttons_checked_cat_items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.daily_layout);

        prefs_name = getResources().getString(R.string.PREFS_NAME);
        device_id = Utils.getDeviceId(this);

        //remove navigation up arrow in actionbar
        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(false);

        //Text views
        TextView question = (TextView) findViewById(R.id.dailyInfo);
        question.setText(getText(R.string.dailyInfoText));
        info_item1 = (TextView) findViewById(R.id.info_item1);
        info_item2 = (TextView) findViewById(R.id.info_item2);
        info_item3 = (TextView) findViewById(R.id.info_item3);

        //set initial value of section descriptions
        if (item1_val == 0) info_item1.setText(getString(R.string.no_selection));
        if (item2_val == 0) info_item2.setText(getString(R.string.no_selection));
        if (item3_val == 0) info_item3.setText(getString(R.string.no_selection));

        //fill and iterate through ButtonMap Array
        LinkedButtonMap();
        Set<Integer> ButtonIds = ButtonCatMap_Items.keySet();

        for (int id : ButtonIds) {
            ToggleButton toggleButton = (ToggleButton) findViewById(id); //find view of each button
            toggleButton.setOnClickListener(this); //set listener for each button
        }

    }

    private void LinkedButtonMap() {
        String[] stringArray = getResources().getStringArray(R.array.ButtonDescMap_Items);
        for (String entry : stringArray) {
            String[] splitResult = entry.split("\\|", 2);
            int buttonId = getResources().getIdentifier(splitResult[0], "id", getPackageName());
            String buttonKey = splitResult[0].substring(splitResult[0].lastIndexOf("_") + 1);
            ButtonCatMap_Items.put(buttonId, splitResult[1]);
            ButtonKeyMap_Items.put(buttonId, buttonKey);
        }
    }

    public void onClick(View v) {
        Set<Integer> ButtonIds = ButtonCatMap_Items.keySet();

        //when button is clicked show corresponding description
        for (int id : ButtonIds) {
            if (id == v.getId()) {
                ToggleButton toggleButton = (ToggleButton) findViewById(id);
                String description = toggleButton.getContentDescription().toString();
                String category = ButtonCatMap_Items.get(id);

                //if button is not pressed in resp. category, don't show description text
                //and set final value of button section to 0
                if (category.equals("item1") && !toggleButton.isChecked()) {
                    info_item1.setText(getString(R.string.no_selection));
                    item1_val = 0;
                } else if (category.equals("item2") && !toggleButton.isChecked()) {
                    info_item2.setText(getString(R.string.no_selection));
                    item2_val = 0;
                } else if (category.equals("item3") && !toggleButton.isChecked()) {
                    info_item3.setText(getString(R.string.no_selection));
                    item3_val = 0;
                }

                //if button is pressed in resp. category, show description text
                //and get the value of the button that was checked
                if (category.equals("item1") && toggleButton.isChecked()) {
                    info_item1.setText(description);
                    item1_val = Integer.parseInt(ButtonKeyMap_Items.get(id));
                } else if (category.equals("item2") && toggleButton.isChecked()) {
                    info_item2.setText(description);
                    item2_val = Integer.parseInt(ButtonKeyMap_Items.get(id));
                } else if (category.equals("item3") && toggleButton.isChecked()) {
                    info_item3.setText(description);
                    item3_val = Integer.parseInt(ButtonKeyMap_Items.get(id));
                }

                setBtn(id, category);
            }
        }
    }

    private void setBtn(int button_on, String cat) {
        Set<Integer> ButtonIds = ButtonCatMap_Items.keySet();
        for (int id : ButtonIds) {
            String category = ButtonCatMap_Items.get(id);
            ToggleButton toggleButton = (ToggleButton) findViewById(id);
            // any button that belongs to cat and is not the one pressed should be turned off
            if (id != button_on && category.equals(cat)) {
                toggleButton.setChecked(false);
            }
        }
    }

    @Override
    //this is called by missingValues class in dialog folder
    public void onMissingResult(int result) {
        //user has declined responses
        if (result == Activity.RESULT_OK) { // DECLINE RESPONSE
            if (item1_val == 0) item1_val = missing_val;
            if (item2_val == 0) item2_val = missing_val;
            if (item3_val == 0) item3_val = missing_val;
            if (!validateResponses()) {
                //no missing values, so insert to DB
                insertToDB();
            }
        }
    }

    private boolean validateResponses() {

        //build error messages
        StringBuilder error_message = new StringBuilder();

        int count_errors = 0;
        buttons_checked_cat_items.clear();

        // go through array of buttons and check state of each
        Set<Integer> ButtonIds = ButtonCatMap_Items.keySet();

        for (int id : ButtonIds) {
            ToggleButton toggleButton = (ToggleButton) findViewById(id);
            String category = ButtonCatMap_Items.get(id);
            //only add buttons to array that have been checked
            if (toggleButton.isChecked()) {
                //array contains all buttons pressed from 3 categories (type, rel and exp)
                buttons_checked_cat_items.add(category);
            }
        }

        //check if a button in the type section was not pressed
        if (!buttons_checked_cat_items.contains("item1") && (item1_val != missing_val)) {
            //button in section not checked and final value not set to decline
            error_message.append("- ").append(getString(R.string.lb_item1)).append("\n");
            count_errors++;
        }

        if (!buttons_checked_cat_items.contains("item2") && (item2_val != missing_val)) {
            error_message.append("- ").append(getString(R.string.lb_item2)).append("\n");
            count_errors++;
        }

        if (!buttons_checked_cat_items.contains("item3") && (item3_val != missing_val)) {
            error_message.append("- ").append(getString(R.string.lb_item3)).append("\n");
            count_errors++;
        }

        //if a value of any buttons is missing, then show an alert dialog
        if (val_missing && count_errors > 0) {
            //prepend error message with plural or singular text versions
            if (count_errors > 1) {
                error_message = error_message.insert(0, getString(R.string.dialog_missing_plu));
            } else {
                error_message = error_message.insert(0, getString(R.string.dialog_missing_sin));
            }
            showMissingValues(String.valueOf(error_message), count_errors);
        }

        if (count_errors == 0) {
            val_missing = false;
        }
        return val_missing;
    }

    private void showMissingValues(String message, int errors) {
        missingValues MissingValuesFormFragment = new missingValues();
        MissingValuesFormFragment.setMessage(message, errors);
        MissingValuesFormFragment.show(fm, TAG);
    }

    public void logDailyActivity(View view) {
        boolean has_errors = validateResponses();
        //if validation returned false == no errors
        if (!has_errors) {
            //Call activity that logs the daily activity
            insertToDB();
        }
    }

    private void insertToDB() {
        //Current Datetime
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String DateTime = dateFormat.format(new Date());

        // add new items to db
        DBAdapter db = new DBAdapter(this);
        db.open();
        long row_id = db.insertItems(device_id, DateTime, item1_val, item2_val, item3_val);

        if (row_id > 0) {
            //save variables that daily activity was shown and saved successfully
            SharedPreferences settings = getSharedPreferences(prefs_name, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("daily_last_shown", System.currentTimeMillis()).apply();
            showSuccessDialog(getString(R.string.item_insert_ok), 1);
        } else {
            showFailedDialog(getString(R.string.item_insert_failed));
        }

        db.close();
    }

    private void showSuccessDialog(String message, int which_btn) {
        SuccessDialog SuccessFormFragment = new SuccessDialog();
        SuccessFormFragment.setMessage(message, which_btn);
        SuccessFormFragment.setCancelable(false);
        SuccessFormFragment.show(fm, TAG);
    }

    private void showFailedDialog(String message) {
        FailedDialog FailedFormFragment = new FailedDialog();
        FailedFormFragment.setMessage(message);
        FailedFormFragment.show(fm, TAG);
    }
}
