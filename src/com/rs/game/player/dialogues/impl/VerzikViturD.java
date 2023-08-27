package com.rs.game.player.dialogues.impl;

import com.rs.game.npc.theatreOfBlood.verzikVitur.VerzikVitur;
import com.rs.game.player.dialogues.Dialogue;

public class VerzikViturD extends Dialogue {

	private VerzikVitur boss;

	@Override
	public void start() {
		boss = (VerzikVitur) parameters[0];
		sendNPCDialogue(boss.getId(), NORMAL, "Now that was quite the show! I haven't been that entertained in a long time.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			stage = 0;
			sendNPCDialogue(boss.getId(), NORMAL, "Of course, you know I can't let you leave here alive. Time for your final performance...");
			break;
		case 0:
			stage = 1;
			sendOptionsDialogue("Is your party ready to fight?", "Yes, let's begin.", "No, don't start yet.");
			break;
		case 1:
			if (componentId == OPTION_1) {
				stage = 2;
				sendPlayerDialogue(NORMAL, "Yes, let's begin.");
			} else {
				stage = -2;
				sendPlayerDialogue(NORMAL, "No, don't start yet.");
			}
			break;
		case 2:
			stage = -2;
			sendNPCDialogue(boss.getId(), NORMAL, "Oh I'm going to enjoy this...");
			boss.start();
			break;
		default:
			end();
			break;
		}
	}

	@Override
	public void finish() {
		
	}

}
