package com.rs.game.player.dialogues.impl;

import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.dialogues.Dialogue;

public class KeepsakeItem extends Dialogue {

	public static final int KEY = 25574;
	Item item;

	@Override
	public void start() {
		item = (Item) parameters[0];
		player.getInterfaceManager().sendChatBoxInterface(1183);
		player.getPackets().sendIComponentText(1183, 7, item.getName());
		player.getPackets().sendIComponentText(1183, 12, "This keepsake key will dissapear upon use.");
		player.getPackets().sendItemOnIComponent(1183, 13, item.getId(), 1);
		player.getPackets().sendIComponentText(1183, 22, "Are you sure you want to keepsake this object?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
		if (interfaceId == 1183 && componentId == 9) {
			int slot = item.getDefinitions().getEquipSlot();
			if (slot == -1 /*|| slot == Equipment.SLOT_WEAPON*/ || slot == Equipment.SLOT_ARROWS || slot == Equipment.SLOT_RING || slot == Equipment.SLOT_AURA) {
				player.getPackets().sendGameMessage("You can not keep sake this item!");
				return;
			}
			if (!ItemConstants.hasLevel(item, player) || !ItemConstants.canWear(item, player)) 
				return;
			player.getInventory().deleteItem(KEY, 1);
			player.getEquipment().getKeepsakeItems().set(item.getDefinitions().getEquipSlot(), item);
			player.getAppearence().generateAppearenceData();
			player.getPackets().sendGameMessage("Keepsaked: "+item.getName()+".");
		}
	}

	@Override
	public void finish() {

	}

}
