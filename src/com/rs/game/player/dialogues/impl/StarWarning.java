package com.rs.game.player.dialogues.impl;

import com.rs.game.minigames.ShootingStars;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogues.Dialogue;

public class StarWarning extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Teleport to wilderness.", "Another time.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (componentId == OPTION_1) 
			Magic.sendCommandTeleportSpell(player, ShootingStars.getStarSprite());
		end();
	}




	@Override
	public void finish() {

	}

}
