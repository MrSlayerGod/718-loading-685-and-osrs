package com.rs.game.minigames.pest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.cache.loaders.NPCConfig;
import com.rs.executor.GameExecutorManager;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.map.MapBuilder;
import com.rs.game.npc.NPC;
import com.rs.game.npc.pest.PestMonsters;
import com.rs.game.npc.pest.PestPortal;
import com.rs.game.npc.pest.Shifter;
import com.rs.game.npc.pest.Spinner;
import com.rs.game.npc.pest.Splatter;
import com.rs.game.player.Player;
import com.rs.game.player.content.box.MinigameBox;
import com.rs.game.player.controllers.pestcontrol.PestControlGame;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

public class PestControl {

	private final static int[][] PORTAL_LOCATIONS =
	{
		{ 4, 56, 45, 21, 32 },
		{ 31, 28, 10, 9, 32 } };
	private final static int[] KNIGHT_IDS =
	{ 3782, 3784, 3785 };

	private int[] boundChunks;
	private int[] pestCounts = new int[5];

	private List<Player> team;
	private List<NPC> brawlers = new LinkedList<NPC>();
	private PestPortal[] portals = new PestPortal[4];

	private PestPortal knight;
	private PestData data;

	private boolean solo;

	private byte portalCount = 5;

	public boolean isSolo() {
		return solo;
	}

	private class PestGameTimer extends TimerTask {

		int seconds = 1200;

		@Override
		public void run() {
			try {
				updateTime(seconds / 60);
				if (seconds == 0 || canFinish()) {
					endGame();
					cancel();
					return;
				}
				if (seconds % 10 == 0)
					sendPortalInterfaces();
				seconds--;
			} catch (Throwable e) {
				Logger.handle(e);
			}
		}
	}

	public PestControl(List<Player> team, PestData data, boolean solo) {
		this.team = Collections.synchronizedList(team);
		this.data = data;
		this.solo = solo;
	}

