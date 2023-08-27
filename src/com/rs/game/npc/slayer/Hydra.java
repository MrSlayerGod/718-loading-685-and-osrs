/**
 * 
 */
package com.rs.game.npc.slayer;

import java.util.ArrayList;
import java.util.List;

import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 */
@SuppressWarnings("serial")
public class Hydra extends NPC {

	private List<Integer> splashes;
	
	private int specialAttackCount;
	
	private long lastSplash;
	
	private boolean useRange;
	private int normalAttackCount;
	

	public Hydra(WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(28609, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		splashes = new ArrayList<Integer>();
		useRange = Utils.random(2) == 0;
		normalAttackCount = -1;
	}
	
    @Override
    public void processNPC() {
    	if (splashes == null || isDead())
    		return;
    	super.processNPC();
    	if (!splashes.isEmpty()) {
    		if (Utils.currentTimeMillis() - lastSplash > 12000) 
    			clearSplashes();
    		else {
    	    	for (Entity player : World.getNearbyPlayers(this, false)) {
    	    		if (splashes.contains(player.getTileHash()))
    	    			player.applyHit(new Hit(this, Utils.random(player.getPoison().isImmune() ? 20 : 40) + 1, HitLook.POISON_DAMAGE));
    	    	}
    	    	/*for (Integer tile : splashes) 
    	    		World.sendObjectAnimation(this, new WorldObject(132744, 22, 0, new WorldTile(tile)), new Animation(28068));
    	    	 */
    		}
    	}
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
	    		World.sendGraphics(Hydra.this, new Graphics(6556), targetTile);
	    		if (targetTile.withinDistance(target, 1)) {
	    			target.applyHit(new Hit(Hydra.this, Utils.random(getMaxHit())+1, HitLook.POISON_DAMAGE));
	    			target.getPoison().makePoisoned(40);
	    		}
		    	if (!splashes.contains(tileInt)) {
		    		splashes.add(tileInt);
		    	//	World.spawnObject(new WorldObject(132744, 22, 0, targetTile));
		    		World.sendGraphics(Hydra.this, new Graphics(6654 + Utils.random(6661 - 6654 + 1)), targetTile);
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
    


	
	
	@Override
	public void finish() {
		specialAttackCount = 0;
		normalAttackCount = -1;
		useRange = Utils.random(2) == 0;
		clearSplashes();
		super.finish();
	}
	
	public boolean useSpecial() {
		return ++specialAttackCount % 7 == 0;
	}
	
	public boolean useRange() {
		if (normalAttackCount++ == 2) {
			useRange = !useRange;
			normalAttackCount = 0;
		}
		return useRange;
	}

}
