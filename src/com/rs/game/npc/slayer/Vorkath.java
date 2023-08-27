/**
 * 
 */
package com.rs.game.npc.slayer;

import java.util.List;

import com.rs.game.Animation;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.Drop;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.VorkathLair;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Jan 11, 2018
 */
@SuppressWarnings("serial")
public class Vorkath extends NPC {

	private VorkathLair lair;
	private boolean firstSpecial;
	private int movesCount;
	private List<Integer> acidPools;
	private NPC zombifiedSpawn;
	
	private static final int SLEEPING_ID = 28059, CUTSCENE_ID = 28058, AWAKEN_ID = 28061;
	public Vorkath(VorkathLair lair) {
		super(SLEEPING_ID, lair.getMap().getTile(new WorldTile(2269, 4062, 0)), -1, true, true);
		this.lair = lair;
		this.setDirection(0);
		setCantFollowUnderCombat(true); //also increases att distance
		setForceMultiAttacked(true); //due to spawn
		setIntelligentRouteFinder(true); //just to be considered boss
		setup();
	}

	@Override
	public void drop() {
		Player p = getMostDamageReceivedSourcePlayer();
		if(p != null && p.getBossKillcount().getOrDefault("Vorkath", 0) == 50) {
			// give assembler on 50th kill
			super.sendDrop(p, new Drop(52109, 1, 1));
		}
		super.drop();
	}

	private void setup() {
		firstSpecial = Utils.random(2) == 0;
		movesCount = 0;
	}
	
	@Override
	public void reset() {
		super.reset();
	/*	setNextNPCTransformation(SLEEPING_ID);
		setNextFaceEntity(null);
		setNextFaceWorldTile(new WorldTile(getRespawnTile().transform(0, -2, 0)));
		setup();*/
		if (zombifiedSpawn != null) {
			zombifiedSpawn.finish();
			zombifiedSpawn = null;
		}
	}
	
	private boolean isSleeping() {
		return getId() == SLEEPING_ID;
	}
	
	public void awake() {
		if (lair == null || !isSleeping())
			return;
		lair.getPlayer().setNextAnimation(new Animation(827));
		setNextAnimation(new Animation(27950));
		setNextNPCTransformation(CUTSCENE_ID);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (isSleeping() || !lair.isRunning())
					return;
				setNextNPCTransformation(AWAKEN_ID);
				setTarget(lair.getPlayer());
				getCombat().setCombatDelay(3);
			}
		}, 6);
	}
	
	public boolean getFirstSpecial() {
		firstSpecial = !firstSpecial;
		movesCount = 0;
		return firstSpecial;
	}
	
	public int getMovesCount() {
		return movesCount;
	}
	
	public void increaseMovesCount() {
		movesCount++;
	}

	public boolean isRunning() {
		return lair != null && lair.isRunning();
	}


	/**
	 * @return the acidPools
	 */
	public List<Integer> getAcidPools() {
		return acidPools;
	}


	/**
	 * @param acidPools the acidPools to set
	 */
	public void setAcidPools(List<Integer> acidPools) {
		this.acidPools = acidPools;
	}
	
	@Override
	public void handleIngoingHit(final Hit hit) {
		super.handleIngoingHit(hit);
		reduceHit(hit);
	}	
	
	
	public void reduceHit(Hit hit) {
		if ((hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE && hit.getLook() != HitLook.MAGIC_DAMAGE))
			return;
		if (hit.getSource() != null && hit.getSource().isFrozen()) {
			hit.setDamage(0);
			return;
		}
		if (acidPools == null)
			return;
		hit.setDamage(hit.getDamage()/2);
		
	}

	public boolean hasZombifiedSpawn() {
		return zombifiedSpawn != null && !zombifiedSpawn.hasFinished() && !zombifiedSpawn.isDead();
	}


	public void setZombifiedSpawn(NPC zombifiedSpawn) {
		this.zombifiedSpawn = zombifiedSpawn;
	}
}
