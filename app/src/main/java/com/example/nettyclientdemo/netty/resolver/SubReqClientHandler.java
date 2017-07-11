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

import com.example.nettyclientdemo.netty.NettyLongConnect;
import com.example.nettyclientdemo.netty.utils.MsgContent;
import com.example.nettyclientdemo.netty.utils.MsgReceiveCallBack;
import com.example.nettyclientdemo.netty.utils.NettyUtils;
import com.example.nettyclientdemo.netty.utils.Utils;

import client.ProtobufProto;
import client.ProtobufProto.ProtobufMessage.Type;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * @author lilinfeng
 * @date 2014年2月14日
 * @version 1.0
 */
public class SubReqClientHandler extends ChannelInboundHandlerAdapter {
	MsgReceiveCallBack msgReceiveCB;
	NettyLongConnect connect;

	public SubReqClientHandler(MsgReceiveCallBack msgCallBack, NettyLongConnect connect) {
		super();
		this.msgReceiveCB = msgCallBack;
		this.connect = connect;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("消息==接收===============" + Thread.currentThread().getName());
		try {
			ProtobufProto.ProtobufMessage message = (ProtobufProto.ProtobufMessage) msg;
			if (message.getReqType() != null) {
				switch (message.getReqType()) {
				case STRMSG:
					// 把消息回调给应用
					msgReceiveCB.onReceiveTabletMessage(NettyUtils.getMsgContent(message));
					break;
				case FILE:
					// 存储图片
					byte[] messageBytes = message.getBody().toByteArray();
					String serverMD5 = message.getMd5();
					String clientMD5 = Utils.doMD5(messageBytes);
					MsgContent msgContent = new MsgContent();

					if (serverMD5.equals(clientMD5)) {
						NettyUtils.byte2image(messageBytes, message.getParam());
						MsgContent msg1=new MsgContent();
						msg1.setType(Type.FILE);
						msgReceiveCB.onReceiveTabletMessage(msg1);
						msgContent.setResult(true);
					} else {
						msgContent.setResult(false);
					}
					connect.sendMsg(msgContent);
					break;
				default:
					break;
				}
			}

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
}
