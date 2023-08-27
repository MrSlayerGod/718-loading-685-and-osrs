package com.rs.game.npc.theatreOfBlood;

import java.util.LinkedList;
import java.util.List;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.map.MapInstance.Stages;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.player.Player;
import com.rs.game.player.content.raids.TheatreOfBlood;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * 
 * @author Alex (dragonkk)
 *
 */
@SuppressWarnings("serial")
public class PestilentBloat extends TOBBoss {

	private static final int[][] WALK_TILES = {
			{24,24,0}, //tile4
			{24,35,0}, //tile1
			{35,35,0}, //tile2
			{35,24,0}, //tile3
	};
	public PestilentBloat(TheatreOfBlood raid) {
		super(raid, 1, 28359, raid.getTile(24, 24, 0));
		setTurnCycle();
		setFreezeCycle();
	}
	
	private WorldTile walkTo;
	private int cycle;
	private int minTurnArroundCycle;
	private boolean inverse;
	private int timeUntilFreeze;
	
	private void setTurnCycle() {
		minTurnArroundCycle = Utils.random(78)+24;
	}
	private void setFreezeCycle() {
		timeUntilFreeze = 6 *  (getHitpoints() <= getMaxHitpoints() / 2 ? 14 : 7);
	}
	
	@Override
	public void sendDeath(Entity killer) {
		if (raid !=null && raid.getStage() == Stages.RUNNING) {
			for (Player player : raid.getTeam())
				player.getPackets().sendStopCameraShake();
		}
		super.sendDeath(killer);
	}
	
    @Override
    public void processNPC() {
    	if (isDead() || raid == null || raid.getStage() != Stages.RUNNING)
    		return;
    	if (timeUntilFreeze <= 0) {
    		if (timeUntilFreeze == 0)
    			setNextAnimation(new Animation(28082));
    		timeUntilFreeze--;
    		if (timeUntilFreeze <= -35) {
    	    	//earthquake when awaking
    			setFreezeCycle();
    		} else if (timeUntilFreeze == -33) {
    			for (Player player : raid.getTeam())
    				player.getPackets().sendStopCameraShake();
    		} else if (timeUntilFreeze == -31) {
    			for (Player player : raid.getTeam())
    				player.getPackets().sendCameraShake(3, 12, 25, 12, 25);
    			//stomp
    			for (Player player : raid.getTargets(this)) //too close
    	    		if (Utils.isOnRange(player, this, 2)) 
    	    			CombatScript.delayHit(this, 0, player, CombatScript.getRegularHit(this, 250 + Utils.random(250)));
    		}
    		raid.setHPBar(this);
    		return;
    	}
    	if (walkTo == null || (walkTo.getX() == getX() && walkTo.getY() == getY())) {
    		cycle = (cycle+(inverse ? -1 : 1)) & 0x3;
    		walkTo = raid.getTile(WALK_TILES[cycle][0], WALK_TILES[cycle][1], WALK_TILES[cycle][2]);
    	}
    	setRun(getHitpoints() >= getMaxHitpoints() * 0.2 && getHitpoints() <= getMaxHitpoints() * 0.6);
    	resetWalkSteps();
    	addWalkSteps(walkTo.getX(), walkTo.getY(), getRun() ? 2 : 1);
    	if (raid.getTargets(this).isEmpty())
    		return;
    	if (minTurnArroundCycle-- == 0) {
    		inverse = !inverse;
    		walkTo = null;
    		setTurnCycle();
    	}
    	if (getHitpoints() != getMaxHitpoints() && timeUntilFreeze % 6  == 0 && timeUntilFreeze >= 6) 
    		performFlesh();
    	timeUntilFreeze--; //doesnt go down if not fighting
    	
    	performFlies();
		raid.setHPBar(this);
    }
    
    @Override
    public void handleIngoingHit(Hit hit) {
    /*	if (timeUntilFreeze > 0) //cant dmg until stops
    		hit.setDamage(0);*/
        super.handleIngoingHit(hit);
    }
    
    private void performFlesh() {
    	List<Integer> fleshTiles = new LinkedList<Integer>();
    	WorldTile min = raid.getTile(25, 25);
    	WorldTile max = raid.getTile(39, 39);
    	WorldTile avoidMin = raid.getTile(29, 29);
    	WorldTile avoidMax = raid.getTile(34, 34);
    	for (int i = 0; i < 20; i++) {
    		//25-25 until 39-39
    		//skip 29-29 until 34, 34
    		WorldTile tile = null;
    		int tryCount = 0;
    		while ((tile == null || fleshTiles.contains(tile.getTileHash())
    				|| (tile.getX() >= avoidMin.getX() && tile.getX() <= avoidMax.getX() &&
    						tile.getY() >= avoidMin.getY() && tile.getY() <= avoidMax.getY())) && tryCount++ < 30) {
    			tile = new WorldTile(min.getX() + Utils.random(1 + max.getX() - min.getX()), min.getY()  + Utils.random(1 + max.getY() - min.getY()), 0);
    		}
    		World.sendGraphics(this, new Graphics(6570 + Utils.random(4)), tile);
    		fleshTiles.add(tile.getTileHash());
    	}
    	WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				if (isDead() || hasFinished() || !isRunning())
					return;
				for (Player player : raid.getTargets(PestilentBloat.this)) {
					if (fleshTiles.contains(player.getTileHash())) {
						player.applyHit(new Hit(PestilentBloat.this, Utils.random(300) + 200, HitLook.REGULAR_DAMAGE));
						player.setNextGraphics(new Graphics(6575));
						player.stopAll();
						player.addFreezeDelay(1800, true);
						player.getPackets().sendGameMessage("You have been stunned.");
					}
						
				}
			}
    		
    	}, 4);
    }
    
    private void performFlies() {
    	boolean perform = false;
    	List<Player> targets = raid.getTargets(this);
    	for (Player player : targets) { //too close
    		if (Utils.isOnRange(player, this, 1)) { 
    			perform = true;
    			break;
    		}
    	}
    	byte[] dirs = Utils.getDirection(getDirection());
		a: for (int distance = 20; distance >= 0; distance--) {
			WorldTile tile = new WorldTile(getX() + (dirs[0] * distance), getY() + (dirs[1] * distance), getPlane());
			for (Player player : targets) { //on path, checks up to 20tiles in same dir
				if (Utils.isOnRange(player.getX(), player.getY(), 1, tile.getX(), tile.getY(), getSize(), 0)) {
					perform = true;
					break a;
				}
			}
		}
    	if (!perform)
    		return;
		for (Player player : targets) {
			player.applyHit(new Hit(this, Utils.random(100) + 100, HitLook.RANGE_DAMAGE));
			player.setNextGraphics(new Graphics(6568));
		}
    }

	@Override
	public double getRangePrayerMultiplier() {
		return 0.6;
	}
    
	@Override
	public void setNextFaceEntity(Entity target) {
		
	}
	
	@Override
	public void setTarget(Entity target) {
		
	}
	
}
