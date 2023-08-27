/**
 * 
 */
package com.rs.game.player.content.box;

import com.rs.discord.Bot;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.ItemConstants;
import com.rs.utils.Utils;

import java.util.Arrays;

/**
 * @author dragonkk(Alex)
 * Oct 8, 2017
 */
public class MinigameBox {

	private static final int[] REWARDS = new int[] {
			995, 75000
			, 986, 3
			, 1620, 25
			, 1618, 25
			, 222, 25
			, 236, 25
			, 226, 25
			, 224, 25
			, 240, 25
			, 232, 25
			,  2971, 25
			, 2504, 20
			, 2498, 5
			, 2492, 5
			, 2490, 10
			, 2496, 10
			, 1128, 5
			, 1114, 5
			, 1080, 5
			, 4132, 5
			, 1202, 5
			, 961, 50
			, 8779, 50
			, 8783, 50
			, 8781, 50
			, 574, 20
			, 572, 20
			, 576, 20
			, 570, 20
			, 441, 30
			, 454, 20
			, 445, 15
			, 560, 200
			, 565, 200
			, 5312, 5
			, 5313, 3
			, 5314, 2
			, 13278, 1250
			, 5295, 5
			, 5300, 2
			, 12158, 100
			, 12159, 75
			, 12160, 50
			, 12163, 25
			, 537, 5
			, 533, 50
			, 41738, 3
	};
	
	public static int[] REWARDS_RARE = {21462, 21463, 21464, 21465, 21466, 21467, 21468, 21469, 21470, 21471, 21472, 21473, 21474, 21475, 21476};
	
	public static final int ID = 25594;
	
	
	
	private static int selectReward(Player player, int[] rewards) {
		/*if (Utils.random(5/*3*///) == 0) { //adk, 1 in 5 cash now, nerfed a bit too from 1.5 to 1.4
			/*for (Item item : rewards)
				if (item.getId() == 995) 
					return new Item(995, (int) (1.4 * ((int) (item.getAmount() * (premium ? 0.6 : 0.4) + Utils.random(item.getAmount() *(premium ? 1.1 : 0.7))))));
		}*/
		int item = -1;
		int tries = 0;
		while ((item == -1 || (!ItemConstants.isTradeable(new Item(item)) && player.containsItem(item))) && tries++ < 10) 
			item = rewards[Utils.random(rewards.length)];
		return item;
	}
	
	
	
	public static void open(Player player) {
		boolean rare;
		Item item;
		if (Utils.random(200) == 0) {
			rare = true;
			item = new Item(selectReward(player, REWARDS_RARE));//new Item(Utils.random(REWARDS_RARE));
		} else {
			rare = false;
			int index = Utils.random(REWARDS.length/2);
			item = new Item(REWARDS[index*2], REWARDS[index*2+1]);
		}
		
		player.getDialogueManager().startDialogue("ItemMessage", "You open the box. Inside you find "+(item.getAmount() > 1 ? "some" : "a")+" "+item.getName().toLowerCase()+".",  item.getId());
		if (item.getAmount() > 1)
			item = new Item(item.getId(), Utils.random(item.getAmount())+1);
		player.getInventory().deleteItem(ID, 1);
		
		if (!player.getInventory().hasFreeSlots()) {
			player.getPackets().sendGameMessage(item.getName() +" x"+item.getAmount()+" has been added to your bank.");
			player.getBank().addItem(item.getId(), item.getAmount(), false);
		} else
			player.getInventory().addItemMoneyPouch(item);
		
		Bot.sendLog(Bot.BOX_CHANNEL, "[type=MINIGAME-BOX][name="+player.getUsername()+"]"+"[item="+item.getName()+"("+item.getId()+")" + "x" + item.getAmount()+"]");
		
		if (rare) 
			World.sendNews(player, "WOW! "+Utils.formatPlayerNameForDisplay(player.getDisplayName())
			+ "just received <img=14><col=ffffff>" +Utils.getFormattedNumber(item.getAmount())+" "+item.getName()+"<col=D80000> <img=14> from "+ "<col=ffff00> minigame box!", 5);

	}
	
	
	public static void preview(Player player) {
		Item[] itemA = new Item[REWARDS.length / 2 + REWARDS_RARE.length];
		for (int i = 0; i < REWARDS_RARE.length; i++) 
			itemA[i] = new Item(REWARDS_RARE[i]);
		for (int i = 0; i < REWARDS.length / 2; i++) {
			itemA[i + REWARDS_RARE.length] = new Item(REWARDS[i * 2], REWARDS[i * 2 + 1]);
		}
		MysteryBox.preview(player, "Minigame Box Rewards", itemA);
	}
	
	
	
	//You open the casket. You find inside some / a. itemid.
}
