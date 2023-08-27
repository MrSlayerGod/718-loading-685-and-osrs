package com.rs.game.player.actions;

import com.rs.game.Animation;
import com.rs.game.player.Player;
import com.rs.game.player.content.construction.HouseConstants;

public class SitRoundTable  extends Action {

	@Override
	public boolean start(Player player) {
		return true;
	}

	@Override
	public boolean process(Player player) {
		player.setNextAnimation(new Animation(HouseConstants.THRONE_EMOTES[player.getDonator()])); //chair
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		return 0;
	}

	@Override
	public void stop(Player player) {
		
	}
	
}
