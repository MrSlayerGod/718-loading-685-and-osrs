package com.rs.game.npc.combat.impl.superiorslayer;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.content.Slayer;
import com.rs.utils.Utils;

public class NuclearSmokeDevil extends CombatScript {

	public static final int ID = 27406;
	@Override
	public Object[] getKeys() {
		return new Object[]
				{ ID };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		NPCCombatDefinitions def = npc.getCombatDefinitions();
		if (!Slayer.hasMask(target)) {
			Player targetPlayer = (Player) target;
			int randomSkill = Utils.random(0, 6);
			int currentLevel = targetPlayer.getSkills().getLevel(randomSkill);
			targetPlayer.getSkills().set(randomSkill, currentLevel < 5 ? 0 : currentLevel - Utils.random(20));
			targetPlayer.getPackets().sendGameMessage("The smoke devil's smoke suffocates you.");
			delayHit(npc, 1, target, getRangeHit(npc, targetPlayer.getMaxHitpoints() / 4));
		}
		else
			delayHit(npc, 1, target, getRangeHit(npc, getRandomMaxHit(npc, def.getMaxHit(), def.getAttackStyle(), target)));
		World.sendProjectile(npc, target, def.getAttackProjectile(), npc.getId() == 20499 ? 50 : 30, 20, 40, 35, 16, 64);		
		npc.setNextAnimation(new Animation(def.getAttackEmote()));
		return def.getAttackDelay();
	}

}
