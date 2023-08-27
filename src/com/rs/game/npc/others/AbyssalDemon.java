package com.rs.game.npc.others;

import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class AbyssalDemon extends NPC {

	public AbyssalDemon(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
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
}
