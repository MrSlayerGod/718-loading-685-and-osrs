package com.rs.game.player.dialogues.impl;

import com.rs.game.player.Player;
import com.rs.game.player.dialogues.Dialogue;

public class OccultAltar extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue("Select an option", "Regular", "Ancient", "Lunar", "Cancel");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -2 || componentId == OPTION_4) {
			end();
			return;
		}
		stage = -2;
		/*if (!player.isDonator() && !player.hasVotedInLast24Hours()) {
			sendDialogue("You need donator rank or to vote once in order to be able to switch magic book.");
			return;
		}*/
		int option = componentId == OPTION_1 ? 0 : componentId == OPTION_2 ? 1 : 2;
		setSpellBook(player, option);
	}
	
	public static void setSpellBook(Player player, int option) {
		if (player.getCombatDefinitions().getSpellBookID() == option) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You already have this spellbook.");
		} else {
			player.getCombatDefinitions().setSpellBook(option);
			player.getDialogueManager().startDialogue("SimpleMessage", "Your mind clears and you switch", "your spellbook.");
		}
	}

	@Override
	public void finish() {

	}

}
