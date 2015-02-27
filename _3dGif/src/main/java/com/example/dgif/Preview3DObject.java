package com.example.dgif;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.example.dgif.customviews.GyroImageView;
import com.example.dgif.sensorlisteners.Gyro.GifGyroscopeSensor;
import com.example.dgif.utils.MemoryManager;
import com.example.dgif.utils.RenderUtils;


public class Preview3DObject extends Activity {

    private static final String DEBUG_TAG = "TEST GIF VIEW";

	GyroImageView mView;
	AnimationDrawable gif = null;

	boolean[] mPicsSelected;
	ArrayList<BitmapDrawable> mBitmaps;

    private static final String STEPS = "STEPS";

    SeekBar mSpeedBar;
	TextView mSpeedView;

    private Button mBtnGyroscope;
	
	SeekBar mBlendBar;
	TextView mBlendView;


    private String[] mFilenames;

    private TextView yLabel;

    private MemoryManager m;

    private ProgressBar mProgressBar;

    private float[] mOrientations;


    private Loaded3DObject loaded3DObject = null;
	
	int mDuration; 
	int mNumBlends;

    boolean mGyroMode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_gif_view);

		m = new MemoryManager(this);

        mGyroMode = false;
        ;
		initViewObjects();
		
		mDuration = mSpeedBar.getProgress();
		mNumBlends = mBlendBar.getProgress();

		mFilenames = fileList();

		setListeners();

        //This returns a boolean array if previous activity was ImageGallery and null otherwise
		mPicsSelected = getIntent().getExtras().getBooleanArray("picsSelected");

        //This returns an integer if previous activity was CameraPreview and -1 otherwise
		int lastIndices = getIntent().getExtras().getInt("lastIndices", -1);

		mBitmaps = getSelectedImages(lastIndices);

	}

    private void initViewObjects() {

        mSpeedBar = (SeekBar) findViewById(R.id.speedBar);
        mSpeedView = (TextView) findViewById(R.id.speedView);
        mBlendBar = (SeekBar) findViewById(R.id.blendBar);
        mBlendView = (TextView) findViewById(R.id.blendView);
        mBtnGyroscope = (Button) findViewById(R.id.btn_start_gyro);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        yLabel = (TextView) findViewById(R.id.y_label);
        mView = (GyroImageView) findViewById(R.id.testGifView);

    }



	private ArrayList<BitmapDrawable> getSelectedImages(int lastIndices) {
		ArrayList<BitmapDrawable> list = new ArrayList<BitmapDrawable>();

        //Previous activity: ImageGallery
		if (lastIndices == -1) {

			for (int i = 0; i < mPicsSelected.length; i++) {
				if (mPicsSelected[i]) {

                    //get name of file for easy lookup
                    String name = mFilenames[i];

                    //Load image from memory based on filename
                    Bitmap bm = m.loadScaledBitmapFromFIS(name, 720, 1280);

					BitmapDrawable b = new BitmapDrawable(getResources(), scaledBitmap(bm));
					list.add(b);
				}
			}
		} else {
		//Previous activity: CameraPreview
            Log.d(STEPS, " set mOrientations");
            mOrientations = getIntent().getExtras().getFloatArray("orientations");


			int first = mFilenames.length - lastIndices;
			
			for (int i = first; i < mFilenames.length; i++) {

                //get name of file for easy lookup
                String name = mFilenames[i];

                //Load image from memory based on filename
                Bitmap bm = m.loadScaledBitmapFromFIS(name, 720, 1280);

				BitmapDrawable b = new BitmapDrawable(getResources(), scaledBitmap(bm));
				list.add(b);
			}


            SerializableGif rawGif = new SerializableGif(list, mOrientations);

            loaded3DObject = new Loaded3DObject(this, rawGif, mView);
            loaded3DObject.play(true);

		}
		
		return list;
	}

    public Loaded3DObject getLoaded3DObject() {
        return loaded3DObject;
    }



    private Bitmap scaledBitmap(Bitmap bm) {
        Log.i(DEBUG_TAG, "bm size: " + bm.getWidth() + " + " + bm.getHeight());
        return Bitmap.createScaledBitmap(bm, bm.getWidth(), 900, true);

    }


	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
        loaded3DObject.pause();

	}

//    class SetDrawableTask extends AsyncTask<ArrayList<BitmapDrawable>, Void, AnimationDrawable> {
//        @Override
//        protected void onPreExecute() {
//            mProgressBar.setVisibility(View.VISIBLE);
//            mProgressBar.bringToFront();
//        }
//
//        @Override
//        protected AnimationDrawable doInBackground(ArrayList<BitmapDrawable>... bitmaps) {
//            return createGif(true);
//        }
//
//        @Override
//        protected void onPostExecute(AnimationDrawable animationDrawable) {
//            mProgressBar.setVisibility(View.GONE);
//            gif = animationDrawable;
//            gif.setOneShot(false);
//            mView.setBackground(gif);
//            gif.start();
//        }
//    }




	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
        if (loaded3DObject != null) {
            loaded3DObject.resume();
        }
	}



    private void setListeners() {

        mBtnGyroscope.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (loaded3DObject != null) {
                    if (mGyroMode) {
                        loaded3DObject.play(true);
                        mGyroMode = false;
                        mBtnGyroscope.setText("Start Compass");
                        yLabel.setText("Y:  ");
                    } else {
                        loaded3DObject.play(false);
                        mGyroMode = true;
                        mBtnGyroscope.setText("Stop Compass");
                    }
                }

            }
        });

        mSpeedBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                mSpeedView.setText(""+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mDuration = seekBar.getProgress();
                //setDrawable(false);
                if (loaded3DObject != null) loaded3DObject.changeFrameRate(mDuration);
            }
        });

        mBlendBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                mBlendView.setText(""+progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //do nothing

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mNumBlends = seekBar.getProgress();
              //  setDrawable(true);
               if (loaded3DObject != null) loaded3DObject.changeBlendCount(mNumBlends);
            }


        });

    }



}
