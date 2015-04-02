package com.example.dgif;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.example.dgif.utils.MemoryManager;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Carmina on 2/24/2015.
 */
public class SerializableGif implements Serializable {

    public byte[][] rawFrames;

    public SerializableGif(byte[][] rawFramesData) {
        Log.d("CONVERT TO ARRAY", "enter Serializable Gif constructor");
        rawFrames = rawFramesData;
        Log.d("CONVERT TO ARRAY", "exit Serializable Gif constructor");
    }

}
