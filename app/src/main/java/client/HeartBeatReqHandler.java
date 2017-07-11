package client;

import com.example.nettyclientdemo.netty.utils.NettyUtils;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author Lilinfeng
 * @date 2014年3月15日
 * @version 1.0
 */
public class HeartBeatReqHandler extends ChannelInboundHandlerAdapter {

	private volatile ScheduledFuture<?> heartBeat;
	ChannelHandlerContext ctx;

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		this.ctx = ctx;
		if (heartBeat != null) {
			heartBeat.cancel(true);
			heartBeat = null;
		}
		heartBeat = ctx.executor().scheduleAtFixedRate(new HeartBeatReqHandler.HeartBeatTask(ctx), 0, 10 * 1000,
				TimeUnit.MILLISECONDS);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ProtobufProto.ProtobufMessage message = (ProtobufProto.ProtobufMessage) msg;
		// 握手成功，主动发送心跳消息
		if (message.getReqType() != null && message.getReqType() == ProtobufProto.ProtobufMessage.Type.HEARTBEAT) {
			System.out.println("心跳==接收==============="+Thread.currentThread().getName());
		} else {
			ctx.fireChannelRead(msg);
		}
	}

	private class HeartBeatTask implements Runnable {
		private final ChannelHandlerContext ctx;

		public HeartBeatTask(final ChannelHandlerContext ctx) {
			this.ctx = ctx;
		}

		@Override
		public void run() {
			sendHeatBeat();
		}

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		if (heartBeat != null) {
			heartBeat.cancel(true);
			heartBeat = null;
		}
		// TODO
		ctx.fireExceptionCaught(cause);
	}

	public void sendHeatBeat() {
		System.out.println("心跳==发送==============="+Thread.currentThread().getName());
		ProtobufProto.ProtobufMessage heatBeat = NettyUtils.buildMsg(ProtobufProto.ProtobufMessage.Type.HEARTBEAT,
				null);
		ctx.writeAndFlush(heatBeat);
	}

	public ScheduledFuture<?> getHeartBeat() {
		return heartBeat;
	}

	public void setHeartBeat(ScheduledFuture<?> heartBeat) {
		this.heartBeat = heartBeat;
	}
	
}
