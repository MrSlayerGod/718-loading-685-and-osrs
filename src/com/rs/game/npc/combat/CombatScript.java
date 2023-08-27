package com.rs.game.npc.combat;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.npc.Drops;
import com.rs.game.npc.NPC;
import com.rs.game.npc.familiar.Steeltitan;
import com.rs.game.player.CombatDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.PlayerCombat;
import com.rs.game.player.content.Combat;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public abstract class CombatScript {

    public static final CombatScript DO_NOTHING = new CombatScript() {
		@Override
		public Object[] getKeys() { return new Object[0]; }
		@Override
		public int attack(NPC npc, Entity target) { return 1; }
	};

    /*
	 * Returns ids and names
	 */
	public abstract Object[] getKeys();

	/*
	 * Returns Move Delay
	 */
	public abstract int attack(NPC npc, Entity target);

	
	public static int getDelay(int msTime) {
		int cycle = msTime / 600;
		/*if ((msTime - (cycle * 600)) > 300)
			cycle++;*/
		return cycle;
	}
	
	public static int delayHitMS(NPC npc, int msTime, final Entity target, final Hit... hits) {
		int cycle = getDelay(msTime);
		/*int d = (msTime - (cycle * 600)) / 10;
		if(d > 30) 
			cycle++;
		else
			for(Hit hit : hits) 
				hit.setDelay(d);*/
		delayHit(npc, cycle, target, hits);
		return cycle;
	}
	
	public static void delayHit(NPC npc, int delay, final Entity target, final Hit... hits) {
		npc.getCombat().addAttackedByDelay(target);
		/*if (target instanceof Player && ((Player)target).getLastTarget() == null) 
			((Player)target).setLastTarget(npc);*/
		if (delay == -1) 
			hit(target, hits);
		else {
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					hit(target, hits);
				}

			}, delay);
		}
	}
	
	public static void hit(final Entity target, final Hit... hits) {
		for (Hit hit : hits) {
			NPC npc = (NPC) hit.getSource();
			if (npc.isDead() || npc.hasFinished() || target.isDead() || target.hasFinished() || target.isTeleporting())
				return;
			target.applyHit(hit);
			if (hit.getLook() == HitLook.HEALED_DAMAGE)
				return;
			npc.getCombat().doDefenceEmote(target, hit);
			if (target instanceof Player) {
				Player p2 = (Player) target;

				if(!p2.getInterfaceManager().containsInterface(Drops.DROP_INTERFACE_ID))
					p2.closeInterfaces();
				if (!npc.isCantSetTargetAutoRelatio() && p2.getCombatDefinitions().isAutoRelatie() && !p2.getActionManager().hasAction() && !p2.hasWalkSteps() && !p2.isLocked() && !p2.getEmotesManager().isDoingEmote()
						&& !p2.hasRouteEvent())
					p2.getActionManager().setAction(new PlayerCombat(npc));
			} else {
				NPC n = (NPC) target;
				if (!n.isUnderCombat() || n.canBeAttackedByAutoRelatie())
					n.setTarget(npc);
			}

		}
	}

	public static Hit getRangeHit(NPC npc, int damage) {
		if (npc.getDifficultyMultiplier() != 0 && damage * npc.getDifficultyMultiplier() < 990) 
			damage *= npc.getDifficultyMultiplier();
		return new Hit(npc, damage, HitLook.RANGE_DAMAGE);
	}

	public static Hit getMagicHit(NPC npc, int damage) {
		return new Hit(npc, damage, HitLook.MAGIC_DAMAGE);
	}

	public static Hit getRegularHit(NPC npc, int damage) {
		return new Hit(npc, damage, HitLook.REGULAR_DAMAGE);
	}

	public static Hit getMeleeHit(NPC npc, int damage) {
		return new Hit(npc, damage, HitLook.MELEE_DAMAGE);
	}

	public static int getRandomMaxHit(NPC npc, int maxHit, int attackStyle, Entity target) {
		if (npc.getDifficultyMultiplier() != 0) 
			maxHit *= npc.getDifficultyMultiplier();
		
		if (attackStyle == NPCCombatDefinitions.RANGE_FOLLOW)
			attackStyle = NPCCombatDefinitions.RANGE;
		else if (attackStyle == NPCCombatDefinitions.MAGE_FOLLOW)
			attackStyle = NPCCombatDefinitions.MAGE;
		double[] bonuses = npc.getBonuses();
		double att = bonuses == null ? 0 : attackStyle == NPCCombatDefinitions.RANGE ? bonuses[CombatDefinitions.RANGE_ATTACK] : attackStyle == NPCCombatDefinitions.MAGE ? bonuses[CombatDefinitions.MAGIC_ATTACK] : bonuses[CombatDefinitions.STAB_ATTACK];
		double def;
		if (target instanceof Player) {
			Player p2 = (Player) target;
			def = (p2.getSkills().getLevel(Skills.DEFENCE) / 2) + p2.getCombatDefinitions().getBonuses()[attackStyle == NPCCombatDefinitions.RANGE ? CombatDefinitions.RANGE_DEF : attackStyle == NPCCombatDefinitions.MAGE ? CombatDefinitions.MAGIC_DEF : CombatDefinitions.STAB_DEF];
			def *= p2.getPrayer().getDefenceMultiplier();
			if (attackStyle == NPCCombatDefinitions.MELEE) {
				if (p2.getFamiliar() instanceof Steeltitan)
					def *= 1.15;
			}
		} else {
			NPC n = (NPC) target;
			def = n.getBonuses() == null ? 0 : n.getBonuses()[attackStyle == NPCCombatDefinitions.RANGE ? CombatDefinitions.RANGE_DEF : attackStyle == NPCCombatDefinitions.MAGE ? CombatDefinitions.MAGIC_DEF : CombatDefinitions.STAB_DEF];
		}
		if (!Combat.rollHit(att, def))
			return 0;
		/*
		 * double att = bonuses == null ? 0 : attackStyle ==
		 * NPCCombatDefinitions.RANGE ? bonuses[CombatDefinitions.RANGE_ATTACK]
		 * : attackStyle == NPCCombatDefinitions.MAGE ?
		 * bonuses[CombatDefinitions.MAGIC_ATTACK] :
		 * bonuses[CombatDefinitions.STAB_ATTACK]; double def; if (target
		 * instanceof Player) { Player p2 = (Player) target; def =
		 * p2.getSkills().getLevel(Skills.DEFENCE) + (2
		 * p2.getCombatDefinitions().getBonuses()[attackStyle ==
		 * NPCCombatDefinitions.RANGE ? CombatDefinitions.RANGE_DEF :
		 * attackStyle == NPCCombatDefinitions.MAGE ?
		 * CombatDefinitions.MAGIC_DEF : CombatDefinitions.STAB_DEF]); def *=
		 * p2.getPrayer().getDefenceMultiplier(); if (attackStyle ==
		 * NPCCombatDefinitions.MELEE) { if (p2.getFamiliar() instanceof
		 * Steeltitan) def *= 1.15; } } else { NPC n = (NPC) target; def =
		 * n.getBonuses() == null ? 0 : n.getBonuses()[attackStyle ==
		 * NPCCombatDefinitions.RANGE ? CombatDefinitions.RANGE_DEF :
		 * attackStyle == NPCCombatDefinitions.MAGE ?
		 * CombatDefinitions.MAGIC_DEF : CombatDefinitions.STAB_DEF]; def *= 2;
		 * } double prob = att / def; if (prob > 0.90) // max, 90% prob hit so
		 * even lvl 138 can miss at lvl 3 prob = 0.90; else if (prob < 0.05) //
		 * minimun 5% so even lvl 3 can hit lvl 138 prob = 0.05; if (prob <
		 * Math.random()) return 0;
		 */
		return Utils.random(maxHit+1);
	}
}
