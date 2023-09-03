package com.rs.game.player.dialogues.impl.tutorialisland;


import com.rs.game.player.controllers.TutorialIsland;
import com.rs.game.player.dialogues.Dialogue;

public class TutBanker extends Dialogue {

	int npcId;
	TutorialIsland controler;
	
	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		controler = (TutorialIsland) parameters[1];
		int s = controler.getStage();
		if (s == 57) {
			sendNPCDialogue(npcId, 9827, "Good day, would you like to access your bank account?");
			player.getHintIconsManager().removeUnsavedHintIcon();
			stage = 2;
		}
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == 2) {
			stage = 3;
			sendOptionsDialogue("Select an Option", "Yes.", "No Thanks.");
		} else if (stage == 3) {
			if (componentId == OPTION_1) {
				player.getBank().openBank();
				controler.updateProgress();
				end();
			} else if (componentId == OPTION_2) {
			end();
			}
		}
	}

	@Override
	public void finish() {

	}

}
