package com.rs.game.npc.nightmare;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;

public class Sleepwalker extends NPC  {

	public Sleepwalker(int id, WorldTile tile) {
		super(id, tile, -1, true, true);
		this.setCantSetTargetAutoRelatio(false);
		setForceMultiArea(true);
		setIntelligentRouteFinder(true);
		setRandomWalk(0);
	}
	
	@Override
	public void handleIngoingHit(Hit hit) {
		hit.setDamage(100);
	}
	
	@Override
	public void setTarget(Entity entity) {
		
	}
	
	@Override
	public void faceEntity(Entity entity) {
		
	}

}
