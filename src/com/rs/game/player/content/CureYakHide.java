/**
 * 
 */
package com.rs.game.player.content;

import com.rs.game.item.Item;
import com.rs.game.player.Player;

/**
 * @author dragonkk(Alex)
 * Sep 26, 2017
 */
public class CureYakHide {

	public static void cure(Player player) {
		int amt = player.getInventory().getAmountOf(10818);
		if (amt == 0) {
			player.getPackets().sendGameMessage("You don't have any yak hide to cure.");
			return;
		}
		int coins = amt * 5;
		if (player.getInventory().getCoinsAmount() < coins) {
			player.getPackets().sendGameMessage("You need "+coins+" coins to cure these hides.");
			return;
		}
		player.getInventory().removeItemMoneyPouch(new Item(995, coins));
		player.getInventory().deleteItem(10818, amt);
		player.getInventory().addItem(10820, amt);
		player.getPackets().sendGameMessage("Thakkrad Sigmundson cures you "+amt+" hides in exchange for "+coins+" coins.");
		
		
	}
}
