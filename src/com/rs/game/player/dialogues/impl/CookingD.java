package com.rs.game.player.dialogues.impl;

import com.rs.game.WorldObject;
import com.rs.game.player.actions.Cooking;
import com.rs.game.player.actions.Cooking.Cookables;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.dialogues.Dialogue;

public class CookingD extends Dialogue {

	private Cookables cooking;
	private WorldObject object;

	@Override
	public void start() {
		this.cooking = (Cookables) parameters[0];
		this.object = (WorldObject) parameters[1];
		if (cooking == Cookables.RAW_MEAT || cooking == Cookables.SINEW) {
			end();
			player.getDialogueManager().startDialogue("MeatDrying", object);
			return;
		}
		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.COOK, "Choose how many you wish to cook,<br>then click on the item to begin.", player.getInventory().getItems().getNumberOf(cooking.getRawItem()), new int[]
		{ cooking.getProduct().getId() }, null);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		player.getActionManager().setAction(new Cooking(object, cooking.getRawItem(), SkillsDialogue.getQuantity(player), cooking));
		end();
	}

	@Override
	public void finish() {

	}

}
