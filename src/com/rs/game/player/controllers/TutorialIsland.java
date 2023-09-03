package com.rs.game.player.controllers;

import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Inventory;

import com.rs.game.player.Skills;
import com.rs.game.player.actions.Smelting;
import com.rs.game.player.actions.mining.Mining;
import com.rs.game.player.actions.woodcutting.Woodcutting;
import com.rs.net.decoders.WorldPacketsDecoder;
import com.rs.net.decoders.handlers.ObjectHandler;

public final class TutorialIsland extends Controller {

	private static final int RUNESCAPE_GUIDE = 945;
	private static final int SURVIVAL_EXPERT = 943;
	private static final int MASTER_CHEF = 942;
	private static final int QUEST_GUIDE = 949;
	private static final int MINING_INSTRUCTOR = 948;
	private static final int COMBAT_INSTRUCTOR = 944;
	private static final int FINANCIAL_ADVISOR = 947;
	private static final int BROTHER_BRACE = 954;
	private static final int MAGIC_INSTRUCTOR = 946;
	private static final int BANKER = 953;
	private static final int FISHING_SPOT = 952;
	private static final int GIANT_RAT = 950;
	private static final int CHICKEN = 951;
	private static final int SKIPPY = 2795;

	@Override
	public void start() {
		if (getStage() < 6)
			player.getInterfaceManager().openGameTab(16);
		refreshStage();
		sendInterfaces();
	}
	
	@Override
	public boolean canEquip(int slotId, int itemId) {
		if (getStage() > 46) {
			if (itemId == 1205) {
				if (getStage() == 47)
					updateProgress(); //48
					} else if ((itemId == 1171 && player.getEquipment().getWeaponId() == 1277) || (itemId == 1277 && player.getEquipment().getShieldId() == 1171))
				 if (getStage() == 49)
					updateProgress();//50
			return true;
		}
		player.getPackets().sendGameMessage("You'll be told how to equip items later.");
		return false;
	}

	public int getStage() {
		if (getArguments() == null)
			setArguments(new Object[] { 0 }); // index 0 = stage
		return (Integer) getArguments()[0];
	}

	public void setStage(int stage) {
		getArguments()[0] = stage;
	}

	public void refreshStage() {
		int stage = getStage();
		if (stage == 0 || stage == 1 || stage == 2) {
			NPC guide = findNPC(RUNESCAPE_GUIDE);
			if (guide != null) // not saving icon as theres no need for more
								// than 1icon
				player.getHintIconsManager().addHintIcon(guide, 0, -1, false);
		} else if (stage == 3)
			player.getHintIconsManager().addHintIcon(3097, 3107, 0, 125, 4, 0, -1, false);
		else if (stage == 4 || stage == 11) {
			NPC survival = findNPC(SURVIVAL_EXPERT);
			if (survival != null) // not saving icon as theres no need for more
									// than 1icon
				player.getHintIconsManager().addHintIcon(survival, 0, -1, false);
		} else if (stage == 6)
			player.getHintIconsManager().addHintIcon(3099, 3095, 0, 150, 4, 0, -1, false);
		else if (stage == 12) {
			NPC spot = findNPC(FISHING_SPOT);
			if (spot != null)
				player.getHintIconsManager().addHintIcon(spot, 0, -1, false);
			
		} else if (stage == 18) {
			player.getHintIconsManager().addHintIcon(3089, 3092, 0, 125, 4, 0, -1, false);
		} else if (stage == 19) {
			player.getHintIconsManager().addHintIcon(3078, 3084, 0, 125, 4, 0, -1, false);
		} else if (stage == 20) {
			NPC chef = findNPC(MASTER_CHEF);
			if (chef != null)
				player.getHintIconsManager().addHintIcon(chef, 0, -1, false);
		} else if (stage == 22) {
			player.getHintIconsManager().addHintIcon(3075, 3081, 0, 125, 4, 0, -1, false);
		} else if (stage == 24) {
			player.getHintIconsManager().addHintIcon(3072, 3090, 0, 125, 4, 0, -1, false);
		} else if (stage == 28) {
			player.getHintIconsManager().addHintIcon(3086, 3126, 0, 125, 4, 0, -1, false);
		} else if (stage == 29) {
			NPC quest_guide = findNPC(QUEST_GUIDE);
			if (quest_guide != null)
				player.getHintIconsManager().addHintIcon(quest_guide, 0, -1, false);
		} else if (stage == 31) {
			NPC quest_guide = findNPC(QUEST_GUIDE);
			if (quest_guide != null)
				player.getHintIconsManager().addHintIcon(quest_guide, 0, -1, false);
		} else if (stage == 32) {
			player.getHintIconsManager().addHintIcon(3088, 3119, 0, 50, 4, 0, -1, false);
		} else if (stage == 33) {
			NPC mining_guide = findNPC(MINING_INSTRUCTOR); //TODO FIX THIS DOESNT SHOW UP FROM LADDER but if u sent the stage it works.. maybe add a timer?
			if (mining_guide != null)
			player.getHintIconsManager().addHintIcon(mining_guide, 0, -1, false);
		} else if (stage == 34) { //Prospecting rocks(TIN)
			player.getHintIconsManager().addHintIcon(3076, 9504, 0, 45, 4, 0, -1, false);
		} else if (stage == 35) { //Prospecting rocks(Copper)
			player.getHintIconsManager().addHintIcon(3086, 9501, 0, 45, 4, 0, -1, false);
		} else if (stage == 36) {
			System.out.println("MINING INSTRUCTOR"); //getting here 
				NPC mining_guide = findNPC(MINING_INSTRUCTOR); //TODO FIX THIS DOESNT SHOW UP FROM LADDER but if u sent the stage it works.. maybe add a timer?
				if (mining_guide != null)
				player.getHintIconsManager().addHintIcon(mining_guide, 0, -1, false);
		} else if (stage == 37) { //Mining tin
			player.getHintIconsManager().addHintIcon(3076, 9504, 0, 45, 4, 0, -1, false);
		} else if (stage == 38) { //Mining Copper
			player.getHintIconsManager().addHintIcon(3086, 9501, 0, 45, 4, 0, -1, false);
		} else if (stage == 39) { //Mining Copper
			player.getHintIconsManager().addHintIcon(3079, 9496, 0, 125, 4, 0, -1, false);
		} else if (stage == 40) {
			System.out.println("MINING INSTRUCTOR"); //getting here 
				NPC mining_guide = findNPC(MINING_INSTRUCTOR); //TODO FIX THIS DOESNT SHOW UP FROM LADDER but if u sent the stage it works.. maybe add a timer?
				if (mining_guide != null)
				player.getHintIconsManager().addHintIcon(mining_guide, 0, -1, false);
		} else if (stage == 41) { //Mining Copper
			player.getHintIconsManager().addHintIcon(3083, 9499, 0, 35, 4, 0, -1, false);
		} else if (stage == 43) { //LEAVE MINING AREA
			player.getHintIconsManager().addHintIcon(3094, 9502, 0, 125, 4, 0, -1, false);
		} else if (stage == 44) {
			System.out.println("MINING INSTRUCTOR"); //getting here 
				NPC combat_instructor = findNPC(COMBAT_INSTRUCTOR); //TODO FIX THIS DOESNT SHOW UP FROM LADDER but if u sent the stage it works.. maybe add a timer?
				if (combat_instructor != null)
				player.getHintIconsManager().addHintIcon(combat_instructor, 0, -1, false);
		} else if (stage == 48) {
			System.out.println("MINING INSTRUCTOR"); //getting here 
				NPC combat_instructor = findNPC(COMBAT_INSTRUCTOR); //TODO FIX THIS DOESNT SHOW UP FROM LADDER but if u sent the stage it works.. maybe add a timer?
				if (combat_instructor != null)
				player.getHintIconsManager().addHintIcon(combat_instructor, 0, -1, false);
		} else if (stage == 51) { //LEAVE MINING AREA
			player.getHintIconsManager().addHintIcon(3111, 9518, 0, 125, 4, 0, -1, false);
		} else if (stage == 52) {
			System.out.println("MINING INSTRUCTOR"); //getting here 
				NPC giant_rat = findNPC(GIANT_RAT); //TODO FIX THIS DOESNT SHOW UP FROM LADDER but if u sent the stage it works.. maybe add a timer?
				if (giant_rat != null)
				player.getHintIconsManager().addHintIcon(giant_rat, 0, -1, false);
		} else if (stage == 54) {
				NPC combat_instructor = findNPC(COMBAT_INSTRUCTOR); //TODO FIX THIS DOESNT SHOW UP FROM LADDER but if u sent the stage it works.. maybe add a timer?
				if (combat_instructor != null)
				player.getHintIconsManager().addHintIcon(combat_instructor, 0, -1, false);
		} else if (stage == 55) {
				NPC giant_rat = findNPC(GIANT_RAT); //TODO FIX THIS DOESNT SHOW UP FROM LADDER but if u sent the stage it works.. maybe add a timer?
				if (giant_rat != null)
				player.getHintIconsManager().addHintIcon(giant_rat, 0, -1, false);
		} else if (stage == 56) { //LEAVE COMBAT AREA
			player.getHintIconsManager().addHintIcon(3111, 9526, 0, 125, 4, 0, -1, false);
		} else if (stage == 57) { //BANK AREA
			player.getHintIconsManager().addHintIcon(3122, 3124, 0, 125, 4, 0, -1, false);
		} else if (stage == 58) { //BANK AREA
			player.getHintIconsManager().addHintIcon(3124, 3124, 0, 125, 4, 0, -1, false);
		} else if (stage == 59) {
			NPC advisor = findNPC(FINANCIAL_ADVISOR); //TODO FIX THIS DOESNT SHOW UP FROM LADDER but if u sent the stage it works.. maybe add a timer?
			if (advisor != null)
			player.getHintIconsManager().addHintIcon(advisor, 0, -1, false);
		} else if (stage == 60) { //BANK AREA
			player.getHintIconsManager().addHintIcon(3129, 3124, 0, 125, 4, 0, -1, false);
		} else if (stage == 61) {
			NPC Brother_brace = findNPC(BROTHER_BRACE);
			if (Brother_brace != null)
			player.getHintIconsManager().addHintIcon(Brother_brace, 0, -1, false);
		} else if (stage == 63) {
			NPC Brother_brace = findNPC(BROTHER_BRACE);
			if (Brother_brace != null)
			player.getHintIconsManager().addHintIcon(Brother_brace, 0, -1, false);
		} else if (stage == 66) {
			NPC Brother_brace = findNPC(BROTHER_BRACE);
			if (Brother_brace != null)
			player.getHintIconsManager().addHintIcon(Brother_brace, 0, -1, false);
		} else if (stage == 67) { //LEAVE CHURCH
			player.getHintIconsManager().addHintIcon(3122, 3102, 0, 125, 4, 0, -1, false);
		} else if (stage == 68) {
			NPC Magic_instructor = findNPC(MAGIC_INSTRUCTOR);
			if (Magic_instructor != null)
			player.getHintIconsManager().addHintIcon(Magic_instructor, 0, -1, false);
		} else if (stage == 70) {
			NPC Magic_instructor = findNPC(MAGIC_INSTRUCTOR);
			if (Magic_instructor != null)
			player.getHintIconsManager().addHintIcon(Magic_instructor, 0, -1, false);
		} else if (stage == 71) {
			NPC Chicken = findNPC(CHICKEN);
			if (Chicken != null)
			player.getHintIconsManager().addHintIcon(Chicken, 0, -1, false);
		} else if (stage == 72) {
			NPC Magic_instructor = findNPC(MAGIC_INSTRUCTOR);
			if (Magic_instructor != null)
			player.getHintIconsManager().addHintIcon(Magic_instructor, 0, -1, false);
		}
		sendInterfaces();
	}

