package com.rs.game.player.dialogues.impl.tutorialisland;

import com.rs.game.player.controllers.TutorialIsland;
import com.rs.game.player.dialogues.Dialogue;

public class CombatInstructor extends Dialogue {

	int npcId;
	TutorialIsland controler;
	
	int short_sword = 1277;
	int shield = 1171;
	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		controler = (TutorialIsland) parameters[1];
		int s = controler.getStage();
		if (s == 44) {
			sendPlayerDialogue(9827, "Hi! My name's " + player.getDisplayName() + ".");
			player.getHintIconsManager().removeUnsavedHintIcon();
		} else if (s == 48) {
			sendNPCDialogue(npcId, 9827, "Very good, but that little butter knife isn't going to", "protect you much. Here, take these.");
			player.getHintIconsManager().removeUnsavedHintIcon();
			stage = 2;
		} else if (s == 54) {
			sendPlayerDialogue(9827, "I did it! I killed a giant rat!");
			player.getHintIconsManager().removeUnsavedHintIcon();
			stage = 4;
		} else
			end();
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			stage = 0;
			sendNPCDialogue(npcId, 9827, "Do I look like I care? To me you're just another", "newcomer who thinks they're ready to fight.");
		} else if (stage == 0) {
			stage = 1;
			sendNPCDialogue(npcId, 9827, "I am Vannaka, the greatest swordsman alive, and I'm", "here to teach you the basics of combat. Let's get started", "by teaching you to wield a weapon."); //end and update after this
		} else if (stage == 1) {
			controler.updateProgress(); //Stage 45 now 
			end();
		} else if (stage == 2) {
			sendItemDialogue(1277, "The Combat Guide gives you a <col=0000FF>bronze sword</col> and a <col=0000FF>wooden shield</col>!"); //to fix
			stage = 3;
		} else if (stage == 3) {
			player.getInventory().addItem(short_sword, 1);
			player.getInventory().addItem(shield, 1);
			end();
			controler.updateProgress(); 
		} else if (stage == 4) {
			sendNPCDialogue(npcId, 9827, "I saw. You seem better at this than I thought. Now", "that you have grasped basic swordplay, let's move on.");
			stage = 5;
		} else if (stage == 5) {
			sendNPCDialogue(npcId, 9827, "Let's try some ranged attacking, with this you can kill", "foes from a distance. Also, foes unable to reach you are", "as good as dead. You'll be able to attack the rats", "without entering the pit.");
			stage = 6;
		} else if (stage == 6) {
			sendItemDialogue(1277, "The Combat Guide gives you a <col=0000FF>shortbow</col> and some <col=0000FF>arrows</col>!");
			stage = 7;
		} else if (stage == 7) {
			player.getInventory().addItem(841, 1);
			player.getInventory().addItem(882, 50);
			end();
			controler.updateProgress(); //55
		} 
		
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

}
