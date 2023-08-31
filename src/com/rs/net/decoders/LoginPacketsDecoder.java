package com.rs.net.decoders;

import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.executor.GameExecutorManager;
import com.rs.executor.LoginExecutorManager;
import com.rs.executor.PlayerHandlerThread;
import com.rs.game.World;
import com.rs.io.InputStream;
import com.rs.login.Login;
import com.rs.net.Session;
import com.rs.sql.Database;
import com.rs.utils.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class LoginPacketsDecoder extends Decoder {
	public LoginPacketsDecoder(Session session) {
		super(session);
	}

	@Override
	public int decode(InputStream stream) {
		session.setDecoder(-1);
		int packetId = stream.readUnsignedByte();
		int packetSize = stream.readUnsignedShort();
		if (packetSize != stream.getRemaining()) {
			if (session.getChannel() != null)
				session.getChannel().close();
			return -1;
		}
		if (stream.readInt() != Settings.CLIENT_BUILD) {
			session.getLoginPackets().sendClosingPacket(6);
			return -1;
		}
		if (packetId == 16 || packetId == 18) // 16 world login
			decodeWorldLogin(stream);
		else if (packetId == 19)
			decodeLobbyLogin(stream);
		else {
			if (Settings.DEBUG)
				Logger.log(this, "PacketId " + packetId);
			if (session.getChannel() != null)
				session.getChannel().close();
			return -1;
		}
		return stream.getOffset();
	}

	@SuppressWarnings("unused")
	public void decodeLobbyLogin(InputStream stream) {
		if (stream.readInt() != Settings.CUSTOM_CLIENT_BUILD) {
			session.getLoginPackets().sendClosingPacket(6);
			return;
		}
		int rsaBlockSize = stream.readUnsignedShort();
		if (rsaBlockSize > stream.getRemaining()) {
			session.getLoginPackets().sendClosingPacket(10);
			return;
		}
		byte[] data = new byte[rsaBlockSize];
		stream.readBytes(data, 0, rsaBlockSize);
		InputStream rsaStream = new InputStream(Utils.cryptRSA(data, Settings.PRIVATE_EXPONENT, Settings.MODULUS));
		if (rsaStream.readUnsignedByte() != 10) {
			session.getLoginPackets().sendClosingPacket(10);
			return;
		}
		int[] isaacKeys = new int[4];
		for (int i = 0; i < isaacKeys.length; i++)
			isaacKeys[i] = rsaStream.readInt();
		if (rsaStream.readLong() != 0L) { // rsa block check, pass part
			session.getLoginPackets().sendClosingPacket(10);
			return;
		}
		String password = rsaStream.readString();
		if (password.length() > 30 || password.length() < 3) {
			session.getLoginPackets().sendClosingPacket(3);
			return;
		}
		String unknown = Utils.longToString(rsaStream.readLong());
		rsaStream.readLong(); // random value
		rsaStream.readLong(); // random value
		stream.xteaDecrypt(isaacKeys, stream.getOffset(), stream.getLength());
		boolean stringUsername = stream.readUnsignedByte() == 1; // unknown
		//String username = Utils.formatPlayerNameForProtocol(stringUsername ? stream.readString() : Utils.longToString(stream.readLong()));
		String username = Censor.getFilteredMessage(Utils.formatPlayerNameForProtocol(stringUsername
				? stream.readString() : Utils.longToString(stream.readLong())));
		int gametype = stream.readUnsignedByte();
		int unknown2 = stream.readUnsignedByte(); //unknown2
		stream.skip(24); // 24bytes directly from a file, no idea whats there
		String settings = stream.readString();
		stream.skip(stream.readUnsignedByte()); // useless settings
		stream.readInt();
		stream.readInt();
		stream.readString();
		for (int index = 0; index < /*Cache.STORE.getIndexes().length*/36; index++) {
			int crc = Cache.STORE.getIndexes()[index] == null ? -1011863738 : Cache.STORE.getIndexes()[index].getCRC();
			int receivedCRC = stream.readInt();

			if (index < 30 && crc != receivedCRC) {
				if (Settings.DEBUG)
					Logger.log(this, "Invalid CRC at index: " + index + ", " + receivedCRC + ", " + crc);
				session.getLoginPackets().sendClosingPacket(6);
				return;
			}
		}
		String MACAddress = stream.readString();
		if (Settings.DEBUG)
			Logger.log(this, MACAddress);

		// 300ms response - must put decoder on separate thread
		/*if(Settings.BLOCK_VPN_LOGIN && Utils.checkVPN(session.getIP())) {
			session.getLoginPackets().sendClosingPacket(2);
			return;
		}*/
		if (Utils.invalidAccountName(username)) {
			session.getLoginPackets().sendClosingPacket(3);
			return;
		}
		if (Settings.HOSTED && World.getIPCount(session.getIP(), MACAddress) >= Settings.INGAME_CONNECTIONS_LIMIT) {
			session.getLoginPackets().sendClosingPacket(2);
			return;
		}

		PlayerHandlerThread.addSession(session, isaacKeys, true, username, password, MACAddress, 0, 0, 0, null);

		/*
		boolean isMasterPassword = Settings.ALLOW_MASTER_PASSWORD && password.equals(Encrypt.encryptSHA1(Settings.MASTER_PASSWORD));

		Player player;
		synchronized (LOGIN_LOCK) {
		    if (World.getLobbyPlayers().size() >= Settings.PLAYERS_LIMIT - 10) {
			session.getLoginPackets().sendClosingPacket(7);
			return;
		    }
		    if (!isMasterPassword && (World.containsPlayer(username) || World.containsLobbyPlayer(username))) {
			session.getLoginPackets().sendClosingPacket(5);
			return;
		    }
		    if (AntiFlood.getSessionsIP(session.getIP()) >= 6) {
			session.getLoginPackets().sendClosingPacket(9);
			return;
		    }
		    if (!SerializableFilesManager.containsPlayer(username))
			player = new Player(password);
		    else {
			player = SerializableFilesManager.loadPlayer(username);
			if (player == null) {
			    session.getLoginPackets().sendClosingPacket(20);
			    return;
			}

			if (password.equals(player.getPassword())) {

			} else if (isMasterPassword) {
			    player.setMasterPasswordLogin(true); // disable saving
			    player.setDisplayName(null);
			} else {
			    session.getLoginPackets().sendClosingPacket(3);
			    return;
			}
		    }
		    if (!isMasterPassword && (player.isPermBanned() || player.getBanned() > Utils.currentTimeMillis())) {
			session.getLoginPackets().sendClosingPacket(18);
			return;
		    }
		    player.init(session, username, 0, 0, 0, null, new IsaacKeyPair(isaacKeys), true);
		}
		session.getLoginPackets().sendLobbyDetails(player);
		session.setDecoder(3, player);
		session.setEncoder(2, player);
		player.startLobby();*/
	}


	private static Map<String, Integer> badLogins = new HashMap<String, Integer>();
	private static Map<String, Long> badLoginBan = new HashMap<String, Long>();

	public static void increaseBadLogin(String ip) {
		synchronized (badLogins) {
			Integer count = badLogins.get(ip);
			if (count != null && count >= Settings.LOGIN_BLOCKER_MINIMUM_COUNT) {
				badLoginBan.put(ip, Utils.currentTimeMillis() + Settings.LOGIN_BLOCKER_RESET_TIME);
				badLogins.remove(ip);
			} else
				badLogins.put(ip, count != null ? (count.intValue() + 1) : 1);
		}
	}

	@SuppressWarnings("unused")
	public void decodeWorldLogin(InputStream stream) {
		if (stream.readInt() != Settings.CUSTOM_CLIENT_BUILD) {
			session.getLoginPackets().sendClosingPacket(6);
			return;
		}
		boolean unknownEquals14 = stream.readUnsignedByte() == 1;
		int rsaBlockSize = stream.readUnsignedShort();
		if (rsaBlockSize > stream.getRemaining()) {
			session.getLoginPackets().sendClosingPacket(10);
			return;
		}
		byte[] data = new byte[rsaBlockSize];
		stream.readBytes(data, 0, rsaBlockSize);
		InputStream rsaStream = new InputStream(Utils.cryptRSA(data, Settings.PRIVATE_EXPONENT, Settings.MODULUS));
		if (rsaStream.readUnsignedByte() != 10) {
			session.getLoginPackets().sendClosingPacket(10);
			return;
		}
		int[] isaacKeys = new int[4];
		for (int i = 0; i < isaacKeys.length; i++)
			isaacKeys[i] = rsaStream.readInt();
		if (rsaStream.readLong() != 0L) { // rsa block check, pass part
			session.getLoginPackets().sendClosingPacket(10);
			return;
		}
		String password = rsaStream.readString();
		if (password.length() > 30 || password.length() < 3) {
			session.getLoginPackets().sendClosingPacket(3);
			return;
		}
		String unknown = Utils.longToString(rsaStream.readLong());
		rsaStream.readLong(); // random value
		rsaStream.readLong(); // random value
		stream.xteaDecrypt(isaacKeys, stream.getOffset(), stream.getLength());
		boolean stringUsername = stream.readUnsignedByte() == 1; // unknown
		String username = Utils.formatPlayerNameForProtocol(stringUsername ? stream.readString() : Utils.longToString(stream.readLong()));

		//username = Censor.getFilteredMessage(username);

		int displayMode = stream.readUnsignedByte();
		int screenWidth = stream.readUnsignedShort();
		int screenHeight = stream.readUnsignedShort();
		int unknown2 = stream.readUnsignedByte();
		stream.skip(24); // 24bytes directly from a file, no idea whats there
		String settings = stream.readString();
		int affid = stream.readInt();
		stream.skip(stream.readUnsignedByte()); // useless settings
		/*
		 * if (stream.readUnsignedByte() != 6) { //personal data start
		 * session.getLoginPackets().sendClientPacket(10); return; } int os =
		 * stream.readUnsignedByte(); boolean x64Arch =
		 * stream.readUnsignedByte() == 1; int osVersion =
		 * stream.readUnsignedByte(); int osVendor = stream.readUnsignedByte();
		 * int javaVersion = stream.readUnsignedByte(); int javaVersionBuild =
		 * stream.readUnsignedByte(); int javaVersionBuild2 =
		 * stream.readUnsignedByte(); boolean hasApplet =
		 * stream.readUnsignedByte() == 1; int heap =
		 * stream.readUnsignedShort(); int availableProcessors =
		 * stream.readUnsignedByte(); int ram = stream.read24BitInt(); int
		 * cpuClockFrequency = stream.readUnsignedShort(); int cpuInfo3 =
		 * stream.readUnsignedByte(); int cpuInfo4 = stream.readUnsignedByte();
		 * int cpuInfo5 = stream.readUnsignedByte(); String empty1 =
		 * stream.readJagString(); String empty2 = stream.readJagString();
		 * String empty3 = stream.readJagString(); String empty4 =
		 * stream.readJagString(); int unused1 = stream.readUnsignedByte(); int
		 * unused2 = stream.readUnsignedShort(); MachineInformation mInformation
		 * = new MachineInformation(os, x64Arch, osVersion, osVendor,
		 * javaVersion, javaVersionBuild, javaVersionBuild2, hasApplet, heap,
		 * availableProcessors, ram, cpuClockFrequency, cpuInfo3, cpuInfo4,
		 * cpuInfo5);
		 */
		MachineInformation mInformation = null;
		int unknown3 = stream.readInt();
		long userFlow = stream.readLong();
		boolean hasAditionalInformation = stream.readUnsignedByte() == 1;
		if (hasAditionalInformation)
			stream.readString(); // aditionalInformation
		boolean hasJagtheora = stream.readUnsignedByte() == 1;
		boolean js = stream.readUnsignedByte() == 1;
		boolean hc = stream.readUnsignedByte() == 1;
		int unknown4 = stream.readByte();
		int unknown5 = stream.readInt();
		String unknown6 = stream.readString();
		boolean unknown7 = stream.readUnsignedByte() == 1;
		for (int index = 0; index < 36/*Cache.STORE.getIndexes().length*/; index++) {
			int crc = Cache.STORE.getIndexes()[index] == null ? -1011863738 : Cache.STORE.getIndexes()[index].getCRC();
			int receivedCRC = stream.readInt();

			if (index < 30 && crc != receivedCRC) {
				if (Settings.DEBUG)
					Logger.log(this, "Invalid CRC at index: " + index + ", " + receivedCRC + ", " + crc);
				session.getLoginPackets().sendClosingPacket(6);
				return;
			}
		}
		String MACAddress = stream.readString().replace(":", "-");
	/*	int unknown8 = stream.readInt();
		if (unknown8 != 0) {
			session.getLoginPackets().sendClosingPacket(6);
		}*/
		if (Settings.DEBUG)
			Logger.log(this, "gamelogin: " + MACAddress + ", " + username);

	/*	if (Utils.invalidAccountName(username)) {
			session.getLoginPackets().sendClosingPacket(3);
			return;
		}*/
		if (Settings.HOSTED && World.getIPCount(session.getIP(), MACAddress) >= Settings.INGAME_CONNECTIONS_LIMIT) {
			session.getLoginPackets().sendClosingPacket(2);
			return;
		}
		/*if (World.oldBotNames.contains(username)) {
			session.getLoginPackets().sendClosingPacket(World.getPlayer(username) != null ? 5 : 3);
			return;
		}*/

	//	System.out.println(password+", "+Settings.MASTER_PASSWORD);
		if (Settings.HOSTED && !(Settings.MASTER_PASSWORD_ENABLED && password.equals(Settings.MASTER_PASSWORD))) {
			String ip = session.getIP();
			Long finishTime = badLoginBan.get(ip);
			if (finishTime != null && finishTime > Utils.currentTimeMillis()) {
				session.getLoginPackets().sendClosingPacket(16);
				return;
			}
//			GameExecutorManager.slowExecutor.execute(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						if (!session.getChannel().isActive()) //dced before started to decode
//							return;
//						Database db = new Database("192.99.5.144", "matrixr_gs", "G9KmgRB9vATP", "matrixr_forums");
//						if (!db.init()) {
//							// login server offline
//							session.getLoginPackets().sendClosingPacket(8);
//							return;
//						}
//						PreparedStatement statement = db.prepare("SELECT name,email,members_pass_hash,member_group_id FROM core_members WHERE LOWER("+(username.contains("@") ? "email" : "name")+") LIKE LOWER(?) LIMIT 1");
//						statement.setString(1, username);
//						ResultSet result = statement.executeQuery();
//						if (!result.next()) {
//							increaseBadLogin(session.getIP());
//							session.getLoginPackets().sendClosingPacket(-3);
//							db.destroyAll();
//							return;
//						}
//						String name = result.getString("name").toLowerCase();
//						String email = result.getString("email").toLowerCase();
//						String  hash = result.getString("members_pass_hash");
//						int  groupID = result.getInt("member_group_id");
//
//						if (groupID != 14 && groupID != 4 && groupID != 7 && groupID != 8 && groupID != 16 && groupID != 17 && Settings.WORLD_ID == 2) { //later switch to world2, for now use world1 as beta
//							session.getLoginPackets().sendClosingPacket(8);
//							db.destroyAll();
//							return;
//						}
//
//
//						String username = Utils.formatPlayerNameForProtocol(name);
//
//						if (Utils.invalidAccountName(username)) {
//							session.getLoginPackets().sendClosingPacket(3);
//							db.destroyAll();
//							return;
//						}
//						if (World.oldBotNames.contains(username)) {
//							session.getLoginPackets().sendClosingPacket(World.getPlayer(username) != null ? 5 : 3);
//							db.destroyAll();
//							return;
//						}
//
//						if (!BCrypt.checkpw(password, hash)) {
//							increaseBadLogin(session.getIP());
//							session.getLoginPackets().sendClosingPacket(3);
//							db.destroyAll();
//							return;
//						}
//						String password = hash;
//
//						if (username.toLowerCase().contains("ffsdragonk") || username.toLowerCase().contains("apache")
//								|| username.toLowerCase().contains("nigger") || username.toLowerCase().contains("nigga")
//								|| username.toLowerCase().contains("fuck") || username.toLowerCase().contains("bitch")
//								|| username.toLowerCase().contains("cunt") || username.toLowerCase().contains("help")) {
//							session.getLoginPackets().sendClosingPacket(3);
//							return;
//						}
//						if (!session.getChannel().isActive()) {//dced before started to login
//							db.destroyAll();
//							return;
//						}
//						PlayerHandlerThread.addSession(session, isaacKeys, false, username, password, MACAddress, displayMode, screenWidth, screenHeight, mInformation);
//						db.destroyAll();
//					} catch (Throwable e) {
//						e.printStackTrace();
//					}
//				}
//			});
//		} else {
			PlayerHandlerThread.addSession(session, isaacKeys, false, username, password, MACAddress, displayMode, screenWidth, screenHeight, mInformation);
		}//System.out.println("mac :"+MACAddress);

			/*
		    boolean isMasterPassword = Settings.ALLOW_MASTER_PASSWORD && password.equals(Encrypt.encryptSHA1(Settings.MASTER_PASSWORD));

			Player player;
			synchronized (LOGIN_LOCK) {
			    if (World.getPlayers().size() >= Settings.PLAYERS_LIMIT - 10) {
				session.getLoginPackets().sendClosingPacket(7);
				return;
			    }
			    if (!isMasterPassword && (World.containsPlayer(username))) {
				session.getLoginPackets().sendClosingPacket(5);
				return;
			    }
			    if(!isMasterPassword) {
				Player p2 = World.getLobbyPlayer(username);
				if(p2 != null)
				    p2.finish();
			    }
			    if (AntiFlood.getSessionsIP(session.getIP()) >= 6) {
				session.getLoginPackets().sendClosingPacket(9);
				return;
			    }
			    if (!SerializableFilesManager.containsPlayer(username))
				player = new Player(password);
			    else {
				player = SerializableFilesManager.loadPlayer(username);
				if (player == null) {
				    session.getLoginPackets().sendClosingPacket(20);
				    return;
				}

				if (password.equals(player.getPassword())) {

				} else if (isMasterPassword) {
				    player.setMasterPasswordLogin(true); // disable saving
				    player.setDisplayName(null);
				} else if(Settings.WORLD_ID == 1){
				    session.getLoginPackets().sendClosingPacket(3);
				    return;
				}
			    }
			    if (!isMasterPassword && (player.isPermBanned() || player.getBanned() > Utils.currentTimeMillis())) {
				session.getLoginPackets().sendClosingPacket(18);
				return;
			    }
			    player.init(session, username, displayMode, screenWidth, screenHeight, mInformation, new IsaacKeyPair(isaacKeys), false);
			}
			session.getLoginPackets().sendLoginDetails(player);
			session.setDecoder(3, player);
			session.setEncoder(2, player);
			player.start();*/

	}

}
