/**
 * 
 */
package com.rs.game.player.content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.game.player.content.Slayer.SlayerMaster;
import com.rs.game.player.content.Slayer.SlayerTask;
import com.rs.game.player.controllers.Wilderness;
import com.rs.utils.BossKillsScore;
import com.rs.utils.BossTimerScore;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Nov 20, 2017
 */
public class NPCKillLog {
	
	//TODO make exeption 4 barrows chests
	public static final String[] BOSS_NAMES = {"Kree'Arra", "Commander Zilyana", "General Graardor", "K'ril Tsutsaroth", "Nex", "Dagannoth Rex", "Dagannoth Prime", "Dagannoth Supreme", "Giant Mole", "Kalphite Queen", "King Black Dragon", "Queen Black Dragon", "Callisto", "Venenatis", "Vet'ion Reborn", "Chaos Elemental", "Chaos Fanatic", "Crazy Archaeologist", "Scorpia", "Barrows Chests", "Corporeal Beast", "Zulrah", "TzTok-Jad", "Har-Aken", "TzKal-Zuk", "Cerberus", "Kraken", "Thermonuclear smoke devil", "Abyssal Sire", "Skotizo", "Deranged archaeologist", "Vorkath", "Bork", "WildyWyrm", "Grotesque Guardians", "Barrelchest", "Phoenix", "Galvek", "Hati", "Skoll", "Theatre of Blood", "Matrix", "Alchemical Hydra", "Nomad", "Corrupted Wolpertinger",
			"The Nightmare", "Zalcano", /*"Callus",*/"Giant Mimic", "Enraged Kree'Arra", "Enraged General Graardor", "Enraged K'ril Tsutsaroth", "Enraged Nomad", "Enraged Zulrah"};
	
	public static void check(Player player) {
		if (player.isCompletedFightCaves() || player.containsItem(6570)) 
			setKilled(player, "TzTok-Jad");
		if (player.isCompletedFightKiln() || player.containsItem(23659)) 
			setKilled(player, "Har-Aken");
		if (player.containsItem(51295)) 
			setKilled(player, "TzKal-Zuk");
	}
	
	public static void setKilled(Player player, String boss) {
		Integer kills = player.getBossKillcount().get(boss.toLowerCase());
		if (kills == null)
			player.getBossKillcount().put(boss.toLowerCase(), 1);
	}

	public static Integer getKilled(Player player, String boss) {
		return player.getBossKillcount().getOrDefault(boss.toLowerCase(), 0);
	}
	
	private static final SlayerTask[] SLAYER_TASKS;
	
	static {
		List<SlayerTask> tasks = new ArrayList<SlayerTask>();
		for (SlayerMaster master : SlayerMaster.values()) {
			for (SlayerTask task : master.getTask()) {
				if (task == SlayerTask.WILDERNESS_BOSSES || task == SlayerTask.WILDERNESS_DEMIBOSSES)
					continue;
				if (!tasks.contains(task))
					tasks.add(task);
			}
		}
        Collections.sort(tasks, new Comparator<SlayerTask>() {
            @Override
            public int compare(SlayerTask t1, SlayerTask t2) {
                return t1.toString().compareTo(t2.toString());
            }
        });
		SLAYER_TASKS = tasks.toArray(new SlayerTask[tasks.size()]);
	}
	
	public static boolean hasBossKills(Player player, int count) {
		for (String boss : BOSS_NAMES) {
			Integer kills = player.getBossKillcount().get(boss.toLowerCase());
			if (kills == null || kills < count)
				return false;
		}
		return true;
	}
	
	public static boolean hasSlayerKills(Player player, int count) {
		for (SlayerTask task : SLAYER_TASKS) {
			Integer kills = player.getSlayerManager().getKillcount().get(task);
			if (kills == null || kills < count)
				return false;
		}
		return true;
	}
	
	public static void addKill(Player player, String name) {
		addKill(player, name, 0, true);
	}
	
