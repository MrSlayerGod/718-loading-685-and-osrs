package com.rs.game.player.controllers;

import java.util.ArrayList;

import com.rs.Settings;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.content.EconomyManager;
import com.rs.game.player.content.FriendsChat;
import com.rs.game.player.content.GameMode;
import com.rs.game.player.content.PlayerLook;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colour;
import com.rs.utils.Utils;

public class NewPlayerController extends Controller {
	private static ArrayList<String> ips = new ArrayList<String>();
	private static ArrayList<String> macs = new ArrayList<String>();

	@Override
	public void start() {
		player.setNextWorldTile(Settings.START_PLAYER_LOCATION);
		player.getMusicsManager().playOSRSMusic("Upper Depths");
		player.getAppearence().setHidden(true);
		PlayerLook.openCharacterCustomizing(player);
		player.setCantWalk(true);
		/*WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				Dialogue.sendNPCDialogueNoContinue(player, 946, Dialogue.HAPPY, "Welcome to "+Settings.SERVER_NAME+"!", "Please select your game mode!");
			}
		});
		GameMode.open(player);*/
	/*	PlayerLook.openMageMakeOver(player);
		player.setCloseInterfacesEvent(new Runnable() {
			@Override
			public void run() {
			//	player.getCutscenesManager().play("HomeCutScene3");
				PlayerLook.openHairdresserSalon(player);
			}
		});*/
	}

	@Override
	public void process() {

	}

	@Override
	public boolean processObjectClick1(WorldObject object) {
		return false;
	}

	@Override
	public boolean processObjectClick2(WorldObject object) {
		return false;
	}

	@Override
	public boolean processObjectClick3(WorldObject object) {
		return false;
	}

	@Override
	public boolean processNPCClick1(NPC npc) {
		return false;
	}

	/*
	 * return remove controler
	 */
	@Override
	public boolean login() {
		start();
		return false;
	}

	/*
	 * return remove controler
	 */
	@Override
	public boolean logout() {
		return false;
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		return false;
	}

	@Override
	public boolean keepCombating(Entity target) {
		return false;
	}

	@Override
	public boolean canAttack(Entity target) {
		return false;
	}

	@Override
	public boolean canHit(Entity target) {
		return false;
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		return false;
	}

	@Override
	public boolean processObjectTeleport(WorldTile toTile) {
		return false;
	}

	
	@Override
	public void forceClose() {
		player.getAppearence().setHidden(false);
		if(player.checkBankPin())
			player.setCantWalk(false);
		Dialogue.closeNoContinueDialogue(player);
		String ip = player.getSession() != null ? player.getLastGameIp() : null;
		String mac = player.getSession() != null ? player.getLastGameMAC() : null;
		boolean newPlayer = false;
		if (ip != null && !ips.contains(ip)) {
			ips.add(ip);
			newPlayer = true;
		}
		if (mac != null && !macs.contains(mac)) {
			macs.add(mac);
			newPlayer = true;
		}
		//player.getHintIconsManager().removeUnsavedHintIcon();
		if (!player.isMuted() && !Settings.NEW_PLAYER_ANNOUNCEMENTS_DISABLED) {
			World.sendNews(player, "<col="+ Colour.ORANGE_RED.hex+">A new player has just joined!  Welcome <shad=ff7200><col=ffff00>"+player.getName()+"<shad=0><col="+ Colour.ORANGE_RED.hex+"> to <shad=0>"+Settings.SERVER_NAME+"!", World.GAME_NEWS);
		}
				/*if (!player.isOldItemsLook())  {
			player.getPackets().sendGameMessage("<col=0000FF>Do ::sil to switch items look. ::shl to switch hits look.");
			player.switchItemsLook();
		}*/
		FriendsChat.requestJoin(player, Utils.formatPlayerNameForDisplay("help"));
	//	player.getDialogueManager().startDialogue("SimpleNPCMessage", 13930, "You are currently under new player protection, this will last for 1 hour. As a result, you have been blessed with Double EXP rates, but you will be unable to interact with other players until the protection has run out.");
		player.getPackets().sendGameMessage("You notice that your inventory got heavier.");
		player.getInventory().addItem(new Item(1323, 1));
		player.getInventory().addItem(new Item(1333, 1));
		player.getInventory().addItem(new Item(4587, 1));
		//player.getInventory().addItem(new Item(25492, 1));
		player.getInventory().addItem(new Item(542, 1));
		player.getInventory().addItem(new Item(544));
		/*player.getInventory().addItem(new Item(1381, 1));
		if (newPlayer)
			player.getInventory().addItem(new Item(1401 , 1));
		player.getInventory().addItem(new Item(577, 1));
		player.getInventory().addItem(new Item(1011, 1));
		/*player.getInventory().addItem(new Item(841, 1));
		if (newPlayer)
			player.getInventory().addItem(new Item(861, 1));
		player.getInventory().addItem(new Item(1129, 1));
		player.getInventory().addItem(new Item(1095, 1));*/
		player.getInventory().addItem(new Item(1540, 1));
		player.getInventory().addItem(new Item(7455, 1));
		player.getInventory().addItem(new Item(newPlayer ? 3105 : 4121, 1));
	//	player.getInventory().addItem(new Item(13560, 1));
		player.getInventory().addItem(new Item(1712, 1));
		player.getInventory().addItem(new Item(/*4315 + Utils.random(26)*2*/1052, 1));
		//player.getInventory().addItem(new Item(10498, 1));
		/*player.getInventory().addItem(new Item(558, newPlayer ? 500 : 100));
		if (newPlayer)
			player.getInventory().addItem(new Item(562, 100));
		player.getInventory().addItem(new Item(554, newPlayer ? 500 : 100));
		player.getInventory().addItem(new Item(884, newPlayer ? 1000 : 100));
		if (newPlayer)
			player.getInventory().addItem(new Item(890, 100));*/
		//player.getInventory().addItem(new Item(1265, 1));
		//player.getInventory().addItem(new Item(1351, 1));
		//player.getInventory().addItem(new Item(303, 1));
		//player.getInventory().addItem(new Item(590, 1));
		//player.getInventory().addItem(new Item(340, 1000));
		player.getInventory().addItem(new Item(newPlayer ? 380 : 10137, 250));
		player.getInventory().addItem(new Item(995, newPlayer ? 900000 : 300000));
	//	player.getInventory().addItem(new Item(22302)); //wisdom aura
		player.getInventory().addItem(new Item(1856, 1));
		player.getMusicsManager().reset();
		if (!newPlayer)
			player.getPackets().sendGameMessage("Your starter inventory was lowered due to not being a new player.");
	}
}
