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



    public SerializableGif(Bitmap[] frames) {
        mFrames = frames;


    }

    public SerializableGif(ArrayList<BitmapDrawable> frames) {
        mFrames = new Bitmap[frames.size()];
        for (int i = 0; i < frames.size(); i++) {
            mFrames[i] = frames.get(i).getBitmap();
        }

    }

    public Bitmap[] getFrames() {
        return mFrames;
    }




    public void save() {

    }
}
