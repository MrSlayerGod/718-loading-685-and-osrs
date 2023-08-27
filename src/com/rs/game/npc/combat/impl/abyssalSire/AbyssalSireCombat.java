package com.rs.game.npc.combat.impl.abyssalSire;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.abyssalNexus.AbyssalSire;
import com.rs.game.npc.abyssalNexus.AbyssalSpawn;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class AbyssalSireCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ AbyssalSire.SLEEPING_ID, AbyssalSire.PHASE1_ID, AbyssalSire.PHASE2_ID, AbyssalSire.PHASE3_ID, AbyssalSire.PHASE4_ID  };
	}

	private void spawnNPC(NPC npc, Entity target, boolean phase1) {
		AbyssalSire sire = (AbyssalSire) npc;
		if (sire.getSpawnCount() > 30)
			return;
		sire.increaseSpawnCount();
		npc.setNextAnimation(new Animation(phase1 ? 24530 : 27095));
		World.sendProjectile(npc, target, 6274, 30, 20, 30, 35, 16, 74);
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				if (npc.hasFinished() || !target.withinDistance(npc, 16))
					return;
				AbyssalSpawn spawn = new AbyssalSpawn((AbyssalSire) npc, target);
				spawn.setTarget(target);
			}
			
		}, 2);
	}
	
	private void spawnFume(NPC npc, Entity target, int phase) {
		if (phase < 3)
			npc.setNextAnimation(new Animation(phase == 1 ? 24531  : 25367));
		WorldTile tile = new WorldTile(target);
		World.sendGraphics(npc, new Graphics(6275), tile);
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				if (npc.hasFinished())
					return;
				if (target.getX() == tile.getX() && target.getY() == tile.getY()) {
					target.applyHit(new Hit(npc, Utils.random(100), HitLook.REGULAR_DAMAGE));
					target.getPoison().makePoisoned(80);
				}
			}
			
		}, 3);
	}
	
	@Override
	public int attack(NPC npc, Entity target) {
		AbyssalSire sire = (AbyssalSire) npc;
		switch (npc.getId()) {
		case AbyssalSire.SLEEPING_ID:
			if (target instanceof Familiar)
				target = ((Familiar)target).getOwner();
			if (target instanceof Player)
				sire.startFight((Player) target);
			break;
		case AbyssalSire.PHASE1_ID:
			if (sire.phase2())
				break;
			if (sire.stun())
				break;
			if (Utils.random(3) == 0) 
				spawnNPC(npc, target, true);
			else
				spawnFume(npc, target, 1);
			break;
		case AbyssalSire.PHASE2_ID:
			if (sire.phase3())
				break;
			int attack = Utils.random(4);
			switch (attack) {
			case 0:
				npc.setNextAnimation(new Animation(25366));
				delayHit(npc, 1, target, getMeleeHit(npc, getRandomMaxHit(npc, 100, NPCCombatDefinitions.MELEE, target)));
				break;
			case 1:
				npc.setNextAnimation(new Animation(25369));
				delayHit(npc, 1, target, getMeleeHit(npc, getRandomMaxHit(npc, 200, NPCCombatDefinitions.MELEE, target)));
				break;
			case 2:
				npc.setNextAnimation(new Animation(25369));
				delayHit(npc, 1, target, getMeleeHit(npc, getRandomMaxHit(npc, 450, NPCCombatDefinitions.MELEE, target)));
				break;
			case 3:
				if (Utils.random(3) == 0) 
					spawnNPC(npc, target, false);
				else
					spawnFume(npc, target, 2);
				break;
			}
			break;
		case AbyssalSire.PHASE3_ID:
			if (sire.phase4())
				break;
			spawnFume(npc, target, 3);
			break;
		case AbyssalSire.PHASE4_ID:
			if (sire.getSpawnCount() > 15)
				break;
			sire.increaseSpawnCount();
			AbyssalSpawn spawn = new AbyssalSpawn(sire, sire.transform(Utils.random(2), Utils.random(2), 0));
			spawn.setTarget(target);
			break;
		}
		
		return 7;
	}

}
