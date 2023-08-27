package com.rs.game.player.actions.mining;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.npc.others.zalcano.Zalcano;
import com.rs.game.player.CoalBag;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.Achievements.Task;
import com.rs.game.player.actions.Smelting.SmeltingBar;
import com.rs.game.player.actions.woodcutting.Woodcutting.TreeDefinitions;
import com.rs.game.player.content.pet.LuckyPets;
import com.rs.game.player.content.pet.LuckyPets.LuckyPet;
import com.rs.utils.Utils;

public final class Mining extends MiningBase {

	private static final int[] UNCUT_GEMS =
	{ 1623, 1619, 1621, 1617, 1631, 6571 };

	public static enum RockDefinitions {

		Clay_Ore(1, 5, 434, 10, 1, 11552, 5, 0),

		Copper_Ore(1, 17.5, 436, 10, 1, 11552, 5, 0),

		Tin_Ore(1, 17.5, 438, 15, 1, 11552, 5, 0),

		Blurite_Ore(10, 17.5, 668, 15, 1, 11552, 7, 0),
		
		Limestone(10, 26.5, 3211, 15, 1, 11552, 7, 0),

		Iron_Ore(15, 35, 440, 15, 1, 11552, 10, 0),

		Sandstone_Ore(35, 30, 6971, 30, 1, 11552, 10, 0),

		Silver_Ore(20, 40, 442, 25, 1, 11552, 20, 0),

		Coal_Ore(30, 50, 453, 50, 10, 11552, 30, 0),

		Granite_Ore(45, 50, 6979, 50, 10, 11552, 20, 0),

		Gold_Ore(40, 60, 444, 80, 20, 11554, 40, 0),

		Mithril_Ore(55, 80, 447, 100, 20, 11552, 60, 0),

		Adamant_Ore(70, 95, 449, 130, 25, 11552, 180, 0),

		Runite_Ore(85, 125, 451, 150, 30, 11552, 360, 0),

		LRC_Coal_Ore(77, 50, 453, 50, 10, -1, -1, -1),

		LRC_Gold_Ore(80, 60, 444, 40, 10, -1, -1, -1),

		RedSandStone(81, 70, 23194, 50, 10, -1, -1, -1),

		Tephra(70, 35, Zalcano.TEPHRA, 50, 1, -1, -1, -1);

		private int level;
		private double xp;
		private int oreId;
		private int oreBaseTime;
		private int oreRandomTime;
		private int emptySpot;
		private int respawnDelay;
		private int randomLifeProbability;

		private RockDefinitions(int level, double xp, int oreId, int oreBaseTime, int oreRandomTime, int emptySpot, int respawnDelay, int randomLifeProbability) {
			this.level = level;
			this.xp = xp;
			this.oreId = oreId;
			this.oreBaseTime = oreBaseTime;
			this.oreRandomTime = oreRandomTime;
			this.emptySpot = emptySpot;
			this.respawnDelay = respawnDelay;
			this.randomLifeProbability = randomLifeProbability;
		}

		public int getLevel() {
			return level;
		}

		public double getXp() {
			return xp;
		}

		public int getOreId() {
			return oreId;
		}

		public int getOreBaseTime() {
			return oreBaseTime;
		}

		public int getOreRandomTime() {
			return oreRandomTime;
		}

		public int getEmptyId() {
			return emptySpot;
		}

		public int getRespawnDelay() {
			return respawnDelay;
		}

		public int getRandomLifeProbability() {
			return randomLifeProbability;
		}
	}

	private WorldObject rock;
	private RockDefinitions definitions;
	private PickAxeDefinitions axeDefinitions;

	public Mining(WorldObject rock, RockDefinitions definitions) {
		this.rock = rock;
		this.definitions = definitions;
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
		int summoningBonus = 0;
		if (player.getFamiliar() != null) {
			if (player.getFamiliar().getId() == 7342 || player.getFamiliar().getId() == 7342)
				summoningBonus += 10;
			else if (player.getFamiliar().getId() == 6832 || player.getFamiliar().getId() == 6831)
				summoningBonus += 1;
		}
		int mineTimer = definitions.getOreBaseTime() - (player.getSkills().getLevel(Skills.MINING) + summoningBonus) - Utils.getRandom(axeDefinitions.getPickAxeTime());
		if (mineTimer < 1 + definitions.getOreRandomTime())
			mineTimer = 1 + Utils.getRandom(definitions.getOreRandomTime());
		mineTimer /= player.getAuraManager().getMininingAccurayMultiplier();
		if (mineTimer > 1) //mining speeded up
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
		if (definitions.getLevel() > player.getSkills().getLevel(Skills.MINING)) {
			player.getPackets().sendGameMessage("You need a mining level of " + definitions.getLevel() + " to mine this rock.");
			return false;
		}
		return true;
	}

	@Override
	public boolean process(Player player) {
		player.setNextAnimation(new Animation(axeDefinitions.getAnimationId()));
		return checkRock(player);
	}

