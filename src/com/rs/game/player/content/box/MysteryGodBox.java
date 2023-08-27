/**
 * 
 */
package com.rs.game.player.content.box;

import java.util.ArrayList;
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

/**
 * @author dragonkk(Alex)
 * Oct 23, 2017
 */
public class MysteryGodBox {

	public static final int ID = 25453;
	
    public static final int[] NEX = {20135, 20139, 20143,//torva
            20147, 20151, 20155,//pernix
            20159, 20163, 20167, //virtus


    };//1 in 300

    public static final int[] NEX_GB = {24977, 24983,//torva gloves boots
            24974, 24989,//pernix gloves boots
            24980, 24986, //virtus gloves boots

    };//2%

	
	private static final int[] GWD_UNCOMMON = {
			41791, //staff of the dead
			25037, //armadyl crossbow
			20171, //zaryte bow
			23679, 23680, 23681, 23682,  //lucky godswords
			11716, 42808, //zspear, saradomin blessed sword instead of ss
			23684, 23685, 23686, //lucky armadyl
			23687, 23688, //lucky bandos
			25420, 25421, //lucky subj
			
			
	}; //25%
	private static final int[] GWD_COMMON = {
			25413, 25414, 25415, 11728, //lucky bandos, normal boots
			25416, 25417, 25418, //lucky armadyl
			25419, 25422, 25423, 25424, //lucky subj
			25028, 25031, 25034, //gwd amulets
	}; //50%
	
	private static final int[] JUNK = {
			2653, 2655, 2657, 2659, //zamorak rune
			2661, 2663, 2665, 2667, //sara rune
			2669, 2671, 2673, 2675, //guthix rune
			19413, 19416, 19419, 19422, 19425, //armadyl rune
			19428, 19431, 19434, 19437, 19440, //bandos rune
			19393, 19401, 19404, 19407, 19410, //ancient rune
			10440, 10442, 10444, 19362, 19364, 19366,//croziers
			10446, 10448, 10450, 19368, 19370, 19372, //cloak
			10452, 10454, 10456, 19374, 19376, 19378,//mitre
			10458, 10460, 10462, 19380, 19382, 19386, //robe top
			10464, 10466, 10468, 19386, 19388, 19390,//robe legs
			10470, 10472, 10474, 19392, 19394, 19396,//stole
			10368, 10370, 10372, 10374, //zamorak hide
			10376, 10378, 10380, 10382, //guthix hide
			10384, 10390, 10388, 10390, //saradomin hide
			19443, 19445, 19447, 19449, //ancient hide
			19451, 19453, 19455, 19457, //bandos hide
			19459, 19461, 19463, 19465, //armadyl hide
			
	}; //25%
	
	
	public static void preview(Player player) {
		List<Item> items = new ArrayList<Item>();
		for (int i : NEX)
			items.add(new Item(i));
		for (int i : NEX_GB)
			items.add(new Item(i));
		for (int i : GWD_UNCOMMON)
			items.add(new Item(i));
		for (int i : GWD_COMMON)
			items.add(new Item(i));
		Item[] itemA = items.toArray(new Item[items.size()]);
		MysteryBox.preview(player, "God Mystery Box Rewards", itemA);
	}
	

