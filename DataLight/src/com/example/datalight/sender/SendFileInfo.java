package com.example.datalight.sender;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import android.content.Context;

import com.example.datalight.common.Config;
import com.example.datalight.common.FileInfo;

public class SendFileInfo extends FileInfo {
	private byte[] dataByteArray;  // the file in byte array
	private File file;
	private int totalBytes;
	
	public Context context; // ONLY FOR TESTING, NOT NEEDED
	
	public SendFileInfo(File file, Context senderActivity) {
		super();
		
		this.context = senderActivity;
		this.file = file;
		
		if (file != null) {
			this.fileName = file.getName();
		} else { // only for testing
			this.fileName = "test.txt";
		}
		
//		if (file.exists()) {
			readInFile();
			
			// get file size
			if (dataByteArray != null) {
				totalBytes = dataByteArray.length;
				totalQRCodes = (int)Math.ceil((double)totalBytes / Config.MAX_BYTE_PER_QR_CODE); // how many qr codes we need to transfer
			} else {
				totalBytes = 0;
				totalQRCodes = 0;
			}
//		} else {
//			dataByteArray = null;
//		}
	}
	
	//--- public methods ---
	// how many bytes can we fit into this qr code, if we start from startingByte
	public int getBytesPerChunk(int startingByte) {
		int chunkBytes;

		// determine how many bytes for this chunk
		if (startingByte + Config.MAX_BYTE_PER_QR_CODE < dataByteArray.length) {
			chunkBytes = Config.MAX_BYTE_PER_QR_CODE;
		} else {
			chunkBytes = dataByteArray.length - startingByte;
		}

		return chunkBytes;
	}

	// get chunk data in string format
	public String getChunkData(int startingByte) {
		String chunkDataString = null;
		int bytesPerChunk = getBytesPerChunk(startingByte);

		// get the chunk into a byte array
		byte[] chunkedByteArray = new byte[bytesPerChunk];
		System.arraycopy(dataByteArray, startingByte, chunkedByteArray, 0, bytesPerChunk);

		// convert the byte array into string
		try {
			chunkDataString = new String(chunkedByteArray, Config.STRING_CHARACTER_SET);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return chunkDataString;
	}
	
	public int getTotalBytes() {
		if (dataByteArray != null) {
			return dataByteArray.length;
		} else {
			return -1;
		}
	}
	
	// --- private classes ---
	// read file into byte array
	private void readInFile() {
		InputStream inputStream = null;
		ByteArrayOutputStream bos = null;

		try {
			if (file != null) {
				inputStream = new FileInputStream(file); // use this for files outside this android package
			} else {
				inputStream = context.getAssets().open("test.txt"); // ONLY FOR TESTING
			}
			
			bos = new ByteArrayOutputStream();
			byte[] b = new byte[1024*8];
			int bytesRead =0;

			while ((bytesRead = inputStream.read(b)) != -1)
			{
				bos.write(b, 0, bytesRead);
			}

			dataByteArray = bos.toByteArray();
			
			bos.flush();
			bos.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
