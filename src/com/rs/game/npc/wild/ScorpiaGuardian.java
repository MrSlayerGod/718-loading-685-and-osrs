/**
 * 
 */
package com.rs.game.npc.wild;

import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Oct 31, 2017
 */
@SuppressWarnings("serial")
public class ScorpiaGuardian extends NPC {

	private static final int ID = 26617;
	
	private Scorpia scorpia;
	private int timer = 0;
	
	public ScorpiaGuardian(Scorpia scorpia, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(ID, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		this.scorpia = scorpia;
		setIntelligentRouteFinder(true);
		setNextGraphics(new Graphics(169));
		faceEntity(scorpia);
	}
	
	
	@Override
	public void processNPC() {
		if (isDead() || scorpia == null)
			return;
		timer++;
		if (timer == 25 || scorpia.hasFinished()) {
			finish();
			return;
		}
		if (!Utils.isOnRange(this, scorpia, 2))
			calcFollow(scorpia, true);
		else
			resetWalkSteps();
		if (timer % 4 == 0) {
			scorpia.heal(10);
			World.sendProjectile(this, scorpia, 168, 15, 20, 41, 25, 15, 0);
		}
	}
		
}
