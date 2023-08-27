package com.rs.game.player.dialogues.impl;

import com.rs.game.WorldTile;
import com.rs.game.player.Skills;
import com.rs.game.player.controllers.RunespanControler;
import com.rs.game.player.dialogues.Dialogue;

public class RunespanPortalD extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue("Where would you like to travel to?", player.getRegionId() == 6741 ? "The Wizard's Tower" : "The Runecrafting Guild", "Low level entrance into the Runespan", "High level entrance into the Runespan");
		stage = 1;
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == 1) {
			if (componentId == OPTION_1) {
				if (player.getSkills().getLevelForXp(Skills.RUNECRAFTING) < 50) {
					player.getDialogueManager().startDialogue("SimpleNPCMessage", 1263, "Come back once you are level 50 runecrafting.");
					return;
				}
				player.useStairs(-1, player.getRegionId() == 6741 ? new WorldTile(3107, 3160, 1) : new WorldTile(1696, 5460, 2), 0, 2);
				//player.getPackets().sendGameMessage("That option isn't yet working.", true);
				end();
			} else {
				RunespanControler.enterRunespan(player, componentId == OPTION_3);
				end();
			}
		}

	}

	@Override
	public void finish() {

	}

}
