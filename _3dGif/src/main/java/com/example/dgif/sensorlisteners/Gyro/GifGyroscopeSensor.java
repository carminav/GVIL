package com.example.dgif.sensorlisteners.Gyro;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import com.example.dgif.Loaded3DObject;
import com.example.dgif.customviews.GyroImageView;

import java.util.ArrayList;

/**
 * Created by Carmina on 2/24/2015.
 */
public class GifGyroscopeSensor extends BaseGyroscopeSensor {

    private float R2D = 57.2957795f; // Conversion from radians to Degrees


    private TextView mLabel;
    private AnimationDrawable mGif;
    private GyroImageView mImageView;
    private ArrayList<Bitmap> frames;

    private Loaded3DObject loaded3DObject;

    public GifGyroscopeSensor(Context c, TextView label, Loaded3DObject loaded3DObject) {
        super(c);
        mLabel = label;
        this.loaded3DObject = loaded3DObject;
        mGif = loaded3DObject.getPlayableGif();
        frames = loaded3DObject.getFramesWithBlends();
    }

    /* Process values from fusedOrientation vector */
    @Override
    public void onFusedOrientationsCalculated() {
        mLabel.setText(" Roll: " + Math.round(fusedOrientation[2] * R2D));
    }


    @Override
    public void start() {
        super.start();
        if (mGif != null && mGif.isRunning()) {
            mImageView.setBackground(null);
            mGif.stop();
        }

    }

    @Override
    public void stop() {
        super.stop();
        if (mGif != null && !mGif.isRunning()) {
            mImageView.setBackground(mGif);
            mGif.start();
        }
    }
}
