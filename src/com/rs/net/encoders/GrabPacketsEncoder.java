package com.rs.net.encoders;

import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.io.OutputStream;
import com.rs.net.Session;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Future;

public final class GrabPacketsEncoder extends Encoder {
	
	private static byte[] F255_255;
	
	private int encryptionValue;

	public GrabPacketsEncoder(Session connection) {
		super(connection);
	}

	public final void sendOutdatedClientPacket() {
		OutputStream stream = new OutputStream(1);
		stream.writeByte(6);
		ChannelFuture future = session.write(stream);
		if (future != null)
			future.addListener(ChannelFutureListener.CLOSE);
		else
			if (session.getChannel() != null)
				session.getChannel().close();
	}

	public final void sendStartUpPacket() {
		OutputStream stream = new OutputStream(
				1 + Settings.GRAB_SERVER_KEYS.length * 4);
		stream.writeByte(0);
		for (int key : Settings.GRAB_SERVER_KEYS)
			stream.writeInt(key);
		session.write(stream);
	}
	
	public Future sendArchive(int folderID, int archiveID, boolean priority, int encryptionValue) {
		byte[] data = getArchive(folderID, archiveID);
		ByteBuf buffer = Unpooled.buffer();
		buffer.writeByte(folderID);
		buffer.writeInt(archiveID);
		buffer.writeByte(data[0] | (priority ? 0 : 0x80));
		int dataLength = data.length;
		if(folderID != 255) //apparently you're not supposed to send revision
			dataLength -= 2;
	
		for(int i = 1; i < dataLength;) {
			int block = 512 - (buffer.writerIndex() & 511);
			if(block == 512) {
				buffer.writeByte(-1);
				block--;
			}
			if(block + i > dataLength) //read remaining
				block = dataLength - i;
			buffer.writeBytes(data, i, block);
			i += block;
		}
		if (encryptionValue != 0) {
			for (int i = 0; i < buffer.writerIndex(); i++)
				buffer.setByte(i, buffer.getByte(i) ^ encryptionValue);
		}
		return session.write(buffer);
	}
	
	private static byte[] getArchive255_255() {
		if (F255_255 == null) {
			byte[] file = Cache.generateUkeysFile();
			OutputStream stream = new OutputStream();
			stream.writeByte(0);
			stream.writeInt(file.length);
			stream.writeBytes(file);
			byte[] data = new byte[stream.getOffset()];
			stream.setOffset(0);
			stream.getBytes(data, 0, data.length);
			F255_255 = data;
		}
		return F255_255;
	}
	
	private static byte[] getArchive(int index, int id) {
		return (index == 255 && id == 255) ? getArchive255_255() : (index == 255 ? Cache.STORE.getIndex255() : Cache.STORE.getIndexes()[index].getMainFile()).getArchiveData(id);
	}
}