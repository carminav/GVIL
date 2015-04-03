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

    private final static int MAX_DURATION = 600; // milliseconds

    private final static float MAX_VELOCITY = 150;

    private final static float MAX_INDEX = 5.0f;
    private final static float MIN_INDEX = 1;

    private boolean initialized = false;

    private float functionX;

    private Bitmap mBitmap = null;
    private long mDuration = 100;
    private ArrayList<Bitmap> mFrames = null;
    private int index;
    private long mVeloc = 0; // Data type should be?
    private final float STEP =  1f;

    private final int MAX_COUNT = 1;
    private int count = 0;

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
        return (float) Math.exp(functionX/5) * 100;
    }

    private float getFunctionX(float velocity) {
        float x = (velocity > MAX_VELOCITY) ? 1 : (velocity / MAX_VELOCITY) * MAX_INDEX;
        return  MAX_INDEX - x;
    }


    public void updateDuration(float velocity) {
        synchronized(this) {
            count = -1;
        }
        mDirection = (velocity > 0) ? RIGHT : LEFT;
        h.removeCallbacksAndMessages(null);
        h.post(r);
    }

    private Runnable r = new Runnable() {
        @Override
        public void run() {
            synchronized(this) {
                count++;
            }
           if (count < MAX_COUNT) invalidate();
        }
    };



//    // Used by gyroscope
//    public void updateDuration(float velocity) {
//        synchronized (this) {
//
//
//            float tmpX = getFunctionX(velocity);
//            if (tmpX < functionX || !initialized) {
//                mDirection = (velocity > 0) ? RIGHT : LEFT;
//                initialized = true;
//                functionX = tmpX;
//                Log.d("INDEX", "update: " + functionX);
//                mDuration =  (long) Math.exp(functionX/3) * 100;
//                count = 0;
//             //   invalidate();
//                Log.d("VELOCITY", "" + velocity);
//                h.removeCallbacksAndMessages(null);
//                h.post(r);
//           }
//
//        }
//
//    }

//    private Runnable r = new Runnable() {
//        @Override
//        public void run() {
//            synchronized(this) {
//                // Update duration based on exponential model
//
//                mDuration =  (long) Math.exp(functionX/3) * 100;
//                functionX += STEP;
//              //  Log.d("DURATION", "runnat: " + mDuration);
//            }
//           // invalidate();
//          //  if (functionX < 5) invalidate();
//        }
//    };



    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);

        if (mFrames == null) return;


            if (mDirection == LEFT && index > 0) index--;
            else if (mDirection == RIGHT && index < mFrames.size() - 1) index++;

            // draw next frame
            Bitmap b = mFrames.get(index);
            float scaleX = c.getWidth() / (float) b.getWidth();
            c.scale(scaleX,1,0,0);
            c.drawBitmap(b, 0 , 0, null);

            // play next frame based on logarithmic function
            h.postDelayed(r, mDuration);

    }
}
