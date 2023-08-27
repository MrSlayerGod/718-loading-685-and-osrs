package com.rs.game.player.dialogues.impl;

import com.rs.game.player.dialogues.Dialogue;

public class Ilfeen extends Dialogue {
	
	private static final int NPC_ID = 1;

	@Override
	public void start() {
		sendPlayerDialogue(NORMAL, "Hello again. Are you still offering to enchant seeds?");
		stage = -1;
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			sendNPCDialogue(NPC_ID, NORMAL, "I am, but you will need your own seed. I can also charge your shield or bow back to full charges if you have it with you.");
		}
	}

	@Override
	public void finish() {

	}
}
