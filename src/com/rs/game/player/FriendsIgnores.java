package com.rs.game.player;

import java.util.ArrayList;
import java.util.List;

import com.rs.cache.loaders.QuickChatOptionDefinition;
import com.rs.io.OutputStream;
import com.rs.net.LoginClientChannelManager;
import com.rs.net.LoginProtocol;
import com.rs.net.decoders.WorldPacketsDecoder;
import com.rs.net.encoders.LoginChannelsPacketEncoder;

public class FriendsIgnores {

	public static final int PM_STATUS_ONLINE = 0;
	public static final int PM_STATUS_FRIENDSONLY = 1;
	public static final int PM_STATUS_OFFLINE = 2;

	/**
	 * Our player instance.
	 */
	private Player player;
	/**
	 * Our pm status.
	 */
	private int pmStatus;
	/**
	 * Friends chat name.
	 */
	private String fcName;
	/**
	 * Friends chat join requirement.
	 */
	private int fcJoinReq;
	/**
	 * Friends chat talk requirement.
	 */
	private int fcTalkReq;
	/**
	 * Friends chat kick requirement.
	 */
	private int fcKickReq;
	/**
	 * Friends chat lootshare requirement.
	 */
	private int fcLootshareReq;
	/**
	 * Friends chat coinshare settings.
	 */
	private boolean fcCoinshare;
	/**
	 * Contains list of friends.
	 */
	private List<String> friends;
	/**
	 * Contains list of ignores.
	 */
	private List<String> ignores;
	/**
	 * Current friends update packet.
	 */
	private OutputStream friendsUpdatePacket;
	/**
	 * Current ignores update packet.
	 */
	private OutputStream ignoresUpdatePacket;
	/**
	 * Amount of ignores written on update.
	 */
	private int ignoresWritten;

	public FriendsIgnores(Player player) {
		this.player = player;
		this.friends = new ArrayList<String>();
		this.ignores = new ArrayList<String>();
	}

