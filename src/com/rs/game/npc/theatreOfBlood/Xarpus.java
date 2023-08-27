package com.rs.game.npc.theatreOfBlood;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.player.Player;
import com.rs.game.player.content.raids.TheatreOfBlood;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class Xarpus extends TOBBoss {

	private static final int[][] SQUARES = {
			{162, 163, 169, 170},
			{155, 163, 161, 170},
			{162, 156, 169, 162},
			{155, 156, 161, 162},
	}
	;
	
	
	private int timer;
	private Map<Integer, Integer> exhumed; //tile, spawn time
	private List<Integer> splashes;
	private int face;
		
	public Xarpus(TheatreOfBlood raid) {
		super(raid, 4, 28338, raid.getTile(161, 161, 1));
		setHitpoints(getStartHP()); //heal at begin. 5080 max
		exhumed = new HashMap<Integer, Integer>();
		splashes = new ArrayList<Integer>();
		setCantInteract(true);
		face = -1;
	}
	
	public int getStartHP() {
		int maxHP = 35000;
		if (raid != null && raid.getTeamSize() >= 1 && raid.getTeamSize() < 5) {
			int hp = (int) ((raid.getTeamSize() * maxHP / 5) * 0.7);
			int base = (int) (maxHP * 0.3);
			return base + hp;
		}
		return maxHP;
	}
	
    @Override
    public void processNPC() {
        if (isDead() || exhumed == null || splashes == null || raid.getTargets(this).isEmpty()) 
            return;
    	raid.setHPBar(this);
        for (Player player : raid.getTargets(this))
        	if (Utils.colides(player.getX(), player.getY(), 1, getX(), getY(), getSize()-1))
        		player.applyHit(new Hit(this, Utils.random(50), HitLook.REGULAR_DAMAGE));
        if (timer < 80)  //phase 1
        	processExhumed();
        else if (timer == 80) 
        	this.setNextAnimation(new Animation(28061));
        else if (timer == 81)  {
        	setCantInteract(false);
        	setNextNPCTransformation(28340);
        } else if (timer > 85) {
        	for (Player player : raid.getTargets(this)) {
        		if (splashes.contains(player.getTileHash()))
        			player.applyHit(new Hit(this, 100 + Utils.random(100), HitLook.POISON_DAMAGE));
        	}
        	for (Integer tile : splashes) 
        		World.sendObjectAnimation(this, new WorldObject(132744, 22, 0, new WorldTile(tile)), new Animation(28068));
        	if ((timer - 86) % 6 == 0) {
            	if (getHitpoints() > getMaxHitpoints() * 0.25) { //phase2
            		List<Player> targets = raid.getTargets(this);
            		if (!targets.isEmpty())
            			sendSplash(targets.get(Utils.random(targets.size())), null);
            	} else if ((timer - 86) % 12 == 0) { //phase3
            		int nextFace = Utils.random(SQUARES.length);
            		if (nextFace == face)
            			face = (nextFace+1) & 0x3;
            		else
            			face = nextFace;
            		setNextFaceWorldTile(raid.getTile(SQUARES[face][face == 1 || face == 3 ? 0 : 2], SQUARES[face][face == 2 || face == 3 ? 1 : 3], 1));
            	}
        	}
        }
    	timer++;
    }
    
    @Override
    public void handleIngoingHit(Hit hit) {
    	super.handleIngoingHit(hit);
    	if (face >= 0 && hit.getSource() instanceof Player) {
    		Player player = (Player) hit.getSource();
    		WorldTile min = raid.getTile(SQUARES[face][0], SQUARES[face][1], 1);
        	WorldTile max = raid.getTile(SQUARES[face][2], SQUARES[face][3], 1);
        	if (player.getX() >= min.getX() && player.getX() <= max.getX() &&
        			player.getY() >= min.getY() && player.getY() <= max.getY())
				player.applyHit(new Hit(this, 100 + Utils.random(200), HitLook.POISON_DAMAGE));
    	}
    }
    
    public void sendSplash(Player target, WorldTile from) {
    	WorldTile targetTile = new WorldTile(target);
    	int tileInt = targetTile.getTileHash();
    	int delay;
    	if (from == null) {
    		delay = CombatScript.getDelay(World.sendProjectile(this, targetTile, 6555, 0, 0, 20, 36, 16, 0)) + 1;
    		setNextFaceWorldTile(target);
    	} else 
    		delay = CombatScript.getDelay(World.sendProjectile(this, from, targetTile, 6555, 0, 0, 20, 36, 30, 0)) + 1;
    	
    	WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (isDead() || hasFinished() || !isRunning())
					return;
	    		World.sendGraphics(Xarpus.this, new Graphics(6556), targetTile);
	    		if (targetTile.withinDistance(target, 1)) {
	    			target.applyHit(new Hit(Xarpus.this, 100 + Utils.random(200), HitLook.POISON_DAMAGE));
	    			target.getPoison().makePoisoned(30);
	    		}
		    	if (!splashes.contains(tileInt)) {
		    		splashes.add(tileInt);
		    		World.spawnObject(new WorldObject(132744, 22, 0, targetTile));
		    	}
		    	if (from == null) {
		    		List<Player> targets = raid.getTargets(Xarpus.this);
		    		targets.remove(target);
		    		if (!targets.isEmpty()) {
		    			Player nextTarget = targets.get(Utils.random(targets.size()));
		    			targets.remove(nextTarget);
		    			sendSplash(nextTarget, targetTile);
		    		}
		    		if (!targets.isEmpty()) 
		    			sendSplash(targets.get(Utils.random(targets.size())), targetTile);
		    	}
			}
    	}, delay);
    }
    
    private void clearSplashes() {
    	for (Integer tile : splashes) {
    		WorldTile tileI = new WorldTile(tile);
    		World.removeObject(World.getObjectWithType(tileI, 22));
    		World.sendGraphics(this, new Graphics(6551 + Utils.random(4)), tileI);
    	}
    	splashes.clear();
    }
    
    @Override
    public void sendDeath(Entity killer) {
    	WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
		    	setNextNPCTransformation(28341);
			}
    	});
    	super.sendDeath(killer);
    }
    
    @Override
    public void finish() {
    	super.finish();
    	if (isRunning())
    		clearSplashes();
    }
    
    private void processExhumed() {
    	if (timer % 4 == 0 && (timer / 4) % 5 < raid.getTeamSize()) { //cycle
    		//spawns
    		WorldTile min = raid.getTile(155, 156, 1);
        	WorldTile max = raid.getTile(169, 170, 1);
        	WorldTile tile = null;
    		int tryCount = 0;
    		while ((tile == null || exhumed.containsKey(tile.getTileHash()) || Utils.isOnRange(getX(), getY(), getSize(), tile.getX(), tile.getY(), 1, 2)) && tryCount++ < 30) 
    			tile = new WorldTile(min.getX() + Utils.random(1 + max.getX() - min.getX()), min.getY()  + Utils.random(1 + max.getY() - min.getY()), 1);
    		exhumed.put(tile.getTileHash(), timer);
    		World.spawnObject(new WorldObject(132743, 22, 0, tile));
    	}
    	Iterator<Integer> tiles = exhumed.keySet().iterator();
    	skip: while (tiles.hasNext()) {
    		int tile = tiles.next();
    		int timer = exhumed.get(tile);
    		WorldTile tileI = new WorldTile(tile);
    		if (timer + 15 <= this.timer || this.timer == 79) {
    			tiles.remove();
    			World.removeObject(World.getObjectWithId(tileI, 132743));
    			World.sendGraphics(this, new Graphics(6549), tileI);
    			continue;
    		} 
    		if (timer + 2 > this.timer)
    			continue;
    		for (Player player : raid.getTargets(this)) 
    			if (player.getTileHash() == tile)
    				continue skip;
    		World.sendProjectile(tileI, this, 6550, 0, 41, 50, 36, 45, 0);
    		applyHit(new Hit(this, 60, HitLook.HEALED_DAMAGE, 20));
    	}
    	//if timer == (phase-1) remove all
    }
    
	
	@Override
	public void setNextFaceEntity(Entity target) {
		
	}
	
	@Override
	public void setTarget(Entity target) {
		
	}
}
