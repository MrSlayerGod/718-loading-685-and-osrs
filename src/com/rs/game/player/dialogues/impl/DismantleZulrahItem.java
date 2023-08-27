package com.rs.game.player.dialogues.impl;

import com.rs.game.item.Item;
import com.rs.game.player.dialogues.Dialogue;

public class DismantleZulrahItem extends Dialogue {

	int slotId;
	Item item;

	@Override
	public void start() {
		slotId = (Integer) parameters[0];
		item = (Item) parameters[1];
		player.getInterfaceManager().sendChatBoxInterface(1183);
		player.getPackets().sendIComponentText(1183, 7, item.getName());
		player.getPackets().sendIComponentText(1183, 12, "This item will turn into 20k zulrah scales upon dismantle.");
		player.getPackets().sendIComponentText(1183, 22, "Are you sure you want to dismantle this item?");
		player.getPackets().sendItemOnIComponent(1183, 13, item.getId(), 1);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (interfaceId == 1183 && componentId == 9) {
			player.getInventory().deleteItem(slotId, item);
			player.getInventory().addItem(42934, 20000);
			player.getPackets().sendGameMessage("You dismantle this item.", true);
		}
		end();
	}

	@Override
	public void finish() {

	}

}