	/**
	 * Initialize's by requesting our data.
	 */
	public void initialize() {
		LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodePlayerFriendIgnoreSendAll(player.getUsername()).trim());
	}

	/**
	 * Set's our pm status.
	 */
	public void setPmStatus(int pmStatus, boolean updateAccount) {
		this.pmStatus = pmStatus;
		player.getPackets().sendPmStatus();
		if (updateAccount)
			LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodeAccountVarUpdate(player.getUsername(), LoginProtocol.VAR_TYPE_PMSTATUS, pmStatus).trim());
	}

	/**
	 * Set's our friends chat name.
	 */
	public void setFcName(String newName, boolean updateAccount) {
		fcName = newName;
		if (updateAccount)
			LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodeAccountVarUpdate(player.getUsername(), LoginProtocol.VAR_TYPE_FCNAME, fcName).trim());
		if (!player.isLobby() && player.getInterfaceManager().containsInterface(1108))
			refreshChatName();
	}

	/**
	 * Set's our friends chat join requirement.
	 */
	public void setFcJoinReq(int newReq, boolean updateAccount) {
		fcJoinReq = newReq;
		if (updateAccount)
			LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodeAccountVarUpdate(player.getUsername(), LoginProtocol.VAR_TYPE_FCJOINREQ, fcJoinReq).trim());
		if (!player.isLobby() && player.getInterfaceManager().containsInterface(1108))
			refreshWhoCanEnterChat();
	}

	/**
	 * Set's our friends chat talk requirement.
	 */
	public void setFcTalkReq(int newReq, boolean updateAccount) {
		fcTalkReq = newReq;
		if (updateAccount)
			LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodeAccountVarUpdate(player.getUsername(), LoginProtocol.VAR_TYPE_FCTALKREQ, fcTalkReq).trim());
		if (!player.isLobby() && player.getInterfaceManager().containsInterface(1108))
			refreshWhoCanTalkOnChat();
	}

	/**
	 * Set's our friends chat kick requirement.
	 */
	public void setFcKickReq(int newReq, boolean updateAccount) {
		fcKickReq = newReq;
		if (updateAccount)
			LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodeAccountVarUpdate(player.getUsername(), LoginProtocol.VAR_TYPE_FCKICKREQ, fcKickReq).trim());
		if (!player.isLobby() && player.getInterfaceManager().containsInterface(1108))
			refreshWhoCanKickOnChat();
	}

	/**
	 * Set's our friends chat loot share requirement.
	 */
	public void setFcLootShareReq(int newReq, boolean updateAccount) {
		fcLootshareReq = newReq;
		if (updateAccount)
			LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodeAccountVarUpdate(player.getUsername(), LoginProtocol.VAR_TYPE_FCLSREQ, fcLootshareReq).trim());
		if (!player.isLobby() && player.getInterfaceManager().containsInterface(1108))
			refreshWhoCanShareloot();
	}

	/**
	 * Set's our friends chat loot share requirement.
	 */
	public void setFcCoinshare(boolean coinshare, boolean updateAccount) {
		fcCoinshare = coinshare;
		if (updateAccount)
			LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodeAccountVarUpdate(player.getUsername(), LoginProtocol.VAR_TYPE_FCCSSTATE, fcCoinshare ? 1 : 0).trim());
	}

	/**
	 * Send's private message.
	 */
	public void sendPrivateMessage(String displayName, String message) {
		LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodePlayerSendPrivateMessage(player.getUsername(), displayName, message).trim());
	}

	/**
	 * Send's private message.
	 */
	public void sendPrivateMessage(String displayName, QuickChatOptionDefinition option, long[] qcData) {
		LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodePlayerSendPrivateMessage(player, displayName, option, qcData).trim());
	}

	/**
	 * Add's new friend.
	 */
	public void addFriend(String displayName) {
		LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodePlayerFriendIgnoreOperation(player.getUsername(), displayName, LoginProtocol.FRIENDIGNORE_OP_FADD).trim());
	}

	/**
	 * Remove's friend.
	 */
	public void removeFriend(String displayName) {
		friends.remove(displayName);

		LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodePlayerFriendIgnoreOperation(player.getUsername(), displayName, LoginProtocol.FRIENDIGNORE_OP_FREMOVE).trim());
	}

	/**
	 * Add's new ignore.
	 */
	public void addIgnore(String displayName, boolean temp) {
		LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodePlayerFriendIgnoreOperation(player.getUsername(), displayName, temp ? LoginProtocol.FRIENDIGNORE_OP_IADDTMP : LoginProtocol.FRIENDIGNORE_OP_IADD).trim());
	}

	/**
	 * Remove's ignore.
	 */
	public void removeIgnore(String displayName) {
		ignores.remove(displayName);

		LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodePlayerFriendIgnoreOperation(player.getUsername(), displayName, LoginProtocol.FRIENDIGNORE_OP_IREMOVE).trim());
	}

	/**
	 * Change's rank.
	 */
	public void changeRank(String displayName, int newRank) {
		LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodePlayerFriendIgnoreSetRank(player.getUsername(), displayName, newRank).trim());
	}

	/**
	 * Whether we have a friend with such display name.
	 */
	public boolean isFriend(String displayName) {
		return friends.contains(displayName);
	}

	/**
	 * Whether we have an ignore with such display name.
	 */
	public boolean isIgnore(String displayName) {
		return ignores.contains(displayName);
	}

	/**
	 * Send's system message.
	 */
	public void fiSystemMessage(String message) {
		player.getPackets().sendMessage(4, message, null);
	}

	/**
	 * Send's system message.
	 */
	public void fcSystemMessage(String message) {
		player.getPackets().sendMessage(11, message, null);
	}

	/**
	 * Start's friends update packet.
	 */
	public void beginFriendsUpdate() {
		friendsUpdatePacket = player.getPackets().startFriendsPacket();
	}

	/**
	 * Handle's friend update request.
	 */
	public void updateFriend(boolean isStatusUpdate, String displayName, String previousDisplayName, int world, int fcRank, String worldName) {
		if (isStatusUpdate && !isFriend(displayName))
			return;

		if (!isStatusUpdate && !isFriend(displayName)) {
			if (previousDisplayName != null)
				friends.remove(previousDisplayName);
			friends.add(displayName);
		}

		player.getPackets().appendFriend(friendsUpdatePacket, isStatusUpdate, displayName, previousDisplayName, world, fcRank, worldName);
	}

	/**
	 * End's friends update packet.
	 */
	public void endFriendsUpdate() {
		player.getPackets().endFriendsPacket(friendsUpdatePacket);
		friendsUpdatePacket = null;
	}

	/**
	 * Start's ignores update packet.
	 */
	public void beginIgnoresUpdate(boolean reset) {
		if (reset) {
			ignores.clear();
			ignoresWritten = 0;
			ignoresUpdatePacket = player.getPackets().startIgnoresPacket();
		}
	}

	/**
	 * Handle's ignore update request.
	 */
	public void updateIgnore(boolean isNameUpdate, String displayName, String previousDisplayName) {
		if (!isNameUpdate && isIgnore(displayName))
			return;
		else if (isNameUpdate && (previousDisplayName == null || isIgnore(displayName) || !isIgnore(previousDisplayName)))
			return;

		if (isNameUpdate) {
			ignores.add(displayName);
			ignores.remove(previousDisplayName);
		} else {
			ignores.add(displayName);
		}

		if (ignoresUpdatePacket != null) {
			player.getPackets().appendIgnore(ignoresUpdatePacket, displayName, previousDisplayName);
		} else {
			player.getPackets().sendPlainIgnore(isNameUpdate, displayName, previousDisplayName);
		}

		ignoresWritten++;

	}

	/**
	 * End's ignores update packet.
	 */
	public void endIgnoresUpdate() {
		if (ignoresUpdatePacket != null) {
			player.getPackets().endIgnoresPacket(ignoresUpdatePacket, ignoresWritten);
			ignoresUpdatePacket = null;
		}
	}

	/**
	 * Open's friends chat setup interface.
	 */
	public void openFriendChatSetup() {
		player.getInterfaceManager().sendInterface(1108);
		refreshChatName();
		refreshWhoCanEnterChat();
		refreshWhoCanTalkOnChat();
		refreshWhoCanKickOnChat();
		refreshWhoCanShareloot();
		player.getPackets().sendHideIComponent(1108, 49, true);
		player.getPackets().sendHideIComponent(1108, 63, true);
		player.getPackets().sendHideIComponent(1108, 77, true);
		player.getPackets().sendHideIComponent(1108, 91, true);
	}

	/**
	 * Handle's interface clicks.
	 */
	public void handleFriendChatButtons(int interfaceId, int componentId, int packetId) {
		if (interfaceId == 1109) {
			if (componentId == 19) {
				if (player.getCurrentFriendsChat() == null) {
					player.getPackets().sendGameMessage("You need to be in a Friends Chat channel to activate LootShare.");
					player.refreshLootShare();
					return;
				}
				/*if (player.isHCIronman()) {
					player.getPackets().sendGameMessage("Hardcore ironman are not able to lootshare.");
					player.refreshLootShare();
					return;
				}*/
				player.getCurrentFriendsChat().toogleLootshare(player);
			} else if (componentId == 31) {
				if (player.getInterfaceManager().containsScreenInter()) {
					player.getPackets().sendGameMessage("Please close the interface you have opened before using Friends Chat setup.");
					return;
				}
				player.stopAll();
				openFriendChatSetup();
			}
		} else if (interfaceId == 1108) {
			if (componentId == 1) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
					player.getPackets().sendExecuteScript(109, new Object[]
					{ "Enter chat prefix:" });
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					setFcName(null, true);
				}
			} else if (componentId == 2) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					setFcJoinReq(-1, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					setFcJoinReq(0, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					setFcJoinReq(1, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					setFcJoinReq(2, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET)
					setFcJoinReq(3, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET)
					setFcJoinReq(4, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON6_PACKET)
					setFcJoinReq(5, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON7_PACKET)
					setFcJoinReq(6, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON10_PACKET)
					setFcJoinReq(7, true);
			} else if (componentId == 3) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					setFcTalkReq(-1, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					setFcTalkReq(0, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					setFcTalkReq(1, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					setFcTalkReq(2, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET)
					setFcTalkReq(3, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET)
					setFcTalkReq(4, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON6_PACKET)
					setFcTalkReq(5, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON7_PACKET)
					setFcTalkReq(6, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON10_PACKET)
					setFcTalkReq(7, true);
			} else if (componentId == 4) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					setFcKickReq(-1, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					setFcKickReq(0, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					setFcKickReq(1, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					setFcKickReq(2, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET)
					setFcKickReq(3, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET)
					setFcKickReq(4, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON6_PACKET)
					setFcKickReq(5, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON7_PACKET)
					setFcKickReq(6, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON10_PACKET)
					setFcKickReq(7, true);
			} else if (componentId == 5) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					setFcLootShareReq(7, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					setFcLootShareReq(0, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					setFcLootShareReq(1, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					setFcLootShareReq(2, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET)
					setFcLootShareReq(3, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET)
					setFcLootShareReq(4, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON6_PACKET)
					setFcLootShareReq(5, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON7_PACKET)
					setFcLootShareReq(6, true);
			}
		}
	}

	/**
	 * Set's our chat prefix.
	 */
	public void setChatPrefix(String name) {
		if (name.length() < 1 || name.length() > 20)
			return;
		setFcName(name, true);
	}

	/**
	 * Refreshes our chat name
	 */
	public void refreshChatName() {
		player.getPackets().sendIComponentText(1108, 1, fcName == null ? "Chat disabled" : fcName);
	}

	/**
	 * Refreshes enter requirement
	 */
	public void refreshWhoCanEnterChat() {
		String text;
		if (fcJoinReq == 0)
			text = "Any friends";
		else if (fcJoinReq == 1)
			text = "Recruit+";
		else if (fcJoinReq == 2)
			text = "Corporal+";
		else if (fcJoinReq == 3)
			text = "Sergeant+";
		else if (fcJoinReq == 4)
			text = "Lieutenant+";
		else if (fcJoinReq == 5)
			text = "Captain+";
		else if (fcJoinReq == 6)
			text = "General+";
		else if (fcJoinReq == 7)
			text = "Only Me";
		else
			text = "Anyone";
		player.getPackets().sendIComponentText(1108, 2, text);
	}

	/**
	 * Refreshes talk requirement
	 */
	public void refreshWhoCanTalkOnChat() {
		String text;
		if (fcTalkReq == 0)
			text = "Any friends";
		else if (fcTalkReq == 1)
			text = "Recruit+";
		else if (fcTalkReq == 2)
			text = "Corporal+";
		else if (fcTalkReq == 3)
			text = "Sergeant+";
		else if (fcTalkReq == 4)
			text = "Lieutenant+";
		else if (fcTalkReq == 5)
			text = "Captain+";
		else if (fcTalkReq == 6)
			text = "General+";
		else if (fcTalkReq == 7)
			text = "Only Me";
		else
			text = "Anyone";
		player.getPackets().sendIComponentText(1108, 3, text);
	}

	/**
	 * Refreshes kick requirement
	 */
	public void refreshWhoCanKickOnChat() {
		String text;
		if (fcKickReq == 0)
			text = "Any friends";
		else if (fcKickReq == 1)
			text = "Recruit+";
		else if (fcKickReq == 2)
			text = "Corporal+";
		else if (fcKickReq == 3)
			text = "Sergeant+";
		else if (fcKickReq == 4)
			text = "Lieutenant+";
		else if (fcKickReq == 5)
			text = "Captain+";
		else if (fcKickReq == 6)
			text = "General+";
		else if (fcKickReq == 7)
			text = "Only Me";
		else
			text = "Anyone";
		player.getPackets().sendIComponentText(1108, 4, text);
	}

	/**
	 * Refreshes ls requirement
	 */
	public void refreshWhoCanShareloot() {
		String text;
		if (fcLootshareReq == 0)
			text = "Any friends";
		else if (fcLootshareReq == 1)
			text = "Recruit+";
		else if (fcLootshareReq == 2)
			text = "Corporal+";
		else if (fcLootshareReq == 3)
			text = "Sergeant+";
		else if (fcLootshareReq == 4)
			text = "Lieutenant+";
		else if (fcLootshareReq == 5)
			text = "Captain+";
		else if (fcLootshareReq == 6)
			text = "General+";
		else
			text = "No-one";
		player.getPackets().sendIComponentText(1108, 5, text);
	}

	public int getPmStatus() {
		return pmStatus;
	}

	public String getFcName() {
		return fcName;
	}

	public int getFcJoinReq() {
		return fcJoinReq;
	}

	public int getFcTalkReq() {
		return fcTalkReq;
	}

	public int getFcKickReq() {
		return fcKickReq;
	}

	public int getFcLootshareReq() {
		return fcLootshareReq;
	}

	public boolean isFcCoinshare() {
		return fcCoinshare;
	}
	
	public List<String> getIgnores() {
		return ignores;
	}
}
