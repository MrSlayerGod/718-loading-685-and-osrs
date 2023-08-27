package com.rs.game.npc.combat.impl.slayer;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.content.Slayer;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class BasiliskKnightCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 29293 };
	}

	@Override
	public int attack(NPC npc, final Entity target) {
		NPCCombatDefinitions def = npc.getCombatDefinitions();
		if (!Slayer.hasReflectiveEquipment(target)) {
			npc.setNextAnimation(new Animation(def.getAttackEmote()));
			Player targetPlayer = (Player) target;
			int randomSkill = Utils.random(0, 6);
			int currentLevel = targetPlayer.getSkills().getLevel(randomSkill);
			targetPlayer.getSkills().set(randomSkill, currentLevel < 5 ? 0 : currentLevel - 5);
			delayHit(npc, 0, target, getRegularHit(npc, targetPlayer.getMaxHitpoints() / 5));
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					target.setNextGraphics(new Graphics(747));
				}
			});
			// TODO player emote hands on ears
			return def.getAttackDelay();
		} 
		//6737 6738 special
		if (Utils.random(2) == 0 && Utils.isOnRange(npc, target, 0)) { //melee
			npc.setNextAnimation(new Animation(def.getAttackEmote()));
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, def.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
			return def.getAttackDelay();
		}
		npc.setNextAnimation(new Animation(28500));
		int msDelay = World.sendProjectile(npc, target, def.getAttackProjectile(), 25, 20, 40, 45, 16, 92);		
		delayHitMS(npc, msDelay, target, getMagicHit(npc, getRandomMaxHit(npc, def.getMaxHit(), NPCCombatDefinitions.MAGE, target)));
		target.setNextGraphics(new Graphics(6736, msDelay / 10, 100));
		return def.getAttackDelay();
	}
}
