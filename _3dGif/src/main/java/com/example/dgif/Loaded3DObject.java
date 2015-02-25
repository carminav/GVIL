package com.example.dgif;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;

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
    private static final String STEPS = "STEPS";

    private static final boolean GIF_MODE = true;
    private static final boolean GYRO_MODE = false;

    private boolean mMode = GIF_MODE;

    private Bitmap[] mFrames = null;
    private ArrayList<Bitmap> mFramesWithBlends = null;
    private AnimationDrawable mPlayableGif = null;
    private float[] mDelta = null;
    private float[] mOrientations = null;
    private int mNumBlends = 0;
    private ImageView mImageView = null;
    private int mFrameRate = 50;
    private Context mContext;

    public Loaded3DObject(Context c, SerializableGif rawGif, ImageView iv) {
       mContext = c;
       mImageView = iv;
       mFrames = rawGif.getFrames();
        Log.d(STEPS, "rawGif.getFrames() " + rawGif.getFrames().length);
       mOrientations = rawGif.getOrientations();
       mDelta = calculateDelta();
       mFramesWithBlends = renderUpdatedFramesWithBlends();
       mPlayableGif = renderPlayableGif();
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
    public void stopPlayingGif() {
        if (mPlayableGif != null && mPlayableGif.isRunning()) {
            mPlayableGif.stop();
            mImageView.setBackground(null);
        }
    }

    // Start the gif animation and attach it to its image view
    // so that its visible again
    public void startPlayingGif() {
        boolean d = mPlayableGif != null && !mPlayableGif.isRunning() && mMode;
        Log.d(STEPS, " Within Start Playing Gif. Will continue: " + d);
        Log.d(STEPS, "mPlayableGif frames: " + mPlayableGif.getNumberOfFrames());
        if (mPlayableGif != null && !mPlayableGif.isRunning() && mMode) {
            mImageView.setBackground(mPlayableGif);
            mPlayableGif.start();
        }
    }

    public void resume() {
        if (mMode == GIF_MODE) {
            if (mPlayableGif != null) mPlayableGif.start();
        } else {
            // TODO: Handle gyro on resume (register listener?)
        }
    }

    public void pause() {

    }

    // Render new frames to update delta values and the playable gif
    // This should be run in an async task since it is time consuming
    public void changeBlendCount(int count) {

        if (count < 0) Log.e(TAG, "Error: Blend count must be positive");
        else {

            startPlayingGif();

            mNumBlends = count;
            mDelta = calculateDelta();
            mFramesWithBlends = renderUpdatedFramesWithBlends();
            mPlayableGif = renderPlayableGif();

            startPlayingGif();
        }

    }
    // Takes already rendered frames and returns it as a looping animation
    // that goes forwards then backwards.
    // This should be called only after mFramesWithBlends is updated accordingly
    public AnimationDrawable renderPlayableGif() {
        Log.d(STEPS, " Call renderPlayableGif");
        AnimationDrawable anim = new AnimationDrawable();

        Log.d(STEPS, " mFramesWithBlends: " + mFramesWithBlends.size());

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

        Log.d(STEPS, " Call render Updated Frames with Blendsr");
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
        Log.d(STEPS, " renderUpdatedFrames gives: " + frames.size() + " frames total");
        return frames;
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

    // Calculate the difference in orientation values
    // This should be called everytime mNumBlends changes
    //TODO: Complete this method
    public float[] calculateDelta() {

        float[] delta = {1.0f, 1.0f, 1.0f};

        return delta;
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


}
