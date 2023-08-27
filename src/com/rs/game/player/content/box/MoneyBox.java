package com.rs.game.player.content.box;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

public class MoneyBox {

	public static final int ID = 25503,
			BOX_PRICE = 4; //4$
	public static int CASH_PER_DOLLAR = 5; //5m
	
	public static void open(Player player, boolean quickOpen) {
		player.stopAll();
		player.lock(1); //just in case
		player.getInventory().deleteItem(ID, 1);
		List<Item> rewards = new ArrayList<Item>(MysteryBox.SLOTS.length);
		int averagePerBox = BOX_PRICE * CASH_PER_DOLLAR; 
		int rarity = Utils.random(100) == 0 ? 4 : Utils.random(25) == 0 ? 3 : Utils.random(10) == 0 ? 2 : Utils.random(4) == 0 ? 1 : 0;
		rewards.add(new Item(995, Utils.random((int) ((averagePerBox * 0.4)), (int) ((averagePerBox * 0.75)+1)) * 1000000));
		rewards.add(new Item(995, Utils.random((int) ((averagePerBox * 0.75)), (int) ((averagePerBox * 2)+1)) * 1000000));
		rewards.add(new Item(995, Utils.random((int) ((averagePerBox * 2)), (int) ((averagePerBox * 2.5)+1)) * 1000000));
		rewards.add(new Item(995, Utils.random((int) ((averagePerBox * 2.5)), (int) ((averagePerBox * 3.5)+1)) * 1000000));
		rewards.add(new Item(995, Utils.random((int) ((averagePerBox * 3.5)), (int) ((averagePerBox * 7)+1)) * 1000000));
		Item reward = rewards.get(rarity);
		WorldTask runTask = new WorldTask() {


			int currentSlot = 0;
			boolean selected;
			
			@Override
			public void run() {

				if (selected) {
					stop();
					for (int i = 0; i < MysteryBox.SLOTS.length; i++)
						player.getPackets().sendIComponentSprite(893, MysteryBox.SLOTS[i], i == currentSlot ? 2206 : 20762);
					player.getDialogueManager().startDialogue("SimpleItemMessageClose", reward.getId(), "<col=ff0000>You just WON "+reward.getAmount()/1000000+" MILLIONS!<br>Continue to claim your reward.");
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
				player.getPackets().sendGameMessage("You open the millionaire's box and receive <img=10>"+Utils.getFormattedNumber(reward.getAmount())+" "+reward.getName()+" <img=10>!");
				if (!player.getInventory().hasFreeSlots()) {
					player.getPackets().sendGameMessage(reward.getName() +" x"+reward.getAmount()+" has been added to your bank.");
					player.getBank().addItem(reward.getId(), reward.getAmount(), false);
				} else
					player.getInventory().addItemMoneyPouch(reward);
				Logger.globalLog(player.getUsername(), player.getSession().getIP(), new String(" got " + reward.getId()+", "+reward.getAmount() + " from millionaire's box.("+ID+")"));
				player.setNextGraphics(new Graphics(rarity == 4 || rarity == 3 ? 1512 : rarity == 2 || rarity == 1 ? 1513 : 1514));
				Bot.sendLog(Bot.BOX_CHANNEL, "[type=MONEY-MBOX][name="+player.getUsername()+"]"+"[item="+reward.getName()+"("+reward.getId()+")" + "x" + reward.getAmount()+"]");
		
				if (rarity == 4) 
					World.sendNews(player, "LEGENDARY! "+Utils.formatPlayerNameForDisplay(player.getDisplayName())
					+ "just received <img=14><col=ffffff>" +Utils.getFormattedNumber(reward.getAmount())+" "+reward.getName()+"<col=D80000> <img=14> from <col=ffff00>millionaire's box!", 0);
				else if (rarity == 3)
					World.sendNews(player, "ULTRA RARE! "+Utils.formatPlayerNameForDisplay(player.getDisplayName())
					+ "just received <img=13><col=ffff00>" +Utils.getFormattedNumber(reward.getAmount())+" "+reward.getName()+"<col=D80000> <img=13> from <col=ffff00>millionaire's box!", 0);
				else if (rarity == 2)
					World.sendNews(player, "VERY RARE! "+Utils.formatPlayerNameForDisplay(player.getDisplayName())
					+ "just received <img=12><col=ff9933>" +Utils.getFormattedNumber(reward.getAmount())+" "+reward.getName()+"<col=D80000> <img=12> from <col=ffff00>millionaire's box!", 0);
				else if (rarity == 1)
					World.sendNews(player, " RARE! "+Utils.formatPlayerNameForDisplay(player.getDisplayName())
					+ " just received <img=11><col=00ACE6>" +Utils.getFormattedNumber(reward.getAmount())+" "+reward.getName()+"<col=D80000> <img=11> from <col=ffff00>millionaire's box!", 0);
			}
			
		};
		if (quickOpen) {
			closeEvent.run();
			return;
		}
		Collections.shuffle(rewards);
		player.getInterfaceManager().sendInterface(893);
		player.getPackets().sendIComponentText(893, 18, "Millionaire's Box");
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
	
	public static void preview(Player player) {
		int averagePerBox = BOX_PRICE * CASH_PER_DOLLAR; 
		List<Item> items = new ArrayList<Item>();
		items.add(new Item(995, (averagePerBox * 7) * 1000000));
		items.add(new Item(995, (int) ((averagePerBox * 3.5) * 1000000)));
		items.add(new Item(995, (int) ((averagePerBox * 2.5) * 1000000)));
		items.add(new Item(995, (int) ((averagePerBox * 2) * 1000000)));
		items.add(new Item(995, (int) ((averagePerBox * 0.75) * 1000000)));
		items.add(new Item(995, (int) ((averagePerBox * 0.4) * 1000000)));
		Item[] itemA = items.toArray(new Item[items.size()]);
		MysteryBox.preview(player, "Millionaire's Box Rewards", itemA);
	}
	
	public static void main(String[] args) {
		int tries = 1000000;
		long total = 0;
		
		int millionsPerDollar = 4;
		int boxPrice = 4;
		int averagePerBox = boxPrice * millionsPerDollar; 
		for (int i = 0; i < tries; i++) {
			if (Utils.random(100) == 0) 
				total += Utils.random((averagePerBox * 3.5), (averagePerBox * 7)+1);
			else if (Utils.random(25) ==0) 
				total += Utils.random(averagePerBox * 2.5, (averagePerBox * 3.5)+1);
			else if (Utils.random(10) == 0) 
				total += Utils.random(averagePerBox*2, (averagePerBox*2.5)+1);
			else if (Utils.random(4) == 0) 
				total += Utils.random(averagePerBox * 0.75, (averagePerBox * 2)+1);
			else 
				total += Utils.random(averagePerBox * 0.4, (averagePerBox * 0.75)+1);
		}
		long perBox = total / tries;
		System.out.println("per box: "+perBox);
	}
}
