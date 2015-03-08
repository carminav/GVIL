package com.example.dgif.customviews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.os.Handler;

import java.util.ArrayList;


/**
 * Created by Carmina on 2/16/2015.
 */
public class GyroImageView extends ImageView {

    public final static int LEFT = 0;
    public final static int RIGHT = 1;

    private int mDirection;

    private final static int MAX_DURATION = 1000; // milliseconds

    private final static float MAX_VELOCITY = 150;

    private final static float MAX_INDEX = 5.0f;
    private final static float MIN_INDEX = 0;

    private float functionX;

    private Bitmap mBitmap = null;
    private long mDuration = MAX_DURATION;
    private ArrayList<Bitmap> mFrames = null;
    private int index;
    private long mVeloc = 0; // Data type should be?
    private final float STEP =  1f;

    private static final long SCALAR = 100;



    public GyroImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GyroImageView(Context context) {
        super(context);
    }

    public void setBitmap(Bitmap b) {
        mBitmap = b;
    }

    public void setFrames(ArrayList<Bitmap> frames) {
        mFrames = frames;
        if (mFrames != null) index = mFrames.size() / 2; // start in middle;

    }

    public void setDuration(int duration) {mDuration = duration;}

    private Handler h = new Handler();

    private float calculateDuration(float velocity) {
        float x = (velocity > MAX_VELOCITY) ? 1 : (velocity / MAX_VELOCITY) * MAX_INDEX;
        functionX = MAX_INDEX - x;
        return (float) Math.exp(functionX/3) * 100;
    }

    // Used by gyroscope
    public void updateDuration(float velocity) {
        synchronized (this) {
            mDirection = (velocity > 0) ? RIGHT : LEFT;
            mDuration = (long) calculateDuration(velocity);
            Log.d("DURATION", "updat: " + mDuration);
            h.removeCallbacksAndMessages(null);
            h.post(r);
        }

    }

    private Runnable r = new Runnable() {
        @Override
        public void run() {
            synchronized(this) {
                // Update duration according to natural log function
                functionX += STEP;
                mDuration =  (long) Math.exp(functionX/3) * 100;
                Log.d("DURATION", "runnat: " + mDuration);
            }
            invalidate();
        }
    };



    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);

        if (mFrames == null) return;

        // update frame in correct direction
//        if (mVeloc > 0) index++;
//        else index--;



        if (mDirection == LEFT && index > 0) index--;
        else if (mDirection == RIGHT && index < mFrames.size() - 1) index++;


        Log.d("DURATION", "index: " + index);


        // draw next frame
        Bitmap b = mFrames.get(index);
        float scaleX = c.getWidth() / (float) b.getWidth();
        c.scale(scaleX,1,0,0);
        c.drawBitmap(b, 0 , 0, null);

        // play next frame based on logarithmic function
        h.postDelayed(r, mDuration);

//        if (mBitmap != null) {
//            float scaleX = c.getWidth() / (float) mBitmap.getWidth();
//            c.scale(scaleX,1,0,0);
//            c.drawBitmap(mBitmap, 0 , 0, null);
//        }

    }
}
