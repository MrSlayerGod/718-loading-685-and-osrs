/**
 * 
 */
package com.rs.game.player.actions;

import com.rs.game.Animation;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;

/**
 * @author dragonkk(Alex)
 * Oct 2, 2017S
 */
public class JakindoFiremaking extends Action {

	private int amount;
	
	public JakindoFiremaking(int amount) {
		this.amount = amount;
	}
	
	@Override
	public boolean start(Player player) {
		if (!checkAll(player))
			return false;
		return true;
	}
	
	public boolean checkAll(Player player) {
		if (!player.getInventory().containsItemToolBelt(590) || !player.getInventory().containsItem(21350, 1)) {
			player.getPackets().sendGameMessage("You do not have the required items to light this.");
			return false;
		} else if (player.getSkills().getLevel(Skills.FIREMAKING) < 83) {
			player.getPackets().sendGameMessage("You do not have the required level to light this.");
			return false;
		}
		return true;
	}

	@Override
	public boolean process(Player player) {
		return checkAll(player);
	}

	@Override
	public int processWithDelay(Player player) {
		player.getInventory().deleteItem(21350, 1);
		player.setNextAnimation(new Animation(16700));
		player.getSkills().addXp(Skills.FIREMAKING, 378.7);
		player.setFavorPoints(3 + player.getFavorPoints());
		player.refreshFavorPoints();
		return --amount <= 0 ? -1 : 3;
	}

	@Override
	public void stop(Player player) {
		setActionDelay(player, 3);
	}

}
