package com.rs.game.player.dialogues.impl;

import com.rs.game.minigames.EvilTrees;
import com.rs.game.player.content.Magic;
import com.rs.game.player.content.SpiritTree;
import com.rs.game.player.dialogues.Dialogue;

public class SpiritTreeD extends Dialogue {

	private int npcId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		if (EvilTrees.isAlive()) {
			sendNPCDialogue(npcId, 9827, "I sense an evil tree nearby.", " Do you wish me to take you there?");
		} else
			sendNPCDialogue(npcId, 9827, "If you are a friend of the gnome people, you are a friend of mine.", " Do you wish to travel?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			stage = 0;
			sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Yes please.", "No thanks.");
		} else if (stage == 0) {
			if (componentId == OPTION_1) {
				stage = 1;
				sendPlayerDialogue(9827, "Yes please.");
			} else {
				stage = 2;
				sendPlayerDialogue(9827, "No thanks.");
			}
		} else if (stage == 1) {
			if (EvilTrees.isAlive())
				Magic.sendTeleportSpell(player, 7082, 7084, 1229, 1229, 1, 0,  EvilTrees.getTile().transform(-1, -1, 0), 4, true, Magic.OBJECT_TELEPORT);
			else
				SpiritTree.openInterface(player, npcId == 3636);
			end();
		} else if (stage == 2) {
			end();
		}
	}

	@Override
	public void finish() {

	}
}
