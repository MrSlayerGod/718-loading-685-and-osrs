/**
 * 
 */
package com.rs.game.npc.skotizo;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.npc.NPC;
import com.rs.game.player.controllers.SkotizoLair;

/**
 * @author dragonkk(Alex)
 * Dec 20, 2017
 */
@SuppressWarnings("serial")
public class SkotizoAltar extends NPC {

	private SkotizoLair lair;
	private int index;
	
	public SkotizoAltar(SkotizoLair lair, int index) {
		super(27288, lair.getMap().getTile(SkotizoLair.ALTARS[index]), -1, true, true);
		this.lair = lair;
		this.index = index;
		setDropRateFactor(2);
		setForceMultiArea(true);
		setIntelligentRouteFinder(true);
	}
	
	
	@Override
	public void setNextFaceEntity(Entity entity) {

	}
	
	@Override
	public void setTarget(Entity target) {
		
	}
	@Override
	public void handleIngoingHit(Hit hit) {
		super.handleIngoingHit(hit);
		if (lair != null && hit.getSource() == lair.getPlayer() && lair.getPlayer().getEquipment().getWeaponId() == 49675) 
			hit.setDamage(getHitpoints());
	}
	
	@Override
	public void finish() {
		if (lair != null)
			lair.setAltar(index, false);
		super.finish();
	}
}
