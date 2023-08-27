package com.rs.net.decoders;

import com.rs.grab.Grab;
import com.rs.io.InputStream;
import com.rs.net.Session;

public final class GrabPacketsDecoder extends Decoder {
	
	private Grab grab;

	public GrabPacketsDecoder(Session connection, Grab attachment) {
		super(connection);
		grab = attachment;
	}

	@Override
	public final int decode(InputStream stream) {	
		while (stream.getRemaining() >= 6 && session.getChannel() != null && session.getChannel().isActive()) {
			int packetId = stream.readUnsignedByte();
			switch (packetId) {
				case 0:
				case 1:
					grab.requestArchive(stream.readUnsignedByte(), stream.readInt(), packetId == 1);
					break;
				case 2:
				case 3:
					grab.setLoggedIn(packetId == 2);
					stream.skip(5);
					break;
				case 4:
					grab.setEncryptionValue(stream.readUnsignedByte());
					stream.skip(4);
					break;
				case 6:
					grab.init();
					stream.skip(5);
					break;
				case 7:
				default:
					grab.finish();
					stream.skip(5);
					break;
			}
		}
		return stream.getOffset();
	}
}
