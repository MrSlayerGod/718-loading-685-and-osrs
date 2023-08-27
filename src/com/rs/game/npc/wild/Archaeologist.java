/**
 * 
 */
package com.rs.game.npc.wild;

import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;

/**
 * @author dragonkk(Alex)
 * Oct 31, 2017
 */
@SuppressWarnings("serial")
public class Archaeologist extends NPC {
	
	public Archaeologist(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		if (id != 26618)
			setIntelligentRouteFinder(true);
		else
			setDropRateFactor(3);
	}
	
	
	@Override
	public void sendDeath(Entity source) {
		setNextForceTalk(new ForceTalk(getId() == 26618 ? "Ow!" : "Oh!"));
		super.sendDeath(source);
	}
	
}
