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
	
	public final static int VIEW_WIDTH = 720;
	public final static int VIEW_HEIGHT = 1280;
	public final static int PIC_WIDTH =  1024; //480; //1458;
	public final static int PIC_HEIGHT =   1280;//640; //2592;
	public final static int VIEW_PIC_RATIO = VIEW_WIDTH / PIC_WIDTH;
    public final static int DPI = 316;
	
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
		
		target = new ImageView(this);
		target.setBackgroundResource(R.drawable.ic_action_locate);
		
		mLastImage = null;
		
		mFrameWrapper = (RelativeLayout) findViewById(R.id.camera_wrapper);
		
	    mCaptureButton = (Button) findViewById(R.id.capture_button);
	    
	    mPreviewButton = (Button) findViewById(R.id.preview_button);
		
		mPreviewFrame = (FrameLayout) findViewById(R.id.camera_preview);
		mGalleryButton = (Button) findViewById(R.id.gallery_button);

		
		mTrashButton = (Button) findViewById(R.id.trash_button);

		
	    mCoordXView = (TextView) findViewById(R.id.xcoordView);
	    mCoordYView = (TextView) findViewById(R.id.ycoordView);
	    mCoordZView = (TextView) findViewById(R.id.zcoordView);
	    
	    mHeader = (GridLayout) findViewById(R.id.coord_view);
	    
	    blinkingArrowView = (ImageView) findViewById(R.id.blinking_arrow_view);
	    blinkingArrow = ((AnimationDrawable) blinkingArrowView.getDrawable());
	    
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
				
				if (target.getParent() != null) {
					((RelativeLayout)target.getParent()).removeView(target);
				}
				
				Log.d(DEBUG_TAG, "preview frame touched");
				float x = event.getX();
				float y = event.getY();
				Log.d(DEBUG_TAG, "preview frame touched at " + x + "," + y);
				RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(150,150);
				p.setMargins((int)x - 75, (int)y - 75, 0, 0);
				target.setLayoutParams(p);
				mFrameWrapper.addView(target);

				
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
		
		//drawHeader();
		
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		View decorView = getWindow().getDecorView();

		// Hide the status bar.
		//int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
		//decorView.setSystemUiVisibility(uiOptions);

		// Hide action bar
//		ActionBar actionBar = getActionBar();
//		actionBar.hide();
		
	
        if (onPauseCalled) {
        	new Thread(new LoadCameraAndPrev()).start();
        }
        
        
		
		
	}
	
//
//	private void drawHeader() {
//
//		int color = Color.parseColor(THEME_COLOR);
//
//		//Get Screen Dimensions
//		Display display = getWindowManager().getDefaultDisplay();
//		Point size = new Point();
//		display.getSize(size);
//		int width = size.x;
//		int height = size.y;
//		Log.d(DEBUG_TAG, "Screen dimensions: " + width + " x " + height);
//
//		//int headerHeight = (int) (0.07 * height);
//		int headerHeight = 41;
//
//		//Draw Header
//		LinearLayout header = new LinearLayout(this);
//		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, headerHeight);
//		header.setLayoutParams(p);
//		header.setOrientation(LinearLayout.HORIZONTAL);
//		header.setBackgroundColor(Color.BLACK);
//		header.setVisibility(View.VISIBLE);
//
//		mFrameWrapper.addView(header);
//
//		//Draw Footer
//
//		//int footerHeight = (int) (.13 * height);
//		int footerHeight = 50;
//		RelativeLayout footer = new RelativeLayout(this);
//
//		RelativeLayout.LayoutParams p2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, footerHeight);
//		//p2.setMargins(0, height - footerHeight - 50, 0, 0);
//		p2.setMargins(0, height - footerHeight - 50, 0, 0);
//		footer.setLayoutParams(p2);
//		footer.setBackgroundColor(Color.BLACK);
//		footer.setVisibility(View.VISIBLE);
//	//	footer.setTop(height - headerHeight);
//
//		mFrameWrapper.addView(footer);
//
//
//	}


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		stopPreview();
		releaseCamera();
		
		if (blinkingArrow.isRunning()) {
			blinkingArrow.stop();
			blinkingArrowView.setVisibility(View.GONE);
		}
		
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
		