	public static void open(Player player,  boolean quickOpen) {
		player.stopAll();
		player.lock(1); //just in case
		player.getInventory().deleteItem(ID, 1);
		double MULTIPlIER = MysteryBox.MULTIPlIER;
		List<Item> rewards = new ArrayList<Item>(MysteryBox.SLOTS.length);
		List<Item> items = new ArrayList<Item>();
		for (int i : NEX)
			items.add(new Item(i));
		for (int i : NEX_GB)
			items.add(new Item(i));
		for (int i : GWD_UNCOMMON)
			items.add(new Item(i));
		for (int i : GWD_COMMON)
			items.add(new Item(i));
		for (int i = 0; i < 5; i++) {
			Item item = items.remove(Utils.random(items.size()));
			rewards.add(item);
		}
		int rarity = 0;
		Item reward;
		if (Utils.random(100) <= 1) { //1 in 50.
			reward = new Item(NEX[Utils.random(NEX.length)]);
			rarity = 4;
		} else {
			int chance = Utils.random(100);
			if (chance >= 42) {
				reward = new Item(GWD_COMMON[Utils.random(GWD_COMMON.length)]);
				rarity = 0;
			} else if (chance >= 6) {
				reward = new Item(GWD_UNCOMMON[Utils.random(GWD_UNCOMMON.length)]);
				rarity = 1;
			} else { // 0 and 1
				reward = new Item(NEX_GB[Utils.random(NEX_GB.length)]);
				rarity = 4;
			}
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
				player.getPackets().sendGameMessage("You open the god mystery box and receive <img=10>"+Utils.getFormattedNumber(reward.getAmount())+" "+reward.getName()+" <img=10>!");
				if (!player.getInventory().hasFreeSlots()) {
					player.getPackets().sendGameMessage(reward.getName() +" x"+reward.getAmount()+" has been added to your bank.");
					player.getBank().addItem(reward.getId(), reward.getAmount(), false);
				} else
					player.getInventory().addItemMoneyPouch(reward);
				Logger.globalLog(player.getUsername(), player.getSession().getIP(), new String(" got " + reward.getId()+", "+reward.getAmount() + " from mystery box.("+ID+")"));
				player.setNextGraphics(new Graphics(r == 4 || r == 3 ? 1512 : r == 2 || r == 1 ? 1513 : 1514));
				Bot.sendLog(Bot.BOX_CHANNEL, "[type=GOD-MBOX][name="+player.getUsername()+"]"+"[item="+reward.getName()+"("+reward.getId()+")" + "x" + reward.getAmount()+"]");
		
				if (r == 4) 
					World.sendNews(player, "LEGENDARY! "+Utils.formatPlayerNameForDisplay(player.getDisplayName())
					+ "just received <img=14><col=ffffff>" +Utils.getFormattedNumber(reward.getAmount())+" "+reward.getName()+"<col=D80000> <img=14> from a <col=ff9933>god mystery box!", 5);
				else if (r == 3)
					World.sendNews(player, "ULTRA RARE! "+Utils.formatPlayerNameForDisplay(player.getDisplayName())
					+ "just received <img=13><col=ffff00>" +Utils.getFormattedNumber(reward.getAmount())+" "+reward.getName()+"<col=D80000> <img=13> from a <col=ff9933>god mystery box!", 5);
				else if (r == 2)
					World.sendNews(player, "VERY RARE! "+Utils.formatPlayerNameForDisplay(player.getDisplayName())
					+ "just received <img=12><col=ff9933>" +Utils.getFormattedNumber(reward.getAmount())+" "+reward.getName()+" from a <col=ff9933>god mystery box!", 5);
				else if (r == 1)
					World.sendNews(player, " RARE! "+Utils.formatPlayerNameForDisplay(player.getDisplayName())
					+ " just received <img=11><col=00ACE6>" +Utils.getFormattedNumber(reward.getAmount())+" "+reward.getName()+" from a <col=ff9933>god mystery box!", 5);
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
/*
	public static void open(Player player) {
		player.getInventory().deleteItem(ID, 1);
		int item;
		if (Utils.random(300) == 0) {
			player.setNextGraphics(new Graphics(1765));
			item = NEX[Utils.random(NEX.length)];
			World.sendNews(player,
					"LEGENDARY! " + Utils.formatPlayerNameForDisplay(player.getDisplayName())
							+ "just received <img=14><col=ffffff>" + ItemConfig.forID(item).getName()
							+ "<col=D80000> <img=14> from <col=ffff00> god mystery box!",
					0);
			
			Bot.sendLog(Bot.DONATIONS_CHANNEL, "[type=MBOX-GOD][name="+player.getUsername()+"]"+"[item="+ItemConfig.forID(item).getName()+"("+item+")" + "x" + 1+"]");
			
		} else {
			int chance = Utils.random(100);
			if (chance >= 37) {
				player.setNextGraphics(new Graphics(1765));
				item = GWD_COMMON[Utils.random(GWD_COMMON.length)];
				World.sendNews(player,
						" RARE! " + Utils.formatPlayerNameForDisplay(player.getDisplayName())
								+ " just received <img=11><col=00ACE6>" + ItemConfig.forID(item).getName()
								+ "<col=D80000> <img=11> from <col=ff9933>god mystery box!",
						0);
				Bot.sendLog(Bot.DONATIONS_CHANNEL, "[type=MBOX-GOD][name="+player.getUsername()+"]"+"[item="+ItemConfig.forID(item).getName()+"("+item+")" + "x" + 1+"]");
				
			} else if (chance >= 2) {
				player.setNextGraphics(new Graphics(1765));
				item = GWD_UNCOMMON[Utils.random(GWD_UNCOMMON.length)];
				World.sendNews(player,
						" RARE! " + Utils.formatPlayerNameForDisplay(player.getDisplayName())
								+ " just received <img=11><col=00ACE6>" + ItemConfig.forID(item).getName()
								+ "<col=D80000> <img=11> from <col=ff9933>god mystery box!",
						0);
				Bot.sendLog(Bot.DONATIONS_CHANNEL, "[type=MBOX-GOD][name="+player.getUsername()+"]"+"[item="+ItemConfig.forID(item).getName()+"("+item+")" + "x" + 1+"]");
				
			} else { // 0 and 1
				player.setNextGraphics(new Graphics(1765));
				item = NEX_GB[Utils.random(NEX_GB.length)];
				World.sendNews(player,
						"LEGENDARY! " + Utils.formatPlayerNameForDisplay(player.getDisplayName())
								+ "just received <img=14><col=ffffff>" + ItemConfig.forID(item).getName()
								+ "<col=D80000> <img=14> from <col=ffff00> god mystery box!",
						0);
				Bot.sendLog(Bot.DONATIONS_CHANNEL, "[type=MBOX-GOD][name="+player.getUsername()+"]"+"[item="+ItemConfig.forID(item).getName()+"("+item+")" + "x" + 1+"]");
				
			}
		}
		player.getPackets().sendGameMessage(
				"You open the god mystery box and receive <img=10>" + ItemConfig.forID(item).getName() + " <img=10>!");
		player.getInventory().addItemMoneyPouch(new Item(item));
		player.setNextAnimation(new Animation(2414));
		player.setNextGraphics(new Graphics(1537));
		player.getPackets().sendMusicEffect(302);
	}*/
}
