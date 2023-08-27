package com.rs.game.player.dialogues.impl;

import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.MTopDonator;
import com.rs.utils.TopCox;
import com.rs.utils.TopDonator;

public class TopCoxD extends Dialogue {

	int currentlyViewing;
	int next, prev;
	boolean osrs;

	@Override
	public void start() {
		currentlyViewing = (int) parameters[0];
		osrs = (boolean) parameters[1];


		next = currentlyViewing + 1;
		prev = currentlyViewing - 1;

		if(next > 99)
			next = 1;
		if(prev < 1)
			prev = 1;

		if(currentlyViewing == -1) {
			sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "<col=ff0000>OSRS</col> mode Chambers of Xeric scores", "<col=80ff80>MATRIX</col> mode Chambers of Xeric scores");
		} else {
			sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, format(osrs ? 0 : 1, next), format(osrs ? 0 : 1, prev));
		}
	}

	String format(int mode, int size) {
		return "View Top " + (size == 1 ? "Solo" : size + "-man ") + " "+ (mode == 0 ? "OSRS" : "Matrix") +" raids";
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if(currentlyViewing == -1) {
			if (componentId == OPTION_1) {
				TopCox.showRanks(player, 0, 1);
				osrs = true;
			} else if (componentId == OPTION_2) {
				TopCox.showRanks(player, 1, 1);
				osrs = false;
			}
			currentlyViewing = 1;
			player.getDialogueManager().startDialogue("TopCoxD", currentlyViewing, osrs);
		} else {
			end();
			if (componentId == OPTION_1) {
				TopCox.showRanks(player, osrs ? 0 : 1, next);
				currentlyViewing = next;
			} else if (componentId == OPTION_2) {
				TopCox.showRanks(player, osrs ? 0 : 1, prev);
				currentlyViewing = prev;
			}
			player.getDialogueManager().startDialogue("TopCoxD", currentlyViewing, osrs);
		}

	}

	@Override
	public void finish() {
		
	}
}
