package com.rs.game.player.controllers;

import com.rs.Settings;
import com.rs.game.Animation;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.map.MapInstance.Stages;
import com.rs.game.player.Player;
import com.rs.game.player.content.construction.HouseConstants;
import com.rs.game.player.content.raids.cox.ChambersHerblore;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.ChambersFarming;
import com.rs.game.player.content.raids.cox.chamber.Chamber;
import com.rs.game.player.content.raids.cox.chamber.impl.GreatOlmChamber;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

import java.util.ArrayList;
import java.util.Objects;

public class ChambersOfXericController extends Controller {

	private ChambersOfXeric raid;
	private int timer;
	
	@Override
	public void start() {
		setup();
	}
	
	public void setup() {
		if (getArguments() == null || getArguments().length != 1) {
			removeControler();
			return;
		}
		WorldTile tile = (WorldTile) getArguments()[0];

		if (tile == null) { //shouldnt happen
			removeControler();
			return;
		}

		ChambersOfXeric raid = ChambersOfXeric.findInstance(player.getLastFriendsChat().toLowerCase());

		if (raid == null) {
			raid = ChambersOfXeric.findRaid(player);
			if(raid == null) {
				removeControler();
				return;
			}
		}

		this.raid = raid;
		player.forceTeleport(tile);
		sendInterfaces();
		player.getInterfaceManager().openGameTab(3);
	}

	@Override
	public void sendInterfaces() {
		if(raid.hasStarted()) {
			raid.writeRaidOverlayInfo(player);
			player.getInterfaceManager().setOverlay(3076, false);
		}

		player.getInterfaceManager().sendQuestTab(3078);
	}

	@Override
	public boolean logout() {
		if (!isRunning())
			return true;
		raid.handleDeath(player, false);
		raid.remove(player, ChambersOfXeric.LOGOUT);
		return false;
	}
	
	//raid.playMusic(player, 1);
	@Override
	public void process() {
		sendInterfaces();
		if (!isRunning() || player.isLocked())
			return;
		timer++;
		if (timer % 100 == 0)
			raid.playMusic(player, player.getPlane()); // so that music doesnt get replaced
		//means kicked or left fc
		getRaid().getTeam().stream().filter(Objects::nonNull).forEach(p -> {
			// process loop for team
			ChambersOfXeric.checkFC(p);
		});
	}
	
