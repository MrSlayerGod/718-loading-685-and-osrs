/**
 * 
 */
package com.rs.game.player.controllers;

import java.util.concurrent.TimeUnit;

import com.rs.executor.GameExecutorManager;
import com.rs.game.Animation;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.map.MapBuilder;
import com.rs.game.npc.zulrah.Zulrah;
import com.rs.game.player.Player;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.content.Summoning;
import com.rs.game.player.cutscenes.Cutscene;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex) Nov 4, 2017
 */
public class ZulrahShrine extends Controller {

	public static final WorldTile OUTSIDE = new WorldTile(2213, 3056, 0);

	private static enum Stages {
		LOADING, RUNNING, DESTROYING
	}

	private transient Stages stage;
	private int[] boundChuncks;
	private int timer;
	private Zulrah zulrah;
	private boolean hardMode;

	public void playMusic() {
		player.getMusicsManager().playOSRSMusic("Coil");
	}

	public static void enterZulrahShrine(Player player, boolean hardMode) {
		if (player.getFamiliar() != null/* || player.getPet() != null*/ || Summoning.hasPouch(player)
				/*|| Pets.hasPet(player)*/) {
			player.getDialogueManager().startDialogue("SimpleMessage",
					"You don't want your friends to be eaten by Zulrah You are not allowed to take pets nor familiars onto Zulrah's Shrine.");
			return;
		}
		player.getControlerManager().startControler("ZulrahShrine", hardMode);
	}

	@Override
	public void start() {
		hardMode = (boolean) this.getArguments()[0];
		enter();
	}

	public void enter() {
		stage = Stages.LOADING;
		player.lock(); // locks player
		player.stopAll();
		player.getDialogueManager().startDialogue("SimpleMessage",
				"The priestess rows you to Zulrah's shrine, then hurridly paddles away.");
		player.getPackets().sendHideIComponent(1186, 7, true);
		FadingScreen.fade(player, new Runnable() {
			@Override
			public void run() {
				GameExecutorManager.slowExecutor.execute(new Runnable() {
					@Override
					public void run() {
						try {
							boundChuncks = MapBuilder.findEmptyChunkBound(8, 8);
							MapBuilder.copyAllPlanesMap(281, 379, boundChuncks[0], boundChuncks[1], 8);
							playMusic();
							player.setForceMultiArea(true);
							player.setLargeSceneView(true);
							player.setNextWorldTile(getWorldTile(20, 36));
							player.getPackets().sendHideIComponent(1186, 7, false);
							stage = Stages.RUNNING;
							player.lock(2); // unlocks player
							WorldTasksManager.schedule(new WorldTask() {

								int stage;

								@Override
								public void run() {
									if (ZulrahShrine.this.stage != Stages.RUNNING) {
										stop();
										return;
									}
									if (stage == 0) {
										WorldTile lookTo = getWorldTile(27, 42);
										player.getPackets().sendCameraLook(Cutscene.getX(player, lookTo.getX()),
												Cutscene.getY(player, lookTo.getY()), 2000);
										WorldTile posTile = getWorldTile(9, 39);
										player.getPackets().sendCameraPos(Cutscene.getX(player, posTile.getX()),
												Cutscene.getY(player, posTile.getY()), 2000);
									} else if (stage == 1) {
										zulrah = new Zulrah(ZulrahShrine.this);
										if (hardMode)
											zulrah.setHardMode();
									} else if (stage == 3) {
										player.getPackets().sendResetCamera();
										stop();
									}
									stage++;
								}

							}, 1, 2);
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				});
			}
		});
	}

	/*
	 * gets worldtile inside the map
	 */
	public WorldTile getWorldTile(int mapX, int mapY) {
		return new WorldTile(boundChuncks[0] * 8 + mapX, boundChuncks[1] * 8 + mapY, 0);
	}

	public WorldTile getWorldTileReal(int x, int y) {
		return new WorldTile(boundChuncks[0] * 8 + (x - 281 * 8), boundChuncks[1] * 8 + (y - 379 * 8), 0);
	}

	public WorldTile getWorldTileReal(WorldTile tile) {
		return new WorldTile(boundChuncks[0] * 8 + (tile.getX() - 281 * 8),
				boundChuncks[1] * 8 + (tile.getY() - 379 * 8), 0);
	}

	@Override
	public void process() {
		if (stage != Stages.RUNNING || zulrah == null)
			return;
		timer++;
		if (timer % 100 == 0)
			playMusic(); // so that music doesnt get replaced
		for (WorldObject object : zulrah.getObjects()) {
			if (zulrah.hasFinished())
				World.removeObject(object);
			if (!World.isSpawnedObject(object)) {
				zulrah.getObjects().remove(object);
				continue;
			}
			if (Utils.collides(player.getX(), player.getY(), 1, object.getX(), object.getY(), 3))
				player.applyHit(new Hit(zulrah, Utils.random(hardMode ? 400 : 40) + 10, HitLook.POISON_DAMAGE));
		}
	}

	/*
	 * @Override public void moved() { WorldTile base = getWorldTile(0, 0); int x =
	 * player.getX() - base.getX(); int y = player.getY() - base.getY();
	 * System.out.println("walk to" + x + ", " + y); }
	 */
	@Override
	public boolean logout() {
		exit(LOGOUT_EXIT);
		return true;
	}

	@Override
	public boolean login() { // shouldnt happen, forcestop wont do anything since it wont be stage running
		player.useStairs(-1, OUTSIDE, 0, 2);
		return true;
	}

	@Override
	public void magicTeleported(int type) {
		exit(TELE_EXIT);
	}

	@Override
	public void forceClose() {
		exit(NORMAL_EXIT); // to prevent glitching
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
					exit(TELE_EXIT);
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

	// could make an enum but w/e, normal doesnt exist tbh lol. but w/e add support
	// for it.s
	private static final int LOGOUT_EXIT = 0, NORMAL_EXIT = 1, TELE_EXIT = 2;

	public void exit(int type) {
		if (stage != Stages.RUNNING)
			return;
		stage = Stages.DESTROYING;
		if (type == LOGOUT_EXIT)
			player.setLocation(OUTSIDE);
		else {
			player.setForceMultiArea(false);
			player.setLargeSceneView(false);
			if (type == NORMAL_EXIT)
				player.useStairs(-1, OUTSIDE, 0, 2);
		}
		removeControler();// remove in logout too to prevent double call
		/*
		 * 1200 delay because of leaving
		 */
		GameExecutorManager.slowExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					if (zulrah != null)
						zulrah.finish();
					MapBuilder.destroyMap(boundChuncks[0], boundChuncks[1], 8, 8);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}, 1200, TimeUnit.MILLISECONDS);
	}

	public boolean isRunning() {
		return stage == Stages.RUNNING;
	}

}
