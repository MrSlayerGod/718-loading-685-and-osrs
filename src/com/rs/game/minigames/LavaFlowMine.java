/**
 * 
 */
package com.rs.game.minigames;

import java.util.TimerTask;

import com.rs.executor.GameExecutorManager;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

/**
 * @author https://www.rune-server.ee/runescape-development/rs-503-client-server/snippets/480044-lava-flow-mine.html
 * Improved by dragonkk(Alex)
 * TODO improve it further
 * Mar 8, 2018
 */
public class LavaFlowMine {

	private static int row;
	private static int rowIndex;

	private static int minute;
	private static int second;

	private static int boilerLevel;
	

	// NEEDED - boiler animation
	//FOREMAN JAAK - "Get to work, there's a blockage in channel -"
	//"What do I have to do here?"
	//Hammer is one west of southwest near door
	//G-pickaxe emote - 250, 12188 - normal
	//NORMAL rune - 624
	//inferno - 10222
	//The Tzhaar hero has caught the lava monster: Increased chance of finding gems for ten minutes
	public static final int[] MINING_SUIT = { 20788, 20787, 20789, 20790, 20791 };
	public static final int[] PICKAXES = { 1265, 1267, 1269, 1271, 1273, 1275, 15259 };
	public static final int[] GILDED = { 20780, 20781, 20782, 20783, 20784, 20785, 20786 };
	

	public static final int[] MINE_AREAS = { 2208, 5633, 2210, 5638, 3, 0, 2208, 5641, 2210, 5646, 3, 1, 2208, 5649, 2210, 5654, 3, 2, 2208, 5657, 2210, 5662, 3, 3, 2208, 5665, 2210, 5670, 3, 4, 2208, 5673, 2210, 5678, 3, 5, 2208, 5681, 2210, 5686, 3, 6, 2208, 5689, 2210, 5694, 3, 7, 2202, 5633, 2204, 5638, 2, 0, 2202, 5641, 2204, 5646, 2, 1, 2202, 5649, 2204, 5654, 2, 2, 2202, 5657, 2204, 5662, 2, 3, 2202, 5665, 2204, 5670, 2, 4, 2202, 5673, 2204, 5678, 2, 5, 2202, 5681, 2204, 5686, 2, 6, 2202, 5689, 2204, 5694, 2, 7, 2193, 5633, 2195, 5638, 1, 0, 2193, 5641, 2195, 5646, 1, 1, 2193, 5649, 2195, 5654, 1, 2, 2193, 5657, 2195, 5662, 1, 3, 2193, 5665, 2195, 5670, 1, 4, 2194, 5673, 2193, 5673, 1, 5, 2193, 5681, 2195, 5686, 1, 6, 2193, 5689, 2195, 5694, 1, 7, 2187, 5633, 2189, 5638, 0, 0, 2187,
			5641, 2189, 5646, 0, 1, 2187, 5649, 2189, 5654, 0, 2, 2187, 5657, 2189, 5662, 0, 3, 2187, 5665, 2189, 5670, 0, 4, 2187, 5673, 2189, 5678, 0, 5, 2187, 5681, 2189, 5686, 0, 6, 2187, 5689, 2189, 5694, 0, 7 };

