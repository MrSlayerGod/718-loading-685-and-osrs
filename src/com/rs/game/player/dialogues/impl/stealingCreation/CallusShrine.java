package com.rs.game.player.dialogues.impl.stealingCreation;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.minigames.WorldBosses;
import com.rs.game.npc.worldboss.CallusFrostborne;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.Utils;

public class CallusShrine extends Dialogue {

	private int coins;
	private int itemSlot = 0;

	Item donateItem = null;

	@Override
	public void start() {

		if(WorldBosses.isBossAlive()) {
			this.sendDialogue("The words \"" +  WorldBosses.getBoss().getName() + "\" appear on the shrine..");
			return;
		}
		int req = CallusFrostborne.getRemainingCoinsToSpawn();

		itemSlot = (Integer) parameters[0];

		if(itemSlot == 101) {
			this.sendDialogue("<col=00ffff><shad=0>The shrine seeks <col=ffff00>" + Utils.getFormattedNumber(req) + "<col=00ffff> more gold as a sacrifice.");
			return;
		}

		donateItem = itemSlot == 100 ? donateItem = new Item(995, player.getInventory().getAmountOf(995)) : player.getInventory().getItem(itemSlot).clone();

		int itemval = GrandExchange.getPrice(donateItem.getId());

		if(itemval > CallusFrostborne.CALLUS_SPAWN_VALUE * 5) {
			this.sendDialogue("This offering is far too great.");
			return;
		}

		if(donateItem.getAmount() > 1) {
			int amtReq = (req / itemval);
			if(itemval * amtReq < req)
				amtReq++;
			if(donateItem.getAmount() > amtReq)
				donateItem.setAmount(amtReq);
		}
		if(donateItem.getAmount() == 100_000_001)
			donateItem.setAmount(100_000_000);

		if(itemSlot == -1)
			coins = donateItem.getAmount();
		else
			coins = itemval * donateItem.getAmount();

		if(donateItem == null || !ItemConstants.isTradeable(donateItem)
				|| donateItem.getName().startsWith("Lucky ")) {
			this.sendDialogue("This item is not a suitable offering.");
			return;
		}

		String val = donateItem.getId() == 995 ? "" : " (Value: " + Utils.getFormattedNumber(coins)+")";
		sendOptionsDialogue("Offer to the shrine: <br>" + donateItem.getName() + " x " + Utils.getFormattedNumber(donateItem.getAmount()) +"" + val, "Yes.", "No.");
		stage = 1;
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == 1 && componentId == OPTION_1 && player.getInventory().containsItem(donateItem.getId(), donateItem.getAmount())) {
			player.setNextAnimation(new Animation(833));
			player.getInventory().deleteItem(donateItem.getId(), donateItem.getAmount());
			player.getPackets().sendGameMessage("<col=ffff00>The shrine collects your offering worth " + Utils.getFormattedNumber(coins) + " gold.");
			CallusFrostborne.addCallusShrineCoins(coins);
			player.addCallusSpawnDonations(coins);
			player.sendMessage("You have now sacrificed " + Utils.getFormattedNumber(player.getCallusSpawnDonations()) + " gold to Callus.");
		}
		end();
	}

	@Override
	public void finish() {

	}

}
