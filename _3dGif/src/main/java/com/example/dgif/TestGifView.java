package com.example.dgif;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.GridLayout.LayoutParams;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;



public class TestGifView extends Activity {

    private static final String DEBUG_TAG = "TEST GIF VIEW";

	ImageView mView;
	AnimationDrawable gif = null;
	Bitmap[] images;
	boolean[] mPicsSelected;
	ArrayList<BitmapDrawable> mBitmaps;

	public final static int PIC_WIDTH = 480; //1458;
	public final static int PIC_HEIGHT = 640; //2592;	
	
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
	
	//TODO: fix out of memory error
	private static final int DEFAULT_DURATION = 50;
	private static final int DEFAULT_NUM_BLENDS = 1;
	
	int mDuration; 
	int mNumBlends;

    boolean mGyroMode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_gif_view);
		
		m = new MemoryManager(this);

        mGyroMode = false;
		
		mSpeedBar = (SeekBar) findViewById(R.id.speedBar);
		mSpeedView = (TextView) findViewById(R.id.speedView);
		
		mBlendBar = (SeekBar) findViewById(R.id.blendBar);
		mBlendView = (TextView) findViewById(R.id.blendView);
		
		mDuration = mSpeedBar.getProgress();
		mNumBlends = mBlendBar.getProgress();

        mBtnGyroscope = (Button) findViewById(R.id.btn_start_gyro);

        yLabel = (TextView) findViewById(R.id.y_label);

		mFilenames = fileList();
		
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mBtnGyroscope.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mGyro != null) {
                    if (mGyroMode) {
                        mGyro.stop();
                        mGyroMode = false;
                        mBtnGyroscope.setText("Start Gyroscope");
                        yLabel.setText("Y:  ");
                    } else {
                        mGyro.start(gif);
                        mGyroMode = true;
                        mBtnGyroscope.setText("Stop Gyroscope");
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
				
		

		
		mView = (ImageView) findViewById(R.id.testGifView);


        //This returns a boolean array if previous activity was ImageGallery and null otherwise
		mPicsSelected = getIntent().getExtras().getBooleanArray("picsSelected");


        //This returns an integer if previous activity was CameraPreview and -1 otherwise
		int lastIndices = getIntent().getExtras().getInt("lastIndices", -1);


		mBitmaps = getSelectedImages(lastIndices);


        if (mProgressBar.getProgress() == 0) {
            setDrawable(false);
        } else setDrawable(true);

        mGyro = new GyroGif(this, mView, yLabel);
		
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
	
	//TODO: Run in a separate thread and/or async task
	private AnimationDrawable createGif(boolean createNewBlends) {
		int count = mBitmaps.size();
		
		/* Create new blends if necessary */
		if (createNewBlends) {
			
			int totalNumBlends = (count - 1) * mNumBlends;
			mBlends =  new BitmapDrawable[totalNumBlends];

			for (int i = 0; i < (count - 1); i++) {
				Bitmap a = mBitmaps.get(i).getBitmap();
				Bitmap b = mBitmaps.get(i + 1).getBitmap();
				for (int j = 0; j < mNumBlends; j++) {
					double weight = ((1 / (double)(mNumBlends + 1))) * (j + 1);
					Bitmap bm = getIntermediateImage(a,b, weight);
					mBlends[i + j] = new BitmapDrawable(getResources(), bm);
				}
			}
		}
		
		
		
		AnimationDrawable anim = new AnimationDrawable();
		
		
		
		//forward
		for (int i = 0; i < count; i++) {
			anim.addFrame(mBitmaps.get(i), mDuration);
			if (i < count - 1) {
				for (int j = 0; j < mNumBlends; j++) {
					anim.addFrame(mBlends[i + j], mDuration);
				}
			}
		}
		
		BitmapDrawable[] x = new BitmapDrawable[anim.getNumberOfFrames()];
		for (int i = 0; i < x.length; i++) {
			x[i] = (BitmapDrawable) anim.getFrame(i);
		}
		
		for (int i = x.length - 1;  i >= 0; i--) {
			anim.addFrame(x[i], mDuration); 
		}
		
//		//TODO: Fix bug here. It crashes for certain numbers of blends
//		//reverse
//		for (int i = count - 1; i >= 0; i--) {
//			anim.addFrame(mBitmaps.get(i), mDuration);
//			Log.d("TEST VIEW GIF", "i: " + i);
//			if (i > 0) {
////				for (int j = mNumBlends - 1; j >= 0; j--) {
////					anim.addFrame(mBlends[mBlends.length - ((i - 1) * (mNumBlends - j))], mDuration);
////				}
//				for (int j = 0; j < mNumBlends; j++) {
//					anim.addFrame(mBlends[mBlends.length - j - 1], mDuration);
//				}
//			}
//			
//		}
		
	
		return anim;

	} 


    private Bitmap scaledBitmap(Bitmap bm) {
        Log.i(DEBUG_TAG, "bm size: " + bm.getWidth() + " + " + bm.getHeight());
        return Bitmap.createScaledBitmap(bm, bm.getWidth(), 900, true);

    }


	/* GET INTERMEDIATE IMAGE 
	 * Uses linear interpolation to get the intermediate blend of pics a and b
	 * based on a weight.
	 */
	private static Bitmap getIntermediateImage(Bitmap a, Bitmap b, double weight) {
		Log.d("TEST GIF VIEW", "Intermediate Image Reached");
		int height = a.getHeight();
		int width = a.getWidth();
		Bitmap blend = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		
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
}
