package com.rs.game.player.dialogues.impl;

import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.player.actions.Cooking;
import com.rs.game.player.actions.Cooking.Cookables;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.dialogues.Dialogue;

public class MeatDrying extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue("What would you like to do?", "Cook the meat.", "Dry the meat.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			player.getActionManager().setAction(new Cooking((WorldObject) this.parameters[0], new Item(2132, 1), SkillsDialogue.getQuantity(player), componentId == OPTION_1 ? Cookables.RAW_MEAT : Cookables.SINEW));
			end();
		}
	}

	@Override
	public void finish() {

	}
}
