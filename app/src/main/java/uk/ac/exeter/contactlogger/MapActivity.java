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
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;

import uk.ac.exeter.contactlogger.dialogs.FailedDialog;
import uk.ac.exeter.contactlogger.dialogs.SuccessDialog;
import uk.ac.exeter.contactlogger.utils.DBAdapter;
import uk.ac.exeter.contactlogger.utils.customTileProvider;
import uk.ac.exeter.contactlogger.utils.offlineTileProvider;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private final static String TAG = MapActivity.class.getName();
    FragmentManager fm = getFragmentManager();

    private static final int CUSTOM_MAP_TILE_HEIGHT = 256;
    private static final int CUSTOM_MAP_TILE_WIDTH = 256;
    private static final int CUSTOM_MAP_TILE_INDEX = -1;
    private static final int BASE_MAP_TILE_INDEX = -2;
    private static final int MAP_ZOOM = 18;

    // Overlay that shows a short help text when first launched.
    private Marker marker;
    private GoogleMap map;
    private int mapType, customOverlay;
    private double old_lat, old_lng, corr_lat, corr_lng;
    private long row_id;
    private float old_acc;
    private TextView info_box;
    private Button close_btn;
    private String overlay_url;
    private SharedPreferences settings;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);

        //remove navigation up arrow in actionbar
        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(false);

        //hide navigation buttons at bottom of screen
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //get session variables from previous activity
        row_id = getIntent().getLongExtra("row_id", 0);
        old_lat = getIntent().getDoubleExtra("lat", 0.00);
        old_lng = getIntent().getDoubleExtra("lng", 0.00);
        old_acc = getIntent().getFloatExtra("accuracy", 0);

        info_box = (TextView) findViewById(R.id.loc_infotext);
        close_btn = (Button) findViewById(R.id.btn_x_close);

        //if infobox was closed once, then dont show them again
        String prefs_name = getResources().getString(R.string.PREFS_NAME);
        settings = getSharedPreferences(prefs_name, 0);
        Boolean show_infobox = settings.getBoolean("show_infobox", true);
        mapType = settings.getInt("mapType", 4); //default is Google Hypbrid
        customOverlay = settings.getInt("customOverlay", 0); //default is Google Hypbrid

        //override map settings if offline
        if (!isNetworkAvailable()) {
            mapType = 0; //no google map
            customOverlay = 3; //offline map
        }

        if (!show_infobox) {
            info_box.setVisibility(View.GONE);
            close_btn.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        updateMap();
    }

    // update Map as chosen by the user
    private void updateMap() {

        //define/construct customTile request urls
        String mapbox_id = getString(R.string.mapbox_id);
        String mapbox_token = getString(R.string.mapbox_token);
        String mapbox_url1 = getString(R.string.mapbox_url1);
        String mapbox_url2 = getString(R.string.mapbox_url2);
        String overlay_mapbox = mapbox_url1 + mapbox_id + mapbox_url2 + mapbox_token;
        String overlay_osm = getString(R.string.osm_url);

        //clear and set the map
        map.clear();
        map.setMapType(mapType);

        offlineTileProvider offTileProvider;

        if (customOverlay > 0 && customOverlay < 3) {
            //determine customTileProvider
            if (customOverlay == 1) {
                overlay_url = overlay_mapbox;
            } else if (customOverlay == 2) {
                overlay_url = overlay_osm;
            }

            // create the tileProvider
            customTileProvider mTileProvider = new customTileProvider(
                    CUSTOM_MAP_TILE_WIDTH, CUSTOM_MAP_TILE_HEIGHT, overlay_url);
            offTileProvider = new offlineTileProvider(this);

            //add the offline map as a basis tile
            map.addTileOverlay(new TileOverlayOptions()
                    .tileProvider(offTileProvider)
                    .zIndex(BASE_MAP_TILE_INDEX));

            //add the selected tile provider ontop of offline map
            map.addTileOverlay(new TileOverlayOptions()
                    .tileProvider(mTileProvider)
                    .zIndex(CUSTOM_MAP_TILE_INDEX));

        } else if (customOverlay == 3) {

            // create the offline tileProvider
            offTileProvider = new offlineTileProvider(this);

            // add the Provider in the offline Map
            map.addTileOverlay(new TileOverlayOptions().
                    tileProvider(offTileProvider));
        }

        // Add a marker to the maps and moves the camera.
        LatLng currentLocation = new LatLng(old_lat, old_lng);

        //set padding, so that button does not obscure google map logo
        //this is necessary in order to comply with google terms of service
        map.setPadding(0, 0, 0, 140);

        //move the map to the current location
        map.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));

        //draw circle around location to show accuracy
        CircleOptions circleOptions = new CircleOptions()
                .center(currentLocation)    //set center
                .radius(old_acc)           //set radius in meters
                .fillColor(0x254285f4)      //semi-transparent 25% transparency
                .strokeColor(0x70ffffff)    //70% white
                .strokeWidth(5);

        map.addCircle(circleOptions);

        //create initial draggable marker to the map
        map.addMarker(new MarkerOptions().position(currentLocation).draggable(true));

        //set up custom marker info box
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoContents(Marker point) {
                LatLng latLng = point.getPosition();

                //limit length of coordinates strings
                int lat_length = String.valueOf(latLng.latitude).length();
                int lng_length = String.valueOf(latLng.longitude).length();
                if (lat_length > 8) lat_length = 8;
                if (lng_length > 8) lng_length = 8;

                View v = getLayoutInflater().inflate(R.layout.map_marker, null);
                TextView title = (TextView) v.findViewById(R.id.marker_title);
                TextView info = (TextView) v.findViewById(R.id.marker_info);

                String marker_titletext = getString(R.string.corr_loc);
                String marker_infotext = getString(R.string.loc_lat) +
                        String.valueOf(latLng.latitude).substring(0, lat_length) +
                        "\n" + getString(R.string.loc_long) +
                        String.valueOf(latLng.longitude).substring(0, lng_length);

                //only show accuracy for initial location
                if (latLng.longitude == old_lng && latLng.latitude == old_lat) {
                    marker_titletext = getString(R.string.your_loc);
                    marker_infotext = marker_infotext + "\n" +
                            getString(R.string.loc_accuracy) +
                            String.valueOf(old_acc) + "m";
                }

                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker_titletext);
                info.setText(marker_infotext);
                return v;
            }

            @Override
            public View getInfoWindow(Marker point) {
                // Getting the position from the marker
                return null;
            }
        });

        //get new location when marker is dragged
        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker point) {
                point.hideInfoWindow();
            }

            @Override
            public void onMarkerDragEnd(Marker point) {
                //get coordinates of final dragged position
                corr_lat = point.getPosition().latitude;
                corr_lng = point.getPosition().longitude;
                CameraUpdate cameraUpdate = CameraUpdateFactory.
                        newLatLngZoom(point.getPosition(), MAP_ZOOM);
                map.animateCamera(cameraUpdate);
                point.showInfoWindow();
            }

            @Override
            public void onMarkerDragStart(Marker point) {
                point.hideInfoWindow();
            }
        });

        //zoom into old initial location
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLocation, MAP_ZOOM);
        map.animateCamera(cameraUpdate);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map_menu, menu);

        //set which menu entry needs to checkbox_on and check it
        for (int i = 0; i < menu.size(); ++i) {
            MenuItem mi = menu.getItem(i);
            if ((mi.getItemId() == R.id.normal_map) && mapType == 1) {
                mi.setChecked(true);
            } else if ((mi.getItemId() == R.id.hybrid_map) && mapType == 4) {
                mi.setChecked(true);
            } else if ((mi.getItemId() == R.id.mapbox_map) && customOverlay == 1) {
                mi.setChecked(true);
            } else if ((mi.getItemId() == R.id.osm_map) && customOverlay == 2) {
                mi.setChecked(true);
            } else if ((mi.getItemId() == R.id.off_map) && customOverlay == 3) {
                mi.setChecked(true);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //only the selected menu item should be checkbox_on
        if (item.isChecked()) {
            item.setChecked(false);
        } else {
            item.setChecked(true);
        }

        switch (id) {
            case R.id.normal_map:
                mapType = GoogleMap.MAP_TYPE_NORMAL; //1
                customOverlay = 0;
                break;
            case R.id.hybrid_map:
                mapType = GoogleMap.MAP_TYPE_HYBRID; //4
                customOverlay = 0;
                break;
            case R.id.mapbox_map:
                mapType = GoogleMap.MAP_TYPE_NONE; //no map 0
                customOverlay = 1;
                break;
            case R.id.osm_map:
                mapType = GoogleMap.MAP_TYPE_NONE; //no map 0
                customOverlay = 2;
                break;
            case R.id.off_map:
                mapType = GoogleMap.MAP_TYPE_NONE; //no map 0
                customOverlay = 3;
                break;
            case android.R.id.home:
                this.finish();
                return true;
        }

        //save the user map type selection
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("mapType", mapType);
        editor.putInt("customOverlay", customOverlay);
        editor.apply();

        updateMap();
        return true;
    }

    public void closeInfoBox(View v) {
        //close textview
        if (v.getId() == R.id.btn_x_close) {
            info_box.setVisibility(View.GONE);
            close_btn.setVisibility(View.GONE);

            //save settings
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("show_infobox", false);
            editor.apply();
        }
    }

    public void updateContact(View view) {

        //if user updated location, save last location to shared prefs
        if (corr_lat != 0 && corr_lng != 0) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("last_known_lat", Double.doubleToLongBits(corr_lat));
            editor.putLong("last_known_lng", Double.doubleToLongBits(corr_lng));
            editor.apply();
        }

        //update last db entry
        DBAdapter db = new DBAdapter(this);
        db.open();
        if (db.updateContactLocation(row_id, corr_lat, corr_lng)) {
            showSuccessDialog(getString(R.string.log_insert_ok),0);
        } else {
            showFailedDialog(getString(R.string.log_insert_failed));
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

    //Check if online or offline
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }
}
