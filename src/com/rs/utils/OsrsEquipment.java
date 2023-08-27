/**
 * 
 */
package com.rs.utils;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.game.player.Equipment;

/**
 * @author dragonkk(Alex) Oct 16, 2017
 */
public class OsrsEquipment {

	private static String[] CAPES = { "cloak", "cape", "ava's", "tokhaar", "sinhaza shroud" };

	private static String[] HATS = { "faceguard", "fedora", "visor", "ears", "goggles", "bearhead", "tiara", "cowl", "druidic wreath", "halo",
			"crown", "sallet", "helm", "hood", "coif", "flaming skull", "Coif", "partyhat", "hat", "cap", " bandana",
			"full helm (t)", "full helm (g)", "full helm (or)", "cav", "boater", "helmet", "afro", "beard",
			"gnome goggles", "mask", "Helm of neitiznot", "mitre", "nemes", "wig", "headdress",
			" head"};

	private static String[] BOOTS = { "boots", "Boots", "shoes", "Shoes", "flippers", "socks", " feet" };

	private static String[] GLOVES = { "gloves", "gauntlets", "Gloves", "vambraces", "vamb", "bracers", "brace" };

	private static String[] AMULETS = { "stole", "amulet", "necklace", "Amulet of", "scarf",
			"Super dominion medallion", "xeric's talisman" };

	private static String[] SHIELDS = { "tome of frost", "kiteshield", "sq shield", "Toktz-ket", "books", "book",
			"kiteshield (t)", "kiteshield (g)", "kiteshield(h)", "defender", "shield", "deflector", "off-hand", " ward", "tome", "buckler"};

	private static String[] ARROWS = { "arrow", "arrows", "arrow(p)", "arrow(+)", "arrow(s)", "bolt", "Bolt rack",
			"Opal bolts", "Dragon bolts", "bolts (e)", "bolts", "Hand cannon shot", "dragon javelin" };

	private static String[] RINGS = { "ring", "elven signet" };

	private static String[] BODY = { "poncho", "apron", "robe top", "armour", "hauberk", "platebody", "chainbody",
			"breastplate", "blouse", "robetop", "leathertop", "platemail", "top", "brassard", "body", "platebody (t)",
			"chestguard", "platebody (g)", "body(g)", "body_(g)", "chestplate", "torso", "shirt", "Rock-shell plate",
			"coat", "jacket", " wings"  };

	private static String[] AURAS = { "poison purge", "Salvation", "Corruption", "salvation", "corruption",
			"runic accuracy", "sharpshooter", "lumberjack", "quarrymaster", "call of the sea", "reverence",
			"five finger discount", "resourceful", "equilibrium", "inspiration", "vampyrism", "penance", "wisdom",
			"jack of trades", "gaze" };

	private static int[] BODY_LIST = { 21463, 21549, 544, 6107 };

	private static int[] LEGS_LIST = { 542, 6108, 10340, 7398,  43389};

	private static String[] LEGS = { "pantaloons", "legguards", "leggings", "void knight robe", "druidic robe", "cuisse", "pants", "platelegs",
			"plateskirt", "skirt", "bottoms", "chaps", "platelegs (t)", "platelegs (or)", "platelegs (g)", "bottom",
			"skirt", "skirt (g)", "skirt (t)", "chaps (g)", "chaps (t)", "tassets", "legs", "trousers", "robe bottom",
			"shorts", };