	public PestControl create() {
		if (solo)
			portalCount = 2; //reduces to 1

		final PestControl instance = this;
		GameExecutorManager.slowExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					boundChunks = MapBuilder.findEmptyChunkBound(8, 8);
					MapBuilder.copyAllPlanesMap(328, 320, boundChunks[0], boundChunks[1], 8);
					sendBeginningWave();

					unlockPortal();
					for (Player player : team) {
						player.getControlerManager().removeControlerWithoutCheck();
						player.useStairs(-1, getWorldTile(35 - Utils.random(4), 54 - (Utils.random(3))), 1, 2);
						player.getControlerManager().startControler("PestControlGame", instance);
					}
					GameExecutorManager.fastExecutor.schedule(new PestGameTimer(), 1000, 1000);
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		});
		return instance;
	}

	private void sendBeginningWave() {
		knight = new PestPortal(KNIGHT_IDS[Utils.random(KNIGHT_IDS.length)], true, getWorldTile(32, 32), this);
		knight.unlock();
		for (int index = 0; index < portals.length; index++) {
			PestPortal portal = portals[index] = new PestPortal(6146 + index, true, getWorldTile(PORTAL_LOCATIONS[0][index], PORTAL_LOCATIONS[1][index]), this);
			portal.setHitpoints(data.ordinal() == 0 ? 2000 : 2500);
		}
	}

	public boolean createPestNPC(int index) {
		if (pestCounts[index] >= (index == 4 ? 4 : (portals[index] != null && portals[index].isLocked()) ? 5 : 15))
			return false;
		pestCounts[index]++;
		WorldTile baseTile = getWorldTile(PORTAL_LOCATIONS[0][index], PORTAL_LOCATIONS[1][index]);
		WorldTile teleTile = baseTile;
		int npcId = index == 4 ? data.getShifters()[Utils.random(data.getShifters().length)] : data.getPests()[Utils.random(data.getPests().length)];
		NPCConfig defs = NPCConfig.forID(npcId);
		for (int trycount = 0; trycount < 10; trycount++) {
			teleTile = new WorldTile(baseTile, 5);
			if (World.isTileFree(baseTile.getPlane(), teleTile.getX(), teleTile.getY(), defs.boundSize))
				break;
			teleTile = baseTile;
		}
		String name = defs.getName().toLowerCase();
		if (name.contains("shifter"))
			new Shifter(npcId, teleTile, -1, true, true, index, this);
		else if (name.contains("splatter"))
			new Splatter(npcId, teleTile, -1, true, true, index, this);
		else if (name.contains("spinner"))
			new Spinner(npcId, teleTile, -1, true, true, index, this);
		else if (name.contains("brawler"))
			brawlers.add(new PestMonsters(npcId, teleTile, -1, true, true, index, this));
		else
			new PestMonsters(npcId, teleTile, -1, true, true, index, this);
		return true;
	}

	public void endGame() {
		final List<Player> team = new LinkedList<Player>();
		team.addAll(this.team);
		this.team.clear();
		if (!knight.isDead() && !solo)
			World.sendNews(null,  "<col=9999ff>A pest control game ("+data.name()+") just finished.", 1);
		for (final Player player : team) {
			final int knightZeal = (int) ((PestControlGame) player.getControlerManager().getControler()).getPoints();
			player.getControlerManager().forceStop();
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					sendFinalReward(player, knightZeal);
				}
			}, 1);
		}
		/*
		 * 6sec is a waste of time lo
		 */
		GameExecutorManager.slowExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				MapBuilder.destroyMap(boundChunks[0], boundChunks[1], 8, 8);
				boundChunks = null;
			}
		}, 1200, TimeUnit.MILLISECONDS);
		
	}
	
	public boolean hasGameEnded() {
		return boundChunks == null && team.isEmpty();
	}

	private void sendFinalReward(Player player, int knightZeal) {
		if (knight.isDead())
			player.getDialogueManager().startDialogue("SimpleMessage", "You failed to protect the void knight and have not been awarded any points.");
		else if (knightZeal < 750)
			player.getDialogueManager().startDialogue("SimpleMessage", "The knights notice your lack of zeal in that battle and have not presented you with any points.");
		else {
			int coinsAmount = player.getSkills().getCombatLevel() * (data.ordinal() == 0 ? 300 : data.ordinal() == 1 ? 600 : 1000);
			int pointsAmount = data.getReward() * 3 * (Settings.DOUBLE_MINIGAME_ENABLED ? 2 : 1);// (int) (data.getReward() * 7.5);
			double mult = ((double) player.getDonator()) / 10 + 1;
			pointsAmount *= mult;
			coinsAmount *= mult;
			player.getDialogueManager().startDialogue("SimpleMessage", "Congratulations! You have successfully kept the lander safe and have been awarded: " + coinsAmount + " gold coins and " + pointsAmount + " commendation points.");
			player.getInventory().addItemMoneyPouch(new Item(995, coinsAmount));
			player.setPestPoints(player.getPestPoints() + pointsAmount);
			player.incrementPestGames();
		}
		/*if (data == PestData.VETERAN && Utils.random(600) == 0)
			dropSet(player);*/
		Item item = new Item(MinigameBox.ID, 1);
		player.getPackets().sendGameMessage(item.getName() +" x"+item.getAmount()+" has been added to your bank.");
		player.getBank().addItem(item.getId(), item.getAmount(), false);
		
	}

	
	public static final int[] PIECES = {21462, 21463, 21464, 21465, 21466};
	
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
		World.sendNews(player, player.getDisplayName() + " has received <col=ffff00>" + ItemConfig.forID(piece).getName() + "<col=ff8c38> from <col=cc33ff>pest control<col=ff8c38>!", 1);
	}
	
	private void sendPortalInterfaces() {
		for (Player player : team) {
			for (int i = 13; i < 17; i++) {
				PestPortal npc = portals[i - 13];
				if (npc != null)
					player.getPackets().sendIComponentText(408, i, npc.getHitpoints() + "");
			}
			player.getPackets().sendIComponentText(408, 1, "" + knight.getHitpoints());
		}
	}

	public void unlockPortal() {
		if (portalCount == 0)
			return;
		else if (portalCount == 1) {
			portalCount--;
			return;
		}
		final int index = Utils.random(portals.length);
		if (portals[index] == null || portals[index].isDead()) {
			unlockPortal();
		} else {
			portalCount--;
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					portals[index].unlock();
				}
			}, /*30*/15);
		}
	}

	public boolean isBrawlerAt(WorldTile tile) {
		for (Iterator<NPC> it = brawlers.iterator(); it.hasNext();) {
			NPC npc = it.next();
			if (npc.isDead() || npc.hasFinished()) {
				it.remove();
				continue;
			}
			if (npc.getX() == tile.getX() && npc.getY() == tile.getY() && tile.getPlane() == tile.getPlane())
				return true;
		}
		return false;
	}

	private void updateTime(int minutes) {
		for (Player player : team)
			player.getPackets().sendIComponentText(408, 0, minutes + " min");
	}

	public void sendTeamMessage(String message) {
		for (Player player : team)
			player.getPackets().sendGameMessage(message, true);
	}

	private boolean canFinish() {
		if (knight == null || knight.isDead())
			return true;
		return portalCount == 0;
	}

	public WorldTile getWorldTile(int mapX, int mapY) {
		if (boundChunks == null) //temporary fix..
			return Settings.START_PLAYER_LOCATION;
		return new WorldTile(boundChunks[0] * 8 + mapX, boundChunks[1] * 8 + mapY, 0);
	}

	public PestPortal[] getPortals() {
		return portals;
	}

	public List<Player> getPlayers() {
		return team;
	}

	public NPC getKnight() {
		return knight;
	}

	public enum PestData {

		NOVICE(new int[]
		{ /*Shifters*/
			3732, 3733, 3734, 3735, /*Ravagers*/
			3742, 3743, 3744, /*Brawler*/
			3772, 3773, /*Splatter*/
			3727, 3728, 3729, /*Spinner*/
			3747, 3748, 3749, /*Torcher*/
			3752, 3753, 3754, 3755, /*Defiler*/
			3762, 3763, 3764, 3765 }, new int[]
		{ 3732, 3733, 3734, 3735 }, 3),

		INTERMEDIATE(new int[]
		{ /*Shifters*/
			3734, 3735, 3736, 3737, 3738, 3739/*Ravagers*/, 3744, 3743, 3745, /*Brawler*/
			3773, 3775, 3776, /*Splatter*/
			3728, 3729, 3730, /*Spinner*/
			3748, 3749, 3750, 3751, /*Torcher*/
			3754, 3755, 3756, 3757, 3758, 3759, /*Defiler*/
			3764, 3765, 3766, 3768, 3769 }, new int[]
		{ 3734, 3735, 3736, 3737, 3738, 3739 }, 4),

		VETERAN(new int[]
		{ /*Shifters*/
			3736, 3737, 3738, 3739, 3740, 3741 /*Ravagers*/, 3744, 3745, 3746, /*Brawler*/
			3776, 3774, /*Splatter*/
			3729, 3730, 3731, /*Spinner*/
			3749, 3750, 3751, /*Torcher*/
			3758, 3759, 3760, 3761, /*Defiler*/
			3770, 3771 }, new int[]
		{ 3736, 3737, 3738, 3739, 3740, 3741 }, 5);

		private int[] pests, shifters;
		private int reward;

		private PestData(int[] pests, int[] shifters, int reward) {
			this.pests = pests;
			this.shifters = shifters;
			this.reward = reward;
		}

		public int[] getShifters() {
			return shifters;
		}

		public int[] getPests() {
			return pests;
		}

		public int getReward() {
			return reward;
		}
	}

	public int[] getPestCounts() {
		return pestCounts;
	}

	public PestData getPestData() {
		return data;
	}

	public int getPortalCount() {
		return portalCount;
	}
}
