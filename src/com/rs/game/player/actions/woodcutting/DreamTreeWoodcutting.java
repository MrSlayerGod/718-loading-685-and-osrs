/**
 * 
 */
package com.rs.game.player.actions.woodcutting;

import com.rs.game.Animation;
import com.rs.game.item.Item;
import com.rs.game.player.Player;

/**
 * @author dragonkk(Alex)
 * Oct 30, 2017
 */
public class DreamTreeWoodcutting extends WoodcuttingBase {

	public static double MULT = 2;
	
	private HatchetDefinitions hatchet;
	
	public DreamTreeWoodcutting() {

	}

	@Override
	public boolean start(Player player) {
		hatchet = getHatchet(player, false);
		if (!checkAll(player))
			return false;
		setActionDelay(player, getWoodcuttingDelay(player));
		return true;
	}

	private boolean checkAll(Player player) {
		if (hatchet == null) {
			player.getPackets().sendGameMessage("You dont have the required level to use that axe or you don't have a hatchet.");
			return false;
		}
		return checkTree(player);
	}
	
	private int getWoodcuttingDelay(Player player) {
	/*	int oreBaseTime = EvilTrees.getConfig().getBaseCutDelay();
		int oreRandomTime = EvilTrees.getConfig().getRandomCutDelay();
		int mineTimer = oreBaseTime - player.getSkills().getLevel(Skills.WOODCUTTING) - Utils.random(hatchet.getAxeTime());
		if (mineTimer < 1 + oreRandomTime)
			mineTimer = 1 + Utils.random(oreRandomTime);
		mineTimer /= player.getAuraManager().getWoodcuttingAccurayMultiplier();*/
		return 10;
	}

	@Override
	public boolean process(Player player) {
		player.setNextAnimation(new Animation(hatchet.emoteId));
		return checkTree(player);
	}

	private boolean checkTree(Player player) {
		if (!player.hasVotedInLast24Hours()) {
			player.sendMessage("You need to vote in order to cut the dream tree!");
			return false;
		}
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		//add money
		player.getInventory().addItemMoneyPouch(new Item(995, (int) (174.5d * player.getDropRateMultiplierI() * MULT)));
		return getWoodcuttingDelay(player);
	}

}