	@Override
	public boolean processObjectClick1(WorldObject object) {
		if (!isRunning())
			return false;
		if (object.getId() == 129778) {

			raid.remove(player, ChambersOfXeric.LEAVE);
			return false;
		} else if (object.getDefinitions().getToObjectName(player).equalsIgnoreCase("passage")
				&& object.getDefinitions().containsOption(0, "enter")) {


			if(raid.isOsrsRaid()) {
				// check for osrs items when entering raid
				// players can drop onyx items in lobby and pickup after raid starts
				ArrayList<Item> bannedItems = new ArrayList<>();
				for(Item item : player.getEquipment().getItems().getItems()) {
					if(item == null) continue;
					if(ChambersOfXeric.checkBlacklist(item.getId()))
						bannedItems.add(item);
				}
				for(Item item : player.getInventory().getItems().getItems()) {
					if(item == null) continue;
					if(ChambersOfXeric.checkBlacklist(item.getId()))
						bannedItems.add(item);
				}
				String s = ChambersOfXeric.getBannedItemsString(bannedItems);
				if(bannedItems.size() > 0) {
					player.sendMessage("<col=ff0000>You have banned items: " + s);
					player.sendMessage("<col=ff0000>You must bank these items before starting an OSRS raid.");
					player.stopAll();
					return false;
				}
			}
			if (!raid.hasStarted()) {
				if (!player.getCurrentFriendsChat().isOwner(player)) {
					player.getDialogueManager().startDialogue("SimpleMessage","The raid has not started yet.");
					return false;
				} else {
					player.getDialogueManager().startDialogue("SimpleMessage","Use the raid controls in quest tab to start the raid.");
					return false;
				}
			}
			if(raid.getCurrentChamber(player) != null && !raid.getCurrentChamber(player).chamberCompleted(player)) {
				return false;
			}
			if (object.getRotation() == 0 || object.getRotation() == 2) 
				player.useStairs(-1, player.transform(object.getX() >= player.getX() ? 4 : -4, 0, 0), 0, 1);
			else if (object.getRotation() == 1 || object.getRotation() == 3) 
				player.useStairs(-1, player.transform(0, object.getY() >= player.getY() ? 4 : -4, 0), 0, 1);
			return false;
		} else if (object.getId() == 129734 && player.getPlane() == 3) { //floor3
			if (true == true) {
				player.getDialogueManager().startDialogue("SkipCoxD", raid);
				return false;
			}
			if(raid.checkpoint(player) && player.getOsrsChambersCompletions() + player.getChambersCompletions() < 3) {
				WorldTasksManager.schedule(() -> {
					player.getDialogueManager().startDialogue("SimpleMessage", "<col=ff0000>If you are having graphics issues - please re-log. Your position is saved and these issues will be fixed.");
				});
			}
			//if(!raid.checkpoint(player)) {
				player.useStairs(827, raid.getTile(144, 47, 2), 1, 2);
				raid.playMusic(player, 2);
			//}
			return false;
		} else if (object.getId() == 132543 && player.getPlane() == 2) {//floor3
			player.useStairs(828, raid.getTile(146, 47, 3), 1, 2);
			raid.playMusic(player, 3);
			return false;
		} else if (object.getId() == 129734 && player.getPlane() == 2) {//floor2
			if(raid.checkpoint(player) && player.getOsrsChambersCompletions() + player.getChambersCompletions() < 3) {
				WorldTasksManager.schedule(() -> {
					player.getDialogueManager().startDialogue("SimpleMessage", "<col=ff0000>If you are having graphics issues - please re-log. Your position is saved and these issues will be fixed.");
				});
			}
			player.useStairs(827, raid.getTile(144, 79, 1), 1, 2);
			raid.playMusic(player, 1);

			return false;
		} else if (object.getId() == 132543 && player.getPlane() == 1) {//floor2
			player.useStairs(828, raid.getTile(144, 81, 2), 1, 2);
			raid.playMusic(player, 2);
			return false;
		} else if (object.getId() == 129735 && player.getPlane() == 1) {//floor1
			player.useStairs(827, raid.getTile(128, 25, 0), 1, 2);
			raid.playMusic(player, 0);
			raid.checkpoint(player);
			WorldTasksManager.schedule(() -> {
				player.getDialogueManager().startDialogue("SimpleMessage", "<col=ff0000>If you are having graphics issues - please re-log. Your position is saved and these issues will be fixed.");
			});
			return false;
		} else if (object.getId() == 132543 && player.getPlane() == 0) {//floor1
			player.useStairs(828, raid.getTile(149, 51, 1), 1, 2);
			raid.playMusic(player, 1);
			return false;
		}


		for(ChambersFarming patch : ChambersFarming.values()) {
			if (object.getId() == patch.objectIdEnd) {
				ChambersFarming.pickHerbPatch(player, object, patch);
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean handleItemOnObject(WorldObject object, Item item) {
		if(object.getId() == 129878 && item.getId() == ChambersHerblore.EMPTY_GOURD_VIAL) {
			ChambersHerblore.fillGourdVials(player);
			return false;
		}
		for(ChambersFarming patch : ChambersFarming.values()) {
			if(object.getId() == 129765 &&
					item.getId() == patch.seedId) {
				ChambersFarming.plantSeed(player, item, object, patch);
				return false;
			}
		}

		for(ChambersFarming patch : ChambersFarming.values()) {
			if (object.getId() == patch.objectIdEnd) {
				ChambersFarming.pickHerbPatch(player, object, patch);
				return false;
			}
		}
		return super.handleItemOnObject(object, item);
	}

	@Override
	public boolean canUseItemOnItem(Item itemUsed, Item usedWith) {
		if(ChambersHerblore.make(player, itemUsed, usedWith))
			return false;
		return super.canUseItemOnItem(itemUsed, usedWith);
	}

	@Override
	public boolean processObjectClick2(WorldObject object) {
		for(ChambersFarming patch : ChambersFarming.values()) {
			if (object.getId() == patch.objectIdEnd) {
				ChambersFarming.clearHerbPatch(player, object);
				return false;
			}

			for (int i = 0; i < 5; i++) {
				if (object.getId() == patch.objectIdStart + (3 * i)) {
					ChambersFarming.inspectHerbPatch(player, object, patch);
					return false;
				}
			}
		}
		return super.processObjectClick2(object);
	}

	@Override
	public boolean processObjectClick3(WorldObject object) {
		for(ChambersFarming patch : ChambersFarming.values()) {
			for (int i = 0; i < 5; i++) {
				if (object.getId() == patch.objectIdStart + (3 * i)) {
					ChambersFarming.clearHerbPatch(player, object);
					return false;
				}
			}
		}
		return super.processObjectClick3(object);
	}

	@Override
	public boolean processObjectClick5(WorldObject object) {

		if(object.getId() == 129769 || object.getId() == 129770  || object.getId() == 129779  || object.getId() == 127780) {
			for (HouseConstants.Builds build : HouseConstants.Builds.values()) {
				if (build.containsId(object.getId())) {
					player.getHouse().openBuildInterface(object, build, false);
					return false;
				}
			}
		}
		return true;
	}

	public ChambersOfXeric getRaid() {
		return raid;
	}
	
	@Override
	public void moved() {
		if (!Settings.HOSTED) {
			int x = player.getX() - (raid.getInstancePos()[0] * 8);
			int y = player.getY() - (raid.getInstancePos()[1] * 8);
			System.out.println(x+", "+y);
		}
	}
	
	public boolean isRunning() { 
		return raid != null && raid.getStage() == Stages.RUNNING;
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		player.sendMessage("You cannot teleport from the Chambers of Xeric.");
		return false;
	}
	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		player.sendMessage("You cannot teleport from the Chambers of Xeric.");
		return false;
	}
	@Override
	public boolean processObjectTeleport(WorldTile toTile) {
		player.sendMessage("You cannot teleport from the Chambers of Xeric.");
		return false;
	}

	public void magicTeleported(int type) {
		if (!isRunning())
			return;
		raid.remove(player, ChambersOfXeric.TELEPORT);
	}
	
	@Override
	public boolean sendDeath() {
		Chamber lastChamber = raid.getCurrentChamber(player);

		player.lock(8);
		player.stopAll();
		player.getPackets().sendStopCameraShake();

		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					player.getPackets().sendGameMessage("Oh dear, you have died.");
				} else if (loop == 3) {
					if(raid.getCurrentChamber(player) == raid.getGreatOlmChamber()) {
						// reset head after death or olm will be repeating anim sent before death
						raid.getGreatOlmChamber().getOlm().resetOlmHeadAnim();
					}
					player.reset();
					if(lastChamber != null && lastChamber instanceof GreatOlmChamber) {
						// fail safe
						player.setNextWorldTile(raid.getTile(144, 48, 1));
					} else {
						player.setNextWorldTile(raid.checkpointTile.clone());
					}
					raid.handleDeath(player, true);
					//raid.remove(player, ChambersOfXeric.LEAVE);
				} else if (loop == 4) {
					player.getPackets().sendMusicEffect(90);
					player.anim(-1);
					stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}
	
	public void forceRemove() {
		
	}


}
