package com.rs.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.rs.Settings;
import com.rs.game.player.Player;
import com.rs.game.player.content.NPCKillLog;

public class BossKillsScore implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3059545128377646817L;
	
	private String name;
	private int kills;
	
	private BossKillsScore(Player player, int kills) {
		name = player.getUsername();
		this.kills = kills;
	}
	
	
	private static Map<String, BossKillsScore> bosses;
	
	public static void init() {
		bosses = SerializableFilesManager.loadBossKills();
		if (bosses == null)
			bosses = new HashMap<String, BossKillsScore>();
	}

	public static final void save() {
		SerializableFilesManager.saveBossKills(bosses);
	}
	
	public static void check(Player player, String key, int kills) {
		if (player.getRights() == 2 && Settings.HOSTED)
			return;
		BossKillsScore score = bosses.get(key);
		if (score == null || score.kills < kills) 
			bosses.put(key, new BossKillsScore(player, kills));
	}
	
	public static boolean hasRecord(Player player) {
		for (BossKillsScore score : bosses.values())
			if (score.name.equalsIgnoreCase(player.getUsername()))
				return true;
		return false;
			
	}
	
	public static void show(Player player) {
		String[] lines = new String[NPCKillLog.BOSS_NAMES.length];
		int count = 0;
		for (String boss : NPCKillLog.BOSS_NAMES) {
			BossKillsScore score = bosses.get(boss.toLowerCase());
			lines[count++] = count+". <col=33cc33>"+boss+ " <img=7> "+(score == null ?"<col=ff0000>N/A" : ("<col=ff9900>"+Utils.formatPlayerNameForDisplay(score.name).toUpperCase()+" <col=ff0000>("+score.kills+")"));
		}
		NPCKillLog.sendQuestTab(player, "<img=6>Boss - Kills Hiscores<img=6>", lines);
	}

}
