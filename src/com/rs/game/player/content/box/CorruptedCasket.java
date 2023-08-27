package com.rs.game.player.content.box;

import com.rs.discord.Bot;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.ItemConstants;
import com.rs.utils.Utils;

public class CorruptedCasket {

	public static final int ID = 25489;
	
	
	private static final Item[] VERY_RARE = {
			new Item(25497), new Item(25498)
	};

	private static final Item[] RARE = {new Item(6571,1), new Item(13754,1),new Item(5295,20),new Item(5300,15),new Item(5304,10),new Item(5302,15),new Item(13736,1),new Item(5315,3),new Item(5316,1),new Item(24379,1),new Item(24382,1),new Item(995,10000000)
};
	private static final Item[] UNCOMMON = {
			new Item(3204,1),new Item(1249,1),new Item(1620,15),new Item(1618,15),new Item(1632,10),new Item(9245,175),new Item(2,1000),new Item(13734,1),new Item(450,100),new Item(452,20),new Item(2362,35),new Item(2364,20),new Item(987,1),new Item(985,1),new Item(989,1),new Item(1079,1),new Item(1127,1),new Item(1201,1),new Item(537,25),new Item(9185,1),new Item(995,5000000),new Item(1305,1),new Item(4587,1),new Item(1149,1)

	}; 
	private static final Item[] COMMON = {

			new Item(1624,15),new Item(1622,15),new Item(7937,2000),new Item(560,300),new Item(565,250),new Item(566,200),new Item(5321,30),new Item(8783,50),new Item(8781,50),new Item(995,2000000),new Item(533,40),new Item(2497,1),new Item(2503,1),new Item(3749,1),new Item(1514,50),new Item(1516,100),new Item(561,150),new Item(454,125),new Item(445,200), new Item(995,2000000),

	};
	
	private static Item selectReward(Item[] rewards) {
		return rewards[Utils.random(rewards.length)];
	}
	
	public static void open(Player player) {
		Item item = null;
		if (!player.getInventory().hasFreeSlots()) {
			player.getPackets().sendGameMessage("Not enough space in your inventory.");
			return;
		}
		player.getInventory().deleteItem(ID, 1);
		if (Utils.random((int) (3000 / player.getDropRateMultiplier())) == 0) {
			player.setNextGraphics(new Graphics(1765));
			int tries = 0;
			item = null;
			while ((item == null || (!ItemConstants.isTradeable(item) && player.containsItem(item.getId()))) && tries++ < 10) 
				item = selectReward(VERY_RARE);
			
			Bot.sendLog(Bot.PICKUP_DROP_CHANNEL, "[type=CORRUPTED-CASKET][name="+player.getUsername()+"]"+"[item="+item.getName()+"("+item.getId()+")" + "x" + item.getAmount()+"]");
			
			
			World.sendNews(player, "ULTRA RARE! "+Utils.formatPlayerNameForDisplay(player.getDisplayName())
					+ "just received <img=13><col=ffff00>" +Utils.getFormattedNumber(item.getAmount())+" "+item.getName()+"<col=D80000> <img=13> from"+ ("<col=ff9933>") +" corrupted casket!", 0);
		}else if (Utils.random((int)(500 / player.getDropRateMultiplier())) == 0) {
			player.setNextGraphics(new Graphics(1765));
			int tries = 0;
			while ((item == null || (!ItemConstants.isTradeable(item) && player.containsItem(item.getId()))) && tries++ < 10) 
				item = selectReward(RARE);
			
			Bot.sendLog(Bot.PICKUP_DROP_CHANNEL, "[type=CORRUPTED-CASKET][name="+player.getUsername()+"]"+"[item="+item.getName()+"("+item.getId()+")" + "x" + item.getAmount()+"]");
			if (item.getId() == 6571 || item.getId() == 13754)
			World.sendNews(player, "VERY RARE! "+Utils.formatPlayerNameForDisplay(player.getDisplayName())
					+ "just received <img=12><col=ff9933>" +Utils.getFormattedNumber(item.getAmount())+" "+item.getName()+"<col=D80000> <img=12> from"+ ("<col=ff9933>") +" corrupted casket!", 0);
		}else if (Utils.random(3) == 0) {
			int tries = 0;
			item = null;
			while ((item == null || (!ItemConstants.isTradeable(item) && player.containsItem(item.getId()))) && tries++ < 10) 
				item = selectReward(UNCOMMON);
			
			Bot.sendLog(Bot.PICKUP_DROP_CHANNEL, "[type=CORRUPTED-CASKET][name="+player.getUsername()+"]"+"[item="+item.getName()+"("+item.getId()+")" + "x" + item.getAmount()+"]");
			} else {
			player.setNextGraphics(new Graphics(199));
			int tries = 0;
			item = null;
			
			
			while ((item == null || (!ItemConstants.isTradeable(item) && player.containsItem(item.getId()))) && tries++ < 10) 
				item = selectReward(COMMON);
			
			Bot.sendLog(Bot.PICKUP_DROP_CHANNEL, "[type=CORRUPTED-CASKET][name="+player.getUsername()+"]"+"[item="+item.getName()+"("+item.getId()+")" + "x" + item.getAmount()+"]");
			
		}
		player.getPackets().sendGameMessage("You open the corrupted casket and receive <img=10>"+Utils.getFormattedNumber(item.getAmount())+" "+item.getName()+" <img=10>!");
		if (!item.getDefinitions().isStackable() && item.getAmount() > 1) {
			player.getPackets().sendGameMessage(item.getName() +" x"+item.getAmount()+" has been added to your bank.");
			player.getBank().addItem(item.getId(), item.getAmount(), false);
		} else
			player.getInventory().addItemMoneyPouch(item);
		player.setNextAnimation(new Animation(4945));
		player.setNextGraphics(new Graphics(1601));
		player.getPackets().sendMusicEffect(302);
	}
}
