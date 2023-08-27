package com.rs.game.player.content;

import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Utils;

public class PlayerExamine {
	
	public static void examine(Player player, Player target) {
		player.getInterfaceManager().sendInterface(1314);
		player.getPackets().sendIComponentText(1314, 91, target.getName());
		player.getPackets().sendIComponentText(1314, 90, target.getGameMode());
		player.getPackets().sendIComponentText(1314, 30, "Play time");
		player.getPackets().sendIComponentText(1314, 60, Utils.longFormatS(target.getTotalOnlineTime()).toLowerCase());
		player.getPackets().sendIComponentText(1314, 86, "" +target.getSkills().getCombatLevelWithSummoning());
		player.getPackets().sendIComponentText(1314, 87, "" +(target.getHitpoints()));
		player.getPackets().sendIComponentText(1314, 61, ""+target.getSkills().getLevelForXp(Skills.ATTACK, !player.isVirtualLevels() ? 99 : 120));
		player.getPackets().sendIComponentText(1314, 62, ""+target.getSkills().getLevelForXp(Skills.STRENGTH, !player.isVirtualLevels() ? 99 : 120));
		player.getPackets().sendIComponentText(1314, 63, ""+target.getSkills().getLevelForXp(Skills.DEFENCE, !player.isVirtualLevels() ? 99 : 120));
		player.getPackets().sendIComponentText(1314, 65, ""+target.getSkills().getLevelForXp(Skills.RANGE, !player.isVirtualLevels() ? 99 : 120));
		player.getPackets().sendIComponentText(1314, 66, ""+target.getSkills().getLevelForXp(Skills.PRAYER, !player.isVirtualLevels() ? 99 : 120));
		player.getPackets().sendIComponentText(1314, 64, ""+target.getSkills().getLevelForXp(Skills.MAGIC, !player.isVirtualLevels() ? 99 : 120));
		player.getPackets().sendIComponentText(1314, 78, ""+target.getSkills().getLevelForXp(Skills.RUNECRAFTING, !player.isVirtualLevels() ? 99 : 120));
		player.getPackets().sendIComponentText(1314, 81, ""+target.getSkills().getLevelForXp(Skills.CONSTRUCTION, !player.isVirtualLevels() ? 99 : 120));
		player.getPackets().sendIComponentText(1314, 76, ""+target.getSkills().getLevelForXp(Skills.DUNGEONEERING, !player.isVirtualLevels() ? 99 : 120));
		
		player.getPackets().sendIComponentText(1314, 82, ""+target.getSkills().getLevelForXp(Skills.HITPOINTS, !player.isVirtualLevels() ? 99 : 120));
		player.getPackets().sendIComponentText(1314, 83, ""+target.getSkills().getLevelForXp(Skills.AGILITY, !player.isVirtualLevels() ? 99 : 120));
		player.getPackets().sendIComponentText(1314, 84, ""+target.getSkills().getLevelForXp(Skills.HERBLORE, !player.isVirtualLevels() ? 99 : 120));
		player.getPackets().sendIComponentText(1314, 80, ""+target.getSkills().getLevelForXp(Skills.THIEVING, !player.isVirtualLevels() ? 99 : 120));
		player.getPackets().sendIComponentText(1314, 70, ""+target.getSkills().getLevelForXp(Skills.CRAFTING, !player.isVirtualLevels() ? 99 : 120));
		player.getPackets().sendIComponentText(1314, 85, ""+target.getSkills().getLevelForXp(Skills.FLETCHING, !player.isVirtualLevels() ? 99 : 120));
		player.getPackets().sendIComponentText(1314, 77, ""+target.getSkills().getLevelForXp(Skills.SLAYER, !player.isVirtualLevels() ? 99 : 120));
		player.getPackets().sendIComponentText(1314, 79, ""+target.getSkills().getLevelForXp(Skills.HUNTER, !player.isVirtualLevels() ? 99 : 120));

		player.getPackets().sendIComponentText(1314, 68, ""+target.getSkills().getLevelForXp(Skills.MINING, !player.isVirtualLevels() ? 99 : 120));
		player.getPackets().sendIComponentText(1314, 69, ""+target.getSkills().getLevelForXp(Skills.SMITHING, !player.isVirtualLevels() ? 99 : 120));
		player.getPackets().sendIComponentText(1314, 74, ""+target.getSkills().getLevelForXp(Skills.FISHING, !player.isVirtualLevels() ? 99 : 120));
		player.getPackets().sendIComponentText(1314, 75, ""+target.getSkills().getLevelForXp(Skills.COOKING, !player.isVirtualLevels() ? 99 : 120));
		player.getPackets().sendIComponentText(1314, 73, ""+target.getSkills().getLevelForXp(Skills.FIREMAKING, !player.isVirtualLevels() ? 99 : 120));
		player.getPackets().sendIComponentText(1314, 71, ""+target.getSkills().getLevelForXp(Skills.WOODCUTTING, !player.isVirtualLevels() ? 99 : 120));
		player.getPackets().sendIComponentText(1314, 72, ""+target.getSkills().getLevelForXp(Skills.FARMING, !player.isVirtualLevels() ? 99 : 120));
		player.getPackets().sendIComponentText(1314, 67, ""+target.getSkills().getLevelForXp(Skills.SUMMONING, !player.isVirtualLevels() ? 99 : 120));

		player.getPackets().sendIComponentText(1314, 88, ""+target.getSkills().getTotalLevel(!player.isVirtualLevels() ? 99 : 120));
		player.getPackets().sendIComponentText(1314, 89, ""+Utils.getFormattedNumber(target.getSkills().getTotalXp()));

		
	}

}