	private boolean usedDeplateAurora;

	@Override
	public int processWithDelay(Player player) {
		addOre(player);
		if (definitions.getEmptyId() != -1) {
			if (!usedDeplateAurora && (1 + Math.random()) < player.getAuraManager().getChanceNotDepleteMN_WC()) {
				usedDeplateAurora = true;
			} else if (Utils.getRandom(definitions.getRandomLifeProbability()) == 0) {
				World.spawnObjectTemporary(new WorldObject(rock.getDefinitions().name.endsWith("vein") ? 20439 : definitions.getEmptyId(), rock.getType(), rock.getRotation(), rock.getX(), rock.getY(), rock.getPlane()), definitions.respawnDelay * 600, false, true);
				player.setNextAnimation(new Animation(-1));
				return -1;
			}
		}
		if (!player.getInventory().hasFreeSlots() && definitions.getOreId() != -1) {
			player.setNextAnimation(new Animation(-1));
			player.getPackets().sendGameMessage("Not enough space in your inventory.");
			return -1;
		}
		return getMiningDelay(player);
	}

	private void addOre(Player player) {
		if (definitions == RockDefinitions.Copper_Ore
				|| definitions == RockDefinitions.Tin_Ore)
			player.getAchievements().add(Task.MINE_COPPER_AND_TIN);
		else if (definitions == RockDefinitions.Mithril_Ore)
			player.getAchievements().add(Task.MINE_MITHRIL_ORE);
		else if (definitions == RockDefinitions.Adamant_Ore)
			player.getAchievements().add(Task.MINE_ADAMANT_ORE);
		else if (definitions == RockDefinitions.Runite_Ore)
			player.getAchievements().add(Task.MINE_RUNITE_ORE);
		double xpBoost = 0;
		int idSome = 0;
		if (definitions == RockDefinitions.Granite_Ore) {
			idSome = Utils.getRandom(2) * 2;
			if (idSome == 2)
				xpBoost += 10;
			else if (idSome == 4)
				xpBoost += 25;
		} else if (definitions == RockDefinitions.Sandstone_Ore) {
			idSome = Utils.getRandom(3) * 2;
			xpBoost += idSome / 2 * 10;
		} else if (definitions == RockDefinitions.RedSandStone) {
			if (player.getRedStoneDelay() >= Utils.currentTimeMillis()) {
				player.getPackets().sendGameMessage("It seems that there is no remaining ore, check again in twelve hours.");
				stop(player);
				return;
			}
			player.increaseRedStoneCount();
			if (player.getRedStoneCount() >= 75 + player.getDonator() * 25) {
				player.resetRedStoneCount();
				player.setStoneDelay(3600000 * 12); // 12 hours
				player.getVarsManager().sendVarBit(10133, 26);
			} else if (player.getRedStoneCount() == 125)
				player.getVarsManager().sendVarBit(10133, 25);
		}
		double totalXp = definitions.getXp() + xpBoost;
		totalXp *= getMiningSuitMultiplier(player);
		player.getSkills().addXp(Skills.MINING, totalXp);
		if (definitions.getOreId() != -1) {
			int oreID = definitions.getOreId() + idSome;
			if (axeDefinitions == PickAxeDefinitions.INFERNAL && Utils.random(3) == 0) {
				SmeltingBar bar = SmeltingBar.getBarWithOre(oreID);
				player.getSkills().addXp(Skills.SMITHING, bar != null ? (bar.getExperience()/3) : (definitions.getLevel() / 5));
			} else {
				if (definitions.getOreId() == 453 && player.getInventory().containsItem(CoalBag.OPENED_COAL_BAG_ID, 1)
						&& !player.getCoalBag().isFull())
					player.getCoalBag().add(1);
				else
					player.getInventory().addItem(oreID, 1);
			}
			if ((Utils.currentTimeMillis() - player.getLastStarSprite()) <= 15*60*1000)
				player.getInventory().addItem(definitions.getOreId() + idSome, 1);
			String oreName = ItemConfig.forID(definitions.getOreId() + idSome).getName().toLowerCase();
			player.getPackets().sendGameMessage(
					oreID == Zalcano.TEPHRA ? "You manage to mine some tephra." : ("You mine some " + oreName + "."), true);
			if (Utils.random(150) == 0) {
				int gem = oreID != Zalcano.TEPHRA ? UNCUT_GEMS[Utils.random(UNCUT_GEMS.length - 2)] : Zalcano.CRYSTAL_SHARD;
				player.getInventory().addItemDrop(gem, 1);
			} else if (Utils.random(10000) == 0)
				player.getInventory().addItemDrop(UNCUT_GEMS[Utils.random(UNCUT_GEMS.length)], 1);
		}
		LuckyPets.checkPet(player, LuckyPet.ROCK_GOLEM);
	}

	private boolean checkRock(Player player) {
		return World.containsObjectWithId(rock, rock.getId());
	}
}
