package com.rs.game.npc.combat.impl.inferno;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.inferno.TzalZuk;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class JalHealerCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 27708 };
	}

	@Override
	public int attack(NPC npc, Entity target) {// yoa
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		if (target instanceof TzalZuk) {
			World.sendProjectile(npc, target, defs.getAttackProjectile(), 44, 30, 30, 20, 5, 32);
			delayHit(npc, 2, target, new Hit(npc, Utils.random(defs.getMaxHit())+1, HitLook.HEALED_DAMAGE));
		} else {
			for (int i = 0; i < 3; i++) {
				WorldTile tile = npc.transform(Utils.random(10) - 5, -5 - Utils.random(3), 0);
				World.sendProjectile(npc, tile, defs.getAttackProjectile(), 44, 30, 30, 20, 32, 32);
				World.sendGraphics(npc, new Graphics(5559, 50, 0), tile);
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						if (npc.isDead() || target.isDead() || target.getX() != tile.getX() || target.getY() != tile.getX())
							return;
						target.applyHit(getRegularHit(npc, Utils.random(defs.getMaxHit())+1));
					}
				}, 2);
			}
		}
		return defs.getAttackDelay();
	}
}
