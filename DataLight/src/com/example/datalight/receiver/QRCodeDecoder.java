package com.example.datalight.receiver;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import android.graphics.Rect;

import com.example.datalight.common.CameraManager;
import com.example.datalight.common.Config;
import com.example.datalight.common.GZipUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

public class QRCodeDecoder {
	private static final String TAG = QRCodeDecoder.class.getSimpleName();
	
	private QRCodeReader qrCodeReader;
	private Map<DecodeHintType, Object> hints;
	private CameraManager cameraManager;
	private GZipUtil gzipUtil;
	
	public QRCodeDecoder(CameraManager cameraManager) {
		hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
		hints.put(DecodeHintType.CHARACTER_SET, Config.QR_CODE_CHARACTER_SET);
		hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
		hints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(BarcodeFormat.QR_CODE));
		
		this.cameraManager = cameraManager;
		qrCodeReader = new QRCodeReader();
		gzipUtil = new GZipUtil();
	}
	
	public Result decodeByteData(byte[] data, int width, int height) {
		Result result = null;
		
		Rect framingRectInPreview = cameraManager.getFramingRectInPreview();
		
		if (framingRectInPreview == null) {
			return null;
		}
		
		try {
			String decompressData = gzipUtil.decompress(data);
			data = decompressData.getBytes();
		} catch (IOException io) {
			io.printStackTrace();
		}
		
		LuminanceSource source = new PlanarYUVLuminanceSource(data, width, height, framingRectInPreview.left, framingRectInPreview.top, framingRectInPreview.width(), framingRectInPreview.height(), false);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		
		try {
			result = qrCodeReader.decode(bitmap, hints);
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (ChecksumException e) {
			e.printStackTrace();
		} catch (FormatException e) {
			e.printStackTrace();
		}
		
		return result;
	}
}