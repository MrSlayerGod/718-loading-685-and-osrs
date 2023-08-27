package com.rs.game.player;

import java.io.Serializable;
import java.util.HashMap;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.item.Item;
import com.rs.game.minigames.clanwars.FfaZone;
import com.rs.game.player.content.ItemConstants;
import com.rs.utils.Utils;

public class ChargesManager implements Serializable {

	private static final long serialVersionUID = -5978513415281726450L;

	private transient Player player;

	private HashMap<Integer, Integer> charges, spawnCharges;

	public ChargesManager() {
		charges = new HashMap<Integer, Integer>();
		spawnCharges =  new HashMap<Integer, Integer>();
	}

	public HashMap<Integer, Integer> getCharges() {
		return player.getControlerManager().getControler() instanceof FfaZone ? spawnCharges : charges;
	}

	public void setPlayer(Player player) {
		this.player = player;
		//temp
		if (spawnCharges == null)
			spawnCharges =  new HashMap<Integer, Integer>();
	}

	public void process() {
		Item[] items = player.getEquipment().getItems().getItems();
		for (int slot = 0; slot < items.length; slot++) {
			Item item = items[slot];
			if (item == null)
				continue;
			if (player.getAttackedByDelay() > Utils.currentTimeMillis()) {
				int newId = ItemConstants.getDegradeItemWhenCombating(item.getId());
				if (newId != -1) {
					player.getPackets().sendGameMessage("<col=FF0040>"+item.getDefinitions().getName() + " has degraded slightly!");
					item.setId(newId);
					player.getEquipment().refresh(slot);
					player.getAppearence().generateAppearenceData();
				}
			}
			int defaultCharges = ItemConstants.getItemDefaultCharges(item.getId());
			if (defaultCharges == -1)
				continue;
			if (ItemConstants.itemDegradesWhileWearing(item.getId()))
				degrade(item.getId(), defaultCharges, slot);
			else if (player.getAttackedByDelay() > Utils.currentTimeMillis() && ItemConstants.itemDegradesWhileCombating(item.getId()))
				degrade(item.getId(), defaultCharges, slot);
		}
	}

	public void die() {
		die(null, null);
	}

	public void die(Integer[] slots, Integer[] slots2) {
		Item[] equipItems = player.getEquipment().getItems().getItems();
		Item[] invItems = player.getInventory().getItems().getItems();

		if (slots == null) {
			for (int slot = 0; slot < equipItems.length; slot++) {
				if (equipItems[slot] != null && degradeCompletly(equipItems[slot]))
					player.getEquipment().getItems().set(slot, null);
			}
			for (int slot = 0; slot < invItems.length; slot++) {
				if (invItems[slot] != null && degradeCompletly(invItems[slot]))
					player.getInventory().getItems().set(slot, null);
			}
		} else {
			for (int slot : slots) {
				if (slot >= 16) {
					if (invItems[slot - 16] != null && degradeCompletly(invItems[slot - 16]))
						player.getInventory().getItems().set(slot - 16, null);
				} else {
					if (equipItems[slot - 1] != null && degradeCompletly(equipItems[slot - 1]))
						player.getEquipment().getItems().set(slot - 1, null);
				}
			}
			for (int slot : slots2) {
				if (slot >= 16) {
					if (invItems[slot - 16] != null && degradeCompletly(invItems[slot - 16]))
						player.getInventory().getItems().set(slot - 16, null);
				} else {
					if (equipItems[slot - 1] != null && degradeCompletly(equipItems[slot - 1]))
						player.getEquipment().getItems().set(slot - 1, null);
				}
			}
		}
	}

	public static final String REPLACE = "##";

	public void checkPercentage(String message, int id, boolean reverse) {
		int charges = getCharges(id);
		int maxCharges = ItemConstants.getItemDefaultCharges(id);
		int percentage = reverse ? (charges == 0 ? 0 : (100 - (charges * 100 / maxCharges))) : charges == 0 ? 100 : (charges * 100 / maxCharges);
		player.getPackets().sendGameMessage(message.replace(REPLACE, String.valueOf(percentage)));
	}

	public void checkCharges(String message, int id) {
		player.getPackets().sendGameMessage(message.replace(REPLACE, String.valueOf(getCharges(id))));
	}

	public int getCharges(int id, boolean max) {
		Integer c = getCharges().get(id);
		return c == null ? (max ? ItemConstants.getItemDefaultCharges(id) : 0) : c;
	}

	public int getCharges(int id) {
		return getCharges(id, false);
	}

