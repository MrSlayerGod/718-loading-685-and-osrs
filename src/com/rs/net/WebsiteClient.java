package com.rs.net;

import com.rs.Settings;
import com.rs.login.Login;
import com.rs.net.decoders.WebsitePacketsDecoder;
import com.rs.utils.Logger;

public class WebsiteClient {

	/**
	 * Connection instance for this client.
	 */
	private SocketConnection connection;
	/**
	 * Buffer that is used to process data.
	 */
	private byte[] buffer;

	/**
	 * Whether this client was 'accepted', which means that the password was ok.
	 */
	private boolean accepted;

	public WebsiteClient(SocketConnection connection) {
		this.connection = connection;
		this.buffer = new byte[65535];
	}

	public void process() {
		if (!isAlive()) {
			return;
		} else if (connection.isDead()) {
			connection.disconnect();
			connection = null;
			return;
		}

		main: for (int amt = connection.peek(buffer, 0, buffer.length); amt > 0; amt = connection.peek(buffer, 0, buffer.length)) {
			for (int i = 0; i < amt; i++) {
				if (buffer[i] == '\n') {
					StringBuilder msg = new StringBuilder(i);
					for (int a = 0; a < i; a++)
						msg.append((char) buffer[a]);
					processMessage(msg.toString());
					if (!isAlive())
						break main;

					connection.discardBuffer(i + 1);
					continue main;
				}
			}
			break;
		}

	}

	private void processMessage(String msg) {
		if (!accepted && !msg.startsWith("PSWD:")) {
			if (Settings.DEBUG)
				Logger.log(WebsiteClient.class, "No password, received (" + msg + ") instead.");
			connection.disconnect();
			connection = null;
			return;
		} else if (!accepted && !msg.substring(5).equals(Settings.WEBSITE_CLIENT_PASSWORD)) {
			if (Settings.DEBUG)
				Logger.log(WebsiteClient.class, "Invalid password.");
			connection.disconnect();
			connection = null;
			return;
		} else if (!accepted) {
			if (Settings.DEBUG)
				Logger.log(WebsiteClient.class, "Website client accepted.");
			accepted = true;
			return;
		}

		try {
			Login.getDecoderLock().lock();
			WebsitePacketsDecoder.decodeIncomingMessage(this, msg);
		} finally {
			Login.getDecoderLock().unlock();
		}
	}

	public void sendMessage(String msg) {
		byte[] buffer = new byte[msg.length() + 1];
		for (int i = 0; i < msg.length(); i++)
			buffer[i] = (byte) msg.charAt(i);
		buffer[buffer.length - 1] = '\n';
		connection.write(buffer, 0, buffer.length);
	}

	public boolean isAlive() {
		return connection != null;
	}

	public void disconnect() {
		if (connection != null) {
			connection.disconnect();
			connection = null;
		}
	}

}
