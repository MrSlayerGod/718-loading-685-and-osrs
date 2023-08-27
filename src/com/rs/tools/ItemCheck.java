package com.rs.tools;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemConfig;
import com.rs.game.player.Equipment;
import com.rs.utils.Utils;

public class ItemCheck {

	private static List<Integer> items = new LinkedList<Integer>();

	public static final void main(String[] args) throws IOException {
		Cache.init();

		for (int itemId = 0; itemId < Utils.getItemDefinitionsSize(); itemId++) {
			ItemConfig def = ItemConfig.forID(itemId);
			if ((def.equipSlot != Equipment.SLOT_WEAPON && def.equipSlot != Equipment.SLOT_ARROWS) || def.lended || def.noted)
				continue;
			if (isPoisoned(def))
				continue;
			ItemConfig p = getPoison(def, "p");
			ItemConfig p_ = getPoison(def, "p+");
			ItemConfig p__ = getPoison(def, "p++");
			//   System.out.println(p+", "+p_+", "+p__);
			if (p == null || p_ == null || p__ == null)
				continue;
			System.out.println(", " + (Utils.formatPlayerNameForProtocol(def.name).toUpperCase()) + "(" + itemId + ", " + p.id + ", " + p_.id + ", " + p__.id + ")");

			//System.out.println(itemId+", "+def.name+", "+p.id+", "+p.name+", "+p_.id+", "+p_.name+", "+p__.id+", "+p__.name);
		}
	}

	private static ItemConfig getPoison(ItemConfig def, String string) {
		for (int i = 0; i < Utils.getItemDefinitionsSize(); i++) {
			ItemConfig def2 = ItemConfig.forID(i);
			if (def2.getName().equals(def.getName().concat(" (" + string + ")"))) {
				return def2;
			}
		}
		return null;
	}

	private static boolean isPoisoned(ItemConfig def) {
		String name = def.getName().toLowerCase();
		return name.contains("(p)") || name.contains("(p+)") || name.contains("(p++)") || name.contains("(b)");
	}

	private static int getItemId(String name) {
		for (int itemId = 0; itemId < Utils.getItemDefinitionsSize(); itemId++) {
			ItemConfig def = ItemConfig.forID(itemId);
			name = name.replace(" legs", " platelegs");
			name = name.replace(" body", " platebody");
			if (def.name.equalsIgnoreCase(name))
				return itemId;
		}
		return -1;

	}

}
