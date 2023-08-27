package com.rs.game.player;

import java.io.Serializable;
import java.util.Arrays;

import com.rs.game.item.Item;
import com.rs.game.player.actions.mining.MiningBase;
import com.rs.game.player.actions.mining.MiningBase.PickAxeDefinitions;
import com.rs.game.player.actions.woodcutting.WoodcuttingBase;
import com.rs.game.player.actions.woodcutting.WoodcuttingBase.HatchetDefinitions;

public class Toolbelt implements Serializable {

	private static final long serialVersionUID = -7244573478285647954L;

	public static final int[][] TOOLBELT_ITEMS = new int[][]
	{
	{ 946 },
	{ 1735 },
	{ 1595 },
	{ 1755 },
	{ 1599 },
	{ 1597 },
	{ 1733 },
	{ 1592 },
	{ 5523 },
	{ 13431 },
	{ 307 },
	{ 309 },
	{ 311 },
	{ 301 },
	{ 303 },
	{ 1265, 1267, 1269, 1273, 1271, 1275, 15259,  53680},
	{ 2347 },
	{ 1351, 1349, 1353, 1357, 1355, 1359, 6739, 53673},
	{ 590 },
	{ -1 },
	{ 8794 },
	{ 4 },
	{ 9434 },
	{ 11065 },
	{ 1785 },
	{ 2976 },
	{ 1594 },
	{ 5343 },
	{ 5325 },
	{ 5341 },
	{ 5329 },
	{ 233 },
	{ 952 },
	{ 305 },
	{ 975 },
	{ 11323 },
	{ 2575 },
	{ 2576 },
	{ 13153 },
	{ 10150 } };
	private static final int[][] DUNG_TOOLBELT_ITEMS = new int[][]
	{
	{ 16295, 16297, 16299, 16301, 16303, 16305, 16307, 16309, 16311, 16313, 16315 },
	{ 16361, 16363, 16365, 16367, 16369, 16371, 16373, 16375, 16377, 16379, 16381 },
	{ 17883 },
	{ 17678 },
	{ 17794 },
	{ 17754 },
	{ 17446 },
	{ 17444 } };

	private static final int[] VAR_IDS = new int[]
	{ 2438, 2439 };
	private static final int[] DUNG_VAR_IDS = new int[]
	{ 2560 };
	private int[][] items;
	private transient Player player;
	private transient boolean dungeonnering;


	
	public Toolbelt() {
		items = new int[][]
		{ new int[TOOLBELT_ITEMS.length], new int[DUNG_TOOLBELT_ITEMS.length] };
	}

	public void setPlayer(Player player) {
		this.player = player;
		if (items == null)
			items = new int[][]
			{ new int[TOOLBELT_ITEMS.length], new int[DUNG_TOOLBELT_ITEMS.length] };

		/*
		 * if(items.length <= TOOLBET_ITEMS.length) //if we add a new item items
		 * = Arrays.copyOf(items, TOOLBET_ITEMS.length);
		 */
	}

	public void init() {
		refreshConfigs();
	}

	private int getVarIndex(int i) {
		return i / 20;
	}

	public int getIncremment(int slot) {
		if (!dungeonnering)
			return 1;
		return slot == 0 ? 5 : slot == 1 ? 4 : 1;
	}

	public void refreshConfigs() {

		int[] varValues = new int[getVars().length];
		int indexIncremment = 0;
		for (int i = 0; i < getItems().length; i++) {
			if (true || getItems()[i] != 0) {
				int index = getVarIndex(indexIncremment);
				int itemIndex = getItems()[i];
				if (!dungeonnering && itemIndex > 0)
					itemIndex = 1;
				varValues[index] |= ((true && itemIndex == 0 ? 1 : itemIndex) << (indexIncremment - (index * 20)));
			}
			indexIncremment += getIncremment(i);
		}
		for (int i = 0; i < getVars().length; i++)
			player.getVarsManager().sendVar(getVars()[i], varValues[i]);
	}

	public int[] getItems() {
		return items[dungeonnering ? 1 : 0];
	}

	public int[] getVars() {
		return dungeonnering ? DUNG_VAR_IDS : VAR_IDS;
	}

	public int[][] getToolbeltItems() {
		return dungeonnering ? DUNG_TOOLBELT_ITEMS : TOOLBELT_ITEMS;
	}

	private int[] getItemSlot(int id) {
		for (int i = 0; i < getToolbeltItems().length; i++)
			for (int i2 = 0; i2 < getToolbeltItems()[i].length; i2++)
				if (getToolbeltItems()[i][i2] == id)
					return new int[]
					{ i, i2 };
		return null;
	}

	public boolean addItem(int invSlot, Item item) {
		int[] slot = getItemSlot(item.getId());
		if (slot == null)
			return false;
		if ((true && slot[1] + 1 <= 1) || getItems()[slot[0]] >= slot[1] + 1)
			player.getPackets().sendInventoryMessage(0, invSlot, "That is already on your tool belt.");
		else {
			
			HatchetDefinitions wdef = WoodcuttingBase.getHatchet(item.getId());
			if (wdef != null && player.getSkills().getLevelForXp(Skills.WOODCUTTING) < wdef.getLevelRequried()) {
				player.getPackets().sendGameMessage("You don't have the required level to use that axe.");
				return true;
			}
			PickAxeDefinitions mdef = MiningBase.getPickaxe(item.getId());
			if (mdef != null && player.getSkills().getLevelForXp(Skills.MINING) < mdef.getLevelRequried()) {
				player.getPackets().sendGameMessage("You don't have the required level to use that pickaxe.");
				return true;
			}
			
			
			getItems()[slot[0]] = slot[1] + 1;
			player.getInventory().deleteItem(invSlot, item);
			refreshConfigs();
			player.getPackets().sendGameMessage("You add the " + item.getDefinitions().getName() + " to your tool belt.");
		}
		return true;
	}

	public void switchDungeonneringToolbelt() {
		this.dungeonnering = !dungeonnering;
		player.getPackets().sendCSVarInteger(1725, dungeonnering ? 11 : 1);
		refreshConfigs();
	}

	public boolean containsItem(int id) {
		int[] slot = getItemSlot(id);
		return slot != null && ((true && slot[1] + 1 <= 1) || getItems()[slot[0]] >= slot[1] + 1);
	}
	

}
