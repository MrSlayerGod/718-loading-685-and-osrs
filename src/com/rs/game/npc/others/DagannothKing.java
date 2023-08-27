package com.rs.game.npc.others;

import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;

@SuppressWarnings("serial")
public class DagannothKing extends NPC {

	public DagannothKing(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setDropRateFactor(20);
		if (this.getId() == 2881)
			this.setDropRateFactor(this.getDropRateFactor() * 1.5);
	}
	
	@Override
	public void handleIngoingHit(Hit hit) {
		if (hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE && hit.getLook() != HitLook.MAGIC_DAMAGE)
			return;
		if((getId() == 2881 && hit.getLook() != HitLook.MELEE_DAMAGE)
				|| (getId() == 2882 && hit.getLook() != HitLook.RANGE_DAMAGE)
				|| (getId() == 2883 && hit.getLook() != HitLook.MAGIC_DAMAGE)) {
			hit.setDamage(0);
			if (hit.getSource() instanceof Player)
				((Player)hit.getSource()).getPackets().sendGameMessage("This npc is invulnerable to this attack style.");
		}
		super.handleIngoingHit(hit);
	}
}
