/**
 * 
 */
package com.rs.game.minigames;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.executor.GameExecutorManager;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.firemaking.Firemaking;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * May 16, 2018
 */
public class EvilTrees {

	private static final WorldTile[] LOCATIONS = {
			
			new WorldTile(1634, 3678, 0), //home
			new WorldTile(2754, 3423, 0), //1
			new WorldTile(2759, 2697, 0), //2
			new WorldTile(2739, 3158, 0), //3
			new WorldTile(2706, 3505, 0), //4
			new WorldTile(3098, 3226, 0), //5
			new WorldTile(2741, 3330, 0), //6
			new WorldTile(2924, 3378, 0), //7
			new WorldTile(3049, 3457, 0), //8
			new WorldTile(2827, 3010, 0), //9
			new WorldTile(2735, 3410, 0), //10
			new WorldTile(2452, 3345, 0), //11
			new WorldTile(2522, 3103, 0), //12
			new WorldTile(1769, 3516, 0), //custom1
			new WorldTile(1586, 3409, 0) //custom2
			
	};
	private static final int MAX_HP = 500, REWARD_CAP = 100;
	private static TreeConfig config;
	private static WorldObject tree;
	private static int health;
	private static final List<WorldObject> roots = Collections.synchronizedList(new LinkedList<WorldObject>());
	private static final List<Integer> fires = Collections.synchronizedList(new LinkedList<Integer>());
	//You divve out of the way as a new root bursts from the ground
	
	public enum TreeConfig {
		NORMAL_EVIL_TREE(1512, 1, 15.1, 20, 4, 11434, 11435, 11436, 14839),
		OAK_EVIL_TREE(1522, 15, 32.4, 30, 4, 11437, 11438, 11439, 14840),
		WILLOW_EVIL_TREE(1520, 30, 45.7, 60, 4, 11440, 11441, 11442, 14841),
		MAPLE_EVIL_TREE(1518, 45, 55.8, 83, 16, 11443, 11444, 11915, 14842),
		YEW_EVIL_TREE(1516, 60, 87.5, 120, 17, 11916, 11917, 11918, 14843),
		MAGIC_EVIL_TREE(1514, 75, 125, 150, 21, 11919, 11920, 11921, 14844),
		ELDER_EVIL_TREE(1514, 85, 162.5, 150, 21, 11922, 11923, 11924, 14845),
		;
		
		private int logID;
		private int[] ids;
		private int level;
		private double xp;
		private int baseCutDelay;
		private int randomCutDelay;
		
		private TreeConfig(int logID, int level, double xp, int baseCutDelay, int randomCutDelay, int... ids) {
			this.logID = logID;
			this.level = level;
			this.xp = xp;
			this.baseCutDelay = baseCutDelay;
			this.randomCutDelay = randomCutDelay;
			this.ids = ids;
		}
		
		public int getLevel() {
			return level;
		}
		
		public double getXP() {
			return xp;
		}
		
		public int getBaseCutDelay() {
			return baseCutDelay;
		}
		
		public int getRandomCutDelay() {
			return randomCutDelay;
		}
	}
	
	public static void init() {
		setSpawnTask();
	}
	
	private static void destroyTree() {
		config = null;
		if (tree != null) {
			fires.clear();
			destroyRoots();
			World.removeObject(tree);
			tree = null;
		}
	}
	
	private static void destroyRoots() {
		for (WorldObject root : roots) 
			World.removeObject(root);
		roots.clear();
	}
	
	public static void destroyRoot(WorldObject root) {
		World.removeObject(root);
		roots.remove(root);
	}
	
