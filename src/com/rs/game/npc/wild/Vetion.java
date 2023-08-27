/**
 * 
 */
package com.rs.game.npc.wild;

import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Oct 31, 2017
 */
@SuppressWarnings("serial")
public class Vetion extends NPC {

	private static final int ID = 26611, REBORN_ID = 26612, DOG_ID = 26613;
	
	private int dogsSummoned;
	private NPC[] dogs = new NPC[2];
	private int timer;
	
	public Vetion(WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(ID, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setDropRateFactor(3);
	}
	
	
	@Override
	public void processNPC() {
		if (isDead())
			return;
		if (isReborn()) {
			timer--;
			if (timer % 100 == 0) {
				if (timer == 0) {
					setNextNPCTransformation(ID);
				} else {
					for (Entity target : getPossibleTargets()) {
						if (target instanceof Player)
							((Player)target).getPackets().sendGameMessage("You have less than "+timer/100+" minutes to deliver your current Aluf Aloft Delivery.");
					}
				}
			}
		}
		super.processNPC();
	}
	
	public void summonDogs(Entity target) {
		if (dogsSummoned >= (isReborn() ? 2 : 1) || getHitpoints() >= getMaxHitpoints()/2) 
			return;
		dogsSummoned++;
		this.setNextForceTalk(new ForceTalk(isReborn() ? "Bahh! Go, Dogs!!" : "Kill, my pets!"));
		int[][] dirs = Utils.getCoordOffsetsNear(2);
		int count = 0;
		for (int dir = 0; dir < dirs[0].length; dir++) {
			final WorldTile tile = new WorldTile(new WorldTile(target.getX() + dirs[0][dir], target.getY() + dirs[1][dir], target.getPlane()));
			if (World.isTileFree(tile.getPlane(), tile.getX(), tile.getY(), 2)) {
				if (dogs[count] != null) 
					dogs[count].finish();
				NPC dog = dogs[count++] = new NPC(DOG_ID + (isReborn() ? 1 : 0), tile, -1, true, true);
				dog.setTarget(target);
				dog.setForceMultiArea(true);
			}
			if (count == 2)
				break;
		}
	}
	
	public void resetDogs() {
		dogsSummoned = 0;
		for (int i = 0; i < dogs.length; i++) {
			if (dogs[i] != null) {
				dogs[i].finish();
				dogs[i] = null;
			}
		}
	}
	
	public boolean isReborn() {
		return getId() == REBORN_ID;
	}
	
	public boolean isDogAlive() {
		for (int i = 0; i < dogs.length; i++) {
			if (dogs[i] != null && !dogs[i].hasFinished())
				return true;
		}
		return false;
	}
	
	@Override
	public void handleIngoingHit(Hit hit) {
		if (isDogAlive()) {
			Entity target = hit.getSource();
			if (target instanceof Player)
				((Player)target).getPackets().sendGameMessage("Vet'ion is currently invulnerable.", true);
			hit.setDamage(0);
		}
		super.handleIngoingHit(hit);
	}
	
	@Override
	public void sendDeath(final Entity source) {
		if (source != null && dogsSummoned < (isReborn() ? 2 : 1)) {
			setHitpoints(1);
			summonDogs(source);
			return;
		}
		if (!isReborn()) {
			setHitpoints(getMaxHitpoints());
			setNextNPCTransformation(REBORN_ID);
			timer = 500;
			this.setNextForceTalk(new ForceTalk("Do it again!"));
			for (Entity target : getPossibleTargets()) {
				if (target instanceof Player)
					((Player)target).getPackets().sendGameMessage("It heals some health.");
			}
			return;
		}
		super.sendDeath(source);
	}
	
	public void finish() {
		setNPC(ID); 
		resetDogs();
		super.finish();
	}
}
