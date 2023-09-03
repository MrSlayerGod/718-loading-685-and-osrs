package com.rs.game.player.dialogues.impl.tutorialisland;

import com.rs.game.player.controllers.TutorialIsland;
import com.rs.game.player.dialogues.Dialogue;

public class MiningInstructor extends Dialogue {

	int npcId;
	TutorialIsland controler;
	
	int PICK_AXE = 1265;
	int HAMMER = 2347;
	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		controler = (TutorialIsland) parameters[1];
		int s = controler.getStage();
		if (s == 33) {
			sendNPCDialogue(npcId, 9827, "Hi there. You must be new around here. So what do I", "call you? 'Newcomer' seems so impersonal, and if we're", "going to be working together, I'd rather call you by", "name.");
			player.getHintIconsManager().removeUnsavedHintIcon();
		} else if (s == 36) {
			sendPlayerDialogue(9827, "I prospected both types of rock! One set contains tin", "and the other has copper ore inside.");
			player.getHintIconsManager().removeUnsavedHintIcon();
			stage = 2;
		} else if (s == 40) {
			sendPlayerDialogue(9827, "How do I make a weapon out of this?");
			player.getHintIconsManager().removeUnsavedHintIcon();
			stage = 6;
		} else
			end();
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			stage = 0;
			sendPlayerDialogue(9827, "You can call me " + player.getDisplayName() + ".");
		} else if (stage == 0) {
			stage = 1;
			sendNPCDialogue(npcId, 9827, "Ok then, "+player.getDisplayName()+". My name is Dezzick and I'm a miner", "by trade. Let's prospect some of those rocks."); //end and update after this
		} else if (stage == 1) {
			controler.updateProgress(); //Stage 34 now 
			end();
		} else if (stage == 2) { //STAGEE 36 start 
			stage = 3;
			sendNPCDialogue(npcId, 9827, "Absolutely right, "+player.getDisplayName()+ ". These two ore types, ", "can be smelted together to make bronze.");
		} else if (stage == 3) {
			stage = 4;
			sendNPCDialogue(npcId, 9827, "So now you know what ore is in the rocks over there,", "why don't you have a go at mining some tin and", "copper? Here, you'll need this to start with.");
		} else if (stage == 4) {
			stage = 5;
			sendItemDialogue(1265, "Dezzick gives you a <col=0000FF>bronze pickaxe</col>!");
		} else if (stage == 5) {
			player.getInventory().addItem(PICK_AXE, 1); //add pickaxe and update progress 
			controler.updateProgress(); //update to 37
			end();
		} else if (stage == 6) {
			sendNPCDialogue(npcId, 9827, "Okay, I'll show you how to make a dagger out of it.", "You'll be needing this...");
			stage = 7;
		} else if (stage == 7) {
			sendItemDialogue(2347, "Dezzick gives you a <col=0000FF>hammer</col>!");
			stage = 8;
		} else if (stage == 8) {
			player.getInventory().addItem(HAMMER, 1);
			controler.updateProgress(); //update to 41
			end();
		}
		
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

}
