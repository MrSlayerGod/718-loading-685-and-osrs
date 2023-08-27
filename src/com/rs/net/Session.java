package com.rs.net;

import java.net.InetSocketAddress;

import com.rs.game.player.Player;
import com.rs.grab.Grab;
import com.rs.io.OutputStream;
import com.rs.net.decoders.ClientPacketsDecoder;
import com.rs.net.decoders.Decoder;
import com.rs.net.decoders.GrabPacketsDecoder;
import com.rs.net.decoders.LoginPacketsDecoder;
import com.rs.net.decoders.WorldPacketsDecoder;
import com.rs.net.encoders.Encoder;
import com.rs.net.encoders.GrabPacketsEncoder;
import com.rs.net.encoders.LoginPacketsEncoder;
import com.rs.net.encoders.WorldPacketsEncoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class Session {

	private Channel channel;
	private Decoder decoder;
	private Encoder encoder;

	// hammershield mod
	protected int iplen = -1;
	protected byte[] ipdata = null;
	protected InetSocketAddress client = null;

	// ----------------

	public Session(Channel channel) {
		this.channel = channel;
		setDecoder(0);
	}

	public final ChannelFuture write(OutputStream outStream) {
		if (outStream == null || channel == null || !channel.isActive())
			return null;
		return channel.writeAndFlush(Unpooled.copiedBuffer(outStream.getBuffer(), 0, outStream.getOffset())).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
	}

	public final ChannelFuture write(ByteBuf outStream) {
		if (outStream == null || channel == null)
			return null;
		if (!channel.isActive()) {
			outStream.release();
			return null;
		}
		return channel.writeAndFlush(outStream).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
	}

	public final Channel getChannel() {
		return channel;
	}

	public final Decoder getDecoder() {
		return decoder;
	}

	public GrabPacketsDecoder getGrabPacketsDecoder() {
		return (GrabPacketsDecoder) decoder;
	}

	public final Encoder getEncoder() {
		return encoder;
	}

	public final void setDecoder(int stage) {
		setDecoder(stage, null);
	}

	public final void setDecoder(int stage, Object attachement) {
		switch (stage) {
		case 0:
			decoder = new ClientPacketsDecoder(this);
			break;
		case 1:
			decoder = new GrabPacketsDecoder(this, (Grab) attachement);
			break;
		case 2:
			decoder = new LoginPacketsDecoder(this);
			break;
		case 3:
			decoder = new WorldPacketsDecoder(this, (Player) attachement);
			break;
		case -1:
		default:
			decoder = null;
			break;
		}
	}

	public final void setEncoder(int stage) {
		setEncoder(stage, null);
	}

	public final void setEncoder(int stage, Object attachement) {
		switch (stage) {
		case 0:
			encoder = new GrabPacketsEncoder(this);
			break;
		case 1:
			encoder = new LoginPacketsEncoder(this);
			break;
		case 2:
			encoder = new WorldPacketsEncoder(this, (Player) attachement);
			break;
		case -1:
		default:
			encoder = null;
			break;
		}
	}

	public LoginPacketsEncoder getLoginPackets() {
		return (LoginPacketsEncoder) encoder;
	}

	public GrabPacketsEncoder getGrabPackets() {
		return (GrabPacketsEncoder) encoder;
	}

	public WorldPacketsEncoder getWorldPackets() {
		return (WorldPacketsEncoder) encoder;
	}

	public String getIP() {
		if (channel == null || !(channel.remoteAddress() instanceof InetSocketAddress))
			return "127.0.0.1";
		InetSocketAddress addr = (InetSocketAddress) channel.remoteAddress();
		return addr.getAddress().getHostAddress();
	}

	public String getLocalAddress() {
		return channel == null ? "127.0.0.1" : channel.localAddress().toString();
	}
}
