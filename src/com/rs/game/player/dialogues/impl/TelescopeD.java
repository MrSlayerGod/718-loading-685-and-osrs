package com.rs.game.player.dialogues.impl;

import com.rs.game.player.dialogues.Dialogue;

public class TelescopeD extends Dialogue {

	@Override
	public void start() {
		player.getInterfaceManager().sendInterface(782);
		String[] messages = new String[parameters.length];
		for (int i = 0; i < messages.length; i++)
			messages[i] = (String) parameters[i];
		sendDialogue(messages);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		player.getInterfaceManager().removeScreenInterface();
		end();
	}

	@Override
	public void finish() {

	}

}