	public static final int[] GUAGE_INDEXES = { 2185, 5690, 0, 7, 2185, 5682, 0, 6, 2185, 5674, 0, 5, 2185, 5666, 0, 4, 2185, 5658, 0, 3, 2185, 5650, 0, 2, 2185, 5642, 0, 1, 2185, 5634, 0, 0, 2191, 5634, 1, 0, 2191, 5637, 0, 0, 2191, 5642, 1, 1, 2191, 5645, 0, 1, 2191, 5650, 1, 2, 2191, 5653, 0, 2, 2191, 5658, 1, 3, 2191, 5661, 0, 3, 2191, 5666, 1, 4, 2191, 5669, 0, 4, 2191, 5674, 1, 5, 2191, 5677, 0, 5, 2191, 5682, 1, 6, 2191, 5685, 0, 6, 2191, 5690, 1, 7, 2191, 5693, 0, 7, 2197, 5693, 1, 7, 2200, 5690, 2, 7, 2197, 5685, 1, 6, 2200, 5682, 2, 6, 2197, 5677, 1, 5, 2200, 5674, 2, 5, 2197, 5669, 1, 4, 2200, 5666, 2, 4, 2197, 5661, 1, 3, 2200, 5658, 2, 3, 2197, 5653, 1, 2, 2200, 5650, 2, 2, 2197, 5645, 1, 1, 2200, 5642, 2, 1, 2197, 5637, 1, 0, 2200, 5634, 2, 0, 2206, 5634, 3, 0, 2206, 5637, 2, 0,
			2206, 5642, 3, 1, 2206, 5645, 2, 1, 2206, 5650, 3, 2, 2206, 5653, 2, 2, 2206, 5658, 3, 3, 2206, 5661, 2, 3, 2206, 5666, 3, 4, 2206, 5669, 2, 4, 2206, 5674, 3, 5, 2206, 5677, 2, 5, 2206, 5682, 3, 6, 2206, 5685, 2, 6, 2206, 5690, 3, 7, 2206, 5693, 2, 7, 2212, 5693, 3, 7, 2212, 5685, 5, 6, 2212, 5677, 3, 5, 2212, 5669, 3, 4, 2212, 5661, 3, 3, 2212, 5653, 3, 2, 2212, 5645, 3, 1, 2212, 5637, 3, 0 };


	private static WorldObject brokenBoiler;
	private static WorldObject actualBoiler;
	
