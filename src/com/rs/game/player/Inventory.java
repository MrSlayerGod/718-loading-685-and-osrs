package com.rs.game.player;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.minigames.clanwars.FfaZone;
import com.rs.game.player.content.RunePouch;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.content.grandExchange.Offer;
import com.rs.utils.Colour;
import com.rs.utils.ItemExamines;
import com.rs.utils.ItemWeights;
import com.rs.utils.Utils;

public final class Inventory implements Serializable {

	private static final long serialVersionUID = 8842800123753277093L;

	private ItemsContainer<Item> items;
	private ItemsContainer<Item> spawnItems;

	private transient Player player;
	private transient double inventoryWeight;

	public static final int INVENTORY_INTERFACE = 679;

	public Inventory() {
		items = new ItemsContainer<Item>(28, false);
		spawnItems = new ItemsContainer<Item>(28, false);
	}

	public void setPlayer(Player player) {
		this.player = player;
		if (spawnItems == null) //TODO temporary
			spawnItems = new ItemsContainer<Item>(28, false);
	}

	public void init() {
		refresh();
	}

	public void unlockInventoryOptions() {
		player.getPackets().sendIComponentSettings(INVENTORY_INTERFACE, 0, 0, 27, 4554126);
		player.getPackets().sendIComponentSettings(INVENTORY_INTERFACE, 0, 28, 55, 2097152);
	}

	public void reset() {
		getItems().reset();
		init(); // as all slots reseted better just send all again
	}

	public void refresh(int... slots) {
		player.getPackets().sendUpdateItems(93, getArray(), slots);
		refreshConfigs(false);
	}
	
	public void refreshConfigs(boolean init) {
		double w = 0;
		for(Item item : getItems().getItems()) {
			if(item == null)
				continue;
			w += ItemWeights.getWeight(item, false);
		}
		inventoryWeight = w;
	}

	public boolean addItemDrop(int itemId, int amount, WorldTile tile) {
		if (itemId < 0 || amount < 0 || !Utils.itemExists(itemId) || !player.getControlerManager().canAddInventoryItem(itemId, amount))
			return false;
		if (itemId == 995)
			return player.getMoneyPouch().sendDynamicInteraction(amount, false);
		Item[] itemsBefore = getArray().clone();//getItems().getItemsCopy();
		if (!getItems().add(new Item(itemId, amount)))
			World.addGroundItem(new Item(itemId, amount), tile, player, true, 180);
		else
			refreshItems(itemsBefore);
		return true;
	}

	public boolean addItemDrop(int itemId, int amount) {
		return addItemDrop(itemId, amount, new WorldTile(player));
	}

	/*
	 * Does not handle remaining items if all items do not fit in inv
	 * use add(Item)
	 */
	public boolean addItem(int itemId, int amount) {
		return add(new Item(itemId, amount)) == null;
	}

	/*
	 * Does not handle remaining items if all items do not fit in inv
	 * use add(Item)
	 */
	@Deprecated
	public boolean addItem(Item item) {
		return add(item) == null;
	}

	public Item addItemMoneyPouch(Item item) {
		if (item.getId() == 995 || item.getId() == 43204) {
			/*int inv = player.getInventory().getAmountOf(995);
			if(player.getMoneyPouch().getCoinsAmount() == Integer.MAX_VALUE && !player.getInventory().hasFreeSlots()
				|| inv == Integer.MAX_VALUE && player.getMoneyPouch().getCoinsAmount() == Integer.MAX_VALUE) {
				player.sendMessage("Not enough space in your inventory or money pouch.");
				return item;
			}*/

			//long overflow = item.getAmount() + player.getMoneyPouch().getCoinsAmount();
			player.getMoneyPouch().sendDynamicInteraction(item.getAmount() * (item.getId() == 43204 ? 1000L : 1L), false);

			/*if(overflow > Integer.MAX_VALUE) {
				// send overflow to inventory if coin pouch full
				return add(new Item(995, (int) (overflow - Integer.MAX_VALUE)));
			}*/

			return null;
		} else {
			return add(item);
		}
	}

	public boolean removeItemMoneyPouch(Item item) {
		if (item.getId() == 995)
			return player.getMoneyPouch().sendDynamicInteraction(item.getAmount(), true);
		return removeItems(item);
	}
	
	public boolean containsItemToolBelt(int id) {
		return containsOneItem(id) || player.getToolbelt().containsItem(id);
	}

	public boolean containsItemToolBelt(int id, int amount) {
		return containsItem(id, amount) || player.getToolbelt().containsItem(id);
	}

