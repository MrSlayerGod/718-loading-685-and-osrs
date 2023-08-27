/**
 * 
 */
package com.rs.game.npc.grotesque;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.others.ConditionalDeath;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.GrotesqueGuardianLair;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Mar 20, 2018
 */
@SuppressWarnings("serial")
public class Dusk extends ConditionalDeath {


	private GrotesqueGuardianLair lair;
	private int specialCount;
	public static final int ID_SHIELD = 27851, ID_PHASE2 = 27882, ID_PHASE_4 = 27888, ID_DEATH = 27889;
	
	public Dusk(GrotesqueGuardianLair lair) {
		super(new int[] {4162, 51742}, "Dusk breaks into peices as you slam the hammer onto its head.", false, ID_SHIELD, lair.getMap().getTile(new WorldTile(5973, 8989, 0)), -1, true, true);
		this.lair = lair;
		setDirection(Utils.getAngle(1, 0));
		setForceMultiArea(true);
		setIntelligentRouteFinder(true); //just to be considered boss
		setRandomWalk(0);
		setNextAnimation(new Animation(27778));
		setCantInteract(true);
	}
	
/*	@Override
	public void sendDeath(Entity source) {
		if (getId() != ID_PHASE_4) {
			heal(1);
			return;
		}
		super.sendDeath(source);
	}*/
	
	public void sendDeath2(Entity source) {
		if (getId() != ID_PHASE_4) {
			setHitpoints(1);
			return;
		}
		final NPCCombatDefinitions defs = getCombatDefinitions();
		resetWalkSteps();
		getCombat().removeTarget();
		setNextAnimation(null);
		if (!isDead())
			setHitpoints(0);
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(defs.getDeathEmote()));
				} else if (loop == 1) {
					setNextNPCTransformation(ID_DEATH);
					setNextAnimation(new Animation(27809));
				} else if (loop >= 3) {
					finish();
					drop();
					if (lair != null && lair.isRunning()) 
						lair.resetFight();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}
	
	
	@Override
	public void handleIngoingHit(Hit hit) {
		if (getId() == ID_SHIELD) {
			Entity target = hit.getSource();
			if (target instanceof Player)
				((Player)target).getPackets().sendGameMessage("Dusk is currently invulnerable.");
			hit.setDamage(0);
		} else if (hit.getLook() == HitLook.MAGIC_DAMAGE || hit.getLook() == HitLook.RANGE_DAMAGE) { //2nd phase id
			Entity target = hit.getSource();
			if (target instanceof Player)
				((Player)target).getPackets().sendGameMessage("Dusk's stone armour absorves all magic and ranged based attacks.");
			hit.setDamage(0);
		}
		super.handleIngoingHit(hit);
	}
	
	@Override
	public double getMeleePrayerMultiplier() {
		return 0.6;
	}
	
	@Override
	public double getRangePrayerMultiplier() {
		return 0.6;
	}
	
	@Override
	public double getMagePrayerMultiplier() {
		return 0.6;
	}
	
	public boolean useSpecial() {
		return (getId() == ID_PHASE2 || getId() == ID_PHASE_4) && specialCount++ % 5== 0;
	}

	/**
	 * @author dragonkk(Alex)
	 * Mar 20, 2018
	 * @return
	 */
	public boolean phase3() {
		if (lair != null && getHitpoints() <= this.getMaxHitpoints()*0.55 && getId() == ID_PHASE2 && !isCantInteract()) {
			lair.sceneSpecial();
			return true;
		}
		return false;
	}
	
	public boolean wasPhase2() {
		return specialCount != 0;
	}
	
	public void resetSpecialCount() {
		specialCount = 0;
	}
	
	public boolean isRunning() {
		return lair != null && lair.isRunning();
	}
}
