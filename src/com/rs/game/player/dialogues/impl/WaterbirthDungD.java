package com.rs.game.player.dialogues.impl;

import com.rs.game.WorldTile;
import com.rs.game.player.dialogues.Dialogue;

public class WaterbirthDungD extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Top floor", "Bottom floor (Kings)", "Nowhere");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (componentId == OPTION_1) 
			player.useStairs(-1, new WorldTile(2442, 10147, 0), 0, 2);
		else if (componentId == OPTION_2) 
			player.useStairs(-1, new WorldTile(2900, 4449, 0), 0, 2);
		end();
	}




	@Override
	public void finish() {

	}

}
