package com.rs.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.rs.Settings;
import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.game.player.content.NPCKillLog;

public class BossTimerScore implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5075651404420947584L;
	
	private String name;
	private long timer;
	
	private BossTimerScore(Player player, long timer) {
		name = player.getUsername();
		this.timer = timer;
	}
	
	
	private static Map<String, BossTimerScore> bosses;
	
	public static void init() {
		bosses = SerializableFilesManager.loadBossTimers();
		if (bosses == null)
			bosses = new HashMap<String, BossTimerScore>();
	}

	public static final void save() {
		SerializableFilesManager.saveBossTimers(bosses);
	}
	
	public static void remove(String key) {
		bosses.remove(key.toLowerCase());
	}
	
	public static void check(Player player, String key, long timer) {
		if (player.getRights() == 2 && Settings.HOSTED)
			return;
		BossTimerScore score = bosses.get(key);
		if (score == null || score.timer > timer) {
			bosses.put(key, new BossTimerScore(player, timer));
			boolean completed = key.equalsIgnoreCase("Theatre of Blood");
			String text = String.format("%d:%02d",
	                TimeUnit.MILLISECONDS.toMinutes(timer),
	                TimeUnit.MILLISECONDS.toSeconds(timer) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timer)));
	//		World.sendNews("BOSS RECORD! "+player.getName()+" just "+(completed ? "completed" : "killed")+" "+Utils.formatPlayerNameForDisplay(key)+" in <col=FF0040>"+text+"<col=FFFF00>!", World.GAME_NEWS);
		}
	}
	
	public static void show(Player player) {
		String[] lines = new String[NPCKillLog.BOSS_NAMES.length];
		int count = 0;
		for (String boss : NPCKillLog.BOSS_NAMES) {
			if (boss.equalsIgnoreCase("Barrows Chests")) //no way to time this atm
				continue;
			BossTimerScore score = bosses.get(boss.toLowerCase());
			String text = score == null ? "": String.format("%d:%02d",
	                TimeUnit.MILLISECONDS.toMinutes(score.timer),
	                TimeUnit.MILLISECONDS.toSeconds(score.timer) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(score.timer)));
			lines[count++] = count+". <col=33cc33>"+boss+ " <img=7> "+(score == null ?"<col=ff0000>N/A" : ("<col=ff9900>"+Utils.formatPlayerNameForDisplay(score.name).toUpperCase()+" <col=ff0000>("+text+")"));
		}
		NPCKillLog.sendQuestTab(player, "<img=6>Boss - Fastest Kills Hiscores<img=6>", lines);
	}

}
