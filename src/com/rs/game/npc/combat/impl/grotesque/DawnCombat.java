/**
 * 
 */
package com.rs.game.npc.combat.impl.grotesque;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.grotesque.Dawn;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Mar 20, 2018
 */
public class DawnCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 27852 };
	}

	@Override
	public int attack(NPC npc, Entity target) {// yoa
		Dawn dawn = (Dawn) npc;
		if (dawn.phase2())
			return 0;
		final NPCCombatDefinitions def = npc.getCombatDefinitions();
		if (dawn.sphere())
			return def.getAttackDelay();
		int attackStyle = Utils.random(Utils.isOnRange(npc, target, 0) ? 4 : 3);
		switch (attackStyle) {
		case 3: //melee
			npc.setNextAnimation(new Animation(def.getAttackEmote()));
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, def.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
			break;
		case 2:
		case 1://range
			npc.setNextAnimation(new Animation(27770));
			World.sendProjectile(npc.transform(1, 2, 0), target, 6444, 74, 16, 30, 45, 16, 64);
			World.sendProjectile(npc.transform(1, 0, 0), target, 6444, 74, 16, 30, 65, 16, 64);
			delayHit(npc, 2, target, getRangeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.RANGE, target)));
			delayHit(npc, 3, target, getRangeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.RANGE, target)));
			break;
		case 0: //magic
			npc.setNextAnimation(new Animation(27771));
			WorldTile tile = new WorldTile(target);
			int msDelay = World.sendProjectile(npc, tile, 6445, 50, 26, 20, 36, 16, 96);
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					if (npc.isDead() || npc.hasFinished() || !dawn.isRunning())
						return;
					for (Entity t : npc.getPossibleTargets()) {
						if (t.hasWalkSteps() || Utils.getDistance(tile.getX(), tile.getY(), t.getX(), t.getY()) > 1)
							continue;
						delayHit(npc, 0, t, getRegularHit(npc, Utils.random(150)+1));
						t.addFreezeDelay(3000, true);
					}
					World.sendGraphics(npc, new Graphics(6454, 30 , 0), tile);
				}
			}, CombatScript.getDelay(msDelay));
			break;
		}
		return def.getAttackDelay();
	}

}
