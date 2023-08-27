package com.rs.game.player.dialogues.impl;

import com.rs.game.player.dialogues.Dialogue;

public class LunarAltar extends Dialogue {

	@Override
	public void start() {
		/*if (!player.isDonator() && !player.hasVotedInLast24Hours()) {
			sendDialogue("You need donator rank or to vote once in order to be able to switch magic book.");
			stage = -2;
			return;
		}*/
		sendOptionsDialogue("Change spellbooks?", "Yes, replace my spellbook.", "Never mind.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -2) {
			end();
			return;
		}
		if (componentId == OPTION_1) {
			if (player.getCombatDefinitions().getSpellBook() != 430) {
				sendDialogue("Your mind clears and you switch", "back to the lunar spellbook.");
				player.getCombatDefinitions().setSpellBook(2);
			} else {
				sendDialogue("Your mind clears and you switch", "back to the normal spellbook.");
				player.getCombatDefinitions().setSpellBook(0);
			}
		} else
			end();
	}

	@Override
	public void finish() {

	}

}
