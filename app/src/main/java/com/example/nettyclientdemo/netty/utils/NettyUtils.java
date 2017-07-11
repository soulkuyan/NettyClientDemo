package com.example.nettyclientdemo.netty.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import client.ProtobufProto;

public class NettyUtils {
	public static ProtobufProto.ProtobufMessage buildMsg(ProtobufProto.ProtobufMessage.Type type, MsgContent msg) {
		ProtobufProto.ProtobufMessage.Builder builder = getProtoBuf();
		builder.setReqType(type);
		if (null != msg) {
			switch (type) {
			case STRMSG:
				builder.setBody(getBody(msg));
				builder.getVersion();
				break;
			case FILE:
				// 设置路径
				String path = msg.getContent();
				String[] paths = path.split("/");
				String fileName = paths[paths.length - 1];
				builder.setParam(fileName);

				// 获取MD5
				byte[] file = Utils.getFile(msg.getContent());
				String md5 = Utils.doMD5(file);
				builder.setMd5(md5);

				// 获取文件
				ByteString body = ByteString.copyFrom(file);
				builder.setBody(body);
				file = null;
				System.gc();
				break;
			default:
				break;
			}
		}
		return builder.build();
	}

	public static ByteString getBody(MsgContent msg) {
		String msgString = new Gson().toJson(msg);
		byte[] msgBytes;
		try {
			msgBytes = msgString.getBytes("utf-8");
			return ByteString.copyFrom(msgBytes);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public static MsgContent getMsgContent(ProtobufProto.ProtobufMessage message) {
		ByteString body = message.getBody();
		String bodyString;
		try {
			bodyString = body.toString("utf-8");
			MsgContent msg=new Gson().fromJson(bodyString, MsgContent.class);
			msg.setType(message.getReqType());
			return msg;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static ProtobufProto.ProtobufMessage.Builder getProtoBuf() {
		ProtobufProto.ProtobufMessage.Builder builder = ProtobufProto.ProtobufMessage.newBuilder();
		builder.setSn(UUID.randomUUID().toString());
		builder.setVersion("1.0.1");
		builder.setTimeStamp(System.currentTimeMillis());
		builder.setContentEncoding("uft-8");
		builder.setPriority(ProtobufProto.ProtobufMessage.Priority.NORMAL);
		builder.setMessageRole(ProtobufProto.ProtobufMessage.MessageRole.REQ);
		System.out.println(
				builder.getVersion() + "====" + builder.getTimeStamp() + "====" + builder.getContentEncoding());
		return builder;
	}



	private static String serverFilePath = Environment.getExternalStorageDirectory().getPath() + "/visunex/server_pic/";

	public static void byte2image(byte[] data, String name) {
		String path = serverFilePath + name;
		if (data.length < 3 || path.equals(""))
			return;
		try {
			FileOutputStream imageOutput = new FileOutputStream(new File(path));
			imageOutput.write(data, 0, data.length);
			imageOutput.close();
			System.out.println("Make Picture success,Please find image in " + path);
		} catch (Exception ex) {
			System.out.println("Exception: " + ex);
			ex.printStackTrace();
		}
	}

	private static String clientFilePath = Environment.getExternalStorageDirectory().getPath() + "/visunex/client_pic/";

	// 获取文件夹下所有文件名称列表
	public static List<String> getFiles() {
		List<String> fileNames = new ArrayList<String>();
		File f = new File(clientFilePath);
		File fa[] = f.listFiles();
		for (int i = 0; i < fa.length; i++) {
			String path = fa[i].getPath();
			fileNames.add(path);
		}
		return fileNames;
	}

	public static void deleteFile(String path) {
		File file = new File(path);
		if (file.exists()){
			System.out.println("==========删除文件:"+path);
			file.delete();
		}else{
			System.out.println("==========删除文件不存在:"+path);
		}
	}

	public static boolean isNetworkConnected(Context context){
		if (context != null){
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
			if (info != null){
				return info.isAvailable();
			}
		}
		return false;
	}

}
