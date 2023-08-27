package com.rs.game.player.controllers;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

import com.rs.executor.GameExecutorManager;
import com.rs.game.Animation;
import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.nightmare.TheNightmare;
import com.rs.game.player.Player;
import com.rs.game.player.actions.ViewingOrb;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.content.Summoning;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;

public class TheNightmareInstance extends Controller {

	private static NPC lobby;
	private static TheNightmare boss;
	private static TimerTask spawnTask;
	
	private static final List<Player> players = Collections.synchronizedList(new LinkedList<Player>());
	
	public static final int NIGHTMARE_LOBBY_EMPTY = 29460, NIGHTMARE_LOBBY_WAITING = 29461, NIGHTMARE_LOBBY_PHASE_1 = 29462,
			NIGHTMARE_LOBBY_PHASE_2 = 29463, NIGHTMARE_LOBBY_PHASE_3 = 29464;
			
			;
	
	public static void init() {
		lobby = World.spawnNPC(NIGHTMARE_LOBBY_EMPTY, new WorldTile(3806, 9757, 1), -1, true, true);
	}
	
	public static final WorldTile[] ORB_TELEPORTS =
		{ new WorldTile(3872, 9951, 3) //center
				
				, new WorldTile(3872, 9958, 3) //north
				
				, new WorldTile(3879, 9951, 3) //east
				
				, new WorldTile(3872, 9944, 3) //south
				
				, new WorldTile(3865, 9951, 3) //west
				
		};

	public static int getPlayersCount() {
		return players.size();
	}
	
	public static void spectate(Player player) {
		player.getActionManager().setAction(new ViewingOrb(ORB_TELEPORTS));
	}
	
	public static void inspect(Player player) {
		//There are 8 adventurers in the dream.
		//The fight has not yet started.
		player.getDialogueManager().startDialogue("SimpleMessage", "There are "+players.size()+" adventurers in the dream.",
				hasFightStarted() ? "The fight has already started." : "The fight has not yet started.");
		
	}
	
	public static boolean hasFightStarted() {
		return players.size() >= 80 || (!players.isEmpty() && spawnTask == null && boss != null);
	}
	
	
	public static void interact(Player player) {
		/*if (hasFightStarted()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "A group is already fighting the Nightmare. You'll have to wait until they are done.");
			return;
		}*/
		if (player.getPrayer().isAncientCurses()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "Curse prayer's won't save you in this Nightmare! Switch your prayer book!");
			return;
		}
		if (player.getFamiliar() != null || Summoning.hasPouch(player)) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You can now take familiars into the dream.");
			return;
		}
		players.add(player);
		player.lock();
		player.setNextAnimation(new Animation(827));
		player.getDialogueManager().startDialogue("SimpleMessage", "The Nightmare pulls you into her dream as you approach her.");
		FadingScreen.fade(player, new Runnable() {
			@Override
			public void run() {
				if (boss == null) {
					boss = new TheNightmare();
					reset();
				}
				player.getControlerManager().startControler("TheNightmareInstance");
			}
		});
	}
	
	private static final int LOGOUT = 0, TELEPORT = 1, EXIT = 2;
	
	private static final WorldTile OUTSIDE = new WorldTile(3808, 9756, 1); 
	
	public static void leave(Player player, int type) {
		if (type == EXIT) {
			player.lock();
			player.setNextAnimation(new Animation(827));
			FadingScreen.fade(player, new Runnable() {
				@Override
				public void run() {
					player.useStairs(-1, OUTSIDE, 0, 2);
					leave(player);
				}
			});
		} else {
			if (type == LOGOUT)
				player.setLocation(OUTSIDE);
			leave(player);
		}
	}
	
	@Override
	public void forceClose() {
		leave(player, LOGOUT);
	}
	
	private static void leave(Player player) {
		players.remove(player);
		player.getInterfaceManager().removeOverlay(false);
		player.setForceMultiArea(false);
		player.setLargeSceneView(false);
		player.getTemporaryAttributtes().remove(Key.SHUFFLE_PRAYERS);
		player.getTemporaryAttributtes().remove(Key.SPORE_INFECTED);
		player.getAppearence().generateAppearenceData();
		player.getControlerManager().removeControlerWithoutCheck();
		if (players.isEmpty()) {
			if (boss != null) {
				boss.finish();
				boss = null;
			}
			reset();
		}
	}
	
	@Override
	public boolean login() { // shouldnt happen, forcestop wont do anything since it wont be stage running
		player.useStairs(-1, OUTSIDE, 0, 2);
		return true;
	}
	
	@Override
	public boolean logout() {
		leave(player, LOGOUT);
		return true;
	}
	
	@Override
	public void sendInterfaces() {
		player.getInterfaceManager().setOverlay(3203, false);
		updateInterface();
	}
	
	@Override
	public void process() {
		updateInterface();
	}
	
	public static void updateInterfaceAll() {
		if (boss != null)
			for (Player player : players)
				boss.updateInterface(player);
	}
	
	private void updateInterface() {
		if (boss != null)
			boss.updateInterface(player);
	}
	
	
	@Override
	public boolean processObjectClick1(WorldObject object) {
		if (object.getId() == 137730) {
			leave(player, EXIT);
			return false;
		}
		return true;
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
					player.getPackets().sendGameMessage("Oh dear, you have died.");
				} else if (loop == 3) {
					leave(player, TELEPORT);
					player.getControlerManager().startControler("DeathEvent", OUTSIDE, player.hasSkull());
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
		leave(player, TELEPORT);
	}
	
	public static void sendMessage(String message) {
		for (Player player : players)
			player.getPackets().sendGameMessage(message);
	}
	
	public static void update() {
		lobby.setNextNPCTransformation(boss == null ? NIGHTMARE_LOBBY_EMPTY : (NIGHTMARE_LOBBY_PHASE_1 + boss.getPhase()));
	}
	
	
	public static void wakeup() {
		if (spawnTask != null) //being double ssafe
			spawnTask.cancel();
		if (boss != null)
			boss.wakeup();
		lobby.setNextAnimation(new Animation(28575));
		update();
	}
	
	public static void reset() {
		lobby.setNextAnimation(new Animation(players.isEmpty() ? (lobby.getId() == NIGHTMARE_LOBBY_WAITING ? 28580 : 28581)	: (lobby.getId() == NIGHTMARE_LOBBY_EMPTY ? 28573 : 28576)));
		lobby.setNextNPCTransformation(players.isEmpty() ? NIGHTMARE_LOBBY_EMPTY : NIGHTMARE_LOBBY_WAITING);
		if (spawnTask != null) //being double ssafe
			spawnTask.cancel();
		
		if (!players.isEmpty()) {
			GameExecutorManager.fastExecutor.scheduleAtFixedRate(spawnTask = new TimerTask() {

				private int time = 4;
				
				@Override
				public void run() {
					try {
						if (time <= 0) {
							wakeup();
							return;
						}
						sendMessage("The Nightmare will awaken in "+(time*10)+" seconds!");
						time--;
					} catch (Throwable e) {
						Logger.handle(e);
					}
				}
				
				@Override
				public boolean cancel() {
					spawnTask = null;
					return super.cancel();
				}
				
			}, 0, 10000);
			
		}
	}

	@Override
	public void start() {
		player.useStairs(-1, new WorldTile(3872, 9948, 3), 0, 2);
		player.setForceMultiArea(true);
		player.setLargeSceneView(true);
		sendInterfaces();
	}
	
	
}
