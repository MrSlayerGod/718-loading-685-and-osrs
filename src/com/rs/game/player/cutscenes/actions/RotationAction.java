package com.rs.game.player.cutscenes.actions;

import com.rs.game.player.Player;

public class RotationAction extends CutsceneAction {
	
	private int x, y;
	
	public RotationAction(int x, int y, int actionDelay) {
		super(-1, actionDelay);
		this.x = x;
		this.y = y;
	}

	@Override
	public void process(Player player, Object[] cache) {
		player.getPackets().sendCameraRotation(x, y);
	}
}
