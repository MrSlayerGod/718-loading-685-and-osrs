package com.rs.game.player.dialogues.impl;

import com.rs.game.WorldTile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogues.Dialogue;

public class SlayerCommand extends Dialogue {

	@Override
	public void start() {
	//	if (player.isRubyDonator())
			sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Turael (No requirement)", "Vannaka (Lvl 40 CB)", "Chaeldar (Lvl 70 CB)", "Duradel (Lvl 100 CB + 50 Slayer)","Kuradal (Lvl 110 + 75 Slayer)");
	/*	else if (player.isEmeraldDonator())
			sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Turadel (No requirement)", "Vannaka (Lvl 40 CB)", "Chaeldar (Lvl 70 CB)", "Duradel (Lvl 100 CB + 50 Slayer)", "Cancel");
		else
			sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Turadel (No requirement)", "Vannaka (Lvl 40 CB)", "Chaeldar (Lvl 70 CB)", "Cancel");
	*/}

	@Override
	public void run(int interfaceId, int componentId) {
		if (componentId == OPTION_1) 
			Magic.sendCommandTeleportSpell(player, new WorldTile(2895, 3372, 0));
		else if (componentId == OPTION_2) 
			Magic.sendCommandTeleportSpell(player, new WorldTile(3145, 9913, 0));
		else if (componentId == OPTION_3) 
			Magic.sendCommandTeleportSpell(player, new WorldTile(2445, 4433, 0));
		else if (componentId == OPTION_4) {
			if (player.isSuperDonator())
				Magic.sendCommandTeleportSpell(player, new WorldTile(2869, 2982, 1));
			else
				player.getPackets().sendGameMessage("You need to be at emerald donator or higher to use this option.");
		}
		else if (componentId == OPTION_5) 
			if (player.isExtremeDonator())
				Magic.sendCommandTeleportSpell(player, new WorldTile(1740, 5312, 1));
			else
				player.getPackets().sendGameMessage("You need to be at ruby donator or higher to use this option.");
		end();
	}




	@Override
	public void finish() {

	}

}
