package com.example.nettyclientdemo;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nettyclientdemo.netty.NettyLongConnect;
import com.example.nettyclientdemo.netty.NettyShortConnect;
import com.example.nettyclientdemo.netty.utils.MsgContent;
import com.example.nettyclientdemo.netty.utils.MsgReceiveCallBack;
import com.example.nettyclientdemo.netty.utils.NettyConnectCallBack;
import com.example.nettyclientdemo.netty.utils.NettyUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements MsgReceiveCallBack, NettyConnectCallBack {
	NettyLongConnect longConnect;
	TextView state_tv, receive_msg_tv, all_tv, has_upload, receive_file_tv, has_uploaded_tv, error_uploaded_tv;
	Button send_msg_bt, send_file_bt, press_test_bt;
	EditText send_msg_et;
	MainActivity _this;
	int index = 0;
	List<String> files;
	private WakeLock mPMWakeLock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_this = this;
		setContentView(R.layout.activity_main);
		// long connect
		longConnect = NettyLongConnect.initLongConnect(_this, _this);
		setListenersAndValues();
		files = NettyUtils.getFiles();
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mPMWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NETTY");
		if (mPMWakeLock != null) {
			mPMWakeLock.acquire();
		}
	}

	private void setListenersAndValues() {
		error_uploaded_tv = (TextView) findViewById(R.id.error_uploaded_tv);
		has_uploaded_tv = (TextView) findViewById(R.id.has_uploaded_tv);
		press_test_bt = (Button) findViewById(R.id.press_test_bt);
		all_tv = (TextView) findViewById(R.id.all_tv);
		has_upload = (TextView) findViewById(R.id.has_upload);
		state_tv = (TextView) findViewById(R.id.state_tv);
		send_msg_bt = (Button) findViewById(R.id.send_msg_bt);
		send_msg_bt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Boolean result = longConnect.sendMsg(new MsgContent(send_msg_et.getText().toString()));
				if (!result) {
					Toast.makeText(_this, "network unavailable", Toast.LENGTH_SHORT).show();
				}
			}
		});
		send_msg_et = (EditText) findViewById(R.id.send_msg_et);
		receive_msg_tv = (TextView) findViewById(R.id.receive_msg_tv);
		receive_file_tv = (TextView) findViewById(R.id.receive_file_tv);
		send_file_bt = (Button) findViewById(R.id.send_file_bt);
		send_file_bt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 获取文件夹下所有文件名称列表
				if (files == null || files.size() == 0) {
					files = NettyUtils.getFiles();
				}
				new sendFile().execute();
			}
		});
		press_test_bt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new sendPressTest().execute();
			}
		});
	}

	@Override
	public void onNettyDisconnected() {
		msgHandler.sendEmptyMessage(ACTION_DISCONNECT);
	}

	@Override
	public void onNettyConnected() {
		msgHandler.sendEmptyMessage(ACTION_CONNECT);
	}

	@Override
	public void onReceiveTabletMessage(MsgContent message) {

		Message msg = msgHandler.obtainMessage(ACTION_RECEIVE_TABLE_MSG, message);
		msgHandler.sendMessage(msg);
	}

	NettyShortConnect shortConnect = new NettyShortConnect();

	class sendFile extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			List<String> tempFile = new ArrayList<String>();
			tempFile.addAll(files);
			int size = tempFile.size();
			Message msg = msgHandler.obtainMessage(ACTION_all_number, new MsgContent(size));
			msgHandler.sendMessage(msg);
			for (int i = 0; i < size; i++) {
				String f = tempFile.get(i);
				System.out.println("==============发送文件：" + i + "====" + f);
				if (longConnect.isConnected()) {

					Boolean result = shortConnect.sendFile(new MsgContent(f));
					if (!result) {
						return false;
					} else {
						// 删除文件
						Message msg2 = msgHandler.obtainMessage(ACTION_Show_number, new MsgContent(i + 1));
						msgHandler.sendMessage(msg2);
						files.remove(f);
					}
				} else {
					return false;
				}
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (!result) {
				Toast.makeText(_this, "upload fail", Toast.LENGTH_SHORT).show();
			}
		}

	}

	class sendPressTest extends AsyncTask<Void, Void, Void> {
		int all_number = 0;
		int error_number = 0;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			List<String> mFiles = NettyUtils.getFiles();
			while (true) {
				for (int i = 0; i < mFiles.size(); i++) {
					Boolean result = shortConnect.sendFile(new MsgContent(mFiles.get(i)));
					System.out.println("===============获取上传文件的结果了=="+result);
					if (result != true){
						Log.i("TAG", "=====此时连接中断==网络连接为：" + NettyUtils.isNetworkConnected(MainActivity.this));
					}
					Message msg_all = msgHandler.obtainMessage(ACTION_press_test_all_number,
							new MsgContent(++all_number));
					msgHandler.sendMessage(msg_all);
					if (!result) {
						Message msg_error = msgHandler.obtainMessage(ACTION_press_test_error_number,
								new MsgContent(++error_number));
						msgHandler.sendMessage(msg_error);
					}
				}
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}

	}

	private static final int ACTION_RECEIVE_TABLE_MSG = 0;
	private static final int ACTION_CONNECT = 1;
	private static final int ACTION_DISCONNECT = 2;
	private static final int ACTION_Show_number = 3;
	private static final int ACTION_all_number = 4;
	private static final int ACTION_press_test_all_number = 5;
	private static final int ACTION_press_test_error_number = 6;
	MsgHandler msgHandler = new MsgHandler();

	private class MsgHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			MsgContent msgContent = (MsgContent) msg.obj;
			switch (msg.what) {
			case ACTION_RECEIVE_TABLE_MSG:

				switch (msgContent.getType()) {
				case STRMSG:
					receive_msg_tv.setText(msgContent.getContent());
					break;
				case FILE:
					receive_file_tv.setText("收到一张图片：" + (++index));
					break;

				default:
					break;
				}
				break;
			case ACTION_CONNECT:
				state_tv.setText("连接");
				break;
			case ACTION_DISCONNECT:
				state_tv.setText("断开");
				break;
			case ACTION_Show_number:
				has_upload.setText("已上传个数" + msgContent.getNumber());
				break;
			case ACTION_all_number:
				all_tv.setText("总数：" + msgContent.getNumber());
				break;
			case ACTION_press_test_all_number:
				has_uploaded_tv.setText("上传个数" + msgContent.getNumber());
				break;
			case ACTION_press_test_error_number:
				error_uploaded_tv.setText("失败个数：" + msgContent.getNumber());
				break;
			default:
				break;
			}

		}
	}

	@Override
	protected void onDestroy() {
		if (mPMWakeLock != null) {
			mPMWakeLock.release();
			mPMWakeLock = null;
		}
		super.onDestroy();
	}
}
