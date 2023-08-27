package com.rs.game.player.dialogues.impl;

import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.item.Item;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.player.Player;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.Utils;

public class BossInstanceD extends Dialogue {

	private Boss boss;
	
	

	@Override
	public void start() {
		boss = (Boss) parameters[0];
		if(boss.hasPublicVersion()) 
			sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Enter an encounter", "Start/Join custom encounter");
		else {
			sendCustomEncounter();
		}
			
	}

	public static void sendInstanceSystem(Player player) {
		player.getInterfaceManager().sendInterface(3086);
		InstanceSettings settings = player.getLastBossInstanceSettings();
		settings.setMaxPlayers(settings.getBoss().getMaxPlayers());
		settings.setMinCombat(1);
		settings.setSpawnSpeed(BossInstance.STANDARD); //fastest
		settings.setProtection(BossInstance.FFA);

		player.getPackets().sendHideIComponent(3086, 118, !settings.getBoss().isHasHM());
		player.getPackets().sendHideIComponent(3086, 120, !settings.getBoss().isHasHM());
		player.getPackets().sendIComponentText(3086, 104, settings.getBoss().name().replace("_"," "));
		player.getPackets().sendItemOnIComponent(3086, 48, 995, settings.getBoss().getInitialCost());
		refreshMaxPlayers(player);
		refreshMinCombatLevel(player);
		refreshProtection(player);
		refreshPracticeMode(player);
		refreshHardMode(player);
	}

	public static void refreshMaxPlayers(Player player) {
		player.getPackets().sendIComponentText(3086, 58, Integer.toString(player.getLastBossInstanceSettings().getMaxPlayers()));
	}

	public static void refreshMinCombatLevel(Player player) {
		player.getPackets().sendIComponentText(3086, 71, Integer.toString(player.getLastBossInstanceSettings().getMinCombat()));
	}

	public static void refreshSpawnSpeed(Player player) {
		player.getPackets().sendIComponentText(3086, 84, player.getLastBossInstanceSettings().getSpawnSpeed() == BossInstance.STANDARD ? "Standard" : player.getLastBossInstanceSettings().getSpawnSpeed() == BossInstance.FAST ? "Fast" : "Fastest");
	}

	public static void refreshProtection(Player player) {
		player.getPackets().sendIComponentText(3086, 97, player.getLastBossInstanceSettings().getProtection() == BossInstance.FFA ? "FFA" : player.getLastBossInstanceSettings().getProtection() == BossInstance.PIN ? "Pin" : "Friends Only");
	}
	public static void refreshPracticeMode(Player player) {
		player.getPackets().sendIComponentSprite(3086, 119, player.getLastBossInstanceSettings().isPractiseMode() ? 3302 : 3303);
	}
	public static void refreshHardMode(Player player) {
		player.getPackets().sendIComponentSprite(3086, 120, player.getLastBossInstanceSettings().isHardMode() ? 3302 : 3303);
	}

	public static void handleInstanceSystem(Player player, int componentID) {
		InstanceSettings settings = player.getLastBossInstanceSettings();
		if(settings == null)
			return;
		if (componentID == 105) {
			if (!settings.isPractiseMode()) {
				int initialCost = settings.getBoss().getInitialCost();
				if(player.getInventory().getCoinsAmount() < initialCost) {
					player.getPackets().sendGameMessage("You don't have enough coins to start this battle.");
					player.setLastBossInstanceSettings(null);
					return;
				}
				if(initialCost > 0)
					player.getInventory().removeItemMoneyPouch(new Item(995, initialCost));
			}
			settings.setCreationTime(Utils.currentTimeMillis());
			BossInstanceHandler.createInstance(player, settings);
		} else if (componentID == 109) {
			player.getTemporaryAttributtes().put(Key.JOIN_BOSS_INSTANCE, player.getLastBossInstanceSettings().getBoss());
			player.getPackets().sendInputNameScript("Enter the name of a player in a battle you wish to join.");
		} else if (componentID == 113) {
			String key = player.getLastBossInstanceKey();
			if(key == null) {
				player.sendMessage("You do not have a battle to rejoin.");
				return;
			}
			if(BossInstanceHandler.findInstance(player.getLastBossInstanceSettings().getBoss(), key) == null) {
				if(key.equals(player.getUsername()) && player.getLastBossInstanceSettings() != null && player.getLastBossInstanceSettings().hasTimeRemaining()) {
					//if the instance is null, and its my own player, use the settings to recreate it
					BossInstanceHandler.createInstance(player, player.getLastBossInstanceSettings());
					return;
				}
				player.sendMessage("You do not have a battle to rejoin.");
				return;
			}
			BossInstanceHandler.joinInstance(player, player.getLastBossInstanceSettings().getBoss(), key, false);
		} else if (componentID == 119) {
			settings.setPractiseMode(!settings.isPractiseMode());
			refreshPracticeMode(player);
		} else if (componentID == 120) {
			if (!settings.getBoss().isHasHM())
				return;
			settings.setHardMode(!settings.isHardMode());
			refreshHardMode(player);
		} else if (componentID == 60) {
			if (settings.getMaxPlayers() >= settings.getBoss().getMaxPlayers())
				return;
			settings.setMaxPlayers(settings.getMaxPlayers()+1);
			refreshMaxPlayers(player);
		} else if (componentID == 59) {
			if (settings.getMaxPlayers() <= 1)
				return;
			settings.setMaxPlayers(settings.getMaxPlayers()-1);
			refreshMaxPlayers(player);
		} else if (componentID == 73) {
			if (settings.getMinCombat() >= 138)
				return;
			settings.setMinCombat(settings.getMinCombat()+1);
			refreshMinCombatLevel(player);
		} else if (componentID == 72) {
			if (settings.getMinCombat() <= 1)
				return;
			settings.setMinCombat(settings.getMinCombat()-1);
			refreshMinCombatLevel(player);
		} else if (componentID == 86) {
			if (settings.getSpawnSpeed() >= BossInstance.FASTEST)
				return;
			settings.setSpawnSpeed(settings.getSpawnSpeed() == BossInstance.STANDARD ? BossInstance.FAST : BossInstance.FASTEST);
			refreshSpawnSpeed(player);
		} else if (componentID == 85) {
			if (settings.getSpawnSpeed() <= BossInstance.STANDARD)
				return;
			settings.setSpawnSpeed(settings.getSpawnSpeed() == BossInstance.FASTEST ? BossInstance.FAST : BossInstance.STANDARD);
			refreshSpawnSpeed(player);
		} else if (componentID == 99) {
			if (settings.getProtection() >= BossInstance.FRIENDS_ONLY)
				return;
			settings.setProtection(BossInstance.FRIENDS_ONLY);
			refreshProtection(player);
		} else if (componentID == 98) {
			if (settings.getProtection() <= BossInstance.FFA)
				return;
			settings.setProtection(BossInstance.FFA);
			refreshProtection(player);
		}
	}

