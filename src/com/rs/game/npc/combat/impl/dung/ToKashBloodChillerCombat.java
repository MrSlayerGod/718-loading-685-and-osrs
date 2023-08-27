package com.rs.game.npc.combat.impl.dung;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.dungeonnering.FrozenAdventurer;
import com.rs.game.npc.dungeonnering.ToKashBloodChiller;
import com.rs.game.player.Player;
import com.rs.game.player.content.dungeoneering.DungeonManager;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class ToKashBloodChillerCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 10024 };
	}

	@Override
	public int attack(final NPC npc, Entity target) {
		final ToKashBloodChiller boss = (ToKashBloodChiller) npc;
		final DungeonManager manager = boss.getManager();

		boolean perfectDamage = false;

		if (target instanceof Player) {
			Player player = (Player) target;
			if (player.getAppearence().isNPC())
				perfectDamage = true;
		}

		if (perfectDamage) {
			((Player) target).getAppearence().transformIntoNPC(-1);
			target.applyHit(new Hit(npc, (int) Utils.random(boss.getMaxHit() * .90, boss.getMaxHit()), HitLook.MAGIC_DAMAGE));
		}

		boolean special = boss.canSpecialAttack() && Utils.random(10) == 0;

		if (special) {
			npc.setNextForceTalk(new ForceTalk("Sleep now, in the bitter cold..."));
			npc.playSound(2896, 2);
			boss.setSpecialAttack(true);
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					npc.setNextForceTalk(new ForceTalk("DEEP FREEZE!"));
					npc.setNextAnimation(new Animation(14396));
					npc.setNextGraphics(new Graphics(2544));
					for (Entity t : boss.getPossibleTargets())
						setSpecialFreeze((Player) t, boss, manager);
				}
			}, 3);
			return 8;
		} else {
			boolean meleeAttack = perfectDamage || Utils.random(3) == 0;

			if (meleeAttack) {
				npc.setNextAnimation(new Animation(14392));
				delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
			} else {
				npc.setNextAnimation(new Animation(14398));
				World.sendProjectile(npc, target, 2546, 16, 16, 41, 30, 0, 0);
				delayHit(npc, 1, target, getMagicHit(npc, getRandomMaxHit(npc, (int) (npc.getMaxHit() * .80), NPCCombatDefinitions.MAGE, target)));
			}
			return meleeAttack ? 4 : 5;
		}
	}

	public static void setSpecialFreeze(final Player player, final ToKashBloodChiller boss, DungeonManager dungManager) {
		player.resetWalkSteps();
		player.stopAll();
		player.lock();
		player.setNextGraphics(new Graphics(2545));
		player.getAppearence().transformIntoNPC(10022);
		FrozenAdventurer npc = new FrozenAdventurer(10023, player, -1, false);
		npc.setPlayer(player);
		player.getPackets().sendGameMessage("You have been frozen solid!");
		WorldTasksManager.schedule(new WorldTask() {

			int counter = 0;

			@Override
			public void run() {
				boss.setSpecialAttack(false);
				for (Entity t : boss.getPossibleTargets()) {
					Player player = (Player) t;
					if (player.isLocked()) {
						counter++;
						player.getAppearence().transformIntoNPC(-1);
					}
				}
				if (counter == 0)
					return;
				boss.setNextForceTalk(new ForceTalk("I will shatter your soul!"));
				boss.setNextGraphics(new Graphics(2549, 5, 100));
			}
		}, 5 * dungManager.getParty().getTeam().size());
	}

	public static void removeSpecialFreeze(Player player) {
		player.unlock();
		player.getAppearence().transformIntoNPC(-1);
		player.setNextGraphics(new Graphics(2548));
		player.getPackets().sendGameMessage("The ice encasing you shatters violently.");
	}
}
