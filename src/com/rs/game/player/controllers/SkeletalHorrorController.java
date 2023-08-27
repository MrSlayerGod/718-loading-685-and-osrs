package com.rs.game.player.controllers;

import com.rs.game.*;
import com.rs.game.item.Item;
import com.rs.game.map.MapInstance;
import com.rs.game.map.MapInstance.Stages;
import com.rs.game.npc.NPC;
import com.rs.game.npc.skeletalhorror.SkeletalHorror;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.content.Magic;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.InventoryOptionsHandler;
import com.rs.net.decoders.handlers.NPCHandler;
import com.rs.net.decoders.handlers.ObjectHandler;
import com.rs.utils.Utils;

import static com.rs.game.npc.skeletalhorror.SkeletalHorror.*;

/**
 * @author Simplex
 * created on 2021-01-31
 */
public class SkeletalHorrorController extends Controller {

	public static void init() {

		// Bury extremity
		InventoryOptionsHandler.register(new int[] {
				LEFT_HAND_ITEM_ID,
				RIGHT_HAND_ITEM_ID,
				TAIL_ITEM_ID}, 1, SkeletalHorrorController::buryExtremity);

		// pick up arm/tail
		NPCHandler.register(new int[] {
				LEFT_NPC_HAND_ID,
				RIGHT_NPC_HAND_ID,
				TAIL_NPC_ID}, 1, SkeletalHorrorController::pickupExtremity);

		// start instance
		ObjectHandler.register(ENTER_INSTANCE_STILE, 1, SkeletalHorrorController::enterInstance);

		ObjectHandler.register(ENTER_FIGHT_STILE, 1, ((player, obj) -> {
			if(player.getX() > obj.getX()) {
				// end fight
				Magic.sendCommandTeleportSpell(player, OUTSIDE);
				return;
			}

			// walk into arena
			player.lock();
			player.setNextAnimation(new Animation(4853));
			player.resetWalkSteps();
			player.setNextForceMovement(new ForceMovement(player, 0, player.relative(2, 0), 2, ForceMovement.EAST));
			WorldTasksManager.schedule(() -> {
				player.setNextWorldTile(player.relative(2, 0));
				player.unlock();
			});
		}));

	}

	private static void buryExtremity(Player player, Item item) {
		if(player.getInventory().containsItem(item)) {
			player.setNextAnimation(new Animation(827));
			player.getInventory().deleteItem(item);
			player.sendMessage("You bury the Skeletal horror's " + item.getName() + ".");
			player.getSkills().addXp(Skills.PRAYER, 250);
		}
	}

	private static void pickupExtremity(Player player, NPC npc) {
		if(player.getControlerManager().getControler() != null
				&& player.getControlerManager().getControler() instanceof SkeletalHorrorController) {
			player.setNextAnimation(new Animation(827));
			int itemId = npc.getId() == LEFT_NPC_HAND_ID ? LEFT_HAND_ITEM_ID : npc.getId() == RIGHT_NPC_HAND_ID ? RIGHT_HAND_ITEM_ID : TAIL_ITEM_ID;
			if(player.getInventory().add(new Item(itemId)) != null) {
				player.sendMessage("Your inventory is full!");
			} else {
				npc.finish();
			}
		}
	}

	public static void enterInstance(Player player, WorldObject obj) {
		long nextHorror = ((player.isDonator() ? 4 : 8)*60*60*1000);
		long elapsed = Utils.currentTimeMillis() - player.getLastSkeletalHorror();
		if(elapsed <= nextHorror) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", 9173, "I haven't finished piecing the the skeleton back together yet..", "Come back in " + Utils.formatTime(nextHorror - elapsed) +" ..");
			return;
		}
		player.resetWalkSteps();
		player.getControlerManager().startControler(new SkeletalHorrorController());
	}

	private MapInstance instance;
	private SkeletalHorror horror;
	private int timer;
	
	@Override
	public void start() {
		enter();
	}
	
	@Override
	public boolean processObjectClick1(WorldObject object) {
		return true;
	}
	
	@Override
	public boolean processNPCClick1(NPC npc) {
		return true;
	}
	
	public void enter() {
		instance = new MapInstance(419, 437, 2, 2);
		player.lock();
		player.setNextAnimation(new Animation(4853));

		player.setNextForceMovement(new ForceMovement(player, 0, player.relative(2, 0), 2, ForceMovement.EAST));
		WorldTasksManager.schedule(() -> {
			player.setNextWorldTile(player.relative(2, 0));
		});
		final long time = FadingScreen.fade(player);
		instance.load(() -> FadingScreen.unfade(player, time, () -> {
			player.setNextWorldTile(instance.getTile(17, 17));
			player.setForceMultiArea(true);
			horror = new SkeletalHorror(instance.getTile(35, 20), SkeletalHorrorController.this);
			horror.setTarget(player);
			player.unlock();
			startFight();
		}));
	}
	
	//0 - logout
	//1 - teleport / death
	//2 - leave
	public void leave(int type) {
		player.stopAll();
		player.getInventory().removeItems(
				new Item(LEFT_HAND_ITEM_ID, 28),
				new Item(RIGHT_HAND_ITEM_ID, 28),
				new Item(TAIL_ITEM_ID, 28));

		if(type != 0) {
			if(type == 1)
				player.lock(3);
			else
				player.useStairs(17803, OUTSIDE, 2, 3);
			player.setForceMultiArea(false);
			player.getMusicsManager().reset();
			removeControler();
		}else
			player.setLocation(OUTSIDE);
		instance.destroy(null);
	}
	
	@Override
	public boolean logout() {
		leave(0);
		return true;
	}
	
	@Override
	public boolean login() {
		player.setNextWorldTile(OUTSIDE);
		return true; //shouldnt happen
	}
	
	@Override
	public void magicTeleported(int type) {
		leave(1);
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
					leave(1);
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

	public void startFight() {
		player.unlock();
		horror.setCantInteract(false);
	}
	
	@Override
	public void process() {
	}

	public void completed() {
		horror = null;
		player.setLastSkeletalHorror(Utils.currentTimeMillis());
	}
	
	public Stages getStage() {
		return instance.getStage();
	}
}