	private static String[] WEAPONS = {"tephra", "lance", "dawnbringer", "hammer", "hasta", "ballista", "bludgeon", "blowpipe", "bolas", "stick", "blade", "Butterfly net", "scythe", "rapier", "hatchet",
			"bow", "Hand cannon", "sword of destruction", "Inferno adze", "arclight", "Silverlight", "Darklight", "wand",
			"Statius's warhammer", "anchor", "spear.", "Vesta's longsword.", "scimitar", "longsword",
			"sword", "longbow", "shortbow", "dagger", "mace", "halberd", "spear", "tentacle", "Abyssal whip", "Abyssal vine whip",
			"Ornate katana", "axe", "flail", "crossbow", "Torags hammers", "Crossbow of love", "dagger(p)",
			"dagger (p++)", "dagger(+)", "dagger(s)", "spear(p)", "spear(+)", "spear(s)", "spear(kp)", "maul", "dart",
			"dart(p)", "javelin", "javelin(p)", "knife", "knife(p)", "Longbow", "primal 1h sword", "Shortbow",
			"Crossbow", "Toktz-xil", "Shark fists", "Toktz-mej", "Tzhaar-ket", "staff", "Staff", "godsword", "c'bow",
			"Crystal bow", "Dark bow", "staff of peace", "claws", "warhammer", "hammers", "adze", "hand", "Broomstick",
			"Upgraded Korasi", "Flowers", "flowers", "trident", "excalibur", "cane", "sled", "Katana", "bag", "Cataclysm",
			"tenderiser", "eggsterminator", "Sled", "sceptre", "decimation", "obliteration", "annihilation", "chinchompa", "bulwark", "harpoon" };

	private static String[] NOT_FULL_BODY = { "zombie shirt" };

	private static String[] FULL_BODY = { "tunic", "chestguard", "robe", "breastplate", "blouse", "pernix body", "vesta's chainbody", "armour",
			"hauberk", "top", "shirt", "platebody", "Ahrims robetop", "Karils leathertop", "brassard", "chestplate",
			"torso", "Morrigan's", "Zuriel's", "changshan jacket", " wings" };

	private static String[] FULL_HAT = {   "hood", "great helm", "ankou mask", "faceguard", "helm", "cowl", "sallet", "med helm", "coif", "Dharoks helm", "Initiate helm",
			"Coif", "Helm of neitiznot" };

	private static String[] FULL_MASK = {  "great helm","ankou mask", "faceguard", "obsidian", "serpentine", "sallet", "mask", "full helm", "mask", "Veracs helm", "Guthans helm",
			"Torags helm", "flaming skull", "Karils coif", "full helm (t)", "full helm (g)", "crystal helm" };

	public static boolean isFullBody(ItemConfig config) {
		String itemName = config.getName();
		if (itemName == null)
			return false;
/*		if (config.getId() == 52327)
			return true;*/
		itemName = itemName.toLowerCase();
		for (int i = 0; i < NOT_FULL_BODY.length; i++)
			if (itemName.contains(NOT_FULL_BODY[i].toLowerCase()))
				return false;
		for (int i = 0; i < FULL_BODY.length; i++)
			if (itemName.contains(FULL_BODY[i].toLowerCase()))
				return true;
		return false;
	}

