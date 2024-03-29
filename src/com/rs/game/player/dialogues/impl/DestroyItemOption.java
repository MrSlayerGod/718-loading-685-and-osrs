package com.rs.game.player.dialogues.impl;

import com.rs.game.item.Item;
import com.rs.game.player.content.RunePouch;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.ItemDestroys;

public class DestroyItemOption extends Dialogue {

	int slotId;
	Item item;

	@Override
	public void start() {
		slotId = (Integer) parameters[0];
		item = (Item) parameters[1];
		player.getInterfaceManager().sendChatBoxInterface(1183);
		player.getPackets().sendIComponentText(1183, 7, item.getName());
		player.getPackets().sendIComponentText(1183, 12, ItemDestroys.getDestroy(item));
		player.getPackets().sendItemOnIComponent(1183, 13, item.getId(), 1);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (interfaceId == 1183 && componentId == 9) {
			player.getInventory().deleteItem(slotId, item);
			player.getCharges().degradeCompletly(item);
			player.getPackets().sendSound(4500, 0, 1);
			if (item.getDefinitions().isBinded())
				player.getDungManager().unbind(item);
			else if (item.getId() == 41941)
				player.getLootingBag().destroy();
			else if (item.getId() == RunePouch.ID)
				player.getRunePouch().clear();
		}
		end();
	}

	@Override
	public void finish() {

	}

}
