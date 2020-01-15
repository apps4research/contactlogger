package uk.ac.exeter.contactlogger;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import uk.ac.exeter.contactlogger.dialogs.AlarmDialog;
import uk.ac.exeter.contactlogger.dialogs.LoginDialog;
import uk.ac.exeter.contactlogger.dialogs.enableLocationAlert;
import uk.ac.exeter.contactlogger.dialogs.missingValues;
import uk.ac.exeter.contactlogger.dialogs.otherInputRel;
import uk.ac.exeter.contactlogger.dialogs.otherInputType;
import uk.ac.exeter.contactlogger.utils.DBAdapter;
import uk.ac.exeter.contactlogger.utils.Utils;
import uk.ac.exeter.contactlogger.utils.cameraHandler;

/**
 * Created by apps4research on 2015-11-12.
 */
public class MainActivity extends Activity implements
        enableLocationAlert.OnLocationDialogResultListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener,
        otherInputType.onOtherTypeDialogResultListener,
        otherInputRel.onOtherRelDialogResultListener,
        missingValues.onMissingValuesResultListener,
        View.OnClickListener {

    private final static String TAG = MainActivity.class.getName();
    private static final long INTERVAL = 1000; //1 seconds
    private static final long FASTEST_INTERVAL = INTERVAL; //1 seconds
    private static final int missing_val = 999;
    private static final int dailyHour = 16; //after 4pm
    private static final int minAccuracy = 0; //meters
    private static final int maxLocations = 5;
    private static final int maxGetLocationMs = 45000; //45 seconds

    private static TextView info_type, info_rel, info_gen, info_exp, info_duration,
            info_typical, info_status, info_stat1, info_stat2;
    private static ImageView pstatus_img, duration_img, typical_img;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private String device_id, otherRel, otherType, prefs_name;
    private boolean gotLocation = false, val_missing = true, insert_status = false,
            pstatus_pressed = false, duration_pressed = false, typical_pressed = false;
    private int count, attitude, dur_listener_val, status_listener_val, typical_listener_val,
            final_btn_type_val, final_btn_rel_val, final_btn_exp_val, final_btn_gen_val,
            final_age_val, sl_typical_default, sl_dur_default, sl_status_default,
            final_typical_val, final_dur_val, final_status_val;
    private double lat, lng, conv_lat, conv_lng, best_lat, best_lng;
    private float accuracy, bestAccuracy, best_acc;
    private EditText age_val;
    private ProgressDialog location_dialog;
    private SharedPreferences settings;

    //Arrays
    private LinkedHashMap<Float, String> locations = new LinkedHashMap<>();
    private final LinkedHashMap<Integer, String> ButtonCatMap = new LinkedHashMap<>();
    private final LinkedHashMap<Integer, String> ButtonKeyMap = new LinkedHashMap<>();
    private ArrayList<String> buttons_checked_cat = new ArrayList<>();
    private FragmentManager fm = getFragmentManager();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        //Shared prefs name
        prefs_name = getResources().getString(R.string.PREFS_NAME);
        settings = getSharedPreferences(prefs_name, 0);

        //get device id from shared prefs or from phone
        device_id = Utils.getDeviceId(this);

        if (!isNetworkAvailable()) {
            Log.d(TAG, "No internet connection!");
        }

        if (!servicesAvailable()) {
            //check if google location services are installed
            Toast.makeText(this, "Google Location Serviced unavailable", Toast.LENGTH_SHORT).show();
        }

        if (!isLocationGpsEnabled() || (isNetworkAvailable() && !isLocationWifiCellEnabled())) {
            //check if gps disabled, or if online, if location services is disabled
            Log.d(TAG, "GPS or Location Network disabled");
            showEnableLocationDialog();
        }

        //check to see if DailyActivity needs to be shown
        showDaily();

        //get thermo_value from DailyActivity
        attitude = getIntent().getIntExtra("thermo_value", missing_val);

        // Initialize and build GoogleApiClient
        buildGoogleApiClient();

        //define and get arrays
        final String[] dur_img_array = getResources().getStringArray(R.array.duration_images);
        final String[] dur_txt_array = getResources().getStringArray(R.array.duration_texts);
        final String[] pstatus_array = getResources().getStringArray(R.array.pstatus_images);
        final int[] transparency_arr = new int[]{0, 16, 32, 48, 64, 80, 96, 112, 128, 144, 255};

        //Textviews
        info_type = (TextView) findViewById(R.id.info_type);
        info_rel = (TextView) findViewById(R.id.info_rel);
        info_gen = (TextView) findViewById(R.id.info_gen);
        info_exp = (TextView) findViewById(R.id.info_exp);
        info_duration = (TextView) findViewById(R.id.info_duration);
        info_typical = (TextView) findViewById(R.id.info_typical);
        info_status = (TextView) findViewById(R.id.info_status);
        info_stat1 = (TextView) findViewById(R.id.info_stat1);
        info_stat2 = (TextView) findViewById(R.id.info_stat2);

        //Edit Text
        age_val = (EditText) findViewById(R.id.age_input);

        //set initial value of section descriptions
        if (final_btn_type_val == 0) info_type.setText(getString(R.string.no_selection));
        if (final_btn_rel_val == 0) info_rel.setText(getString(R.string.no_selection));
        if (final_btn_exp_val == 0) info_exp.setText(getString(R.string.no_selection));
        if (final_btn_gen_val == 0) info_gen.setText(getString(R.string.no_selection));
        if (!typical_pressed) info_typical.setText(getString(R.string.no_selection));
        if (!duration_pressed) info_duration.setText(getString(R.string.no_selection));
        if (!pstatus_pressed) info_status.setText(getString(R.string.no_selection));

        //Set defaults for typicality seekbar
        SeekBar typical_seekbar = (SeekBar) findViewById(R.id.typical_seekbar);
        typical_img = (ImageView) findViewById(R.id.typical_img);
        sl_typical_default = typical_seekbar.getProgress(); //default 5

        //Set default for approx. duration seekbar
        SeekBar duration_seekbar = (SeekBar) findViewById(R.id.dur_seekbar);
        duration_img = (ImageView) findViewById(R.id.dur_img);
        sl_dur_default = duration_seekbar.getProgress(); //default 1 minute

        //Set default for relative status seekbar
        SeekBar pstatus_seekbar = (SeekBar) findViewById(R.id.pstatus_seekbar);
        pstatus_img = (ImageView) findViewById(R.id.pstatus_img);
        sl_status_default = pstatus_seekbar.getProgress(); //default 5

        //fill and iterate through ButtonMap Array
        LinkedButtonMap();
        Set<Integer> ButtonIds = ButtonCatMap.keySet();

        for (int id : ButtonIds) {
            ToggleButton toggleButton = (ToggleButton) findViewById(id); //find view of each button
            toggleButton.setOnClickListener(this); //set listener for each button
        }

        //onClick listeners for slider images
        typical_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drawable status_drawable = getResources().getDrawable(R.drawable.icon_typical, getTheme());
                SeekBar typical_seekbar = (SeekBar) findViewById(R.id.typical_seekbar);
                typical_seekbar.setProgress(sl_typical_default); //5
                typical_img.setImageDrawable(status_drawable);
                info_typical.setText(getString(R.string.no_selection));
                typical_pressed = false;
            }
        });

        duration_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //set duration seekbar to no response
                Drawable status_drawable = getResources().getDrawable(R.drawable.icon_duration12, getTheme());
                SeekBar duration_seekbar = (SeekBar) findViewById(R.id.dur_seekbar);
                duration_seekbar.setProgress(sl_dur_default); //default 50 seconds
                duration_img.setImageDrawable(status_drawable);
                info_duration.setText(getString(R.string.no_selection));
                duration_pressed = false;
            }
        });

        pstatus_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drawable status_drawable = getResources().getDrawable(R.drawable.icon_status5, getTheme());
                SeekBar status_seekbar = (SeekBar) findViewById(R.id.pstatus_seekbar);
                status_seekbar.setProgress(sl_status_default); //5
                pstatus_img.setImageDrawable(status_drawable);
                info_status.setVisibility(View.VISIBLE);
                info_stat1.setVisibility(View.GONE);
                info_stat2.setVisibility(View.GONE);
                pstatus_pressed = false;
            }
        });


        //onchange listeners for sliders
        typical_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar typical_seekbar, int progress, boolean fromUser) {
                Drawable group_drawable = getResources().getDrawable(R.drawable.icon_typical, getTheme());
                Drawable typical_drawable = getResources().getDrawable(R.drawable.typical_person, getTheme());
                if (typical_drawable != null) typical_drawable.setAlpha(transparency_arr[progress]);
                typical_img.setBackground(group_drawable);
                typical_img.setImageDrawable(typical_drawable);
                info_typical.setText(String.valueOf(progress * 10) + getString(R.string.of100));
                typical_listener_val = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar typical_seekbar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar typical_seekbar) {
                //make the icon for this slide bar grey when user stops touching seekbar
                Drawable group_drawable = getResources().getDrawable(R.drawable.icon_typical, getTheme());
                Drawable typical_drawable = getResources().getDrawable(R.drawable.typical_person, getTheme());
                if (typical_drawable != null)
                    typical_drawable.setAlpha(transparency_arr[typical_listener_val]);
                Drawable final_group_drawable = convertToGrayscale(group_drawable);
                typical_img.setBackground(final_group_drawable);
                Drawable final_typical_drawable = convertToGrayscale(typical_drawable);
                typical_img.setImageDrawable(final_typical_drawable);
                typical_pressed = true;
            }

        });

        duration_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar pstatus_seekbar, int progress, boolean fromUser) {
                //set duration clock image and duration text
                int dur_img = getResources().getIdentifier(dur_img_array[progress], "drawable", getPackageName());
                Drawable dur_drawable = getResources().getDrawable(dur_img, getTheme());
                duration_img.setImageDrawable(dur_drawable);
                info_duration.setText(dur_txt_array[progress]);
                dur_listener_val = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar duration_seekbar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar duration_seekbar) {
                //make the icon for this slide bar grey when user stops touching seekbar
                int dur_img = getResources().getIdentifier(dur_img_array[dur_listener_val], "drawable", getPackageName());
                Drawable final_dur_drawable = convertToGrayscale(getResources().getDrawable(dur_img, getTheme()));
                duration_img.setImageDrawable(final_dur_drawable);
                duration_pressed = true;
            }

        });

        pstatus_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar pstatus_seekbar, int progress, boolean fromUser) {
                int status_img = getResources().getIdentifier(pstatus_array[progress], "drawable", getPackageName());
                Drawable status_drawable = getResources().getDrawable(status_img, getTheme());
                pstatus_img.setImageDrawable(status_drawable);
                info_status.setVisibility(View.GONE);
                info_stat1.setVisibility(View.VISIBLE);
                info_stat2.setVisibility(View.VISIBLE);
                status_listener_val = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar pstatus_seekbar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar pstatus_seekbar) {
                //make the icon for this slide bar grey when user stops touching seekbar
                int status_img = getResources().getIdentifier(pstatus_array[status_listener_val], "drawable", getPackageName());
                Drawable final_status_drawable = convertToGrayscale(getResources().getDrawable(status_img, getTheme()));
                pstatus_img.setImageDrawable(final_status_drawable);
                pstatus_pressed = true;
            }

        });
    }

    private void LinkedButtonMap() {
        String[] stringArray = getResources().getStringArray(R.array.ButtonDescMap);
        for (String entry : stringArray) {
            String[] splitResult = entry.split("\\|", 2);
            int buttonId = getResources().getIdentifier(splitResult[0], "id", getPackageName());
            String buttonKey = splitResult[0].substring(splitResult[0].lastIndexOf("_") + 1);
            ButtonCatMap.put(buttonId, splitResult[1]);
            ButtonKeyMap.put(buttonId, buttonKey);
        }
    }

    protected Drawable convertToGrayscale(Drawable drawable) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        drawable.setColorFilter(filter);
        return drawable;
    }

    public void onClick(View v) {
        Set<Integer> ButtonIds = ButtonCatMap.keySet();

        //when button is clicked show corresponding description
        for (int id : ButtonIds) {
            if (id == v.getId()) {
                ToggleButton toggleButton = (ToggleButton) findViewById(id);
                String description = toggleButton.getContentDescription().toString();
                String category = ButtonCatMap.get(id);

                //if button is not pressed in resp. category, don't show description text
                //and set final value of button section to 0
                if (category.equals("rel") && !toggleButton.isChecked()) {
                    info_rel.setText(getString(R.string.no_selection));
                    final_btn_rel_val = 0;
                } else if (category.equals("exp") && !toggleButton.isChecked()) {
                    info_exp.setText(getString(R.string.no_selection));
                    final_btn_exp_val = 0;
                } else if (category.equals("gen") && !toggleButton.isChecked()) {
                    info_gen.setText(getString(R.string.no_selection));
                    final_btn_gen_val = 0;
                } else if (category.equals("type") && !toggleButton.isChecked()) {
                    info_type.setText(getString(R.string.no_selection));
                    final_btn_type_val = 0;
                }

                //if button is pressed in resp. category, show description text
                //and get the value of the button that was checked
                if (category.equals("rel") && toggleButton.isChecked()) {
                    info_rel.setText(description);
                    final_btn_rel_val = Integer.parseInt(ButtonKeyMap.get(id));
                } else if (category.equals("exp") && toggleButton.isChecked()) {
                    info_exp.setText(description);
                    final_btn_exp_val = Integer.parseInt(ButtonKeyMap.get(id));
                } else if (category.equals("gen") && toggleButton.isChecked()) {
                    info_gen.setText(description);
                    final_btn_gen_val = Integer.parseInt(ButtonKeyMap.get(id));
                } else if (category.equals("type") && toggleButton.isChecked()) {
                    info_type.setText(description);
                    final_btn_type_val = Integer.parseInt(ButtonKeyMap.get(id));
                }

                //show dialog if "other" buttons are pressed
                if (id == R.id.btn_type_9 && toggleButton.isChecked()) {
                    showOtherType();
                } else if (id == R.id.btn_relate_9 && toggleButton.isChecked()) {
                    showOtherRel();
                }

                //set other values to null if other button is unchecked
                if (id == R.id.btn_type_9 && !toggleButton.isChecked()) {
                    otherType = null;
                } else if (id == R.id.btn_relate_9 && !toggleButton.isChecked()) {
                    otherRel = null;
                }

                setBtn(id, category);
            }
        }
    }

    private void setBtn(int button_on, String cat) {
        Set<Integer> ButtonIds = ButtonCatMap.keySet();
        for (int id : ButtonIds) {
            String category = ButtonCatMap.get(id);
            ToggleButton toggleButton = (ToggleButton) findViewById(id);
            // any button that belongs to cat and is not the one pressed should be turned off
            if (id != button_on && category.equals(cat)) {
                toggleButton.setChecked(false);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //get admin login status
        Boolean admin_login = settings.getBoolean("admin_login", false);

        switch (id) {
            case R.id.action_about:
                Intent showAbout = new Intent(this, AboutActivity.class);
                startActivity(showAbout);
                return true;
            case R.id.action_admin:
                // Show login Fragment if not already logged in
                if (!admin_login) {
                    showAdminLogin();
                } else {
                    Intent showAdmin = new Intent(this, AdminActivity.class);
                    startActivity(showAdmin);
                }
                return true;
            case R.id.action_alarm:
                //Link to Location Settings
                showAlarmDialog();
                return true;
            case R.id.action_loc_settings:
                //Link to Location Settings
                startEnableLocationSettings();
                return true;
            case R.id.action_daily:
                Intent showDaily = new Intent(this, DailyActivity.class);
                startActivity(showDaily);
                return true;
            case R.id.action_camera:
                cameraHandler takephoto = new cameraHandler(this);
                takephoto.takePic(device_id);
                return true;
            case R.id.action_show_photos:
                Intent showpics = new Intent(this, PhotoActivity.class);
                startActivity(showpics);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //get admin login status
        Boolean admin_login = settings.getBoolean("admin_login", false);
        //if not logged in as admin, do not show Daily Activity option in menu
        menu.findItem(R.id.action_daily).setVisible(admin_login);
        return true;
    }

    // Builds a GoogleApiClient. Uses the addApi method to request the LocationServices API
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    @Override
    //The visible lifetime of an activity
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showDaily();
        if (mGoogleApiClient.isConnected()) {
            locations.clear(); //empty the locations array
            startLocationUpdates();
        }
        if (settings.getBoolean("firstrun", true)) {
            //set variable if application is run for first time
            settings.edit().putBoolean("firstrun", false).apply();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            locations.clear(); //empty the locations array
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // get admin login status
        Boolean admin_login = settings.getBoolean("admin_login", false);
        if (admin_login) {
            // make sure to logout of admin area if logged in
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("admin_login", false).apply();
        }
    }

    // LocationRequest objects are used to request of service parameters
    // for request to the FusedLocationProviderApi
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    // Used for receiving notifications from FusedLocationProviderApi when location changed
    // It will be called if the LocationListener has been registered with the location client
    // using the requestLocationUpdates function.
    @Override
    public void onLocationChanged(Location location) {
        Location mCurrentLocation = location;

        if (null != mCurrentLocation && mCurrentLocation.getLatitude() != 0) {
            //got a fused location back
            gotLocation = true;
        } else {
            //handle location is null
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            //location is Null
            gotLocation = mCurrentLocation != null;
        }

        if (gotLocation) {
            lat = mCurrentLocation.getLatitude();
            lng = mCurrentLocation.getLongitude();
            accuracy = mCurrentLocation.getAccuracy();
        } else {
            //get location from shared prefs
            lat = Double.longBitsToDouble(settings.getLong("last_known_lat", 0));
            lng = Double.longBitsToDouble(settings.getLong("last_known_lng", 0));
            accuracy = 0;
        }

        //put location info into array
        locationsArray();
        count++;
    }

    private void locationProgress() {
        final long mStartTime = System.currentTimeMillis();
        location_dialog = ProgressDialog.show(MainActivity.this,
                getString(R.string.please_wait), getString(R.string.getting_location), true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // If online, do this until array is fully filled, if offline the array would
                    // never be fully filled, thus just get the first location possible
                    // providing the whole process does not take longer than maxGetLocationMs
                    while ((((locations.size() < maxLocations) && isNetworkAvailable()) ||
                            (!isNetworkAvailable() && locations.size() < 1)) &&
                            (System.currentTimeMillis() - mStartTime < maxGetLocationMs)) {
                        //normally takes about 17sec
                    }

                    final String destination;

                    // no location at all available at this time, so get last known
                    if (locations.size() == 0) {
                        conv_lat = Double.longBitsToDouble(settings.getLong("last_known_lat", 0));
                        conv_lng = Double.longBitsToDouble(settings.getLong("last_known_lng", 0));
                        bestAccuracy = 0;
                    } else if (locations.size() > 0) {
                        //at least one location should be in the array, before continuing
                        //get the smallest key in the array, which is the best accuracy
                        //then get the associated values, split and convert them
                        float sortAccuracy = Collections.min(locations.keySet());
                        String bestLocation = locations.get(sortAccuracy);
                        String[] best_coords = bestLocation.split(";");
                        conv_lat = Double.parseDouble(best_coords[0]);
                        conv_lng = Double.parseDouble(best_coords[1]);
                        float seed = Float.parseFloat(best_coords[2]);
                        bestAccuracy = (sortAccuracy - seed);
                    }

                    //if location was NOT accurate enough go to MapActivity
                    if ((bestAccuracy < minAccuracy) && bestAccuracy != 0) {
                        //accuracy is good enough, dont show map
                        destination = "main";
                    } else {
                        destination = "map";
                    }

                    //insert into db
                    runOnUiThread(new Runnable() {
                        public void run() {
                            insertToDB(conv_lat, conv_lng, bestAccuracy, destination);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
                location_dialog.dismiss();
            }
        }).start();
    }

    //add a new location to the location array, by initially filling it up with 5
    //values and then removing the oldest one before adding a new element.
    //This keeps the array size constant to and equal to maxLocations.
    //This process keeps running while main activity is running
    private void locationsArray() {

        //generate a random float number between 0 and 0.99999
        Random rand = new Random();
        float frand = rand.nextFloat();

        locations.put((accuracy + frand), String.valueOf(lat) + ";" + String.valueOf(lng)
                + ";" + String.valueOf(frand));

        if (locations.size() > maxLocations) {
            //locations.remove(0);
            Float key = locations.keySet().iterator().next();
            locations.remove(key);
        }
    }

    // Before any operation is executed, the GoogleApiClient must be connected using the
    // connect() method. The client is not considered connected until the onConnected(Bundle)
    // callback has been called.
    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    // Request location updates with GoogleApiClient object, LocationRequest and listener
    // to pass result.
    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    // User is leaving screen, so stop location updates
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    // On connection suspended with Google API client, this method will be called.
    @Override
    public void onConnectionSuspended(int i) {
    }

    // On a failure to connect Google API client, this method will be called.
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onLocationDialogResult(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            startEnableLocationSettings();
        }
    }

    @Override
    public void onOtherTypeResult(int result, String resultString) {
        ToggleButton btn_type_other = (ToggleButton) findViewById(R.id.btn_type_9);
        if (result == Activity.RESULT_OK && resultString != null && !resultString.isEmpty()) {
            otherType = resultString;
            info_type.setText(resultString);
        } else {
            btn_type_other.setChecked(false);
            info_type.setText(getString(R.string.no_selection));
            final_btn_type_val = 0;
        }
    }

    @Override
    public void onOtherRelResult(int result, String resultString) {
        ToggleButton btn_rel_other = (ToggleButton) findViewById(R.id.btn_relate_9);
        if (result == Activity.RESULT_OK && resultString != null && !resultString.isEmpty()) {
            otherRel = resultString;
            info_rel.setText(resultString);
        } else {
            btn_rel_other.setChecked(false);
            info_rel.setText(getString(R.string.no_selection));
            final_btn_rel_val = 0;
        }
    }

    public void clearAge(View view) {
        age_val.setText("");
    }

    @Override
    //this is called by missingValues class in dialog folder
    public void onMissingResult(int result) {
        //user has declined responses
        if (result == Activity.RESULT_OK) { // DECLINE RESPONSE
            if (final_btn_type_val == 0) final_btn_type_val = missing_val;
            if (final_btn_rel_val == 0) final_btn_rel_val = missing_val;
            if (final_btn_exp_val == 0) final_btn_exp_val = missing_val;
            if (final_btn_gen_val == 0) final_btn_gen_val = missing_val;
            if (final_age_val == 0) final_age_val = missing_val;
            if (!duration_pressed) final_dur_val = missing_val;
            if (!pstatus_pressed) final_status_val = missing_val;
            if (!typical_pressed) final_typical_val = missing_val;
            if (!validateResponses()) {
                //no missing values, so go get location
                locationProgress();
            }
        }
    }

    private boolean validateResponses() {

        //build error messages
        StringBuilder error_message = new StringBuilder();

        int count_errors = 0;
        buttons_checked_cat.clear();

        // go through array of buttons and check state of each
        Set<Integer> ButtonIds = ButtonCatMap.keySet();

        for (int id : ButtonIds) {
            ToggleButton toggleButton = (ToggleButton) findViewById(id);
            String category = ButtonCatMap.get(id);
            //only add buttons to array that have been checked
            if (toggleButton.isChecked()) {
                //array contains all buttons pressed from 3 categories (type, rel and exp)
                buttons_checked_cat.add(category);
            }
        }


        //check if a button in the type section was not pressed
        if (!buttons_checked_cat.contains("type") && (final_btn_type_val != missing_val)) {
            //button in section not checked and final value not set to decline
            error_message.append("- ").append(getString(R.string.lb_contacttype)).append("\n");
            count_errors++;
        }

        if (!buttons_checked_cat.contains("rel") && (final_btn_rel_val != missing_val)) {
            error_message.append("- ").append(getString(R.string.lb_relationship)).append("\n");
            count_errors++;
        }

        if (!buttons_checked_cat.contains("exp") && (final_btn_exp_val != missing_val)) {
            error_message.append("- ").append(getString(R.string.lb_experience)).append("\n");
            count_errors++;
        }

        if (!buttons_checked_cat.contains("gen") && (final_btn_gen_val != missing_val)) {
            error_message.append("- ").append(getString(R.string.lb_gender)).append("\n");
            count_errors++;
        }

        if (final_age_val < 1 && final_age_val != missing_val) {
            error_message.append("- ").append(getString(R.string.lb_age)).append("\n");
            count_errors++;
        }

        if (!typical_pressed && final_typical_val != missing_val) {
            //if slider has not been moved and final value not set to decline
            error_message.append("- ").append(getString(R.string.lb_typical)).append("\n");
            count_errors++;
        } else if (typical_pressed && final_typical_val != missing_val) {
            //this can only happen if the slider has been moved
            final_typical_val = typical_listener_val;
        }

        if (!duration_pressed && final_dur_val != missing_val) {
            error_message.append("- ").append(getString(R.string.lb_duration)).append("\n");
            count_errors++;
        } else if (duration_pressed && final_dur_val != missing_val) {
            final_dur_val = dur_listener_val;
        }

        if (!pstatus_pressed && final_status_val != missing_val) {
            error_message.append("- ").append(getString(R.string.lb_pstatus)).append("\n");
            count_errors++;
        } else if (pstatus_pressed && final_status_val != missing_val) {
            final_status_val = status_listener_val;
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

    private void startEnableLocationSettings() {
        // Launch location settings, to allow user to make a change
        Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(enableLocationIntent, 1);
    }

    private void showAdminLogin() {
        LoginDialog LoginFormFragment = new LoginDialog();
        LoginFormFragment.show(fm, TAG);
    }

    private void showAlarmDialog() {
        AlarmDialog AlarmFormFragment = new AlarmDialog();
        AlarmFormFragment.show(fm, TAG);
    }

    private void showOtherType() {
        otherInputType OtherFormFragmentType = new otherInputType();
        OtherFormFragmentType.setCancelable(false);
        OtherFormFragmentType.show(fm, TAG);
    }

    private void showOtherRel() {
        otherInputRel OtherFormFragmentRel = new otherInputRel();
        OtherFormFragmentRel.setCancelable(false);
        OtherFormFragmentRel.show(fm, TAG);
    }

    private void showMissingValues(String message, int errors) {
        missingValues MissingValuesFormFragment = new missingValues();
        MissingValuesFormFragment.setMessage(message, errors);
        MissingValuesFormFragment.show(fm, TAG);
    }

    private void showEnableLocationDialog() {
        enableLocationAlert LocationDialog = new enableLocationAlert();
        LocationDialog.setCancelable(false);
        LocationDialog.show(fm, TAG);
    }

    private boolean servicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0).show();
            return false;
        }
    }

    //Check if GPS is enabled
    private boolean isLocationGpsEnabled() {
        LocationManager manager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    //Check if locations are available from Wifi or Cell sources
    private boolean isLocationWifiCellEnabled() {
        LocationManager manager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    //Check if online or offline
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }

    private void showDaily() {
        //if info box was closed once, then don't show them again
        long daily_last_shown = settings.getLong("daily_last_shown", 0);
        Calendar rightNow = Calendar.getInstance();
        int hour_now = rightNow.get(Calendar.HOUR_OF_DAY);

        //if shown_daily is false and the time last shown is not today and time now
        //is equal to or later than dailyHour
        if (!DateUtils.isToday(daily_last_shown) && hour_now >= dailyHour) {
            Intent showDailyActivity = new Intent(this, DailyActivity.class);
            startActivity(showDailyActivity);
            finish();
        }
    }

    private void insertToDB(double best_lat, double best_lng, float best_acc, String dest) {

        //Current Datetime
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String DateTime = dateFormat.format(new Date());

        //update shared prefs first
        if (best_lng !=0 && best_lat != 0){
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("last_known_lat", Double.doubleToLongBits(best_lat));
            editor.putLong("last_known_lng", Double.doubleToLongBits(best_lng));
            editor.apply();
        }

        // add new contact to db
        DBAdapter db = new DBAdapter(this);
        db.open();
        long row_id = db.insertContact(device_id, DateTime, final_btn_type_val, otherType,
                final_btn_rel_val, otherRel, final_typical_val, final_dur_val, final_status_val,
                final_btn_exp_val, final_btn_gen_val, final_age_val, attitude, best_lat, best_lng,
                best_acc, 0, 0, 0, 0);

        if (row_id > 0) {
            Toast.makeText(MainActivity.this, getString(R.string.log_insert_ok), Toast.LENGTH_LONG).show();
            insert_status = true;
        } else {
            Toast.makeText(this, getString(R.string.log_insert_failed), Toast.LENGTH_LONG).show();
        }

        db.close();

        if (dest.equals("main") && insert_status) {
            recreate();
        } else if (dest.equals("map") && insert_status) {
            goToMap(best_lat, best_lng, best_acc, row_id);
        }
    }

    private void goToMap(double best_lat, double best_lng, float best_acc, long row_id) {
        //got to MapActivity
        Intent showMap = new Intent(MainActivity.this, MapActivity.class);
        showMap.putExtra("row_id", row_id);
        showMap.putExtra("lat", best_lat);
        showMap.putExtra("lng", best_lng);
        showMap.putExtra("accuracy", best_acc);
        finish();
        startActivity(showMap);
    }

    public void logContact(View view) {

        //get age from EditText input
        String age_string = age_val.getText().toString().trim();
        if (age_string.isEmpty()) age_string = "0";
        final_age_val = Integer.parseInt(age_string);

        boolean has_errors = validateResponses();
        //if validation returned false == no errors
        if (!has_errors) {
            //Call Activity that logs the contact details and GPS coordinates
            //Later this activity will only show if GPS coordinates are too inaccurate
            locationProgress();
        }
    }
}
