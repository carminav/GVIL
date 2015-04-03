package com.example.dgif;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
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

import com.example.dgif.customviews.CameraPreview;
import com.example.dgif.customviews.GyroImageView;
import com.example.dgif.sensorlisteners.Gyro.GifGyroscopeSensor;
import com.example.dgif.utils.Constants;
import com.example.dgif.utils.MemoryManager;
import com.example.dgif.utils.RenderUtils;



public class Preview3DObject extends Activity {

    private static final String DEBUG_TAG = "TEST GIF VIEW";

	GyroImageView mView;


    private static final String STEPS = "STEPS";



    SeekBar mSpeedBar;
	TextView mSpeedView;

    private Button mBtnGyroscope;

    private Button mGalleryButton;
    private Button mCameraButton;
	
	SeekBar mBlendBar;
	TextView mBlendView;


    private String[] mFilenames;


    private MemoryManager m;

    private ProgressBar mProgressBar;


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

        String filename = (String) getIntent().getExtras().get(Constants.SERIALIZABLE_GIF);
        SerializableGif rawGif = m.readSerializableGif(filename);
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
        mView = (GyroImageView) findViewById(R.id.testGifView);
        mGalleryButton = (Button) findViewById(R.id.prev_to_gallery_button);
        mCameraButton = (Button) findViewById(R.id.prev_to_cam_button);

        Typeface fontFamily = Typeface.createFromAsset(getAssets(), Constants.GALLERY_ICON);
        mGalleryButton.setTypeface(fontFamily);
        mCameraButton.setTypeface(fontFamily);

    }

    public Loaded3DObject getLoaded3DObject() {
        return loaded3DObject;
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


        mGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Preview3DObject.this, ImageGallery.class);
                startActivity(i);
                overridePendingTransition (android.R.anim.slide_in_left, android.R.anim.slide_out_right);

                finish();
            }
        });

        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Preview3DObject.this, CameraPreview.class);
                startActivity(i);
                overridePendingTransition (android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                finish();
            }
        });



        mBtnGyroscope.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (loaded3DObject != null) {
                    if (mGyroMode) {
                        loaded3DObject.play(true);
                        mGyroMode = false;
                        mBtnGyroscope.setText("Start Compass");
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
