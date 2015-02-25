package com.example.dgif;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Carmina on 2/24/2015.
 */
public class SerializableGif {

    private Bitmap[] mFrames;
    private float[] mOrientations;

    public SerializableGif(Bitmap[] frames, float[] orientations) {
        mFrames = frames;
        mOrientations = orientations;
    }

    public SerializableGif(ArrayList<BitmapDrawable> frames, float[] orientations) {
        Log.d("STEPS", " SeriazableGif Constructor. frames:  " + frames);
        mFrames = new Bitmap[frames.size()];
        for (int i = 0; i < frames.size(); i++) {
            mFrames[i] = frames.get(i).getBitmap();
        }
        mOrientations = orientations;
    }

    public Bitmap[] getFrames() {
        return mFrames;
    }

    public float[] getOrientations() {
        return mOrientations;
    }

    public void save() {

    }
}
