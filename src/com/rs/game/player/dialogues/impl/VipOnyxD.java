package com.rs.game.player.dialogues.impl;

import com.rs.game.WorldTile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class VipOnyxD extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "VIP Altar + Thieving", "VIP Farming", "VIP Runespan", "More");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1 && componentId == OPTION_4) {
			stage = 0;
			sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "VIP Dragon Cove", "VIP Demons", "Close");
			return;
		}
		if (stage == -1) {
			if (componentId == OPTION_1) 
				Magic.sendCommandTeleportSpell(player, new WorldTile(3728, 5521, 0));
			else if (componentId == OPTION_2) 
				Magic.sendCommandTeleportSpell(player, new WorldTile(3710, 5518, 0));
			else if (componentId == OPTION_3) {
				Magic.sendCommandTeleportSpell(player, new WorldTile(3687, 5529, 0));
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						player.getControlerManager().startControler("RuneSpanControler");
					}
				}, 3);
			}
		} else {
			if (componentId == OPTION_1) 
				Magic.sendCommandTeleportSpell(player, new WorldTile(3748, 5527, 0));
			else if (componentId == OPTION_2) 
				Magic.sendCommandTeleportSpell(player, new WorldTile(3749, 5552, 0));
		
		}
		end();
	}




	@Override
	public void finish() {

	}

}
