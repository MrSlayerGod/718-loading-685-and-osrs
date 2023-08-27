/**
 * 
 */
package com.rs.game.player.actions.thieving;

import com.rs.game.Animation;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.actions.woodcutting.WoodcuttingBase;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.ObjectHandler;

/**
 * @author Simplex
 * @since Oct 22, 2020
 */
public class HalloweenStallThieving extends WoodcuttingBase {

	public static void init() {
		ObjectHandler.register(104874, 2, (player, object)
				-> player.getActionManager().setAction(new HalloweenStallThieving()));
	}

	public static double MULT = 1;

	@Override
	public boolean start(Player player) {
		player.sendMessage("You begin thieving from the stall.");
		setActionDelay(player, 10);
		processWithDelay(player);
		return true;
	}

	@Override
	public boolean process(Player player) {
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		//add money
		player.setNextAnimation(new Animation(881));
		WorldTasksManager.schedule(()
				-> player.getInventory().addItemMoneyPouch(new Item(995, (int) (174.5d * player.getDropRateMultiplierI() * MULT))));
		return 10;
	}

}
