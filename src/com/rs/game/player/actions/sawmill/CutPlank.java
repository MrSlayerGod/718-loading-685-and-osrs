package com.rs.game.player.actions.sawmill;

import com.rs.game.Animation;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.woodcutting.Woodcutting;
import com.rs.game.player.controllers.SawmillController;

public class CutPlank extends Action {

	private SawmillController sawmill;
	private int amount;
	private int type;

	private static double[] XP =
	{ 15, 18, 25, 22 };

	public CutPlank(int type, int amount, SawmillController sawmill) {
		this.type = type;
		this.amount = amount;
		this.sawmill = sawmill;
	}

	@Override
	public boolean start(Player player) {
		return process(player);
	}

	@Override
	public boolean process(Player player) {
		if (!player.getInventory().containsItemToolBelt(8794, 1) && !player.getInventory().containsItem(9625, 1)) {
			player.getPackets().sendGameMessage("You need a saw to cut the planks.");
			return false;
		}
		if (!player.getInventory().containsItem(960, 1)) {
			player.getPackets().sendGameMessage("You have run out of planks.");
			return false;
		}
		return amount > 0;
	}

	/*
	 *   this.shortPlank = shortPlank;
	    this.longPlank = longPlank;
	    this.diagonalPlank = diagonalPlank;
	    this.toothPlank = toothPlank;
	    this.groovePlank = groovePlank;
	    this.curvedPlank = curvedPlank;
	 */

	@Override
	public int processWithDelay(Player player) {
		boolean crystalSaw = player.getInventory().containsItem(9625, 1);
		player.setNextAnimation(new Animation(crystalSaw ? 12382 : 12379));
		player.getSkills().addXp(Skills.WOODCUTTING, (crystalSaw ? XP[type] * 2 : XP[type]) * Woodcutting.getSetBonus(player));
		player.getInventory().deleteItem(new Item(960, 1));
		if (type == 0) {
			sawmill.addPlank(0, 1);
			sawmill.addPlank(1, 1);
		} else if (type == 1) {
			sawmill.addPlank(2, 2);
		} else if (type == 2) {
			sawmill.addPlank(3, 1);
			sawmill.addPlank(4, 1);
		} else {
			sawmill.addPlank(5, 1);
		}
		return amount-- == 1 ? -1 : 1;
	}

	@Override
	public void stop(Player player) {
		setActionDelay(player, 3);
	}

}
