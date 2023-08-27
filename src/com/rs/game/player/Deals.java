package com.rs.game.player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.rs.cache.loaders.ItemConfig;
import com.rs.discord.Bot;
import com.rs.game.item.Item;
import com.rs.game.player.content.Shop;
import com.rs.utils.ShopsHandler;
import com.rs.utils.Utils;

public class Deals implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8540286692491447146L;
	
	private int donated, slot1Item, slot2Item, slot3Item;
	private boolean claimedFirst, claimedSecond;
	private long date;
	
	private transient Player player;
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public void init() {
		if (needsUpdate()) {
			player.getPackets().sendGameMessage("<shad=0000FF>New ::deals available! Check them now!");	
			reset();
		}
	}
	
	private boolean needsUpdate() {
		Calendar old = Calendar.getInstance();
		old.setTimeInMillis(date);
		Calendar current = Calendar.getInstance();
		if (old.get(Calendar.DAY_OF_MONTH) != current.get(Calendar.DAY_OF_MONTH)
				|| old.get(Calendar.DAY_OF_MONTH) != current.get(Calendar.DAY_OF_MONTH)
				|| old.get(Calendar.DAY_OF_YEAR) != current.get(Calendar.DAY_OF_YEAR))
			return true;
		return false;
	}
	
	public int getRequiredClaim1() {
		return (Shop.getDollarPrice(new Item(slot1Item))+Shop.getDollarPrice(new Item(slot2Item)))*4;
	}
	
	public int getRequiredClaim2() {
		return Shop.getDollarPrice(new Item(slot3Item))*4;
	}

	public void process() {
		if (!player.getInterfaceManager().containsInterface(925))
			return;
        Calendar calendar = Calendar.getInstance();
        int hour = 24 - calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = 60 - calendar.get(Calendar.MINUTE);
        int seconds = 60 - calendar.get(Calendar.SECOND);
        if (minutes != 60)
            hour--;
        if (seconds != 60)
            minutes--;
    	player.getPackets().sendIComponentText(925, 77, (hour < 10 ? "0" : "")+hour + ":" + (minutes < 10 ? "0" : "")+minutes + ":" + (seconds < 10 ? "0" : "")+seconds + " (H:M:S)");
	}
	
	public void addDonated(int donated) {
		this.donated += donated;
		if ((!claimedFirst && donated < getRequiredClaim1())
			|| (!claimedSecond && donated < getRequiredClaim2())) {
			player.getPackets().sendGameMessage("You can now claim your reward from ::deals!");
			return;
		}
	}

	public void claim1() {
		if (needsUpdate()) {
			open();
			return;
		}
		int required = getRequiredClaim1();
		if (donated < required) {
			player.getPackets().sendGameMessage("You need to donate another "+(required-donated)+"$ today to claim this reward!");
			return;
		}
		if (claimedFirst) {
			player.getPackets().sendGameMessage("You already claimed this reward!");
			return;
		}
		if (player.getInventory().getFreeSlots() < 2) {
			player.getPackets().sendGameMessage("You need two inventory spaces to claim this reward.");
			return;
		}
		player.getInventory().addItem(slot1Item, 1);
		player.getInventory().addItem(slot2Item, 1);
		player.getPackets().sendGameMessage("Contragulations. You received a free bonus as proof of your loyality!");
		Bot.sendLog(Bot.BOX_CHANNEL, "[type=DEAL][name="+player.getUsername()+"]"+"[item="+ItemConfig.forID(slot3Item).getName()+"("+slot1Item+")" + "x1]");
		Bot.sendLog(Bot.BOX_CHANNEL, "[type=DEAL][name="+player.getUsername()+"]"+"[item="+ItemConfig.forID(slot2Item).getName()+"("+slot2Item+")" + "x1]");
		claimedFirst = true;
	}
	

	public void claim2() {
		if (needsUpdate()) {
			open();
			return;
		}
		int required = getRequiredClaim2();
		if (donated < required) {
			player.getPackets().sendGameMessage("You need to donate another "+(required-donated)+"$ today to claim this reward!");
			return;
		}
		if (claimedSecond) {
			player.getPackets().sendGameMessage("You already claimed this reward!");
			return;
		}
		if (player.getInventory().getFreeSlots() < 1) {
			player.getPackets().sendGameMessage("You need one inventory space to claim this reward.");
			return;
		}
		player.getInventory().addItem(slot3Item, 1);
		player.getPackets().sendGameMessage("Contragulations. You received a free bonus as proof of your loyality!");
		claimedSecond = true;
		Bot.sendLog(Bot.BOX_CHANNEL, "[type=DEAL][name="+player.getUsername()+"]"+"[item="+ItemConfig.forID(slot3Item).getName()+"("+slot3Item+")" + "x1]");
		
	}
	
	public void open() {
		if (needsUpdate()) {
			player.getPackets().sendGameMessage("Your deal has reset since it's been over 24h!");
			reset();
		}
		player.getInterfaceManager().sendInterface(925);
		player.getPackets().sendIComponentText(925, 18, "Matrix's Deal of the Day");
		player.getPackets().sendIComponentText(925, 75, "Time left:");
		player.getPackets().sendIComponentText(925, 76, "Donated:");
		
        Calendar calendar = Calendar.getInstance();
        int hour = 24 - calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = 60 - calendar.get(Calendar.MINUTE);
        int seconds = 60 - calendar.get(Calendar.SECOND);
        if (minutes != 60)
            hour--;
        if (seconds != 60)
            minutes--;
        
    	player.getPackets().sendIComponentText(925, 77, (hour < 10 ? "0" : "")+hour + ":" + (minutes < 10 ? "0" : "")+minutes + ":" + (seconds < 10 ? "0" : "")+seconds + " (H:M:S)");
    	player.getPackets().sendIComponentText(925, 78, donated+"$");
    	int percentage1 = Math.min((int) ((double)donated / (double)getRequiredClaim1() * 100d), 100);
      	int percentage2 = Math.min((int) ((double)donated / (double)getRequiredClaim2() * 100d), 100);
    	player.getPackets().sendIComponentText(925, 51, "Claim at: "+getRequiredClaim1()+"$ ("+percentage1+"%)");
    	player.getPackets().sendIComponentText(925, 25, "Claim at: "+getRequiredClaim2()+"$ ("+percentage2+"%)");
    	player.getPackets().sendIComponentText(925, 26, "");
    	
    	player.getPackets().sendIComponentText(925, 69, new Item(slot1Item).getName()+"<br>"+new Item(slot2Item).getName());
    	player.getPackets().sendIComponentText(925, 28, new Item(slot3Item).getName());
    	//67, 68
    	player.getPackets().sendItemOnIComponent(925, 67, slot1Item, 1);
    	player.getPackets().sendItemOnIComponent(925, 68, slot2Item, 1);
    	
    	player.getPackets().sendItemOnIComponent(925, 41, slot3Item, 1);
    	
    	player.getPackets().sendIComponentText(925, 52, "Worth: "+Shop.getDollarPrice(new Item(slot1Item))+"$");
    	player.getPackets().sendIComponentText(925, 53, "Worth: "+Shop.getDollarPrice(new Item(slot2Item))+"$");
    	player.getPackets().sendIComponentText(925, 26, "Worth: "+Shop.getDollarPrice(new Item(slot3Item))+"$");
	}
	
	public void reset() { 
		Shop donatorShop = ShopsHandler.getShop(912);
		List<Integer> possibleItems = new ArrayList<Integer>();
		possibleItems.add(6199);
	//	possibleItems.add(27004);
		possibleItems.add(25453);
		possibleItems.add(25436);
		possibleItems.add(25503);
		possibleItems.add(25763);

		/*for (Item item : donatorShop.getMainStock()) { //20
			int price = Shop.getDollarPrice(item);
			if (price <= 15)
				possibleItems.add(item.getId());
		}*/
		slot1Item = possibleItems.get(Utils.random(possibleItems.size()));
		int totalPrice = Shop.getDollarPrice(new Item(slot1Item));
		possibleItems.clear();
		/*for (Item item : donatorShop.getMainStock()) { //20
			int price = Shop.getDollarPrice(item);
			if (price <= 20-totalPrice)
				possibleItems.add(item.getId());
		}*/
		possibleItems.add(6199);
		//possibleItems.add(27004);
		possibleItems.add(25453);
		possibleItems.add(25436);
		possibleItems.add(25503);
		possibleItems.add(25763);
		possibleItems.remove((Object)slot1Item);
		slot2Item = possibleItems.get(Utils.random(possibleItems.size()));
		totalPrice += Shop.getDollarPrice(new Item(slot2Item));
		possibleItems.clear();
		for (Item item : donatorShop.getMainStock()) { //20
			int price = Shop.getDollarPrice(item);
			if (price >= totalPrice+2 && price <= 125)
				possibleItems.add(item.getId());
		}
		slot3Item = possibleItems.get(Utils.random(possibleItems.size()));
		donated = 0;
		claimedFirst = false;
		claimedSecond = false;
		date = Utils.currentTimeMillis();
	}
	
}
