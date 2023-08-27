package com.rs.game.player.dialogues.impl;

import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.Utils;

public class MakePresetD extends Dialogue {

	private int index;
	private String name;
	
	@Override
	public void start() {
		index = (Integer) parameters[0];
		name = (String) parameters[1];
		sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Set Inventory & Equipment", "Set Inventory", "Set Equipment");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			end();
			if (componentId == OPTION_4) 
				return;
			player.getPresets().set(index, Utils.formatPlayerNameForDisplay(name), componentId == OPTION_1 || componentId == OPTION_2,  componentId == OPTION_1 ||  componentId == OPTION_3 );
		}
	}

	@Override
	public void finish() {

	}

}
