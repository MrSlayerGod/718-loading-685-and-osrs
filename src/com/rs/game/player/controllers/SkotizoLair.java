/**
 * 
 */
package com.rs.game.player.controllers;

import java.util.ArrayList;
import java.util.List;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.map.MapInstance;
import com.rs.game.map.MapInstance.Stages;
import com.rs.game.npc.NPC;
import com.rs.game.npc.skotizo.Skotizo;
import com.rs.game.npc.skotizo.SkotizoAltar;
import com.rs.game.player.Player;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.content.Summoning;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Dec 20, 2017
 */
public class SkotizoLair extends Controller {

	public static void enter(Player player) {
		if (player.getFamiliar() != null || /*player.getPet() != null ||*/ Summoning.hasPouch(player)
				/*|| Pets.hasPet(player)*/) {
			player.getDialogueManager().startDialogue("SimpleMessage","You don't want your friends to be eaten by Skotizo. You are not allowed to take pets nor familiars onto Skotizo's Lair.");
			return;
		}
		player.getControlerManager().startControler("SkotizoLair");
	}
	
	private static final WorldTile OUTSIDE = new WorldTile(1664, 10046, 0), ENTRANCE = new WorldTile(1694, 9898, 0);
	
	public static final WorldTile[] ALTARS = {new WorldTile(1696, 9871, 0),
			new WorldTile(1678, 9888, 0),
			new WorldTile(1694, 9904, 0),
			new WorldTile(1714, 9888, 0)
			};
	
	private MapInstance map;
	private NPC boss;
	private NPC[] altars;
	private int timer;
	
	private void playMusic() {
		player.getMusicsManager().playOSRSMusic("Darkly Altared");
	}
	
	@Override
	public void start() {
		load();
	}
	
	private void load() {
		player.lock();
		player.stopAll();
		player.setNextAnimation(new Animation(8939));
		player.setNextGraphics(new Graphics(1576));
		map = new MapInstance(208, 1232);
		final long time = FadingScreen.fade(player);
		map.load(() -> {
				FadingScreen.unfade(player, time, () -> {
					player.useStairs(-1, map.getTile(ENTRANCE), 0, 2);
					player.setNextFaceWorldTile(map.getTile(ENTRANCE).transform(0, -1, 0));
					player.setForceMultiArea(true);
					player.setLargeSceneView(true);
					playMusic();
					setAltars();
					boss = new Skotizo(this);
					boss.setTarget(player);
				});
			});
	}
	
	public void activateAltar() {
		List<Integer> possibleAltars = new ArrayList<Integer>();
		for (int i = 0; i < altars.length; i++)
			if (altars[i] == null || altars[i].hasFinished())
				possibleAltars.add(i);
		if (possibleAltars.size() > 0)
			setAltar(possibleAltars.get(Utils.random(possibleAltars.size())), true);
	}
	
	private void setAltars() {
		altars = new NPC[4];
		for (int i = 0; i < altars.length; i++)
			setAltar(i, false);
	}
	
	public void setAltar(int index, boolean alive) {
		if (altars == null || (alive && altars[index] != null &&!altars[index].hasFinished())) //still alive
			return;
		World.spawnObject(new WorldObject(128923 + (!alive ? 1 : 0), 10, index, map.getTile(ALTARS[index])));
		if (alive) {
			altars[index] = new SkotizoAltar(this, index);
			player.getPackets().sendGameMessage("<col=FF0040>"+(index == 0 ? "South" : index == 1 ? "West" : index == 2 ? "North" : "East") + "!");
		}
	}
	
	public void finishAltars() {
		if (altars == null)
			return;
		for (NPC n : altars)
			if (n != null && !n.hasFinished())
				n.finish();
	}
	
	public int getAliveAltarsCount() {
		if (altars == null)
			return 0;
		int count = 0;
		for (NPC n : altars)
			if (n != null && !n.isDead() && !n.hasFinished())
				count++;
		return count;
	}
	@Override
	public void process() {
		if (!isRunning()) 
			return;
		timer++;
		if (timer % 100 == 0)
			playMusic(); // so that music doesnt get replaced
	}
	
	
	@Override
	public boolean processObjectClick1(WorldObject object) {
		if (!isRunning()) 
			return false;
		if (object.getId() == 128925) {
			exit(NORMAL_EXIT);
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
