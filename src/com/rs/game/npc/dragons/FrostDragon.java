package com.rs.game.npc.dragons;

import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class FrostDragon extends NPC {

	private boolean magicOnly, orb;
	private int attackStage;

	public FrostDragon(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setMagicOnly();
	}

	@Override
	public void processHit(Hit hit) {
		if (orb) {
			if (hit.getSource() != null)
				hit.getSource().applyHit(new Hit(null, hit.getDamage(), HitLook.REFLECTED_DAMAGE));//Just incase
			return;
		}
		super.processHit(hit);
	}

	@Override
	public double getRangePrayerMultiplier() {
		return 0.6;
	}

	@Override
	public double getMagePrayerMultiplier() {
		return 0.6;
	}

	@Override
	public double getMeleePrayerMultiplier() {
		return 0.6;
	}

	public boolean isMagicOnly() {
		return magicOnly;
	}

	private void setMagicOnly() {
		this.magicOnly = Utils.random(2) == 0;
	}

	public void setOrb(boolean orb) {
		this.orb = orb;
	}

	public int getAttackStage() {
		return attackStage;
	}

	public void setAttackStage(int attackStage) {
		this.attackStage = attackStage;
	}

	@Override
	public void sendDeath(Entity source) {
		super.sendDeath(source);
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				setMagicOnly();
				attackStage = 0;
				orb = false;//Just incase idk
				setNextGraphics(new Graphics(1315));
			}
		}, getCombatDefinitions().getRespawnDelay() + 2);
	}

	public boolean getOrb() {
		return orb;
	}
}
