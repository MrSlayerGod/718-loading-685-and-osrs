/**
 * 
 */
package com.rs.game.npc.wild;

import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;

/**
 * @author dragonkk(Alex)
 * Oct 31, 2017
 */
@SuppressWarnings("serial")
public class Callisto extends NPC {

	private static final int ID = 26503;
	
	public Callisto(WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(ID, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setDropRateFactor(3);
	}
	
	
	@Override
	public void handleIngoingHit(Hit hit) {
		if (hit.getLook() == HitLook.MAGIC_DAMAGE)
			hit.setDamage(0);
		super.handleIngoingHit(hit);
	}
	
}
