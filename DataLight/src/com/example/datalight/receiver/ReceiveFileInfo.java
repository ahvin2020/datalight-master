package com.example.datalight.receiver;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;

import android.os.Environment;

import com.example.datalight.common.Config;
import com.example.datalight.common.FileInfo;

public class ReceiveFileInfo extends FileInfo {
	public String[] dataArray; // store each qr code in this array
	
	public ReceiveFileInfo() {
		super();
		
		dataArray = null;
	}
	
	// --- public methods
	public void setFileMetaData(String fileMetaData) {
		super.setFileMetaData(fileMetaData);
		
		dataArray = new String[totalQRCodes];
	}
	
	// save the qr code
	public void setData(String data, int qrCodeIndex) {
		if (dataArray != null && dataArray.length > qrCodeIndex) {
			dataArray[qrCodeIndex] = data;
		}
	}
	
	// save data to file
	public boolean saveData() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i=0; i<totalQRCodes; i++) {
			stringBuilder.append(dataArray[i]);
		}
		
		byte[] dataByteArray = stringBuilder.toString().getBytes(Charset.forName(Config.STRING_CHARACTER_SET));
		 
		// write to /sdcard
		try {
			File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), fileName);
			FileOutputStream outputStream = new FileOutputStream(file);
			outputStream.write(dataByteArray);
			outputStream.close();
			
			return file.exists();
//				Log.d(TAG, chunkedDataString);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
