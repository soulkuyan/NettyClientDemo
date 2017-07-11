/*
 * Copyright 2015 The Netty Project
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

import com.google.protobuf.CodedOutputStream;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * An encoder that prepends the the Google Protocol Buffers <a href=
 * "http://code.google.com/apis/protocolbuffers/docs/encoding.html#varints">Base
 * 128 Varints</a> integer length field. For example:
 * 
 * <pre>
 * BEFORE ENCODE (300 bytes)       AFTER ENCODE (302 bytes)
 * +---------------+               +--------+---------------+
 * | Protobuf Data |-------------->| Length | Protobuf Data |
 * |  (300 bytes)  |               | 0xAC02 |  (300 bytes)  |
 * +---------------+               +--------+---------------+
 * </pre>
 * 
 * *
 *
 * @see {@link CodedOutputStream} or (@link CodedOutputByteBufferNano)
 */
@Sharable
public class HeaderLengthFrameEncoder extends MessageToByteEncoder<ByteBuf> {

	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
		System.out.println("发送数据了");
		int bodyLen = msg.readableBytes();
		//bodyLen=180348;
		// byte[] bytes = { 124, -64, 2, 0 };
		// int data[] = new int[bytes.length];
		// for (int i = 0; i < bytes.length; i++) {
		// data[i] = bytes[i] & 0xff;
		// }
		// System.out.println(Arrays.toString(data));
		out.ensureWritable(4 + bodyLen);
		// out.writeChar(data[0]);
		// out.writeChar(data[1]);
		// out.writeChar(data[2]);
		// out.writeChar(data[3]);
		// out.writeInt(180348);
		// out.writeByte(data[0]);
		// out.writeByte(data[1]);
		// out.writeByte(data[2]);
		// out.writeByte(data[3]);
		 //out.writeInt(bodyLen);
		setBodyLen(out, bodyLen);
		out.writeBytes(msg, msg.readerIndex(), bodyLen);
	}

	private void setBodyLen(ByteBuf out, int value) {
		// byte[] bytes = intToBytes(len);
		// int data[] = new int[bytes.length];
		// for (int i = 0; i < bytes.length; i++) {
		// data[i] = bytes[i] & 0xff;
		// out.writeByte(data[i]);
		// }

		out.writeByte((byte) value);
		out.writeByte((byte) (value >>> 8));

		out.writeByte((byte) (value >>> 16));
		out.writeByte((byte) (value >>> 24));

	}

	public static byte[] intToBytes(int value) {
		byte[] src = new byte[4];
		src[3] = (byte) ((value >> 24) & 0xFF);
		src[2] = (byte) ((value >> 16) & 0xFF);
		src[1] = (byte) ((value >> 8) & 0xFF);
		src[0] = (byte) (value & 0xFF);
		return src;
	}

}
