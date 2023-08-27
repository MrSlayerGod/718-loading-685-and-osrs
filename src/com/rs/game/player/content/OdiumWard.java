/**
 * 
 */
package com.rs.game.player.content;

import com.rs.game.Animation;
import com.rs.game.player.Player;

/**
 * @author dragonkk(Alex)
 * Nov 2, 2017
 */
public class OdiumWard {

	public static final int[] SHIELDS = {41926, 41924};
	
	public static final int[][] SHIELDPARTS = {{41928, 41929, 41930}, {41931, 41932, 41933}};
	
	public static boolean makeShield(Player player, int id) {
		for(int i = 0; i < 2; i++) {
			for (int piece : SHIELDPARTS[i]) {
				if (piece == id) {
					for (int p : SHIELDPARTS[i])
						if (!player.getInventory().containsItem(p, 1)) {
							player.getPackets().sendGameMessage("You are missing one or more of the components to make this shield.");
							return true;
						}
					player.lock(2);
					for (int p : SHIELDPARTS[i])
						player.getInventory().deleteItem(p, 1);
					player.getInventory().addItem(SHIELDS[i], 1);
					player.setNextAnimation(new Animation(7271));
					player.getPackets().sendGameMessage("You forge the shield pieces together in the chambers of fire and are blown back by the intense heat.");
					return true;
				}
			}
		}
		return false;
		
	}
}