	/*
	 * -1 inv
	 */
	public void addCharges(int id, int amount, int wearSlot) {
		int maxCharges = ItemConstants.getItemDefaultCharges(id);
		if (maxCharges == -1) {
			//System.out.println("This item cant get charges atm " + id);
			return;
		}
		Integer c = getCharges().get(id);
		int amt = c == null ? (id == 23029 ? amount : maxCharges) : (amount + c);
		if (amt > maxCharges)
			amt = maxCharges;
		if (amt <= 0) {
			int newId = ItemConstants.getItemDegrade(id);
			if (newId == -1) {
				if (wearSlot == -1)
					player.getInventory().deleteItem(id, 1);
				else {
					player.getEquipment().getItems().set(wearSlot, null);
					player.getEquipment().refresh(wearSlot);
					player.getAppearence().generateAppearenceData();
					player.getPackets().sendGameMessage(ItemConfig.forID(id).getName() + " turned into dust.");
				}
			} else if (wearSlot == -1) {
				player.getInventory().deleteItem(id, 1);
				player.getInventory().addItem(newId, 1);
			} else {
				Item item = player.getEquipment().getItem(wearSlot);
				if (item == null)
					return;
				item.setId(newId);
				player.getEquipment().refresh(wearSlot);
				player.getAppearence().generateAppearenceData();
			}
			resetCharges(id);
		} else
			getCharges().put(id, amt);
	}

	public void resetCharges(int id) {
		getCharges().remove(id);
	}

	public void maxCharges(int id) {
		getCharges().put(id, ItemConstants.getItemDefaultCharges(id));
	}

	public boolean degradesUponDrop(Item item) {
		int defaultCharges = ItemConstants.getItemDefaultCharges(item.getId());
		if (!ItemConstants.itemDegradesInDeath(item.getId()))
			return false;
		return defaultCharges != -1 || ItemConstants.getItemDegrade(item.getId()) != -1;
	}
	/*
	 * return disapear;
	 */
	public boolean degradeCompletly(Item item) {
		int defaultCharges = ItemConstants.getItemDefaultCharges(item.getId());
		if (!ItemConstants.itemDegradesInDeath(item.getId()))
			return false;
		if (defaultCharges != -1 || ItemConstants.getItemDegrade(item.getId()) != -1) {
			int tries = 0;
			while (tries++ < 10) {
				if (ItemConstants.itemDegradesWhileWearing(item.getId()) || ItemConstants.itemDegradesWhileCombating(item.getId())) {
					getCharges().remove(item.getId());
					int newId = ItemConstants.getItemDegrade(item.getId());
					if (newId == -1)
						return ItemConstants.getItemDefaultCharges(item.getId()) == -1 ? false : true;
					item.setId(newId);
				} else {
					int newId = ItemConstants.getItemDegrade(item.getId());
					if (newId != -1) {
						getCharges().remove(item.getId());
						item.setId(newId);
					}
					break;
				}
			}
			return false;
		}
		return false;
	}

	public void wear(int slot) {
		Item item = player.getEquipment().getItems().get(slot);
		if (item == null)
			return;
		int newId = ItemConstants.getDegradeItemWhenWear(item.getId());
		if (newId == -1)
			return;
		player.getEquipment().getItems().set(slot, new Item(newId, 1));
		player.getEquipment().refresh(slot);
		player.getAppearence().generateAppearenceData();
		player.getPackets().sendGameMessage("<col=FF0040>"+item.getDefinitions().getName() + " has degraded slightly!");
	}

	private void degrade(int itemId, int defaultCharges, int slot) {
		if (!(itemId >= 13845 && itemId <= 13990) && player.isVIPDonator())
			return;
		Integer c = getCharges().remove(itemId);
		if (c == null && itemId != 42926 && itemId != 42931)
			c = defaultCharges;
		else {
			if (c == null)
				c = 1;
			c--;
			if (c == 0) {
				int newId = ItemConstants.getItemDegrade(itemId);
				player.getEquipment().getItems().set(slot, newId != -1 ? new Item(newId, 1) : null);
				if (newId == -1)
					player.getPackets().sendGameMessage(ItemConfig.forID(itemId).getName() + " turned into dust.");
				else
					player.getPackets().sendGameMessage("<col=FF0040>"+ItemConfig.forID(itemId).getName() + " has degraded slightly!");
				player.getEquipment().refresh(slot);
				player.getAppearence().generateAppearenceData();
				return;
			}
		}
		getCharges().put(itemId, c);
	}

}
