/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.example.nettyclientdemo.netty.resolver;

import com.example.nettyclientdemo.netty.utils.MsgContent;
import com.example.nettyclientdemo.netty.utils.NettyUtils;
import com.example.nettyclientdemo.netty.utils.Utils;

import java.util.concurrent.CountDownLatch;

import client.ProtobufProto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * @author lilinfeng
 * @date 2014年2月14日
 * @version 1.0
 */
public class FileClientHandler extends ChannelInboundHandlerAdapter {
	CountDownLatch lathc;
	MsgContent result;

	public FileClientHandler(CountDownLatch lathc) {
		super();
		this.lathc = lathc;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
	}

	public ChannelHandlerContext ctx;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		ProtobufProto.ProtobufMessage message = (ProtobufProto.ProtobufMessage) msg;
		System.out.println("====FileClientHandler文件接收=======" + Thread.currentThread().getName());
		try {
			result = NettyUtils.getMsgContent(message);
			lathc.countDown();// 消息收取完毕后释放同步锁
		} finally {
			ReferenceCountUtil.release(msg); // (2)
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	public MsgContent getResult() {
		return result;
	}

	public void setResult(MsgContent result) {
		this.result = result;
	}

	public CountDownLatch getLathc() {
		return lathc;
	}

	public void setLathc(CountDownLatch lathc) {
		this.lathc = lathc;
	}

}
