package com.rs.game.player.actions.sawmill;

import com.rs.game.Animation;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.actions.Action;
import com.rs.game.player.controllers.SawmillController;

public class ConveyorBeltHopper extends Action {

	private SawmillController sawmill;
	private int amount;

	public ConveyorBeltHopper(int amount, SawmillController sawmill) {
		this.amount = amount;
		this.sawmill = sawmill;
	}

	@Override
	public boolean start(Player player) {
		return process(player);
	}

	@Override
	public boolean process(Player player) {
		if (sawmill.isPlanksFull()) {
			player.getPackets().sendGameMessage("The conveyor belt is full.");
			return false;
		}
		if (!player.getInventory().containsItem(1511, 1)) {
			player.getPackets().sendGameMessage("You have no logs to put on the conveyor belt.");
			return false;
		}
		return amount > 0;
	}

	@Override
	public int processWithDelay(Player player) {
		player.setNextAnimation(new Animation(12398));
		player.getInventory().deleteItem(new Item(1511));
		sawmill.addPlank();
		return amount-- == 1 ? -1 : 1;
	}

	@Override
	public void stop(Player player) {
		setActionDelay(player, 3);
	}

}
