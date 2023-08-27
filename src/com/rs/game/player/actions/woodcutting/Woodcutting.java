package com.rs.game.player.actions.woodcutting;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.cox.impl.IceDemon;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.Achievements.Task;
import com.rs.game.player.actions.firemaking.Firemaking;
import com.rs.game.player.actions.firemaking.Firemaking.Fire;
import com.rs.game.player.content.pet.LuckyPets;
import com.rs.game.player.content.pet.LuckyPets.LuckyPet;
import com.rs.utils.Utils;

public final class Woodcutting extends WoodcuttingBase {

	private static final int[] BIRD_NESTS =
	{ 5070, 5071, 5072, 5073, 5074, 5075, 7413, 11966 };

	public static enum TreeDefinitions {

		NORMAL(1, 25, 1511, 20, 4, 1341, 8, 0), // TODO

		DRAMEN(36, 0, 771, 20, 4, -1, 8, 0),

		EVERGREEN(1, 25, 1511, 20, 4, 57931, 8, 0),

		DEAD(1, 25, 1511, 20, 4, 12733, 8, 0),

		OAK(15, 37.5, 1521, 30, 4, 1341, 15, 15), // TODO

		WILLOW(30, 67.5, 1519, 60, 4, 5554, 51, 15), // TODO

		TEAK(35, 85, 6333, 30, 4, 9037, 60, 10),

		MAPLE(45, 100, 1517, 83, 16, 31057, 72, 10),

		MAHOGANY(50, 125, 6332, 95, 16, 9035, 83, 10),

		YEW(60, 175, 1515, 120, 17, 7402, 94, 10), // TODO

		IVY(68, 332.5, -1, 120, 17, 46319, 58, 10),

		MAGIC(75, 250, 1513, 150, 21, 37824, 121, 10),

		CURSED_MAGIC(82, 275, 1513, 150, 21, 37822, 121, 10),
		
		REDWOOD(90, 380, 49669, 120, 17, 129671, 58, 10),

		FRUIT_TREES(1, 25, -1, 20, 4, 1341, 8, 0),

		MUTATED_VINE(83, 140, 21358, 83, 16, 5, -1, 0),

		CURLY_VINE(83, 140, null, 83, 16, 12279, 72, 0),

		CURLY_VINE_COLLECTABLE(83, 140, new int[]
		{ 21350, 21350, 21350, 21350 }, 83, 16, 12283, 72, 0),

		STRAIT_VINE(83, 140, null, 83, 16, 12277, 72, 0),

		STRAIT_VINE_COLLECTABLE(83, 140, new int[]
		{ 21349, 21349, 21349, 21349 }, 83, 16, 12283, 72, 0),

		SAPLING(1, 20, IceDemon.KINDLING, 30, 4, -1, -1, 0),

		/**
		 * ADD NEW TREE TYPES BEFORE THIS LINE (DG TREES FOLLOW)
		 */
		TANGLE_GUM_VINE(1, 35, 17682, 20, 4, 49706, 8, 5),

		SEEPING_ELM_TREE(10, 60, 17684, 25, 4, 49708, 12, 5),

		BLOOD_SPINDLE_TREE(20, 85, 17686, 35, 4, 49710, 16, 5),

		UTUKU_TREE(30, 115, 17688, 60, 4, 49712, 51, 5),

		SPINEBEAM_TREE(40, 145, 17690, 76, 16, 49714, 68, 5),

		BOVISTRANGLER_TREE(50, 175, 17692, 85, 16, 49716, 75, 5),

		THIGAT_TREE(60, 210, 17694, 95, 16, 49718, 83, 10),

		CORPESTHORN_TREE(70, 245, 17696, 111, 16, 49720, 90, 10),

		ENTGALLOW_TREE(80, 285, 17698, 120, 17, 49722, 94, 10),

		GRAVE_CREEPER_TREE(90, 330, 17700, 150, 21, 49724, 121, 10),

		;

		private int level;
		private double xp;
		private int[] logsId;
		private int logBaseTime;
		private int logRandomTime;
		private int stumpId;
		private int respawnDelay;
		private int randomLifeProbability;

		private TreeDefinitions(int level, double xp, int[] logsId, int logBaseTime, int logRandomTime, int stumpId, int respawnDelay, int randomLifeProbability) {
			this.level = level;
			this.xp = xp;
			this.logsId = logsId;
			this.logBaseTime = logBaseTime;
			this.logRandomTime = logRandomTime;
			this.stumpId = stumpId;
			this.respawnDelay = respawnDelay;
			this.randomLifeProbability = randomLifeProbability;
		}

		private TreeDefinitions(int level, double xp, int logsId, int logBaseTime, int logRandomTime, int stumpId, int respawnDelay, int randomLifeProbability) {
			this(level, xp, new int[]
			{ logsId }, logBaseTime, logRandomTime, stumpId, respawnDelay, randomLifeProbability);
		}

		public int getLevel() {
			return level;
		}

