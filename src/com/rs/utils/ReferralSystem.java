package com.rs.utils;

import java.io.Serializable;
import java.util.*;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.discord.Bot;
import com.rs.game.TemporaryAtributtes;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.dialogues.impl.SelectModeDialogue;



public class ReferralSystem implements Serializable {

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -109293133735315304L;
	
	
	private static ReferralSystem data;

	private List<String> users = new LinkedList<String>();
	private List<String> macs = new LinkedList<String>();
	private List<String> ips = new LinkedList<String>();
	private LinkedList<String> lastRefs = new LinkedList<String>();
	private Map<String, Reward> rewards = new HashMap<String, Reward>();
	
	private static class Reward implements Serializable {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 6813856656155951467L;
		private int id, amount;
		private int claimCount;
		
		public Reward(int id, int amount) {
			this.id = id;
			this.amount = amount;
		}

		@Override
		public String toString() {
			return String.format("[id=%d amount=%d claimCount=%d]", id, amount, claimCount);
		}
	}
	
	public static void viewRefs(Player player) {
		sendQuestTab(player, "Referrals", data.lastRefs.toArray(new String[data.lastRefs.size()]));
	}
	
	
	public static void viewRefList(Player player) {
		String[] list = new String[data.rewards.size()];
		int i = 0;
		for (String name : data.rewards.keySet()) {
			Reward reward = data.rewards.get(name);
			list[i++] = name +" - "+ItemConfig.forID(reward.id).getName()+"("+reward.id+")x"+reward.amount+" - "+reward.claimCount;
		}
		sendQuestTab(player, "Referral rewards", list);
 	}
	
	public static void sendQuestTab(Player player, String title, String... lines) {
		player.getInterfaceManager().sendInterface(275);
		player.getPackets().sendIComponentText(275, 1, title);
		for (int i = 0; i < 300; i++) 
			player.getPackets().sendIComponentText(275, i + 10, i >= lines.length || lines[i] == null ? "" : lines[i]);
	}
	
	public static void addReward(String name, int id, int amount) {
		name = name.toLowerCase().replace("_", " ");
		data.rewards.put(name, new Reward(id, amount));
	}
	
	public static boolean removeReward(String name) {
		name = name.toLowerCase().replace("_", " ");
		if (data.rewards.remove(name) != null) 
			return true;
		return false;
	}
	
	public static void addRef(Player player, String input) {
		if(Settings.DISABLE_REFS) {
			player.sendMessage("An admin has disabled the referral system.");
			return;
		}

		if(player.hasSubmittedReferral()) {
			player.sendMessage("You have already submitted a referral, or the window to submit your referral has expired.");
			return;
		}

		SelectModeDialogue.realFinish(player);
		Integer refIndex = (Integer) player.getTemporaryAttributtes().remove(TemporaryAtributtes.Key.REFERRAL_TYPE);

		if(refIndex == null || refIndex-11 >= REFS.length) {
			refIndex = 11;
			System.out.println("Error: player clicked invalid referral option: " + refIndex + "! Set to 0 (Top-List)");
		}

		String type = refIndex != null ? REFS[refIndex-11] : REFS[0];
		final String referral = input.toLowerCase().replace("_", " ");
		Reward reward = data.rewards.get(referral);
		if(ReferralSystem.isNewPlayer(player)) {
			player.setSubmittedReferral(true);
			player.getInventory().addItemDrop(25492, 1);
			player.getPackets().sendGameMessage("You receive a mystery box thanks to mentioning your referral!");
			data.lastRefs.addFirst("Player: "+player.getUsername()+" - Referral: "+referral);
			data.users.add(player.getUsername());
			if (data.lastRefs.size() > 300)
				data.lastRefs.removeLast();
			if (data.macs.contains(player.getLastGameMAC()) || data.ips.contains(player.getSession().getIP()))
				return;
			//if (!player.getLastGameMAC().contains("00-00-00-00-00"))
			data.macs.add(player.getLastGameMAC());
			data.ips.add(player.getSession().getIP());
			//	player.getBank().addItem(25492, 1, false);
			if (reward != null) {
				reward.claimCount++;
				player.getInventory().addItemDrop(reward.id, reward.amount);
				player.getPackets().sendGameMessage("You receive a " + ItemConfig.forID(reward.id).getName().toLowerCase() + " thanks to " + referral + ".");
			}
		} else {
			player.sendMessage("You have already submitted a referral.");
		}

		String log = String.format("[type=REF][username=%s][ip=%s][mac=%s][referralType=%s][referrer=%s][reward=%s]", player.getUsername(), player.getSession().getIP(), player.getLastGameMAC(), type, referral, reward == null ? "null" : reward.toString());
		Bot.sendLog(Bot.REF_CHANNEL, log);
	}

	public final static String[] REFS = {"Top-list", "Advertisement", "Friend recommendation", "Youtuber"};

	public static void init() {
		data = SerializableFilesManager.loadReferralSystem();
		if (data == null)
			data = new ReferralSystem();
	}

	public static void save() {
		SerializableFilesManager.saveReferralSystem(data);
	}

	public static boolean isNewPlayer(Player player) {
		if ((!player.getLastGameMAC().contains("00-00-00-00-00") && data.macs.contains(player.getLastGameMAC())) || data.ips.contains(player.getSession().getIP()))
			return false;
		return true;
	}
}
