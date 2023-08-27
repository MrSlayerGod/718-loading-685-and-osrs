
package com.rs.game.player;

import java.io.Serializable;

import com.rs.game.minigames.clanwars.FfaZone;
import com.rs.game.player.controllers.DungeonController;
import com.rs.utils.Utils;

public class MoneyPouch implements Serializable {

	private static final long serialVersionUID = -3847090682601697992L;

	private transient Player player;
	private boolean usingPouch;
	private int coinAmount;
	private long coinAmountL;

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void switchPouch() {
		usingPouch = !usingPouch;
		swap();
	}

	public void init() {
		if (coinAmount != 0) { //convert to long
			coinAmountL = coinAmount;
			coinAmount = 0;
		}
			
		if (usingPouch)
			swap();
		//refreshCoins();
	}

	private void swap() {
		player.getPackets().sendExecuteScript(5557, 1);
	}

	public void examinePouch() {
		player.getPackets().sendGameMessage("Your money pouch currently contains " + Utils.getFormattedNumber(coinAmountL) + " coins.");
	}

	public void withdrawPouch() {
		if(player.tournamentResetRequired()) {
			player.sendMessage("You cannot access your money pouch within a PK Tournament.");
			return;
		}
		if (player.getControlerManager().getControler() instanceof DungeonController) {
			player.getPackets().sendGameMessage("You cannot access your money pouch within the walls of Daemonheim.");
			return;
		}
		if (!player.getBank().hasVerified(12))
			return;
	//	player.getPackets().sendInputIntegerScript("Your money pouch contains " + Utils.getFormattedNumber(coinAmountL) + " coins.<br>How many would you like to withdraw?");
	//	player.getTemporaryAttributtes().put("withdrawingPouch", Boolean.TRUE);
		player.getDialogueManager().startDialogue("WithdrawPouch");
	}

	public void refreshCoins() {
		//player.getPackets().sendExecuteScript(5560, (int)coinAmountL);
		
		
		String color = coinAmountL > 10000000 ? "<col=00FF00>" : coinAmountL > 100000 ? "<col=ffffff>" : "<col=ffff00>";
		player.getPackets().sendIComponentText(player.getInterfaceManager().isResizable() ? 746 : 548, 
				player.getInterfaceManager().isResizable() ? 206 : 196, color+formatMoney(Long.toString(coinAmountL))); //to allow over max
	
		//548, 196
	}

	public boolean sendDynamicInteraction(long amount, boolean remove) {
		return sendDynamicInteraction(amount, remove, TYPE_INV);
	}

	public static final int TYPE_POUCH_INVENTORY = 0, TYPE_REMOVE = 1, TYPE_INV = 2;

	/*
	 * TYPE_POUCH_INVENTORY - from pouch to inventory TYPE_REMOVE - remove from
	 * pouch as much as it can(example bank) TYPE_INV - remove/add from pouch
	 * and if not enough, inventory
	 */
	public boolean sendDynamicInteraction(long amount, boolean remove, int type) {
		if (amount == 0)
			return false;
		if(player.tournamentResetRequired() || player.getControlerManager().getControler() instanceof FfaZone)
			return false;
		if (remove) {
			if (type == TYPE_POUCH_INVENTORY) {
				if (amount > coinAmountL)
					amount = coinAmountL;
				int invAmt = player.getInventory().getAmountOf(995);
				if (coinAmountL != 0 && invAmt + amount > Integer.MAX_VALUE) {
					amount = Integer.MAX_VALUE - invAmt;
					player.getPackets().sendGameMessage("Not enough space in your inventory.");
				}
			} else if (type == TYPE_INV && amount > coinAmountL) {
				long removeAmt = amount - coinAmountL;
				if (player.getInventory().getAmountOf(995) < removeAmt)
					return false;
				player.getInventory().deleteItem(995, (int) removeAmt);
				amount -= removeAmt;
			}
		} /*else if (!remove && amount + coinAmountL <= 0) {
			if (type == TYPE_INV) // added from somewhere else example shop but
				// moneypouch full so adds to inv
				player.getInventory().addItem(995, amount - (Integer.MAX_VALUE - coinAmountL));
			else
				player.getPackets().sendGameMessage("Your money-pouch is currently full. Your coins will now go to your inventory.");
			amount = Integer.MAX_VALUE - coinAmountL;
		}*/
		if (amount == 0)
			return true;
		player.getPackets().sendGameMessage(Utils.getFormattedNumber(amount) + " coins have been " + (remove ? "removed" : "added") + " to your money pouch.");
		if (type == TYPE_POUCH_INVENTORY) {
			if (remove) {
				if (!player.getInventory().addItem(995, (int) amount))
					return false;
			} else
				player.getInventory().deleteItem(995, (int) amount);
		}
		setAmount(amount, remove);
		return true;
	}
	
	public boolean setPlatinumToken(long amount, boolean remove) {
		if (amount == 0)
			return false;
		if(player.tournamentResetRequired())
			return false;
		if (remove) {
			if (amount * 1000 > coinAmountL)
				amount = coinAmountL / 1000;
			int invAmt = player.getInventory().getAmountOf(43204);
			if (coinAmountL != 0 && invAmt + amount > Integer.MAX_VALUE) {
				amount = Integer.MAX_VALUE - invAmt;
				player.getPackets().sendGameMessage("Not enough space in your inventory.");
			}
		}
		if (amount == 0)
			return true;
		player.getPackets().sendGameMessage(Utils.getFormattedNumber(amount * 1000) + " coins have been " + (remove ? "removed" : "added") + " to your money pouch.");
		if (remove) {
			if (!player.getInventory().addItem(43204, (int) amount))
				return false;
		} else
			player.getInventory().deleteItem(43204, (int) amount);
		setAmount(amount * 1000, remove);
		return true;
	}

	void setAmount(long amt, boolean remove) {
		if (remove)
			coinAmountL -= amt;
		else
			coinAmountL += amt;

		player.getPackets().sendExecuteScript(5561, remove ? 0 : 1, (int) Math.min(Integer.MAX_VALUE, amt));
		
		
		player.getPackets().sendIComponentText(player.getInterfaceManager().isResizable() ? 746 : 548, 
				player.getInterfaceManager().isResizable() ? 213 : 199, (remove ? "-" : "+")+ formatMoney(Long.toString(amt))); //to allow over max
		//
		refreshCoins();
	}
	
    public static String formatMoney(String value) {
        if (value.length() > 16)
            value = value.substring(0, value.length() - 15) + "Q";
        else if (value.length() > 13)
            value = value.substring(0, value.length() - 12) + "T";
        else if (value.length() > 10)
            value = value.substring(0, value.length() - 9) + "B";
        else if (value.length() > 7)
            value = value.substring(0, value.length() - 6) + "M";
        else if (value.length() > 3)
            value = value.substring(0, value.length() - 3) + "K";
        return value;
    }

	public long getCoinsAmount() {
		return coinAmountL;
	}

	public void setCoinsAmount(long amt) {
		// TODO remove
		coinAmountL = amt;
	}
}