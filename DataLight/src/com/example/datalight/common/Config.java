package com.example.datalight.common;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class Config {
	//--- send qr code settings ---
	public static final int MAX_BYTE_PER_QR_CODE = 500; // how many bytes per qr code
	public static final long SEND_QRCODE_INTERVAL = 1000 / 2; // 2 fps
	public static final String STRING_CHARACTER_SET = "ISO8859_1";	// used when initializing strings
	
	//--- receive qr code settings ---
	public static final int MIN_FRAME_WIDTH = 240;
	public static final int MIN_FRAME_HEIGHT = 240;
	public static final int MAX_FRAME_WIDTH = 1200; // = 5/8 * 1920
	public static final int MAX_FRAME_HEIGHT = 675; // = 5/8 * 1080
	
	//--- qr code settings ---
	public static final String QR_CODE_CHARACTER_SET = "ISO-8859-1"; // used to specify qr code character set
	public static final ErrorCorrectionLevel QR_CODE_ERROR_CORRECTION_LEVEL = ErrorCorrectionLevel.L;
	public static final BarcodeFormat QR_CODE_BARCODE_FORMAT = BarcodeFormat.QR_CODE;

	//--- flash signal settings ---
	public static final int START_SEND_FILE_FLASH_COUNT = 5; // flash 4 times to start sending file
	
	
	//--- receive flash settings ---
	public static final long RECEIVE_FLASH_INTERVAL = 1000 / 15; 	// 15 fps
	public static final int FLASHED_STATE_COUNT_THRESHOLD = 50000;//80000;	// if at least 50000 pixels have "flash", consider a flash has occured
	public static final int FLASH_LUMI = 170; //240;						// consider a flash if luminance reaches FLASH_LUMI
	public static final long FLASH_TIME_RANGE = 250;				// (in milliseconds) if flash occurs within this time, is considered a flash
	public static final long RESET_FLASH_TIME = 1000;				// (in milliseconds) if no flash occurs within this time, the flash count wil be resetted 
	
	//--- send flash settings ---
	public static final long FLASH_ON_INTERVAL = 100;				// flash on interval
	public static final long FLASH_OFF_INTERVAL = 200;				// flash off interval
		
	//--- color ---
	public static final int WHITE = 0xFFFFFFFF;
	public static final int BLACK = 0xFF000000;
	
	//--- FileInfo constants ---
	protected static final String NAME_KEY = "name";
	protected static final String QR_CODE_COUNT_KEY = "count";
}
