/**
 * 
 */
package com.rs.game.player.actions.woodcutting;

import com.rs.game.Animation;
import com.rs.game.minigames.EvilTrees;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Oct 30, 2017
 */
public class EvilTreeWoodcutting extends WoodcuttingBase {

	private HatchetDefinitions hatchet;
	
	public EvilTreeWoodcutting() {

	}

	@Override
	public boolean start(Player player) {
		hatchet = getHatchet(player, false);
		if (!checkAll(player))
			return false;
		setActionDelay(player, getWoodcuttingDelay(player));
		player.getMusicsManager().playMusic(655);
		return true;
	}

	private boolean checkAll(Player player) {
		if (hatchet == null) {
			player.getPackets().sendGameMessage("You dont have the required level to use that axe or you don't have a hatchet.");
			return false;
		}
		int level = EvilTrees.getConfig().getLevel();
		if (level > player.getSkills().getLevelForXp(Skills.WOODCUTTING)) {
			player.getPackets().sendGameMessage("You need a woodcutting level of " + level + " to chop down this tree.");
			return false;
		}
		return checkTree(player);
	}
	
	private int getWoodcuttingDelay(Player player) {
		int oreBaseTime = EvilTrees.getConfig().getBaseCutDelay();
		int oreRandomTime = EvilTrees.getConfig().getRandomCutDelay();
		int mineTimer = oreBaseTime - player.getSkills().getLevel(Skills.WOODCUTTING) - Utils.random(hatchet.getAxeTime());
		if (mineTimer < 1 + oreRandomTime)
			mineTimer = 1 + Utils.random(oreRandomTime);
		mineTimer /= player.getAuraManager().getWoodcuttingAccurayMultiplier();
		return mineTimer;
	}

	@Override
	public boolean process(Player player) {
		player.setNextAnimation(new Animation(hatchet.emoteId));
		return checkTree(player);
	}

	private boolean checkTree(Player player) {
		return EvilTrees.isAlive();
	}

	@Override
	public int processWithDelay(Player player) {
		player.getSkills().addXp(Skills.WOODCUTTING, EvilTrees.getConfig().getXP());
		if (Utils.random(30) == 0) 
			player.getInventory().addItem(14666, 1);
		if (Utils.random(10) == 0) 
			EvilTrees.spawnRoot(player);
		EvilTrees.damage(player);
		if (!EvilTrees.isAlive()) { //killed it
			player.setNextAnimation(new Animation(-1));
			return -1;
		}
		return getWoodcuttingDelay(player);
	}

}
