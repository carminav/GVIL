package com.example.dgif;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;

/* @author Carmina Villaflores
 * Manages the saving and retrieving of data to internal app memory
 * TODO: Allow video files to be saved to external memory
 */

//TODO: Make Memory Manager a Singleton Class? But it needs a context

public class MemoryManager {

	private static final String DEBUG_TAG = "Memory Manager";
	private static final String IMG_TAG = "3dgif";
	

	private Context context;


	public MemoryManager(Context context) {
		this.context = context;
	}

	/*SAVE IMAGE
	 * Saves byte array as image in internal memory
	 */
	@SuppressLint("SimpleDateFormat")
	public void saveImage(byte[] data) {

		String filename = IMG_TAG + (new SimpleDateFormat("yyMMddHHmmss").format(new Date()));
		try {
			FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
			fos.write(data);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*GET ALL IMAGES
	 * Returns an array of bitmaps located in app's internal memory
	 * Loads scaled version of each Bitmap to save memory
	 */
	public Bitmap[] getAllImages(int reqW, int reqH) {

		String[] files = context.fileList();
		Bitmap[] images = new Bitmap[files.length];

		for (int i = 0; i < images.length; i++) {
            String filename = files[i];
            images[i] = loadScaledBitmapFromFIS(filename, reqW, reqH);
        }

		return images;

	}


    /* LOAD SCALED BITMAP FROM FILE INPUT STREAM
     * Saves memory loading bitmap by loading image in lowest resolution possible */
    public Bitmap loadScaledBitmapFromFIS(String filename, int reqW, int reqH) {

        FileInputStream fis = null;
        try {
            fis = context.openFileInput(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //Decode with inJustDecodeBounds = true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(fis, null, options);

        //calculate inSampleSize
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqH || width > reqW) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;

            //calculate the largest inSampleSize value that is a power of 2 and keeps both
            //height and width largest than the requested height and width
            while ((halfHeight / inSampleSize) > reqH
                    && (halfWidth / inSampleSize) > reqW) {
                inSampleSize *= 2;
            }
        }

        Log.d(DEBUG_TAG, "inSampleSize: " + inSampleSize);
        Log.d(DEBUG_TAG, "reqW: " + reqW + " reqH: " + reqH);
        options.inSampleSize = inSampleSize;

        try {
            fis = context.openFileInput(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //Decode Bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Rect rectPadding = new Rect(-1,-1,1,-1);
        return BitmapFactory.decodeStream(fis, rectPadding, options);

    }
	
	/* EXTERNAL STORAGE 
	 * Checks if external storage is available for read and write
	 */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) return true;
		else return false;
	}
	
	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) ||
				Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) return true;
		else return false;
	}

	
	

}
