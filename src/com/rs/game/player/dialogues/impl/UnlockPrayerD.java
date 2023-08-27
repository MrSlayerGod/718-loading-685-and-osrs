package com.rs.game.player.dialogues.impl;

import com.rs.game.WorldObject;
import com.rs.game.player.Player;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.ChambersRewards;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.Colour;

public class UnlockPrayerD extends Dialogue {

	int prayerScroll;
	String prayer;

	@Override
	public void start() {
		prayerScroll = (int) this.parameters[0];
		if(prayerScroll == ChambersRewards.DEX && player.isRigourUnlocked()) {
			sendItemDialogue(prayerScroll, "You can make out some faded words on the ancient parchment. It's an archaic invocation of the gods. However there's nothing more for you to learn.");
			stage = -1;
		} else {
			sendItemDialogue(prayerScroll, "You can make out some faded words on the ancient parchment. It's an archaic invocation of the gods. Would you like to absorb its power?");
			stage++;
		}
		prayer = prayerScroll == ChambersRewards.DEX ? "Rigour" : prayerScroll == ChambersRewards.ARCANE ? "Augury" : "Preserve";
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if(!player.getInventory().containsItem(prayerScroll, 1)) {
			end();
			return;
		}

		if(stage == -1) {
			end();
		} else if(stage == 0) {
			sendOptionsDialogue("Scroll will be consumed!", "Learn to " + prayer, "Cancel");
			stage = 1;
		} else if(stage == 1) {
			if(componentId != OPTION_1) {
				end();
				return;
			}
			player.getInventory().deleteItem(prayerScroll, 1);
			switch (prayer) {
				case "Rigour":
					player.setRigourUnlocked(true);
					break;
				case "Augury":
					player.setAuguryUnlocked(true);
					break;
				case "Preserve":
					player.setPreserveUnlocked(true);
					break;
			}
			sendItemDialogue(prayerScroll, "You study the scroll and learn a new Prayer: " + Colour.DARK_RED.wrap(prayer));
			stage = 2;
		} else {
			end();
		}
	}

	@Override
	public void finish() {

	}
}
