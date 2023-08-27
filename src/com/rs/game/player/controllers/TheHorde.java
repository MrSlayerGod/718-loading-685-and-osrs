package com.rs.game.player.controllers;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.rs.cache.loaders.NPCConfig;
import com.rs.executor.GameExecutorManager;
import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.map.MapBuilder;
import com.rs.game.npc.NPC;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.inferno.InfernoJad;
import com.rs.game.npc.nomad.Nomad;
import com.rs.game.npc.others.Pet;
import com.rs.game.npc.worldboss.OnyxBoss;
import com.rs.game.player.Player;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.content.Magic;
import com.rs.game.player.content.Summoning;
import com.rs.game.player.content.collectionlog.CategoryType;
import com.rs.game.player.content.pet.LuckyPets;
import com.rs.game.player.content.pet.LuckyPets.LuckyPet;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

public class TheHorde extends Controller {

	public static final WorldTile OUTSIDE = new WorldTile(1716, 5599, 0);

	private static final int GUIDE = 410;

	public void playMusic() {
		player.getMusicsManager().playMusic(603); //fixed
	}
	
	private static final int[][] ITEM_SPAWNS = new int[][] {
		{8, 3},
		{28, 8},
		{19, 24},
		{3, 23}
	};

	private static final int[][] WAVES =
{
		{26766, 26766},
		{27144, 27144, 27144},
		{1591, 1592, 54},
		{3068, 28031, 28030},
		{3340, 3340},
		{7133, 7133},
		{5666, 5666, 5666},
		{8549, 8549, 8549},
		{2025, 2026, 2027, 2028, 2029, 2030},
		{13460, 14836},
		{13465, 13466, 13470, 13471, 13472, 13473, 13474, 13475, 13476, 13477, 13478, 13479, 13480, 13481},
		{8528, 14836},
		{2881, 2882, 2883},
		{50, 50, 5363, 5363},
		{25863, 25863, 25863, 25863},
		{13460, 14836, 3200},
		{26503, 26504},
		{26615, 26611},
		{1158, 1158},
		{8133},
		{6260, 6247},
		{6203, 6222},
		{26766, 26766, 26766, 26766, 26766},
		{20499, 14301, 8349},
		{26619, 27806, 26618},
		{15208},
		{2745, 2741, 2741},
		{2745, 2739, 2739, 2739},
		{2745, 2743},
		{27700, 27700, 27700},
		{7770, 7770, 7770, 7770, 7770},
		{8528, 8528},
		{26503, 26504, 26611, 6203},
		{6260, 6222, 6247, 26615},
		{1158, 1158, 1158},
		{26619, 26618, 27806, 3200},
		{16026, 16026, 16026},
		{27700, 27700, 27700, 27699},
		{16025},
		{15186}
};

	private int[] boundChuncks;
	private Stages stage;
	private boolean logoutAtEnd;
	private boolean login;
	public boolean spawned;
	private int timer;

