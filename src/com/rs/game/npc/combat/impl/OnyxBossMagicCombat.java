package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.worldboss.OnyxBoss;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.TheHorde;
import com.rs.utils.Utils;

public class OnyxBossMagicCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 15185 };
	}

	@Override
	public int attack(final NPC npc, final Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		final OnyxBoss oracle = (OnyxBoss) npc;
		if (Utils.random(10) <= 7) {
			if (Utils.randomDouble() <= 0.30)
				npc.setNextForceTalk(new ForceTalk(OnyxBoss.TEXTS[Utils.random(OnyxBoss.TEXTS.length)]));
			// standart att
			int distanceX = target.getX() - npc.getX();
			int distanceY = target.getY() - npc.getY();
			int size = npc.getSize();
			int attackStyle = Utils.random(8);
			if (attackStyle == 0 && (distanceX > size || distanceX < -1 || distanceY > size || distanceY < -1))
				attackStyle = 1;
			switch (attackStyle) {
			case 0:
				npc.setNextAnimation(new Animation(defs.getAttackEmote()));
				delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, oracle.calculateMaxHit(npc.getMaxHit() * 2), NPCCombatDefinitions.MELEE, target)));
				break;
			default:
				npc.setNextAnimation(new Animation(16122));
				for (final Entity trg : npc.getPossibleTargets(true, true)) {
					if (trg == npc)
						continue;
					World.sendProjectile(npc, trg, 2991, 34, 16, 30, 35, 16, 0);
					delayHit(npc, 2, trg, getMagicHit(npc, getRandomMaxHit(npc, oracle.calculateMaxHit(trg == target ? npc.getMaxHit() : (npc.getMaxHit() / 2)), NPCCombatDefinitions.MAGE, target)));
				}
				break;
			}
		} else {
			// special att
			int attack = Utils.random(oracle.getMinnions().size() == 0 && Utils.random(3) == 0 ? 3 : 2);
			if (attack == 0) {
				String name = "";
				if (target instanceof Player)
					name = ((Player) target).getDisplayName();
				else
					name = ((NPC) target).getName();
				npc.setNextAnimation(new Animation(16122));
				target.setNextGraphics(new Graphics(2929));
				npc.setNextForceTalk(new ForceTalk("Unlucky day for " + name + "! How sad! Hahhahaha!"));
				delayHit(npc, 2, target, getMagicHit(npc, getRandomMaxHit(npc, oracle.calculateMaxHit(npc.getMaxHit() * 2), NPCCombatDefinitions.MAGE, target)));
				return 8;
			} else if (attack == 1) {
				npc.setNextForceTalk(new ForceTalk("I can control your mind..."));
				npc.setNextAnimation(new Animation(defs.getAttackEmote()));
				for (final Entity trg : npc.getPossibleTargets(true, true)) {
					if (trg == npc || (trg != target && Utils.random(2) != 1))
						continue;
					if (trg instanceof Player) {
						((Player) trg).setNextAnimation(new Animation(14869));
						((Player) trg).lock(7);
					} else if (((NPC) trg).getSize() <= 1)
						((NPC) trg).setNextAnimation(new Animation(14869));
					trg.addFrozenBlockedDelay(5000);

					delayHit(npc, 7, trg, getMagicHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MAGE, target)));
				}
				return 8;
			} else if (attack == 2) {
				if ( target instanceof Player && 
							((Player)target).getControlerManager().getControler() instanceof TheHorde )
					return 0;
				if (oracle.getPhase() == 2) {
					npc.setNextForceTalk(new ForceTalk("Face my dogs!"));
					NPC mirrage = World.spawnNPC(15208, oracle.transform(5, 5, 0), -1, true, true);
					mirrage.setName("Onyx's Dog");
					mirrage.setForceTargetDistance(64);
					mirrage.setForceAgressive(true);
					mirrage.setForceMultiArea(true);
					mirrage.setForceMultiAttacked(true);
					mirrage.getCombat().setTarget(target);
					oracle.registerMinnion(mirrage);
					mirrage = World.spawnNPC(15208, oracle.transform(-5, 5, 0), -1, true, true);
					mirrage.setName("Onyx's Dog");
					mirrage.setForceTargetDistance(64);
					mirrage.setForceAgressive(true);
					mirrage.setForceMultiArea(true);
					mirrage.setForceMultiAttacked(true);
					mirrage.getCombat().setTarget(target);
					oracle.registerMinnion(mirrage);
				} else {
					npc.setNextAnimation(new Animation(defs.getAttackEmote()));
					npc.setNextForceTalk(new ForceTalk("Face my strongest dog!"));
					NPC jad = World.spawnNPC(8133, oracle.transform(5, 2, 0), -1, true, true);
					jad.setName("Onyx's Beast");
					jad.setForceTargetDistance(64);
					jad.setForceMultiArea(true);
					jad.setForceMultiAttacked(true);
					jad.getCombat().setTarget(target);
					oracle.registerMinnion(jad);
				}
			}

		}

		return oracle.calculateAttackSpeed(defs.getAttackDelay());
	}
}
