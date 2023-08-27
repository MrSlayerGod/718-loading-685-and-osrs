package com.rs.game.player.content;

import java.util.ArrayList;
import java.util.List;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.minigames.Sawmill;
import com.rs.game.minigames.Sawmill.Plank;
import com.rs.game.minigames.clanwars.ClanWarRequestController;
import com.rs.game.minigames.duel.DuelArena;
import com.rs.game.minigames.pktournament.PkTournament;
import com.rs.game.minigames.stealingcreation.StealingCreationController;
import com.rs.game.npc.NPC;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Cooking.Cookables;
import com.rs.game.player.actions.DreamSpellAction;
import com.rs.game.player.actions.HomeTeleport;
import com.rs.game.player.actions.Smelting.SmeltingBar;
import com.rs.game.player.actions.WaterFilling.Fill;
import com.rs.game.player.content.dungeoneering.DungeonConstants;
import com.rs.game.player.content.prayer.Burying.Bone;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.controllers.DungeonController;
import com.rs.game.player.controllers.GodWars;
import com.rs.game.player.controllers.Kalaboss;
import com.rs.game.player.controllers.Wilderness;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.WorldPacketsDecoder;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

/*
 * content package used for static stuff
 */
public class Magic {

	public static final int MAGIC_TELEPORT = 0, ITEM_TELEPORT = 1, OBJECT_TELEPORT = 2;

	public static final int AIR_RUNE = 556;
	public static final int WATER_RUNE = 555;
	public static final int EARTH_RUNE = 557;
	public static final int FIRE_RUNE = 554;
	public static final int BODY_RUNE = 559;
	public static final int MIND_RUNE = 558;
	public static final int NATURE_RUNE = 561;
	public static final int CHAOS_RUNE = 562;
	public static final int COSMIC_RUNE = 564;
	public static final int DEATH_RUNE = 560;
	public static final int BLOOD_RUNE = 565;
	public static final int SOUL_RUNE = 566;
	public static final int ASTRAL_RUNE = 9075;
	public static final int LAW_RUNE = 563;
	@SuppressWarnings("unused")
	private static final int STEAM_RUNE = 4694;
	@SuppressWarnings("unused")
	private static final int MIST_RUNE = 4695;
	@SuppressWarnings("unused")
	private static final int DUST_RUNE = 4696;
	@SuppressWarnings("unused")
	private static final int SMOKE_RUNE = 4697;
	@SuppressWarnings("unused")
	private static final int MUD_RUNE = 4698;
	@SuppressWarnings("unused")
	private static final int LAVA_RUNE = 4699;
	private static final int ARMADYL_RUNE = 21773;
	private static final int ELEMENTAL_RUNE = 12850;
	private static final int CATALYTIC_RUNE = 12851;

	public static final boolean hasInfiniteRunes(int runeId, int weaponId, int shieldId) {
		if (runeId == AIR_RUNE) {
			if (weaponId == 42000 || weaponId == 23044 || weaponId == 1381 || weaponId == 1397 || weaponId == 1405 || weaponId == 21777 || weaponId == 11736 || weaponId == 11738) // air
				// staff
				return true;
		} else if (runeId == WATER_RUNE) {
			if (weaponId == 6562 || weaponId == 51006 || weaponId == 23045 || weaponId == 1383 || weaponId == 1395 || weaponId == 1403 || shieldId == 18346 || weaponId == 6563 || weaponId == 6726) // water
				// staff
				return true;
		} else if (runeId == EARTH_RUNE) {
			if (weaponId == 6562 || weaponId == 23046 || weaponId == 1385 || weaponId == 1399 || weaponId == 1407 || weaponId == 3053 || weaponId == 3054 || weaponId == 6563 || weaponId == 6726) // earth
				// staff
				return true;
		} else if (runeId == FIRE_RUNE) {
			if (shieldId == 50714 || weaponId == 42000 || weaponId == 23047 || weaponId == 1387 || weaponId == 1393 || weaponId == 1401 || weaponId == 3053 || weaponId == 3054 || weaponId == 11736 || weaponId == 11738) // fire
				// staff
				return true;
		} else if (runeId == 17780) {//air staff
			if (weaponId == 16170 || weaponId == 16169 || weaponId == 17009 || weaponId == 17011)
				return true;
		} else if (runeId == 17781) {//water staff
			if (weaponId == 16163 || weaponId == 16164 || weaponId == 16999 || weaponId == 16997)
				return true;
		} else if (runeId == 17782) {//earth staff
			if (weaponId == 16165 || weaponId == 16166 || weaponId == 17001 || weaponId == 17003)
				return true;
		} else if (runeId == 17783) {//fire staff
			if (weaponId == 16167 || weaponId == 16168 || weaponId == 17005 || weaponId == 17007)
				return true;
		}
		return weaponId == 24457 || weaponId == 255450 || weaponId == 25641;
	}

	public static boolean hasSpecialRunes(Player player, int runeId, int amountRequired) {
		if (player.getInventory().containsItem(ELEMENTAL_RUNE, amountRequired)) {
			if (runeId == AIR_RUNE || runeId == WATER_RUNE || runeId == EARTH_RUNE || runeId == FIRE_RUNE)
				return true;
		}
		if (player.getInventory().containsItem(CATALYTIC_RUNE, amountRequired)) {
			if (runeId == ARMADYL_RUNE || runeId == MIND_RUNE || runeId == CHAOS_RUNE || runeId == DEATH_RUNE || runeId == BLOOD_RUNE || runeId == BODY_RUNE || runeId == NATURE_RUNE || runeId == ASTRAL_RUNE || runeId == SOUL_RUNE || runeId == LAW_RUNE)
				return true;
		}
		return false;
	}

	public static int getRuneForId(int runeId) {
		if (runeId == AIR_RUNE || runeId == WATER_RUNE || runeId == EARTH_RUNE || runeId == FIRE_RUNE)
			return ELEMENTAL_RUNE;
		else if (runeId == ARMADYL_RUNE || runeId == DEATH_RUNE || runeId == MIND_RUNE || runeId == CHAOS_RUNE || runeId == BLOOD_RUNE || runeId == BODY_RUNE || runeId == NATURE_RUNE || runeId == ASTRAL_RUNE || runeId == SOUL_RUNE || runeId == LAW_RUNE)
			return CATALYTIC_RUNE;
		return -1;
	}

