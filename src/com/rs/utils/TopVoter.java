package com.rs.utils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

import com.rs.Settings;
import com.rs.game.player.Player;

public class TopVoter implements Serializable {

	

	private static final long serialVersionUID = -1592621095956926266L;
	private String username;
	private int votes;
	private static TopVoter[] ranks;
	
	public TopVoter(Player player) {
		this.username = player.getUsername();
		votes = player.getVoteCount();
	}
	
	public static void init() {
		ranks = SerializableFilesManager.loadTopVoter();
		if (ranks == null)
			ranks = new TopVoter[300];
	}

	public static final void save() {
		SerializableFilesManager.saveTopVoter(ranks);
	}
	
	public static void sort() {
		Arrays.sort(ranks, new Comparator<TopVoter>() {
			@Override
			public int compare(TopVoter arg0, TopVoter arg1) {
				if (arg0 == null)
					return 1;
				if (arg1 == null)
					return -1;
				if (arg0.votes < arg1.votes)
					return 1;
				else if (arg0.votes > arg1.votes)
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
									+  " - voted: " + ranks[i].votes);
		}
		player.getPackets().sendIComponentText(275, 1,
				"Top Voters Table");
		player.getInterfaceManager().sendInterface(275);
	}


	public static void checkRank(Player player) {
		if (player.getRights() == 2 && Settings.HOSTED)
			return;
		int totalDonated = player.getVoteCount();
		for (int i = 0; i < ranks.length; i++) {
			TopVoter rank = ranks[i];
			if (rank == null)
				break;
			if (rank.username.equalsIgnoreCase(player.getUsername())) {
				ranks[i] = new TopVoter(player);
				sort();
				return;
			}
		}
		for (int i = 0; i < ranks.length; i++) {
			TopVoter rank = ranks[i];
			if (rank == null) {
				ranks[i] = new TopVoter(player);
				sort();
				return;
			}
		}
		for (int i = 0; i < ranks.length; i++) {
			if (ranks[i].votes < totalDonated) {
				ranks[i] = new TopVoter(player);
				sort();
				return;
			}
		}
	}
	
}
