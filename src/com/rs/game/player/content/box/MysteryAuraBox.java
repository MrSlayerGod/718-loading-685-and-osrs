/**
 * 
 */
package com.rs.game.player.content.box;

import com.rs.discord.Bot;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dragonkk(Alex)
 * Oct 23, 2017
 */
public class MysteryAuraBox {

	public static final int ID = 25763;


    public static final int[] LEGENDARY = {23862, 23864, 23866, 23870, 23846, 23846, 23858, 23856, 23860, 23868, 23872, 23878, 23874, 23854, 23876, 22889,23880, 23882, 23884, 23886, 23890, 23892, 23894
			, 25477, 25554
    };//1%


	public static final int[] RARE = {
			22919, 22931, 22921, 22887, 23852, 22903, 22909, 22917, 22913, 22923, 22911, 22915, 22925, 23844, 22891, 22893, 22933
	};//2%

	
	private static final int[] UNCOMMON = {
			22901, 22907, 23850, 22268, 22274, 22282, 22286, 22290, 23842, 22276, 22929, 22272, 22270, 22298, 22294, 22300, 22296, 22292
	}; //10%

	private static final int[] COMMON = {
			20957, 20958, 20961, 20962, 20963, 20964, 20965, 20967, 23848, 22899, 20964, 23840, 22284, 20966, 22883, 22905, 20965, 22280, 22927
	}; //100%
	
	
	public static void preview(Player player) {
		List<Item> items = new ArrayList<Item>();
		for (int i : LEGENDARY)
			items.add(new Item(i));
		for (int i : RARE)
			items.add(new Item(i));
		for (int i : UNCOMMON)
			items.add(new Item(i));
		for (int i : COMMON)
			items.add(new Item(i));
		Item[] itemA = items.toArray(new Item[items.size()]);
		MysteryBox.preview(player, "Aura Mystery Box Rewards", itemA);
	}
	

