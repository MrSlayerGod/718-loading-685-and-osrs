package com.rs.game.player.dialogues.impl;

import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.MTopDonator;
import com.rs.utils.TopDonator;

public class DonatorHiscoresD extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "All Time Top Donators", "Monthly Top Donators");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
		if (componentId == OPTION_1)
			TopDonator.showRanks(player);
		else if (componentId == OPTION_2)
			MTopDonator.showRanks(player);

	}

	@Override
	public void finish() {
		
	}
}
