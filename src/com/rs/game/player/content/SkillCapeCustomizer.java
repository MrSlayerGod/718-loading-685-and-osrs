package com.rs.game.player.content;

import java.util.Arrays;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.player.Player;

public final class SkillCapeCustomizer {

	private SkillCapeCustomizer() {

	}

	public static void resetSkillCapes(Player player) {
		player.setMaxedCapeCustomized(Arrays.copyOf(ItemConfig.forID(20767).originalModelColors, 4));
		player.setCompletionistCapeCustomized(Arrays.copyOf(ItemConfig.forID(20769).originalModelColors, 4));
	}

	public static void startCustomizing(Player player, int itemId) {
		player.getTemporaryAttributtes().put("SkillcapeCustomizeId", itemId);
		int[] skillCape = itemId == 32151 || itemId == 20767 ? player.getMaxedCapeCustomized() : player.getCompletionistCapeCustomized();
		player.getInterfaceManager().sendInterface(20);
		for (int i = 0; i < 4; i++)
			player.getVarsManager().sendVarBit(9254 + i, skillCape[i]);
		player.getPackets().sendIComponentModel(20, 55, player.getAppearence().isMale() ? ItemConfig.forID(itemId).getMaleWornModelId1() : ItemConfig.forID(itemId).getFemaleWornModelId1());
	}

	public static void costumeColorCustomize(Player player) {
		player.getTemporaryAttributtes().put(Key.COSTUME_COLOR_CUSTOMIZE, Boolean.TRUE);
		player.getInterfaceManager().sendInterface(19);
		player.getVarsManager().sendVar(2174, player.getEquipment().getCostumeColor());
	}
	
	public static void handleCostumeColor(Player player, int color) {
		player.closeInterfaces();
		player.getEquipment().setCostumeColor(color);
	}
	
	public static int getCapeId(Player player) {
		Integer id = (Integer) player.getTemporaryAttributtes().get("SkillcapeCustomizeId");
		if (id == null)
			return -1;
		return id;
	}

	public static void handleSkillCapeCustomizerColor(Player player, int colorId) {
		int capeId = getCapeId(player);
		if (capeId == -1)
			return;
		Integer part = (Integer) player.getTemporaryAttributtes().get("SkillcapeCustomize");
		if (part == null)
			return;
		int[] skillCape = capeId == 32151 || capeId == 20767 ? player.getMaxedCapeCustomized() : player.getCompletionistCapeCustomized();
		skillCape[part] = colorId;
		player.getVarsManager().sendVarBit(9254 + part, colorId);
		player.getInterfaceManager().sendInterface(20);
	}

	public static void handleSkillCapeCustomizer(Player player, int buttonId) {
		int capeId = getCapeId(player);
		if (capeId == -1)
			return;
		int[] skillCape = capeId == 32151 || capeId == 20767 ? player.getMaxedCapeCustomized() : player.getCompletionistCapeCustomized();
		if (buttonId == 58) { // reset
			if (capeId == 32151 || capeId == 20767)
				player.setMaxedCapeCustomized(Arrays.copyOf(ItemConfig.forID(capeId).originalModelColors, 4));
			else
				player.setCompletionistCapeCustomized(Arrays.copyOf(ItemConfig.forID(capeId).originalModelColors, 4));
			for (int i = 0; i < 4; i++)
				player.getVarsManager().sendVarBit(9254 + i, skillCape[i]);
		} else if (buttonId == 34) { // detail top
			player.getTemporaryAttributtes().put("SkillcapeCustomize", 0);
			player.getInterfaceManager().sendInterface(19);
			player.getVarsManager().sendVar(2174, skillCape[0]);
		} else if (buttonId == 71) { // background top
			player.getTemporaryAttributtes().put("SkillcapeCustomize", 1);
			player.getInterfaceManager().sendInterface(19);
			player.getVarsManager().sendVar(2174, skillCape[1]);
		} else if (buttonId == 83) { // detail button
			player.getTemporaryAttributtes().put("SkillcapeCustomize", 2);
			player.getInterfaceManager().sendInterface(19);
			player.getVarsManager().sendVar(2174, skillCape[2]);
		} else if (buttonId == 95) { // background button
			player.getTemporaryAttributtes().put("SkillcapeCustomize", 3);
			player.getInterfaceManager().sendInterface(19);
			player.getVarsManager().sendVar(2174, skillCape[3]);
		} else if (buttonId == 114 || buttonId == 142) { // done / close
			player.getAppearence().generateAppearenceData();
			player.closeInterfaces();
		}
	}
}