	/**
	 * Attempts to add the item to inventory, if fail return remainder of item not moved
	 * or return null if the item was moved successfully.
	 *
	 * *** If return is not null items should be dealt with or items will go missing ***
	 */
	public Item add(Item item) {
		if (item.getId() < 0 || item.getAmount() < 0 || !Utils.itemExists(item.getId()) || !player.getControlerManager().canAddInventoryItem(item.getId(), item.getAmount()))
			return item;

		// remaining items that did not get added
		Item remainder = null;

		// duplicate array to check for changes
		Item[] itemsBefore = getArray().clone();

		int freeSlots = getItems().getFreeSlots();
		boolean stackable = item.getDefinitions().isStackable();

		if (freeSlots < requiredSlots(item)) {
			if(stackable) {
				// stackable item + inventory is full & not held
				player.sendMessage("Your inventory is full.");
				return item;
			}

			// remaining items not moved
			int newAmt = item.getAmount() - freeSlots;

			// unstackable item - take as many as the player can hold
			getItems().add(new Item(item.getId(), freeSlots));

			// indicate to the player not all items were moved
			player.getPackets().sendGameMessage("Not enough space in your inventory.");
			remainder = newAmt == 0 ? null : new Item(item.getId(), newAmt);
		} else {
			// has enough inventory space
			if(stackable)  {
				// check overflow of stackables
				long overflow = item.getAmount() + getAmountOf(item.getAmount());
				if (overflow > Integer.MAX_VALUE) {
					// has overflow
					remainder = new Item(item.getId(), (int) (overflow - Integer.MAX_VALUE));
					getItems().add(new Item(item.getId(), item.getAmount() - remainder.getAmount()));
				} else {
					// has space
					getItems().add(new Item(item.getId(), item.getAmount()));
				}
			} else {
				// non stackable
				getItems().add(new Item(item.getId(), item.getAmount()));
			}
		}

		// refresh any changes
		refreshItems(itemsBefore);
		return remainder;
	}

	public void deleteItem(int slot, Item item) {
		if (!player.getControlerManager().canDeleteInventoryItem(item.getId(), item.getAmount()))
			return;
		Item[] itemsBefore = getArray().clone();//getItems().getItemsCopy();
		getItems().remove(slot, item);
		refreshItems(itemsBefore);
	}

	public boolean removeItems(Item... list) {
		for (Item item : list) {
			if (item == null)
				continue;
			deleteItem(item);
		}
		return true;
	}

	public boolean removeItems(List<Item> list) {
		for (Item item : list) {
			if (item == null)
				continue;
			deleteItem(item);
		}
		return true;
	}

	public void deleteItem(int itemId, int amount) {
		if (!player.getControlerManager().canDeleteInventoryItem(itemId, amount))
			return;
		Item[] itemsBefore = getArray().clone();//getItems().getItemsCopy();
		getItems().remove(new Item(itemId, amount));
		refreshItems(itemsBefore);
	}

	public void deleteItem(Item item) {
		if (!player.getControlerManager().canDeleteInventoryItem(item.getId(), item.getAmount()))
			return;
		// only remove from specific slot if item hasn't moved
		if(item.getFromSlot() == -1 || item.getId() != getItems().get(item.getFromSlot()).getId()) {
			Item[] itemsBefore = getArray().clone();//getItems().getItemsCopy();
			getItems().remove(item);
			refreshItems(itemsBefore);
		} else {
			deleteItem(item.getFromSlot(), item);
		}
	}

	/*
	 * No refresh needed its client to who does it :p
	 */
	public void switchItem(int fromSlot, int toSlot) {
		Item[] itemsBefore = getArray().clone();//getItems().getItemsCopy();
		Item fromItem = getItems().get(fromSlot);
		Item toItem = getItems().get(toSlot);
		getItems().set(fromSlot, toItem);
		getItems().set(toSlot, fromItem);
		refreshItems(itemsBefore);
	}

	public void refreshItems(Item[] itemsBefore) {
		Item[] array = getArray();
		int[] changedSlots = new int[itemsBefore.length];
		int count = 0;
		for (int index = 0; index < itemsBefore.length; index++) {
			if (itemsBefore[index] != array[index])
				changedSlots[count++] = index;
		}
		int[] finalChangedSlots = new int[count];
		System.arraycopy(changedSlots, 0, finalChangedSlots, 0, count);
		refresh(finalChangedSlots);
	}

	public ItemsContainer<Item> getItems() {
		return player.getControlerManager().getControler() instanceof FfaZone ? spawnItems : items;
	}

	public boolean hasFreeSlots() {
		return getItems().getFreeSlot() != -1;
	}

