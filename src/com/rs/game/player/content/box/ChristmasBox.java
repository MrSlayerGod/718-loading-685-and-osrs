package com.rs.game.player.content.box;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.npc.Drop;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.net.decoders.handlers.InventoryOptionsHandler;
import com.rs.utils.Colour;
import com.rs.utils.NPCDrops;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChristmasBox {

	public static final int ID = 25553;
	
	private static final int[] COSMETIC = {14600 , 14602, 14603, 14605, 981, 10507, 42887, 42888, 42889, 42890, 42891, 42892, 42894, 42895, 42896};
	private static final int[] COSMETIC_RARE = {1050, 43343, 43344, 1038, 1040, 1042, 1044, 1046, 1048};

	private static final int[] PVM = {14484,15220,15019,15018,41791,24992,24995,24998,25007,25004,25001,25022,11724,11726,11728,25019,25025,11718,11720,11722,25016,25010,25013,6199,25436,25453,11283,52002,25037,2577,6585,25031,25034,25028,42931,10551,42932,15486,42006,21371,42785,11848,11856,11854,11846,11852,11850, 14479, 42924, 11335, 51633, 6585, 11694, 11696, 11698, 11700, 6920, 43236, 43238, 43240};

	public static final int DYE = 25648, CRACKER = 25642;

	private static final int[] PVM_RARE = {
			25552, // hat
			25551, // top
			25550, // bot
			25549, // gloves
			25548, // boots
			25523};// upg gem
	public static final int[] EVIL_WEP = {
			25545, // wand
			25546, // xbow
			25547  // hammer
	};

	public static final int[] INFERNAL = {
			25634, // hat
			25635, // top
			25637, // bot
			25636, // gloves
			25638, // boot
			25643  // phat
	};

	public static final int[] INFERNAL_WEP = {
			25641,  // wand
			25639, // xbow
			25640  // hammer
	};

	public static void upgradeEvil(Player player, Item item) {
		if(!item.getDefinitions().getName().toLowerCase().contains("evil")) {
			player.sendMessage("This item cannot be dyed.");
			return;
		}

		int replace = -1;

		for(int i = 0; i < EVIL_WEP.length; i++) {
			if(item.getId() == EVIL_WEP[i])
				replace = INFERNAL_WEP[i];
		}

		for(int i = 0; i < PVM_RARE.length; i++) {
			if(item.getId() == PVM_RARE[i])
				replace = INFERNAL[i];
		}

		if(replace != -1) {
			player.sendMessage(Colour.ORANGE_RED.wrap("You infuse the infernal dye into the " + item.getDefinitions().getName() + " and create " + new Item(replace).getDefinitions().getName() + "!"));
			int slot = player.getInventory().getItems().getThisItemSlot(item);
			player.getInventory().getItem(slot).setId(replace);
			player.getInventory().deleteItem(DYE, 1);
			player.getInventory().refresh();
		} else {
			player.sendMessage("This item cannot be dyed.");
		}
	}

	public static void init() {
		InventoryOptionsHandler.register(CRACKER, 1, ((player, item) -> {
			openCracker(player);
		}));
	}
	public static void openCracker(Player player) {
		int roll = /*Utils.rollDie(3, 1) ? Utils.random(INFERNAL_WEP) :*/ Utils.random(INFERNAL);
		int slot = player.getInventory().getItems().getThisItemSlot(CRACKER);
		player.getInventory().getItem(slot).setId(roll);
		player.getInventory().refresh();
		player.getDialogueManager().startDialogue("ItemMessage",
				"Inside the Infernal cracker you find a " + Colour.RED.wrap(new Item(roll).getDefinitions().getName()) + "!", roll);
		World.sendNews(Colour.ORANGE_RED.wrap(player.getDisplayName()) + " has found " + Colour.ORANGE_RED.wrap(new Item(roll).getDefinitions().getName()) + " inside an " + Colour.ORANGE_RED.wrap("Infernal cracker") + "!", 0);
	}

	public static void preview(Player player) {
		List< Item > items = new ArrayList< Item >();
		for (int i : COSMETIC_RARE)
			items.add(new Item(i));
		for (int i : COSMETIC)
			items.add(new Item(i));
		items.add(new Item(CRACKER));
		for (int i : PVM_RARE)
			items.add(new Item(i));
		for (int i : PVM)
			items.add(new Item(i));
		Item[] itemA = items.toArray(new Item[items.size()]);
		MysteryBox.preview(player, "Christmas Box Rewards", itemA);
	}

	public static double MULTIPlIER = 1.0;
	public static void open(Player player) {
		if (player.getInventory().getFreeSlots() < 3) {
			player.getPackets().sendGameMessage("Not enough space in your inventory.");
			return;
		}
		player.getInventory().deleteItem(ID, 1);
		Item money = new Item(995, Utils.random(5_000_000, 15_000_000));
		Item comestic;
		if (Utils.random((int)(10 * MULTIPlIER)) == 0) {
			comestic = new Item(COSMETIC_RARE[Utils.random(COSMETIC_RARE.length)]);
			String strClr = "ff00ff";
			String highlightClr = "ff0000";
			World.sendNews(player, "<col="+ highlightClr + "><shad=0>RARE! <col=" + strClr + ">"+Utils.formatPlayerNameForDisplay(player.getDisplayName())
					+ " <col=" + strClr + ">just received <col="+highlightClr+">" +Utils.getFormattedNumber(comestic.getAmount())+" "+comestic.getName()+"<col="+strClr+"> from <col=ff0000><shad=ff981f>Christmas box!", 0);
		} else
			comestic = new Item(COSMETIC[Utils.random(COSMETIC.length)]);

		Item pvm;
		if (Utils.random((int)(100 * MULTIPlIER)) == 0) {
			pvm = new Item(CRACKER, 1);
			String highlightClr = Colour.ORANGE_RED.hex;
			String strClr = "ff981f";
			World.sendNews(player, "<col="+ highlightClr + "><shad=ff981f>INFERNAL! "+Utils.formatPlayerNameForDisplay(player.getDisplayName()) + " " +
					"<col="+strClr+ ">just received an <col=" + highlightClr + ">Infernal cracker", 0);
			World.sendNews(player, "from a <col=ff0000><shad=ff981f>Christmas box!", 0);
		/*} else if (Utils.random((int)(25 * MULTIPlIER)) == 0) {
			pvm = new Item(INFERNAL[Utils.random(INFERNAL.length)]);
			String strClr = "ff0000";
			String highlightClr = "ff981f";
			World.sendNews(player, "<col="+ highlightClr + "><shad=ff981f>INFERNAL! <col=" + strClr + ">"+Utils.formatPlayerNameForDisplay(player.getDisplayName())
					+ " <col=" + strClr + ">just received <img=11><col="+highlightClr+">" +Utils.getFormattedNumber(pvm.getAmount())+" "+pvm.getName(), 0);
			World.sendNews(player, "from a <col=ff0000><shad=ff981f>Christmas box!", 0);*/
		/*} else if (Utils.random((int)(50 * MULTIPlIER)) == 0) {
			pvm = new Item(EVIL_WEP[Utils.random(EVIL_WEP.length)]);
			String strClr = "ffff00";
			String highlightClr = "ff981f";
			World.sendNews(player, "<col="+ highlightClr + "><shad=0>EVIL WEP! <col=" + strClr + ">"+Utils.formatPlayerNameForDisplay(player.getDisplayName())
					+ " <col=" + strClr + ">just received <img=11><col="+highlightClr+">" +Utils.getFormattedNumber(pvm.getAmount())+" "+pvm.getName(), 0);
			World.sendNews(player, "from a <col=ff0000><shad=ff981f>Christmas box!", 0);
		*/} else if (Utils.random((int)(10 * MULTIPlIER)) == 0) {
			pvm = new Item(PVM_RARE[Utils.random(PVM_RARE.length)]);
			String strClr = "ffff00";
			String highlightClr = "ff981f";
			World.sendNews(player, "<col="+ highlightClr + "><shad=0>EVIL! <col=" + strClr + ">"+Utils.formatPlayerNameForDisplay(player.getDisplayName())
					+ " <col=" + strClr + ">just received <img=11><col="+highlightClr+">" +Utils.getFormattedNumber(pvm.getAmount())+" "+pvm.getName(), 0);
			World.sendNews(player, "from a <col=ff0000><shad=ff981f>Christmas box!", 0);
		} else {
			pvm = new Item(PVM[Utils.random(PVM.length)]);
			if(NPC.announceDrop(new Drop(pvm.getId(), pvm.getAmount(), pvm.getAmount()))) {
				String strClr = "ff981f";
				String highlightClr = "ffffff";
				World.sendNews(player, "<col="+ highlightClr + "><shad=0>RARE! <col=" + strClr + ">"+Utils.formatPlayerNameForDisplay(player.getDisplayName())
						+ " just received <img=11><col="+highlightClr+">" +Utils.getFormattedNumber(pvm.getAmount())+" "+pvm.getName(), 0);
				World.sendNews(player, "from a <col=ff0000><shad=ff981f>Christmas box!", 0);
			}
		}
		player.getPackets().sendGameMessage(Colour.ORANGE_RED.wrap("You open the <col=ff0000><shad=ff981f>Christmas box</shad></col> " + Colour.ORANGE_RED.wrap("and receive:")));
		player.getPackets().sendGameMessage(" +  " + Colour.ORANGE_RED.wrap(Utils.getFormattedNumber(money.getAmount())+" "+money.getName()));
		player.getPackets().sendGameMessage(" +  " + Colour.ORANGE_RED.wrap(Utils.getFormattedNumber(comestic.getAmount())+" "+comestic.getName()));
		player.getPackets().sendGameMessage(" +  " + Colour.ORANGE_RED.wrap(Utils.getFormattedNumber(pvm.getAmount())+" "+pvm.getName()));
		player.getInventory().addItemMoneyPouch(money);
		player.getInventory().addItemMoneyPouch(pvm);
		player.getInventory().addItemMoneyPouch(comestic);
		player.setNextAnimation(new Animation(4945));
		player.setNextGraphics(new Graphics(1601));
		player.getPackets().sendMusicEffect(302);
	}
}