		public double getXp() {
			return xp;
		}

		public int[] getLogsId() {
			return logsId;
		}

		public int getLogBaseTime() {
			return logBaseTime;
		}

		public int getLogRandomTime() {
			return logRandomTime;
		}

		public int getStumpId() {
			return stumpId;
		}

		public int getRespawnDelay() {
			return respawnDelay;
		}

		public int getRandomLifeProbability() {
			return randomLifeProbability;
		}
	}

	private WorldObject tree;
	private TreeDefinitions definitions;
	private HatchetDefinitions hatchet;

	private boolean usingBeaver;

	public Woodcutting(WorldObject tree, TreeDefinitions definitions, boolean usingBeaver) {
		this.tree = tree;
		this.definitions = definitions;
		this.usingBeaver = usingBeaver;
	}

	public Woodcutting(WorldObject tree, TreeDefinitions definitions) {
		this(tree, definitions, false);
	}

	@Override
	public boolean start(Player player) {
		if (!checkAll(player))
			return false;
		player.getPackets().sendGameMessage(usingBeaver ? "Your beaver uses its strong ivory teeth to chop down the tree..." : "You swing your hatchet at the " + (TreeDefinitions.IVY == definitions ? "ivy" : "tree") + "...", true);
		setActionDelay(player, getWoodcuttingDelay(player));
		return true;
	}

	private int getWoodcuttingDelay(Player player) {
		int mapID = player.getRegionId();
		int summoningBonus = player.getFamiliar() != null ? (player.getFamiliar().getId() == 6808 || player.getFamiliar().getId() == 6807) ? 10 : 0 : 0;
		int wcTimer = definitions.getLogBaseTime() - (player.getSkills().getLevel(8) + summoningBonus + (mapID == 6198 || mapID == 6454 ? 7 : 0)) - Utils.random(hatchet.axeTime);
		if (wcTimer < 1 + definitions.getLogRandomTime())
			wcTimer = 1 + Utils.random(definitions.getLogRandomTime());
		wcTimer /= player.getAuraManager().getWoodcuttingAccurayMultiplier();
		if (wcTimer > 1) //wcing speeded up
			wcTimer /= 2;
		return wcTimer;
	}
	
	private boolean checkAll(Player player) {
		hatchet = getHatchet(player, definitions.ordinal() >= TreeDefinitions.TANGLE_GUM_VINE.ordinal());
		if (hatchet == null) {
			player.getPackets().sendGameMessage("You don't have the required level to use that axe or you don't have a hatchet.");
			return false;
		}
		if (!hasWoodcuttingLevel(player))
			return false;
		if (!player.getInventory().hasFreeSlots()) {
			player.getPackets().sendGameMessage("Not enough space in your inventory.");
			return false;
		}
		return true;
	}

	private boolean hasWoodcuttingLevel(Player player) {
		if (definitions.getLevel() > player.getSkills().getLevelForXp(8)) {
			player.getPackets().sendGameMessage("You need a woodcutting level of " + definitions.getLevel() + " to chop down this tree.");
			return false;
		}
		return true;
	}

	@Override
	public boolean process(Player player) {
		if (usingBeaver) {
			player.getFamiliar().setNextAnimation(new Animation(7722));
			player.getFamiliar().setNextGraphics(new Graphics(1458));
		} else
			player.setNextAnimation(new Animation(definitions == TreeDefinitions.IVY ? hatchet.ivyEmoteID : hatchet.emoteId));
		return checkTree(player);
	}

	private boolean usedDeplateAurora;

