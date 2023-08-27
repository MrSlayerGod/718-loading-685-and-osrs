/**
 * 
 */
package com.rs.game.player.actions.woodcutting;

import java.util.ArrayList;
import java.util.List;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Animation;
import com.rs.game.npc.others.Ent;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Oct 30, 2017
 */
public class EntWoodcutting extends WoodcuttingBase {


	private Ent tree;
	private HatchetDefinitions hatchet;
	
	public EntWoodcutting(Ent tree) {
		this.tree = tree;
	}

	@Override
	public boolean start(Player player) {
		hatchet = getHatchet(player, false);
		if (!checkAll(player))
			return false;
		player.getPackets().sendGameMessage("You swing your hatchet at the Ent trunk.", true);
		setActionDelay(player, getWoodcuttingDelay(player));
		return true;
	}

	private boolean checkAll(Player player) {
		if (hatchet == null) {
			player.getPackets().sendGameMessage("You dont have the required level to use that axe or you don't have a hatchet.");
			return false;
		}
		if (!player.getInventory().hasFreeSlots()) {
			player.getPackets().sendGameMessage("Not enough space in your inventory.");
			return false;
		}
		if (!tree.canCut(player)) {
			player.getPackets().sendGameMessage("You must wait at least one minute before you can cut an ent that someone else defeated.");
			return false;
		}
		return true;
	}
	
	private int getWoodcuttingDelay(Player player) {
		int oreBaseTime = 25;
		int oreRandomTime = 10;
		int mineTimer = oreBaseTime - player.getSkills().getLevel(Skills.WOODCUTTING) - Utils.getRandom(hatchet.getAxeTime());
		if (mineTimer < 1 + oreRandomTime)
			mineTimer = 1 + Utils.getRandom(oreRandomTime);
		mineTimer /= player.getAuraManager().getWoodcuttingAccurayMultiplier();
		return mineTimer;
	}

	@Override
	public boolean process(Player player) {
		player.setNextAnimation(new Animation(hatchet.emoteId));
		return checkTree(player);
	}

	private boolean checkTree(Player player) {
		return !tree.hasFinished();
	}

	@Override
	public int processWithDelay(Player player) {
		addLog(player);
		if (Utils.random(5) == 0) {
			tree.takeLogs();
			player.setNextAnimation(new Animation(-1));
			return -1;
		}
		if (!player.getInventory().hasFreeSlots()) {
			player.setNextAnimation(new Animation(-1));
			player.getPackets().sendGameMessage("Not enough space in your inventory.");
			return -1;
		}
		return getWoodcuttingDelay(player);
	}
	
	public static void addLog(Player player) {
		player.getSkills().addXp(Skills.WOODCUTTING, 25);
		List<Integer> loot = new ArrayList<Integer>();
		int level = player.getSkills().getLevelForXp(Skills.WOODCUTTING);
		loot.add(1512);
		if (level >= 15)
			loot.add(1522);
		if (level >= 30)
			loot.add(1520);
		if (level >= 45)
			loot.add(1518);
		if (level >= 60)
			loot.add(1516);
		if (level >= 75)
			loot.add(1514);
		int id = loot.get(Utils.random(loot.size()));
		player.getInventory().addItem(id, 1);
		player.getPackets().sendGameMessage("You manage to chop some "+ItemConfig.forID(id).name.toLowerCase()+" from the carcass.");
	}

}
