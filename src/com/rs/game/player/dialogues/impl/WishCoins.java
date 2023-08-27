package com.rs.game.player.dialogues.impl;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.Shop;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.content.pet.LuckyPets;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.Utils;

public class WishCoins extends Dialogue {

	private int coins;
	private int itemSlot = 0;
	Item donateItem = null;

	@Override
	public void start() {
		coins = player.getInventory().getAmountOf(995);
		itemSlot = (Integer) parameters[0];

		donateItem = player.getInventory().getItem(itemSlot).clone();

		if(donateItem == null || !ItemConstants.isTradeable(donateItem)
				|| donateItem.getName().startsWith("Lucky ")) {
			this.sendDialogue("This item has no market value.");
			return;
		}

		coins = GrandExchange.getPrice(donateItem.getId()) * donateItem.getAmount();

		if (coins < 1000000) {
			this.sendDialogue("You need to donate items worth at least 1.000,000 coins to activate the wishing well.");
			return;
		}

		String val = donateItem.getId() == 995 ? "" : " (Value: " + Utils.getFormattedNumber(coins)+")";
		sendOptionsDialogue("Do you wish donate <br>" + donateItem.getName() + " x " + Utils.getFormattedNumber(donateItem.getAmount()) +"" + val, "Yes.", "No.");
		stage = 1;
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == 1 && componentId == OPTION_1 && player.getInventory().containsItem(donateItem.getId(), donateItem.getAmount())) {
			player.setNextAnimation(new Animation(833));
			player.getInventory().deleteItem(donateItem.getId(), donateItem.getAmount());
			player.getPackets().sendGameMessage("<col=ffff00>You donate " + Utils.getFormattedNumber(coins) + " to the well!");
			World.addWishingWell(player, coins/200000 * 60000, false); //before 100000
			player.setThrownWishingCoins(player.getThrownWishingCoins() + coins);
			rollPet(coins);
		}
		end();

	}

	private void rollPet(int coins) {
		if(coins >= 100_000_000) {
			int roll = 1;
			while(coins >= 100000000) {
				if(!LuckyPets.checkPet(player, LuckyPets.LuckyPet.COINS)) {
					player.sendMessage("Wishing well " + Utils.getFormattedNumber(roll * 100_000_000) + " donation pet roll: <col=ffff00>Fail!");
				} else {
					player.sendMessage("Wishing well " + Utils.getFormattedNumber(roll * 100_000_000) + " donation pet roll: <col=00ff00>Success! Congratulations");
					player.getThrownWishingCoins();
				}
				roll++;
				coins -= 100_000_000;

			}
		} else {
			player.sendMessage("You must donate at least 100 million coins to have a chance at the Coins pet.");
		}
	}

	@Override
	public void finish() {

	}

}
