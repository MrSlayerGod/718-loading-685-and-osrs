/**
 * 
 */
package com.rs.game.player.controllers;

import java.util.LinkedList;
import java.util.List;

import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.map.MapInstance;
import com.rs.game.map.MapInstance.Stages;
import com.rs.game.npc.slayer.AlchemicalHydra;
import com.rs.game.player.Player;
import com.rs.game.player.content.Summoning;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author Alex (Dragonkk)
 * Feb 24, 2019
 */
public class HydraLair extends Controller {

	private static final WorldTile OUTSIDE = new WorldTile(1351, 10250, 0), ENTRANCE = new WorldTile(1351, 10252, 0);
	
	private static final int LOGOUT_EXIT = 0, NORMAL_EXIT = 1, TELE_EXIT = 2;
	
	private static WorldTile[] VENTS = {new WorldTile(1371, 10263, 0)
			, new WorldTile(1371, 10272, 0)
			, new WorldTile(1362, 10272, 0)};
	
	public static WorldTile[] ORB_SPAWNS = {new WorldTile(1361, 10273, 0)
			, new WorldTile(1371, 10273, 0)
			, new WorldTile(1370, 10262, 0)
			, new WorldTile(1361, 10262, 0)};
	
	
	public static void enter(Player player) {
		if (player.getFamiliar() != null ||/* player.getPet() != null ||*/ Summoning.hasPouch(player)
				/*|| Pets.hasPet(player)*/) {
			player.getDialogueManager().startDialogue("SimpleMessage","You don't want your friends to be eaten by Hydra. You are not allowed to take familiars onto Hydra's Lair.");
			return;
		}
		player.getControlerManager().startControler("HydraLair");
	}
	
	private MapInstance map;
	private AlchemicalHydra boss;
	private int timer;
	private boolean insideBoss;
	private List<Effect> lights;
	private List<Effect> flames;
	private Effect flame; //this one follows the player and spawns others in the path
	private int bleed;
	
	private void playMusic() {
		player.getMusicsManager().playOSRSMusic(
				insideBoss ? "Alchemical Attack!" : "Way of the Wyrm");
	}
	
	private class Effect {
		
		private int timer;
		private WorldTile tile;
		
		private Effect(WorldTile tile) {
			this.tile = tile;
		}
	}
	
	public void addLight(WorldTile tile) {
		lights.add(new Effect(tile));
	}
	
	public boolean addFlame(WorldTile tile) {
		for (Effect effect : flames) {
			if (effect != null && effect.tile.getX() == tile.getX()
					&& effect.tile.getY() == tile.getY()) {
				return false;
			}
		}
		flames.add(new Effect(tile));
		return true;
	}
	
	public void setFlame() {
		flame = new Effect(getMap().getTile(new WorldTile(1366, 10267, 0)));
	}
	
	@Override
	public void start() {
		lights = new LinkedList<Effect>();
		flames = new LinkedList<Effect>();
		load();
	}
	
