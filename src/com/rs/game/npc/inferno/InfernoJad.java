package com.rs.game.npc.inferno;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.controllers.Inferno;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class InfernoJad extends InfernoNPC {

	private int healersCount;
	private InfernoNPC[] healers;
	
	public InfernoJad(WorldTile tile, int healersCount) {
		super(Inferno.JAD, tile);
		this.healersCount = healersCount;
	}
	
	@Override
	public void processNPC() {
		if (isDead())
			return;
		super.processNPC();
		double perc = (double)getHitpoints() / (double)getMaxHitpoints();
		if (perc <= 0.5 && healers == null)
			spawnHealers();
	}

	private void spawnHealers() {
		healers = new InfernoNPC[healersCount];
		int count = 0;
		int[][] dirs = Utils.getCoordOffsetsNear(4);
		for (int dir = 0; dir < dirs[0].length; dir++) {
			final WorldTile tile = new WorldTile(new WorldTile(getX() + dirs[0][dir], getY() + dirs[1][dir], getPlane()));
			if (World.isTileFree(tile.getPlane(), tile.getX(), tile.getY(), 2)) {
				healers[count] = new InfernoNPC(27701, tile);
				healers[count++].setTarget(this);
			}
			if (count == healers.length)
				break;
		}
	}

	public void finish() {
		finishHealers();
		super.finish();
	}
	
	public void finishHealers() {
		if (healers != null) {
			for (NPC npc : healers)
				if (npc != null)
					npc.finish();
		}
	}
}
