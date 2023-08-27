package com.rs.game.player.dialogues.impl;

import com.rs.game.item.Item;
import com.rs.game.player.content.AccessorySmithing;
import com.rs.game.player.dialogues.Dialogue;

public class ImbueingDialouge extends Dialogue {

	@Override
	public void start() {
		sendDialogue("Warning. Imbueing items costs 50 cw tickets.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			stage = 0;
			sendOptionsDialogue("Would you like to imbue your item?", "Yes.", "No.");
		} else if (stage == 0) {
			if (componentId == OPTION_1) {
				int itemId = (int) this.parameters[0];
				int cost = 50 - player.getDonator();
				if (player.getInventory().getAmountOf(4067) >= cost) {
					if (player.isDonator())
						player.getPackets().sendGameMessage("Your rank saved you "+player.getDonator()+" cw tickets.");
					player.getInventory().removeItemMoneyPouch(new Item(4067, cost));
					player.getInventory().deleteItem(itemId, 1);
					player.getInventory().addItemDrop(AccessorySmithing.getImbuedId(itemId), 1);
					sendDialogue("You dip the ring into the fountain and it begins to brightly shimmer.");
				} else
					sendDialogue("You need at least "+cost+" cw tickets in order to imbue an item.");
				stage = 1;
			} else
				end();
		} else if (stage == 1) {
			end();
		}
	}

	@Override
	public void finish() {

	}
}
