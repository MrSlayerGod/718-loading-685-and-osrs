package com.rs.game.npc.dungeonnering;

import com.rs.game.WorldTile;
import com.rs.game.player.CombatDefinitions;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.dungeoneering.DungeonManager;
import com.rs.utils.WeaponTypesLoader.WeaponType;

@SuppressWarnings("serial")
public class SkeletonMage extends DungeonNPC {

	public SkeletonMage(int id, WorldTile tile, DungeonManager manager, double multiplier) {
		super(id, tile, manager, multiplier);
	}

	private static final WeaponType[][] WEAKNESS =
	{
	{ new WeaponType(Combat.RANGE_TYPE, -1) },
	{ new WeaponType(Combat.MELEE_TYPE, CombatDefinitions.CRUSH_ATTACK) },
	{ new WeaponType(Combat.MELEE_TYPE, CombatDefinitions.STAB_ATTACK) } };

	public WeaponType[][] getWeaknessStyle() {
		return WEAKNESS;
	}
}