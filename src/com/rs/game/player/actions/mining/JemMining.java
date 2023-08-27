package com.rs.game.player.actions.mining;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.pet.LuckyPets;
import com.rs.game.player.content.pet.LuckyPets.LuckyPet;
import com.rs.utils.Utils;

public class JemMining extends MiningBase {

	private WorldObject rock;
	private PickAxeDefinitions axeDefinitions;

	public JemMining(WorldObject rock) {
		this.rock = rock;
	}

	@Override
	public boolean start(Player player) {
		axeDefinitions = getPickAxeDefinitions(player, false);
		if (!checkAll(player))
			return false;
		player.getPackets().sendGameMessage("You swing your pickaxe at the rock.", true);
		setActionDelay(player, getMiningDelay(player));
		return true;
	}

	private int getMiningDelay(Player player) {
		int mineTimer = 50 - player.getSkills().getLevel(Skills.MINING) - Utils.getRandom(axeDefinitions.getPickAxeTime());
		if (mineTimer < 1 + 10)
			mineTimer = 1 + Utils.getRandom(10);
		mineTimer /= player.getAuraManager().getMininingAccurayMultiplier();
		if(mineTimer > 1)
			mineTimer /= 2;
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
		if (40 > player.getSkills().getLevelForXp(Skills.MINING)) {
			player.getPackets().sendGameMessage("You need a mining level of 45 to mine this rock.");
			return false;
		}
		return true;
	}

	@Override
	public boolean process(Player player) {
		player.setNextAnimation(new Animation(axeDefinitions.getAnimationId()));
		return checkRock(player);
	}

	@Override
	public int processWithDelay(Player player) {
		addOre(player);
		World.spawnObjectTemporary(new WorldObject(11193, rock.getType(), rock.getRotation(), rock.getX(), rock.getY(), rock.getPlane()), 10000, false, true);
		player.setNextAnimation(new Animation(-1));
		return -1;
	}

	private void addOre(Player player) {
		player.getSkills().addXp(Skills.MINING, 65);
		
		if (Utils.random(150) == 0)
			player.getInventory().addItem(1631, 1);
		else if (Utils.random(10000) == 0)
			player.getInventory().addItem(6571, 1);
		else {
			double random = Math.random() * 100;
			player.getInventory().addItem(random <= 3.5 ? 1617 : random <= 10 ? 1619 : random <= 20 ? 1621 : random <= 30 ? 1623 : random <= 50 ? 1629 : random <= 75 ? 1627 : 1625, 1);
		}
		player.getPackets().sendGameMessage("You receive a gem.", true);
		LuckyPets.checkPet(player, LuckyPet.ROCK_GOLEM);
	}

	private boolean checkRock(Player player) {
		return World.containsObjectWithId(rock, rock.getId());
	}
}