	public static void init() {
		row = Utils.random(3);
		rowIndex = Utils.random(7);
		minute = 4;
		second = 0;
		GameExecutorManager.fastExecutor.schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					process();
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
			
		}, 1000, 1000);

	}
	
	//TODO process every min
	public static void process() {
		if (second-- <= 0 && minute > 0) {
			minute--;
			second = 60;
		}
		if (second <= 0 && minute <= 0) {
			if (Utils.random(200) == 10) {
				switchMine();
				second = 0;
				minute = 4;
			}
		}
		if (Utils.random(700) == 0)
			if (brokenBoiler == null) 
				breakBoiler();
	}

	// 5679 5639

	public static void breakBoiler() {
		int x = 2198;
		int y = 5639;
		while (true) {
			if (y > 5679)
				y = 5639;
			if (Utils.random(10) == 1)
				break;
			y += 8;
		}
		boilerLevel = Utils.random(70, 90);
		actualBoiler = World.getObjectWithId(new WorldTile(x, y, 0), 57186);
		World.spawnObject(brokenBoiler = new WorldObject(57187, 10, 0, new WorldTile(x, y, 0)));
	}

	public static boolean boilerBroken() {
		return brokenBoiler != null;
	}

	public static int getBoilerLevel() {
		return boilerLevel;
	}
	
	public static void fixBoiler(Player player) {
		if (!boilerBroken())
			return;
		if (player.getSkills().getLevel(Skills.SMITHING) < boilerLevel) {
			player.getPackets().sendGameMessage("You need a smithing level of "+boilerLevel+" to fix this boiler.");
			return;
		}
		player.setNextAnimation(new Animation(player.getInventory().containsOneItem(14112, 14104) ? 3923 : 898));
		player.setNextGraphics(new Graphics(2123));
		player.getSkills().addXp(Skills.SMITHING, 150 * boilerLevel - 7500);
		player.getPackets().sendGameMessage("You repair the boiler.");
		World.removeObject(brokenBoiler);
		if (actualBoiler != null)
			World.spawnObject(actualBoiler);
		/*World.getRegion(8792).removeObject(brokenBoiler);
		for (Player p2 : World.getPlayers()) {
			if (p2 == null || !p2.hasStarted() || p2.hasFinished() || !p2.getMapRegionsIds().contains(8792))
				continue;
			if (actualBoiler != null)
				p2.getPackets().sendSpawnedObject(actualBoiler);
			else
				p2.getPackets().sendDestroyObject(brokenBoiler);
		}*/
		brokenBoiler = null;
		actualBoiler = null;
	}
	
	public static void sendGuageDialogue(Player player, WorldObject object) {
		int[] i = getIndex(object);
		int row = i[0];
		int rowIndex = i[1];
		int percentage = getPercentage(i);
		player.getDialogueManager().startDialogue("SimpleMessage", "Channel "+getLetter(row)+" - Segment "+(rowIndex + 1)+"<br>Flow rate "+percentage+"%");
	}
	
	public static String getLetter(int row) {
		switch(row) {
		case 0:
			return "A";
		case 1:
			return "B";
		case 2:
			return "C";
		case 3:
			return "D";
		}
		return "A";
	}

	public static void switchMine() {
		row = Utils.random(3);
		rowIndex = Utils.random(7);
		for (Player player : World.getPlayers())
			if (player.getRegionId() == 8792)
				player.getPackets().sendGameMessage("<col=FF0000>The flow blockage has moved to a new segment.");
	}

	public static WorldTile getNymphTile(Player player) {
		int playerX = player.getX();
		int count = 2;
		if (playerX == 2190 || playerX == 2196 || playerX == 2205 || playerX == 2211)
			count = -2;
		return new WorldTile(playerX + count, player.getY(), 0);
	}
	

	public static int getXp(WorldTile tile) {
		int percentage = getPercentage(getIndex(tile));
		if (percentage == 100)
			return 50;
		if (percentage == 90)
			return 60;
		if (percentage == 80)
			return 70;
		if (percentage == 70)
			return 80;
		if (percentage == 60)
			return 90;
		if (percentage == 50)
			return 100;
		return 50;
	}

	public static int getPercentage(int... i) {
		if (i[0] != row)
			return 100;
		int index = i[1];
		if (rowIndex == index)
			return 50;
		if (index == rowIndex - 1 || index == rowIndex + 1)
			return 60;
		if (index == rowIndex - 2 || index == rowIndex + 2)
			return 70;
		if (index == rowIndex - 3 || index == rowIndex + 3)
			return 80;
		if (index == rowIndex - 4 || index == rowIndex + 4)
			return 90;
		return 100;
	}
	
	public int[] getBestIndex() {
		return new int[] { row, rowIndex };
	}

	public static int[] getIndex(WorldTile tile) {
		int count = 0;
		while (count < GUAGE_INDEXES.length) {
			int x = GUAGE_INDEXES[count++];
			int y = GUAGE_INDEXES[count++];
			int row = GUAGE_INDEXES[count++];
			int index = GUAGE_INDEXES[count++];
			if (tile.getX() == x && tile.getY() == y)
				return new int[] { row, index };
		}
		count = 0;
		while (count < MINE_AREAS.length) {
			int blx = MINE_AREAS[count++];
			int bly = MINE_AREAS[count++];
			WorldTile bl = new WorldTile(blx, bly, 0);
			int trx = MINE_AREAS[count++];
			int trY = MINE_AREAS[count++];
			WorldTile tr = new WorldTile(trx, trY, 0);
			int row = MINE_AREAS[count++];
			int index = MINE_AREAS[count++];
		//	WorldArea area = new WorldArea(bl, tr);
			if (/*area.inArea(tile)*/tile.getX() >= bl.getX() && bl.getY() >= bl.getY() && tile.getX() <= tr.getX() && tile.getY() <= tr.getY() )
				return new int[] { row, index };
		}
		return new int[] { 0, 0 };
	}

	public int getRow() {
		return row;
	}

	public int getRowIndex() {
		return rowIndex;
	}
}
