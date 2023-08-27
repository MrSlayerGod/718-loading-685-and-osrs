package com.rs;

import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import com.rs.discord.Bot;
import com.rs.executor.LoginExecutorManager;
import com.rs.login.GameWorld;
import com.rs.login.Login;
import com.rs.login.account.Account;
import com.rs.net.LoginServerChannelManager;
import com.rs.net.encoders.LoginChannelsPacketEncoder;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

public class LoginLauncher {

	/**
	 * Whether shutdown has been started.
	 */
	public static boolean shutdown;

	
	public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException {
		GameLauncher.skipSSLValidation();
		if (args.length < 2) {
			System.out.println("USE: debug(boolean) hosted(boolean)");
			return;
		}
		Settings.DEBUG = Boolean.parseBoolean(args[0]);
		Settings.HOSTED = Boolean.parseBoolean(args[1]);
		long currentTime = Utils.currentTimeMillis();

		if (!Settings.HOSTED) {
			Settings.LOGIN_SERVER_ADDRESS_BASE = new InetSocketAddress("127.0.0.1", 37777);
			Settings.LOGIN_CLIENT_ADDRESS_BASE = new InetSocketAddress("127.0.0.1", 37778);
		}

		Logger.log("Launcher", "Starting login core...");
		Login.init();
		Logger.log("Launcher", "Starting executors...");
		LoginExecutorManager.init();
		Logger.log("Launcher", "Initing Discord bot...");
		Bot.init(true);
		Logger.log("Launcher", "Initing Login Server Channel Manager...");
		try {
			LoginServerChannelManager.init();
		} catch (Throwable e) {
			Logger.handle(e);
			Logger.log("Launcher", "Failed initing Login Server Channel Manager. Shutting down...");
			System.exit(1);
			return;
		}
	/*	Logger.log("Launcher", "Initing Website Channels Manager...");
		try {
			WebsiteChannelsManager.init();
		} catch (Throwable e) {
			Logger.handle(e);
			Logger.log("Launcher", "Failed initing Website Channels Manager. Shutting down...");
			System.exit(1);
			return;
		}
		Logger.log("Launcher", "Initing Http Panel Channels Manager...");
		try {
			HttpPanelChannelsManager.init();
		} catch (Throwable e) {
			Logger.handle(e);
			Logger.log("Launcher", "Failed initing Http Panel Channels Manager. Shutting down...");
			System.exit(1);
			return;
		}*/

		Logger.log("Launcher", "Login server took " + (Utils.currentTimeMillis() - currentTime) + " milli seconds to launch.");

		GameWorld world = Login.getWorld(1);
		LoginServerChannelManager.sendReliablePacket(world, LoginChannelsPacketEncoder.encodeConsoleMessage("Hello there from login!").trim());
		//LoginServerChannelManager.sendReliablePacket(world, LoginChannelsPacketEncoder.encodePingPong(0).trim());

		Scanner scanner = new Scanner(System.in);
		for (String line = scanner.nextLine(); line != null; line = scanner.nextLine()) {
			try {
				handleCommand(line);
			} catch (Throwable t) {
				Logger.handle(t);
			}
		}
		scanner.close();

	}

