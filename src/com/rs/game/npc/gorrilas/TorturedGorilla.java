/**
 * 
 */
package com.rs.game.npc.gorrilas;

import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Nov 17, 2017
 */
@SuppressWarnings("serial")
public class TorturedGorilla extends NPC {

	private int attackStyle;
	private int missedHits;
	
	public TorturedGorilla(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea,boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setAttackStyle();
	}
	
	public void miss() {
		if (++missedHits >= 3) 
			setAttackStyle();
	}
	
	@Override
	public void finish() {
		setAttackStyle();
		super.finish();
	}
	
	private void setAttackStyle() {
		missedHits = 0;
		int lastStyle = attackStyle;
		int tries = 0;
		while (attackStyle == lastStyle && tries < 10)
			attackStyle = Utils.random(3);
		setForceFollowClose(attackStyle == 0);
	}
	
	public int getAttackStyle() {
		return attackStyle;
	}
	
	
}
