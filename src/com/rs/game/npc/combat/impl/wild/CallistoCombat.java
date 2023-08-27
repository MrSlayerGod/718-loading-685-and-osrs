/**
 * 
 */
package com.rs.game.npc.combat.impl.wild;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.NewForceMovement;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex) Oct 31, 2017
 */
public class CallistoCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 26503 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		if (Utils.random(3) == 0)
			target.getPoison().makePoisoned(80);
		int attack = Utils.random(18); //12
		if (attack == 0 || (!Utils.isOnRange(npc, target, 0) && Utils.random(4) == 0)) {// special attack, done
			if (target instanceof Player) {
				((Player) target).getPackets()
						.sendGameMessage("Callisto's fury sends an almighty shockwave through you!", true);
				 ((Player) target).lock(2);
				 ((Player) target).stopAll();
			}
			delayHit(npc, 1, target, getRegularHit(npc, Utils.random(600) + 1));
			World.sendProjectile(npc, target, 5159, 35, 20, 41, 25, 5, 140);
			target.setNextGraphics(new Graphics(80, 5, 60));
		}
		if (!Utils.isOnRange(npc, target, 0))
			return defs.getAttackDelay();
		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		if (attack == 1) {
			if (target instanceof Player) {
				 ((Player) target).getPackets().sendGameMessage("Callisto's roar throws you backwards.", true);
				 ((Player) target).lock(2);
				 ((Player) target).stopAll();
				 target.setNextAnimation(new Animation(10070));
				WorldTile nextTile = Utils.getFreeTile(target, 1);
				target.setNextForceMovement(new NewForceMovement(new WorldTile(target), 0, nextTile, 1, Utils.getAngle(target.getX() - nextTile.getX(), target.getY() - nextTile.getY())));
				target.setNextWorldTile(nextTile);
			}
		}else if (attack == 2) { //done
			if (target instanceof Player)
				 ((Player) target).getPackets().sendGameMessage("Callisto absorbs his next attack, healing himself a bit.", true);
			npc.setNextGraphics(new Graphics(5157, 0, 100));
			npc.heal(50);
		}
		delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
		return defs.getAttackDelay();
	}

}
