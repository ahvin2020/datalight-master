package com.example.datalight.sender;

import java.io.IOException;

import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.os.Handler;
import android.util.Log;

import com.example.datalight.common.CameraManager;
import com.example.datalight.common.Config;
import com.google.zxing.LuminanceSource;

@SuppressWarnings("deprecation")
public class FlashReceiver {
	
	// a flash is defined as: low lumi value, a high lumi value, then back to low lumi value
	private static final String TAG = FlashReceiver.class.getSimpleName();
	
	private SenderActivityManager senderActivityManager;
	private CameraManager cameraManager;
	
	// receiving variables
	private boolean processNextPreview;		// we want to process the next preview, to see if there's a flash
	private long oldLumiTime[];				// the last lumi SystemMillisecond
	private boolean[] possibleFlashes;		// keep track whether the pixels possible has possible flash? 
	private int flashCount;					// how many times it flashed? (will be resetted after a few seconds)
	private long previousFlashTime;			// when was the last time a flash was detected?
	
	// runnable variables
	private Handler handler;			// handler for repeating task (to handle the runnable)
	private Runnable runnable;			// runs a task
	
	private int highestLumi = 0;
	public FlashReceiver(SenderActivityManager senderActivityManager) {
		this.senderActivityManager = senderActivityManager;
		cameraManager = new CameraManager(senderActivityManager.getSenderActivity(), CameraInfo.CAMERA_FACING_FRONT); // init camera manager
	}
	
	// --- public methods ----
	public void startReceiving() {
		if (cameraManager.isOpen()) {
			Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
			return;
		}
		
		try {
			// start the camera
			cameraManager.openDriver(null);

			// get number of pixels
			Point cameraResolution = cameraManager.getCameraResolution();
			int size = cameraResolution.x * cameraResolution.y;
			
			// the array haven't been initialied yet?
			if (oldLumiTime == null) {
				oldLumiTime = new long[size];
				possibleFlashes = new boolean[size];
			}
			
			// reset all the values
			for (int i=0; i<size; i++) {
				oldLumiTime[i] = 0;
				possibleFlashes[i] = false;
			}
			processNextPreview = false;
			flashCount = 0;
			previousFlashTime = 0;
			
			// start the previewCallback
			cameraManager.startPreview(previewCallback);
			
//			senderActivityManager.getSenderActivity().showDebugText("waiting for signal");
			
			// this function start a runnable, to toggle on processNextPreview every few millisecond,
			// so that previewCallback no need to process the preview data every time
			// can this be improved?
			startReceivingLoop();	
		} catch (IOException e) {
			Log.w(TAG, e);
		}
	}
	
	// stop camera
	public void stopReceiving() {
		cameraManager.stopPreview();
		cameraManager.closeDriver();

		stopRepeatingTask();

//		oldLuminances = null;
//		latestLuminances = null;
//		possibleFlash = null;
	}
	                                                          
	
	// --- private methods ---
	private Camera.PreviewCallback previewCallback = new PreviewCallback() {
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			if (processNextPreview == true) {
				
				int flashedStateCount = 0; // count the number of pixels which have high lumi, we will only assume is a flash if it exceeds flash threshold
				long currentTime = System.currentTimeMillis();
				
				for (int i=0; i<oldLumiTime.length; i++) {
					// get the lumi value
					int luminance = data[i] & 0xFF;
					
					if (i == 0) {
						if (luminance > highestLumi) {
							highestLumi = luminance;
						}
						
						//senderActivityManager.getSenderActivity().showDebugText(highestLumi + " " + luminance);
					}
					// now is flash lumi
					if (luminance >= Config.FLASH_LUMI) {
						// pixel currently in not yet flashed?
						if (possibleFlashes[i] == false) {
							// upgrade to possible flash state and save current time
							possibleFlashes[i] = true;
							oldLumiTime[i] = currentTime;
							
							if (i == 0) {
							//	senderActivityManager.getSenderActivity().showDebugText("upgraded");
							}
						}
					} 
					// not flash lumi
					else {
						// pixel currently in possible flash state?
						if (possibleFlashes[i] == true) {
							// within flash time range?
							if ((currentTime - oldLumiTime[i]) < Config.FLASH_TIME_RANGE) {
								// flashed!
								flashedStateCount++;
								
								if (i == 0) {
								//	senderActivityManager.getSenderActivity().showDebugText(luminance + " counted and resetted");
								}
							}
							
							// reset state
							possibleFlashes[i] = false;
						}
					}
				}
				
				// a flash has occured?
				if (flashedStateCount > Config.FLASHED_STATE_COUNT_THRESHOLD) {
					flashCount++;
					previousFlashTime = System.currentTimeMillis();
					senderActivityManager.getSenderActivity().showDebugText(flashCount + " " + flashedStateCount);
					
					// let's the senderActivityManager know a flash has just occured
					senderActivityManager.informFlashCount(flashCount);
				} else if (flashCount > 0) {
					// check if it is time to reset the flash
					long flashRange = System.currentTimeMillis() - previousFlashTime;
					if (flashRange > Config.RESET_FLASH_TIME) {
						flashCount = 0;
						senderActivityManager.getSenderActivity().showDebugText(flashCount + " " + flashedStateCount);
					}
				}
				
				processNextPreview = false;
			}
		}
	};
	
	// this function start a runable, to toggle on processNextPreview every few millisecond,
	// so that previewCallback no need to process the preview data every time
	// can this be improved?
	private void startReceivingLoop() {
		// create the runnable
		handler = new Handler();
		runnable = new Runnable() {
			@Override
			public void run() {
				// lets previewCallback to know to process the preview data next time it runs
				processNextPreview = true;
				
				// run next one
				handler.postDelayed(runnable, Config.RECEIVE_FLASH_INTERVAL);
			}
		};
		
		// start the runnable
		runnable.run();
	}
	
	private void stopRepeatingTask() {
		if (handler != null) {
			handler.removeCallbacks(runnable);
		}
	}
}
