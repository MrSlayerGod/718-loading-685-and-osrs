/**
 * 
 */
package com.rs.game.npc.combat.impl.wild;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Oct 31, 2017
 */
public class VenenatisCombat extends CombatScript {


	@Override
	public Object[] getKeys() {
		return new Object[] {26504};
	}
	
	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		if (Utils.random(3) == 0) 
			target.getPoison().makePoisoned(80);
		int attack = Utils.random(12);
		if (attack == 0) {//special attack1
			if (target instanceof Player)
				((Player)target).getPackets().sendGameMessage("Venenatis hurls her web at you, sticking you to the ground!", true);
			delayHit(npc, 0, target, getRegularHit(npc, Utils.random(500)+1));
		}
		if (attack > 3 && Utils.isOnRange(npc, target, 0) && Utils.random(2) == 0) { //uses mage more often
			npc.setNextAnimation(new Animation(25327));
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
		} else if (attack == 1) {
			if (target instanceof Player) {
				Player player = ((Player)target);
				player.getPackets().sendGameMessage("Your prayer was drained!", true);
				player.getPrayer().drainPrayer(50);
			}
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			npc.setNextGraphics(new Graphics(170, 0, 100));
			target.setNextGraphics(new Graphics(172, 60, 100));
			World.sendProjectile(npc, target, 171, 35, 20, 41, 25, 5, 74);
		} else {
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			npc.setNextGraphics(new Graphics(defs.getAttackGfx(), 0, 100));
			int damage = getRandomMaxHit(npc, npc.getMaxHit(), defs.getAttackStyle(), target);
			delayHit(npc, 1, target, getMagicHit(npc, damage));
			World.sendProjectile(npc, target, defs.getAttackProjectile(), 35, 20, 41, 25, 5, 74);
			target.setNextGraphics(new Graphics(damage == 0 ? 85 : 5166, 60,  100));
		}
		return defs.getAttackDelay();
	}

}
