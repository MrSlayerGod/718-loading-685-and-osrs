package com.rs.game.player.dialogues.impl;

import com.rs.game.player.dialogues.Dialogue;

public class UpgradeMode extends Dialogue {

	@Override
	public void start() {
		if (player.isDungeoneer() || player.isNormal())
			return;
		sendDialogue("Warning! Upgrading game mode can't be reversed. Are you sure you wish to continue?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			stage = 0;
			sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Yes.", "No.");
		} else if (stage == 0) {
			if (componentId == OPTION_1) {
				if (player.isIronman() || player.isDeadman() || player.isExpert())
					player.setNormal();
				/*else if (player.isNormal())
					player.setFast();*/
				else if (player.isUltimateIronman() || player.isHCIronman())
					player.setIronman();
				/*else if (player.isFast())
					player.setSuperFast();*/
				player.getPackets().sendGameMessage("You are now playing on "+player.getGameMode().toLowerCase()+" mode.");
			}
			end();
		}
	}

	@Override
	public void finish() {

	}

}
