package com.rs.game.player.dialogues.impl;

import com.rs.game.player.Skills;
import com.rs.game.player.content.AccessorySmithing;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.SkillsDialogue.ItemNameFilter;
import com.rs.game.player.dialogues.Dialogue;

public class ZenyteD extends Dialogue {

	private int index;

	@Override
	public void start() {
		this.index = (Integer) parameters[0];
		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "Choose how many you wish to make,<br>then click on the item to begin.", player.getInventory().getItems().getNumberOf(AccessorySmithing.GEMS[index]), new int[]
		{ AccessorySmithing.ITEMS[0][index],AccessorySmithing.ITEMS[1][index],AccessorySmithing.ITEMS[2][index],AccessorySmithing.ITEMS[3][index]}, new ItemNameFilter() {
			int count = 0;

			@Override
			public String rename(String name) {
				int level = AccessorySmithing.LEVEL[count++][index];
				if (player.getSkills().getLevel(Skills.CRAFTING) < level)
					name = "<col=ff0000>" + name + "<br><col=ff0000>Level " + level;
				return name;

			}
		});

	}

	@Override
	public void run(int interfaceId, int componentId) {
		AccessorySmithing.make(player, SkillsDialogue.getItemSlot(componentId), index, SkillsDialogue.getQuantity(player));
		end();
	}

	@Override
	public void finish() {

	}

}