	public static final boolean checkCombatSpell(Player player, int spellId, int set, boolean delete) {
		if (spellId == 65531 || spellId == 65532 || spellId == 65533 || spellId == 65534 || spellId == 65535 || spellId == 65536 || spellId == 65530)
			return true;
		switch (player.getCombatDefinitions().getSpellBook()) {
		case 193:
			switch (spellId) {
			case 28:
				if (!checkSpellRequirements(player, 50, delete, CHAOS_RUNE, 2, DEATH_RUNE, 2, FIRE_RUNE, 1, AIR_RUNE, 1))
					return false;
				break;
			case 32:
				if (!checkSpellRequirements(player, 52, delete, CHAOS_RUNE, 2, DEATH_RUNE, 2, AIR_RUNE, 1, SOUL_RUNE, 1))
					return false;
				break;
			case 24:
				if (!checkSpellRequirements(player, 56, delete, CHAOS_RUNE, 2, DEATH_RUNE, 2, BLOOD_RUNE, 1))
					return false;
				break;
			case 20:
				if (!checkSpellRequirements(player, 58, delete, CHAOS_RUNE, 2, DEATH_RUNE, 2, WATER_RUNE, 2))
					return false;
				break;
			case 30:
				if (!checkSpellRequirements(player, 62, delete, CHAOS_RUNE, 4, DEATH_RUNE, 2, FIRE_RUNE, 2, AIR_RUNE, 2))
					return false;
				break;
			case 34:
				if (!checkSpellRequirements(player, 64, delete, CHAOS_RUNE, 4, DEATH_RUNE, 2, AIR_RUNE, 1, SOUL_RUNE, 2))
					return false;
				break;
			case 26:
				if (!checkSpellRequirements(player, 68, delete, CHAOS_RUNE, 4, DEATH_RUNE, 2, BLOOD_RUNE, 2))
					return false;
				break;
			case 22:
				if (!checkSpellRequirements(player, 70, delete, CHAOS_RUNE, 4, DEATH_RUNE, 2, WATER_RUNE, 4))
					return false;
				break;
			case 29:
				if (!checkSpellRequirements(player, 74, delete, DEATH_RUNE, 2, BLOOD_RUNE, 2, FIRE_RUNE, 2, AIR_RUNE, 2))
					return false;
				break;
			case 33:
				if (!checkSpellRequirements(player, 76, delete, DEATH_RUNE, 2, BLOOD_RUNE, 2, AIR_RUNE, 2, SOUL_RUNE, 2))
					return false;
				break;
			case 25:
				if (!checkSpellRequirements(player, 80, delete, DEATH_RUNE, 2, BLOOD_RUNE, 4))
					return false;
				break;
			case 21:
				if (!checkSpellRequirements(player, 82, delete, DEATH_RUNE, 2, BLOOD_RUNE, 2, WATER_RUNE, 3))
					return false;
				break;
			case 31:
				if (!checkSpellRequirements(player, 86, delete, DEATH_RUNE, 4, BLOOD_RUNE, 2, FIRE_RUNE, 4, AIR_RUNE, 4))
					return false;
				break;
			case 35:
				if (!checkSpellRequirements(player, 88, delete, DEATH_RUNE, 4, BLOOD_RUNE, 2, AIR_RUNE, 4, SOUL_RUNE, 3))
					return false;
				break;
			case 27:
				if (!checkSpellRequirements(player, 92, delete, DEATH_RUNE, 4, BLOOD_RUNE, 4, SOUL_RUNE, 1))
					return false;
				break;
			case 23:
				if (!checkSpellRequirements(player, 94, delete, DEATH_RUNE, 4, BLOOD_RUNE, 2, WATER_RUNE, 6))
					return false;
				break;
			case 36: // Miasmic rush.
				if (!checkSpellRequirements(player, 61, delete, CHAOS_RUNE, 2, EARTH_RUNE, 1, SOUL_RUNE, 1)) {
					return false;
				}
				int weaponId = player.getEquipment().getWeaponId();
				if (weaponId != 13867 && weaponId != 13869 && weaponId != 13941 && weaponId != 13943) {
					player.getPackets().sendGameMessage("You need a Zuriel's staff to cast this spell.");
					return false;
				}
				break;
			case 38: // Miasmic burst.
				if (!checkSpellRequirements(player, 73, delete, CHAOS_RUNE, 4, EARTH_RUNE, 2, SOUL_RUNE, 2)) {
					return false;
				}
				weaponId = player.getEquipment().getWeaponId();
				if (weaponId != 13867 && weaponId != 13869 && weaponId != 13941 && weaponId != 13943) {
					player.getPackets().sendGameMessage("You need a Zuriel's staff to cast this spell.");
					return false;
				}
				break;
			case 37: // Miasmic blitz.
				if (!checkSpellRequirements(player, 85, delete, BLOOD_RUNE, 2, EARTH_RUNE, 3, SOUL_RUNE, 3)) {
					return false;
				}
				weaponId = player.getEquipment().getWeaponId();
				if (weaponId != 13867 && weaponId != 13869 && weaponId != 13941 && weaponId != 13943) {
					player.getPackets().sendGameMessage("You need a Zuriel's staff to cast this spell.");
					return false;
				}
				break;
			case 39: // Miasmic barrage.
				if (!checkSpellRequirements(player, 97, delete, BLOOD_RUNE, 4, EARTH_RUNE, 4, SOUL_RUNE, 4)) {
					return false;
				}
				weaponId = player.getEquipment().getWeaponId();
				if (weaponId != 13867 && weaponId != 13869 && weaponId != 13941 && weaponId != 13943) {
					player.getPackets().sendGameMessage("You need a Zuriel's staff to cast this spell.");
					return false;
				}
				break;
			default:
				return false;
			}
			break;
		case 950:
			switch (spellId) {
			case 25:
				if (!checkSpellRequirements(player, 1, delete, true, 17780, 1, 17784, 1))
					return false;
				break;
			case 27:
				if (!checkSpellRequirements(player, 5, delete, true, 17781, 1, 17780, 1, 17784, 1))
					return false;
				break;
			case 28:
				if (!checkSpellRequirements(player, 9, delete, true, 17782, 2, 17780, 1, 17784, 1))
					return false;
				break;
			case 30:
				if (!checkSpellRequirements(player, 13, delete, true, 17783, 3, 17780, 2, 17784, 1))
					return false;
				break;
			case 32: // air bolt
				if (!checkSpellRequirements(player, 17, delete, true, 17780, 2, 17785, 1))
					return false;
				break;
			case 36: // water bolt
				if (!checkSpellRequirements(player, 23, delete, true, 17781, 2, 17780, 2, 17785, 1))
					return false;
				break;
			case 37: // earth bolt
				if (!checkSpellRequirements(player, 29, delete, true, 17782, 3, 17780, 2, 17785, 1))
					return false;
				break;
			case 41: // fire bolt
				if (!checkSpellRequirements(player, 35, delete, true, 17783, 4, 17780, 3, 17785, 1))
					return false;
				break;
			case 42: // air blast
				if (!checkSpellRequirements(player, 41, delete, true, 17780, 3, 17786, 1))
					return false;
				break;
			case 43: // water blast
				if (!checkSpellRequirements(player, 47, delete, true, 17781, 3, 17780, 3, 17786, 1))
					return false;
				break;
			case 45: // earth blast
				if (!checkSpellRequirements(player, 53, delete, true, 17782, 4, 17780, 3, 17786, 1))
					return false;
				break;
			case 47: // fire blast
				if (!checkSpellRequirements(player, 59, delete, true, 17783, 5, 17780, 4, 17786, 1))
					return false;
				break;
			case 48: // air wave
				if (!checkSpellRequirements(player, 62, delete, true, 17780, 5, 17787, 1))
					return false;
				break;
			case 49: // water wave
				if (!checkSpellRequirements(player, 65, delete, true, 17781, 7, 17780, 5, 17787, 1))
					return false;
				break;
			case 54: // earth wave
				if (!checkSpellRequirements(player, 70, delete, true, 17782, 7, 17780, 5, 17787, 1))
					return false;
				break;
			case 58: // fire wave
				if (!checkSpellRequirements(player, 75, delete, true, 17783, 7, 17780, 5, 17787, 1))
					return false;
				break;
			case 61:// air surge
				if (!checkSpellRequirements(player, 81, delete, true, 17780, 7, 17786, 1, 17787, 1))
					return false;
				break;
			case 62:// water surge
				if (!checkSpellRequirements(player, 85, delete, true, 17781, 10, 17780, 7, 17786, 1, 17787, 1))
					return false;
				break;
			case 63:// earth surge
				if (!checkSpellRequirements(player, 90, delete, true, 17782, 10, 17780, 7, 17786, 1, 17787, 1))
					return false;
				break;
			case 67:// fire surge
				if (!checkSpellRequirements(player, 95, delete, true, 17783, 10, 17780, 7, 17786, 1, 17787, 1))
					return false;
				break;
			case 34:// bind
				if (!checkSpellRequirements(player, 20, delete, true, 17782, 3, 17781, 3, 17791, 2))
					return false;
				break;
			case 44:// snare
				if (!checkSpellRequirements(player, 50, delete, true, 17782, 4, 17781, 4, 17791, 3))
					return false;
				break;
			case 59:// entangle
				if (!checkSpellRequirements(player, 79, delete, true, 17782, 5, 17781, 5, 17791, 4))
					return false;
				break;
			case 26: //confuse
				if (!checkSpellRequirements(player, 3, delete, true, WATER_RUNE, 3, EARTH_RUNE, 2, BODY_RUNE, 1))
					return false;
				break;
			case 29: //weaken
				if (!checkSpellRequirements(player, 11, delete, true, WATER_RUNE, 3, EARTH_RUNE, 2, BODY_RUNE, 1))
					return false;
				break;
			case 33: //curse
				if (!checkSpellRequirements(player, 19, delete, true, WATER_RUNE, 3, EARTH_RUNE, 2, BODY_RUNE, 1))
					return false;
				break;
			case 50: //vulnerability
				if (!checkSpellRequirements(player, 66, delete, true, EARTH_RUNE, 5, WATER_RUNE, 5, SOUL_RUNE, 1))
					return false;
				break;
			case 56: //enfeeble
				if (!checkSpellRequirements(player, 73, delete, true, EARTH_RUNE, 8, WATER_RUNE, 8, SOUL_RUNE, 1))
					return false;
				break;
			case 60: //stun
				if (!checkSpellRequirements(player, 80, delete, true, EARTH_RUNE, 12, WATER_RUNE, 12, SOUL_RUNE, 1))
					return false;
			}
			break;
		case 192:
			switch (spellId) {
			case 98:
				if (!checkSpellRequirements(player, 1, delete, AIR_RUNE, 1))
					return false;
				break;
			case 25:
				if (!checkSpellRequirements(player, 1, delete, AIR_RUNE, 1, MIND_RUNE, 1))
					return false;
				break;
			case 28:
				if (!checkSpellRequirements(player, 5, delete, WATER_RUNE, 1, AIR_RUNE, 1, MIND_RUNE, 1))
					return false;
				break;
			case 30:
				if (!checkSpellRequirements(player, 9, delete, EARTH_RUNE, 2, AIR_RUNE, 1, MIND_RUNE, 1))
					return false;
				break;
			case 32:
				if (!checkSpellRequirements(player, 13, delete, FIRE_RUNE, 3, AIR_RUNE, 2, MIND_RUNE, 1))
					return false;
				break;
			case 34: // air bolt
				if (!checkSpellRequirements(player, 17, delete, AIR_RUNE, 2, CHAOS_RUNE, 1))
					return false;
				break;
			case 36:// bind
				if (!checkSpellRequirements(player, 20, delete, EARTH_RUNE, 3, WATER_RUNE, 3, NATURE_RUNE, 2))
					return false;
				break;
			case 55: // snare
				if (!checkSpellRequirements(player, 50, delete, EARTH_RUNE, 4, WATER_RUNE, 4, NATURE_RUNE, 3))
					return false;
				break;
			case 81:// entangle
				if (!checkSpellRequirements(player, 79, delete, EARTH_RUNE, 5, WATER_RUNE, 5, NATURE_RUNE, 4))
					return false;
				break;
			case 39: // water bolt
				if (!checkSpellRequirements(player, 23, delete, WATER_RUNE, 2, AIR_RUNE, 2, CHAOS_RUNE, 1))
					return false;
				break;
			case 42: // earth bolt
				if (!checkSpellRequirements(player, 29, delete, EARTH_RUNE, 3, AIR_RUNE, 2, CHAOS_RUNE, 1))
					return false;
				break;
			case 45: // fire bolt
				if (!checkSpellRequirements(player, 35, delete, FIRE_RUNE, 4, AIR_RUNE, 3, CHAOS_RUNE, 1))
					return false;
				break;
			case 54: //iban blast
				if (player.getEquipment().getWeaponId() != 1409) {
					player.getPackets().sendGameMessage("You need to be equipping an iban's staff to cast this spell.");
					return false;
				}
				if (!checkSpellRequirements(player, 50, delete, FIRE_RUNE, 5, DEATH_RUNE, 1))
					return false;
				break;
			case 47: // crumble death
				if (!checkSpellRequirements(player, 39, delete, EARTH_RUNE, 2, AIR_RUNE, 2, CHAOS_RUNE, 1))
					return false;
				break;
			case 49: // air blast
				if (!checkSpellRequirements(player, 41, delete, AIR_RUNE, 3, DEATH_RUNE, 1))
					return false;
				break;
			case 52: // water blast
				if (!checkSpellRequirements(player, 47, delete, WATER_RUNE, 3, AIR_RUNE, 3, DEATH_RUNE, 1))
					return false;
				break;
			case 56: // slayer dart
				if (player.getEquipment().getWeaponId() != 4170 && player.getEquipment().getWeaponId() != 51255) {
					player.getPackets().sendGameMessage("You need to be equipping a slayer staff to cast this spell.");
					return false;
				}
				if (!checkSpellRequirements(player, 50, delete, DEATH_RUNE, 1, MIND_RUNE, 1))
					return false;
				break;
			case 58: // earth blast
				if (!checkSpellRequirements(player, 53, delete, EARTH_RUNE, 4, AIR_RUNE, 3, DEATH_RUNE, 1))
					return false;
				break;
			case 63: // fire blast
				if (!checkSpellRequirements(player, 59, delete, FIRE_RUNE, 5, AIR_RUNE, 4, DEATH_RUNE, 1))
					return false;
				break;
			case 70: // air wave
				if (!checkSpellRequirements(player, 62, delete, AIR_RUNE, 5, BLOOD_RUNE, 1))
					return false;
				break;
			case 73: // water wave
				if (!checkSpellRequirements(player, 65, delete, WATER_RUNE, 7, AIR_RUNE, 5, BLOOD_RUNE, 1))
					return false;
				break;
			case 77: // earth wave
				if (!checkSpellRequirements(player, 70, delete, EARTH_RUNE, 7, AIR_RUNE, 5, BLOOD_RUNE, 1))
					return false;
				break;
			case 80: // fire wave
				if (!checkSpellRequirements(player, 75, delete, FIRE_RUNE, 7, AIR_RUNE, 5, BLOOD_RUNE, 1))
					return false;
				break;
			case 84:
				if (!checkSpellRequirements(player, 81, delete, AIR_RUNE, 7, DEATH_RUNE, 1, BLOOD_RUNE, 1))
					return false;
				break;
			case 87:
				if (!checkSpellRequirements(player, 85, delete, WATER_RUNE, 10, AIR_RUNE, 7, DEATH_RUNE, 1, BLOOD_RUNE, 1))
					return false;
				break;
			case 89:
				if (!checkSpellRequirements(player, 90, delete, EARTH_RUNE, 10, AIR_RUNE, 7, DEATH_RUNE, 1, BLOOD_RUNE, 1))
					return false;
				break;
			case 91:
				if (!checkSpellRequirements(player, 95, delete, FIRE_RUNE, 10, AIR_RUNE, 7, DEATH_RUNE, 1, BLOOD_RUNE, 1))
					return false;
				break;
			case 66: // Sara Strike
				if (player.getEquipment().getWeaponId() != 2415 && player.getEquipment().getWeaponId() != 15486) {
					player.getPackets().sendGameMessage("You need to be equipping a Saradomin staff to cast this spell.", true);
					return false;
				}
				if (!checkSpellRequirements(player, 60, delete, AIR_RUNE, 4, FIRE_RUNE, 1, BLOOD_RUNE, 2))
					return false;
				break;
			case 67: // Guthix Claws
				if (player.getEquipment().getWeaponId() != 2416) {
					player.getPackets().sendGameMessage("You need to be equipping a Guthix Staff or Void Mace to cast this spell.", true);
					return false;
				}
				if (!checkSpellRequirements(player, 60, delete, AIR_RUNE, 4, FIRE_RUNE, 1, BLOOD_RUNE, 2))
					return false;
				break;
			case 68: // Flame of Zammy
				if (player.getEquipment().getWeaponId() != 2417 && !hasStaffOfDead(player.getEquipment().getWeaponId())) {
					player.getPackets().sendGameMessage("You need to be equipping a Zamorak Staff to cast this spell.", true);
					return false;
				}
				if (!checkSpellRequirements(player, 60, delete, AIR_RUNE, 1, FIRE_RUNE, 4, BLOOD_RUNE, 2))
					return false;
				break;
			case 86: // teleblock
				if (!checkSpellRequirements(player, 85, delete, CHAOS_RUNE, 1, LAW_RUNE, 1, DEATH_RUNE, 1))
					return false;
				break;
			case 99: // Storm of Armadyl
				if (!checkSpellRequirements(player, 77, delete, ARMADYL_RUNE, 1))
					return false;
				break;
			case 26: //confuse
				if (!checkSpellRequirements(player, 3, delete, WATER_RUNE, 3, EARTH_RUNE, 2, BODY_RUNE, 1))
					return false;
				break;
			case 31: //weaken
				if (!checkSpellRequirements(player, 11, delete, WATER_RUNE, 3, EARTH_RUNE, 2, BODY_RUNE, 1))
					return false;
				break;
			case 35: //curse
				if (!checkSpellRequirements(player, 19, delete, WATER_RUNE, 3, EARTH_RUNE, 2, BODY_RUNE, 1))
					return false;
				break;
			case 75: //vulnerability
				if (!checkSpellRequirements(player, 66, delete, EARTH_RUNE, 5, WATER_RUNE, 5, SOUL_RUNE, 1))
					return false;
				break;
			case 78: //enfeeble
				if (!checkSpellRequirements(player, 73, delete, EARTH_RUNE, 8, WATER_RUNE, 8, SOUL_RUNE, 1))
					return false;
				break;
			case 82: //stun
				if (!checkSpellRequirements(player, 80, delete, EARTH_RUNE, 12, WATER_RUNE, 12, SOUL_RUNE, 1))
					return false;
				break;
			default:
				return false;
			}
			break;
		default:
			return false;
		}

		player.getTemporaryAttributtes().put("lastSpellCast", spellId);

		if (set >= 0) {
			if (set == 0)
				player.getCombatDefinitions().setAutoCastSpell(spellId);
			else
				player.getTemporaryAttributtes().put("tempCastSpell", spellId);
		}
		return true;
	}

