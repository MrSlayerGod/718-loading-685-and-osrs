/**
 * 
 */
package com.rs.game.player.actions.woodcutting;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.minigames.EvilTrees;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Oct 30, 2017
 */
public class EvilRootWoodcutting extends WoodcuttingBase {

	private HatchetDefinitions hatchet;
	private WorldObject root;
	
	public EvilRootWoodcutting(WorldObject root) {
		this.root = root;
	}

	@Override
	public boolean start(Player player) {
		hatchet = getHatchet(player, false);
		if (!checkAll(player))
			return false;
		setActionDelay(player, 5 + Utils.random(5));
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

	@Override
	public boolean process(Player player) {
		player.setNextAnimation(new Animation(hatchet.emoteId));
		return checkTree(player);
	}

	private boolean checkTree(Player player) {
		return EvilTrees.isAlive() && World.containsObjectWithId(root, root.getId());
	}

	@Override
	public int processWithDelay(Player player) {
		player.getSkills().addXp(Skills.WOODCUTTING, EvilTrees.getConfig().getXP());
		player.getInventory().addItem(14666, 1);
		EvilTrees.destroyRoot(root);
		player.setNextAnimation(new Animation(-1));
		return -1;
	}

}
