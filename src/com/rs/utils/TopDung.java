package com.rs.utils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

import com.rs.Settings;
import com.rs.game.player.Player;

public class TopDung implements Serializable {


	
	private static final long serialVersionUID = 4122388468601046501L;
	
	
	private String username;
	private long points;
	private static TopDung[] ranks;
	
	public TopDung(Player player) {
		this.username = player.getUsername();
		points = player.getDungManager().getPoints();
	}
	
	public static void init() {
		ranks = SerializableFilesManager.loadTopDung();
		if (ranks == null)
			ranks = new TopDung[300];
	}

	public static final void save() {
		SerializableFilesManager.saveTopDung(ranks);
	}
	
	public static void sort() {
		Arrays.sort(ranks, new Comparator<TopDung>() {
			@Override
			public int compare(TopDung arg0, TopDung arg1) {
				if (arg0 == null)
					return 1;
				if (arg1 == null)
					return -1;
				if (arg0.points < arg1.points)
					return 1;
				else if (arg0.points > arg1.points)
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
									+ " - points: " + Utils.getFormattedNumber(ranks[i].points));
		}
		player.getPackets().sendIComponentText(275, 1,
				"Top Dungeoneering Table");
		player.getInterfaceManager().sendInterface(275);
	}


	public static void checkRank(Player player) {
		if (player.getRights() == 2 && Settings.HOSTED)
			return;
		long totalDonated = player.getDungManager().getPoints();
		for (int i = 0; i < ranks.length; i++) {
			TopDung rank = ranks[i];
			if (rank == null)
				break;
			if (rank.username.equalsIgnoreCase(player.getUsername())) {
				ranks[i] = new TopDung(player);
				sort();
				return;
			}
		}
		for (int i = 0; i < ranks.length; i++) {
			TopDung rank = ranks[i];
			if (rank == null) {
				ranks[i] = new TopDung(player);
				sort();
				return;
			}
		}
		for (int i = 0; i < ranks.length; i++) {
			if (ranks[i].points < totalDonated) {
				ranks[i] = new TopDung(player);
				sort();
				return;
			}
		}
	}
	
}
