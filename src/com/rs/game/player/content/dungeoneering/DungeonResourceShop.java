package com.rs.game.player.content.dungeoneering;

import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.ItemConfig;
import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.ItemConstants;

public class DungeonResourceShop {

	public static final int RESOURCE_SHOP = 956, RESOURCE_SHOP_INV = 957;
	private static final int[] CS2MAPS =
	{ 2989, 2991, 2993, 2987 };

	public static void openResourceShop(final Player player, int complexity) {
		if (complexity <= 1) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", DungeonConstants.SMUGGLER, "Sorry, but I don't have anything to sell.");
			return;
		}
		player.getPackets().sendCSVarInteger(1320, complexity);
		player.getTemporaryAttributtes().put(Key.DUNG_COMPLEXITY, complexity);
		player.getPackets().sendUnlockIComponentOptionSlots(RESOURCE_SHOP, 24, 0, 429, 0, 1, 2, 3, 4);
		player.getPackets().sendUnlockIComponentOptionSlots(RESOURCE_SHOP_INV, 0, 0, 27, 0, 1, 2, 3, 4, 5);
		player.getPackets().sendInterSetItemsOptionsScript(RESOURCE_SHOP_INV, 0, 93, 4, 7, "Value", "Sell 1", "Sell 5", "Sell 10", "Sell 50", "Examine");
		player.getInterfaceManager().sendInterface(RESOURCE_SHOP);
		player.getInterfaceManager().sendInventoryInterface(RESOURCE_SHOP_INV);
		player.setCloseInterfacesEvent(new Runnable() {

			@Override
			public void run() {
				player.getTemporaryAttributtes().remove(Key.DUNG_COMPLEXITY);
			}
		});
	}

	public static void handlePurchaseOptions(Player player, int slotId, int quantity) {
		Integer complexity = (Integer) player.getTemporaryAttributtes().get(Key.DUNG_COMPLEXITY);
		if (complexity == null || complexity <= 1) //not error, just hacking
			return;
		int baseMap = CS2MAPS[complexity >= 5 ? 3 : complexity - 2];
		int slot = (slotId - 2) / 5;
		ClientScriptMap map = ClientScriptMap.getMap(baseMap);
		if (slot >= map.getSize()) {
			slot -= map.getSize();
			map = ClientScriptMap.getMap(baseMap + 1);
		}
		int item = map.getIntValue(slot);
		if (item == -1)
			return;
		ItemConfig def = ItemConfig.forID(item);
		int value = (int) (def.getValue() * def.getDungShopValueMultiplier());
		if (quantity == -1) {
			player.getPackets().sendGameMessage(def.getName() + ": currently costs " + value + " coins.");
			return;
		}
		int coinsCount = player.getInventory().getAmountOf(DungeonConstants.RUSTY_COINS);
		int price = value * quantity;
		if (price > coinsCount) {
			quantity = coinsCount / value;
			price = quantity * value;
			player.getPackets().sendGameMessage("You don't have enough money to buy that!");
		}
		int openSlots = player.getInventory().getFreeSlots();
		if (!def.isStackable())
			quantity = quantity > openSlots ? openSlots : quantity;
		if (quantity == 0)
			return;
		if (player.getInventory().addItem(item, quantity))
			player.getInventory().deleteItem(new Item(DungeonConstants.RUSTY_COINS, price));
	}

	public static void handleSellOptions(Player player, int slotId, int itemId, int quantity) {
		Item item = player.getInventory().getItem(slotId);
		if (item == null || itemId != item.getId())
			return;
		if (!ItemConstants.isTradeable(item) || item.getId() == DungeonConstants.RUSTY_COINS) {
			player.getPackets().sendGameMessage("You can't sell this item.");
			return;
		}
		ItemConfig def = item.getDefinitions();

		int value = (int) ((def.getValue() * def.getDungShopValueMultiplier()) * 0.3D);
		if (quantity == -1) {
			player.getPackets().sendGameMessage(def.getName() + ": shop will buy for " + value + ". Right-click the item to sell.");
			return;
		}
		int itemCount = player.getInventory().getAmountOf(item.getId());
		if (quantity > itemCount)
			quantity = itemCount;
		int price = value * quantity;
		player.getInventory().deleteItem(new Item(item.getId(), quantity));
		player.getInventory().addItem(DungeonConstants.RUSTY_COINS, price);
	}
}
