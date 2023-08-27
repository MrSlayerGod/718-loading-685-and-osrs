/**
 * 
 */
package com.rs.game.npc.inferno;

import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;

/**
 * @author dragonkk(Alex)
 * Nov 26, 2017
 */
@SuppressWarnings("serial")
public class InfernoNPC extends NPC {


	public InfernoNPC(int id, WorldTile tile) {
		super(id, tile, -1, true, true);
		setForceMultiArea(true);
		setForceAgressive(true);
		setNoDistanceCheck(true);
		setLureDelay(0);
	}

}
