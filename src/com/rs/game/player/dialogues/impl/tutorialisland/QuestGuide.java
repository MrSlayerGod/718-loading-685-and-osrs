package com.rs.game.player.dialogues.impl.tutorialisland;

import com.rs.game.player.controllers.TutorialIsland;
import com.rs.game.player.dialogues.Dialogue;

public class QuestGuide extends Dialogue {

	int npcId;
	TutorialIsland controler;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		controler = (TutorialIsland) parameters[1];
		int s = controler.getStage();
		if (s == 29) {
			sendNPCDialogue(npcId, 9827, "Ah. Welcome, adventurer. I'm here to tell you all about", "quests. Let's start by opening the quest side panel.");
		} else if (s == 31) {
			sendNPCDialogue(npcId, 9827, "Now you have the journal open I'll tell you a bit about", "it. At the moment all the quests are shown in red, which", "means you have not started them yet.");
			player.getHintIconsManager().removeUnsavedHintIcon();
			stage = 0;
		} else
			end();
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			stage = 0;
			controler.updateProgress();
			end();
		} else if (stage == 0) {
			stage = 1;
			sendNPCDialogue(npcId, 9827, "When you start a quest it will change colour to yellow,", "and to green when you've finished. This is so you can", "easily see what's complete, what's started, and what's left", "to begin.");
		} else if (stage == 1) {
			stage = 2;
			sendNPCDialogue(npcId, 9827, "The start of quests are easy to find. Look out for the", "star icons on the minimap, just like the one you should", "see marking my house.");
		} else if (stage == 2) {
			stage = 3;
			sendNPCDialogue(npcId, 9827, "The quests themselves can vary greatly from collecting", "beads to hunting down dragons. Generally quests are", "started by talking to a non-player character like me,", "and will involve a series of tasks.");
		} else if (stage == 3) {
			stage = 4;
			sendNPCDialogue(npcId, 9827, "There's a lot more I can tell you about questing.", "You have to experience the thrill of it yourself to fully", "understand. You may find some adventure in the caves", "under my house.");
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