	public static void claimRewards(Player player) {
		if (config == null)
			return;
		if ((Utils.currentTimeMillis() - player.getLastEvilTree()) <= (/*(player.isDonator() ? 1player.isRubyDonator() ? 1 : 12 : 24)**/60*60*1000)) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You already claimed your rewards.");
			return;
		}
		int rewardPerc = getRewardPerc(player);
		if (rewardPerc == 0) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You can not claim this reward.");
			return;
		}
		player.getTemporaryAttributtes().remove(Key.EVIL_TREE_DAMAGE);
		player.getDialogueManager().startDialogue("SimpleMessage", "Inside the hollow stump you found some items. For the next 15 minutes your logs will be autobanked if full inventory.");
		player.getInventory().addItemDrop(995, config.getLevel() * rewardPerc * 300); //min 10k, max 850k depending on tree and contribution. trippled
		player.getInventory().addItemDrop(config.logID, rewardPerc); //min 1. max 100 logs of the tree type dependig on contribution
		if (rewardPerc >= 100 && !player.getTreasureTrailsManager().hasClueScrollItem()) { //easy clue scroll if 100% reward
			player.getTreasureTrailsManager().resetCurrentClue();
			player.getInventory().addItemDrop(2677, 1);
		}
		if (Utils.random(10) == 0) 
			dropSet(player);
		player.setLastEvilTree(Utils.currentTimeMillis());
		//logs amt x contribution
		//1 clue scroll easy if 100% contribution
	}
	
	private static final int[] PIECES = {13659, 13660, 13661};
	
	private static void dropSet(Player player) {
		List<Integer> pieces = new ArrayList<Integer>();
		for (int i : PIECES)
			if (!player.containsItem(i))
				pieces.add(i);
		if (pieces.isEmpty())
			return;
		int piece = pieces.get(Utils.random(pieces.size()));
		player.getPackets().sendGameMessage("You feel your inventory getting heavier.");
		player.getInventory().addItemDrop(piece, 1);
		World.sendNews(player, player.getDisplayName() + " has received " + ItemConfig.forID(piece).getName() + " from evil tree!", 1);
	}
	
	
	//You dive out of the way as a new root bursts from the ground.
	public static void spawnRoot(Player player) {
		if (!player.withinDistance(tree, 3) && !containsFire(player))
			return;
		for (WorldObject root : roots)
			if (root.withinDistance(player, 0))
				return;
		WorldObject root = new WorldObject(11426, 10, 0, player);
		World.spawnObject(root);
		World.sendObjectAnimation(root, new Animation(353));
		roots.add(root);
		for (Entity p2 : World.getNearbyPlayers(player, false)) {
			if (p2.getX() == root.getX() && p2.getY() == root.getY() && p2.getPlane() == root.getPlane()) {
				Player player2 = (Player) p2;
				player2.stopAll();
				player2.lock(3);
				player2.getPackets().sendGameMessage("You dive out of the way as a new root bursts from the ground.");
				if (!player2.addWalkSteps(root.getX()+1, root.getY()))
					if (!player2.addWalkSteps(root.getX(), root.getY()+1))
						if (!player2.addWalkSteps(root.getX()-1, root.getY()))
							player2.addWalkSteps(root.getX(), root.getY()-1);
								
			}
		}
	}
	
	
	public static void spawnTree() {
		health = MAX_HP;
		config = TreeConfig.values()[Utils.random(TreeConfig.values().length)];
		WorldTile location = LOCATIONS[Utils.random(LOCATIONS.length)];
		World.spawnObject(tree = new WorldObject(config.ids[0], 10, 0, location));
	}
	
	public static boolean isTree(WorldObject object) {
		return tree != null && object.getId() == tree.getId();
	}
	
	public static TreeConfig getConfig() {
		return config;
	}
	
	public static void makeFire(Player player) {
		if (!isAlive())
			return;
		//player.getPackets().sendGameMessage("You can't light a fire here.");
		if (player.getSkills().getLevelForXp(Skills.FIREMAKING) < config.getLevel()) {
			player.getPackets().sendGameMessage("You do not have the required level to light this.");
			return;
		}
		if (!player.withinDistance(tree, 3) || containsFire(player)) {
			player.getPackets().sendGameMessage("That part of the tree is already on fire!");
			return;
		}
		if (containsNearbyRoot(player)) {
			player.getPackets().sendGameMessage("The root is moving too much for you to light a fire at its base.");
			return;
		}
		if (!player.getInventory().containsItem(14666, 1)) {
			player.getPackets().sendGameMessage("You do not have the required items to light this.");
			return;
		}
		player.lock(3);
		player.getPackets().sendGameMessage("You crouch to light the kindling.");
		player.setNextAnimation(new Animation(16700));
		player.getInventory().deleteItem(14666, 1);
		player.getSkills().addXp(Skills.FIREMAKING, Firemaking.increasedExperience(player, config.getXP()*16));
		WorldTile fireTile = new WorldTile(player);
		fires.add(fireTile.getTileHash());
		WorldTasksManager.schedule(new WorldTask() {

			int cycle;
			@Override
			public void run() {
				if (cycle++ > 30 || !isAlive() || !fires.contains(fireTile.getTileHash())) {
					fires.remove((Integer)fireTile.getTileHash());
					stop();
					return;
				}
				World.sendGraphics(null, new Graphics(453), fireTile);
				damage(player);
			}
			
		}, 0, 0);

	}
	
	public static WorldTile getTile() {
		return tree;
	}
	
	public static boolean containsFire(Player player) {
		return fires.contains(player.getTileHash());
	}
	
	public static boolean containsNearbyRoot(Player player) {
		for (WorldObject root : roots)
			if (root.withinDistance(player, 1))
				return true;
		return false;
	}
	public static void damage(Player player) {
		Integer damage = (Integer) player.getTemporaryAttributtes().get(Key.EVIL_TREE_DAMAGE);
		player.getTemporaryAttributtes().put(Key.EVIL_TREE_DAMAGE, damage == null ? 1 : (damage+1));
		health--;
		int stage = health <= 0 ? 3 : health <= MAX_HP/4 ? 2 : health <= MAX_HP / 2 ? 1 : 0;
		int id = config.ids[stage];
		if (id != tree.getId()) {//switches tree object
			World.spawnObject(tree = new WorldObject(id, 10, 0, tree));
			if (stage == 3) {
				fires.clear();
				destroyRoots();
			}
		}
	}
	
	public static int getHealthPerc() {
		return (int) ((double)health / (double)MAX_HP * 100d);
	}
	
	public static int getRewardPerc(Player player) {
		Integer damage = (Integer) player.getTemporaryAttributtes().get(Key.EVIL_TREE_DAMAGE);
		return damage == null ? 0 : Math.min(100, (int) ((double)damage / (double)REWARD_CAP * 100d));
	}
	
	public static boolean isAlive() {
		return tree != null && health > 0;
	}
	
	public static long NEXT_TREE;
	
	public static long getNextTree() {
		return NEXT_TREE - Utils.currentTimeMillis();
	}
	
	private static void setSpawnTask() {
		NEXT_TREE = (long) (Utils.currentTimeMillis() + 3600000 * 1.5);
		GameExecutorManager.fastExecutor.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				try {
					NEXT_TREE = (long) (Utils.currentTimeMillis() + 3600000 * 3);
					destroyTree();
					spawnTree();
					World.sendNews("<col=cc33ff>An evil tree just appeared! Talk to a spirit tree to find it.", 1);
					for (Player player : World.getPlayers()) {
						if (!player.hasStarted() || player.hasFinished())
							continue;
						player.getInterfaceManager().sendNotification("WARNING", "An evil tree just appeared!");
					}
				
				
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
			
		}, (long) (!Settings.HOSTED ? 10000 : (3600000 * 1.5)), 3600000 * 3); //every 3h also, but starts 1.5h after shooting stars
	}
	
}
