package com.example.datalight.receiver;

import java.io.IOException;

import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;

import com.example.datalight.common.CameraManager;
import com.example.datalight.common.Config;
import com.google.zxing.Result;

@SuppressWarnings("deprecation")
public class ReceiverActivityManager {
	// what is the receiver doing right now?
	enum ReceiveState {
		IDLE,							// not doing anything
		READY_RECEIVE_FILE_METADATA,	// camera preview started, and it is waiting for a file metadata
		RECEIVED_FILE_METADATA,			// received file metadata, waiting for user to start flash signal
//		SENDING_SIGNAL_REQUEST_DATA,	// received file metadata, now using flashes to signal sender to start sending file
		RECEIVING_FILE,					// receiving file chunks
		PROCESSING_FILE					// finish receiving, now processing data and saving file
	}
		
	private static final String TAG = ReceiverActivityManager.class.getSimpleName();

	private final ReceiverActivity receiverActivity;
	
	private QRCodeDecoder qrCodeDecoder;
	private CameraManager cameraManager;
	private FlashSender flashSender;
	
	// receive variables
	private ReceiveState receiveState;				// what is the receiver doing right now
	private ReceiveFileInfo currentReceiveFileInfo;	// stores the file to receive
	private String currentReceivedData;				// what is the last received qr code
	private int currentQRCode;

	public ReceiverActivityManager(ReceiverActivity receiverActivity) {
		this.receiverActivity = receiverActivity;
		
		cameraManager = new CameraManager(receiverActivity, CameraInfo.CAMERA_FACING_BACK); // init camera manager
		qrCodeDecoder = new QRCodeDecoder(cameraManager);
		flashSender = new FlashSender(this);
		
		// receiver is not doing anything now
		receiveState = ReceiveState.IDLE;
	}

	// --- public methods
	public CameraManager getCameraManager() {
		return cameraManager;
	}

	// flash sender informs that it has finish flashing
//	public void informFinishFlashing(int flashCount) {
//		if (flashCount == Config.START_SEND_FILE_FLASH_COUNT) {
//			if (receiveState == ReceiveState.SENDING_SIGNAL_REQUEST_DATA) {
//				receiveState = ReceiveState.RECEIVING_FILE;
//			}
//		}
//	}
	
	// start the camera
	public void startCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}

		if (cameraManager.isOpen()) {
			Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
			return;
		}

		// start camera
		try {
			currentReceivedData = null;
			
			cameraManager.openDriver(surfaceHolder);
			cameraManager.startPreview(previewCallback);
			
// TODO: what if come back after a pause? 
			receiveState = ReceiveState.READY_RECEIVE_FILE_METADATA;
			
//			flashSender.startFlashing(Config.START_SEND_FILE_FLASH_COUNT);
		} catch (IOException e) {
			Log.w(TAG, e);
		}
	}

	// stop camera
	public void stopCamera() {
		flashSender.stopFlashing();
		
		cameraManager.stopPreview();
		cameraManager.closeDriver();
	}

	public void startFlashingToReceiveFile() {
		receiveState = ReceiveState.RECEIVING_FILE;
		flashSender.startFlashing(Config.START_SEND_FILE_FLASH_COUNT);
	}
	
	// --- private methods

	// preview callback for camera
	private Camera.PreviewCallback previewCallback = new PreviewCallback() {
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			
			// only process the preview data when we need it
			if (receiveState == ReceiveState.READY_RECEIVE_FILE_METADATA || receiveState == ReceiveState.RECEIVING_FILE) {
				// an array to rotate the data (because we rotate the preview by 90 degrees)
				byte[] rotatedData = new byte[data.length];

				// get camera resolution
				Point cameraResolution = cameraManager.getCameraResolution();
				int width = cameraResolution.x;
				int height = cameraResolution.y;

				// then rotate the data now
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						rotatedData[x * height + height - y - 1] = data[x + y * width];
					}
				}

				// after we rotate data, dimension is rotated too
				int tmp = width;
				width = height;
				height = tmp;

				Result result = qrCodeDecoder.decodeByteData(rotatedData, width, height);

				if (result != null) {

					// set result points on view finder
					//				viewFinderView.setResultPoints(result.getResultPoints());

					// get string data on the qr code
					String newReceivedData = result.getText();

					// new qr code?
					if (currentReceivedData == null || currentReceivedData.equals(newReceivedData) == false) {
						currentReceivedData = newReceivedData;

// TODO: differentiate between file metadata and normal file chunk
						// receive file metadata?
						if (receiveState == ReceiveState.READY_RECEIVE_FILE_METADATA) {
							currentQRCode = 0;

							currentReceiveFileInfo = new ReceiveFileInfo();
							currentReceiveFileInfo.setFileMetaData(newReceivedData);

							
							// finish receiving file metadata, enable receive file button
							receiveState = ReceiveState.RECEIVED_FILE_METADATA;
							receiverActivity.enableReceiveFileButton(true);
							
							receiverActivity.showDebugText("file: " + currentReceiveFileInfo.getFileName());
						} 
						// receive file chunks
						else if (receiveState == ReceiveState.RECEIVING_FILE) {
							// add the receive data to string builder
							currentReceiveFileInfo.setData(newReceivedData, currentQRCode);

							// increment qr code received
							currentQRCode++;

							receiverActivity.showDebugText(currentQRCode + "/" + currentReceiveFileInfo.getTotalQRCodes());

							// received all the data?
							if (currentQRCode >= currentReceiveFileInfo.getTotalQRCodes()) {
								receiveState = ReceiveState.PROCESSING_FILE;
								
								// stop receiving data
								stopCamera();

								boolean isSaved = currentReceiveFileInfo.saveData();
								receiverActivity.showReceiveResult(currentReceiveFileInfo.getFileName(), isSaved);
								
								receiveState = ReceiveState.IDLE;
							}
						}
					}
				}
			}
		}
	};
}
