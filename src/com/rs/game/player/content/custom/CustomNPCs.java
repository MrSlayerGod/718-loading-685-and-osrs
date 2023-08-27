package com.rs.game.player.content.custom;
/**
 * 
 */

import com.rs.cache.loaders.NPCConfig;
import com.rs.game.npc.NPC;

/**
 * @author dragonkk(Alex)
 * Oct 18, 2017
 */
public class CustomNPCs {

	
	public static int getAnim(int npcID, int animID) {
		if (npcID == 50) {
			if (animID == 17780)
				return 92;
			if (animID == 17779)
				return 89;
			if (animID == 17782)
				return 91;
			
			if (animID == 17784 || animID == 17783 || animID == 17785 || animID == 17786)
				return 81;
		}
		
		if (npcID == 6260) {
			if (animID == 17389)
				return 7060;
			if (animID == 17390)
				return 7061;
			if (animID == 17391)
				return 7063;
			if (animID == 17392)
				return 7062;
		}
		if (npcID == 6261 || npcID == 6263 || npcID == 6265) {
			if (animID == 6185)
				return 6154;
			if (animID == 6189)
				return 6155;
			if (animID == 6182)
				return 6156;
		}
		
		
		
		if (npcID == 6247) {
			if (animID == 6964)
				return 26967;
			if (animID == 6966)
				return 26969;
			if (animID == 6965)
				return 26968;
			if (animID == 6967)
				return 26970;
		}
		if (npcID == 6252) {
			if (animID == 17486)
				return 7009;
			if (animID == 17487)
				return 7010;
			if (animID == 17488)
				return 7011;
		}
		if (npcID == 6250) {
			if (animID == 17420)
				return 7019;
			if (animID == 17418)
				return 7017;
			if (animID == 17421)
				return 7016;
		}
		
		if (npcID == 6248) {
			if (animID == 17499)
				return 6376;
			if (animID == 17498)
				return 6375;
			if (animID == 17496)
				return 6377;
		}
		
		if (npcID == 6203) {
			if (animID == 14963)
				return 26948;
			if (animID == 14965)
				return 26947;
			if (animID == 14961)
				return 26949;
			if (animID == 14384)
				return 26950;
		}
		
		if (npcID == 6204) {
			if (animID == 17463)
				return 64;
			if (animID == 17465)
				return 65;
			if (animID == 17464)
				return 67;
		}
		
		
		if (npcID == 6206) {
			if (animID == 17439)
				return 6945;
			if (animID == 17440)
				return 65;
			if (animID == 17441)
				return 67;
		}
		
		if (npcID == 6208) {
			if (animID == 17459)
				return 7033;
			if (animID == 17460)
				return 65;
			if (animID == 17461)
				return 67;
		}


		if (npcID == 6222) {
			if (animID == 17396)
				return 6977;
			if (animID == 17395)
				return 6974;
			if (animID == 17398)
				return 6975;
			if (animID == 17397)
				return 6976;
		}
		
		if (npcID == 6223) {
			if (animID == 17447)
				return 6952;
			if (animID == 17448)
				return 6955;
			if (animID == 17449)
				return 6956;
		}
		
		if (npcID == 6225) {
			if (animID == 17446)
				return 6954;
			if (animID == 17448)
				return 6955;
			if (animID == 17450)
				return 6956;
		}
		if (npcID == 6227) {
			if (animID == 17443)
				return 6953;
			if (animID == 17448)
				return 6955;
			if (animID == 17451)
				return 6956;
		}
		
		return animID;
	}
	
	public static int getGFX(int npcID, int gfxID) {
		if (npcID == 50) {
			if (gfxID == 3433)
				return 396;
			if (gfxID == 3439)
				return 395;
			if (gfxID == 3436)
				return 394;
			if (gfxID == 3442)
				return 393;
			if (gfxID == 3441 || gfxID == 3443
					|| gfxID == 3435 || gfxID == 3437
					|| gfxID == 3438 || gfxID == 3440
					|| gfxID == 3432 || gfxID == 3434)
				return -1;
		}
		if (npcID == 6208 && gfxID == 3389) 
			return -1;
		
		if (npcID == 6222) {
			if (gfxID == 3349)
				return 1197;
			if (gfxID == 3388)
				return 1196;
		}
		
		if (npcID == 6225 && gfxID == 3366)
			return -1;
		
		return   gfxID;
	}