	public static void addKill(Player player, String name, long time, boolean multi) {
		if (name.equalsIgnoreCase("dusk")) //grotesque guardians exception
			name = "Grotesque Guardians";


		var amountKilled = player.getNpcKillCountTracker().increment(name, 1);
		if (amountKilled % 10 == 0) {
			player.sendMessage(" You have killed <col=FF0040>" + amountKilled + "</col> " + name + "s.");
		}


		for (String boss : BOSS_NAMES) {
			if (name.equalsIgnoreCase(boss)) {
				String key = boss.toLowerCase();
				Integer kills = player.getBossKillcount().get(key);
				
				int next = kills == null ? 1 : (kills+1);
				player.getBossKillcount().put(key, next);
				BossKillsScore.check(player, key, next);
				player.getPackets().sendGameMessage("Your "+boss+" kill count is: <col=FF0040>"+next+".");
				if (time > 0) {
					Long bestTime = player.getBosskilltime().get(key);
					//(Personal best!)
					if ((bestTime == null || bestTime > time) && !multi) {
						player.getBosskilltime().put(key, bestTime = time);
						BossTimerScore.check(player, key, time);
					}
					String bestText = bestTime == null ? "N/A" : String.format("%d:%02d",
			                TimeUnit.MILLISECONDS.toMinutes(bestTime),
			                TimeUnit.MILLISECONDS.toSeconds(bestTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(bestTime)));
					String text = String.format("%d:%02d",
			                TimeUnit.MILLISECONDS.toMinutes(time),
			                TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
					//Theatre of Blood completion time:
					player.getPackets().sendGameMessage(
							(key.equalsIgnoreCase("Theatre of Blood") ? "Theatre of Blood completion time: " : "Fight duration: ")+"<col=FF0040>"+text+"</col>. "+(bestText.equals(text) ? "(Personal best!)" : ("Personal best: "+bestText)));
				}
				if (Utils.random(20) == 0 && DollarContest.winner == null) {
					int keyCount = key.equalsIgnoreCase("Theatre of Blood") || key.equalsIgnoreCase("TzKal-Zuk") ? 60 : key.equalsIgnoreCase("Nex") ? 20 : Wilderness.isAtWild(player) ? 8 : 4;
					player.setLastDollarKeyFragment();
					player.getInventory().addItemDrop(DollarContest.FRAGMENT_ID, keyCount);
					player.getPackets().sendGameMessage("You receive "+keyCount+" pandora key fragments from "+name+".");
				}
				break;
			}
		}

		for (SlayerTask task : SLAYER_TASKS) {
			List<SlayerTask> tasks = new LinkedList<SlayerTask>(Arrays.asList(task.getAlternatives()));
			tasks.add(task);
			name = name.replace("'", "");
			for (SlayerTask currentTask : tasks) {
				if (name.toLowerCase().contains(currentTask.toString().replace("_", " ").toLowerCase())) {
					Integer kills = player.getSlayerManager().getKillcount().get(task);
					player.getSlayerManager().getKillcount().put(task, kills == null ? 1 : (kills+1));
		//			break tasksL;
				}
			}
		}
	}
	
	public static void sendBossLog(Player player) {
		String[] lines = new String[BOSS_NAMES.length];
		int count = 0;
		for (String boss : BOSS_NAMES) {
			Integer kills = player.getBossKillcount().get(boss.toLowerCase());
			lines[count++] = count+". <col=33cc33>"+boss+ " <img=7><col=ff0000> "+(kills == null ? 0 : kills);
		}
		sendQuestTab(player, "<img=6>Boss - Kills Log<img=6>", lines);
	}
	
	
	public static void sendSlayerLog(Player player) {
		String[] lines = new String[Slayer.SlayerTask.values().length];
		int count = 0;
		for (SlayerTask task : SLAYER_TASKS) {
			Integer kills = player.getSlayerManager().getKillcount().get(task);
			lines[count++] = count+". <col=33cc33>"+Utils.formatPlayerNameForDisplay(task.getName())+ " <img=7><col=ff0000> "+(kills == null ? 0 : kills);
		}
		sendQuestTab(player, "<img=6>Slayer - Kills Log<img=6>", lines);
	}
	
	
	public static void sendQuestTab(Player player, String title, String... lines) {
		player.getInterfaceManager().sendInterface(275);
		player.getPackets().sendIComponentText(275, 1, title);
		for (int i = 0; i < 300; i++) 
			player.getPackets().sendIComponentText(275, i + 10, i >= lines.length || lines[i] == null ? "" : lines[i]);
	}
	
	public static void sendQuestTabBG(Player player, String title, String... lines) {
		player.getInterfaceManager().setScreenInterface(96, 275);
		player.setCloseInterfacesEvent(null);
		player.getPackets().sendIComponentText(275, 1, title);
		for (int i = 0; i < 300; i++) 
			player.getPackets().sendIComponentText(275, i + 10, i >= lines.length || lines[i] == null ? "" : lines[i]);
	}
}
