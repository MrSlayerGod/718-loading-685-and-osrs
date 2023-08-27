/**
 * 
 */
package com.rs.game.npc.others;

import com.rs.game.minigames.LavaFlowMine;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Mar 8, 2018
 */

public class LiquidGoldNymph extends NPC {

	private static final long serialVersionUID = -5786364612304984152L;
	
	private Player player;
	private long time;
	private boolean interacting;

	public LiquidGoldNymph(Player player) {
		super(14, LavaFlowMine.getNymphTile(player), -1, false);
		this.player = player;
		time = Utils.currentTimeMillis() + 300000;
		setNextFaceWorldTile(player);
		player.getTemporaryAttributtes().put("LiquidGoldNymph", Boolean.TRUE);
	}
	
	@Override
	public void finish() {
		super.finish();
		player.getTemporaryAttributtes().remove("LiquidGoldNymph");
	}
	
	@Override
	public void processNPC() {
		if(player == null || player.hasFinished() || !player.hasStarted() || player.isDead() || !player.getMapRegionsIds().contains(getRegionId()))
			finish();
		if(time < Utils.currentTimeMillis() && !interacting)
			finish();
		else if(time < Utils.currentTimeMillis() && interacting)
			time = Utils.currentTimeMillis() + 60000;
	}
	
	@Override
	public boolean withinDistance(Player tile, int distance) {
		return tile == player && super.withinDistance(tile, distance);
	}
	
	public void setInteracting(boolean interacting) {
		this.interacting = interacting;
	}
	
	public Player getPlayer() {
		return player;
	}

}
