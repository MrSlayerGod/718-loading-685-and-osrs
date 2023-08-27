package com.rs.net.decoders;

import com.rs.Settings;
import com.rs.grab.Grab;
import com.rs.io.InputStream;
import com.rs.net.Session;
import com.rs.utils.Logger;

public final class ClientPacketsDecoder extends Decoder {

	public ClientPacketsDecoder(Session connection) {
		super(connection);
	}

	@Override
	public final int decode(InputStream stream) {
		session.setDecoder(-1);
		int packetId = stream.readUnsignedByte();
		switch (packetId) {
		case 14:
			return decodeLogin(stream);
		case 15:
			return decodeGrab(stream);
		default:
			if (Settings.DEBUG)
				Logger.log(this, "PacketId " + packetId);
			if (session.getChannel() != null)
				session.getChannel().close();
			return -1;
		}
	}

	private final int decodeLogin(InputStream stream) {
		if (stream.getRemaining() != 0) {
			if (session.getChannel() != null)
				session.getChannel().close();
			return -1;
		}
		session.setDecoder(2);
		session.setEncoder(1);
		session.getLoginPackets().sendStartUpPacket();
		return stream.getOffset();
	}

	private final int decodeGrab(InputStream stream) {
		int size = stream.readUnsignedByte();
		if (stream.getRemaining() < size) {
			if (session.getChannel() != null)
				session.getChannel().close();
			return -1;
		}
		session.setEncoder(0);
		//TODO Our matrix has these swapped, fix the order later on.
		if (stream.readInt() != Settings.CLIENT_BUILD || stream.readInt() != Settings.CUSTOM_CLIENT_BUILD) {
			session.setDecoder(-1);
			session.getGrabPackets().sendOutdatedClientPacket();
			return -1;
		}
		if (!stream.readString().equals(Settings.GRAB_SERVER_TOKEN)) {
			if (session.getChannel() != null)
				session.getChannel().close();
			return -1;
		}
		session.setDecoder(1, new Grab(session));
		session.getGrabPackets().sendStartUpPacket();
		return stream.getOffset();
	}
}
