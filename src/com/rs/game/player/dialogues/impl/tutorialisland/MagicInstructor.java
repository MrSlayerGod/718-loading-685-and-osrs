package com.rs.game.player.dialogues.impl.tutorialisland;

import com.rs.Settings;
import com.rs.game.WorldTile;
import com.rs.game.player.controllers.TutorialIsland;
import com.rs.game.player.dialogues.Dialogue;

public class MagicInstructor extends Dialogue {

	int npcId;
	TutorialIsland controler;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		controler = (TutorialIsland) parameters[1];
		int s = controler.getStage();
		if (s == 68) {
			sendPlayerDialogue(9827, "Good day, brother my name's " + player.getUsername() + ".");
			player.getHintIconsManager().removeUnsavedHintIcon();
		} else if (s == 70) {
			sendNPCDialogue(npcId, 9827, "Good. This is a list of your spells, Currently you can", "only cast one offensive spell called", "Wind Strike. Let's try it out on one of those chickens.");
			player.getHintIconsManager().removeUnsavedHintIcon();
			stage = 1;
		} else if (s == 72) {
			sendNPCDialogue(npcId, 9827, "Well, you're all finished here now. I'll give you a", "reasonable number of runes when you leave.");
			player.getHintIconsManager().removeUnsavedHintIcon();
			stage = 3;
		} else
			end();
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			sendNPCDialogue(npcId, 9827, "Good day, newcomer. My name is Terrova. I'm here", "to tell you about Magic. Let's start by opening your", "spell list.");
			stage = 0;
		 } else if (stage == 0) {
				controler.updateProgress(); //69 Opening magic tab
				end();
		 } else if (stage == 1) {
			 sendItemDialogue(558, "Terrova gives you some <col=0000FF>air runes</col> and <col=0000FF>mind runes</col>."); //to fix
				stage = 2;
		 } else if (stage == 2) {
			player.getInventory().addItem(558, 5);
			player.getInventory().addItem(556, 5);
			end();
			controler.updateProgress();  //71
		 } else if (stage == 3) {
			 sendOptionsDialogue("Do you want to go to the mainland?", "Yes.", "No.");
			 controler.sendProgress(20);
				stage = 4;
		 } else if (stage == 4) {
				if (componentId == OPTION_1) {
					 sendNPCDialogue(npcId , 9827, "When you get to the mainland you will find yourself in", "the town of Lumbridge. If you want some ideas on", "where to go next, talk to my friend the Lumbridge", "Guide. You can't miss him; he's holding a big staff with"); //to fix
					stage = 6;
				} else if (componentId == OPTION_2) {
				end();
				}
		 } else if (stage == 6) {
			 sendNPCDialogue(npcId , 9827, "a question mark on the end. He also has a white beard", "and carries a rucksack full of scrolls. There are also", "many tutors willing to teach you about the many skills", "you could learn."); //to fix
			 stage = 7;
		 } else if (stage == 7) {
			 sendItemDialogue(5079, "When you get to Lumbridge, look for this icon on your", "mini-map. The Lumbridge Guide or one of the other", "tutors should be standing near there. The Lumbridge", "Guide should be standing slightly to the north-east of"); //to fix
			 stage = 8;
	 	} else if (stage == 8) {
		 sendItemDialogue(5079, "the castle's courtyard and the others you will find", "scattered around Lumbridge."); //to fix
			stage = 9;
	 	} else if (stage == 9) {
	 		sendNPCDialogue(npcId, 9827, "If all else fails, visit the Runescape website for a whole", "chestload of information on quests, skills and minigames", "as well as a very good starter's guide.");
			 stage = 10;
	 	} else if (stage == 10) {
	 		end();
	 		player.getControlerManager().forceStop();
	 		player.getInterfaceManager().removeInterfaceByParent(752, 11);
			player.getInterfaceManager().sendInterfaces();
			player.getInventory().unlockInventoryOptions();
			player.getInterfaceManager().removeWindowInterface(player.getInterfaceManager().hasRezizableScreen() ? 6 : 17);
			player.getHintIconsManager().removeUnsavedHintIcon();
	 		player.setNextWorldTile(new WorldTile(Settings.START_PLAYER_LOCATION));
			player.getGameMode();//TODO;
	 	}
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

}
