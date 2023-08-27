package com.rs.game.player.dialogues.impl;

import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.ReferralSystem;

/**
 * 
 * @author Alex (Dragonkk)
 * Jan 13, 2019
 */
public class ReferralD extends Dialogue {

	@Override
	public void start() {
		//if (ReferralSystem.isNewPlayer(player))
		sendOptionsDialogue("How did you hear about us?", ReferralSystem.REFS);
		//else {
		//	end();
		//	player.getPackets().sendGameMessage("You are no longer a new player or already submmited your referral in past.");
		//}
	}
	@Override
	public void run(int interfaceId, int componentId) {
		player.getTemporaryAttributtes().put(Key.REFERRAL_NAME, Boolean.TRUE);
		player.getTemporaryAttributtes().put(Key.REFERRAL_TYPE, new Integer(componentId));
		player.getPackets().sendInputNameScript("Name?");
		end();
		
	}
	@Override
	public void finish() {
		
	}
	
}
