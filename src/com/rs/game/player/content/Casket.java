/**
 * 
 */
package com.rs.game.player.content;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Oct 8, 2017
 */
public class Casket {

	private static final Item[] REWARDS = new Item[] {new Item(995, 600 * 10), new Item(985, 1), new Item(987, 1), new Item(1623, 1), new Item(1621, 1), new Item(1619, 1), new Item(1631, 1), new Item(1452, 1), new Item(1454, 1), new Item(1462, 1)};
	public static final int ID = 405;
	
	public static void open(Player player) {
		Item item = REWARDS[Utils.random(REWARDS.length)];
		player.getDialogueManager().startDialogue("ItemMessage", "You open the casket. Inside you find "+(item.getAmount() > 1 ? "some" : "a")+" "+item.getName().toLowerCase()+".",  item.getId());
		if (item.getAmount() > 1)
			item = new Item(item.getId(), Utils.random(item.getAmount())+1);
		player.getInventory().deleteItem(ID, 1);
		player.getInventory().addItemMoneyPouch(item);
	}
	
	//You open the casket. You find inside some / a. itemid.
}
