package com.rs.game.player.content.seasonalEvents;

import java.util.ArrayList;
import java.util.List;

import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class Hallowen2018 {

	private static final int[] ITEMS = {9920, 9921, 9922, 9923, 9924, 9925, 9911};
	
	public static boolean isEventItem(int id) {
		for (int i : ITEMS)
			if (id == i)
				return true;
		return false;
	}
	public static int getItemDrop(Player player) {
		List<Integer> pieces = new ArrayList<Integer>();
		for (int i : ITEMS)
			if (!player.containsItem(i))
				pieces.add(i);
		if (pieces.isEmpty())
			return Utils.random(2) == 0 ? 23737 : 23713;
		return pieces.get(Utils.random(pieces.size()));
	}
	
}
