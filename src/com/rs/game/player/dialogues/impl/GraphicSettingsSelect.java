package com.rs.game.player.dialogues.impl;

import com.rs.game.player.content.ExtraSettings;
import com.rs.game.player.dialogues.Dialogue;

public class GraphicSettingsSelect extends Dialogue {

	@Override
	public void start() {
			sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "GFX Settings", "Game Settings", "Cancel");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
		if (componentId == OPTION_1)
			player.getInterfaceManager().sendInterface(742);
		else if (componentId == OPTION_2)
			ExtraSettings.open(player);
	}




	@Override
	public void finish() {

	}

}
