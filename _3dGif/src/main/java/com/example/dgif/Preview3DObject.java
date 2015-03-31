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
import com.example.dgif.utils.Constants;
import com.example.dgif.utils.MemoryManager;
import com.example.dgif.utils.RenderUtils;



public class Preview3DObject extends Activity {

    private static final String DEBUG_TAG = "TEST GIF VIEW";

	GyroImageView mView;
	AnimationDrawable gif = null;

	boolean[] mPicsSelected;
	ArrayList<BitmapDrawable> mBitmaps;

    private static final String STEPS = "STEPS";

    private int mOrigin;

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

		initViewObjects();
		
		mDuration = mSpeedBar.getProgress();
		mNumBlends = mBlendBar.getProgress();

		mFilenames = fileList();

		setListeners();

        // Get activity that lead of to Preview3DObject (this)
        int origin = getIntent().getExtras().getInt(Constants.ORIGIN);
        mBitmaps = getSelectedImages(origin);
        SerializableGif rawGif = new SerializableGif(mBitmaps);
        loaded3DObject = new Loaded3DObject(this, rawGif, mView);
        loaded3DObject.play(true);

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


    private BitmapDrawable loadBitmapDrawable(String filename) {
        Bitmap bm = m.loadScaledBitmapFromFIS(filename, 720, 1280);
        return new BitmapDrawable(getResources(), scaledBitmap(bm));
    }

    private ArrayList<BitmapDrawable> getSelectedImages(int origin) {
        ArrayList<BitmapDrawable> images = new ArrayList<BitmapDrawable>();
        if (origin == Constants.FROM_CAMERA_PREVIEW) {
            int lastIndices = getIntent().getIntExtra(Constants.LAST_PICS_SELECTED, -1);
            int first = mFilenames.length - lastIndices;

            for (int i = first; i < mFilenames.length; i++) {
                images.add(loadBitmapDrawable(mFilenames[i]));
            }

        } else {
            boolean[] selectedPics = getIntent().getBooleanArrayExtra(Constants.SELECTED_PICS_ARRAY);

            for (int i = 0; i < selectedPics.length; i++) {
                if (selectedPics[i]) {
                    images.add(loadBitmapDrawable(mFilenames[i]));
                }
            }
        }
        return images;
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