	private static void handleCommand(String cmd) {
		if (cmd.startsWith("shutdown ")) {
			String[] args = cmd.split("\\s");
			if (args.length < 2) {
				Logger.log("Console", "Usage shutdown [delay]");
				return;
			}
			safeShutdown(Integer.parseInt(args[1]));
		} else if (cmd.startsWith("emergencyshutdown ")) {
			String[] args = cmd.split("\\s");
			if (args.length < 2) {
				Logger.log("Console", "Usage emergencyshutdown [maxlogoutcycles]");
				return;
			}
			emergencyShutdown(Integer.parseInt(args[1]));
		} else if (cmd.startsWith("jointest ")) {
			String name = cmd.substring(9);
			Account account = Login.findAccount(name);
			if (account != null) {
				Login.joinFriendsChat(account, Utils.formatPlayerNameForDisplay("mgi125"));
				Logger.log("Console", "Sent!");
			}
		} else if (cmd.startsWith("chattest ")) {
			String message = cmd.substring(9);
			Account account = Login.findAccount("mgi125");
			if (account != null) {
				Login.sendFriendsChatMessage(account, message);
				Logger.log("Console", "Sent!");
			}
		} else if (cmd.startsWith("freename ")) {
			String name = cmd.substring(9);
			Logger.log("Console", Login.removeDisplayNameUnsafe(name));
		} else if (cmd.startsWith("setnname ")) {
			String name = cmd.substring(9);
			Account acc = Login.findAccount(name);
			if (acc != null) {
				Login.changeDisplayName(acc, "Rnd" + Integer.toHexString(new Object().hashCode()));
			}
			Logger.log("Console", acc != null);
		} else {
			Logger.log("Console", "Unknown command!");
		}
	}

	/**
	 * Perform's safe shutdown of login server.
	 */
	public static void safeShutdown(int world_shutdown_delay) {
		if (shutdown)
			return;

		shutdown = true;
		Logger.log("Launcher", "Full safe shutdown has been started!");

		Logger.log("Launcher", "Sending shutdown requests to worlds...");
		for (int i = 0; i < Login.getWorldsSize(); i++) {
			GameWorld world = Login.getWorld(i);
			if (world == null)
				continue;

			LoginServerChannelManager.sendReliablePacket(world, LoginChannelsPacketEncoder.encodeWorldShutdownRequest(world_shutdown_delay).trim());
		}
		try {
			Thread.sleep(5000); //give time to save
		} catch (InterruptedException e) {
		}
		for (int cycle = 0;; cycle++) {
			int total = Login.getTotalOnline();
			Logger.log("Launcher", "Waiting for player logouts, remaining players:" + total + ", cycle #" + cycle);
			if (total <= 0)
				break;

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
		}

		Logger.log("Launcher", "Awaiting worker shutdown...");
		LoginExecutorManager.shutdown(true);
		
		//Logger.log("Launcher", "Awaiting for last packets to arrive to game servers...");
		//LoginServerChannelManager.awaitQueue();

		Logger.log("Launcher", "Shutting down network...");
	//	WebsiteChannelsManager.shutdown();
		LoginServerChannelManager.shutdown();
	//	HttpPanelChannelsManager.shutdown();

		Logger.log("Launcher", "Shutting down login core...");
		Login.shutdown();

		Logger.log("Launcher", "Done...");
		System.exit(0);
	}

	/**
	 * Perform's emergency shutdown of login server.
	 */
	public static void emergencyShutdown(int max_cycles) {
		if (shutdown)
			return;

		shutdown = true;
		Logger.log("Launcher", "Emergency shutdown has been started!");

		for (int cycle = 0; cycle < max_cycles; cycle++) {
			Logger.log("Launcher", "Requesting player logouts, cycle #" + cycle);
			for (int i = 0; i < Login.getWorldsSize(); i++) {
				GameWorld world = Login.getWorld(i);
				if (world == null)
					continue;

				for (Account account : world.getAllOnlineAccountsCopy())
					LoginServerChannelManager.sendReliablePacket(world, LoginChannelsPacketEncoder.encodeLogoutRequest(account.getUsername(), true).trim());
			}

			int total = Login.getTotalOnline();
			Logger.log("Launcher", "Requests sent, online players:" + total);
			if (total <= 0)
				break;

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
		}

		Logger.log("Launcher", "Awaiting worker shutdown...");
		LoginExecutorManager.shutdown(true);

		Logger.log("Launcher", "Shutting down network...");
		/*if (Settings.HOSTED)
			WebsiteChannelsManager.shutdown();*/
		LoginServerChannelManager.shutdown();

		Logger.log("Launcher", "Shutting down login core...");
		Login.shutdown();

		Logger.log("Launcher", "Done...");
		System.exit(0);
	}

}
