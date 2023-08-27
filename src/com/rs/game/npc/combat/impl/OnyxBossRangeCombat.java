package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.worldboss.OnyxBoss;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.TheHorde;
import com.rs.utils.Utils;

public class OnyxBossRangeCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 15184 };
	}

	@Override
	public int attack(final NPC npc, final Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		final OnyxBoss oracle = (OnyxBoss) npc;
		if (Utils.random(15) <= 13) {
			if (Utils.randomDouble() <= 0.30)
				npc.setNextForceTalk(new ForceTalk(OnyxBoss.TEXTS[Utils.random(OnyxBoss.TEXTS.length)]));
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			for (final Entity trg : npc.getPossibleTargets(true, true)) {
				if (trg == npc)
					continue;
				World.sendProjectile(npc, trg, 2985, 41, 16, 41, 35, 16, 0);
				delayHit(npc, 2, trg, getRangeHit(npc, getRandomMaxHit(npc, oracle.calculateMaxHit(trg == target ? npc.getMaxHit() : (npc.getMaxHit() / 2)), NPCCombatDefinitions.RANGE, trg)));
			}
		} else if (oracle.getMinnions().size() <= 0) {
			if ( target instanceof Player && 
					((Player)target).getControlerManager().getControler() instanceof TheHorde )
			return 0;
			npc.setNextForceTalk(new ForceTalk("Face my pets!"));
			NPC mirrage = World.spawnNPC(28030, oracle.transform(5, 5, 0), -1, true, true);
			mirrage.setName("Matrix's Pet");
			mirrage.setForceTargetDistance(64);
			mirrage.setForceAgressive(true);
			mirrage.setForceMultiArea(true);
			mirrage.setForceMultiAttacked(true);
			mirrage.getCombat().setTarget(target);
			oracle.registerMinnion(mirrage);
			mirrage = World.spawnNPC(28031, oracle.transform(-5, 5, 0), -1, true, true);
			mirrage.setName("Matrix's Pet");
			mirrage.setForceTargetDistance(64);
			mirrage.setForceAgressive(true);
			mirrage.setForceMultiArea(true);
			mirrage.setForceMultiAttacked(true);
			mirrage.getCombat().setTarget(target);
			oracle.registerMinnion(mirrage);
		}

		return oracle.calculateAttackSpeed(defs.getAttackDelay());
	}
}
