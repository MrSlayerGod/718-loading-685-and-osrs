package com.rs.game.player.content;

import com.rs.Settings;
import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.player.Player;
import com.rs.game.player.dialogues.Dialogue;

public class GameMode {

	public static int getSelectedGameMode() {
		return Player.NORMAL;
	}
	
	public static void open(Player player) {
		player.getInterfaceManager().sendInterface(1015);
		player.getPackets().sendIComponentText(1015, 107, "Select game mode");
		player.getPackets().sendIComponentText(1015, 156, "<col=00cc00>Confirm");
		player.getPackets().sendIComponentText(1015, 31, "Game mode");
		player.getPackets().sendIComponentText(1015, 11, "Selected mode:");
		//player.getPackets().sendHideIComponent(1015, 151, true);
		//player.getPackets().sendHideIComponent(1015, 152, true);
		//player.getPackets().sendHideIComponent(1015, 153, true);
		//player.getPackets().sendHideIComponent(1015, 154, true);
		player.getPackets().sendHideIComponent(1015, 157, true);
		player.getPackets().sendHideIComponent(1015, 158, true);
		player.getPackets().sendHideIComponent(1015, 159, true);
		player.getPackets().sendHideIComponent(1015, 160, true);
		player.getPackets().sendHideIComponent(1015, 161, true);
		player.getPackets().sendHideIComponent(1015, 162, true);
		player.getPackets().sendHideIComponent(1015, 108, true);
		player.getPackets().sendHideIComponent(1015, 109, true);
		player.getPackets().sendHideIComponent(1015, 4, true);
		updateDescription(player);
		player.setCloseInterfacesEvent(new Runnable() {

			@Override
			public void run() {
				Integer remove = (Integer) player.getTemporaryAttributtes().get(Key.GAME_MODE);
				player.setMode(remove == null ? Player.NORMAL : remove);
			}
			
		});
	}
	
	public static void updateDescription(Player player) {
		Mode selecteData = getMode(getMode(player)); 
		int i = 0;
		for (Mode mode : Mode.values()) {
			player.getPackets().sendIComponentText(1015, 144 + (i++*2), (mode == selecteData ? "<col=ffffff>" : "") + mode.name);
		}
		player.getPackets().sendIComponentText(1015, 12, selecteData.name);
		player.getPackets().sendIComponentText(1015, 135, "<col=ffff00>"+selecteData.description1);
		player.getPackets().sendIComponentText(1015, 136, selecteData.description2);
	}
	
	public static void selectOption(Player player, int option) {
		if (option == 0)
			selectMode(player, Player.NORMAL);
		else if (option == 1)
			selectMode(player, Player.EXPERT);
		else if (option == 2)
			selectMode(player, Player.DEADMAN);
		else if (option == 3)
			selectMode(player, Player.IRONMAN);
		else if (option == 4)
			selectMode(player, Player.ULTIMATE_IRONMAN);
		else if (option == 5)
			selectMode(player, Player.HARDCORE_IRONMAN);
		else if (option == 6)
			confirm(player);
	}
	
	public static void selectMode(Player player, int mode) {
		setMode(player, mode);
		updateDescription(player);
	}
	
	public static void confirm(Player player) {
		player.closeInterfaces();
		Dialogue.sendNPCDialogueNoContinue(player, 946, Dialogue.HAPPY, "Next select your graphic settings!", "Close the interface once your finished.");
		ExtraSettings.open(player);
		player.setCloseInterfacesEvent(new Runnable() {
			@Override
			public void run() {
				player.getInterfaceManager().sendInterface(742);
				player.setCloseInterfacesEvent(new Runnable() {
					@Override
					public void run() {
						Dialogue.closeNoContinueDialogue(player);
						player.setCantWalk(false);
						player.getCutscenesManager().play("HomeCutScene3");
						/*Dialogue.sendNPCDialogueNoContinue(player, 946, Dialogue.HAPPY, "We're almost done!", "Please select your appearence!");
					//	player.getCutscenesManager().play("HomeCutScene3");
						PlayerLook.openMageMakeOver(player);
						player.setCloseInterfacesEvent(new Runnable() {
							@Override
							public void run() {
								player.getPackets().sendIComponentText(309, 3, Settings.SERVER_NAME+"'s Hairstyle");
								PlayerLook.openHairdresserSalon(player);
							}
						});*/
					}
				});
			}
		});
		/*PlayerLook.openMageMakeOver(player);
		player.setCloseInterfacesEvent(new Runnable() {
			@Override
			public void run() {
			//	player.getCutscenesManager().play("HomeCutScene3");
				PlayerLook.openHairdresserSalon(player);
			}
		});*/
	}
	
	public static int getMode(Player player) {
		Integer mode = (Integer) player.getTemporaryAttributtes().get(Key.GAME_MODE);
		return mode == null ? Player.NORMAL : mode;
	}
	
	public static void setMode(Player player, int mode) {
		player.getTemporaryAttributtes().put(Key.GAME_MODE, mode);
	}
	
	public static Mode getMode(int id) {
		for (Mode mode : Mode.values())
			if (mode.id == id)
				return mode;
		return Mode.NORMAL;
	}
	
	private static enum Mode {
		NORMAL(Player.NORMAL, "Normal", "This mode is the easiest on Matrix, granting players fastest XP rates while having a standard drop rate."
				, "X400 Combat XP<br>X40 Skilling XP<br>100% Drop Rate"),
		EXPERT(Player.EXPERT, "Expert", "This mode is the hardest on Matrix, granting players slow XP while having a higher drop rate."
				, "X10 Combat XP<br>X5 Skilling XP<br>115% Drop Rate"),
		DEADMAN(Player.DEADMAN, "Deadman Mode", "Open PvP environment, with some exceptions. If you kill another deadman you'll be rewarded with one of their items."
				, "X1000 Combat XP<br>X40 Skilling XP<br>100% Drop Rate<br>Worldwide PVP Enabled<br>Drop 1 Item on PK Death"),
		IRONMAN(Player.IRONMAN, "Ironman", "Looking for a challenge? Lets see how well you can be self sufficient."
				, "X40 Combat XP<br>X20 Skilling XP<br>105% Drop Rate<br>Group Mode Option"),
		ULTIMATE_IRONMAN(Player.ULTIMATE_IRONMAN, "Ultimate Ironman", " Ironman mode but without banking."
				, "X20 Combat XP<br>X10 Skilling XP<br>110% Drop Rate<br>Group Mode Option<br>Banking Disabled"),
		HARDCORE_IRONMAN(Player.HARDCORE_IRONMAN, "Hardcore Ironman", " Ironman mode but with one life."
				, "X20 Combat XP<br>X10 Skilling XP<br>110% Drop Rate<br>Group Mode Option<br>One Life")
		;
		
		private int id;
		private String name;
		private String description1, description2;
		private Mode(int id, String name, String description1, String description2) {
			this.id = id;
			this.name = name;
			this.description1 = description1;
			this.description2 = description2;
		}
	}
}
