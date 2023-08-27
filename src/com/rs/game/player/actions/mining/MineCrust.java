/**
 * 
 */
package com.rs.game.player.actions.mining;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.minigames.LavaFlowMine;
import com.rs.game.npc.others.LiquidGoldNymph;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.pet.LuckyPets;
import com.rs.game.player.content.pet.LuckyPets.LuckyPet;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Mar 8, 2018
 */
public class MineCrust extends MiningBase {
	
	private int count;
	private WorldObject object;
	private PickAxeDefinitions axeDefinitions;
	
	public MineCrust(WorldObject object) {
		this.object = object;
	}

	@Override
	public boolean start(Player player) {
		axeDefinitions = getPickAxeDefinitions(player, false);
		if (!checkAll(player))
			return false;
		player.getPackets().sendGameMessage("You swing your pickaxe at the rock.");
		setActionDelay(player, getMiningDelay(player));
		return true;
	}

	@Override
	public boolean process(Player player) {
		player.setNextAnimation(new Animation(axeDefinitions.getAnimationId()));
		return checkAll(player) && checkRock(player);
	}
	
	private boolean checkRock(Player player) {
		return World.containsObjectWithId(object, object.getId());
	}

	private int getMiningDelay(Player player) {
		int summoningBonus = 0;
		if (player.getFamiliar() != null) {
			if (player.getFamiliar().getId() == 7342
					|| player.getFamiliar().getId() == 7342)
				summoningBonus += 10;
			else if (player.getFamiliar().getId() == 6832
					|| player.getFamiliar().getId() == 6831)
				summoningBonus += 1;
		}
		int oreBaseTime = 50;
		int oreRandomTime = 20;
		int mineTimer = oreBaseTime
				- (player.getSkills().getLevel(Skills.MINING) + summoningBonus)
				- Utils.getRandom(axeDefinitions.getPickAxeTime());
		if (mineTimer < 1 + oreRandomTime)
			mineTimer = 1 + Utils.getRandom(oreRandomTime);
		mineTimer /= player.getAuraManager().getMininingAccurayMultiplier();
		return mineTimer;
	}

	private boolean checkAll(Player player) {
		if (axeDefinitions == null) {
			player.getPackets().sendGameMessage("You do not have a pickaxe or do not have the required level to use the pickaxe.");
			return false;
		}
		if (!hasMiningLevel(player))
			return false;
		if (!player.getInventory().hasFreeSlots()) {
			player.getPackets().sendGameMessage("Not enough space in your inventory.");
			return false;
		}
		return true;
	}

	private boolean hasMiningLevel(Player player) {
		if (player.getSkills().getLevel(Skills.MINING) < 68) {
			player.getPackets().sendGameMessage("You need a mining level of " + 68 + " to mine this rock.");
			return false;
		}
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		player.getSkills().addXp(Skills.MINING, LavaFlowMine.getXp(object) * getMiningSuitMultiplier(player));
		player.getPackets().sendGameMessage("You mine away some crust.", true);
		if(Utils.random(200) == 0 && player.getTemporaryAttributtes().get("LiquidGoldNymph") == null) {
			new LiquidGoldNymph(player);
			player.getPackets().sendGameMessage("<col=FF0000>A Liquid Gold Nymph emerges from the mined away crust!");
		}
		LuckyPets.checkPet(player, LuckyPet.ROCK_GOLEM);
		if (count++ >= 30) 
			return -1;
		return getMiningDelay(player);
	}

}