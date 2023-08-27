/**
 * 
 */
package com.rs.game.npc.gorrilas;

import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex) Nov 17, 2017
 */
@SuppressWarnings("serial")
public class DemonicGorilla extends TorturedGorilla {

	@SuppressWarnings("unused") // guess might not use these lol
	private static int MELEE_ID = 27144, RANGE_ID = 27145, MAGE_ID = 27146;

	private int[] damage;

	public DemonicGorilla(WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(MELEE_ID, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		switchPrayer(Utils.random(3));
		setDropRateFactor(3);
	}

	@Override
	public void finish() {
		switchPrayer(Utils.random(3));
		super.finish();
	}

	public void switchPrayer(int type) {
		damage = new int[3];
		setNextNPCTransformation(MELEE_ID + type);
	}

	private int getHitType(HitLook look) {
		if (look == HitLook.MELEE_DAMAGE)
			return 0;
		if (look == HitLook.RANGE_DAMAGE)
			return 1;
		return look == HitLook.MAGIC_DAMAGE ? 2 : -1;
	}

	@Override
	public void handleIngoingHit(Hit hit) {
		super.handleIngoingHit(hit);
		int hitType = getHitType(hit.getLook());
		if (hitType != -1) {
			int protectType = getId() - MELEE_ID;
			if (protectType == hitType)
				hit.setDamage(0);
			else if (damage != null) {
				damage[hitType] += hit.getDamage();
				if (damage[hitType] >= 500)
					switchPrayer(hitType);
			}
		}
	}

}
