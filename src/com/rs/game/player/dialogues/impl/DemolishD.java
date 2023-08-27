package com.rs.game.player.dialogues.impl;

import com.rs.game.npc.others.GraveStone;
import com.rs.game.player.dialogues.Dialogue;

public class DemolishD extends Dialogue {

	private GraveStone gravestone;
	
	@Override
	public void start() {
		gravestone = (GraveStone) parameters[0];
		sendDialogue("Warning! Demolishing will turn all your untradeable items into coins. Are you sure you wish to demolish?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			stage = 0;
			sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Yes.", "No.");
		} else if (stage == 0) {
			if (componentId == OPTION_1) 
				gravestone.confirmDemolish(player);
			end();
		}
	}

	@Override
	public void finish() {

	}

}
