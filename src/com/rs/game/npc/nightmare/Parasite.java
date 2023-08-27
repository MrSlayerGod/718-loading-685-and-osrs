/**
 * 
 */
package com.rs.game.npc.nightmare;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Oct 31, 2017
 */
@SuppressWarnings("serial")
public class Parasite extends NPC {
	
	private TheNightmare boss;
	private int timer = 0;
	
	public Parasite(boolean big, TheNightmare boss, WorldTile tile) {
		super(big ? 29452 : 29453, tile, -1, true, true);
		this.boss = boss;
		setRandomWalk(0);
		setForceMultiArea(true);
		setIntelligentRouteFinder(true);
		setCantSetTargetAutoRelatio(true);
	//	setNextGraphics(new Graphics(169));
		faceEntity(boss);
	}
	
	
	@Override
	public void processNPC() {
		if (isDead() || boss == null)
			return;
		timer++;
		if (!boss.isAwaken() || boss.isDead() || boss.hasFinished()) {
			finish();
			return;
		}
		if (!Utils.isOnRange(this, boss, 2))
			calcFollow(boss, true);
		else
			resetWalkSteps();
		faceEntity(boss);
		if (timer % 4 == 2) {
			anim(28554);
			World.sendProjectile(this, boss, 6774, 15, 20, 41, 25, 15, 0);
		}else if (timer % 4 == 0) {
			if (boss.hasShield())
				boss.applyHit(new Hit(this, Utils.random(800)+1, HitLook.HEALED_DAMAGE));
			else
				boss.healTotems();
		}
	}
		
	
	@Override
	public void setTarget(Entity entity) {
		
	}
	
}
