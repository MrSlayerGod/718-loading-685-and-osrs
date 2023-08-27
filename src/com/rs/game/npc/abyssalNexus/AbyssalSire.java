/**
 * 
 */
package com.rs.game.npc.abyssalNexus;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.Magic;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Nov 9, 2017
 */
@SuppressWarnings("serial")
public class AbyssalSire extends NPC {
	
	
	public static final int SLEEPING_ID = 25888, PHASE1_ID = 25887, PHASE1_ID_DESIORIENTED = 25886, PHASE2_ID = 25890, PHASE3_ID = 25891, PHASE4_ID = 25908;
	private static final WorldTile BASE = new WorldTile(3102, 4854, 0);
	private static final WorldTile[] TENTACLES_BASE = {new WorldTile(3092, 4844, 0), new WorldTile(3109, 4844, 0), new WorldTile(3095, 4835, 0), new WorldTile(3107, 4835, 0), new WorldTile(3093, 4826, 0), new WorldTile(3110, 4826, 0)};
	private static final int[] TENTACLE_IDS = {25910, 25910, 25913, 25913, 25909, 25909};
	private static final WorldTile[] VENTS_BASE = {new WorldTile(3089, 4844, 0), new WorldTile(3117, 4843, 0), new WorldTile(3092, 4834, 0), new WorldTile(3120, 4833, 0)};
	private Tentacle[] tentacles = new Tentacle[6];
	private Vent[] vent = new Vent[4];
	private Player target;
	private int damageReceived;
	private int spawnCount;
	private int resetDelay;
	
	public AbyssalSire(WorldTile tile) {
		super(SLEEPING_ID, tile, -1, true, false);
		spawnTentacles();
		spawnVent();
		setIntelligentRouteFinder(true);
		setDropRateFactor(5);
		reset();
	}
	

	@Override
	public  void finish() {
		reset();
		super.finish();
	}
	
