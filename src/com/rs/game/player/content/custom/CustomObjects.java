/**
 * 
 */
package com.rs.game.player.content.custom;

import com.rs.cache.loaders.ObjectConfig;

/**
 * @author dragonkk(Alex)
 * Oct 30, 2017
 */
public class CustomObjects {


	public static void modify(ObjectConfig config) {
		int id = config.id;

		if(config.name.equals("Bank booth") || config.name.equals("Bank chest") || config.name.equals("Bank table")) {
			config.options[3] = "Last-preset";
			config.options[4] = "Clan-Bank";
		}

		switch(config.name.toLowerCase()) {
		case "range":
		case "cooking range":
		case "stove":
		case "clay oven":
			if (config.options[0] == null)
				config.options[0] = "Cook";
			break;
		}
		switch(id) {
		case 133307:
			config.name = "Ominous shrine";
			config.options[1] = "Inspect";
			config.options[0] = "Offer";
			break;
		case 107127:
			config.name = "Achievement Cup";
			break;
		case 126727:
			config.name = "Crucible of Bloodlust";
			config.options[0] = "Enter";
			break;
		case 56907:
			copy(28296, config);
			config.cliped = 0;
			config.solid = false;
			break;
		case 26074:
			config.options[0] = "Sit";
			break;
		case 172: //crystal chest
			config.name = "Crystal chest";
			break;
		case 134662: //konar chest
			config.osrsVarEnabled = true;
			break;
		case 127114: //allotment
		case 127113:
		case 133693:
		case 133694:
		case 134922:
		case 134921:
			config.osrsVarEnabled = true;
			config.toObjectIds  = ObjectConfig.forID(8550).toObjectIds;
			break;
		case 127111: //flower
		case 133649:
		case 134919:
			config.osrsVarEnabled = true;
			config.toObjectIds  = ObjectConfig.forID(7847).toObjectIds;
			break;
		case 127115: //herb
		case 133979:
			config.osrsVarEnabled = true;
			config.toObjectIds  = ObjectConfig.forID(8150).toObjectIds;
			break;
		case 134006: //bushes
			config.osrsVarEnabled = true;
			config.toObjectIds  = ObjectConfig.forID(7577).toObjectIds;
			break;
		case 133761: //cactus
			config.osrsVarEnabled = true;
			config.toObjectIds  = ObjectConfig.forID(7771).toObjectIds;
			break;
		case 133732: //tree
			config.osrsVarEnabled = true;
			config.toObjectIds  = ObjectConfig.forID(8388).toObjectIds;
			break;
		case 134007: //fruit tree
			config.osrsVarEnabled = true;
			config.toObjectIds  = ObjectConfig.forID(7962).toObjectIds;
			break;
		case 107453:
		case 107484:
		case 110943:
		case 111161:
			config.name = "Copper ore rocks";
			break;
		case 107454:
		case 107487:
		case 111362:
		case 111363:
		case 136210:
			config.name = "Clay rocks";
			break;
		case 107455:
		case 107488:
		case 111364:
		case 111365:
		case 136203:
			config.name = "Iron ore rocks";
			break;
		case 107456:
		case 107489:
		case 111366:
		case 111367:
		case 136204:
			config.name = "Coal rocks";
			break;
		case 107457:
		case 107485:
		case 107486:
		case 107490:
		case 111360:
		case 111361:
		case 136202:
			config.name = "Tin ore rocks";
			break;
		case 111368:
		case 111369:
		case 136205:
			config.name = "Silver ore rocks";
			break;
		case 107458:
		case 107491:
		case 111370:
		case 111371:
		case 136206:
			config.name = "Gold ore rocks";
			break;
		case 107459:
		case 107492:
		case 111372:
		case 111373:
		case 136207:
			config.name = "Mithril ore rocks";
			break;
		case 107460:
		case 107493:
		case 111374:
		case 111375:
		case 136208:
			config.name = "Adamantite ore rocks";
			break;
		case 107461:
		case 107494:
		case 111376:
		case 111377:
		case 136209:
			config.name = "Runite ore rocks";
			break;
		case 107462:
		case 107495:
		case 111378:
		case 111379:
			config.name = "Blurite ore rocks";
			break;
		case 107463:
		case 107464:
		case 111380:
		case 111381:
			config.name = "Gem rocks";
			break;
		case 103641:
		case 26945:
			config.options[0] = "Make-wish";
			break;
			case 54019:
			case 54020:
				config.name = "Hiscores";
				break;
		case 65458:
		case 65459:
			config.options[0] = "Enter";
			config.name = "Cavern";
			break;
		case 59941:
			config.options[0] = "Enter";
			break;
		case 78322: //custom
			copy(9270, config);
			config.name = "Overload table";
			config.options[0] = "Take";
			break;
		case 78323: //custom
			copy(4875, config);
			config.name = "Rocktail table";
			config.options[0] = "Take";
			break;
		case 78324: //custom
			copy(34385, config);
			config.name = "Sapphire stall";
			break;
		case 78325: //custom
			copy(34385, config);
			config.name = "Emerald stall";
			break;
		case 78326: //custom
			copy(34385, config);
			config.name = "Ruby stall";
			break;
		case 78327: //custom
			copy(34385, config);
			config.name = "Diamond stall";
			break;
		case 78328: //custom
			copy(34385, config);
			config.name = "Onyx stall";
			break;
		case 78329: //custom
			copy(132990, config);
			config.name = "<col=ffff00>Pandora Chest";
			config.options[1] = "Info";
			break;
		case 78330: //custom
			copy(34385, config);
			config.name = "Zenyte stall";
			break;
		case 129937:
			config.name = "Matrix's statue";
			break;
		case 78331: //custom
		case 133114:
			copy(59731, config);
			config.name = "Upgrade chest";
			config.options[0] = "Info";
			config.options[1] = "Upgrade";
			break;
			case 99999:
				copy(55992, config);
				config.name = "Sacrifice Altar";
				config.options[0] = "Sacrifice";
				config.options[1] = null;
				break;
		}
	}
	
	public static void copy(int id, ObjectConfig to) {
		copy(ObjectConfig.forID(id), to);
	}
	
	public static void copy(ObjectConfig from, ObjectConfig to) {
		to.name = from.name;
		to.sizeX = from.sizeX;
		to.sizeY = from.sizeY;
		to.modelIDs = from.modelIDs;
		to.modelTypes = from.modelTypes;
		to.cliped = from.cliped;
		to.ignoreClipOnAlternativeRoute = from.ignoreClipOnAlternativeRoute;
		to.solid = from.solid;
		to.animation = from.animation;
		to.acessBlockFlag = from.acessBlockFlag;
		to.options = from.options.clone();
		to.optionType = from.optionType;
	}
}
