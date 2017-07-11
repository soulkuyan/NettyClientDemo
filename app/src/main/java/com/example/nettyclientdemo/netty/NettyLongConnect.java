/*
 * Copyright 2013-2018 Lilinfeng.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.nettyclientdemo.netty;

import com.example.nettyclientdemo.netty.resolver.HeaderLengthFrameDecoder;
import com.example.nettyclientdemo.netty.resolver.HeaderLengthFrameEncoder;
import com.example.nettyclientdemo.netty.resolver.SubReqClientHandler;
import com.example.nettyclientdemo.netty.utils.MsgContent;
import com.example.nettyclientdemo.netty.utils.MsgReceiveCallBack;
import com.example.nettyclientdemo.netty.utils.NettyConnectCallBack;
import com.example.nettyclientdemo.netty.utils.NettyConstant;
import com.example.nettyclientdemo.netty.utils.NettyUtils;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import client.HeartBeatReqHandler;
import client.ProtobufProto;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;

/**
 * @author Lilinfeng
 * @date 2014年3月15日
 * @version 1.0
 */
public class NettyLongConnect {

	private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	
	SubReqClientHandler reqHandler;
	Channel channel;
	boolean connected = false;
	// callback
	MsgReceiveCallBack msgReceiveCB;
	NettyConnectCallBack nettyConnectCB;
	NettyLongConnect _this;

	public NettyLongConnect(MsgReceiveCallBack msgReceiveCB, NettyConnectCallBack nettyConnectCB) {
		super();
		_this = this;
		this.msgReceiveCB = msgReceiveCB;
		this.nettyConnectCB = nettyConnectCB;

	}

	//连接Socket端
	public void connect(int port, String host) {

		// 配置客户端NIO线程组
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			reqHandler = new SubReqClientHandler(msgReceiveCB, _this);
			Bootstrap b = new Bootstrap();
			b.group(group)//
					.channel(NioSocketChannel.class)//
					.option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(new HeaderLengthFrameDecoder());
							ch.pipeline()
									.addLast(new ProtobufDecoder(ProtobufProto.ProtobufMessage.getDefaultInstance()));
							ch.pipeline().addLast(new HeaderLengthFrameEncoder());
							ch.pipeline().addLast(new ProtobufEncoder());
							// 增加了读超时handler
							ch.pipeline().addLast("readTimeoutHandler", new ReadTimeoutHandler(50));
							ch.pipeline().addLast("HeartBeatHandler", new HeartBeatReqHandler());
							ch.pipeline().addLast(reqHandler);
						}
					});
			// 发起异步连接操作
			ChannelFuture future = b.connect(new InetSocketAddress(host, port)).sync();
			channel = future.channel();
			nettyConnectCB.onNettyConnected();
			setConnected(true);
			// 等待客户端链路关闭
			future.channel().closeFuture().sync();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			group.shutdownGracefully();
			channel = null;
			nettyConnectCB.onNettyDisconnected();
			setConnected(false);
			// 所有资源释放完成之后，清空资源，再次发起重连操作
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						TimeUnit.SECONDS.sleep(1);
						try {
							connect(NettyConstant.MSG_PORT, NettyConstant.MSG_IP);// ������������
						} catch (Exception e) {
							e.printStackTrace();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	// /**
	// * @param args
	// * @throws Exception
	// */
	// public static void main(String[] args) throws Exception {
	// NettyLongConnect messageConnect = new NettyLongConnect(null,null);
	// messageConnect.connect(NettyConstant.MSG_PORT, NettyConstant.MSG_IP);
	// }
	public Boolean sendMsg(MsgContent msg) {
		System.out.println("消息==发送===============" + Thread.currentThread().getName());
		if (channel != null) {
			ProtobufProto.ProtobufMessage heatBeat = NettyUtils.buildMsg(ProtobufProto.ProtobufMessage.Type.STRMSG,
					msg);
			System.out.println("Client send messsage to server : --->Type.STRMSG== " + heatBeat.getBody().toString());
			channel.writeAndFlush(heatBeat);
			return true;
		}
		return false;
	}

	public static NettyLongConnect initLongConnect(MsgReceiveCallBack msgReceiveCB,
			NettyConnectCallBack nettyConnectCB) {
		final NettyLongConnect messageConnect = new NettyLongConnect(msgReceiveCB, nettyConnectCB);
		new Thread(new Runnable() {

			@Override
			public void run() {
				messageConnect.connect(NettyConstant.MSG_PORT, NettyConstant.MSG_IP);
			}
		}).start();

		return messageConnect;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

}