	public static final void setCombatSpell(Player player, int spellId) {
		if (player.getCombatDefinitions().getAutoCastSpell() == spellId)
			player.getCombatDefinitions().resetSpells(true);
		else
			checkCombatSpell(player, spellId, 0, false);
	}

	public static final void processLunarSpell(Player player, int spellId, byte slot) {
		final Item target = player.getInventory().getItem(slot);
		player.stopAll(true, true, false);
		switch (spellId) {
		case 33: // plank make
			if (!Magic.checkSpellLevel(player, 86))
				return;
			Plank plank = Sawmill.getPlankForLog(target.getId());
			if (plank == null) {
				player.getPackets().sendGameMessage("You can only convert plain, oak, teak and mahogany logs into planks.");
				return;
			}
			int cost = (int) (plank.getCost() * 0.7);
			if (player.getInventory().getCoinsAmount() < cost) {
				player.getPackets().sendGameMessage(" You do not have enough coins to cast this spell.");
				return;
			}
			if (!checkRunes(player, true, ASTRAL_RUNE, 2, EARTH_RUNE, 15, NATURE_RUNE, 1))
				return;
			player.lock(4);
			player.getInterfaceManager().openGameTab(7);
			target.setId(plank.getId());
			player.getInventory().refresh(slot);
			player.getInventory().removeItemMoneyPouch(new Item(995, cost));
			player.getSkills().addXp(Skills.MAGIC, 90);
			player.setNextAnimation(new Animation(4413));
			player.setNextGraphics(new Graphics(1063, 0, 100));
			break;
		}
	}

	public static final void processNormalSpell(Player player, int spellId, byte slot) {
		final Item target = player.getInventory().getItem(slot);
		player.stopAll(true, true, false);
		switch (spellId) {
		case 29:
		case 41:
		case 53:
		case 61:
		case 76:
		case 88:
			Enchanting.processMagicEnchantSpell(player, slot, Enchanting.getJewleryIndex(spellId));
			break;
		case 50://superheat item
			if (!Magic.checkSpellLevel(player, 43) || player.isLocked())
				return;
			for (int index = 0; index < 9; index++) {
				SmeltingBar bar = SmeltingBar.values()[index];
				Item[] required = bar.getItemsRequired();
				if (target.getId() != required[0].getId())
					continue;
				if (bar == SmeltingBar.IRON && player.getInventory().containsItems(SmeltingBar.STEEL.getItemsRequired()))
					bar = SmeltingBar.STEEL;
				if (player.getSkills().getLevel(Skills.SMITHING) < bar.getLevelRequired()) {
					player.getPackets().sendGameMessage("You need a Smithing level of at least " + bar.getLevelRequired() + " to smelt " + bar.getProducedBar().getDefinitions().getName());
					return;
				}  else if (!player.getInventory().containsItems(required)) {
					player.getDialogueManager().startDialogue("SimpleMessage", "You are missing required ingredients to the spell.");
					return;
				} else if (!Magic.checkRunes(player, true, NATURE_RUNE, 1, FIRE_RUNE, 4))
					return;
				double xp = bar.getExperience();
				if (bar == SmeltingBar.GOLD && player.getEquipment().getGlovesId() == 776)
					xp *= 2.5;
				player.lock(3);
				player.setNextAnimation(new Animation(725));
				player.setNextGraphics(new Graphics(148, 0, 100));
				player.getSkills().addXp(Skills.SMITHING, xp);
				player.getSkills().addXp(Skills.MAGIC, 53);
				player.getInventory().removeItems(required);
				player.getInventory().addItem(bar.getProducedBar());
				player.getInterfaceManager().openGameTab(7);
				return;
			}
			player.setNextGraphics(new Graphics(85, 0, 96));
			player.getPackets().sendSound(227, 0, 1);
			player.getPackets().sendGameMessage("You cannot cast superheat on this item.");
			break;
		case 38: // low alch
		case 59: // high alch
			boolean highAlch = spellId == 59;
			if (!Magic.checkSpellLevel(player, (highAlch ? 55 : 21)))
				return;
			if (target.getId() == 995) {
				player.getPackets().sendGameMessage("You can't cast " + (highAlch ? "high" : "low") + " alchemy on gold.");
				return;
			}
			if (target.getDefinitions().isDestroyItem() /*|| ItemConstants.getItemDefaultCharges(target.getId()) != -1*/) {
				player.getPackets().sendGameMessage("You can't convert this item..");
				return;
			}
			if (target.getAmount() != 1 && !player.getInventory().hasFreeSlots()) {
				player.getPackets().sendGameMessage("Not enough space in your inventory.");
				return;
			}
			if (player.isSafePk() || player.getControlerManager().getControler() instanceof StealingCreationController) {
				player.getPackets().sendGameMessage("You can't alch here.");
				return;
			}
			if (!checkRunes(player, true, FIRE_RUNE, highAlch ? 5 : 3, NATURE_RUNE, 1))
				return;
			player.lock(4);
			player.getInterfaceManager().openGameTab(7);
			player.getInventory().deleteItem(target.getId(), 1);
			player.getSkills().addXp(Skills.MAGIC, highAlch ? 65 : 31);
			Item coins = new Item(995, highAlch ? ItemConstants.getHighAlchValue(target) : (int) (target.getDefinitions().getValue() * (highAlch ? 0.6D : 0.3D)));
			if (player.isCanPvp())
				player.getInventory().addItem(coins);
			else
				player.getInventory().addItemMoneyPouch(coins);
			Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
			if (weapon != null && (weapon.getName().toLowerCase().contains("staff") || weapon.getId() == 25699 )) {
				player.setNextAnimation(new Animation(highAlch ? 9633 : 9625));
				player.setNextGraphics(new Graphics(highAlch ? 1693 : 1692));
			} else {
				player.setNextAnimation(new Animation(713));
				player.setNextGraphics(new Graphics(highAlch ? 113 : 112));
			}
			break;
		}
	}

