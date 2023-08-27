package com.rs.game.player;

import java.io.Serializable;
import java.util.List;

import com.rs.cache.loaders.ObjectConfig;
import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.minigames.clanwars.FfaZone;
import com.rs.game.npc.NPC;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.commands.Commands;
import com.rs.game.player.content.questTab.QuestTab;
import com.rs.utils.Utils;

/**
 * 
 * @author Alex (Dragonkk)
 * Jan 27, 2020
 */
public class Presets implements Serializable {


	public int getLastPreset() {
		return lastPreset;
	}

	public static class Preset implements Serializable {
		
		   private static final long serialVersionUID = 1385575955598546603L;
		
		   private String name;
		   private Item[] inventory, equipment;
		   
		   public Preset(String name, Item[] inventory, Item[] equipment) {
			   this.name = name;
			   this.inventory = inventory;
			   this.equipment = equipment;
		   }
		   
		   public String getName() {
			   return name;
		   }
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4381860913289687500L;

	private Preset[] presets;

	private int lastPreset = -1;
	
	public Presets() {
		presets = new Preset[10];
	}
	
	private transient Player player;
	private transient long loadTime;
	
	public int getSetsCount() {
		int count = 0;
		for (Preset preset : presets)
			if (preset != null)
				count++;
		return count;
	}
	public Preset[] getPresets() {
		return presets;
	}
	
	public void set(int index, String name, boolean setInventory, boolean setEquipment) {
		presets[index] = new Preset(name, setInventory ? player.getInventory().getItems().getItemsCopy() : null, setEquipment ?player.getEquipment().getItems().getItemsCopy() : null);
		player.getPackets().sendGameMessage("You've successfully stored the set " + name + ".", true);
		QuestTab.refresh(player, false);
	}
	
	public void empty(int index) {
		Preset preset = presets[index];
		if (preset == null)
			return;
		player.getPackets().sendGameMessage("Successfully removed the set: "+preset.name);
		presets[index] = null;
		QuestTab.refresh(player, false);
	}
	
	public boolean hasBankNear() {
		if (player.getControlerManager().getControler() instanceof FfaZone)
			return true;
		for (int regionId : player.getMapRegionsIds()) {
			Region region = World.getRegion(regionId);
			List<Integer> indexes = World.getRegion(regionId).getNPCsIndexes();
			if (indexes != null) {
				for (int npcIndex : indexes) {
					NPC n = World.getNPCs().get(npcIndex);
					if (n == null || n.hasFinished() || !n.withinDistance(player, 8) || n.isDead() || !n.getDefinitions().hasOption("Bank"))
						continue;
					return true;
				}
			}
			List<WorldObject> objects = region.getAllObjects();
			if (objects != null) {
				for (WorldObject object : objects) {
					if (object.getPlane() == player.getPlane() &&  object.getType() == 10 && player.withinDistance(object, 8)) {
						ObjectConfig config = object.getDefinitions();
						if (config.containsOption("Bank") || config.name.toLowerCase().contains("bank"))
							return true;
					}
				}
			}
			objects = region.getSpawnedObjects();
			if (objects != null) {
				for (WorldObject object : objects) {
					if (object.getPlane() == player.getPlane() &&  object.getType() == 10 && player.withinDistance(object, 5)) {
						ObjectConfig config = object.getDefinitions();
						if (config.containsOption("Bank") || config.name.toLowerCase().contains("bank"))
							return true;
					}
				}
			}
		}
		return false;
	}
	
