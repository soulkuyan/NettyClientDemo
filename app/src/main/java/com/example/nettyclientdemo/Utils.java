package com.example.nettyclientdemo;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class Utils {
	private static final String SSID_KEY = "11235813";
	private static final String PW_KEY = "21345589";

	private static SecretKey desKeySSID;
	private static IvParameterSpec ivParamSSID;

	private static SecretKey desKeyPW;
	private static IvParameterSpec ivParamPW;

	static {
		try {
			byte[] sk, iv;
			DESKeySpec dks;
			SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");

			sk = SSID_KEY.getBytes("ascii");
			iv = SSID_KEY.getBytes("ascii");

			ivParamSSID = new IvParameterSpec(iv);
			dks = new DESKeySpec(sk);
			desKeySSID = skf.generateSecret(dks);

			sk = PW_KEY.getBytes("ascii");
			iv = PW_KEY.getBytes("ascii");

			ivParamPW = new IvParameterSpec(iv);
			dks = new DESKeySpec(sk);
			desKeyPW = skf.generateSecret(dks);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}

	}

	public static String generateSSID(String str)// 生成SSID
	{
		try {
			return Encrypt(str.getBytes("utf-8"), desKeySSID, ivParamSSID);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public static String generatePW(String str)// 生成Password
	{
		try {
			return Encrypt(str.getBytes("utf-8"), desKeyPW, ivParamPW);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	private static char[] CodeLib = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
			'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

	private static String Encrypt(byte[] input, SecretKey sKey, IvParameterSpec iv) {
		try {
			Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, sKey, iv);
			byte[] encrypt = cipher.doFinal(input);
			byte[] chars = Base64.encodeToString(encrypt, Base64.DEFAULT).substring(0, 10).toUpperCase().getBytes();
			byte[] output = new byte[10];
			out: for (int i = 0; i < chars.length; i++) {
				for (char c : CodeLib) {
					if (c == chars[i]) {
						output[i] = chars[i];
						continue out;
					}
				}

				output[i] = 'X';
			}

			return new String(output);
		} catch (NoSuchAlgorithmException e) {
			return null;// never happen
		} catch (NoSuchPaddingException e) {
			return null;// never happen
		} catch (InvalidKeyException e) {
			return null;// never happen
		} catch (InvalidAlgorithmParameterException e) {
			return null;// never happen
		} catch (BadPaddingException e) {
			return null;
		} catch (IllegalBlockSizeException e) {
			return null;
		}
	}

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

	public static byte[] concat(byte[] a, byte[] b) {
		int aLen;
		int bLen;
		if (a == null)
			aLen = 0;
		else
			aLen = a.length;

		if (b == null)
			bLen = 0;
		else
			bLen = b.length;

		byte[] c = new byte[aLen + bLen];
		System.arraycopy(a, 0, c, 0, aLen);
		System.arraycopy(b, 0, c, aLen, bLen);
		return c;
	}

//	public static void saveNV21Img(byte[] data, String fileName) {
//		YuvImage image = new YuvImage(data, ImageFormat.NV21, 960, 720, null);
//		Rect rectangle = new Rect();
//		rectangle.bottom = 720;
//		rectangle.top = 0;
//		rectangle.left = 0;
//		rectangle.right = 960;
//		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//		image.compressToJpeg(rectangle, 100, buffer);
//
//		FileOutputStream out = null;
//		try {
//			out = new FileOutputStream(Constants.APP_LOCAL_FILE_URL + fileName + ".jpg");
//			out.write(buffer.toByteArray());
//		} catch (Exception e) {
//			return;
//		} finally {
//			try {
//				out.close();
//			} catch (Exception e) {
//			}
//		}
//	}

	public static void showSystemUI() {
		execCommand("am startservice --user 0 -n com.android.systemui/.SystemUIService");
	}

	public static void hideSystemUI() {
		execCommand("service call activity 42 s16 com.android.systemui");
	}

	public static void cleanK3CameraFolder() {
		execCommand("rm -r /data/k3_camera\n");
	}

	public static void shutDownDevice(Context ctx) {
		execCommand("reboot -p\n");
	}

	public static void rebootDownDevice() {
		execCommand("reboot\n");
	}

	public static void enableWirelessAdb() {
		execCommand("setprop persist.adb.tcp.port 5555\n" + "stop adbd\n" + "start adbd\n");
	}

	public static void disableWirelessAdb() {
		execCommand("setprop persist.adb.tcp.port -1\n" + "stop adbd\n" + "start adbd\n");
	}

	public static void execCommand(String command) {
		try {
			String data = null;
			Process p = Runtime.getRuntime().exec("su");
			DataOutputStream stream = new DataOutputStream(p.getOutputStream());
			stream.writeBytes(command);
			stream.writeBytes("exit\n");
			BufferedReader ie = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String error = null;
			while ((error = ie.readLine()) != null && !error.equals("null")) {
				data += error + "\n";
				Log.e("execCommandSimple", data);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	public static void execCommandSimple(String command) {
		try {
			String data = null;
			Process p = Runtime.getRuntime().exec(command);
			BufferedReader ie = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String error = null;
			while ((error = ie.readLine()) != null && !error.equals("null")) {
				data += error + "\n";
				Log.e("execCommandSimple", data);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	public static float clamp(float x, float min, float max) {
		if (x > max) {
			return max;
		}
		if (x < min) {
			return min;
		}
		return x;
	}

	public static int clamp(int x, int min, int max) {
		if (x > max) {
			return max;
		}
		if (x < min) {
			return min;
		}
		return x;
	}

	public static long clamp(long x, long min, long max) {
		if (x > max) {
			return max;
		}
		if (x < min) {
			return min;
		}
		return x;
	}

	public static Bitmap getThumbnail(ContentResolver cr, String path) throws Exception {

		Cursor ca = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.MediaColumns._ID },
				MediaStore.MediaColumns.DATA + "=?", new String[] { path }, null);
		if (ca != null && ca.moveToFirst()) {
			int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
			ca.close();
			return MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MICRO_KIND, null);
		}

		ca.close();
		return null;

	}

//	public static Object buildObjectFromCursor(Cursor cursor, Class clazz) {
//		try {
//			Object obj = clazz.newInstance();
//			for (Field field : clazz.getDeclaredFields()) {
//				SqlLiteField sf = field.getAnnotation(SqlLiteField.class);
//				if (sf != null) {
//					field.setAccessible(true);
//					int columnIndex = cursor.getColumnIndex(field.getName());
//					System.out.println("=====columnIndex:" + columnIndex);
//					System.out.println("=====field.getName():" + field.getName());
//					if (field.getType() == int.class) {
//						field.set(obj, cursor.getInt(columnIndex));
//					} else if (field.getType() == long.class) {
//						field.set(obj, cursor.getLong(columnIndex));
//					} else if (field.getType() == String.class) {
//						field.set(obj, cursor.getString(columnIndex));
//					} else if (field.getType().isEnum()) {
//						field.set(obj, field.getType().getEnumConstants()[cursor.getInt(columnIndex)]);
//					} else if (field.getType() == Timestamp.class) {
//						field.set(obj, Timestamp.valueOf(cursor.getString(columnIndex)));
//					}
//				}
//			}
//			return obj;
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//			return null;
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//			return null;
//		} catch (InstantiationException e) {
//			e.printStackTrace();
//			return null;
//		} catch (CursorIndexOutOfBoundsException e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
//
//	public static ContentValues buildContentValuesFromObject(Object obj, Class clazz) {
//		try {
//			ContentValues cv = new ContentValues();
//			for (Field field : clazz.getDeclaredFields()) {
//				SqlLiteField sf = field.getAnnotation(SqlLiteField.class);
//				if (sf != null) {
//					field.setAccessible(true);
//					if (field.getType() == int.class) {
//						cv.put(field.getName(), (Integer) field.get(obj));
//					} else if (field.getType() == long.class) {
//						cv.put(field.getName(), (Long) field.get(obj));
//					} else if (field.getType() == String.class) {
//						cv.put(field.getName(), (String) field.get(obj));
//					} else if (field.getType().isEnum()) {
//						cv.put(field.getName(), ((Enum) field.get(obj)).ordinal());
//					} else if (field.getType() == Timestamp.class) {
//						final SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//						cv.put(field.getName(), parser.format(((Timestamp) field.get(obj))));
//					}
//				}
//			}
//			return cv;
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//			return null;
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//			return null;
//		}
//	}

//	public static String genCreateTableScript(Class clazz) {
//		StringBuilder sb = new StringBuilder();
//		sb.append("CREATE TABLE IF NOT EXISTS " + clazz.getSimpleName() + "(");
//		sb.append("_id INTEGER PRIMARY KEY AUTOINCREMENT");
//		Field[] fields = clazz.getDeclaredFields();
//		for (Field field : fields) {
//			SqlLiteField sf = field.getAnnotation(SqlLiteField.class);
//			if (sf != null) {
//				sb.append("," + field.getName() + " ");
//
//				if (field.getType() == int.class) {
//					sb.append("INTEGER");
//				} else if (field.getType() == long.class) {
//					sb.append("INTEGER");
//				} else if (field.getType() == String.class) {
//					sb.append("TEXT");
//				} else if (field.getType() == Timestamp.class) {
//					sb.append("TimeStamp");
//				} else if (field.getType().isEnum()) {
//					sb.append("INTEGER");
//				}
//
//				if (sf.notNull())
//					sb.append(" NOT NULL");
//
//				if (sf.unique())
//					sb.append(" UNIQUE");
//
//				if (!sf.foreign().equals("")) {
//					sb.append(" REFERENCES " + sf.foreign());
//					sb.append(" ON DELETE CASCADE ON UPDATE CASCADE");
//				}
//			}
//		}
//		sb.append(");");
//		return sb.toString();
//	}

	public static Timestamp strToTimestamp(String str) {
		try {
			DateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date date = null;
			date = mFormat.parse(str);
			long time = date.getTime();
			return new Timestamp(time);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Timestamp nowTimeTimestamp() {
		try {
			return new Timestamp(System.currentTimeMillis());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Date strToDate(String str) {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = format.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	public static String strSimple(String str) {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = format.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm:ss");
		String s = sdf.format(date);
		return s;
	}

	public static String getNowDate() {
		Calendar c = Calendar.getInstance();
		Date nowdate = c.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String s = sdf.format(nowdate);
		return s;
	}

	public static String getNowDateAccurate() {
		Calendar c = Calendar.getInstance();
		Date nowdate = c.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		String s = sdf.format(nowdate);
		return s;
	}

	public static String getNowDateToSecond() {
		Calendar c = Calendar.getInstance();
		Date nowdate = c.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String s = sdf.format(nowdate);
		return s;
	}
}
