package com.example.datalight.common;
import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.WindowManager;


@SuppressWarnings("deprecation")
public class CameraManager {
	
	private static final String TAG = CameraManager.class.getSimpleName();

	private Camera camera;
	private Rect framingRect;
	private Rect framingRectInPreview;
	private Point screenResolution;
	private Point cameraResolution;
	private boolean isPreviewing;
	private boolean isInitialized;
	private int cameraId;	// back or front facing camera
	
	public CameraManager(Context context, int cameraId) {
		isPreviewing = false;
		isInitialized = false;
		
		this.cameraId = cameraId;
		
		// init screen resolution
		WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	    Display display = manager.getDefaultDisplay();
	    screenResolution = new Point();
	    display.getSize(screenResolution);
		
	    // init framing rect
		initFramingRect();
	}
	
	// holder is where the camera will draw preview frames into
	public void openDriver(SurfaceHolder holder) throws IOException {
		if (camera == null) {
			Log.d(TAG, "CAMERA IS OPENING " + cameraId);
			camera = Camera.open(cameraId);
			
			if (camera == null) {
				throw new IOException();
			}
		}
		
		// show preview if there's a SurfaceHolder
		if (holder != null) {
			camera.setPreviewDisplay(holder);
		}
		
		camera.setDisplayOrientation(90);
		
		// initialize camera stuff
		if (!isInitialized) {
			isInitialized = true;
			
			Camera.Parameters parameters = camera.getParameters();
			
			// set camera resolution
		    cameraResolution = CameraConfigurationUtils.findBestPreviewSizeValue(parameters, screenResolution);
		    parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
		    
		    // set focus mode
		    if (cameraId == CameraInfo.CAMERA_FACING_BACK) {
		    	parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		    }

		    camera.setParameters(parameters);
		}
	}
	
	public boolean isOpen() {
		return camera != null;
	}
	
	public void closeDriver() {
		if (camera != null) {
			Log.d(TAG, "CAMERA IS CLOSING " + cameraId);
			camera.release();
			camera = null;
			
			framingRect = null;
			framingRectInPreview = null;
		}
	}
	
	public void startPreview(PreviewCallback previewCallback) {
		if (camera != null && !isPreviewing) {
			camera.setPreviewCallback(previewCallback);
			camera.startPreview();
			isPreviewing = true;
		}
	}
	
	public void stopPreview() {
		if (camera != null && isPreviewing) {
			camera.setPreviewCallback(null);
			camera.stopPreview();
			isPreviewing = false;
		}
	}
	
	public Point getCameraResolution() {
		return cameraResolution;
	}
	
	/**
	 * Calculates the framing rect which the UI should draw to show the user where to place the
	 * barcode. This target helps with alignment as well as forces the user to hold the device
	 * far enough away to ensure the image will be in focus.
	 *
	 * @return The rectangle to draw on screen in window coordinates.
	 */
	public Rect getFramingRect() {
		return framingRect;
	}
	
	public Rect getFramingRectInPreview() {
		if (framingRectInPreview == null) {
			if (framingRect == null) {
				return null;
			}

			Rect rect = new Rect(framingRect);
			if (cameraResolution == null || screenResolution == null) {
				// Called early, before init even finished
				return null;
			}
			rect.left = rect.left * cameraResolution.y / screenResolution.x;
		    rect.right = rect.right * cameraResolution.y / screenResolution.x;
		    rect.top = rect.top * cameraResolution.x / screenResolution.y;
		    rect.bottom = rect.bottom * cameraResolution.x / screenResolution.y;
			framingRectInPreview = rect;
		}
		
		return framingRectInPreview;
	}
	
	public void turnOnFlash(boolean isTurnOn) {
		// only turn on flash if is back camera
		if (camera != null && cameraId == CameraInfo.CAMERA_FACING_BACK) {
			
			Camera.Parameters parameters = camera.getParameters();
			
			if (isTurnOn) {
				parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
			} else {
				parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
			}
			
			camera.setParameters(parameters);
		}
	}
	
	// --- private functions ---
	
	public void initFramingRect() {
		int width = findDesiredDimensionInRange(screenResolution.x, Config.MIN_FRAME_WIDTH, Config.MAX_FRAME_WIDTH);
		int height = findDesiredDimensionInRange(screenResolution.y, Config.MIN_FRAME_HEIGHT, Config.MAX_FRAME_HEIGHT);

		int leftOffset = (screenResolution.x - width) / 2;
		int topOffset = (screenResolution.y - height) / 2;
		framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
		Log.d(TAG, "Calculated framing rect: " + screenResolution.x + " " + screenResolution.y + " " + width + " " + height + " " + topOffset);
	}
	
	private static int findDesiredDimensionInRange(int resolution, int hardMin, int hardMax) {
		int dim = 5 * resolution / 8; // Target 5/8 of each dimension
		if (dim < hardMin) {
			return hardMin;
		}
		if (dim > hardMax) {
			return hardMax;
		}
		return dim;
	}
}