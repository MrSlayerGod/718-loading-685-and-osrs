package com.rs.game.player.content;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;

public enum Costumes {

	CAT_COSTUME(24605, 24607, 24609, 24611, 24613),
	CABARET_COSTUME(24583, 24585, 24587, 24589, 24591),
	COLOSSEUM_COSTUME(24595, 24597, 24599, 24601),
	GOTHIC_COSTUME(24617, 24619, 24621, 24623),
	SWASHBUCKLER_COSTUME(24627, 24629, 24631, 24633, 24635),
	ASSASSIN_COSTUME(24639, 24641, 24643, 24645, 24647, 24649),
	PRINCE_COSTUME(25074, 25076, 25078, 25080, 25082),
	LORD_COSTUME(25086, 25088, 25090, 25092, 25094),
	BUTLER_COSTUME(25098, 25100, 25102, 25104, 25106, 25108),
	FOX_COSTUME(25136, 25138, 25140, 25142, 25144),
	WOLF_COSTUME(25146, 25148, 25150, 25152, 25154, 25156),
	PANDA_COSTUME(25160, 25162, 25164, 25166, 25168),
	WARSUIT_COSTUME(25273, 25275, 25277, 25281),
	DEMON_COSTUME(25374, 25376, 25378, 25382),
	WARSUIT_2_COSTUME(25386, 25388, 25390, 25394),
	ARIANE_COSTUME(false, 26043, 26045, 26047, 26051),
	OZAN_COSTUME(true, 26063, 26065, 26069, 26071),
	DEMON_2_COSTUME(26158, 26160, 26162, 26166),
	WARSUIT_3_COSTUME(26170, 26172, 26174, 26178),
	WARSUIT_4_COSTUME(26182, 26184, 26186, 26190),
	WARSUIT_5_COSTUME(26450, 26452, 26454, 26458),
	PALADIN_COSTUME(26464, 26466, 26468, 26470, 26472),
	WEST_CAPTAIN_COSTUME(26390, 26392, 26394, 26396, 26398),
	EAST_CAPTAIN_COSTUME(26402, 26404, 26406, 26408, 26410),
	EAST_CREW_COSTUME(26414, 26416, 26418, 26420, 26422),
	WEST_CREW_COSTUME(26426, 26428, 26430, 26432, 26434),
	DEFAULT();
	private ItemsContainer<Item> items;
	private int type;

	/*
	 * 0 both , 1 male , 2 female,
	 */
	private Costumes(boolean male, int... ids) {
		this(ids);
		type = male ? 1 : 2;
	}

	private Costumes(int... ids) {
		items = new ItemsContainer<Item>(15, false);
		for (int i : ids) {
			if (i >= 25354)
				continue;
			int slot = ItemConfig.forID(i).equipSlot;
			items.set(slot, new Item(i));
		}
	}

	public ItemsContainer<Item> getItems() {
		return items;
	}

	public int getType() {
		return type;
	}

}
