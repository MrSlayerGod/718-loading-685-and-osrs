package com.rs.game.player.content;

import java.util.List;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Entity;
import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.Wilderness;
import com.rs.utils.Utils;

public final class Combat {
	
	public static final int MELEE_TYPE = 0, RANGE_TYPE = 1, MAGIC_TYPE = 2, ALL_TYPE = 3;
	public static final int NONE_STYLE = 0, ARROW_STYLE = 8, BOLT_STYLE = 9, THROWN_STYLE = 10;

	public static boolean rollHit(double att, double def) {
		if (att < 0) // wont happen unless low att lv plus negative bonus
			return false;
		if (def < 0) // wont happen unless low def lv plus negative bonus
			return true;
		return Utils.random((int) (att + def)) >= def;
	}

	public static boolean instantProtectPrayer(Entity target) {
		return target instanceof Player;
	}

	public static boolean hasAntiDragProtection(Entity target) {
		if (target instanceof NPC)
			return false;
		Player p2 = (Player) target;
		int shieldId = p2.getEquipment().getShieldId();
		return shieldId == 1540 || shieldId == 11283 || shieldId == 11284 || shieldId == 16933 || shieldId == 52002 || shieldId == 52003
				|| shieldId == 51633 || shieldId == 51634;
	}

	public static Entity getLastTarget(Player from) {
		Region region = World.getRegion(from.getRegionId());
		if (from.isCanPvp()) {
			List<Integer> playerIndexes = region.getPlayerIndexes();
			if (playerIndexes != null) {
				for (int playerIndex : playerIndexes) {
					Player player = World.getPlayers().get(playerIndex);
					if (player == null || player.isDead() || player.hasFinished() || !player.isCanPvp() || (from.getAttackedBy() != player && from.getAttackedByDelay() > Utils.currentTimeMillis()) || player.getAttackedBy() != from || !from.withinDistance(player, 16))
						continue;
					return player;
				}
			}
		}
		List<Integer> npcsIndexes = region.getNPCsIndexes();
		if (npcsIndexes != null) {
			for (int npcIndex : npcsIndexes) {
				NPC npc = World.getNPCs().get(npcIndex);
				if (npc == null || npc.isDead() || npc.hasFinished() || (from.getAttackedBy() != npc && from.getAttackedByDelay() > Utils.currentTimeMillis()) || npc.getAttackedBy() != from || !from.withinDistance(npc, 16))
					continue;
				return npc;
			}
		}
		return null;
	}

	public static boolean hasRingOfWealth(Player player) {
		int ringId = player.getEquipment().getRingId();
		return ringId == 25741 || ringId == 25488 || ringId == 2572 || (ringId >= 20653 && ringId <= 20659) || ringId == 42785;
	}

	public static boolean hasAntiFireProtection(Entity target) {
		return target instanceof Player && ((Player) target).hasFireImmunity();
	}
	
	public static boolean hasRoyalCrossbow(Player player) {
		int weaponId = player.getEquipment().getWeaponId();
		return (weaponId == 24338 || weaponId == 24339);
	}

