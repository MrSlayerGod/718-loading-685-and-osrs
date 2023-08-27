/**
 * 
 */
package com.rs.game.npc.combat.impl.wild;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Oct 31, 2017
 */
public class CrazyArchaeologistCombat extends CombatScript {


	@Override
	public Object[] getKeys() {
		return new Object[] {26618};
	}

	private static final String[] ATTACKS = new String[] {"Rain of knowledge!", "You belong in a museum!", "Taste my knowledge!", "These ruins are mine!", "No-one messes with Bellock's dig!", "Get off my site!", "I'm Bellock - respect me!"};

	
	private void doExplosion(NPC npc, int count, WorldTile from) {
		for (int i = 0; i < count; i++) {
			final WorldTile newTile = new WorldTile(from, 1);
			if (!World.isTileFree(newTile.getPlane(), newTile.getX(), newTile.getY(), 1))
				continue;
			World.sendProjectile(npc, count == 2 ? from : npc, newTile, 6260, 41, 30, 15, 0, 30, 0);
			boolean send = count == 3 && i == 0;
			WorldTasksManager.schedule(new WorldTask() {
				
				boolean send2;
				@Override
				public void run() {
					if (send2) {
						doExplosion(npc, 2, newTile);
						stop();
					} else {
						for (Entity t : World.getNearbyPlayers(npc, false)) {
							if (t.hasWalkSteps() || Utils.getDistance(newTile.getX(), newTile.getY(), t.getX(), t.getY()) > 1 || !t.clipedProjectile(newTile, false))
								continue;
							delayHit(npc, 0, t, getRegularHit(npc, Utils.random(240)+1));
						}
						World.sendGraphics(npc, new Graphics(5157, 30 , 0), newTile);
						if (send) 
							send2 = true;
						else
							stop();
					}
				}
			}, count == 3 ? (true || Utils.getDistance(npc, from) > 3 ? 2 : 1) : 0, 1);
		}
	}
	
	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int attack = Utils.random(ATTACKS.length);
		npc.setNextForceTalk(new ForceTalk(ATTACKS[attack]));
		if (attack == 0) {
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			doExplosion(npc, 3, new WorldTile(target));
			return 8;
		} else if (attack != 1 && Utils.isOnRange(npc, target, 0) && Utils.random(2) == 0) {
			npc.setNextAnimation(new Animation(423));
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
		} else {
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			delayHit(npc, 1, target, getRangeHit(npc, getRandomMaxHit(npc, attack == 1 ? 240 : npc.getMaxHit(), defs.getAttackStyle(), target)));
			World.sendProjectile(npc, target, defs.getAttackProjectile(), 41, 30, 41, 40, 16, 0);
			if (attack == 1) //special attack1
				target.setNextGraphics(new Graphics(5305, 75, 100));
		}
		return defs.getAttackDelay();
	}

}
