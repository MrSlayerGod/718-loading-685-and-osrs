package com.rs.game.player;

import java.io.Serializable;

import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.controllers.Wilderness;

public class LootingBag implements Serializable {

	private static final long serialVersionUID = -4319751839899222243L;
	
	private transient Player player;
	private int depositQuantity;
	private ItemsContainer<Item> items;

	public LootingBag() {
		items = new ItemsContainer<Item>(28, false);
		depositQuantity = 1;
	}
	
	public boolean isFull() {
		return items.freeSlot() != -1;
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}

	public void check() {
		if (player.getInterfaceManager().containsScreenInter() || player.isLocked()) {
			player.getPackets().sendGameMessage("Please finish what you're doing before opening the looting bag.");
			return;
		}
		player.stopAll();
		player.getInterfaceManager().sendInterface(206);
		player.getInterfaceManager().sendInventoryInterface(207);
		player.getPackets().sendIComponentText(206, 14, "Looting Bag");
		sendInterItems();
		sendOptions();
		player.getTemporaryAttributtes().put(Key.LOOTING_BAG, true);
		player.setCloseInterfacesEvent(new Runnable() {
			@Override
			public void run() {
				player.getTemporaryAttributtes().remove(Key.LOOTING_BAG);
			}
		});
	}

	public int getSlotId(int clickSlotId) {
		return clickSlotId / 2;
	}

	public void addItem(int slot) {
		Item item = player.getInventory().getItem(slot);
		if (item == null)
			return;
		addItem(slot, item.getDefinitions().isStackable() ? Integer.MAX_VALUE : depositQuantity);
	}
	
	
	public void addItemNew(Item item) {
		int amount = item.getAmount();
		if (item.getId() == 41941) {
			player.getPackets().sendGameMessage("You may be surprised to learn that bagception is not permitted.");
			return;
		}
		if (!ItemConstants.isTradeable(item)) {
			player.getPackets().sendGameMessage("That item isn't tradeable.");
			return;
		}
		if (!(player.getControlerManager().getControler() instanceof Wilderness)) {
			player.getPackets().sendGameMessage("You can't put items in the bag unless you're in the Wilderness.");
			return;
		}
		Item stackableItem = item.getDefinitions().isStackable() ? items.lookup(item.getId()) : null;
		if (stackableItem != null) {
			if (stackableItem.getAmount() + amount <= 0) {
				//item = item.clone();
				amount = Integer.MAX_VALUE - stackableItem.getAmount();
				//item.setAmount(Integer.MAX_VALUE - stackableItem.getAmount());
				player.getPackets().sendGameMessage("Not enough space in your looting bag.");
			} else if (stackableItem.getAmount() + item.getAmount() >= Integer.MAX_VALUE) {
				player.getPackets().sendGameMessage("Could not store your " + item.getName());
				return;
			}
		} else if (items.getFreeSlots() == 0) {
			player.getPackets().sendGameMessage("Not enough space in your looting bag.");
			return;
		} else if (!item.getDefinitions().isStackable() && amount > items.getFreeSlots()) {
			amount = items.getFreeSlots();
			player.getPackets().sendGameMessage("Not enough space in your looting bag.");
		}
		if (amount == 0)
			return;
		Item[] itemsBefore = items.getItemsCopy();
		int maxAmount = player.getInventory().getItems().getNumberOf(item);
		if (amount < maxAmount)
			item = new Item(item.getId(), amount);
		else
			item = new Item(item.getId(), maxAmount);
		items.add(item);
		refreshItems(itemsBefore);
	}
	
