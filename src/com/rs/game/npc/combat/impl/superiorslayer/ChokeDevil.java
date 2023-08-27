package com.rs.game.npc.combat.impl.superiorslayer;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.Slayer;
import com.rs.utils.Utils;

public class ChokeDevil extends CombatScript {

	public static final int ID = 27404;
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
			if (randomSkill == Skills.HITPOINTS)
				randomSkill++;
			int currentLevel = targetPlayer.getSkills().getLevel(randomSkill);
			targetPlayer.getSkills().set(randomSkill, currentLevel < 5 ? 0 : currentLevel - Utils.random(20));
			targetPlayer.getPackets().sendGameMessage("The dust devil's smoke suffocates you.");
			delayHit(npc, 1, target, getMeleeHit(npc, targetPlayer.getMaxHitpoints() / 4));
		} else
			delayHit(npc, 1, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), def.getAttackStyle(), target)));
		npc.setNextAnimation(new Animation(def.getAttackEmote()));
		return def.getAttackDelay();
	}

}
