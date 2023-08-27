/**
 * 
 */
package com.rs.game.npc.zulrah;

import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;

/**
 * @author dragonkk(Alex)
 * Nov 5, 2017
 */
@SuppressWarnings("serial")
public class Snakeling extends NPC {


	private Zulrah zulrah;
	
	public Snakeling(Zulrah zulrah, WorldTile tile) {
		super(22045, tile, -1, true, true);
		this.zulrah = zulrah;
		setForceAgressive(true);
		setForceTargetDistance(64);
		setForceMultiArea(true);
		setIntelligentRouteFinder(true);
	}

	@Override
	public void processNPC() {
		if (zulrah != null && zulrah.hasFinished()) {
			finish();
			return;
		}
		super.processNPC();
	}
}
