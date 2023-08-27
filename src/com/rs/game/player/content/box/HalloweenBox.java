package com.rs.game.player.content.box;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.net.decoders.handlers.InventoryOptionsHandler;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HalloweenBox {

	public static final int ID = 25542;

	public static final int DYE = 25621;
	
	private static final int[] COSMETIC = {9921,9922,9923,9924,9925,1959,981,50095,50098,50101,50104,50107,/*9920,*/15352,11789};
	private static final int[] COSMETIC_RARE = {1053,1055,1057, 24437, DYE};
	
	private static final int[] PVM = {14484,15220,15019,15018,41791,24992,24995,24998,25007,25004,25001,25022,11724,11726,11728,25019,25025,11718,11720,11722,25016,25010,25013,6199,25436,25453,11283,52002,25037,2577,6585,25031,25034,25028,42931,10551,42932,15486,42006,21371,42785,11848,11856,11854,11846,11852,11850};
	private static final int[] PVM_RARE = {25534, 25535, 25536, 25537, 25538};
	public static final int[] DEMONIC_WEP = {25539, 25540, 25541};

	public static final int[] HALLOWED = {25611, 25612, 25613, 25614, 25615, 25616, 25619};

	public static final int[] HALLOWED_WEP = {25618, 25617, 25620};

	public static double MULTIPlIER = 1.0;

	public static void open(Player player) {
		if (player.getInventory().getFreeSlots() < 3) {
			player.getPackets().sendGameMessage("Not enough space in your inventory.");
			return;
		}
		player.getInventory().deleteItem(ID, 1);	
		Item money = new Item(995, Utils.random(10_000_000, 30_000_000));
		Item comestic;
		if (Utils.random((int)(10 * MULTIPlIER)) == 0) {
			comestic = new Item(COSMETIC_RARE[Utils.random(COSMETIC_RARE.length)]);
			World.sendNews(player, "RARE! "+Utils.formatPlayerNameForDisplay(player.getDisplayName())
			+ " just received <img=11><col=00ACE6>" +Utils.getFormattedNumber(comestic.getAmount())+" "+comestic.getName()+" from <col=ff9933>halloween box!", 0);
		} else
			comestic = new Item(COSMETIC[Utils.random(COSMETIC.length)]);

		Item pvm;
		if (Utils.random((int)(35 * MULTIPlIER)) == 0) {
			pvm = new Item(HALLOWED_WEP[Utils.random(HALLOWED_WEP.length)]);
			World.sendNews(player, "<col=900090><shad=<shad=ff981f>HALLOWED! <col=0>"+Utils.formatPlayerNameForDisplay(player.getDisplayName())
					+ " just received <img=11><col=900090>" +Utils.getFormattedNumber(pvm.getAmount())+" "+pvm.getName()+"<col=000000> from <col=900090>halloween box!", 0);
		} else if (Utils.random((int)(25 * MULTIPlIER)) == 0) {
			pvm = new Item(HALLOWED[Utils.random(HALLOWED.length)]);
			World.sendNews(player, "<col=900090><shad=<shad=ff981f>HALLOWED! <col=0>"+Utils.formatPlayerNameForDisplay(player.getDisplayName())
					+ " just received <img=11><col=900090>" +Utils.getFormattedNumber(pvm.getAmount())+" "+pvm.getName()+"<col=000000> from <col=900090>halloween box!", 0);
		} else if (Utils.random((int)(15 * MULTIPlIER)) == 0) {
			pvm = new Item(DEMONIC_WEP[Utils.random(DEMONIC_WEP.length)]);
			World.sendNews(player, "RARE! "+Utils.formatPlayerNameForDisplay(player.getDisplayName())
					+ " just received <img=11><col=00ACE6>" +Utils.getFormattedNumber(pvm.getAmount())+" "+pvm.getName()+" from <col=ff9933>halloween box!", 0);
		} else if (Utils.random((int)(10 * MULTIPlIER)) == 0) {
			pvm = new Item(PVM_RARE[Utils.random(PVM_RARE.length)]);
			World.sendNews(player, "RARE! "+Utils.formatPlayerNameForDisplay(player.getDisplayName())
			+ " just received <img=11><col=00ACE6>" +Utils.getFormattedNumber(pvm.getAmount())+" "+pvm.getName()+" from <col=ff9933>halloween box!", 0);
		} else
			pvm = new Item(PVM[Utils.random(PVM.length)]);
		player.getPackets().sendGameMessage("You open the halloween box and receive:");
		player.getPackets().sendGameMessage("<col=ff6600>"+Utils.getFormattedNumber(money.getAmount())+" "+money.getName());
		player.getPackets().sendGameMessage("<col=ff6600>"+Utils.getFormattedNumber(comestic.getAmount())+" "+comestic.getName());
		player.getPackets().sendGameMessage("<col=ff6600>"+Utils.getFormattedNumber(pvm.getAmount())+" "+pvm.getName());
		player.getInventory().addItemMoneyPouch(money);
		player.getInventory().addItemMoneyPouch(pvm);
		player.getInventory().addItemMoneyPouch(comestic);
		player.setNextAnimation(new Animation(4945));
		player.setNextGraphics(new Graphics(1601));
		player.getPackets().sendMusicEffect(302);
	}

	public static final int[][] HALLOWED_DYE_DATA = {
			{25534, 25611},
			{25535, 25612},
			{25536, 25613},
			{25537, 25614},
			{25538, 25615},
			{25539, 25617},
			{25540, 25618},
			{14484, 25619},
			{25541, 25620}
	};

	public static boolean dyeItem(Player player, Item itemUsed, Item usedWith) {
		for(int[] i : HALLOWED_DYE_DATA) {
			if(InventoryOptionsHandler.contains(DYE, i[0], itemUsed, usedWith)) {
				World.sendNews("<col=800080><shad=ff981f>" + player.getDisplayName() + " just created a " + ItemConfig.forID(i[1]).getName() + "!", 1);
				player.sendMessage("You dye the " + ItemConfig.forID(i[0]).getName() + " using the Hallowed dye, transforming it into a " + ItemConfig.forID(i[1]).getName() + "!");
				player.getInventory().deleteItem(i[0], 1);
				player.getInventory().deleteItem(DYE, 1);
				player.getInventory().addItem(i[1], 1);
				return true;
			}
		}

		return false;
	}

	public static void preview(Player player) {
		List< Item > items = new ArrayList< Item >();

		for(int i : HALLOWED)
			items.add(new Item(i));
		for(int i : PVM_RARE)
			items.add(new Item(i));
		for(int i : PVM)
			items.add(new Item(i));
		for(int i : COSMETIC_RARE)
			items.add(new Item(i));
		for(int i : COSMETIC)
			items.add(new Item(i));

		Item[] itemA = items.toArray(new Item[items.size()]);
		MysteryBox.preview(player, "<col=ff981f><shad=900090>Halloween Box Rewards", itemA);
	}
}
