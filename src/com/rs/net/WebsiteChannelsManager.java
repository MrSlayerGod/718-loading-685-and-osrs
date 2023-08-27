package com.rs.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.rs.Settings;
import com.rs.utils.Logger;

public class WebsiteChannelsManager {
	/**
	 * Our socket.
	 */
	private static ServerSocket socket;
	/**
	 * Acceptor thread.
	 */
	private static Thread acceptor;
	/**
	 * Processor thread.
	 */
	private static Thread processor;
	/**
	 * Contains list of clients.
	 */
	private static List<WebsiteClient> clients;

	public static void init() throws IOException {
		socket = new ServerSocket();
		socket.bind(Settings.WEBSITE_LISTENER_ADDRESS);
		clients = new ArrayList<WebsiteClient>();
		acceptor = new Thread("website manager[acceptor]") {
			@Override
			public void run() {
				runAcceptor();
			}
		};
		processor = new Thread("website manager[processor]") {
			@Override
			public void run() {
				runProcessor();
			}
		};

		acceptor.start();
		processor.start();
	}

	@SuppressWarnings("deprecation")
	public static void shutdown() {
		try {
			acceptor.stop();
		} catch (Throwable t) {
		}
		acceptor = null;
		try {
			processor.stop();
		} catch (Throwable t) {
		}
		processor = null;
		synchronized (clients) {
			for (WebsiteClient client : clients)
				client.disconnect();
			clients.clear();
			clients = null;
		}
		try {
			socket.close();
		} catch (Throwable t) {
		}
		socket = null;

	}

	/**
	 * Run's acceptor thread.
	 */
	private static void runAcceptor() {
		do {
			try {
				Socket s = socket.accept();
				if (s == null)
					break;
				if (Settings.DEBUG)
					Logger.log(WebsiteChannelsManager.class, "Accepted website connection from:" + s.getRemoteSocketAddress());
				s.setSoTimeout(Settings.READ_TIMEOUT * 1000);
				SocketConnection connection = new SocketConnection();
				connection.connect(s);

				synchronized (clients) {
					clients.add(new WebsiteClient(connection));
				}
			} catch (ThreadDeath death) {
				// we are getting killed by shutdown()
				throw death;
			} catch (Throwable t) {
				Logger.handle(t);
			}
		} while (true);
	}

	/**
	 * Run's processor thread.
	 */
	private static void runProcessor() {
		List<WebsiteClient> list = null;

		while (true) {
			try {
				synchronized (clients) {
					list = new ArrayList<WebsiteClient>(clients);
				}

				for (WebsiteClient client : list) {
					try {
						client.process();
					} catch (Throwable t) {
						client.disconnect();
						throw t;
					}

					if (!client.isAlive()) {
						synchronized (clients) {
							clients.remove(client);
						}
					}
				}

				try {
					Thread.sleep(20);
				} catch (InterruptedException t) {
				}
			} catch (ThreadDeath death) {
				// we are getting killed by shutdown()
				throw death;
			} catch (Throwable t) {
				Logger.handle(t);
			}
		}
	}

}