	private void sendCustomEncounter() {
		end();
		player.setLastBossInstanceSettings(new InstanceSettings(boss));
		sendInstanceSystem(player);
	/*	stage = 0;
		sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Start", "Join", "Rejoin");
	*/}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			switch(componentId) {
				case OPTION_1:
					BossInstanceHandler.joinInstance(player, boss, "", false);
					end();
					break;
				default:
					sendCustomEncounter();
					break;
			}
			break;
		case 0:
			switch(componentId) {
				case OPTION_1:
					stage = 1;
					player.setLastBossInstanceSettings(new InstanceSettings(boss)); //the settings
					sendOptionsDialogue("Enable practise mode?", "Yes.", "No.");
					break;
				case OPTION_2:
					end();
					player.getTemporaryAttributtes().put(Key.JOIN_BOSS_INSTANCE, boss);
					player.getPackets().sendInputNameScript("Enter the name of a player in a battle you wish to join.");
					break;
				default:
					String key = player.getLastBossInstanceKey();
					if(key == null) {
						stage = -2;
						sendDialogue("You do not have a battle to rejoin.");
						return;
					}
					if(BossInstanceHandler.findInstance(boss, key) == null) {
						
						if(key.equals(player.getUsername()) && player.getLastBossInstanceSettings() != null && player.getLastBossInstanceSettings().getBoss() == boss && player.getLastBossInstanceSettings().hasTimeRemaining()) {
							end();
							//if the instance is null, and its my own player, use the settings to recreate it
							BossInstanceHandler.createInstance(player, player.getLastBossInstanceSettings());
							return;
						}
						
						stage = -2;
						sendDialogue("You do not have a battle to rejoin.");
						return;
					}
					end();
					BossInstanceHandler.joinInstance(player, boss, key, false);
					//You do not have a battle to rejoin.
					break;
			}
			break;
		case 1:
			if(player.getLastBossInstanceSettings() == null) {
				end();
				return;
			}
			player.getLastBossInstanceSettings().setPractiseMode(componentId == OPTION_1);
			if(boss.isHasHM()) {
				stage = 2;
				sendOptionsDialogue("Enable hard mode?", "Yes.", "No.");
				//sendOptionsDialogue("Choose a difficulty mode", "Normal", "Extreme");
			}else
				sendSelectMaxPlayers();
			break;
		case 2:
			if(player.getLastBossInstanceSettings() == null) {
				end();
				return;
			}
			player.getLastBossInstanceSettings().setHardMode(componentId == OPTION_1);
			sendSelectMaxPlayers();
			break;
		default:
			end();
			break;
		}

	}
	
	public void sendSelectMaxPlayers() {
		end();
		InstanceSettings settings = player.getLastBossInstanceSettings();
		if(settings == null) 
			return;
		settings.setMaxPlayers(settings.getBoss().getMaxPlayers());
		settings.setMinCombat(1);
		settings.setSpawnSpeed(BossInstance.FAST); //fastest
		settings.setProtection(BossInstance.FFA);
		startInstance();
		
	//	player.getTemporaryAttributtes().put(Key.JOIN_BOSS_INSTANCE, boss);
	//	player.getPackets().sendInputNameScript("Enter the minimum combat level.");
		
	}
	
	public void startInstance() {
		InstanceSettings settings = player.getLastBossInstanceSettings();
		if(settings == null) 
			return;
		if (!settings.isPractiseMode()/* && !player.isOnyxDonator()*/) {
			int initialCost = settings.getBoss().getInitialCost();
			if(player.getInventory().getCoinsAmount() < initialCost) {
				player.getPackets().sendGameMessage("You don't have enough coins to start this battle.");
				player.setLastBossInstanceSettings(null);
				return;
			}
			if(initialCost > 0)
				player.getInventory().removeItemMoneyPouch(new Item(995, initialCost));
		}
		settings.setCreationTime(Utils.currentTimeMillis());
		BossInstanceHandler.createInstance(player, settings);
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

}
