package com.rs.game.player.dialogues.impl;

import com.rs.game.player.SlayerManager;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.ShopsHandler;

public class SlayerRewardsD extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Rewards Interface", "Rewards Shop.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
		if (componentId == OPTION_1) 
			player.getSlayerManager().sendSlayerInterface(SlayerManager.BUY_INTERFACE);
		else
			ShopsHandler.openShop(player, 279);
	}

	@Override
	public void finish() {

	}

}
