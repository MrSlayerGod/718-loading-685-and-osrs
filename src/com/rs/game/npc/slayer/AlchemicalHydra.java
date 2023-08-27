/**
 * 
 */
package com.rs.game.npc.slayer;

import java.util.ArrayList;
import java.util.List;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.HydraLair;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 */
@SuppressWarnings("serial")
public class AlchemicalHydra extends NPC {

	
	private HydraLair lair;
	private List<Integer> splashes;
	
	private int specialAttackCount;
	
	private long lastSplash;
	
	private boolean useRange;
	private int normalAttackCount;
	
	private int shieldStage;
	
	public static final int POISON_ID = 28615, POISON_DYING_ID = 28616, LIGHTNING_ID = 28619, LIGHTNING_DYING_ID = 28617, FLAME_ID = 28620, FLAME_DYING_ID = 28618, ENRAGE_ID = 28621, ENRANGE_DYING_ID = 28622;

	public static final int[] IDS = {POISON_ID, POISON_DYING_ID, LIGHTNING_ID, LIGHTNING_DYING_ID, FLAME_ID, FLAME_DYING_ID, ENRAGE_ID, ENRANGE_DYING_ID};
	
	public AlchemicalHydra(HydraLair lair) {
		super(POISON_ID, lair.getMap().getTile(new WorldTile(1364, 10265, 0)), -1, true, false);
		this.lair = lair;
		splashes = new ArrayList<Integer>();
		setIntelligentRouteFinder(true); //just to be considered boss
		setDropRateFactor(3);
		setup();
	}
	
