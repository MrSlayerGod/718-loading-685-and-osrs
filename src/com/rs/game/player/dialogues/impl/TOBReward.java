package com.rs.game.player.dialogues.impl;

import com.rs.game.player.content.raids.TheatreOfBlood;
import com.rs.game.player.dialogues.Dialogue;

public class TOBReward extends Dialogue {

	private TheatreOfBlood instance;
	
	@Override
	public void start() {
		instance = (TheatreOfBlood) parameters[0];
		sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Theatre of Blood Rewards", "Chambers of Xeric Rewards", "Cancel");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (componentId == OPTION_2) 
			instance.getRewards(player);
		end();
		if (componentId != OPTION_3)
			instance.openChest(player);
	}




	@Override
	public void finish() {

	}

}
