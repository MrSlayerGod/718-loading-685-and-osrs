package com.rs.game.player.content;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Animation;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.utils.Utils;

public class AccessorySmithing {

	public static final int ACCESSORY_INTERFACE = 675;
	private static final int[] MOLDS =
	{ 1592, 1597, 1595, 11065 };
	public static final int[] GEMS =
	{ 2357, 1607, 1605, 1603, 1601, 1615, 6573, 49493 };
	public static final int[][] ITEMS =
	{
	{ 1635, 1637, 1639, 1641, 1643, 1645, 6575, 49538 },
	{ 1654, 1656, 1658, 1660, 1662, 1664, 6577, 49535 },
	{ 1692/*1673*/, 1694/*1675*/, 1696/*1677*/, 1698/*1679*/, 1700/*1681*/, 1702/*1683*/, /*6579*/6581, 49541 },
	{ 11069, 11072, 11076, 11085, 11092, 11115, 11130, 49532 } };
	private static final int[] NULL_ITEMS =
	{ 1647, 1666, 1685, 11067 };
	private static final int[] COMPONENTS_BASE =
	{ 20, 39, 58, 77};
	private static final double[][] EXPERIENCE =
	{
	{ 15, 40, 55, 70, 85, 100, 115, 150},
	{ 20, 55, 60, 75, 90, 105, 120, 165 },
	{ 25, 60, 65, 80, 95, 110, 125, 200},
	{ 30, 65, 70, 85, 100, 150, 165, 180 } };
	public static final byte[][] LEVEL =
	{
	{ 5, 20, 27, 34, 43, 55, 67, 89},
	{ 6, 22, 29, 40, 56, 72, 82, 92},
	{ 8, 24, 31, 50, 70, 80, 90, 98},
	{ 7, 23, 30, 42, 58, 74, 84, 95} };
	private static final int[] ONYX =
	{ 6575, 6577, 6579, 11130 };
	private static final int[] PRE_IMBUED_RINGS =
	{ 1635, 1637, 1639, 1641, 1643, 1645, 9104, 4202, 6575, 6731, 6733, 6735, 6737, 42601, 42603, 42605, 49550, 51739,  10588};
	public static final int[] POST_IMBUED_RINGS =
	{ 15009, 15010, 15011, 15012, 15013, 15014, 15015, 15016, 15017, 15018, 15019, 15020, 15220, 43202, 42691, 42692, 49710, 51752, 42018 };

	private static final int[] PRE_IMBUED_RINGS_M =
		{ 25486, 25488};
		public static final int[] POST_IMBUED_RINGS_M =
		{ 25740, 25741 };

	
	public static void openInterface(Player player, boolean zenyte) {
		if (zenyte && player.getInventory().containsOneItem(49493)) {
			player.getDialogueManager().startDialogue("ZenyteD", 7);
			return;
		}
		player.getInterfaceManager().sendInterface(ACCESSORY_INTERFACE);
		callCS2(player);
		for (int primaryIndex = 0; primaryIndex < MOLDS.length; primaryIndex++) {
			if (player.getInventory().containsItemToolBelt(MOLDS[primaryIndex])) {
				player.getPackets().sendIComponentText(ACCESSORY_INTERFACE, 16 + (primaryIndex * 19), "");
				for (int secondaryIndex = 0; secondaryIndex < ITEMS[primaryIndex].length - 1; secondaryIndex++) {
					player.getPackets().sendItems((299 + (primaryIndex * 14) + secondaryIndex), new Item[]
					{ new Item(!player.getInventory().containsItemToolBelt(GEMS[secondaryIndex]) ? NULL_ITEMS[primaryIndex] : ITEMS[primaryIndex][secondaryIndex]) });
				}
			} else
				resetIndex(player, primaryIndex);
		}
	}

	private static void resetIndex(Player player, int primaryIndex) {
		for (int secondaryIndex = 0; secondaryIndex < ITEMS[primaryIndex].length - 1; secondaryIndex++)
			player.getPackets().sendItems((299 + (primaryIndex * 14) + secondaryIndex), new Item[] {});
	}

	private static void callCS2(Player player) {
		for (int primaryIndex = 0; primaryIndex < COMPONENTS_BASE.length; primaryIndex++) {
			for (int secondaryIndex = 0; secondaryIndex < ITEMS[primaryIndex].length; secondaryIndex++) {
				player.getPackets().sendInterSetItemsOptionsScript(ACCESSORY_INTERFACE, (COMPONENTS_BASE[primaryIndex] + secondaryIndex * 2), (299 + (primaryIndex * 14) + secondaryIndex), false, 6, 4, checkAll(player, primaryIndex, secondaryIndex, true) ? new String[]
				{ "Make-X", "Make 10", "Make 5" } : new String[] {});
				player.getPackets().sendUnlockIComponentOptionSlots(ACCESSORY_INTERFACE, (COMPONENTS_BASE[primaryIndex] + secondaryIndex * 2), 0, 28, 0, 1, 2, 3);
			}
		}
	}

