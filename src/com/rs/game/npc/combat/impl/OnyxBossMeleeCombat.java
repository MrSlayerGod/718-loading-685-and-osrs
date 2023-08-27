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
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class OnyxBossMeleeCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 15186 };
	}

	@Override
	public int attack(final NPC npc, final Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		final OnyxBoss oracle = (OnyxBoss) npc;
		if (Utils.random(10) <= 8) {
			if (Utils.randomDouble() <= 0.30)
				npc.setNextForceTalk(new ForceTalk(OnyxBoss.TEXTS[Utils.random(OnyxBoss.TEXTS.length)]));
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, oracle.calculateMaxHit(npc.getMaxHit()), NPCCombatDefinitions.MELEE, target)));
		} else {
			// special att
			int attack = Utils.random(oracle.getMinnions().size() <= 0 && Utils.random(2) == 0 ? 2 : 1);
			if (attack == 0) {
				npc.setNextForceTalk(new ForceTalk("RAWRRRRR!!!!!!!!"));
				npc.setNextAnimation(new Animation(defs.getDefenceEmote()));
				for (final Entity trg : npc.getPossibleTargets(true, true)) {
					if (trg == npc)
						continue;
					if (trg instanceof Player) {
						final Player ptrg = (Player) trg;
						ptrg.getPackets().sendCameraShake(3, 12, 25, 12, 25);
						WorldTasksManager.schedule(new WorldTask() {
							@Override
							public void run() {
								ptrg.getPackets().sendStopCameraShake();
							}
						}, 7);
					}
					delayHit(npc, 7, trg, getMeleeHit(npc, getRandomMaxHit(npc, oracle.calculateMaxHit(npc.getMaxHit()), NPCCombatDefinitions.MELEE, trg)));
				}
				return 7;
			} else if (attack == 1) {
				if ( target instanceof Player && 
						((Player)target).getControlerManager().getControler() instanceof TheHorde )
				return 0;
				npc.setNextForceTalk(new ForceTalk("Face my mirrage!"));
				NPC mirrage = World.spawnNPC(15204, oracle.transform(5, 5, 0), -1, true, true);
				mirrage.setName("Onyx's mirrage");
				mirrage.setForceTargetDistance(64);
				mirrage.setForceAgressive(true);
				mirrage.setForceMultiArea(true);
				mirrage.setForceMultiAttacked(true);
				mirrage.setIntelligentRouteFinder(true);
				mirrage.getCombat().setTarget(target);
				oracle.registerMinnion(mirrage);
				
				mirrage = World.spawnNPC(15204, oracle.transform(-5, 5, 0), -1, true, true);
				mirrage.setName("Onyx's mirrage");
				mirrage.setForceTargetDistance(64);
				mirrage.setForceAgressive(true);
				mirrage.setForceMultiArea(true);
				mirrage.setForceMultiAttacked(true);
				mirrage.setIntelligentRouteFinder(true);
				mirrage.getCombat().setTarget(target);
				oracle.registerMinnion(mirrage);
			}
		}

		return oracle.calculateAttackSpeed(defs.getAttackDelay());
	}
}