	public static void open(Player player,  boolean quickOpen) {
		player.stopAll();
		player.lock(1); //just in case
		player.getInventory().deleteItem(ID, 1);
		double MULTIPlIER = MysteryBox.MULTIPlIER;
		List<Item> rewards = new ArrayList<Item>(MysteryBox.SLOTS.length);
		List<Item> items = new ArrayList<Item>();
		for (int i : LEGENDARY)
			items.add(new Item(i));
		for (int i : RARE)
			items.add(new Item(i));
		for (int i : UNCOMMON)
			items.add(new Item(i));
		for (int i : COMMON)
			items.add(new Item(i));
		for (int i = 0; i < 5; i++) {
			Item item = items.remove(Utils.random(items.size()));
			rewards.add(item);
		}
		int rarity = 0;
		Item reward;
		if (Utils.random(100) <= 0) { //1 in 100
			reward = new Item(LEGENDARY[Utils.random(LEGENDARY.length)]);
			rarity = 4;
		} else if (Utils.random(50) <= 0) {
			reward = new Item(RARE[Utils.random(RARE.length)]);
			rarity = 2;
		} else if (Utils.random(10) <= 0) {
			reward = new Item(UNCOMMON[Utils.random(UNCOMMON.length)]);
			rarity = 1;
		} else {
			reward = new Item(COMMON[Utils.random(COMMON.length)]);
			rarity = 0;
		}
		
		setItem: {
			for (Item item : rewards) {
				if (item.getId() == reward.getId())
					break setItem;
			}
			rewards.set(Utils.random(rewards.size()), reward);
		}
		int r = rarity;
		WorldTask runTask = new WorldTask() {


			int currentSlot = 0;
			boolean selected;
			
			@Override
			public void run() {

				if (selected) {
					stop();
					for (int i = 0; i < MysteryBox.SLOTS.length; i++)
						player.getPackets().sendIComponentSprite(893, MysteryBox.SLOTS[i], i == currentSlot ? 2206 : 20762);
					player.getDialogueManager().startDialogue("SimpleItemMessageClose", reward.getId(), "<col=ff0000>You just WON "+reward.getName()+"!<br>Continue to claim your reward.");
					return;
				}
				int currentIndex = (currentSlot % MysteryBox.SLOTS.length);
				Item item = rewards.get(currentIndex);
				player.getPackets().sendIComponentSprite(893, MysteryBox.SLOTS[(currentIndex == 0 ? MysteryBox.SLOTS.length  : currentIndex) - 1],  21120);
				player.getPackets().sendIComponentSprite(893, MysteryBox.SLOTS[currentIndex],  21121);
				player.getPackets().sendIComponentText(893, 3, item.getName());
				if (item == reward && Utils.random(2) == 0) {
					selected = true;
					return;
				}
				currentSlot = ++currentSlot % MysteryBox.SLOTS.length;
			}
			
		};
		Runnable closeEvent = new Runnable() {

			@Override
			public void run() {
				runTask.stop();
				Dialogue.closeNoContinueDialogue(player);
				player.getPackets().sendGameMessage("You open the aura mystery box and receive <img=10>"+Utils.getFormattedNumber(reward.getAmount())+" "+reward.getName()+" <img=10>!");
				if (!player.getInventory().hasFreeSlots()) {
					player.getPackets().sendGameMessage(reward.getName() +" x"+reward.getAmount()+" has been added to your bank.");
					player.getBank().addItem(reward.getId(), reward.getAmount(), false);
				} else
					player.getInventory().addItemMoneyPouch(reward);
				Logger.globalLog(player.getUsername(), player.getSession().getIP(), new String(" got " + reward.getId()+", "+reward.getAmount() + " from mystery box.("+ID+")"));
				player.setNextGraphics(new Graphics(r == 4 || r == 3 ? 1512 : r == 2 || r == 1 ? 1513 : 1514));
				Bot.sendLog(Bot.BOX_CHANNEL, "[type=AURA-MBOX][name="+player.getUsername()+"]"+"[item="+reward.getName()+"("+reward.getId()+")" + "x" + reward.getAmount()+"]");
		
				if (r == 4) 
					World.sendNews(player, "LEGENDARY! "+Utils.formatPlayerNameForDisplay(player.getDisplayName())
					+ "just received <img=14><col=ffffff>" +Utils.getFormattedNumber(reward.getAmount())+" "+reward.getName()+"<col=D80000> <img=14> from a <col=ff9933>aura mystery box!", 5);
				else if (r == 3)
					World.sendNews(player, "ULTRA RARE! "+Utils.formatPlayerNameForDisplay(player.getDisplayName())
					+ "just received <img=13><col=ffff00>" +Utils.getFormattedNumber(reward.getAmount())+" "+reward.getName()+"<col=D80000> <img=13> from a <col=ff9933>aura mystery box!", 5);
				else if (r == 2)
					World.sendNews(player, "VERY RARE! "+Utils.formatPlayerNameForDisplay(player.getDisplayName())
					+ "just received <img=12><col=ff9933>" +Utils.getFormattedNumber(reward.getAmount())+" "+reward.getName()+" from a <col=ff9933>aura mystery box!", 5);
				else if (r == 1)
					World.sendNews(player, " RARE! "+Utils.formatPlayerNameForDisplay(player.getDisplayName())
					+ " just received <img=11><col=00ACE6>" +Utils.getFormattedNumber(reward.getAmount())+" "+reward.getName()+" from a <col=ff9933>aura mystery box!", 5);
			}
			
		};
		if (quickOpen) {
			closeEvent.run();
			return;
		}
		player.getInterfaceManager().sendInterface(893);
		player.getPackets().sendIComponentText(893, 18, "Mystery Box");
		player.getPackets().sendIComponentText(893, 3, "");
		for (int i = 0; i < MysteryBox.SLOTS.length; i++) {
			Item item = rewards.get(i);
			player.getPackets().sendIComponentSprite(893, MysteryBox.SLOTS[i], 21120);
			player.getPackets().sendItemOnIComponent(893, MysteryBox.SLOTS[i]+1, item.getId(), item.getAmount());
			player.getPackets().sendIComponentSettings(893, MysteryBox.SLOTS[i]+1, -1, 0, 0);
		}
		player.setCloseInterfacesEvent(closeEvent);
		Dialogue.sendItemDialogueNoContinue(player, ID, "Rolling....");
		WorldTasksManager.schedule(runTask, 0, 0);
	}

}