	private static boolean checkAll(Player player, int primaryIndex, int secondaryIndex, boolean cs2Call) {
		if (primaryIndex >=  LEVEL.length || secondaryIndex >= LEVEL[primaryIndex].length)
			return false;
		int level = LEVEL[primaryIndex][secondaryIndex];
		if (player.getSkills().getLevel(Skills.CRAFTING) < level) {
			if (!cs2Call)
				player.getPackets().sendGameMessage("You need a Crafting level of " + level + ".");
			return false;
		} else if (!player.getInventory().containsItem(2357, 1) && !cs2Call) {
			player.getPackets().sendGameMessage("You need a gold bar in order to create an accessory.");
			return false;
		} else if (secondaryIndex != 0 && !player.getInventory().containsItem(GEMS[secondaryIndex], 1)) {
			if (!cs2Call)
				player.getPackets().sendGameMessage("You are missing required the requried items in order to create this type of accessory.");
			return false;
		}
		return true;
	}


	public static void make(Player player, int actionPrimaryIndex, int actionSecondaryIndex, final int tick) {
		player.closeInterfaces();
		player.getActionManager().setAction(new Action() {

			int ticks;

			@Override
			public boolean start(Player player) {
				this.ticks = tick;
				return process(player);
			}

			@Override
			public boolean process(Player player) {
				if (ticks <= 0 || !checkAll(player, actionPrimaryIndex, actionSecondaryIndex, false))
					return false;
				return true;
			}

			@Override
			public int processWithDelay(Player player) {
				ticks--;
				player.setNextAnimation(new Animation(3243));
				player.getInventory().deleteItem(2357, 1);
				int gem = GEMS[actionSecondaryIndex];
				if (gem != 2357)
					player.getInventory().deleteItem(gem, 1);
				player.getInventory().addItem(actionSecondaryIndex == 6 ? ONYX[actionPrimaryIndex] : ITEMS[actionPrimaryIndex][actionSecondaryIndex], 1);
				player.getSkills().addXp(Skills.CRAFTING, EXPERIENCE[actionPrimaryIndex][actionSecondaryIndex]);
				return 2;
			}

			@Override
			public void stop(Player player) {
				setActionDelay(player, 3);
			}
		});
	}
	
	public static void handleButtonClick(Player player, int componentId, final int tick) {
		for (int primaryIndex = 0; primaryIndex < COMPONENTS_BASE.length; primaryIndex++) {
			for (int secondaryIndex = 0; secondaryIndex < ITEMS[primaryIndex].length; secondaryIndex++) {
				if (componentId == (COMPONENTS_BASE[primaryIndex] + secondaryIndex * 2)) {
					if (!player.getInventory().containsItemToolBelt(MOLDS[primaryIndex]))
						return;
					final int actionPrimaryIndex = primaryIndex, actionSecondaryIndex = secondaryIndex;
					make(player, actionPrimaryIndex, actionSecondaryIndex, tick);
				}
			}
		}
	}

	private static final int[] EGG_IDS =
	{ 3689, 3690, 3691, 3692, 3693, 3694 };

	public static void ringTransformation(Player player, final int itemId) {
		if (player.getActionManager().getAction() != null) {
			player.getPackets().sendGameMessage("Please finish what you are doing before transforming.");
			return;
		}
		player.getActionManager().setAction(new Action() {

			@Override
			public boolean start(Player player) {
				player.stopAll();
				player.lock(2);
				int transformationId = EGG_IDS[Utils.random(EGG_IDS.length)];
				if (itemId == 6583)
					transformationId = 2626;
				player.getAppearence().transformIntoNPC(transformationId);
				player.getInterfaceManager().sendInventoryInterface(375);
				return true;
			}

			@Override
			public boolean process(Player player) {
				return true;
			}

			@Override
			public int processWithDelay(Player player) {
				return 0;
			}

			@Override
			public void stop(Player player) {
				setActionDelay(player, 3);
				resetTransformation(player);
			}
		});
	}

	public static void resetTransformation(Player player) {
		player.getInterfaceManager().removeInventoryInterface();
		player.getInventory().init();
		player.setNextAnimation(new Animation(14884));
		player.getAppearence().transformIntoNPC(-1);
	}

	public static boolean isEmptyRing(int itemId) {
		for (int ring : PRE_IMBUED_RINGS) {
			if (ring == itemId)
				return true;
		}
		return false;
	}

	public static int getImbuedId(int itemId) {
		for (int index = 0; index < PRE_IMBUED_RINGS.length; index++) {
			if (PRE_IMBUED_RINGS[index] == itemId)
				return POST_IMBUED_RINGS[index];
		}
		return -1;
	}
	
	public static int getImbuedIdMythical(int itemId) {
		for (int index = 0; index < PRE_IMBUED_RINGS_M.length; index++) {
			if (PRE_IMBUED_RINGS_M[index] == itemId)
				return POST_IMBUED_RINGS_M[index];
		}
		return -1;
	}
	
	public static int getPreImbuedId(int itemId) {
		for (int index = 0; index < PRE_IMBUED_RINGS.length; index++) {
			if (POST_IMBUED_RINGS[index] == itemId)
				return PRE_IMBUED_RINGS[index];
		}
		return -1;
	}
	
	public static boolean uncharge(Player player, Item item) {
		int id = getPreImbuedId(item.getId());
		if (id == -1)
			return false;
		player.lock(2);
		player.getInventory().deleteItem(item.getId(), 1);
		player.getInventory().addItem(id, 1);
		player.getPackets().sendGameMessage("You uncharge your "+ItemConfig.forID(id).getName()+".");
		return true;
		
	}
}
