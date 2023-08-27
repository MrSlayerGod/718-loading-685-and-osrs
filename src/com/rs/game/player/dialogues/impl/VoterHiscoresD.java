package com.rs.game.player.dialogues.impl;

import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.MTopVoter;
import com.rs.utils.TopVoter;

public class VoterHiscoresD extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "All Time Top Voters", "Monthly Top Voters");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
		if (componentId == OPTION_1)
			TopVoter.showRanks(player);
		else if (componentId == OPTION_2)
			MTopVoter.showRanks(player);

	}

	@Override
	public void finish() {
		
	}
}
