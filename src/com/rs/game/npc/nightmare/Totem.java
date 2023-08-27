package com.rs.game.npc.nightmare;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.Hit.HitLook;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.TheNightmareInstance;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class Totem extends NPC {

	public static final int INACTIVE = 29434, ACTIVE = 29435, CHARGED = 29436;
			
	private static final String[] DIRECTIONS = {"north west", "north east", "south west", "south east"};
	public static final int[][] DIRECTIONS_ANGLE = {{-1,0}, {0, 1}, {0, -1}, {1, 0}};
			
	private int direction;
	private TheNightmare boss;
	
	public Totem(TheNightmare boss, WorldTile tile, int direction) {
		super(INACTIVE, tile, -1, true, true);
		this.direction = direction;
		this.boss = boss;
		setCantInteract(true);
		setCantFollowUnderCombat(true);
		setCantSetTargetAutoRelatio(true);
		setForceMultiArea(true);
		setDirection(Utils.getAngle(DIRECTIONS_ANGLE[direction][0], DIRECTIONS_ANGLE[direction][1]));
		setRandomWalk(0);
	}
	
	@Override
	public int getHitbarSprite(Player player) {
		return 22482;
	}
	
	@Override
	public void setTarget(Entity entity) {
		
	}
	
	@Override
	public void faceEntity(Entity entity) {
		
	}
	
	@Override
	public void sendDeath(final Entity source) {
		if (getId() == ACTIVE) {
			setNextNPCTransformation(CHARGED);
			TheNightmareInstance.sendMessage("The "+DIRECTIONS[direction]+" totem is fully charged.");
			boss.switchPhase();
		}
	}
	
	@Override
	public void handleIngoingHit(Hit hit) {
		if (hit.getLook() == HitLook.MAGIC_DAMAGE) 
			hit.setDamage(hit.getDamage() * 2);
		else if (hit.getLook() != HitLook.HEALED_DAMAGE) 
			hit.setDamage(hit.getDamage() / 3);
	}
	
	public void inactive() {
		setNextNPCTransformation(INACTIVE);
		reset();
		setDirection(Utils.getAngle(DIRECTIONS_ANGLE[direction][0], DIRECTIONS_ANGLE[direction][1]));
		setCantInteract(true);
	}
	
	@Override
	public void setHitpoints(int hp) {
		super.setHitpoints(hp);
		TheNightmareInstance.updateInterfaceAll();
	}
	
	@Override
	public int getMaxHitpoints() {
		return boss == null || getId() == INACTIVE ? 1 : boss.getTotemHP();
	}

	public void activate() {
		setNextNPCTransformation(ACTIVE);
		reset();
		setDirection(Utils.getAngle(DIRECTIONS_ANGLE[direction][0], DIRECTIONS_ANGLE[direction][1]));
		this.setCantInteract(false);
		
	}

}