	public static final void processLunarSpell(Player player, int spellId, Entity target) {
		player.setNextFaceWorldTile(new WorldTile(target.getCoordFaceX(target.getSize()), target.getCoordFaceY(target.getSize()), target.getPlane()));
		//doesnt stop what u doing on rs
		switch (spellId) {
		case 42://venge other
			if (!(target instanceof Player))
				return;
			if (player.getSkills().getLevel(Skills.MAGIC) < 93) {
				player.getPackets().sendGameMessage("Your Magic level is not high enough for this spell.");
				return;
			}
			Long lastVeng = (Long) player.getTemporaryAttributtes().get("LAST_VENG");
			if (lastVeng != null && lastVeng + 30000 > Utils.currentTimeMillis()) {
				player.getPackets().sendGameMessage("Players may only cast vengeance once every 30 seconds.");
				return;
			}
			if (!((Player) target).isAcceptingAid()) {
				player.getPackets().sendGameMessage(((Player) target).getDisplayName() + " is not accepting aid");
				return;
			}
			if (((Player) target).getControlerManager().getControler() != null && ((Player) target).getControlerManager().getControler() instanceof DuelArena) {
				return;
			}
			if (!checkRunes(player, true, ASTRAL_RUNE, 3, DEATH_RUNE, 2, EARTH_RUNE, 10))
				return;
			player.setNextAnimation(new Animation(4411));
			player.getTemporaryAttributtes().put("LAST_VENG", Utils.currentTimeMillis());
			player.setVengTimer(30000);
			player.getPackets().sendGameMessage("You cast a vengeance.");
			((Player) target).setNextGraphics(new Graphics(725, 0, 100));
			((Player) target).setCastVeng(true);
			((Player) target).getPackets().sendGameMessage("You have the power of vengeance!");
			break;
		case 23: //cure other
			if (!(target instanceof Player))
				return;
			if (player.getSkills().getLevel(Skills.MAGIC) < 68) {
				player.getPackets().sendGameMessage("Your Magic level is not high enough for this spell.");
				return;
			}
			if (!((Player) target).isAcceptingAid()) {
				player.getPackets().sendGameMessage(((Player) target).getDisplayName() + " is not accepting aid");
				return;
			}
			if (((Player) target).getControlerManager().getControler() != null && ((Player) target).getControlerManager().getControler() instanceof DuelArena) {
				return;
			}
			if (!checkRunes(player, true, ASTRAL_RUNE, 1, EARTH_RUNE, 10))
				return;
			player.setNextAnimation(new Animation(4411));
			Player p2 = (Player) target;
			p2.setNextGraphics(new Graphics(736, 0, 150));
			p2.getPoison().reset();
			p2.getPackets().sendGameMessage("You have been healed by " + player.getDisplayName() + "!");
			break;
		case 28://stat spy npc
			if (!(target instanceof NPC))
				return;
			if (player.getSkills().getLevel(Skills.MAGIC) < 66) {
				player.getPackets().sendGameMessage("Your Magic level is not high enough for this spell.");
				return;
			}
			NPC npc = (NPC) target;
			if (!npc.getDefinitions().hasAttackOption()) {
				player.getPackets().sendGameMessage("That NPC cannot be examined.");
				return;
			}
			if (!checkRunes(player, true, ASTRAL_RUNE, 1, 564, 1, MIND_RUNE, 1))
				return;
			player.getInterfaceManager().sendInventoryInterface(522);
			player.getPackets().sendIComponentText(522, 0, "Monster Name: " + npc.getName());
			player.getPackets().sendIComponentText(522, 1, "Combat Level: " + npc.getCombatLevel());
			player.getPackets().sendIComponentText(522, 2, "Life Points: " + npc.getHitpoints());
			player.getPackets().sendIComponentText(522, 3, "Creature's Max Hit: " + npc.getMaxHit());
			player.getPackets().sendIComponentText(522, 4, (player.getSlayerManager().isValidTask(npc.getName()) ? "Valid Slayer Task" : ""));
			player.setNextAnimation(new Animation(4413));
			player.setNextGraphics(new Graphics(1061, 0, 150));
			break;
		}
	}

	@SuppressWarnings("unused")
	private static final double[][] TELEPORT_EXPERIENCE =
	{
	{ 67, 72, 77, 81, 90, 93, 99, 102 }, {} };
	private static final int[][] TELEPORT_LEVEL =
	{
	{ 66, 73, 76, 79, 86, 88, 90, 93 }, {} };
	private static final int[][][] TELEPORT_RUNES =
	{
	{
	{ LAW_RUNE, 1, ASTRAL_RUNE, 2, EARTH_RUNE, 1 },
	{ WATER_RUNE, 5, LAW_RUNE, 1, ASTRAL_RUNE, 2 },
	{ LAW_RUNE, 2, FIRE_RUNE, 6, ASTRAL_RUNE, 2 },
	{ LAW_RUNE, 2, WATER_RUNE, 8, ASTRAL_RUNE, 2 },
	{ LAW_RUNE, 3, WATER_RUNE, 10, ASTRAL_RUNE, 3 },
	{ LAW_RUNE, 3, WATER_RUNE, 15, ASTRAL_RUNE, 3 },
	{ LAW_RUNE, 3, WATER_RUNE, 16, ASTRAL_RUNE, 3 },
	{ LAW_RUNE, 3, WATER_RUNE, 20, ASTRAL_RUNE, 3 } } };

	public static final void processLunarSpell(Player player, int componentId, int packetId) {
		switch (componentId) {
		case 37:
			if (player.getSkills().getLevel(Skills.MAGIC) < 94) {
				player.getPackets().sendGameMessage("Your Magic level is not high enough for this spell.");
				return;
			} else if (player.getSkills().getLevel(Skills.DEFENCE) < 40) {
				player.getPackets().sendGameMessage("You need a Defence level of 40 for this spell");
				return;
			}
			Long lastVeng = (Long) player.getTemporaryAttributtes().get("LAST_VENG");
			if (lastVeng != null && lastVeng + 30000 > Utils.currentTimeMillis()) {
				player.getPackets().sendGameMessage("Players may only cast vengeance once every 30 seconds.");
				return;
			}
			if (!checkRunes(player, true, ASTRAL_RUNE, 4, DEATH_RUNE, 2, EARTH_RUNE, 10))
				return;
			player.getSkills().addXp(Skills.MAGIC, 112);
			player.setNextGraphics(new Graphics(726, 0, 100));
			player.setNextAnimation(new Animation(4410));
			player.setCastVeng(true);
			player.getTemporaryAttributtes().put("LAST_VENG", Utils.currentTimeMillis());
			player.setVengTimer(30000);
			player.getPackets().sendGameMessage("You cast a vengeance.");
			break;
		case 39:
			useHomeTele(player, packetId);
			break;
		case 32:
			if (player.getPoison().isPoisoned()) {
				player.getPackets().sendGameMessage("You can't dream while you're poisoned.");
				return;
			} else if (player.isUnderCombat()) {
				player.getPackets().sendGameMessage("You can't cast dream until 10 seconds after the end of combat.");
				return;
			} else if (player.getHitpoints() == player.getMaxHitpoints()) {
				player.getPackets().sendGameMessage("You have no need to cast this spell since your life points are already full.");
				return;
			} else if (!checkRunes(player, true, ASTRAL_RUNE, 2, COSMIC_RUNE, 1, BODY_RUNE, 5))
				return;
			player.getActionManager().setAction(new DreamSpellAction());
			break;
		case 74: // vegeance group
			if (player.getSkills().getLevel(Skills.MAGIC) < 95) {
				player.getPackets().sendGameMessage("Your Magic level is not high enough for this spell.");
				return;
			}
			lastVeng = (Long) player.getTemporaryAttributtes().get("LAST_VENG");
			if (lastVeng != null && lastVeng + 30000 > Utils.currentTimeMillis()) {
				player.getPackets().sendGameMessage("Players may only cast vengeance once every 30 seconds.");
				return;
			}
			if (!checkRunes(player, true, ASTRAL_RUNE, 4, DEATH_RUNE, 3, EARTH_RUNE, 11))
				return;
			int affectedPeopleCount = 0;
			for (int regionId : player.getMapRegionsIds()) {
				List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
				if (playerIndexes == null)
					continue;
				for (int playerIndex : playerIndexes) {
					Player p2 = World.getPlayers().get(playerIndex);
					if (p2 == null || p2 == player || p2.isDead() || !p2.hasStarted() || p2.hasFinished() || !p2.withinDistance(player, 4) || !player.getControlerManager().canHit(p2))
						continue;
					if (!p2.isAcceptingAid()) {
						player.getPackets().sendGameMessage(p2.getDisplayName() + " is not accepting aid");
						continue;
					} else if (p2.getControlerManager().getControler() != null && p2.getControlerManager().getControler() instanceof DuelArena) {
						continue;
					}
					p2.setNextGraphics(new Graphics(725, 0, 100));
					p2.setCastVeng(true);
					p2.getPackets().sendGameMessage("You have the power of vengeance!");
					affectedPeopleCount++;
				}
			}
			player.getSkills().addXp(Skills.MAGIC, 120);
			player.setNextAnimation(new Animation(4411));
			player.getTemporaryAttributtes().put("LAST_VENG", Utils.currentTimeMillis());
			player.setVengTimer(30000);
			player.getPackets().sendGameMessage("The spell affected " + affectedPeopleCount + " nearby people.");
			break;
		case 43: // moonclan teleport
			sendLunarTeleportSpell(player, 69, 66, new WorldTile(2114, 3914, 0), ASTRAL_RUNE, 2, LAW_RUNE, 1, EARTH_RUNE, 2);
			break;
		case 54: // ourania teleport
			sendLunarTeleportSpell(player, 71, 69, new WorldTile(2467, 3247, 0), ASTRAL_RUNE, 2, LAW_RUNE, 1, EARTH_RUNE, 6);
			break;
		case 67: // south falador teleport
			sendLunarTeleportSpell(player, 72, 70, new WorldTile(3006, 3327, 0), ASTRAL_RUNE, 2, LAW_RUNE, 1, AIR_RUNE, 2);
			break;
		case 47: // waterbirth teleport
			sendLunarTeleportSpell(player, 72, 71, new WorldTile(2546, 3758, 0), ASTRAL_RUNE, 2, LAW_RUNE, 1, WATER_RUNE, 1);
			break;
		case 22: // barbarian teleport
			sendLunarTeleportSpell(player, 75, 76, new WorldTile(2635, 3166, 0), ASTRAL_RUNE, 2, LAW_RUNE, 1, FIRE_RUNE, 3);
			break;
		case 69: // North Ardroudge teleport
			sendLunarTeleportSpell(player, 76, 76, new WorldTile(2613, 3349, 0), ASTRAL_RUNE, 2, LAW_RUNE, 1, WATER_RUNE, 5);
			break;
		case 41: // Khazard teleport
			sendLunarTeleportSpell(player, 78, 80, new WorldTile(2635, 3166, 0), ASTRAL_RUNE, 2, LAW_RUNE, 2, WATER_RUNE, 4);
			break;
		case 40: // Fishing guild teleport
			sendLunarTeleportSpell(player, 85, 89, new WorldTile(2612, 3383, 0), ASTRAL_RUNE, 3, LAW_RUNE, 3, WATER_RUNE, 8);
			break;
		case 44: // Catherbay teleport
			sendLunarTeleportSpell(player, 87, 92, new WorldTile(2800, 3451, 0), ASTRAL_RUNE, 3, LAW_RUNE, 3, WATER_RUNE, 10);
			break;
		case 51: // Ice Plateau teleport
			sendLunarTeleportSpell(player, 89, 96, new WorldTile(2974, 3940, 0), ASTRAL_RUNE, 3, LAW_RUNE, 3, WATER_RUNE, 8);
			break;
		case 75: // Throheim teleport
			sendLunarTeleportSpell(player, 92, 101, new WorldTile(2814, 3680, 0), ASTRAL_RUNE, 3, LAW_RUNE, 3, WATER_RUNE, 10);
			break;
		case 38:
			int totalEXP = 0,
			totalAmount = 0;
			if (player.getSkills().getLevel(Skills.MAGIC) < 66) {
				player.getPackets().sendGameMessage("Your Magic level is not high enough for this spell.");
				return;
			}
			player.lock(2);
			for (Cookables food : Cookables.values()) {
				if (food.toString().toLowerCase().contains("_pie")) {
					if (player.getSkills().getLevel(Skills.COOKING) < food.getLvl())
						continue;
					Item item = food.getRawItem();
					if (player.getInventory().containsItem(item.getId(), 1)) {
						totalAmount += player.getInventory().getAmountOf(item.getId());
						for (int i = 0; i < player.getInventory().getAmountOf(item.getId()); i++) {
							if (!checkRunes(player, true, ASTRAL_RUNE, 1, FIRE_RUNE, 5, WATER_RUNE, 4))
								return;
							totalEXP += food.getXp();
							player.getInventory().replaceItem(food.getProduct().getId(), item.getAmount(), player.getInventory().getItems().getThisItemSlot(item.getId()));
						}
					}
				}
			}
			player.getSkills().addXp(Skills.MAGIC, 65 * totalAmount);
			player.getSkills().addXp(Skills.COOKING, totalEXP);
			totalAmount = 0;
			totalEXP = 0;
			player.getInterfaceManager().openGameTab(4);
			player.setNextAnimation(new Animation(4413));
			player.setNextGraphics(new Graphics(746));
			break;
		case 29:
			if (player.getSkills().getLevel(Skills.MAGIC) < 68) {
				player.getPackets().sendGameMessage("Your Magic level is not high enough for this spell.");
				return;
			}
			if (!checkRunes(player, true, ASTRAL_RUNE, 1, FIRE_RUNE, 1, WATER_RUNE, 3))
				return;
			player.lock(2);
			for (Item item : player.getInventory().getItems().getItems()) {
				if (item == null)
					continue;
				for (Fill fill : Fill.values()) {
					if (fill.getEmpty() == item.getId())
						item.setId(fill.getFull());
				}
			}
			player.getInventory().refresh();
			player.getSkills().addXp(Skills.MAGIC, 65);
			player.getInterfaceManager().openGameTab(4);
			player.setNextAnimation(new Animation(4413));
			player.setNextGraphics(new Graphics(1061, 0, 150));
			break;
		case 56:
		case 57:
		case 58:
		case 59:
		case 60:
		case 61:
		case 62:
		case 76:
			int index = componentId == 76 ? 7 : componentId - 56;
			if (player.getSkills().getLevel(Skills.MAGIC) < TELEPORT_LEVEL[0][index]) {
				player.getPackets().sendGameMessage("Your Magic level is not high enough for this spell.");
				return;
			}
			if (!checkRunes(player, true, TELEPORT_RUNES[0][index]))
				return;
			//player.getSkills().addXp(Skills.MAGIC, TELEPORT_EXPERIENCE[0][index]);
			String name = player.getDisplayName();
			for (int regionId : player.getMapRegionsIds()) {
				List<Integer> playersIndexes = World.getRegion(regionId).getPlayerIndexes();
				if (playersIndexes == null)
					continue;
				for (Integer playerIndex : playersIndexes) {
					Player p2 = World.getPlayers().get(playerIndex);
					if (p2 == null || p2.isLocked() || p2.isDead() || p2.hasFinished() || !p2.isRunning() || !p2.isAcceptingAid() && player.getIndex() != p2.getIndex() || p2.getInterfaceManager().containsScreenInter() || !p2.withinDistance(player, 5))
						continue;
					ManiFoldTeleport.openInterface(p2, name, index, true);
				}
			}
			break;
		case 46:
			if (player.getSkills().getLevel(Skills.MAGIC) < 68) {
				player.getPackets().sendGameMessage("Your Magic level is not high enough for this spell.");
				return;
			}
			if (!checkRunes(player, true, ASTRAL_RUNE, 2, COSMIC_RUNE, 2))
				return;
			player.setNextAnimation(new Animation(4411));
			player.setNextGraphics(new Graphics(736, 0, 150));
			player.getPoison().reset();
			break;
		case 1://TODO for real lunars xD
			if (!Magic.checkSpellLevel(player, 74))
				return;
			else if (!checkRunes(player, true, ASTRAL_RUNE, 2, COSMIC_RUNE, 2))
				return;
			affectedPeopleCount = 0;
			for (int regionId : player.getMapRegionsIds()) {
				List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
				if (playerIndexes == null)
					continue;
				for (int playerIndex : playerIndexes) {
					Player p2 = World.getPlayers().get(playerIndex);
					if (p2 == null || p2 == player || p2.isDead() || !p2.hasStarted() || p2.hasFinished() || !p2.withinDistance(player, 4))
						continue;
					if (!p2.isAcceptingAid())
						continue;
					player.setNextGraphics(new Graphics(736, 0, 150));
					p2.getPackets().sendGameMessage("You have been cured of all illnesses!");
					affectedPeopleCount++;
				}
			}
			player.setNextAnimation(new Animation(4411));
			player.getPackets().sendGameMessage("The spell affected " + affectedPeopleCount + " nearby people.");
			break;
		}
	}

