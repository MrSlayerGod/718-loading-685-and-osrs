package com.rs.game.player.dialogues.impl;

import com.rs.game.WorldTile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.controllers.SkotizoLair;
import com.rs.game.player.dialogues.Dialogue;

public class CatacombsTeleport extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue(Dialogue.DEFAULT_OPTIONS_TITLE, "Fight boss.", "Teleport");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			if (componentId == OPTION_1) {
				stage = 0;
				sendOptionsDialogue("Are you sure you wish to fight Skotizo?", "Yes.", "No.");
			} else {
				stage = 1;
				sendOptionsDialogue(Dialogue.DEFAULT_OPTIONS_TITLE, "Demon's Run", "Dragon's Den", "Reeking Cove", "The Shallows", "Cancel");
			}
		} else {
			end();
			if (stage == 1) {
				WorldTile tile = componentId == OPTION_1 ? new WorldTile(1719, 10101, 0)
						: componentId == OPTION_2 ? new WorldTile(1617, 10101, 0)
						: componentId == OPTION_3 ? new WorldTile(1650, 9987, 0)
						: componentId == OPTION_4 ? new WorldTile(1726, 9992, 0)
						: null;
				if (tile != null)
					Magic.sendObjectTeleportSpell(player, false, tile);
			} else if (componentId == OPTION_1)
				SkotizoLair.enter(player);
		}
	}

	@Override
	public void finish() {

	}

}
