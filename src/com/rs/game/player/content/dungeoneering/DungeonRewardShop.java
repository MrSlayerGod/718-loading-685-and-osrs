package com.rs.game.player.content.dungeoneering;

import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.GeneralRequirementMap;
import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;

public class DungeonRewardShop {

	public static final int REWARD_SHOP = 940;

	public static void openRewardShop(final Player player) {
		player.getInterfaceManager().sendInterface(REWARD_SHOP);
		player.getPackets().sendUnlockIComponentOptionSlots(REWARD_SHOP, 2, 0, 205, 0, 1, 2);
		refreshPoints(player);
		player.setCloseInterfacesEvent(new Runnable() {

			@Override
			public void run() {
				player.getTemporaryAttributtes().remove(Key.DUNGEON_REWARD_SLOT);
			}
		});
	}

	public static void purchase(Player player) {
		if (!canPurchase(player))// Client hax
			return;
		removeConfirmationPurchase(player);
		int slot = (int) player.getTemporaryAttributtes().get(Key.DUNGEON_REWARD_SLOT);
		if (slot == 1004) {
			player.getPackets().sendInputIntegerScript("How much experience would you like to buy?");
			player.getTemporaryAttributtes().put(Key.PURCHASE_TOKEN_AMOUNT, true);
			return;
		}
		GeneralRequirementMap map = GeneralRequirementMap.getMap(slot);
		player.getInventory().addItemDrop(map.getIntValue(1070), 1);
		player.getDungManager().addTokens(-map.getIntValue(1072));
		refreshPoints(player);
	}

	public static void sendConfirmationPurchase(Player player) {
		if (!canPurchase(player))
			return;
		player.getPackets().sendHideIComponent(REWARD_SHOP, 42, false);
	}

	public static void removeConfirmationPurchase(Player player) {
		player.getPackets().sendHideIComponent(REWARD_SHOP, 42, true);
	}

	public static void select(Player player, int slot) {
		player.getTemporaryAttributtes().put(Key.DUNGEON_REWARD_SLOT, ClientScriptMap.getMap(3015).getValue(slot / 5));
	}

	private static boolean canPurchase(Player player) {
		if (player.getTemporaryAttributtes().get(Key.DUNGEON_REWARD_SLOT) == null)
			return false;
		int slot = (int) player.getTemporaryAttributtes().get(Key.DUNGEON_REWARD_SLOT);
		boolean purchaseEnabled = true;
		GeneralRequirementMap map = GeneralRequirementMap.getMap(slot);
		int skill = map.getIntValue(1073), levelRequirement = map.getIntValue(1071), price = map.getIntValue(1072), dungeoneeringLevel = map.getIntValue(1074);
		if (player.getSkills().getLevel(skill) < levelRequirement || player.getSkills().getLevel(Skills.DUNGEONEERING) < dungeoneeringLevel || player.getDungManager().getTokens() < price)
			purchaseEnabled = false;
	
		int id = map.getIntValue(1070);
		if ((id == 18839 && player.isRigourUnlocked()) || id == 18343 || (id == 18344 && player.isAuguryUnlocked())) {
			player.getPackets().sendGameMessage("You already unlocked this prayer!");
			return false;
		}
		
		if (purchaseEnabled)
			return true;
		String message = map.getStringValue(1078);
		message = message.substring(0, message.indexOf(".") + 1);
		player.getPackets().sendGameMessage(message);
		player.getPackets().sendGameMessage("You do not meet the requirements to purchase this item.");
		return false;
	}

	public static void refreshPoints(Player player) {
		player.getPackets().sendIComponentText(REWARD_SHOP, 31, "" + player.getDungManager().getTokens());
	}
}