	public static final void processAncientSpell(Player player, int spellId, int packetId) {
		switch (spellId) {
		case 28:
		case 32:
		case 24:
		case 20:
		case 30:
		case 34:
		case 26:
		case 22:
		case 29:
		case 33:
		case 25:
		case 21:
		case 31:
		case 35:
		case 27:
		case 23:
		case 36:
		case 37:
		case 38:
		case 39:
			setCombatSpell(player, spellId);
			break;
		case 40:
			sendAncientTeleportSpell(player, 54, 64, new WorldTile(3099, 9882, 0), LAW_RUNE, 2, FIRE_RUNE, 1, AIR_RUNE, 1);
			break;
		case 41:
			sendAncientTeleportSpell(player, 60, 70, new WorldTile(3222, 3336, 0), LAW_RUNE, 2, SOUL_RUNE, 1);
			break;
		case 42:
			sendAncientTeleportSpell(player, 66, 76, new WorldTile(3492, 3471, 0), LAW_RUNE, 2, BLOOD_RUNE, 1);
			break;
		case 43:
			sendAncientTeleportSpell(player, 72, 82, new WorldTile(3006, 3471, 0), LAW_RUNE, 2, WATER_RUNE, 4);
			break;
		case 44:
			sendAncientTeleportSpell(player, 78, 88, new WorldTile(2990, 3696, 0), LAW_RUNE, 2, FIRE_RUNE, 3, AIR_RUNE, 2);
			break;
		case 45:
			sendAncientTeleportSpell(player, 84, 94, new WorldTile(3217, 3677, 0), LAW_RUNE, 2, SOUL_RUNE, 2);
			break;
		case 46:
			sendAncientTeleportSpell(player, 90, 100, new WorldTile(3288, 3886, 0), LAW_RUNE, 2, BLOOD_RUNE, 2);
			break;
		case 47:
			sendAncientTeleportSpell(player, 96, 106, new WorldTile(2977, 3873, 0), LAW_RUNE, 2, WATER_RUNE, 8);
			break;
		case 48:
			useHomeTele(player, packetId);
			break;
		}
	}
	
	private final static WorldTile[] ANCIENT_TABS =
	{ new WorldTile(3288, 3886, 0)
	, new WorldTile(2977, 3873, 0)
	, new WorldTile(2990, 3696, 0)
	, new WorldTile(2977, 3873, 0)
	,new WorldTile(3492, 3471, 0),
	new WorldTile(3006, 3471, 0),  new WorldTile(3099, 9882, 0), new WorldTile(3222, 3336, 0) };


	public static final void processNormalSpell(Player player, int spellId, int packetId) {
		switch (spellId) {
		case 98: //air rush
		case 25: // air strike
		case 28: // water strike
		case 30: // earth strike
		case 32: // fire strike
		case 34: // air bolt
		case 39: // water bolt
		case 42: // earth bolt
		case 45: // fire bolt
		case 47: //crumble dead
		case 56: //slayer dart
		case 54: //iban blast
		case 49: // air blast
		case 52: // water blast
		case 58: // earth blast
		case 63: // fire blast
		case 70: // air wave
		case 73: // water wave
		case 77: // earth wave
		case 80: // fire wave
		case 99:
		case 84:
		case 87:
		case 89:
		case 91:
		case 36:
		case 55:
		case 81:
		case 66:
		case 67:
		case 68:
			setCombatSpell(player, spellId);
			break;
		case 33://bones to banana
		case 65://bones to peaches
			boolean bones_to_peaches = spellId == 65;
			if (!Magic.checkSpellLevel(player, bones_to_peaches ? 60 : 15))
				return;
			else if (!checkRunes(player, true, NATURE_RUNE, bones_to_peaches ? 2 : 1, EARTH_RUNE, bones_to_peaches ? 4 : 2, WATER_RUNE, bones_to_peaches ? 4 : 2))
				return;
			int bones = 0;
			for (int i = 0; i < 28; i++) {
				Item item = player.getInventory().getItem(i);
				if (item == null || Bone.forId(item.getId()) == null)
					continue;
				item.setId(bones_to_peaches ? 6883 : 1963);
				bones++;
			}
			if (bones != 0) {
				player.getSkills().addXp(Skills.MAGIC, bones_to_peaches ? 35.5 : 25);
				player.getInventory().refresh();
			}
			break;
		case 27: // crossbow bolt enchant
			if (player.getSkills().getLevel(Skills.MAGIC) < 4) {
				player.getPackets().sendGameMessage("Your Magic level is not high enough for this spell.");
				return;
			}
			player.stopAll();
			player.getInterfaceManager().sendInterface(432);
			break;
		case 24:
			useHomeTele(player, packetId);
			break;
		case 37: // mobi
			sendNormalTeleportSpell(player, 10, 19, new WorldTile(2413, 2848, 0), LAW_RUNE, 1, WATER_RUNE, 1, AIR_RUNE, 1);
			break;
		case 40: // varrock
			sendNormalTeleportSpell(player, 25, 19, new WorldTile(3212, 3424, 0), FIRE_RUNE, 1, AIR_RUNE, 3, LAW_RUNE, 1);
			break;
		case 43: // lumby
			sendNormalTeleportSpell(player, 31, 41, new WorldTile(3222, 3218, 0), EARTH_RUNE, 1, AIR_RUNE, 3, LAW_RUNE, 1);
			break;
		case 46: // fally
			sendNormalTeleportSpell(player, 37, 48, new WorldTile(2964, 3379, 0), WATER_RUNE, 1, AIR_RUNE, 3, LAW_RUNE, 1);
			break;
		case 51: // camelot
			sendNormalTeleportSpell(player, 45, 55.5, new WorldTile(2757, 3478, 0), AIR_RUNE, 5, LAW_RUNE, 1);
			break;
		case 57: // ardy
			sendNormalTeleportSpell(player, 51, 61, new WorldTile(2664, 3305, 0), WATER_RUNE, 2, LAW_RUNE, 2);
			break;
		case 62: // watch
			sendNormalTeleportSpell(player, 58, 68, new WorldTile(2547, 3113, 2), EARTH_RUNE, 2, LAW_RUNE, 2);
			break;
		case 69: // troll
			sendNormalTeleportSpell(player, 61, 68, new WorldTile(2888, 3674, 0), FIRE_RUNE, 2, LAW_RUNE, 2);
			break;
		case 72: // ape
			sendNormalTeleportSpell(player, 64, 76, new WorldTile(2767, 2795, 0), FIRE_RUNE, 2, WATER_RUNE, 2, LAW_RUNE, 2, 1963, 1);
			break;
		case 48: // house
			sendNormalTeleportSpell(player, 40, 30, null, LAW_RUNE, 1, AIR_RUNE, 1, EARTH_RUNE, 1);
			break;
		}
	}

