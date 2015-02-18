package com.example.dgif.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

/**
 * Created by Carmina on 2/17/2015.
 */
public class RenderUtils {

    //Method from Ketai project! Not mine! See below...
    public static void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {

        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {       int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0)                  r = 0;               else if (r > 262143)
                    r = 262143;
                if (g < 0)                  g = 0;               else if (g > 262143)
                    g = 262143;
                if (b < 0)                  b = 0;               else if (b > 262143)
                    b = 262143;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }



    /* GET INTERMEDIATE IMAGE
     * Uses linear interpolation to get the intermediate blend of pics a and b
     * based on a weight.
     */
    public static Bitmap getIntermediateImage(Bitmap a, Bitmap b, double weight) {
        Log.d("TEST GIF VIEW", "Intermediate Image Reached");
        int height = a.getHeight();
        int width = a.getWidth();
        Bitmap blend = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

			/*Get color information for pixels at x,y*/

		    /* Pixel A */
                int pixelA =  a.getPixel(x, y);
                int redA = Color.red(pixelA);
                int greenA = Color.green(pixelA);
                int blueA = Color.blue(pixelA);
                int alphaA = Color.alpha(pixelA);

			/* Pixel B */
                int pixelB = b.getPixel(x, y);
                int redB = Color.red(pixelB);
                int greenB = Color.green(pixelB);
                int blueB = Color.blue(pixelB);
                int alphaB = Color.alpha(pixelB);

			/* Blended Pixel */
                int red = (int) ((1 - weight) * redA + weight * redB);
                int green = (int) ((1 - weight) * greenA + weight * greenB);
                int blue = (int) ((1 - weight) * blueA + weight * blueB);
                int alpha = (int) ((1 - weight) * alphaA + weight * alphaB);

                int blendedColor = Color.argb(alpha, red, green, blue);


                blend.setPixel(x, y, blendedColor);

            }
        }


        return blend;
    }


}
