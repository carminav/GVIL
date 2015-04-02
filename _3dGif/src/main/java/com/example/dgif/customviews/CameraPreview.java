package com.example.dgif.customviews;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.dgif.ImageGallery;
import com.example.dgif.SerializableGif;
import com.example.dgif.sensorlisteners.Gyro.CameraPreviewGyroscopeSensor;
import com.example.dgif.utils.Constants;
import com.example.dgif.utils.MemoryManager;
import com.example.dgif.R;
import com.example.dgif.Preview3DObject;
import com.example.dgif.sensorlisteners.AutoFocusSensorListener;
import com.example.dgif.utils.RenderUtils;


public class CameraPreview extends Activity {
	
	
	private static final String DEBUG_TAG = "Preview";

    protected static final int LOAD_CAM_PREV = 0;

    // Hardcoded values for Samsung Galaxy Nexus
	public final static int VIEW_WIDTH = 720;
	public final static int VIEW_HEIGHT = 1280;
	public final static int PIC_WIDTH =  1024;
	public final static int PIC_HEIGHT =   1280;
	
    byte[] mSurfaceBytes;

	private Camera mCamera;
	private CamView mPreview;

	private UIHandler mHandler;
	private FrameLayout mPreviewFrame;

	private int[] mPixels;

    int mCount = 0;
	
	private MemoryManager mMemoryManager;
	
	private boolean onPauseCalled;
    private boolean mIsPreviewing;

    // Views
    private TextView mRollLabel;
	private ImageView mOverlayView;

    // Buttons
    Button mCaptureButton;
    Button mTrashButton;
    Button mPreviewButton;
    private Button mGalleryButton;

    // Sensors
    private AutoFocusSensorListener mAutoFocusCallback;
    private Sensor mAccelerometer;
    private SensorManager mSensorManager;

    CameraPreviewGyroscopeSensor mCompass;

    private ArrayList<byte[]> mRawFrames = new ArrayList<byte[]>();


	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.camera_preview);

        initViewObjects();

        // Use Font-Awesome glyph for gallery button
        Typeface fontFamily = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont.ttf");
	    mGalleryButton.setTypeface(fontFamily);

        mCompass = new CameraPreviewGyroscopeSensor(this, mRollLabel);
        mHandler = new UIHandler();

		mMemoryManager = new MemoryManager(this);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		mIsPreviewing = false;
        onPauseCalled = false;

        addOnClickListeners();

		// Begin loading camera resource
		new Thread(new LoadCameraAndPrev()).start();
	}


    // Find Views by ID all in one method
    private void initViewObjects() {

        mCaptureButton = (Button) findViewById(R.id.capture_button);
        mPreviewButton = (Button) findViewById(R.id.preview_button);
        mPreviewFrame = (FrameLayout) findViewById(R.id.camera_preview);
        mGalleryButton = (Button) findViewById(R.id.gallery_button);
        mTrashButton = (Button) findViewById(R.id.trash_button);
        mRollLabel = (TextView) findViewById(R.id.rollLabel);
        mOverlayView = (ImageView) findViewById(R.id.image_overlay_view);

    }

    // Add functionality to buttons in view
    private void addOnClickListeners() {

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
                mRawFrames.clear();
                if (mOverlayView.getVisibility() == View.VISIBLE)
                    mOverlayView.setVisibility(View.GONE);
                mTrashButton.setVisibility(View.GONE);
                mPreviewButton.setVisibility(View.GONE);

            }

        });

        mPreviewButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                SerializableGif gif = new SerializableGif(convertToArray(mRawFrames));
                String filename = mMemoryManager.saveRaw3DObject(gif);
                mRawFrames.clear();
                Intent i = new Intent(CameraPreview.this, Preview3DObject.class);
                i.putExtra(Constants.SERIALIZABLE_GIF, filename);
                startActivity(i);
            }

        });

    }



    public byte[][] convertToArray(ArrayList<byte[]> list) {
        Log.d("CONVERT TO ARRAY", "enter method");
        byte[][] a = new byte[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            Log.d("CONVERT TO ARRAY", "process byte array " + i);
            a[i] = list.get(i);
        }


        return a;
    }



    // Make sure camera and other resources restart/start
	@Override
	protected void onResume() {
		super.onResume();
	    mCompass.start();
        if (onPauseCalled) {
        	new Thread(new LoadCameraAndPrev()).start();
        }
	}
	

    // Make sure camera and other resources are released
	@Override
	protected void onPause() {
		super.onPause();
		mCompass.stop();
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



	// Picture Callback Object to handle events when pictures are taken
	Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

        private Bitmap constructBitmap() {
            Size prevSize = mCamera.getParameters().getPreviewSize();
            Bitmap bm = Bitmap.createBitmap(prevSize.width, prevSize.height, Bitmap.Config.ARGB_8888);
            bm.copyPixelsFromBuffer(IntBuffer.wrap(mPixels));
            return bm;
        }


		@Override
		public void onPictureTaken(byte[] data, Camera camera) {


          //  Log.d("ORIENTATION", "Roll: " + mCompass.getRoll());
			mIsPreviewing = false;
          //  mMemoryManager.saveImage(data);

            mRawFrames.add(data);


            //set overlay
            Bitmap b = constructBitmap();
            Matrix matrix = new Matrix();
            matrix.postRotate(90);

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(b,VIEW_HEIGHT,VIEW_WIDTH,true);
            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
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
			} catch (RuntimeException e) {
				Log.e(DEBUG_TAG, "Camera will not open. onCreate");
				finish();
			}

            mAutoFocusCallback = new AutoFocusSensorListener(mCamera);
			
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
            RenderUtils.decodeYUV420SP(pixels, bytes, previewSize.width, previewSize.height);
            mPixels = pixels;
        }

    }
	
}
