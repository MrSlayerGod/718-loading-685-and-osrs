package com.rs.game.player.content.box;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class EasterBox {

	public static final int ID = 25586;
	
	private static final int[] COSMETIC = { 24149, 24150, 11021, 11020, 11022, 11019, 43663, 43664, 4566, 14728, 24145, 52351, 52353, 50433, 50436, 50439, 50442};
	private static final int[] COSMETIC_RARE = {1037, 53448, 4084, 7927, 1038, 1040, 1042, 1044, 1046, 1048};

	
	private static final int[] PVM = {14484,15220,15019,15018,41791,24992,24995,24998,25007,25004,25001,25022,11724,11726,11728,25019,25025,11718,11720,11722,25016,25010,25013,6199,25436,25453,11283,52002,25037,2577,6585,25031,25034,25028,42931,10551,42932,15486,42006,21371,42785,11848,11856,11854,11846,11852,11850};
	private static final int[] PVM_RARE = {25585, 25584, 25583, 25582, 25581, 25580, 25579};

	
	public static void open(Player player) {
		if (player.getInventory().getFreeSlots() < 3) {
			player.getPackets().sendGameMessage("Not enough space in your inventory.");
			return;
		}
		player.getInventory().deleteItem(ID, 1);	
		Item money = new Item(995, Utils.random(10000000, 30000000));
		Item comestic;
		double MULTIPlIER = MysteryBox.MULTIPlIER;

		if (Utils.random((int)(100 * MULTIPlIER)) == 0) {
			comestic = new Item(25664);
			World.sendNews(player, "<shad=ffff00>ULTRA RARE! "+Utils.formatPlayerNameForDisplay(player.getDisplayName())
					+ " just received <img=11><col=00ACE6>" +Utils.getFormattedNumber(comestic.getAmount())+" "+comestic.getName()+" from <col=ff9933>Easter Mystery Basket!", 0);
		} else if (Utils.random((int)(10 * MULTIPlIER)) == 0) {
			comestic = new Item(COSMETIC_RARE[Utils.random(COSMETIC_RARE.length)]);
			World.sendNews(player, "<shad=ffff00>RARE! "+Utils.formatPlayerNameForDisplay(player.getDisplayName())
			+ " just received <img=11><col=00ACE6>" +Utils.getFormattedNumber(comestic.getAmount())+" "+comestic.getName()+" from <col=ff9933>Easter Mystery Basket!", 0);
		} else
			comestic = new Item(COSMETIC[Utils.random(COSMETIC.length)]);
		Item pvm;
		if (Utils.random((int)(10 * MULTIPlIER)) == 0) {
			pvm = new Item(PVM_RARE[Utils.random(PVM_RARE.length)]);
			World.sendNews(player, "RARE! "+Utils.formatPlayerNameForDisplay(player.getDisplayName())
			+ " just received <img=11><col=00ACE6>" +Utils.getFormattedNumber(pvm.getAmount())+" "+pvm.getName()+" from <col=ff9933>Easter Mystery Basket!", 0);
		} else
			pvm = new Item(PVM[Utils.random(PVM.length)]);
		player.getPackets().sendGameMessage("You open the Easter Mystery Basket and receive:");
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
}
