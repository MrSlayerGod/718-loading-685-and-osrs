package com.rs.game.minigames.pest;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.HerbCleaning.Herbs;
import com.rs.game.player.content.Nest;
import com.rs.game.player.content.collectionlog.CategoryType;
import com.rs.utils.Utils;

public class CommendationExchange {//1875 TODO

	// can be static :) 1513 cs for names (useless really)

	private static final int INTERFACE = 1011, RATE_ONE = 1, RATE_TEN = 10, RATE_HUNDRED = 100;
	/**
	 * EXP related stuff
	 */
	private static final int[] SKILL_BASE_COMPONENTS =
	{ 100, 116, 132, 148, 164, 180 };
	private static final int[] SKILLS =
	{ Skills.STRENGTH, Skills.DEFENCE, Skills.HITPOINTS, Skills.RANGE, Skills.MAGIC, Skills.PRAYER };

	/**
	 * Void related stuff
	 */
	private static final int[] VOID_BASE_COMPONENTS =
	{ 15, 196, 208, 220, 232, 244, 256, 268, 280 };
	public static final int[] VOID =
	{ 11676, 11675, 11674, 8839, 8840, 8842, 8841, 19712, 11666 };
	private static final int[] VOID_POINTS_COST =
	{ 200, 200, 200, 250, 250, 150, 250, 150, 10 };

	/**
	 * Charm related stuff
	 */
	private static final int[] CHARM_BASE_COMPONENTS =
	{ 324, 339, 354, 369 };
	public static final int[] CHARMS =
	{ 12166, 12167, 12164, 12165 };

	public static void openExchangeShop(Player player) {
		player.getInterfaceManager().sendInterface(INTERFACE);
		player.getVarsManager().sendVar(1875, 1250);
		refreshPoints(player);
		player.getPackets().sendGameMessage("XP rewards are x10 the amount displayed.");
		
	}

	private static void refreshPoints(Player player) {
		player.getVarsManager().sendVarBit(2086, Math.min(500, player.getPestPoints()));
		player.getPackets().sendIComponentText(1011, 61, "Commendations: "+player.getPestPoints());
		player.getPackets().sendHideIComponent(1011, 62, true);
		//player.getPackets().sendIComponentText(1011, 62, Integer.toString(player.getPestPoints()));
	}

	private static boolean exchangeCommendation(Player player, int price) {
		int newPoints = player.getPestPoints() - price;
		if (newPoints < 0) {
			player.getPackets().sendGameMessage("You don't have enough Commendations remaining to complete this exchange.");
			return false;
		}
		player.setPestPoints(newPoints);
		refreshPoints(player);
		return true;
	}

	public static void handleButtonOptions(Player player, int componentId) {
		if (componentId == 68) {
			addXPForSkill(player, Skills.ATTACK, RATE_ONE);
		} else if (componentId == 86) {
			addXPForSkill(player, Skills.ATTACK, RATE_TEN);
		} else if (componentId == 88) {
			addXPForSkill(player, Skills.ATTACK, RATE_HUNDRED);
		} else if (componentId == 29) {
			player.getPackets().sendHideIComponent(INTERFACE, 69, false);
			player.getPackets().sendHideIComponent(INTERFACE, 63, true);
		} else if (componentId == 75) {
			player.getPackets().sendHideIComponent(INTERFACE, 70, true);
			player.getPackets().sendHideIComponent(INTERFACE, 69, false);
			player.getPackets().sendHideIComponent(INTERFACE, 63, true);
		} else if (componentId == 20 || componentId == 73) {
			player.getPackets().sendHideIComponent(INTERFACE, 70, true);
			player.getPackets().sendHideIComponent(INTERFACE, 69, true);
			player.getPackets().sendHideIComponent(INTERFACE, 63, false);
		} else if (componentId == 24 || componentId == 31) {
			player.getPackets().sendHideIComponent(INTERFACE, 70, false);
			player.getPackets().sendHideIComponent(INTERFACE, 63, true);
			player.getPackets().sendHideIComponent(INTERFACE, 69, true);
		} else if (componentId == 291) {
			if (player.getSkills().getLevelForXp(Skills.HERBLORE) < 25) {
				player.getPackets().sendGameMessage("You need a herblore level of 25 in order to purchase a herblore pack.");
				return;
			} else if (!exchangeCommendation(player, 30))
				return;
			player.getInventory().addItemDrop(Herbs.values()[Utils.random(5)].getHerbId() + 1, Utils.random(1, 5));
			player.getInventory().addItemDrop(Herbs.values()[Utils.random(Herbs.values().length - 9)].getHerbId() + 1, Utils.random(1, 4));
			player.getPackets().sendGameMessage("You exchange 30 commendation points for a herblore pack.");
		} else if (componentId == 302) {
			if (player.getSkills().getLevelForXp(Skills.MINING) < 25) {
				player.getPackets().sendGameMessage("You need a mining level of 25 in order to purchase a mineral pack.");
				return;
			} else if (!exchangeCommendation(player, 15))
				return;
			player.getInventory().addItemDrop(441, Utils.random(20));
			player.getInventory().addItemDrop(454, Utils.random(30));
			player.getPackets().sendGameMessage("You exchange 15 commendation points for a mineral pack.");
		} else if (componentId == 313) {
			if (player.getSkills().getLevelForXp(Skills.FARMING) < 25) {
				player.getPackets().sendGameMessage("You need a farming level of 25 in order to purchase a seed pack.");
				return;
			} else if (!exchangeCommendation(player, 15))
				return;
			player.getInventory().addItemDrop(Nest.SEEDS[0][Utils.random(Nest.SEEDS[0].length)], Utils.random(5));
			player.getInventory().addItemDrop(Nest.SEEDS[0][Utils.random(Nest.SEEDS[0].length)], Utils.random(3));
			player.getInventory().addItemDrop(Nest.SEEDS[1][Utils.random(Nest.SEEDS[1].length)], Utils.random(2));
			player.getPackets().sendGameMessage("You exchange 15 commendation points for a seed pack.");
		} else {
			for (int index = 0; index < SKILL_BASE_COMPONENTS.length; index++) {
				int skillComponent = SKILL_BASE_COMPONENTS[index];
				for (int i = 0; i < 6; i += 2) {
					if (skillComponent + i == componentId)
						addXPForSkill(player, SKILLS[index], getRateForIndex(i / 2));
				}
			}

			for (int index = 0; index < VOID_BASE_COMPONENTS.length; index++) {
				if (VOID_BASE_COMPONENTS[index] == componentId)
					addVoidItem(player, index);
			}

			for (int index = 0; index < CHARM_BASE_COMPONENTS.length; index++) {
				int charmComponent = CHARM_BASE_COMPONENTS[index];
				for (int i = 0; i < 6; i += 2) {
					if (charmComponent + i == componentId)
						addCharm(player, CHARMS[index], getRateForIndex(i / 2));
				}
			}
		}
	}

