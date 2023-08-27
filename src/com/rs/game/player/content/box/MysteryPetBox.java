/**
 * 
 */
package com.rs.game.player.content.box;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.rs.discord.Bot;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Oct 23, 2017
 */
public class MysteryPetBox {

	public static final int ID = 25432;

	private static final int[] COMMON = {/*1555, 1556, 1557, 1558, 1559, 1560, 7583, 14089, 7771,*/ 22994, 22993, 22995, 22992, 23030, /*12522, 12720, 12722, 12518, 12712, 12714, 12514, 12704, 12706, 12516, 12708, 12710,
			12520, 12716, 12718, 12512, 12700, 12702,*/ 14652, 14653, 14654, 14655, 14651, 15626, 15627, 15628, 15629, 15630, 15631, 15632,15633,15634,15635,15636,15637,15638,15639,12488,12738,12739,12740,12741,
			12551,12552,12553,14533,12481,12763,12765,18671,12500,12746,12748,12750,12752,12484,12724,12726,12728,12730,12732,12490,12754,12756,12758,12760,12503,12506,12509,13335,14627,14626,12486,12734,12736,
			19894,12498,12766,12768,12770,12772,12774,12492,12496,12682,12684,12686,12688,12690,12692,12694,12696,12698,12469,12471,12473,12475,21512,
			24511, 24512,
			//osrs
			43262,42646, 43178, 43247, 51291, 42647, 42654, 50851, 41995, 42644, 42645, 42643, 42816, 42650, 42652, 42655, 42649, 42648, 42921, 42651, 42653, 43181, 51273, 43177, 43179, 
			43326, 43322, 50659, 43320, 50665, 43321, 50663, 50661, 49730, 51992, 51748, 51750,
			
			25443, 25444, 25445, 25446, 52473, 52746
			
			//barrow pets
			, 25461, 25462, 25463, 25464, 25465, 25466
			
			
	};

	
	public static void preview(Player player) {
		List<Item> items = new ArrayList<Item>();
		for (int i : COMMON)
			items.add(new Item(i));
		Item[] itemA = items.toArray(new Item[items.size()]);
		MysteryBox.preview(player, "Pet Box Rewards", itemA);
		
	}
	
	public static void open(Player player, boolean quickOpen) {
		player.stopAll();
		player.lock(1); //just in case
		player.getInventory().deleteItem(ID, 1);
		
		List<Item> rewards = new ArrayList<Item>(MysteryBox.SLOTS.length);
		for (int item : COMMON)
			rewards.add(new Item(item));
		Collections.shuffle(rewards);
		Item reward = rewards.get(Utils.random(MysteryBox.SLOTS.length));
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
				player.getPackets().sendGameMessage("You open the pet mystery box and receive <img=10>"+Utils.getFormattedNumber(reward.getAmount())+" "+reward.getName()+" <img=10>!");
				if (!player.getInventory().hasFreeSlots()) {
					player.getPackets().sendGameMessage(reward.getName() +" x"+reward.getAmount()+" has been added to your bank.");
					player.getBank().addItem(reward.getId(), reward.getAmount(), false);
				} else
					player.getInventory().addItemMoneyPouch(reward);
				player.setNextGraphics(new Graphics(1515));
				Bot.sendLog(Bot.BOX_CHANNEL, "[type=PET-MBOX][name="+player.getUsername()+"]"+"[item="+reward.getName()+"("+reward.getId()+")" + "x" + reward.getAmount()+"]");
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
		int id = -1;
		for (int i = 0; i < 10; i++) {
			id = COMMON[Utils.random(COMMON.length)];
			if (!player.containsItem(i))
				break;
		}
		player.setNextGraphics(new Graphics(1765));
		player.getPackets().sendGameMessage("You open the mystery box and receive "+ItemConfig.forID(id).getName()+"!");
		player.getInventory().addItem(id, 1);
		Logger.globalLog(player.getUsername(), player.getSession().getIP(), new String(" got " + id + " from  pet mystery box."));

		Bot.sendLog(Bot.DONATIONS_CHANNEL, "[type=MBOX-PET][name="+player.getUsername()+"]"+"[item="+ItemConfig.forID(id).getName()+"("+id+")" + "x" + 1+"]");
		
	}*/
}