	public boolean phase4() {
		if (getHitpoints() >= 1390)
			return false;
		setNextNPCTransformation(PHASE4_ID);
		setNextAnimation(new Animation(27098));
		spawnCount = 0;
		if (withinDistance(target, 32) && !target.hasFinished()) {
			Magic.sendObjectTeleportSpell(target, false, this.transform(3, -1, 0));
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					if (target == null || AbyssalSire.this.isDead() || AbyssalSire.this.hasFinished())
						return;
					if (Utils.isOnRange(AbyssalSire.this, target, 0))
						target.applyHit(new Hit(AbyssalSire.this, Utils.random(320)+400, HitLook.REGULAR_DAMAGE));
				}
			}, 12);
		}
		return true;
	}
	
	public boolean phase3() {
		if (getHitpoints() >= getMaxHitpoints()/2)
			return false;
		setCantInteract(true);
		getCombat().removeTarget();
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (AbyssalSire.this.hasFinished() || !isPhase2()) {
					stop();
					return;
				}
				WorldTile tile = getTile(new WorldTile(3102, 4837, 0));
				if (tile.getX() != getX() || tile.getY() != getY()) {
					resetWalkSteps();
					addWalkSteps(tile.getX(), tile.getY(), 2, false);
				} else {
					stop();
					setLocked(true);
					setCantFollowUnderCombat(true);
					setNextAnimation(new Animation(27096));
					setNextNPCTransformation(PHASE3_ID);
					setNextFaceEntity(null);
					setNextFaceWorldTile(new WorldTile(tile.transform(3, -3, 0)));
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							if (AbyssalSire.this.hasFinished())
								return;
							setCantInteract(false);
							setLocked(false);
							getCombat().setCombatDelay(7);
							setTarget(target);
							awakeTentacles();
							for (int i = 0; i < 4; i++) {
								AbyssalSpawn spawn = new AbyssalSpawn(AbyssalSire.this, AbyssalSire.this.transform(Utils.random(2), Utils.random(2), 0));
								spawn.setTarget(target);
							}
						}
					}, 7);
				}
			}
		}, 0, 0);
		return true;
	}
	
	public boolean phase2() {
		if (hasRespiratories())
			return false;
		setCantInteract(true);
		setLocked(true);
		setNextAnimation(new Animation(24532));
		setNextNPCTransformation(PHASE2_ID);
		stunTentacles();
		getCombat().removeTarget();
		WorldTasksManager.schedule(new WorldTask() {
			
			boolean walk = false;
			
			@Override
			public void run() {
				if (AbyssalSire.this.hasFinished()) {
					stop();
					return;
				}
				if (!walk)  {
					walk = true;
					WorldTile tile = getTile(BASE.transform(0, -9, 0));
					resetWalkSteps();
					addWalkSteps(tile.getX(), tile.getY(), 10, false);
				} else {
					stop();
					setCantInteract(false);
					setLocked(false);
					setCantFollowUnderCombat(false);
					getCombat().setCombatDelay(7);
					setTarget(target);
				}
			}
		}, 7, 7);
		return true;
	}
	
	public boolean stun() {
		if (damageReceived >= 0 && damageReceived < 750)
			return false;
		sendMessage(damageReceived == -1 ? "Your Shadow spell disorients the Sire." : "You disorient the Sire.");
		setCantInteract(true);
		setLocked(true);
		setNextAnimation(new Animation(24527));
		setNextNPCTransformation(PHASE1_ID_DESIORIENTED);
		stunTentacles();
		getCombat().removeTarget();
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (AbyssalSire.this.hasFinished())
					return;
				if (phase2())
					return;
				setCantInteract(false);
				setLocked(false);
				awake();
			}
		}, 40);
		return true;
	}
	
	@Override
	public void handleIngoingHit(Hit hit) {
		if (damageReceived >= 0)
			damageReceived += hit.getDamage();
		if (isSleeping() || isPhase1()) {
			int maxDamage = getHitpoints() - 3250;
			if (hit.getDamage() > maxDamage)
				hit.setDamage(Math.max(0, maxDamage));
		}else if (isPhase2()) {
			int maxDamage = getHitpoints() - 1900;
			if (hit.getDamage() > maxDamage)
				hit.setDamage(Math.max(0, maxDamage));
		}else if (isPhase3()) {
			int maxDamage = getHitpoints() - 1000;
			if (hit.getDamage() > maxDamage)
				hit.setDamage(Math.max(0, maxDamage));
		}
		super.handleIngoingHit(hit);
	}
	
	public void sendMessage(String message) {
		if (target == null || !target.withinDistance(this, 32) || target.hasFinished())
			return;
		target.getPackets().sendGameMessage(message);
	}
	
	
	@Override
	public void reset() {
		super.reset();
		setNextNPCTransformation(SLEEPING_ID);
		setCantFollowUnderCombat(true);
		sleepTentacles();
		sleepVents();
		setNextFaceEntity(null);
		setNextFaceWorldTile(new WorldTile(getRespawnTile().transform(0, -1, 0)));
		damageReceived = 0;
		spawnCount = 0;
		resetDelay = 0;
		target = null;
	}
	

	@Override
	public void setNextFaceEntity(Entity entity) {
		if (entity != null && !isPhase2())
			return;
		super.setNextFaceEntity(entity);
	}
	
	public boolean isPhase1() {
		return getId() == PHASE1_ID;
	}
	
	public boolean isPhase2() {
		return getId() == PHASE2_ID;
	}
	
	public boolean isPhase3() {
		return getId() == PHASE3_ID;
	}
	
	public boolean isSleeping() {
		return getId() == SLEEPING_ID;
	}
	
	public void startFight(Player player) {
		this.target = player;
		awakeRespiratory();
		awake();
	}
	
	private void awake() {
		setNextNPCTransformation(PHASE1_ID);
		this.setNextAnimation(new Animation(24528));
		damageReceived = 0;
		setTarget(target);
		awakeTentacles();
	}

	private WorldTile getTile(WorldTile base) {
		return base.transform(-BASE.getX(), -BASE.getY(), 0).transform(getRespawnTile().getX(), getRespawnTile().getY(), 0);
	}
	
	private void spawnTentacles() {
		for(int i = 0; i < tentacles.length; i++) 
			tentacles[i] = new Tentacle(TENTACLE_IDS[i], i % 2 == 0, getTile(TENTACLES_BASE[i]));
	}
	
	private void spawnVent() {
		for(int i = 0; i < vent.length; i++) 
			vent[i] = new Vent(getTile(VENTS_BASE[i]));
	}
	
	private void sleepVents() {
		for(int i = 0; i < vent.length; i++) {
			if (vent[i] != null) {
				if (vent[i].hasFinished())
					vent[i].spawn();
				vent[i].setSleeping();
			}
		}
	}
	
	private void sleepTentacles() {
		for(int i = 0; i < tentacles.length; i++) 
			if (tentacles[i] != null) {
				if (tentacles[i].hasFinished()) //shouldnt happen
					tentacles[i].spawn();
				tentacles[i].setSleeping();
			}
	}
	
	private void stunTentacles() {
		for(int i = 0; i < tentacles.length; i++) 
			if (tentacles[i] != null) {
				if (tentacles[i].hasFinished()) //shouldnt happen
					tentacles[i].spawn();
				tentacles[i].setStunned();
			}
	}
	
	
	private void awakeTentacles() {
		for(int i = 0; i < tentacles.length; i++) 
			if (tentacles[i] != null) 
				tentacles[i].setAwaken(target);
	}
	
	private void awakeRespiratory() {
		for(int i = 0; i < vent.length; i++) 
			if (vent[i] != null) 
				vent[i].setAwaken();
	}
	
	public boolean hasRespiratories() {
		for(int i = 0; i < vent.length; i++) 
			if (vent[i] != null && !vent[i].hasFinished() && !vent[i].isDead()) 
				return true;
		return false;
	}
	
	public Player getFightTarget() {
		return target;
	}
	
	public boolean canAttack(Player player) {
		if (isSleeping() || target == null || (!target.withinDistance(this, 32) || target.hasFinished())) {
			target = player;
			return true;
		}
		return target == player;
	}

	
	@Override
	public void processNPC() {
		if (!isSleeping() && (target == null || !target.withinDistance(this, 32) || target.hasFinished())) {
			resetDelay++;
			if (resetDelay >= 100)
				reset();
		} else {
			resetDelay = 0;
		}
		super.processNPC();
		if (resetDelay == 0 && !isSleeping() && target != null && isPhase2() && getCombat().getCombatDelay() <= 3 && !target.isLocked() && !target.hasWalkSteps() && !hasWalkSteps() && !Utils.isOnRange(this, target, 0) && !isCantFollowUnderCombat())
			Magic.sendObjectTeleportSpell(target, false, this.transform(3, -1, 0)); //no safespoting
	}

	public int getSpawnCount() {
		return spawnCount;
	}

	public void increaseSpawnCount() {
		this.spawnCount++;
	}
	
	public void shadowSpell() {
		this.damageReceived = -1;
	}
	
	@Override
	public double getMeleePrayerMultiplier() {
		return 0.6;
	}

}
