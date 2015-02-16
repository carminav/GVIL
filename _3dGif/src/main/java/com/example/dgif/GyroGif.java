package com.example.dgif;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Carmina on 2/11/2015.
 */
public class GyroGif implements SensorEventListener {

    private Context mContext;
    private AnimationDrawable mGif;
    private GyroImageView mImageView;
    private float lastValue;
    boolean start;
    private TextView mYLabel;

    private Canvas mCanvas;

    int mLastIndex;


    private float lowerBound;
    private float upperBound;

    private final static float RANGE = (float) 1.2;

    private SensorManager mSensorManager;

    private float mDelta;


    public GyroGif(Context context, GyroImageView imageView, TextView yLabel) {
        mContext = context;
        mImageView = imageView;
        mYLabel = yLabel;

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

    }

    public void start(AnimationDrawable gif) {
        mGif = gif;
        mDelta = RANGE / (float) gif.getNumberOfFrames();
        Log.d("GyroGif", gif.getNumberOfFrames() + " Delta: " + mDelta);
        if (gif.isRunning()) {
            gif.stop();
            mImageView.setBackground(gif.getFrame(0));
        }
        mLastIndex = 0;
        upperBound = 0;
        lowerBound = 0;
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);
    }

    public void stop() {
        start = false;
        mSensorManager.unregisterListener(this);
        printDebug();

        if (!mGif.isRunning()) {
            mImageView.setBitmap(null);
            mImageView.setBackground(mGif);
            mGif.start();
        }
    }

    private void printDebug() {
        Log.d("GyroGif", "[" + lowerBound + ", " + upperBound + "]");
    }

    public boolean isRunning() {
        return start;
    }


    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) return;

        if (!start) {
            start = true;
        } else {

        }

        float value = event.values[0];
       // mYLabel.setText("X: " + value);

        if (value > upperBound) {
            upperBound = value;
        }

        if (value < lowerBound) {
            lowerBound = value;
        }



        float radius = RANGE / 2;
        if (value >= -radius && value <= radius) {
            Drawable frame = null;
            int index = (int) (value / mDelta);
            if (value > 0 && index != 0) {
                frame = mGif.getFrame(index);
                //mImageView.setBackground(frame);
                mImageView.setBitmap(drawableToBitmap(frame));
                mImageView.invalidate();
            } else if (value < 0 && index != 0) {
                frame = mGif.getFrame(-index);
                //mImageView.setBackground(frame);
                mImageView.setBitmap(drawableToBitmap(frame));
                mImageView.invalidate();
            } else {
                index = 0;
               // mImageView.setBackground(mGif.getFrame(index));
            }
             if (index != mLastIndex) {
                 Log.d("Gyro", "index: " + index);
             }
            mLastIndex = index;
        }



    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


}
