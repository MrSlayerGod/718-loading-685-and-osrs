package com.rs.game.player.content;

import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.CallusController;
import com.rs.game.player.controllers.UnderGroundDungeon;

import java.util.Arrays;

public class LightSource {

	private static final int[][] LIGHT_SOURCES =
	{
	{ 596, 36, 4529, 4522, 4537, 7051, 4548, 5014, 4701},
	{ 594, 33, 4534, 4524, 4539, 7053, 4550, 5013, 4702} };

	public static boolean hasExplosiveSource(Player player) {
		for (Item item : player.getInventory().getItems().getItems()) {
			if (item == null)
				continue;
			int slot = getSlot(item.getId(), false);
			if (slot != -1 && (slot == 0 || slot == 1))
				return true;
		}
		for (Item item : player.getEquipment().getItems().getItems()) {
			if (item == null)
				continue;
			int slot = getSlot(item.getId(), false);
			if (slot != -1 && (slot == 0 || slot == 1))
				return true;
		}
		return false;
	}

	public static boolean hasPermenantSource(Player player) {
		for (Item item : player.getInventory().getItems().getItems()) {
			if (item == null)
				continue;
			int slot = getSlot(item.getId(), false);
			if (slot != -1 && slot != 0 && slot != 1)
				return true;
		}
		for (Item item : player.getEquipment().getItems().getItems()) {
			if (item == null)
				continue;
			int slot = getSlot(item.getId(), false);
			if (slot != -1 && slot != 0 && slot != 1)
				return true;
		}
		return false;
	}

	public static boolean hasLightSource(Player player) {
		return hasExplosiveSource(player) || hasPermenantSource(player);
	}

	private static int getSlot(int itemId, boolean extinguished) {
		for (int slot = 0; slot < 9; slot++) {
			int id = LIGHT_SOURCES[extinguished ? 0 : 1][slot];
			if (id == itemId)
				return slot;
		}
		return -1;
	}

	public static void extinguishAll(Player player) {
		for(int i = 0; i < 28; i++)
			extinguishSource(player, i, true);
		Arrays.stream(player.getEquipment().getItems().getItems()).forEach(item -> {
			if(item != null) {
				int slot = getSlot(item.getId(), false);
				if(slot != -1)
					item.setId(LIGHT_SOURCES[0][slot]);
			}
		});

		player.getEquipment().init();
		player.getAppearence().generateAppearenceData();
	}

	public static boolean extinguishSource(Player player, int itemSlot, boolean forceExtinguish) {
		Item item = player.getInventory().getItem(itemSlot);
		if (item == null)
			return false;
		int slot = getSlot(item.getId(), false);
		if (slot == -1)
			return false;
		else if (!forceExtinguish && player.getControlerManager().getControler() != null && player.getControlerManager().getControler() instanceof UnderGroundDungeon) {
			player.getPackets().sendGameMessage("You cannot extinguish the " + item.getName().toLowerCase() + " as you will not have a light source.");
			return true;
		}
		player.getInventory().replaceItem(LIGHT_SOURCES[0][slot], item.getAmount(), itemSlot);
		player.getPackets().sendGameMessage("You extinguish the " + item.getName().toLowerCase() + ".");
		return true;
	}

	public static boolean lightSource(Player player, int itemSlot) {
		Item item = player.getInventory().getItem(itemSlot);
		if (item == null)
			return false;
		int slot = getSlot(item.getId(), true);
		if (slot == -1)
			return false;
		else if (!player.getInventory().containsItemToolBelt(590)) {
			player.getPackets().sendGameMessage("You need a tinderbox in order to light the " + item.getName().toLowerCase() + ".");
			return false;
		}
		player.getInventory().replaceItem(LIGHT_SOURCES[1][slot], item.getAmount(), itemSlot);
		player.getPackets().sendGameMessage("You light the " + item.getName().toLowerCase() + ".");
		return true;
	}
}
