/**
 * 
 */
package com.rs.game.npc.combat.impl.wild;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.wild.Vetion;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Oct 31, 2017
 */
public class VetionCombat extends CombatScript {


	@Override
	public Object[] getKeys() {
		return new Object[] {26611, 26612};
	}


	private void doExplosion(NPC npc, int count, WorldTile from) {
		for (int i = 0; i < count; i++) {
			final WorldTile newTile = new WorldTile(from, 1);
			if (!World.isTileFree(newTile.getPlane(), newTile.getX(), newTile.getY(), 1))
				continue;
			World.sendProjectile(npc, count == 2 ? from : npc, newTile, 280, 41, 30, 15, 0, 30, 0);
			boolean send = false;//count == 3 && i == 0;
			WorldTasksManager.schedule(new WorldTask() {
				
				boolean send2;
				@Override
				public void run() {
					if (send2) {
						doExplosion(npc, 2, newTile);
						stop();
					} else {
						for (Entity t : npc.getPossibleTargets()) {
							if (t.hasWalkSteps() || Utils.getDistance(newTile.getX(), newTile.getY(), t.getX(), t.getY()) > 1 || !t.clipedProjectile(newTile, false))
								continue;
							delayHit(npc, 0, t, getRegularHit(npc, Utils.random(120)+1));
						}
						World.sendGraphics(npc, new Graphics(281, 30 , 0), newTile);
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
		Vetion vetion = (Vetion) npc;
		vetion.summonDogs(target);
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int attack = Utils.random(5);
		if (attack == 0 || !Utils.isOnRange(npc, target, 0)) {
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			doExplosion(npc, 3, new WorldTile(target));
			return 8;
		} else if (attack == 1) {
			if (target instanceof Player) 
				((Player)target).getPackets().sendGameMessage("Vet'ion pummels the groun sending a shattering earthquake shockwave through you.", true);
			npc.setNextAnimation(new Animation(25507));
			delayHit(npc, 1, target, getRegularHit(npc, Utils.random(450)+1));
		} else {
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), defs.getAttackStyle(), target)));
		}
		return defs.getAttackDelay();
	}

}
