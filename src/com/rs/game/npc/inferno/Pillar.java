/**
 * 
 */
package com.rs.game.npc.inferno;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Nov 23, 2017
 */
@SuppressWarnings("serial")
public class Pillar extends NPC {


	private WorldObject object;
	
	public Pillar(WorldTile tile) {
		super(27709, tile.transform(-1, -1, 0), -1, true, true);
		setCantSetTargetAutoRelatio(true);
		setCantFollowUnderCombat(true);
		setForceMultiArea(true);
		spawnObject();
		World.getRegion(getRegionId(), true); // forceload
	}
	
	@Override
	public void setNextFaceEntity(Entity target) {
		
	}
	
	@Override
	public void setTarget(Entity target) {
		
	}
	
	private int getObjectID() {
		double perc = (double)getHitpoints() / (double)getMaxHitpoints();
		if (perc > 0.75)
			return 130284;
		if (perc > 0.5)
			return 130285;
		if (perc > 0.25)
			return 130286;
		return 130287;
	}
	
	private void spawnObject() {
		int id = getObjectID();
		if (object != null && object.getId() == id) 
			return;
		World.spawnObject(object = new WorldObject(id, 10, 0, getX()+1, getY()+1, 0));
	}
	
	@Override
	public void processNPC() {
		if (isDead())
			return;
		spawnObject();
		super.processNPC();
	}

	@Override
	public void sendDeath(Entity killer) {
		Region region = World.getRegion(getRegionId());
		if (region.getPlayerIndexes() != null) {
			for (Integer i : region.getPlayerIndexes()) {
				Player player = World.getPlayers().get(i);
				if (player != null && !player.hasFinished() && !player.isDead() && Utils.isOnRange(this, player, 0)) 
					player.applyHit(new Hit(this, 490, HitLook.REGULAR_DAMAGE));
			}
		}
		if (region.getNPCsIndexes() != null) {
			for (Integer i : region.getNPCsIndexes()) {
				NPC npc = World.getNPCs().get(i);
				if (npc != null && !npc.hasFinished() && !npc.isDead() && Utils.isOnRange(this, npc, 0)) 
					npc.applyHit(new Hit(this, 490, HitLook.REGULAR_DAMAGE));
			}
		}
		if (object != null)
			World.removeObject(object);
		setNextNPCTransformation(27710);
		super.sendDeath(killer);
	}
	@Override
	public void finish() {
		clip(false);
		super.finish();
	}
	public void clip(boolean clip) {
		Region region = World.getRegion(getRegionId());
		if (clip) {
			region.forceGetRegionMap().addObject(0, getXInRegion(), getYInRegion(), 3, 3, true, true);
			region.forceGetRegionMapClipedOnly().addObject(0, getXInRegion(), getYInRegion(), 3, 3, true, true);
		} else {
			region.forceGetRegionMap().removeObject(0, getXInRegion(), getYInRegion(), 3, 3, true, true);
			region.forceGetRegionMapClipedOnly().removeObject(0, getXInRegion(), getYInRegion(), 3, 3, true, true);
		}
	}
}
