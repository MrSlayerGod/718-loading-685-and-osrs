package com.rs.game.player.dialogues.impl;

import com.rs.game.player.content.SacrificeAltar;
import com.rs.game.player.dialogues.Dialogue;

public class SacrificeAltarD extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Sacrifice Fire Cape", "Sacrifice Twisted Bow", "Cancel");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (componentId == OPTION_1)
			SacrificeAltar.claimFireCape(player);
		else if (componentId == OPTION_2)
			SacrificeAltar.claimTwistedBow(player);
		end();
	}

	@Override
	public void finish() {

	}

}
