package com.rs.game.player.dialogues.impl.tutorialisland;

import com.rs.game.player.controllers.TutorialIsland;
import com.rs.game.player.dialogues.Dialogue;

public class FinancialAdvisor extends Dialogue {

	int npcId;
	TutorialIsland controler;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		controler = (TutorialIsland) parameters[1];
		int s = controler.getStage();
		if (s == 59) {
			sendPlayerDialogue(9827, "Hello, Who are you?");
		} else {
			sendOptionsDialogue("Would you like to hear about making money again?", "Yes!", "No thanks.");
		}
		//} else
		//	end();
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			stage = 0;
			sendNPCDialogue(npcId, 9827, "I'm the Financial Advisor. I'm here to tell people how to", "make money.");
		} else if (stage == 0) {
			stage = 1;
			sendPlayerDialogue(9827, "Okay. How can I make money then?");
		} else if (stage == 1) {
			stage = 2;
			sendNPCDialogue(npcId, 9827, "How you can make money? Quite.");
		} else if (stage == 2) {
			stage = 3;
			sendNPCDialogue(npcId, 9827, "Well, there are three basic ways of making money here:", "combat, quests and trading. I will talk you through each", "of them very quickly.");
		} else if (stage == 3) {
			stage = 4;
			sendNPCDialogue(npcId, 9827, "Let's start with combat as it is probably still fresh in", "your mind. Many enemies, both human and monster,", "will drop items when they die.");
		} else if (stage == 4) {
			stage = 5;
			sendNPCDialogue(npcId, 9827, "Now, the next way to earn money quickly is by quests.", "Many people on Runescape have things they need", "doing, which they will reward you for.");
		} else if (stage == 5) {
			stage = 6;
			sendNPCDialogue(npcId, 9827, "By getting a high level in skills such as Cooking, Mining,", "Smithing or Fishing, you can create or catch your own", "items and sell them for pure profit.");
		} else if (stage == 6) {
			stage = 7;
			sendNPCDialogue(npcId, 9827, "Well, that about covers it. Come back if you'd like to go", "over this again.");
		} else {
			controler.updateProgress();
			end();
		}
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

}
