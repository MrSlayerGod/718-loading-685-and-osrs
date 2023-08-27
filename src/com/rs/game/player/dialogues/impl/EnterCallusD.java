package com.rs.game.player.dialogues.impl;

import com.rs.game.WorldObject;
import com.rs.game.npc.worldboss.CallusFrostborne;
import com.rs.game.player.dialogues.Dialogue;

public class EnterCallusD extends Dialogue {

	WorldObject object;

	@Override
	public void start() {
		object = (WorldObject) this.parameters[0];
		player.faceObject(object);
		sendDialogue("It looks like you may be able to get through the gate<br>" +
						    "with a large amount of force.<br><br>" +
							"You won't be able to escape, continue?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if(++stage == 0) {
			sendOptionsDialogue("Continue?", "Yes.", "No.");
		} else {
			if (componentId == OPTION_1)
				CallusFrostborne.enterCallus(player, object);
			end();
		}
	}

	@Override
	public void finish() {

	}
}
