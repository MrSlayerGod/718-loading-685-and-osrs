package com.rs.utils;

import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.rs.Settings;
import com.rs.game.player.Player;

public class MTopDonator {

	private static Map<String, Double> donated;

	public static String getTopString() {
		StringBuilder sb = new StringBuilder();

		List<String> sorted = donated.entrySet().stream()
				.sorted(reverseOrder(comparing(Entry::getValue)))
				.map(Entry::getKey)
				.collect(toList());

		for(int i = 0; i < 5 && i < sorted.size(); i++) {
			if(sorted.get(i) == null)
				continue;
			String username = sorted.get(i);
			sb.append("#" + (i+1) + ":" + username + " ($" + donated.get(username) + ")\n");
		}
		return sb.toString();
	}

	
	public static void init() {
		donated = SerializableFilesManager.loadMTopDonator();
		if (donated == null)
			donated = new HashMap<String, Double>();//all donators
	}

	public static final void save() {
		SerializableFilesManager.saveMTopDonator(donated);
	}

	public static void showRanks(Player player) {
		
		List<String> sorted = donated.entrySet().stream()
                .sorted(reverseOrder(comparing(Entry::getValue)))
                .map(Entry::getKey)
                .collect(toList());
		
		
		for (int i = 0; i < 310; i++)
			player.getPackets().sendIComponentText(275, i, "");
		for (int i = 0; i < Math.min(300, sorted.size()); i++) {
			String username = sorted.get(i);
			Double amount = donated.get(username);
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
									+ Utils.formatPlayerNameForDisplay(username)
									+ (player.getRights() == 2 ? " - donated: " + amount : ""));
		}
		player.getPackets().sendIComponentText(275, 1,
				"Monthly Top Donators Table");
		player.getInterfaceManager().sendInterface(275);
	}


	public static void add(Player player, double amount) {
		if (player.getRights() == 2 && Settings.HOSTED)
			return;
		Double amt = donated.get(player.getUsername());
		if (amt == null)
			amt = 0d;
		donated.put(player.getUsername(), amt += amount);
	}
	
	public static void resetMTopDonator() {
		donated.clear();
	}
	
}
