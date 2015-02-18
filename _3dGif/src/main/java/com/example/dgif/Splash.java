package com.example.dgif;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.example.dgif.customviews.CameraPreview;

/* @author Carmina Villaflores
 * Splash screen which appears upon starting up 3dGif APP 
 * TODO: Fix actionbar from showing in initial load of screen 
 */
public class Splash extends ActionBarActivity {

	private static int SPLASH_TIME_OUT = 4000;
	private static final String DEBUG_TAG = "Splash";
	private boolean active = true;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_splash);
		
	    // Switch to next activity after SPLASH_TIME_OUT
		Thread splashThread = new Thread() {
			@Override
			public void run() {
				try {
					int waited = 0;
					while (active && (waited < SPLASH_TIME_OUT)) {
						sleep(100);
						if (active) {
							waited += 100;
						}
					}
				} catch (InterruptedException e) {
					//do nothing
				} finally {
					finish();
					startActivity(new Intent(Splash.this, CameraPreview.class));
				}
			}
		};
		splashThread.start();
	
	}

    @Override
    protected void onPause() {
    	super.onPause();
    	overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

    }
    
    
    
    
   

}
