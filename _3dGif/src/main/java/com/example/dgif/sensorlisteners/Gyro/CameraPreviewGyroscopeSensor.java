package com.example.dgif.sensorlisteners.Gyro;

import android.content.Context;
import android.widget.TextView;

/**
 * Created by Carmina on 2/24/2015.
 */
public class CameraPreviewGyroscopeSensor extends BaseGyroscopeSensor {

    private TextView mLabel;
    private float R2D = 57.2957795f; // Conversion from radians to Degrees

    public CameraPreviewGyroscopeSensor(Context c, TextView label) {
        super(c);
        mLabel = label;
    }

    /* Perform processing of values in fusedOrientations field */
    @Override
    public void onFusedOrientationsCalculated() {
        mLabel.setText(" Roll: " + Math.round(fusedOrientation[2] * R2D));
    }



}
