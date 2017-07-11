package com.example.nettyclientdemo.netty.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utils {
	public static byte[] read(String filePath) throws IOException {
		File file = new File(filePath);
		return read(file);
	}

	public static byte[] read(File file) throws IOException {
		byte[] buffer = new byte[(int) file.length()];
		InputStream ios = null;
		try {
			ios = new FileInputStream(file);
			if (ios.read(buffer) == -1) {
				throw new IOException("EOF reached while trying to read the whole file");
			}
		} finally {
			try {
				if (ios != null)
					ios.close();
			} catch (IOException e) {
			}
		}

		return buffer;
	}

	public static String getNowDateAccurate() {
		Calendar c = Calendar.getInstance();
		Date nowdate = c.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String s = sdf.format(nowdate);
		return s;
	}

	public static String doMD5(byte[] content) {
		try {
			if (content != null) {
				MessageDigest mdInst = MessageDigest.getInstance("MD5");
				byte[] md5Bytes = mdInst.digest(content);
				StringBuffer hexValue = new StringBuffer();
				for (int i = 0; i < md5Bytes.length; i++) {
					int val = ((int) md5Bytes[i]) & 0xff;
					if (val < 16)
						hexValue.append("0");
					hexValue.append(Integer.toHexString(val));
				}
				return hexValue.toString();
			} else {
				return null;
			}
		} catch (NoSuchAlgorithmException e) {
			// should never happen;
			return null;
		}
	}

	public static byte[] getFile(String path) {
		byte[] body;
		File file = new File(path);
		if (file.exists()) {
			try {
				body = Utils.read(file);
				return body;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;

	}
}
