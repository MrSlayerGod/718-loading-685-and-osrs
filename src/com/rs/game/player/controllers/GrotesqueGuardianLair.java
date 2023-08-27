/**
 * 
 */
package com.rs.game.player.controllers;

import java.util.ArrayList;
import java.util.List;

import com.rs.game.Animation;
import com.rs.game.ForceMovement;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.map.MapInstance;
import com.rs.game.map.MapInstance.Stages;
import com.rs.game.npc.grotesque.Dawn;
import com.rs.game.npc.grotesque.Dusk;
import com.rs.game.player.Player;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.content.Summoning;
import com.rs.game.player.cutscenes.Cutscene;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Dec 20, 2017
 */
public class GrotesqueGuardianLair extends Controller {

	public static void enter(Player player) {
		if (player.getFamiliar() != null || /*player.getPet() != null ||*/ Summoning.hasPouch(player)
				/*|| Pets.hasPet(player)*/) {
			player.getDialogueManager().startDialogue("SimpleMessage","You don't want your friends to be eaten by Grotesque Guardians. You are not allowed to take pets nor familiars onto Grotesque Guardians's Lair.");
			return;
		}
		player.getControlerManager().startControler("GrotesqueGuardianLair");
	}
	
	private static final WorldTile OUTSIDE = new WorldTile(3422, 3542, 2), ENTRANCE = new WorldTile(5985, 8983, 0);
	
	private MapInstance map;
	private int timer;
	private Dusk dusk;
	private Dawn dawn;
	private WorldObject[] spheres;
	private int sphereStartTimer;
	
	private void playMusic() {
		player.getMusicsManager().playOSRSMusic("Tempest");
	}
	
	@Override
	public void start() {
		load();
	}
	
