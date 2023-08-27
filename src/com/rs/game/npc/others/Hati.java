package com.rs.game.npc.others;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;

@SuppressWarnings("serial")
public class Hati extends NPC {

	public Hati(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setIntelligentRouteFinder(true);
	}
	
	@Override
	public void sendDeath(Entity killer) {
		if (killer instanceof Player) {
			Player player = (Player) killer;
			int bonusXP = player.isUltimateIronman() || player.isHCIronman() ? 25000 : 50000;
			if (player.getEquipment().getBootsId() == 23029) {
				player.getCharges().addCharges(23029, bonusXP, Equipment.SLOT_FEET);
				player.getPackets().sendGameMessage("Your boots gain "+bonusXP+" agility bonus XP.");
			} else
				player.getPackets().sendGameMessage("You would have received "+bonusXP+" had you been wearing Skoll boots.");
		}
		super.sendDeath(killer);
	}

	public static int addSkillXP(Player player, int skill, int xp) {
		if (skill != Skills.AGILITY || player.getEquipment().getBootsId() != 23029)
			return 0;
		int xpBonus = Math.min(xp, player.getCharges().getCharges(23029));
		if (xpBonus > 0)
			player.getCharges().addCharges(23029, -xpBonus, Equipment.SLOT_FEET);
		return xpBonus;
		
	}
}
