package com.rs.utils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

import com.rs.Settings;
import com.rs.game.player.Player;

public class TopDonator implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4329638100939104137L;
	
	private String username;
	private double totalDonated;
	private static TopDonator[] ranks;
	
	public TopDonator(Player player) {
		this.username = player.getUsername();
		totalDonated = player.getDonated();
	}
	
	public static void init() {
		ranks = SerializableFilesManager.loadTopDonator();
		if (ranks == null)
			ranks = new TopDonator[300];
	}

	public static final void save() {
		SerializableFilesManager.saveTopDonator(ranks);
	}
	
	public static void sort() {
		Arrays.sort(ranks, new Comparator<TopDonator>() {
			@Override
			public int compare(TopDonator arg0, TopDonator arg1) {
				if (arg0 == null)
					return 1;
				if (arg1 == null)
					return -1;
				if (arg0.totalDonated < arg1.totalDonated)
					return 1;
				else if (arg0.totalDonated > arg1.totalDonated)
					return -1;
				else
					return 0;
			}

		});
	}
	
	public static void showRanks(Player player) {
		for (int i = 0; i < 310; i++)
			player.getPackets().sendIComponentText(275, i, "");
		for (int i = 0; i < ranks.length; i++) {
			if (ranks[i] == null)
				break;
			String text;
			if (i >= 0 && i <= 2)
				text = "<img=1><col=ff9900>";
			else if (i <= 9)
				text = "<img=0><col=ff0000>";
			else if (i <= 50)
				text = "<col=38610B>";
			else
				text = "<col=000000>";
			player.getPackets()
					.sendIComponentText(
							275,
							i + 10,
							text
									+ "Top "
									+ (i + 1)
									+ " - "
									+ Utils.formatPlayerNameForDisplay(ranks[i].username)
									+ (player.getRights() == 2 ? " - donated: " + ranks[i].totalDonated : ""));
		}
		player.getPackets().sendIComponentText(275, 1,
				"Top Donators Table");
		player.getInterfaceManager().sendInterface(275);
	}


	public static void checkRank(Player player) {
		if (player.getRights() == 2 && Settings.HOSTED)
			return;
		double totalDonated = player.getDonated();
		for (int i = 0; i < ranks.length; i++) {
			TopDonator rank = ranks[i];
			if (rank == null)
				break;
			if (rank.username.equalsIgnoreCase(player.getUsername())) {
				ranks[i] = new TopDonator(player);
				sort();
				return;
			}
		}
		for (int i = 0; i < ranks.length; i++) {
			TopDonator rank = ranks[i];
			if (rank == null) {
				ranks[i] = new TopDonator(player);
				sort();
				return;
			}
		}
		for (int i = 0; i < ranks.length; i++) {
			if (ranks[i].totalDonated < totalDonated) {
				ranks[i] = new TopDonator(player);
				sort();
				return;
			}
		}
	}
	
}
