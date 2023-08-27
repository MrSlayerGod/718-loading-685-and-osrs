package com.rs.game.player.dialogues.impl;

import com.rs.game.WorldTile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogues.Dialogue;

public class TrainCommand extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Rock Crabs", "Sand Crabs", "Ammonite Crabs", "Cancel");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (componentId == OPTION_1) 
			Magic.sendCommandTeleportSpell(player, new WorldTile(2675, 3712, 0));
		else if (componentId == OPTION_2) 
			Magic.sendCommandTeleportSpell(player, new WorldTile(1868, 3551, 0));
		else if (componentId == OPTION_3) 
			Magic.sendCommandTeleportSpell(player, new WorldTile(3732, 3845, 0));
		end();
	}




	@Override
	public void finish() {

	}

}
