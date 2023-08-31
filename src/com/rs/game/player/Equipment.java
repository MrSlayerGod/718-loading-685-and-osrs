package com.rs.game.player;

import java.io.Serializable;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.minigames.clanwars.FfaZone;
import com.rs.game.player.actions.firemaking.Bonfire;
import com.rs.game.player.content.Costumes;
import com.rs.game.player.content.ItemConstants;
import com.rs.utils.ItemExamines;
import com.rs.utils.ItemWeights;
import com.rs.utils.OsrsEquipment;

public final class Equipment implements Serializable {

	private static final long serialVersionUID = -4147163237095647617L;

	public static final byte SLOT_HAT = 0, SLOT_CAPE = 1, SLOT_AMULET = 2, SLOT_WEAPON = 3, SLOT_CHEST = 4, SLOT_SHIELD = 5, SLOT_LEGS = 7, SLOT_HANDS = 9, SLOT_FEET = 10, SLOT_RING = 12,
			SLOT_ARROWS = 13, SLOT_AURA = 14;

	private ItemsContainer<Item> items;
	private ItemsContainer<Item> spawnItems;
	private ItemsContainer<Item> keepsakeItems;

	private transient Player player;
	private transient int equipmentHpIncrease;
	private transient double equipmentWeight;
	private transient ItemsContainer<Item> keepsakeItemsFiltered;
	
	private Costumes costume;
	private int costumeColor;

	static final int[] DISABLED_SLOTS = new int[]
	{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0 };

	public Equipment() {
		items = new ItemsContainer<Item>(15, false);
		keepsakeItems = new ItemsContainer<Item>(15, false);
	}

	public void setPlayer(Player player) {
		this.player = player;
		if (spawnItems == null) //TODO temporary
			spawnItems = new ItemsContainer<Item>(15, false);
	}

	public void init() {
		player.getPackets().sendItems(94, getItems());
		refresh(null);
	}

	public void checkItems() {
		for (int i = 0; i < getItems().getSize(); i++) {
			Item item = getItems().get(i);
			if (item == null)
				continue;
			if (!ItemConstants.canWear(item, player)) {
				getItems().set(i, null);
				player.getInventory().addItemDrop(item.getId(), item.getAmount());
			}
		}
	}

	public void refresh(int... slots) {
		if (slots != null) {
			player.getPackets().sendUpdateItems(94, getItems(), slots);
			player.getCombatDefinitions().checkAttackStyle();
		}
		player.getCombatDefinitions().refreshBonuses();
		refreshConfigs(slots == null);
	}

	public void reset() {
		getItems().reset();
		init();
	}

	public Item getItem(int slot) {
		return getItems().get(slot);
	}

	public void sendExamine(int slotId) {
		Item item = getItems().get(slotId);
		if (item == null)
			return;
		player.getPackets().sendGameMessage(ItemExamines.getExamine(item));
	}

	public void refreshConfigs(boolean init) {
		double hpIncrease = 0;
		for (int index = 0; index < getItems().getSize(); index++) {
			Item item = getItems().get(index);
			if (item == null)
				continue;
			hpIncrease += item.getDefinitions().getHP();
/*			int id = item.getId();
			if (index == Equipment.SLOT_HAT) {
				if (id == 20135 || id == 20137// torva
				|| id == 20147 || id == 20149// pernix
				|| id == 20159 || id == 20161// virtus
				)
					hpIncrease += 66;
				else if (id == 52326)
					hpIncrease += 33;

			} else if (index == Equipment.SLOT_CHEST) {
				if (id == 20139 || id == 20141// torva
				|| id == 20151 || id == 20153// pernix
				|| id == 20163 || id == 20165// virtus
				)
					hpIncrease += 200;
				else if (id == 52327)
					hpIncrease += 100;
			} else if (index == Equipment.SLOT_LEGS) {
				if (id == 20143 || id == 20145// torva
				|| id == 20155 || id == 20157// pernix
				|| id == 20167 || id == 20169// virtus
				)
					hpIncrease += 134;
				else if (id == 52328)
					hpIncrease += 67;
			} else if (index == Equipment.SLOT_HANDS) {
				if (id == 24450)
					hpIncrease += 40;
				else if (id == 24451)
					hpIncrease += 75;
				else if (id == 24452)
					hpIncrease += 120;
				else if (id == 24453)
					hpIncrease += 140;
				else if (id == 24454)
					hpIncrease += 155;
				else if (id == 24977// torva
				|| id == 24980// virtus
				|| id == 24974// pernix
				)
					hpIncrease += 24;
			} else if (index == Equipment.SLOT_FEET) {
				if (id == 24983 || id == 24984// torva
				|| id == 24989 || id == 24990// pernix
				|| id == 24986 || id == 24987 // virtus
				)
					hpIncrease += 33;
			}*/
		}
		if (player.getLastBonfire() > 0) {
			int maxhp = player.getSkills().getLevel(Skills.HITPOINTS) * 10;
			hpIncrease += (maxhp * Bonfire.getBonfireBoostMultiplier(player)) - maxhp;
		}
		if (player.getHpBoostMultiplier() != 0) {
			int maxhp = player.getSkills().getLevel(Skills.HITPOINTS) * 10;
			hpIncrease += maxhp * player.getHpBoostMultiplier();
		}
		if (hpIncrease != equipmentHpIncrease) {
			equipmentHpIncrease = (int) hpIncrease;
			/*int maxHP = player.getMaxHitpoints();
			if(player.getHitpoints() > maxHP) {
				player.setHitpoints(maxHP);
				player.refreshHitPoints();
			}*/
		}
		double w = 0;
		for (Item item : getItems().getItems()) {
			if (item == null)
				continue;
			w += ItemWeights.getWeight(item, true);
		}
		equipmentWeight = w;
	}

