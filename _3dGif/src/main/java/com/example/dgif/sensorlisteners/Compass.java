package com.example.dgif.sensorlisteners;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import com.example.dgif.customviews.GyroImageView;

/**
 * Created by Carmina on 2/17/2015.
 */
public class Compass implements SensorEventListener {

    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;

    float[] mGravity;
    float[] mGeomagnetic;

    float roll;
    static final int ROLL_INDEX = 2;

    float mCurrentRoll;

    GyroImageView mImageView;
    AnimationDrawable mGif;

    TextView mLabel;

    static int RANGE = 10;


    public Compass(Context c, TextView label) {
        mSensorManager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mLabel = label;
    }

    public Compass(Context c, GyroImageView imageView, AnimationDrawable gif, TextView label) {
        mSensorManager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mLabel = label;
        mGif = gif;
        mImageView = imageView;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                roll = orientation[ROLL_INDEX]; // orientation contains: azimut, pitch and roll
                float roundedRoll = (roll * 100) / 100;
                mLabel.setText("Roll: " + roundedRoll);
                mCurrentRoll = roundedRoll;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public float getRoll() {
        return mCurrentRoll;
    }

    public void start() {
//
//        if (mGif.isRunning()) {
//            mGif.stop();
//            mImageView.setBackground(null);
//        }

        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);



    }

    public void stop() {
        mSensorManager.unregisterListener(this);

//        if (!mGif.isRunning()) {
//            mImageView.setBackground(mGif);
//            mGif.start();
//        }
    }


}