	public NPC findNPC(int id) {
		// as it may be far away
		for (NPC npc : World.getNPCs()) {
			if (npc == null || npc.getId() != id)
				continue;
			return npc;
		}
		return null;
	}

	public void updateProgress() {
		setStage(getStage() + 1);
		System.out.println("Tut updateProgress------------------------- = " + getStage());
		if (getStage() == 1)
			player.getInterfaceManager().sendSettings();
		else if (getStage() == 2)
			player.getPackets().sendVar(1021, 0); // unflash
		else if (getStage() == 5) {
			player.getInterfaceManager().sendInventory();
			player.getInventory().unlockInventoryOptions();
			player.getHintIconsManager().removeUnsavedHintIcon();
		} else if (getStage() == 6)
			player.getPackets().sendVar(1021, 0); // unflash
		else if (getStage() == 7)
			player.getHintIconsManager().removeUnsavedHintIcon();
		else if (getStage() == 10) {
			System.out.println("Skill Tab Part");
			player.getInterfaceManager().sendSkills();
		} else if (getStage() == 11)
			player.getPackets().sendVar(1021, 0); // unflash
		else if (getStage() == 13 || getStage() == 21)
			player.getHintIconsManager().removeUnsavedHintIcon();
		else if (getStage() == 23) {
			System.out.println("Send Music Player?");
			player.getInterfaceManager().setWindowInterface(125, 187); // music
			player.getHintIconsManager().removeUnsavedHintIcon();
		} else if (getStage() == 24) {
			player.getPackets().sendVar(1021, 0); // unflash
		} else if (getStage() == 25) {
			player.getInterfaceManager().sendEmotes();
			player.getHintIconsManager().removeUnsavedHintIcon();
		} else if (getStage() == 26) {//was 27
			player.getPackets().sendVar(1021, 0); // unflash
		} else if (getStage() == 28) {
			player.getPackets().sendVar(1021, 0); // run Orb?
		} else if (getStage() == 29) {
			player.getHintIconsManager().removeUnsavedHintIcon();
		} else if (getStage() == 30) {
			player.getInterfaceManager().sendQuestTab();
			player.getHintIconsManager().removeUnsavedHintIcon();
		} else if (getStage() == 31) {
			player.getPackets().sendVar(1021, 0); // unflash
		} else if (getStage() == 35) { //removes hint icon for prospecting iron rocks
			player.getHintIconsManager().removeUnsavedHintIcon();
		} else if (getStage() == 36) { //removes hint icon for prospecting iron rocks
			player.getHintIconsManager().removeUnsavedHintIcon();
		} else if (getStage() == 38) { //38
			player.getHintIconsManager().removeUnsavedHintIcon();
		} else if (getStage() == 39) { //38
			player.getHintIconsManager().removeUnsavedHintIcon();
		} else if (getStage() == 40) { //40
			player.getHintIconsManager().removeUnsavedHintIcon();
		} else if (getStage() == 41) { //40
			player.getHintIconsManager().removeUnsavedHintIcon();
		} else if (getStage() == 42) { //40
			player.getHintIconsManager().removeUnsavedHintIcon();
		} else if (getStage() == 43) { //40
			player.getHintIconsManager().removeUnsavedHintIcon();
		} else if (getStage() == 45) { //40
			player.getInterfaceManager().sendEquipment();
		} else if (getStage() == 46) {
			player.getPackets().sendVar(1021, 0); // unflash
		} else if (getStage() == 50) {
			player.getInterfaceManager().sendCombatStyles();
		} else if (getStage() == 51) {
			player.getPackets().sendVar(1021, 0); // unflash
		} else if (getStage() == 52) {
			player.getHintIconsManager().removeUnsavedHintIcon();
		} else if (getStage() == 54) {
			player.getHintIconsManager().removeUnsavedHintIcon();
		} else if (getStage() == 56) {
			player.getHintIconsManager().removeUnsavedHintIcon();
		} else if (getStage() == 57) {
			player.getHintIconsManager().removeUnsavedHintIcon();
		} else if (getStage() == 61) {
			player.getHintIconsManager().removeUnsavedHintIcon();
		} else if (getStage() == 62) {
			player.getInterfaceManager().sendPrayerBook();
		} else if (getStage() == 63) {
			player.getPackets().sendVar(1021, 0); // unflash
		} else if (getStage() == 64) {
			player.getInterfaceManager().setWindowInterface(120, 550); // friend list

		} else if (getStage() == 65) {
			player.getPackets().sendVar(1021, 0); // unflash
			player.getInterfaceManager().setWindowInterface(121, 1109);
		} else if (getStage() == 66) {
			player.getPackets().sendVar(1021, 0); // unflash
		} else if (getStage() == 68) {
			player.getHintIconsManager().removeUnsavedHintIcon();
		} else if (getStage() == 69) {
			player.getInterfaceManager().sendMagicBook();
		} else if (getStage() == 70) {
			player.getPackets().sendVar(1021, 0);
		} else if (getStage() == 72) {
			player.getHintIconsManager().removeUnsavedHintIcon();
		}
		refreshStage();
	}