//		mPreviewFrame.setOnTouchListener(new View.OnTouchListener() {
//			
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				Log.i(DEBUG_TAG, "view touched");
//
//				if (mCamera != null && mIsPreviewing) {
//					mCamera.takePicture(null, null, mPictureCallback);
//				}
//
//				return false;
//			}
//		});

		mPreviewFrame.addView(mPreview);
		
		mCaptureButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mCamera != null && mIsPreviewing) {
					mCount++;

//
//                    mPreview.setDrawingCacheEnabled(true);
//                    mPreview.buildDrawingCache(true);
//                    Bitmap bmp = Bitmap.createBitmap(mPreview.getDrawingCache());
//                    mPreview.setDrawingCacheEnabled(false);
//
//                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//                    byte[] bitmapdata = bos.toByteArray();
//
//                    mMemoryManager.saveImage(bitmapdata);
//                    mOverlayView.setImageBitmap(bmp);
//                    mOverlayView.setVisibility(View.VISIBLE);


    				mCamera.takePicture(null, null, mPictureCallback);
				}
				
			}
			
		});
		


	}
//
//    private void setImageOverlay() {
//
//
//
//    }
//
//    private Bitmap getSurfaceViewPixels() {
//
//    }


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

    public static void YUV_NV21_TO_RGB(int[] argb, byte[] yuv, int width, int height) {
        final int frameSize = width * height;

        final int ii = 0;
        final int ij = 0;
        final int di = +1;
        final int dj = +1;

        int a = 0;
        for (int i = 0, ci = ii; i < height; ++i, ci += di) {
            for (int j = 0, cj = ij; j < width; ++j, cj += dj) {
                int y = (0xff & ((int) yuv[ci * width + cj]));
                int v = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 0]));
                int u = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 1]));
                y = y < 16 ? 16 : y;

                int r = (int) (1.164f * (y - 16) + 1.596f * (v - 128));
                int g = (int) (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = (int) (1.164f * (y - 16) + 2.018f * (u - 128));

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                argb[a++] = 0xff000000 | (r << 16) | (g << 8) | b;
            }
        }
    }

    /* START PREVIEW
	 * - Sets preview display as surface holder
	 * - starts camera's preview
	 */
	private void startPreview(final SurfaceHolder holder) {
		
		if (mCamera != null && !mIsPreviewing) {

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
			Log.d(DEBUG_TAG, "stop preview");
			mIsPreviewing = false;
			mCamera.stopPreview();
            mCamera.setPreviewCallback(null);

		}
	}
	
	/* RELEASE CAMERA
	 * Release camera so other applications can use it
	 */
	private void releaseCamera() {
		if (mCamera != null) {
			mSensorManager.unregisterListener(mAutoFocusCallback, mAccelerometer);
            mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;

		}
	}


	
	Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {



        public Bitmap loadScaledOverlayBitmap(byte[] data) {

            /* Crop from both sides */
            int cropWidth = (PIC_WIDTH - VIEW_WIDTH) / 2;
            Log.d("LOAD SCALED OVERLAY", "crop width: " + cropWidth);

            //convert 1280 x 1024 bitmap to 1280 x 720
            Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);

            Log.d("LOAD SCALED OVERLAY", "bitmap width: " + bm.getWidth());
            Log.d("LOAD SCALED OVERLAY", "bitmap height: " + bm.getHeight());

            //test
            //cropWidth -= 30;
            //test

            Bitmap croppedBM = Bitmap.createBitmap(bm, cropWidth, 0, VIEW_WIDTH, VIEW_HEIGHT);

            Log.d("LOAD SCALED OVERLAY", "final bitmap width: " + croppedBM.getWidth());
            Log.d("LOAD SCALED OVERLAY", "final bitmap height: " + croppedBM.getHeight());



            return croppedBM;
        }

        private Bitmap screenShot(View v) {
//            Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
//            Canvas canvas = new Canvas(bitmap);
//            view.draw(canvas);


            v.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v.getDrawingCache());
            v.setDrawingCacheEnabled(false);

