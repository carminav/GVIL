package com.example.dgif;



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.YuvImage;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


//TODO: Add Rotation Vector sensor usage and replace accelerometer data in view
public class CameraPreview extends Activity {
	
	
	private static final String DEBUG_TAG = "Preview";
	
	
	
	int mCount = 0;


    /* Hardcoded values for Samsung Galaxy Nexus */
	public final static int VIEW_WIDTH = 720;
	public final static int VIEW_HEIGHT = 1280;
	public final static int PIC_WIDTH =  1024;
	public final static int PIC_HEIGHT =   1280;
	
    byte[] mSurfaceBytes;

	protected static final int LOAD_CAM_PREV = 0;
	
	private Camera mCamera;
	private CamView mPreview;
	private boolean mIsPreviewing;
	private UIHandler mHandler;
	private FrameLayout mPreviewFrame;
	
	private Button mGalleryButton;

	private int[] mPixels;

	
	private float mMotionX;
	private float mMotionY;
	private float mMotionZ;
	
	
	private float mMotionXView;
	private float mMotionYView;
	private float mMotionZView;
	private GridLayout mHeader;
	
	private AutoFocusListener mAutoFocusCallback;
	private Sensor mAccelerometer;
	private SensorManager mSensorManager;
	
	private MemoryManager mMemoryManager;
	
	private boolean onPauseCalled;
	
	private TextView mCoordXView;
	private TextView mCoordYView;
	private TextView mCoordZView;
	
	private ImageView blinkingArrowView;
	private AnimationDrawable blinkingArrow;
	
	private ImageView mOverlayView;
	private Bitmap mLastImage;
	
	private RelativeLayout mFrameWrapper;
	Button mCaptureButton;
	Button mTrashButton;
	Button mPreviewButton;
	
