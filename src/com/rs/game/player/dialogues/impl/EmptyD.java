/**
 * 
 */
package com.rs.game.player.dialogues.impl;

import java.util.Arrays;

import com.rs.discord.Bot;
import com.rs.game.player.dialogues.Dialogue;

/**
 * @author dragonkk(Alex)
 * Oct 1, 2017
 */
public class EmptyD extends Dialogue {

	/* (non-Javadoc)
	 * @see com.rs.game.player.dialogues.Dialogue#start()
	 */
	@Override
	public void start() {
		sendOptionsDialogue("Empty inventory?", "Yes.", "No.");
	}

	/* (non-Javadoc)
	 * @see com.rs.game.player.dialogues.Dialogue#run(int, int)
	 */
	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1 && componentId == OPTION_1) {
			Bot.sendLog(Bot.COMMAND_CHANNEL, "[type=COMMAND][name="+player.getUsername()+"]" + "[message=::empty items are as follows:"
					+ Arrays.toString(player.getInventory().getItems().getItems()).replace("null,", "") + "]");
			player.getInventory().reset();
		}
		end();
		
	}

	/* (non-Javadoc)
	 * @see com.rs.game.player.dialogues.Dialogue#finish()
	 */
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
	}

}
