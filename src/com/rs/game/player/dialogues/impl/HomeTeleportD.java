package com.rs.game.player.dialogues.impl;

import com.rs.game.player.actions.HomeTeleport;
import com.rs.game.player.content.EconomyManager;
import com.rs.game.player.content.teleportation.TeleportationInterface;
import com.rs.game.player.dialogues.Dialogue;

public class HomeTeleportD extends Dialogue {


	@Override
	public void start() {
	//	if (player.isDonator())
			sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Home", "Donator Zone", "Lodestone Network", "Teleports");
	/*	else
			sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Home", "Lodestone Network"/*, "Teleports"*/
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
		if (componentId == OPTION_1) {
			HomeTeleport.useLodestone(player, -2);
		} else if (player.isDonator() && componentId == OPTION_2) {
			HomeTeleport.useLodestone(player, -3);
		} else if (componentId == OPTION_3)  {
			player.stopAll();
			player.getInterfaceManager().sendInterface(1092);
		} else if (player.isDonator() && componentId == OPTION_4) {
			//EconomyManager.openTPS(player);
			TeleportationInterface.openInterface(player);
		}
		
	}

	@Override
	public void finish() {

	}
}
