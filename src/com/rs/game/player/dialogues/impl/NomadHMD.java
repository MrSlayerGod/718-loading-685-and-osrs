package com.rs.game.player.dialogues.impl;

import java.util.Arrays;

import com.rs.discord.Bot;
import com.rs.game.player.content.NPCKillLog;
import com.rs.game.player.controllers.NomadsRequiem;
import com.rs.game.player.dialogues.Dialogue;

/**
 * 
 * @author Dragonkk (alex_dkk@hotmail.com)
 * Jun 12, 2021
 */
public class NomadHMD extends Dialogue {

	/* (non-Javadoc)
	 * @see com.rs.game.player.dialogues.Dialogue#start()
	 */
	@Override
	public void start() {
		sendOptionsDialogue("Choose a difficulty mode", "Normal", "Hard");
	}


	@Override
	public void run(int interfaceId, int componentId) {
		end();
		if (componentId == OPTION_2 && NPCKillLog.getKilled(player, "Nomad") < 100 && !player.isYoutuber() && player.getRights() != 2) {
			player.getPackets().sendGameMessage("You need to kill 100 Nomad's (Normal Mode) in order to enable hard mode!");
			return;
		}
		NomadsRequiem.enterNomadsRequiem(player, componentId == OPTION_2);
		
	}


	@Override
	public void finish() {
		
	}

}
