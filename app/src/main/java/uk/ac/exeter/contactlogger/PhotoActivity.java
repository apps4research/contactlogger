package uk.ac.exeter.contactlogger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.io.File;

import uk.ac.exeter.contactlogger.utils.Utils;

/**
 * Created by apps4research on 2015-11-12.
 */
public class PhotoActivity extends Activity {

    public static final String TAG = PhotoActivity.class.getName();

    protected ImageLoader imageLoader;
    protected DisplayImageOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_layout);

        imageLoader = ImageLoader.getInstance();

        //set options for image display
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.img_loader)
                .showImageForEmptyUri(R.drawable.img_alert)
                .showImageOnFail(R.drawable.img_alert)
                .resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(false)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        final GridView gridview = (GridView) findViewById(R.id.gridView);
        ImageAdapter photoGrid = new ImageAdapter();
        gridview.setAdapter(photoGrid);
        updateTitle(gridview);

        //on click start new activity and show image in full
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // launch full screen activity
                Intent intent = new Intent(PhotoActivity.this, PhotoDetailsActivity.class);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });

        gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                AlertDialog.Builder alert = new AlertDialog.Builder(PhotoActivity.this);
                alert.setTitle(R.string.attention);
                alert.setMessage(R.string.really_delete_msg);
                alert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do your work here
                        String imageUri = String.valueOf(photo_array[position]);

                        //delete image from memory cache and disk cache (if it was used)
                        DiskCacheUtils.removeFromCache(imageUri, imageLoader.getDiskCache());
                        MemoryCacheUtils.removeFromCache(imageUri, imageLoader.getMemoryCache());

                        //delete file from storage
                        File file = new File(imageUri);
                        file.delete();

                        if (!file.exists()) {
                            Toast.makeText(getApplicationContext(), getString(R.string.photo_deleted),
                                    Toast.LENGTH_SHORT).show();
                            //refresh gridview
                            ImageAdapter photoGrid = new ImageAdapter();
                            gridview.setAdapter(photoGrid);
                            photoGrid.notifyDataSetChanged();
                            updateTitle(gridview);
                        }
                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                alert.show();
                return true;
            }
        });
    }

    static class ViewHolder {
        TextView textView;
        ImageView imageView;
    }

    //custom Adatpter
    public class ImageAdapter extends BaseAdapter {

        @Override
        public void notifyDataSetChanged() {
            photo_array = Utils.ReadSDCard().toArray(); //cast string list array to object array
            super.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return photo_array.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final ViewHolder gridViewImageHolder;

            //check to see if we have a view
            if (convertView == null) {
                //no view - so create a new one
                view = getLayoutInflater().inflate(R.layout.photo_grid_item, parent, false);
                gridViewImageHolder = new ViewHolder();

                gridViewImageHolder.imageView = (ImageView) view.findViewById(R.id.photo_img);
                //need to set this to make sure UIL downsamples low enough
                gridViewImageHolder.imageView.setMaxHeight(120); //Moto G viewport width 360
                gridViewImageHolder.imageView.setMaxWidth(120);

                //get datetime of image from file name and show on top of thumbnail
                gridViewImageHolder.textView = (TextView) view.findViewById(R.id.photo_text);
                gridViewImageHolder.textView.setText(Utils.getReadablePicName(String.valueOf(photo_array[position])));
                view.setTag(gridViewImageHolder);
            } else {
                //we've got a view
                gridViewImageHolder = (ViewHolder) view.getTag();
            }

            //show gallery gridview with options as set above
            imageLoader.displayImage("file://" + photo_array[position], gridViewImageHolder.imageView, options);
            return view;
        }

    }

    private void updateTitle(GridView gridview) {
        //count number of photos shown
        int num_photos = gridview.getCount();
        setTitle(num_photos + " " + getString(R.string.title_photolist));
    }

    Object[] photo_array = Utils.ReadSDCard().toArray(); //cast string list array to object array

}