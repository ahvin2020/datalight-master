package com.example.datalight.receiver;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.example.datalight.R;


public class ReceiverActivity extends Activity implements SurfaceHolder.Callback  {
	
	private static final String TAG = ReceiverActivity.class.getSimpleName();
	
	// ui variables
	private ViewFinderView viewFinderView;
	private SurfaceView previewView;
	private TextView debugTextView;
	private Button receiveFileButton;
	private AlertDialog mDialog;
	
	private ReceiverActivityManager receiverActivityManager;
	private boolean hasSurface;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_receiver);

		// init ReceiverActivityManager
		receiverActivityManager = new ReceiverActivityManager(this);
		
		// init ui
		mDialog = new AlertDialog.Builder(this).create();
		debugTextView = (TextView)findViewById(R.id.debug_text_view);
		
		// preview area
		previewView = (SurfaceView) findViewById(R.id.preview_view);
		
		// the view finder rectangle border
		viewFinderView = (ViewFinderView)findViewById(R.id.viewfinder_view);
		viewFinderView.setFramingRect(receiverActivityManager.getCameraManager().getFramingRect());
		
		// receive file button, disabled by default
		receiveFileButton = (Button)findViewById(R.id.receive_file_button);
		enableReceiveFileButton(false);
		receiveFileButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				receiverActivityManager.startFlashingToReceiveFile();
			}
		});
		
		Log.d(TAG, "START");
		
	}
	
	// --- overrides ---
	@Override
	protected void onResume() {
		super.onResume();
		
		// start preview callback
		SurfaceHolder previewHolder = previewView.getHolder();
		
		if (hasSurface) {
			// The activity was paused but not stopped, so the surface still exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			receiverActivityManager.startCamera(previewHolder);
		} else {
			// Install the callback and wait for surfaceCreated() to init the camera.
			previewHolder.addCallback(this);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();

		// stop camera
		receiverActivityManager.stopCamera();
		
		if (!hasSurface) {
			previewView.getHolder().removeCallback(this);
		}
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			receiverActivityManager.startCamera(holder);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}
	
	// --- public functions ---
	
	public void enableReceiveFileButton(boolean isEnable) {
		receiveFileButton.setEnabled(isEnable);
	}
	
	public void showReceiveResult(String fileName, boolean isSaved) {
		mDialog.setTitle("Result");
		mDialog.setMessage("file: " + fileName + ", saved: " + isSaved);
		mDialog.show();
	}
	
	public void showDebugText(String debugText) {
		debugTextView.setText(debugText);
	}
	
//	public void setResultPoints(ResultPoint[] resultPoints) {
//		viewFinderView.setResultPoints(resultPoints);
//	}
}