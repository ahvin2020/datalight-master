package com.example.datalight.common;

import org.json.JSONException;
import org.json.JSONObject;

// TODO: handle when file is NULL

// class containing file info
public class FileInfo {
	// file details
	protected String fileName;
	protected int totalQRCodes;
	
	public FileInfo() {
		fileName = null;
		totalQRCodes = -1;
	}
	
	// --- public classes ---
	public String getFileMetaData() {
		String fileMetaData = null;
		
		try {
			JSONObject fileMetaDataJson = new JSONObject();
			fileMetaDataJson.put(Config.NAME_KEY, fileName);
			fileMetaDataJson.put(Config.QR_CODE_COUNT_KEY, totalQRCodes);
			fileMetaData = fileMetaDataJson.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return fileMetaData;
	}
	
	public void setFileMetaData(String fileMetaData) {
		try {
			JSONObject fileMetaDataJson = new JSONObject(fileMetaData);
			fileName = fileMetaDataJson.getString("name");
			totalQRCodes = fileMetaDataJson.getInt("count");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public int getTotalQRCodes() {
		return totalQRCodes;
	}
}