/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.datalight.sender;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import android.graphics.Bitmap;

import com.example.datalight.common.Config;
import com.example.datalight.common.GZipUtil;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public final class QRCodeEncoder {
	private int dimension;
	
	// qr code stuff
	private QRCodeWriter writer;
	private Map<EncodeHintType, Object> hints;
	
	private GZipUtil gzipUtil;
	
	public QRCodeEncoder(int dimension) {
		hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
		hints.put(EncodeHintType.CHARACTER_SET, Config.QR_CODE_CHARACTER_SET);
		hints.put(EncodeHintType.ERROR_CORRECTION, Config.QR_CODE_ERROR_CORRECTION_LEVEL);

		writer = new QRCodeWriter();
		gzipUtil = new GZipUtil();
		
		this.dimension = dimension;
	}

	public Bitmap encodeStringAsBitmap(String data) throws WriterException {
		try {
			byte[] compressedData = gzipUtil.compress(data);
			data = new String(compressedData);
		} catch (IOException io) {
			io.printStackTrace();
		}
		
		BitMatrix result = writer.encode(data, Config.QR_CODE_BARCODE_FORMAT, dimension, dimension, hints);
		
		int width = result.getWidth();
		int height = result.getHeight();
		int[] pixels = new int[width * height];
		
		// All are 0, or black, by default
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				pixels[offset + x] = result.get(x, y) ? Config.BLACK : Config.WHITE;
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}
}