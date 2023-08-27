package com.rs.game.player.dialogues.impl;

import com.rs.game.player.OwnedObjectManager;
import com.rs.game.player.content.DwarfMultiCannon;
import com.rs.game.player.dialogues.Dialogue;

public class ReplaceCannon extends Dialogue {

	private int npcId = 209;

	@Override
	public void start() {
		if (OwnedObjectManager.containsObjectValue(player, DwarfMultiCannon.CANNON_OBJECTS)) {
			sendNPCDialogue(npcId, NORMAL, "It seems your dwarf multicannon is still up. Come back later!");
			return;
		}
		if (!player.isLostCannon()) 
			sendNPCDialogue(npcId, NORMAL, "Sorry, you haven't lost any dwarf multicannon.");
		else {
			sendNPCDialogue(npcId, NORMAL, "It seems you lost your dwarf multicannon. Enjoy your new cannon!");
			player.setLostCannon(false);
			for (int i : DwarfMultiCannon.CANNON_PIECES)
				player.getInventory().addItemDrop(i, 1);
		}
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
	}

	@Override
	public void finish() {

	}
}