	private static void addCharm(Player player, int itemId, int rate) {
		if (rate == 100)
			rate = player.getInventory().getFreeSlots();
		for (int i = 0; i < rate; i++) {
			if (!exchangeCommendation(player, 2)) {
				rate = i;
				break;
			}
		}
		if (rate == 0)
			return;
		player.getInventory().addItemDrop(itemId, rate);
		player.getPackets().sendGameMessage("You exchange " + rate * 2 + " Commendations for a charm.");
	}

	private static int getRateForIndex(int index) {
		if (index == 0)
			return RATE_ONE;
		else if (index == 1)
			return RATE_TEN;
		else if (index == 2)
			return RATE_HUNDRED;
		return 0;
	}

	private static void addXPForSkill(Player player, int skill, int rate) {
		if (player.getSkills().getLevelForXp(skill) < 25) {
			player.getPackets().sendGameMessage("You need a " + Skills.SKILL_NAME[skill] + " of at least 25 in order to gain experience.");
			return;
		}
		if (player.isIronman() || player.isUltimateIronman() || player.isHCIronman()) {
			player.getPackets().sendGameMessage("You can't use this feature as an ironman.");
			return;
		}
		for (int i = 0; i < rate; i++) {
			if (!exchangeCommendation(player, 1)) {
				rate = i;
				break;
			}
		}
		double experience = calculateExperience(player, skill) * rate * (player.isUltimateIronman() ? 3 : (Settings.XP_RATE * (player.isFast() || player.isSuperFast() ? Settings.FAST_MODE_MULTIPLIER : 1)));
		player.getSkills().addXp(skill, experience, true);
		player.getDialogueManager().startDialogue("SimpleMessage", "You gain " + Utils.getFormattedNumber((int) experience) + " experience in " + Skills.SKILL_NAME[skill] + ".");
	}

	private static void addVoidItem(Player player, int index) {
		if (!player.getSkills().hasRequiriments(Skills.ATTACK, 42, Skills.STRENGTH, 42, Skills.DEFENCE, 42, Skills.HITPOINTS, 42, Skills.RANGE, 42, Skills.MAGIC, 42, Skills.PRAYER, 22)) {
			player.getPackets().sendGameMessage("You need an attack, strength, defence, constitution, range, and magic level of 42, and a prayer level of 22 in order to purchase void equipment.");
			return;
		}
		int cost = VOID_POINTS_COST[index];
		if (!exchangeCommendation(player, cost))
			return;
		int voidItem = VOID[index];
		player.getInventory().addItemDrop(voidItem, 1);
		player.getDialogueManager().startDialogue("ItemMessage", "You exchange " + cost + " commendation points for a " + ItemConfig.forID(voidItem).name.toLowerCase() + ".", voidItem);
		player.getCollectionLog().add(CategoryType.MINIGAMES, "Pest Control", new Item(voidItem));
	}

	private static double calculateExperience(Player player, int skill) {
		int level = player.getSkills().getLevelForXp(skill);
		int constant = 35;
		if (skill == Skills.MAGIC || skill == Skills.RANGE)
			constant = 32;
		else if (skill == Skills.PRAYER)
			constant = 18;
		return (Math.ceil(((level + 25) * (level - 24)) / 606) * constant) + constant;
	}
}