	public static int getDefenceEmote(Entity target) {
		if (target instanceof NPC) {
			NPC n = (NPC) target;
			return n.getCombatDefinitions().getDefenceEmote();
		} else {
			Player p = (Player) target;
			int shieldId = p.getEquipment().getShieldId();
			String shieldName = shieldId == -1 ? null : ItemConfig.forID(shieldId).getName().toLowerCase();
			if (shieldId == -1 || (shieldName.contains("book") && shieldId != 18346)) {
				int weaponId = p.getEquipment().getWeaponId();
				if (weaponId == -1)
					return 424;
				if (weaponId == 7671 || weaponId == 7673)
					return 3678;
				String weaponName = ItemConfig.forID(weaponId).getName().toLowerCase();
				if (weaponName != null && !weaponName.equals("null")) {
					if (weaponName.contains("ballista"))
						return -1;
					if (weaponName.contains("scimitar") || weaponName.contains("korasi sword"))
						return 15074;
					if (weaponName.contains("whip"))
						return 11974;
					if (weaponName.contains("staff of light"))
						return 12806;
					if (weaponName.contains("longsword") || weaponName.equals("darklight") || weaponName.equals("silverlight") || weaponName.contains("excalibur")|| weaponName.equals("arclight"))
						return 388;
					if (weaponName.contains("dagger"))
						return 378;
					if (weaponName.contains("rapier"))
						return 13038;
					if (weaponName.contains("pickaxe"))
						return 397;
					if (weaponName.contains("mace") || weaponName.contains("annihilation"))
						return 403;
					if (weaponName.contains("claws"))
						return 404;
					if (weaponName.contains("hatchet"))
						return 397;
					if (weaponName.contains("greataxe"))
						return 12004;
					if (weaponName.contains("wand"))
						return 415;
					if (weaponName.contains("chaotic staff") || weaponId == 25699)
						return 13046;
					if (weaponName.contains("staff") || weaponName.contentEquals("obliteration"))
						return 420;
					if (weaponName.contains("warhammer") || weaponName.contains("tzhaar-ket-em"))
						return 403;
					if (weaponName.contains("maul") || weaponName.contains("tzhaar-ket-om"))
						return 1666;
					if (weaponName.contains("zamorakian spear"))
						return 12008;
					if (weaponName.contains("spear") || weaponName.contains("halberd") || weaponName.contains("hasta"))
						return 430;
					if (weaponName.contains("2h sword") || weaponName.contains("godsword") || weaponName.equals("saradomin sword"))
						return 7050;
					if (weaponName.contains("bulwark"))
						return 27517;
				}
				return 424;
			}
			if (shieldName != null) {
				if (shieldName.contains("shield") || shieldName.contains("toktz-ket-xil"))
					return 1156;
				if (shieldName.contains("defender"))
					return 4177;
			}
			switch (shieldId) {
			case -1:
			default:
				return 424;
			}
		}
	}

	public static boolean isUndead(Entity target) {
		if (target instanceof Player)
			return false;
		NPC npc = (NPC) target;
		return npc.isUndead(); //pest

	}

	private Combat() {
	}

	public static boolean hasDarkbow(Player player) {
		int weaponId = player.getEquipment().getWeaponId();
		return (weaponId == 11235 || weaponId == 25380 || weaponId >= 15701 && weaponId <= 15704)
				|| weaponId == 25533 || weaponId == 25662 || weaponId == 25662 || weaponId == 25592 || weaponId == 25609 || weaponId == 25575 || weaponId == 25539 || weaponId == 25617 || weaponId == 25544;
	}
	
	public static double getDragonFireMultiplier(Player player) {
		final boolean hasPrayerProtection = player.getPrayer().isMageProtecting();
		final boolean hasShieldProtection = Combat.hasAntiDragProtection(player);
		final boolean hasPotionProtection = player.getFireImmune() > Utils.currentTimeMillis();
		double multiplier = 1;
		if (hasShieldProtection) {
			multiplier = hasPotionProtection ? 0 : 0.1;
		} else if (hasPotionProtection)
			multiplier = player.isSuperAntiFire() ? 0 : 0.1;
		else if (hasPrayerProtection)
			multiplier = 0.1;
		return multiplier;
	}
	
	public static boolean hasCustomWeapon(Player player) {
		int id = player.getEquipment().getWeaponId();
		int shieldID = player.getEquipment().getShieldId();
		return (id >= 25354 && id < Settings.OSRS_ITEM_OFFSET)
				|| (shieldID >= 25354 && shieldID < Settings.OSRS_ITEM_OFFSET);
	}
	
	public static boolean hasCustomWeaponOnWild(Player player) {
		/*if (!(player.getControlerManager().getControler() instanceof Wilderness))
			return false;*/
		return player.isCanPvp() && hasCustomWeapon(player);
	}
}