	public boolean containsOneItem(int... itemIds) {
		for (int itemId : itemIds) {
			if (getItems().containsOne(new Item(itemId, 1)))
				return true;
		}
		return false;
	}

	public static boolean hideArms(int id) {
		/*
		 * String name = item.getName().toLowerCase(); if //temp old graphics
		 * fix, but bugs alil new ones (name.contains("d'hide body") ||
		 * name.contains("dragonhide body") ||
		 * name.equals("stripy pirate shirt") || (name.contains("chainbody") &&
		 * (name.contains("iron") || name.contains("bronze") ||
		 * name.contains("steel") || name.contains("black") ||
		 * name.contains("mithril") || name.contains("adamant") ||
		 * name.contains("rune") || name.contains("white"))) ||
		 * name.equals("leather body") || name.equals("hardleather body") ||
		 * name.contains("studded body")) return false;
		 */
		return ItemConfig.forID(id).getEquipType() == 6;
	}

	public static boolean hideHair(int id) {
		return ItemConfig.forID(id).getEquipType() == 8;
	}


	public static boolean showBear(int id) {
		if (id == 25582)
			return true;
		if (id >= Settings.OSRS_ITEM_OFFSET || id == 25578)  
			 return !hideHair(id) || !OsrsEquipment.isFullMask(ItemConfig.forID(id));
		String name = ItemConfig.forID(id).getName().toLowerCase();
		if (id >= 20159 && id <= 20162 || id == 25534)
			return false;
		return !(id >= 20159 && id <= 20162) && id != 25499 && ( !hideHair(id) || name.contains("coif") || name.contains("horns") || name.contains("hat") || name.contains("afro") || name.contains("cowl") || name.contains("tattoo") || name.contains("headdress") || name.contains("hood") || (name.contains("mask") && !name.contains("h'ween")) || (name.contains("helm") && !name.contains("full")));
	}

	public static int getItemSlot(int itemId) {
		return ItemConfig.forID(itemId).getEquipSlot();
	}

	public static boolean isTwoHandedWeapon(Item item) {
		return item.getDefinitions().getEquipType() == 5;
	}

	public int getWeaponStance() {
		Item weapon = getItems().get(3);
		if (weapon == null)
			return 1426;
		return weapon.getDefinitions().getRenderAnimId();
	}

	public boolean hasShield() {
		return getItems().get(5) != null;
	}

	public int getWeaponId() {
		Item item = getItems().get(SLOT_WEAPON);
		if (item == null)
			return -1;
		return item.getId();
	}

	public int getChestId() {
		Item item = getItems().get(SLOT_CHEST);
		if (item == null)
			return -1;
		return item.getId();
	}

	public int getHatId() {
		Item item = getItems().get(SLOT_HAT);
		if (item == null)
			return -1;
		return item.getId();
	}

	public int getShieldId() {
		Item item = getItems().get(SLOT_SHIELD);
		if (item == null)
			return -1;
		return item.getId();
	}

	public int getLegsId() {
		Item item = getItems().get(SLOT_LEGS);
		if (item == null)
			return -1;
		return item.getId();
	}

	public void removeAmmo(int ammoId, int amount) {
		removeAmmo(ammoId, amount, false);
	}
	public void removeAmmo(int ammoId, int amount, boolean removeAll) {
		if ((getWeaponId() == 25502) && player.getInfernalBlowpipeDarts() != null) {
			int newAmt = player.getInfernalBlowpipeDarts().getAmount() + amount; //amount -1
			if (newAmt <= 0)
				player.setInfernalBlowpipeDarts(null);
			else
				player.getInfernalBlowpipeDarts().setAmount(newAmt);
			return;
		}
		if ((getWeaponId() == 42926) && player.getBlowpipeDarts() != null) {
			int newAmt = player.getBlowpipeDarts().getAmount() + amount; //amount -1
			if (newAmt <= 0)
				player.setBlowpipeDarts(null);
			else
				player.getBlowpipeDarts().setAmount(newAmt);
			return;
		}
		if (amount == -1) {
			getItems().remove(SLOT_WEAPON, new Item(ammoId, removeAll ? Integer.MAX_VALUE : 1));
			refresh(SLOT_WEAPON);
			if (player.getEquipment().getWeaponId() == -1) {
				player.removeWeaponAttackOption(ammoId);
				player.getAppearence().generateAppearenceData();
			}
		} else {
			getItems().remove(SLOT_ARROWS, new Item(ammoId, amount));
			refresh(SLOT_ARROWS);
		}
	}
	public int getAuraId() {
		Item item = getItems().get(SLOT_AURA);
		if (item == null)
			return -1;
		return item.getId();
	}

