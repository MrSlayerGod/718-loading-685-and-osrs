package com.rs.game.player.dialogues.impl;

import com.rs.game.TemporaryAtributtes;
import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.SkillsDialogue.ItemNameFilter;
import com.rs.game.player.controllers.DungeonController;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.Utils;

public class WithdrawPouch extends Dialogue {


	@Override
	public void start() {
		this.sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Coins", "Platinum token (1000 Coins)");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if(player.tournamentResetRequired()) {
			player.sendMessage("You cannot access your money pouch within a PK Tournament.");
			return;
		}
		if (player.getControlerManager().getControler() instanceof DungeonController) {
			player.getPackets().sendGameMessage("You cannot access your money pouch within the walls of Daemonheim.");
			return;
		}
		if (!player.getBank().hasVerified(12))
			return;
		end();
		long coins = player.getMoneyPouch().getCoinsAmount();
		if (componentId == OPTION_1) 
			player.getPackets().sendInputIntegerScript("Your money pouch contains " + Utils.getFormattedNumber(coins) + " coins.<br>How many would you like to withdraw?");
		else
			player.getPackets().sendInputIntegerScript("Your money pouch contains " + Utils.getFormattedNumber(coins / 1000) + " platinum tokens.<br>How many would you like to withdraw?");
		player.getTemporaryAttributtes().put("withdrawingPouch", Boolean.TRUE);
		player.getTemporaryAttributtes().put(Key.WITHDRAW_PLATINUM_TOKEN, componentId == OPTION_1 ? Boolean.FALSE : Boolean.TRUE);
	}

	@Override
	public void finish() {

	}
}
