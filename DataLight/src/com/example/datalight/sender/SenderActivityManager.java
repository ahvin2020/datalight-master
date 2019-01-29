package com.example.datalight.sender;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Handler;
import android.view.Display;
import android.view.WindowManager;

import com.example.datalight.common.Config;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

public class SenderActivityManager {

	// what is the sender doing right now?
	enum SenderState {
		IDLE,							// not doing anything
		PROCESSING_FILE,				// reading in file and encoding it
		WAITING_SIGNAL_TO_START_SEND,	// displaying file metadata, and waiting for a signal to start sending file
		SENDING_FILE					// sending file
	}

	// ui variables
	private final SenderActivity senderActivity;

	// helper classes
	private QRCodeEncoder qrCodeEncoder;
	private FlashReceiver flashReceiver;

	// variables for sending stuff
	private SenderState senderState;			// what is the sender doing right now?
	private SendFileInfo currentSendFileInfo; 	// stores the file to send
	private Handler handler;					// handler for repeating task (to handle the runnable)
	private Runnable runnable;					// runs a task
	private int currentByte;					// which byte we are gonna send next?

	public SenderActivityManager(SenderActivity senderActivity) {
		this.senderActivity = senderActivity;

		initQRCodeEncoder(senderActivity); 		// init qr code encoder
		flashReceiver = new FlashReceiver(this); 	// init flash receiver

		// sender is not doing anything now
		senderState = SenderState.IDLE;
	}

	//--- public functions ---

	// flashReceiver alerts that flashCount has occured
	public void informFlashCount(int flashCount) {
		// we are waiting for signal to start sending?
		if (senderState == SenderState.WAITING_SIGNAL_TO_START_SEND) {
			// we received signal to start sending?
			if (flashCount == Config.START_SEND_FILE_FLASH_COUNT) {
				senderActivity.disableFileButtons();
				startSendingFileData();
			}
		}
	}

	public SenderActivity getSenderActivity() {
		return senderActivity;
	}

	// read in file to memory, then send
	public void prepareFileForSending(File file) {
		// sender is going to process file now
		this.senderState = SenderState.PROCESSING_FILE;

		// read in the file
		currentSendFileInfo = new SendFileInfo(file, senderActivity);

		// display file metadata
		displayFileMetadata();
		
		// start the flash receiver
		flashReceiver.startReceiving();
	}

	// FOR TESTING
	public void generateTestQR() {
		try {
			// encode string
			String testString = "Hello World!";
			Bitmap bitmap = qrCodeEncoder.encodeStringAsBitmap(testString);
			senderActivity.showQRCode(bitmap);
			
			// convert bitmap to binary bitmap
			int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
            bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
            RGBLuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), pixels);
            BinaryBitmap bbitmap = new BinaryBitmap(new HybridBinarizer(source));
            
            // now decode it
            QRCodeReader qrCodeReader =  new QRCodeReader();
			Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
			hints.put(DecodeHintType.CHARACTER_SET, Config.QR_CODE_CHARACTER_SET);
            try {
				Result result = qrCodeReader.decode(bbitmap, hints);
				if (result != null) {
					senderActivity.showDebugText(result.getText());
				} else {
					senderActivity.showDebugText("is null");
				}
			} catch (NotFoundException | ChecksumException | FormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (WriterException e) {
			e.printStackTrace();
		}
	}
	
	public void stopCamera() {
		flashReceiver.stopReceiving();
	}
	
	//--- private functions ----

	// display file metadata, then wait for a flash signal to start sending file data
	private void displayFileMetadata() {
		try {
			// show file metadata
			String fileMetadata = currentSendFileInfo.getFileMetaData();
			Bitmap bitmap = qrCodeEncoder.encodeStringAsBitmap(fileMetadata.toString());
			senderActivity.showQRCode(bitmap);

			// show debug text
			String debugText = "file: " + currentSendFileInfo.getFileName() + " bytes: " + currentSendFileInfo.getTotalBytes();
			senderActivity.showDebugText(debugText);

			// now, wait for a signal to start sending the data
			this.senderState = SenderState.WAITING_SIGNAL_TO_START_SEND;
		} catch (WriterException e) {
			e.printStackTrace();
		}
	}

	private void initQRCodeEncoder(Context context) {
		if (senderActivity != null) {
			WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			Display display = manager.getDefaultDisplay();
			Point point = new Point();
			display.getSize(point);
			int width = point.x;
			int height = point.y;
			int qrCodeDimension = width < height ? width : height;
			qrCodeDimension = qrCodeDimension * 3/4;

			qrCodeEncoder = new QRCodeEncoder(qrCodeDimension);
		}
	}

	private void startSendingFileData() {
		currentByte = 0;

		// create the runnable
		handler = new Handler();
		runnable = new Runnable() {
			@Override
			public void run() {
				try {
					// get chunk data and convert into qr code, then show it
					String chunkedDataString = currentSendFileInfo.getChunkData(currentByte);
					Bitmap bitmap = qrCodeEncoder.encodeStringAsBitmap(chunkedDataString);
					senderActivity.showQRCode(bitmap);

					// show debug text
					int currentQRCode = (int)Math.ceil((double)currentByte / Config.MAX_BYTE_PER_QR_CODE);
					int bytesPerChunk = currentSendFileInfo.getBytesPerChunk(currentByte);
					String debugText = (currentQRCode + 1) + " of " + currentSendFileInfo.getTotalQRCodes() + " " + bytesPerChunk + " Bytes (total " + currentSendFileInfo.getTotalBytes() + " Bytes)";
					senderActivity.showDebugText(debugText);

					// get the next byte, then check have we finished sending
					currentByte += Config.MAX_BYTE_PER_QR_CODE;
					if (currentByte >= currentSendFileInfo.getTotalBytes()) {
						stopRepeatingTask();
						
						// start the flash receiver
						flashReceiver.startReceiving();
					} else {
						// run next one
						handler.postDelayed(runnable, Config.SEND_QRCODE_INTERVAL);
					}
				} catch (WriterException e) {
					e.printStackTrace();
				}
			}
		};

		// start the runnable
		runnable.run();
	}

	private void stopRepeatingTask() {
		handler.removeCallbacks(runnable);
	}
}
