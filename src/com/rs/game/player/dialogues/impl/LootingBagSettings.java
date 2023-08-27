package com.rs.game.player.dialogues.impl;

import com.rs.game.player.dialogues.Dialogue;

public class LootingBagSettings extends Dialogue {

	@Override
	public void start() {
		if (player.isDungeoneer()) //not sure why disabled this for fast mode
			return;
		sendDialogue("How many items do you wish to store?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			stage = 0;
			sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "One", "Five", "All");
		} else if (stage == 0) {
			player.getLootingBag().setDepositQuantity(componentId == OPTION_1 ? 1 : componentId == OPTION_2 ? 5 : Integer.MAX_VALUE);
			end();
		}
	}

	@Override
	public void finish() {

	}

}
