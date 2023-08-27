package com.rs.game.npc.combat.impl.dragons;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.dragons.FrostDragon;
import com.rs.game.player.Player;
import com.rs.game.player.content.Combat;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class FrostDragonCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ "Frost dragon" };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		final FrostDragon dragon = (FrostDragon) npc;

		if (dragon.getAttackStage() > 9 && Utils.random(2) == 0) {
			dragon.setAttackStage(0);
			WorldTasksManager.schedule(new WorldTask() {

				int ticks = -1;

				@Override
				public void run() {
					ticks++;
					if (ticks == 2)
						dragon.setOrb(true);
					World.sendGraphics(dragon, new Graphics(2875, 0, 750), new WorldTile(dragon.getMiddleWorldTile()));
					//World.sendProjectile(dragon, dragon, new WorldTile(dragon), 2875, 50, 28, 15, ticks * 15, 0, 4);
					if (ticks == 8) {
						dragon.setOrb(false);
						stop();
						return;
					}
				}
			}, 0, 0);
		}
		dragon.setAttackStage(dragon.getAttackStage() + 1);

		if (Utils.random(5) == 0) { //regular dragonfire
			npc.setNextAnimation(new Animation(13155));
			World.sendProjectile(npc, target, 2464, 28, 28, 50, 41, 16, 0);
			delayHit(npc, 2, target, getRegularHit(npc, getBreathDamage(npc, target)));
		} else {
			boolean meleeAttack = Utils.random(2) == 0 && Utils.isOnRange(npc.getX(), npc.getY(), npc.getSize(), target.getX(), target.getY(), target.getSize(), 0);
			if (meleeAttack) {
				if (Utils.random(5) == 0) {//Fire breath swipe
					npc.setNextAnimation(new Animation(13152));
					npc.setNextGraphics(new Graphics(2465));
					delayHit(npc, 1, target, getRegularHit(npc, getBreathDamage(npc, target)));
				} else {
					npc.setNextAnimation(new Animation(13151));
					delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
				}
			} else {
				boolean isMagicOnly = dragon.isMagicOnly();
				npc.setNextAnimation(new Animation(13155));
				World.sendProjectile(npc, target, isMagicOnly ? 2705 : 16, 20, 28, 35, 50, 0, 1);
				if (isMagicOnly)
					delayHit(npc, 2, target, getMagicHit(npc, getRandomMaxHit(npc, npc.getMaxHit() + 12, NPCCombatDefinitions.MAGE, target)));
				else
					delayHit(npc, 2, target, getRangeHit(npc, getRandomMaxHit(npc, npc.getMaxHit() + 15, NPCCombatDefinitions.RANGE, target)));
			}
		}
		return defs.getAttackDelay();
	}

	private int getBreathDamage(NPC npc, Entity target) {
		int damage = getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MAGE, target) + Utils.random(200, 450);
		if (target instanceof Player) {
			final Player player = (Player) target;
			final boolean hasPrayerProtection = player.getPrayer().isMageProtecting();
			final boolean hasShieldProtection = Combat.hasAntiDragProtection(target);
			final boolean hasPotionProtection = player.getFireImmune() > Utils.currentTimeMillis();

			if (hasShieldProtection) {
				damage *= hasPotionProtection ? 0 : 0.1;
			} else if (hasPotionProtection)
				damage *= player.isSuperAntiFire() ? 0 : 0.1;
			else if (hasPrayerProtection)
				damage *= 0.1;

			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					String message = "You've been horribly burned by the dragon's breath.";
					if (hasPrayerProtection)
						message = "Your prayers offer some protection from the dragon's breath";
					else if (hasShieldProtection)
						message = "Your shield offers some protection from the dragon's breath.";
					else if (hasPotionProtection)
						message = "Your " + (player.isSuperAntiFire() ? "super " : "") + "anti-fire potion offers some protection from the dragon's breath.";
					player.getPackets().sendGameMessage(message);
				}
			}, 1);
		}
		return damage;
	}

}
