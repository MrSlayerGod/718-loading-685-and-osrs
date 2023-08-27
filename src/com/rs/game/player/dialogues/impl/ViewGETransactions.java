package com.rs.game.player.dialogues.impl;

import com.rs.game.player.dialogues.Dialogue;

public class ViewGETransactions extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "View Offers", "View Transactions", "Cancel");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == 0) {
			end();
			if (componentId != OPTION_3)
				player.getGeManager().openOffers(componentId == OPTION_1);
		} else {
			if (componentId == OPTION_1) {
				stage = 0;
				sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Buying Offers", "Selling Offers", "Cancel");
			//	player.getGeManager().openOffers();
			} else if (componentId == OPTION_2) { 
				end();
				player.getGeManager().openTransactions();
			} else
				end();
		}
	}


	@Override
	public void finish() {
		
	}


}
