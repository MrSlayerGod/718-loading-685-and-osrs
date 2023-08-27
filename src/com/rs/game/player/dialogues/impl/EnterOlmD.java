package com.rs.game.player.dialogues.impl;

import com.rs.game.Animation;
import com.rs.game.WorldObject;
import com.rs.game.npc.worldboss.CallusFrostborne;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.dialogues.Dialogue;

public class EnterOlmD extends Dialogue {

	WorldObject object;

	@Override
	public void start() {
		object = (WorldObject) this.parameters[0];
		player.faceObject(object);
		sendDialogue("<col=ff0000>If you are having graphical issues - please re-log before the fight.");
		stage = 0;
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if(stage == 0) {
			stage = 1;
			sendDialogue("This is a one-way passage to the Great Olm's chamber.<br><br>Are you sure you wish to go through?");
		} else if(stage == 1) {
			stage = 2;
			sendOptionsDialogue("Continue?", "Yes.", "No.");
		} else {
			if (componentId == OPTION_1) {
				ChambersOfXeric.enterOlmRoom(player);
			}
			end();
		}
	}

	@Override
	public void finish() {

	}
}