	public void sendProgress() {
		int stage = getStage();
		//TODO: FIXED TABS
		player.getInterfaceManager().setWindowInterface(player.getInterfaceManager().hasRezizableScreen() ? 6 : 17, 371);
			player.getInterfaceManager().replaceRealChatBoxInterface(372);
		if (stage == 0) {
			player.getPackets().sendHideIComponent(371, 4, true);
			player.getPackets().sendIComponentText(372, 0, "Getting Started");
			player.getPackets().sendIComponentText(372, 1, "To start the tutorial use your left mouse button to click on the");
			player.getPackets().sendIComponentText(372, 2, "Runescape Guide in this room .He is indicated by a flashing");
			player.getPackets().sendIComponentText(372, 3, "yellow arrow above his head. If you can't see him use your");
			player.getPackets().sendIComponentText(372, 4, "keyboard arrow keys to rotate the view.");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 1) {
			player.getPackets().sendHideIComponent(371, 4, true);
			player.getPackets().sendIComponentText(372, 0, "");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "Player controls");
			player.getPackets().sendIComponentText(372, 3, "Please click on the flashing spanner icon found at the buttom");
			player.getPackets().sendIComponentText(372, 4, "right of your screen. This will display your player controls.");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
			player.getPackets().sendVar(1021, 13); // flashing setting tab
		} else if (stage == 2) {
			player.getPackets().sendHideIComponent(371, 4, true); 
			player.getPackets().sendIComponentText(372, 0, "Player controls");
			player.getPackets().sendIComponentText(372, 1, "On the side panel you can now see a variety of options from");
			player.getPackets().sendIComponentText(372, 2, "changing the brightness of the screen and of the volume of");
			player.getPackets().sendIComponentText(372, 3, "music, to selecting whether your player should help");
			player.getPackets().sendIComponentText(372, 4, "from other players. Don't worry about these too much for now.");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 3) {
			player.getPackets().sendHideIComponent(371, 4, true);
			player.getPackets().sendIComponentText(372, 0, "Interacting with the scenery");
			player.getPackets().sendIComponentText(372, 1, "You can interact with many items of the scenery by simply clicking");
			player.getPackets().sendIComponentText(372, 2, "on them. Right clicking will also give more options. Feel free to");
			player.getPackets().sendIComponentText(372, 3, "try it with the things in this room, then click on the door");
			player.getPackets().sendIComponentText(372, 4, "indicated with the yellow arrow to go througth to the next");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 4) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(2);
			player.getPackets().sendIComponentText(372, 0, "Moving around");
			player.getPackets().sendIComponentText(372, 1, "You can interact with many items of the scenery by simply clicking");
			player.getPackets().sendIComponentText(372, 2, "ground will walk you to that point. Talk to the Survival Expert by");
			player.getPackets().sendIComponentText(372, 3, "the pond to continue the tutorial. Remember you can rotate");
			player.getPackets().sendIComponentText(372, 4, "the view by pressing the arrow keys.");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 5) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(2);
			player.getPackets().sendIComponentText(372, 0, "Viewing the items that you were given.");
			player.getPackets().sendIComponentText(372, 1, "Click on the flashing backpack icon to the right hand size of");
			player.getPackets().sendIComponentText(372, 2, "the main window to view your inventory. Your inventory is a list");
			player.getPackets().sendIComponentText(372, 3, "of everything you have in your backpack.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
			player.getPackets().sendVar(1021, 5); // flashing inv tab
		} else if (stage == 6) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(2);
			player.getPackets().sendIComponentText(372, 0, "Cut down a tree");
			player.getPackets().sendIComponentText(372, 1, "You can click on the backpack icon at any time to view the");
			player.getPackets().sendIComponentText(372, 2, "items that you currently have in your inventory. You will see");
			player.getPackets().sendIComponentText(372, 3, "that you now have an axe in your inventory. Use this to get");
			player.getPackets().sendIComponentText(372, 4, "some logs by clicking on one of the trees in the area.");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 7) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(2);
			player.getPackets().sendIComponentText(372, 0, "Please wait");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "Your character is now attemping to cut down the tree. Sit back");
			player.getPackets().sendIComponentText(372, 3, "for a moment while he does all the hard work.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 8) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(2);
			player.getPackets().sendIComponentText(372, 0, "Making a fire");
			player.getPackets().sendIComponentText(372, 1, "Well done! You managed to cut some logs from the tree! Next,");
			player.getPackets().sendIComponentText(372, 2, "use the tinderbox in your inventory to light the logs.");
			player.getPackets().sendIComponentText(372, 3, "First click on the tinderbox to 'use' it.");
			player.getPackets().sendIComponentText(372, 4, "Then click on the logs in your inventory to light them.");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 9) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(2);
			player.getPackets().sendIComponentText(372, 0, "Please wait");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "Your character is now attemping to light the fire.");
			player.getPackets().sendIComponentText(372, 3, "This should only take a few seconds.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 10) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(2);
			player.getPackets().sendIComponentText(372, 0, "");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "You gained some experience.");
			player.getPackets().sendIComponentText(372, 3, "Click on the flashing bar graph icon near the inventory button");
			player.getPackets().sendIComponentText(372, 4, "to see your skill stats.");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
			player.getPackets().sendVar(1021, 3); // flashing skills tab
		} else if (stage == 11) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(3);
			player.getPackets().sendIComponentText(372, 0, "Your skill stats");
			player.getPackets().sendIComponentText(372, 1, "Here you will see how good your skills are. As you move your");
			player.getPackets().sendIComponentText(372, 2, "mouse over any of the icons in this panel, the small yellow");
			player.getPackets().sendIComponentText(372, 3, "popup box will show you the start amount of experience you");
			player.getPackets().sendIComponentText(372, 4, "have and how much is needed to get to the next level. Speak to");
			player.getPackets().sendIComponentText(372, 5, "the Survival Expert to continue.");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 12) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(3);
			player.getPackets().sendIComponentText(372, 0, "Catch some Shrimp.");
			player.getPackets().sendIComponentText(372, 1, "Click on the sparkling fishing spot indicated by the fishing");
			player.getPackets().sendIComponentText(372, 2, "arrow. Remember, you can check your inventory by clicking the");
			player.getPackets().sendIComponentText(372, 3, "backpack icon.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 13 || stage == 16) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(3);
			player.getPackets().sendIComponentText(372, 0, "");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "Please wait");
			player.getPackets().sendIComponentText(372, 3, "This should only take a few seconds.");
			player.getPackets().sendIComponentText(372, 4, "As you gain Fishing experience you'll find that there are many types");
			player.getPackets().sendIComponentText(372, 5, "of fish and many ways to catch them.");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 14) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(3);
			player.getPackets().sendIComponentText(372, 0, "Cooking your shrimp");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "Now you have caught some shrimp, let's cook it. First light a fire: chop");
			player.getPackets().sendIComponentText(372, 3, "down a tree and then use a tinderbox on the logs. If you've lost");
			player.getPackets().sendIComponentText(372, 4, "your hatchet or tinderbox! Brynna will give you another.");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 15 || stage == 17) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(3);
			player.getPackets().sendIComponentText(372, 0, "Burning your shrimp");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "You have just burnt your first shrimp. This is normal. As you get");
			player.getPackets().sendIComponentText(372, 3, "more experience in Cooking, you will burn stuff less often. Let's try");
			player.getPackets().sendIComponentText(372, 4, "cooking without burning it this time. First catch some more shrimp,");
			player.getPackets().sendIComponentText(372, 5, "then use it on a fire.");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 18) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(3);
			player.getPackets().sendIComponentText(372, 0, "");
			player.getPackets().sendIComponentText(372, 1, "Well done, you've just cooked your first RuneScape meal.");
			player.getPackets().sendIComponentText(372, 2, "If you'd like a recap on anything you've learnt so far speak to the");
			player.getPackets().sendIComponentText(372, 3, "Survival Expert. You can now move on the next instructor. Click");
			player.getPackets().sendIComponentText(372, 4, "on the gate shown and follow the path. Remember, you can move the");
			player.getPackets().sendIComponentText(372, 5, "camera with the arrow keys.");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 19) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(4);
			player.getPackets().sendIComponentText(372, 0, "Find your next instructor");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "Follow the path until you get to the door with the yellow arrow above");
			player.getPackets().sendIComponentText(372, 3, "it. Click on the door to open it. Notice the mini map in the top right,");
			player.getPackets().sendIComponentText(372, 4, "this shows a top down view of the area arround you. This can also");
			player.getPackets().sendIComponentText(372, 5, "be used tor navigation.");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 20) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(4);
			player.getPackets().sendIComponentText(372, 0, "Find your next instructor");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "Talk to the chef indicated. He will teach you the more advanced");
			player.getPackets().sendIComponentText(372, 3, "aspects of Cocking such as combiding ingredients. He will also teach");
			player.getPackets().sendIComponentText(372, 4, "you about your Music Player.");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 21) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(4);
			player.getPackets().sendIComponentText(372, 0, "Making dough");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "This is the base of the meals. To make dough we must mix");
			player.getPackets().sendIComponentText(372, 3, "flour and water. First right click the bucket of water and select use,");
			player.getPackets().sendIComponentText(372, 4, "then left click on the pot of flour.");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 22) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(4);
			player.getPackets().sendIComponentText(372, 0, "Cooking dough.");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "Now you have made dough, you can cook it. To cook the dough,");
			player.getPackets().sendIComponentText(372, 3, "use it with the range shown by the arrow. If you lose your,");
			player.getPackets().sendIComponentText(372, 4, "dough, talk to Lev - he will give you more ingredients.");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 23) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(5);
			player.getPackets().sendIComponentText(372, 0, "Cooking dough.");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "Well done! Your first loaf of bread. As you gain experience in,");
			player.getPackets().sendIComponentText(372, 3, "Cooking, you will be able to make other things like pies, cakes,");
			player.getPackets().sendIComponentText(372, 4, "and even kebabs. Now you've got the hang of cooking, let's");
			player.getPackets().sendIComponentText(372, 5, "move on. Click on the flashing icon in the bottom right to see");
			player.getPackets().sendIComponentText(372, 6, "the jukebox.");
			player.getPackets().sendVar(1021, 15); // flashing Music
		} else if (stage == 24) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(5);
			player.getPackets().sendIComponentText(372, 0, "The music player.");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "From this interface you can control the music that is played.");
			player.getPackets().sendIComponentText(372, 3, "As you explore the world, more of the tunes will become");
			player.getPackets().sendIComponentText(372, 4, "unlocked. Once you've examined this menu use the next door");
			player.getPackets().sendIComponentText(372, 5, "to continue. If you need a recap on anything covered here,");
			player.getPackets().sendIComponentText(372, 6, "talk to Lev.");
		} else if (stage == 25) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(6);
			player.getPackets().sendIComponentText(372, 0, "Emotes.");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "Now, how about showing some feelings? You will see a flashing");
			player.getPackets().sendIComponentText(372, 3, "icon in the shape of a person. Click on that to access your");
			player.getPackets().sendIComponentText(372, 4, "emotes.");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
			player.getPackets().sendVar(1021, 14); // flashing Emote
		} else if (stage == 26) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(6);
			player.getPackets().sendIComponentText(372, 0, "Emotes.");
			player.getPackets().sendIComponentText(372, 1, "For those situations where words don't quest describe how you");
			player.getPackets().sendIComponentText(372, 2, "feel, try an emote. Go ahead, try one out! You might notice");
			player.getPackets().sendIComponentText(372, 3, "that some of the emotes are grey and cannot be used now.");
			player.getPackets().sendIComponentText(372, 4, "Don't worry! As you progress further into the game you'll gain");
			player.getPackets().sendIComponentText(372, 5, "access to all sorts of things, including more fun emotes like");
			player.getPackets().sendIComponentText(372, 6, "these");
		} else if (stage == 27) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(6);
			player.getPackets().sendIComponentText(372, 0, "Running.");
			player.getPackets().sendIComponentText(372, 1, "It's only a short distance to the next guide.");
			player.getPackets().sendIComponentText(372, 2, "Why not try running there? You can run by clicking");
			player.getPackets().sendIComponentText(372, 3, "on the boot icon next to your minimap or by holding");
			player.getPackets().sendIComponentText(372, 4, "down your control key while clicking your destination.");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
			player.getPackets().sendVar(1021, 17); // flashing run orb
		} else if (stage == 28) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(6);
			player.getPackets().sendIComponentText(372, 0, "Run to the next guide.");
			player.getPackets().sendIComponentText(372, 1, "Now that you have the run button turned on, follow the path");
			player.getPackets().sendIComponentText(372, 2, "until you come to the end. You may notice that the number on");
			player.getPackets().sendIComponentText(372, 3, "the button goes down. This is your run energy. If your run");
			player.getPackets().sendIComponentText(372, 4, "energy reaches zero, you'll stop running. Click on the door");
			player.getPackets().sendIComponentText(372, 5, "to pass through it.");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 29) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(7);
			player.getPackets().sendIComponentText(372, 0, "");
			player.getPackets().sendIComponentText(372, 1, "Talk with the Quest Guide.");
			player.getPackets().sendIComponentText(372, 2, "");
			player.getPackets().sendIComponentText(372, 3, "He will tell you all about quests.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 30) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(7);
			player.getPackets().sendIComponentText(372, 0, "");
			player.getPackets().sendIComponentText(372, 1, "Open the Quest Journal.");
			player.getPackets().sendIComponentText(372, 2, "");
			player.getPackets().sendIComponentText(372, 3, "Click on the flashing icon next to your inventory.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
			player.getPackets().sendVar(1021, 4); // flashing Quest Tab
		} else if (stage == 31) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(7);
			player.getPackets().sendIComponentText(372, 0, "Your Quest Journal");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "This is your Quest Journal, a list of all the quests in the game.");
			player.getPackets().sendIComponentText(372, 3, "Talk to the Quest Guide again for an explanation.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 32) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(7);
			player.getPackets().sendIComponentText(372, 0, "");
			player.getPackets().sendIComponentText(372, 1, "Moving on.");
			player.getPackets().sendIComponentText(372, 2, "It's time to enter some caves. Click on the ladder to go down to");
			player.getPackets().sendIComponentText(372, 3, "the next area");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 33) { //Start of Dungeon
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(8);
			player.getPackets().sendIComponentText(372, 0, "Mining and Smithing.");
			player.getPackets().sendIComponentText(372, 1, "Next let's get you a weapon, or more to the point, you can");
			player.getPackets().sendIComponentText(372, 2, "make your first weapon yourself. Don't panic, the Mining");
			player.getPackets().sendIComponentText(372, 3, "Instructor will help you. Talk to him and he'll tell you all about it.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 34) { //Prospecting Rocks
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(8);
			player.getPackets().sendIComponentText(372, 0, "Prospecting.");
			player.getPackets().sendIComponentText(372, 1, "To prospect a mineable rock, just right click it and select the");
			player.getPackets().sendIComponentText(372, 2, "'prospect rock' option. This will tell you the type of ore you can");
			player.getPackets().sendIComponentText(372, 3, "mine from it. Try it now on one of the rocks indicated.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 35) { //Prospecting Rocks
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(8);
			player.getPackets().sendIComponentText(372, 0, "It's tin.");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "So now you know there's tin in the grey rocks, try prospecting");
			player.getPackets().sendIComponentText(372, 3, "the brown ones next.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 36) { //Prospecting Rocks
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(8);
			player.getPackets().sendIComponentText(372, 0, "It's copper.");
			player.getPackets().sendIComponentText(372, 1, "Talk to the Mining Instructor to find out about these types of");
			player.getPackets().sendIComponentText(372, 2, "ore and how you can mind them. He'll even give you the");
			player.getPackets().sendIComponentText(372, 3, "required tools.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 37) { //Stage 37 mining rocks
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(8);
			player.getPackets().sendIComponentText(372, 0, "Mining.");
			player.getPackets().sendIComponentText(372, 1, "It's quite simple really. All you need to do is right click on the");
			player.getPackets().sendIComponentText(372, 2, "rock and select 'mine'. You can only mine when you have a");
			player.getPackets().sendIComponentText(372, 3, "pickaxe. So give it a try: first mine one tin ore.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 38) { //Stage 38 //Mining copper
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(8);
			player.getPackets().sendIComponentText(372, 0, "Mining.");
			player.getPackets().sendIComponentText(372, 1, "Now you have some tin ore you just need some copper ore,");
			player.getPackets().sendIComponentText(372, 2, "then you'll have all you need to create a bronze bar. As you");
			player.getPackets().sendIComponentText(372, 3, "did before right click on the copper rock and select 'mine");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 39) { //Stage 39 smelting
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(9);
			player.getPackets().sendIComponentText(372, 0, "Smelting.");
			player.getPackets().sendIComponentText(372, 1, "You should now have both some copper and tin ore. So let's");
			player.getPackets().sendIComponentText(372, 2, "smelt them to make a bronze bar. To do this, right click on");
			player.getPackets().sendIComponentText(372, 3, "either tin or copper ore and select use then left click on the");
			player.getPackets().sendIComponentText(372, 4, "furnace. Try it now.");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 40) { //Stage 40 SMELT A BAR
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(9);
			player.getPackets().sendIComponentText(372, 0, "You've made a bronze bar!");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "Speak to the Mining Instructor and he'll show you how to make");
			player.getPackets().sendIComponentText(372, 3, "it into a weapon.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 41) { //Stage 41 making dagger
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(9);
			player.getPackets().sendIComponentText(372, 0, "Smithing a dagger.");
			player.getPackets().sendIComponentText(372, 1, "To smith you'll need a hammer and enough metal bars to make");
			player.getPackets().sendIComponentText(372, 2, "the desired item, as well as a handy anvil. To start the");
			player.getPackets().sendIComponentText(372, 3, "process, click on the anvil, or alternatively use the bar on it.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 42) { //Smithing a dagger
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(9);
			player.getPackets().sendIComponentText(372, 0, "Smithing a dagger.");
			player.getPackets().sendIComponentText(372, 1, "Now you have the Smithing menu open, you will see a list of all");
			player.getPackets().sendIComponentText(372, 2, "the things you can make. Only the dagger can be made at your");
			player.getPackets().sendIComponentText(372, 3, "skill level; this is shown by the white text under it. You'll need");
			player.getPackets().sendIComponentText(372, 4, "to select the dagger to continue.");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 43) { //FINISH MINING AREA
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(9);
			player.getPackets().sendIComponentText(372, 0, "You've finished in this area.");
			player.getPackets().sendIComponentText(372, 1, "So let's move on. Go through the gates shown by the arrow.");
			player.getPackets().sendIComponentText(372, 2, "Remember, you may need to move the camera to see your");
			player.getPackets().sendIComponentText(372, 3, "surroundings. Speak to the guide for a recap at any time.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 44) { //FINISH MINING AREA
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(10);
			player.getPackets().sendIComponentText(372, 0, "Combat.");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "In this area you will find out about combat with swords and");
			player.getPackets().sendIComponentText(372, 3, "bows. Speak to the guide and he will tell you all about it.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 45) { //FINISH MINING AREA
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(10);
			player.getPackets().sendIComponentText(372, 0, "Wielding weapons.");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "You now have access to a new interface. Click on the flashing");
			player.getPackets().sendIComponentText(372, 3, "icon of a man, the one to the right of your backpack icon.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
			player.getPackets().sendVar(1021, 6); // flashing Quest Tab
		} else if (stage == 46) { //Open equipment
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(10);
			player.getPackets().sendIComponentText(372, 0, "This is your worn inventory.");
			player.getPackets().sendIComponentText(372, 1, "From here you can see what items you have equipped. You will");
			player.getPackets().sendIComponentText(372, 2, "notice the button 'View equipment stats'. Click on this now to");
			player.getPackets().sendIComponentText(372, 3, "display the details of what you have equipped.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 47) { //Open equipment
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(10);
			player.getPackets().sendIComponentText(372, 0, "Worn interface");
			player.getPackets().sendIComponentText(372, 1, "You can see what items you are wearing in the worn inventory");
			player.getPackets().sendIComponentText(372, 2, "to the left of the screen, with their combined statistics on the");
			player.getPackets().sendIComponentText(372, 3, "right. Let's add something. Left click your dagger to 'wield' it.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 48) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(10);
			player.getPackets().sendIComponentText(372, 0, "You're now holding your dagger.");
			player.getPackets().sendIComponentText(372, 1, "Clothes, armour, weapons and many other items are equipped");
			player.getPackets().sendIComponentText(372, 2, "like this. You can unequip items by clicking on the item in the");
			player.getPackets().sendIComponentText(372, 3, "worn inventory. You can close this window by clicking on the");
			player.getPackets().sendIComponentText(372, 4, "small 'x' in the top right hand corner. Speak to the Combat");
			player.getPackets().sendIComponentText(372, 5, "Instructor to continue.");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 49) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(10);
			player.getPackets().sendIComponentText(372, 0, "Unequipping items.");
			player.getPackets().sendIComponentText(372, 1, "In your worn inventory panel, right click on the dagger and");
			player.getPackets().sendIComponentText(372, 2, "select the remove option from the drop down list. After you've");
			player.getPackets().sendIComponentText(372, 3, "unequipped the dagger, wield the sword and shield. As you");
			player.getPackets().sendIComponentText(372, 4, "pass the mouse over an item, you will see its name appear at");
			player.getPackets().sendIComponentText(372, 5, "the top left of the screen.");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 50) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(10);
			player.getPackets().sendIComponentText(372, 0, "Combat interface.");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "Click on the flashing crossed swords icon to see the combat");
			player.getPackets().sendIComponentText(372, 3, "interface.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
			player.getPackets().sendVar(1021, 1); // flashing combat tab
		} else if (stage == 51) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(11);
			player.getPackets().sendIComponentText(372, 0, "This is your combat interface.");
			player.getPackets().sendIComponentText(372, 1, "From this interface you can select the type of attack your");
			player.getPackets().sendIComponentText(372, 2, "character will use. Different monsters have different");
			player.getPackets().sendIComponentText(372, 3, "weaknesses. If you hover your mouse over the buttons, you");
			player.getPackets().sendIComponentText(372, 4, "will see the type of XP you will receive when using each type of");
			player.getPackets().sendIComponentText(372, 5, "attack. Now you have the tools needed for battle why not slay");
			player.getPackets().sendIComponentText(372, 6, "some rats. Click on the gates indicated to continue.");
			player.getPackets().sendVar(1021, 1); // flashing combat tab
		} else if (stage == 52) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(11);
			player.getPackets().sendIComponentText(372, 0, "Attacking.");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "To attack the rat, right click it and select the attack option. You");
			player.getPackets().sendIComponentText(372, 3, "will then walk over to it and start hitting it.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 53) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(11);
			player.getPackets().sendIComponentText(372, 0, "Sit back and watch.");
			player.getPackets().sendIComponentText(372, 1, "While you are fighting you will see a bar over your head. The");
			player.getPackets().sendIComponentText(372, 2, "bar shows how much health you have left. Your opponent will");
			player.getPackets().sendIComponentText(372, 3, "have one too. You will continue to attack the rat until it's dead");
			player.getPackets().sendIComponentText(372, 4, "or you do something else.");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 54) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(11);
			player.getPackets().sendIComponentText(372, 0, "Well done, you've made your first kill!");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "Pass through the gate and talk to the Combat Instructor; he");
			player.getPackets().sendIComponentText(372, 3, "will give you your next task.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 55) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(12);
			player.getPackets().sendIComponentText(372, 0, "Rat ranging.");
			player.getPackets().sendIComponentText(372, 1, "Now you have a bow and some arrows. Before you can use");
			player.getPackets().sendIComponentText(372, 2, "them you'll need to equip them. Once equipped with the");
			player.getPackets().sendIComponentText(372, 3, "ranging gear, try killing another rat. Remember: to attack, right");
			player.getPackets().sendIComponentText(372, 4, "click on the monster and select attack.");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 56) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(12);
			player.getPackets().sendIComponentText(372, 0, "Moving on.");
			player.getPackets().sendIComponentText(372, 1, "You have completed the tasks here. To move on, click on the");
			player.getPackets().sendIComponentText(372, 2, "ladder shown. If you need to go over any of what you learnt");
			player.getPackets().sendIComponentText(372, 3, "here, just talk to the Combat Instructor and he'll tell you what");
			player.getPackets().sendIComponentText(372, 4, "he can.");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 57) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(12);
			player.getPackets().sendIComponentText(372, 0, "Banking.");
			player.getPackets().sendIComponentText(372, 1, "Follow the path and you will come to the front of the building.");
			player.getPackets().sendIComponentText(372, 2, "This is the Bank of Runescape, where you can store all you");
			player.getPackets().sendIComponentText(372, 3, "most valued items. To open your bank box just right click on an");
			player.getPackets().sendIComponentText(372, 4, "open booth indicated and select 'use'.");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 58) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(12);
			player.getPackets().sendIComponentText(372, 0, "This is your bank box.");
			player.getPackets().sendIComponentText(372, 1, "You can store stuff here for safekeeping. If you die, anything");
			player.getPackets().sendIComponentText(372, 2, "in your bank will be saved. To deposit something, right click it");
			player.getPackets().sendIComponentText(372, 3, "and select 'store'. Once you've had a good look, close the");
			player.getPackets().sendIComponentText(372, 4, "window and move through the door indicated.");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 59) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(15);
			player.getPackets().sendIComponentText(372, 0, "Financial advice.");
			player.getPackets().sendIComponentText(372, 1, "The guide here will tell you all about making cash. Just click on");
			player.getPackets().sendIComponentText(372, 2, "him to hear what he's got to say.");
			player.getPackets().sendIComponentText(372, 3, "");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 60) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(15);
			player.getPackets().sendIComponentText(372, 0, "");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "Continue through the next door.");
			player.getPackets().sendIComponentText(372, 3, "");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 61) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(16);
			player.getPackets().sendIComponentText(372, 0, "Prayer.");
			player.getPackets().sendIComponentText(372, 1, "Follow the path to the chapel and enter it.");
			player.getPackets().sendIComponentText(372, 2, "Once inside talk to the monk. He'll tell you all about the Prayer");
			player.getPackets().sendIComponentText(372, 3, "skill.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 62) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(16);
			player.getPackets().sendIComponentText(372, 0, "Your Prayer menu.");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "Click on the flashing icon to open the Prayer menu.");
			player.getPackets().sendIComponentText(372, 3, "");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
			player.getPackets().sendVar(1021, 7); // flashing prayer tab
		} else if (stage == 63) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(16);
			player.getPackets().sendIComponentText(372, 0, "Your Prayer Menu.");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "Talk with Brother Brace and he'll tell you about prayers.");
			player.getPackets().sendIComponentText(372, 3, "");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 64) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(16);
			player.getPackets().sendIComponentText(372, 0, "");
			player.getPackets().sendIComponentText(372, 1, "Friends list.");
			player.getPackets().sendIComponentText(372, 2, "You should now see another new icon. Click on the flashing");
			player.getPackets().sendIComponentText(372, 3, "smiling face to open your friends list.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
			player.getPackets().sendVar(1021, 10); // flashing friends tab
		} else if (stage == 65) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(17);
			player.getPackets().sendIComponentText(372, 0, "This is your ignore list.");
			player.getPackets().sendIComponentText(372, 1, "The two lists - friends and ignore - can be very helpful for");
			player.getPackets().sendIComponentText(372, 2, "keeping track of when your friends are online or for blocking");
			player.getPackets().sendIComponentText(372, 3, "messages from people you simply don't like. Speak with");
			player.getPackets().sendIComponentText(372, 4, "Brother Brace and he will tell you more.");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
			player.getPackets().sendVar(1021, 11); // flashing ignore tab
		} else if (stage == 67) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(18);
			player.getPackets().sendIComponentText(372, 0, "");
			player.getPackets().sendIComponentText(372, 1, "Your final instructor!");
			player.getPackets().sendIComponentText(372, 2, "You're almost finished on tutorial island. Pass through the");
			player.getPackets().sendIComponentText(372, 3, "door to find the path leading to your final instructor.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 68) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(18);
			player.getPackets().sendIComponentText(372, 0, "Your final instructor!");
			player.getPackets().sendIComponentText(372, 1, "Just follow the path to the Wizard's house, where you will be");
			player.getPackets().sendIComponentText(372, 2, "shown how to cast spells. Just talk with the mage indicated to");
			player.getPackets().sendIComponentText(372, 3, "find out more.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 69) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(18);
			player.getPackets().sendIComponentText(372, 0, "Open up your final menu.");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "Open up the Magic menu by clicking on the flashing icon next");
			player.getPackets().sendIComponentText(372, 3, "to the Prayer button you just learned about.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
			player.getPackets().sendVar(1021, 8); // flashing magic tab
		} else if (stage == 70) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(19);
			player.getPackets().sendIComponentText(372, 0, "");
			player.getPackets().sendIComponentText(372, 1, "This is your spells list.");
			player.getPackets().sendIComponentText(372, 2, "");
			player.getPackets().sendIComponentText(372, 3, "Ask the mage about it.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 71) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(19);
			player.getPackets().sendIComponentText(372, 0, "Cast Wind Strike at a chicken.");
			player.getPackets().sendIComponentText(372, 1, "Now you have runes you should see the Wind Strike icon at the");
			player.getPackets().sendIComponentText(372, 2, "top left corner of the Magic interface - third in from the");
			player.getPackets().sendIComponentText(372, 3, "left. Walk over to the caged chickens, click the Wind Strike icon");
			player.getPackets().sendIComponentText(372, 4, "and then select one of the chickens to cast it on. It may take");
			player.getPackets().sendIComponentText(372, 5, "several tries. If you need more runes ask Terrova.");
			player.getPackets().sendIComponentText(372, 6, "");
		} else if (stage == 72) {
			player.getPackets().sendHideIComponent(371, 4, true);
			sendProgress(19);
			player.getPackets().sendIComponentText(372, 0, "You have almost completed the tutorial!");
			player.getPackets().sendIComponentText(372, 1, "");
			player.getPackets().sendIComponentText(372, 2, "All you need to do now is move on to the mainland. Just speak");
			player.getPackets().sendIComponentText(372, 3, "with Terrova and he'll teleport you to Lumbridge Castle.");
			player.getPackets().sendIComponentText(372, 4, "");
			player.getPackets().sendIComponentText(372, 5, "");
			player.getPackets().sendIComponentText(372, 6, "");
		}
	}

	public void sendProgress(int progress) {
		player.getPackets().sendVar(406, progress);
	}

	/*
	 * return can use
	 */
	@Override
	public boolean canUseItemOnItem(Item itemUsed, Item usedWith) {
		if (getStage() == 8) {
			if ((itemUsed.getId() == 590 || usedWith.getId() == 590) && (itemUsed.getId() == 1511 || usedWith.getId() == 1511)) {
				updateProgress();
			}
		}
		if (getStage() == 21) {
			if ((itemUsed.getId() == 1929 || usedWith.getId() == 1929) && (itemUsed.getId() == 1933 || usedWith.getId() == 1933)) {
				updateProgress();
			}
		}
		return true;
	}
	@Override
	public boolean handleItemOnObject(WorldObject object, Item item) {
		if ((item.getId() == 438 || item.getId() == 436) && object.getId() == 3044 && getStage() > 38) {
			player.getActionManager().setAction(new Smelting(Smelting.SmeltingBar.BRONZE, object, 1));
			if (getStage() == 39)
			updateProgress(); //40
			return true;
		} else if ((item.getId() == 2349 || item.getId() == 2349) && object.getId() == 2783 && getStage() == 41) {
			updateProgress(); //42
			return true;
		}
			return true;
	}
	
	@Override
	public void trackXP(int skillId, int addedXp) {
		if (getStage() == 9 && skillId == Skills.FIREMAKING)
			updateProgress();
		else if ((getStage() == 13 || getStage() == 16) && skillId == Skills.FISHING) {
			updateProgress();
			player.stopAll();
		} else if (getStage() == 22 && skillId == Skills.COOKING) {
			updateProgress();
		} else if (getStage() == 52 && (skillId == Skills.ATTACK || skillId == Skills.STRENGTH || skillId == Skills.DEFENCE)) {
			updateProgress(); //53
		} else if (getStage() == 71 && (skillId == Skills.MAGIC)) {
			updateProgress(); //72
		}
	}
	
	public void processNPCDeath(int npcId) {
		if (npcId == GIANT_RAT && getStage() == 53) {
			updateProgress(); //54
		} else if (npcId == GIANT_RAT && getStage() == 55) { 
			updateProgress();
		}
	}

	/*
	 * return can add
	 */
	@Override
	public boolean canAddInventoryItem(int itemId, int amount) {
		if (getStage() == 7) {
			for (int logId : Woodcutting.TreeDefinitions.NORMAL.getLogsId()) {
				if (itemId == logId) {
					updateProgress();
					player.getDialogueManager().startDialogue("ItemMessage", "You get some logs.", itemId);
					return true;
			}
		}
		} if (getStage() == 14 && (itemId == 7954 || itemId == 315)) {
			updateProgress();
			player.getInventory().addItem(7954, 1);
			return false;
		} else if (getStage() == 17 && (itemId == 7954 || itemId == 315)) {
			updateProgress();
			player.getInventory().addItem(315, 1);
			return false;
		} else if (getStage() == 37 && (itemId == 438)) {
			updateProgress();
			player.getInventory().addItem(438, 1);
			return false;
		} else if (getStage() == 38 && (itemId == 436)) {
			updateProgress(); //updates to stage 39
			player.getInventory().addItem(436, 1);
			return false;
		} else if (getStage() == 42 && (itemId == 1205)) {
			updateProgress(); //updates to stage 43
			player.getInventory().addItem(1205, 1);
			return false;
		}
		return true;
	}

	/*
	 * return process normaly
	 */
	@Override
	public boolean processButtonClick(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		System.out.println("Update Stage for settings tab");
		if (((interfaceId == 548 && componentId == 102) || (interfaceId == 746 && componentId == 47)) && getStage() == 1) { //Option Tab
			updateProgress();
			
		} else if (((interfaceId == 548 && componentId == 132) || (interfaceId == 746 && componentId == 39)) && getStage() == 5) { //inventory Tab
			updateProgress();
		} else if (getStage() == 8 && interfaceId == Inventory.INVENTORY_INTERFACE && packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
			Item item = player.getInventory().getItem(slotId);
			if (item != null && item.getId() == 1511)
				updateProgress();
		} else if (((interfaceId == 548 && componentId == 130) || (interfaceId == 746 && componentId == 37)) && getStage() == 10) { //Skill Tab
			updateProgress();
		} else if (((interfaceId == 548 && componentId == 104) || (interfaceId == 746 && componentId == 49)) && getStage() == 23) { //Music Tab
			updateProgress();
		} else if (((interfaceId == 548 && componentId == 103) || (interfaceId == 746 && componentId == 48)) && getStage() == 25) { //Emote Tab
			updateProgress();
		} else if (((interfaceId == 548 && componentId == 131) || (interfaceId == 746 && componentId == 38)) && getStage() == 30) { //Quest Tab
			updateProgress();
		} else if (((interfaceId == 464 && componentId == 2) && getStage() == 26)) {
			updateProgress();
		} else if (((interfaceId == 750 && componentId == 1) && getStage() == 27)) {
			updateProgress();
		} else if (((interfaceId == 548 && componentId == 133) || (interfaceId == 746 && componentId == 40)) && getStage() == 45) { //equipment tab
			updateProgress(); //updates to 46 progress
		} else if (((interfaceId == 387 && componentId == 39) && getStage() == 46)) {
			updateProgress(); //47?
		} else if (((interfaceId == 548 && componentId == 128) || (interfaceId == 746 && componentId == 35)) && getStage() == 50) { //Attack Tab
			updateProgress(); //updates to 51 progress
		} else if (((interfaceId == 548 && componentId == 134) || (interfaceId == 746 && componentId == 41)) && getStage() == 62) { //prayer tab
			updateProgress(); //updates to 63 progress
		} else if (((interfaceId == 548 && componentId == 99) || (interfaceId == 746 && componentId == 44)) && getStage() == 64) { //friends tab
			updateProgress(); //updates to 65 progress
		} else if (((interfaceId == 548 && componentId == 100) || (interfaceId == 746 && componentId == 45)) && getStage() == 65) { //ignore tab
			updateProgress(); //updates to 66 progress
		} else if (((interfaceId == 548 && componentId == 135) || (interfaceId == 746 && componentId == 42)) && getStage() == 69) { //<agic
			updateProgress(); //updates to 70 progress
		}
		return true;
	}
	@Override
	public boolean processObjectClick2(WorldObject object) {
		int stage = getStage();
		if (object.getId() == 3043 && stage == 34) {
			updateProgress(); //updates to stage 35!
		} else if (object.getId() == 3042) {
			updateProgress(); //updates to stage 36!
		}
		
		return true;
	}
	@Override
	public boolean processObjectClick1(WorldObject object) {
		if (object.getId() == 3014 && object.getX() == 3098 && object.getY() == 3107) {
			int stage = getStage();
			if (stage < 3 || player.getY() != object.getY())
				return false;
			if (stage == 3) {
				updateProgress();
			}
			WorldObject openedDoor = new WorldObject(object.getId(), object.getType(), object.getRotation() + 1,
					object.getX() - 1, object.getY(), object.getPlane());
			if (World.removeObjectTemporary(object, 1200)) {
				World.spawnObjectTemporary(openedDoor, 1200);
				player.lock(2);
				player.stopAll();
				player.addWalkSteps(player.getX() >= object.getX() ? object.getX() - 1 : object.getX(), player.getY(),
						-1, false);
			}
			return false;
		} else if (object.getId() == 3018 && object.getX() == 3072 && object.getY() == 3090) {
				int stage = getStage();
				if (stage < 23 || player.getY() != object.getY())
					return false;
				if (stage == 24) {
					updateProgress();
				} //Cooking Guide Leave door to fix to right direction
				WorldObject openedDoor = new WorldObject(object.getId(), object.getType(), object.getRotation() + 1, object.getX(), object.getY() + 1, object.getPlane());
				if (World.removeObjectTemporary(object, 1200)) {
					World.spawnObjectTemporary(openedDoor, 1200);
					player.lock(2);
					player.stopAll();
					player.addWalkSteps(player.getX() >= object.getX() ? object.getX() - 1 : object.getX(), player.getY(), -1, false);
				}
				return false;
			
		} else if (object.getId() == 3029 && object.getX() == 3088 && object.getY() == 3119) { //Quest Guide Ladder
			player.getHintIconsManager().removeUnsavedHintIcon();
			int stage = getStage();
			if (stage == 32) {
				updateProgress();
				player.useStairs(828, new WorldTile(3088, 9520, 0), 1, 2);
			}
			return false;
			
		} else if (object.getId() == 3019 && object.getX() == 3086 && object.getY() == 3126) { //Quet Guide Door
			int stage = getStage();
			if (stage < 27 || player.getY() != object.getY())
				return false;
			if (stage == 28) {
				updateProgress();
			}
			WorldObject openedDoor = new WorldObject(object.getId(), object.getType(), object.getRotation() + 5, object.getX(), object.getY() - 1, object.getPlane());
			if (World.removeObjectTemporary(object, 1200)) {
				World.spawnObjectTemporary(openedDoor, 1200);
				player.lock(2);
				player.stopAll();
				player.addWalkSteps(player.getX() >= object.getX() ? object.getX() : object.getX(), player.getY() - 1, - 1, false);
			}
			return false;
		} else if (object.getId() == 3033) {
			int stage = getStage();
			if (stage == 6)
				updateProgress();
		} else if (object.getId() == 3015 || object.getId() == 3016) {
			int stage = getStage();
			if (stage < 18 || player.getY() != object.getY())
				return false;
			if (stage == 18)
				updateProgress();
			player.lock(2);
			player.stopAll();
			player.addWalkSteps(player.getX() > object.getX() ? object.getX() : object.getX() + 1, player.getY(), -1,
					false);
			return false;

		} else if (object.getId() == 3017 && object.getX() == 3079 && object.getY() == 3084) {
			int stage = getStage();
			if (stage < 19 || player.getY() != object.getY())
				return false;
			if (stage == 19)
				updateProgress();
			WorldObject openedDoor = new WorldObject(object.getId(), object.getType(), object.getRotation() + 1, object.getX() - 1, object.getY(), object.getPlane());
			if (World.removeObjectTemporary(object, 1200)) {
				World.spawnObjectTemporary(openedDoor, 1200);
				player.lock(2);
				player.stopAll();
				player.addWalkSteps(player.getX() >= object.getX() ? object.getX() - 1 : object.getX(), player.getY(), -1, false);
			}
			return false;
			
		} else if (object.getId() == 3043) {
				int stage = getStage();
				if (stage < 37) 
					return false;
				player.getActionManager().setAction(new Mining(object, Mining.RockDefinitions.Tin_Ore));//dont update till u get ore
		} else if (object.getId() == 3042) {
			int stage = getStage();
			if (stage < 38) 
				return false;
			player.getActionManager().setAction(new Mining(object, Mining.RockDefinitions.Copper_Ore));
		} else if (object.getId() == 3020 && object.getX() == 3094 && object.getY() == 9503 || object.getId() == 3021 && object.getX() == 3094 && object.getY() == 9502) { //Leaving mining Gate To fix gate #2
			int stage = getStage();
			if (stage < 41 || player.getY() != object.getY()) //maybe have to fix
			return false;
			if (stage == 43)
				updateProgress();
			ObjectHandler.handleGate(player, object);
			player.lock(2);
			player.stopAll();
			player.addWalkSteps(player.getX() >= object.getX() ? object.getX()  + 2 : object.getX(), player.getY(), - 1, false);
			return false;
		} else if (object.getId() == 3022 && object.getX() == 3110 && object.getY() == 9519 || object.getId() == 3023 && object.getX() == 3110 && object.getY() == 9518) { //Leaving mining Gate To fix gate #2
			int stage = getStage();
			if (stage < 50)
				return false;
			if (stage == 51)
				updateProgress();
			ObjectHandler.handleGate(player, object);
			return false;
		} else if (object.getId() == 3030 && getStage() > 55) {
			updateProgress(); //57
			player.useStairs(828, new WorldTile(3111, 3125, 0), 1, 2);
		} else if (object.getId() == 3045 && getStage() > 56) {
			player.getDialogueManager().startDialogue("TutBanker", BANKER, this);
		} else if (object.getId() == 3024 && object.getX() == 3125 && object.getY() == 3124) { //Bank Door
				int stage = getStage();
				if (stage < 57 || player.getY() != object.getY())
					return false;
				if (stage == 58) {
					updateProgress();
				}
				WorldObject openedDoor = new WorldObject(object.getId(), object.getType(), object.getRotation() + 1,
						object.getX() - 1, object.getY(), object.getPlane());
				if (World.removeObjectTemporary(object, 1200)) {
					World.spawnObjectTemporary(openedDoor, 1200);
					player.lock(2);
					player.stopAll();
					player.addWalkSteps(player.getX() >= object.getX() ? object.getX() - 1 : object.getX(), player.getY(), -1, false);
				}
				return false;
		} else if (object.getId() == 3025 && object.getX() == 3130 && object.getY() == 3124) { //Bank Door
			int stage = getStage();
			if (stage < 59 || player.getY() != object.getY())
				return false;
			if (stage == 60) {
				updateProgress();
			}
			WorldObject openedDoor = new WorldObject(object.getId(), object.getType(), object.getRotation() + 1,
					object.getX() - 1, object.getY(), object.getPlane());
			if (World.removeObjectTemporary(object, 1200)) {
				World.spawnObjectTemporary(openedDoor, 1200);
				player.lock(2);
				player.stopAll();
				player.addWalkSteps(player.getX() >= object.getX() ? object.getX() - 1 : object.getX(), player.getY(), -1, false);
			}
			return false;
		} else if (object.getId() == 37002 && object.getX() == 3129 && object.getY() == 3107 || object.getId() == 36999 && object.getX() == 3129 && object.getY() == 3106) { //Leaving mining Gate To fix gate #2
			int stage = getStage();
			ObjectHandler.handleGate(player, object);
			player.lock(2);
			player.stopAll();
			player.addWalkSteps(player.getX() >= object.getX() ? object.getX() - 1 : object.getX(), player.getY(), -1, false);
			return false;
		
		} else if (object.getId() == 3026 && object.getX() == 3122 && object.getY() == 3102) { //leave church
			int stage = getStage();
			if (stage < 66)
				return false;
			if (stage == 67) {
				updateProgress(); //68
			}
			WorldObject openedDoor = new WorldObject(object.getId(), object.getType(), object.getRotation() + 1, object.getX(), object.getY() + 1 , object.getPlane());
			if (World.removeObjectTemporary(object, 1200)) {
				World.spawnObjectTemporary(openedDoor, 1200);
				player.lock(2);
				player.stopAll();
				player.addWalkSteps(player.getX() >= object.getX() ? object.getX(): object.getX(), player.getY() - 1,
						-1, false);
			}
			return false;
			
		}
		
		return true;
	}

	@Override
	public boolean processNPCClick1(NPC npc) {
		if (npc.getId() == RUNESCAPE_GUIDE) {
			player.getDialogueManager().startDialogue("RuneScapeGuide", RUNESCAPE_GUIDE, this);
			npc.faceEntity(player);
			npc.resetWalkSteps();
			return false;
		} else if (npc.getId() == SURVIVAL_EXPERT) {
			player.getDialogueManager().startDialogue("SurvivalExpert", SURVIVAL_EXPERT, this);
			npc.faceEntity(player);
			npc.resetWalkSteps();
			return false;
		} else if (npc.getId() == SURVIVAL_EXPERT) {
			
		} else if (npc.getId() == 952 && (getStage() == 12 || getStage() == 15))
			updateProgress();
		else if (npc.getId() == MASTER_CHEF) {
			player.getDialogueManager().startDialogue("MasterChef", MASTER_CHEF, this);
			npc.faceEntity(player);
			npc.resetWalkSteps();
			return false;
		} else if (npc.getId() == QUEST_GUIDE) {
			player.getDialogueManager().startDialogue("QuestGuide", QUEST_GUIDE, this);
			npc.faceEntity(player);
			npc.resetWalkSteps();
			return false;
		} else if (npc.getId() == MINING_INSTRUCTOR) {
			player.getDialogueManager().startDialogue("MiningInstructor", MINING_INSTRUCTOR, this);
			npc.faceEntity(player);
			npc.resetWalkSteps();
			return false;
		} else if (npc.getId() == COMBAT_INSTRUCTOR) {
			player.getDialogueManager().startDialogue("CombatInstructor", COMBAT_INSTRUCTOR, this);
			npc.faceEntity(player);
			npc.resetWalkSteps();
			return false;
		} else if (npc.getId() == FINANCIAL_ADVISOR) {
			player.getDialogueManager().startDialogue("FinancialAdvisor", FINANCIAL_ADVISOR, this);
			npc.faceEntity(player);
			npc.resetWalkSteps();
			return false;
		} else if (npc.getId() == BROTHER_BRACE) {
			player.getDialogueManager().startDialogue("BrotherBrace", BROTHER_BRACE, this);
			npc.faceEntity(player);
			npc.resetWalkSteps();
			return false;
		} else if (npc.getId() == MAGIC_INSTRUCTOR) {
			player.getDialogueManager().startDialogue("MagicInstructor", MAGIC_INSTRUCTOR, this);
			npc.faceEntity(player);
			npc.resetWalkSteps();
			return false;
		}
		return true;
	}
	
	@Override
	public void sendInterfaces() {
		int stage = getStage();
		if (stage < 45) {
		player.getInterfaceManager().removeWindowInterface(player.getInterfaceManager().hasRezizableScreen() ? 91 : 207);// Equipment tab
		player.getInterfaceManager().removeWindowInterface(player.getInterfaceManager().hasRezizableScreen() ? 86 : 202);// Attack tab
		}
		if (stage < 10)
		player.getInterfaceManager().removeWindowInterface(player.getInterfaceManager().hasRezizableScreen() ? 88 : 204);// Skill tab
		player.getInterfaceManager().removeWindowInterface(player.getInterfaceManager().hasRezizableScreen() ? 87 : 203);// Achievement tab
		
		if (stage < 5)
		player.getInterfaceManager().removeWindowInterface(player.getInterfaceManager().hasRezizableScreen() ? 90 : 206);// Inventory tab
		if (stage < 61)
		player.getInterfaceManager().removeWindowInterface(player.getInterfaceManager().hasRezizableScreen() ? 92 : 208);// pray tab
		if (stage < 63)
		player.getInterfaceManager().removeWindowInterface(player.getInterfaceManager().hasRezizableScreen() ? 95 : 211);// Friend tab
		
		if (stage < 64) {
		player.getInterfaceManager().removeWindowInterface(player.getInterfaceManager().hasRezizableScreen() ? 96 : 212);// ignore tab
		}
		if (stage < 68) {
			player.getInterfaceManager().removeWindowInterface(player.getInterfaceManager().hasRezizableScreen() ? 93 : 209);// magic tab	
		}
		if (stage <22) {
			player.getInterfaceManager().removeWindowInterface(player.getInterfaceManager().hasRezizableScreen() ? 100 : 216);// Music tab
		}
		if (stage < 24) {
			player.getInterfaceManager().removeWindowInterface(player.getInterfaceManager().hasRezizableScreen() ? 99 : 215);// Emote tab
		}
		if (stage < 29) {
			player.getInterfaceManager().removeWindowInterface(player.getInterfaceManager().hasRezizableScreen() ? 89 : 205);// Quest tab
		}
		player.getInterfaceManager().removeWindowInterface(player.getInterfaceManager().hasRezizableScreen() ? 97 : 213);// Clan tab
		if (stage == 0)
		player.getInterfaceManager().removeWindowInterface(player.getInterfaceManager().hasRezizableScreen() ? 98 : 214);// Settings tab
		player.getInterfaceManager().removeWindowInterface(player.getInterfaceManager().hasRezizableScreen() ? 101 : 217);// Notes tab
		sendProgress();
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

}