	public static final int[] COX_BOSSES = {
			27540,
			27541,
			27542,
			27543,
			27544,
			27545,
			27566,
			27568,
			27567,
			27530,
			27532,
			27539,
			27538,
			27533,
			27525,
			27526,
			27527,
			27528,
			27562,
			27561,
			27563,
			27553,
			27554,
			27555,
			27552,
			27550,
			27576,
			27577,
			27578,
			27579,
			27548,
			27549,
			27569,
			27570,
			27584,
			27585,
			27573,
			27604,
			27605,
			27606,
			27559,
			27560};
	
	public static void modify(NPCConfig config) {
		int id = config.id;
		if ((id >= 6565 && id <= 6603) || (id >= 13296 && id <= 13299)) 
			config.actions[4] = "Loot-all";
		if(id >= 28196 && id <= 28205) {
			config.actions = new String[]{"Talk-to", null, "Pick-up", "Metamorphosis", null, "Examine"};
		}

		for(int i : COX_BOSSES) {
			if(i == id) {
				config.combatLevel = 500;
			}
		}

		if (config.hasOption("bank") && config.hasOption("collect")) {
			config.actions[4] = "Clan-Bank";
		}

		switch(id) {
			case 21230: //giant mimic
				copy(28633, config);
				config.boundSize = 2;
				config.combatLevel = 1860;
				config.name = "Giant Mimic";
				break;
		case 21229:
			copy(28387, config);
			config.name = "Tommy";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			break;
		case 21224:
			copy(9181, config);
			config.name = "Horrific right arm";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			break;
		case 21225:
			copy(9182, config);
			config.name = "Horrific left arm";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			break;
		case 21226:
			copy(9183, config);
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			config.name = "Horrific tail";
			break;
		case 7711: // somebody changed this to 'cerberis' -- priest in peril guardian dog
			config.name = "Temple guardian";
			break;
		case 21223:
			config.name = "Wise old man (Zipfer)";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			break;
		case 21227:
			config.name = "Bl0aty";
			config.standAnimation = 28082;
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			break;
		case 21220:
			copy(27520, config);
			config.name = "Twisted olmlet";
			break;
		case 21221:
			copy(27520, config);
			config.name = "Elder olmlet";
			break;
		case 21222:
			copy(27520, config);
			config.name = "Ancestral olmlet";
			break;
		case 9085:
			config.actions[1] = "Get-boss-task";
			config.movementCapabilities = 0;
			break;
		case 6537:
		case 943:
		case 2253:
			config.actions[2] = "Trade";
			break;
		case 28058: //vorkhat
			config.contrast = 1 >> 32;
			config.respawnDirection = 0; //south
			break;
		case 2021:  //light creature
			config.movementCapabilities = (byte) (NPC.NORMAL_WALK | NPC.FLY_WALK);
			break;
		case 2114:
			config.movementCapabilities = (byte) (NPC.NORMAL_WALK);
			break;
		case 28623:
		case 8480: //slayer npc
		case 28491:
			config.movementCapabilities = 0;
			break;
			case 3050:
			case 27690:
				config.contrast = 1 >> 32;
				config.respawnDirection = 8; // north
				config.movementCapabilities = 0;
				break;
		case 2024:
		case 1835:
		case 1918:
		case 3709:
			config.movementCapabilities = 0;
			config.actions[2] = "Trade";
			break;
	/*	case 3705:
		case 2241:
		case 2242:
			config.contrast = 1 >> 32;
			config.respawnDirection = 8; // north
			config.movementCapabilities = 0;
			break;*/
		case 1694:
			copy(7746, config);
			config.name = "Voting guide";
			config.actions[2] = "Trade";
			config.contrast = 1 >> 32;
			config.respawnDirection = 2; //east
			config.movementCapabilities = 0;
			break;
		case 3705:
			config.actions[2] = "Trade";
			config.contrast = 1 >> 32;
			config.respawnDirection = 2; //east
			config.movementCapabilities = 0;
			break;
		case 954:
			config.name = "Loyal Dan";
			config.actions[2] = "Trade";
			config.movementCapabilities = 0;

			config.contrast = 1 >> 32;
			config.respawnDirection = 2; //east
			break;
		case 949:
			config.name = "Skilling guide";
			config.actions[2] = "Trade";
			config.movementCapabilities = 0;
			
			config.contrast = 1 >> 32;
			config.respawnDirection = 2; //east
			break;
		case 944: //starter guide
			config.name = "Gear guide";
			config.actions[2] = "Trade";
			config.movementCapabilities = 0;
			config.contrast = 1 >> 32;
			config.respawnDirection = 2; //east
			break;
		case 16006:
			copy(1303, config);
			config.name = "Fremennik guide";
			config.movementCapabilities = 0;
			config.contrast = 1 >> 32;
			config.respawnDirection = 2; //east
			break;
		case 7969:
			break;
		case 945: //starter guide
			config.name = "Pure guide";
			config.actions[2] = "Trade";
			config.movementCapabilities = 0;
			
			config.contrast = 1 >> 32;
			config.respawnDirection = 2; //east
			break;
		case 13727:
			config.actions[2] = "Trade";
			config.actions[3] = null;
			config.movementCapabilities = 0;
			config.contrast = 1 >> 32;
			config.respawnDirection = 2; //east
			break;
		case 15151:
		case 15147:
		//case 3463:
			config.contrast = 1 >> 32;
			config.respawnDirection = 6; //west
			break;
		case 946:
			config.name = "Matrix guide";
			config.actions[2] = "Trade";
			config.actions[3] = "Teleport";
			config.contrast = 1 >> 32;
			config.respawnDirection = 2; //east
			config.movementCapabilities = 0;
			break;
		case 947:
			config.name = "Skilling secondaries guide";
			config.contrast = 1 >> 32;
			config.respawnDirection = 6; //west
			config.movementCapabilities = 0;
			break;
		case 3811:
		case 2620:
		case 456:
		case 1040:
			config.contrast = 1 >> 32;
			config.respawnDirection = 8; //north
			config.movementCapabilities = 0;
			break;
			//16k start custom
		case 16000:
			copy(22668, config);
			config.actions[0] = config.actions[2] = config.actions[3] =  config.actions[4] = null;
			break;
		case 16001:
			copy(22668, config);
			config.name = "DZ Combat Dummy";
			config.actions[0] = config.actions[2] = config.actions[3] =  config.actions[4] = null;
			break;
		case 16008:
			copy(22668, config);
			config.name = "Maxhit Combat Dummy";
			config.combatLevel = 1000;
			config.actions[0] = config.actions[2] = config.actions[3] =  config.actions[4] = null;
			break;
		case 16002:
			copy(15211, config);
			config.name = "Shrimpy";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			config.boundSize = 2;
			config.combatLevel = 0;
			break;
		case 1553:
			copy(1552, config);
			config.name = "Infernal Santa";
			config.actions = new String[]{null, "Attack", null, null, null, "Examine"};
			config.combatLevel = 5;
			config.boundSize = 2;
			config.drawMapdot = true;
			break;
		case 21993:
			copy(89, config);
			config.name = "Infernal unicorn";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			config.drawMapdot = false;
			break;
		case 21992:
			copy(89, config);
			config.name = "Shadow Callus";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			config.drawMapdot = false;
			break;
		case 21997:
			copy(8536, config);
			config.name = "Infernal imp pet";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			config.drawMapdot = false;
			break;
		case 16003:
			copy(13447, config);
			config.name = "Nexterminator";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			config.drawMapdot = false;
			break;
		case 16004:
			copy(15454, config);
			config.name = "Queen Black Dragonling";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			config.boundSize = 2;
			config.combatLevel = 0;
			break;
		case 16005:
			copy(28097, config);
			config.name = "Galvek pet";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			break;
		case 16027:
			copy(16025, config);
			config.name = "Wolpertinger pet";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			break;
		case 16007:
			copy(956, config);
			config.name = "Drunken Dwarf pet";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			break;
		case 16009:
			copy(27315, config);
			config.name = "Coins pet";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			break;
		case 16010:
			copy(2025, config);
			config.name = "Ahrim pet";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			break;
		case 16011:
			copy(2026, config);
			config.name = "Dharok pet";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			break;
		case 16012:
			copy(2027, config);
			config.name = "Guthan pet";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			break;
		case 16013:
			copy(2028, config);
			config.name = "Karil pet";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			break;
		case 16014:
			copy(2029, config);
			config.name = "Torag pet";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			break;
		case 16015:
			copy(2030, config);
			config.name = "Verac pet";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			break;
		case 16016:
			copy(15186, config);
			config.name = "Onyx pet";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			break;
		case 16017:
			copy(15184, config);
			config.name = "Onyx pet";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			break;
		case 16018:
			copy(648, config);
			config.name = "Dicing King";
			config.actions = new String[]{"Gamble-with", null, null, null, null, "Examine"};
			config.contrast = 1 >> 32;
			config.respawnDirection = 8; // north
			config.movementCapabilities = 0;
			break;
		case 16019:
			copy(8528, config);
			config.name = "Nomad pet";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			break;
		case 16020:
			copy(7585, config);
			config.name = "Donation guide";
			config.actions[0] = "Talk-to";
			config.actions[2] = "Trade";
			config.contrast = 1 >> 32;
			config.respawnDirection = 2; //east
			break;
		case 16021:
			copy(28387, config);
			config.name = "Sotetseg pet";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			break;
		case 16022:
			copy(8597, config);
			config.name = "Avatar of Creation pet";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			break;
		case 16023:
			copy(15185, config);
			config.name = "Onyx pet";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			break;
		case 16024:
			copy(22668, config);
			config.name = "Combat dummy pet";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			break;
		case 16025:
			copy(6990, config);
			config.name = "Corrupted Wolpertinger";
			config.actions = new String[]{null, "Attack", null, null, null, "Examine"};
			config.combatLevel = 1900;
			config.boundSize = config.boundSize * 4;
			config.drawMapdot = true;
			config.movementCapabilities = (byte) (NPC.NORMAL_WALK);
			break;
		case 16026:
			copy(6990, config);
			config.name = "Dark Wolpertinger";
			config.actions = new String[]{null, "Attack", null, null, null, "Examine"};
			config.combatLevel = 475;
			config.boundSize = config.boundSize * 2;
			config.drawMapdot = true;
			config.movementCapabilities = (byte) (NPC.NORMAL_WALK);
			break;
		case 16032:
			copy(6743, config);
			config.name = "Evil Snowman";
			config.combatLevel = 10000;
			config.actions = new String[]{null, "Attack", null, null, null, "Examine"};
			config.boundSize = 4;
			config.drawMapdot = true;
			break;
		case 16033:
			copy(15147, config);
			config.name = "LolthenKILL pet";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			break;
		case 16034:
			copy(4397, config);
			config.name = "Catablepon pet";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			break;
		case 16035:
			copy(3077, config);
			config.name = "Dead Monk pet";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			break;
		case 16031:
			copy(12379, config);
			config.combatLevel = 10000;
			config.actions = new String[]{null, "Attack", null, null, null, "Examine"};
			config.boundSize = 3;
			config.drawMapdot = true;
			break;
		case 16030:
			copy(24724, config);
			config.name = "The Horde Slave";
			config.boundSize = 1;
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			break;
		case 16028:
			copy(409, config);
			config.name = "Genie pet";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			break;
		case 21950:
			copy(409, config);
			config.name = "Covid";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			break;
		case 21951:
			copy(653, config);
			config.name = "Pet fairy queen";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			break;
		case 21952:
			copy(27881, config);
			config.name = "Lil'smokey";
			config.actions = new String[]{"Talk-to", null, "Pick-up", "Metamorphosis", null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			break;
		case 21930:
			copy(26593, config);
			config.name = "Baby lava dragon";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			break;
		case 21931:
			copy(28633, config);
			config.name = "Mimi";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			break;
		case 21932:
		case 21939:
		case 21940:
			copy(28633, config);
			config.name = "Superior slayer pet";
			config.actions = new String[]{"Talk-to", null, "Pick-up", "Metamorphosis", null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			break;
		case 21943:
			copy(28633, config);
			config.name = "September top donator pet";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			break;
		case 21944:
			copy(14387, config);
			config.name = "Death Jr.";
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			config.boundSize = 2;
			config.combatLevel = 0;
			break;
		case 21945:
		case 21991:
		case 21947:
			copy(25491, config);
			config.name = "Lucky's Balloon Boi";
			config.actions = new String[]{"Talk-to", null, "Pick-up", "Metamorphosis", null, "Examine"};
			//config.boundSize = 2;
			config.combatLevel = 0;
			break;
		case 21948:
		case 21949:
		case 21990:
			copy(1, config);
			config.name = "Flowers";
			config.animateIdle = true;
			config.walkAnimation = config.standAnimation = 912;
			config.actions = new String[]{"Talk-to", null, "Pick-up", "Metamorphosis", null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			break;

			case 21941:
			copy(27881, config);
			config.name = "Lil'hazey";
			config.actions = new String[]{"Talk-to", null, "Pick-up", "Metamorphosis", null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			break;
		case 21927:
		case 21928:
		case 21929:
			copy(14304 - (21929 - id), config);
			config.name = "Mini callus";
			config.actions = new String[]{"Talk-to", null, "Pick-up", "Metamorphosis", null, "Examine"};
			config.boundSize = 1;
			config.combatLevel = 0;
			break;
		case 16029:
			config.name = "Twisted bow pet";
			config.combatLevel = 0;
			config.drawMapdot = false;
			config.actions = new String[]{"Talk-to", null, "Pick-up", null, null, "Examine"};
			break;
		case 15186:
		case 15185:
		case 15184:
			config.name = "Matrix";
			config.boundSize *= 2;
			break;
		case 21212:
			copy(/*14301*/14304, config);
			config.name = "Callus Frostborne";
			config.boundSize *= 3;
			break;
		case 21200:
			copy(14304, config);
			config.name = "Callus (Enduring)";
			config.boundSize *= 3;
			break;
		case 21201:
			copy(14303, config);
			config.name = "Callus (Sapping)";
			config.boundSize *= 3;
			break;
		case 21202:
			copy(14302, config);
			config.name = "Callus (Unstable)";
			config.boundSize *= 3;
			break;
		case 21203:
			copy(87, config);
			config.name = "Brazier (unlit)";
			config.boundSize *= 2;
			break;
		case 21204:
			copy(87, config);
			config.name = "Brazier (lit)";
			config.boundSize *= 2;
			break;
		}
	}
	
	public static void copy(int id, NPCConfig to) {
		copy(NPCConfig.forID(id), to);
	}
		
		
	
	public static void copy(NPCConfig from, NPCConfig to) {
		to.name = from.name;
		to.actions = from.actions.clone();
		to.models = from.models;
		to.headModels = from.headModels;
		to.clientScriptData = from.clientScriptData;
		to.drawMapdot = from.drawMapdot;
		to.standAnimation = from.standAnimation;
		to.walkAnimation = from.walkAnimation;
		to.boundSize = from.boundSize;
		to.combatLevel = from.combatLevel;
		to.renderEmote = from.renderEmote;
	}
}