	public static boolean isFullHat(ItemConfig config) {
		String itemName = config.getName();
		if (itemName == null)
			return false;
		itemName = itemName.toLowerCase();
		if (itemName.contains("hood hat"))
			return false;
		for (int i = 0; i < FULL_HAT.length; i++) {
			if (itemName.contains(FULL_HAT[i].toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public static boolean isFullMask(ItemConfig config) {
		String itemName = config.getName();
		if (itemName == null)
			return false;
		itemName = itemName.toLowerCase();
		for (int i = 0; i < FULL_MASK.length; i++)
			if (itemName.contains(FULL_MASK[i].toLowerCase()))
				return true;
		return false;
	}

	public static int getItemSlot(ItemConfig config) {
		if (config.getId() >= 52388 && config.getId() <= 52396)
			return Equipment.SLOT_CAPE;
		if (config.getId() == 52516)
			return Equipment.SLOT_WEAPON;
		for (int i = 0; i < BODY_LIST.length; i++)
			if (config.id == BODY_LIST[i])
				return 4;
		for (int i = 0; i < LEGS_LIST.length; i++)
			if (config.id == LEGS_LIST[i])
				return 7;
		String item = config.getName().toLowerCase();
		if (item == null)
			return -1;
		for (int i = 0; i < CAPES.length; i++)
			if (item.contains(CAPES[i].toLowerCase()))
				return 1;
		for (int i = 0; i < BOOTS.length; i++)
			if (item.contains(BOOTS[i].toLowerCase()))
				return 10;
		for (int i = 0; i < GLOVES.length; i++)
			if (item.contains(GLOVES[i].toLowerCase()))
				return 9;
		for (int i = 0; i < SHIELDS.length; i++)
			if (item.contains(SHIELDS[i].toLowerCase()))
				return 5;
		for (int i = 0; i < AMULETS.length; i++)
			if (item.contains(AMULETS[i].toLowerCase()))
				return 2;
		for (int i = 0; i < ARROWS.length; i++)
			if (item.contains(ARROWS[i].toLowerCase()))
				return 13;
		for (int i = 0; i < RINGS.length; i++)
			if (item.contains(RINGS[i].toLowerCase()))
				return 12;
		for (int i = 0; i < WEAPONS.length; i++)
			if (item.contains(WEAPONS[i].toLowerCase()))
				return 3;
		for (int i = 0; i < HATS.length; i++)
			if (item.contains(HATS[i].toLowerCase()))
				return 0;
		for (int i = 0; i < BODY.length; i++)
			if (item.contains(BODY[i].toLowerCase()))
				return 4;
		for (int i = 0; i < LEGS.length; i++)
			if (item.contains(LEGS[i].toLowerCase()))
				return 7;
		for (int i = 0; i < AURAS.length; i++)
			if (item.contains(AURAS[i].toLowerCase()))
				return Equipment.SLOT_AURA;
		
		if (config.getId() >= Settings._685_ITEM_OFFSET) 
			return getItemSlot(ItemConfig.forID(config.getId() - Settings._685_ITEM_OFFSET));
		
		return -1;
	}

	public static boolean isTwoHandedWeapon(ItemConfig config) {
		String wepEquiped = config.getName().toLowerCase();
		if (wepEquiped == null)
			return false;
		else if (wepEquiped.contains("blessed sword"))
			return true;
		else if (wepEquiped.equals("stone of power"))
			return true;
		else if (wepEquiped.equals("dominion sword"))
			return true;
		else if (wepEquiped.endsWith("claws"))
			return true;
		else if (wepEquiped.endsWith("anchor"))
			return true;
		else if (wepEquiped.endsWith("bludgeon"))
			return true;
		else if (wepEquiped.endsWith("blowpipe"))
			return true;
		else if (wepEquiped.contains("2h sword"))
			return true;
		else if (wepEquiped.contains("katana"))
			return true;
		else if (wepEquiped.equals("seercull"))
			return true;
		else if (wepEquiped.contains("shortbow"))
			return true;
		else if (wepEquiped.contains("longbow"))
			return true;
		else if (wepEquiped.endsWith(" bow"))
			return true;
		else if (wepEquiped.contains("scythe"))
			return true;
		else if (wepEquiped.contains("shortbow"))
			return true;
		else if (wepEquiped.contains("bow full"))
			return true;
		else if (wepEquiped.equals("zaryte bow"))
			return true;
		else if (wepEquiped.equals("dark bow"))
			return true;
		else if (wepEquiped.contains("halberd"))
			return true;
		else if (wepEquiped.contains("maul"))
			return true;
		else if (wepEquiped.equals("karil's crossbow"))
			return true;
		else if (wepEquiped.equals("torag's hammers"))
			return true;
		else if (wepEquiped.equals("verac's flail"))
			return true;
		else if (wepEquiped.contains("greataxe"))
			return true;
		else if (wepEquiped.contains("spear"))
			return true;
		else if (wepEquiped.equals("tzhaar-ket-om"))
			return true;
		else if (wepEquiped.contains("godsword"))
			return true;
		else if (wepEquiped.equals("saradomin sword"))
			return true;
		else if (wepEquiped.equals("hand cannon"))
			return true;
		else if (wepEquiped.equals("primal 1h sword"))
			return true;
		else if (wepEquiped.contains("ballista"))
			return true;
		else if (wepEquiped.endsWith("bulwark"))
			return true;
		return false;
	}

}
