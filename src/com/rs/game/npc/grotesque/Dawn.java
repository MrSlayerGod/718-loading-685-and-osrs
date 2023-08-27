/**
 * 
 */
package com.rs.game.npc.grotesque;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinitions;
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
public class Dawn extends NPC {

	private boolean secondPhase;
	private GrotesqueGuardianLair lair;
	
	public Dawn(GrotesqueGuardianLair lair) {
		super(27852, lair.getMap().getTile(new WorldTile(5993, 8989, 0)), -1, true, true);
		this.lair = lair;
		setDirection(Utils.getAngle(-1, 0));
		setForceMultiArea(true);
		setIntelligentRouteFinder(true); //just to be considered boss
		setRandomWalk(0);
		setNextAnimation(new Animation(27766));
		setCantInteract(true);
	}
	
	@Override
	public void sendDeath(Entity source) {
		if (!secondPhase || isCantInteract()) {
			setHitpoints(1);
			return;
		}
		final NPCCombatDefinitions defs = getCombatDefinitions();
		resetWalkSteps();
		getCombat().removeTarget();
		setNextAnimation(null);
		if (!isDead())
			setHitpoints(0);
		if (lair != null && lair.isRunning() && lair.getDusk() != null) {
			lair.getDusk().setCantInteract(true);
			lair.getDusk().resetWalkSteps();
			lair.getDusk().setNextFaceEntity(this);
			setNextFaceEntity(lair.getDusk());
		}
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(defs.getDeathEmote()));
				} else if (loop == 1) {
					setNextNPCTransformation(27885);
					setNextAnimation(new Animation(27809));
					if (lair != null && lair.isRunning() && lair.getDusk() != null) {
						lair.getDusk().setNextNPCTransformation(Dusk.ID_PHASE_4);
						lair.getDusk().setNextAnimation(new Animation(27795));
						
					}
				} else if (loop >= 3) {
					drop();
					finish();
					if (lair != null && lair.isRunning() && lair.getDusk() != null) {
						lair.getDusk().resetSpecialCount();
						lair.getDusk().setCantInteract(false);
						lair.getDusk().setTarget(lair.getPlayer());
					}
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}
	
	public boolean phase2() {
		if (lair != null && getHitpoints() <= this.getMaxHitpoints()*0.55 && !secondPhase && !isCantInteract()) {
			lair.sceneSpecial();
			secondPhase = true;
			return true;
		}
		return false;
	}
	
	public boolean sphere() {
		if (lair != null && secondPhase && !isCantInteract()) 
			return lair.createSpheres();
		return false;
	}
	
	@Override
	public void handleIngoingHit(Hit hit) {
		if (hit.getLook() == HitLook.MAGIC_DAMAGE) {
			Entity target = hit.getSource();
			if (target instanceof Player)
				((Player)target).getPackets().sendGameMessage("Dawn is immune to all magic attacks.");
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

	/**
	 * @author dragonkk(Alex)
	 * Mar 20, 2018
	 * @return
	 */
	public boolean isRunning() {
		return lair != null && lair.isRunning();
	}
	
}
