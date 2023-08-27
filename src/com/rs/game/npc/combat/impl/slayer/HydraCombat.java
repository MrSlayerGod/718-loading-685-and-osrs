package com.rs.game.npc.combat.impl.slayer;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.slayer.Hydra;
import com.rs.utils.Utils;

public class HydraCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 28609 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		NPCCombatDefinitions def = npc.getCombatDefinitions();
		Hydra hydra = (Hydra) npc;
		if (hydra.useSpecial()) { //special
			npc.setNextAnimation(new Animation(def.getAttackEmote()+2));
			hydra.sendSplash(target, new WorldTile(target));
			hydra.sendSplash(target, new WorldTile(target).transform(-2 + Utils.random(5), -2 + Utils.random(5), 0));
			hydra.sendSplash(target, new WorldTile(target).transform(-2 + Utils.random(5), -2 + Utils.random(5), 0));
			return def.getAttackDelay();
		}
		if (hydra.useRange()) { //range
			npc.setNextAnimation(new Animation(def.getAttackEmote()+1));
			int msDelay = World.sendProjectile(npc, target, 6663, 35, 20, 40, 40, 16, 64);		
			delayHitMS(npc, msDelay, target, getRangeHit(npc, getRandomMaxHit(npc, def.getMaxHit(), NPCCombatDefinitions.RANGE, target)));
		} else { //magic
			npc.setNextAnimation(new Animation(def.getAttackEmote()));
			int msDelay = World.sendProjectile(npc, target, def.getAttackProjectile(), 35, 20, 40, 40, 16, 64);		
			delayHitMS(npc, msDelay, target, getMagicHit(npc, getRandomMaxHit(npc, def.getMaxHit(), def.getAttackStyle(), target)));

		}
		return def.getAttackDelay();
	}

}