	public boolean hasFreeSlots(Item item) {
		int slots = getFreeSlots();
		int requiredSlots = requiredSlots(item);
		boolean stackable = item.getDefinitions().isStackable();
		int held = getAmountOf(item.getId());
		if(item.getId() == 995) {
			long avail = (long) Integer.MAX_VALUE * 2 - ((long) held + player.getMoneyPouch().getCoinsAmount());
			return (avail > item.getAmount());
		}

		if(stackable && (long) item.getAmount() + held > Integer.MAX_VALUE) {
			return false;
		}

		return slots >= requiredSlots;
	}

	public int requiredSlots(Item item) {
		int held = getAmountOf(item.getId());
		return !item.getDefinitions().isStackable() ? item.getAmount() :
				(item.getDefinitions().isStackable() && held > 0 ? 0 : 1);
	}
	public int getFreeSlots() {
		return getItems().getFreeSlots();
	}

	public int getAmountOf(int itemId) {
		return getItems().getNumberOf(itemId);
	}

	public Item getItem(int slot) {
		return getItems().get(slot);
	}

	public int getItemsContainerSize() {
		return getItems().getSize();
	}

	public boolean containsItems(List<Item> list) {
		for (Item item : list)
			if (!getItems().contains(item))
				return false;
		return true;
	}

	public boolean containsItems(Item... item) {
		for (int i = 0; i < item.length; i++)
			if (!getItems().contains(item[i]))
				return false;
		return true;
	}

	public boolean containsItems(int[] itemIds, int[] ammounts) {
		int size = itemIds.length > ammounts.length ? ammounts.length : itemIds.length;
		for (int i = 0; i < size; i++)
			if (!getItems().contains(new Item(itemIds[i], ammounts[i])))
				return false;
		return true;
	}

	public boolean containsItem(Item item) {
		return getItems().contains(new Item(item.getId(), item.getAmount()));
	}

	public boolean containsItem(int itemId, int ammount) {
		return getItems().contains(new Item(itemId, ammount));
	}

	public Item findItem(int id) {
		for(Item item : getItems().getItems()) {
			if(item != null && item.getId() == id)
				return item;
		}

		return null;
	}

	public long getCoinsAmount() {
		long coins = getItems().getNumberOf(995) + player.getMoneyPouch().getCoinsAmount();
		return coins < 0 ? Integer.MAX_VALUE : coins;
	}

	public boolean containsOneItem(int... itemIds) {
		for (int itemId : itemIds) {
			if (getItems().containsOne(new Item(itemId, 1)))
				return true;
		}
		return false;
	}

	public void sendExamine(int slotId) {
		if (slotId >= getItemsContainerSize())
			return;
		Item item = getItems().get(slotId);
		if (item == null)
			return;
		String price = "";
		String idstr =  !player.isAdmin() ? "" : Colour.WHEAT.wrap(" Id: " + item.getId()) + " - ";
		if (item.getDefinitions().tradeable) {
			int itemId = item.getId();
			if (item.getDefinitions().isNoted())
				itemId = item.getDefinitions().getCertId();
			Offer bestOffer = GrandExchange.getBestOffer(player, itemId, true);
			price += "<br>GE guide price: "+Utils.getFormattedNumber(GrandExchange.getPrice(item.getId()))+" gp each. Best Offer: "+(bestOffer == null ? "None." : Utils.getFormattedNumber(bestOffer.getPrice())+" gp each.");
		}
		player.getPackets().sendInventoryMessage(0, slotId, idstr + ItemExamines.getExamine(item)+price);
	}
	
	private Item[] getArray() {
		Item[] runePouch = !containsOneItem(RunePouch.ID) ? new Item[3] : player.getRunePouch().toArray();
		Item[] array = Arrays.copyOf(getItems().toArray(), getItems().getSize() + runePouch.length);
		System.arraycopy(runePouch, 0, array, getItems().getSize(), runePouch.length);
		return array;
	}

	public void refresh() {
		for (int i = 0; i < getItems().getItems().length; i++) {
			if(getItems().getItems()[i] != null && getItems().getItems()[i].getId() == -1) {
				getItems().set(i, null);
			}
		}
		player.getPackets().sendItems(93, /*items*/ getArray());
		refreshConfigs(true);
	}
	
	public double getInventoryWeight() {
		return inventoryWeight;
	}

	public void replaceItem(int id, int amount, int slot) {
		Item item = getItems().get(slot);
		if (item == null)
			return;
		item.setId(id);
		item.setAmount(amount);
		refresh(slot);
	}
}