	ImageView target;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.camera_preview);

		
		mFrameWrapper = (RelativeLayout) findViewById(R.id.camera_wrapper);
	    mCaptureButton = (Button) findViewById(R.id.capture_button);
	    mPreviewButton = (Button) findViewById(R.id.preview_button);
		mPreviewFrame = (FrameLayout) findViewById(R.id.camera_preview);
		mGalleryButton = (Button) findViewById(R.id.gallery_button);
		mTrashButton = (Button) findViewById(R.id.trash_button);


        Typeface fontFamily = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont.ttf");
		mGalleryButton.setTypeface(fontFamily);

	    mCoordXView = (TextView) findViewById(R.id.xcoordView);
	    mCoordYView = (TextView) findViewById(R.id.ycoordView);
	    mCoordZView = (TextView) findViewById(R.id.zcoordView);

	    
	    mOverlayView = (ImageView) findViewById(R.id.image_overlay_view);
	    
		
		mMemoryManager = new MemoryManager(this);
		
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mAutoFocusCallback = new AutoFocusListener();
		
		mIsPreviewing = false;
		mHandler = new UIHandler();
		
		mMotionX = 0;
		mMotionY = 0;
		mMotionZ = 0;
		
		mMotionXView = 0;
		mMotionYView = 0;
		mMotionZView = 0;
		
		
		
		mFrameWrapper.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {


				float x = event.getX();
				float y = event.getY();

                //TODO: autofocus on touch
				
				return false;
			}
			
		});
		
		mGalleryButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(CameraPreview.this, ImageGallery.class);
				startActivity(i);
				
			}
			
		});
		

		
		
		mTrashButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mCount = 0;
				if (mOverlayView.getVisibility() == View.VISIBLE) 
					mOverlayView.setVisibility(View.GONE);
				mTrashButton.setVisibility(View.GONE);
				mPreviewButton.setVisibility(View.GONE);
			}
			
		});
		
		mPreviewButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(CameraPreview.this, TestGifView.class);
				i.putExtra("lastIndices", mCount);
				startActivity(i);
			}
			
		});
		
		
		
		// Begin loading camera resource
		new Thread(new LoadCameraAndPrev()).start();
		onPauseCalled = false;
		

		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	
        if (onPauseCalled) {
        	new Thread(new LoadCameraAndPrev()).start();
        }
	}
	

	@Override
	protected void onPause() {
		super.onPause();
		
		stopPreview();
		releaseCamera();
		
		if (mOverlayView.getVisibility() == View.VISIBLE) {
			mOverlayView.setVisibility(View.GONE);
		}
		
		if (mTrashButton.getVisibility() == View.VISIBLE) {
			mTrashButton.setVisibility(View.GONE);
			mPreviewButton.setVisibility(View.GONE);
		}
		
		mCount = 0;
		onPauseCalled = true;
	}
	

	/*********************************************************************************************/
	
	 
	/*SETUP VIEW
	 * - Called only after a LoadCameraAndPrev thread is finished
	 * - Adds listener to newly created view view and adds it to the frame
	 */
	public void setupView() {

		mPreview = new CamView(this, mCamera);
		mPreviewFrame.addView(mPreview);
		
		mCaptureButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mCamera != null && mIsPreviewing) {
					mCount++;
    				mCamera.takePicture(null, null, mPictureCallback);
				}
				
			}
			
		});
		


	}


    //Method from Ketai project! Not mine! See below...
    void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {

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


    /* START PREVIEW
	 * - Sets preview display as surface holder
	 * - starts camera's preview
	 */
	private void startPreview(final SurfaceHolder holder) {
		
		if (mCamera != null && !mIsPreviewing) {
            mCamera.setPreviewCallback(mPreview);
			try {
				mCamera.setPreviewDisplay(holder);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			mCamera.startPreview();
			mIsPreviewing = true;
			
		} else {
			Log.e(DEBUG_TAG, "mCamera is null and mIsPrev: " + mIsPreviewing);
		}

	}
	
	
	/* STOP PREVIEW
	 * Stops camera preview on surface view (cam view) and unregisters listener
	 */
	private void stopPreview() {
		if (mCamera != null && mIsPreviewing) {
            mCamera.setPreviewCallback(null);
			Log.d(DEBUG_TAG, "stop preview");
			mIsPreviewing = false;
			mCamera.stopPreview();


		}
	}
	
	/* RELEASE CAMERA
	 * Release camera so other applications can use it
	 */
	private void releaseCamera() {
		if (mCamera != null) {
			mSensorManager.unregisterListener(mAutoFocusCallback, mAccelerometer);
			mCamera.release();
			mCamera = null;

		}
	}


	
	Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

        private Bitmap constructBitmap() {
            Size prevSize = mCamera.getParameters().getPreviewSize();
            Bitmap bm = Bitmap.createBitmap(prevSize.width, prevSize.height, Bitmap.Config.ARGB_8888);
            bm.copyPixelsFromBuffer(IntBuffer.wrap(mPixels));
            return bm;
        }



//        private Bitmap getCroppedBitmap(byte[] data) {
//            int[] pixels = new int[VIEW_WIDTH * VIEW_HEIGHT];
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//            bitmap.getPixels;
//
//        }


		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			
			mIsPreviewing = false;

            mMemoryManager.saveImage(data);

			//save image
            //TODO: move saveImage to when user agrees to save it, not upon picture taking

            //set overlay
            Bitmap b = constructBitmap();
            Matrix matrix = new Matrix();
            matrix.postRotate(90);

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(b,VIEW_HEIGHT,VIEW_WIDTH,true);
            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);
            mOverlayView.setImageBitmap(rotatedBitmap);

			//restart preview
			startPreview(mPreview.getHolder());


		    mTrashButton.setVisibility(View.VISIBLE);

		    mCaptureButton.bringToFront();
		    mTrashButton.bringToFront();
		    mGalleryButton.bringToFront();
		    
		    if (mCount >= 2) {
		    	mPreviewButton.setVisibility(View.VISIBLE);
			    mPreviewButton.bringToFront();
		    }

			if (mOverlayView.getVisibility() == View.GONE) {
				mOverlayView.setVisibility(View.VISIBLE);
				mGalleryButton.bringToFront();
				
			}

		}
		
	};
	

	
	/*********************************************************************************************/
    /*									 INNER CLASSES                                           */
	/*********************************************************************************************/
	
	/*LOAD CAMERA AND PREV CLASS (THREAD USE)
	 * - Opens camera instance
	 * - Sets parameters for camera
	 * - Attaches sensor listener
	 */
	private class LoadCameraAndPrev implements Runnable {

		@Override
		public void run() {
			
			//Open Camera
			try {
				mCamera = Camera.open();
				Log.i(DEBUG_TAG, "Camera opened");
			} catch (RuntimeException e) {
				Log.e(DEBUG_TAG, "Camera will not open. onCreate");
				finish();
			}
			
			Camera.Parameters p = mCamera.getParameters();
			p.setPreviewSize(VIEW_HEIGHT, VIEW_WIDTH);      //Note that height and width are switched 
			
			p.setRotation(90);
            p.setPreviewFormat(ImageFormat.NV21); //Possibly unnecessary


			
			p.setPictureSize(PIC_HEIGHT, PIC_WIDTH);
			List<Integer> formats = p.getSupportedPreviewFormats();

			mCamera.setParameters(p);
			
			mSensorManager.registerListener(mAutoFocusCallback, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
			
			mCamera.setDisplayOrientation(90);
			
			mHandler.sendMessage(mHandler.obtainMessage(LOAD_CAM_PREV));
			
		}
		
	}
	
	/*UI HANDLER CLASS
	 * Responsible for posting tasks to UI's task queue from another thread
	 */
	@SuppressLint("HandlerLeak")
	private class UIHandler extends Handler {
		
		@Override
		public void handleMessage(Message msg) {

			if (msg != null) {
				switch (msg.what) {
				case LOAD_CAM_PREV:
				
					setupView();
					
				}
			} else {
				Log.e(DEBUG_TAG, "handler msg is null");
			}

		}
		
		
	}
	
	
	/* AUTO FOCUS LISTENER CLASS
	 * - Used to auto focus whenever there is a change in movement
	 */
	private class AutoFocusListener implements SensorEventListener, AutoFocusCallback {

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			//Log.d(DEBUG_TAG, "onAutoFocus");
			
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			
			if(Math.abs(event.values[0] - mMotionX) > 1 
		            || Math.abs(event.values[1] - mMotionY) > 1 
		            || Math.abs(event.values[2] - mMotionZ) > 1 ) {
		           ;
		            try {
		          
		                mCamera.autoFocus(this);
		            } catch (RuntimeException e) { 
		            	Log.e(DEBUG_TAG, "try autofocus FAIL");
		            }
		            
		            mMotionX = event.values[0];
		            mMotionY = event.values[1];
		            mMotionZ = event.values[2];
		            
		            
		        }
			
			if(Math.abs(event.values[0] - mMotionXView) > 0.05 
		            || Math.abs(event.values[1] - mMotionYView) > 0.05 
		            || Math.abs(event.values[2] - mMotionZView) > 0.05 ) {
				
				mMotionXView = (float) Math.round(event.values[0] * 1000)/1000;
				mMotionYView = (float) Math.round(event.values[1] * 1000)/1000;
				mMotionZView = (float) Math.round(event.values[2] * 1000)/1000;
				
				mCoordXView.setText("" + mMotionXView);
				mCoordYView.setText("" + mMotionYView);
				mCoordZView.setText("" + mMotionZView);
			}
			
		}

		

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}
		
	}
	

	
	/*CAM VIEW CLASS
	 * Serves as the preview screen for seeing what the camera sees live
	 */
	private class CamView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

		private SurfaceHolder mHolder;
		private Camera mCamera;

		
		public CamView(Context context, Camera camera) {
			super(context);
			mCamera = camera;
			mHolder = getHolder();
			mHolder.addCallback(this);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			startPreview(mHolder);
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			if (mHolder.getSurface() == null) {
				return;
			}
			
			stopPreview();
			startPreview(mHolder);

		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			//do nothing
		}


        @Override
        public void onPreviewFrame(byte[] bytes, Camera camera) {

            mSurfaceBytes = bytes;

            Size previewSize = camera.getParameters().getPreviewSize();
            int format = camera.getParameters().getPreviewFormat();
            int[] pixels = new int[previewSize.width * previewSize.height * (ImageFormat.getBitsPerPixel(format) / 8)];
            decodeYUV420SP(pixels, bytes, previewSize.width, previewSize.height);
            mPixels = pixels;
        }

    }
	
}
