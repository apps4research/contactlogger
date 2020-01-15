package uk.ac.exeter.contactlogger.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by apps4research on 2015-11-12.
 */
public class squareImageView extends ImageView {
    public squareImageView(Context context) {
        super(context);
    }

    public squareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public squareImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth()); //Snap to width
    }
}