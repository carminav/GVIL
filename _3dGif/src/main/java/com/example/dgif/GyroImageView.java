package com.example.dgif;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by Carmina on 2/16/2015.
 */
public class GyroImageView extends ImageView {

    private Bitmap mBitmap = null;

    public GyroImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GyroImageView(Context context) {
        super(context);
    }

    public void setBitmap(Bitmap b) {
        mBitmap = b;
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);

        if (mBitmap != null) {
            float scaleX = c.getWidth() / (float) mBitmap.getWidth();
            c.scale(scaleX,1,0,0);
            c.drawBitmap(mBitmap, 0 , 0, null);
        }



    }
}