	private void load() {
		player.lock();
		player.stopAll();
		player.getDialogueManager().startDialogue("SimpleMessage","You enter the passageway and it takes you to the roof of the tower.");
		player.getPackets().sendHideIComponent(1186, 7, true);
		map = new MapInstance(744, 1120);
		final long time = FadingScreen.fade(player);
		map.load(() -> {
				FadingScreen.unfade(player, time, () -> {
					player.useStairs(-1, map.getTile(ENTRANCE), 0, 2);
					player.setForceMultiArea(true);
					player.setLargeSceneView(true);
					player.getPackets().sendHideIComponent(1186, 7, false);
					playMusic();
				});
			});
	}

	
	@Override
	public void process() {
		if (!isRunning()) 
			return;
		timer++;
		if (timer % 100 == 0)
			playMusic(); // so that music doesnt get replaced
		List<Integer> traps = new ArrayList<Integer>();
		if (timer % 20 == 0 && dusk != null && dusk.getId() == Dusk.ID_PHASE2 && !dusk.isCantInteract()) {
			for (int i = 0; i < 10; i++) {
				WorldTile tile = dusk.transform(Utils.random(12 + 3) - 6, Utils.random(12 + 3) - 6, 0);
				int id = tile.getTileHash();
				if (!traps.contains(id) && World.isFloorFree(0, tile.getX(), tile.getY())) {
					traps.add(id);
					//World.sendGraphics(player, new Graphics(6435), tile);
					World.sendProjectile(dusk, tile.transform(0, -3, 0), tile, 6435, 150, 0, 1, 25, 5, 128);
				}
			}
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					if (!isRunning())
						return;
					for (int trap : traps) 
						World.sendGraphics(dusk, new Graphics(6436), new WorldTile(trap));
					if (traps.contains(player.getTileHash()))
						player.applyHit(new Hit(player, Utils.random(30) + 300, HitLook.REGULAR_DAMAGE));
				}

			}, 2);
		}
		if (dusk != null && !dusk.isCantInteract() && !dusk.hasFinished() && !dusk.isDead() && (dusk.getId() == Dusk.ID_PHASE2 || dusk.getId() == Dusk.ID_PHASE_4) && Utils.collides(player, dusk)
				&& timer % 5 == 0) 
			player.applyHit(new Hit(player, Utils.random(100) /*+ 300*/, HitLook.REGULAR_DAMAGE));
		processSpheres();
	}
	
	private void processSpheres() {
		if (spheres == null || sphereStartTimer == 0) 
			return;
		if (dawn == null || dawn.isDead() || dawn.hasFinished()) {
			for (int i = 0; i < spheres.length; i++) {
				if (spheres[i] != null) {
					World.removeObject(spheres[i]);
					spheres[i] = null;
				}
			}
			return;	
		}
		
		if (((timer - sphereStartTimer)+1) % 10 == 0) {
			for (int i = 0; i < spheres.length; i++) {
				if (spheres[i] != null) {
					if (spheres[i].getId() == 131680) {
						World.removeObject(spheres[i]);
						World.sendGraphics(player, new Graphics(6442), spheres[i]);
						spheres[i] = null;
						player.applyHit(new Hit(player, 300, HitLook.REGULAR_DAMAGE));
						dawn.heal(900);
					} else {
						spheres[i] = new WorldObject(spheres[i].getId()+1, 10, 0, spheres[i]);
						World.spawnObject(spheres[i]);
					}
				}
			}
		}
	}
	
	private void useSphere(WorldObject object) {
		for (int i = 0; i < spheres.length; i++) {
			if (spheres[i] == object) {
				World.removeObject(object);
				spheres[i] = null;
				player.applyHit(new Hit(player, object.getId() == 131678 ? 20 : object.getId() == 131679 ? 100 : 200, HitLook.REGULAR_DAMAGE));
				return;
			}
		}
		
	}
	public void resetFight() {
		dusk = null;
		dawn = null;
		spheres = null;
		sphereStartTimer = 0;
		World.spawnObject(new WorldObject(131682, 10, 3, map.getTile(new WorldTile(5973, 8989, 1))));
		World.spawnObject(new WorldObject(131683, 10, 3, map.getTile(new WorldTile(5993, 8989, 1))));
	}
	
	public void startFight(WorldObject bell) {
		if (dusk != null || dawn != null) {
			player.getPackets().sendGameMessage("Concentrate on your fight!");
			return;
		}
		player.lock();
		player.getDialogueManager().startDialogue("SimpleMessage","You ring the bell, the sound echoes out across the roof...");
		player.getPackets().sendHideIComponent(1186, 7, true);
		player.setNextAnimation(new Animation(395));
	//	World.sendObjectAnimation(player, bell, new Animation(27665)); //TODO find real one
		Cutscene.setCameraPos(player, map.getTile(new WorldTile(5984, 8987, 0)), 4000);
		Cutscene.setCameraLook(player, map.getTile(new WorldTile(5984, 8995, 0)), 2500);
		player.getVarsManager().sendVar(1241, 1);
		WorldTasksManager.schedule(new WorldTask() {
			
			int tick;
			
			@Override
			public void run() {
				tick++;
				if (tick == 1) {
					Cutscene.setCameraLook(player, map.getTile(new WorldTile(5984, 8995, 0)), 2500);
					Cutscene.setCameraPos(player, map.getTile(new WorldTile(5999, 8987, 0)), 2500, 20, 20);
				} else if (tick == 2) {
					World.removeObject(World.getObjectWithId(map.getTile(new WorldTile(5973, 8989, 1)), 131682));
					World.removeObject(World.getObjectWithId(map.getTile(new WorldTile(5993, 8989, 1)), 131683));
					dusk = new Dusk(GrotesqueGuardianLair.this);
					dawn = new Dawn(GrotesqueGuardianLair.this);
				} else if (tick == 5) {
					player.getPackets().sendHideIComponent(1186, 7, false);
				} else if (tick == 10) {
					dusk.setNextAnimation(new Animation(27789));
					dusk.setNextForceMovement(new ForceMovement(new WorldTile(dusk), 0, dusk.transform(3, 0,0), 1, ForceMovement.EAST));
					dusk.setNextWorldTile(dusk.transform(3, 0,0));
					dawn.setNextAnimation(new Animation(27767));
					dawn.setNextForceMovement(new ForceMovement(new WorldTile(dawn), 0, dawn.transform(-3, 0,0), 1, ForceMovement.WEST));
					dawn.setNextWorldTile(dawn.transform(-3, 0,0));
				} else if (tick == 15) {
					dawn.faceEntity(player);
					dusk.faceEntity(player);
				} else if (tick == 18) {
					stop();
					dawn.setCantInteract(false);
					dusk.setCantInteract(false);
					dawn.setTarget(player);
					dusk.setTarget(player);
					player.getPackets().sendResetCamera();
					player.getVarsManager().sendVar(1241, 0);
					player.unlock();
				}
				
			}
			
		}, 2, 0);
		/*WorldTile lookTo = getWorldTile(27, 42);
		player.getPackets().sendCameraLook(Cutscene.getX(player, lookTo.getX()),
				Cutscene.getY(player, lookTo.getY()), 2000);
		WorldTile posTile = getWorldTile(9, 39);
		player.getPackets().sendCameraPos(Cutscene.getX(player, posTile.getX()),
				Cutscene.getY(player, posTile.getY()), 2000);*/
	
	}
	
	@Override
	public boolean processObjectClick1(WorldObject object) {
		if (!isRunning()) 
			return false;
		if (object.getId() == 131674) {
			exit(NORMAL_EXIT);
			return false;
		}
		if (object.getId() == 131669) {
			startFight(object);
			return false;
		}
		if (object.getId() >= 131678 && object.getId() <= 131680) {
			useSphere(object);
			return false;
		}
		return true;
	}
	
	@Override
	public boolean login() { // shouldnt happen, forcestop wont do anything since it wont be stage running
		player.useStairs(-1, OUTSIDE, 0, 2);
		return true;
	}
	
	@Override
	public boolean logout() {
		exit(LOGOUT_EXIT);
		return true;
	}
	
	@Override
	public void magicTeleported(int type) {
		exit(TELE_EXIT);
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
	
	@Override
	public void forceClose() {
		exit(NORMAL_EXIT); // to prevent glitching
	}

	
	private static final int LOGOUT_EXIT = 0, NORMAL_EXIT = 1, TELE_EXIT = 2;
	
	private void exit(int type) {
		if (!isRunning())  //didnt tp inside anyway
			return;
		if (type == LOGOUT_EXIT)
			player.setLocation(OUTSIDE);
		else {
			player.setForceMultiArea(false);
			player.setLargeSceneView(false);
			if (type == NORMAL_EXIT) 
				player.useStairs(-1, OUTSIDE, 0, 2);
		}
		removeControler();// remove in logout too to prevent double call
		map.destroy(() -> { //just double prevention
			if (dusk != null)
				dusk.finish();
			if (dawn != null)
				dawn.finish();
		});
	}
	
	
	
	public boolean isRunning() {
		return map != null && map.getStage() == Stages.RUNNING;
	}
	
	public MapInstance getMap() {
		return map;
	}

	public void sceneSpecial() {
		dusk.setCantInteract(true);
		dusk.getCombat().removeTarget();
		dawn.setCantInteract(true);
		dawn.getCombat().removeTarget();
		dusk.resetWalkSteps();
		dawn.resetWalkSteps();
		dusk.setForceWalk(map.getTile(new WorldTile(5973 + 5, 8989, 0)));
		dawn.setForceWalk(map.getTile(new WorldTile(5993 - 5, 8989, 0)));
		List<Integer> traps = new ArrayList<Integer>();
		WorldTasksManager.schedule(new WorldTask() {

			int tick;
			@Override
			public void run() {
				if (!isRunning()) {
					stop();
					return;
				}
				tick++;
				if (tick == 10) {
					dusk.resetWalkSteps();
					dawn.resetWalkSteps();
					if (dawn.hasFinished()) {
						dawn.spawn();
						dawn.setNextAnimation(new Animation(27774));
					}
					dusk.setNextFaceEntity(dawn);
					dawn.setNextFaceEntity(dusk);
				} else if (tick == 12) {
					dusk.setNextAnimation(new Animation(27790));
					dusk.setNextNPCTransformation(27855);
					dawn.setNextNPCTransformation(27853);
				} else if (tick == 16) {
					dusk.setNextAnimation(new Animation(27792));
					dawn.setNextAnimation(new Animation(27772));
				} else if (tick == 17) {
					for (int i = 0; i < 20; i++) {
						WorldTile tile = dusk.transform(Utils.random(12 + 3) - 6, Utils.random(12 + 3) - 6, 0);
						int id = tile.getTileHash();
						if (!traps.contains(id) && World.isFloorFree(0, tile.getX(), tile.getY())) {
							traps.add(id);
							World.sendGraphics(player, new Graphics(6416 + Utils.random(18)), tile);
						}
						tile = dawn.transform(Utils.random(12 + 3) - 6, Utils.random(12 + 3) - 6, 0);
						if (!traps.contains(id) && World.isFloorFree(0, tile.getX(), tile.getY())) {
							traps.add(id);
							World.sendGraphics(player, new Graphics(6416 + Utils.random(18)), tile);
						}
					}
				} else if (tick >= 18 && tick <= 27) {
					if (traps.contains(player.getTileHash())) 
						player.applyHit(new Hit(player, 50+Utils.random(20), HitLook.REGULAR_DAMAGE));
				} else if (tick == 28) {
					dusk.setNextAnimation(new Animation(27793));
					if (!dusk.wasPhase2())
						dawn.setNextAnimation(new Animation(27773));
				} else if (tick == 30) {
					if (dusk.wasPhase2()) { //2nd aoe
						dawn.setNextNPCTransformation(27852);
						dusk.setNextNPCTransformation(Dusk.ID_SHIELD);
						dawn.setCantInteract(false);
						dawn.setTarget(player);
					} else { //first aoe
						dusk.setNextNPCTransformation(Dusk.ID_PHASE2);
						dawn.finish();
					}
					dusk.setCantInteract(false);
					dusk.setTarget(player);
					stop();
				}
			}
			
		}, 0, 0);
	}
	
	public boolean createSpheres() {
		if (spheres != null)
			return false;
		spheres = new WorldObject[3];
		//5991 8990 0
		//5985 8995 0
		//5983 8987 0
		World.sendProjectile(dawn, dawn, map.getTile(new WorldTile(5991, 8990, 0)), 6437, 74, 16, 30, 65, 16, 64);
		World.sendProjectile(dawn, dawn, map.getTile(new WorldTile(5985, 8995, 0)), 6437, 74, 16, 30, 65, 16, 64);
		World.sendProjectile(dawn, dawn, map.getTile(new WorldTile(5983, 8987, 0)), 6437, 74, 16, 30, 65, 16, 64);
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				if (!isRunning() || dawn == null || dawn.isDead() || dawn.hasFinished())
					return;
				sphereStartTimer = timer;
				spheres[0] = new WorldObject(131678, 10, 0, map.getTile(new WorldTile(5991, 8990, 0)));
				World.spawnObject(spheres[0]);
				spheres[1] = new WorldObject(131678, 10, 0, map.getTile(new WorldTile(5985, 8995, 0)));
				World.spawnObject(spheres[1]);
				spheres[2] = new WorldObject(131678, 10, 0, map.getTile(new WorldTile(5983, 8987, 0)));
				World.spawnObject(spheres[2]);
			}
		}, 3);
		return true;
	}
	
	public Dusk getDusk() {
		return dusk;
	}
	
}
