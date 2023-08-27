package com.rs.game.player.dialogues.impl;

import com.rs.game.player.dialogues.Dialogue;

public class LoadPresetD extends Dialogue {

	private int index;
	
	@Override
	public void start() {
		index = (Integer) parameters[0];
		sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Load", "Empty");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			end();
			if (componentId == OPTION_1)
				player.getPresets().load(index);
			else
				player.getPresets().empty(index);
		}
	}

	@Override
	public void finish() {

	}

}
