package com.rs.game.player.dialogues.impl;

import com.rs.game.player.controllers.partyroom.PartyRoom;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.Utils;

public class PartyRoomLever extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue(DEFAULT_OPTIONS_TITLE,
				"Balloon Bonanza ("+ Utils.getFormattedNumber(PartyRoom.BALLOON_PARTY_GP)+" coins).",
				"Knightly Dance ("+ Utils.getFormattedNumber(PartyRoom.KNIGHTLY_DANCE_GP)+" coins).", "No action.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (componentId == 11) {
			PartyRoom.purchase(player, true);
		} else if (componentId == 13) {
			PartyRoom.purchase(player, false);
		}
		end();
	}

	@Override
	public void finish() {}
}
