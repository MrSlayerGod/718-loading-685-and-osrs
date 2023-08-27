package com.rs.game.npc.fightkiln;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.FightKiln;

@SuppressWarnings("serial")
public class TokHaarKetDill extends FightKilnNPC {

	private int receivedHits;

	public TokHaarKetDill(int id, WorldTile tile, FightKiln controler) {
		super(id, tile, controler);
	}

	@Override
	public void handleIngoingHit(final Hit hit) {
		handleHit(hit);
		super.handleIngoingHit(hit);
	}

	public void handleHit(Hit hit) {
		if (receivedHits != -1) {
			Entity source = hit.getSource();
			if (source == null || !(source instanceof Player))
				return;
			hit.setDamage(0);
			if (hit.getLook() != HitLook.MELEE_DAMAGE)
				return;
			Player playerSource = (Player) source;
			int weaponId = playerSource.getEquipment().getWeaponId();
			if (weaponId == 1275 || weaponId == 14107 || weaponId == 14099 || weaponId == 13661 || weaponId == 15259 || weaponId == 20786 || weaponId == 50014 || weaponId == 43243 || weaponId == 53677) {
				receivedHits++;
				if ((weaponId == 1275 && receivedHits >= 5) || ((weaponId == 13661 || weaponId == 15259 || weaponId == 20786 || weaponId == 50014 || weaponId == 43243 || weaponId == 53677) && receivedHits >= 3)) {
					receivedHits = -1;
					setNextNPCTransformation(getId() + 1);
					playerSource.getPackets().sendGameMessage("Your pickaxe breaks the TokHaar-Ket-Dill's thick armour!");
				} else
					playerSource.getPackets().sendGameMessage("Your pickaxe slowy  cracks its way through the TokHaar-Ket-Dill's armour.");
			}
		}
	}

}