	public static final void processDungSpell(Player player, int spellId, int packetId) {
		processDungSpell(player, spellId, -1, packetId);
	}

	public static final void processDungSpell(Player player, int spellId, int slot, int packetId) {
		final Item target = player.getInventory().getItem(slot);
		if (target == null && slot != -1)
			return;
		switch (spellId) {
		case 25:
		case 27:
		case 28:
		case 30:
		case 32: // air bolt
		case 36: // water bolt
		case 37: // earth bolt
		case 41: // fire bolt
		case 42: // air blast
		case 43: // water blast
		case 45: // earth blast
		case 47: // fire blast
		case 48: // air wave
		case 49: // water wave
		case 54: // earth wave
		case 58: // fire wave
		case 61:// air surge
		case 62:// water surge
		case 63:// earth surge
		case 67:// fire surge
		case 34:// bind
		case 44:// snare
		case 59:// entangle
			setCombatSpell(player, spellId);
			break;
		case 65:
			if (player.getSkills().getLevel(Skills.MAGIC) < 94) {
				player.getPackets().sendGameMessage("Your Magic level is not high enough for this spell.");
				return;
			} else if (player.getSkills().getLevel(Skills.DEFENCE) < 40) {
				player.getPackets().sendGameMessage("You need a Defence level of 40 for this spell");
				return;
			}
			Long lastVeng = (Long) player.getTemporaryAttributtes().get("LAST_VENG");
			if (lastVeng != null && lastVeng + 30000 > Utils.currentTimeMillis()) {
				player.getPackets().sendGameMessage("Players may only cast vengeance once every 30 seconds.");
				return;
			}
			if (!checkRunes(player, true, true, 17790, 4, 17786, 2, 17782, 10))
				return;
			player.getSkills().addXp(Skills.MAGIC, 112);
			player.setNextGraphics(new Graphics(726, 0, 100));
			player.setNextAnimation(new Animation(4410));
			player.setCastVeng(true);
			player.getTemporaryAttributtes().put("LAST_VENG", Utils.currentTimeMillis());
			player.setVengTimer(30000);
			player.getPackets().sendGameMessage("You cast a vengeance.");
			break;
		case 66: // vegeance group
			if (player.getSkills().getLevel(Skills.MAGIC) < 95) {
				player.getPackets().sendGameMessage("Your Magic level is not high enough for this spell.");
				return;
			}
			lastVeng = (Long) player.getTemporaryAttributtes().get("LAST_VENG");
			if (lastVeng != null && lastVeng + 30000 > Utils.currentTimeMillis()) {
				player.getPackets().sendGameMessage("Players may only cast vengeance once every 30 seconds.");
				return;
			}
			if (!checkRunes(player, true, true, 17790, 4, 17786, 3, 17782, 11))
				return;
			int affectedPeopleCount = 0;
			for (int regionId : player.getMapRegionsIds()) {
				List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
				if (playerIndexes == null)
					continue;
				for (int playerIndex : playerIndexes) {
					Player p2 = World.getPlayers().get(playerIndex);
					if (p2 == null /*|| p2 == player*/ || p2.isDead() || !p2.hasStarted() || p2.hasFinished() || !p2.withinDistance(player, 4) || !player.getControlerManager().canHit(p2))
						continue;
					if (p2 != player && !p2.isAcceptingAid()) {
						player.getPackets().sendGameMessage(p2.getDisplayName() + " is not accepting aid");
						continue;
					} else if (p2.getControlerManager().getControler() != null && p2.getControlerManager().getControler() instanceof DuelArena) {
						continue;
					}
					p2.setNextGraphics(new Graphics(725, 0, 100));
					p2.setCastVeng(true);
					p2.getPackets().sendGameMessage("You have the power of vengeance!");
					affectedPeopleCount++;
				}
			}
			player.getSkills().addXp(Skills.MAGIC, 120);
			player.setNextAnimation(new Animation(4411));
			player.getTemporaryAttributtes().put("LAST_VENG", Utils.currentTimeMillis());
			player.setVengTimer(30000);
			player.getPackets().sendGameMessage("The spell affected " + affectedPeopleCount + " nearby people.");
			break;
		case 53:
			if (player.getSkills().getLevel(Skills.MAGIC) < 68) {
				player.getPackets().sendGameMessage("Your Magic level is not high enough for this spell.");
				return;
			}
			if (!checkRunes(player, true, true, 17790, 1, 17783, 1, 17781, 3))
				return;
			player.lock(2);
			Item[] itemsBefore = player.getInventory().getItems().getItemsCopy();
			for (Item item : player.getInventory().getItems().getItems()) {
				if (item == null)
					continue;
				for (Fill fill : Fill.values()) {
					if (fill.getEmpty() == item.getId())
						item.setId(fill.getFull());
				}
			}
			player.getInventory().refreshItems(itemsBefore);
			player.getSkills().addXp(Skills.MAGIC, 65);
			player.getInterfaceManager().openGameTab(4);
			player.setNextAnimation(new Animation(4413));
			player.setNextGraphics(new Graphics(1061, 0, 150));
			break;
		case 35: // low alch
		case 46: // high alch
			boolean highAlch = spellId == 46;
			if (!Magic.checkSpellLevel(player, (highAlch ? 55 : 21)))
				return;
			if (target.getId() == DungeonConstants.RUSTY_COINS) {
				player.getPackets().sendGameMessage("You can't cast " + (highAlch ? "high" : "low") + " alchemy on gold.");
				return;
			}
			if (target.getDefinitions().isDestroyItem() || ItemConstants.getItemDefaultCharges(target.getId()) != -1 || !ItemConstants.isTradeable(target)) {
				player.getPackets().sendGameMessage("You can't convert this item..");
				return;
			}
			if (target.getAmount() != 1 && !player.getInventory().hasFreeSlots()) {
				player.getPackets().sendGameMessage("Not enough space in your inventory.");
				return;
			}
			if (!checkRunes(player, true, true, 17783, highAlch ? 5 : 3, 17791, 1))
				return;
			player.lock(4);
			player.getInterfaceManager().openGameTab(7);
			player.getInventory().deleteItem(target.getId(), 1);
			player.getSkills().addXp(Skills.MAGIC, highAlch ? 25 : 15);
			player.getInventory().addItemMoneyPouch(new Item(DungeonConstants.RUSTY_COINS, (int) (target.getDefinitions().getValue() * (highAlch ? 0.6D : 0.3D))));
			Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
			if (weapon != null && (weapon.getName().toLowerCase().contains("staff") || weapon.getId() == 25699)) {
				player.setNextAnimation(new Animation(highAlch ? 9633 : 9625));
				player.setNextGraphics(new Graphics(highAlch ? 1693 : 1692));
			} else {
				player.setNextAnimation(new Animation(713));
				player.setNextGraphics(new Graphics(highAlch ? 113 : 112));
			}
			break;
		case 31:// bones to bananas
			if (!Magic.checkSpellLevel(player, 15))
				return;
			else if (!checkRunes(player, true, true, 17791, 1, 17781, 2, 17782, 2))
				return;
			int bones = 0;
			for (int i = 0; i < 28; i++) {
				Item item = player.getInventory().getItem(i);
				if (item == null || Bone.forId(item.getId()) == null)
					continue;
				item.setId(18199);
				bones++;
			}
			if (bones != 0) {
				player.getSkills().addXp(Skills.MAGIC, 25);
				player.getInventory().refresh();
			}
			break;
		case 55:
			if (player.getSkills().getLevel(Skills.MAGIC) < 71) {
				player.getPackets().sendGameMessage("Your Magic level is not high enough for this spell.");
				return;
			}
			if (!checkRunes(player, true, true, 17790, 2, 17789, 2))
				return;
			player.setNextAnimation(new Animation(4411));
			player.setNextGraphics(new Graphics(736, 0, 150));
			player.getPoison().reset();
			break;
		case 57:
			if (!Magic.checkSpellLevel(player, 74))
				return;
			else if (!checkRunes(player, true, true, 17790, 2, 17789, 2))
				return;
			affectedPeopleCount = 0;
			for (int regionId : player.getMapRegionsIds()) {
				List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
				if (playerIndexes == null)
					continue;
				for (int playerIndex : playerIndexes) {
					Player p2 = World.getPlayers().get(playerIndex);
					if (p2 == null || p2 == player || p2.isDead() || !p2.hasStarted() || p2.hasFinished() || !p2.withinDistance(player, 4))
						continue;
					if (!p2.isAcceptingAid())
						continue;
					player.setNextGraphics(new Graphics(736, 0, 150));
					p2.getPackets().sendGameMessage("You have been cured of all illnesses!");
					affectedPeopleCount++;
				}
			}
			player.setNextAnimation(new Animation(4411));
			player.getPackets().sendGameMessage("The spell affected " + affectedPeopleCount + " nearby people.");
			break;
		default:
			if(Settings.DEBUG)
				Logger.log(Magic.class, "Component " + spellId);
			break;
		}
	}

