package com.rs.game.player.content.questTab;

import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.World;
import com.rs.game.minigames.*;
import com.rs.game.minigames.pktournament.PkTournament;
import com.rs.game.player.Achievements.Difficulty;
import com.rs.game.player.Achievements.Task;
import com.rs.game.player.FriendsIgnores;
import com.rs.game.player.Player;
import com.rs.game.player.Presets.Preset;
import com.rs.game.player.QuestManager.Quests;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.PlayerCombat;
import com.rs.game.player.content.AdventurersLog;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.NPCKillLog;
import com.rs.game.player.content.Slayer.SlayerMaster;
import com.rs.game.player.content.seasonalEvents.Easter2021;
import com.rs.game.player.content.seasonalEvents.XmasBoss;
import com.rs.utils.Utils;
import com.rs.utils.WeaponTypesLoader;
import com.rs.utils.WeaponTypesLoader.WeaponType;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class QuestTab {
	
	public static void refresh(Player player, boolean force) {
		
		if (force) {
			player.getPackets().sendIComponentSprite(3002, 62, 21536);
			player.getPackets().sendIComponentSprite(3002, 61, 21454);
			player.getPackets().sendIComponentSprite(3002, 60, 21522);
			player.getPackets().sendIComponentSprite(3002, 59, 21486);
			player.getPackets().sendIComponentSprite(3002, 26, 21538);

			player.getPackets().sendHideIComponent(3002, 58, true);
			player.getPackets().sendHideIComponent(3002, 27, true);
		}
		
		List<String> lines = new ArrayList<String>();
		String title;
		Integer page = (Integer) player.getTemporaryAttributtes().get(Key.QUEST_JOURNALS_PAGE);
		if (page == null)
			page = 0;
		if (page == 4) {
			title = "Presets";
			player.getPackets().sendIComponentText(3002, 25, "Sets:<col=00FF00>"+player.getPresets().getSetsCount()+"/"+player.getPresets().getPresets().length);
			for (Preset preset : player.getPresets().getPresets()) {
				lines.add(preset == null ? "<col=b3b3cc>Preset" : ("<col=4da6ff>"+preset.getName()));
			}
		} else if (page == 3) {
			title = "Completion";
			int type = 0;
			int completedCount = 0;
			//String[] lines = new String[AdventurersLog.REQUIREMENTS.length];
			lines.add("<col=ff6600>-Normal-");
			for (int i = 0; i < AdventurersLog.REQUIREMENTS.length; i++) {
				if (type == 0 && AdventurersLog.REQUIREMENTS[i].contains("[Trim]")) {
					lines.add("<col=ff6600>-Trim-");
					type++;
				}else if (type == 1 && AdventurersLog.REQUIREMENTS[i].contains("[Elite]")) {
					lines.add("<col=ff6600>-Elite-");
					type++;
				}
				boolean completed = AdventurersLog.hasRequirement(player, i);
				if (completed)
					completedCount++;
				lines.add("<col="+(completed ? "00FF00" : "FF0000")+">"+
				AdventurersLog.REQUIREMENTS[i].replace("[Trim]", "").replace("[Elite]", ""));
			}
			
			player.getPackets().sendIComponentText(3002, 25, "Completed: <col=00FF00>"+completedCount+"/"+AdventurersLog.REQUIREMENTS.length);
			
			
		} else if (page == 2) {
			title = "Tasks";
			int completedCount = 0;
			for (Difficulty difficulty : Difficulty.values()) {
				lines.add("<col=ff6600>-"+Utils.formatPlayerNameForDisplay(difficulty.toString())+"-");
				for (Task task : Task.values()) {
					if (task.getDifficulty() == difficulty) {
						boolean completed = player.getAchievements().isTaskCompleted(task);
						if (completed)
							completedCount++;
						lines.add("<col="+(completed ? "00FF00" : player.getAchievements().isTaskStarted(task) ? "ffff00" : "FF0000")+">"+Utils.formatPlayerNameForDisplay(task.toString())
						+ " ("+player.getAchievements().getTaskProgress(task)+"/"+task.getAmount()+")");
					}
				}
			}
			player.getPackets().sendIComponentText(3002, 25, "Completed: <col=00FF00>"+completedCount+"/"+Task.values().length);
		} else if (page == 1) {
			title = "Quests";
			lines.add(player.getQuestManager().getQuest("Dragon Slayer", Quests.DRAGON_SLAYER));
			lines.add(player.getQuestManager().getQuest("The Hunt for Surok", Quests.THE_HUNT_FOR_SUROK));
			lines.add(player.getQuestManager().getQuest("The Great Brain Robbery", Quests.THE_GREAT_BRAIN_ROBBERY));
			lines.add(player.getQuestManager().getQuest("In Pyre Need", Quests.IN_PYRE_NEED));
			lines.add(player.getQuestManager().getQuest("Heroes' Quest", Quests.HEROES_QUEST_2));
			lines.add(player.getQuestManager().getQuest("Nomad's Requiem", Quests.NOMADS_REQUIEM));
			int completedCount = 0;
			if (player.getQuestManager().completedQuest(Quests.DRAGON_SLAYER))
				completedCount++;
			if (player.getQuestManager().completedQuest(Quests.THE_HUNT_FOR_SUROK))
				completedCount++;
			if (player.getQuestManager().completedQuest(Quests.THE_GREAT_BRAIN_ROBBERY))
				completedCount++;
			if (player.getQuestManager().completedQuest(Quests.IN_PYRE_NEED))
				completedCount++;
			if (player.getQuestManager().completedQuest(Quests.HEROES_QUEST_2))
				completedCount++;
			if (player.getQuestManager().completedQuest(Quests.NOMADS_REQUIEM))
				completedCount++;
			player.getPackets().sendIComponentText(3002, 25, "Completed: <col=00FF00>"+completedCount+"/"+6);
			

		} else {
			title = "Journal";
			lines.add("<col=00FF00>-General-");
			
			 Calendar calendar = Calendar.getInstance();
		     int hour = calendar.get(Calendar.HOUR_OF_DAY);
		     int minutes = calendar.get(Calendar.MINUTE);
		     int seconds = calendar.get(Calendar.SECOND);

			lines.add("<col=ff6600>Server time: <col=ffff00>"+(hour < 10 ? "0" : "")+hour + ":" + (minutes < 10 ? "0" : "")+minutes + ":" + (seconds < 10 ? "0" : "")+seconds);
			lines.add("<col=ff6600>Play time: <col=ffff00>"+Utils.longFormat(player.getTotalOnlineTime()));
			lines.add("<col=ff6600>Game mode: <col=ffff00>"+player.getGameMode()  );
			  DecimalFormat df = new DecimalFormat("#.##");
			lines.add("<col=ff6600>" +
					"Drop rate: <col=ffff00>"+df.format(player.getDropRateMultiplierI())+"x");
			lines.add("<col=ff6600>Kills: <col=ffff00>"+player.getKillCount()  );
			lines.add("<col=ff6600>Deaths: <col=ffff00>"+player.getDeathCount() );

			String timeUntilTourney = PkTournament.getQuestTabString();
			lines.add("<col=00FF00>-Donator-");
			lines.add("<col=ff6600>Donor Rank: <col=ffff00>"+player.getRank());
			lines.add("<col=ff6600>Total Spent: <col=ffff00>"+player.getDonated()+"$"  );


			lines.add("<col=00FF00>-Events-");
			lines.add("<col=ff6600>PK Tournament: " + timeUntilTourney);
			lines.add("<col=ff6600>Skill Of Day: <col=ffff00>"+Skills.SKILL_NAME[World.getSkillOfTheDay()]);
			lines.add("<col=ff6600>Boss Of Day: <col=ffff00>"+NPCKillLog.BOSS_NAMES[World.getBossOfTheDay()] );
			lines.add("<col=ff6600>Wishing well: "+(World.isWishingWellActive() ? ("<col=00cc66>"+Utils.longFormat(World.getWishingWellRemaining())) : "<col=FF0040>Inactive"));
			if(Easter2021.ENABLED) {
				lines.add("<col=990099>Easter bunny: " + (Easter2021.isEventActive() ? "<col=ff981f><shad=ffff00>At home!" : Utils.formatTime(Easter2021.getNextSpawnTime())));
			}
			//lines.add("<col=990099>Halloween Boss: " + (HalloBoss.isBossAlive() ? "<col=ff981f><shad=ffff00>Alive!" : Utils.formatTime(HalloBoss.getNextSpawnTime())));
			//lines.add("<col=ff0000>Christmas Boss: " + (XmasBoss.isBossAlive() ? "<col=ff981f><shad=ffff00>Alive!" : Utils.formatTime(XmasBoss.getNextSpawnTime())));
			lines.add("<col=ff6600>World Boss: "+(!WorldBosses.isBossAlive() ? ("<col=FF0040>"+Utils.longFormat(WorldBosses.getNextSpawnTime())) : "<col=00cc66>Spawned"));
			lines.add("<col=ff6600>Vote World Boss: "+(!VoteWorldBoss.isBossAlive() ? ("<col=FF0040>"+Utils.longFormat(VoteWorldBoss.getNextSpawnTime())) : "<col=00cc66>Spawned"));
			lines.add("<col=ff6600>Wild World Boss: "+(!WildernessBoss.isBossAlive() ? ("<col=FF0040>"+Utils.longFormat(WildernessBoss.getNextSpawnTime())) : "<col=00cc66>Spawned"));
			lines.add("<col=ff6600>Evil Tree: "+(!EvilTrees.isAlive() ? ("<col=FF0040>"+Utils.longFormat(EvilTrees.getNextTree())) : "<col=00cc66>Spawned"));
			lines.add("<col=ff6600>Shooting Star: "+(!	ShootingStars.getStarSprite().isAlive()
					? ("<col=FF0040>"+Utils.longFormat(ShootingStars.getStarSprite().getNextStar())) : "<col=00cc66>Spawned"));

			
			lines.add("<col=00FF00>-Slayer-");
			lines.add("<col=ff6600>Normal Task: <col=ffff00>"+(player.getSlayerManager().getCurrentTask() == null ? "N/A" : (player.getSlayerManager().getCurrentTask().getName())
					+ (player.getSlayerManager().getCurrentMaster() == SlayerMaster.KRYSTILIA ? " (Wild)" : "")));
			lines.add("<col=ff6600>Remaining: <col=ffff00>"+(player.getSlayerManager().getCurrentTask() == null ? "0" : player.getSlayerManager().getCount())  );
			lines.add("<col=ff6600>Boss Task: <col=ffff00>"+(player.getSlayerManager().getBossTask() == null ? "N/A" :  Utils.formatPlayerNameForDisplay(player.getSlayerManager().getBossTask())));
			lines.add("<col=ff6600>Remaining: <col=ffff00>"+(player.getSlayerManager().getBossTask() == null ? "0" :  player.getSlayerManager().getBossTaskRemaining() ) );
			lines.add("<col=ff6600>Points: <col=ffff00>"+player.getSlayerManager().getPoints()  );
			lines.add("<col=00FF00>-Statistics-");
			PlayerCombat combat = new PlayerCombat(null);
			WeaponType type = WeaponTypesLoader.getWeaponDefinition(player.getEquipment().getWeaponId());
			int maxHit = combat.getMaxHit(player, player.getEquipment().getWeaponId(), player.getCombatDefinitions().getAttackStyle(), type.getType() == Combat.RANGE_TYPE, false, 1);
			lines.add("<col=ff6600>Max hit: <col=ffff00>"+ maxHit  );
			lines.add("<col=ff6600>Kills: <col=ffff00>"+ player.getKillCount()  );
			lines.add("<col=ff6600>Deaths: <col=ffff00>"+player.getDeathCount() );
			lines.add("<col=00FF00>-Minigames-");
			lines.add("<col=ff6600>PC Points: <col=ffff00>"+ player.getPestPoints()  );
			lines.add("<col=ff6600>SC Points: <col=ffff00>"+player.getStealingCreationPoints() );
			lines.add("<col=ff6600>Dominion Factor: <col=ffff00>"+player.getDominionTower().getDominionFactor() );
			lines.add("<col=00FF00>-Online Staff-");
			List<Player> staff = World.getOnlineStaff();
			
			int staffOn = 0;
			for (int idx = 0; idx < staff.size(); idx++) {
				Player p2 = staff.get(idx);
				if (p2 == null || p2.getFriendsIgnores().getPmStatus() == FriendsIgnores.PM_STATUS_OFFLINE)
					continue;
				int icon = p2.getMessageIcon();
				lines.add("<img=" + (icon <= 2 ? icon - 1 : icon) + "> " + p2.getDisplayName());
				staffOn++;
			}
			if (staffOn == 0)
				lines.add("None are currently online.");
			player.getPackets().sendIComponentText(3002, 25, "Players Online: <col=00FF00>"+World.getPlayerCount());
		}
		
		if (force)
			player.getPackets().sendIComponentText(3002, 24, title);
		int i = 0;
		for (int k = 3; k <= 56; k++) {
			if (k == 23)
				k = 28;
			String line = i < lines.size() ? lines.get(i) : "";
			String oldLine = player.getJournalLines() != null && i < player.getJournalLines().size()
					? player.getJournalLines().get(i) : "";
			if (force || !line.equals(oldLine))
				player.getPackets().sendIComponentText(3002, k, line);
			i++;
		}
		player.setJournalLines(lines);
	}
	
	public static void set(Player player, int page) {
		Integer oldPage = (Integer) player.getTemporaryAttributtes().get(Key.QUEST_JOURNALS_PAGE);
		if (oldPage == null)
			oldPage = 0;
		if (page == oldPage)
			return;
		player.getTemporaryAttributtes().put(Key.QUEST_JOURNALS_PAGE, page);
		refresh(player, true);
		if (page == 3) {
			player.getPackets().sendGameMessage("<col=FFBF00>" + player.getDominionTower().getKilledBossesCount() + "/500 dominion tower boss kills.");
			player.getPackets().sendGameMessage("<col=FFBF00>" + player.getBossTasksCompleted() + "/30 boss tasks.");
			player.getPackets().sendGameMessage("<col=FFBF00>" + player.getTreasureTrailsManager().getCluesCompleted()+ "/10 treasure trails.");
			player.getPackets().sendGameMessage("<col=FFBF00>" + player.getWonTrivias() + "/10 trivia sessions.");
			player.getPackets().sendGameMessage("<col=FFBF00>" + Utils.getFormattedNumber(player.getThrownWishingCoins()) + "/500.000.000 coins given away.");
			player.getPackets().sendGameMessage("<col=FFBF00>" + Utils.getFormattedNumber(player.getDropPartyValue()) + "/1.000.000.000 hosted on a drop party.");
			
		}
	}
	
	public static void handleInterface(Player player, int componentId) {
		if ((componentId >= 59 && componentId <= 62) || componentId == 26) 
			set(player, componentId == 26 ? 4 : (62 - componentId));
		else if (componentId >= 3 && componentId <= 12) {
			int line = componentId - 3;
			Integer page = (Integer) player.getTemporaryAttributtes().get(Key.QUEST_JOURNALS_PAGE);
			if (page == null)
				page = 0;
			if (page == 4) { //presets
				if (line >= player.getPresets().getPresets().length)
					return;
				if (player.getPresets().getPresets()[line] == null) {
					player.stopAll(true, false);
					
					int count = player.getPresets().getSetsCount();
					if (count > 2 && !player.isDonator()) {
						player.getPackets().sendGameMessage("You need to be a donator to use over 2 presets.");
						return;
					}
					if (count > 5 && !player.isExtremeDonator()) {
						player.getPackets().sendGameMessage("You need to be an extreme donator to use over 5 presets.");
						return;
					}
					player.getPackets().sendInputNameScript("Enter the preset name you'd like to create:");
					player.getTemporaryAttributtes().put(Key.MAKE_PRESET, line);
				} else {
					player.stopAll();
					player.getDialogueManager().startDialogue("LoadPresetD", line);
				}
			}
		}
	}

}
