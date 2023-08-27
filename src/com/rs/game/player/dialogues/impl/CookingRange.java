package com.rs.game.player.dialogues.impl;

import java.util.Arrays;

import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Cooking;
import com.rs.game.player.actions.Cooking.Cookables;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.dialogues.Dialogue;

public class CookingRange extends Dialogue {

	private WorldObject object;
	private int[] ids;
	@Override
	public void start() {
		object = (WorldObject) parameters[0];
		ids = new int[10];
		int count = 0;
		for (Cookables cook : Cooking.Cookables.values()) {
			if (player.getInventory().containsItems(cook.getRawItem()) && cook.getLvl() < player.getSkills().getLevel(Skills.COOKING)) {
				ids[count++] = cook.getRawItem().getId();
				if (count >= 10)
					break;
			}
		}
		if (count == 0) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You don't have anything to cook.");
			return;
		}
		ids = Arrays.copyOf(ids, count);
		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "Choose how many you wish to cook,<br>then click on the item to begin.", 28, ids, null);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		int slot = SkillsDialogue.getItemSlot(componentId);
		if (slot >= ids.length) {
			end();
			return;
		}
		Cookables cooking = Cooking.isCookingSkill(new Item(ids[slot]));
		if (cooking != null) 
			player.getActionManager().setAction(new Cooking(object, cooking.getRawItem(), SkillsDialogue.getQuantity(player), cooking));
		end();
	}

	@Override
	public void finish() {
	}
}
