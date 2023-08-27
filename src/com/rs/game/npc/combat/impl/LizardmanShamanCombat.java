package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.NewForceMovement;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.others.LizardmanSpawn;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * 
 * @author Alex
 *
 */
public class LizardmanShamanCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 26766, 28565  };
	}

	@Override
	public int attack(NPC npc, final Entity target) {
		NPCCombatDefinitions def = npc.getCombatDefinitions();
		int attackStyle = Utils.random(5); //skip jump for now
		if (attackStyle == 0 && Utils.random(2) == 0)
			attackStyle = 4;
		if (attackStyle == 4 && !Utils.isOnRange(npc, target, 0)
				|| (attackStyle == 1 && !World.isTileFree(target.getPlane(), target.getX(), target.getY(), npc.getSize())))
			attackStyle = 3;
		switch (attackStyle) {
		case 4: //melee
			npc.setNextAnimation(new Animation(def.getAttackEmote()));
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, def.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
			break;
		case 3: //range
			npc.setNextAnimation(new Animation(27193));
			World.sendProjectile(npc, target, 6291, 105, 16, 20, 45, 16, 0);
			delayHit(npc, 2, target, getRangeHit(npc, getRandomMaxHit(npc, def.getMaxHit(), NPCCombatDefinitions.RANGE, target)));
			if (Utils.random(3) == 0) 
				target.getPoison().makePoisoned(100);
			break;
		case 2: //magic splash
			npc.setNextAnimation(new Animation(27193));
			WorldTile tile = new WorldTile(target);
			int msDelay = World.sendProjectile(npc, tile, 6293, 105, 16, 20, 45, 16, 0);
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					if (npc.isDead() || npc.hasFinished())
						return;
					for (Entity t : World.getNearbyPlayers(npc, true)) {
						if (t.hasWalkSteps() || Utils.getDistance(tile.getX(), tile.getY(), t.getX(), t.getY()) > 1)
							continue;
						delayHit(npc, 0, t, new Hit(npc, Utils.random(300)+1, HitLook.POISON_DAMAGE));
					
					}
					World.sendGraphics(npc, new Graphics(6294, 30 , 0), tile);
				}
			}, CombatScript.getDelay(msDelay));
			break;
		case 1: //jump
			npc.setNextAnimation(new Animation(27192));
			WorldTile to = new WorldTile(target);
			npc.setNextForceMovement(new NewForceMovement(new WorldTile(npc), 0, to, 2, Utils.getAngle(to.getX() - npc.getX(), to.getY() - npc.getY())));
			npc.setNextWorldTile(to);
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					if (npc.isDead() || npc.hasFinished())
						return;
					for (Entity t : npc.getPossibleTargets()) {
						if (/*t.hasWalkSteps() ||*/ !Utils.isOnRange(npc, target, 0))
							continue;
						t.applyHit(new Hit(npc, Utils.random(510)+1, HitLook.REGULAR_DAMAGE));
					}
				}
			}, 1);
			break;
		case 0: //spawns
			npc.setNextAnimation(new Animation(27193));
			NPC[] spawns = new NPC[5];
			int count = 0;
			int[][] dirs = Utils.getCoordOffsetsNear(4);
			for (int dir = 0; dir < dirs[0].length; dir++) {
				WorldTile tile2 = new WorldTile(new WorldTile(npc.getX() + dirs[0][dir], npc.getY() + dirs[1][dir], npc.getPlane()));
				if (World.isTileFree(tile2.getPlane(), tile2.getX(), tile2.getY(), 2)) {
					spawns[count] = new LizardmanSpawn(tile2, target);
					spawns[count++].setNextGraphics(new Graphics(6290));
				}
				if (count == spawns.length)
					break;
			}
			break; 
		}
		return def.getAttackDelay();
	}
}