//            DisplayMetrics dm = getResources().getDisplayMetrics();
//            v.measure(View.MeasureSpec.makeMeasureSpec(dm.widthPixels, View.MeasureSpec.EXACTLY),
//                    View.MeasureSpec.makeMeasureSpec(dm.heightPixels, View.MeasureSpec.EXACTLY));
//            v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
//            Bitmap returnedBitmap = Bitmap.createBitmap(v.getMeasuredWidth(),
//                    v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
//            Canvas c = new Canvas(returnedBitmap);
//            v.draw(c);
//
//            return returnedBitmap;

            return bitmap;
        }



        private Bitmap constructBitmap() {
            Size prevSize = mCamera.getParameters().getPreviewSize();
            Bitmap bm = Bitmap.createBitmap(prevSize.width, prevSize.height, Bitmap.Config.ARGB_8888);
            bm.copyPixelsFromBuffer(IntBuffer.wrap(mPixels));
            return bm;
        }

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			
			mIsPreviewing = false;


			
			//save image
            //TODO: move saveImage to when user agrees to save it, not upon picture taking
			//mMemoryManager.saveImage(data);
            Bitmap b = constructBitmap();

            Matrix matrix = new Matrix();
            matrix.postRotate(90);

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(b,VIEW_HEIGHT,VIEW_WIDTH,true);

            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);

            mOverlayView.setImageBitmap(rotatedBitmap);


            //Get current mPixels
            Log.i("PIXELS", "mPixels[10] = " +  mPixels[10]);
            Log.i("PIXELS", "mPixels length: " + Integer.toHexString(mPixels.length));
            int format = mCamera.getParameters().getPreviewFormat();
            int bitsPerPixel = ImageFormat.getBitsPerPixel(format / 8);
			Log.i("PIXELS", "mPixels bitsPerPixel: " + bitsPerPixel);


			//restart preview
			startPreview(mPreview.getHolder());

            Camera.Parameters p = mCamera.getParameters();
            Camera.Size size = p.getPictureSize();
            Camera.Size s = p.getPreviewSize();
            Log.d(DEBUG_TAG, "Picture Size: " + size.width + " x " + size.height);
            Log.d(DEBUG_TAG, "Preview Size: " + s.width + " x " + s.height);

			
//			//TODO: Make counter. When n pictures are taken, give option to create gif.
//			/* Set picture to image overlay */
//			Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
//
//			//resize
//			/***OLD**/
//			int cropFromBothSides = 45;
//			int newWidth = 480 - (2 * cropFromBothSides);
//			double newRatio = (double)720 / newWidth;
//			int newHeight = (int) (640 * newRatio);
//
//			Bitmap croppedBM = Bitmap.createBitmap(bm, cropFromBothSides - 5, 0, newWidth, 640);
//			Log.d(DEBUG_TAG, "cropped bm: " + croppedBM.getWidth() + " x " + croppedBM.getHeight());
//
//			Bitmap scaledBM = Bitmap.createScaledBitmap(croppedBM, 720, 1050, true);
//
//
//			//put in imageview
//		    mOverlayView.setImageBitmap(scaledBM);

           // mOverlayView.setImageBitmap(loadScaledOverlayBitmap(data));


          //  mMemoryManager.saveImage(byteArray);
          //  mOverlayView.setImageBitmap(screenShot(mPreview));
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
				mHeader.bringToFront();
				
			}

            Log.d("MOVERLAY", "mOverlay width: " + mOverlayView.getWidth());
            Log.d("MOVERLAY", "mOverlay height: " + mOverlayView.getHeight());
			
			
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
            p.setPreviewFormat(ImageFormat.NV21);


			
			p.setPictureSize(PIC_HEIGHT, PIC_WIDTH);
			List<Integer> formats = p.getSupportedPreviewFormats();
            Log.d("FORMATS", "formats size: " + formats.size());
			//List<Size> sHeights = p.getSupportedPictureSizes();
			Log.d(DEBUG_TAG, "SUPPORTED FORMATS:");
			for (int i = 0; i < formats.size(); i++) {
				Log.d(DEBUG_TAG, "format: " + formats.get(i));
			}


			
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
			mCamera.setPreviewCallback(this);
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
            mCamera.setPreviewCallback(this);
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
