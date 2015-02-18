package com.example.dgif.sensorlisteners;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

/**
 * Created by Carmina on 2/17/2015.
 */
public class AutoFocusSensorListener implements SensorEventListener, Camera.AutoFocusCallback {

    private static String TAG = "Auto Focus Sensor Listener";
    private float mMotionX;
    private float mMotionY;
    private float mMotionZ;
    private Camera mCamera;

    public AutoFocusSensorListener(Camera camera) {
        mCamera = camera;
        if (camera == null) {
            Log.e(TAG, "Camera is null");
        }
        mMotionX = 0;
        mMotionX = 0;
        mMotionX = 0;
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        //Log.d(DEBUG_TAG, "onAutoFocus");

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(Math.abs(event.values[0] - mMotionX) > 1
                || Math.abs(event.values[1] - mMotionY) > 1
                || Math.abs(event.values[2] - mMotionZ) > 1 ) {

            try {

                mCamera.autoFocus(this);
                Log.e(TAG, "try autofocus SUCCESS");
            } catch (RuntimeException e) {
                Log.e(TAG, "try autofocus FAIL");
            }

            mMotionX = event.values[0];
            mMotionY = event.values[1];
            mMotionZ = event.values[2];


        }

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

}