	public void setFire(int angle) {
		setFire(angle, true);
	}
	
	
	public void setFire(int angle, boolean check) {
		WorldTile minTile = getMap().getTile(new WorldTile(1356, 10257, 0));
		WorldTile maxTile = getMap().getTile(new WorldTile(1377, 10278, 0));
		WorldTile centerTile = getMap().getTile(new WorldTile(1366, 10267, 0));
		double degree = Math.toDegrees(Utils.getAngle(player.getX() - centerTile.getX(), player.getY() - centerTile.getY()) / 2607.5945876176133);
		degree += angle;
		if (degree < 0)
			degree += 360;
		else if (degree > 360)
			degree -= 360;
		double dir = Math.toRadians(degree) * 2607.5945876176133;
		byte[] dirs = Utils.getDirection((int) dir);//Utils.getDirection((int) (boss.getDirection() + (2607.5945876176133 * angle) ) & 0x3fff);
		
		if (check) {
		for (int distance = 20; distance >= 0; distance--) {
			for (int x = 1; x < boss.getSize() - 1; x++) {
				for (int y = 1; y < boss.getSize() - 1; y++) {
					WorldTile fireTile = new WorldTile(new WorldTile(boss.getX() + x + (dirs[0] * distance),
							boss.getY() + y + (dirs[1] * distance), 0));
					if (!Utils.colides(boss.getX(), +boss.getY(), boss.getSize(), fireTile.getX(), fireTile.getY(), 1)
							&& fireTile.getX() >= minTile.getX() && fireTile.getY() >= minTile.getY()
							&& fireTile.getX() <= maxTile.getX() && fireTile.getY() <= maxTile.getY()) {
						if (fireTile.getX() == player.getX() && fireTile.getY() == player.getY()) {
							setFire(angle * 2, false);
							return;
						}
					}
				}
			}
		}
		}
		
		for (int distance = 20; distance >= 0; distance--) {
			for (int x = 1; x < boss.getSize() - 1; x++) {
				for (int y = 1; y < boss.getSize() - 1; y++) {
					WorldTile fireTile = new WorldTile(new WorldTile(boss.getX() + x + (dirs[0] * distance),
							boss.getY() + y + (dirs[1] * distance), 0));
					int d = Math.max(0, Utils.getDistance(centerTile, fireTile) - 2);
					if (d == 1) 
						World.sendProjectile(boss, fireTile, 6667, 40, 20, 20, 0, 0, boss.getSize() * 32 - 32);	
					if (!Utils.colides(boss.getX(), +boss.getY(), boss.getSize(), fireTile.getX(), fireTile.getY(), 1)
							&& fireTile.getX() >= minTile.getX() && fireTile.getY() >= minTile.getY()
							&& fireTile.getX() <= maxTile.getX() && fireTile.getY() <= maxTile.getY()) {
						if (addFlame(fireTile))
							World.sendGraphics(player, new Graphics(6668, d * 5 + 10, 0), fireTile);
						 if (boss.getNextFaceWorldTile() == null)
							boss.setNextFaceWorldTile(fireTile);
					}
				}
			}
		}
	}
	
	
	@Override
	public void process() {
		if (!isRunning()) 
			return;
		if (timer % 100 == 0)
			playMusic(); // so that music doesnt get replaced
		if (insideBoss) {
			if (timer % 25 == 0) {
				for (WorldTile tile : VENTS) {
					WorldObject object = World.getStandartObject(map.getTile(tile));
					if (object == null)
						continue;
					player.getPackets().sendObjectAnimation(object, new Animation(5771));
				}
			} else if (timer % 25 == 5) {
				for (WorldTile tile : VENTS) {
					WorldObject object = World.getStandartObject(map.getTile(tile));
					if (object == null)
						continue;
					if (player.getX() == object.getX() && player.getY() == object.getY()) {
						player.applyHit(new Hit(player, Utils.random(500)+1, HitLook.REGULAR_DAMAGE));
						player.getPackets().sendGameMessage("The chemical burns you as it cascades over you.");
					}
					if (boss != null && !boss.hasFinished() && Utils.colides(boss.getX(), boss.getY(), boss.getSize(), object.getX(), object.getY(), 1)) {
						if (boss.getId() == AlchemicalHydra.POISON_ID)
							boss.useChemical(object.getId() == 134568);
						else if (boss.getId() == AlchemicalHydra.LIGHTNING_ID)
							boss.useChemical(object.getId() == 134569);
						else if (boss.getId() == AlchemicalHydra.FLAME_ID)
							boss.useChemical(object.getId() == 134570);
					}
				}
			} else if (timer % 25 == 15) {
				for (WorldTile tile : VENTS) {
					WorldObject object = World.getStandartObject(map.getTile(tile));
					if (object == null)
						continue;
					player.getPackets().sendObjectAnimation(object, new Animation(-1));
				}
			}
			if (bleed > 5) {
				bleed--;
				player.applyHit(new Hit(boss, 50, HitLook.REGULAR_DAMAGE));
			}
			if (flame != null) {
				if (flame.timer++ >= 12 || boss == null || boss.hasFinished()) 
					flame = null;
				else {
					flame.tile = flame.tile.transform(player.getX() > flame.tile.getX() ? 1 : player.getX() < flame.tile.getX() ? -1 : 0, player.getY() > flame.tile.getY() ? 1 : player.getY() < flame.tile.getY() ? -1 : 0, 0);
					if (addFlame(new WorldTile(flame.tile)))
						World.sendGraphics(player, new Graphics(6668, 0, 0), flame.tile);
				}
			}
			for (Effect light : flames.toArray(new Effect[lights.size()])) {
				if (light == null)
					continue;
				if (light.timer++ >= 45 || boss == null || boss.hasFinished()) {
					flames.remove(light);
				//	World.sendGraphics(boss, new Graphics(-1), light.tile);
				} else if (light.tile.getX() == player.getX() && light.tile.getY() == player.getY()) {
					player.applyHit(new Hit(boss, Utils.random(200)+1, HitLook.REGULAR_DAMAGE));
					bleed = 5;
					player.setNextForceTalk(new ForceTalk("Yowch!"));
					player.getPackets().sendGameMessage("The fire scorches you leaving a lingering burn....");
				}
			}
			for (Effect light : lights.toArray(new Effect[lights.size()])) {
				if (light == null)
					continue;
				if (light.timer++ >= 12 || boss == null || boss.hasFinished())
					lights.remove(light);
				else {
					World.sendGraphics(player, new Graphics(6666), light.tile);
					if (light.tile.getX() == player.getX() && light.tile.getY() == player.getY()) {
						lights.remove(light);
						player.stopAll();
						player.addFreezeDelay(1200);
						player.applyHit(new Hit(boss, Utils.random(200)+1, HitLook.REGULAR_DAMAGE));
						player.getPackets().sendGameMessage("<col=D80000>The eletricity temporarily paralyzes you!"); 
					} else
						light.tile = light.tile.transform(player.getX() > light.tile.getX() ? 1 : player.getX() < light.tile.getX() ? -1 : 0, player.getY() > light.tile.getY() ? 1 : player.getY() < light.tile.getY() ? -1 : 0, 0);
				}
			}
		}
		timer++;
	}
	
