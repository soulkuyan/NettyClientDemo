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

import com.example.nettyclientdemo.netty.resolver.FileClientHandler;
import com.example.nettyclientdemo.netty.resolver.HeaderLengthFrameDecoder;
import com.example.nettyclientdemo.netty.resolver.HeaderLengthFrameEncoder;
import com.example.nettyclientdemo.netty.utils.MsgContent;
import com.example.nettyclientdemo.netty.utils.NettyConstant;
import com.example.nettyclientdemo.netty.utils.NettyUtils;
import com.example.nettyclientdemo.netty.utils.Utils;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import client.ProtobufProto;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

/**
 * @author Lilinfeng
 * @date 2014年3月15日
 * @version 1.0
 */
public class NettyShortConnect {
	private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

	Channel channel;
	boolean connected = false;
	// callback
	FileClientHandler handler;
	Bootstrap b;
	int port;
	String host;

	public NettyShortConnect() {
		super();
	}

	public Boolean connect(final int port, final String host, MsgContent msg) {
		Boolean value = false;
		// 配置客户端NIO线程组
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			CountDownLatch lathc = new CountDownLatch(1);
			handler = new FileClientHandler(lathc);
			b = new Bootstrap();
			b.group(group)//
					.channel(NioSocketChannel.class)//
//2017.6.5 by george for Android 4.0上传图片失败问题
					.option(ChannelOption.TCP_NODELAY, true)
//					.option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<SocketChannel>() {
					.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000).handler(new ChannelInitializer<SocketChannel>() {
//2017.6.5 by george for Android 4.0上传图片失败问题
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(new HeaderLengthFrameDecoder());
							ch.pipeline()
									.addLast(new ProtobufDecoder(ProtobufProto.ProtobufMessage.getDefaultInstance()));
							ch.pipeline().addLast(new HeaderLengthFrameEncoder());
							ch.pipeline().addLast(new ProtobufEncoder());
							// 增加了读超时handler
							ch.pipeline().addLast(handler);
						}
					});
			// 发起异步连接操作
//2017.6.5 by george for Android 4.0上传图片失败问题
//			ChannelFuture future = b.connect(new InetSocketAddress(host, port));
//			channel = future.sync().channel();
			if (channel != null && channel.isOpen()){
				channel.close();
			}
			this.host = host;
			this.port = port;
			ChannelFuture future = b.connect(new InetSocketAddress(host, port));
			channel = future.sync().channel();
			future.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture channelFuture) throws Exception {
					if (!channelFuture.isSuccess()){
						System.err.println("Connect to host error: " + channelFuture.cause());
						channel = b.connect(new InetSocketAddress(host, port)).sync().channel();
					}
				}
			});
//2017.6.5 by george for Android 4.0上传图片失败问题
			setConnected(true);
			sendMsg(msg);
			lathc.await(60 * 1000, TimeUnit.MILLISECONDS);// 开启等待会等待服务器返回结果之后再执行下面的代码
			MsgContent result = handler.getResult();
			value = ((result == null) ? false : result.getResult());
			// 等待客户端链路关闭
			// future.channel().closeFuture().sync();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("===============打印错误日志了");
			value = false;
		} finally {
			// 优雅退出，释放NIO线程组
			group.shutdownGracefully();
			setConnected(false);
		}
		return value;
	}

	public Boolean sendMsg(MsgContent msg) {
		ProtobufProto.ProtobufMessage heatBeat = NettyUtils.buildMsg(ProtobufProto.ProtobufMessage.Type.FILE, msg);
		System.out.println("====FileClientHandler文件发送=======" + Thread.currentThread().getName());
		channel.writeAndFlush(heatBeat);
		return true;
	}

	CountDownLatch lathc;

	/**
	 * 1.建立连接 2.发送数据 3.等待数据 4.关闭连接
	 */
	public Boolean sendFile(MsgContent msg) {
		return connect(NettyConstant.FILE_PORT, NettyConstant.FILE_IP, msg);
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

}
