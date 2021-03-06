package com.example.dgif.utils;


import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;

import com.example.dgif.SerializableGif;

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



    public String saveSerializableGif(SerializableGif sg) {
        String filename = IMG_TAG + (new SimpleDateFormat("yyMMddHHmmss").format(new Date()));
        try {
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(sg);
            os.close();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();

        }

        return filename;
    }

    private Bitmap scaledBitmap(Bitmap bm) {
        Log.i(DEBUG_TAG, "bm size: " + bm.getWidth() + " + " + bm.getHeight());
        return Bitmap.createScaledBitmap(bm, bm.getWidth(), 900, true);

    }

    public Bitmap[] extractFramesFromSerialGif(SerializableGif sg) {
        System.gc();
        byte[][] rawFrames = sg.rawFrames;
        Bitmap[] bmps = new Bitmap[rawFrames.length];
        for (int i = 0; i < bmps.length; i++) {
            bmps[i] = scaledBitmap(loadBitmapFromByteArray(rawFrames[i],
                    506, 900));
        }
        return bmps;
    }

    public static Bitmap flip(Bitmap src, Constants.Direction type) {
        Matrix matrix = new Matrix();

        if(type == Constants.Direction.VERTICAL) {
            matrix.preScale(1.0f, -1.0f);
        }
        else if(type == Constants.Direction.HORIZONTAL) {
            matrix.preScale(-1.0f, 1.0f);
        } else {
            return src;
        }

        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    public SerializableGif flipSerializableGif(SerializableGif sg) {
        byte[][] rawFrames = sg.rawFrames;
        byte[][] flippedFrames = new byte[sg.rawFrames.length][];
        for (int i = 0; i < sg.rawFrames.length; i++) {
            Bitmap orig = BitmapFactory.decodeByteArray(rawFrames[i], 0, rawFrames[i].length);
            Bitmap b = flip(orig, Constants.Direction.HORIZONTAL);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            flippedFrames[i] = byteArray;
        }
        return new SerializableGif(flippedFrames);
    }

    public Bitmap getSerialGifThumbnail(SerializableGif sg) {
        // Uses first frame as avatar by default
        return loadBitmapFromByteArray(sg.rawFrames[0], Constants.AVATAR_WIDTH, Constants.AVATAR_HEIGHT);
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

    public String saveRaw3DObject(SerializableGif o) {
        String filename = IMG_TAG + (new SimpleDateFormat("yyMMddHHmmss").format(new Date()));
        try {
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(o);
            os.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filename;
    }

    public SerializableGif readSerializableGif(String filename) {
        SerializableGif o = null;
        try {
            FileInputStream fis = context.openFileInput(filename);
            ObjectInputStream is = new ObjectInputStream(fis);
            o = (SerializableGif) is.readObject();
            is.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return o;
    }



    public Bitmap[] getAllThumbnails() {
        System.gc();
        String[] files = context.fileList();
        Bitmap[] avatars = new Bitmap[files.length];
        for (int i = 0; i < avatars.length; i++) {
            SerializableGif sg = readSerializableGif(files[i]);
            avatars[i] = getSerialGifThumbnail(sg);
        }
        return avatars;
    }

    public Bitmap loadBitmapFromByteArray(byte[] data, int reqW, int reqH) {


        //Decode with inJustDecodeBounds = true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

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

        options.inSampleSize = inSampleSize;

        //Decode Bitmap with inSampleSize set and return
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);

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
