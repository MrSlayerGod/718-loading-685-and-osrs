package com.rs.game.player.content;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import com.rs.Settings;
import com.rs.cache.loaders.QuickChatOptionDefinition;
import com.rs.discord.Bot;
import com.rs.game.WorldTile;
import com.rs.game.minigames.clanwars.ClanWars;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.net.LoginClientChannelManager;
import com.rs.net.encoders.LoginChannelsPacketEncoder;
import com.rs.utils.Censor;
import com.rs.utils.Utils;

public class FriendsChat {

	/**
	 * Contains all cached chats.
	 */
	private static Map<String, FriendsChat> cache;
	/**
	 * Contains the channel key(aka owner username)
	 */
	private String channel;
	/**
	 * Contains list of local (this world) members.
	 */
	private CopyOnWriteArrayList<Player> localMembers;
	/**
	 * Clan wars instance.
	 */
	private ClanWars clanWars;

	public static void init() {
		cache = new HashMap<String, FriendsChat>();
	}

	public FriendsChat(String channel) {
		this.channel = channel;
		this.localMembers = new CopyOnWriteArrayList<Player>();
	}


	public static boolean contains(String channel, Player player) {
		synchronized (cache) {
			FriendsChat chat = cache.get(channel);
			return chat != null && chat.getLocalMembers().contains(player);
		}
	}

	public static boolean isFull(String channel) {
		synchronized (cache) {
			FriendsChat chat = cache.get(channel);
			return chat != null && chat.getLocalMembers().size() >= 100;
		}
	}

	/**
	 * Attaches player to given channel.
	 */
	public static void attach(Player player, String channel) {
		if (player.getCurrentFriendsChat() != null)
			detach(player);

		synchronized (cache) {
			FriendsChat chat = cache.get(channel);
			if (chat == null) {
				chat = new FriendsChat(channel);
				cache.put(channel, chat);
			}
			chat.getLocalMembers().add(player);
			player.setCurrentFriendsChat(chat);
		}
	}

	public static void detach(Player player) {
		if (player.getCurrentFriendsChat() == null)
			return;
		synchronized (cache) {
			FriendsChat chat = player.getCurrentFriendsChat();
			player.setCurrentFriendsChat(null);
			chat.getLocalMembers().remove(player);
			player.disableLootShare();
			if (chat.clanWars != null) 
				chat.clanWars.leaveFC(player);
			if (chat.getLocalMembers().size() <= 0) 
				removeChat(chat);

		}
	}
	
	public static void removeChat(FriendsChat chat) {
		cache.remove(chat.getChannel());
		if (chat.clanWars != null) 
			chat.clanWars.endWar();
	}
	

	/**
	 * Requests joining of specific channel.
	 */
	public static void requestJoin(Player player, String name) {
		player.getFriendsIgnores().fcSystemMessage("Attempting to join channel...");
		LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodePlayerFriendsChatJoinLeaveRequest(player.getUsername(), name).trim());
	}

	/**
	 * Requests leaving of current channel.
	 */
	public static void requestLeave(Player player) {
		LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodePlayerFriendsChatJoinLeaveRequest(player.getUsername(), null).trim());
	}

	/**
	 * Send's message request.
	 */
	public void sendMessage(Player player, String message) {
		// no talking until pin
		if(!player.checkBankPin()) {
			player.sendMessage("You must type your bank PIN first!");
			return;
		}

		if(Settings.DISABLE_GLOBAL_PROFANITY) {
			message = Censor.getFilteredMessage(message);
		}

		if(!player.isDonator()) {
			// always censor non-donor messages
			//message = Censor.getFilteredMessage(message);

			if(Utils.currentTimeMillis() - player.lastChatMessage < Settings.FC_MESSAGE_THROTTLE) {
				player.sendMessage("You must wait "+(Settings.FC_MESSAGE_THROTTLE/1000)+" seconds between messages in the Matrix friends chat.");
				return;
			}
			player.lastChatMessage = Utils.currentTimeMillis();
		}
		LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodePlayerFriendsChatMessageRequest(player.getUsername(), message).trim());
	}

	/**
	 * Send's quick chat message request.
	 */
	public void sendMessage(Player player, QuickChatOptionDefinition option, long[] qcData) {
		LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodePlayerFriendsChatMessageRequest(player, option, qcData).trim());
	}

	/**
	 * Send's kick request.
	 */
	public void kickMember(Player player, String target) {
		LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodePlayerFriendsChatKickRequest(player.getUsername(), target).trim());
	}

	/**
	 * Send's lootshare request.
	 */
	public void toogleLootshare(Player player) {
		if (player.isLootShareEnabled())
			player.disableLootShare();
		else {
			LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodePlayerFriendsChatLootshareRequest(player.getUsername()).trim());
		}
	}
	

	public static List<Player> getLootSharingPeople(Player player) {
		return getLootSharingPeople(player, null);
	}

	/**
	 * Get's list of loot sharing people.
	 */
	public static List<Player> getLootSharingPeople(Player player, NPC npc) {
		if (!player.isLootShareEnabled())
			return null;
		FriendsChat chat = player.getCurrentFriendsChat();
		if (chat == null)
			return null;
		List<Player> players = new ArrayList<Player>();
		for (Player p2 : player.getCurrentFriendsChat().getLocalMembers()) {
			if (p2.isLootShareEnabled() && p2.withinDistance(player)
					&& (npc == null || npc.getDamageReceived(p2) > 0)
				/*	&& p2.isExtreme() == player.isExtreme()*/)
				players.add(p2);
		}
		return players;
	}

	/**
	 * Send's message to all local members.
	 */
	public void sendLocalMessage(WorldTile tile, String message) {
		for (Player p2 : localMembers)
			if (p2.withinDistance(tile))
				p2.getPackets().sendGameMessage(message);
		Bot.sendLog(Bot.FRIEND_CHAT_CHANNEL, "[type=FC][fc="+channel+"][message="+message+"]");
	}

	public String getChannel() {
		return channel;
	}

	public boolean isOwner(Player player) {
		return channel.equalsIgnoreCase(player.getUsername());
	}

	public CopyOnWriteArrayList<Player> getLocalMembers() {
		return localMembers;
	}

	public ClanWars getClanWars() {
		return clanWars;
	}

	public void setClanWars(ClanWars clanWars) {
		this.clanWars = clanWars;
	}

}
