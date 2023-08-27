package com.rs.game.player.dialogues.impl;

import com.rs.game.player.dialogues.Dialogue;

public class GamblerKingD extends Dialogue {


	@Override
	public void start() {
		sendNPCDialogue(16018, NORMAL, "Hi. I am the Dicing king. If you wish to gamble agaisn't me use an item on me. Should you win I will double your reward! If loss... well too bad.");
		stage = 0;
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == 0) {
			stage = 1;
			sendNPCDialogue(16018, NORMAL, "I only gamble one time per day. Good luck bro!");

		} else
			end();
	}

	@Override
	public void finish() {

	}
}
