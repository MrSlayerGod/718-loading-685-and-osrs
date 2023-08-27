/**
 * 
 */
package com.rs.game.npc.others;

import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Feb 9, 2018
 */
public class LizardmanSpawn extends NPC {

	int life;
	
	private Entity target; 
	public LizardmanSpawn(WorldTile tile, Entity target) {
		super(26768, tile, -1, true, true);
		life = 10;
		setRandomWalk(0);
		setIntelligentRouteFinder(true);
		this.target = target;
	}
	
	@Override
	public void processNPC() {
		if (target == null)
			return;
		life--;
		if (!isDead() && life == 0) {
			World.sendGraphics(this, new Graphics(6295), new WorldTile(this));
			for (Entity entity : World.getNearbyPlayers(this, true)) {
				if (Utils.isOnRange(entity, this, 1))
					entity.applyHit(new Hit(this, Utils.random(50)+50, HitLook.REGULAR_DAMAGE));
			}
			finish();
		}else {
			resetWalkSteps();
			calcFollow(target, true);
		}
	}

}
