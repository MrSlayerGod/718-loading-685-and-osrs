package com.rs.game.player.dialogues.impl;

import com.rs.game.player.Skills;
import com.rs.game.player.content.dungeoneering.DungeonRewardShop;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.Utils;

public class DungExperiencePurchase extends Dialogue {
	
	private int amount;

	@Override
	public void start() {
		amount = (int) this.parameters[0];
		sendOptionsDialogue("Would you like to purchase "+amount+" experience?", "Yes.", "No thanks.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			if (componentId == OPTION_1) {
				if (player.getDungManager().getTokens() < amount) {
					stage = 0;
					sendDialogue("You don't have enough tokens. The maximum experience you could buy is "+player.getDungManager().getTokens()+".");
					return;
				}
				player.getSkills().addXp(Skills.DUNGEONEERING, amount, true);
				player.getDungManager().addTokens(-amount);
				sendDialogue("You purchased "+Utils.getFormattedNumber(amount)+" Dungeoneering experience.");
				stage = 0;
			} else
				end();
		} else if (stage == 0)
			end();
	}

	@Override
	public void finish() {
		DungeonRewardShop.refreshPoints(player);
	}
}