	public static void useHomeTele(Player player, int packetId) {
		if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
			player.stopAll();
			player.getInterfaceManager().sendInterface(1092);
		/*	player.stopAll();
			player.getDialogueManager().startDialogue("HomeTeleportD");*/
		//	player.stopAll();
		//	EconomyManager.openTPS(player);
		} else 
			HomeTeleport.useLodestone(player, player.getPreviousLodestone());
	}

	public static final boolean checkSpellRequirements(Player player, int level, boolean delete, int... runes) {
		return checkSpellRequirements(player, level, delete, false, runes);
	}

	public static final boolean checkSpellRequirements(Player player, int level, boolean delete, boolean dungeoneering, int... runes) {
		if (!checkSpellLevel(player, level))
			return false;
		return checkRunes(player, delete, dungeoneering, runes);
	}

	public static boolean checkSpellLevel(Player player, int level) {
		if (player.getSkills().getLevel(Skills.MAGIC) < level && player.getSkills().getLevelForXp(Skills.MAGIC) < level) {
			player.getPackets().sendGameMessage("Your Magic level is not high enough for this spell.");
			return false;
		}
		return true;
	}

	public static boolean hasStaffOfLight(int weaponId) {
		if (weaponId == 15486 || weaponId == 25379 ||weaponId == 22207 || weaponId == 22209 || weaponId == 22211 || weaponId == 22213)
			return true;
		return false;
	}
	
	public static boolean hasStaffOfDead(int weaponId) {
		if (weaponId == 42904 || weaponId == 41791 ||weaponId == 42902)
			return true;
		return false;
	}

	public static final boolean checkRunes(Player player, boolean delete, int... runes) {
		return checkRunes(player, delete, false, runes);
	}

	public static final boolean checkRunes(Player player, boolean delete, boolean dungeoneering, int... runes) {
		return checkRunes(player, delete, dungeoneering, true, runes);
	}

	public static final boolean checkRunes(Player player, boolean delete, boolean dungeoneering, boolean message, int... runes) {
		if (player.withinDistance(new WorldTile(3374, 3893, 0), 8))
			return true;
		if (delete && player.isSafePk())
			delete = false;
		int weaponId = player.getEquipment().getWeaponId();
		int shieldId = player.getEquipment().getShieldId();
		int runesCount = 0;
		while (runesCount < runes.length) {
			int runeId = runes[runesCount++];
			int amount = runes[runesCount++];
			if (hasInfiniteRunes(runeId, weaponId, shieldId))
				continue;
			else if (hasSpecialRunes(player, runeId, amount))
				continue;
			else if (dungeoneering || (runeId >= 17780 && runeId <= 17792)) {
				if (player.getInventory().containsItem(runeId - 1689, amount))
					continue;
			}
			if (!player.getInventory().containsItem(runeId, amount)
					&& !(player.getRunePouch().contains(new Item(runeId, amount)) && player.getInventory().containsOneItem(RunePouch.ID))) {
				player.getPackets().sendGameMessage("You do not have enough " + ItemConfig.forID(runeId).getName().replace("rune", "Rune") + "s to cast this spell.");
				return false;
			}

		}
		if (delete) {
			runesCount = 0;
			while (runesCount < runes.length) {
				int runeId = runes[runesCount++];
				int amount = runes[runesCount++];
				if (hasInfiniteRunes(runeId, weaponId, shieldId))
					continue;
				else if (hasSpecialRunes(player, runeId, amount))
					runeId = getRuneForId(runeId);
				else if (dungeoneering || (runeId >= 17780 && runeId <= 17792)) {
					int bindedRune = runeId - 1689;
					if (player.getInventory().containsItem(bindedRune, amount)) {
						player.getInventory().deleteItem(bindedRune, amount);
						continue; // won't delete the extra rune anyways.
					}
				}else if (hasStaffOfLight(weaponId) && !containsRune(LAW_RUNE, runes) && !containsRune(NATURE_RUNE, runes) && Utils.random(8) == 0 && runeId != 21773) {
					player.getPackets().sendGameMessage("The power of your staff of light saves some runes from being drained.", true);
					continue;
				} else if (weaponId == 51006 && !containsRune(LAW_RUNE, runes) && !containsRune(NATURE_RUNE, runes) && Utils.random(8) == 0 && runeId != 21773) {
					player.getPackets().sendGameMessage("The power of your wand saves some runes from being drained.", true);
					continue;
				} else if (hasStaffOfDead(weaponId) && !containsRune(LAW_RUNE, runes) && !containsRune(NATURE_RUNE, runes) && Utils.random(8) == 0 && runeId != 21773) {
						player.getPackets().sendGameMessage("The power of your staff of dead saves some runes from being drained.", true);
						continue;
				}
				Item rune = new Item(runeId, amount);
				if (player.getInventory().containsOneItem(RunePouch.ID)
						&& player.getRunePouch().contains(rune)) {
					player.getRunePouch().remove(rune);
					if (player.getRunePouch().getNumberOf(rune) <= 0) {
						player.getRunePouch().shift();
						player.getPackets().sendGameMessage("You are out of " + rune.getDefinitions().getName() + "s.", true);
						continue;
					}
				//	player.getPackets().sendGameMessage(rune.getAmount() + " x " + rune.getDefinitions().getName() + " were used up.", true);
					continue;
				}
				player.getInventory().deleteItem(runeId, amount);
			}
		}
		return true;
	}

	private static boolean containsRune(int rune, int[] runes) {
		for (int id : runes) {
			if (rune == id)
				return true;
		}
		return false;
	}

	public static final void sendAncientTeleportSpell(Player player, int level, double xp, WorldTile tile, int... runes) {
		sendTeleportSpell(player, 1979, -1, 1681, -1, level, xp, tile, 5, true, MAGIC_TELEPORT, runes);
	}

	public static final void sendLunarTeleportSpell(Player player, int level, double xp, WorldTile tile, int... runes) {
		sendTeleportSpell(player, 9606, -2, 1685, -1, level, xp, tile, 5, true, MAGIC_TELEPORT, runes);
	}

	public static final boolean sendNormalTeleportSpell(Player player, int level, double xp, WorldTile tile, int... runes) {
		return sendTeleportSpell(player, 8939, 8941, 1576, 1577, level, xp, tile, 3, true, MAGIC_TELEPORT, runes);
	}

	public static final boolean sendItemTeleportSpell(Player player, boolean randomize, int upEmoteId, int upGraphicId, int delay, WorldTile tile) {
		return sendTeleportSpell(player, upEmoteId, -2, upGraphicId, -1, 0, 0, tile, delay, randomize, ITEM_TELEPORT);
	}
	
	public static final boolean sendCommandTeleportSpell(Player player, WorldTile tile) {
		if(!canTeleport(player, tile))
			return false;
		return Magic.sendNormalTeleportSpell(player, 0, 0, tile);
	}
	public static boolean canTeleport(Player player) {
		return canTeleport(player, null);
	}

	public static boolean canTeleport(Player player, WorldTile tile) {
		if (player.isLocked())
			return false;
		if (tile != HomeTeleport.HOME_LODE_STONE && !player.getBank().hasVerified(10))
			return true;
		Controller c = player.getControlerManager().getControler();
		if (c != null && c instanceof DungeonController) { //becaus dung can tp
			player.getPackets().sendGameMessage("A magical force prevents you from leaving this area.");
			return false;
		}
		if (player.isUnderCombat() && player.getControlerManager().getControler() instanceof Wilderness) { //or could be abused
			player.getPackets().sendGameMessage("You can't home teleport shortly after the end of combat.");
			return false;
		}
		/*if (tile != HomeTeleport.HOME_LODE_STONE && player.isIronman() && (!player.isDonator() && !player.hasVotedInLast24Hours())) {
			player.getPackets().sendGameMessage("You can't use this feature as an ironman unless you vote or donate.");
			return false;
		}*/
		if (player.isDungeoneer()) {
			player.getPackets().sendGameMessage("You can't use this feature as a dungeoneer.");
			return false;
		}

		return true;
	}

	public static void pushLeverTeleport(final Player player, final WorldTile tile) {
		pushLeverTeleport(player, tile, 2140, null, null);
	}

	public static void pushLeverTeleport(final Player player, final WorldTile tile, int emote, String startMessage, final String endMessage) {
		if (!player.getControlerManager().processObjectTeleport(tile))
			return;
		player.setNextAnimation(new Animation(emote));
		if (startMessage != null)
			player.getPackets().sendGameMessage(startMessage, true);
		player.lock();
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.unlock();
				Magic.sendObjectTeleportSpell(player, false, tile);
				if (endMessage != null)
					player.getPackets().sendGameMessage(endMessage, true);
			}
		}, 1);
	}

	public static final void sendObjectTeleportSpell(Player player, boolean randomize, WorldTile tile) {
		sendTeleportSpell(player, 8939, 8941, 1576, 1577, 0, 0, tile, 3, randomize, OBJECT_TELEPORT);
	}

	public static final void sendDelayedObjectTeleportSpell(Player player, int delay, boolean randomize, WorldTile tile) {
		sendTeleportSpell(player, 8939, 8941, 1576, 1577, 0, 0, tile, delay, randomize, OBJECT_TELEPORT);
	}
	
	public static final boolean sendTeleportSpell(final Player player, int upEmoteId, final int downEmoteId, int upGraphicId, final int downGraphicId, int level, final double xp, final WorldTile tile, int delay, final boolean randomize, final int teleType, int... runes) {
		return sendTeleportSpell(player, upEmoteId, downEmoteId, upGraphicId, downGraphicId, level, xp, tile, delay, randomize, teleType, false, runes);
	}

	public static final boolean sendTeleportSpell(final Player player, int upEmoteId, final int downEmoteId, int upGraphicId, final int downGraphicId, int level, final double xp, final WorldTile tile, int delay, final boolean randomize, final int teleType, final boolean dung, int... runes) {
		if (player.isLocked())
			return false;
		if (player.getSkills().getLevel(Skills.MAGIC) < level) {
			player.getPackets().sendGameMessage("Your Magic level is not high enough for this spell.");
			return false;
		}
		if (!checkRunes(player, false, dung, runes))
			return false;
		final WorldTile checkTile = tile == null ? player.getHouse().getLocation().getTile() : tile;
		if (teleType == MAGIC_TELEPORT) {
			if (!player.getControlerManager().processMagicTeleport(checkTile))
				return false;
		} else if (teleType == ITEM_TELEPORT) {
			if (!player.getControlerManager().processItemTeleport(checkTile))
				return false;
		} else if (teleType == OBJECT_TELEPORT) {
			if (!player.getControlerManager().processObjectTeleport(checkTile))
				return false;
		}

		// nullify all damage on teleport

		player.setTeleporting(true);
		checkRunes(player, true, dung, runes);
		player.stopAll();
		if (upEmoteId != -1)
			player.setNextAnimation(new Animation(upEmoteId));
		if (upGraphicId != -1)
			player.setNextGraphics(new Graphics(upGraphicId));
		if (teleType == MAGIC_TELEPORT)
			player.getPackets().sendSound(5527, 0, 2);
		player.lock(3 + delay);
		WorldTasksManager.schedule(new WorldTask() {

			boolean removeDamage;

			@Override
			public void run() {
				if (!removeDamage) {
					WorldTile teleTile = checkTile;
					if (randomize) {
						// attemps to randomize tile by 4x4 area
						for (int trycount = 0; trycount < 10; trycount++) {
							teleTile = new WorldTile(checkTile, 2);
							if (World.isTileFree(checkTile.getPlane(), teleTile.getX(), teleTile.getY(), player.getSize()))
								break;
							teleTile = checkTile;
						}
					}
					player.setNextWorldTile(teleTile);
					player.getControlerManager().magicTeleported(teleType);
					if (xp != 0)
						player.getSkills().addXp(Skills.MAGIC, xp);
					if (downEmoteId != -1)
						player.setNextAnimation(new Animation(downEmoteId == -2 ? -1 : downEmoteId));
					if (downGraphicId != -1)
						player.setNextGraphics(new Graphics(downGraphicId));
					if (teleType == MAGIC_TELEPORT) {
						player.getPackets().sendSound(5524, 0, 2);
						player.setNextFaceWorldTile(new WorldTile(teleTile.getX(), teleTile.getY() - 1, teleTile.getPlane()));
						player.setDirection(6);
					}
					if (tile == null && !player.getHouse().isArriveInPortal())
						player.getHouse().enterMyHouse();
					else if (player.getControlerManager().getControler() == null)
						teleControlersCheck(player, teleTile);
					removeDamage = true;
				} else {
					player.resetReceivedHits();
					player.setTeleporting(false);
					stop();
				}
			}
		}, delay, 0);
		return true;
	}
	
	private final static WorldTile[] TABS =
	{ new WorldTile(3217, 3426, 0), new WorldTile(3222, 3218, 0), new WorldTile(2965, 3379, 0), new WorldTile(2758, 3478, 0), new WorldTile(2660, 3306, 0), new WorldTile(2549, 3115, 2), null };

	public static void useVecnaSkull(Player player) {
		Long time = (Long) player.getTemporaryAttributtes().get("VecnaSkullDelay");
		long currentTime = Utils.currentTimeMillis();
		if (time != null && time >= currentTime) {
			int minutes = (int) ((time - currentTime) / 1000 / 60);
			player.getPackets().sendGameMessage("The skull has not yet regained its mysterious aura. You will have to wait another " + minutes + " minutes.");
			return;
		}
		int newLevel = player.getSkills().getLevel(Skills.MAGIC) + 6;
		int maxLevel = player.getSkills().getLevelForXp(Skills.MAGIC) + 6;
		if (newLevel > maxLevel)
			newLevel = maxLevel;
		player.getSkills().set(Skills.MAGIC, newLevel);
		player.setNextAnimation(new Animation(10530));
		player.setNextGraphics(new Graphics(738, 0, 100));
		player.getTemporaryAttributtes().put("VecnaSkullDelay", currentTime + 1000 * 60 * 8);
		player.getPackets().sendGameMessage("The skull feeds off the life arround you, boosting your magical ability.");
	}

	public static void imbuedHeart(Player player) {
		long currentTime = Utils.currentTimeMillis();
		Long time = (Long) player.getTemporaryAttributtes().get("VecnaSkullDelay");

		if (time != null && time >= currentTime) {
			int minutes = (int) ((time - currentTime) / 1000 / 60);
			player.getPackets().sendGameMessage("The heart is still drained of its power. Judging by how it feels, it will be ready in " + (minutes == 0 ? "under a minute." : " around " + minutes + " minutes."));
			return;
		}
		//int newLevel = player.getSkills().getLevel(Skills.MAGIC) + 11;
		int maxLevel = player.getSkills().getLevelForXp(Skills.MAGIC) + 11;
		//if (newLevel > maxLevel)
		//	newLevel = maxLevel;
		player.getSkills().set(Skills.MAGIC, maxLevel);
		player.gfx(6316);
		player.getTemporaryAttributtes().put("VecnaSkullDelay", currentTime + 1000 * 60 * 8);
		player.getPackets().setTimer(0, 50724, 1000 * 60 * 8);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				Long time = (Long) player.getTemporaryAttributtes().get("VecnaSkullDelay");
				// override all other bonuses
				if (!player.isDead()
						&& time != null && time > Utils.currentTimeMillis()) {
					player.getSkills().set(Skills.MAGIC, maxLevel);
				} else {
					player.sendMessage("The effects of your imbued heart wears off.");
					player.getSkills().set(Skills.MAGIC, player.getSkills().getLevelForXp(Skills.MAGIC));
					stop();
				}
			}
		}, 0, 0);
	}
	
	
	public static void usePKTabTeleport(Player player) {
		if (!Wilderness.isAtWild(player) || Wilderness.isAtWildSafe(player)) {
			player.getPackets().sendGameMessage("You can only use this tab inside wilderness.");
			return;
		}
		Long lastTele = (Long) player.getTemporaryAttributtes().get("LAST_PKTAB");
		if (lastTele != null && lastTele + 60000*2 > Utils.currentTimeMillis()) {
			player.getPackets().sendGameMessage("Players may only use pk tabs once every 2 minutes.");
			return;
		}
		List<Player> tiles = new ArrayList<Player>();
		for (Player p : World.getPlayers()) {
			if (p == player || !(p.getControlerManager().getControler() instanceof Wilderness) || Wilderness.isAtWildSafe(p)
					|| !p.getControlerManager().canHit(player)
					|| (Wilderness.getRiskedWealth(player)) < 100000
					/*|| p.getRegionId() == 11837*/)
				continue;
			tiles.add(p);
		}
		if (tiles.isEmpty()) {
			player.getPackets().sendGameMessage("There is currently no one attackable in wilderness.");
			return;
		}
		Player target = tiles.get(Utils.random(tiles.size()));
		
		//worldmap
		int coordinateHash = target.getTileHash();
		int x = coordinateHash >> 14;
		int y = coordinateHash & 0x3fff;
		int plane = coordinateHash >> 28;
		player.getHintIconsManager().addHintIcon(x, y, plane, 20, 0, 2, -1, true);
		player.getVarsManager().sendVar(1159, coordinateHash);
		
	//	if (useTeleTab(player, new WorldTile(target), false)) {
			player.getInventory().deleteItem(25433, 1);
			//target.getPackets().sendGameMessage("<col=FF0040>"+player.getName()+" used pk locator to find you!");
			player.getPackets().sendGameMessage("<col=FF0040>The gods curse you for revealing the location of another player.");
			player.getTemporaryAttributtes().put("LAST_PKTAB", Utils.currentTimeMillis());
			player.setWildernessSkull();
	//	}
	}
	
	public static boolean useAncientTabTeleport(final Player player, final int itemId) {
		if (itemId < 42775 || itemId > 42775 + ANCIENT_TABS.length - 1)
			return false;
		if (useTeleTab(player, ANCIENT_TABS[itemId - 42775], true))
			player.getInventory().deleteItem(itemId, 1);
		return true;
	}
	
	public static boolean useTabTeleport(final Player player, final int itemId) {
		if (itemId < 8007 || itemId > 8007 + TABS.length - 1)
			return false;
		if (useTeleTab(player, TABS[itemId - 8007], true))
			player.getInventory().deleteItem(itemId, 1);
		return true;
	}

	public static boolean useTeleTab(final Player player, final WorldTile tile, boolean randomize) {
		if (!player.getControlerManager().processItemTeleport(tile))
			return false;
		player.lock();
		player.setNextAnimation(new Animation(9597));
		player.setNextGraphics(new Graphics(1680));
		player.setTeleporting(true);
		final boolean arriveInHouse = player.getHouse().isArriveInPortal();
		WorldTasksManager.schedule(new WorldTask() {
			int stage;

			@Override
			public void run() {
				if (stage == 0) {
					player.setNextAnimation(new Animation(4731));
					stage = 1;
				} else if (stage == 1) {
					WorldTile checkTile = tile == null ? player.getHouse().getLocation().getTile() : tile;
					WorldTile teleTile = null;
					
					if (randomize) {
						// attemps to randomize tile by 4x4 area
						for (int trycount = 0; trycount < 10; trycount++) {
							teleTile = new WorldTile(checkTile, 2);
							if (World.isTileFree(checkTile.getPlane(), teleTile.getX(), teleTile.getY(), player.getSize()))
								break;
							teleTile = checkTile;
						}
					} else
						teleTile = tile;
					player.setNextWorldTile(teleTile);
					player.getControlerManager().magicTeleported(ITEM_TELEPORT);
					player.setNextFaceWorldTile(new WorldTile(teleTile.getX(), teleTile.getY() - 1, teleTile.getPlane()));
					player.setDirection(6);
					player.setNextAnimation(new Animation(-1));
					if (tile == null && !arriveInHouse)
						player.getHouse().enterMyHouse();
					else if (player.getControlerManager().getControler() == null)
						teleControlersCheck(player, teleTile);
					stage = 2;
				} else if (stage == 2) {
					player.resetReceivedHits();
					if (tile != null || arriveInHouse)
						player.unlock();
					stop();
					player.setTeleporting(false);
				}

			}
		}, 2, 1);
		return true;
	}

	public static void teleControlersCheck(Player player, WorldTile teleTile) {
		if (Kalaboss.isAtKalaboss(teleTile))
			player.getControlerManager().startControler("Kalaboss");
		else if (Wilderness.isAtWild(teleTile))
			player.getControlerManager().startControler("Wilderness");
		else if (ClanWarRequestController.inWarRequest(player))
			player.getControlerManager().startControler("clan_wars_request");
		else if (GodWars.isAtGodwars(teleTile)) {
			player.getControlerManager().startControler("GodWars");
			Controller activity = player.getControlerManager().getControler();
			if (activity instanceof GodWars) {
				int mapID = teleTile.getRegionId();
				((GodWars)activity).setSector(
						mapID == 11347 ? GodWars.BANDOS_SECTOR
								: mapID == 11603 ? GodWars.ZAMORAK_SECTOR 
										: mapID == 11346 ? GodWars.ARMADYL_SECTOR :
											mapID == 11602 ? GodWars.SARADOMIN_SECTOR :
												mapID == 11601 ? GodWars.ZAROS_SECTOR :
													GodWars.EMPTY_SECTOR
										);
				if (mapID == 11603) //zammy
					activity.sendInterfaces();
			}
		}
	}

	private Magic() {

	}

	public static void useEctoPhial(final Player player, Item item) {
		//rs fills it anyway
	//	player.getInventory().deleteItem(item);
		player.setNextGraphics(new Graphics(1688));
		player.setNextAnimation(new Animation(9609));
		//player.setTeleporting(true);
		player.lock(7);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				sendTeleportSpell(player, 8939, 8941, 1678, 1679, 0, 0, new WorldTile(3662, 3518, 0), 4, true, ITEM_TELEPORT);
			}
		}, 6);
	}

	/**
	 * @author dragonkk(Alex)
	 * Jun 14, 2018
	 * @param id
	 * @return
	 */
	public static boolean isRune(int id) {
		return (id >= 554 && id <= 566) || id == 9075;
	}
}