	public static void enter(Player player) {
		if (player.getFamiliar() != null || Summoning.hasPouch(player)) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", GUIDE, "No familiars in the horde! This is a fight for YOU, not your friends!");
			return;
		}
		player.getControlerManager().startControler("TheHorde", 1); // start
		// at
		// wave
		// 1
	}

	private static enum Stages {
		LOADING, RUNNING, DESTROYING
	}

	@Override
	public void start() {
		loadCave(false);
	}

	@Override
	public boolean processButtonClick(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		if (stage != Stages.RUNNING)
			return false;
		if (interfaceId == 182 && (componentId == 6 || componentId == 13)) {
			if (!logoutAtEnd) {
				logoutAtEnd = true;
				player.getPackets().sendGameMessage("<col=ff0000>You will be logged out automatically at the end of this wave.");
				player.getPackets().sendGameMessage("<col=ff0000>If you log out sooner, you will have to repeat this wave.");
			} else
				player.disconnect(true, true);
			return false;
		}
		return true;
	}

	/**
	 * return process normaly
	 */
	@Override
	public boolean processObjectClick1(WorldObject object) {
		if (object.getId() == 30141) {
			if (stage != Stages.RUNNING)
				return false;
			exitCave(1);
			return false;
		}
		return true;
	}

	/*
	 * return false so wont remove script
	 */
	@Override
	public boolean login() {
		loadCave(true);
		return false;
	}

	public void spawnFood(int id, int slot) {
		World.addGroundItem(new Item(id), getWorldTile(ITEM_SPAWNS[slot][0], ITEM_SPAWNS[slot][1]));
	}
	public void loadCave(final boolean login) {
		this.login = login;
		stage = Stages.LOADING;
		player.lock(); // locks player
		Runnable event = new Runnable() {
			@Override
			public void run() {
		GameExecutorManager.slowExecutor.execute(new Runnable() {
			@Override
			public void run() {
				// finds empty map bounds
				boundChuncks = MapBuilder.findEmptyChunkBound(8, 8);
				// copys real map into the empty map
				// 552 640
				MapBuilder.copyAllPlanesMap(206, 710, boundChuncks[0], boundChuncks[1], 4);
					
				// selects a music
				player.setNextWorldTile(getWorldTile(15, 15));
				// 1delay because player cant walk while teleing :p, + possible
				// issues avoid
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						WorldTile base = getWorldTile(0, 0);
						for (int x = 0; x < 4 * 8; x++) {
							for (int y = 0; y < 4 * 8; y++) {
								for (int z = 0; z < 2; z++) {
								for (int slot = 0; slot < 3; slot++) {
									WorldObject object = World.getObjectWithSlot(base.transform(x, y, z), slot);
									if (object != null/* && (object.getType() != 10 || z == 1)*/)
										World.removeObject(object);
								}
								}
							}
						}
						
						for (int y = 0; y < 4 * 8; y++) {
							World.spawnObject(new WorldObject(30141,0, 2, base.transform(0, y, 0)));
							World.spawnObject(new WorldObject(30141,0, 0, base.transform(4 * 8 - 1, y, 0)));
							World.spawnObject(new WorldObject(30141,0, 1, base.transform(y, 0, 0)));
							World.spawnObject(new WorldObject(30141,0, 3, base.transform(y, 4 * 8 - 1, 0)));
						}
						spawnFood(25430, 0);
						spawnFood(25431, 1);
						spawnFood(25431, 2);
						spawnFood(25431, 3);
						player.getDialogueManager().startDialogue("SimpleNPCMessage", GUIDE, "You're on your own now!<br>Prepare to fight for your life!");
						player.setForceMultiArea(true);
						player.setLargeSceneView(true);
						playMusic();
						player.unlock(); // unlocks player
						stage = Stages.RUNNING;
					}

				}, 1);
				if (!login) {
					/*
					 * lets stress less the worldthread, also fastexecutor used
					 * for mini stuff
					 */
					GameExecutorManager.fastExecutor.schedule(new TimerTask() {

						@Override
						public void run() {
							if (stage != Stages.RUNNING)
								return;
							try {
								startWave();
							} catch (Throwable t) {
								Logger.handle(t);
							}
						}
					}, 6000);
				}
			}
		});
			}};
		if (!login)
			FadingScreen.fade(player, event);
		else
			event.run();
	}

	public WorldTile getSpawnTile(int count) {
		switch (count) {
		case 0:
			return getWorldTile(1, 30);
		case 1:
			return getWorldTile(30, 30);
		case 2:
			return getWorldTile(30, 1);
		case 3:
			return getWorldTile(1, 1);
		case 4:
			return getWorldTile(15, 30);
		case 5:
			return getWorldTile(30, 15);
		case 6:
			return getWorldTile(15, 1);
		case 7:
			return getWorldTile(15, 1);
		default:return getWorldTile(Utils.random(30)+1, Utils.random(30)+1);
		}
	}

	@Override
	public void moved() {
		if (stage != Stages.RUNNING || !login)
			return;
		login = false;
		setWaveEvent();
	}

	public void startWave() {
		int currentWave = getCurrentWave();
		if (currentWave > WAVES.length) {
			win();
			return;
		}
		player.getPackets().sendGameMessage("<col=FF0040>Wave: "+currentWave);
		player.getInterfaceManager().removeOverlay(false);
		player.getInterfaceManager().setOverlay(316, false);
		player.getVarsManager().forceSendVar(639, currentWave);
		if (stage != Stages.RUNNING)
			return;
		int count = 0;
		
		WorldTile base = getWorldTile(0, 0);
		for (int id : WAVES[currentWave - 1]) {
			/*if (id == 2736)
				new TzKekCaves(id, getSpawnTile());
			else if (id == 2745)
				new TzTok_Jad(id, getSpawnTile(), this);
			else*/
			int size = NPCConfig.forID(id).boundSize - 1;
			WorldTile tile = getSpawnTile(count++);
			int x = tile.getX() - base.getX();
			int y = tile.getY() - base.getY();
			tile = tile.transform(x == 1 ? size : x == 30 ? -size : 0, 
					y == 1 ? size : y == 30 ? -size : 0, 0);
			//	new FightCavesNPC(id, tile);
				NPC npc = id == 27700 ? new InfernoJad(tile, 3) : World.spawnNPC(id, tile, -1, true, true);
				npc.setForceMultiArea(true);
				npc.setNoDistanceCheck(true);
				npc.setTarget(player);
				npc.getCombat().setCombatDelay((count * 4) + 3);
				npc.setLastAttackByTargetInfinite();
				if (count == 2 && npc.getId() == 8528)
					((Nomad)npc).setNextMovePerformHorde();
				if (npc instanceof OnyxBoss || npc.getId() == 8549 || npc.getId() == 25863)
					npc.setCantFollowUnderCombat(false);
		}
		spawned = true;
	}

	public void win() {
		if (stage != Stages.RUNNING)
			return;
		exitCave(4);
	}

	public void nextWave() {
		setCurrentWave(getCurrentWave() + 1);
		spawnFood(25431, Utils.random(4));
		spawnFood(25431, Utils.random(4));
		spawnFood(2434, Utils.random(4));
		player.heal(player.getMaxHitpoints());
		player.getPrayer().restorePrayer(990);
		player.getCombatDefinitions().restoreSpecialAttack(100);
		if (logoutAtEnd) {
			player.disconnect(true, true);
			return;
		}
		playMusic();
		setWaveEvent();
	}

	public void setWaveEvent() {
		if (getCurrentWave() == 63)
			player.getDialogueManager().startDialogue("SimpleNPCMessage", GUIDE, "Look out, here comes Onyx!");
		GameExecutorManager.fastExecutor.schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					if (stage != Stages.RUNNING)
						return;
					startWave();
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, 6000/*600*/);
	}

	@Override
	public void process() {
		if (timer % 100 == 0)
			playMusic(); // so that music doesnt get replaced
		//WorldTile base = getWorldTile(0, 0);
	//	System.out.println((player.getX() - base.getX()) +", "+ (player.getY() - base.getY()));
		if (spawned) {
			int count = 0;
			List<Integer> npcs = World.getRegion(getWorldTile(0, 0).getRegionId()).getNPCsIndexes();
			if (npcs != null) {
				for (int npcIndex : npcs) {
					NPC npc = World.getNPCs().get(npcIndex);
					if (npc == null || npc instanceof Familiar || npc instanceof Pet ||
							npc.getId() == 9441)
						continue;
					count++;
				}
			}
			if (count == 0) {
				spawned = false;
				nextWave();
			}
		}
	}

	@Override
	public boolean sendDeath() {
		player.lock(8);
		player.stopAll();
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					player.getPackets().sendGameMessage("You have been defeated!");
				} else if (loop == 3) {
					player.reset();
					exitCave(1);
					player.setNextAnimation(new Animation(-1));
				} else if (loop == 4) {
					player.getPackets().sendMusicEffect(90);
					stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}

	@Override
	public void magicTeleported(int type) {
		if (type != Magic.OBJECT_TELEPORT)
			exitCave(2);
	}

	public static int AVAS_BLESSING = 25531, HORDE_BLESSING_AURA = 25554;

	/*
	 * logout or not. if didnt logout means lost, 0 logout, 1, normal, 2 tele
	 */
	public void exitCave(int type) {
		stage = Stages.DESTROYING;
		WorldTile outside = new WorldTile(OUTSIDE, 2); // radomizes alil
		if (type == 0)
			player.setLocation(outside);
		else {
			removeFreeItems();
			player.setForceMultiArea(false);
			player.setLargeSceneView(false);
			player.getInterfaceManager().removeOverlay(false);
			if (type == 1 || type == 4) {
				player.useStairs(-1, outside, 0, 2);
				if (type == 4) {
					if(player.getSlayerManager().getBossTask().equalsIgnoreCase("The Horde"))
						player.getSlayerManager().addBossKill(0);
					World.sendNews(player, player.getDisplayName() + " has completed <col=D80000>The Horde<col=ff8c38>!", 1);
					player.setCompletedHorde();
					player.reset();
					player.getDialogueManager().startDialogue("SimpleNPCMessage", GUIDE, "You even defeated Onyx, I am most impressed! Please accept this gift as a reward.");
					player.getPackets().sendGameMessage("You were victorious!!");
					player.getInventory().addItemDrop(25531, 1, outside);
					player.getCollectionLog().add(CategoryType.MINIGAMES, "The Horde", new Item(25531));
					player.incrementHordeCompletions();
					if (Utils.random(3) == 0) {
						player.getInventory().addItemDrop(25554, 1, outside);
						player.getCollectionLog().add(CategoryType.MINIGAMES, "The Horde", new Item(25554));
					}
					LuckyPets.checkPet(player, LuckyPet.HORDE, "The Horde");
				} else {// if (getCurrentWave() <= player.getDonator() * 5 + (player.isDonator() ? 10 : 1))
					if (getCurrentWave() >= 10)
						World.sendNews(player, player.getDisplayName() + " has reached wave "+getCurrentWave()+" in <col=D80000>The Horde<col=ff8c38>!", 1);
					
					player.getDialogueManager().startDialogue("SimpleNPCMessage", GUIDE, "Well I suppose you tried... better luck next time.");
				}
					/*else {
					int tokkul = getCurrentWave() * 8032 / (WAVES.length - (player.getDonator() * 5 + (player.isDonator() ? 10 : 1)) + 1);
					player.getInventory().addItemDrop(6529, tokkul * 10, outside);
					player.getDialogueManager().startDialogue("SimpleNPCMessage", GUIDE, "Well done in the horde, here, take this as reward.");
					// TODO tokens
				}*/
			}
			removeControler();
		}
		/*
		 * 1200 delay because of leaving
		 */
		GameExecutorManager.slowExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				MapBuilder.destroyMap(boundChuncks[0], boundChuncks[1], 8, 8);
			}
		}, 1200, TimeUnit.MILLISECONDS);
	}

	public void removeFreeItems() {
		player.getInventory().removeItems(new Item(25430, 28), new Item(25431, 28));
		player.getSkills().restoreSkills();
		player.setOverloadDelay(0);
	}
	
	/*
	 * gets worldtile inside the map
	 */
	public WorldTile getWorldTile(int mapX, int mapY) {
		return new WorldTile(boundChuncks[0] * 8 + mapX, boundChuncks[1] * 8 + mapY, 0);
	}

	/*
	 * return false so wont remove script
	 */
	@Override
	public boolean logout() {
		/*
		 * only can happen if dungeon is loading and system update happens
		 */
		if (stage != Stages.RUNNING)
			return false;
		exitCave(0);
		return false;

	}

	public int getCurrentWave() {
		if (getArguments() == null || getArguments().length == 0)
			return 1;
		return (Integer) getArguments()[0];
	}

	public void setCurrentWave(int wave) {
		if (getArguments() == null || getArguments().length == 0)
			this.setArguments(new Object[1]);
		getArguments()[0] = wave;
	}

	@Override
	public void forceClose() {
		/*
		 * shouldnt happen
		 */
		if (stage != Stages.RUNNING)
			return;
		exitCave(2);
	}
}
