package com.rs.game.npc.combat.impl.cox;

import com.rs.Settings;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.cox.COXBoss;
import com.rs.game.npc.cox.impl.Vespula;
import com.rs.game.player.Player;
import com.rs.game.player.Projectile;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Direction;
import com.rs.utils.Utils;

public class VespulaCombat extends CombatScript {
	public static final int GROUNDED = 27532;
	public static final int FLYING = 27530;

	private static final Projectile TILE_PROJECTILE_FLYING = new Projectile(Settings.OSRS_GFX_OFFSET + 1486, 70, 0, 0, 80, 30, 0);
	private static final Projectile TILE_PROJECTILE_GROUNDED = new Projectile(Settings.OSRS_GFX_OFFSET + 1486, 40, 0, 0, 80, 30, 0);
	private static final Projectile PROJECTILE_FLYING = new Projectile(Settings.OSRS_GFX_OFFSET + 1486, 70, 43, 0, 30, 30, 0);
	private static final Projectile PROJECTILE_GROUNDED = new Projectile(Settings.OSRS_GFX_OFFSET + 1486, 40, 0, 0, 10, 30, 0);

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ FLYING, GROUNDED };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		NPCCombatDefinitions def = npc.getCombatDefinitions();
		Vespula v = (Vespula) npc;
		if(v.getProtectionTicks() > 0) {
			npc.getCombat().reset();
			npc.setNextFaceEntity(null);
			return 0;
		}

		if(Utils.random(7) == 1)
			target.applyHit(null, 200, Hit.HitLook.POISON_DAMAGE);

		if (npc.getId() == GROUNDED || (target.withinDistance(target,0) && Utils.random(1.0) > .66))
			meleeAttack(npc, target);
		else
			rangedAttack(npc, target);
		return def.getAttackDelay();
	}

	private void meleeAttack(NPC npc, Entity target) {
		NPCCombatDefinitions def = npc.getCombatDefinitions();
		npc.anim(npc.getId() == GROUNDED ? 27451 : 27454);
		delayHit(npc, 1, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
	}

	private void rangedAttack(NPC npc, Entity target) {
		NPCCombatDefinitions def = npc.getCombatDefinitions();
		npc.anim(def.getAttackEmote());
		//targeted attack
		int ms;
		if (npc.getId() == FLYING)
			ms = PROJECTILE_FLYING.fire(npc, target);
		else
			ms = PROJECTILE_GROUNDED.fire(npc, target);
		delayHit(npc, CombatScript.getDelay(ms), target, getRangeHit(npc, getRandomMaxHit(npc, def.getMaxHit(), NPCCombatDefinitions.RANGE, target)));
		//echo projectile
		Direction echoDir = Utils.get(Direction.values());
		WorldTile echoPosition = target.clone().transform(echoDir.deltaX, echoDir.deltaY, 0);
		if (npc.getId() == FLYING)
			TILE_PROJECTILE_FLYING.fire(npc, echoPosition);
		else
			TILE_PROJECTILE_GROUNDED.fire(npc, echoPosition);
		Entity immune = target;
		WorldTasksManager.schedule(() -> {
			for(Player p : ((COXBoss) npc).getTeam()) {
				int dmg = def.getMaxHit();
				if(p != immune && p.matches(echoPosition)) {
					if(p.getPrayer().isRangeProtecting())
						dmg /= 2;
				}
				delayHit(npc, 0, p, getRangeHit(npc, getRandomMaxHit(npc, dmg, NPCCombatDefinitions.RANGE, p)));
			}
		}, 4);
	}

}