	@Override
	public boolean processObjectClick1(WorldObject object) {
		if (!isRunning()) 
			return false;
		if (object.getId() == 134548) {
			player.setNextAnimation(new Animation(4853));
			player.getPackets().sendGameMessage("You climb over the rocks.");
			exit(NORMAL_EXIT);
			return false;
		}
		if (object.getId() == 134553 || object.getId() == 134554) {
			if (!insideBoss)
				player.getDialogueManager().startDialogue("HydraDoor", this);
			else
				passBossDoor();
			return false;
		}
		return true;
	}
	
	@Override
	public boolean processObjectClick2(WorldObject object) {
		if (!isRunning()) 
			return false;
		if (object.getId() == 134553 || object.getId() == 134554) {
			passBossDoor();
			return false;
		}
		return true;
	}
	
	public void passBossDoor() {
		if (!isRunning())
			return;
		if (insideBoss && boss != null && !boss.hasFinished()) {
			player.getPackets().sendGameMessage("The door is jammed. You can not open it right now.");
			return;
		}
		insideBoss = !insideBoss;
		player.stopAll();
		player.lock(3);
		player.addWalkSteps(player.getX() + (insideBoss ? 1 : -1), player.getY(), 2, false);
		playMusic();
		timer = 0;
		if (!insideBoss) {
			for (WorldTile tile : VENTS) {
				WorldObject object = World.getStandartObject(map.getTile(tile));
				if (object == null)
					continue;
				player.getPackets().sendObjectAnimation(object, new Animation(-1));
			}
		}
	}
	
	private void load() {
		player.lock();
		player.stopAll();
		map = new MapInstance(166, 1278);
		map.load(() -> {
			player.useStairs(4853, map.getTile(ENTRANCE), 1, 2, "You climb over the rocks.");
			playMusic();
			boss = new AlchemicalHydra(this);
		});
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
	
	private void exit(int type) {
		if (!isRunning())  //didnt tp inside anyway
			return;
		if (type == LOGOUT_EXIT)
			player.setLocation(OUTSIDE);
		else {
			player.setFreezeDelay(0);
			if (type == NORMAL_EXIT) 
				player.useStairs(-1, OUTSIDE, 0, 2);
		}
		removeControler();// remove in logout too to prevent double call
		map.destroy(() -> { //just double prevention
			if (boss != null)
				boss.finish();
		});
	}
	
	public boolean isRunning() {
		return map != null && map.getStage() == Stages.RUNNING;
	}
	
	public MapInstance getMap() {
		return map;
	}
	
	

}
