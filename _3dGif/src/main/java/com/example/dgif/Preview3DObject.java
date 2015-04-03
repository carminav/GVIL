package com.example.dgif;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.widget.NumberPicker;
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

import org.w3c.dom.Text;


public class Preview3DObject extends Activity {

    private static final String DEBUG_TAG = "TEST GIF VIEW";

	GyroImageView mView;


    private static final String STEPS = "STEPS";

    private SeekBar mBlendsBar;


    private TextView blend0Text;
    private TextView blend1Text;
    private TextView blend2Text;
    private TextView blend3Text;


    private ProgressBar loadingCircle;
    private Button mGalleryButton;
    private Button mCameraButton;
	


    private String[] mFilenames;


    private MemoryManager m;

    private static int MAX_BLENDS = 3;

    private ColorStateList defaultGrey;

    private Loaded3DObject loaded3DObject = null;
	
	int mDuration; 
	int mNumBlends;

    private TextView currentBlendText;

    boolean mGyroMode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_gif_view);

		m = new MemoryManager(this);

        mGyroMode = false;


		initViewObjects();



		/* COME BACK HERE */
	//	mDuration = mSpeedBar.getProgress();
		mNumBlends = mBlendsBar.getProgress();

		mFilenames = fileList();

		setListeners();

        String filename = (String) getIntent().getExtras().get(Constants.SERIALIZABLE_GIF);
        SerializableGif rawGif = m.readSerializableGif(filename);
        loaded3DObject = new Loaded3DObject(this, rawGif, mView);
        loaded3DObject.play(false);

	}

    private void initViewObjects() {


        mView = (GyroImageView) findViewById(R.id.testGifView);
        mGalleryButton = (Button) findViewById(R.id.prev_to_gallery_button);
        mCameraButton = (Button) findViewById(R.id.prev_to_cam_button);

        Typeface fontFamily = Typeface.createFromAsset(getAssets(), Constants.GALLERY_ICON);
        mGalleryButton.setTypeface(fontFamily);
        mCameraButton.setTypeface(fontFamily);

        mBlendsBar = (SeekBar) findViewById(R.id.blend_seekbar);
        blend0Text = (TextView) findViewById(R.id.blend_0_textview);
        blend1Text = (TextView) findViewById(R.id.blend_1_textview);
        blend2Text = (TextView) findViewById(R.id.blend_2_textview);
        blend3Text = (TextView) findViewById(R.id.blend_3_textview);
        currentBlendText = blend0Text;
        defaultGrey = blend0Text.getTextColors();
        blend0Text.setTextColor(Color.parseColor(Constants.RED));

        loadingCircle = (ProgressBar) findViewById(R.id.progressBar);


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

    private TextView getBlendTextView(int i) {
        switch (i) {
            case 0: return blend0Text;
            case 1: return blend1Text;
            case 2: return blend2Text;
            default: return blend3Text;
        }
    }


    private void setListeners() {


        mBlendsBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                currentBlendText.setTextColor(defaultGrey);
                currentBlendText = getBlendTextView(i);
                currentBlendText.setTextColor(Color.parseColor(Constants.RED));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mNumBlends = seekBar.getProgress();
                new LoadBlends().execute(loaded3DObject, mNumBlends);
            }
        });

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

    }

    public class LoadBlends extends AsyncTask<Object, Void, Void> {

        Loaded3DObject loaded3DObject;
        int blendCount;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingCircle.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Object... objects) {
            loaded3DObject = (Loaded3DObject) objects[0];
            blendCount = (Integer) objects[1];
            loaded3DObject.pause();
            loaded3DObject.changeBlendCount(blendCount);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            loadingCircle.setVisibility(View.INVISIBLE);
            loaded3DObject.resume();

        }
    }





}
