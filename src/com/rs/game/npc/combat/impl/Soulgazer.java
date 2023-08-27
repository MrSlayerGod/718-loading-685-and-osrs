package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.dungeonnering.DungeonSlayerNPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.dungeoneering.DungeonManager;

public class Soulgazer extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 10705 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		if(!(npc instanceof DungeonSlayerNPC)) {
			// error from console
			System.err.println("Error: " + npc.getId() + " (Soulgazer) not classified as DungeonSlayerNPC");
			return -1;
		}
		DungeonSlayerNPC dungeonNPC = (DungeonSlayerNPC) npc;

		DungeonManager manager = dungeonNPC.getManager();

		if (manager.isDestroyed())
			return -1;

		npc.setNextAnimation(new Animation(13779));

		for (Player player : manager.getParty().getTeam()) {
			if (!player.withinDistance(npc, 8) || !npc.clipedProjectile(target, true))
				continue;
			World.sendProjectile(npc, target, 2615, 41, 16, 41, 35, 16, 0);
			delayHit(npc, 2, target, getMagicHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MAGE, target)));
		}
		return 5;
	}
}
