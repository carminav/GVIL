package com.example.dgif.sensorlisteners.Gyro;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.TextView;

import com.example.dgif.Loaded3DObject;
import com.example.dgif.customviews.GyroImageView;

import java.util.ArrayList;

/**
 * Created by Carmina on 2/24/2015.
 *
 * Uses Gyroscope sensor roll values to determine which image appears
 */
public class GifGyroscopeSensor extends BaseGyroscopeSensor {

    private float R2D = 57.2957795f; // Conversion from radians to Degrees


    private TextView mLabel = null;
    private ArrayList<Bitmap> mFrames;
    private float mDelta;
    private boolean mRunning = false;
    private Loaded3DObject mLoaded3DObject;
    private int index;
    private GyroImageView mView;
    private float prevRoll;

    private boolean initialized = false;

    public GifGyroscopeSensor(Context c, TextView label, Loaded3DObject loaded3DObject) {
        super(c);
        mLabel = label;
        mLoaded3DObject = loaded3DObject;
        mFrames = loaded3DObject.getFramesWithBlends();
        mDelta = loaded3DObject.getDelta();
        mView = loaded3DObject.getView();
    }

    public GifGyroscopeSensor(Context c, Loaded3DObject loaded3DObject) {
        super(c);
        mLoaded3DObject = loaded3DObject;
        mFrames = loaded3DObject.getFramesWithBlends();
        mDelta = loaded3DObject.getDelta();
        index = mFrames.size() / 2; //start in middle
        mView = loaded3DObject.getView();
    }

    // Must be called after blend count changes in Loaded3DObject
    public void update() {
        mDelta = mLoaded3DObject.getDelta();
        mFrames = mLoaded3DObject.getFramesWithBlends();
        index = mFrames.size() / 2; //reset to start in middle
    }

    /* Process values from fusedOrientation vector */
    @Override
    public void onFusedOrientationsCalculated() {
        if (mLabel != null) mLabel.setText(" Roll: " + Math.round(fusedOrientation[2] * R2D));
        float roll = Math.round(fusedOrientation[2] * R2D);

        if (initialized) {

            float diff = roll - prevRoll;
            if (diff <= -mDelta && index > 0) {
                index--;
            } else if (diff >= mDelta && index < mFrames.size() - 1) {
                index++;
            }
            mView.setBitmap(mFrames.get(index));
            mView.invalidate();
        } else initialized = true;

        prevRoll = roll;

    }

    public boolean isRunning() {
        return mRunning;
    }


    @Override
    public void start() {
        super.start();
        mRunning = true;
    }

    @Override
    public void stop() {
        super.stop();
        mView.setBitmap(null);
        mRunning = false;
    }
}