	public int getCapeId() {
		Item item = getItems().get(SLOT_CAPE);
		if (item == null)
			return -1;
		return item.getId();
	}

	public int getRingId() {
		Item item = getItems().get(SLOT_RING);
		if (item == null)
			return -1;
		return item.getId();
	}

	public int getAmmoId() {
		Item item = getItems().get(SLOT_ARROWS);
		if (item == null)
			return -1;
		return item.getId();
	}

	public void deleteItem(int itemId, int amount) {
		Item[] itemsBefore = getItems().getItemsCopy();
		getItems().remove(new Item(itemId, amount));
		refreshItems(itemsBefore);
	}

	public void refreshItems(Item[] itemsBefore) {
		int[] changedSlots = new int[itemsBefore.length];
		int count = 0;
		for (int index = 0; index < itemsBefore.length; index++) {
			if (itemsBefore[index] != getItems().getItems()[index])
				changedSlots[count++] = index;
		}
		int[] finalChangedSlots = new int[count];
		System.arraycopy(changedSlots, 0, finalChangedSlots, 0, count);
		refresh(finalChangedSlots);
	}

	public int getBootsId() {
		Item item = getItems().get(SLOT_FEET);
		if (item == null)
			return -1;
		return item.getId();
	}

	public int getGlovesId() {
		Item item = getItems().get(SLOT_HANDS);
		if (item == null)
			return -1;
		return item.getId();
	}

	public ItemsContainer<Item> getItems() {
		return player.getControlerManager().getControler() instanceof FfaZone ? spawnItems : items;
	}

	public int getEquipmentHpIncrease() {
		return equipmentHpIncrease;
	}

	public void setEquipmentHpIncrease(int hp) {
		this.equipmentHpIncrease = hp;
	}

	public boolean wearingArmour() {
		return getItem(SLOT_HAT) != null || getItem(SLOT_CAPE) != null || getItem(SLOT_AMULET) != null || getItem(SLOT_WEAPON) != null || getItem(SLOT_CHEST) != null || getItem(SLOT_SHIELD) != null || getItem(SLOT_LEGS) != null || getItem(SLOT_HANDS) != null || getItem(SLOT_FEET) != null || getItem(SLOT_AURA) != null;
	}

	public int getAmuletId() {
		Item item = getItems().get(SLOT_AMULET);
		if (item == null)
			return -1;
		return item.getId();
	}

	public boolean hasTwoHandedWeapon() {
		Item weapon = getItems().get(SLOT_WEAPON);
		return weapon != null && isTwoHandedWeapon(weapon);
	}

	public ItemsContainer<Item> getCostume() {
		return player.isCanPvp() ? Costumes.DEFAULT.getItems() : 
			(costume == null || (costume.getType() != 0 && (player.getAppearence().isMale() ? 1 : 2) != costume.getType())) ?
					keepsakeItemsFiltered : costume.getItems();
	}
	
	public ItemsContainer<Item> getKeepsakeItems() {
		return keepsakeItems;
	}
	

	public void setCostume(Costumes costume) {
		this.costume = costume;
		player.getAppearence().generateAppearenceData();
	}

	public int getCostumeColor() {
		return costumeColor;
	}

	public void setCostumeColor(int costumeColor) {
		this.costumeColor = costumeColor;
		player.getAppearence().generateAppearenceData();
	}

	public double getEquipmentWeight() {
		return equipmentWeight;
	}

	public ItemsContainer<Item> getKeepsakeItemsFiltered() {
		return keepsakeItemsFiltered;
	}

	public void updateKeepsakeFilter() {
		if (getCostume() != keepsakeItemsFiltered)
			return;
		keepsakeItemsFiltered = keepsakeItems.asItemContainer();
		for (int i = 0; i < keepsakeItemsFiltered.getSize(); i++) {
			Item item = keepsakeItemsFiltered.get(i);
			if (item != null && (!player.containsItem(item.getId())
					
					|| !ItemConstants.hasLevel(item, player) || !ItemConstants.canWear(item, player))
					
					)
				keepsakeItemsFiltered.set(i, null);
		}
		
		
		Item weapon = keepsakeItemsFiltered.get(Equipment.SLOT_WEAPON);
		Item realWeapon = getItem(Equipment.SLOT_WEAPON);
		
		
		if (weapon != null && realWeapon != null) {
			int style = weapon.getDefinitions().getAttackStyle();
			int realStyle = realWeapon.getDefinitions().getAttackStyle();
			if (style != realStyle || isTwoHandedWeapon(weapon) != isTwoHandedWeapon(realWeapon)) {
				keepsakeItemsFiltered.set(Equipment.SLOT_WEAPON, null);
				weapon = realWeapon;
			}
		} else if (weapon == null)
			weapon = realWeapon;
		
		if (weapon != null && isTwoHandedWeapon(weapon))
			keepsakeItemsFiltered.set(Equipment.SLOT_SHIELD, null);
	}
}
