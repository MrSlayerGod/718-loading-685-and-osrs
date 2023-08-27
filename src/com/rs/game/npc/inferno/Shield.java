/**
 * 
 */
package com.rs.game.npc.inferno;

import com.rs.game.Entity;
import com.rs.game.ForceMovement;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.controllers.Inferno;

/**
 * @author dragonkk(Alex)
 * Nov 26, 2017
 */
@SuppressWarnings("serial")
public class Shield extends NPC {

	private Inferno inferno;

	public Shield(Inferno inferno, WorldTile tile) {
		super(27707, tile, -1, true, true);
		setCantSetTargetAutoRelatio(true);
		setCantFollowUnderCombat(true);
		setForceMultiArea(true);
		delay = 6;
		this.inferno = inferno;
	}
	
	private static WorldTile[] TILES = new WorldTile[] {new WorldTile(2257, 5297, 0), new WorldTile(2283, 5297, 0)};
	
	private int current;
	private int delay;
	
	@Override
	public void processNPC() {
		super.processNPC();
		if (inferno == null || !inferno.isRunning())
			return;
		if (delay > 0) {
			delay--;
			return;
		}
		WorldTile to = inferno.getMap().getTile(TILES[current]);
		if (to.getX() == getX()) {
			current = current == 0 ? 1 : 0;
			delay = 4;
		} else {
			WorldTile nextTile = transform(current == 0 ? -1 : 1, 0, 0);
			this.setNextForceMovement(new ForceMovement(new WorldTile(this), 0, nextTile, 1, ForceMovement.SOUTH));
			this.setNextWorldTile(nextTile);
		}
	}
	
	@Override
	public void setNextFaceEntity(Entity target) {
		
	}
	
	@Override
	public void setTarget(Entity target) {
		
	}
}
