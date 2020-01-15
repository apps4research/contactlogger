package uk.ac.exeter.contactlogger.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by apps4research on 2015-11-12.
 */
public class cameraHandler {

    private Context mContext;
    private final static String TAG = cameraHandler.class.getName();
    private static final File IMG_DIR = new File(Environment.
            getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ContactLogger");

    public cameraHandler(Context context) {
        mContext = context;
    }

    public void takePic(String device_id) {
        // Dispatch take picture intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Check to see if there is an application available to handle capturing images.
        // This is required else if no camera handling application is found, the pic app will crash
        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {

            // Create the file where the photo should be saved to
            File picFile = null;

            try {
                picFile = createPicFile(device_id);
            } catch (IOException e) {
                Log.d(TAG, "Error creating photo file", e);
            }

            if (picFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(picFile));
                ((Activity) mContext).startActivityForResult(takePictureIntent, 1);
            }
        }
    }

    private File createPicFile(String device_id) throws IOException {
        // Create an image file name appended by date
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = device_id + "_" + timeStamp + "_";

        // Make sure the directory exists
        if (!IMG_DIR.exists()) IMG_DIR.mkdirs();

        return File.createTempFile(imageFileName, ".jpg", IMG_DIR);
    }

}
