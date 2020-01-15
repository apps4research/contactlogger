package uk.ac.exeter.contactlogger;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import uk.ac.exeter.contactlogger.dialogs.thermoAlert;
import uk.ac.exeter.contactlogger.dialogs.thermoAlert.OnThermoDialogResultListener;

/**
 * Created by apps4research on 2015-11-12.
 */
public class DailyActivity extends Activity implements OnThermoDialogResultListener {

    private final static String TAG = DailyActivity.class.getName();

    FragmentManager fm = getFragmentManager();

    private Button btnPlus, btnMinus;
    private ProgressBar thermoProgressBar;
    private int updown, thermoVal;
    private final static int missing_val = 999;
    private final static int default_val = 50;
    private Handler repeatUpdateHandler = new Handler();
    private final static long REPEAT_DELAY = 0;
    private boolean mAutoChange, buttonPressed = false;
    private String prefs_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.daily_layout);

        prefs_name = getResources().getString(R.string.PREFS_NAME);

        //remove navigation up arrow in actionbar
        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(false);

        btnPlus = (Button)findViewById(R.id.btn_plus);
        btnMinus = (Button)findViewById(R.id.btn_minus);
        thermoProgressBar = (ProgressBar)findViewById(R.id.thermoProgressBar);

        btnPlus.setOnTouchListener(touch_listener);
        btnPlus.setOnLongClickListener(longclick_listener);
        btnPlus.setOnClickListener(onclick_listener);

        btnMinus.setOnTouchListener(touch_listener);
        btnMinus.setOnLongClickListener(longclick_listener);
        btnMinus.setOnClickListener(onclick_listener);
    }

    View.OnClickListener onclick_listener = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_plus:
                    updown = 1;
                    break;
                case R.id.btn_minus:
                    updown = 0;
                    break;
            }

            buttonPressed = true;
            mAutoChange = false;
            repeatUpdateHandler.post(new RptUpdater());
        }
    };

    View.OnLongClickListener longclick_listener = new View.OnLongClickListener() {
        public boolean onLongClick(View v) {

            switch (v.getId()) {
                case R.id.btn_plus:
                    updown = 1;
                    break;
                case R.id.btn_minus:
                    updown = 0;
                    break;
            }

            buttonPressed = true;
            mAutoChange = true;
            repeatUpdateHandler.post(new RptUpdater());
            return true;
        }
    };


    View.OnTouchListener touch_listener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            // if finger is taken off button OR event is cancelled decrement of progressbar is stopped!
            if ((event.getAction() == MotionEvent.ACTION_UP ||
                    event.getAction() == MotionEvent.ACTION_CANCEL) && mAutoChange) {
                mAutoChange = false;
            }
            return false;
        }
    };

    class RptUpdater implements Runnable {
        public void run() {
            //if mAutodrecement is true continuously do something
            if (mAutoChange) {
                change_progress(updown); //0 down, 1 up
                repeatUpdateHandler.postDelayed(new RptUpdater(), REPEAT_DELAY);
            } else {
                int currentValue = thermoProgressBar.getProgress();
                if (updown > 0) { //up
                    thermoProgressBar.setProgress(currentValue+1);
                } else { //down
                    thermoProgressBar.setProgress(currentValue-1);
                }
            }
        }
    }

    //this is constantly happening as long as the button is checkbox_on and being long pressed
    private void change_progress(int updown) {
        int currentValue = thermoProgressBar.getProgress();
        if (updown > 0) { //up
            currentValue++;
        } else { //down
            currentValue--;
        }
        thermoProgressBar.setProgress(currentValue);
        //Update the textfield to show current value of progressbar
        //textProgress1.setText(Integer.toString(thermoProgressBar.getProgress()) + "%");
    }

    private void showThermoDialog() {
        thermoAlert thermoDialog = new thermoAlert();
        thermoDialog.show(fm, TAG);
    }

    public void onThermoDialogResult(int resultCode) {
        //decline response ... does not wish to answer
        if (resultCode == Activity.RESULT_OK) {
            thermoVal = missing_val;
            startMainActivity();

        //accept default value
        } else if (resultCode == Activity.RESULT_FIRST_USER) {
            thermoVal = default_val;
            startMainActivity();
        }
    }

    public void gotoNextActivity(View view){
        //check to see if thermometer value was changed
        if (!buttonPressed) {
            showThermoDialog();
        } else {
            thermoVal = thermoProgressBar.getProgress();
            startMainActivity();
        }
    }

    public void startMainActivity() {
        //save variables that daily activity was shown
        SharedPreferences settings = getSharedPreferences(prefs_name, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("daily_last_shown", System.currentTimeMillis()).apply();

        //go to main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("thermo_value", thermoVal);
        startActivity(intent);
    }
}
