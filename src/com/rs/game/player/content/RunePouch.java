/**
 * 
 */
package com.rs.game.player.content;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;

/**
 * Written by andreas. Integrated in source by dragonkk.
 * @author dragonkk(Alex)
 * Jun 14, 2018
 */
public class RunePouch {

	public static final int ID = 42791;
	
	public static void refreshRunePouch(Player player) {
		player.getPackets().sendItems(100, player.getRunePouch().getItems());
		player.getInventory().refresh(28, 29, 30);
	}
	
	public static void openRunePouch(Player player) {
		player.getInterfaceManager().sendInterface(1284);
		player.getInterfaceManager().sendInventoryInterface(670);
		player.getPackets().sendInterSetItemsOptionsScript(670, 0, 93, 4, 7, "Store 1", "Store 10", "Store 100",
				"Store-All");
		player.getPackets().sendUnlockIComponentOptionSlots(670, 0, 0, 27, 0, 1, 2, 3);
		player.getPackets().sendIComponentText(1284, 28, "Rune Pouch");
		player.getPackets().sendHideIComponent(1284, 8, true);
		player.getPackets().sendHideIComponent(1284, 9, true);
		player.getPackets().sendIComponentText(1284, 46, "Take-All");
		player.getPackets().sendInterSetItemsOptionsScript(1284, 7, 100, 8, 4, "Withdraw 1", "Withdraw 10", "Withdraw 100",
				"Withdraw-All");
		player.getPackets().sendUnlockIComponentOptionSlots(1284, 7, 0, 3, 0, 1, 2, 3);
		player.getPackets().sendInterSetItemsOptionsScript(1284, 10, 99, 8, 3, "Take all");
		refreshRunePouch(player);
		player.getTemporaryAttributtes().put("runepouch", Boolean.TRUE);
		player.setCloseInterfacesEvent(new Runnable() {
			@Override
			public void run() {
				player.getTemporaryAttributtes().remove("runepouch");
			}
		});
	}

	public static void storeRunePouch(Player player, Item item, int amount) {
		Item newItem = item;
		if (newItem.getAmount() < amount) {
			amount = newItem.getAmount();
		}
		if (!Magic.isRune(newItem.getId())) {
			player.getPackets()
					.sendGameMessage("You can't store " + newItem.getName() + " in the rune pouch.");
			return;
		}
		if (player.getRunePouch().getNumberOf(newItem) == 16000) {
			player.getPackets().sendGameMessage("You can't have more than 16,000 of each rune in the rune pouch.");
			return;
		}
		if (player.getRunePouch().getFreeSlots() == 0 && !player.getRunePouch().contains(newItem)) {
			player.getPackets().sendGameMessage("You can't store more than 3 type of runes in the rune pouch.");
			return;
		}
		if (amount + player.getRunePouch().getNumberOf(newItem) > 16000)
			amount = 16000 - player.getRunePouch().getNumberOf(newItem);
		player.getInventory().deleteItem(newItem.getId(), amount);
		player.getRunePouch().add(new Item(newItem.getId(), amount));
		player.getRunePouch().shift();
		player.getPackets().sendGameMessage("You store " + amount + " x " + item.getName() + "s in the rune pouch.");
		refreshRunePouch(player);
	}
	
	public static void withdrawRunePouch(Player player, int slotId, Item item, int amount) {
		if (player.getInventory().getFreeSlots() == 0
				&& !player.getInventory().containsItem(player.getRunePouch().get(slotId).getId(), 1)) {
			player.getPackets().sendGameMessage("You don't have enough inventory space.");
			return;
		}
		if (amount > player.getRunePouch().get(slotId).getAmount()) {
			amount = player.getRunePouch().get(slotId).getAmount();
		}
		player.getRunePouch().get(slotId).setAmount(player.getRunePouch().get(slotId).getAmount() - amount);
		player.getInventory().addItem(item.getId(), amount);
		if (player.getRunePouch().get(slotId).getAmount() == 0) {
			player.getRunePouch().remove(item);
			player.getRunePouch().shift();
		}
		refreshRunePouch(player);
		player.getPackets().sendGameMessage("You withdraw " + amount + " x " + item.getName() + "s from the rune pouch.");
		
	}
	
	public static void fillRunePouch(Player player, Item itemUsed) {
		if (!Magic.isRune(itemUsed.getId())) {
			player.getPackets()
					.sendGameMessage("You can't store " + itemUsed.getName() + " in the rune pouch.");
			return;
		}
		if (player.getRunePouch().getNumberOf(itemUsed) == 16000) {
			player.getPackets().sendGameMessage("You can't have more than 16,000 of each rune in the rune pouch.");
			return;
		}
		if (player.getRunePouch().getFreeSlots() == 0 && !player.getRunePouch().contains(itemUsed)) {
			player.getPackets().sendGameMessage("You can't store more than 3 type of runes in the rune pouch.");
			return;
		}
		int amount = itemUsed.getAmount();
		if (player.getRunePouch().getNumberOf(itemUsed) + itemUsed.getAmount() > 16000)
			amount = 16000 - player.getRunePouch().getNumberOf(itemUsed);
		player.getRunePouch().add(new Item(itemUsed.getId(), amount));
		player.getInventory().deleteItem(itemUsed.getId(), amount);
		player.getPackets().sendGameMessage("You stored " + amount + " x "+ itemUsed.getAmount() + " in the rune pouch.");

		refreshRunePouch(player);
	}
	
	public static void empty(Player player) {
		if (player.getRunePouch().getFreeSlots() < 3) {
			for (Item runes : player.getRunePouch().getItems()) {
				if (runes == null)
					continue;
				if (!player.getInventory().hasFreeSlots()
						&& !player.getInventory().containsOneItem(runes.getId())) {
					player.getPackets().sendGameMessage("You don't have enough inventory spaces.");
					return;
				}
				player.getRunePouch().remove(runes);
				player.getRunePouch().shift();
				player.getInventory().addItem(runes);
				refreshRunePouch(player);
			}
		} else 
			player.getPackets().sendGameMessage("Your rune pouch is empty.");
	}
	
	public static void dropItems(Player player, Player killer, WorldTile deathTile) {
		for (Item runes : player.getRunePouch().getItems()) {
			if (runes == null)
				continue;
			World.addGroundItem(runes, deathTile, killer, true, 300, killer == player ? 2 : 0);
		}
		player.getRunePouch().clear();
		player.getInventory().deleteItem(ID, Integer.MAX_VALUE);
		refreshRunePouch(player);
	//	player.getPackets().sendGameMessage("Your rune pouch was lost at death.");
	}
}
