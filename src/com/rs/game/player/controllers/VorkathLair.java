/**
 * 
 */
package com.rs.game.player.controllers;

import com.rs.game.Animation;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.map.MapInstance;
import com.rs.game.map.MapInstance.Stages;
import com.rs.game.npc.NPC;
import com.rs.game.npc.slayer.Vorkath;
import com.rs.game.player.Player;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.content.Summoning;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Jan 11, 2018
 */
public class VorkathLair extends Controller {

	private static final WorldTile OUTSIDE = new WorldTile(2272, 4052, 0), ENTRANCE = new WorldTile(2272, 4054, 0);
	
	public static void enter(Player player) {
		if (player.getFamiliar() != null ||/* player.getPet() != null ||*/ Summoning.hasPouch(player)
				/*|| Pets.hasPet(player)*/) {
			player.getDialogueManager().startDialogue("SimpleMessage","You don't want your friends to be eaten by Vorkath. You are not allowed to take pets nor familiars onto Vorkath's Lair.");
			return;
		}
		player.getControlerManager().startControler("VorkathLair");
	}
	
	
	public static void travel(Player player) {
		player.lock();
		boolean ungael = player.getRegionId() == 9023;
		FadingScreen.fade(player, 0, new Runnable() {
			@Override
			public void run() {
				player.getPackets().sendGameMessage("You board the boat and travel to "+(!ungael ? "Ungael" : "Fremmenik") +".");
				player.useStairs(-1, !ungael ? new WorldTile(2278, 4035, 0) :new WorldTile(2641, 3697, 0), 0, 1);
			}
		});
	}

	private MapInstance map;
	private Vorkath boss;
	private int timer;
	
	private void playMusic() {
		player.getMusicsManager().playOSRSMusic("On Thin Ice");
	}

	@Override
	public void start() {
		load();
	}
	
	@Override
	public void process() {
		if (!isRunning()) 
			return;
		timer++;
		if (timer % 100 == 0)
			playMusic(); // so that music doesnt get replaced
		if (boss != null && boss.getAcidPools() != null && boss.getAcidPools().contains(player.getTileHash())) {
			int damage = Utils.random(100)+1;
			player.applyHit(new Hit(boss, damage, HitLook.REGULAR_DAMAGE));
			boss.applyHit(new Hit(player, damage, HitLook.HEALED_DAMAGE));
		}
	}
	
	@Override
	public boolean processObjectClick1(WorldObject object) {
		if (!isRunning()) 
			return false;
		if (object.getId() == 131990) {
			player.setNextAnimation(new Animation(4853));
			player.getPackets().sendGameMessage("You climb over the ice.");
			exit(NORMAL_EXIT);
			return false;
		}
		return true;
	}
	
	@Override
	public void processNPCDeath(NPC npc) {
		if (npc == boss && isRunning())//respawn
			boss = new Vorkath(this);
	}
	
	@Override
	public boolean processNPCClick1(NPC npc) {
		if (!isRunning()) 
			return false;
		if (npc instanceof Vorkath) {
			((Vorkath)npc).awake();
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

	private void load() {
		player.lock();
		player.stopAll();
		map = new MapInstance(280, 504);
		map.load(() -> {
			player.useStairs(4853, map.getTile(ENTRANCE), 1, 2, "You climb over the ice.");
			playMusic();
			boss = new Vorkath(this);
		});
	}
	
	private static final int LOGOUT_EXIT = 0, NORMAL_EXIT = 1, TELE_EXIT = 2;
	
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

