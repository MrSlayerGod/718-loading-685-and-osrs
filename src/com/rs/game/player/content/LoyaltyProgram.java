package com.rs.game.player.content;

import com.rs.Settings;
import com.rs.cache.loaders.GeneralRequirementMap;
import com.rs.game.player.Player;

public class LoyaltyProgram {
	
	public static final int LOYALTY_INTERFACE = 1143;
	private static final int[] REQUIRMENT_SCRIPTS = { 2586 };

	/**
	 * The loyalty shop interface
	 */
	public static final int INTERFACE_ID = 1143;

	/**
	 * The tab switch config
	 */
	public static final int TAB_CONFIG = 2226;

	/**
	 * The player using the programme
	 */
	private Player player;

	/**
	 * Opens the loyalty shop interface
	 */
	public static void openShop(Player player) {
		player.getInterfaceManager().setScreenInterface( false, 96, INTERFACE_ID);
		player.getPackets().sendVar(TAB_CONFIG, -1);
		//currentTab = -1;
		player.getPackets().sendIComponentText(INTERFACE_ID, 127,
				"#points");
	}

	public static void open(Player player) {
		player.getInterfaceManager().setWindowsPane(LOYALTY_INTERFACE);
		player.getPackets().sendCSVarInteger(1648, player.getInventory().getAmountOf(Settings.VOTE_TOKENS_ITEM_ID));
	}
	
	public static void handleButtonClick(Player player, int componentId, int slot) {
		//if (componentId >= 7 && componentId <= 13)//tabs
			//sendTab(player, componentId - 7, slot);
		switch (componentId) {
			case 3:
				openTab(player, "favorites");
				break;
			case 103:
				player.getInterfaceManager().setWindowInterface(
						player.getInterfaceManager().hasRezizableScreen() ? 746
								: 548, 0);
				break;
			case 1:
				openTab(player, "home");
				break;
			case 7:
				openTab(player, "auras");
				break;
			case 8:
				openTab(player, "effects");
				break;
			case 9:
				openTab(player, "emotes");
				break;
			case 10:
				openTab(player, "outfits");
				break;
			case 11:
				openTab(player, "titles");
				break;
			case 12:
				openTab(player, "recolor");
				break;
			case 13:
				openTab(player, "special-offers");
				break;
		}
	}

	/**
	 * Opens a tab on the loyalty interface
	 *
	 * @param tab
	 *            The tab to open
	 */
	public static void openTab(Player player, String tab) {
		int currentTab = 0;
		switch (tab.toLowerCase()) {
			case "home":
				player.getPackets().sendVar(TAB_CONFIG, -1);
				currentTab = -1;
			case "auras":
				player.getPackets().sendVar(TAB_CONFIG, 1);
				currentTab = 1;
				break;
			case "emotes":
				player.getPackets().sendVar(TAB_CONFIG, 2);
				currentTab = 2;
				break;
			case "outfits":
				player.getPackets().sendVar(TAB_CONFIG, 3);
				currentTab = 3;
				break;
			case "titles":
				player.getPackets().sendVar(TAB_CONFIG, 4);
				currentTab = 4;
				break;
			case "recolor":
				player.getPackets().sendVar(TAB_CONFIG, 5);
				currentTab = 5;
				break;
			case "special-offers":
				player.getPackets().sendVar(TAB_CONFIG, 6);
				currentTab = 6;
				break;
			case "limmited-edition":
				player.getPackets().sendVar(TAB_CONFIG, 7);
				currentTab = 7;
				break;
			case "favorites":
				player.getPackets().sendVar(TAB_CONFIG, 8);
				currentTab = 8;
				break;
			case "effects":
				player.getPackets().sendVar(TAB_CONFIG, 9);
				currentTab = 9;
				break;
			default:
				player.getPackets().sendGameMessage(
						"This tab is currently un-available"
								+ (player.getRights() >= 2 ? ": " + "\"" + tab
								+ "\"" : "."));
		}
		player.getTemporaryAttributtes().put("LOYALTY_SHOP_TAB", currentTab);
	}

	private static void sendTab(Player player, int selectedTab, int slot) {
		GeneralRequirementMap map = GeneralRequirementMap.getMap(REQUIRMENT_SCRIPTS[selectedTab]);
		if (map == null)
			return;
		player.getPackets().sendUnlockIComponentOptionSlots(LOYALTY_INTERFACE, 0, 0, map.getValues().size(), 0, 1);
	}
}
