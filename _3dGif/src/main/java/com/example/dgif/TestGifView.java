package com.example.dgif;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
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
import com.example.dgif.sensorlisteners.Compass;
import com.example.dgif.utils.MemoryManager;
import com.example.dgif.utils.RenderUtils;


public class TestGifView extends Activity {

    private static final String DEBUG_TAG = "TEST GIF VIEW";

    Compass mCompass;

	GyroImageView mView;
	AnimationDrawable gif = null;

	boolean[] mPicsSelected;
	ArrayList<BitmapDrawable> mBitmaps;

	
	SeekBar mSpeedBar;
	TextView mSpeedView;

    private Button mBtnGyroscope;
	
	SeekBar mBlendBar;
	TextView mBlendView;

    GyroGif mGyro;
	
	BitmapDrawable[] mBlends;
    String[] mFilenames;

    TextView yLabel;

    MemoryManager m;

    ProgressBar mProgressBar;

	
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

        //This returns a boolean array if previous activity was ImageGallery and null otherwise
		mPicsSelected = getIntent().getExtras().getBooleanArray("picsSelected");

        //This returns an integer if previous activity was CameraPreview and -1 otherwise
		int lastIndices = getIntent().getExtras().getInt("lastIndices", -1);

		mBitmaps = getSelectedImages(lastIndices);

        if (mProgressBar.getProgress() == 0) {
            setDrawable(false);
        } else setDrawable(true);

        mGyro = new GyroGif(this, mView, yLabel);

        mCompass = new Compass(this, mView, gif, yLabel);
		
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


	//TODO: Instead of scanning all of mPicsSelected, just use array of indexes which
	//are selected
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

			int first = mFilenames.length - lastIndices;
			
			for (int i = first; i < mFilenames.length; i++) {

                //get name of file for easy lookup
                String name = mFilenames[i];

                //Load image from memory based on filename
                Bitmap bm = m.loadScaledBitmapFromFIS(name, 720, 1280);

				BitmapDrawable b = new BitmapDrawable(getResources(), scaledBitmap(bm));
				list.add(b);
			}
			
		}
		
		return list;
	}
	

	private AnimationDrawable createGif(boolean createNewBlends) {
		int count = mBitmaps.size();
        String tag = "CREATE GIF";
		Log.d(tag, "Begin");
		/* Create new blends if necessary */
		if (createNewBlends) {
			
			int totalNumBlends = (count - 1) * mNumBlends;
			mBlends =  new BitmapDrawable[totalNumBlends];
			for (int i = 0; i < (count - 1); i++) {
				Bitmap a = mBitmaps.get(i).getBitmap();
				Bitmap b = mBitmaps.get(i + 1).getBitmap();
				for (int j = 0; j < mNumBlends; j++) {
					double weight = ((1 / (double)(mNumBlends + 1))) * (j + 1);
					Bitmap bm = RenderUtils.getIntermediateImage(a,b, weight);
					mBlends[(i * mNumBlends) + j] = new BitmapDrawable(getResources(), bm);
				}
			}
		}

		
		AnimationDrawable anim = new AnimationDrawable();
		

		//forward
		for (int i = 0; i < count; i++) {
			anim.addFrame(mBitmaps.get(i), mDuration);
            Log.d(tag, "Add orig frame:" + i);
			if (i < count - 1) {
				for (int j = 0; j < mNumBlends; j++) {
                    Log.d(tag, "Add inter frame:" + ((i * mNumBlends) + j));
					anim.addFrame(mBlends[(i * mNumBlends) + j], mDuration);
				}
			}
		}

        Log.d(tag, "forward frames: " + anim.getNumberOfFrames());
        //TODO: Change this and play around with keeping edge frames

        //reverse
		for (int i = anim.getNumberOfFrames() - 1;  i >= 0; i--) {
			anim.addFrame(anim.getFrame(i), mDuration);
		}
	
		return anim;

	} 


    private Bitmap scaledBitmap(Bitmap bm) {
        Log.i(DEBUG_TAG, "bm size: " + bm.getWidth() + " + " + bm.getHeight());
        return Bitmap.createScaledBitmap(bm, bm.getWidth(), 900, true);

    }

	private void setDrawable (boolean createNewBlend) {
		
		if (gif != null && gif.isRunning()) {
			gif.stop();
			mView.setBackground(null);
		}

        if (createNewBlend) {
            new SetDrawableTask().execute(mBitmaps);
        } else {
            gif = createGif(createNewBlend);
            gif.setOneShot(false);

            mView.setBackground(gif);

            gif.start();
        }
	}


	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (gif.isRunning()) {
			gif.stop();
		}

        if (mGyro.isRunning()) {
            mGyro.stop();
        }
	}

    class SetDrawableTask extends AsyncTask<ArrayList<BitmapDrawable>, Void, AnimationDrawable> {
        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.bringToFront();
        }

        @Override
        protected AnimationDrawable doInBackground(ArrayList<BitmapDrawable>... bitmaps) {
            return createGif(true);
        }

        @Override
        protected void onPostExecute(AnimationDrawable animationDrawable) {
            mProgressBar.setVisibility(View.GONE);
            gif = animationDrawable;
            gif.setOneShot(false);
            mView.setBackground(gif);
            gif.start();
        }
    }


    /* */
    class DrawIntermediateFrameTask extends AsyncTask<Object, Void, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Bitmap doInBackground(Object... params) {
            Double weight = (Double) params[2];
            return RenderUtils.getIntermediateImage((Bitmap) params[0], (Bitmap) params[1], weight);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);


        }
    }

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (!gif.isRunning()) {
			gif.start();
		}
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test_gif_view, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    private void setListeners() {

        mBtnGyroscope.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (mGyro != null) {
//                    if (mGyroMode) {
//                        mGyro.stop();
//                        mGyroMode = false;
//                        mBtnGyroscope.setText("Start Gyroscope");
//                        yLabel.setText("Y:  ");
//                    } else {
//                        mGyro.start(gif);
//                        mGyroMode = true;
//                        mBtnGyroscope.setText("Stop Gyroscope");
//                    }
//                }


                if (mCompass != null) {
                    if (mGyroMode) {
                        mCompass.stop();
                        mGyroMode = false;
                        mBtnGyroscope.setText("Start Compass");
                        yLabel.setText("Y:  ");
                    } else {
                        // mGyro.start(gif);
                        mCompass.start();
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
                setDrawable(false);

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
                setDrawable(true);
            }


        });

    }



}