    @Override
    public void processNPC() {
    	if (splashes == null || isDead())
    		return;
    	super.processNPC();
    	
    	if (this.getHitpoints() <= getMaxHitpoints() * 0.75 && getId() == POISON_ID) {
    		setCantInteract(true);
    		setLocked(true);
    		setNextFaceEntity(null);
    		setNextNPCTransformation(POISON_DYING_ID);
    		setNextAnimation(new Animation(28237));
    		WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					if (hasFinished() || !isRunning())
						return;
					setCantInteract(false);
		    		setLocked(false);
		    		setNextNPCTransformation(LIGHTNING_ID);
		    		setNextAnimation(new Animation(28238));
		    		getCombat().setTarget(lair.getPlayer());
		    		getCombat().setCombatDelay(6);
		    		reverse();
				}
    		}, 2);
    	} else if (this.getHitpoints() <= getMaxHitpoints() * 0.5 && getId() == LIGHTNING_ID) {
    		setCantInteract(true);
    		setLocked(true);
    		setNextFaceEntity(null);
    		setNextNPCTransformation(LIGHTNING_DYING_ID);
    		setNextAnimation(new Animation(28244));
    		WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					if (hasFinished() || !isRunning())
						return;
					setCantInteract(false);
		    		setLocked(false);
		    		setNextNPCTransformation(FLAME_ID);
		    		setNextAnimation(new Animation(28245));
		    		getCombat().setTarget(lair.getPlayer());
		    		getCombat().setCombatDelay(6);
		    		reverse();
				}
    		}, 1);
    	} else if (this.getHitpoints() <= getMaxHitpoints() * 0.25 && getId() == FLAME_ID) {
    		setCantInteract(true);
    		setLocked(true);
    		setNextFaceEntity(null);
    		setNextNPCTransformation(FLAME_DYING_ID);
    		setNextAnimation(new Animation(28251));
    		WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					if (hasFinished() || !isRunning())
						return;
					setCantInteract(false);
		    		setLocked(false);
		    		setNextNPCTransformation(ENRAGE_ID);
		    		setNextAnimation(new Animation(28252));
		    		getCombat().setTarget(lair.getPlayer());
		    		getCombat().setCombatDelay(6);
		    		reverse();
		    		shieldStage = 1;
		    		lair.getPlayer().getPackets().sendGameMessage("The Alchemical Hydra becomes enranged!");
				}
    		}, 2);
    	}
    		
    	
    	if (!splashes.isEmpty()) {
    		if (Utils.currentTimeMillis() - lastSplash > 12000) 
    			clearSplashes();
    		else {
    	    	for (Entity player : World.getNearbyPlayers(this, false)) {
    	    		if (splashes.contains(player.getTileHash()))
    	    			player.applyHit(new Hit(this, Utils.random(player.getPoison().isImmune() ? 60 : 120) + 1, HitLook.POISON_DAMAGE));
    	    	}
    	    	/*for (Integer tile : splashes) 
    	    		World.sendObjectAnimation(this, new WorldObject(132744, 22, 0, new WorldTile(tile)), new Animation(28068));
    	    	 */
    		}
    	}
    }
    
	@Override
	public void sendDeath(Entity source) {
		if (getId() != ENRAGE_ID) {
			setHitpoints(1);
			return;
		}
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				if (!isRunning() || hasFinished())
					return;
				setNextNPCTransformation(ENRANGE_DYING_ID);
				setNextAnimation(new Animation(28258));
			}
			
		}, 3);
		super.sendDeath(source);
	}
	
	public void spawn() {
		if (!isRunning())
			return;
		super.spawn();
	}
	
    
    public void sendSplash(Entity target, WorldTile targetTile) {
    	lastSplash = Utils.currentTimeMillis();
    	int tileInt = targetTile.getTileHash();
    	int delay = CombatScript.getDelay(World.sendProjectile(this, targetTile, 6555, 35, 0, 20, 36, 16, 64)) + 1;
    	WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (isDead() || hasFinished())
					return;
	    		World.sendGraphics(AlchemicalHydra.this, new Graphics(6556), targetTile);
	    		if (targetTile.withinDistance(target, 1)) {
	    			target.applyHit(new Hit(AlchemicalHydra.this, Utils.random(getMaxHit())+1, HitLook.POISON_DAMAGE));
	    			target.getPoison().makePoisoned(120);
	    		}
		    	if (!splashes.contains(tileInt)) {
		    		splashes.add(tileInt);
		    		World.sendGraphics(AlchemicalHydra.this, new Graphics(6654 + Utils.random(6661 - 6654 + 1)), targetTile);
		    		
		    		//World.spawnObject(new WorldObject(132744, 10, Utils.random(4), targetTile));
		    	}
			}
    	}, delay);
    }

    private void clearSplashes() {
    	for (Integer tile : splashes) {
    		WorldTile tileI = new WorldTile(tile);
    		//World.removeObject(World.getObjectWithType(tileI, 22));
    		World.sendGraphics(this, new Graphics(6551 + Utils.random(4)), tileI);
    	}
    	splashes.clear();
    }
    


    private void reverse() {
    	shieldStage = 0;
    	useRange = !useRange;
    	specialAttackCount = 0;
		normalAttackCount = -1;
    }
	
    private void setup() {
    	shieldStage = 0;
		specialAttackCount = 0;
    	normalAttackCount = -1;
		useRange = Utils.random(2) == 0;
    }
	
	@Override
	public void finish() {
		setNPC(POISON_ID);
		setup();
		clearSplashes();
		super.finish();
	}
	
	public boolean useSpecial() {
		specialAttackCount++;
		return specialAttackCount == 4
				|| (specialAttackCount > 4 && (specialAttackCount-4) % (getId() == FLAME_ID ? 10 : 7) == 0)
				//every 6 attacks after 3.
				
				;
	}
	
	public boolean useRange() {
		if (++normalAttackCount >= 3) {
			useRange = !useRange;
			normalAttackCount = 0;
		}
		return useRange;
	}
	
	public boolean isRunning() {
		return lair != null && lair.isRunning();
	}
	
	public void useChemical(boolean neutralise) {
		if (neutralise)
			neutralise();
		else
			empower();
	}
	
	public void neutralise() {
		if (shieldStage != 1) {
			lair.getPlayer().getPackets().sendGameMessage("The chemicals neutralise the Alchemical Hydra's defences!");
			shieldStage = 1;
			setNextForceTalk(new ForceTalk("Roaaaaaaaaaaaaaar!"));
		}
	}
	
	public void empower() {
		if (shieldStage != 2) {
			lair.getPlayer().getPackets().sendGameMessage("The chemicals are absorved by the Alchemical Hydra; empowering it further!");
			shieldStage = 2;
		}
	}
	
	@Override
	public int getMaxHit() {
		return shieldStage == 2 ? 520 : super.getMaxHit();
	}
	
	@Override
	public void handleIngoingHit(Hit hit) {
		if (shieldStage != 1) {
			if (hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE && hit.getLook() != HitLook.MAGIC_DAMAGE)
				return;
			hit.setDamage((int) (hit.getDamage() * 0.25));
			if (hit.getSource() instanceof Player)
				((Player) hit.getSource()).getPackets().sendGameMessage("The Alchemical Hydra's defences partially absorb your attack!");
		}
		super.handleIngoingHit(hit);
	}

	public HydraLair getLair() {
		return lair;
	}
}
