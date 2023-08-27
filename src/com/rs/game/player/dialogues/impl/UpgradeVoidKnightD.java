package com.rs.game.player.dialogues.impl;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.player.dialogues.Dialogue;

public class UpgradeVoidKnightD extends Dialogue {

	private static final int ID = 25513;
	@Override
	public void start() {
		sendNPCDialogue(ID, NORMAL, "Hey! Do you want me to upgrade your void to elite void for 200 commendation points each?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			stage = 0;
			sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Yes, upgrade my top.", "Yes, upgrade my robe.", "No.");
		} else if (stage == 0) {
			if (componentId != OPTION_3) {
				stage = 1;
				int id = componentId == OPTION_1 ? 8839 : 8840;
				if (!player.getInventory().containsOneItem(id)) {
					sendNPCDialogue(ID, NORMAL, "Come back once you have a "+ItemConfig.forID(id).getName().toLowerCase()+" in your inventory.");
					return;
				}
				if (player.getPestPoints() < 200) {
					sendNPCDialogue(ID, NORMAL, "Come back once you have 200 commendation points.");
					return;
				}
				sendNPCDialogue(ID, NORMAL, "Welcome to the elite knight party!");
				player.getInventory().deleteItem(id, 1);
				player.getInventory().addItem(componentId == OPTION_1 ? 19785 : 19786, 1);
				player.setPestPoints(player.getPestPoints() - 200);
			} else
				end();
		} else {
			end();
		}
	}

	@Override
	public void finish() {

	}

}
