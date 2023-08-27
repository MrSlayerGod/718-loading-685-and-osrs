package com.rs.game.npc.combat.impl.superiorslayer;

import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class GreaterAbyssalDemon extends NPC {
	public static final int ID = 27410;

	public GreaterAbyssalDemon(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setDropRateFactor(1.5);
	}

	@Override
	public void processNPC() {
		super.processNPC();
		Entity target = getCombat().getTarget();
		if (target != null && Utils.isOnRange(target.getX(), target.getY(), target.getSize(), getX(), getY(), getSize(), 4) && Utils.random(50) == 0) {
			sendTeleport(target);
			sendTeleport(this);
		}
	}

	private void sendTeleport(Entity entity) {
		entity.setNextGraphics(new Graphics(409));
		entity.setNextWorldTile(Utils.getFreeTile(new WorldTile(entity), 1));
	}

	/* empty for combat def auto init */
	public GreaterAbyssalDemon() { }
}
