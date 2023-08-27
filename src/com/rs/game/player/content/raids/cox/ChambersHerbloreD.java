package com.rs.game.player.content.raids.cox;

import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.SkillsDialogue.ItemNameFilter;
import com.rs.game.player.dialogues.Dialogue;

public class ChambersHerbloreD extends Dialogue {

	private ChambersHerblore.Potions[] items;
	private int tier;

	// componentId, amount, option

	@Override
	public void start() {
		items = (ChambersHerblore.Potions[]) parameters[0];
		tier = (int) parameters[1];

		int[] display = new int[items.length];
		int i = 0;
		for(ChambersHerblore.Potions pot : items)
			display[i++] = pot.potionIds[tier];

		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "Choose how many you wish to make,<br>then click on the item to begin.", 28, display,  new ItemNameFilter() {
			@Override
			public String rename(String name) {
				return name.replace(" (4)", "");
			}
		});
	}

	@Override
	public void run(int interfaceId, int componentId) {
		int option = SkillsDialogue.getItemSlot(componentId);
		if (option > items.length) {
			end();
			return;
		}
		int quantity = SkillsDialogue.getQuantity(player);
		int invQuantity = player.getInventory().getItems().getNumberOf(items[option].secondaryId);
		if (quantity > invQuantity)
			quantity = invQuantity;
		ChambersHerblore.mixPotion(player, items[option], quantity, tier);
		end();
	}

	@Override
	public void finish() {
	}

}
