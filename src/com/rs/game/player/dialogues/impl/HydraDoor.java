package com.rs.game.player.dialogues.impl;

import com.rs.game.player.controllers.HydraLair;
import com.rs.game.player.dialogues.Dialogue;

public class HydraDoor extends Dialogue {

	private HydraLair lair;
	@Override
	public void start() {
		lair = (HydraLair) parameters[0];
		sendOptionsDialogue("Enter the laboratory? The door is notorious for getting jammed.", "Yes.", "No.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
		lair.passBossDoor();
		
	}

	@Override
	public void finish() {

	}

}
