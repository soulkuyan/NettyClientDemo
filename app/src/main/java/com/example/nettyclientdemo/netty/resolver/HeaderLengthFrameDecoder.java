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

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

public class HeaderLengthFrameDecoder extends ByteToMessageDecoder {

	// TODO maxFrameLength + safe skip + fail-fast option
	// (just like LengthFieldBasedFrameDecoder)

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		System.out.println("接收到数据了");
		in.markReaderIndex();
		int preIndex = in.readerIndex();
		int length = getBodyLen(in);
		// int length2 = in.readInt();
		// int length = readRawVarint32(in);
		if (preIndex == in.readerIndex()) {
			return;
		}
		if (length < 0) {
			throw new CorruptedFrameException("negative length: " + length);
		}

		if (in.readableBytes() < length) {
			in.resetReaderIndex();
		} else {
			out.add(in.readRetainedSlice(length));
		}
		
	}

	private int getBodyLen(ByteBuf in) {
		byte[] array = new byte[4];
		for (int i = 0; i < 4; i++) {
			array[i] = in.readByte();
		}
		return bytesToInt2(array);
	}

	public static int bytesToInt2(byte[] src) {
		int value;
		int offset = 0;
		value = (int) (((src[offset + 3] & 0xFF) << 24) | ((src[offset + 2] & 0xFF) << 16)
				| ((src[offset + 1] & 0xFF) << 8) | (src[offset] & 0xFF));
		return value;
	}
}