	public void load(int index) {
		if(index >= presets.length)
			return;
		Preset preset = presets[index];
		if (preset == null)
			return;
		if (loadTime > Utils.currentTimeMillis()) {
			player.getPackets().sendGameMessage("Please wait 3 seconds before loading a setup again.");
			return;
		}
		this.loadTime = Utils.currentTimeMillis() + 3000;
		if (player.getControlerManager().getControler() != null && !(player.getControlerManager().getControler() instanceof FfaZone)) {
			player.getPackets().sendGameMessage("You can not use presets here.");
			return;
		}
		if (player.isCanPvp()) {
			player.getPackets().sendGameMessage("You can not use presets in a pvp area.");
			return;
		}
		if (!player.isSupremeVIPDonator() && !hasBankNear()) {
			player.getPackets().sendGameMessage("<col=FF0040>You can only load presets near a bank.");
			return;
		}

		if ((player.getControlerManager().getControler() instanceof FfaZone)) {
			if (preset.inventory != null) {
				for (Item item : preset.inventory)
					if (item != null && !Commands.canSpawnItem(player, item.getId(), item.getAmount())) {
						player.sendMessage("You can't spawn this preset in Spawn PK!");
						return;
					}
			}
			if (preset.equipment != null) {
				for (Item item : preset.equipment)
					if (item != null && !Commands.canSpawnItem(player, item.getId(), item.getAmount())) {
						player.sendMessage("You can't spawn this preset in Spawn PK!");
						return;
					}
			}
		}
		lastPreset = index;
		
		if (preset.inventory != null) {
			for (Item item : player.getInventory().getItems().getItems()) 
				if (item != null) {
					Item bankItem = player.getBank().getItem(player.getBank().getItemSlot(item.getId()));
					if ((bankItem != null && ((long)bankItem.getAmount() + (long)item.getAmount()) > Integer.MAX_VALUE)
							|| (bankItem == null && !player.getBank().hasBankSpace())) {
						player.getPackets().sendGameMessage("Couldn't bank inventory. Not enough space in your bank.");
						return;
				}
			}
		}
		if (preset.equipment != null) {
			for (Item item : player.getEquipment().getItems().getItems()) 
				if (item != null) {
					Item bankItem = player.getBank().getItem(player.getBank().getItemSlot(item.getId()));
					if ((bankItem != null && ((long)bankItem.getAmount() + (long)item.getAmount()) > Integer.MAX_VALUE)
							|| (bankItem == null && !player.getBank().hasBankSpace())) {
						player.getPackets().sendGameMessage("Couldn't use preset. Not enough space in your bank.");
						return;
				}
			}
		}
		
		player.getAuraManager().removeAura();
		if (!(player.getControlerManager().getControler() instanceof FfaZone)) {
			//unload inv & equip
			if (preset.inventory != null) {
				for (Item item : player.getInventory().getItems().getItems())
					if (item != null)
						player.getBank().addItem(item, false);
				player.getInventory().getItems().reset();
			}

			if (preset.equipment != null) {
				for (Item item : player.getEquipment().getItems().getItems())
					if (item != null)
						player.getBank().addItem(item, false);
				player.getEquipment().getItems().reset();
			}
		}
		
		
		if (preset.inventory != null) {
			for (int i = 0; i < preset.inventory.length; i++) {
				Item item = preset.inventory[i];
				if (item == null)
					continue;
				int[] bankSlot = player.getBank().getItemSlot(item.isNoted() ? item.getNotedId() : item.getId());
				Item bankItem = player.getBank().getItem(bankSlot);
				if (!(player.getControlerManager().getControler() instanceof FfaZone) && (bankItem == null || bankItem.getAmount() < item.getAmount())) {
					player.getPackets().sendGameMessage("Couldn't find item " + item.getAmount() + " x " + item.getName() + " in bank.");
					if (bankItem == null || bankItem.getAmount() == 0)
						continue;
				}
				int amount = player.getControlerManager().getControler() instanceof FfaZone ? item.getAmount() : Math.min(item.getAmount(), bankItem.getAmount());
				if (!(player.getControlerManager().getControler() instanceof FfaZone))
					player.getBank().removeItem(bankSlot, amount, false, false);
				player.getInventory().getItems().set(i, new Item(item.getId(), amount));
			}
			player.getInventory().refresh();
		}
		if (preset.equipment != null) {
			for (int i = 0; i < preset.equipment.length; i++) {
				Item item = preset.equipment[i];
				if (item == null || (i == Equipment.SLOT_AURA && player.getAuraManager().isActivated()))
					continue;
				int[] bankSlot = player.getBank().getItemSlot(item.getId());
				Item bankItem = player.getBank().getItem(bankSlot);
				if ((!(player.getControlerManager().getControler() instanceof FfaZone)) && (bankItem == null || bankItem.getAmount() < item.getAmount())) {
					player.getPackets().sendGameMessage("Couldn't find item " + item.getAmount() + " x " + item.getName() + " in bank.");
					if (bankItem == null || bankItem.getAmount() == 0)
						continue;
				}
				if (!ItemConstants.hasLevel(item, player) || !ItemConstants.canWear(item, player)) {
					player.getPackets().sendGameMessage("Couldn't wear item " + item.getAmount() + " x " + item.getName() + " in bank.");
					continue;
				}
				int amount = player.getControlerManager().getControler() instanceof FfaZone ? item.getAmount() : Math.min(item.getAmount(), bankItem.getAmount());
				if (!(player.getControlerManager().getControler() instanceof FfaZone))
					player.getBank().removeItem(bankSlot, amount, false, false);
				player.getEquipment().getItems().set(i, new Item(item.getId(), amount));
			}
			player.getEquipment().init();
			player.getAppearence().generateAppearenceData();
		}
		player.getPackets().sendGameMessage("Loaded setup: " + preset.name + ".");
	}
	
	public void reset() {
		for (int i = 0; i < presets.length; i++)
			presets[i] = null;
		QuestTab.refresh(player, false);
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}
}
