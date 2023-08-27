package com.rs.game.player.dialogues.impl;

import com.rs.game.player.Player;
import com.rs.game.player.dialogues.Dialogue;

public class SelectModeDialogue extends Dialogue {

	@Override
	public void start() {
		
	}

	@Override
	public void run(int interfaceId, int componentId) {
		
	}
	
	public static void realFinish(Player player) {
		player.getDialogueManager().startDialogue("SimpleNPCMessage", 946, "Welcome to Matrix!", "If you have any questions make sure to read the guide book in your inventory.");
	}

	@Override
	public void finish() {
		player.getDialogueManager().finishWithoutChecking();
		player.setRun(true);// run energy
		player.setCantWalk(false);
		player.getCutscenesManager().play("HomeCutScene3");
	}
}
