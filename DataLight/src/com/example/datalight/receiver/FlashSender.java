package com.example.datalight.receiver;

import android.os.Handler;

import com.example.datalight.common.Config;
import com.example.datalight.sender.FlashReceiver;

public class FlashSender {
	private static final String TAG = FlashReceiver.class.getSimpleName();
	
	private ReceiverActivityManager receiverActivityManager;
	
	// runnable variables
	private Handler handler;			// handler for repeating task (to handle the runnable)
	private Runnable runnable;			// runs a task
	
	// flash variables
	private boolean isFlashlightOn;
	private int currentFlashCount;
	
	public FlashSender(ReceiverActivityManager receiverActivityManager) {
		this.receiverActivityManager = receiverActivityManager;
		
		isFlashlightOn = false;
	}
	
	// --- public methods ---
	public void startFlashing(final int totalFlashCount) {
		// stop any current flashing, turn off any on flash
		stopFlashing();
		isFlashlightOn = false;
		receiverActivityManager.getCameraManager().turnOnFlash(isFlashlightOn);
		
		currentFlashCount = 0;
		
		// create the runnable
		handler = new Handler();
		runnable = new Runnable() {
			@Override
			public void run() {
				isFlashlightOn = !isFlashlightOn;
				
				// toggle the flashlight
				receiverActivityManager.getCameraManager().turnOnFlash(isFlashlightOn);
				
				// increment count if flash light is off
				if (isFlashlightOn == false) {
					currentFlashCount++;
				}
				
				// finish flashing?
				if (currentFlashCount >= totalFlashCount) {
					stopRepeatingTask();
//					receiverActivityManager.informFinishFlashing(totalFlashCount);
				} else {
					if (isFlashlightOn) {
						handler.postDelayed(runnable, Config.FLASH_ON_INTERVAL);
					} else {
						handler.postDelayed(runnable, Config.FLASH_OFF_INTERVAL);
					}
				}
			}
		};
		
		// start the runnable
		runnable.run();
	}
	
	public void stopFlashing() {
		stopRepeatingTask();
	}
	
	// --- private methods ---
	private void stopRepeatingTask() {
		if (handler != null) {
			handler.removeCallbacks(runnable);
		}
	}
}
