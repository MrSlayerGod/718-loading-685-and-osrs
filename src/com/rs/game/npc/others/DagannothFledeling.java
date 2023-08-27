package com.rs.game.npc.others;

import java.util.ArrayList;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;

@SuppressWarnings("serial")
public class DagannothFledeling extends NPC {

	public DagannothFledeling(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
	}

	@Override
	public ArrayList<Entity> getPossibleTargets() {
		ArrayList<Entity> targets = getPossibleTargets(true, false);
		ArrayList<Entity> targetsCleaned = new ArrayList<Entity>();
		for (Entity t : targets) {
			if (!(t instanceof NPC) || ((NPC)t).getId() != 2879)
				continue;
			targetsCleaned.add(t);
		}
		return targetsCleaned;
	}
}
