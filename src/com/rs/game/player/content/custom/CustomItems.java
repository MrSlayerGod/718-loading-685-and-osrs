package com.rs.game.player.content.custom;

/**
 *
 */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemConfig;
import com.rs.game.player.AuraManager;
import com.rs.game.player.Equipment;
import com.rs.game.player.Skills;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;

/**
 * @author dragonkk(Alex) Sep 15, 2017 f TOO LAZY TO EDIT CACHE
 */
public class CustomItems {

	public static void main(String[] args) {
		try {
			Cache.init();
		} catch (IOException e) {
			e.printStackTrace();
		}
		dumpItems();
	}


	public static void dumpItems() {
		ItemConfig c = null;
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(System.getProperty("user.home") + "/Desktop/Onyx item dump.txt", false))) {
			for (int i = 0; i < 82442; i++) {
				try {
					c = null;
					try {
						c = ItemConfig.forID(i);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if(c == null) {
						bw.write("" + i + " - " + (c.name == null ? "null" : "\"" + c.name + "\"") + "");
						bw.newLine();
					} else {
						bw.write("" + i + " - null");
						bw.newLine();
					}
				} catch (Exception e) {
					System.out.println("ERROR AT " +i);
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    public static void makeNote(int noteID, int unnotedID) {
    	ItemConfig config = ItemConfig.forID(unnotedID);
    	config.cert = noteID;
    	ItemConfig note = ItemConfig.forID(noteID);
    	copy(4152, note);
    	note.name = config.name;
    	note.cert = unnotedID;
    	note.certTemplate = 799;
    	note.noted = true;
    }
    
    private static boolean settedNotes;
    
    public static void makeNotes() {
    	settedNotes = true;
        for (int i = 0; i <= 35; i++) //overload and so on
        	makeNote(25703 + i, 15300 + i);
    }

	public static void modify(ItemConfig config) {
		if (!settedNotes) 
    		makeNotes();
		config.setHPOld();
		int id = config.getId();

		for(int i : ChambersOfXeric.getCoxItems()) {
			if(id == i) {
				config.tradeable = true;
			}
		}

		for (int i = 0; i < LUCKY_ITEMS.length; i += 2) {
			if (LUCKY_ITEMS[i] == id) {
				copy(ItemConfig.forID(LUCKY_ITEMS[i + 1]), config);
				config.name = "Lucky " + config.name.toLowerCase();
				config.inventoryOptions[4] = "Drop";
				config.setData(1397, 1);
				break;
			}
		}

		if (config.name.startsWith("Lucky ")) { // tradeable & kept on death outside wild
			config.inventoryOptions[4] = "Drop";
			config.tradeable = true;
			config.name = config.name.replace("Lucky ", "");
			config.name = Character.toUpperCase(config.name.charAt(0)) + config.name.substring(1);
			config.name += " (d)";
		}

		if(id == 51079 || id == 51047 || id == 51034)
			config.tradeable = true;

		if ((id >= 15300 && id <= 15335) || (id >= 23483 && id <= 23536)) {
			config.tradeable = true;
			config.value *= 200; // makes selling them to shop good money
		} else if ((id >= 42695 && id <= 42702)) {
			config.tradeable = true;
			if (!config.isNoted())
				config.value *= 50; // makes selling them to shop good money
		} else if (id == 21630) {
			config.value *= 100;
		} else if (config.getEquipSlot() == Equipment.SLOT_AURA) {
			int price = AuraManager.getPrice(id);
			if (price > 0)
				config.value = price;
		}

		if ((id == 6133 + Settings._685_ITEM_OFFSET) || (id == 6139 + Settings._685_ITEM_OFFSET)
				|| (id == 6129 + Settings._685_ITEM_OFFSET) || (id == 6107 + Settings._685_ITEM_OFFSET)
				|| (id == 6184 + Settings._685_ITEM_OFFSET) || id == 52327 || id == 54420 || id == 21473 + Settings._685_ITEM_OFFSET
				|| id == 21468 + Settings._685_ITEM_OFFSET || id == 21463 + Settings._685_ITEM_OFFSET
				|| id == 21480 + Settings._685_ITEM_OFFSET || id == 21484 + Settings._685_ITEM_OFFSET
				|| (id >= 20163 + Settings._685_ITEM_OFFSET && id <= 20166 + Settings._685_ITEM_OFFSET)
				|| (id >= 20151 + Settings._685_ITEM_OFFSET && id <= 20154 + Settings._685_ITEM_OFFSET)
				|| (id >= 20139 + Settings._685_ITEM_OFFSET && id <= 20146 + Settings._685_ITEM_OFFSET))
			config.equipType = 6;

		if ((id >= 1053 + Settings._685_ITEM_OFFSET && id <= 1057 + Settings._685_ITEM_OFFSET)
				|| (id >= 20159 + Settings._685_ITEM_OFFSET && id <= 20162 + Settings._685_ITEM_OFFSET))
			config.equipType = 8;

		switch (config.getId()) {
		case 2677:
		case 2801:
		case 2722:
		case 19043:
			config.inventoryOptions[3] = "Dig";
			break;
        case 1267:
        case 1269:
        case 1273:
        case 1271:
        case 1275:
        case 15259:
        case 1349: 
        case 1353: 
        case 1357: 
        case 1355:
        case 1359:
        case 6739:
            config.inventoryOptions[3] = "Add-to-toolbelt";
        	break;
		case 53242:
			config.clientScriptData = (HashMap<Integer, Object>) ItemConfig.forID(10346).clientScriptData.clone();
			break;
			case 25665:
				config.name = "Sotetseg pet";
				break;
			case 25661:
				copy(ItemConfig.forID(16955), config);
				config.name = "Rapier of insanity";
				config.inventoryOptions[2] = "";
				config.setData(14, 2);
				config.setStrengthBonus(95);
				config.setStabAttack(90);
				config.setLevel(Skills.ATTACK, 85);
				config.tradeable = true;
				break;
			case 25656:
				copy(15217, config);
				config.name = "Horrific right arm";
				config.inventoryOptions[0] = null;
				break;
			case 25657:
				copy(15218, config);
				config.name = "Horrific left arm";
				config.inventoryOptions[0] = null;
				break;
			case 25658:
				copy(15219, config);
				config.name = "Horrific tail";
				config.inventoryOptions[0] = null;
				break;
			case 25655:
				copy(42399, config);
				config.name = "December 2020 top donator pet";
				break;
			case 25654:
				copy(50851, config);
				config.name = "Twisted olmlet";
				break;
			case 25653:
				copy(50851, config);
				config.name = "Elder olmlet";
				break;
			case 25652:
				copy(50851, config);
				config.name = "Ancestral olmlet";
				break;
			case 25630:
				copy(73902, config);
				config.name = "Elder warhammer";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(3);
				config.setSlashAttack(3);
				config.setCrushAttack(145);
				config.setMagicAttack(0);
				config.setRangeAttack(0);
				config.setStabDef(0);
				config.setSlashDef(8);
				config.setCrushDef(10);
				config.setMagicDef(0);
				config.setRangeDef(0);
				config.setStrengthBonus(130);
				config.setRangedStrBonus(0);
				config.setMagicDamage(0);
				config.setPrayerBonus(1);
				config.setLevel(Skills.ATTACK, 80);
				config.setLevel(Skills.STRENGTH, 80);
				break;

			case 25629:
				copy(51012, config);
				config.name = "Dragonshredder crossbow";
				config.value = 100000000;
				config.tradeable = true;
				config.stackable = 0;
				config.setStabAttack(0);
				config.setSlashAttack(0);
				config.setCrushAttack(0);
				config.setMagicAttack(0);
				config.setRangeAttack(100);
				config.setStabDef(0);
				config.setSlashDef(0);
				config.setCrushDef(0);
				config.setMagicDef(0);
				config.setRangeDef(0);
				config.setStrengthBonus(0);
				config.setRangedStrBonus(10);
				config.setMagicDamage(0);
				config.setPrayerBonus(2);
				config.setLevel(Skills.RANGE, 80);
				break;

			case 25628:
				copy(73744, config);
				config.name = "Corrosive spirit shield";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(15);
				config.setSlashAttack(15);
				config.setCrushAttack(15);
				config.setMagicAttack(10);
				config.setRangeAttack(15);
				config.setStabDef(63);
				config.setSlashDef(65);
				config.setCrushDef(75);
				config.setMagicDef(30);
				config.setRangeDef(57);
				config.setStrengthBonus(1);
				config.setRangedStrBonus(10);
				config.setMagicDamage(0);
				config.setPrayerBonus(3);
				config.setLevel(Skills.RANGE, 80);
				config.setLevel(Skills.DEFENCE, 80);
				break;

			case 25633:
				copy(74484, config);
				config.name = "Dragonhunter claws";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(50);
				config.setSlashAttack(75);
				config.setCrushAttack(0);
				config.setMagicAttack(0);
				config.setRangeAttack(0);
				config.setStabDef(26);
				config.setSlashDef(52);
				config.setCrushDef(14);
				config.setMagicDef(0);
				config.setRangeDef(0);
				config.setStrengthBonus(75);
				config.setRangedStrBonus(0);
				config.setMagicDamage(0);
				config.setPrayerBonus(3);
				config.setLevel(Skills.ATTACK, 80);
				config.setLevel(Skills.STRENGTH, 80);
				break;

			case 25631:
				copy(54463, config);
				config.name = "Tekton's blueprints";
				config.value = 100000000;
				config.tradeable = true;
				break;

			case 25632:
				copy(69016, config);
				config.name = "Olm's claw";
				config.value = 100000000;
				config.tradeable = true;
				break;

			case 25627:
				copy(13752, config);
				config.name = "Corrosive sigil";
				config.value = 100000000;
				config.tradeable = true;
				break;
			case 25552:
				copy(1050, config);
				config.name = "Evil Santa's Hat";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(1);
				config.setSlashAttack(1);
				config.setCrushAttack(1);
				config.setMagicAttack(8);
				config.setRangeAttack(-15);
				config.setStabDef(70);
				config.setSlashDef(75);
				config.setCrushDef(65);
				config.setMagicDef(13);
				config.setRangeDef(-5);
				config.setStrengthBonus(4);
				config.setRangedStrBonus(0);
				config.setMagicDamage(2);
				config.setPrayerBonus(1);
				config.setLevel(Skills.DEFENCE, 80);
				break;
			case 25551:
				copy(14595, config);
				config.name = "Evil Santa's Robe Top";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(1);
				config.setSlashAttack(1);
				config.setCrushAttack(1);
				config.setMagicAttack(38);
				config.setRangeAttack(-20);
				config.setStabDef(117);
				config.setSlashDef(111);
				config.setCrushDef(126);
				config.setMagicDef(48);
				config.setRangeDef(-15);
				config.setStrengthBonus(4);
				config.setRangedStrBonus(0);
				config.setMagicDamage(2);
				config.setPrayerBonus(1);
				config.setLevel(Skills.DEFENCE, 80);
				break;
			case 25549:
				copy(14602, config);
				config.name = "Evil Santa's Gloves";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(12);
				config.setSlashAttack(12);
				config.setCrushAttack(0);
				config.setMagicAttack(5);
				config.setRangeAttack(-10);
				config.setStabDef(10);
				config.setSlashDef(10);
				config.setCrushDef(10);
				config.setMagicDef(10);
				config.setRangeDef(-2);
				config.setStrengthBonus(12);
				config.setRangedStrBonus(0);
				config.setMagicDamage(1);
				config.setPrayerBonus(1);
				config.setLevel(Skills.DEFENCE, 80);
				break;
			case 25550:
				copy(14603, config);
				config.name = "Evil Santa's Robe Bottom";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(1);
				config.setSlashAttack(1);
				config.setCrushAttack(1);
				config.setMagicAttack(28);
				config.setRangeAttack(-10);
				config.setStabDef(85);
				config.setSlashDef(75);
				config.setCrushDef(79);
				config.setMagicDef(30);
				config.setRangeDef(-12);
				config.setStrengthBonus(3);
				config.setRangedStrBonus(0);
				config.setMagicDamage(2);
				config.setPrayerBonus(1);
				config.setLevel(Skills.DEFENCE, 80);
				break;
			case 25548:
				copy(14605, config);
				config.name = "Evil Santa's Boots";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(1);
				config.setSlashAttack(1);
				config.setCrushAttack(1);
				config.setMagicAttack(5);
				config.setRangeAttack(-2);
				config.setStabDef(20);
				config.setSlashDef(21);
				config.setCrushDef(22);
				config.setMagicDef(4);
				config.setRangeDef(-2);
				config.setStrengthBonus(3);
				config.setRangedStrBonus(0);
				config.setMagicDamage(1);
				config.setPrayerBonus(1);
				config.setLevel(Skills.DEFENCE, 80);
				break;
			case 25546:
				copy(51012, config);
				config.name = "Evil Santa's crossbow";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(0);
				config.setSlashAttack(0);
				config.setCrushAttack(0);
				config.setMagicAttack(0);
				config.setRangeAttack(85);
				config.setStabDef(0);
				config.setSlashDef(0);
				config.setCrushDef(0);
				config.setMagicDef(0);
				config.setRangeDef(0);
				config.setStrengthBonus(0);
				config.setRangedStrBonus(5);
				config.setMagicDamage(0);
				config.setPrayerBonus(3);
				config.setLevel(Skills.RANGE, 90);
				break;
			case 25547:
				copy(13902, config);
				config.name = "Evil Santa's warhammer";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(3);
				config.setSlashAttack(3);
				config.setCrushAttack(125);
				config.setMagicAttack(0);
				config.setRangeAttack(0);
				config.setStabDef(0);
				config.setSlashDef(8);
				config.setCrushDef(10);
				config.setMagicDef(0);
				config.setRangeDef(0);
				config.setStrengthBonus(120);
				config.setRangedStrBonus(0);
				config.setMagicDamage(0);
				config.setPrayerBonus(0);
				config.setLevel(Skills.ATTACK, 80);
				config.setLevel(Skills.STRENGTH, 85);
				break;
			case 25545:
				copy(6914, config);
				config.name = "Evil Santa's wand";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(0);
				config.setSlashAttack(0);
				config.setCrushAttack(50);
				config.setMagicAttack(60);
				config.setRangeAttack(0);
				config.setStabDef(0);
				config.setSlashDef(0);
				config.setCrushDef(0);
				config.setMagicDef(0);
				config.setRangeDef(0);
				config.setStrengthBonus(0);
				config.setRangedStrBonus(0);
				config.setMagicDamage(30);
				config.setPrayerBonus(3);
				config.setLevel(Skills.MAGIC, 90);
				break;

			case 25634:
				copy(25534, config);
				config.name = "Infernal Santa's Hat";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(1);
				config.setSlashAttack(1);
				config.setCrushAttack(1);
				config.setMagicAttack(8);
				config.setRangeAttack(-15);
				config.setStabDef(70);
				config.setSlashDef(75);
				config.setCrushDef(65);
				config.setMagicDef(13);
				config.setRangeDef(-5);
				config.setStrengthBonus(7);
				config.setRangedStrBonus(0);
				config.setMagicDamage(2);
				config.setPrayerBonus(1);
				config.setLevel(Skills.DEFENCE, 80);
				break;
			case 25635:
				copy(25535, config);
				config.name = "Infernal Santa's Robe Top";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(1);
				config.setSlashAttack(1);
				config.setCrushAttack(1);
				config.setMagicAttack(38);
				config.setRangeAttack(-20);
				config.setStabDef(117);
				config.setSlashDef(111);
				config.setCrushDef(126);
				config.setMagicDef(48);
				config.setRangeDef(-15);
				config.setStrengthBonus(4);
				config.setRangedStrBonus(0);
				config.setMagicDamage(2);
				config.setPrayerBonus(1);
				config.setLevel(Skills.DEFENCE, 80);
				break;
			case 25636:
				copy(25536, config);
				config.name = "Infernal Santa's Gloves";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(12);
				config.setSlashAttack(12);
				config.setCrushAttack(0);
				config.setMagicAttack(5);
				config.setRangeAttack(-10);
				config.setStabDef(10);
				config.setSlashDef(10);
				config.setCrushDef(10);
				config.setMagicDef(10);
				config.setRangeDef(-2);
				config.setStrengthBonus(12);
				config.setRangedStrBonus(0);
				config.setMagicDamage(1);
				config.setPrayerBonus(1);
				config.setLevel(Skills.DEFENCE, 80);
				break;
			case 25637:
				copy(25537, config);
				config.name = "Infernal Santa's Robe Bottom";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(1);
				config.setSlashAttack(1);
				config.setCrushAttack(1);
				config.setMagicAttack(28);
				config.setRangeAttack(-10);
				config.setStabDef(85);
				config.setSlashDef(75);
				config.setCrushDef(79);
				config.setMagicDef(30);
				config.setRangeDef(-12);
				config.setStrengthBonus(3);
				config.setRangedStrBonus(0);
				config.setMagicDamage(2);
				config.setPrayerBonus(1);
				config.setLevel(Skills.DEFENCE, 80);
				break;
			case 25638:
				copy(25538, config);
				config.name = "Infernal Santa's Boots";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(1);
				config.setSlashAttack(1);
				config.setCrushAttack(1);
				config.setMagicAttack(5);
				config.setRangeAttack(-2);
				config.setStabDef(20);
				config.setSlashDef(21);
				config.setCrushDef(22);
				config.setMagicDef(4);
				config.setRangeDef(-2);
				config.setStrengthBonus(3);
				config.setRangedStrBonus(0);
				config.setMagicDamage(1);
				config.setPrayerBonus(1);
				config.setLevel(Skills.DEFENCE, 80);
				break;

			case 25639:
				copy(51012, config);
				config.name = "Infernal Santa's crossbow";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(0);
				config.setSlashAttack(0);
				config.setCrushAttack(0);
				config.setMagicAttack(0);
				config.setRangeAttack(95);
				config.setStabDef(0);
				config.setSlashDef(0);
				config.setCrushDef(0);
				config.setMagicDef(0);
				config.setRangeDef(0);
				config.setStrengthBonus(0);
				config.setRangedStrBonus(13);
				config.setMagicDamage(0);
				config.setPrayerBonus(3);
				config.setLevel(Skills.RANGE, 90);
				break;
			case 25640:
				copy(13902, config);
				config.name = "Infernal Santa's warhammer";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(3);
				config.setSlashAttack(3);
				config.setCrushAttack(140);
				config.setMagicAttack(0);
				config.setRangeAttack(0);
				config.setStabDef(0);
				config.setSlashDef(8);
				config.setCrushDef(10);
				config.setMagicDef(0);
				config.setRangeDef(0);
				config.setStrengthBonus(125);
				config.setRangedStrBonus(0);
				config.setMagicDamage(0);
				config.setPrayerBonus(0);
				config.setLevel(Skills.ATTACK, 80);
				config.setLevel(Skills.STRENGTH, 85);
				break;
			case 25641:
				copy(6914, config);
				config.name = "Infernal Santa's wand";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(0);
				config.setSlashAttack(0);
				config.setCrushAttack(55);
				config.setMagicAttack(70);
				config.setRangeAttack(0);
				config.setStabDef(0);
				config.setSlashDef(0);
				config.setCrushDef(0);
				config.setMagicDef(0);
				config.setRangeDef(0);
				config.setStrengthBonus(0);
				config.setRangedStrBonus(0);
				config.setMagicDamage(32);
				config.setPrayerBonus(3);
				config.setLevel(Skills.MAGIC, 90);
				break;


			case 25642:
				copy(962, config);
				config.name = "Infernal cracker";
				config.tradeable = true;
				config.value = 100000000;
				break;

			case 25644:
				copy(1038, config);
				config.name = "Lava partyhat";
				config.tradeable = true;
				config.value = 100000000;
				break;
			case 25643:
				copy(1038, config);
				config.name = "Infernal partyhat";
				config.tradeable = true;
				config.value = 100000000;
				break;
			case 27000:
				copy(405, config);
				config.name = "Small Slayer Casket";
				config.tradeable = false;
				break;
			case 27001:
				copy(405, config);
				config.name = "Medium Slayer Casket";
				config.tradeable = false;
				break;
			case 27002:
				copy(405, config);
				config.name = "Large Slayer Casket";
				config.tradeable = false;
				break;
			case 27003:
				copy(405, config);
				config.name = "Boss Slayer Casket";
				config.tradeable = false;
				break;
			case 27004:
				copy(50703, config);
				config.name = "Mystery Crate";
				config.tradeable = true;
				config.inventoryOptions[4] = "Drop";
				config.value = 20000000;
				break;
			case 27005:
				copy(68871, config);
				config.name = "Oversized Mystery Crate";
				config.tradeable = true;
				config.equipSlot = Equipment.SLOT_WEAPON;
				break;
			case 27006:
				copy(50703, config);
				config.name = "Slayer Resource Crate";
				config.tradeable = true;
				break;
			case 25645:
				copy(43453, config);
				config.name = "Infernal imp";
				config.tradeable = true;
				break;
			case 44702:
				copy(51018, config);
				config.name = "Catalyst Hat";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(0);
				config.setSlashAttack(0);
				config.setCrushAttack(0);
				config.setMagicAttack(13);
				config.setRangeAttack(-3);
				config.setStabDef(23);
				config.setSlashDef(20);
				config.setCrushDef(23);
				config.setMagicDef(10);
				config.setRangeDef(-1);
				config.setStrengthBonus(0);
				config.setRangedStrBonus(0);
				config.setMagicDamage(2);
				config.setPrayerBonus(1);
				config.setLevel(Skills.DEFENCE, 80);
				config.setLevel(Skills.MAGIC, 80);
				break;

			case 25695:
				copy(74497, config);
				config.name = "Catalyst Robe Top";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(0);
				config.setSlashAttack(0);
				config.setCrushAttack(0);
				config.setMagicAttack(50);
				config.setRangeAttack(-12);
				config.setStabDef(72);
				config.setSlashDef(57);
				config.setCrushDef(80);
				config.setMagicDef(41);
				config.setRangeDef(0);
				config.setStrengthBonus(0);
				config.setRangedStrBonus(0);
				config.setMagicDamage(5);
				config.setPrayerBonus(2);
				config.setLevel(Skills.DEFENCE, 80);
				config.setLevel(Skills.MAGIC, 80);
				break;

			case 25696:
				copy(74501, config);
				config.name = "Catalyst Robe Bottom";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(0);
				config.setSlashAttack(0);
				config.setCrushAttack(0);
				config.setMagicAttack(35);
				config.setRangeAttack(-9);
				config.setStabDef(43);
				config.setSlashDef(46);
				config.setCrushDef(46);
				config.setMagicDef(29);
				config.setRangeDef(0);
				config.setStrengthBonus(0);
				config.setRangedStrBonus(0);
				config.setMagicDamage(4);
				config.setPrayerBonus(2);
				config.setLevel(Skills.DEFENCE, 80);
				config.setLevel(Skills.MAGIC, 80);
				break;

			case 25697:
				copy(66922, config);
				config.name = "Catalyst Gloves";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(0);
				config.setSlashAttack(0);
				config.setCrushAttack(0);
				config.setMagicAttack(12);
				config.setRangeAttack(-4);
				config.setStabDef(6);
				config.setSlashDef(6);
				config.setCrushDef(6);
				config.setMagicDef(9);
				config.setRangeDef(-6);
				config.setStrengthBonus(0);
				config.setRangedStrBonus(0);
				config.setMagicDamage(5);
				config.setPrayerBonus(2);
				config.setLevel(Skills.DEFENCE, 80);
				config.setLevel(Skills.MAGIC, 80);
				break;

			case 25698:
				copy(66920, config);
				config.name = "Catalyst Boots";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(0);
				config.setSlashAttack(0);
				config.setCrushAttack(0);
				config.setMagicAttack(12);
				config.setRangeAttack(-4);
				config.setStabDef(0);
				config.setSlashDef(0);
				config.setCrushDef(0);
				config.setMagicDef(6);
				config.setRangeDef(-4);
				config.setStrengthBonus(0);
				config.setRangedStrBonus(0);
				config.setMagicDamage(2);
				config.setPrayerBonus(2);
				config.setLevel(Skills.DEFENCE, 80);
				config.setLevel(Skills.MAGIC, 80);
				break;

			case 25699:
				copy(81777, config);
				config.name = "Cataclysm";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(0);
				config.setSlashAttack(0);
				config.setCrushAttack(0);
				config.setMagicAttack(40);
				config.setRangeAttack(0);
				config.setStabDef(2);
				config.setSlashDef(3);
				config.setCrushDef(1);
				config.setMagicDef(15);
				config.setRangeDef(0);
				config.setStrengthBonus(0);
				config.setRangedStrBonus(0);
				config.setMagicDamage(35);
				config.setPrayerBonus(2);

				config.setStance(2553);
				config.setAttackSpeed(4);
				config.setAttackStyle(28);
				config.setLevel(Skills.DEFENCE, 80);
				config.setLevel(Skills.MAGIC, 80);
				break;

			case 25700:
				copy(13750, config);
				config.name = "Catalyst sigil";
				config.tradeable = true;
				break;
			case 25701:
				copy(81775, config);
				config.name = "Cataclysm orb";
				config.tradeable = true;
				break;

			case 25702:
				copy(13742, config);
				config.setLevel(Skills.DEFENCE, 75);
				config.setLevel(Skills.MAGIC, 65);
				config.setLevel(Skills.STRENGTH, 65);
				config.setLevel(Skills.PRAYER, 65);
				config.setSlashAttack(15);
				config.setStabAttack(15);
				config.setCrushAttack(15);
				config.setMagicAttack(20);
				config.setMagicDef(30);
				config.setMagicDamage(15);
				config.setStrengthBonus(3);
				config.name = "Catalyst spirit shield";
				config.value = 5000000;
				config.tradeable = true;
				break;

			case 13117:
			config.equipType = 0; //one handed
			break;
		case 18338: // gem bag
			copy(42020, config);
			break;
		case 18339: // coal bag
			copy(42019, config);
			break;
		case 1961:
			config.stackable = 1;
			break;
		case 25587:
			copy(13731, config);
			config.name = "Upgrade fragments";
		//	config.tradeable = true;
			config.inventoryOptions[0] = "Craft";
			config.inventoryOptions[4] = "Drop";
			config.stackable = 1;
			break;
		case 25586:
			copy(4565, config);
			config.name = "Easter Mystery Basket";
			config.tradeable = true;
			config.inventoryOptions[0] = "Open";
			config.inventoryOptions[4] = "Drop";
			config.stackable = 1;
			config.setStance(19998);
			break;
		case 25585:
			copy(14728, config);
			config.name = "Corrupted Carrot";
			config.value = 1000000;
			config.tradeable = true;
			config.clientScriptData = (HashMap<Integer, Object>) ItemConfig.forID(24455).clientScriptData.clone();
			config.inventoryOptions[4] = "Drop";
			break;
		case 54034: //helm
			config.clientScriptData = (HashMap<Integer, Object>) ItemConfig.forID(1163).clientScriptData.clone();
			break;
		case 54037: //body
			config.clientScriptData = (HashMap<Integer, Object>) ItemConfig.forID(1127).clientScriptData.clone();
			config.equipType = 6;
			break;
		case 54040: //legs
			config.clientScriptData = (HashMap<Integer, Object>) ItemConfig.forID(1079).clientScriptData.clone();
			break;
		case 54043: //boots
			config.clientScriptData = (HashMap<Integer, Object>) ItemConfig.forID(4131).clientScriptData.clone();
			break;
		case 54046:
			config.clientScriptData = (HashMap<Integer, Object>) ItemConfig.forID(7460).clientScriptData.clone();
			break;
		case 25584:
			copy(15241, config);
			config.name = "Corrupted Cannon";
			config.value = 1000000;
			config.tradeable = true;
			config.inventoryOptions[4] = "Drop";
			break;
		case 25583: //dawnbringer
			copy(52516, config);
			config.name = "Corrupted Eggbringer";
			config.value = 1000000;
			config.tradeable = true;
			config.setAttackStyle(28);
			config.setAttackSpeed(4);
			config.inventoryOptions[4] = "Drop";
			break;
		case 25582:
			copy(11021, config);
			config.name = "Corrupted Chicken Head";
			config.value = 1000000;
			config.tradeable = true;
			config.setStabAttack(1);
			config.setSlashAttack(1);
			config.setCrushAttack(1);
			config.setMagicAttack(10);
			config.setRangeAttack(-4);
			config.setStabDef(69);
			config.setSlashDef(57);
			config.setCrushDef(58);
			config.setMagicDef(9);
			config.setRangeDef(63);
			config.setStrengthBonus(3);
			config.setRangedStrBonus(0);
			config.setMagicDamage(2);
			config.setPrayerBonus(3);
			config.inventoryOptions[4] = "Drop";
			break;
		case 25581:
			copy(11020, config);
			config.name = "Corrupted Chicken Wings";
			config.value = 1000000;
			config.tradeable = true;
			config.setStabAttack(1);
			config.setSlashAttack(1);
			config.setCrushAttack(1);
			config.setMagicAttack(40);
			config.setRangeAttack(-12);
			config.setStabDef(117);
			config.setSlashDef(111);
			config.setCrushDef(126);
			config.setMagicDef(48);
			config.setRangeDef(159);
			config.setStrengthBonus(4);
			config.setRangedStrBonus(0);
			config.setMagicDamage(3);
			config.setPrayerBonus(3);
			config.inventoryOptions[4] = "Drop";
			break;
		case 25580:
			copy(11022, config);
			config.name = "Corrupted Chicken Legs";
			config.value = 1000000;
			config.tradeable = true;
			config.setStabAttack(1);
			config.setSlashAttack(1);
			config.setCrushAttack(1);
			config.setMagicAttack(30);
			config.setRangeAttack(-8);
			config.setStabDef(85);
			config.setSlashDef(75);
			config.setCrushDef(79);
			config.setMagicDef(30);
			config.setRangeDef(111);
			config.setStrengthBonus(2);
			config.setRangedStrBonus(0);
			config.setMagicDamage(2);
			config.setPrayerBonus(3);
			config.inventoryOptions[4] = "Drop";
			break;
		case 25579:
			copy(11019, config);
			config.name = "Corrupted Chicken Feet";
			config.value = 1000000;
			config.tradeable = true;
			config.setStabAttack(1);
			config.setSlashAttack(1);
			config.setCrushAttack(1);
			config.setMagicAttack(7);
			config.setRangeAttack(-2);
			config.setStabDef(20);
			config.setSlashDef(21);
			config.setCrushDef(22);
			config.setMagicDef(4);
			config.setRangeDef(2);
			config.setStrengthBonus(3);
			config.setRangedStrBonus(0);
			config.setMagicDamage(1);
			config.setPrayerBonus(3);
			config.tradeable = true;
			config.inventoryOptions[4] = "Drop";
			break;
		case 25578:
			copy(54419, config);
			config.name = "Templar's Great Helm";
			config.value = 1000000;
			config.tradeable = true;
			config.setStabAttack(0);
			config.setSlashAttack(0);
			config.setStrengthBonus(5);
			config.addAtt(1.3);

			config.setStabDef(60);
			config.setSlashDef(63);
			config.setCrushDef(59);
			config.setRangeDef(67);

			config.setLevel(Skills.STRENGTH, 80);
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 25577:
			copy(54420, config);
			config.name = "Templar's Hauberk";
			config.value = 1000000;
			config.tradeable = true;
			config.setStabAttack(0);
			config.setSlashAttack(0);
			config.setStrengthBonus(5);
			config.addAtt(1.3);

			config.setStabDef(132);
			config.setSlashDef(130);
			config.setCrushDef(117);
			config.setRangeDef(142);


			config.setLevel(Skills.STRENGTH, 80);
			config.setLevel(Skills.DEFENCE, 75);

			break;
		case 25576:
			copy(54421, config);
			config.name = "Templar's Plateskirt";
			config.value = 1000000;
			config.tradeable = true;

			config.setStabAttack(0);
			config.setSlashAttack(0);
			config.addAtt(1.3);
			config.addDef(1.3);// 20%

			config.setStabDef(95);
			config.setSlashDef(92);
			config.setCrushDef(93);
			config.setRangeDef(102);

			config.setLevel(Skills.STRENGTH, 80);
			config.setLevel(Skills.DEFENCE, 75);

			break;

		case 54422: ///nightmare staff
			config.setAttackSpeed(5);
			config.setStance(2553);
			config.setAttackStyle(26);
			config.setMagicAttack(16);
			config.setMagicDef(14);
			config.setMagicDamage(15);
			config.setLevel(Skills.MAGIC, 65);
			config.setLevel(Skills.HITPOINTS, 50);
			break;
		case 54423:///nightmare staff upgrade
		case 54424:
		case 54425:
			copy(54422, config);
			config.setLevel(Skills.MAGIC, 75);
			break;
		case 54417:
			config.setAttackSpeed(4);
			config.setAttackStyle(8);
			config.setStance(1426);
			config.setStabAttack(52);
			config.setSlashAttack(-4);
			config.setCrushAttack(92);
			config.setStrengthBonus(89);
			config.setPrayerBonus(2);
			config.setLevel(Skills.ATTACK, 75);
			break;
		case 54421:
			config.setStabAttack(-3);
			config.setSlashAttack(-3);
			config.setCrushAttack(12);
			config.setMagicAttack(-9);
			config.setRangeAttack(-5);
			config.setStabDef(42);
			config.setSlashDef(30);
			config.setCrushDef(49);
			config.setRangeDef(22);
			config.setStrengthBonus(2);
			config.setPrayerBonus(2);

			config.setLevel(Skills.STRENGTH, 70);
			config.setLevel(Skills.DEFENCE, 30);
			break;
		case 54420:
			config.setStabAttack(-3);
			config.setSlashAttack(-3);
			config.setCrushAttack(12);
			config.setMagicAttack(-11);
			config.setRangeAttack(-10);
			config.setStabDef(67);
			config.setSlashDef(55);
			config.setCrushDef(71);
			config.setRangeDef(35);
			config.setStrengthBonus(4);
			config.setPrayerBonus(2);

			config.setLevel(Skills.STRENGTH, 70);
			config.setLevel(Skills.DEFENCE, 30);
			break;
		case 54419:
			config.setStabAttack(-2);
			config.setSlashAttack(-2);
			config.setCrushAttack(8);
			config.setMagicAttack(-5);
			config.setRangeAttack(-5);
			config.setStabDef(19);
			config.setSlashDef(10);
			config.setCrushDef(21);
			config.setRangeDef(12);
			config.setStrengthBonus(4);
			config.setPrayerBonus(1);

			config.setLevel(Skills.STRENGTH, 70);
			config.setLevel(Skills.DEFENCE, 30);
			break;
		case 25574:
			copy(14471, config);
			config.name = "Keepsake key";
			config.inventoryOptions[4] = "Drop";
			config.tradeable = true;
			config.stackable = 1;
			config.value = 3000000;
			break;
		case 25573:
			copy(20135, config);
			config.name = "Ultimate Torva Full Helmet";
			config.value = 1000000;
			config.tradeable = true;
			config.addAtt(1.1);
			config.addDef(1.1);
			config.addHP(1.1);
			config.addLevel(5);
			break;
		case 25572:
			copy(20139, config);
			config.name = "Ultimate Torva Chestplate";
            config.value = 1000000;
            config.tradeable = true;
        	config.addAtt(1.1);
			config.addDef(1.1);
			config.addHP(1.1);
			config.addLevel(5);
			break;
		case 25571:
			copy(20139, config);
            config.name = "Ultimate Torva Chestplate";
            break;
		case 25570:
			copy(20143, config);
			config.name = "Ultimate Torva Platelegs";
			config.value = 1000000;
			config.tradeable = true;
			config.addAtt(1.1);
			config.addDef(1.1);
			config.addHP(1.1);
			config.addLevel(5);
			break;
		case 25569:
			copy(24977, config);
			config.name = "Ultimate Torva Gloves";
			config.value = 1000000;
			config.tradeable = true;
			config.addAtt(1.1);
			config.addDef(1.1);
			config.addHP(1.1);
			config.addLevel(5);
			break;
		case 25568:
			copy(24983, config);
			config.name = "Ultimate Torva Boots";
			config.value = 1000000;
			config.tradeable = true;
			config.addAtt(1.1);
			config.addDef(1.1);
			config.addHP(1.1);
			config.addLevel(5);
			break;
		case 25567:
			copy(20147, config);
			config.name = "Ultimate Pernix Cowl";
			config.value = 1000000;
			config.tradeable = true;
			config.addAtt(1.15);
			config.addDef(1.15);
			config.addHP(1.1);
			config.addLevel(5);
			break;
		case 25566:
			copy(20151, config);
			config.name = "Ultimate Pernix Body";
			config.value = 1000000;
			config.tradeable = true;
			config.addAtt(1.15);
			config.addDef(1.15);
			config.addHP(1.1);
			config.addLevel(5);
			break;
		case 25565:
			copy(20151, config);
			config.name = "Ultimate Pernix Body";
			break;
		case 25564:
			copy(20155, config);
			config.name = "Ultimate Pernix Chaps";
			config.value = 1000000;
			config.tradeable = true;
			config.addAtt(1.15);
			config.addDef(1.15);
			config.addHP(1.1);
			config.addLevel(5);
			break;
		case 25563:
			copy(24974, config);
			config.name = "Ultimate Pernix Gloves";
			config.value = 1000000;
			config.tradeable = true;
			config.addAtt(1.15);
			config.addDef(1.15);
			config.addHP(1.1);
			config.addLevel(5);
			break;
		case 25562:
			copy(24989, config);
			config.name = "Ultimate Pernix Boots";
			config.value = 1000000;
			config.tradeable = true;
			config.addAtt(1.15);
			config.addDef(1.15);
			config.addHP(1.1);
			config.addLevel(5);
			break;
		case 25561:
			copy(20159, config);
			config.name = "Ultimate Virtus Mask";
			config.value = 1000000;
			config.tradeable = true;
			config.addAtt(1.15);
			config.addDef(1.15);
			config.addHP(1.1);
			config.addLevel(5);
			break;
		case 25560:
			copy(20163, config);
			config.name = "Ultimate Virtus Robe Top";
			config.value = 1000000;
			config.tradeable = true;
			config.addAtt(1.15);
			config.addDef(1.15);
			config.addHP(1.1);
			config.addLevel(5);
			break;
		case 25559:
			copy(20163, config);
			config.name = "Ultimate Virtus Robe Top";
			break;
		case 25558:
			copy(20167, config);
			config.name = "Ultimate Virtus Robe Legs";
			config.value = 1000000;
			config.tradeable = true;
			config.addAtt(1.15);
			config.addDef(1.15);
			config.addHP(1.1);
			config.addLevel(5);
			break;
		case 25557:
			copy(24980, config);
			config.name = "Ultimate Virtus Gloves";
			config.value = 1000000;
			config.tradeable = true;
			config.addAtt(1.15);
			config.addDef(1.15);
			config.addHP(1.1);
			config.addLevel(5);
			break;
		case 25556:
			copy(24986, config);
			config.name = "Ultimate Virtus Boots";
			config.value = 1000000;
			config.tradeable = true;
			config.addAtt(1.15);
			config.addDef(1.15);
			config.addHP(1.1);
			config.addLevel(5);
			break;
		case 25555:
			config.name = "LolthenKILL pet";
			break;
		case 25590:
			config.name = "Catablepon pet";
			break;
		case 25593:
			config.name = "Dead Monk pet";
			break;
		case 25554:
			copy(23874, config);
			config.name = "Wrath of the Horde";
			config.inventoryOptions[4] = "Drop";
			config.tradeable = true;
			config.setPrayerBonus(5);
			config.setLevel(Skills.PRAYER, 60);
			break;
		case 25553:
			copy(22988, config);
			config.name = "Christmas box";
			config.tradeable = true;
			config.stackable = 1;
			config.inventoryOptions[0] = "Open";
			config.inventoryOptions[4] = "Drop";
			break;
		case 25648:
			copy(79675, config);
			config.name = "Infernal dye";
			config.tradeable = true;
			break;
		case 43329:
			config.clientScriptData = (HashMap<Integer, Object>) ItemConfig.forID(20767).clientScriptData.clone();
			break;
		case 51285:
			config.clientScriptData = (HashMap<Integer, Object>) ItemConfig.forID(51295).clientScriptData.clone();
			break;
		case 25544:
			copy(52550, config);
            config.name = "Night's End";
            config.value = 100000000;
            config.tradeable = true;
            break;
		case 19784:
			config.value *= 10;
			break;
		case 25542:
			copy(10025, config);
			config.name = "Halloween box";
			config.tradeable = true;
			config.stackable = 1;
			config.inventoryOptions[0] = "Open";
			break;

			case 25621:
				copy(79675, config);
				config.name = "Hallowed Dye";
				config.value = 100000000;
				config.tradeable = true;
				break;

			case 25616:
				copy(61055, config);
				config.name = "Hallowed H'ween Mask";
				config.value = 100000000;
				config.tradeable = true;
				break;

			case 25534:
				copy(50095, config);
				config.name = "Demonic Reaper's Mask";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(1);
				config.setSlashAttack(1);
				config.setCrushAttack(1);
				config.setMagicAttack(8);
				config.setRangeAttack(-15);
				config.setStabDef(70);
				config.setSlashDef(75);
				config.setCrushDef(65);
				config.setMagicDef(13);
				config.setRangeDef(-5);
				config.setStrengthBonus(4);
				config.setRangedStrBonus(0);
				config.setMagicDamage(2);
				config.setPrayerBonus(1);
				config.setLevel(Skills.DEFENCE, 80);
				break;
			case 25535:
				copy(50098, config);
				config.name = "Demonic Reaper's Robe Top";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(1);
				config.setSlashAttack(1);
				config.setCrushAttack(1);
				config.setMagicAttack(38);
				config.setRangeAttack(-20);
				config.setStabDef(117);
				config.setSlashDef(111);
				config.setCrushDef(126);
				config.setMagicDef(48);
				config.setRangeDef(-15);
				config.setStrengthBonus(4);
				config.setRangedStrBonus(0);
				config.setMagicDamage(2);
				config.setPrayerBonus(1);
				config.setLevel(Skills.DEFENCE, 80);
				break;
			case 25536:
				copy(50101, config);
				config.name = "Demonic Reaper's Gauntlets";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(12);
				config.setSlashAttack(12);
				config.setCrushAttack(0);
				config.setMagicAttack(5);
				config.setRangeAttack(-10);
				config.setStabDef(10);
				config.setSlashDef(10);
				config.setCrushDef(10);
				config.setMagicDef(10);
				config.setRangeDef(-2);
				config.setStrengthBonus(12);
				config.setRangedStrBonus(0);
				config.setMagicDamage(1);
				config.setPrayerBonus(1);
				config.setLevel(Skills.DEFENCE, 80);
				break;
			case 25537:
				copy(50104, config);
				config.name = "Demonic Reaper's Robe Bottom";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(1);
				config.setSlashAttack(1);
				config.setCrushAttack(1);
				config.setMagicAttack(28);
				config.setRangeAttack(-10);
				config.setStabDef(85);
				config.setSlashDef(75);
				config.setCrushDef(79);
				config.setMagicDef(30);
				config.setRangeDef(-12);
				config.setStrengthBonus(3);
				config.setRangedStrBonus(0);
				config.setMagicDamage(2);
				config.setPrayerBonus(1);
				config.setLevel(Skills.DEFENCE, 80);
				break;
			case 25538:
				copy(50107, config);
				config.name = "Demonic Reaper's Greaves";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(1);
				config.setSlashAttack(1);
				config.setCrushAttack(1);
				config.setMagicAttack(5);
				config.setRangeAttack(-2);
				config.setStabDef(20);
				config.setSlashDef(21);
				config.setCrushDef(22);
				config.setMagicDef(4);
				config.setRangeDef(-2);
				config.setStrengthBonus(3);
				config.setRangedStrBonus(0);
				config.setMagicDamage(1);
				config.setPrayerBonus(1);
				config.setLevel(Skills.DEFENCE, 80);
				break;
			case 25539:
				copy(11235, config);
				config.name = "Demonic Reaper's Fang";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(0);
				config.setSlashAttack(0);
				config.setCrushAttack(0);
				config.setMagicAttack(0);
				config.setRangeAttack(90);
				config.setStabDef(0);
				config.setSlashDef(0);
				config.setCrushDef(0);
				config.setMagicDef(0);
				config.setRangeDef(0);
				config.setStrengthBonus(0);
				config.setRangedStrBonus(80);
				config.setMagicDamage(0);
				config.setPrayerBonus(3);
				config.setAttackSpeed(config.getAttackSpeed() + 1);
				config.setLevel(Skills.RANGE, 90);
				break;
			case 25540:
				copy(52325, config);
				config.name = "Demonic Reaper's Bane";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(91);
				config.setSlashAttack(143);
				config.setCrushAttack(39);
				config.setMagicAttack(-6);
				config.setRangeAttack(0);
				config.setStabDef(0);
				config.setSlashDef(8);
				config.setCrushDef(10);
				config.setMagicDef(0);
				config.setRangeDef(0);
				config.setStrengthBonus(85);
				config.setRangedStrBonus(0);
				config.setMagicDamage(0);
				config.setPrayerBonus(0);
				config.setLevel(Skills.ATTACK, 80);
				config.setLevel(Skills.STRENGTH, 85);
				break;
			case 25541:
				copy(42902, config);
				config.name = "Demonic Staff";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(70);
				config.setSlashAttack(70);
				config.setCrushAttack(70);
				config.setMagicAttack(50);
				config.setRangeAttack(0);
				config.setStabDef(0);
				config.setSlashDef(3);
				config.setCrushDef(3);
				config.setMagicDef(20);
				config.setRangeDef(0);
				config.setStrengthBonus(0);
				config.setRangedStrBonus(0);
				config.setMagicDamage(30);
				config.setPrayerBonus(3);
				config.setLevel(Skills.MAGIC, 90);
				break;

			case 25611:
				copy(25534, config);
				config.name = "Hallowed Mask";
				config.value = 100000000;
				config.tradeable = true;
				config.addHP(1.05);
				config.setStabAttack(1);
				config.setSlashAttack(1);
				config.setCrushAttack(1);
				config.setMagicAttack(8);
				config.setRangeAttack(-15);
				config.setStabDef(70);
				config.setSlashDef(75);
				config.setCrushDef(65);
				config.setMagicDef(13);
				config.setRangeDef(-5);
				config.setStrengthBonus(4);
				config.setRangedStrBonus(0);
				config.setMagicDamage(2);
				config.setPrayerBonus(1);
				config.addHP(1.05);
				config.setLevel(Skills.DEFENCE, 80);
				break;
			case 25612:
				copy(25535, config);
				config.name = "Hallowed Robe Top";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(1);
				config.setSlashAttack(1);
				config.setCrushAttack(1);
				config.setMagicAttack(38);
				config.setRangeAttack(-20);
				config.setStabDef(117);
				config.setSlashDef(111);
				config.setCrushDef(126);
				config.setMagicDef(48);
				config.setRangeDef(-15);
				config.setStrengthBonus(4);
				config.setRangedStrBonus(0);
				config.setMagicDamage(2);
				config.setPrayerBonus(1);
				config.addHP(1.05);
				config.setLevel(Skills.DEFENCE, 80);
				break;
			case 25613:
				copy(25536, config);
				config.name = "Hallowed Gauntlets";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(12);
				config.setSlashAttack(12);
				config.setCrushAttack(0);
				config.setMagicAttack(5);
				config.setRangeAttack(-10);
				config.setStabDef(10);
				config.setSlashDef(10);
				config.setCrushDef(10);
				config.setMagicDef(10);
				config.setRangeDef(-2);
				config.setStrengthBonus(12);
				config.setRangedStrBonus(0);
				config.setMagicDamage(1);
				config.setPrayerBonus(1);
				config.addHP(1.05);
				config.setLevel(Skills.DEFENCE, 80);
				break;
			case 25614:
				copy(25537, config);
				config.name = "Hallowed Robe Bottom";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(1);
				config.setSlashAttack(1);
				config.setCrushAttack(1);
				config.setMagicAttack(28);
				config.setRangeAttack(-10);
				config.setStabDef(85);
				config.setSlashDef(75);
				config.setCrushDef(79);
				config.setMagicDef(30);
				config.setRangeDef(-12);
				config.setStrengthBonus(3);
				config.setRangedStrBonus(0);
				config.setMagicDamage(2);
				config.setPrayerBonus(1);
				config.addHP(1.05);
				config.setLevel(Skills.DEFENCE, 80);
				break;
			case 25615:
				copy(25538, config);
				config.name = "Hallowed Greaves";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(1);
				config.setSlashAttack(1);
				config.setCrushAttack(1);
				config.setMagicAttack(5);
				config.setRangeAttack(-2);
				config.setStabDef(20);
				config.setSlashDef(21);
				config.setCrushDef(22);
				config.setMagicDef(4);
				config.setRangeDef(-2);
				config.setStrengthBonus(3);
				config.setRangedStrBonus(0);
				config.setMagicDamage(1);
				config.setPrayerBonus(1);
				config.addHP(1.05);
				config.setLevel(Skills.DEFENCE, 80);
				break;

			case 25617:
				copy(25539, config);
				config.name = "Hallowed Fang";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(0);
				config.setSlashAttack(0);
				config.setCrushAttack(0);
				config.setMagicAttack(0);
				config.setRangeAttack(110);
				config.setStabDef(0);
				config.setSlashDef(0);
				config.setCrushDef(0);
				config.setMagicDef(0);
				config.setRangeDef(0);
				config.setStrengthBonus(0);
				config.setRangedStrBonus(100);
				config.setMagicDamage(0);
				config.setPrayerBonus(3);
				config.setLevel(Skills.RANGE, 90);
				break;
			case 25618:
				copy(52325, config);
				config.name = "Hallowed Bane";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(91);
				config.setSlashAttack(155);
				config.setCrushAttack(39);
				config.setMagicAttack(-6);
				config.setRangeAttack(0);
				config.setStabDef(0);
				config.setSlashDef(8);
				config.setCrushDef(10);
				config.setMagicDef(0);
				config.setRangeDef(0);
				config.setStrengthBonus(85);
				config.setRangedStrBonus(0);
				config.setMagicDamage(0);
				config.setPrayerBonus(0);
				config.setLevel(Skills.ATTACK, 80);
				config.setLevel(Skills.STRENGTH, 85);
				break;
			case 25620:
				copy(54423, config);
				config.name = "Hallowed Staff";
				config.value = 100000000;
				config.tradeable = true;
				config.setAttackSpeed(config.getAttackSpeed()-1);
				config.setStabAttack(70);
				config.setSlashAttack(70);
				config.setCrushAttack(70);
				config.setMagicAttack(60);
				config.setRangeAttack(0);
				config.setStabDef(0);
				config.setSlashDef(3);
				config.setCrushDef(3);
				config.setMagicDef(20);
				config.setRangeDef(0);
				config.setStrengthBonus(0);
				config.setRangedStrBonus(0);
				config.setMagicDamage(32);
				config.setPrayerBonus(3);
				config.setLevel(Skills.MAGIC, 90);
				break;

			case 25619:
				copy(74484, config);
				config.name = "Hallowed Claws";
				config.value = 100000000;
				config.tradeable = true;
				config.setStabAttack(50);
				config.setSlashAttack(75);
				config.setCrushAttack(0);
				config.setMagicAttack(0);
				config.setRangeAttack(0);
				config.setStabDef(26);
				config.setSlashDef(52);
				config.setCrushDef(14);
				config.setMagicDef(0);
				config.setRangeDef(0);
				config.setStrengthBonus(75);
				config.setRangedStrBonus(0);
				config.setMagicDamage(0);
				config.setPrayerBonus(3);
				config.setLevel(Skills.ATTACK, 60);
				break;
			case 25591:
            copy(52325, config);
            config.name = "Scythe of Shalit";
            config.value = 100000000;
        	config.setStance(2553);
			config.setAttackSpeed(4);
			config.setLevel(Skills.ATTACK, 80);
			config.setLevel(Skills.STRENGTH, 80);
			config.setStabAttack(91);
			config.setSlashAttack(143);
			config.setCrushAttack(39);
			config.setMagicAttack(-6);
			config.setSlashDef(2);
			config.setSlashDef(8);
			config.setCrushDef(10);
			config.setStrengthBonus(80);
			config.tradeable = true;
            break;
		case 25592:
            copy(50997, config);
            config.name = "Apologyse's Toxic Rain";
			config.setStance(2588);
			config.setAttackSpeed(6);
			config.setRangeAttack(91);
			config.setRangedStrBonus(26);
			config.setPrayerBonus(4);
			config.setStabDef(30);
			config.setSlashDef(29);
			config.setCrushDef(28);
			config.setLevel(Skills.RANGE, 80);
            break;
		case 25609:
			copy(50997, config);
			config.name = "Psykotix Venom Rain";
			config.setStance(2588);
			config.setAttackSpeed(6);
			config.setRangeAttack(91);
			config.setRangedStrBonus(26);
			config.setPrayerBonus(4);
			config.setStabDef(30);
			config.setSlashDef(29);
			config.setCrushDef(28);
			config.setLevel(Skills.RANGE, 80);
			break;
		case 989:
		case 53083:
		case 53951:
			config.inventoryOptions[0] = "Teleport";
			break;
		case 562:
		case 560:
		case 565:
		case 566:
		case 561:
		case 563:
		case 564:
		case 9075:
		case 888:
		case 890:
		case 892:
		case 11212:
		case 9244:
		case 9245:
			config.value /= 3;
			break;
		case 54271:
			config.setStabDef(36);
			config.setSlashDef(34);
			config.setCrushDef(38);
			config.setMagicDef(3);
			config.setRangeDef(34);
			config.setStrengthBonus(6);
			config.setPrayerBonus(3);
			config.setLevel(Skills.DEFENCE, 70);
			break;
		case 25543:
			copy(9920, config);
			config.setStabDef(36);
			config.setSlashDef(34);
			config.setCrushDef(38);
			config.setMagicDef(3);
			config.setRangeDef(34);
			config.setStrengthBonus(6);
			config.setPrayerBonus(4);
			config.name = "Flaming Jack-O-Lantern";
			break;
		case 25532:
			config.name = "Slave pet";
			break;
		case 25600:
			config.name = "Covid";
			break;
		case 25601:
			config.name = "Pet fairy queen";
			break;
		case 25610:
			config.name = "Kai's shadow critter";
			break;
		case 25602:
			config.name = "Lil'smokey";
			break;
		case 25603:
		case 25604:
		case 25605:
			config.name = "Mini callus";
			break;
		case 25606:
			config.name = "Baby lava dragon";
			break;
		case 25607:
			config.name = "Mimi";
			break;
		case 25608:
			config.name = "Superior slayer pet";
			break;
		case 25623:
			config.name = "Death Jr.";
			config.inventoryOptions[4] = "Drop";
			break;
		case 25625:
			config.name = "October 2020 top donator pet (Lucky)";
			config.inventoryOptions[4] = "Drop";
			break;
		case 25626:
			config.name = "October 2020 top donator pet (Bigzy)";
			config.inventoryOptions[4] = "Drop";
			break;
		case 25624:
			copy(4587, config);
			config.name = "Zio's whaler.";
			config.setAttackSpeed(1);
			config.inventoryOptions[4] = "Drop";
			break;
		case 25575: //Versace's Rage
			copy(50997, config);
			config.name = "Versace's Rage";
			config.setStance(2588);
			config.setAttackSpeed(6);
			config.setRangeAttack(91);
			config.setRangedStrBonus(26);
			config.setPrayerBonus(4);
			config.setStabDef(30);
			config.setSlashDef(29);
			config.setCrushDef(28);
			config.setLevel(Skills.RANGE, 80);
			break;
		case 25672:
			copy(7462, config);
			config.setStabAttack(16);
			config.setSlashAttack(16);
			config.setCrushAttack(16);
			config.setMagicAttack(10);
			config.setRangeAttack(19);
			config.setStabDef(19);
			config.setSlashDef(17);
			config.setCrushDef(17);
			config.setMagicDef(9);
			config.setRangeDef(15);
			config.setStrengthBonus(15);
			config.setMagicDamage(5);
			config.setRangedStrBonus(1);
			config.setPrayerBonus(2);
			config.name = "Gloves of the gods";
			config.setLevel(Skills.MAGIC, 90);
			config.setLevel(Skills.RANGE, 90);
			config.setLevel(Skills.DEFENCE, 90);
			config.setHP(155);
			config.tradeable = true;
			break;
		case 25671:
		case 25670:
		case 25659:
			copy(43239, config);
			config.setStabAttack(10);
			config.setSlashAttack(10);
			config.setCrushAttack(10);
			config.setMagicAttack(12);
			config.setRangeAttack(15);
			config.setStabDef(35);
			config.setSlashDef(35);
			config.setCrushDef(35);
			config.setMagicDef(20);
			config.setRangeDef(35);
			config.setStrengthBonus(10);
			config.setMagicDamage(5);
			config.setRangedStrBonus(5);
			config.name = "Boots of the gods";
			config.setLevel(Skills.MAGIC, 90);
			config.setLevel(Skills.RANGE, 90);
			config.setLevel(Skills.DEFENCE, 90);
			config.tradeable = true;
			break;
		case 25664:
			copy(79675, config);
			config.name = "Easter Mk. II dye";
			config.tradeable = true;
			break;
		case 25662: // easter mk.2
			copy(50997, config);
			config.name = "Twisted bow Mk. II";
			config.setStance(2588);
			config.setAttackSpeed(6);
			config.setRangeAttack(70);
			config.setRangedStrBonus(20);
			config.setPrayerBonus(4);
			config.setLevel(Skills.RANGE, 80);
			config.tradeable = true;
			config.inventoryOptions[2] = "Revert";
			break;
		case 25533:
			copy(50997, config);
			config.name = "Twisted bow Mk. II";
			config.setStance(2588);
			config.setAttackSpeed(6);
			config.setRangeAttack(70);
			config.setRangedStrBonus(20);
			config.setPrayerBonus(4);
			config.setLevel(Skills.RANGE, 80);
			config.tradeable = true;
			break;
		case 25531:
			copy(20068, config);
			config.name = "Ava's blessing";
			config.setLevel(Skills.RANGE, 85);
			config.setRangedStrBonus(5);
			config.setRangeAttack(12);
			break;
		case 777:
			config.setLevel(Skills.DEFENCE, 1);
			break;
		case 25530: // tbow pet
			copy(50997, config);
			config.name = "Twisted bow pet";
			config.inventoryOptions = new String[config.inventoryOptions.length];
			config.setDefaultOptions();
			config.maleEquip1 = config.femaleEquip1 = -1;
			config.equipSlot = -1;
			config.equipType = -1;
			break;
		case 25663:
			copy(ItemConfig.forID(16955), config);
			config.name = "Legendary lightning rapier (u)";
			config.inventoryOptions[2] = "";
			config.setData(14, 2);
			config.setStrengthBonus(95);
			config.setStabAttack(90);
			config.setLevel(Skills.ATTACK, 85);
			config.tradeable = true;
			config.inventoryOptions[2] = "Revert";
			break;
		case 25529:
			copy(ItemConfig.forID(16955), config);
			config.name = "Legendary lightning rapier (u)";
			config.inventoryOptions[2] = "";
			config.setData(14, 2);
			config.setStrengthBonus(95);
			config.setStabAttack(90);
			config.setLevel(Skills.ATTACK, 85);
			config.tradeable = true;
			break;
		case 25588://nightmare mace
			copy(54417, config);
            config.name = "Templar's mace";
            config.value = 1000000;
            config.setData(14, 3);
			config.setStrengthBonus(95);
			config.setStabAttack(90);
			config.setLevel(Skills.ATTACK, 85);
            config.tradeable = true;
            break;
		case 25589://nightmare mace (u)
			copy(25588, config);
            config.name = "Templar's mace (u)";
            config.setData(14, 2);
            config.setLevel(Skills.ATTACK, 85);
            config.tradeable = true;
            break;
		case 25528:
			copy(20771, config);
			config.name = "Elite completionist cape";
			config.setRangedStrBonus(5);
			config.setRangeAttack(12);
			break;
		case 25527:
			copy(11704, config);
			config.name = "Almighty hilt";
			config.tradeable = true;
			break;
		case 25526:
			copy(11696, config);
			config.name = "Almighty godsword";
			config.value = 1000000;
			config.tradeable = true;
			config.addAtt(1.2);
			config.setStrengthBonus(158);
			config.setLevel(Skills.ATTACK, 85);
			break;
		case 20139:
		case 20141:
			config.setStrengthBonus(5);
			break;
		case 25505:
			copy(11724, config);
			config.name = "Ultimate bandos chestplate";
			config.value = 1000000;
			config.tradeable = true;
			config.setHP(100);
			config.addAtt(1.2);
			config.addDef(1.2);// 20%
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 25506:
			copy(11726, config);
			config.name = "Ultimate bandos tassets";
			config.value = 1000000;
			config.tradeable = true;
			config.setHP(67);
			config.addAtt(1.2);
			config.addDef(1.2);// 20%
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 25507:
			copy(25022, config);
			config.name = "Ultimate bandos helmet";
			config.value = 1000000;
			config.tradeable = true;
			config.setHP(33);
			config.addDef(1.2);// 20%
			config.addAtt(1.2);
			config.setStrengthBonus(3);
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 25508:
			copy(25025, config);
			config.name = "Ultimate bandos gloves";
			config.value = 1000000;
			config.tradeable = true;
			config.setHP(12);
			config.addAtt(1.2);
			config.addDef(1.2);// 20%
			// config.setStrengthBonus(3);
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 25509:
			copy(11728, config);
			config.name = "Ultimate bandos boots";
			config.value = 1000000;
			config.tradeable = true;
			config.setHP(17);
			config.addAtt(1.2);
			config.addDef(1.2);// 20%
			config.setStrengthBonus(3);
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 25510:
			copy(25019, config);
			config.name = "Ultimate bandos warshield";
			config.value = 1000000;
			config.tradeable = true;
			config.setHP(16);
			config.addDef(1.2);// 20%
			config.setStrengthBonus(6);
			config.setLevel(Skills.DEFENCE, 75);
			break;
			case 25742:
				copy(11724, config);
				config.name = "Ultimate bandos chestplate (i)";
				config.value = 1000000;
				config.tradeable = true;
				config.setHP(100);
				config.addAtt(1.2);
				config.addDef(1.2);// 20%
				config.setLevel(Skills.DEFENCE, 75);
				break;
			case 25743:
				copy(11726, config);
				config.name = "Ultimate bandos tassets (i)";
				config.value = 1000000;
				config.tradeable = true;
				config.setHP(67);
				config.addAtt(1.2);
				config.addDef(1.2);// 20%
				config.setLevel(Skills.DEFENCE, 75);
				break;
			case 25744:
				copy(25022, config);
				config.name = "Ultimate bandos helmet (i)";
				config.value = 1000000;
				config.tradeable = true;
				config.setHP(33);
				config.addDef(1.2);// 20%
				config.addAtt(1.2);
				config.setStrengthBonus(3);
				config.setLevel(Skills.DEFENCE, 75);
				break;
			case 25745:
				copy(25025, config);
				config.name = "Ultimate bandos gloves (i)";
				config.value = 1000000;
				config.tradeable = true;
				config.setHP(12);
				config.addAtt(1.2);
				config.addDef(1.2);// 20%
				// config.setStrengthBonus(3);
				config.setLevel(Skills.DEFENCE, 75);
				break;
			case 25746:
				copy(11728, config);
				config.name = "Ultimate bandos boots (i)";
				config.value = 1000000;
				config.tradeable = true;
				config.setHP(17);
				config.addAtt(1.2);
				config.addDef(1.2);// 20%
				config.setStrengthBonus(3);
				config.setLevel(Skills.DEFENCE, 75);
				break;
			case 25747:
				copy(25019, config);
				config.name = "Ultimate bandos warshield (i)";
				config.value = 1000000;
				config.tradeable = true;
				config.setHP(16);
				config.addDef(1.2);// 20%
				config.setStrengthBonus(6);
				config.setLevel(Skills.DEFENCE, 75);
				break;
		case 25511:
			copy(11718, config);
			config.name = "Ultimate armadyl helmet";
			config.value = 1000000;
			config.tradeable = true;
			config.setHP(33);
			config.addAtt(1.2); // 20%
			config.addDef(1.2);// 20%
			config.setLevel(Skills.DEFENCE, 75);

			break;
		case 25512:
			copy(11720, config);
			config.name = "Ultimate armadyl chestplate";
			config.value = 1000000;
			config.tradeable = true;
			config.setHP(100);
			config.addAtt(1.2); // 20%
			config.addDef(1.2);// 20%
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 25513:
			copy(11722, config);
			config.name = "Ultimate armadyl chainskirt";
			config.value = 1000000;
			config.tradeable = true;
			config.setHP(67);
			config.addAtt(1.2); // 20%
			config.addDef(1.2);// 20%
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 25514:
			copy(25016, config);
			config.name = "Ultimate armadyl gloves";
			config.value = 1000000;
			config.tradeable = true;
			config.setHP(12);
			config.addAtt(1.2); // 20%
			config.addDef(1.2);// 20%
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 25515:
			copy(25010, config);
			config.name = "Ultimate armadyl boots";
			config.value = 1000000;
			config.tradeable = true;
			config.setHP(17);
			config.addAtt(1.2); // 20%
			config.addDef(1.2);// 20%
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 25516:
			copy(25013, config);
			config.name = "Ultimate armadyl buckler";
			config.value = 1000000;
			config.tradeable = true;
			config.setHP(16);
			config.addAtt(1.2); // 20%
			config.addDef(1.2);// 20%
			config.setLevel(Skills.DEFENCE, 75);
			break;
			case 25748:
				copy(11718, config);
				config.name = "Ultimate armadyl helmet (i)";
				config.value = 1000000;
				config.tradeable = true;
				config.setHP(33);
				config.addAtt(1.2); // 20%
				config.addDef(1.2);// 20%
				config.setLevel(Skills.DEFENCE, 75);

				break;
			case 25749:
				copy(11720, config);
				config.name = "Ultimate armadyl chestplate (i)";
				config.value = 1000000;
				config.tradeable = true;
				config.setHP(100);
				config.addAtt(1.2); // 20%
				config.addDef(1.2);// 20%
				config.setLevel(Skills.DEFENCE, 75);
				break;
			case 25750:
				copy(11722, config);
				config.name = "Ultimate armadyl chainskirt (i)";
				config.value = 1000000;
				config.tradeable = true;
				config.setHP(67);
				config.addAtt(1.2); // 20%
				config.addDef(1.2);// 20%
				config.setLevel(Skills.DEFENCE, 75);
				break;
			case 25751:
				copy(25016, config);
				config.name = "Ultimate armadyl gloves (i)";
				config.value = 1000000;
				config.tradeable = true;
				config.setHP(12);
				config.addAtt(1.2); // 20%
				config.addDef(1.2);// 20%
				config.setLevel(Skills.DEFENCE, 75);
				break;
			case 25752:
				copy(25010, config);
				config.name = "Ultimate armadyl boots (i)";
				config.value = 1000000;
				config.tradeable = true;
				config.setHP(17);
				config.addAtt(1.2); // 20%
				config.addDef(1.2);// 20%
				config.setLevel(Skills.DEFENCE, 75);
				break;
			case 25753:
				copy(25013, config);
				config.name = "Ultimate armadyl buckler (i)";
				config.value = 1000000;
				config.tradeable = true;
				config.setHP(16);
				config.addAtt(1.2); // 20%
				config.addDef(1.2);// 20%
				config.setLevel(Skills.DEFENCE, 75);
				break;
		case 25517:
			copy(24992, config);
			config.name = "Ultimate hood of subjugation";
			config.value = 1000000;
			config.tradeable = true;
			config.setHP(33);
			config.addAtt(1.2); // 20%
			config.addDef(1.2);// 20%
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 25518:
			copy(24995, config);
			config.name = "Ultimate garb of subjugation";
			config.value = 1000000;
			config.tradeable = true;
			config.setHP(100);
			config.addAtt(1.2); // 20%
			config.addDef(1.2);// 20%
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 25519:
			copy(24998, config);
			config.name = "Ultimate gown of subjugation";
			config.value = 1000000;
			config.tradeable = true;
			config.setHP(33);
			config.addAtt(1.2); // 20%
			config.addDef(1.2);// 20%
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 25520:
			copy(25001, config);
			config.name = "Ultimate ward of subjugation";
			config.value = 1000000;
			config.tradeable = true;
			config.setHP(16);
			config.addAtt(1.2); // 20%
			config.addDef(1.2);// 20%
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 25521:
			copy(25004, config);
			config.name = "Ultimate boots of subjugation";
			config.value = 1000000;
			config.tradeable = true;
			config.setHP(17);
			config.addAtt(1.2); // 20%
			config.addDef(1.2);// 20%
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 25522:
			copy(25007, config);
			config.name = "Ultimate gloves of subjugation";
			config.value = 1000000;
			config.tradeable = true;
			config.setHP(12);
			config.addAtt(1.2); // 20%
			config.addDef(1.2);// 20%
			config.setLevel(Skills.DEFENCE, 75);
			break;
			case 25754:
				copy(24992, config);
				config.name = "Ultimate hood of subjugation (i)";
				config.value = 1000000;
				config.tradeable = true;
				config.setHP(33);
				config.addAtt(1.2); // 20%
				config.addDef(1.2);// 20%
				config.setLevel(Skills.DEFENCE, 75);
				break;
			case 25755:
				copy(24995, config);
				config.name = "Ultimate garb of subjugation (i)";
				config.value = 1000000;
				config.tradeable = true;
				config.setHP(100);
				config.addAtt(1.2); // 20%
				config.addDef(1.2);// 20%
				config.setLevel(Skills.DEFENCE, 75);
				break;
			case 25756:
				copy(24998, config);
				config.name = "Ultimate gown of subjugation (i)";
				config.value = 1000000;
				config.tradeable = true;
				config.setHP(33);
				config.addAtt(1.2); // 20%
				config.addDef(1.2);// 20%
				config.setLevel(Skills.DEFENCE, 75);
				break;
			case 25757:
				copy(25001, config);
				config.name = "Ultimate ward of subjugation (i)";
				config.value = 1000000;
				config.tradeable = true;
				config.setHP(16);
				config.addAtt(1.2); // 20%
				config.addDef(1.2);// 20%
				config.setLevel(Skills.DEFENCE, 75);
				break;
			case 25758:
				copy(25004, config);
				config.name = "Ultimate boots of subjugation (i)";
				config.value = 1000000;
				config.tradeable = true;
				config.setHP(17);
				config.addAtt(1.2); // 20%
				config.addDef(1.2);// 20%
				config.setLevel(Skills.DEFENCE, 75);
				break;
			case 25759:
				copy(25007, config);
				config.name = "Ultimate gloves of subjugation (i)";
				config.value = 1000000;
				config.tradeable = true;
				config.setHP(12);
				config.addAtt(1.2); // 20%
				config.addDef(1.2);// 20%
				config.setLevel(Skills.DEFENCE, 75);
				break;
		case 25523:
			copy(1603, config);
			config.inventoryOptions[0] = "Teleport";
			config.name = "Upgrade gem";
			config.tradeable = true;
			break;
		case 25524:
			copy(11724, config);
			config.name = "Ultimate bandos chestplate";
			break;
		case 25525:
			copy(11720, config);
			config.name = "Ultimate armadyl chestplate";
			break;
		case 20147:
		case 20149:
		case 20155:
		case 20157:
		case 24975:
		case 24990:
			config.setRangedStrBonus(1);
			break;
		case 24974:
		case 24989:
			config.setRangedStrBonus(1);
			config.tradeable = true;
			break;
		case 20151:
		case 20153:
			config.setRangedStrBonus(2);
			break;
		case 20159:
		case 20161:
		case 24981:
		case 24987:
			config.setMagicDamage(1);
			break;
		case 24980:
		case 24986:
			config.setMagicDamage(1);
			config.tradeable = true;
			break;
		case 24977:
		case 24983:
			config.tradeable = true;
			break;
		case 20163:
		case 20165:
			config.setMagicDamage(3);
			break;
		case 20167:
		case 20169:
			config.setMagicDamage(2);
			break;
		case 52817:
			config.setStance(ItemConfig.forID(10024).getRenderAnimId());
			break;
		case 3385 + Settings._685_ITEM_OFFSET:
			config.equipType = -1;
			break;
		case 20135:
		case 20137:
			config.setStrengthBonus(5); // torva full helm
			break;
		case 53673:// c axe
			config.setStance(1426);
			config.setLevel(Skills.ATTACK, 70);
			config.setLevel(Skills.AGILITY, 50);
			config.setStabAttack(-2);
			config.setSlashAttack(38);
			config.setCrushAttack(32);
			config.setSlashDef(1);
			config.setStrengthBonus(42);
			config.inventoryOptions[3] = "Add-to-toolbelt";
			break;
		case 53680:// c pickaxe
			config.setStance(1426);
			config.setLevel(Skills.ATTACK, 70);
			config.setLevel(Skills.AGILITY, 50);
			config.setStabAttack(38);
			config.setSlashAttack(-2);
			config.setCrushAttack(32);
			config.setSlashDef(1);
			config.setStrengthBonus(42);
			config.inventoryOptions[3] = "Add-to-toolbelt";
			break;
		case 53762: // c harpoon
			config.setStance(1426);
			config.setLevel(Skills.ATTACK, 70);
			config.setLevel(Skills.AGILITY, 50);
			config.setStabAttack(38);
			config.setSlashAttack(32);
			config.setSlashDef(1);
			config.setStrengthBonus(42);
			config.setAttackStyle(5);
			break;
		case 53995:
			config.setLevel(Skills.ATTACK, 75);
			config.setAttackSpeed(4);
			config.setStabAttack(55);
			config.setSlashAttack(94);
			config.setStrengthBonus(89);
			config.setAttackStyle(6);
			config.tradeable = true;
			break;
		case 53971: // crystal helm
			config.setLevel(Skills.DEFENCE, 70);
			config.setMagicAttack(-10);
			config.setRangeAttack(8);
			config.setStabDef(12);
			config.setSlashDef(8);
			config.setCrushDef(14);
			config.setMagicDef(26);
			config.setRangeDef(18);
			config.setPrayerBonus(2);
			break;
		case 53975: // crystal body
			config.setLevel(Skills.DEFENCE, 70);
			config.equipType = 6;
			config.setMagicAttack(-18);
			config.setRangeAttack(30);
			config.setStabDef(34);
			config.setSlashDef(24);
			config.setCrushDef(32);
			config.setMagicDef(44);
			config.setRangeDef(53);
			config.setPrayerBonus(3);
			break;
		case 53979: // crystal legs
			config.setLevel(Skills.DEFENCE, 70);
			config.setMagicAttack(-12);
			config.setRangeAttack(17);
			config.setStabDef(20);
			config.setSlashDef(16);
			config.setCrushDef(24);
			config.setMagicDef(34);
			config.setRangeDef(29);
			config.setPrayerBonus(2);
			break;
		case 14684:
			config.setLevel(Skills.RANGE, 48);
			config.tradeable = true;
			config.inventoryOptions[4] = "Drop";
			return;
		case 52975:
			config.setStabAttack(4);
			config.setSlashAttack(4);
			config.setCrushAttack(4);
			config.setMagicAttack(6);
			config.setRangeAttack(4);
			config.setStabDef(4);
			config.setSlashDef(4);
			config.setCrushDef(4);
			config.setMagicDef(6);
			config.setRangeDef(4);
			config.setStrengthBonus(4);
			break;
		case 52954:
			config.setPrayerBonus(5);
			config.setLevel(Skills.PRAYER, 60);
			break;
		case 52951:
			config.setStabAttack(3);
			config.setMagicAttack(3);
			config.setRangeAttack(5);
			config.setStabDef(10);
			config.setSlashDef(10);
			config.setCrushDef(10);
			config.setMagicDef(5);
			config.setRangeDef(5);
			config.setLevel(Skills.SLAYER, 44);
			config.setLevel(Skills.DEFENCE, 70);
			config.setLevel(Skills.MAGIC, 70);
			config.setLevel(Skills.RANGE, 70);
			break;
		case 52981:
			config.setStabAttack(16);
			config.setSlashAttack(16);
			config.setCrushAttack(16);
			config.setMagicAttack(-16);
			config.setRangeAttack(-16);
			config.setStrengthBonus(14);
			config.setLevel(Skills.ATTACK, 80);
			config.setLevel(Skills.DEFENCE, 80);
			config.tradeable = true;
			break;
		case 53037:
			config.setStabDef(1);
			config.setSlashDef(1);
			config.setCrushDef(1);
			config.setLevel(Skills.SLAYER, 44);
			break;
		case 22985:
			copy(6914, config);
			break;
		case 1038:
		case 1040:
		case 1042:
		case 1044:
		case 1046:
		case 1048:
			config.value = 150000000;
			break;

		case 19785:
		case 19787:
		case 19789:
			config.value = ItemConfig.forID(8839).value;
			break;
		case 19786:
		case 19788:
		case 19790:
			config.value = ItemConfig.forID(8840).value;
			break;
		case 23679:
			config.value = ItemConfig.forID(11694).value;
			break;
		case 23680:
			config.value = ItemConfig.forID(11696).value;
			break;
		case 23681:
			config.value = ItemConfig.forID(11698).value;
			break;
		case 23682:
			config.value = ItemConfig.forID(11700).value;
			break;
		case 23683:
			config.value = ItemConfig.forID(11716).value;
			break;
		case 23684:
			config.value = ItemConfig.forID(11718).value;
			break;
		case 23685:
			config.value = ItemConfig.forID(11720).value;
			break;
		case 23686:
			config.value = ItemConfig.forID(11722).value;
			break;
		case 23687:
			config.value = ItemConfig.forID(11724).value;
			break;
		case 23688:
			config.value = ItemConfig.forID(11726).value;
			break;
		case 23689:
			config.value = ItemConfig.forID(11728).value;
			break;
		case 23690:
			config.value = ItemConfig.forID(11730).value;
			break;
		case 23691:
			config.value = ItemConfig.forID(4151).value;
			break;
		case 23692:
			config.value = ItemConfig.forID(11335).value;
			break;
		case 23693:
			config.value = ItemConfig.forID(14479).value;
			break;
		case 23694:
			config.value = ItemConfig.forID(3140).value;
			break;
		case 23695:
			config.value = ItemConfig.forID(14484).value;
			break;
		case 23696:
			config.value = ItemConfig.forID(7158).value;
			break;
		case 23697:
			config.value = ItemConfig.forID(13738).value;
			break;
		case 23698:
			config.value = ItemConfig.forID(13740).value;
			break;
		case 23699:
			config.value = ItemConfig.forID(13742).value;
			break;
		case 23700:
			config.value = ItemConfig.forID(13744).value;
			break;
		case 52555:
			config.setStance(2553);
			config.setAttackSpeed(5);
			config.setMagicAttack(20);
			config.setMagicDef(20);
			config.setLevel(Skills.MAGIC, 60);
			config.setAttackStyle(3);
			config.tradeable = true;
			break;
		case 52550:
			config.setStance(2588);
			config.setAttackSpeed(4);
			config.setRangeAttack(75);
			config.setRangedStrBonus(60);
			config.setLevel(Skills.RANGE, 60);
			config.tradeable = true;
			config.setAttackStyle(17);
			break;
		case 52545:
			config.setStance(373);
			config.setAttackSpeed(4);
			config.setStabAttack(53);
			config.setSlashAttack(-2);
			config.setCrushAttack(67);
			config.setSlashDef(1);
			config.setStrengthBonus(66);
			config.setPrayerBonus(2);
			config.setLevel(Skills.ATTACK, 60);
			config.setAttackStyle(8);
			config.tradeable = true;
			break;
		case 52557:
			config.setStabAttack(10);
			config.setSlashAttack(10);
			config.setCrushAttack(10);
			config.setMagicAttack(10);
			config.setRangeAttack(10);
			config.setStabDef(3);
			config.setSlashDef(3);
			config.setCrushDef(3);
			config.setMagicDef(3);
			config.setRangeDef(3);
			config.setStrengthBonus(6);
			config.setPrayerBonus(3);
			break;
		case 19613:
		case 22422:
			config.setStrengthBonus(2);
			config.tradeable = true;
			break;
		case 19615:
		case 22421:
			config.setRangeAttack(10);
			config.tradeable = true;
			break;
		case 19617:
		case 22420:
			config.setMagicAttack(10);
			config.tradeable = true;
			break;
		case 52326: // justiciar
			config.setMagicAttack(-6);
			config.setRangeAttack(-2);
			config.setStabDef(60);
			config.setSlashDef(63);
			config.setCrushDef(59);
			config.setMagicDef(-6);
			config.setRangeDef(67);
			config.setPrayerBonus(2);
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 52327: // justiciar
			config.setMagicAttack(-40);
			config.setRangeAttack(-20);
			config.setStabDef(132);
			config.setSlashDef(130);
			config.setCrushDef(117);
			config.setMagicDef(-16);
			config.setRangeDef(142);
			config.setPrayerBonus(4);
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 52328: // justiciar
			config.setMagicAttack(-31);
			config.setRangeAttack(-17);
			config.setStabDef(95);
			config.setSlashDef(92);
			config.setCrushDef(93);
			config.setMagicDef(-14);
			config.setRangeDef(102);
			config.setPrayerBonus(4);
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 51024:
			config.setMagicAttack(26);
			config.setRangeAttack(-7);
			config.setStabDef(27);
			config.setSlashDef(24);
			config.setCrushDef(30);
			config.setMagicDef(20);
			config.setMagicDamage(2);
			config.setLevel(Skills.MAGIC, 65);
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 51021:
			config.setMagicAttack(35);
			config.setRangeAttack(8);
			config.setStabDef(42);
			config.setSlashDef(31);
			config.setCrushDef(51);
			config.setMagicDef(28);
			config.setMagicDamage(2);
			config.setLevel(Skills.MAGIC, 65);
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 51018:
			config.setMagicAttack(8);
			config.setRangeAttack(-2);
			config.setStabDef(12);
			config.setSlashDef(13);
			config.setCrushDef(13);
			config.setMagicDef(5);
			config.setMagicDamage(2);
			config.setLevel(Skills.MAGIC, 65);
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 51015: // bulwark
			// TODO stance
			config.setStance(19999);
			config.setAttackSpeed(6);
			config.setCrushAttack(110);
			config.setStabDef(141);
			config.setSlashDef(145);
			config.setCrushDef(145);
			config.setMagicDef(18);
			config.setRangeDef(148);
			config.setStrengthBonus(38);
			config.setLevel(Skills.ATTACK, 75);
			config.setLevel(Skills.DEFENCE, 75);
			config.setAttackStyle(10);
			break;
		case 51031: // infernal harpoon
		case 51028:// dragon harpoon, worn like a sword
			config.setStance(1426);
			config.setAttackSpeed(5);
			config.setStabAttack(38);
			config.setSlashAttack(32);
			config.setSlashDef(1);
			config.setStrengthBonus(42);
			config.setLevel(Skills.ATTACK, 60);
			config.setAttackStyle(5);
			break;
		case 50849:
			config.setStance(1426);
			config.setAttackSpeed(5);
			config.setRangeAttack(36);
			config.setRangedStrBonus(47);
			config.setLevel(Skills.RANGE, 61);
			config.setAttackStyle(17);
			break;
		case 52804:
		case 52806:
		case 52808:
		case 52810:
			config.setStance(1426);
			config.setAttackSpeed(3);
			config.setRangeAttack(28);
			config.setRangedStrBonus(30);
			config.setLevel(Skills.RANGE, 60);
			config.setAttackStyle(17);
			break;
		case 51009:// dragon sword
			config.setStance(2584);
			config.setAttackSpeed(4);
			config.setStabAttack(65);
			config.setSlashAttack(55);
			config.setCrushAttack(-2);
			config.setSlashDef(2);
			config.setStrengthBonus(63);
			config.setLevel(Skills.ATTACK, 60);
			config.setAttackStyle(5);
			break;
		case 51000:
			config.setStabAttack(-7);
			config.setSlashAttack(-8);
			config.setStabAttack(-7);
			config.setMagicAttack(-18);
			config.setRangeAttack(18);
			config.setSlashDef(22);
			config.setSlashDef(24);
			config.setCrushDef(22);
			config.setMagicDef(26);
			config.setRangeDef(58);
			config.setLevel(Skills.RANGE, 75);
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 25477:
			copy(23874, config);
			config.name = "Wings of wealth";
			config.inventoryOptions[4] = "Drop";
			config.tradeable = true;
			config.setPrayerBonus(5);
			config.setLevel(Skills.PRAYER, 60);
			break;
		case 25622:
			copy(23874, config);
			config.name = "Lucky wings";
			config.inventoryOptions[4] = "Drop";
			config.tradeable = true;
			config.setPrayerBonus(5);
			config.setLevel(Skills.PRAYER, 60);
			break;
		case 25478:
			copy(1603, config);
			config.name = "Onyx blood gem";
			config.tradeable = true;
			break;
		case 25476: // scythe u
			copy(52325, config);
			config.name = "Onyx blood scythe";
			config.setStance(2553);
			config.setAttackSpeed(5);
			config.setLevel(Skills.ATTACK, 80);
			config.setLevel(Skills.STRENGTH, 80);
			config.setStabAttack(91);
			config.setSlashAttack(143);
			config.setCrushAttack(39);
			config.setMagicAttack(-6);
			config.setSlashDef(2);
			config.setSlashDef(8);
			config.setCrushDef(10);
			config.setStrengthBonus(80);
			config.tradeable = true;
			break;
		case 52325: // scythe
			config.setStance(2553);
			config.setAttackSpeed(5);
			config.setLevel(Skills.ATTACK, 75);
			config.setLevel(Skills.STRENGTH, 75);
			config.setStabAttack(70);
			config.setSlashAttack(110);
			config.setCrushAttack(30);
			config.setMagicAttack(-6);
			config.setSlashDef(2);
			config.setSlashDef(8);
			config.setCrushDef(10);
			config.setStrengthBonus(75);
			config.tradeable = true;
			config.setAttackStyle(22);
			break;
		case 52322: // avernic defender
			config.setLevel(Skills.ATTACK, 70);
			config.setLevel(Skills.DEFENCE, 70);
			config.setStabAttack(30);
			config.setSlashAttack(29);
			config.setCrushAttack(28);
			config.setMagicAttack(-5);
			config.setRangeAttack(-4);
			config.setStabDef(30);
			config.setSlashDef(29);
			config.setCrushDef(28);
			config.setMagicDef(-5);
			config.setRangeDef(-4);
			config.setStrengthBonus(8);
			break;
		case 52324: // Ghrazi rapier
			config.setLevel(Skills.ATTACK, 75);
			config.setStance(2622);
			config.setAttackSpeed(4);
			config.setStabAttack(94);
			config.setSlashAttack(55);
			config.setStrengthBonus(89);
			config.setAttackStyle(5);
			break;

		case 21462:
			config.setMagicAttack(8);
			config.setRangeAttack(-17);
			config.setStabDef(65);
			config.setSlashDef(70);
			config.setCrushDef(63);
			config.setMagicDef(12);
			config.setRangeDef(-8);
			config.setSummoningDef(15);
			config.setStrengthBonus(3);
			break;
		case 21463:
			config.setMagicAttack(38);
			config.setRangeAttack(-30);
			config.setStabDef(154);
			config.setSlashDef(145);
			config.setCrushDef(121);
			config.setMagicDef(52);
			config.setRangeDef(-16);
			config.setSummoningDef(60);
			config.setStrengthBonus(5);
			break;
		case 21464:
			config.setMagicAttack(25);
			config.setRangeAttack(-21);
			config.setStabDef(110);
			config.setSlashDef(106);
			config.setCrushDef(97);
			config.setMagicDef(36);
			config.setRangeDef(-12);
			config.setSummoningDef(30);
			config.setStrengthBonus(3);
			break;
		case 21465:
			config.setStabAttack(10);
			config.setSlashAttack(10);
			config.setCrushDef(10);
			config.setMagicAttack(5);
			config.setRangeAttack(-10);
			config.setStabDef(8);
			config.setSlashDef(8);
			config.setCrushDef(8);
			config.setMagicDef(7);
			config.setRangeDef(-2);
			config.setSummoningDef(6);
			config.setStrengthBonus(12);
			break;
		case 21466:
			config.setMagicAttack(2);
			config.setRangeAttack(-7);
			config.setStabDef(12);
			config.setSlashDef(13);
			config.setCrushDef(14);
			config.setMagicDef(4);
			config.setRangeDef(-2);
			config.setSummoningDef(15);
			config.setStrengthBonus(4);
			break;
		case 21467:
			config.setMagicAttack(8);
			config.setRangeAttack(14);
			config.setMagicDef(12);
			config.setRangeDef(18);
			config.setSummoningDef(15);
			break;
		case 21468:
			config.setMagicAttack(38);
			config.setRangeAttack(39);
			config.setMagicDef(52);
			config.setRangeDef(80);
			config.setSummoningDef(60);
			break;
		case 21469:
			config.setMagicAttack(25);
			config.setRangeAttack(27);
			config.setMagicDef(36);
			config.setRangeDef(52);
			config.setSummoningDef(30);
			break;
		case 21470:
			config.setMagicAttack(5);
			config.setRangeAttack(8);
			config.setMagicDef(7);
			config.setRangeDef(12);
			config.setSummoningDef(6);
			break;
		case 21471:
			config.setMagicAttack(2);
			config.setRangeAttack(6);
			config.setMagicDef(4);
			config.setRangeDef(9);
			config.setSummoningDef(15);
			break;
		case 21472:
			config.setMagicAttack(-17);
			config.setRangeAttack(14);
			config.setStabDef(65);
			config.setSlashDef(70);
			config.setCrushDef(63);
			config.setMagicDef(-14);
			config.setRangeDef(18);
			config.setSummoningDef(15);
			config.setStrengthBonus(3);
			break;
		case 21473:
			config.setMagicAttack(-30);
			config.setRangeAttack(39);
			config.setStabDef(154);
			config.setSlashDef(145);
			config.setCrushDef(121);
			config.setMagicDef(-20);
			config.setRangeDef(80);
			config.setSummoningDef(60);
			config.setStrengthBonus(5);
			break;
		case 21474:
			config.setMagicAttack(-21);
			config.setRangeAttack(25);
			config.setStabDef(110);
			config.setSlashDef(106);
			config.setCrushDef(97);
			config.setMagicDef(-14);
			config.setRangeDef(52);
			config.setSummoningDef(30);
			config.setStrengthBonus(3);
			break;
		case 21475:
			config.setStabAttack(10);
			config.setSlashAttack(10);
			config.setCrushDef(10);
			config.setMagicAttack(-10);
			config.setRangeAttack(8);
			config.setStabDef(8);
			config.setSlashDef(8);
			config.setCrushDef(8);
			config.setMagicDef(-6);
			config.setRangeDef(12);
			config.setSummoningDef(6);
			config.setStrengthBonus(12);
			break;
		case 21476:
			config.setMagicAttack(-7);
			config.setRangeAttack(6);
			config.setStabDef(12);
			config.setSlashDef(13);
			config.setCrushDef(14);
			config.setMagicDef(-6);
			config.setRangeDef(9);
			config.setSummoningDef(15);
			config.setStrengthBonus(4);
			break;
		case 41941:
		case 42791:
			config.setData(1397, 1);
			break;
		case 6731:
		case 13426:
			config.setMagicAttack(6);
			break;
		case 15018:
			config.setMagicAttack(12);
			break;
		case 15263:
			config.value = 500;
			break;
		case 9040:
			config.value = 750;
			break;
		case 9028:
			config.value = 1000;
			break;
		case 9034:
			config.value = 1250;
			break;
		case 20795:
			config.value = 1000;
			break;
		case 20796:
			config.value = 10000;
			break;
		case 20797:
		case 24454:
			config.value = 100000;
			break;
		case 20798:
			config.value = 1000000;
			break;
		case 20799:
			config.value = 3000000;
			break;
		case 20800:
			config.value = 10000000;
			break;
		case 24455:
			config.setLevel(Skills.ATTACK, 87);
			config.setAttackSpeed(5);
			config.value = 10000000;
			config.tradeable = true;
			break;
		case 24456:
			config.setLevel(Skills.RANGE, 87);
			config.setAttackSpeed(4); // 5 slower
			config.value = 10000000;
			config.tradeable = true;
			break;
		case 24457:
			config.setLevel(Skills.MAGIC, 87);
			config.setAttackSpeed(5);
			config.setMagicDamage(20);
			config.value = 10000000;
			config.tradeable = true;
			break;
		case 50714:
			config.setLevel(Skills.MAGIC, 50);
			config.setMagicAttack(8);
			config.setMagicDef(8);
			config.tradeable = true;
			break;
		case 42018:
			config.clientScriptData = (HashMap<Integer, Object>) ItemConfig.forID(10588).clientScriptData.clone();
			break;
		case 42808:
			config.clientScriptData = (HashMap<Integer, Object>) ItemConfig.forID(11730).clientScriptData.clone();
			config.setSlashAttack(100);
			config.setStrengthBonus(88);
			config.tradeable = true;
			break;
		case 42809:
			config.clientScriptData = (HashMap<Integer, Object>) ItemConfig.forID(11730).clientScriptData.clone();
			config.setSlashAttack(100);
			config.setStrengthBonus(88);
			break;
		case 42788:
			config.clientScriptData = (HashMap<Integer, Object>) ItemConfig.forID(861).clientScriptData.clone();
			config.setRangeAttack(75);
			config.tradeable = true;
			break;
		case 51733: // guardian boots
			config.setMagicAttack(-3);
			config.setRangeDef(-1);
			config.setSlashDef(32);
			config.setStabDef(32);
			config.setCrushDef(32);
			config.setMagicDef(-3);
			config.setRangeDef(24);
			config.setStrengthBonus(3);
			config.setPrayerBonus(2);
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 51742: // granite hammer
			config.setAttackSpeed(4);
			config.setStance(1426);
			config.setCrushAttack(57);
			config.setMagicAttack(-3);
			config.setRangeAttack(-1);
			config.setStrengthBonus(56);
			config.setLevel(Skills.STRENGTH, 50);
			config.setLevel(Skills.ATTACK, 50);
			config.setAttackStyle(10);
			break;
		case 51752: // granite ring (i)
			config.setStabDef(4);
			config.setSlashDef(4);
			config.setCrushDef(4);
			config.setMagicDef(-4);
			config.setRangeDef(16);
			config.setLevel(Skills.STRENGTH, 50);
			config.setLevel(Skills.DEFENCE, 50);
			break;
		case 51739: // granite ring
			config.setStabDef(2);
			config.setSlashDef(2);
			config.setCrushDef(2);
			config.setMagicDef(-2);
			config.setRangeDef(8);
			config.setLevel(Skills.STRENGTH, 50);
			config.setLevel(Skills.DEFENCE, 50);
			break;
		case 51736: // granite gloves
			config.setStabAttack(5);
			config.setSlashAttack(5);
			config.setCrushAttack(9);
			config.setMagicAttack(-3);
			config.setRangeAttack(-1);
			config.setStabDef(8);
			config.setSlashDef(8);
			config.setCrushDef(8);
			config.setMagicDef(-3);
			config.setRangeDef(5);
			config.setStrengthBonus(7);
			config.setLevel(Skills.STRENGTH, 50);
			config.setLevel(Skills.DEFENCE, 50);
			break;
		case 51643: // granite boots
			config.setMagicAttack(-3);
			config.setRangeAttack(-1);
			config.setStabDef(15);
			config.setSlashDef(16);
			config.setCrushDef(17);
			config.setRangeDef(8);
			config.setStrengthBonus(3);
			config.setLevel(Skills.STRENGTH, 50);
			config.setLevel(Skills.DEFENCE, 50);
			break;
		case 51646: // granite longsword
			config.setStance(2554);
			config.setAttackSpeed(5);
			config.setStabAttack(56);
			config.setSlashAttack(65);
			config.setCrushAttack(-2);
			config.setSlashDef(3);
			config.setCrushDef(2);
			config.setStrengthBonus(62);
			config.setLevel(Skills.STRENGTH, 50);
			config.setLevel(Skills.ATTACK, 50);
			config.setAttackStyle(6);
			break;
		case 43385:
			config.setMagicAttack(3);
			config.setMagicDef(3);
			config.setLevel(Skills.MAGIC, 20);
			config.setLevel(Skills.DEFENCE, 10);
			break;
		case 43387:
			config.setMagicAttack(12);
			config.setMagicDef(10);
			config.setLevel(Skills.MAGIC, 20);
			config.setLevel(Skills.DEFENCE, 10);
			break;
		case 43389:
			config.setMagicAttack(8);
			config.setMagicDef(7);
			config.setLevel(Skills.MAGIC, 20);
			config.setLevel(Skills.DEFENCE, 10);
			break;
		case 43393: // xeric's talisman
			config.setMagicAttack(3);
			config.setMagicDef(1);
			break;
		case 43576:// dragon warhammer
			config.setAttackSpeed(6);
			config.setStance(1426);
			config.setSlashAttack(-4);
			config.setStabAttack(-4);
			config.setCrushAttack(95);
			config.setMagicAttack(-4);
			config.setStrengthBonus(85);
			config.setLevel(Skills.ATTACK, 60);
			config.setAttackStyle(10);
			break;
		case 51003: // elder maul
			config.setStance(1747);
			config.setAttackSpeed(6);
			config.setCrushAttack(135);
			config.setMagicAttack(-4);
			config.setStrengthBonus(147);
			config.setLevel(Skills.ATTACK, 75);
			config.setLevel(Skills.STRENGTH, 75);
			config.setAttackStyle(10);
			break;
		case 51006: // kodai wand
			config.setStance(2587);
			config.setAttackSpeed(4);
			config.setMagicAttack(28);
			config.setSlashDef(3);
			config.setCrushDef(3);
			config.setMagicDef(20);
			config.setMagicDamage(15);
			config.setLevel(Skills.MAGIC, 75);
			config.setAttackStyle(1);
			break;
		case 52251:
			config.setStabDef(5);
			config.setSlashDef(6);
			config.setCrushDef(4);
			config.setMagicDef(1);
			config.setRangeDef(5);
			config.setLevel(Skills.DEFENCE, 10);
			break;
		case 52269:
			config.setSlashAttack(-15);
			config.setCrushAttack(-15);
			config.setMagicAttack(-10);
			config.setRangeAttack(2);
			config.setStabDef(8);
			config.setSlashDef(7);
			config.setCrushDef(7);
			config.setMagicDef(5);
			config.setRangeDef(9);
			config.setLevel(Skills.RANGE, 20);
			config.setLevel(Skills.DEFENCE, 10);
			break;
		case 52254:
			config.setStabDef(6);
			config.setSlashDef(7);
			config.setCrushDef(5);
			config.setMagicDef(2);
			config.setRangeDef(6);
			config.setLevel(Skills.DEFENCE, 30);
			break;
		case 52272:
			config.setSlashAttack(-15);
			config.setCrushAttack(-15);
			config.setMagicAttack(-10);
			config.setRangeAttack(3);
			config.setStabDef(10);
			config.setSlashDef(9);
			config.setCrushDef(8);
			config.setMagicDef(7);
			config.setRangeDef(10);
			config.setLevel(Skills.RANGE, 30);
			config.setLevel(Skills.DEFENCE, 30);
			break;
		case 52257:
			config.setStabDef(7);
			config.setSlashDef(8);
			config.setCrushDef(6);
			config.setMagicDef(2);
			config.setRangeDef(7);
			config.setLevel(Skills.DEFENCE, 40);
			break;
		case 52275:
			config.setSlashAttack(-15);
			config.setCrushAttack(-15);
			config.setMagicAttack(-10);
			config.setRangeAttack(4);
			config.setStabDef(14);
			config.setSlashDef(12);
			config.setCrushDef(11);
			config.setMagicDef(9);
			config.setRangeDef(11);
			config.setLevel(Skills.RANGE, 40);
			config.setLevel(Skills.DEFENCE, 40);
			break;
		case 52260:
			config.setStabDef(8);
			config.setSlashDef(9);
			config.setCrushDef(7);
			config.setMagicDef(3);
			config.setRangeDef(8);
			config.setLevel(Skills.DEFENCE, 40);
			break;
		case 52278:
			config.setSlashAttack(-15);
			config.setCrushAttack(-15);
			config.setMagicAttack(-10);
			config.setRangeAttack(5);
			config.setStabDef(16);
			config.setSlashDef(14);
			config.setCrushDef(12);
			config.setMagicDef(12);
			config.setRangeDef(12);
			config.setLevel(Skills.RANGE, 50);
			config.setLevel(Skills.DEFENCE, 40);
			break;
		case 52263:
			config.setStabDef(10);
			config.setSlashDef(13);
			config.setCrushDef(9);
			config.setMagicDef(3);
			config.setRangeDef(9);
			config.setLevel(Skills.DEFENCE, 40);
			break;
		case 52281:
			config.setSlashAttack(-15);
			config.setCrushAttack(-15);
			config.setMagicAttack(-10);
			config.setRangeAttack(6);
			config.setStabDef(18);
			config.setSlashDef(16);
			config.setCrushDef(14);
			config.setMagicDef(13);
			config.setRangeDef(13);
			config.setLevel(Skills.RANGE, 60);
			config.setLevel(Skills.DEFENCE, 40);
			break;
		case 52266:
			config.setStabDef(12);
			config.setSlashDef(15);
			config.setCrushDef(11);
			config.setMagicDef(4);
			config.setRangeDef(10);
			config.setLevel(Skills.DEFENCE, 40);
			break;
		case 52284:
			config.setSlashAttack(-15);
			config.setCrushAttack(-15);
			config.setMagicAttack(-10);
			config.setRangeAttack(7);
			config.setStabDef(21);
			config.setSlashDef(18);
			config.setCrushDef(16);
			config.setMagicDef(15);
			config.setRangeDef(14);
			config.setLevel(Skills.RANGE, 70);
			config.setLevel(Skills.DEFENCE, 40);
			break;
		case 43204: // platinum coins
			config.tradeable = true;
         	config.inventoryOptions[3] = "Add-to-pouch";
			break;
		case 51902:
			config.setAttackSpeed(6);
			config.setStance(2556);
			config.setRangeAttack(94);
			config.setLevel(Skills.RANGE, 64);
			config.setAttackStyle(17);
			break;
		case 51012:
			config.setAttackSpeed(6);
			config.setStance(2556);
			config.setRangeAttack(95);
			config.setLevel(Skills.RANGE, 95);
			config.setAttackStyle(17);
			break;
		case 51905:
		case 51924:
		case 51926:
		case 51928:
		case 51932:
		case 51934:
		case 51936:
		case 51938:
		case 51940:
		case 51942:
		case 51944:
		case 51946:
		case 51948:
		case 51950:
		case 51955:
		case 51957:
		case 51959:
		case 51961:
		case 51963:
		case 51965:
		case 51967:
		case 51969:
		case 51971:
		case 51973:
			config.value /= 3;
			config.setRangedStrBonus(122);
			config.setLevel(Skills.RANGE, 64);
			break;
		case 51633:// aws
		case 51634:
			config.setStabAttack(-10);
			config.setSlashAttack(-10);
			config.setCrushAttack(-10);
			config.setMagicAttack(15);
			config.setRangeAttack(-10);
			config.setStabDef(22);
			config.setSlashDef(30);
			config.setCrushDef(-25);
			config.setMagicDef(15);
			config.setRangeDef(-55);
			config.setStrengthBonus(-2);
			config.setLevel(Skills.MAGIC, 70);
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 52002:// dfw
		case 52003:
			config.setStabAttack(-10);
			config.setSlashAttack(-10);
			config.setCrushAttack(-10);
			config.setMagicAttack(-10);
			config.setRangeAttack(15);
			config.setStabDef(-25);
			config.setSlashDef(-20);
			config.setCrushDef(-22);
			config.setMagicDef(28);
			config.setRangeDef(18);
			config.setStrengthBonus(-2);
			config.setLevel(Skills.RANGE, 70);
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 52109:
			config.setRangeAttack(8);
			config.setStabDef(1);
			config.setSlashDef(1);
			config.setCrushDef(1);
			config.setMagicDef(8);
			config.setRangeDef(2);
			config.setRangedStrBonus(2);
			config.setLevel(Skills.RANGE, 70);
			break;
		case 24365:
			config.setStabDef(56);
			config.setSlashDef(60);
			config.setCrushDef(58);
			config.setRangeDef(58);
			break;
		case 20769:
		case 20771:
			config.setMagicAttack(15);
			config.setMagicDef(15);
			config.setRangedStrBonus(2);
			config.setMagicDamage(2);
			break;
		case 52114:
			config.setCrushAttack(6);
			config.setStabDef(8);
			config.setSlashDef(8);
			config.setCrushDef(8);
			config.setMagicDef(8);
			config.setRangeDef(8);
			config.setStrengthBonus(1);
			config.setPrayerBonus(1);
			break;
		case 52111:
		case 52986:
			config.setPrayerBonus(12);
			config.setLevel(Skills.PRAYER, 80);
			break;
		case 42422:
			config.setStance(2587);
			config.setAttackSpeed(4);
			config.setMagicAttack(20);
			config.setMagicDef(20);
			config.setLevel(Skills.MAGIC, 65);
			config.setAttackStyle(1);
			break;
		case 42424:
			config.setStance(2588);
			config.setAttackSpeed(4);
			config.setRangeAttack(80);
			config.setLevel(Skills.RANGE, 65);
			config.setAttackStyle(17);
			break;
		case 42426:
			config.setStance(2554);
			config.setAttackSpeed(5);
			config.setStabAttack(60);
			config.setSlashAttack(72);
			config.setCrushAttack(-2);
			config.setSlashDef(3);
			config.setCrushDef(2);
			config.setStrengthBonus(75);
			config.setLevel(Skills.ATTACK, 65);
			config.setAttackStyle(6);
			break;
		case 50011:
			config.setStance(2586);
			config.setAttackSpeed(5);
			config.setStabAttack(-2);
			config.setSlashAttack(38);
			config.setCrushAttack(32);
			config.setSlashDef(1);
			config.setStrengthBonus(42);
			config.setLevel(Skills.ATTACK, 65);
			config.setAttackStyle(2);
			break;
		case 50014:
			config.setStance(1426);
			config.setAttackSpeed(5);
			config.setStabAttack(38);
			config.setSlashAttack(-2);
			config.setCrushAttack(32);
			config.setSlashDef(1);
			config.setStrengthBonus(42);
			config.setLevel(Skills.ATTACK, 65);
			config.setAttackStyle(4);
			break;
		case 42437:
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setPrayerBonus(5);
			break;
		case 50727:
			config.setStance(2586);
			config.setAttackSpeed(5);
			config.setStabAttack(2);
			config.setSlashAttack(72);
			config.setCrushAttack(72);
			config.setRangeDef(-1);
			config.setStrengthBonus(92);
			config.setLevel(Skills.ATTACK, 65);
			config.setLevel(Skills.SLAYER, 55);
			config.setAttackStyle(2);
			break;
		case 3150:
			config.stackable = 1;
			break;
		case 49675:
			config.setStance(2554);
			config.setAttackSpeed(4);
			config.setStabAttack(10);
			config.setSlashAttack(38);
			config.setStabDef(3);
			config.setCrushDef(2);
			config.setMagicDamage(2);
			config.setStrengthBonus(8);
			config.setLevel(Skills.ATTACK, 75);
			config.setAttackStyle(6);
			break;
		case 24338: // royal crossbow
		case 10887:
		case 19748:
			// halloween event
		case 9920:
		case 9921:
		case 9922:
		case 9923:
		case 9924:
		case 9225:
		case 43343:
		case 43344:
		case 43307:
			config.tradeable = true;
			config.inventoryOptions[4] = "Drop";
			break;
		case 23643:
			config.value = 50000;
			break;
		case 41889: // zamorakian hasta
			config.setAttackSpeed(4);
			config.setStance(2585);
			config.setStabAttack(85);
			config.setSlashAttack(65);
			config.setCrushAttack(65);
			config.setStabDef(13);
			config.setSlashDef(13);
			config.setCrushDef(12);
			config.setRangeDef(13);
			config.setStrengthBonus(75);
			config.setPrayerBonus(2);
			config.setLevel(Skills.ATTACK, 70);
			config.setAttackStyle(14);
			break;
		case 52978: // dragon hunter lance
			config.setAttackSpeed(4);
			config.setStance(2585);
			config.setStabAttack(85);
			config.setSlashAttack(65);
			config.setCrushAttack(65);
			config.setStrengthBonus(70);
			config.setLevel(Skills.ATTACK, 70);
			config.setAttackStyle(14);
			break;
		case 52731:
		case 52734:
		case 52737:
		case 52740:
		case 52743:
			config.setAttackSpeed(4);
			config.setStance(2585);
			config.setStabAttack(55);
			config.setSlashAttack(55);
			config.setCrushAttack(55);
			config.setStabDef(-15);
			config.setSlashDef(-15);
			config.setCrushDef(-12);
			config.setRangeDef(-15);
			config.setStrengthBonus(60);
			config.setLevel(Skills.ATTACK, 60);
			config.setAttackStyle(14);
			break;
		case 51298: // obsidian helmet
			config.setStabDef(25);
			config.setSlashDef(23);
			config.setCrushDef(26);
			config.setRangeDef(24);
			config.setStrengthBonus(3);
			config.setLevel(Skills.DEFENCE, 60);
			break;
		case 51301: // obsidian pl8
			config.setStabDef(55);
			config.setSlashDef(78);
			config.setCrushDef(56);
			config.setMagicDef(-15);
			config.setRangeDef(60);
			config.setStrengthBonus(3);
			config.setLevel(Skills.DEFENCE, 60);
			break;
		case 51304: // obsidian legs
			config.setStabDef(46);
			config.setSlashDef(43);
			config.setCrushDef(41);
			config.setMagicDef(-10);
			config.setRangeDef(40);
			config.setStrengthBonus(1);
			config.setLevel(Skills.DEFENCE, 60);
			break;
		case 51791: // imbued god capes
		case 51793:
		case 51795:
			config.setMagicAttack(15);
			config.setStabDef(3);
			config.setSlashDef(3);
			config.setCrushDef(3);
			config.setMagicDef(15);
			config.setMagicDamage(2);
			config.setLevel(Skills.MAGIC, 75);
			break;
		case 51295:
			config.setStabAttack(4);
			config.setSlashAttack(4);
			config.setCrushAttack(4);
			config.setMagicAttack(1);
			config.setRangeAttack(1);
			config.setStabDef(12);
			config.setSlashDef(12);
			config.setCrushDef(12);
			config.setMagicDef(12);
			config.setRangeDef(12);
			config.setStrengthBonus(8);
			config.setPrayerBonus(2);
			break;
		// blisterwood
		case 21580:
		case 21582:
			config.value = 100;
			break;
		case 21581:
			config.value = 1;
			break;
		case 13157: // vyre corpse
			config.stackable = 1;
			break;
		case 4866: // ahrim staff
		case 4865:
		case 4864:
		case 4863:
		case 4862:
		case 4710:
			config.equipType = -1;
			config.setMagicDamage(5);
			config.setAttackSpeed(4);
			break;
		case 25441:
			copy(50997, config);
			config.name += " (u)";
			;
			config.setStance(2588);
			config.setAttackSpeed(6);
			config.setRangeAttack(90);
			config.setRangedStrBonus(25);
			config.setPrayerBonus(4);
			config.setLevel(Skills.RANGE, 75);
			break;
		case 25460:
			copy(50997, config);
			config.name = "Twisted bow (Halloween)";
			config.setStance(2588);
			config.setAttackSpeed(6);
			config.setRangeAttack(70);
			config.setRangedStrBonus(20);
			config.setPrayerBonus(4);
			config.setLevel(Skills.RANGE, 75);
			config.tradeable = true;
			break;
		case 25469:
			copy(50997, config);
			config.name = "Twisted bow (New Year)";
			config.setStance(2588);
			config.setAttackSpeed(6);
			config.setRangeAttack(70);
			config.setRangedStrBonus(20);
			config.setPrayerBonus(4);
			config.setLevel(Skills.RANGE, 75);
			config.tradeable = true;
			break;
		case 25470:
			copy(14599, config);
			config.clientScriptData = (HashMap<Integer, Object>) ItemConfig.forID(6585).clientScriptData.clone();
			config.name = "Looter's amulet";
			config.inventoryOptions[4] = "Drop";
			config.tradeable = true;
			break;
		case 25479:
			copy(14599, config);
			config.clientScriptData = (HashMap<Integer, Object>) ItemConfig.forID(49553).clientScriptData.clone();
			config.name = "Looter's amulet of torture";
			config.inventoryOptions[4] = "Drop";
			config.tradeable = true;
			break;
		case 25480:
			copy(14599, config);
			config.clientScriptData = (HashMap<Integer, Object>) ItemConfig.forID(49547).clientScriptData.clone();
			config.name = "Looter's necklace of anguish";
			config.inventoryOptions[4] = "Drop";
			config.tradeable = true;
			break;
		case 25481:
			copy(1505, config);
			config.name = "Imbue scroll";
			config.tradeable = true;
			break;
		case 25739:
			copy(1505, config);
			config.name = "Infinity imbue scroll";
			config.tradeable = true;
			break;
			case 25760:
				copy(1505, config);
				config.name = "Bandos imbue scroll";
				config.tradeable = true;
				break;
			case 25761:
				copy(1505, config);
				config.name = "Armadyl imbue scroll";
				config.tradeable = true;
				break;
			case 25762:
				copy(1505, config);
				config.name = "Subjugation imbue scroll";
				config.tradeable = true;
				break;
		case 25483:
			copy(14646, config);
			config.name = "Soul stone";
			config.stackable = 0;
			break;
		case 25484: // combo with occult
			copy(18335, config);
			config.setLevel(Skills.MAGIC, 70);
			config.setMagicAttack(15);
			config.setMagicDamage(15);
			config.setPrayerBonus(2);
			config.name = "Soul necklace";
			config.tradeable = true;
			break;
		case 25485: // combo with looters
			copy(42002, config);
			config.setLevel(Skills.MAGIC, 70);
			config.setMagicAttack(15);
			config.setMagicDamage(15);
			config.setPrayerBonus(2);
			config.name = "Looter's soul necklace";
			config.tradeable = true;
			break;
		case 25740: // infinity necklace, combo of looters torture, looters anguish and looter soul
			copy(17291, config);
			config.setLevel(Skills.MAGIC, 70);
			config.setLevel(Skills.HITPOINTS, 75);
			config.setStabAttack(15);
			config.setSlashAttack(15);
			config.setCrushAttack(15);
			config.setStrengthBonus(10);
			config.setRangeAttack(15);
			config.setRangedStrBonus(5);
			config.setMagicAttack(15);
			config.setMagicDamage(15);
			config.setPrayerBonus(5);
			config.setStabDef(15);
			config.setSlashDef(15);
			config.setCrushDef(15);
			config.setMagicDef(15);
			config.setRangeDef(15);
			config.name = "Infinity necklace (i)";
			config.inventoryOptions[4] = "Drop";
			config.inventoryOptions[2] = "Switch auto-loot";
			config.tradeable = true;
			break;
		case 25741: // infinity necklace, combo of looters torture, looters anguish and looter soul
			copy(49710, config);
			config.setStabAttack(8);
			config.setSlashAttack(8);
			config.setCrushAttack(8);
			config.setRangeAttack(8);
			config.setMagicAttack(12);
			config.setStrengthBonus(8);
			config.setPrayerBonus(8);
			config.name = "Infinity ring (i)";
			config.tradeable = true;
			break;
		case 25486: // infinity necklace, combo of looters torture, looters anguish and looter soul
			copy(17291, config);
			config.setLevel(Skills.MAGIC, 70);
			config.setLevel(Skills.HITPOINTS, 75);
			config.setStabAttack(15);
			config.setSlashAttack(15);
			config.setCrushAttack(15);
			config.setStrengthBonus(10);
			config.setRangeAttack(15);
			config.setRangedStrBonus(5);
			config.setMagicAttack(15);
			config.setMagicDamage(15);
			config.setPrayerBonus(5);
			config.setStabDef(15);
			config.setSlashDef(15);
			config.setCrushDef(15);
			config.setMagicDef(15);
			config.setRangeDef(15);
			config.name = "Infinity necklace";
			config.inventoryOptions[4] = "Drop";
			config.inventoryOptions[2] = "Switch auto-loot";
			config.tradeable = true;
			break;
		case 25488: // infinity necklace, combo of looters torture, looters anguish and looter soul
			copy(49710, config);
			config.setStabAttack(8);
			config.setSlashAttack(8);
			config.setCrushAttack(8);
			config.setRangeAttack(8);
			config.setMagicAttack(12);
			config.setStrengthBonus(8);
			config.setPrayerBonus(8);
			config.name = "Infinity ring";
			config.tradeable = true;
			break;
		case 25442:
			copy(21777, config);
			config.name += " (u)";
			config.setMagicDamage(15);
			break;
		case 25443:
			config.name = "Shrimpy";
			break;
		case 25444:
			config.name = "Nexterminator";
			break;
		case 25445:
			config.name = "Queen Black Dragonling";
			break;
		case 25446:
			config.name = "Galvek pet";
			break;
		case 25447:
			copy(3706, config);
			config.name = "Aura token";
			config.stackable = 1;
			config.tradeable = true;
			config.inventoryOptions[4] = "Drop";
			break;
		case 25448:
			copy(1633, config);
			config.name = "Crushed sapphire";
			config.value = 3000;
			config.tradeable = true;
			break;
		case 25449:
			copy(1633, config);
			config.name = "Crushed emerald";
			config.value = 3750;
			config.tradeable = true;
			break;
		case 25450:
			copy(1633, config);
			config.name = "Crushed ruby";
			config.value = 4500;
			config.tradeable = true;
			break;
		case 25451:
			copy(1633, config);
			config.name = "Crushed diamond";
			config.value = 6000;
			config.tradeable = true;
			break;
		case 25452:
			copy(1633, config);
			config.name = "Crushed onyx";
			config.value = 7500;
			config.tradeable = true;
			break;
		case 25473:
			copy(1633, config);
			config.name = "Crushed zenyte";
			config.value = 9000;
			config.tradeable = true;
			break;
		case 25594:
			copy(6199, config);
			config.name = "Minigame Box";
			config.tradeable = true;
			config.value = 100000;
			break;
		case 25453: // god mystery box
			copy(6199, config);
			config.name = "God Mystery Box";
			config.tradeable = true;
			config.value = 15000000;
			break;
			case 25763: // aura mystery box
				copy(10025, config);
				config.name = "Aura Mystery Box";
				config.tradeable = true;
				config.value = 50000;
				config.inventoryOptions[0] = "Open";
				config.inventoryOptions[1] = "Quick-open";
				config.inventoryOptions[2] = "Preview";
				config.stackable = 1;
				break;
		case 25454:
			config.name = "Drunken Dwarf pet";
			break;
		case 25501:
			config.name = "Genie pet";
			break;
		case 25457:
			config.name = "Coins pet";
			break;
		case 25461:
			config.name = "Ahrim pet";
			break;
		case 25462:
			config.name = "Dharok pet";
			break;
		case 25463:
			config.name = "Guthan pet";
			break;
		case 25464:
			config.name = "Karil pet";
			break;
		case 25465:
			config.name = "Torag pet";
			break;
		case 25466:
			config.name = "Verac pet";
			break;
		case 25467:
		case 25468:
		case 25482:
			config.name = "Onyx pet";
			break;
		case 25487:
			config.name = "Combat dummy";
			break;
		case 25471:
			config.name = "Nomad pet";
			break;
		case 25474:
			config.name = "Sotetseg pet";
			break;
		case 25475:
			config.name = "Avatar of Creation pet";
			break;
		case 25472:
			copy(12183, config);
			config.name = "Donator Shards";
			config.tradeable = true;
			break;
		case 25455:
			copy(780, config);
			config.inventoryOptions[0] = "Repair";
			config.inventoryOptions[2] = "Info";
			config.name = "Pandora key fragment";
			config.tradeable = true;
			config.stackable = 1;
			break;
		case 25456:
			copy(6754, config);
			config.inventoryOptions[0] = "Info";
			config.name = "Pandora key";
			config.inventoryOptions[4] = "Drop";
			config.tradeable = true;
			config.stackable = 1;
			break;
		case 25458:
			copy(15378, config);
			config.name = "Barrow token";
			config.inventoryOptions[4] = "Drop";
			config.tradeable = true;
			config.stackable = 1;
			break;
		case 25459:
			config.name = "Vote ticket";
			config.inventoryOptions[4] = "Drop";
			config.stackable = 1;
			config.tradeable = true;
			break;
		case 50997: // twisted bow
			config.setStance(2588);
			config.setAttackSpeed(6);
			config.setRangeAttack(70);
			config.setRangedStrBonus(20);
			config.setPrayerBonus(4);
			config.setLevel(Skills.RANGE, 75);
			config.setAttackStyle(17);
			break;
		// 49550, 49547, 49553, 49544
		case 49710: // ring of suffering
			config.setStabDef(20);
			config.setSlashDef(20);
			config.setCrushDef(20);
			config.setMagicDef(20);
			config.setRangeDef(20);
			config.setPrayerBonus(4);
			config.setLevel(Skills.HITPOINTS, 75);
			break;
		case 49550: // ring of suffering
			config.setStabDef(10);
			config.setSlashDef(10);
			config.setCrushDef(10);
			config.setMagicDef(10);
			config.setRangeDef(10);
			config.setPrayerBonus(2);
			config.setLevel(Skills.HITPOINTS, 75);
			break;
		case 49547: // Necklace of anguish
			config.setRangeAttack(15);
			config.setRangedStrBonus(5);
			config.setPrayerBonus(2);
			config.setLevel(Skills.HITPOINTS, 75);
			break;
		case 49553: // amulet of torture
			config.setStabAttack(15);
			config.setSlashAttack(15);
			config.setCrushAttack(15);
			config.setStrengthBonus(10);
			config.setPrayerBonus(2);
			config.setLevel(Skills.HITPOINTS, 75);
			break;
		case 49544: // tormented bracelet
			config.setMagicAttack(10);
			config.setMagicDamage(5);
			config.setPrayerBonus(2);
			config.setLevel(Skills.HITPOINTS, 75);
			break;
		case 49669:// redwood
			config.inventoryOptions[0] = "Craft";
			config.inventoryOptions[1] = "Light";
			break;
		case 49496: // uncut zenyte
		case 43383: // xerican fabric
			config.inventoryOptions[0] = "Craft";
			break;
		case 52838:
			config.equipSlot = Equipment.SLOT_CAPE;
			config.setSlashDef(1);
			config.setCrushDef(1);
			config.setRangeDef(-2);
			break;
		case 52840:
			config.equipSlot = Equipment.SLOT_WEAPON;
			config.setSlashAttack(-100);
			config.setStabAttack(-100);
			config.setStabAttack(-50);
			config.setStrengthBonus(-10);
			break;
		case 52826:
		case 52829:
		case 52832:
		case 52835:
			config.inventoryOptions[0] = "Gut";
			config.tradeable = true;
			break;
		case 49484: // dragon javelin
		case 49486:
		case 49488:
		case 49490:
			config.setRangedStrBonus(150);
			break;
		case 49478: // balista
			config.setStance(1603);
			config.setRangeAttack(110);
			config.setAttackSpeed(7);
			config.setLevel(Skills.RANGE, 65);
			config.setAttackStyle(17);
			break;
		case 49481:
			config.setStance(1603);
			config.setRangeAttack(125);
			config.setRangedStrBonus(15);
			config.setAttackSpeed(7);
			config.setLevel(Skills.RANGE, 75);
			config.setAttackStyle(17);
			break;
		case 43241: // infernal axe
			config.clientScriptData = (HashMap<Integer, Object>) ItemConfig.forID(6739).clientScriptData.clone();
			break;
		case 43243: // infernal pickaxe
			config.clientScriptData = (HashMap<Integer, Object>) ItemConfig.forID(15259).clientScriptData.clone();
			break;
		case 43239: // primodial boots
			config.setStabAttack(2);
			config.setSlashAttack(2);
			config.setCrushAttack(2);
			config.setMagicAttack(-4);
			config.setRangeAttack(-1);
			config.setStabDef(22);
			config.setSlashDef(22);
			config.setCrushDef(22);
			config.setStrengthBonus(5);
			config.setLevel(Skills.STRENGTH, 75);
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 43235: // eternal boots
			config.setMagicAttack(8);
			config.setStabDef(5);
			config.setSlashDef(5);
			config.setCrushDef(5);
			config.setMagicDef(8);
			config.setRangeDef(5);
			config.setLevel(Skills.MAGIC, 75);
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 43237: // pegasian boots
			config.setMagicAttack(-12);
			config.setRangeAttack(12);
			config.setStabDef(5);
			config.setSlashDef(5);
			config.setCrushDef(5);
			config.setMagicDef(5);
			config.setRangeDef(5);
			config.setLevel(Skills.RANGE, 75);
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 43263: // bludgeon
			config.setAttackSpeed(4);
			config.setStance(1580);
			config.setCrushAttack(102);
			config.setStrengthBonus(85);
			config.setLevel(Skills.ATTACK, 70);
			config.setLevel(Skills.STRENGTH, 70);
			config.setAttackStyle(10);
			break;
		case 52516: // dawnbrigher
		case 52323: // sang staff
			config.tradeable = true;
			config.setAttackSpeed(4);
			config.setStance(2553);
			config.setMagicAttack(25);
			config.setStabDef(2);
			config.setSlashDef(3);
			config.setCrushDef(1);
			config.setMagicDef(15);
			config.setLevel(Skills.MAGIC, 75);
			config.setAttackStyle(28);
			config.setAttackSpeed(4);
			break;
		case 42899:
		case 42900: // toxic trident
			config.setAttackSpeed(5);
			config.setStance(2553);
			config.setMagicAttack(25);
			config.setStabDef(2);
			config.setSlashDef(3);
			config.setCrushDef(1);
			config.setMagicDef(15);
			config.setLevel(Skills.MAGIC, 75);
			config.setAttackStyle(28);
			config.setAttackSpeed(4);
			break;
		case 41905: // trident
		case 41907:
		case 41908:
			config.setStance(2553);
			config.setMagicAttack(15);
			config.setStabDef(2);
			config.setSlashDef(3);
			config.setCrushDef(1);
			config.setMagicDef(15);
			config.setLevel(Skills.MAGIC, 75);
			config.setAttackStyle(28);
			config.setAttackSpeed(4);
			break;
		case 42006: // abyssal tentacle
			config.setAttackSpeed(4);
			config.setStance(234);
			config.setSlashAttack(90);
			config.setStrengthBonus(86);
			config.setLevel(Skills.ATTACK, 75);
			config.setAttackStyle(11);
			break;
		case 42926: // blowpipe
			config.setAttackSpeed(3);
			config.setStance(28);
			config.setRangeAttack(60);
			config.setRangedStrBonus(40);
			config.setLevel(Skills.RANGE, 75);
			config.setAttackStyle(18);
			break;
		case 42904: // toxic staff of dead
			config.setAttackSpeed(4);
			config.setStance(2553);
			config.setStabAttack(55);
			config.setSlashAttack(70);
			config.setMagicAttack(25);
			config.setSlashDef(3);
			config.setCrushDef(3);
			config.setMagicDef(17);
			config.setStrengthBonus(72);
			config.setMagicDamage(15);
			config.setLevel(Skills.MAGIC, 75);
			config.setLevel(Skills.ATTACK, 75);
			config.setAttackStyle(26);
			break;
		case 42000: // mystic smoke staff
			config.setAttackSpeed(5);
			config.setStance(2553);
			config.setStabAttack(10);
			config.setSlashAttack(-1);
			config.setCrushAttack(40);
			config.setMagicAttack(10);
			config.setStabDef(2);
			config.setSlashDef(3);
			config.setCrushDef(1);
			config.setMagicDef(10);
			config.setStrengthBonus(50);
			config.setLevel(Skills.MAGIC, 40);
			config.setLevel(Skills.ATTACK, 40);
			config.setAttackStyle(26);
			break;
		case 41791: // staff of dead
		case 42902:
			config.setAttackSpeed(4);
			config.setStance(2553);
			config.setStabAttack(55);
			config.setSlashAttack(70);
			config.setMagicAttack(17);
			config.setSlashDef(3);
			config.setCrushDef(3);
			config.setMagicDef(17);
			config.setStrengthBonus(72);
			config.setMagicDamage(15);
			config.setLevel(Skills.MAGIC, 75);
			config.setLevel(Skills.ATTACK, 75);
			config.setAttackStyle(26);
			break;
		case 42931: // serpentine helm
			config.setMagicAttack(-5);
			config.setRangeAttack(-5);
			config.setStabDef(52);
			config.setSlashDef(55);
			config.setCrushDef(58);
			config.setMagicDef(0);
			config.setRangeDef(50);
			config.setStrengthBonus(5);
			config.setLevel(Skills.DEFENCE, 75);
			break;
		case 49707: // eternal glory
			config.clientScriptData = (HashMap<Integer, Object>) ItemConfig.forID(1712).clientScriptData.clone();
			break;
		case 42691: // i
			config.setCrushAttack(8);
			config.setCrushDef(8);
			break;
		case 42603: // tynical ring
			config.setCrushAttack(4);
			config.setCrushDef(4);
			break;
		case 42692: // i
			config.setStabAttack(8);
			config.setStabDef(8);
			break;
		case 42605: // treas ring
			config.setStabAttack(4);
			config.setStabDef(4);
			break;
		case 43202: // i
			config.setStabDef(1);
			config.setSlashDef(1);
			config.setCrushDef(1);
			config.setMagicDef(1);
			config.setRangeDef(1);
			config.setPrayerBonus(8);
			break;
		case 42601: // ring of the gods
			config.setStabDef(1);
			config.setSlashDef(1);
			config.setCrushDef(1);
			config.setMagicDef(1);
			config.setRangeDef(1);
			config.setPrayerBonus(4);
			break;
		case 25430:
			config.name = "Overload (Special)";
			config.value = 0;
			break;
		case 25431:
			config.name = "Rocktail (Special)";
			config.value = 0;
			break;
		case 25432: // mystery box
			copy(6199, config);
			config.name = "Pet Mystery Box";
			config.tradeable = true;
			config.value = 10000000;
			break;
		case 25433:
			copy(8013, config);
			config.name = "PK tablet";
			config.inventoryOptions[1] = null;
			config.tradeable = true;
			config.value = 30000;
			break;
		case 25434:
			copy(12852, config);
			config.name = "Loyalty token";
			break;
		case 25435:
			copy(13663, config);
			config.name = "200k Coins ticket";
			config.inventoryOptions[4] = "Drop";
			config.value = 1;
			break;
		case 25436: // premium mystery box
			copy(6199, config);
			config.name = "Premium mystery box";
			config.tradeable = true;
			config.value = 8000000;
			break;
		case 25489: // Chungus loot box
			copy(405, config);
			config.name = "Corrupted casket";
			config.stackable = 1;
			config.tradeable = true;
			break;
		case 25490:
			copy(4084, config);
			config.name = "Purple Sled";
			config.tradeable = true;
			break;
		case 25491:
			config.name = "Wolpertinger pet";
			break;
		case 25492: // Beginner mystery box
			copy(6199, config);
			config.name = "Mystery Box";
			config.value = 50000;
			break;
		case 25503: // millionare mystery box
			copy(6199, config);
			config.name = "Millionaire's box";
			config.tradeable = true;
			config.value = 1000000;
			break;
		case 25504:
			copy(ItemConfig.forID(16955), config);
			config.name = "Legendary lightning rapier";
			config.inventoryOptions[2] = "";
			config.setData(14, 3);
			config.setStrengthBonus(95);
			config.setStabAttack(90); // 100
			config.setLevel(Skills.ATTACK, 85);
			config.tradeable = true;
			break;
		case 25437: // emerald ticket
			copy(ItemConfig.forID(43190), config);
			config.name = "Normal-Super Donator ticket";
			config.inventoryOptions[4] = "Drop";
			config.tradeable = true;
			config.value = 5000000;
			break;
		case 25438: // ruby ticket
			copy(ItemConfig.forID(43190), config);
			config.name = "Super-Extreme Donator ticket";
			config.inventoryOptions[4] = "Drop";
			config.tradeable = true;
			config.value = 10000000;
			break;
		case 25439: // diamond ticket
			copy(ItemConfig.forID(43190), config);
			config.name = "Extreme-Legendary Donator ticket";
			config.inventoryOptions[4] = "Drop";
			config.tradeable = true;
			config.value = 20000000;
			break;
		case 25440: // onyx ticket
			copy(ItemConfig.forID(43190), config);
			config.name = "Legendary-VIP Donator ticket";
			config.inventoryOptions[4] = "Drop";
			config.tradeable = true;
			config.value = 40000000;
			break;
		case 25493: // zenyte ticket
			copy(ItemConfig.forID(43190), config);
			config.name = "Supreme VIP Donator ticket";
			config.inventoryOptions[4] = "Drop";
			config.tradeable = true;
			config.value = 80000000;
			break;
		case 25494: // zenyte ticket
			copy(ItemConfig.forID(43190), config);
			config.name = "VIP-Supreme VIP Donator ticket";
			config.inventoryOptions[4] = "Drop";
			config.tradeable = true;
			config.value = 80000000;
			break;
		case 25495:
			copy(13742, config);
			config.setLevel(Skills.DEFENCE, 75);
			config.setLevel(Skills.MAGIC, 65);
			config.setLevel(Skills.STRENGTH, 65);
			config.setLevel(Skills.PRAYER, 65);
			config.setSlashAttack(15);
			config.setStabAttack(15);
			config.setCrushAttack(15);
			config.setMagicAttack(20);
			config.setMagicDef(30);
			config.setStrengthBonus(6);
			config.name = "Almighty spirit shield";
			config.value = 5000000;
			config.tradeable = true;
			break;
		case 25496:
			copy(52323, config);
			config.name = "Empowered sanguinesti staff";
			config.tradeable = true;
			config.setAttackSpeed(4);
			config.setStance(2553);
			config.setMagicAttack(25);
			config.setStabDef(2);
			config.setSlashDef(3);
			config.setCrushDef(1);
			config.setMagicDef(15);
			config.setMagicDamage(30);
			config.setAttackSpeed(4);
			config.setLevel(Skills.MAGIC, 75);
			break;
			case 25764:
				copy(52323, config);
				config.name = "Lucien's staff";
				config.tradeable = true;
				config.setAttackSpeed(4);
				config.setStance(2553);
				config.setMagicAttack(25);
				config.setStabDef(2);
				config.setSlashDef(3);
				config.setCrushDef(1);
				config.setMagicDef(15);
				config.setMagicDamage(30);
				config.setAttackSpeed(4);
				config.setLevel(Skills.MAGIC, 80);
				break;
			case 25765:
				copy(1603, config);
				config.name = "Lucien empowered gem";
				config.tradeable = true;
				break;
		case 25497:
			config.name = "Almighty sigil";
			config.value = 50000000;
			config.tradeable = true;
			break;
		case 25498:
			copy(964, config);
			config.name = "Corrupted curse";
			config.value = 20000000;
			config.tradeable = true;
			break;
		case 25499:
			copy(15492, config);
			config.name = "Eternal slayer helmet";
			config.equipType = 8;
			config.value = 5000000;
			config.setStabAttack(1);
			config.setSlashAttack(1);
			config.setCrushAttack(1);
			config.setRangeAttack(5);
			config.setMagicAttack(5);
			config.setMagicDamage(1);
			config.setStrengthBonus(3);
			config.setPrayerBonus(1);
			config.setStabDef(45);
			config.setSlashDef(48);
			config.setCrushDef(41);
			config.setMagicDef(12);
			config.setRangeDef(46);
			config.setLevel(Skills.DEFENCE, 40);
			break;
		case 25502:
			copy(42926, config);
			config.name = "Infernal Blowpipe";
			config.value = 10000000;
			config.tradeable = true;
			config.setRangeAttack(80);
			config.setRangedStrBonus(50);
			config.setLevel(Skills.RANGE, 80);
			break;
		case 25500:
			copy(8921, config);
			config.inventoryOptions = new String[] { null, null, null, null, "Drop" };
			config.name = "Eternal slayer enchantment";
			config.tradeable = true;
			break;
		case 23713: // small lamp
			config.value = 10;
			break;
		case 24154: // spins
			config.value = 10;
			config.name = "500k Coins Ticket";
			config.inventoryOptions[0] = "Tear";
			config.stackable = 1;
			break;
			case 24155: // spins
				config.name = "1000k Coins Ticket";
				config.inventoryOptions[0] = "Tear";
				config.stackable = 1;
				break;
		case 24108: // tophats
		case 24110:
		case 24112:
		case 24114:
			config.value = 100;
			break;
		case 21258: // robin hats
		case 21260:
		case 21262:
		case 21264:
			config.value = 175;
			break;
		case 19747: // rainbow
			config.value = 75;
			break;
		case 7003: // camel mask
			config.value = 50;
			break;
		case 24317: // monkey cape
			config.value = 275;
			break;
		case 7927: // easter ring
			config.value = 350;
			break;
		case 5607: // fox
		case 5608: // chicken
		case 5609: // bag
			config.value = 187;
			break;
		case 4566: // rubber chicken
			config.value = 500;
			break;
		case 4084: // sled
			config.value = 3000;
			break;
		case 15441:
		case 15442:
		case 15443:
		case 15444:
		case 15701:
		case 15702:
		case 15703:
		case 15704:
			config.value = 300;
			break;
		case 22207:
		case 22209:
		case 22211:
		case 22213:
			config.value = 500;
			break;
		case 41924:
			config.setStabAttack(-8);
			config.setSlashAttack(-8);
			config.setCrushAttack(-8);
			config.setMagicAttack(12);
			config.setRangeAttack(-12);
			config.setStabDef(50);
			config.setSlashDef(52);
			config.setCrushDef(48);
			config.setMagicDef(15);
			config.setLevel(Skills.DEFENCE, 60);
			break;
		case 41926:
			config.setStabAttack(-12);
			config.setSlashAttack(-12);
			config.setMagicAttack(-8);
			config.setRangeAttack(12);
			config.setMagicDef(24);
			config.setRangeDef(52);
			config.setLevel(Skills.DEFENCE, 60);
			break;
		case 50517:
			config.setMagicAttack(10);
			config.setMagicDef(8);
			config.setLevel(Skills.MAGIC, 40);
			break;
		case 50520:
			config.setMagicAttack(6);
			config.setMagicDef(6);
			config.setLevel(Skills.MAGIC, 40);
			config.equipSlot = Equipment.SLOT_LEGS;
			break;
		case 50595:
			config.setMagicAttack(5);
			config.setMagicDef(4);
			config.setLevel(Skills.MAGIC, 40);
			break;
		case 41959: // black chincompa
			config.setStance(234);
			config.setAttackSpeed(4);
			config.setRangeAttack(80);
			config.setRangedStrBonus(30);
			config.setLevel(Skills.RANGE, 65);
			config.setAttackStyle(19);
			break;
		case 43092: // crystal hally
			copy(53987, config);
			config.setStance(28);
			config.setAttackSpeed(7);
			config.setStabAttack(85);
			config.setSlashAttack(110);
			config.setCrushAttack(5);
			config.setMagicAttack(-4);
			config.setStabDef(-1);
			config.setSlashDef(4);
			config.setCrushDef(5);
			config.setStrengthBonus(118);
			config.setLevel(Skills.ATTACK, 70);
			config.setLevel(Skills.STRENGTH, 35);
			config.setLevel(Skills.AGILITY, 50);
			config.setAttackStyle(15);
			// western province diary
			/*
			 * config.setLevel(Skills.RANGE, 70); config.setLevel(Skills.FISHING, 62);
			 * config.setLevel(Skills.COOKING, 62); config.setLevel(Skills.HUNTER, 69);
			 * config.setLevel(Skills.AGILITY, 50); config.setLevel(Skills.FIREMAKING, 50);
			 * config.setLevel(Skills.MINING, 70); config.setLevel(Skills.FARMING, 68);
			 * config.setLevel(Skills.FLETCHING, 5); config.setLevel(Skills.CONSTRUCTION,
			 * 65); config.setLevel(Skills.MAGIC, 64); config.setLevel(Skills.THIEVING, 75);
			 */
			config.value = 2225000;
			break;
		case 42002: // occult neck
			config.setMagicAttack(12);
			config.setMagicDamage(10);
			config.setLevel(Skills.MAGIC, 70);
			break;
		case 43265: // abyssal dagger
		case 43267:
		case 43269:
		case 43271:
			config.setStance(2584);
			config.setAttackSpeed(4);
			config.setStabAttack(75);
			config.setSlashAttack(40);
			config.setCrushAttack(-4);
			config.setMagicAttack(1);
			config.setMagicDef(1);
			config.setStrengthBonus(75);
			config.setLevel(Skills.ATTACK, 70);
			config.setAttackStyle(5);
			break;
		case 6199: // mystery box
			config.name = "Super Mystery Box";
			config.tradeable = true;
			config.value = 5000000;
			config.inventoryOptions[1] = "Quick-open";
			config.inventoryOptions[2] = "Preview";
			config.stackable = 1;
			break;
		// pots
		case 2436:
		case 2440:
		case 2442:
		case 2444:
		case 3040:
		case 2452:
		case 3024:
		case 6685:
		case 2434:
		case 12140:
			config.value *= 25;
			break;
		// bolts
		case 9140:
			config.value = 20;
			break;
		case 9141:
			config.value = 40;
			break;
		case 9142:
			config.value = 76;
			break;
		case 9143:
			config.value = 172;
			break;
		case 9144:
			config.value = 510;
			break;
		case 10498:
			config.value = 5000;
			break;
		case 10499:
			config.value = 50000;
			break;
		case 20068:
			config.value = 100000;
			break;
		case 3105:
			config.value = 12;
			break;
		case 385: // shark
			config.value = 2000;
			break;
		case 43441:
		case 15272:
			config.value = 5000;
			break;
		case 41936:
		case 391:
			config.value = 3000;
			break;
		case 7946: // monkfish
			config.value = 600;
			break;
		case 12936:// leather coifs
		case 12943:
		case 12950:
		case 12957:
		case 24388:
			config.name = config.name.replace(" 100", "");
			config.tradeable = true;
			break;
		case 15432: // nomad capes
		case 15433:
			config.value = 100000;
			break;
		case 7461: // dragon gloves
			config.value = 50000;
			break;
		case 43221: // music cape
		case 43222:
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setSummoningDef(9);
			if (config.getId() == 43222)
				config.setPrayerBonus(4);
			break;
		case 25354:
			config.name = "Attack master cape";
			config.inventoryOptions = new String[] { null, "Wear", null, null, "Drop" };
			config.value = 120000;
			config.equipSlot = 1;
			config.model = 100000;
			config.maleEquip1 = 100000;
			config.femaleEquip1 = 100000;
			config.itemRequiriments = new HashMap<Integer, Integer>();
			config.itemRequiriments.put(Skills.ATTACK, 120);
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setSummoningDef(9);
			config.setPrayerBonus(4);
			break;
		case 25355:
			config.name = "Defence master cape";
			config.inventoryOptions = new String[] { null, "Wear", null, null, "Drop" };
			config.value = 120000;
			config.equipSlot = 1;
			config.model = 100001;
			config.maleEquip1 = 100001;
			config.femaleEquip1 = 100001;
			config.itemRequiriments = new HashMap<Integer, Integer>();
			config.itemRequiriments.put(Skills.DEFENCE, 120);
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setSummoningDef(9);
			config.setPrayerBonus(4);
			break;
		case 25356:
			config.name = "Strength master cape";
			config.inventoryOptions = new String[] { null, "Wear", null, null, "Drop" };
			config.value = 120000;
			config.equipSlot = 1;
			config.model = 100002;
			config.maleEquip1 = 100002;
			config.femaleEquip1 = 100002;
			config.itemRequiriments = new HashMap<Integer, Integer>();
			config.itemRequiriments.put(Skills.STRENGTH, 120);
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setSummoningDef(9);
			config.setPrayerBonus(4);
			break;
		case 25357:
			config.name = "Constitution master cape";
			config.inventoryOptions = new String[] { null, "Wear", null, null, "Drop" };
			config.value = 120000;
			config.equipSlot = 1;
			config.model = 1000003;
			config.maleEquip1 = 100003;
			config.femaleEquip1 = 100003;
			config.itemRequiriments = new HashMap<Integer, Integer>();
			config.itemRequiriments.put(Skills.HITPOINTS, 120);
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setSummoningDef(9);
			config.setPrayerBonus(4);
			break;
		case 25358:
			config.name = "Ranged master cape";
			config.inventoryOptions = new String[] { null, "Wear", null, null, "Drop" };
			config.value = 120000;
			config.equipSlot = 1;
			config.model = 100004;
			config.maleEquip1 = 100004;
			config.femaleEquip1 = 100004;
			config.itemRequiriments = new HashMap<Integer, Integer>();
			config.itemRequiriments.put(Skills.RANGE, 120);
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setSummoningDef(9);
			config.setPrayerBonus(4);
			break;
		case 25359:
			config.name = "Prayer master cape";
			config.inventoryOptions = new String[] { null, "Wear", null, null, "Drop" };
			config.value = 120000;
			config.equipSlot = 1;
			config.model = 100005;
			config.maleEquip1 = 100005;
			config.femaleEquip1 = 100005;
			config.itemRequiriments = new HashMap<Integer, Integer>();
			config.itemRequiriments.put(Skills.PRAYER, 120);
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setSummoningDef(9);
			config.setPrayerBonus(4);
			break;
		case 25360:
			config.name = "Magic master cape";
			config.inventoryOptions = new String[] { null, "Wear", null, null, "Drop" };
			config.value = 120000;
			config.equipSlot = 1;
			config.model = 100006;
			config.maleEquip1 = 100006;
			config.femaleEquip1 = 100006;
			config.itemRequiriments = new HashMap<Integer, Integer>();
			config.itemRequiriments.put(Skills.MAGIC, 120);
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setSummoningDef(9);
			config.setPrayerBonus(4);
			break;
		case 25361:
			config.name = "Cooking master cape";
			config.inventoryOptions = new String[] { null, "Wear", null, null, "Drop" };
			config.value = 120000;
			config.equipSlot = 1;
			config.model = 100007;
			config.maleEquip1 = 100007;
			config.femaleEquip1 = 100007;
			config.itemRequiriments = new HashMap<Integer, Integer>();
			config.itemRequiriments.put(Skills.COOKING, 120);
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setSummoningDef(9);
			config.setPrayerBonus(4);
			break;
		case 25362:
			config.name = "Woodcutting master cape";
			config.inventoryOptions = new String[] { null, "Wear", null, null, "Drop" };
			config.value = 120000;
			config.equipSlot = 1;
			config.model = 100008;
			config.maleEquip1 = 100008;
			config.femaleEquip1 = 100008;
			config.itemRequiriments = new HashMap<Integer, Integer>();
			config.itemRequiriments.put(Skills.WOODCUTTING, 120);
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setSummoningDef(9);
			config.setPrayerBonus(4);
			break;
		case 25363:
			config.name = "Fletching master cape";
			config.inventoryOptions = new String[] { null, "Wear", null, null, "Drop" };
			config.value = 120000;
			config.equipSlot = 1;
			config.model = 100009;
			config.maleEquip1 = 100009;
			config.femaleEquip1 = 100009;
			config.itemRequiriments = new HashMap<Integer, Integer>();
			config.itemRequiriments.put(Skills.FLETCHING, 120);
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setSummoningDef(9);
			config.setPrayerBonus(4);
			break;
		case 25364:
			config.name = "Fishing master cape";
			config.inventoryOptions = new String[] { null, "Wear", null, null, "Drop" };
			config.value = 120000;
			config.equipSlot = 1;
			config.model = 100010;
			config.maleEquip1 = 100010;
			config.femaleEquip1 = 100010;
			config.itemRequiriments = new HashMap<Integer, Integer>();
			config.itemRequiriments.put(Skills.FISHING, 120);
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setSummoningDef(9);
			config.setPrayerBonus(4);
			break;
		case 25365:
			config.name = "Firemaking master cape";
			config.inventoryOptions = new String[] { null, "Wear", null, null, "Drop" };
			config.value = 120000;
			config.equipSlot = 1;
			config.model = 100011;
			config.maleEquip1 = 100011;
			config.femaleEquip1 = 100011;
			config.itemRequiriments = new HashMap<Integer, Integer>();
			config.itemRequiriments.put(Skills.FIREMAKING, 120);
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setSummoningDef(9);
			config.setPrayerBonus(4);
			break;
		case 25366:
			config.name = "Crafting master cape";
			config.inventoryOptions = new String[] { null, "Wear", null, null, "Drop" };
			config.value = 120000;
			config.equipSlot = 1;
			config.model = 100012;
			config.maleEquip1 = 100012;
			config.femaleEquip1 = 100012;
			config.itemRequiriments = new HashMap<Integer, Integer>();
			config.itemRequiriments.put(Skills.CRAFTING, 120);
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setSummoningDef(9);
			config.setPrayerBonus(4);
			break;
		case 25367:
			config.name = "Smithing master cape";
			config.inventoryOptions = new String[] { null, "Wear", null, null, "Drop" };
			config.value = 120000;
			config.equipSlot = 1;
			config.model = 100013;
			config.maleEquip1 = 100013;
			config.femaleEquip1 = 100013;
			config.itemRequiriments = new HashMap<Integer, Integer>();
			config.itemRequiriments.put(Skills.SMITHING, 120);
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setSummoningDef(9);
			config.setPrayerBonus(4);
			break;
		case 25368:
			config.name = "Mining master cape";
			config.inventoryOptions = new String[] { null, "Wear", null, null, "Drop" };
			config.value = 120000;
			config.equipSlot = 1;
			config.model = 100014;
			config.maleEquip1 = 100014;
			config.femaleEquip1 = 100014;
			config.itemRequiriments = new HashMap<Integer, Integer>();
			config.itemRequiriments.put(Skills.MINING, 120);
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setSummoningDef(9);
			config.setPrayerBonus(4);
			break;
		case 25369:
			config.name = "Herblore master cape";
			config.inventoryOptions = new String[] { null, "Wear", null, null, "Drop" };
			config.value = 120000;
			config.equipSlot = 1;
			config.model = 100015;
			config.maleEquip1 = 100015;
			config.femaleEquip1 = 100015;
			config.itemRequiriments = new HashMap<Integer, Integer>();
			config.itemRequiriments.put(Skills.HERBLORE, 120);
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setSummoningDef(9);
			config.setPrayerBonus(4);
			break;
		case 25370:
			config.name = "Agility master cape";
			config.inventoryOptions = new String[] { null, "Wear", null, null, "Drop" };
			config.value = 120000;
			config.equipSlot = 1;
			config.model = 100016;
			config.maleEquip1 = 100016;
			config.femaleEquip1 = 100016;
			config.itemRequiriments = new HashMap<Integer, Integer>();
			config.itemRequiriments.put(Skills.AGILITY, 120);
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setSummoningDef(9);
			config.setPrayerBonus(4);
			break;
		case 25371:
			config.name = "Thieving master cape";
			config.inventoryOptions = new String[] { null, "Wear", null, null, "Drop" };
			config.value = 120000;
			config.equipSlot = 1;
			config.model = 100017;
			config.maleEquip1 = 100017;
			config.femaleEquip1 = 100017;
			config.itemRequiriments = new HashMap<Integer, Integer>();
			config.itemRequiriments.put(Skills.THIEVING, 120);
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setSummoningDef(9);
			config.setPrayerBonus(4);
			break;
		case 25372:
			config.name = "Slayer master cape";
			config.inventoryOptions = new String[] { null, "Wear", null, null, "Drop" };
			config.value = 120000;
			config.equipSlot = 1;
			config.model = 100018;
			config.maleEquip1 = 100018;
			config.femaleEquip1 = 100018;
			config.itemRequiriments = new HashMap<Integer, Integer>();
			config.itemRequiriments.put(Skills.SLAYER, 120);
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setSummoningDef(9);
			config.setPrayerBonus(4);
			break;
		case 25373:
			config.name = "Farming master cape";
			config.inventoryOptions = new String[] { null, "Wear", null, null, "Drop" };
			config.value = 120000;
			config.equipSlot = 1;
			config.model = 100018;
			config.maleEquip1 = 100019;
			config.femaleEquip1 = 100019;
			config.itemRequiriments = new HashMap<Integer, Integer>();
			config.itemRequiriments.put(Skills.FARMING, 120);
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setSummoningDef(9);
			config.setPrayerBonus(4);
			break;
		case 25374:
			config.name = "Runecrafting master cape";
			config.inventoryOptions = new String[] { null, "Wear", null, null, "Drop" };
			config.value = 120000;
			config.equipSlot = 1;
			config.model = 100020;
			config.maleEquip1 = 100020;
			config.femaleEquip1 = 100020;
			config.itemRequiriments = new HashMap<Integer, Integer>();
			config.itemRequiriments.put(Skills.RUNECRAFTING, 120);
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setSummoningDef(9);
			config.setPrayerBonus(4);
			break;
		case 25375:
			config.name = "Hunter master cape";
			config.inventoryOptions = new String[] { null, "Wear", null, null, "Drop" };
			config.value = 120000;
			config.equipSlot = 1;
			config.model = 100021;
			config.maleEquip1 = 100021;
			config.femaleEquip1 = 100021;
			config.itemRequiriments = new HashMap<Integer, Integer>();
			config.itemRequiriments.put(Skills.HUNTER, 120);
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setSummoningDef(9);
			config.setPrayerBonus(4);
			break;
		case 25376:
			config.name = "Construction master cape";
			config.inventoryOptions = new String[] { null, "Wear", null, null, "Drop" };
			config.value = 120000;
			config.equipSlot = 1;
			config.model = 100022;
			config.maleEquip1 = 100022;
			config.femaleEquip1 = 100022;
			config.itemRequiriments = new HashMap<Integer, Integer>();
			config.itemRequiriments.put(Skills.CONSTRUCTION, 120);
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setSummoningDef(9);
			config.setPrayerBonus(4);
			break;
		case 25377:
			config.name = "Summoning master cape";
			config.inventoryOptions = new String[] { null, "Wear", null, null, "Drop" };
			config.value = 120000;
			config.equipSlot = 1;
			config.model = 100023;
			config.maleEquip1 = 100023;
			config.femaleEquip1 = 100023;
			config.itemRequiriments = new HashMap<Integer, Integer>();
			config.itemRequiriments.put(Skills.SUMMONING, 120);
			config.setStabDef(9);
			config.setSlashDef(9);
			config.setCrushDef(9);
			config.setMagicDef(9);
			config.setRangeDef(9);
			config.setSummoningDef(9);
			config.setPrayerBonus(4);
			break;
		case 25378: // old kiln cape
			copy(ItemConfig.forID(23659), config);
			config.model = 100032;
			config.maleEquip1 = 100025;
			config.femaleEquip1 = 100026;
			break;
		case 25425: // saphire ticket
			copy(ItemConfig.forID(43190), config);
			config.name = "Donator ticket";
			config.inventoryOptions[4] = "Drop";
			config.tradeable = true;
			config.value = 5000000;
			break;
		case 25426: // emerald ticket
			copy(ItemConfig.forID(43190), config);
			config.name = "Super Donator ticket";
			config.inventoryOptions[4] = "Drop";
			config.tradeable = true;
			config.value = 10000000;
			break;
		case 25427: // ruby ticket
			copy(ItemConfig.forID(43190), config);
			config.name = "Extreme Donator ticket";
			config.inventoryOptions[4] = "Drop";
			config.tradeable = true;
			config.value = 20000000;
			break;
		case 25428: // diamond ticket
			copy(ItemConfig.forID(43190), config);
			config.name = "Legendary Donator ticket";
			config.inventoryOptions[4] = "Drop";
			config.tradeable = true;
			config.value = 40000000;
			break;
		case 25429: // onyx ticket
			copy(ItemConfig.forID(43190), config);
			config.name = "VIP Donator ticket";
			config.inventoryOptions[4] = "Drop";
			config.tradeable = true;
			config.value = 80000000;
			break;
		case 15403:
		case 23674:
		case 41862: // black phat
		case 20120: // frozen key
		case 15426:
		case 14595:
		case 14603:
		case 14602:
		case 14605:
		case 15422:
		case 15423:
		case 15425:
			config.inventoryOptions[4] = "Drop";
			config.tradeable = true;
			break;
		case 14596:
			config.clientScriptData = (HashMap<Integer, Object>) ItemConfig.forID(6585).clientScriptData.clone();
			config.inventoryOptions[4] = "Drop";
			config.tradeable = true;
			break;

		case 15600:
		case 15602:
		case 15604:
		case 15606:
		case 15608:
		case 15610:
		case 15612:
		case 15614:
		case 15616:
		case 15618:
		case 15620:
		case 15622:
		case 10547:
		case 10548:
		case 10549:
		case 10550:
		case 10551: // figher torso
		case 10552:
		case 10553:
		case 10555:
		case 41942: // ecu key
		case 51728: // granite balls
		case 21776: // armadyl shards
		case 20115:
		case 20116:
		case 20117:
		case 20118:
		case 20119:
		case 42785: // ring of wealth (i)
		case 1633: // crushed gem
		case 22423:
		case 22424:
		case 22425:
			config.tradeable = true;
			break;
		case 15098: // dice bag
			config.value = 10000000;
			config.tradeable = true;
			break;
		}

	}

	// new, old
	public static final int[] LUCKY_ITEMS = new int[] { 25379, 15486, // sol
			25380, 11235, // dbow
			25381, 4087, // dragon
			25382, 4585, 25383, 24365, 25384, 11732, 25385, 4708, // ahrim
			25386, 4710, 25387, 4712, 25388, 4714, 25389, 4716, // dharok
			25390, 4718, 25391, 4720, 25392, 4722, 25393, 4724, // guthan
			25394, 4726, 25395, 4728, 25396, 4730, 25397, 4732, // karil
			25398, 4734, 25399, 4736, 25400, 4738, 25401, 4745, // torag
			25402, 4747, 25403, 4749, 25404, 4751, 25405, 4753, // verac
			25406, 4755, 25407, 4757, 25408, 4759, 25409, 21736, // Akrisae
			25410, 21744, 25411, 21752, 25412, 21760, 25413, 25019, // bandos
			25414, 25022, 25415, 25025, 25416, 25010, // armadyl
			25417, 25013, 25418, 25016, 25419, 24992, // subjugation
			25420, 24995, 25421, 24998, 25422, 25001, 25423, 25004, 25424, 25007 };

	public static int getNonLuckyID(int id) {
		for (int i = 0; i < LUCKY_ITEMS.length; i += 2)
			if (LUCKY_ITEMS[i] == id)
				return LUCKY_ITEMS[i + 1];
			else if (id == 23679)
				id = 11694;
			else if (id == 23680)
				id = 11696;
			else if (id == 23681)
				id = 11698;
			else if (id == 23682)
				id = 11700;
			else if (id == 23683)
				id = 11716;
			else if (id == 23684)
				id = 11718;
			else if (id == 23685)
				id = 11720;
			else if (id == 23686)
				id = 11722;
			else if (id == 23687)
				id = 11724;
			else if (id == 23688)
				id = 11726;
			else if (id == 23689)
				id = 11728;
			else if (id == 23690)
				id = 11730;
			else if (id == 23691)
				id = 4151;
			else if (id == 23692)
				id = 11335;
			else if (id == 23693)
				id = 14479;
			else if (id == 23694)
				id = 3140;
			else if (id == 23695)
				id = 14484;
			else if (id == 23696)
				id = 7158;
			else if (id == 23697)
				id = 13738;
			else if (id == 23698)
				id = 13740;
			else if (id == 23699)
				id = 13742;
			else if (id == 23700)
				id = 13744;
		return id;
	}

	public static int getOldLookItem(int id) {
		for (int i = 0; i < LUCKY_ITEMS.length; i += 2)
			if (LUCKY_ITEMS[i] == id)
				return LUCKY_ITEMS[i + 1];

		if ((id >= 863 && id <= 876) || (id >= 5654 && id <= 5667))
			return id;
		if (id == 22448 || id == 301 || id == 3242 || id == 8855 || id == 4056 || id == 4057 || id == 4058
				|| id == 13242) // fix graphicsl bug
			return id;

		if (id == 25572) // old ultimate
			return 25571;
		if (id == 25566)// old ultimate
			return 25565;
		if (id == 25560)// old ultimate
			return 25559;

		if (id == 25505|| id == 25742) // old ultimate bandos
			return 25524;
		if (id == 25512 || id == 25749)// old ultimate armadyl
			return 25525;
		if (id == 23659) // old kiln
			return 25378;
		if (id == 24365) // old dragon kite
			return 51895;

		if (id == 25037)
			return 41785;
		if (id >= 1265 && id <= 1334)
			return id + Settings.OSRS_ITEM_OFFSET;

		if (id == 7158 || id == 1305 || id == 4587)
			return id + Settings.OSRS_ITEM_OFFSET;

		if (id < 22443)
			return id + Settings._685_ITEM_OFFSET;

		// lucky to non lucky

		else if (id == 23679)
			id = 11694;
		else if (id == 23680)
			id = 11696;
		else if (id == 23681)
			id = 11698;
		else if (id == 23682)
			id = 11700;
		else if (id == 23683)
			id = 11716;
		else if (id == 23684)
			id = 11718;
		else if (id == 23685)
			id = 11720;
		else if (id == 23686)
			id = 11722;
		else if (id == 23687)
			id = 11724;
		else if (id == 23688)
			id = 11726;
		else if (id == 23689)
			id = 11728;
		else if (id == 23690)
			id = 11730;
		else if (id == 23691)
			id = 4151;
		else if (id == 23692)
			id = 11335;
		else if (id == 23693)
			id = 14479;
		else if (id == 23694)
			id = 3140;
		else if (id == 23695)
			id = 14484;
		else if (id == 23696)
			id = 7158;
		else if (id == 23697)
			id = 13738;
		else if (id == 23698)
			id = 13740;
		else if (id == 23699)
			id = 13742;
		else if (id == 23700)
			id = 13744;
		return id;
	}

	public static void copy(int from, ItemConfig to) {
		copy(ItemConfig.forID(from), to);
	}

	@SuppressWarnings("unchecked")
	public static void copy(ItemConfig from, ItemConfig to) {
		to.name = from.name;
		to.inventoryOptions = from.inventoryOptions.clone();
		to.equipSlot = from.equipSlot;
		to.equipType = from.equipType;
		to.model = from.model;
		to.maleEquip1 = from.maleEquip1;
		to.femaleEquip1 = from.femaleEquip1;
		to.maleEquip2 = from.maleEquip2;
		to.femaleEquip2 = from.femaleEquip2;
		to.scale = from.scale;
		to.roll = from.roll;
		to.pitch = from.pitch;
		to.scaleX = from.scaleX;
		to.scaleY = from.scaleY;
		to.scaleZ = from.scaleZ;
		to.shadow = from.shadow;
		to.lightness = from.lightness;
		if (from.clientScriptData != null)
			to.clientScriptData = (HashMap<Integer, Object>) from.clientScriptData.clone();
		to.value = from.value;
		to.stackable = from.stackable;
		to.setHP(from.getHP());
	}
}