	public void addItem(int slot, int amount) {
		Item item = player.getInventory().getItem(slot);
		if (item == null)
			return;
		amount = Math.min(player.getInventory().getAmountOf(item.getId()), amount);
		if (item.getId() == 41941) {
			player.getPackets().sendGameMessage("You may be surprised to learn that bagception is not permitted.");
			return;
		}
		if (!ItemConstants.isTradeable(item)) {
			player.getPackets().sendGameMessage("That item isn't tradeable.");
			return;
		}
		if (!(player.getControlerManager().getControler() instanceof Wilderness)) {
			player.getPackets().sendGameMessage("You can't put items in the bag unless you're in the Wilderness.");
			return;
		}
		Item stackableItem = item.getDefinitions().isStackable() ? items.lookup(item.getId()) : null;
		if (stackableItem != null) {
			if (stackableItem.getAmount() + amount <= 0) {
				//item = item.clone();
				amount = Integer.MAX_VALUE - stackableItem.getAmount();
				//item.setAmount(Integer.MAX_VALUE - stackableItem.getAmount());
				player.getPackets().sendGameMessage("Not enough space in your looting bag.");
			} else if (stackableItem.getAmount() + item.getAmount() >= Integer.MAX_VALUE) {
				player.getPackets().sendGameMessage("Could not store your " + item.getName());
				return;
			}
		} else if (items.getFreeSlots() == 0) {
			player.getPackets().sendGameMessage("Not enough space in your looting bag.");
			return;
		} else if (!item.getDefinitions().isStackable() && amount > items.getFreeSlots()) {
			amount = items.getFreeSlots();
			player.getPackets().sendGameMessage("Not enough space in your looting bag.");
		}
		if (amount == 0)
			return;
		Item[] itemsBefore = items.getItemsCopy();
		int maxAmount = player.getInventory().getItems().getNumberOf(item);
		if (amount < maxAmount)
			item = new Item(item.getId(), amount);
		else
			item = new Item(item.getId(), maxAmount);
		items.add(item);
		player.getInventory().deleteItem(slot, item);
		refreshItems(itemsBefore);
	}

	public void refreshItems(Item[] itemsBefore) {
		if (!player.getInterfaceManager().containsInterface(206))
			return;
		int totalPrice = 0;
		int[] changedSlots = new int[itemsBefore.length];
		int count = 0;
		for (int index = 0; index < itemsBefore.length; index++) {
			Item item = items.getItems()[index];
			if (item != null)
				totalPrice += GrandExchange.getPrice(item.getId()) * item.getAmount();
			if (itemsBefore[index] != item) {
				changedSlots[count++] = index;
				player.getPackets().sendCSVarInteger(700 + index, item == null ? 0 : GrandExchange.getPrice(item.getId()));
			}

		}
		int[] finalChangedSlots = new int[count];
		System.arraycopy(changedSlots, 0, finalChangedSlots, 0, count);
		refresh(finalChangedSlots);
		player.getPackets().sendCSVarInteger(728, totalPrice);
		hideMessage();
	}
	

	public void sendInterItems() {
		int totalPrice = 0;
		player.getPackets().sendItems(90, items);
		for (int index = 0; index < items.getItems().length; index++) {
			Item item = items.getItems()[index];
			if (item != null)
				totalPrice += GrandExchange.getPrice(item.getId()) * item.getAmount();
			player.getPackets().sendCSVarInteger(700 + index, item == null ? 0 : GrandExchange.getPrice(item.getId()));
		}
		player.getPackets().sendCSVarInteger(728, totalPrice);
		hideMessage();
	}
	

	public void refresh(int... slots) {
		player.getPackets().sendUpdateItems(90, items, slots);
	}

	public void sendOptions() {
		player.getPackets().sendUnlockIComponentOptionSlots(206, 15, 0, 54);
		player.getPackets().sendUnlockIComponentOptionSlots(207, 0, 0, 27, 0, 1, 2, 3, 4, 5);
		player.getPackets().sendInterSetItemsOptionsScript(207, 0, 93, 4, 7, "Store", "Store-5", "Store-10", "Store-All", "Store-X", "Examine");
	}
	
	public void hideMessage() {
		player.getPackets().sendHideIComponent(206, 15, items.getUsedSlots() == 0);
	}
	
	public ItemsContainer<Item> getItems() {
		return items;
	}

	public boolean depositItems() {
		if (items.getUsedSlots() == 0)
			return false;
		for (Item item : items.getItems())
			if (item != null) {
				player.getBank().addItem(item.getDefinitions().isNoted() ? item.getDefinitions().getCertId() : item.getId(), item.getAmount(), true);
			}
		items.clear();
		return true;
	}
	
	public void destroy() {
		items.clear();
	}
	
	public void dropItems(Player killer, WorldTile deathTile) {
		for (Item item : items.getItems())
			if (item != null) 
				World.addGroundItem(item, deathTile, killer, true, 300, killer == player ? 2 : 0);
		destroy();
	}
	
	public void setDepositQuantity(int quantity) {
		depositQuantity = quantity;
	}
}
