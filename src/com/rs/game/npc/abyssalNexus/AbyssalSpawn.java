/**
 * 
 */
package com.rs.game.npc.abyssalNexus;

import com.rs.game.Animation;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;

/**
 * @author dragonkk(Alex)
 * Nov 5, 2017
 */
@SuppressWarnings("serial")
public class AbyssalSpawn extends NPC {

	private static final int ID = 25916, SCION_ID = 25918;
	
	private AbyssalSire sire;
	private int delay;
	
	public AbyssalSpawn(AbyssalSire sire, WorldTile tile) {
		super(ID, tile, -1, true, true);
		this.sire = sire;
		setForceAgressive(true);
		setForceTargetDistance(64);
		setIntelligentRouteFinder(true);
	}
	
	private boolean isScion() {
		return getId() == SCION_ID;
	}

	@Override
	public void processNPC() {
		if (isDead())
			return;
		if (sire != null && (sire.hasFinished() || sire.isSleeping())) {
			finish();
			return;
		}
		if (!isScion()) {
			delay++;
			if (delay >= 20) {
				setNextNPCTransformation(SCION_ID);
				setNextAnimation(new Animation(27123));
				setHitpoints(getMaxHitpoints());
				setTarget(sire.getFightTarget());
				getCombat().setCombatDelay(4);
			}
		}
		super.processNPC();
	}
	
	@Override
	public double getMeleePrayerMultiplier() {
		return 0.6;
	}

	public boolean canAttack(Player player) {
		return sire != null && sire.canAttack(player);
	}
}
