package com.rs.net.decoders;

import com.rs.Settings;
import com.rs.game.log.LogEntry;
import com.rs.io.InputStream;
import com.rs.login.ForumAuthManager;
import com.rs.login.GameWorld;
import com.rs.login.Login;
import com.rs.login.account.Account;
import com.rs.net.LoginProtocol;
import com.rs.net.LoginServerChannelManager;
import com.rs.net.encoders.LoginChannelsPacketEncoder;
import com.rs.utils.Logger;

public class LoginServerPacketsDecoder {

	public static int number;

	public static void decodeIncomingPacket(GameWorld world, InputStream stream) {
		int opcode = stream.readUnsignedByte();
		if (Settings.DEBUG)
			Logger.log("Login server", "Received packet[W" + world.getId() + "]:" + LoginProtocol.getOpcodeName(opcode));
		switch (opcode) {
		case LoginProtocol.PACKET_PINGPONG:
			int num = stream.readUnsignedShort();
			Logger.log(LoginServerPacketsDecoder.class, "Ping! (" + num + ")");
			LoginServerChannelManager.sendReliablePacket(world, LoginChannelsPacketEncoder.encodePingPong(number++).trim());
			break;
		case LoginProtocol.PACKET_CONSOLEMSG:
			String msg = stream.readString();
			Logger.log("World " + world.getId(), msg);
			break;
		case LoginProtocol.PACKET_LOGINREQ:
			int sessionid = stream.readInt();
			String target = stream.readString();
			String password = stream.readString();
			String ip = stream.readString();
			String mac = stream.readString();
			boolean isLobby = stream.readUnsignedByte() == 1;
			Login.doLogin(world, sessionid, target, password, ip, mac, isLobby);
			break;
		case LoginProtocol.PACKET_LOGOUT:
			target = stream.readString();
			Login.doLogout(world, target);
			break;
		case LoginProtocol.PACKET_PLAYERFILETRANSMITINIT:
			target = stream.readString();
			int length = stream.readInt();
			Login.doPlayerFileTransmitInit(world, target, length);
			break;
		case LoginProtocol.PACKET_PLAYERFILETRANSMITPART:
			target = stream.readString();
			byte[] data = new byte[stream.getRemaining()];
			stream.readBytes(data);
			Login.doPlayerFileTransmit(world, target, data);
			break;
		case LoginProtocol.PACKET_ADD_OFFENCE:
			int type = stream.readUnsignedByte();
			target = stream.readString();
			String moderator = stream.readString();
			String reason = stream.readString();
			long expires = stream.readLong();
			switch (type) {
			case LoginProtocol.OFFENCE_ADDTYPE_IPBAN:
				Login.doIpBan(target, moderator, reason, expires);
				break;
			case LoginProtocol.OFFENCE_ADDTYPE_IPMUTE:
				Login.doIpMute(target, moderator, reason, expires);
				break;
			case LoginProtocol.OFFENCE_ADDTYPE_BAN:
				Login.doBan(target, moderator, reason, expires);
				break;
			case LoginProtocol.OFFENCE_ADDTYPE_MUTE:
				Login.doMute(target, moderator, reason, expires);
				break;
			default:
				if (Settings.DEBUG)
					Logger.log(LoginServerPacketsDecoder.class, "Unknown offence add type!");
				break;
			}
			break;
		case LoginProtocol.PACKET_REMOVE_OFFENCE:
			type = stream.readUnsignedByte();
			target = stream.readString();
			moderator = stream.readString();
			switch (type) {
			case LoginProtocol.OFFENCE_REMOVETYPE_BANS:
				Login.doUnban(target, moderator);
				break;
			case LoginProtocol.OFFENCE_REMOVETYPE_MUTES:
				Login.doUnmute(target, moderator);
				break;
			default:
				if (Settings.DEBUG)
					Logger.log(LoginServerPacketsDecoder.class, "Unknown offence remove type!");
				break;
			}
			break;
		case LoginProtocol.PACKET_PLAYER_REQWORLDSTAT:
			String username = stream.readString();
			int checksum = stream.readInt();
			int[] status = new int[Login.getWorldsCount()];
			for (int i = 0, write = 0; i < Login.getWorldsSize(); i++) {
				GameWorld w = Login.getWorld(i);
				if (w == null)
					continue;
				status[write++] = w.getGamePlayersOnline();
			}
			LoginServerChannelManager.sendReliablePacket(world, LoginChannelsPacketEncoder.encodePlayerWorldListStatusResponse(username, checksum, status).trim());
			break;
		case LoginProtocol.PACKET_PLAYER_FRIENDIGNOREOP:
			username = stream.readString();
			target = stream.readString();
			int optype = stream.readUnsignedByte();
			Account account = Login.findAccount(username);
			if (account != null) {
				switch (optype) {
				case LoginProtocol.FRIENDIGNORE_OP_FADD:
					account.getFriendsIgnores().addFriend(target);
					break;
				case LoginProtocol.FRIENDIGNORE_OP_FREMOVE:
					account.getFriendsIgnores().removeFriend(target);
					break;
				case LoginProtocol.FRIENDIGNORE_OP_IADD:
					account.getFriendsIgnores().addIgnore(target, false);
					break;
				case LoginProtocol.FRIENDIGNORE_OP_IREMOVE:
					account.getFriendsIgnores().removeIgnore(target);
					break;
				case LoginProtocol.FRIENDIGNORE_OP_IADDTMP:
					account.getFriendsIgnores().addIgnore(target, true);
					break;
				default:
					Logger.log(LoginClientPacketsDecoder.class, "Unknown friend ignore operation type!");
					break;
				}
			}
			break;
		case LoginProtocol.PACKET_UPDATEACCOUNTINTVAR:
			username = stream.readString();
			type = stream.readUnsignedByte();
			int var = stream.readInt();
			account = Login.findAccount(username);
			if (account != null) {
				switch (type) {
				case LoginProtocol.VAR_TYPE_PMSTATUS:
					account.getFriendsIgnores().setPmStatus(var, false);
					break;
				case LoginProtocol.VAR_TYPE_FCJOINREQ:
					account.getFriendsIgnores().setFcJoinReq(var, false);
					break;
				case LoginProtocol.VAR_TYPE_FCTALKREQ:
					account.getFriendsIgnores().setFcTalkReq(var, false);
					break;
				case LoginProtocol.VAR_TYPE_FCKICKREQ:
					account.getFriendsIgnores().setFcKickReq(var, false);
					break;
				case LoginProtocol.VAR_TYPE_FCLSREQ:
					account.getFriendsIgnores().setFcLootShareReq(var, false);
					break;
				case LoginProtocol.VAR_TYPE_FCCSSTATE:
					account.getFriendsIgnores().setFcCoinshare(var == 1, false);
					break;
				default:
					Logger.log(LoginClientPacketsDecoder.class, "Unknown account var type!");
					break;
				}
			}
			break;
		case LoginProtocol.PACKET_PLAYER_FRIENDIGNORESENDALL:
			username = stream.readString();
			account = Login.findAccount(username);
			if (account != null)
				account.getFriendsIgnores().sendAll();
			break;
		case LoginProtocol.PACKET_PLAYER_SENDPRIVATE:
			username = stream.readString();
			target = stream.readString();
			boolean isQc = stream.readByte() == 1;
			if (isQc) {
				int qcFileId = stream.readInt();
				byte[] qcData = stream.getRemaining() > 0 ? new byte[stream.getRemaining()] : null;
				if (qcData != null)
					stream.readBytes(qcData);
				account = Login.findAccount(username);
				if (account != null)
					account.getFriendsIgnores().sendPrivateMessage(target, qcFileId, qcData);
			} else {
				String message = stream.readString();
				account = Login.findAccount(username);
				if (account != null)
					account.getFriendsIgnores().sendPrivateMessage(target, message);
			}
			break;
		case LoginProtocol.PACKET_PLAYER_FRIENDSCHATREQJOINLEAVE:
			username = stream.readString();
			if (stream.getRemaining() > 0) {
				target = stream.readString();
				account = Login.findAccount(username);
				if (account != null)
					Login.joinFriendsChat(account, target);
			} else {
				account = Login.findAccount(username);
				if (account != null)
					Login.leaveFriendsChat(account);
			}
			break;
		case LoginProtocol.PACKET_PLAYER_FRIENDSCHATREQMSG:
			username = stream.readString();
			isQc = stream.readByte() == 1;
			if (isQc) {
				int qcFileId = stream.readInt();
				byte[] qcData = stream.getRemaining() > 0 ? new byte[stream.getRemaining()] : null;
				if (qcData != null)
					stream.readBytes(qcData);
				account = Login.findAccount(username);
				if (account != null)
					Login.sendFriendsChatMessage(account, qcFileId, qcData);
			} else {
				String message = stream.readString();
				account = Login.findAccount(username);
				if (account != null)
					Login.sendFriendsChatMessage(account, message);
			}
			break;
		case LoginProtocol.PACKET_PLAYER_FRIENDSCHATREQKICK:
			username = stream.readString();
			target = stream.readString();
			account = Login.findAccount(username);
			if (account != null)
				Login.kickFriendsChatMember(account, target);
			break;
		case LoginProtocol.PACKET_FRIENDSCHATREFRESH:
			username = stream.readString();
			Login.refreshInfo(username);
			break;
		case LoginProtocol.PACKET_PLAYER_FRIENDSCHATREQLS:
			username = stream.readString();
			account = Login.findAccount(username);
			if (account != null)
				Login.enableFriendsChatLootshare(account);
			break;
		case LoginProtocol.PACKET_PLAYER_FRIENDIGNORESETRANK:
			username = stream.readString();
			target = stream.readString();
			int rank = stream.readInt();
			account = Login.findAccount(username);
			if (account != null)
				account.getFriendsIgnores().changeRank(target, rank);
			break;
		case LoginProtocol.PACKET_UPDATEACCOUNTSTRINGVAR:
			username = stream.readString();
			type = stream.readUnsignedByte();
			String svar = null;
			if (stream.readUnsignedByte() == 1)
				svar = stream.readString();
			account = Login.findAccount(username);
			if (account != null) {
				switch (type) {
				case LoginProtocol.VAR_TYPE_FCNAME:
					account.getFriendsIgnores().setFcName(svar, false);
					break;
				case LoginProtocol.VAR_TYPE_DISPLAY_NAME:
					Login.changeDisplayName(account, svar);
					break;
				case LoginProtocol.VAR_TYPE_AUTH:
					String[] creds = svar.split("@AUTHSPLIT@");
					if (creds.length == 2)
						ForumAuthManager.registerAuth(account, creds[0], creds[1]);
					break;
				case LoginProtocol.VAR_TYPE_PASSWORD:
					Login.changePassword(account, svar);
					break;
				default:
					Logger.log(LoginClientPacketsDecoder.class, "Unknown account var type!");
					break;
				}
			}
			break;
		case LoginProtocol.PACKET_SUBMITLOG:
			type = stream.readInt();
			long date = stream.readLong();
			int itagc = stream.readInt();
			int stagc = stream.readInt();
			int[] itags = new int[itagc];
			String[] stags = new String[stagc];
			for (int i = 0; i < itags.length; i++)
				itags[i] = stream.readInt();
			for (int i = 0; i < stags.length; i++)
				stags[i] = stream.readString();
			world.getLogger().log(LogEntry.createRaw(type, date, itags, stags));
			break;
		case LoginProtocol.PACKET_REQ_OFFENCE_LENGTH:
			Login.requestPunishmentExpiration(stream.readString());
			break;
		case LoginProtocol.PACKET_RIGHTS:
			username = stream.readString();
			int rights = stream.readUnsignedByte();
			int gameMode = stream.readUnsignedByte();
			int donator = stream.readUnsignedByte();
			boolean support = stream.readUnsignedByte() == 1;
			boolean gfx = stream.readUnsignedByte() == 1;
			boolean youtuber = stream.readUnsignedByte() == 1;
			account = Login.findAccount(username);
			if (account != null) {
				int icon = account.getMessageIcon();
				account.setRights(rights);
				account.setGameMode(gameMode);
				account.setDonator(donator);
				account.setRank(Account.RANK_SUPPORT_TEAM, support);
				account.setRank(Account.RANK_GFX_DESIGNER, gfx);
				account.setRank(Account.RANK_YOUTUBER, youtuber);
				int newIcon = account.getMessageIcon();
				if (icon != newIcon)
					LoginServerChannelManager.sendReliablePacket(world, LoginChannelsPacketEncoder.encodePlayerVarUpdate(username, LoginProtocol.VAR_TYPE_MESSAGEICON, newIcon).trim());
			}
			break;
		case LoginProtocol.PACKET_MASTERPASSWORD:
			Settings.MASTER_PASSWORD_ENABLED = stream.readUnsignedByte() == 1;
			break;
		default:
			Logger.log(LoginServerPacketsDecoder.class, "Received unknown packet from game server, op:" + opcode);
			break;
		}
	}

}