	@Override
	public int processWithDelay(Player player) {
		boolean dungeoneering = definitions.ordinal() > 18;
		addLog(hatchet, definitions, dungeoneering, usingBeaver, player);
		if (!usedDeplateAurora && (1 + Math.random()) < player.getAuraManager().getChanceNotDepleteMN_WC()) {
			usedDeplateAurora = true;
		} else if (definitions.stumpId != -1 && Utils.random(definitions.getRandomLifeProbability()) == 0) {
			long time = definitions.respawnDelay * 600;
			if (player.isAtVipZone() && time >= 3000)
				time = 3000;
			if (time < 0) {
				if (dungeoneering) {
					World.spawnObject(new WorldObject(tree.getId() + 1, tree.getType(), tree.getRotation(), tree));
					player.getPackets().sendGameMessage("You have depleted this resource.");
				} else
					World.removeObject(tree);
			} else
				World.spawnObjectTemporary(new WorldObject(dungeoneering ? tree.getId() + 1 : definitions.getStumpId(), tree.getType(), tree.getRotation(), tree.getX(), tree.getY(), tree.getPlane()), time, false, true);
			if (tree.getPlane() < 3 && definitions != TreeDefinitions.SAPLING &&  definitions != TreeDefinitions.IVY && definitions != TreeDefinitions.REDWOOD) {
				WorldObject object = World.getStandartObject(new WorldTile(tree.getX() - 1, tree.getY() - 1, tree.getPlane() + 1));

				if (object == null) {
					object = World.getStandartObject(new WorldTile(tree.getX(), tree.getY() - 1, tree.getPlane() + 1));
					if (object == null) {
						object = World.getStandartObject(new WorldTile(tree.getX() - 1, tree.getY(), tree.getPlane() + 1));
						if (object == null) {
							object = World.getStandartObject(new WorldTile(tree.getX(), tree.getY(), tree.getPlane() + 1));
						}
					}
				}

				if (object != null)
					World.removeObjectTemporary(object, time);
			}
			player.setNextAnimation(new Animation(-1));
			return -1;
		}
		
		if (!dungeoneering && !player.getInventory().hasFreeSlots() && definitions.logsId[0] != -1 && (Utils.currentTimeMillis() - player.getLastEvilTree()) <= 15*60*1000) {
			int logsCount = player.getInventory().getAmountOf(definitions.logsId[0]);
			if (logsCount != 0) {
				player.getPackets().sendGameMessage("The evil tree power automatically banks your logs.");
				player.getInventory().deleteItem(definitions.logsId[0], logsCount);
				player.getBank().addItem(definitions.logsId[0], logsCount, false);
			}
		}
			
		
		if (!player.getInventory().hasFreeSlots()) {
			player.setNextAnimation(new Animation(-1));
			player.getPackets().sendGameMessage("Not enough space in your inventory.");
			return -1;
		}
		return getWoodcuttingDelay(player);
	}

	public static double getSetBonus(Player player) {
		double xpBoost = 1.00;
		if (player.getEquipment().getChestId() == 10939)
			xpBoost += 0.01;
		if (player.getEquipment().getLegsId() == 10940)
			xpBoost += 0.01;
		if (player.getEquipment().getHatId() == 10941)
			xpBoost += 0.01;
		if (player.getEquipment().getBootsId() == 10933)
			xpBoost += 0.01;
		if (player.getEquipment().getChestId() == 10939 && player.getEquipment().getLegsId() == 10940 && player.getEquipment().getHatId() == 10941 && player.getEquipment().getBootsId() == 10933)
			xpBoost += 0.01;
		return xpBoost;
	}
	
	public static void addLog(HatchetDefinitions hatchet, TreeDefinitions definitions, boolean dungeoneering, boolean usingBeaver, Player player) {
		if (definitions == TreeDefinitions.NORMAL)
			player.getAchievements().add(Task.CHOP_LOGS);
		else if (definitions == TreeDefinitions.MAPLE)
			player.getAchievements().add(Task.CHOP_MAPLE_LOGS);
		else if (definitions == TreeDefinitions.YEW)
			player.getAchievements().add(Task.CHOP_YEW_LOGS);
		else if (definitions == TreeDefinitions.MAGIC)
			player.getAchievements().add(Task.CHOP_MAGIC_LOGS);

		if (!usingBeaver) {
			player.getSkills().addXp(8, definitions.getXp() * getSetBonus(player));
		}
		if (definitions.getLogsId() != null) {
			if(definitions == TreeDefinitions.SAPLING) {
				int wc = player.getSkills().getLevel(Skills.WOODCUTTING);
				player.getInventory().addItem(definitions.getLogsId()[0], wc <= 1 ? 1 : (int) ((double) wc * 0.20));
			} else if (usingBeaver) {
				if (player.getFamiliar() != null) {
					for (int item : definitions.getLogsId())
						player.getInventory().addItemDrop(item, 1);
				}
			} else {
				for (int item : definitions.getLogsId()) {
					if (hatchet == HatchetDefinitions.INFERNAL && Utils.random(3) == 0) {
						Fire fire = Firemaking.getFire(item);
						if (fire != null) {
							player.getSkills().addXp(Skills.FIREMAKING, fire.getExperience()/2);
							continue;
						}
					}
					player.getInventory().addItemDrop(item, 1);
				}
				if (!dungeoneering && Utils.random(50) == 0) {
					int nest = BIRD_NESTS[Utils.random(BIRD_NESTS.length)];
					if (!((nest == 5070 || nest == 11966 || nest == 5071 || nest == 5072) && Utils.random(10) != 0)) {
						player.getInventory().addItemDrop(nest, 1);
						player.getPackets().sendGameMessage("A bird's nest falls out of the tree!");
					}
				}
			}
			if (definitions == TreeDefinitions.IVY) {
				player.getPackets().sendGameMessage("You succesfully cut an ivy vine.", true);
				// todo gfx
			} else {
				String logName = ItemConfig.forID(definitions.getLogsId()[0]).getName().toLowerCase();
				player.getPackets().sendGameMessage("You get some " + logName + ".", true);
				// todo infernal adze
			}
		}
		LuckyPets.checkPet(player, LuckyPet.BEAVER);
	}

	private boolean checkTree(Player player) {
		return World.containsObjectWithId(tree, tree.getId());
	}

}
