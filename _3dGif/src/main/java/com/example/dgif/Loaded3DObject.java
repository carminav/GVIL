package com.example.dgif;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;

import com.example.dgif.customviews.GyroImageView;
import com.example.dgif.sensorlisteners.Gyro.GifGyroscopeSensor;
import com.example.dgif.utils.MemoryManager;
import com.example.dgif.utils.RenderUtils;

import java.util.ArrayList;

/**
 * Created by Carmina on 2/24/2015.
 *
 * Represents the finished object compiled when taking a series of images,
 * which can be played as a looping GIF or through movement with the
 * gyroscope in the GifGyroscopeSensor class.
 */
public class Loaded3DObject {

    private static final String TAG = "Loaded3DObject";

    private static final boolean GIF_MODE = true;
    private static final boolean GYRO_MODE = false;

    private boolean mMode = GIF_MODE;

    private Bitmap[] mFrames = null;
    private ArrayList<Bitmap> mFramesWithBlends = null;
    private AnimationDrawable mPlayableGif = null;
    private int mNumBlends = 0;
    private GyroImageView mImageView = null;
    private int mFrameRate = 50;
    private Context mContext;


    private GifGyroscopeSensor mGyroscopeSensor = null;

    public Loaded3DObject(Context c, SerializableGif rawGif, GyroImageView iv) {
        MemoryManager m = new MemoryManager(c);
       mContext = c;
       mImageView = iv;
       mFrames = m.extractFramesFromSerialGif(rawGif);
       mFramesWithBlends = renderUpdatedFramesWithBlends();
       mPlayableGif = renderPlayableGif();
       mGyroscopeSensor = new GifGyroscopeSensor(c, this);
    }



    // Rebuild the playable gif with a different fps
    // There is no need to call this in a separate thread since
    // no new blends are being rendered.
    public void changeFrameRate(int fps) {

        stopPlayingGif();

        mFrameRate = fps;

        //Rebuild animation with different fps, but with the same frames
        AnimationDrawable anim = new AnimationDrawable();
        for (int i = 0; i < mPlayableGif.getNumberOfFrames(); i++) {
            anim.addFrame(mPlayableGif.getFrame(i), mFrameRate);
        }

        anim.setOneShot(false);
        mPlayableGif = anim;
        startPlayingGif();
    }


    // Stop the gif animation and detach it from its image view
    private void stopPlayingGif() {
        if (mPlayableGif != null && mPlayableGif.isRunning()) {
            mPlayableGif.stop();
        }
    }

    // Start the gif animation and attach it to its image view
    // so that its visible again
    private void startPlayingGif() {
        if (mPlayableGif != null && !mPlayableGif.isRunning()) {
            mImageView.setBackground(mPlayableGif);
            mPlayableGif.start();
        }
    }

    public void play(boolean gif) {
        mMode = gif;
        if (gif) {
            if (mGyroscopeSensor.isRunning()) mGyroscopeSensor.stop();
            startPlayingGif();
        } else {
            stopPlayingGif();
            mImageView.setBackground(mPlayableGif);
            mGyroscopeSensor.start();
        }
    }

    public void resume() {
        play(mMode);
    }

    public void pause() {
        if (mMode) {
            stopPlayingGif();
        } else {
            mGyroscopeSensor.stop();
        }
    }

    // Render new frames to update delta values and the playable gif
    // This should be run in an async task since it is time consuming
    public void changeBlendCount(int count) {

        if (count < 0) Log.e(TAG, "Error: Blend count must be positive");
        else {


            mGyroscopeSensor.update();
         //   stopPlayingGif();

            mNumBlends = count;

            mFramesWithBlends = null;
            System.gc();
            mFramesWithBlends = renderUpdatedFramesWithBlends();
            System.gc();
          //  mPlayableGif = renderPlayableGif();

            //startPlayingGif();
        }

    }



    // Takes already rendered frames and returns it as a looping animation
    // that goes forwards then backwards.
    // This should be called only after mFramesWithBlends is updated accordingly
    public AnimationDrawable renderPlayableGif() {
        AnimationDrawable anim = new AnimationDrawable();

        //Add frames with blends forward
        for (int i = 0; i < mFramesWithBlends.size(); i++) {
            anim.addFrame(new BitmapDrawable(mContext.getResources(), mFramesWithBlends.get(i)),
                            mFrameRate);
        }

        //Add frames with blends reverse
        for (int i = mFramesWithBlends.size() - 1; i >= 0; i--) {
            anim.addFrame(anim.getFrame(i), mFrameRate);
        }

        anim.setOneShot(false);
        return anim;
    }


    //Render new intermediate blends
    //This would be called after the number of blends changes
    private ArrayList<Bitmap> renderUpdatedFramesWithBlends() {

        int totalBlends = (mFrames.length - 1) * mNumBlends;
        ArrayList<Bitmap> frames = new ArrayList<Bitmap>();

        for (int i = 0; i < mFrames.length - 1; i++)  {
            frames.add(mFrames[i]);
            for (int j = 0; j < mNumBlends; j++) {
                double weight = ((1 / (double)(mNumBlends + 1))) * (j + 1);

                frames.add(RenderUtils.getIntermediateImage(mFrames[i], mFrames[i + 1], weight));
            }
        }

        //add last frame
        frames.add(mFrames[mFrames.length - 1]);

        return frames;
    }

    public void setMode(boolean gif) {
        mMode = gif;
    }

    //Switch between GIF mode and GYRO mode
    public void toggleMode() {
        if (mMode == GIF_MODE) {
            mMode = GYRO_MODE;
            stopPlayingGif();
        } else {
            mMode = GIF_MODE;
            startPlayingGif();
        }
    }

    public boolean isGifMode() {
        return mMode;
    }

    public ArrayList<Bitmap> getFramesWithBlends() {
        return mFramesWithBlends;
    }

    public Bitmap[] getFrames() {
        return mFrames;
    }

    public AnimationDrawable getPlayableGif() {
        return mPlayableGif;
    }

    public GyroImageView getView() {
        return mImageView;
    }


}
