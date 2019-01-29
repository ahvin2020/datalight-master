package com.example.datalight.common;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZipUtil {
	private static final String UTF8 = "UTF-8";
	
	public GZipUtil() {
	}

	public byte[] compress(String string) throws IOException {
		if (string == null || string.length() == 0) {
			return string.getBytes(UTF8);
		}
		
		System.out.println("Input Array length : " + string.getBytes(UTF8).length);
		System.out.println("Input String length : " + string.length());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(baos);
		gzip.write(string.getBytes(UTF8));
		gzip.close();
		
		byte[] outputArray = baos.toByteArray();
		
		System.out.println("Output Array length : " + outputArray.length);
		System.out.println("Output String length : " + new String(outputArray).length());
		
		return outputArray;
	}

	public String decompress(byte[] bytes) throws IOException {
		if (bytes == null || bytes.length == 0) {
			return "";
		}
		
		System.out.println("Input Array length : " + bytes.length);
		GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));
		BufferedReader br = new BufferedReader(new InputStreamReader(gis, UTF8));
		
		String outputString = "";
		String read = br.readLine();
		
		while (read != null) {
			outputString += read;
			read = br.readLine();
		}
		
		System.out.println("Output String length : " + outputString.getBytes(UTF8).length);
		
		return outputString;
	}
	
	public static void main(String[] args) throws IOException {
		GZipUtil gzipUtil = new GZipUtil();
		
		String string = "I am what I am hhhhhhhhhhhhhhhhhhhhhhhhhhhhh"
				+ "bjggujhhhhhhhhh" + "rggggggggggggggggggggggggg"
				+ "esfffffffffffffffffffffffffffffff"
				+ "esffffffffffffffffffffffffffffffff"
				+ "esfekfgy enter code here`etd`enter code here wdd"
				+ "heljwidgutwdbwdq8d" + "skdfgysrdsdnjsvfyekbdsgcu"
				+ "jbujsbjvugsduddbdj";
		
		System.out.println("after compress:");
		byte[] compressed = gzipUtil.compress(string);
		System.out.println(compressed + ": " + compressed.length);
		System.out.println("after decompress:");
		String decomp = gzipUtil.decompress(compressed);
		System.out.println(decomp);
	}
}