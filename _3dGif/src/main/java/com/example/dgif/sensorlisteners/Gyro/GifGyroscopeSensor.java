package com.example.dgif.sensorlisteners.Gyro;

import android.content.Context;
import android.graphics.Bitmap;
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
    private boolean mRunning = false;
    private Loaded3DObject mLoaded3DObject;
    private int index;
    private GyroImageView mView;
    private float prevRoll;

    private static final int MIN_DATA_POINTS = 10;

    private int dataPoints = 0;

    private float EPSILON = 1.0f;

    private float mOrientation = 0;

    private boolean initialized = false;

    public GifGyroscopeSensor(Context c, TextView label, Loaded3DObject loaded3DObject) {
        super(c);
        mLabel = label;
        mLoaded3DObject = loaded3DObject;
        mFrames = loaded3DObject.getFramesWithBlends();
        mView = loaded3DObject.getView();
    }

    public GifGyroscopeSensor(Context c, Loaded3DObject loaded3DObject) {
        super(c);
        mLoaded3DObject = loaded3DObject;
        mFrames = loaded3DObject.getFramesWithBlends();
        index = mFrames.size() / 2; //start in middle
        mView = loaded3DObject.getView();
    }

    // Must be called after blend count changes in Loaded3DObject
    public void update() {
        mFrames = mLoaded3DObject.getFramesWithBlends();
        index = mFrames.size() / 2; //reset to start in middle
    }

    /* Process values from fusedOrientation vector */
    @Override
    public void onFusedOrientationsCalculated() {

        if (dataPoints == MIN_DATA_POINTS) {

            // calculate velocity
            float dOrient = (fusedOrientation[2] * R2D) - mOrientation;
            float velocity = dOrient / dT;


            // If enough rotation is occurring, update the GyroImageView
            if (Math.abs(velocity) > EPSILON) {
                Log.d("LOG FUNCTION", "veloc: " + velocity);
                mView.updateDuration(velocity);
            }

        } else dataPoints++;

        mOrientation = fusedOrientation[2] * R2D;

    }

    public boolean isRunning() {
        return mRunning;
    }


    @Override
    public void start() {
        mView.setFrames(mLoaded3DObject.getFramesWithBlends());
        super.start();
        mRunning = true;
    }

    @Override
    public void stop() {
        super.stop();
        mView.setFrames(null);
        mRunning = false;
    }
}
