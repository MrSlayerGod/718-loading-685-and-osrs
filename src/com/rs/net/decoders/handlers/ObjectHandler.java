package com.rs.net.decoders.handlers;

import com.rs.Settings;
import com.rs.cache.loaders.ObjectConfig;
import com.rs.game.*;
import com.rs.game.Hit.HitLook;
import com.rs.game.item.Item;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.minigames.*;
import com.rs.game.minigames.pest.Lander;
import com.rs.game.minigames.pktournament.PkTournament;
import com.rs.game.minigames.stealingcreation.StealingCreationLobbyController;
import com.rs.game.npc.NPC;
import com.rs.game.npc.holiday.EvilSanta;
import com.rs.game.npc.others.Mogre;
import com.rs.game.npc.others.PolyporeCreature;
import com.rs.game.npc.others.TreeSpirit;
import com.rs.game.npc.others.zalcano.Zalcano;
import com.rs.game.player.*;
import com.rs.game.player.QuestManager.Quests;
import com.rs.game.player.actions.*;
import com.rs.game.player.actions.Cooking.Cookables;
import com.rs.game.player.actions.Fishing.FishingSpots;
import com.rs.game.player.actions.Smelting.SmeltingBar;
import com.rs.game.player.actions.construction.BoneOffering;
import com.rs.game.player.actions.firemaking.Bonfire;
import com.rs.game.player.actions.mining.*;
import com.rs.game.player.actions.mining.EssenceMining.EssenceDefinitions;
import com.rs.game.player.actions.mining.Mining.RockDefinitions;
import com.rs.game.player.actions.mining.MiningBase.PickAxeDefinitions;
import com.rs.game.player.actions.runecrafting.SiphionActionNodes;
import com.rs.game.player.actions.thieving.Thieving;
import com.rs.game.player.actions.woodcutting.DreamTreeWoodcutting;
import com.rs.game.player.actions.woodcutting.EvilRootWoodcutting;
import com.rs.game.player.actions.woodcutting.EvilTreeWoodcutting;
import com.rs.game.player.actions.woodcutting.Woodcutting;
import com.rs.game.player.actions.woodcutting.Woodcutting.TreeDefinitions;
import com.rs.game.player.actions.woodcutting.WoodcuttingBase.HatchetDefinitions;
import com.rs.game.player.content.*;
import com.rs.game.player.content.agility.*;
import com.rs.game.player.content.construction.House;
import com.rs.game.player.content.construction.HouseConstants;
import com.rs.game.player.content.dungeoneering.rooms.puzzles.FishingFerretRoom;
import com.rs.game.player.content.prayer.Burying.Bone;
import com.rs.game.player.content.prayer.Ectofuntus;
import com.rs.game.player.content.raids.TheatreOfBlood;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.impl.ThievingChamber;
import com.rs.game.player.content.teleportation.TeleportationInterface;
import com.rs.game.player.controllers.*;
import com.rs.game.player.controllers.partyroom.PartyBalloon;
import com.rs.game.player.controllers.partyroom.PartyRoom;
import com.rs.game.player.dialogues.impl.*;
import com.rs.game.player.dialogues.impl.UpgradeItemOption.Upgrade;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.io.InputStream;
import com.rs.utils.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public final class ObjectHandler {

	private ObjectHandler() {
	}

	public static void register(int[] id, int actionIndex, ObjectAction runnable) {
		for(int i : id) {
			register(i, actionIndex, runnable);
		}
	}

	public static void register(int id, int actionIndex, ObjectAction runnable) {
		actionRepository.putIfAbsent(id, new ObjectAction[10]);
		ObjectAction[] actionList = actionRepository.get(id);
		if(actionList[actionIndex] != null)
			System.err.println("Warning: " + id + " action " + actionIndex + " is being overwritten!");
		actionList[actionIndex] = runnable;
	}

	public static boolean handle(Player player, WorldObject object, int action) {
		if(action < 1)
			return false;
		if (actionRepository.containsKey(object.getId())) {
			//System.out.println("A " + object.getId() + " act " + action);
			ObjectAction act = actionRepository.get(object.getId())[action];
			if(act != null) {
				player.setRouteEvent(new RouteEvent(object, new Runnable() {
					@Override
					public void run() {
						player.faceObject(object);
						if (Utils.getDistance(player, object) > 2)
							return;
						if (!player.getControlerManager().processObjectClick1(object))
							return;
						act.handle(player, object);
						return;
					}
				}, true));
				return true;
			} else return false;
		}

		return false;
	}

	static HashMap<Integer, ObjectAction[]> actionRepository = new HashMap<Integer, ObjectAction[]>();

	public static void handleOption(final Player player, InputStream stream, int option) {
		if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead())
			return;
		if (player.isLocked() || player.getEmotesManager().getNextEmoteEnd() >= Utils.currentTimeMillis())
			return;
		boolean forceRun = stream.readUnsignedByte128() == 1;
		final int id = stream.readIntLE();
		int x = stream.readUnsignedShortLE();
		int y = stream.readUnsignedShortLE128();
		final WorldTile tile = new WorldTile(x, y, player.getPlane());
		final int regionId = tile.getRegionId();
		if (!player.getMapRegionsIds().contains(regionId))
			return;
		WorldObject mapObject = World.getObjectWithId(tile, id);
		if (mapObject == null || mapObject.getId() != id) {
			return;
		}
		final WorldObject object = mapObject;
		player.stopAll();
		if (forceRun)
			player.setRun(forceRun);

		if(!player.getStopwatch().finished()) {
			player.sendMessage("I can't reach that.");
			return;
		}

		if(handle(player, object, option)) {
			return;
		}

		switch (option) {
		case 1:
			handleOption1(player, object);
			break;
		case 2:
			handleOption2(player, object);
			break;
		case 3:
			handleOption3(player, object);
			break;
		case 4:
			handleOption4(player, object);
			break;
		case 5:
			handleOption5(player, object);
			break;
		case -1:
			handleOptionExamine(player, object);
			break;
		}
	}
	
	//part1 method is full. this isnt a proper way to handle objects but what can i say..
	private static boolean handleOption1_part2(final Player player, final WorldObject object) {
		final ObjectConfig objectDef = object.getDefinitions();
		final int id = object.getId();
		final int x = object.getX();
		final int y = object.getY();

		if (Ectofuntus.handleObjects(player, object.getId()))
			return true;
		if (CastleWars.handleObjects(player, id))
			return true;
		if (player.getDungManager().enterResourceDungeon(object))
			return true;
		if (player.getFarmingManager().isFarming(id, null, 1))
			return true;
		if ((id == 134947|| id == 128822 || (id >= 15477 && id <= 15482)) && House.enterHousePortal(player))
			return true;
		if (TrapAction.isTrap(player, object, id) || TrapAction.isTrap(player, object))
			return true;

		if (EvilTrees.isTree(object)) {
			if (!EvilTrees.isAlive())
				EvilTrees.claimRewards(player);
			else
				player.getActionManager().setAction(new EvilTreeWoodcutting());
			return true;
		}
		
		if (id == 126727) {
			player.getDialogueManager().startDialogue("PkTournamentD", PkTournament.NPC_ID, 0);
			return true;
		}
		
		if (id == EnhancedCrystalChest.CHEST_ID) {
			if (player.getInventory().containsItem(CrystalSinging.ENHANCED_CRYSTAL_KEY, 1)) {
				EnhancedCrystalChest.openChest(player, object);
				return true;
			}
			player.getDialogueManager().startDialogue("SimpleMessage",
					"This chest is securely locked shut. You need an Enhanced crystal key to open it!");
			return true;
		}

		if (id == 130397 && x == 3084 && y == 3497) {
			player.getDialogueManager().startDialogue("PkTournamentD", PkTournament.NPC_ID, 0);
			return true;
		}
		if (id == 110721) {
			World.removeObjectTemporary(object, 1200);
			player.addWalkSteps(x, y + (player.getY() >= 5088 ? 1 : 1), -1, false);
			return true;
		}

		if(id == PartyRoom.PARTY_CHEST_CLOSED) {
			PartyRoom.openChest(player, object);
			return true;
		}
		if(id == PartyRoom.PARTY_CHEST_OPEN) {
			PartyRoom.openPartyChest(player);
			return true;
		}
		if (id == CrystalSinging.SINGING_BOWL) {
			player.getDialogueManager().startDialogue("CrystalSinging", object);
			return true;
		}
		if (id == 11426) {
			player.getActionManager().setAction(new EvilRootWoodcutting(object));
			return true;
		}
		if (id == 131673) {
			GrotesqueGuardianLair.enter(player);
			return true;
		}
		if (id == 131989) {
			VorkathLair.travel(player);
			return true;
		}
		if (id == 131990) {
			VorkathLair.enter(player);
			return true;
		}
		if (id == 133614) {
			AerialFishing.enter(player);
			return true;
		}
		if (id == 134548 && x == 1351 && y == 10251) {
			HydraLair.enter(player);
			return true;
		}

		if(id == Zalcano.DOOR_ID) {
			Zalcano.handleJoin(player);
			return true;
		}

		if (id == 213 || id == 214) {
			Agility.faladorGrappleWall(player, object);
			return true;
		}
		
		if (id == 9309 || id == 9310) {
			Agility.faladorTunnel(player, object);
			return true;
		}
		
		if (id == 17051 || id == 17052) {
			Agility.faladorJumpDown(player, object);
			return true;
		}
		
		if (id == 11844) {
			Agility.faladorCrumbledWall(player, object);
			return true;
		}
		
		//jormungand prison
		if (id == 137408) {
			player.useStairs(-1, new WorldTile(2620, 3687, 0), 0, 1);
			return true;
		}
		if (id == 137411) {
			player.useStairs(-1, new WorldTile(2465, 4010, 0), 0, 1);
			return true;
		}
		if (id == 137433) {
			player.useStairs(-1, new WorldTile(2461, 10417, 0), 0, 1);
			return true;
		}
		if (id == 137417) {
			player.useStairs(-1, new WorldTile(player.getX() == 2471 ? 2468 : 2471, 10403, 0), 0, 1);
			return true;
		}
		if (id == 26074) {
			player.getActionManager().setAction(new SitRoundTable());
			return true;
		}
		//vip zone
		 if (id == 129241 || id == 62594) {
				/*if (!player.isDiamondDonator()) {
					player.getPackets().sendGameMessage("You must be a diamond donator in order to access this feature.");
					player.getPackets().sendGameMessage("If you would like to subscribe and become a diamond donator, please do the command ::donate to learn how.");
					return;
				}*/
			 if (id == 62594) {
					Long cd = (Long) player.getTemporaryAttributtes().get("LAST_POOL_HEAL");
					if (cd != null && cd + 30000 > Utils.currentTimeMillis()) {
						player.getPackets().sendGameMessage("You can only use this every 30 seconds!");
						return true;
					}
					player.getTemporaryAttributtes().put("LAST_POOL_HEAL", Utils.currentTimeMillis());
			 	}
				player.lock(1);
				player.heal(player.getMaxHitpoints());
				player.getPrayer().restorePrayer(990);
				player.getPoison().reset();
				player.getSkills().set(Skills.SUMMONING, player.getSkills().getLevelForXp(Skills.SUMMONING));
				player.getPackets().sendGameMessage("The rejuvenation pool restores you.");
				
				if (player.isLegendaryDonator())
					player.getCombatDefinitions().restoreSpecialAttack(100);
				else
					player.getPackets().sendGameMessage("Become a legendary donator to also restore special attack!");
				player.setNextGraphics(new Graphics(444));
				return true;
		 }
		if (id == 31845 && x == 3681 && y == 5551) {
			if (!player.isVIPDonator()) {
				player.getPackets().sendGameMessage("You must be an onyx donator in order to access this feature.");
				player.getPackets().sendGameMessage("If you would like to subscribe and become an onyx donator, please do the command ::donate to learn how.");
				return true;
			}
			player.getDialogueManager().startDialogue("VipOnyxD");
			return true;
		}
		if (id == 20608 && x == 2968 && y == 9670) {
			player.useStairs(-1, new WorldTile(1677, 5599, 0), 0, 1);
			return true;
		}
		if (id == 30203 && x == 1675 && y == 5598) {
			player.useStairs(-1, new WorldTile(2970, 9672, 0), 0, 1);
			return true;
		}
		if (id == 30224 && x == 1719 && y == 5598) {
			TheHorde.enter(player);
			return true;
		}
		//prifinas
		if (id == 136221) {//temp disabled
			player.getPackets().sendGameMessage("This feature has not been added yet. Expect it soon!");
			return true;
		}
		if (id == 136219) {
			player.getBank().openDepositBox();
			return true;
		}
		if (id == 136390 && x == 2242 && y == 3330) {
			player.useStairs(-1, new WorldTile(2240, 3326, 0), 0, 1);
			return true;
		}
		if (id == 136387 && x == 2239 && y == 3327) {
			player.useStairs(-1, new WorldTile(2244, 3330, 2), 0, 1);
			return true;
		}
		if (id == 136523 || id == 136522) {
			player.addWalkSteps(player.getX() + 
					(object.getRotation() == 1 || object.getRotation() == 3 ? (player.getX() > object.getX() ? -3 : 3) : 0) , player.getY()
					+ (object.getRotation() == 0 || object.getRotation() == 2 ? (player.getY() > object.getY() ? -3 : 3) : 0), 3, false);
			player.lock(3);
			return true;
		}
		if (id == 136490) {
			player.setNextGraphics(new Graphics(2000));
			player.useStairs(-1, player.transform(0, 0, player.getPlane() == 0 ? 2 : -2), 0, 1);
			return true;
		}
		if (id == 136614) { //grand library teleport plataform
			player.setNextGraphics(new Graphics(2000));
			player.useStairs(-1, new WorldTile(3232, 12537, 0), 0, 1);
			return true;
		}
		if (id == 136615) { //grand library teleport plataform
			player.setNextGraphics(new Graphics(2000));
			player.useStairs(-1, new WorldTile(2232, 3330, 2), 0, 1);
			return true;
		}
		if (id == 136081) { // the gaunglet
			player.setNextGraphics(new Graphics(2000));
			player.useStairs(-1, new WorldTile(3033, 6125, 1), 0, 1);
			return true;
		}
		if (id == 136082) {
			player.setNextGraphics(new Graphics(2000));
			player.useStairs(-1, new WorldTile(2204, 3364, 0), 0, 1);
			return true;
		}
		if (id == 136198) { //zalcano
			player.setNextGraphics(new Graphics(2000));
			player.useStairs(-1, new WorldTile(3034, 6068, 0), 0, 1);
			return true;
		}
		if (id == 136197) {
			player.setNextGraphics(new Graphics(2000));
			player.useStairs(-1, new WorldTile(2259, 3306, 0), 0, 1);
			return true;
		}
		if (id == 136556) { //prif mine
			player.useStairs(-1, new WorldTile(3302, 12454, 0), 0, 1);
			return true;
		}
		if (id == 136215) { //prif exit
			player.useStairs(-1, new WorldTile(2247, 3299, 0), 0, 1);
			return true;
		}
		if (id == 136690) { //prif slayer dung
			player.useStairs(-1, new WorldTile(3225, 12445, 0), 0, 1);
			return true;
		}
		if (id == 136691) { 
			player.useStairs(-1, new WorldTile(2202, 3294, 0), 0, 1);
			return true;
		}
		if (id == 136695 || id == 136694) {
			if (player.getSkills().getLevel(Skills.AGILITY) < 60) {
				player.getPackets().sendGameMessage("You need an agility level of 60 to use this obstacle.");
				return true;
			}
			player.lock();
			WorldTasksManager.schedule(new WorldTask() {
				int count = 0;

				@Override
				public void run() {
					if (count == 0) {
						player.setNextAnimation(new Animation(2594));
						WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -2 : +2),
								object.getY(), 0);
						player.setNextForceMovement(new ForceMovement(tile, 4, Utils
								.getMoveDirection(tile.getX() - player.getX(), tile.getY() - player.getY())));
					} else if (count == 2) {
						WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -2 : +2),
								object.getY(), 0);
						player.setNextWorldTile(tile);
					} else if (count == 5) {
						player.setNextAnimation(new Animation(2590));
						WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -5 : +5),
								object.getY(), 0);
						player.setNextForceMovement(new ForceMovement(tile, 4, Utils
								.getMoveDirection(tile.getX() - player.getX(), tile.getY() - player.getY())));
					} else if (count == 7) {
						WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -5 : +5),
								object.getY(), 0);
						player.setNextWorldTile(tile);
					} else if (count == 10) {
						WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -9 : +9),
								object.getY(), 0);
						player.setNextForceMovement(new ForceMovement(tile, 4, Utils
								.getMoveDirection(tile.getX() - player.getX(), tile.getY() - player.getY())));
					} else if (count == 12) {
						player.setNextAnimation(new Animation(2595));
						WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -9 : +9),
								object.getY(), 0);
						player.setNextWorldTile(tile);
					} else if (count == 14) {
						stop();
						player.unlock();
					}
					count++;
				}

			}, 0, 0);
			return true;
		}
		if (id == 136693 || id == 136692) {
				if (player.getSkills().getLevel(Skills.AGILITY) < 42) {
					player.getPackets().sendGameMessage("You need an agility level of 42 to use this obstacle.");
					return true;
				}
				player.lock();
				WorldTasksManager.schedule(new WorldTask() {
					int count = 0;

					@Override
					public void run() {
						if (count == 0) {
							player.setNextAnimation(new Animation(2594));
							WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -2 : +2),
									object.getY(), 0);
							player.setNextForceMovement(new ForceMovement(tile, 4, Utils
									.getMoveDirection(tile.getX() - player.getX(), tile.getY() - player.getY())));
						} else if (count == 2) {
							WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -2 : +2),
									object.getY(), 0);
							player.setNextWorldTile(tile);
						} else if (count == 5) {
							WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -5 : +5),
									object.getY(), 0);
							player.setNextForceMovement(new ForceMovement(tile, 4, Utils
									.getMoveDirection(tile.getX() - player.getX(), tile.getY() - player.getY())));
						} else if (count == 7) {
							player.setNextAnimation(new Animation(2595));
							WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -5 : +5),
									object.getY(), 0);
							player.setNextWorldTile(tile);
						} else if (count == 9) {
							stop();
							player.unlock();
						}
						count++;
					}

				}, 0, 0);
				return true;
		}
		if (id == 128890 && x == 1639 && y == 10038) { //kourend shortcut
			final PickAxeDefinitions defs = MiningBase.getPickAxeDefinitions(player, false);
			if (defs == null) {
				player.getPackets().sendGameMessage("You need a usable pickaxe in order to clear this obstacle.");
				return true;
			}
			player.useStairs(defs.getAnimationId(), player.transform(0, player.getY() < object.getY() ? 3 : -3, 0), 2, 3, null, true);
			return true;
		}
		//farm guild
		if (id == 132153) {
			if ((x == 1248 || x == 1249) && y == 3746) {
				if (player.getSkills().getLevelForXp(Skills.FARMING) < 85) {
					player.getDialogueManager().startDialogue("SimpleNPCMessage", 2234,
							"Come back once you are level 85 farming.");
					return true;
				}
			}
			if ((y == 3729 || y == 3730) && x == 1242) {
				if (player.getSkills().getLevelForXp(Skills.FARMING) < 65) {
					player.getDialogueManager().startDialogue("SimpleNPCMessage", 2234,
							"Come back once you are level 65 farming.");
					return true;
				}
			}
		}
		if (id == 134477) {
			player.useStairs(828, player.transform(object.getRotation() == 3 ? -2 : 2, 0, 1), 1, 2);
			return true;
		}
		if (id == 134478) {
			player.useStairs(827, player.transform(object.getRotation() == 3 ? 2 : -2, 0, -1), 1, 2);
			return true;
		}
		if (id == 126206) { //seed vault
			player.getBank().openBank();
			return true;
		}
		if (id == 134463 || id == 134464) { // farm guild
				if (World.isSpawnedObject(object))
					return true;
				if (player.getSkills().getLevelForXp(Skills.FARMING) < 45) {
					player.getDialogueManager().startDialogue("SimpleNPCMessage", 2234,
							"Come back once you are level 45 farming.");
					return true;
				}

				WorldObject openedDoor = new WorldObject(object.getId(), object.getType(), object.getRotation() - 1,
						object.getX(), object.getY(), object.getPlane());
				World.spawnObjectTemporary(openedDoor, 1200);
				player.lock(2);
				player.stopAll();
				player.addWalkSteps(object.getX(), player.getY() >= object.getY() ? object.getY() - 1 : object.getY(), -1,
						false);
				if (player.getY() < object.getY())
					player.getDialogueManager().startDialogue("SimpleNPCMessage", 2234,
							"Greetings accomplished adventurer. Welcome to the guild of", "Farmers.");
			return true;
		}
		if (id == 134662 || id == 134660) {
			BrimstoneChest.openChest(player);
			return true;
		}
		switch (objectDef.getToObjectName(player).toLowerCase()) {
		case "limestone rocks":
		case "pile of rock":
			if (objectDef.containsOption(0, "Mine"))
				player.getActionManager().setAction(new Mining(object, RockDefinitions.Limestone));
			return true;
		}
		return false;
	}

	public static void handleOption1(final Player player, final WorldObject object) {
		final ObjectConfig objectDef = object.getDefinitions();
		final int id = object.getId();
		final int x = object.getX();
		final int y = object.getY();

		if (id == 14939) {
			player.setRouteEvent(new RouteEvent(object, new Runnable() {
				@Override
				public void run() {
					player.faceObject(object);
					if (Utils.getDistance(player, object) > 2)
						return;
					if (!player.getControlerManager().processObjectClick1(object))
						return;
					player.getActionManager().setAction(new Fishing(FishingSpots.NET4, null));
					return;
				}
			}, true));
			return;
		}
		
		if (id == 67044) {
			player.setRouteEvent(new RouteEvent(object, new Runnable() {
				@Override
				public void run() {
					// unreachable objects exception
					player.faceObject(object);
					player.useStairs(-1, new WorldTile(2924, 3408, 0), 0, 1);
				}
			}, true));
			return;
		}
		if (id == ThievingChamber.EMPTY_TROUGH || id == ThievingChamber.FILLED_TROUGH) {
			player.setRouteEvent(new RouteEvent(object, new Runnable() {
				@Override
				public void run() {
					// unreachable objects exception
					player.faceObject(object);
					player.useStairs(-1, new WorldTile(2924, 3408, 0), 0, 1);
				}
			}, true));
			return;
		}
		if (id == 65734) {
			if(player.getY() < object.getY()) {
				player.sendMessage("I can't reach that!");
				return;
			} else {
				// climbUpWall will set route event properly
				player.setRouteEvent(null);
			}
			WildernessAgility.climbUpWall(player, object);
			return;
		}
		if (id == PartyRoom.PARTY_LEVER) {
			player.setRouteEvent(new RouteEvent(object, new Runnable() {
				@Override
				public void run() {
					// unreachable objects exception
					player.getDialogueManager().startDialogue("PartyRoomLever");
				}
			}, true));
			return;
		}
		if (id == 132297) {
			ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
			if(raid != null)
				raid.getGreatOlmChamber().getOlm().douseFlameWall(player, object);
			return;
		}
		if (id == Settings.OSRS_OBJECTS_OFFSET + 29777) {
			player.setRouteEvent(new RouteEvent(object, new Runnable() {
				@Override
				public void run() {
					// unreachable objects exception
					ChambersOfXeric.enter(player);
				}
			}, true));
			return;
		}
		if (id == Settings.OSRS_OBJECTS_OFFSET + 32297) {
			ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
			if (raid != null && raid.getCurrentChamber(player) == raid.getGreatOlmChamber()) {
				player.setRouteEvent(new RouteEvent(object, new Runnable() {
					@Override
					public void run() {
						// unreachable objects exception
						player.resetWalkSteps();
						raid.getGreatOlmChamber().getOlm().douseFlameWall(player, object);
					}
				}, true));
			} else {
				player.setRouteEvent(null);
				if(player.getRegionId() == 10841) {
					EvilSanta.douseFire(player, object);
				}
			}
		}
		if (PartyBalloon.forId(id) != null) {
			player.setRouteEvent(new RouteEvent(object, new Runnable() {
				@Override
				public void run() {
					PartyBalloon.pop(player, object);
				}
			}, true));
			return;
		}
		if (object.getId() == 132738 || object.getId() == 6912 || (object.getId() >= HouseConstants.HObject.WOOD_BENCH.getId()
				&& object.getId() <= HouseConstants.HObject.GILDED_BENCH.getId())) {
			player.setRouteEvent(new RouteEvent(object, new Runnable() {
				@Override
				public void run() {
					player.getControlerManager().processObjectClick1(object);
				}
			}, true));
			return;
		}
		if (id == 128893 || id == 127054 || id == 110663 || id == 43529 || id == 69514 || (id >= 4550 && id <= 4559)
				|| id == 131809 || id == 134515 || id == 104909) {
			player.setRouteEvent(new RouteEvent(object, new Runnable() {
				@Override
				public void run() {
					// unreachable agility objects exception
					player.faceObject(object);
					if(id == 104909)
						handle(player, object, 1);
					else if (id == 127054 && x == 3040 && y == 4802) // nexus
						player.useStairs(-1, new WorldTile(3040, 4797, 0), 0, 1);
					else if (id == 134515 && object.getRotation() == 0) {
						WorldTile teleTo = player.transform(0, player.getY() > object.getY() ? -5 : 5, 0);
						player.setNextForceMovement(new NewForceMovement(player, 1, teleTo, 2,
								Utils.getAngle(teleTo.getX() - player.getX(), teleTo.getY() - player.getY())));
						player.useStairs(-1, teleTo, 1, 2);
						player.setNextAnimation(new Animation(769));
						return;
					} else if (id == 43529) {
						GnomeAgility.preSwing(player, object);
					} else if (id == 69514) {
						GnomeAgility.runGnomeBoard(player, object);
					} else if (id == 128893 || id >= 4550 && id <= 4559) {
						if (!Agility.hasLevel(player, id == 128893 ? 34 : 35))
							return;
						if (object.withinDistance(player, 2)) {
							if (!Agility.hasLevel(player, 35))
								return;
							player.setNextForceMovement(new NewForceMovement(player, 1, object, 2,
									Utils.getAngle(object.getX() - player.getX(), object.getY() - player.getY())));
							player.useStairs(-1, object, 1, 2);
							player.setNextAnimation(new Animation(769));
							// player.getSkills().addXp(Skills.AGILITY, 2); could be spammed. no1 really
							// does lighthouse rocks 4 xp
						}
						return;
					} else if (object.getId() == 110663) {
						if (!Agility.hasLevel(player, 76))
							return;
						final boolean isEast = player.getX() > 2159;
						player.lock(5);
						player.getPackets().sendGameMessage("You leap across with a mighty leap!");
						WorldTasksManager.schedule(new WorldTask() {
							int ticks = 0;

							@Override
							public void run() {
								ticks++;
								int direction = !isEast ? NewForceMovement.EAST : NewForceMovement.WEST;
								WorldTile tile = new WorldTile(isEast ? 2154 : 2160, 3072, 0);
								if (ticks == 1) {
									player.setNextForceMovement(new ForceMovement(player, 1, object, 2, direction));
									player.setNextAnimation(new Animation(769));
								} else if (ticks == 2) {
									player.useStairs(769, object, 0, 1);
								} else if (ticks == 3) {
									player.setNextAnimation(new Animation(769));
									player.setNextForceMovement(new ForceMovement(player, 1, tile, 2, direction));
								} else if (ticks == 4) {
									player.useStairs(-1, tile, 0, 1);
									stop();
									return;
								}
							}
						}, 0, 1);
						return;
					} else if (object.getId() == 131809) {
						if (!Agility.hasLevel(player, 30))
							return;
						final boolean isEast = player.getY() > object.getY();
						player.lock(5);
						player.getPackets().sendGameMessage("You leap across with a mighty leap!");
						WorldTasksManager.schedule(new WorldTask() {
							int ticks = 0;

							@Override
							public void run() {
								ticks++;
								int direction = !isEast ? NewForceMovement.NORTH : NewForceMovement.SOUTH;
								WorldTile tile = new WorldTile(object.getX(), !isEast ? 8998 : 8994, 1);
								if (ticks == 1) {
									player.setNextForceMovement(new ForceMovement(player, 1, object, 2, direction));
									player.setNextAnimation(new Animation(769));
								} else if (ticks == 2) {
									player.useStairs(769, object, 0, 1);
								} else if (ticks == 3) {
									player.setNextAnimation(new Animation(769));
									player.setNextForceMovement(new ForceMovement(player, 1, tile, 2, direction));
								} else if (ticks == 4) {
									player.useStairs(-1, tile, 0, 1);
									stop();
									return;
								}
							}
						}, 0, 1);
						return;
					}
				}
			}, true));
			return;
		}
		if ((object.getId() == 10089 || object.getId() == 10088 || object.getId() == 10087)) {
			player.getPackets().sendGameMessage("Something seems to have scared all the fishes away...");
			return;
		}
		if (SiphionActionNodes.siphion(player, object))
			return;
		else if (id == 75463) {
			player.getControlerManager().processObjectClick1(object);
			return;
		} else if (object.getId() == 5949) {
			final boolean isSouth = player.getY() > 9553;
			player.getPackets().sendGameMessage("You leap across with a mighty leap!");
			WorldTasksManager.schedule(new WorldTask() {
				int ticks = 0;

				@Override
				public void run() {
					ticks++;
					int direction = isSouth ? NewForceMovement.SOUTH : NewForceMovement.NORTH;
					WorldTile tile = new WorldTile(3221, isSouth ? 9552 : 9556, 0);
					if (ticks == 1) {
						player.setNextForceMovement(new ForceMovement(player, 1, object, 2, direction));
						player.setNextAnimation(new Animation(769));
					} else if (ticks == 2) {
						player.useStairs(769, object, 0, 1);
					} else if (ticks == 3) {
						player.setNextAnimation(new Animation(769));
						player.setNextForceMovement(new ForceMovement(player, 1, tile, 2, direction));
					} else if (ticks == 4) {
						player.useStairs(-1, tile, 0, 1);
						stop();
						return;
					}
				}
			}, 0, 1);
			return;
		}

		player.setRouteEvent(new RouteEvent(object, new Runnable() {
			@Override
			public void run() {
				player.stopAll();
				if (object.getType() != 0)
					player.faceObject(object);
				if (!player.getControlerManager().processObjectClick1(object))
					return;
				if (player.getTreasureTrailsManager().useObject(object))
					return;
				if (handleOption1_part2(player, object))
					return;
				else if (id == 132659)
					player.useStairs(-1, new WorldTile(3649, 3219, 0), 0, 1);
				else if (id == 132660)
					player.useStairs(-1, new WorldTile(3631, 3219, 0), 0, 1);
				else if (id == 54019 || id == 54020 || id == 43797 || id == 107127)
					player.getDialogueManager().startDialogue("HiscoresD");
				else if (id == 99999)
					player.getDialogueManager().startDialogue("SacrificeAltarD");
				else if (id == 132653)
					TheatreOfBlood.enter(player);
				else if (id == 132987)
					BossTimerScore.show(player);
				else if (id == 25161) {
					player.getQuestManager().setQuestStage(Quests.DRAGON_SLAYER, 1);
					player.useStairs(-1, new WorldTile(object.getX() + (player.getX() < object.getX() ? 1 : -1), player.getY(), 0), 0, 1);
				} else if (id == 16960 && x == 2213 && y == 3795) //pirte cove
					player.useStairs(828, new WorldTile(2213, 3796, 1), 1, 2);
				else if (id == 16962 && x == 2213 && y == 3795) 
					player.useStairs(827, new WorldTile(2213, 3794, 0), 1, 2);
				else if (id == 16960 && x == 2212 && y == 3809) //pirte cove
					player.useStairs(828, new WorldTile(2213, 3809, 1), 1, 2);
				else if (id == 16962 && x == 2212 && y == 3809) 
					player.useStairs(827, new WorldTile(2211, 3809, 0), 1, 2);
				else if (id == 16959 && x == 2214 && y == 3801) //pirte cove
					player.useStairs(828, new WorldTile(2214, 3800, 2), 1, 2);
				else if (id == 16961 && x == 2214 && y == 3801) 
					player.useStairs(827, new WorldTile(2214, 3802, 1), 1, 2);
				else if (id == 69505)
					player.useStairs(-1, new WorldTile(object.getX()+1, object.getY(), 1), 0, 1);
				else if (id == 130381 && x == 1305 && y == 9974) //lizardshaman cave
					player.useStairs(-1, new WorldTile(1309, 3574, 0), 0, 1);
				else if (id == 130380 && x == 1306 && y == 3573) //lizardshaman cave
					player.useStairs(-1, new WorldTile(1305, 9973, 0), 0, 1);
				else if (id == 130384 && x == 1320 && y == 9966) //lizardshaman cave
					player.useStairs(-1, new WorldTile(1323, 9966, 0), 0, 1);
				else if (id == 130385 && x == 1322 && y == 9966) //lizardshaman cave
					player.useStairs(-1, new WorldTile(1319, 9966, 0), 0, 1);
				else if (id == 130384 && x == 1296 && y == 9959) //lizardshaman cave
					player.useStairs(-1, new WorldTile(1299, 9959, 0), 0, 1);
				else if (id == 130385 && x == 1298 && y == 9959) //lizardshaman cave
					player.useStairs(-1, new WorldTile(1295, 9959, 0), 0, 1);
				else if (id == 130383 && x == 1318 && y == 9959) //lizardshaman cave
					player.useStairs(-1, new WorldTile(1318, 9956, 0), 0, 1);
				else if (id == 130382 && x == 1318 && y == 9957) //lizardshaman cave
					player.useStairs(-1, new WorldTile(1318, 9960, 0), 0, 1);
				else if (id == 130383 && x == 1305 && y == 9956) //lizardshaman cave
					player.useStairs(-1, new WorldTile(1305, 9953, 0), 0, 1);
				else if (id == 130382 && x == 1305 && y == 9954) //lizardshaman cave
					player.useStairs(-1, new WorldTile(1305, 9957, 0), 0, 1);
				else if (id == 127362 && x == 1471 && y == 3687) //lizardshaman hill
					player.useStairs(-1, new WorldTile(1476, 3687, 0), 0, 1);
				else if (id == 127362 && x == 1475 && y == 3687) //lizardshaman hill
					player.useStairs(-1, new WorldTile(1470, 3687, 0), 0, 1);
				else if (id == 127362 && x == 1459 && y == 3690) //lizardshaman hill
					player.useStairs(-1, new WorldTile(1454, 3690, 0), 0, 1);
				else if (id == 127362 && x == 1455 && y == 3690) //lizardshaman hill
					player.useStairs(-1, new WorldTile(1460, 3690, 0), 0, 1);
				else if (id == 131842 && x == 3682 && y == 3716)
					player.useStairs(4853, new WorldTile(3682, player.getY() > object.getY() ? 3715 : 3717,0), 1, 2);
				else if (object.getId() == 57033)
					LavaFlowMine.sendGuageDialogue(player, object);
				else if (id == 57187)
					LavaFlowMine.fixBoiler(player);
				else if (object.getId() == 56989 && x == 2176 && y == 5663)
					player.useStairs(-1, new WorldTile(2939, 10198, 0), 0, 1);
				else if (object.getId() == 56990 && x == 2940 && y == 10196)
					player.useStairs(-1, new WorldTile(2177, 5664, 0), 0, 1);
				else if (id == 5008 && x == 2731 && y == 3712) //keldagrim
					player.useStairs(-1, new WorldTile(2773, 10162, 0), 0, 1);
				else if (id == 5014 && x == 2771 && y == 10161)
					player.useStairs(-1, new WorldTile(2730, 3713, 0), 0, 1);
				else if (id == 5012 && x == 2795 && y == 3717) //keldagrim
					player.useStairs(-1, new WorldTile(2799, 10134, 0), 0, 1);
				else if (id == 5013 && x == 2798 && y == 10135)
					player.useStairs(-1, new WorldTile(2796, 3719, 0), 0, 1);
				else if (id == 5973 && x == 2781 && y == 10161) //keldagrim
					player.useStairs(-1, new WorldTile(2838, 10124, 0), 0, 1);
				else if (id == 5998 && x == 2838 && y == 10123)
					player.useStairs(-1, new WorldTile(2780, 10161, 0), 0, 1);
				else if (id == 2452) {
					Runecrafting.enterAirAltar(player);
				} else if (id == 2455) {
					Runecrafting.enterEarthAltar(player);
				} else if (id == 2456) {
					Runecrafting.enterFireAltar(player);
				} else if (id == 2454) {
					Runecrafting.enterWaterAltar(player);
				} else if (id == 2457) {
					Runecrafting.enterBodyAltar(player);
				} else if (id == 2453) {
					Runecrafting.enterMindAltar(player);
				} else if (id == 2462) {
					Runecrafting.enterDeathAltar(player);
				} else if (id == 2461) {
					Runecrafting.enterChoasAltar(player);
				} else if (id == 2464) {
					Runecrafting.enterBloodAltar(player);
				} else if (id == 131607) {
					Runecrafting.enterWrathAltar(player);
				} else if (id == 2458) {
					Runecrafting.enterCosmicAltar(player);
				} else if (id == 2460) {
					Runecrafting.enterNatureAltar(player);
				} else if (id == 2459) {
					Runecrafting.enterLawAltar(player);
				} else if (id == 115615 || id == 28296 || id == 56907) {
					if(player.getInventory().getFreeSlots() == 0 && !player.getInventory().containsItem(10501, 1)) {
						player.sendMessage("Your inventory is full.");
						return;
					}
					player.lock(1);
					player.setNextAnimation(new Animation(833));
					WorldTasksManager.schedule(() -> player.getInventory().addItem(10501, 1));
				} else if (id == 110068)
					player.getDialogueManager().startDialogue("ZulrahEnter");
				 else if (id == 78331 || id == 133114) {
						/*String[] lines = new String[Upgrade.values().length+5];
						int count = 0;
						lines[count++] = "";
						lines[count++] = "Use an item on the upgrade chest in order to upgrade it!";
						lines[count++] = "If you success it will gain extra stats but if fails";
						lines[count++] = "your item will be destroyed. GOOD LUCK!";
						lines[count++] = "";
						for (Upgrade u : Upgrade.values()) {
							int perc = (int)(100d/(double)u.getChance() * (u.isProperRate() ? 1 : 1.5d));
							lines[count++] = "<col=66ff33>"+ItemConfig.forID(u.getFrom()).getName() +" -> <col=0033cc>"+ItemConfig.forID(u.getTo()).getName() +" ("+perc+"%)";
						}
						NPCKillLog.sendQuestTab(player, "<img=6><col=cc33ff>Upgrade Chest<img=6>", lines);*/
					UpgradeItemOption.openInterface(player, 0);
				// Hefin Agility
				} else if (id == 94050)
					Hefin.leap(player);
				else if (id == 94051)
					Hefin.traverse(player);
				else if (id == 77461) {
					player.getPackets().sendGameMessage("Looks like you need special gloves to make this jump.");
					return;
				} else if (id == 1804 && object.getX() == 3115 && object.getY() == 3449) {
					if (!player.getInventory().containsItem(983, 1)) {
						player.getPackets().sendGameMessage("This door is locked.");
						return;
					}
					player.addWalkSteps(x, y + (player.getY() >= 3450 ? 0 : 1), -1, false);
				} else if (id == 2350 && (object.getX() == 3352 && object.getY() == 3417 && object.getPlane() == 0))
					player.useStairs(832, new WorldTile(3177, 5731, 0), 1, 2);
				else if (id == 3998 && x == 2187 && y == 3169)
					player.useStairs(-1, new WorldTile(2189, 3162, 0), 0, 1);
				else if (id == 3999 && x == 2187 && y == 3163)
					player.useStairs(-1, new WorldTile(2188, 3171, 0), 0, 1);
				else if (id == 6088 && x == 2895 && y == 10209 && object.getPlane() == 1)
					player.useStairs(-1, new WorldTile(2893, 10210, 0), 0, 1);
				else if (id == 2114)
					player.getCoalTrucksManager().removeCoal();
				else if (id == 38698) { // Safe FFA portal.
					player.setNextWorldTile(new WorldTile(2815, 5511, 0));
					player.getControlerManager().startControler("clan_wars_ffa", false);
				} else if (id == 48496)
					player.getDungManager().enterDungeon(true);
				else if (id == 78322)
					player.getInventory().addItem(25430, 1);
				else if (id == 78323)
					player.getInventory().addItem(25431, 5);
				else if (id == 65458 && x == 3231 && y == 3950)
					player.useStairs(-1, new WorldTile(3232, 10351, 0), 0, 1);
				else if (id == 126763 && x == 3232 && y == 10352)
					player.useStairs(-1, new WorldTile(3233, 3949, 0), 0, 1);
				else if (id == 65459 && x == 3241 && y == 3948)
					player.useStairs(-1, new WorldTile(3243, 10351, 0), 0, 1);
				else if (id == 126763 && x == 3233 && y == 10331)
					player.useStairs(-1, new WorldTile(3233, 3939, 0), 0, 1);
				else if (id == 65458 && x == 3231 && y == 3935)
					player.useStairs(-1, new WorldTile(3233, 10332, 0), 0, 1);
				else if (id == 126763 && x == 3243 && y == 10352)
					player.useStairs(-1, new WorldTile(3242, 3947, 0), 0, 1);
				else if (id == 32270 && x == 2603 && y == 9478)
					player.useStairs(-1, new WorldTile(2606, 3079, 0), 0, 1);
				else if (id == 32271 && x == 2603 && y == 3078)
					player.useStairs(-1, new WorldTile(2602, 9479, 0), 0, 1);
				else if (id == 45060)
					player.useStairs(844, new WorldTile(1520, 4704, 0), 0, 1);
				else if (id == 45008)
					player.useStairs(844, new WorldTile(2817, 10155, 0), 0, 1);
				else if (id == 128687 && x == 3793 && y == 5391) {
					player.useStairs(828, new WorldTile(2026, 5611, 0), 1, 2);
				} else if (id == 134359 && x == 1311 && y == 3807) {
					player.lock();
					FadingScreen.fade(player, 0, new Runnable() {
						@Override
						public void run() {
							player.useStairs(-1, new WorldTile(1312, 10188, 0), 0, 1);
						}
					});
				} else if (id == 134530 && x == 1330 && y == 10205) {
					player.useStairs(-1, new WorldTile(1334, 10205, 1), 0, 1);
					player.getMusicsManager().playOSRSMusic("Ful to the Brim");
				} else if (id == 134531 && x == 1330 && y == 10205) {
					player.useStairs(-1, new WorldTile(1329, 10206, 0), 0, 1);
					player.getMusicsManager().playOSRSMusic("Way of the Wyrm");
				} else if (id == 134530 && x == 1314 && y == 10188) {
					player.useStairs(-1, new WorldTile(1318, 10188, 2), 0, 1);
					player.getMusicsManager().playOSRSMusic("Kanon of Kahlith");
				} else if (id == 134531 && x == 1314 && y == 10188) {
					player.useStairs(-1, new WorldTile(1313, 10189, 1), 0, 1);
					player.getMusicsManager().playOSRSMusic("Ful to the Brim");
				} else if (id == 134514 && x == 1311 && y == 10185) {
					player.useStairs(-1, new WorldTile(1311, 3807, 0), 0, 1);
				} else if (id == 134516 && object.getRotation() == 0) {
					player.useStairs(-1, new WorldTile(player.getX() + (player.getX() < object.getX() ? 7 : -7), player.getY(), player.getPlane()), 0, 1);
				} else if (id == 134544) {
					player.useStairs(4853,
							new WorldTile(object.getX(), object.getY(), 0),
							2, 3);
 				} else if (id == 128686 && x == 2026 && y == 5612) {
					player.lock();
					FadingScreen.fade(player, 0, new Runnable() {
						@Override
						public void run() {
							player.getPackets().sendGameMessage("You enter the cavern beneath the crash site.");
							player.useStairs(-1, new WorldTile(3792, 5391, 0), 0, 1);
						}
					});
				} else if (id == 129916 && x == 3658 && y == 3848) {
					player.lock();
					FadingScreen.fade(player, 0, new Runnable() {
						@Override
						public void run() {
							player.getPackets().sendGameMessage("You board the boat and travel to Lithkren.");
							player.useStairs(-1, new WorldTile(3583, 3973, 0), 0, 1);
						}
					});
				} else if (id == 132079 && x == 3582 && y == 3971) {
					player.lock();
					FadingScreen.fade(player, 0, new Runnable() {
						@Override
						public void run() {
							player.getPackets().sendGameMessage("You board the boat and travel to the Fossil Island.");
							player.useStairs(-1, new WorldTile(3660, 3849, 0), 0, 1);
						}
					});
				} else if (id == 132081 && x == 3558 && y == 4003)
					player.useStairs(-1, new WorldTile(3556, 4004, 1), 0, 1);
				else if (id == 132082 && x == 3557 && y == 4003)
					player.useStairs(-1, new WorldTile(3561, 4004, 0), 0, 1);
				else if (id == 132084)
					player.useStairs(827, new WorldTile(player.getX(), player.getY(), 0), 1, 2);
				else if (id == 132080 && x == 3554 && y == 4003)
					player.useStairs(-1, new WorldTile(3549, 10448, 0), 0, 1);
				else if (id == 132112 && x == 3549 && y == 10444)
					player.useStairs(-1, new WorldTile(3554, 4002, 0), 0, 1);
				else if (id == 132113 && x == 3549 && y == 10469)
					player.useStairs(-1, new WorldTile(object.getX(), player.getY() < object.getY() ? 10473 : 10468, 0),0, 1);
				else if (id == 132117)
					player.useStairs(-1, new WorldTile(1567, 5061, 0), 0, 1);
				else if (id == 132132)
					player.useStairs(-1, new WorldTile(3549, 10481, 0), 0, 1);
				else if (id == 132153)
					player.addWalkSteps(object.getX(), object.getY(), 1, false);
				else if (id == 61321) {
					ShopsHandler.openShop(player, 247);
				} else if (id == 30205 || id == 110061) {
					player.getDialogueManager().startDialogue("ViewGETransactions");
				} else if (id == 128807 && x == 1987 && y == 5568)
					player.useStairs(-1, new WorldTile(2436, 3519, 0), 0, 1);
				else if (id == 128807 && x == 2436 && y == 3520)
					player.useStairs(-1, new WorldTile(1987, 5568, 0), 0, 1);
				// ancient cavern
				else if (id == 25336 && x == 1770 && y == 5365)
					player.useStairs(-1, new WorldTile(1768, 5366, 1), 0, 1);
				else if (id == 25338 && x == 1769 && y == 5365)
					player.useStairs(-1, new WorldTile(1772, 5366, 0), 0, 1);
				else if (id == 25339 && x == 1778 && y == 5344)
					player.useStairs(-1, new WorldTile(1778, 5343, 1), 0, 1);
				else if (id == 25340 && x == 1778 && y == 5344)
					player.useStairs(-1, new WorldTile(1778, 5346, 0), 0, 1);
				else if (id == 25337 && x == 1744 && y == 5323)
					player.useStairs(-1, new WorldTile(1744, 5321, 1), 0, 1);
				else if (id == 39468 && x == 1744 && y == 5322)
					player.useStairs(-1, new WorldTile(1744, 5325, 0), 0, 1);
				else if (id == 25341)
					player.useStairs(-1, new WorldTile(1823, 5273, 0), 0, 1);
				else if (id == 40208)
					player.useStairs(-1, new WorldTile(1759, 5342, 1), 0, 1);
				else if (id == 69505 && x == 2445 && y == 3433)
					player.useStairs(-1, new WorldTile(2445, 3433, 1), 0, 1);
				else if (id == 36772)
					player.useStairs(827, new WorldTile(3207, 3224, 2), 0, 1);
				else if (id == 36771)
					player.useStairs(828, new WorldTile(3207, 3222, 3), 0, 1);
				else if (id == 55402)
					player.useStairs(-1, new WorldTile(2538, 3100, 1), 0, 1);
				 else if (id == 41900 && x == 2293 && y == 3627) //phoenix
						player.useStairs(-1, new WorldTile(3535, 5186, 0), 0, 1);
				else if (id == 1723 && x == 2537 && y == 3097)
					player.useStairs(-1, new WorldTile(2537, 3095, 0), 0, 1);
				else if (id == 36000 && x == 2433 && y == 3313) // underground pass
					player.useStairs(-1, new WorldTile(2312, 3216, 0), 0, 1);
				else if (id == 4006 && x == 2313 && y == 3215)
					player.useStairs(-1, new WorldTile(2438, 3315, 0), 0, 1);
				else if (id == 126566 && x == 1310 && y == 1236) // cerberus lair
					player.useStairs(-1, new WorldTile(2871, 9849, 0), 0, 1);
				else if (id == 2 && x == 2872 && y == 9848)
					player.useStairs(-1, new WorldTile(1310, 1237, 0), 0, 1);
				else if (id == 123104 && x == 1291 && y == 1254)
					player.useStairs(-1, new WorldTile(1240, 1226, 0), 0, 1);
				else if (id == 121772 && x == 1239 && y == 1225)
					player.useStairs(-1, new WorldTile(1291, 1253, 0), 0, 1);
				else if (id == 123104 && x == 1307 && y == 1269)
					player.useStairs(-1, new WorldTile(1304, 1290, 0), 0, 1);
				else if (id == 121772 && x == 1303 && y == 1289)
					player.useStairs(-1, new WorldTile(1309, 1269, 0), 0, 1);
				else if (id == 123104 && x == 1328 && y == 1254)
					player.useStairs(-1, new WorldTile(1368, 1226, 0), 0, 1);
				else if (id == 121772 && x == 1367 && y == 1225)
					player.useStairs(-1, new WorldTile(1329, 1253, 0), 0, 1);
				else if (id == 8783 && x == 2542 && y == 3327)
					player.useStairs(-1, new WorldTile(2044, 4649, 0), 0, 1);
				else if (id == 8785 && x == 2044 && y == 4650)
					player.useStairs(-1, new WorldTile(2542, 3326, 0), 0, 1);
				// inferno
				else if (id == 68110 && x == 4570 && y == 5249)
					player.useStairs(-1, new WorldTile(4739, 5067, 0), 0, 1);
				else if (id == 68841 && x == 4738 && y == 5065)
					player.useStairs(-1, new WorldTile(4570, 5252, 0), 0, 1);
				// varrock palace stairs
				else if (id == 24367 && x == 3212 && y == 3473)
					player.useStairs(-1, new WorldTile(3212, 3476, 1), 0, 1);
				else if (id == 24359 && x == 3212 && y == 3474)
					player.useStairs(-1, new WorldTile(3212, 3472, 0), 0, 1);
				else if (id == 2 && x == 2411 && y == 3056) {// smoke devil dung
					player.useStairs(-1, new WorldTile(2404, 9415, 0), 0, 1);
					player.getControlerManager().startControler("UnderGroundDungeon", true, false);
				} else if (id == 100534 && x == 2404 && y == 9414)
					player.useStairs(-1, new WorldTile(2412, 3055, 0), 0, 1);
				else if (id == 100535 && x == 2378 && y == 9452)
					player.useStairs(-1, new WorldTile(2376, 9452, 0), 0, 1);
				else if (id == 100536 && x == 2377 && y == 9452)
					player.useStairs(-1, new WorldTile(2379, 9452, 0), 0, 1);
				//wvrym caves
				else if (id == 130869 && x == 3745 && y == 3777)
					player.useStairs(-1, new WorldTile(3604, 10231, 0), 0, 1);
				else if (id == 130878 && x == 3603 && y == 10232)
					player.useStairs(-1, new WorldTile(3746, 3779, 0), 0, 1);
				else if (id == 130849 && x == 3633 && y == 10261)
					player.useStairs(-1, new WorldTile(3633, 10264, 0), 0, 1);
				else if (id == 130847 && x == 3633 && y == 10262)
					player.useStairs(-1, new WorldTile(3633, 10260, 0), 0, 1);
				else if (id == 131485 && x == 3604 && y == 10290)
					player.useStairs(-1, new WorldTile(object.getX() >= player.getX() ? 3607 : 3603, 10291, 0), 0, 1);
				else if (id == 130844 && x == 3595 && y == 10292)
					player.useStairs(828, new WorldTile(3680, 3854, 0), 1, 2);
				else if (id == 130842 && x == 3677 && y == 3853)
					player.useStairs(827, new WorldTile(3596, 10291, 0), 1, 2);
				// kraken cove
				else if (id == 100537 && x == 2280 && y == 10017)
					BossInstanceHandler.enterInstance(player, Boss.Kraken);
				//	player.useStairs(-1, new WorldTile(2280, 10022, 0), 0, 1);
				else if (id == 100538 && x == 2280 && y == 10021)
					player.useStairs(-1, new WorldTile(2280, 10016, 0), 0, 1);
				else if (id == 130178 && x == 2276 && y == 9987)
					player.useStairs(-1, new WorldTile(2278, 3611, 0), 0, 1);
				else if (id == 2 && x == 2274 && y == 3610)
					player.useStairs(-1, new WorldTile(2276, 9988, 0), 0, 1);
				// nexus
				else if (id == 127055 && x == 3040 && y == 4798)
					player.useStairs(-1, new WorldTile(3040, 4805, 0), 0, 1);
				else if (id == 21306 && x == 2317 && y == 3824) {
					if (!Agility.hasLevel(player, 40))
						return;
					player.lock(5);
					player.addWalkSteps(2317, 3832, -1, false);
				} else if (id == 68953 && x == 2490 && y == 3515) {
					if (!Agility.hasLevel(player, 37))
						return;
					player.useStairs(-1, new WorldTile(2490, 3521, 0), 0, 1);
				} else if (id == 68954 && x == 2490 && y == 3520) {
					if (!Agility.hasLevel(player, 37))
						return;
					player.useStairs(-1, new WorldTile(2489, 3515, 0), 0, 1);
				} else if (id == 21307 && x == 2317 && y == 3831) {
					if (!Agility.hasLevel(player, 40))
						return;
					player.lock(5);
					player.addWalkSteps(2317, 3823, -1, false);
				} else if (id == 21308) {
					player.lock(5);
					player.addWalkSteps(2343, 3829, -1, false);
				} else if (id == 21309) {
					player.lock(5);
					player.addWalkSteps(2343, 3820, -1, false);
				}else if (id == 129150) 
						player.getDialogueManager().startDialogue("OccultAltar");
				else if (id == 68122) {
					player.getDialogueManager().startDialogue("InfernoEnter");
				} else if (id == 67966) {
					player.lock();
					player.setNextAnimation(new Animation(6723));
					player.setNextForceMovement(new NewForceMovement(new WorldTile(player), 4,
							new WorldTile(2512, 3509, 0), 7, Utils.getAngle(0, -1)));
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							FadingScreen.fade(player, 0, new Runnable() {

								@Override
								public void run() {
									player.getPackets().sendGameMessage(
											"You are swept, out of control, thought horrific underwater currents.");
									player.getPackets().sendGameMessage(
											"You are swirled beneath the water, dashed agaisnt sharp rocks.");
									player.getPackets().sendGameMessage(
											"Mystical forces guide you into a cavern below the whirpool.");
									player.setNextWorldTile(new WorldTile(1763, 5365, 1));
									player.lock(1);
								}

							});
						}
					}, 4);
				} else if (id == 25216) {
					player.lock();
					FadingScreen.fade(player, 0, new Runnable() {
						@Override
						public void run() {
							player.getPackets().sendGameMessage(
									"You are swept, out of control, thought horrific underwater currents.");
							player.getPackets()
									.sendGameMessage("You are swirled beneath the water, dashed agaisnt sharp rocks.");
							player.getPackets().sendGameMessage(
									"You find yourself on the banks of the river, far below the lake.");
							player.setNextWorldTile(new WorldTile(2531, 3446, 0));
							player.lock(1);
						}
					});
					// Kuradal dungeon
				} else if (id == 47232)
					KuradalDungeon.enter(player);
				// Waterbirth island dungeon
				else if (id >= 8958 && id <= 8960) {
					List<Integer> pIndex = World.getRegion(object.getRegionId()).getPlayerIndexes();
					if (pIndex != null) { // switched to reqire only 1 person
						for (Integer i : pIndex) {
							Player p = World.getPlayers().get(i);
							if (p == null || /* p == player || */ !Utils.isOnRange(p.getX(), p.getY(), p.getSize(),
									object.getX(), object.getY(), 3, 0))
								continue;
							player.lock(1);
							World.removeObjectTemporary(object, 60000);
							return;
						}
					}
					player.getPackets().sendGameMessage("You cannot see a way to open this door...");
				} else if (id == 10177 && x == 2546 && y == 10143)
					player.getDialogueManager().startDialogue("ClimbEmoteStairs", new WorldTile(2544, 3741, 0),
							new WorldTile(1798, 4407, 3), "Go up the stairs.", "Go down the stairs.", 828);
				else if ((id == 10193 && x == 1798 && y == 4406) || (id == 8930 && x == 2542 && y == 3740))
					player.useStairs(-1, new WorldTile(2545, 10143, 0), 0, 1);
				else if (id == 10195 && x == 1808 && y == 4405)
					player.useStairs(-1, new WorldTile(1810, 4405, 2), 0, 1);
				else if (id == 10196 && x == 1809 && y == 4405)
					player.useStairs(-1, new WorldTile(1807, 4405, 3), 0, 1);
				else if (id == 10198 && x == 1823 && y == 4404)
					player.useStairs(-1, new WorldTile(1825, 4404, 3), 0, 1);
				else if (id == 10197 && x == 1824 && y == 4404)
					player.useStairs(-1, new WorldTile(1823, 4404, 2), 0, 1);
				else if (id == 10199 && x == 1834 && y == 4389)
					player.useStairs(-1, new WorldTile(1834, 4388, 2), 0, 1);
				else if (id == 10200 && x == 1834 && y == 4388)
					player.useStairs(-1, new WorldTile(1834, 4390, 3), 0, 1);
				else if (id == 10201 && x == 1811 && y == 4394)
					player.useStairs(-1, new WorldTile(1810, 4394, 1), 0, 1);
				else if (id == 10202 && x == 1810 && y == 4394)
					player.useStairs(-1, new WorldTile(1812, 4394, 2), 0, 1);
				else if (id == 10203 && x == 1799 && y == 4388)
					player.useStairs(-1, new WorldTile(1799, 4386, 2), 0, 1);
				else if (id == 10204 && x == 1799 && y == 4387)
					player.useStairs(-1, new WorldTile(1799, 4389, 1), 0, 1);
				else if (id == 10205 && x == 1797 && y == 4382)
					player.useStairs(-1, new WorldTile(1797, 4382, 1), 0, 1);
				else if (id == 10206 && x == 1798 && y == 4382)
					player.useStairs(-1, new WorldTile(1796, 4382, 2), 0, 1);
				else if (id == 10207 && x == 1802 && y == 4369)
					player.useStairs(-1, new WorldTile(1800, 4369, 2), 0, 1);
				else if (id == 10208 && x == 1801 && y == 4369)
					player.useStairs(-1, new WorldTile(1802, 4369, 1), 0, 1);
				else if (id == 10209 && x == 1826 && y == 4362)
					player.useStairs(-1, new WorldTile(1828, 4362, 1), 0, 1);
				else if (id == 10210 && x == 1827 && y == 4362)
					player.useStairs(-1, new WorldTile(1825, 4362, 2), 0, 1);
				else if (id == 10211 && x == 1863 && y == 4371)
					player.useStairs(-1, new WorldTile(1863, 4373, 2), 0, 1);
				else if (id == 10212 && x == 1863 && y == 4372)
					player.useStairs(-1, new WorldTile(1863, 4370, 1), 0, 1);
				else if (id == 10213 && x == 1864 && y == 4388)
					player.useStairs(-1, new WorldTile(1864, 4389, 1), 0, 1);
				else if (id == 10214 && x == 1864 && y == 4389)
					player.useStairs(-1, new WorldTile(1864, 4387, 2), 0, 1);
				else if (id == 10215 && x == 1890 && y == 4407)
					player.useStairs(-1, new WorldTile(1890, 4408, 0), 0, 1);
				else if (id == 10216 && x == 1890 && y == 4408)
					player.useStairs(-1, new WorldTile(1890, 4406, 1), 0, 1);
				else if (id == 10230 && x == 1911 && y == 4367)
					BossInstanceHandler.enterInstance(player, Boss.Dagannoth_Kings);
				//	player.useStairs(-1, new WorldTile(2900, 4449, 0), 0, 1);
				else if (id == 10229 && x == 2899 && y == 4449)
					player.useStairs(-1, new WorldTile(1912, 4367, 0), 0, 1);
				else if (id == 10217 && x == 1957 && y == 4371)
					player.useStairs(-1, new WorldTile(1957, 4373, 1), 0, 1);
				else if (id == 10218 && x == 1957 && y == 4372)
					player.useStairs(-1, new WorldTile(1957, 4370, 0), 0, 1);
				else if (id == 10226 && x == 1932 && y == 4378)
					player.useStairs(-1, new WorldTile(1932, 4380, 2), 0, 1);
				else if (id == 10225 && x == 1932 && y == 4379)
					player.useStairs(-1, new WorldTile(1932, 4377, 1), 0, 1);
				else if (id == 10228 && x == 1961 && y == 4391)
					player.useStairs(-1, new WorldTile(1961, 4393, 3), 0, 1);
				else if (id == 10227 && x == 1961 && y == 4392)
					player.useStairs(-1, new WorldTile(1961, 4392, 2), 0, 1);
				else if (id == 10194 && x == 1975 && y == 4408)
					player.useStairs(-1, new WorldTile(2501, 3636, 0), 0, 1);
				else if (id == 10219 && x == 1824 && y == 4381)
					player.useStairs(-1, new WorldTile(1824, 4379, 3), 0, 1);
				else if (id == 10220 && x == 1824 && y == 4380)
					player.useStairs(-1, new WorldTile(1824, 4382, 2), 0, 1);
				else if (id == 10221 && x == 1838 && y == 4376)
					player.useStairs(-1, new WorldTile(1838, 4374, 2), 0, 1);
				else if (id == 10222 && x == 1838 && y == 4375)
					player.useStairs(-1, new WorldTile(1838, 4377, 3), 0, 1);
				else if (id == 10223 && x == 1850 && y == 4386)
					player.useStairs(-1, new WorldTile(1850, 4385, 1), 0, 1);
				else if (id == 10224 && x == 1850 && y == 4385)
					player.useStairs(-1, new WorldTile(1850, 4387, 2), 0, 1);
				// White Wolf Mountain cut

				else if (id == 56 && x == 2876 && y == 9880)
					player.useStairs(-1, new WorldTile(2879, 3465, 0), 0, 1);
				else if (id == 66990 && x == 2876 && y == 3462)
					player.useStairs(-1, new WorldTile(2875, 9880, 0), 0, 1);
				else if (id == 54 && x == 2820 && y == 9883)
					player.useStairs(-1, new WorldTile(2820, 3486, 0), 0, 1);
				else if (id == 55 && x == 2820 && y == 3484)
					player.useStairs(-1, new WorldTile(2821, 9882, 0), 0, 1);
				// sabbot lair
				else if (id == 34395 && x == 2857 && y == 3578)
					player.useStairs(-1, new WorldTile(2893, 10074, 0), 0, 1);
				else if (id >= 2889 && id <= 2892) {
					boolean isNorth = player.getY() >= 2940;
					player.useStairs(-1, player.transform(0, isNorth ? -8 : 8, 0), 2, 3,
							"You slash your way through the marsh and get to the other side.");
				} else if (id == 32738 && x == 2892 && y == 10072)
					player.useStairs(-1, new WorldTile(2858, 3577, 0), 0, 1);
				else if (id == 34548 && x == 2610 && y == 3305)
					player.useStairs(-1, new WorldTile(2611, 3307, 1), 1, 2);
				else if (id == 34550 && x == 2610 && y == 3305)
					player.useStairs(-1, new WorldTile(2611, 3304, 0), 1, 2);
				else if (id == 1987)
					player.useStairs(-1, new WorldTile(2513, 3480, 0), 1, 2,
							"The raft is pulled down by the strong currents.");
				else if (id == 10283)
					player.getPackets().sendGameMessage("I don't think that's a very smart idea...");
				// varrock museum
				else if (id == 2562)
					player.getDialogueManager().startDialogue("CompletionistCape");
				else if (id == 21514)
					player.useStairs(828, new WorldTile(2329, 3802, 1), 1, 2);
				else if (id == 21515)
					player.useStairs(827, new WorldTile(2331, 3802, 0), 1, 2);
				else if (id == 21512)
					player.useStairs(828, new WorldTile(2364, 3799, 2), 1, 2);
				else if (id == 21513)
					player.useStairs(827, new WorldTile(2362, 3799, 0), 1, 2);
				else if (id == 24359 && x == 3253 && y == 3444)
					player.useStairs(-1, new WorldTile(3253, 3442, 1), 0, 1);
				else if (id == 24357 && x == 3253 && y == 3443)
					player.useStairs(-1, new WorldTile(3253, 3446, 2), 0, 1);
				else if (id == 24359 && x == 3266 && y == 3453)
					player.useStairs(-1, new WorldTile(3267, 3451, 0), 0, 1);
				else if (id == 71903 && x == 2965 && y == 3219)
					player.useStairs(-1, new WorldTile(2964, 3219, 0), 0, 1);
				else if (id == 71902 && x == 2965 && y == 3219)
					player.useStairs(-1, new WorldTile(2968, 3219, 1), 0, 1);
				else if (id == 24358 && x == 3266 && y == 3452)
					player.useStairs(-1, new WorldTile(3267, 3455, 1), 0, 1);
				else if (id == 5259 && x == 3659 && y == 3508) {
					player.lock(3);
					player.addWalkSteps(player.getX(), y + (player.getY() > y ? -2 : 1), 2, false);
				} else if (id == 5259 && x == 3652 && y == 3485) {
					player.lock(3);
					player.addWalkSteps(x + (player.getX() > x ? -2 : 1), player.getY(), 2, false);
				} else if (id == 22119) {
					player.addWalkSteps(x + (player.getX() >= 3805 ? -1 : 2), object.getY(), 2, false);
				} else if (id == 2788 || id == 2789) {
					boolean isEntering = player.getX() >= 2504;
					player.addWalkSteps(x + (isEntering ? -1 : 0), y, 1, false);
					handleGate(player, object, 600);
				} else if (id == 29099) {
					if (!Agility.hasLevel(player, 29))
						return;
					player.useStairs(1133, new WorldTile(2596, player.getY() <= 2869 ? 2871 : 2869, 0), 1, 2);
				} else if (id == 3944 || id == 3945)
					player.addWalkSteps(x, y + (player.getY() >= 3335 ? -1 : 1), -1, false);
				else if (id == 31149) {
					boolean isEntering = player.getX() <= 3295;
					player.useStairs(isEntering ? 9221 : 9220, new WorldTile(x + (isEntering ? 1 : 0), y, 0), 1, 2);
				} else if (id == 2333 || id == 2334 || id == 2335) {
					if (!Agility.hasLevel(player, 30))
						return;
					player.setNextAnimation(new Animation(741));
					player.setNextForceMovement(new NewForceMovement(object, 1, object, 2,
							Utils.getAngle(object.getX() - player.getX(), object.getY() - player.getY())));
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							player.setNextWorldTile(object);
						}
					});
				} else if (id == 56805) {
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							player.useStairs(3303,
									new WorldTile(player.getX(), player.getY()
											+ ((y == 2941 ? player.getY() >= 2942 : player.getY() >= 2917) ? -2 : 2),
											0),
									4, 5, null, true);
						}
					}, 1);
				}
				//stronghold of player safety
				else if (id == 29728 && x == 3076 && y == 3462)
					player.useStairs(-1, new WorldTile(3159, 4279, 3), 0, 1);
				else if (id == 29729 && x == 3159 && y == 4280)
					player.useStairs(828, new WorldTile(3076, 3461, 0), 1, 2);
				else if (id == 29671 && x == 3171 && y == 4272)
					player.useStairs(-1, new WorldTile(3174, 4273, 2), 0, 1);
				else if (id == 29672 && x == 3171 && y == 4273)
					player.useStairs(-1, new WorldTile(3171, 4271, 3), 0, 1);
				else if (id == 29624 && x == 3141 && y == 4272)
					player.useStairs(-1, new WorldTile(3143, 4270, 0), 0, 1);
				else if (id == 29624 && x == 3142 && y == 4270)
					player.useStairs(-1, new WorldTile(3142, 4272, 1), 0, 1);
				else if (id == 29624 && x == 3178 && y == 4266)
					player.useStairs(-1, new WorldTile(3177, 4269, 2), 0, 1);
				else if (id == 29624 && x == 3178 && y == 4269)
					player.useStairs(-1, new WorldTile(3177, 4266, 0), 0, 1);
				else if (id == 29623 && x == 3139 && y == 4230)
					player.useStairs(-1, new WorldTile(3082, 4229, 0), 0, 1);
				else if (id == 29602 && x == 3082 && y == 4228)
					player.useStairs(-1, new WorldTile(3140, 4230, 2), 0, 1);
				else if (id == 29589 && x == 3086 && y == 4244)
					player.useStairs(-1, new WorldTile(3083, 3452, 0), 0, 1);
				else if (id == 29592 && x == 3084 && y == 3452)
					player.useStairs(-1, new WorldTile(3086, 4247, 0), 0, 1);
				else if (id == 29603 && x == 3075 && y == 3456)
					player.useStairs(-1, new WorldTile(3086, 4247, 0), 0, 1);
				else if (id == 29734 && !player.containsItem(12629)) {
					player.getInventory().addItem(12629, 1);
					player.getPackets().sendGameMessage("You find a pair of gloves in the chest.");
				}
				// entrana dungeon
				else if (id == 2408 && x == 2820 && y == 3374)
					player.useStairs(828, new WorldTile(2829, 9773, 0), 1, 2);
				else if (id == 2407)
					player.useStairs(-1, new WorldTile(2453, 4476, 0), 0, 1);
				else if (id >= 65616 && id <= 65622)
					WildernessObelisk.activateObelisk(id, player);
				else if (id == 59463)
					//EconomyManager.openTPS(player); // player.getDialogueManager().startDialogue("TrainCommand");
					TeleportationInterface.openInterface(player);
				// polypore dungon
				else if (id == 64125) {
					int value = player.getVarsManager().getBitValue(10232);
					if (value == 7)
						return;
					player.lock(2);
					player.setNextAnimation(new Animation(15460));
					player.getInventory().addItem(22445, 1);
					if (value == 0) {
						WorldTasksManager.schedule(new WorldTask() {

							@Override
							public void run() {
								int value = player.getVarsManager().getBitValue(10232);
								player.getVarsManager().sendVarBit(10232, value - 1);
								if (value == 1)
									stop();
							}
						}, 9, 9);
					}
					player.getVarsManager().sendVarBit(10232, value + 1);
				} else if (id == 64360 && x == 4629 && y == 5453)
					PolyporeCreature.useStairs(player, new WorldTile(4629, 5451, 2), true);
				else if (id == 64361 && x == 4629 && y == 5452)
					PolyporeCreature.useStairs(player, new WorldTile(4629, 5454, 3), false);
				else if (id == 64359 && x == 4632 && y == 5443)
					PolyporeCreature.useStairs(player, new WorldTile(4632, 5443, 1), true);
				else if (id == 64361 && x == 4632 && y == 5442)
					PolyporeCreature.useStairs(player, new WorldTile(4632, 5444, 2), false);
				else if (id == 64359 && x == 4632 && y == 5409)
					PolyporeCreature.useStairs(player, new WorldTile(4632, 5409, 2), true);
				else if (id == 64361 && x == 4633 && y == 5409)
					PolyporeCreature.useStairs(player, new WorldTile(4631, 5409, 3), false);
				else if (id == 64359 && x == 4642 && y == 5389)
					PolyporeCreature.useStairs(player, new WorldTile(4642, 5389, 1), true);
				else if (id == 64361 && x == 4643 && y == 5389)
					PolyporeCreature.useStairs(player, new WorldTile(4641, 5389, 2), false);
				else if (id == 64359 && x == 4652 && y == 5388)
					PolyporeCreature.useStairs(player, new WorldTile(4652, 5388, 0), true);
				else if (id == 64362 && x == 4652 && y == 5387)
					PolyporeCreature.useStairs(player, new WorldTile(4652, 5389, 1), false);
				else if (id == 64359 && x == 4691 && y == 5469)
					PolyporeCreature.useStairs(player, new WorldTile(4691, 5469, 2), true);
				else if (id == 64361 && x == 4691 && y == 5468)
					PolyporeCreature.useStairs(player, new WorldTile(4691, 5470, 3), false);
				else if (id == 64359 && x == 4689 && y == 5479)
					PolyporeCreature.useStairs(player, new WorldTile(4689, 5479, 1), true);
				else if (id == 64361 && x == 4689 && y == 5480)
					PolyporeCreature.useStairs(player, new WorldTile(4689, 5478, 2), false);
				else if (id == 64359 && x == 4698 && y == 5459)
					PolyporeCreature.useStairs(player, new WorldTile(4698, 5459, 2), true);
				else if (id == 64361 && x == 4699 && y == 5459)
					PolyporeCreature.useStairs(player, new WorldTile(4697, 5459, 3), false);
				else if (id == 64359 && x == 4705 && y == 5460)
					PolyporeCreature.useStairs(player, new WorldTile(4704, 5461, 1), true);
				else if (id == 64361 && x == 4705 && y == 5461)
					PolyporeCreature.useStairs(player, new WorldTile(4705, 5459, 2), false);
				else if (id == 64359 && x == 4718 && y == 5467)
					PolyporeCreature.useStairs(player, new WorldTile(4718, 5467, 0), true);
				else if (id == 64361 && x == 4718 && y == 5466)
					PolyporeCreature.useStairs(player, new WorldTile(4718, 5468, 1), false);
				else if (id == 2331)
					player.getDialogueManager().startDialogue("RobustGlassD");
				else if (id == 12290 || id == 12272) {
					if (id == 12290)
						player.setFavorPoints(1 - player.getFavorPoints());
					player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.STRAIT_VINE));// start
																												// of
																												// jadinkos
				} else if (id == 12328) {
					player.useStairs(3527, new WorldTile(3012, 9275, 0), 5, 6);
					player.setNextForceMovement(new ForceMovement(player, 3, object, 2, ForceMovement.WEST));
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							player.setNextFaceWorldTile(new WorldTile(3012, 9274, 0));
							player.setNextAnimation(new Animation(11043));
							player.getControlerManager().startControler("JadinkoLair");
						}
					}, 4);
				} else if (id == 12277)
					player.getActionManager()
							.setAction(new Woodcutting(object, TreeDefinitions.STRAIT_VINE_COLLECTABLE));// start of
																											// jadinkos
				else if (id == 12291)
					player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.MUTATED_VINE));
				else if (id == 12274)
					player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.CURLY_VINE));
				else if (id == 12279)
					player.getActionManager()
							.setAction(new Woodcutting(object, TreeDefinitions.CURLY_VINE_COLLECTABLE));
				else if (id == 26684 || id == 26685 || id == 26686) // poison waste cave
					player.useStairs(-1, new WorldTile(1989, 4174, 0), 1, 2, "You enter the murky cave...");
				else if (id == 26571 || id == 26572 || id == 26573 || id == 26574)
					player.useStairs(-1, new WorldTile(2321, 3100, 0), 1, 2);
				else if (id == 26560 && x == 2015 && y == 4255)
					player.getDialogueManager().startDialogue("SimpleMessage",
							"The room beyond the door is covred in gas, it is probably dangerous to go in there.");
				else if (id == 28715 || id == 103641 || id == 26945)
					player.getDialogueManager().startDialogue("SimpleMessage",
							"<col=2F4F4F>Throw at least 1m coins into the well to activate 10% xp and 5% drop rate boost to everyone.<br><col=8B4513>Status: "+(World.isWishingWellActive() ? (Utils.longFormat(World.getWishingWellRemaining())+" Remaining") : "Inactive"));
				else if (id == 26519) {
					if (x == 1991 && y == 4175)
						player.useStairs(827, new WorldTile(1991, 4175, 0), 1, 2);
					else if (x == 1998 && y == 4218)
						player.useStairs(827, new WorldTile(1998, 4218, 0), 1, 2);
					else if (x == 2011 && y == 4218)
						player.useStairs(827, new WorldTile(2011, 4218, 0), 1, 2);
					else
						player.useStairs(827, new WorldTile(x - 1, y, 0), 1, 2);
				} else if (id == 26518) {
					if (x == 1991 && y == 4175)
						player.useStairs(828, new WorldTile(1991, 4176, 1), 1, 2);
					else if (x == 1998 && y == 4218)
						player.useStairs(828, new WorldTile(1998, 4219, 1), 1, 2);
					else if (x == 2011 && y == 4218)
						player.useStairs(828, new WorldTile(2011, 4219, 1), 1, 2);
					else
						player.useStairs(828, new WorldTile(x + 1, y, 1), 1, 2);
				} else if (id == 17270 || id == 17269)
					player.addWalkSteps(x, id == 17270 ? 9817 : 9814, -1, false);
				else if (id == 17239)
					player.getDialogueManager().startDialogue("SimpleMessage",
							"You look into the bowl and a strange feeling goes amonst your body.");
				// wizards guild
				else if (id == 1722 && x == 2590 && y == 3089)
					player.useStairs(-1, new WorldTile(2590, 3092, 1), 0, 1);
				else if (id == 1723 && x == 2590 && y == 3090)
					player.useStairs(-1, new WorldTile(2590, 3088, 0), 0, 1);
				else if (id == 1722 && x == 2590 && y == 3084)
					player.useStairs(-1, new WorldTile(2590, 3087, 2), 0, 1);
				else if (id == 1723 && x == 2590 && y == 3085)
					player.useStairs(-1, new WorldTile(2591, 3083, 1), 0, 1);
				else if (id == 2158)
					player.useStairs(-1, new WorldTile(3104, 3163, 2), 0, 1);
				else if (id == 2157)
					player.useStairs(-1, new WorldTile(2908, 3332, 2), 0, 1);
				else if (id == 2156)
					player.useStairs(-1, new WorldTile(2702, 3405, 3), 0, 1);
				else if (id == 1754 && x == 2594 && y == 3085)
					player.useStairs(827, new WorldTile(2594, 9486, 0), 1, 2);
				else if (id == 1757 && x == 2594 && y == 9485)
					player.useStairs(828, new WorldTile(2594, 3086, 0), 1, 2);
				else if (id == 65203) {
					if (x == 3118 && y == 3570)
						player.useStairs(827, new WorldTile(3249, 5491, 0), 1, 2);
					else if (x == 3058 && y == 3550)
						player.useStairs(827, new WorldTile(3184, 5471, 0), 1, 2);
					else if (x == 3129 && y == 3587)
						player.useStairs(827, new WorldTile(3235, 5560, 0), 1, 2);
					else if (x == 3176 && y == 3585)
						player.useStairs(827, new WorldTile(3290, 5539, 0), 1, 2);
					else if (x == 3164 && y == 3561)
						player.useStairs(827, new WorldTile(3292, 5479, 0), 1, 2);
				} else if (id == 28782) {
					if (x == 3183 && y == 5470)
						player.useStairs(828, new WorldTile(3057, 3551, 0), 1, 2);
					else if (x == 3248 && y == 5490)
						player.useStairs(828, new WorldTile(3119, 3570, 0), 1, 2);
					else if (x == 3234 && y == 5559)
						player.useStairs(828, new WorldTile(3130, 3586, 0), 1, 2);
					else if (x == 3291 && y == 5538)
						player.useStairs(828, new WorldTile(3177, 3585, 0), 1, 2);
					else if (x == 3292 && y == 5479)
						player.useStairs(828, new WorldTile(3165, 3562, 0), 1, 2);
					player.getControlerManager().startControler("Wilderness");
				} else if (id == 77745) {
					player.addWalkSteps(object.getX(), object.getY(), -1, false);
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							if (getRepeatedTele(player, 3285, 5474, 0, 3286, 5470, 0))
								return;
							else if (getRepeatedTele(player, 3302, 5469, 0, 3290, 5463, 0))
								return;
							else if (getRepeatedTele(player, 3280, 5460, 0, 3273, 5460, 0))
								return;
							else if (getRepeatedTele(player, 3299, 5450, 0, 3296, 5455, 0))
								return;
							else if (getRepeatedTele(player, 3283, 5448, 0, 3287, 5448, 0))
								return;
							else if (getRepeatedTele(player, 3260, 5491, 0, 3266, 5446, 0))
								return;
							else if (getRepeatedTele(player, 3239, 5498, 0, 3244, 5495, 0))
								return;
							else if (getRepeatedTele(player, 3238, 5507, 0, 3232, 5501, 0))
								return;
							else if (getRepeatedTele(player, 3222, 5488, 0, 3218, 5497, 0))
								return;
							else if (getRepeatedTele(player, 3222, 5474, 0, 3224, 5479, 0))
								return;
							else if (getRepeatedTele(player, 3215, 5475, 0, 3218, 5478, 0))
								return;
							else if (getRepeatedTele(player, 3210, 5477, 0, 3208, 5471, 0))
								return;
							else if (getRepeatedTele(player, 3212, 5452, 0, 3214, 5456, 0))
								return;
							else if (getRepeatedTele(player, 3235, 5457, 0, 3229, 5454, 0))
								return;
							else if (getRepeatedTele(player, 3204, 5445, 0, 3197, 5448, 0))
								return;
							else if (getRepeatedTele(player, 3191, 5495, 0, 3194, 5490, 0))
								return;
							else if (getRepeatedTele(player, 3185, 5478, 0, 3191, 5482, 0))
								return;
							else if (getRepeatedTele(player, 3186, 5472, 0, 3192, 5472, 0))
								return;
							else if (getRepeatedTele(player, 3189, 5444, 0, 3187, 5460, 0))
								return;
							else if (getRepeatedTele(player, 3178, 5460, 0, 3168, 5456, 0))
								return;
							else if (getRepeatedTele(player, 3171, 5478, 0, 3167, 5478, 0))
								return;
							else if (getRepeatedTele(player, 3171, 5473, 0, 3167, 5471, 0))
								return;
							else if (getRepeatedTele(player, 3142, 5489, 0, 3141, 5480, 0))
								return;
							else if (getRepeatedTele(player, 3142, 5462, 0, 3154, 5462, 0))
								return;
							else if (getRepeatedTele(player, 3155, 5449, 0, 3143, 5443, 0))
								return;
							else if (getRepeatedTele(player, 3303, 5477, 0, 3299, 5484, 0))
								return;
							else if (getRepeatedTele(player, 3318, 5481, 0, 3322, 5480, 0))
								return;
							else if (getRepeatedTele(player, 3307, 5496, 0, 3317, 5496, 0))
								return;
							else if (getRepeatedTele(player, 3265, 5491, 0, 3260, 5491, 0))
								return;
							else if (getRepeatedTele(player, 3297, 5510, 0, 3300, 5514, 0))
								return;
							else if (getRepeatedTele(player, 3325, 5518, 0, 3323, 5531, 0))
								return;
							else if (getRepeatedTele(player, 3321, 5554, 0, 3315, 5552, 0))
								return;
							else if (getRepeatedTele(player, 3291, 5555, 0, 3285, 5556, 0))
								return;
							else if (getRepeatedTele(player, 3285, 5508, 0, 3280, 5501, 0))
								return;
							else if (getRepeatedTele(player, 3285, 5527, 0, 3282, 5531, 0))
								return;
							else if (getRepeatedTele(player, 3289, 5532, 0, 3288, 5536, 0))
								return;
							else if (getRepeatedTele(player, 3266, 5552, 0, 3262, 5552, 0))
								return;
							else if (getRepeatedTele(player, 3268, 5534, 0, 3261, 5536, 0))
								return;
							else if (getRepeatedTele(player, 3248, 5547, 0, 3253, 5561, 0))
								return;
							else if (getRepeatedTele(player, 3256, 5561, 0, 3252, 5543, 0))
								return;
							else if (getRepeatedTele(player, 3244, 5526, 0, 3241, 5529, 0))
								return;
							else if (getRepeatedTele(player, 3230, 5547, 0, 3226, 5553, 0))
								return;
							else if (getRepeatedTele(player, 3206, 5553, 0, 3204, 5546, 0))
								return;
							else if (getRepeatedTele(player, 3211, 5533, 0, 3214, 5533, 0))
								return;
							else if (getRepeatedTele(player, 3208, 5527, 0, 3211, 5523, 0))
								return;
							else if (getRepeatedTele(player, 3201, 5531, 0, 3197, 5529, 0))
								return;
							else if (getRepeatedTele(player, 3202, 5516, 0, 3196, 5512, 0))
								return;
						}
					});
				} else if (id == 28779) {
					if (x == 3142 && y == 5545) {
						BorkController.enterBork(player);
						return;
					}
					player.addWalkSteps(object.getX(), object.getY(), -1, false);
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							if (getRepeatedTele(player, 3197, 5529, 0, 3201, 5531, 0))
								return;
							else if (getRepeatedTele(player, 3165, 5515, 0, 3173, 5530, 0))
								return;
							else if (getRepeatedTele(player, 3156, 5523, 0, 3152, 5520, 0))
								return;
							else if (getRepeatedTele(player, 3148, 5533, 0, 3153, 5537, 0))
								return;
							else if (getRepeatedTele(player, 3143, 5535, 0, 3147, 5541, 0))
								return;
							else if (getRepeatedTele(player, 3158, 5561, 0, 3162, 5557, 0))
								return;
							else if (getRepeatedTele(player, 3162, 5545, 0, 3166, 5553, 0))
								return;
							else if (getRepeatedTele(player, 3168, 5541, 0, 3171, 5542, 0))
								return;
							else if (getRepeatedTele(player, 3190, 5549, 0, 3190, 5554, 0))
								return;
							else if (getRepeatedTele(player, 3180, 5557, 0, 3174, 5558, 0))
								return;
							else if (getRepeatedTele(player, 3190, 5519, 0, 3190, 5515, 0))
								return;
							else if (getRepeatedTele(player, 3185, 5518, 0, 3181, 5517, 0))
								return;
							else if (getRepeatedTele(player, 3196, 5512, 0, 3202, 5516, 0))
								return;
						}
					});
				} else if (id == 1600 || id == 1601) {
					if (player.getSkills().getLevel(Skills.MAGIC) < 66) {
						player.getDialogueManager().startDialogue("SimpleNPCMessage", 13,
								"Sorry, but you need level 66 Magic to enter here.");
						return;
					}
					player.lock(1);
					player.addWalkSteps(player.getX() == x ? x - 1 : x, object.getY(), 1, false);
				}
				// heroes guild
				else if (id == 2624 || id == 2625) {
					if (!player.getQuestManager().completedQuest(Quests.HEROES_QUEST_2)) {
						player.getPackets()
								.sendGameMessage("Please come back after you have Heroes' Quest quest requiriments.");
						return;
					}
					player.lock(1);
					player.addWalkSteps(player.getX() == x ? x - 1 : x, object.getY(), 1, false);
				} else if (id == 67346 && x == 2908 && y == 3512)
					player.useStairs(-1, new WorldTile(2912, 3514, 2), 0, 1);
				else if (id == 67694 && x == 2911 && y == 3513)
					player.useStairs(-1, new WorldTile(2907, 3514, 1), 0, 1);
				else if (id == 67690 && x == 2905 && y == 3516)
					player.useStairs(-1, new WorldTile(2893, 9907, 0), 0, 1);
				else if (id == 67691 && x == 2892 && y == 9907)
					player.useStairs(-1, new WorldTile(2906, 3516, 0), 0, 1);
				else if (id == 66518 && x == 2897 && y == 3447)
					player.useStairs(-1, new WorldTile(3047, 4971, 0), 0, 1);
				else if (id == 7258 && x == 3047 && y == 4972)
					player.useStairs(-1, new WorldTile(2896, 3447, 0), 0, 1);
				else if (id == 15653) {
					if (World.isSpawnedObject(object) || !WarriorsGuild.canEnter(player))
						return;
					player.lock(2);
					WorldObject opened = new WorldObject(object.getId(), object.getType(), object.getRotation() - 1,
							object.getX(), object.getY(), object.getPlane());
					// TODO: properly fix
					// World.spawnObjectTemporary(opened, 600);
					player.addWalkSteps(object.getX() - 1, player.getY(), 2, false);
				}
				// tarns lair
				else if (id == 20573 && x == 3149 && y == 4663)
					player.getControlerManager().startControler("TerrorDogsTarnsLairController");
				else if (id == 4918 && x == 3445 && y == 3236)
					player.useStairs(4853, new WorldTile(player.getX() == 3446 ? 3444 : 3446, object.getY(), 0), 2, 3);
				else if (id == 12776)
					player.useStairs(4853, new WorldTile(player.getX() == 3474 ? 3473 : 3474, object.getY(), 0), 2, 3);
				else if (id == 17757 || id == 17760)
					player.useStairs(4853,
							new WorldTile(object.getX(), object.getY() == player.getY() ? 3243 : 3244, 0), 2, 3);
				else if (id == 20979)
					player.useStairs(-1, new WorldTile(3149, 4666, 0), 0, 1);
				else if (id == 4913 && x == 3440 && y == 3232)
					player.useStairs(-1, new WorldTile(3436, 9637, 0), 0, 1);
				else if (id == 4920 && x == 3437 && y == 9637)
					player.useStairs(-1, new WorldTile(3441, 3232, 0), 0, 1);
				else if (id == 3522 && y == 3329)
					player.useStairs(-1, new WorldTile(x, 3332, 0), 0, 1);
				else if (id == 3522 && y == 3331)
					player.useStairs(-1, new WorldTile(x, 3329, 0), 0, 1);
				else if (id == 4914 && x == 3430 && y == 3233)
					player.useStairs(-1, new WorldTile(3405, 9631, 0), 0, 1);
				else if (id == 4921 && x == 3404 && y == 9631)
					player.useStairs(-1, new WorldTile(3429, 3233, 0), 0, 1);
				else if (id == 20524 && x == 3408 && y == 9623)
					player.useStairs(-1, new WorldTile(3428, 3225, 0), 0, 1);
				else if (id == 4915 && x == 3429 && y == 3225)
					player.useStairs(-1, new WorldTile(3409, 9623, 0), 0, 1);
				else if (id == 28515)
					player.useStairs(4853, new WorldTile(3420, 2803, 1), 2, 3);
				// scabaras florest
				else if (id == 28515)
					player.useStairs(4853, new WorldTile(3420, 2803, 1), 2, 3);
				else if (id == 28516)
					player.useStairs(4853, new WorldTile(3420, 2801, 0), 2, 3);
				// corsair cove
				else if (id == 131757 && x == 2418 && y == 2808) {
					if (!Agility.hasLevel(player, 10))
						return;
					player.useStairs(4853,
							new WorldTile(object.getX(), object.getY() + (object.getY() < player.getY() ? -1 : 1), 0),
							2, 3);
				} else if ((id == 131616 && y == 2797) || (id == 131617 && x == 1968))
					MythGuild.enter(player, object);
				else if (id == 132205 && x == 1937 && y == 9009)
					player.useStairs(828, new WorldTile(2329, 2785, 0), 1, 2);
				else if (id == 131626)
					player.useStairs(827, new WorldTile(1936, 9009, 1), 1, 2);
				else if (id == 131790 && x == 2012 && y == 9005)
					player.useStairs(828, new WorldTile(2395, 2798, 0), 1, 2);
				else if (id == 131791 && x == 2395 && y == 2797)
					player.useStairs(827, new WorldTile(2012, 9006, 1), 1, 2);
				else if (id == 131806 && x == 1970 && y == 9033)
					player.useStairs(-1, new WorldTile(2400, 2857, 0), 0, 1);
				else if (id == 2 && x == 2399 && y == 2858)
					player.useStairs(-1, new WorldTile(1971, 9035, 1), 0, 1);
				else if (id == 131807 && x == 1938 && y == 8966)
					player.useStairs(-1, new WorldTile(2317, 2754, 0), 0, 1);
				else if (id == 131606 && x == 2316 && y == 2755)
					player.useStairs(-1, new WorldTile(1939, 8968, 1), 0, 1);
				else if (id == 131618)
					player.useStairs(-1, new WorldTile(3191, 3361, 0), 0, 1, "You step through the portal...");
				else if (id == 131621)
					player.useStairs(-1, new WorldTile(2908, 3513, 0), 0, 1, "You step through the portal...");
				else if (id == 131622)
					player.useStairs(-1, new WorldTile(2728, 3371, 0), 0, 1, "You step through the portal...");
				else if (id == 131610 && x == 2322 && y == 2783)
					player.useStairs(-1, new WorldTile(2321, 2783, 1), 0, 1);
				// legends guild
				else if (id == 41435 && x == 2732 && y == 3377)
					player.useStairs(-1, new WorldTile(2732, 3380, 1), 0, 1);
				else if (id == 41436 && x == 2732 && y == 3378)
					player.useStairs(-1, new WorldTile(2732, 3376, 0), 0, 1);
				else if (id == 41425 && x == 2724 && y == 3374)
					player.useStairs(-1, new WorldTile(2720, 9775, 0), 0, 1);
				else if (id == 32048 && x == 2717 && y == 9773)
					player.useStairs(-1, new WorldTile(2723, 3375, 0), 0, 1);
				else if (id == 41449 || id == 66449)
					player.getBank().openBank();
				else if (id == 2938) { // recharge combat bracelet + skill
					// necklackes
					for (Item item : player.getInventory().getItems().getItems()) {
						if (item == null)
							continue;
						if (item.getId() >= 11120 && item.getId() <= 11126 && item.getId() % 2 == 0)
							item.setId(11118);
						else if (item.getId() >= 11107 && item.getId() <= 11113 && item.getId() % 2 != 0)
							item.setId(11105);
					}
					player.getInventory().refresh();
					player.getDialogueManager().startDialogue("ItemMessage",
							"Your combat bracelet and skill necklace have all been recharged.", 11105);
				} else if (id == 2896 || id == 2897) {
					player.lock(1);
					player.addWalkSteps(object.getX(), player.getY() == y ? y + 1 : y, 1, false);
				} else if (id == 2391 || id == 2392) {
					if (!player.getQuestManager().completedQuest(Quests.LEGENDS_QUEST)) {
						player.getPackets()
								.sendGameMessage("Please come back after you have Legends' Quest quest requiriments.");
						return;
					}
					player.lock(1);
					player.addWalkSteps(object.getX(), player.getY() == y ? y + 1 : y, 1, false);
					if (player.getY() == y) {
						List<Integer> indexes = World.getRegion(10804).getNPCsIndexes();
						if (indexes != null) {
							for (Integer index : indexes) {
								NPC npc = World.getNPCs().get(index);
								if (npc == null || (npc.getId() != 398 && npc.getId() != 399))
									continue;
								npc.setNextForceTalk(new ForceTalk("Legends' guild member approaching!"));
							}
						}
					}
				}
				// start of death platue
				else if (id == 34877 || id == 34878 || id == 9303 || id == 9304 || id == 9305 || id == 9306) {
					if (!Agility.hasLevel(player, 61))
						return;
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							boolean isGoingDown = id == 34877 ? player.getY() >= 3620
									: id == 34878 ? player.getY() >= 9587
											: id == 9303 ? player.getX() >= 2856
													: id == 9306 ? player.getX() <= 2909
															: id == 9305 ? player.getX() <= 2894
																	: player.getY() >= 3662;
							if (isGoingDown)
								player.useStairs(3382,
										id == 34877 ? new WorldTile(2877, 3618, 0)
												: id == 34878 ? new WorldTile(2875, 3594, 0)
														: id == 9303 ? new WorldTile(2854, 3664, 0)
																: id == 9306 ? new WorldTile(2912, 3687, 0)
																		: id == 9305 ? new WorldTile(2897, 3674, 0)
																				: new WorldTile(2875, 3659, 0),
										6, 7, null, true);
							else
								player.useStairs(3381,
										id == 34877 ? new WorldTile(2876, 3622, 0)
												: id == 34878 ? new WorldTile(2875, 3598, 0)
														: id == 9303 ? new WorldTile(2858, 3664, 0)
																: id == 9306 ? new WorldTile(2908, 3686, 0)
																		: id == 9305 ? new WorldTile(2893, 3673, 0)
																				: new WorldTile(2874, 3663, 0),
										6, 7, null, true);
						}
					}, 1);
				} else if (id == 3803) {
					if (!Agility.hasLevel(player, 64))
						return;
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							boolean isGoingDown = player.getX() >= 2877;
							if (isGoingDown)
								player.useStairs(15239, new WorldTile(2875, 3672, 0), 4, 5, null, true);
							else
								player.useStairs(3378, new WorldTile(2879, 3673, 0), 4, 5, null, true);
						}
					}, 1);
				} else if (id == 3748 || id == 5847) {
					if (!Agility.hasLevel(player, 41))
						return;
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							boolean isFailed = Utils.random(4) == 0;
							if (isFailed) {
								player.applyHit(new Hit(player, Utils.random(20, 50), HitLook.REGULAR_DAMAGE));
								player.getPackets().sendGameMessage("You skid your knee across the rocks.");
							}
							boolean isTravelingEast = id == 5847 ? player.getX() >= 2760
									: (x == 2817 && y == 3630) && player.getX() >= 2817;
							boolean isTravelingNorth = isTravelingEast ? false
									: (x == 2846 && y == 3620) ? player.getY() >= 3620 : player.getY() >= 3675;

							if (x == 2846 && y == 3620) {
								if (player.getEquipment().getBootsId() != 3105
										&& player.getEquipment().getBootsId() != 6145) {
									player.getDialogueManager().startDialogue("SimpleMessage",
											"You need rock climbing boots in order to jump this ledge.");
									return;
								}
							}
							player.useStairs(3377,
									new WorldTile(isTravelingNorth ? player.getX() : (isTravelingEast ? -1 : 1) + x,
											(isTravelingEast || (x == 2817 && y == 3630) || id == 5847 ? 0
													: (isTravelingNorth ? -2 : 2)) + player.getY(),
											0),
									4, 5, null, true);
						}
					});
				} else if (id == 35391 || id == 2832) {
					if (!Agility.hasLevel(player, id == 2832 ? 20 : 41))
						return;
					player.addWalkSteps(x, y);
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							boolean isTravelingWest = id == 2832 ? player.getX() >= 2508
									: (x == 2834 && y == 3626) ? player.getX() >= 2834 : player.getX() >= 2900;
							player.useStairs(3303,
									new WorldTile((isTravelingWest ? -2 : 2) + player.getX(), player.getY(), 0), 4, 5,
									null, true);
						}
					});
				} else if (id == 73657) {
					if (!Agility.hasLevel(player, 74))
						return;
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							WorldTile to = player.getY() < y ? new WorldTile(3193, 3801, 0)
									: new WorldTile(3194, 3799, 0);
							player.useStairs(3303, to, 2, 3, null, true);
						}
					});
					// catacombs
				} else if (id == 128892)
					player.useStairs(-1,
							object.getY() == 10057 ? new WorldTile(1706, 10078, 0)
									: object.getY() == 10077 ? new WorldTile(1716, 10056, 0)
											: object.getY() == 10008 ? new WorldTile(1646, 10000, 0)
													: new WorldTile(1648, 10009, 0),
							0, 1);
				else if (id == 128894 && x == 1666 && y == 10051)
					player.useStairs(828, new WorldTile(1639, 3673, 0), 1, 2);
				else if (id == 128898 && x == 1719 && y == 10102) // demon run
					player.useStairs(828, new WorldTile(1696, 3864, 0), 1, 2);
				else if (id == 128920 && x == 1696 && y == 3865)
					player.useStairs(827, new WorldTile(1719, 10101, 0), 1, 2);
				else if (id == 128895 && x == 1617 && y == 10102) // Dragon's Den
					player.useStairs(828, new WorldTile(1562, 3791, 0), 1, 2);
				else if (id == 128921 && x == 1563 && y == 3791)
					player.useStairs(827, new WorldTile(1617, 10101, 0), 1, 2);
				else if (id == 128896 && x == 1650 && y == 9986) // Reeking Cove
					player.useStairs(828, new WorldTile(1469, 3653, 0), 1, 2);
				else if (id == 128919 && x == 1470 && y == 3653)
					player.useStairs(827, new WorldTile(1650, 9987, 0), 1, 2);
				else if (id == 128897 && x == 1726 && y == 9993) // The Shallows
					player.useStairs(828, new WorldTile(1667, 3565, 0), 1, 2);
				else if (id == 128918 && x == 1666 && y == 3565)
					player.useStairs(827, new WorldTile(1726, 9992, 0), 1, 2);
				else if (id == 128900)
					player.getDialogueManager().startDialogue("CatacombsTeleport");
				else if (id == 127785) {
					player.lock();
					player.getPackets()
							.sendGameMessage("You investigate what looks the hinges on the plaque and find it opens.");
					FadingScreen.fade(player, 0, new Runnable() {
						@Override
						public void run() {
							player.useStairs(827, new WorldTile(1666, 10050, 0), 1, 2, "You climb down the hole.");
						}
					});
				}else if (id == 19171) {
					if (!Agility.hasLevel(player, 20))
						return;
					player.useStairs(-1, new WorldTile(player.getX() >= 2523 ? 2522 : 2523, 3375, 0), 1, 2,
							"You easily squeeze through the railing.");
				} else if (id == 2830 || id == 2831) {
					player.useStairs(-1, new WorldTile(player.getX(), id == 2831 ? 3026 : 3029, 0), 1, 2);
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							player.getDialogueManager().startDialogue("SimplePlayerMessage", "Phew! I barely made it.");
						}
					}, 1);
				} else if (id == 34839 || id == 34836) {
					boolean firstDoor = x == 2912;
					boolean withinArea = firstDoor ? player.getX() > 2912 : player.getY() < 3619;
					WorldObject opened = new WorldObject(object.getId(), object.getType(), object.getRotation() - 1,
							object.getX(), object.getY(), object.getPlane());
					World.spawnObjectTemporary(opened, 600);
					if (firstDoor)
						player.addWalkSteps(withinArea ? x : x + 1, player.getY(), 1, false);
					else
						player.addWalkSteps(player.getX(), withinArea ? y : y - 1, 1, false);
				} else if (id == 3758) {
					if (x == 2906 && y == 10017)
						player.useStairs(-1, new WorldTile(2911, 3636, 0), 1, 2);
					else if (x == 2906 && y == 10036)
						player.useStairs(-1, new WorldTile(2922, 3658, 0), 1, 2);
				} else if (id == 34395) {
					if (x == 2910 && y == 3637)
						player.useStairs(-1, new WorldTile(2907, 10019, 0), 1, 2);
					else if (x == 2920 && y == 3654)
						player.useStairs(-1, new WorldTile(2907, 10035, 0), 1, 2);
					else if (x == 2796 && y == 3614)
						player.useStairs(-1, new WorldTile(2808, 10002, 0), 1, 2);
				} else if (id == 35390)
					GodWars.passGiantBoulder(player, object, true);
				// shanty pass
				else if (id == 76651 || id == 76652)
					player.getDialogueManager().startDialogue("ShantyPassDangerSignD");
				else if (id == 12774) {
					player.lock(3);
					player.addWalkSteps(player.getX(), player.getY() <= object.getY() ? 3118 : 3115, 3, false);
					// stronghold of security
				} else if (id == 16154) // entrance
					player.useStairs(-1, new WorldTile(1859, 5243, 0), 0, 1);
				else if (id == 16148 || id == 16146) // stairs entrance up
					player.useStairs(828, new WorldTile(3081, 3421, 0), 1, 2,
							"You climb up the ladder to the surface.");
				else if (id == 16640 && object.getX() == 2330 && object.getY() == 10353)
					player.useStairs(828, new WorldTile(2141, 3944, 0), 1, 2);
				else if (id == 36306 && object.getX() == 2142 && object.getY() == 3944)
					player.useStairs(833, new WorldTile(2329, 10353, 2), 1, 2);
				else if (id == 16150) { // portal
					if (!player.getShosRewards()[0]) {
						player.getPackets().sendGameMessage(
								"You can't use this portal without looting the rewards on this floor first.");
						return;
					}
					player.useStairs(-1, new WorldTile(1914, 5222, 0), 0, 1);
				} else if (id == 16149) // stairs down
					player.useStairs(828, new WorldTile(2042, 5245, 0), 1, 2,
							"You climb down the ladder to the next level.");
				else if (id == 16082) { // portal
					if (!player.getShosRewards()[1]) {
						player.getPackets().sendGameMessage(
								"You can't use this portal without looting the rewards on this floor first.");
						return;
					}
					player.useStairs(-1, new WorldTile(2021, 5223, 0), 0, 1);
				} else if (id == 16080 || id == 16078) // stairs up
					player.useStairs(828, new WorldTile(1859, 5243, 0), 1, 2,
							"You climb up the ladder to the level above.");
				else if (id == 16081) // stairs down
					player.useStairs(828, new WorldTile(2123, 5252, 0), 1, 2,
							"You climb down the ladder to the next level.");
				else if (id == 16116) { // portal
					if (!player.getShosRewards()[2]) {
						player.getPackets().sendGameMessage(
								"You can't use this portal without looting the rewards on this floor first.");
						return;
					}
					player.useStairs(-1, new WorldTile(2146, 5287, 0), 0, 1);
				} else if (id == 7143 || id == 7153)
					AbbysObsticals.clearRocks(player, object);
				else if (id == 7152 || id == 7144)
					AbbysObsticals.clearTendrills(player, object, new WorldTile(id == 7144 ? 3028 : 3051, 4824, 0));
				else if (id == 7150 || id == 7146)
					AbbysObsticals.clearEyes(player, object,
							new WorldTile(object.getX() == 3021 ? 3028 : 3050, 4839, 0));
				else if (id == 7147)
					AbbysObsticals.clearGap(player, object, new WorldTile(3030, 4843, 0), false);
				else if (id == 7148)
					AbbysObsticals.clearGap(player, object, new WorldTile(3040, 4845, 0), true);
				else if (id == 7149)
					AbbysObsticals.clearGap(player, object, new WorldTile(3048, 4842, 0), false);
				else if (id == 7151)
					AbbysObsticals.burnGout(player, object, new WorldTile(3053, 4831, 0));
				else if (id == 7145)
					AbbysObsticals.burnGout(player, object, new WorldTile(3024, 4834, 0));
				else if (id == 7137)
					Runecrafting.enterWaterAltar(player);
				else if (id == 7139)
					Runecrafting.enterAirAltar(player);
				else if (id == 7140)
					Runecrafting.enterMindAltar(player);
				else if (id == 7131)
					Runecrafting.enterBodyAltar(player);
				else if (id == 7130)
					Runecrafting.enterEarthAltar(player);
				else if (id == 7129)
					Runecrafting.enterFireAltar(player);
				else if (id == 7133)
					Runecrafting.enterNatureAltar(player);
				else if (id == 7132)
					Runecrafting.enterCosmicAltar(player);
				else if (id == 7141)
					Runecrafting.enterBloodAltar(player);
				else if (id == 7134)
					Runecrafting.enterChoasAltar(player);
				else if (id == 7138)
					Runecrafting.enterSoulAltar(player);
				else if (id == 26341) {
					player.useStairs(827, new WorldTile(2882, 5311, 0), 2, 1, "You climb down the rope...");
					player.getControlerManager().startControler("GodWars");
				} else if (id == 7135) { // law abbys altar
					boolean hasEquip = false;
					for (Item item : player.getInventory().getItems().getItems()) {
						if (item == null)
							continue;
						if (Equipment.getItemSlot(item.getId()) != -1) {
							hasEquip = true;
							break;
						}
					}
					if (player.getEquipment().wearingArmour() || hasEquip) {
						player.getPackets().sendGameMessage(
								"The monk notices that you tried to fool him. Deposit your armor near the deposit box to travel to Entrana.");
						return;
					}
					Runecrafting.enterLawAltar(player);
				} else if (id == 7136)
					Runecrafting.enterDeathAltar(player);
				else if (id == 2469)
					player.useStairs(-1, new WorldTile(3315, 3253, 0), 1, 2);
				else if (id == 2468)// leaving earth
					player.useStairs(-1, new WorldTile(3308, 3476, 0), 1, 2);
				else if (id == 2470)// leaving body
					player.useStairs(-1, new WorldTile(3056, 3443, 0), 1, 2);
				else if (id == 2466)// leaving mind
					player.useStairs(-1, new WorldTile(2985, 3512, 0), 1, 2);
				else if (id == 2465)// leaving air
					player.useStairs(-1, new WorldTile(3051, 3443, 0), 1, 2);
				else if (id == 2467)// leaving water
					player.useStairs(-1, new WorldTile(3188, 3162, 0), 1, 2);
				else if (id == 2473)// leaving nature
					player.useStairs(-1, new WorldTile(2872, 3020, 0), 1, 2);
				else if (id == 2475)// leaving death
					player.useStairs(-1, new WorldTile(1865, 4639, 0), 1, 2);
				else if (id == 2477)// leaving blood
					player.useStairs(-1, new WorldTile(3559, 9778, 0), 1, 2);
				else if (id == 2472)// leaving law
					player.useStairs(-1, new WorldTile(2857, 3378, 0), 1, 2);
				else if (id == 2474) {// leaving choas
					player.useStairs(-1, new WorldTile(3058, 3588, 0), 1, 2);
					player.getControlerManager().startControler("Wilderness");
				} else if (id == 2471)// leaving cosmic
					player.useStairs(-1, new WorldTile(2409, 4380, 0), 1, 2);
				else if (id == 132491)// leaving wrath
					player.useStairs(-1, new WorldTile(2319, 2759, 0), 1, 2);
				else if (id == 16114 || id == 16112) // stairs up
					player.useStairs(828, new WorldTile(2042, 5245, 0), 1, 2,
							"You climb up the ladder to the level above.");
				else if (id == 16115) // stairs down
					player.useStairs(828, new WorldTile(2358, 5215, 0), 1, 2,
							"You climb down the ladder to the next level.");
				else if (id == 16050) { // portal
					if (!player.getShosRewards()[3]) {
						player.getPackets().sendGameMessage(
								"You can't use this portal without looting the rewards on this floor first.");
						return;
					}
					player.useStairs(-1, new WorldTile(2341, 5219, 0), 0, 1);
				} else if (id == 16050 || id == 16048) // stairs up
					player.useStairs(828, new WorldTile(3081, 3421, 0), 1, 2,
							"You climb up the ladder, which seems to twist wind in all directions.");
				else if (id == 16123 || id == 16124 || id == 16065 || id == 16066 || id == 16089 || id == 16090
						|| id == 16043 || id == 16044) { // stronghold
					// doors
					player.lock(3);
					player.setNextAnimation(new Animation(4282));
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							WorldTile tile;
							switch (object.getRotation()) {
							case 0:
								tile = new WorldTile(object.getX() == player.getX() ? object.getX() - 1 : object.getX(),
										player.getY(), 0);
								break;
							case 1:
								tile = new WorldTile(player.getX(),
										object.getY() == player.getY() ? object.getY() + 1 : object.getY(), 0);
								break;
							case 2:
								tile = new WorldTile(object.getX() == player.getX() ? object.getX() + 1 : object.getX(),
										player.getY(), 0);
								break;
							case 3:
							default:
								tile = new WorldTile(player.getX(),
										object.getY() == player.getY() ? object.getY() - 1 : object.getY(), 0);
								break;
							}
							player.setNextWorldTile(tile);
							player.setNextAnimation(new Animation(4283));
							player.faceObject(object);
						}
					}, 0);
				} else if (id == 16135) {
					if (player.getShosRewards()[0]) {
						player.getPackets().sendGameMessage("You have already claimed your reward from this level.");
						return;
					}
					player.getDialogueManager().startDialogue("StrongHoldOfSecurityRewards", 0);
				} else if (id == 16077) {
					if (player.getShosRewards()[1]) {
						player.getPackets().sendGameMessage("You have already claimed your reward from this level.");
						return;
					}
					player.getDialogueManager().startDialogue("StrongHoldOfSecurityRewards", 1);
				} else if (id == 16118) {
					if (player.getShosRewards()[2]) {
						player.getPackets().sendGameMessage("You have already claimed your reward from this level.");
						return;
					}
					player.getDialogueManager().startDialogue("StrongHoldOfSecurityRewards", 2);
				} else if (id == 16047)
					player.getDialogueManager().startDialogue("StrongHoldOfSecurityRewards", 3);
				else if (id == 16152)
					player.getDialogueManager().startDialogue("StrongholdSecurityDeadBody");
				else if (id == 2811 || id == 2812) {
					player.useStairs(id == 2812 ? 827 : -1,
							id == 2812 ? new WorldTile(2501, 2989, 0) : new WorldTile(2574, 3029, 0), 1, 2);
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							player.getDialogueManager().startDialogue("SimplePlayerMessage",
									"Wow! That tunnel went a long way.");
						}
					});
				} else if (id == 2890 || id == 2893) {
					if (player.getEquipment().getWeaponId() != 975
							&& !player.getInventory().containsItemToolBelt(975, 1)) {
						player.getPackets().sendGameMessage("You need a machete in order to cutt through the terrain.");
						return;
					}
					player.setNextAnimation(new Animation(910));
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							if (Utils.random(3) == 0) {
								player.getPackets().sendGameMessage("You fail to slash through the terrain.");
								return;
							}
							WorldObject o = new WorldObject(object);
							o.setId(id + 1);
							World.spawnObjectTemporary(o, 5000);
						}
					});
				} else if (id == 2231)
					player.useStairs(-1, new WorldTile(x == 2792 ? 2795 : 2791, 2979, 0), 1, 2,
							x == 2792 ? "You climb down the slope." : "You climb up the slope.");
				else if (id == 23157)
					player.useStairs(-1, new WorldTile(2729, 3734, 0), 1, 2);
				else if (id == 492 && x == 2856 && y == 3168) // karamja
					// underground and
					// crandor
					player.useStairs(827, new WorldTile(2856, 9570, 0), 1, 2);
				else if (id == 1764 && x == 2856 && y == 9569)
					player.useStairs(828, new WorldTile(2855, 3169, 0), 1, 2);
				else if (id == 2606 && x == 2836 && y == 9600) {
					if (World.isSpawnedObject(object))
						return;
					player.lock(1);
					WorldObject opened = new WorldObject(object.getId(), object.getType(), object.getRotation() - 1,
							object.getX(), object.getY(), object.getPlane());
					World.spawnObjectTemporary(opened, 1200);
					player.addWalkSteps(2836, player.getY() == y ? y - 1 : y, 1, false);
				} else if (id == 25154)
					player.useStairs(827, new WorldTile(2834, 9657, 0), 1, 2);
				else if (id == 25213 && x == 2833 && y == 9657)
					player.useStairs(828, new WorldTile(2834, 3258, 0), 1, 2);
				else if (id == 68134) // thzaar entrance
					player.useStairs(-1, new WorldTile(4667, 5059, 0), 0, 1);
				else if (id == 68135)
					player.useStairs(-1, new WorldTile(2845, 3170, 0), 0, 1);
				// alkarid east area,(abbey + desert wyverms)
				else if (id == 75882) {
					player.lock(3);
					player.addWalkSteps(player.getX() > object.getX() ? 3332 : 3335, player.getY(), 3, false);
					// elemental workshop
				} else if (id == 26114 || id == 26115) {
					if (World.isSpawnedObject(object))
						return;
					if (!player.getQuestManager().completedQuest(Quests.ELEMENTAL_WORKSHOP_I)) {
						player.getPackets().sendGameMessage(
								"Please come back after you have Elemental Workshop I quest requiriments.");
						return;
					}
					player.lock(1);
					World.spawnObjectTemporary(new WorldObject(object.getId(), object.getType(),
							object.getRotation() - 1, object.getX(), object.getY(), object.getPlane()), 1200);
					player.addWalkSteps(object.getX(), player.getY() == y ? y + 1 : y, 1, false);
				} else if (id == 3415 && x == 2710 && y == 3497)
					player.useStairs(-1, new WorldTile(2716, 9888, 0), 0, 1);
				else if (id == 3416 && x == 2714 && y == 9887)
					player.useStairs(-1, new WorldTile(2709, 3498, 0), 0, 1);
				else if (id == 34395 && object.getX() == 2796 && object.getY() == 3614)
					player.useStairs(-1, new WorldTile(2808, 10003, 0), 0, 1);
				else if (id == 77053 && object.getX() == 2809 && object.getY() == 10001)
					player.useStairs(-1, new WorldTile(2795, 3616, 0), 0, 1);
				else if (id == 21585)
					player.getPackets().sendGameMessage(
							"The doors seem to be frozen shut. Perhaps there is another way to enter.");
				else if (id == 21584) {
					player.getPackets().sendGameMessage(
							"The doors seem to be barricaded by a pile of rocks. You decide to enter anyways.");
					player.useStairs(-1, new WorldTile(2394, 10299, 1), 1, 2);
				} else if (id == 21598)
					player.useStairs(-1, new WorldTile(2402, 3887, 0), 1, 2);
				else if (id == 3413)
					ShopsHandler.openShop(player, 80);
				// canafis shortcutbehind bar
				else if (id == 5055 && x == 3495 && y == 3465)
					player.useStairs(827, new WorldTile(3477, 9845, 0), 1, 2);
				else if (id == 5054 && x == 3477 && y == 9846)
					player.useStairs(828, new WorldTile(3494, 3465, 0), 1, 2);
				// Falador castle
				else if (id == 11724 && x == 2968 && y == 3347)
					player.useStairs(-1, new WorldTile(2968, 3348, 1), 1, 2);
				else if (id == 11725 && x == 2968 && y == 3347)
					player.useStairs(-1, new WorldTile(2971, 3347, 0), 1, 2);
				// Lumbridge Church
				else if (id == 36986 && x == 3246 && y == 3213)
					player.useStairs(828, new WorldTile(3247, 3213, 1), 1, 2);
				else if (id == 36987 && x == 3246 && y == 3213)
					player.useStairs(827, new WorldTile(3246, 3213, 0), 1, 2);
				else if (id == 36984 && x == 3241 && y == 3213)
					player.useStairs(828, new WorldTile(3240, 3213, 1), 1, 2);
				else if (id == 36985 && x == 3241 && y == 3213)
					player.useStairs(827, new WorldTile(3241, 3213, 0), 1, 2);
				// daemonheim big stairs
				else if (id == 48611 && x == 3451 && y == 3733)
					player.useStairs(-1, new WorldTile(3451, 3738, 1), 1, 2);
				else if (id == 50568 && x == 3451 && y == 3737)
					player.useStairs(-1, new WorldTile(3452, 3732, 0), 1, 2);
				else if (id == 48612 && x == 3447 && y == 3733)
					player.useStairs(-1, new WorldTile(3448, 3738, 1), 1, 2);
				else if (id == 50567 && x == 3448 && y == 3737)
					player.useStairs(-1, new WorldTile(3447, 3732, 0), 1, 2);
				// waterfall quest
				else if (id == 2020 && x == 2512 && y == 3465)
					player.useStairs(828, new WorldTile(2511, 3463, 0), 1, 2);
				else if (id == 37247 && x == 2511 && y == 3464)
					player.useStairs(9105, new WorldTile(2575, 9862, 0), 1, 2);
				else if (id == 32711 && x == 2575 && y == 9860)
					player.useStairs(9105, new WorldTile(2511, 3463, 0), 1, 2);
				else if (id == 5052) {
					if (World.isSpawnedObject(object))
						return;
					if (!player.getQuestManager().completedQuest(Quests.IN_SEARCH_OF_THE_MYREQUE)) {
						player.getPackets().sendGameMessage(
								"Please come back after you have In Search of The Myreque quest requiriments.");
						return;
					}
					WorldObject opened = new WorldObject(object.getId(), object.getType(), object.getRotation() - 1,
							object.getX(), object.getY(), object.getPlane());
					World.spawnObjectTemporary(opened, 1200);
					player.lock(1);
					player.addWalkSteps(object.getX(), player.getY() == y ? y - 1 : y, 1, false);
				} else if (id == 2261 || id == 2262)
					player.addWalkSteps(player.getX() >= 2868 ? 2867 : 2868, player.getY(), -1, false);
				else if (id == 2259 || id == 2260)
					player.addWalkSteps(player.getX() >= 2875 ? 2874 : 2875, player.getY(), -1, false);
				else if ((id == 59958 || id == 59961) && x == 3622)
					player.addWalkSteps(player.getX() >= 3623 ? 3622 : 3623, player.getY(), -1, false);
				else if ((id == 59958 || id == 59961) && x == 3632) {
					player.addWalkSteps(player.getX() >= 3633 ? 3632 : 3633, player.getY(), -1, false);
					player.getMusicsManager().playMusic(player.getX() >= 3633 ? 987 : 988);
				} else if ((id == 69197 || id == 69198) && y == 3492)
					player.addWalkSteps(player.getX(), player.getY() > 3491 ? 3491 : 3493, -1, false);
				else if (id == 2216)
					player.useStairs(-1, new WorldTile(player.getX() >= 2877 ? 2876 : 2880, 2953, 0), 1, 2);
				else if (id == 30261 || id == 30262)
					player.useStairs(-1, new WorldTile(3491, 3411, 0), 0, 1);
				// mortayna obstacles
				else if (id == 5005 && x == 3502 && y == 3431)
					player.useStairs(828, new WorldTile(3502, 3425, 0), 1, 2);
				else if (id == 5005 && x == 3502 && y == 3426)
					player.useStairs(828, new WorldTile(3502, 3432, 0), 1, 2);
				// lava maze dungeon
				else if (id == 1767 && x == 3069 && y == 3857)
					player.useStairs(828, new WorldTile(3017, 10248, 0), 1, 2);
				else if (id == 32015 && x == 3017 && y == 10249)
					player.useStairs(-1, new WorldTile(3069, 3858, 0), 0, 1);
				// agarnia ice dungeon
				else if (id == 9472 && x == 3008 && y == 3150)
					player.useStairs(-1, new WorldTile(3009, 9550, 0), 0, 1);
				else if (id == 32015 && x == 3008 && y == 9550)
					player.useStairs(-1, new WorldTile(3009, 3150, 0), 0, 1);
				else if (id == 33173)
					player.useStairs(-1, new WorldTile(3056, 9555, 0), 0, 1);
				else if (id == 33174)
					player.useStairs(-1, new WorldTile(3056, 9562, 0), 0, 1);
				// sop bank
				else if (id == 20275 && x == 3315 && y == 2797)
					player.useStairs(827, new WorldTile(2799, 5160, 0), 1, 2);
				else if (id == 20277 && x == 2799 && y == 5159)
					player.useStairs(828, new WorldTile(3315, 2796, 0), 1, 2);
				else if (id == 20281 && x == 3318 && y == 9274)
					player.useStairs(828, new WorldTile(2800, 5160, 0), 1, 2);
				else if (id == 20278 && x == 2800 && y == 5159)
					player.useStairs(827, new WorldTile(3318, 9273, 2), 1, 2);
				// Gnome Stronghold Brimstail cave
				else if (id == 5083)
					player.getDialogueManager().startDialogue("SaniBoch", true);
				else if (id == 77421)
					player.useStairs(-1, new WorldTile(2745, 3152, 0), 0, 1);
				else if (id == 17222 || id == 17223)
					player.useStairs(-1, new WorldTile(2402, 3419, 0), 0, 1);
				else if (id == 17209)
					player.useStairs(-1, new WorldTile(2408, 9812, 0), 0, 1);
				// The Tale of the Muspah cave
				else if (id == 42793 && x == 2737 && y == 3729)
					player.useStairs(-1, new WorldTile(
							player.getQuestManager().completedQuest(Quests.THE_TALE_OF_THE_MUSPAH) ? 3485 : 3421, 5511,
							0), 0, 1);
				else if (id == 42891)
					player.useStairs(-1, new WorldTile(2736, 3731, 0), 0, 1);
				else if (id == 52626 || id == 52627)
					player.addWalkSteps(player.getX(), 2937, -1, false);
				else if (id == 52628 || id == 52629)
					player.addWalkSteps(player.getX(), 2940, -1, false);
				else if (id == 52624 || id == 52625)
					player.addWalkSteps(3754, player.getY(), -1, false);
				else if (id == 52622 || id == 52623)
					player.addWalkSteps(3751, player.getY(), -1, false);
				else if (id == 4627)
					player.useStairs(-1, new WorldTile(2893, 3567, 0), 1, 2);
				else if (id == 66973)
					player.useStairs(-1, new WorldTile(2206, 4934, 1), 1, 2);
				else if (id == 4620)
					player.useStairs(-1, new WorldTile(2207, 4938, 0), 1, 2);
				else if (id == 4622 && x == 2212)
					player.useStairs(-1, new WorldTile(2212, 4944, 1), 1, 2);
				else if (id == 42794)
					player.useStairs(-1, new WorldTile(object.getX(), object.getY() + 7, 0), 0, 1);
				else if (id == 42795)
					player.useStairs(-1, new WorldTile(object.getX(), object.getY() - 6, 0), 0, 1);
				else if (id == 48188)
					player.useStairs(-1, new WorldTile(3435, 5646, 0), 0, 1);
				else if (id == 48189)
					player.useStairs(-1, new WorldTile(3509, 5515, 0), 0, 1);
				else if (id == 15767)
					player.getDialogueManager().startDialogue("CaveyDavey");
				// slayer tower
				else if (id == 9319) {
					if (!Agility.hasLevel(player, x == 3422 && y == 3550 ? 61 : 71))
						return;
					player.useStairs(828, new WorldTile(player.getX(), player.getY(), player.getPlane() + 1), 1, 2);
				} else if (id == 9320) {
					if (!Agility.hasLevel(player, 61))
						return;
					player.useStairs(827, new WorldTile(player.getX(), player.getY(), player.getPlane() - 1), 1, 2);
				} else if (object.getId() == 15791) {
					if (object.getX() == 3829)
						player.useStairs(-1, new WorldTile(3830, 9461, 0), 1, 2);
					if (object.getX() == 3814)
						player.useStairs(-1, new WorldTile(3815, 9461, 0), 1, 2);
					player.getControlerManager().startControler("UnderGroundDungeon", false, true);
				} else if (object.getId() == 6898) {
					player.setNextAnimation(new Animation(10578));
					player.useStairs(-1, object, 1, 2);
					player.useStairs(10579, new WorldTile(3221, 9618, 0), 1, 2);
					player.getControlerManager().startControler("UnderGroundDungeon", false, true);
					player.getPackets().sendGameMessage("You squeeze through the hole.");
					return;
				} else if (id == 36002) {
					player.getControlerManager().startControler("UnderGroundDungeon", true, false);
					player.useStairs(833, new WorldTile(3206, 9379, 0), 1, 2);
				} else if (id == 4493 && x == 3434 && y == 3537)
					player.useStairs(-1, new WorldTile(3433, 3538, 1), 0, 1);
				else if (id == 4494 && x == 3434 && y == 3537)
					player.useStairs(-1, new WorldTile(3438, 3538, 0), 0, 1);
				else if (id == 4495 && x == 3413 && y == 3540)
					player.useStairs(-1, new WorldTile(3417, 3541, 2), 0, 1);
				else if (id == 4496 && x == 3415 && y == 3540)
					player.useStairs(-1, new WorldTile(3412, 3541, 1), 0, 1);
				// Paterdomus underground
				else if (id == 30572 && x == 3405 && y == 3507)
					player.useStairs(827, new WorldTile(3405, 9906, 0), 1, 2);
				else if (id == 30575 && x == 3405 && y == 9907)
					player.useStairs(828, new WorldTile(3405, 3506, 0), 1, 2);
				else if (id == 3443)
					player.useStairs(-1, new WorldTile(3423, 3484, 0), 0, 1);
				else if (id == 30574 && x == 3422 && y == 3484)
					player.useStairs(827, new WorldTile(3440, 9887, 0), 1, 2);
				// artisian workshop stairs
				else if (id == 29392)
					player.useStairs(-1, new WorldTile(3061, 3335, 0), 0, 1);
				else if (id == 29385 || id == 29385)
					player.useStairs(-1, new WorldTile(3067, 9710, 0), 0, 1);
				else if (id == 29387)
					player.useStairs(-1, new WorldTile(3035, 9713, 0), 0, 1);
				else if (id == 29391)
					player.useStairs(-1, new WorldTile(3037, 3342, 0), 0, 1);
				else if (id == 29375) {
					final boolean isNorth = player.getY() > 9964;
					final WorldTile tile = new WorldTile(player.getX(), player.getY() + (isNorth ? -7 : 7), 0);
					player.setNextAnimation(new Animation(745));
					player.setNextForceMovement(
							new ForceMovement(player, 1, tile, 5, isNorth ? ForceMovement.SOUTH : ForceMovement.NORTH));
					WorldTasksManager.schedule(new WorldTask() {
						int ticks = 0;

						@Override
						public void run() {
							ticks++;
							if (ticks > 1)
								player.setNextAnimation(new Animation(744));
							if (ticks == 5) {
								player.setNextWorldTile(tile);
								stop();
								return;
							}
						}
					}, 0, 0);
				} else if (id == 44339) {
					if (!Agility.hasLevel(player, 81))
						return;
					boolean isEast = player.getX() > 2772;
					final WorldTile tile = new WorldTile(isEast ? 2768 : 2775, 10002, 0);
					WorldTasksManager.schedule(new WorldTask() {

						int ticks = -1;

						@Override
						public void run() {
							ticks++;
							if (ticks == 0)
								player.setNextFaceWorldTile(object);
							else if (ticks == 1) {
								player.setNextAnimation(new Animation(10738));
								player.setNextForceMovement(new NewForceMovement(player, 0, tile, 5,
										Utils.getAngle(object.getX() - player.getX(), object.getY() - player.getY())));
							} else if (ticks == 3)
								player.setNextWorldTile(tile);
							else if (ticks == 4) {
								player.getPackets().sendGameMessage("Your feet skid as you land floor.");
								stop();
								return;
							}
						}
					}, 0, 0);
				} else if (id == 77052) {
					if (!Agility.hasLevel(player, 70))
						return;
					boolean isEast = player.getX() > 2734;
					final WorldTile tile = new WorldTile(isEast ? 2730 : 2735, 10008, 0);
					WorldTasksManager.schedule(new WorldTask() {

						int ticks = -1;

						@Override
						public void run() {
							ticks++;
							if (ticks == 0)
								player.setNextFaceWorldTile(object);
							else if (ticks == 1)
								player.setNextAnimation(new Animation(17811));
							else if (ticks == 9)
								player.setNextWorldTile(tile);
							else if (ticks == 10) {
								stop();
								return;
							}
						}
					}, 0, 0);
				} else if (id == 9311 || id == 9312) {
					if (!Agility.hasLevel(player, 21))
						return;
					WorldTasksManager.schedule(new WorldTask() {

						int ticks = 0;

						@Override
						public void run() {
							boolean withinGE = id == 9312;
							WorldTile tile = withinGE ? new WorldTile(3139, 3516, 0) : new WorldTile(3143, 3514, 0);
							player.lock();
							ticks++;
							if (ticks == 1) {
								player.setNextAnimation(new Animation(2589));
								player.setNextForceMovement(new ForceMovement(object, 1,
										withinGE ? ForceMovement.WEST : ForceMovement.EAST));
							} else if (ticks == 3) {
								player.setNextWorldTile(new WorldTile(3141, 3515, 0));
								player.setNextAnimation(new Animation(2590));
							} else if (ticks == 5) {
								player.setNextAnimation(new Animation(2591));
								player.setNextWorldTile(tile);
							} else if (ticks == 6) {
								player.setNextWorldTile(
										new WorldTile(tile.getX() + (withinGE ? -1 : 1), tile.getY(), tile.getPlane()));
								player.unlock();
								stop();
							}
						}
					}, 0, 0);
				} else if (id == 14922) {
					WorldTasksManager.schedule(new WorldTask() {

						int ticks = 0;

						@Override
						public void run() {
							boolean withinGE = y == 3654;
							WorldTile tile = withinGE ? new WorldTile(2344, 3650, 0) : new WorldTile(2344, 3655, 0);
							player.lock();
							ticks++;
							if (ticks == 1) {
								player.setNextAnimation(new Animation(2589));
								player.setNextForceMovement(new ForceMovement(object, 1,
										withinGE ? ForceMovement.SOUTH : ForceMovement.NORTH));
							} else if (ticks == 3) {
								player.setNextWorldTile(new WorldTile(2344, 3652, 0));
								player.setNextAnimation(new Animation(2590));
							} else if (ticks == 5) {
								player.setNextAnimation(new Animation(2591));
								player.setNextWorldTile(tile);
							} else if (ticks == 6) {
								player.unlock();
								stop();
							}
						}
					}, 0, 0);
				} else if (id == 2878 || id == 2879) {
					player.getDialogueManager().startDialogue("SimpleMessage",
							"You step into the pool of sparkling water. You feel the energy rush through your veins.");
					final boolean isLeaving = id == 2879;
					final WorldTile tile = isLeaving ? new WorldTile(2509, 4687, 0) : new WorldTile(2542, 4720, 0);
					player.setNextForceMovement(new ForceMovement(player, 1, tile, 2,
							isLeaving ? ForceMovement.SOUTH : ForceMovement.NORTH));
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							player.setNextAnimation(new Animation(13842));
							WorldTasksManager.schedule(new WorldTask() {

								@Override
								public void run() {
									player.setNextAnimation(new Animation(-1));
									player.setNextWorldTile(
											isLeaving ? new WorldTile(2542, 4718, 0) : new WorldTile(2509, 4689, 0));
								}
							}, 2);
						}
					});
				} else if (id == 24991) {
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							player.getControlerManager().startControler("PuroPuro");
						}
					}, 10);
					Magic.sendTeleportSpell(player, 6601, -1, 1118, -1, 0, 0, new WorldTile(2591, 4320, 0), 9, false,
							Magic.OBJECT_TELEPORT);
				} else if (id == 2873 || id == 2874 || id == 2875) {
					player.getPackets().sendGameMessage(
							"You kneel and begin to chant to " + objectDef.name.replace("Statue of ", "") + "...");
					player.setNextAnimation(new Animation(645));
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							player.getDialogueManager().startDialogue("SimpleMessage",
									"You feel a rush of energy charge through your veins. Suddenly a cape appears before you.");
							World.addGroundItem(new Item(id == 2873 ? 2412 : id == 2874 ? 2414 : 2413),
									new WorldTile(object.getX(), object.getY() - 1, 0),
									null, false, -1, 2, 150);
						}
					}, 3);
				} else if (id == 18050) {
					return;
				} else if (id == 77574 || id == 77573) {
					boolean back = id == 77573;
					player.lock(4);
					final WorldTile tile = back ? new WorldTile(2687, 9506, 0) : new WorldTile(2682, 9506, 0);
					final boolean isRun = player.isRunning();
					player.setRun(false);
					player.addWalkSteps(tile.getX(), tile.getY(), -1, false);
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							player.setRun(isRun);
						}
					}, 4);
				} else if (id == 77377 || id == 77379 || id == 77375 || id == 77373 || id == 77371) {
					final HatchetDefinitions hatchet = Woodcutting.getHatchet(player, false);
					if (hatchet == null) {
						player.getPackets().sendGameMessage(
								"You dont have the required level to use that axe or you don't have a hatchet.");
						return;
					}
					player.lock();
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							player.setNextAnimation(new Animation(hatchet.getEmoteId()));
							if (Utils.getRandom(13 - hatchet.getAxeTime()) <= 3) {
								stop();
								WorldObject o = new WorldObject(object);
								o.setId(object.getId() - 1);
								World.spawnObjectTemporary(o, 10000);
								player.addWalkSteps(x, y, 1, false);
								player.unlock();
							}
						}
					}, 1, 1);
				} else if (id == 49016 || id == 49014) {
					if (player.getSkills().getLevel(Skills.FISHING) < 60) {
						player.getPackets()
								.sendGameMessage("You need a Fishing level of 60 in order to pass through this gate.");
						return;
					}
					player.addWalkSteps(x, player.getY() < 3387 ? y + 1 : y, 1, false);
				} else if (id == 77506 || id == 77507) {
					player.useStairs(-1,
							new WorldTile(player.getX(), player.getY() + (id == 77506 ? -9 : 9), id == 77506 ? 2 : 0),
							1, 2);
				} else if (id == 77508 || id == 77509) {
					player.useStairs(-1, id == 77508 ? new WorldTile(2643, 9595, 2) : new WorldTile(2649, 9591, 0), 1,
							2);
				} else if (id >= 77570 && id <= 77572) {
					player.lock(1);
					player.setNextAnimation(new Animation(741));
					player.setNextForceMovement(new NewForceMovement(player, 0, object, 1,
							Utils.getAngle(object.getX() - player.getX(), object.getY() - player.getY())));
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							player.setNextWorldTile(object);
						}
					});
				} else if (id == 73681) {
					WorldTile dest = new WorldTile(player.getX() == 2595 ? 2598 : 2595, 3608, 0);
					player.setNextForceMovement(new NewForceMovement(player, 1, dest, 2,
							Utils.getAngle(dest.getX() - player.getX(), dest.getY() - player.getY())));
					player.useStairs(-1, dest, 1, 2);
					player.setNextAnimation(new Animation(769));
				} else if (id == 9738 || id == 9330) {
					boolean rightDoor = object.getId() == 9330;
					WorldObject o = new WorldObject(object);
					o.setRotation(rightDoor ? -1 : 1);
					World.spawnObjectTemporary(o, 1000);
					WorldObject o2 = new WorldObject(rightDoor ? 9738 : 9330, object.getType(), object.getRotation(),
							2558, rightDoor ? 3299 : 3300, object.getPlane());
					o2.setRotation(rightDoor ? 1 : 3);
					World.spawnObjectTemporary(o2, 1000);
					player.addWalkSteps(player.getX() + (player.getX() >= 2559 ? -3 : 3), y, -1, false);
				} else if (id == 2406) {
					if (FairyRings.checkAll(player)) {
						player.useStairs(-1, new WorldTile(2452, 4473, 0), 1, 2);
						return;
					} else
						handleDoor(player, object);
				} else if (id == 50552) {
					player.setNextForceMovement(new ForceMovement(object, 1, ForceMovement.NORTH));
					player.useStairs(13760, new WorldTile(3454, 3725, 0), 2, 3);
				} else if (id == 74867 && object.getX() == 2842 && object.getY() == 3424) {
					player.useStairs(833, new WorldTile(2842, 9823, 0), 1, 2);
				} else if (id == 74866 && object.getX() == 2842 && object.getY() == 9824) {
					player.useStairs(828, new WorldTile(2842, 3423, 0), 1, 2);
				} else if (id == 9294) {
					if (!Agility.hasLevel(player, 80))
						return;
					final boolean isRunning = player.getRun();
					final boolean isSouth = player.getY() > 9813;
					final WorldTile tile = isSouth ? new WorldTile(2878, 9812, 0) : new WorldTile(2881, 9814, 0);
					player.setRun(true);
					player.addWalkSteps(isSouth ? 2881 : 2877, isSouth ? 9814 : 9812);
					WorldTasksManager.schedule(new WorldTask() {
						int ticks = 0;

						@Override
						public void run() {
							ticks++;
							if (ticks == 2)
								player.setNextFaceWorldTile(object);
							else if (ticks == 3) {
								player.setNextAnimation(new Animation(1995));
								player.setNextForceMovement(new NewForceMovement(player, 0, tile, 4,
										Utils.getAngle(object.getX() - player.getX(), object.getY() - player.getY())));
							} else if (ticks == 4)
								player.setNextAnimation(new Animation(1603));
							else if (ticks == 7) {
								player.setNextWorldTile(tile);
								player.setRun(isRunning);
								stop();
								return;
							}
						}
					}, 0, 0);
				} else if (id == 2333 || id == 2334 || id == 2335) {
					if (!Agility.hasLevel(player, 74))
						return;
					player.lock(2);
					player.setNextAnimation(new Animation(741));
					player.setNextForceMovement(new ForceMovement(object, 1,
							Utils.getMoveDirection(player.getX() - object.getX(), player.getY() - object.getY()) == 6
									? 0
									: 2));
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							player.setNextWorldTile(object);
						}
					});
				} else if (id == 70794) {
					player.useStairs(-1, new WorldTile(1340, 6488, 0), 0, 2);
				} else if (id == 70795) {
					if (!Agility.hasLevel(player, 50))
						return;
					player.getDialogueManager().startDialogue("GrotwormLairD", true);
				} else if (id == 70812) {
					player.getDialogueManager().startDialogue("GrotwormLairD", false);
				} else if (id == 70799) {
					player.useStairs(-1, new WorldTile(1178, 6355, 0), 0, 2);
				} else if (id == 70796) {
					player.useStairs(-1, new WorldTile(1090, 6360, 0), 0, 2);
				} else if (id == 70798) {
					player.useStairs(-1, new WorldTile(1340, 6380, 0), 0, 2);
				} else if (id == 70797) {
					player.useStairs(-1, new WorldTile(1090, 6497, 0), 0, 2);
				} else if (id == 70792) {
					player.getDialogueManager().startDialogue("GrotwormDungD");
					//player.useStairs(-1, new WorldTile(1206, 6371, 0), 0, 2);
				} else if (id == 70793) {
					player.useStairs(-1, new WorldTile(2989, 3237, 0), 0, 2);
				} else if (id == 2353 && (object.getX() == 3177 && object.getY() == 5730 && object.getPlane() == 0))
					player.useStairs(828, new WorldTile(3353, 3416, 0), 1, 2);
				else if (id == 38279)
					player.getDialogueManager().startDialogue("RunespanPortalD");
				else if (id == 38315) {
					player.getInterfaceManager().sendInterface(780);
					player.getPackets().sendIComponentText(780, 79, "All the altars of Matrix.");
				} else if (id == 31359) {
					player.useStairs(-1, new WorldTile(3360, 9352, 0), 1, 2);
					player.getControlerManager().startControler("UnderGroundDungeon", true, false);
				} else if (id == 8929)
					player.getDialogueManager().startDialogue("WaterbirthDungD");
					//player.useStairs(-1, new WorldTile(2442, 10147, 0), 1, 2);
				else if (id == 8966)
					player.useStairs(-1, new WorldTile(2523, 3739, 0), 1, 2);
				else if (id == 76219) {
					player.getPackets().sendGameMessage("You search the shelves...");
					player.lock();
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							player.unlock();
							player.getPackets().sendGameMessage(
									"...and among the strange paraphernalia, you find an empty beer glass.");
							player.getInventory().addItem(SqirkFruitSqueeze.BEER_GLASS, 1);
						}
					}, 2);
				} else if (id == 25268) {
					player.getPackets().sendGameMessage("You search the bed...");
					player.lock();
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							player.unlock();
							player.getPackets().sendGameMessage(
									"...and you find a barbarian rod.");
							player.getInventory().addItem(11323, 1);
						}
					}, 2);
				} else if (id == 12163 || id == 12164 || id == 12165 || id == 12166) {
					if (player.getTemporaryAttributtes().get("canoe_shaped") != null
							&& (boolean) player.getTemporaryAttributtes().get("canoe_shaped"))
						Canoes.openTravelInterface(player, id - 12163);
					else if (player.getTemporaryAttributtes().get("canoe_chopped") != null
							&& (boolean) player.getTemporaryAttributtes().get("canoe_chopped"))
						Canoes.openSelectionInterface(player);
					else
						Canoes.chopCanoeTree(player, id - 12163);
				} else if (id == 76499) {
					player.lock(1);
					player.addWalkSteps(player.getX(), player.getY() == 3164 ? 3162 : 3164, 2, false);
				} else if (object.getId() == 39508 || object.getId() == 39509) {
					StealingCreationLobbyController.climOverStile(player, object, true);
				} else if (id == 2491 || id == 16684 || id == 110796 || id == 108981)
					player.getActionManager()
							.setAction(new EssenceMining(object,
									player.getSkills().getLevel(Skills.MINING) < 30 ? EssenceDefinitions.Rune_Essence
											: EssenceDefinitions.Pure_Essence));
				else if (id == 63093)
					player.useStairs(-1, new WorldTile(4620, 5458, 3), 0, 1);
				else if (id == 63094)
					player.useStairs(-1, new WorldTile(3410, 3329, 0), 0, 1);
				else if (id == 64294 || id == 64295) {
					if (!Agility.hasLevel(player, 73)) {
						player.getDialogueManager().startDialogue("SimplePlayerMessage",
								"A fall would be a long way down.");
						return;
					}
					player.setNextAnimation(new Animation(6132));
					player.lock(3);
					final WorldTile toTile;
					if (id == 64295 && x == 4661 && y == 5476)
						toTile = new WorldTile(4658, 5476, 3);
					else if (id == 64295 && x == 4682 && y == 5476)
						toTile = new WorldTile(4685, 5476, 3);
					else if (id == 64294 && x == 4684 && y == 5476)
						toTile = new WorldTile(4681, 5476, 3);
					else
						toTile = new WorldTile(4662, 5476, 3);
					player.setNextForceMovement(new ForceMovement(player, 0, toTile, 2,
							object.getRotation() == 2 ? ForceMovement.EAST : ForceMovement.WEST));
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							player.setNextWorldTile(toTile);
						}
					}, 1);
				}else if (id == 2478)
					Runecrafting.craftEssence(player, 556, 1, 5, false, 11, 2, 22, 3, 34, 4, 44, 5, 55, 6, 66, 7, 77, 8,
							88, 9, 99, 10);
				else if (id == 2479)
					Runecrafting.craftEssence(player, 558, 2, 5.5, false, 14, 2, 28, 3, 42, 4, 56, 5, 70, 6, 84, 7, 98,
							8);
				else if (id == 2480)
					Runecrafting.craftEssence(player, 555, 5, 6, false, 19, 2, 38, 3, 57, 4, 76, 5, 95, 6);
				 else if (id == 2481)
						Runecrafting.craftEssence(player, 557, 9, 6.5, false, 26, 2, 52, 3, 78, 4);
				else if (id == 2482)
					Runecrafting.craftEssence(player, 554, 14, 7, false, 35, 2, 70, 3);
				else if (id == 2483)
					Runecrafting.craftEssence(player, 559, 20, 7.5, false, 46, 2, 92, 3);
				else if (id == 2484)
					Runecrafting.craftEssence(player, 564, 27, 8, true, 59, 2);
				else if (id == 2487)
					Runecrafting.craftEssence(player, 562, 35, 8.5, true, 74, 2);
				else if (id == 17010) 
					Runecrafting.craftEssence(player, 9075, 40, 8.7, true, 82, 2);
				 else if (id == 2486)
					Runecrafting.craftEssence(player, 561, 44, 9, true, 91, 2);
				else if (id == 2485)
					Runecrafting.craftEssence(player, 563, 54, 9.5, true);
				else if (id == 2488)
					Runecrafting.craftEssence(player, 560, 65, 10, true);
				else if (id == 30624 || id == 127978)
					Runecrafting.craftEssence(player, 565, 77, 10.5, true);
				else if (id == 127980)
					Runecrafting.craftEssence(player, 566, 90, 11, true);
				else if (id == 132492)
					Runecrafting.craftEssence(player, 51880, 95, /*8*/11.5, true);
				else if (id == 2452) {
					int hatId = player.getEquipment().getHatId();
					if (hatId == Runecrafting.AIR_TIARA || hatId == Runecrafting.OMNI_TIARA
							|| player.getInventory().containsItem(1438, 1))
						Runecrafting.enterAirAltar(player);
				} else if (id == 2455) {
					int hatId = player.getEquipment().getHatId();
					if (hatId == Runecrafting.EARTH_TIARA || hatId == Runecrafting.OMNI_TIARA
							|| player.getInventory().containsItem(1440, 1))
						Runecrafting.enterEarthAltar(player);
				} else if (id == 2456) {
					int hatId = player.getEquipment().getHatId();
					if (hatId == Runecrafting.FIRE_TIARA || hatId == Runecrafting.OMNI_TIARA
							|| player.getInventory().containsItem(1442, 1))
						Runecrafting.enterFireAltar(player);
				} else if (id == 2454) {
					int hatId = player.getEquipment().getHatId();
					if (hatId == Runecrafting.WATER_TIARA || hatId == Runecrafting.OMNI_TIARA
							|| player.getInventory().containsItem(1444, 1))
						Runecrafting.enterWaterAltar(player);
				} else if (id == 2457) {
					int hatId = player.getEquipment().getHatId();
					if (hatId == Runecrafting.BODY_TIARA || hatId == Runecrafting.OMNI_TIARA
							|| player.getInventory().containsItem(1446, 1))
						Runecrafting.enterBodyAltar(player);
				} else if (id == 2453) {
					int hatId = player.getEquipment().getHatId();
					if (hatId == Runecrafting.MIND_TIARA || hatId == Runecrafting.OMNI_TIARA
							|| player.getInventory().containsItem(1448, 1))
						Runecrafting.enterMindAltar(player);
				} else if (id == 47120) { // zaros altar
					// recharge if needed
					if (player.getPrayer().getPrayerpoints() < player.getSkills().getLevelForXp(Skills.PRAYER) * 10) {
						player.lock(12);
						player.setNextAnimation(new Animation(12563));
						player.getPrayer()
								.setPrayerpoints((int) ((player.getSkills().getLevelForXp(Skills.PRAYER) * 10) * 1.15));
						player.getPrayer().refreshPrayerPoints();
					}
					player.getDialogueManager().startDialogue("ZarosAltar");
				} else if (id == 19222 || id == 119222)
					Falconry.beginFalconry(player);
				else if (id == 5947) {
					player.useStairs(540, new WorldTile(3170, 9571, 0), 8, 9);
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							player.getControlerManager().startControler("UnderGroundDungeon", false, true);
							player.setNextAnimation(new Animation(-1));
						}
					}, 8);
					return;
				} else if (id == 6242 && x == 3353 && y == 2958) {
					player.useStairs(-1, new WorldTile(3354, 2958, 1), 0, 2);
				} else if (id == 6243 && x == 3353 && y == 2958) {
					player.useStairs(-1, new WorldTile(3353, 2961, 0), 0, 2);
				} else if (object.getId() == 6481) {
					player.useStairs(-1, new WorldTile(3233, 9313, 0), 0, 2);
				} else if (object.getId() == 6550 && x == 3233 && y == 9312) {
					player.useStairs(-1, new WorldTile(3233, 2887, 0), 0, 2);
				} else if (object.getId() == 6658) {
					player.useStairs(-1, new WorldTile(3226, 9542, 0), 0, 2);
					player.getControlerManager().startControler("UnderGroundDungeon", false, true);
				} else if (object.getId() == 6673) {
					player.useStairs(-1, new WorldTile(player.getX() == 3239 ? player.getX() + 3 : player.getX() - 3,
							player.getY(), player.getPlane()), 0, 2);
				} else if (id == 10137) {
					if (player.getX() == 2153 && player.getY() == 5108)
						player.useStairs(-1, new WorldTile(2150, 5109, 0), 0, 2);
					else if (player.getX() == 2130 && player.getY() == 5096)
						player.useStairs(-1, new WorldTile(2129, 5093, 0), 0, 2);
					else
						player.useStairs(-1, new WorldTile(player.getX() + 3, player.getY() - 1, 0), 0, 2);
				} else if (id == 10136) {
					if (player.getX() == 2150 && player.getY() == 5109)
						player.useStairs(-1, new WorldTile(2153, 5108, 1), 0, 2);
					else if (player.getX() == 2129 && player.getY() == 5093)
						player.useStairs(-1, new WorldTile(2130, 5096, 1), 0, 2);
					else
						player.useStairs(-1, new WorldTile(player.getX() - 3, player.getY() + 1, 1), 0, 2);
				} else if (id == 36786)
					player.getDialogueManager().startDialogue("Banker", 4907);
				else if (id == 42377 || id == 42378)
					player.getDialogueManager().startDialogue("Banker", 2759);
				else if (id == 42217 || id == 782 || id == 34752)
					player.getDialogueManager().startDialogue("Banker", 553);
				else if (id == 57437 || id == 12309 || id == 2693)
					player.getBank().openBank();
				else if (id == 9356)
					player.getDialogueManager().startDialogue("FightCavesEnter");
				else if (id == 68107)
					FightKiln.enterFightKiln(player, false);
				else if (id == 68223)
					FightPits.enterLobby(player, false);

				/*
				 * else if (id == 9294) { // tav dungeon if
				 * (player.getSkills().getLevel(Skills.AGILITY) < 80) { player.getPackets().
				 * sendGameMessage("You need an agility level of 80 to use this obstacle.",
				 * true); return; } int x = player.getX() == 2881 ? 2878 : 2881; int y =
				 * player.getY() == 9814 ? 9812 : 9814; WorldTasksManager.schedule(new
				 * WorldTask() {
				 * 
				 * @Override public void run() { player.setNextAnimation(new Animation(3067)); }
				 * }, 0); player.setNextForceMovement(new ForceMovement(new WorldTile(x, y, 0),
				 * 3, player.getX() == 2878 ? 1 : 3)); player.useStairs(-1, new WorldTile(x, y,
				 * 0), 3, 4); }
				 */ // ADDED BY ARYJAEY, IT DOESNT WORK PLS FIX

				else if (id == 9293) {
					if (player.getSkills().getLevel(Skills.AGILITY) < 70) {
						player.getPackets().sendGameMessage("You need an agility level of 70 to use this obstacle.",
								true);
						return;
					}
					int x = player.getX() == 2886 ? 2892 : 2886;
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							player.setNextAnimation(new Animation(10580));
						}
					}, 0);
					player.setNextForceMovement(
							new ForceMovement(new WorldTile(x, 9799, 0), 3, player.getX() == 2886 ? 1 : 3));
					player.useStairs(-1, new WorldTile(x, 9799, 0), 3, 4);
				} else if (id == 29370 && (object.getX() == 3150 || object.getX() == 3153) && object.getY() == 9906) { // edgeville
					// dungeon
					// cut
					if (player.getSkills().getLevel(Skills.AGILITY) < 53) {
						player.getPackets().sendGameMessage("You need an agility level of 53 to use this obstacle.");
						return;
					}
					final boolean running = player.getRun();
					player.setRunHidden(false);
					player.lock(8);
					player.addWalkSteps(x == 3150 ? 3155 : 3149, 9906, -1, false);
					player.getPackets().sendGameMessage("You pulled yourself through the pipes.", true);
					WorldTasksManager.schedule(new WorldTask() {
						boolean secondloop;

						@Override
						public void run() {
							if (!secondloop) {
								secondloop = true;
								player.getAppearence().setRenderEmote(295);
							} else {
								player.getAppearence().setRenderEmote(-1);
								player.setRunHidden(running);
								player.getSkills().addXp(Skills.AGILITY, 7);
								stop();
							}
						}
					}, 0, 5);
				}
				// wilderness godwar dungon
				else if (id == 20599 && object.getX() == 3011 && object.getY() == 3733)
					player.useStairs(-1, new WorldTile(3257, 10159, 3), 0, 1);
				else if (id == 126767 && object.getX() == 3257 && object.getY() == 10160)
					player.useStairs(-1, new WorldTile(3010, 3734, 0), 0, 1);
				else if (id == 126415 && x == 3245 && y == 10165)
					GodWars.passGiantBoulder2(player, object, true);
				else if (id == 126768 && x == 3258 && y == 10148)
					GodWars.passGiantBoulder2(player, object, false);
				else if (id == 126767 && object.getX() == 3258 && object.getY() == 10142)
					player.useStairs(-1, new WorldTile(3254, 10130, 0), 0, 1);
				else if (id == 126769 && object.getX() == 3254 && object.getY() == 10131)
					player.useStairs(-1, new WorldTile(3258, 10143, 3), 0, 1);
				else if (id == 126767 && object.getX() == 3241 && object.getY() == 10165)
					player.useStairs(-1, new WorldTile(3226, 10158, 0), 0, 1);
				else if (id == 126769 && object.getX() == 3227 && object.getY() == 10158)
					player.useStairs(-1, new WorldTile(3242, 10165, 3), 0, 1);
				// start forinthry dungeon
				else if (id == 18341 && object.getX() == 3036 && object.getY() == 10172)
					player.useStairs(-1, new WorldTile(3039, 3765, 0), 0, 1);
				else if (id == 20599 && object.getX() == 3038 && object.getY() == 3761)
					player.useStairs(-1, new WorldTile(3037, 10171, 0), 0, 1);
				else if (id == 18342 && object.getX() == 3075 && object.getY() == 10057)
					player.useStairs(-1, new WorldTile(3071, 3649, 0), 0, 1);
				else if (id == 20600 && object.getX() == 3072 && object.getY() == 3648)
					player.useStairs(-1, new WorldTile(3077, 10058, 0), 0, 1);
				// nomads requiem
				else if (id == 18425/* && !player.getQuestManager().completedQuest(Quests.NOMADS_REQUIEM)*/)
					if (!player.getQuestManager().completedQuest(Quests.NOMADS_REQUIEM))
						NomadsRequiem.enterNomadsRequiem(player, false);
					else
						player.getDialogueManager().startDialogue("NomadHMD");
				else if (id == 42219) {
					player.useStairs(-1, new WorldTile(1886, 3178, 0), 0, 1);
					if (player.getQuestManager().getQuestStage(Quests.NOMADS_REQUIEM) == -2) // for
						player.getQuestManager().setQuestStageAndRefresh(Quests.NOMADS_REQUIEM, 0);
				} else if (id == 8689)
					player.getActionManager().setAction(new CowMilkingAction());
				else if (id == 42220)
					player.useStairs(-1, new WorldTile(3082, 3475, 0), 0, 1);
				else if (id == 67043)
					player.useStairs(-1, new WorldTile(2219, 4532, 0), 0, 1);
				// start falador mininig
				else if (id == 30942 && object.getX() == 3019 && object.getY() == 3450)
					player.useStairs(828, new WorldTile(3020, 9850, 0), 1, 2);
				else if (id == 6226 && object.getX() == 3019 && object.getY() == 9850)
					player.useStairs(833, new WorldTile(3018, 3450, 0), 1, 2);
				else if (id == 31002 /* && player.getQuestManager().completedQuest(Quests.PERIL_OF_ICE_MONTAINS) */)
					player.useStairs(833, new WorldTile(2998, 3452, 0), 1, 2);
				else if (id == 31012 /* && player.getQuestManager().completedQuest(Quests.PERIL_OF_ICE_MONTAINS) */)
					player.useStairs(828, new WorldTile(2996, 9845, 0), 1, 2);
				else if (id == 30943 && object.getX() == 3059 && object.getY() == 9776)
					player.useStairs(-1, new WorldTile(3061, 3376, 0), 0, 1);
				else if (id == 30944 && object.getX() == 3059 && object.getY() == 3376)
					player.useStairs(-1, new WorldTile(3058, 9776, 0), 0, 1);
				else if (id == 2112 && object.getX() == 3046 && object.getY() == 9756) {
					if (player.getSkills().getLevelForXp(Skills.MINING) < 60) {
						player.getDialogueManager().startDialogue("SimpleNPCMessage",
								MiningGuildDwarf.getClosestDwarfID(player),
								"Sorry, but you need level 60 Mining to go in there.");
						return;
					}
					WorldObject openedDoor = new WorldObject(object.getId(), object.getType(), object.getRotation() - 1,
							object.getX(), object.getY() + 1, object.getPlane());
					if (World.removeObjectTemporary(object, 1200)) {
						World.spawnObjectTemporary(openedDoor, 1200);
						player.lock(2);
						player.stopAll();
						player.addWalkSteps(3046, player.getY() > object.getY() ? object.getY() : object.getY() + 1, -1,
								false);
					}
				} else if (id == 2113) {
					if (player.getSkills().getLevelForXp(Skills.MINING) < 60) {
						player.getDialogueManager().startDialogue("SimpleNPCMessage",
								MiningGuildDwarf.getClosestDwarfID(player),
								"Sorry, but you need level 60 Mining to go in there.");
						return;
					}
					player.useStairs(-1, new WorldTile(3021, 9739, 0), 0, 1);
				} else if (id == 6226 && object.getX() == 3019 && object.getY() == 9740)
					player.useStairs(828, new WorldTile(3019, 3341, 0), 1, 2);
				else if (id == 6226 && object.getX() == 3019 && object.getY() == 9738)
					player.useStairs(828, new WorldTile(3019, 3337, 0), 1, 2);
				else if (id == 6226 && object.getX() == 3018 && object.getY() == 9739)
					player.useStairs(828, new WorldTile(3017, 3339, 0), 1, 2);
				else if (id == 6226 && object.getX() == 3020 && object.getY() == 9739)
					player.useStairs(828, new WorldTile(3021, 3339, 0), 1, 2);
				else if (id == 30963)
					player.getBank().openBank();
				else if (id == 6045)
					player.getPackets().sendGameMessage("You search the cart but find nothing.");
				else if (id == 5906) {
					if (player.getSkills().getLevel(Skills.AGILITY) < 42) {
						player.getPackets().sendGameMessage("You need an agility level of 42 to use this obstacle.");
						return;
					}
					player.lock();
					WorldTasksManager.schedule(new WorldTask() {
						int count = 0;

						@Override
						public void run() {
							if (count == 0) {
								player.setNextAnimation(new Animation(2594));
								WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -2 : +2),
										object.getY(), 0);
								player.setNextForceMovement(new ForceMovement(tile, 4, Utils
										.getMoveDirection(tile.getX() - player.getX(), tile.getY() - player.getY())));
							} else if (count == 2) {
								WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -2 : +2),
										object.getY(), 0);
								player.setNextWorldTile(tile);
							} else if (count == 5) {
								player.setNextAnimation(new Animation(2590));
								WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -5 : +5),
										object.getY(), 0);
								player.setNextForceMovement(new ForceMovement(tile, 4, Utils
										.getMoveDirection(tile.getX() - player.getX(), tile.getY() - player.getY())));
							} else if (count == 7) {
								WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -5 : +5),
										object.getY(), 0);
								player.setNextWorldTile(tile);
							} else if (count == 10) {
								player.setNextAnimation(new Animation(2595));
								WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -6 : +6),
										object.getY(), 0);
								player.setNextForceMovement(new ForceMovement(tile, 4, Utils
										.getMoveDirection(tile.getX() - player.getX(), tile.getY() - player.getY())));
							} else if (count == 12) {
								WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -6 : +6),
										object.getY(), 0);
								player.setNextWorldTile(tile);
							} else if (count == 14) {
								stop();
								player.unlock();
							}
							count++;
						}

					}, 0, 0);
					// BarbarianOutpostAgility start
				} else if (id == 20210)
					BarbarianOutpostAgility.enterObstaclePipe(player, object);
				else if (id == 43526)
					BarbarianOutpostAgility.swingOnRopeSwing(player, object);
				else if (id == 43595 && x == 2550 && y == 3546)
					BarbarianOutpostAgility.walkAcrossLogBalance(player, object);
				else if (id == 20211 && x == 2538 && y == 3545)
					BarbarianOutpostAgility.climbObstacleNet(player, object);
				else if (id == 2302 && x == 2535 && y == 3547)
					BarbarianOutpostAgility.walkAcrossBalancingLedge(player, object);
				else if (id == 1948)
					BarbarianOutpostAgility.climbOverCrumblingWall(player, object);
				else if (id == 43533)
					BarbarianOutpostAgility.runUpWall(player, object);
				else if (id == 43597)
					BarbarianOutpostAgility.climbUpWall(player, object);
				else if (id == 43587)
					BarbarianOutpostAgility.fireSpringDevice(player, object);
				else if (id == 43527)
					BarbarianOutpostAgility.crossBalanceBeam(player, object);
				else if (id == 43531)
					BarbarianOutpostAgility.jumpOverGap(player, object);
				else if (id == 43532)
					BarbarianOutpostAgility.slideDownRoof(player, object);
				// sawmill
				else if (id == 46307 && x == 3311 && y == 3491)
					Sawmill.enter(player, object);
				// Wilderness course start
				else if (id == 64698)
					WildernessAgility.walkAcrossLogBalance(player, object);
				else if (id == 64699)
					WildernessAgility.jumpSteppingStones(player, object);
				else if (id == 65362 && x == 3004 && y == 3938)
					WildernessAgility.enterWildernessPipe(player, object.getX(), object.getY());
				else if (id == 64696)
					WildernessAgility.swingOnRopeSwing(player, object);
				else if (id == 65365)
					WildernessAgility.enterWildernessCourse(player);
				else if (id == 65367)
					WildernessAgility.exitWildernessCourse(player);
				else if (object.getId() == 13523)
					ItemTransportation.transportationDialogue(player, new Item(1712, 1), false, false);
				// rock living caverns
				else if (id == 45077) {
					player.lock();
					if (player.getX() != object.getX() || player.getY() != object.getY())
						player.addWalkSteps(object.getX(), object.getY(), -1, false);
					WorldTasksManager.schedule(new WorldTask() {

						private int count;

						@Override
						public void run() {
							if (count == 0) {
								player.setNextFaceWorldTile(new WorldTile(object.getX() - 1, object.getY(), 0));
								player.setNextAnimation(new Animation(12216));
								player.unlock();
							} else if (count == 2) {
								player.setNextWorldTile(new WorldTile(3651, 5122, 0));
								player.setNextFaceWorldTile(new WorldTile(3651, 5121, 0));
								player.setNextAnimation(new Animation(12217));
							} else if (count == 3) {
								// TODO find emote
								// player.getPackets().sendObjectAnimation(new
								// WorldObject(45078, 0, 3, 3651, 5123, 0), new
								// Animation(12220));
							} else if (count == 5) {
								player.unlock();
								stop();
							}
							count++;
						}

					}, 1, 0);
				} else if (id == 45076)
					player.getActionManager().setAction(new Mining(object, RockDefinitions.LRC_Gold_Ore));
				else if (id == 5999)
					player.getActionManager().setAction(new Mining(object, RockDefinitions.LRC_Coal_Ore));
				else if (id == 45078)
					player.useStairs(2413, new WorldTile(3012, 9832, 0), 2, 2);
				else if (id == 45079)
					player.getBank().openDepositBox();
				// champion guild
				else if (id == 24357 && object.getX() == 3188 && object.getY() == 3355)
					player.useStairs(-1, new WorldTile(3189, 3354, 1), 0, 1);
				else if (id == 24359 && object.getX() == 3188 && object.getY() == 3355)
					player.useStairs(-1, new WorldTile(3189, 3358, 0), 0, 1);
				else if (id == 1805 && object.getX() == 3191 && object.getY() == 3363) {
					if (World.isSpawnedObject(object))
						return;
					WorldObject openedDoor = new WorldObject(object.getId(), object.getType(), object.getRotation() - 1,
							object.getX(), object.getY(), object.getPlane());
					World.spawnObjectTemporary(openedDoor, 1200);
					player.lock(2);
					player.stopAll();
					player.addWalkSteps(3191, player.getY() >= object.getY() ? object.getY() - 1 : object.getY(), -1,
							false);
					if (player.getY() >= object.getY())
						player.getDialogueManager().startDialogue("SimpleNPCMessage", 198,
								"Greetings bolt adventurer. Welcome to the guild of", "Champions.");
				} else if ((id == 128851 || id == 128852) && (object.getX() == 1562 || object.getX() == 1657)) {
					if (World.isSpawnedObject(object))
						return;
					if (player.getSkills().getLevel(Skills.WOODCUTTING) < 60) {
						player.getDialogueManager().startDialogue("SimpleNPCMessage", 27236,
								"Come back once you are level 60 woodcutting.");
						return;
					}
					WorldObject openedDoor = new WorldObject(object.getId(), object.getType(), object.getRotation() - 1,
							object.getX(), object.getY(), object.getPlane());
					World.spawnObjectTemporary(openedDoor, 1200);
					player.lock(2);
					player.stopAll();

					player.addWalkSteps(player.getX() > object.getX() ? object.getX() : object.getX() + 1,
							object.getY(), -1, false);
					if (!(player.getX() > object.getX() ^ object.getX() == 1657))
						player.getDialogueManager().startDialogue("SimpleNPCMessage", 27236,
								"Greetings bolt adventurer. Welcome to the guild of", "Woodcutting.");
				}
				//
				else if (id == 2647 && object.getX() == 2933 && object.getY() == 3289) { // craft guild
					if (World.isSpawnedObject(object))
						return;
					if (player.getSkills().getLevelForXp(Skills.CRAFTING) < 40) {
						player.getDialogueManager().startDialogue("SimpleNPCMessage", 198,
								"Come back once you are level 40 crafting.");
						return;
					}

					WorldObject openedDoor = new WorldObject(object.getId(), object.getType(), object.getRotation() - 1,
							object.getX(), object.getY(), object.getPlane());
					World.spawnObjectTemporary(openedDoor, 1200);
					player.lock(2);
					player.stopAll();
					player.addWalkSteps(2933, player.getY() >= object.getY() ? object.getY() - 1 : object.getY(), -1,
							false);
					if (player.getY() >= object.getY())
						player.getDialogueManager().startDialogue("SimpleNPCMessage", 805,
								"Greetings accomplished adventurer. Welcome to the guild of", "Crafters.");
				} else if (id == 2514 && object.getX() == 2658 && object.getY() == 3438) { // ranging guild
					if (World.isSpawnedObject(object))
						return;
					if (player.getSkills().getLevelForXp(Skills.RANGE) < 40) {
						player.getDialogueManager().startDialogue("SimpleNPCMessage", 198,
								"Come back once you are level 40 ranging.");
						return;
					}

					// WorldObject openedDoor = new WorldObject(object.getId(), object.getType(),
					// object.getRotation() - 1, object.getX(), object.getY(), object.getPlane());
					// World.spawnObjectTemporary(openedDoor, 1200);
					player.lock(3);
					player.stopAll();
					player.addWalkSteps(player.getX() >= object.getX() ? object.getX() - 1 : object.getX() + 1,
							object.getY(), -1, false);
					if (player.getX() < object.getX())
						player.getDialogueManager().startDialogue("SimpleNPCMessage", 805,
								"Greetings accomplished adventurer. Welcome to the guild of", "Ranging.");
				}
				// wc guild
				else if (id == 128857 && x == 1575)
					player.useStairs(828, new WorldTile(1574, object.getY(), 1), 1, 2);
				else if (id == 128858 && x == 1575)
					player.useStairs(833, new WorldTile(1575, object.getY(), 0), 1, 2);
				else if (id == 128857 && x == 1566)
					player.useStairs(828, new WorldTile(1567, object.getY(), 1), 1, 2);
				else if (id == 128858 && x == 1566)
					player.useStairs(833, new WorldTile(1566, object.getY(), 0), 1, 2);
				else if (id == 129681 || id == 129682)
					player.useStairs(828, new WorldTile(player.getX(), player.getY(), id == 129682 ? 1 : 2), 1, 2);
				else if (id == 128855 && x == 1601 && y == 3506)
					player.useStairs(-1, new WorldTile(1596, 9900, 0), 0, 1);
				else if (id == 128856 && x == 1597 && y == 9900)
					player.useStairs(-1, new WorldTile(1606, 3508, 0), 0, 1);
				else if (id == 126720 || id == 126721) {
					player.lock(1);
					player.addWalkSteps(object.getX(), object.getY(), -1, false);
					// lighthouse dungeon
				} else if (id == 4383)
					player.useStairs(833, new WorldTile(2519, 9993, 1), 1, 2);
				else if (id == 4412)
					player.useStairs(828, new WorldTile(2510, 3644, 0), 1, 2);
				else if (id == 4546) {
					if (player.getY() <= 10002) {
						player.getPackets().sendGameMessage("This door cannot be opened from this side.");
						return;
					}
					player.lock(2);
					player.addWalkSteps(2513, 10002, 1, false);
				} else if (id == 4545) {
					if (player.getY() >= 10003) {
						player.getPackets().sendGameMessage("This door cannot be opened from this side.");
						return;
					}
					player.lock(2);
					player.addWalkSteps(2516, 10003, 1, false);
				} else if (id == 4544)
					player.getInterfaceManager().sendInterface(142);
				else if (id == 29355 && object.getX() == 3209 && object.getY() == 9616)
					player.useStairs(828, new WorldTile(3210, 3216, 0), 1, 2);
				else if (id == 36687 && object.getX() == 3209 && object.getY() == 3216)
					player.useStairs(833, new WorldTile(3208, 9616, 0), 1, 2);
				else if (id == 4485)
					player.useStairs(828, new WorldTile(2515, 10007, 0), 1, 2);
				else if (id == 4413)
					player.useStairs(828, new WorldTile(2515, 10005, 1), 1, 2);
				// start of varrock dungeon
				else if (id == 29355 && object.getX() == 3230 && object.getY() == 9904) // varrock
					// dungeon
					// climb
					// to
					// bear
					player.useStairs(828, new WorldTile(3229, 3503, 0), 1, 2);
				else if (id == 24264)
					player.useStairs(833, new WorldTile(3229, 9904, 0), 1, 2);
				else if (id == 24366)
					player.useStairs(828, new WorldTile(3237, 3459, 0), 1, 2);
				else if (id == 882 && object.getX() == 3237 && object.getY() == 3458)
					player.useStairs(833, new WorldTile(3237, 9858, 0), 1, 2);
				else if (id == 29355 && object.getX() == 3097 && object.getY() == 9868) // edge
					// dungeon
					// climb
					player.useStairs(828, new WorldTile(3096, 3468, 0), 1, 2);
				else if (id == 101579 || (id == 26934 && x == 3097 && y == 3468))
					player.useStairs(833, new WorldTile(3096, 9868, 0), 1, 2);
				else if (id == 74864)
					player.useStairs(-1, new WorldTile(2885, 3395, 0), 1, 2);
				else if (id == 66991)
					player.useStairs(-1, new WorldTile(2885, 9795, 0), 1, 2);
				else if (id == 29355 && object.getX() == 3088 && object.getY() == 9971)
					player.useStairs(828, new WorldTile(3087, 3571, 0), 1, 2);
				else if (id == 65453)
					player.useStairs(833, new WorldTile(3089, 9971, 0), 1, 2);
				else if (id == 12389 && object.getX() == 3116 && object.getY() == 3452)
					player.useStairs(833, new WorldTile(3117, 9852, 0), 1, 2);
				else if (id == 29355 && object.getX() == 3116 && object.getY() == 9852)
					player.useStairs(833, new WorldTile(3115, 3452, 0), 1, 2);
				else if (id == 2348) {
					if (x == 3092 && y == 3281)
						player.useStairs(-1, new WorldTile(3091, 3281, 0), 1, 2);
					else if (x == 3098 && y == 3281)
						player.useStairs(-1, new WorldTile(3100, 3281, 0), 1, 2);
					else if (x == 3100 && y == 3266)
						player.useStairs(-1, new WorldTile(3099, 3266, 0), 1, 2);
					else if (x == 3084 && y == 3262)
						player.useStairs(-1, new WorldTile(3086, 3262, 0), 1, 2);
					else if (x == 3091 && y == 3251)
						player.useStairs(-1, new WorldTile(3090, 3251, 0), 1, 2);
					else if (x == 3100 && y == 3255)
						player.useStairs(-1, new WorldTile(3099, 3255, 0), 1, 2);
				} else if (id == 2347) {
					if (x == 3092 && y == 3281)
						player.useStairs(-1, new WorldTile(3094, 3281, 1), 1, 2);
					else if (x == 3098 && y == 3281)
						player.useStairs(-1, new WorldTile(3097, 3281, 1), 1, 2);
					else if (x == 3100 && y == 3266)
						player.useStairs(-1, new WorldTile(3102, 3266, 1), 1, 2);
					else if (x == 3084 && y == 3262)
						player.useStairs(-1, new WorldTile(3083, 3262, 1), 1, 2);
					else if (x == 3091 && y == 3251)
						player.useStairs(-1, new WorldTile(3093, 3251, 1), 1, 2);
					else if (x == 3100 && y == 3255)
						player.useStairs(-1, new WorldTile(3102, 3255, 1), 1, 2);
				} else if (id == 2332 && y == 3049) {
					if (!Agility.hasLevel(player, 20))
						return;
					final boolean running = player.getRun();
					player.setRunHidden(false);
					player.lock(5);
					player.addWalkSteps(object.getX() + (object.getX() == 2907 ? 3 : -3), object.getY(), -1, false);
					player.getPackets().sendGameMessage("You walk carefully across the slippery log...", true);
					WorldTasksManager.schedule(new WorldTask() {
						boolean secondloop;

						@Override
						public void run() {
							if (!secondloop) {
								secondloop = true;
								player.getAppearence().setRenderEmote(155);
							} else {
								player.getAppearence().setRenderEmote(-1);
								player.setRunHidden(running);
								player.getSkills().addXp(Skills.AGILITY, 7.5);
								player.getPackets().sendGameMessage("... and make it safely to the other side.", true);
								stop();
							}
						}
					}, 0, 3);
				} else if (id == 2296 && y == 3477) { // log
					if (!Agility.hasLevel(player, 20))
						return;
					final boolean running = player.getRun();
					player.setRunHidden(false);
					player.lock(5);
					player.addWalkSteps(object.getX() + (object.getRotation() == 3 ? 4 : -4), object.getY(), -1, false);
					player.getPackets().sendGameMessage("You walk carefully across the slippery log...", true);
					WorldTasksManager.schedule(new WorldTask() {
						boolean secondloop;

						@Override
						public void run() {
							if (!secondloop) {
								secondloop = true;
								player.getAppearence().setRenderEmote(155);
							} else {
								player.getAppearence().setRenderEmote(-1);
								player.setRunHidden(running);
								player.getSkills().addXp(Skills.AGILITY, 7.5);
								player.getPackets().sendGameMessage("... and make it safely to the other side.", true);
								stop();
							}
						}
					}, 0, 4);

				} else if (id == 69526)
					GnomeAgility.walkGnomeLog(player);
				else if (id == 69383)
					GnomeAgility.climbGnomeObstacleNet(player);
				else if (id == 69508)
					GnomeAgility.climbUpGnomeTreeBranch(player);
				else if (id == 2312)
					GnomeAgility.walkGnomeRope(player);
				else if (id == 4059)
					GnomeAgility.walkBackGnomeRope(player);
				else if (id == 69507)
					GnomeAgility.climbDownGnomeTreeBranch(player);
				else if (id == 69384)
					GnomeAgility.climbGnomeObstacleNet2(player, object);
				else if (id == 69377 || id == 69378)
					GnomeAgility.enterGnomePipe(player, object.getX(), object.getY());
				else if (id == 69389)
					GnomeAgility.jumpDown(player, object);
				else if (id == 69506)
					GnomeAgility.climbUpTree(player);
				else if (Wilderness.isDitch(id)) {// wild ditch
					player.stopAll();
					player.lock(4);
					player.setNextAnimation(new Animation(6132));
					final WorldTile toTile = new WorldTile(
							object.getRotation() == 3 || object.getRotation() == 1 ? object.getX() - 1 : player.getX(),
							object.getRotation() == 0 || object.getRotation() == 2 ? object.getY() + 2 : player.getY(),
							object.getPlane());
					player.setNextForceMovement(new ForceMovement(new WorldTile(player), 1, toTile, 2,
							object.getRotation() == 0 || object.getRotation() == 2 ? ForceMovement.NORTH
									: ForceMovement.WEST));
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							player.setNextWorldTile(toTile);
							player.faceObject(object);
							player.getControlerManager().startControler("Wilderness");
							player.resetReceivedDamage();
						}
					}, 2);
				} else if (id == 42611) {// Magic Portal
					player.getDialogueManager().startDialogue("MagicPortal");
				} else if (id == 27254) {// Edgeville portal
					player.getPackets().sendGameMessage("You enter the portal...");
					player.useStairs(10584, new WorldTile(3087, 3488, 0), 2, 3, "..and are transported to Edgeville.");
					player.addWalkSteps(1598, 4506, -1, false);
				} else if (id == 12202) {// mole entrance
					if (!player.getInventory().containsItemToolBelt(952)) {
						player.getPackets().sendGameMessage("You need a spade to dig this.");
						return;
					}
					if (player.getX() != object.getX() || player.getY() != object.getY()) {
						player.lock();
						player.addWalkSteps(object.getX(), object.getY());
						WorldTasksManager.schedule(new WorldTask() {
							@Override
							public void run() {
								InventoryOptionsHandler.dig(player);
							}

						}, 1);
					} else
						InventoryOptionsHandler.dig(player);
				} else if (id == 12230 && object.getX() == 1752 && object.getY() == 5136)// mole
					// exit
					player.setNextWorldTile(new WorldTile(2986, 3316, 0));
				else if (id == 66115 || id == 66116)
					InventoryOptionsHandler.dig(player);
				else if (id == 15522) {// portal sign
					if (player.withinDistance(new WorldTile(1598, 4504, 0), 1)) {// PORTAL
						// 1
						player.getInterfaceManager().sendInterface(327);
						player.getPackets().sendIComponentText(327, 13, "Edgeville");
						player.getPackets().sendIComponentText(327, 14, "This portal will take you to edgeville. There "
								+ "you can multi pk once past the wilderness ditch.");
					}
					if (player.withinDistance(new WorldTile(1598, 4508, 0), 1)) {// PORTAL
						// 2
						player.getInterfaceManager().sendInterface(327);
						player.getPackets().sendIComponentText(327, 13, "Mage Bank");
						player.getPackets().sendIComponentText(327, 14, "This portal will take you to the mage bank. "
								+ "The mage bank is a 1v1 deep wilderness area.");
					}
					if (player.withinDistance(new WorldTile(1598, 4513, 0), 1)) {// PORTAL
						// 3
						player.getInterfaceManager().sendInterface(327);
						player.getPackets().sendIComponentText(327, 13, "Magic's Portal");
						player.getPackets().sendIComponentText(327, 14,
								"This portal will allow you to teleport to areas that "
										+ "will allow you to change your magic spell book.");
					}
				} else if (id == 38811 || id == 37929) {// corp beast
					if (object.getX() == 2971 && object.getY() == 4382)
						player.getInterfaceManager().sendInterface(650);
					else if (object.getX() == 2918 && object.getY() == 4382) {
						player.stopAll();
						player.setNextWorldTile(
								new WorldTile(player.getX() == 2921 ? 2917 : 2921, player.getY(), player.getPlane()));
					}
				} else if (id == 37928 && object.getX() == 2883 && object.getY() == 4370) {
					player.stopAll();
					player.setNextWorldTile(new WorldTile(3214, 3782, 0));
					player.getControlerManager().startControler("Wilderness");
				} else if ((id == 29319 || id == 29320) && object.getY() == 9918) {
					player.lock(2);
					player.addWalkSteps(object.getX(), object.getY(), 1, false);
					player.getControlerManager().startControler("Wilderness");
				} else if (id == 38815 && object.getX() == 3209 && object.getY() == 3780 && object.getPlane() == 0) {
					if (player.getSkills().getLevelForXp(Skills.WOODCUTTING) < 37
							|| player.getSkills().getLevelForXp(Skills.MINING) < 45
							|| player.getSkills().getLevelForXp(Skills.SUMMONING) < 23
							|| player.getSkills().getLevelForXp(Skills.FIREMAKING) < 47
							|| player.getSkills().getLevelForXp(Skills.PRAYER) < 55) {
						player.getPackets().sendGameMessage(
								"You need 23 Summoning, 37 Woodcutting, 45 Mining, 47 Firemaking and 55 Prayer to enter this dungeon.");
						return;
					}
					player.stopAll();
					player.setNextWorldTile(new WorldTile(2885, 4372, 2));
					player.getControlerManager().forceStop();
					// TODO all reqs, skills not added
				} else if (id == 48803 && player.isKalphiteLairSetted()) {
					BossInstanceHandler.enterInstance(player, Boss.Kalphite_Queen);
					//player.setNextWorldTile(new WorldTile(3508, 9494, 0));
				} else if (id == 48802) {
					if (player.isKalphiteLairEntranceSetted())
						player.setNextWorldTile(new WorldTile(3420, 9510, 0));
					else if (player.getInventory().containsItem(954, 1)) {
						player.getInventory().deleteItem(954, 1);
						player.setKalphiteLairEntrance();
					} else
						player.getPackets().sendGameMessage("You need a rope to climb down.");
				} else if (id == 3829) {
					if (object.getX() == 3419 && object.getY() == 9510) {
						player.useStairs(828, new WorldTile(3226, 3108, 0), 1, 2);
					}
				} else if (id == 3832) {
					if (object.getX() == 3508 && object.getY() == 9494) {
						player.useStairs(828, new WorldTile(3446, 9496, 0), 1, 2);
					}
				} else if (id == 9369) {
					player.getControlerManager().startControler("FightPits");
				} else if (id == 54019 || id == 54020 || id == 55301)
					player.getPackets().sendGameMessage("This feature is disabled due to rework.");
				else if (id == 1817)// kbd lever
					Magic.pushLeverTeleport(player, new WorldTile(3048, 3519, 0), 827, "You activate the artefact...",
							"and teleport out of the dragon's lair.");
				else if (id == 77834)
					player.getDialogueManager().startDialogue("KBDArtifact");
				else if (id == 1816 && object.getX() == 3067 && object.getY() == 10252) { // kbd
					// out
					// lever
					Magic.pushLeverTeleport(player, new WorldTile(2273, 4681, 0));
				} else if (id == 32015 && object.getX() == 3069 && object.getY() == 10256) { // kbd
					// stairs
					player.useStairs(828, new WorldTile(3017, 3848, 0), 1, 2);
					player.getControlerManager().startControler("Wilderness");
				} else if (id == 1765 && object.getX() == 3017 && object.getY() == 3849) { // kbd
					// out
					// stairs
					player.stopAll();
					player.setNextWorldTile(new WorldTile(3069, 10255, 0));
				//	player.getControlerManager().forceStop();
				} else if (id == 14315) {
					/*if (Lander.canEnter(player, 0))
						return;*/
					player.getDialogueManager().startDialogue("PestControlEnterD", 0);
				} else if (id == 25631) {
					/*if (Lander.canEnter(player, 1))
						return;*/
					player.getDialogueManager().startDialogue("PestControlEnterD", 1);
				} else if (id == 25632) {
					/*if (Lander.canEnter(player, 2))
						return;*/
					player.getDialogueManager().startDialogue("PestControlEnterD", 2);
				} else if (id == 5959) {
					Magic.pushLeverTeleport(player, new WorldTile(2539, 4712, 0));
				} else if (id == 5960) {
					Magic.pushLeverTeleport(player, new WorldTile(3089, 3957, 0));
				} else if (id == 1814) {
					Magic.pushLeverTeleport(player, new WorldTile(3155, 3923, 0));
				} else if (id == 1815) {
					Magic.pushLeverTeleport(player, new WorldTile(2561, 3311, 0));
				} else if (id == 62675)
					player.getCutscenesManager().play("DTPreview");
				else if (id == 2322 || id == 2323) {
					if (!Agility.hasLevel(player, 10))
						return;
					if ((id == 2322 && player.getX() < 2709) || (id == 2323 && player.getX() > 2705))
						return;
					player.lock(4);
					player.setNextAnimation(new Animation(751));
					World.sendObjectAnimation(player, object, new Animation(497));
					final WorldTile toTile = new WorldTile(id == 2323 ? 2709 : 2704, y, object.getPlane());
					player.setNextForceMovement(new ForceMovement(player, 1, toTile, 3,
							id == 2323 ? ForceMovement.EAST : ForceMovement.WEST));
					player.getSkills().addXp(Skills.AGILITY, 22);
					player.getPackets().sendGameMessage("You skilfully swing across.", true);
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							player.setNextWorldTile(toTile);
						}

					}, 1);
				} else if (id == 62681)
					player.getDominionTower().viewScoreBoard();
				else if (id == 62678 || id == 62679)
					player.getDominionTower().openModes();
				else if (id == 62688)
					player.getDialogueManager().startDialogue("DTClaimRewards");
				else if (id == 62677)
					player.getDominionTower().talkToFace();
				else if (id == 62680)
					player.getDominionTower().openBankChest();
				else if (id == 48797)
					player.useStairs(-1, new WorldTile(3877, 5526, 1), 0, 1);
				else if (id == 48798)
					player.useStairs(-1, new WorldTile(3246, 3198, 0), 0, 1);
				else if (id == 48678 && x == 3858 && y == 5533)
					player.useStairs(-1, new WorldTile(3861, 5533, 0), 0, 1);
				else if (id == 48678 && x == 3858 && y == 5543)
					player.useStairs(-1, new WorldTile(3861, 5543, 0), 0, 1);
				else if (id == 48678 && x == 3858 && y == 5533)
					player.useStairs(-1, new WorldTile(3861, 5533, 0), 0, 1);
				else if (id == 48677 && x == 3858 && y == 5543)
					player.useStairs(-1, new WorldTile(3856, 5543, 1), 0, 1);
				else if (id == 48677 && x == 3858 && y == 5533)
					player.useStairs(-1, new WorldTile(3856, 5533, 1), 0, 1);
				else if (id == 48679)
					player.useStairs(-1, new WorldTile(3875, 5527, 1), 0, 1);
				else if (id == 48688)
					player.useStairs(-1, new WorldTile(3972, 5565, 0), 0, 1);
				else if (id == 48683)
					player.useStairs(-1, new WorldTile(3868, 5524, 0), 0, 1);
				else if (id == 48682)
					player.useStairs(-1, new WorldTile(3869, 5524, 0), 0, 1);
				else if (id == 62676) { // dominion exit
					player.useStairs(-1, new WorldTile(3374, 3093, 0), 0, 1);
				} else if (id == 62674) { // dominion entrance
					player.useStairs(-1, new WorldTile(3744, 6405, 0), 0, 1);
				} else if (id == 11993 && x == 3107 && y == 3162 && object.getPlane() == 0) {
					player.addWalkSteps(player.getY() >= 3163 ? 3106 : 3107, player.getY() >= 3163 ? 3161 : 3163, -1,
							false);
				}else if (id == 2147 && x == 3104 && y == 3162)
						player.useStairs(827, new WorldTile(3104, 9576, 0), 1, 2);
				else if (id == 32015 && x == 3103 && y == 9576)  
						player.useStairs(828, new WorldTile(3104, 3161, 0), 1, 2);
				else if (id == 3192) {
					player.getPackets().sendGameMessage("This feature is disabled due to rework.");
				} else if (id == 65349) {
					player.useStairs(-1, new WorldTile(3044, 10325, 0), 0, 1);
				} else if (id == 32048 && object.getX() == 3043 && object.getY() == 10328) {
					player.useStairs(-1, new WorldTile(3045, 3927, 0), 0, 1);
				} else if (id == 24168 || id == 61190 || id == 61191 || id == 61192 || id == 61193) {
					if (objectDef.containsOption(0, "Chop down"))
						player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.NORMAL));
				} else if (id == 20573)
					player.getControlerManager().startControler("RefugeOfFear");
				else if (id == 11739)
					player.useStairs(828, new WorldTile(3050, 3354, 2), 1, 2);
				// crucible
				else if (id == 67050)
					player.useStairs(-1, new WorldTile(3359, 6110, 0), 0, 1);
				else if (id == 14909) {
					player.setNextAnimation(new Animation(832));
					player.getInventory().addItem(1265, 1);
					player.lock(2);
					World.removeObjectTemporary(object, 60000);
				} else if (id == 28742 && x == 2328 && y == 3645)
					player.useStairs(-1, new WorldTile(2209, 5348, 0), 0, 1);
				else if (id == 28714 && x == 2209 && y == 5349)
					player.useStairs(-1, new WorldTile(2329, 3645, 0), 0, 1);
				else if (id == 14911) {
					player.setNextAnimation(new Animation(832));
					player.getInventory().addItem(1351, 1);
					player.lock(2);
					World.removeObjectTemporary(object, 60000);
				} else if (id == 9662) {
					player.setNextAnimation(new Animation(832));
					player.getInventory().addItem(952, 1);
					player.lock(2);
					World.removeObjectTemporary(object, 60000);
				} else if (id == 10375) {
					player.setNextAnimation(new Animation(832));
					player.getInventory().addItem(5341, 1);
					player.getInventory().addItem(952, 1);
					player.lock(2);
					World.removeObjectTemporary(object, 60000);
				} else if (id == 67053)
					player.useStairs(-1, new WorldTile(3120, 3519, 0), 0, 1);
				else if (id == 20602) {// gamers groto
					if (player.getControlerManager().getControler() != null) {
						player.getPackets().sendGameMessage("You can't enter this dungeon during an activity!");
						return;
					}
					player.useStairs(-1, new WorldTile(2954, 9675, 0), 0, 1);
				} else if (id == 20604) // gamers groto
					player.useStairs(-1, new WorldTile(3018, 3404, 0), 0, 1);
				else if (id == 4577) {
					player.lock(2);
					player.addWalkSteps(2509, player.getY() == 3635 ? 3636 : 3635, 1, false);
				} else if (id == 67051)
					player.getDialogueManager().startDialogue("Marv", false);
				else if (id == 67052)
					Crucible.enterCrucibleEntrance(player);
				else if (id == 6) {
					if (OwnedObjectManager.isPlayerObject(player, object))
						DwarfMultiCannon.fire(player);
					else
						player.getPackets().sendGameMessage("This is not your cannon!");

				} else if (id == 9)
					DwarfMultiCannon.pickupCannon(player, 3, object);
				else if (id == 8)
					DwarfMultiCannon.pickupCannon(player, 2, object);
				else if (id == 7)
					DwarfMultiCannon.pickupCannon(player, 1, object);
				else if (id == 172) {
					if (player.getInventory().containsItem(989, 1)) {
						CrystalChest.openChest(player, object);
						return;
					}
					player.getPackets().sendGameMessage("This chest is securely locked shut. You need a crystal key to open it!");
				} else if (id == 129495 || id == 16944)
					FairyRings.openRingInterface(player, object, false);
				else if (id == 2330)
					player.getActionManager().setAction(new Mining(object, RockDefinitions.RedSandStone));
				else if (id == 26723)
					player.getDialogueManager().startDialogue("SpiritTreeD", (object.getId() == 68973 && object
							.getId() == 68974) ? 3637 : 3636);
				else if (id == 16604)
					player.getActionManager().setAction(new DreamTreeWoodcutting());
				else if (id == 38669)
					ShootingStars.openNoticeboard(player);
				else if (id == 25591)
					ShootingStars.openTelescope(player);
				else if (id == 28094) // keldagrim TODO make real dialogue in future
					player.useStairs(-1, new WorldTile(2911, 10174, 0), 0, 1);
				else if (id == 2230) // cart brim -> shillo village TODO make real dialogue in future
					player.useStairs(-1, new WorldTile(2832, 2954, 0), 0, 1);
				else if (id == 4787 || id == 4788)
					player.useStairs(-1, new WorldTile(object.getX(), object.getY() + (player.getY() == 2767 ? -1 : 1), 0), 0, 1);
				else if (id == 2311)
					player.getActionManager().setAction(new Mining(object, RockDefinitions.Silver_Ore));
				else if (id == 78329)
					DollarContest.open(player, object);
				else {
					switch (objectDef.getToObjectName(player).toLowerCase()) {
					case "trapdoor":
					case "manhole":
						if (objectDef.containsOption(0, "Open")) {
							WorldObject openedHole = new WorldObject(object.getId() + 1, object.getType(), object
									.getRotation(), object.getX(), object.getY(), object.getPlane());
							World.spawnObjectTemporary(openedHole, 60000);
						}
						break;
					case "closed chest":
						if (objectDef.containsOption(0, "Open") && (ObjectConfig.forID(object.getId() + 1).name
								.toLowerCase().equals("open chest"))) {
							player.setNextAnimation(new Animation(536));
							player.lock(2);
							WorldObject openedChest = new WorldObject(object.getId() + 1, object.getType(), object
									.getRotation(), object.getX(), object.getY(), object.getPlane());
							World.spawnObjectTemporary(openedChest, 60000);
						}
						break;
					case "open chest":
						if (objectDef.containsOption(0, "Search"))
							player.getPackets().sendGameMessage("You search the chest but find nothing.");
						break;
					case "crate":
						if (objectDef.containsOption(0, "Search"))
							player.getPackets().sendGameMessage("You search the crate but find nothing.");
						break;
					case "spirit tree":
						player.getDialogueManager().startDialogue("SpiritTreeD", (object.getId() == 68973 && object
								.getId() == 68974) ? 3637 : 3636);
						break;
					case "fairy ring":
					case "enchanted land":
						FairyRings.openRingInterface(player, object, id == 12128);
						break;
					case "spiderweb":
						if (object.getRotation() == 2) {
							player.lock(2);
							if (Utils.random(2) == 0) {
								player.addWalkSteps(player.getX(), player.getY() < y ? object.getY() + 2
										: object.getY() - 1, -1, false);
								player.getPackets().sendGameMessage("You squeeze though the web.");
							} else
								player.getPackets().sendGameMessage(
										"You fail to squeeze though the web; perhaps you should try again.");
						}
						break;
					case "web":
						if (objectDef.containsOption(0, "Slash")) {
							player.setNextAnimation(new Animation(PlayerCombat.getWeaponAttackEmote(player
									.getEquipment().getWeaponId(), player.getCombatDefinitions().getAttackStyle())));
							slashWeb(player, object);
						}
						break;
					case "anvil":
						for (int index = 0; index < Smithing.BARS[0].length; index++) {
							if (player.getInventory().containsItem(Smithing.BARS[0][index], 1)) {
								Smithing.sendForgingInterface(player, index, false);
								return;
							}
						}
						player.getDialogueManager().startDialogue("SimpleMessage",
								"Use a metal on the anvil in order to begin working with the metal.");
						break;
					case "potter's wheel":
						player.getDialogueManager().startDialogue("PotteryWheel");
						break;
					case "pottery oven":
						player.getDialogueManager().startDialogue("PotteryFurnace");
						break;
					case "range":
					case "cooking range":
					case "stove":
					case "clay oven":
						if (objectDef.containsOption(0, "Cook")) 
							player.getDialogueManager().startDialogue("CookingRange", object);
						break;
					case "crashed star":
						if (objectDef.containsOption(0, "Mine"))
							ShootingStars.mine(player, object);
						break;
					case "rocks":
					case "depleted vein":
						if (objectDef.containsOption(0, "Mine"))
							player.getPackets().sendGameMessage("That rock is currently unavailable.");
						break;
					case "tin ore rocks":
					case "tin ore vein":
						if (objectDef.containsOption(0, "Mine"))
							player.getActionManager().setAction(new Mining(object, RockDefinitions.Tin_Ore));
						break;
					case "gold ore rocks":
					case "gold ore vein":
						if (objectDef.containsOption(0, "Mine"))
							player.getActionManager().setAction(new Mining(object, RockDefinitions.Gold_Ore));
						break;
					case "iron ore rocks":
					case "iron ore vein":
						if (objectDef.containsOption(0, "Mine"))
							player.getActionManager().setAction(new Mining(object, RockDefinitions.Iron_Ore));
						break;
					case "silver ore rocks":
					case "silver ore vein":
						if (objectDef.containsOption(0, "Mine"))
							player.getActionManager().setAction(new Mining(object, RockDefinitions.Silver_Ore));
						break;
					case "coal rocks":
					case "coal ore vein":
						if (objectDef.containsOption(0, "Mine"))
							player.getActionManager().setAction(new Mining(object, RockDefinitions.Coal_Ore));
						break;
					case "clay rocks":
					case "clay ore vein":
						if (objectDef.containsOption(0, "Mine"))
							player.getActionManager().setAction(new Mining(object, RockDefinitions.Clay_Ore));
						break;
					case "copper ore rocks":
					case "copper ore vein":
						if (objectDef.containsOption(0, "Mine"))
							player.getActionManager().setAction(new Mining(object, RockDefinitions.Copper_Ore));
						break;
					case "blurite ore rocks":
					case "blurite ore vein":
						if (objectDef.containsOption(0, "Mine"))
							player.getActionManager().setAction(new Mining(object, RockDefinitions.Blurite_Ore));
						break;
					case "adamantite ore rocks":
					case "adamantite ore vein":
						if (objectDef.containsOption(0, "Mine"))
							player.getActionManager().setAction(new Mining(object, RockDefinitions.Adamant_Ore));
						break;
					case "runite ore rocks":
					case "runite ore vein":
						if (objectDef.containsOption(0, "Mine"))
							player.getActionManager().setAction(new Mining(object, RockDefinitions.Runite_Ore));
						break;
					case "gem rocks":
						if (objectDef.containsOption(0, "Mine"))
							player.getActionManager().setAction(new JemMining(object));
						break;
					case "granite rocks":
						if (objectDef.containsOption(0, "Mine"))
							player.getActionManager().setAction(new Mining(object, RockDefinitions.Granite_Ore));
						break;
					case "sandstone rocks":
						if (objectDef.containsOption(0, "Mine"))
							player.getActionManager().setAction(new Mining(object, RockDefinitions.Sandstone_Ore));
						break;
					case "mithril ore rocks":
						if (objectDef.containsOption(0, "Mine"))
							player.getActionManager().setAction(new Mining(object, RockDefinitions.Mithril_Ore));
						break;
					case "crust":
						if (objectDef.containsOption(0, "Mine"))
							player.getActionManager().setAction(new MineCrust(object));
						break;
					case "beehive":
						if (player.getInventory().addItem(12156, 1)) {
							player.setNextAnimation(new Animation(827));
							player.lock(2);
							player.getInventory().addItem(12156, 1);
							player.getInventory().addItem(12156, 1);
						}
						break;
					case "deposit box":
					case "bank deposit box":
						if (objectDef.containsOption(0, "Deposit"))
							player.getBank().openDepositBox();
						break;
					case "bank":
					case "bank chest":
					case "bank table":
					case "bank booth":
					case "counter":
						if (objectDef.containsOption(0, "Bank") || objectDef.containsOption(0, "Use"))
							player.getBank().openBank();
						break;
					// Woodcutting start
					case "dramen tree":
						if (objectDef.containsOption(0, "Chop down")) {
							if (!player.isKilledLostCityTree()) {
								if (player.getTemporaryAttributtes().get("HAS_SPIRIT_TREE") != null)
									return;
								new TreeSpirit(player, new WorldTile(2859, 9734, 0));
								return;
							}
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.DRAMEN));
						}
						break;
					case "tree":
						if (objectDef.containsOption(0, "Chop down"))
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.NORMAL));
						break;
					case "evergreen":
						if (objectDef.containsOption(0, "Chop down"))
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.EVERGREEN));
						break;
					case "dead tree":
						if (objectDef.containsOption(0, "Chop down"))
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.DEAD));
						break;
					case "oak":
						if (objectDef.containsOption(0, "Chop down"))
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.OAK));
						break;
					case "teak":
						if (objectDef.containsOption(0, "Chop down"))
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.TEAK));
						break;
					case "mahogany":
						if (objectDef.containsOption(0, "Chop down"))
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.MAHOGANY));
						break;
					case "willow":
						if (objectDef.containsOption(0, "Chop down"))
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.WILLOW));
						break;
					case "maple tree":
						if (objectDef.containsOption(0, "Chop down"))
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.MAPLE));
						break;
					case "ivy":
						if (objectDef.containsOption(0, "Chop"))
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.IVY));
						break;
					case "redwood":
						if (objectDef.containsOption(0, "Cut"))
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.REDWOOD));
						break;
					case "yew":
						if (objectDef.containsOption(0, "Chop down"))
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.YEW));
						break;
					case "magic tree":
						if (objectDef.containsOption(0, "Chop down"))
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.MAGIC));
						break;
					case "cursed magic tree":
						if (objectDef.containsOption(0, "Chop down"))
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.CURSED_MAGIC));
						break;
					// Woodcutting end
					case "gate":
					case "large door":
					case "metal door":
					case "magic door":
					case "tree door":
					case "wooden gate":
					case "hardwood grove doors":
					case "colony gate":
						if (objectDef.containsOption(0, "Open"))
							if (!handleGate(player, object))
								handleDoor(player, object);
						break;
					case "door":
					case "blacksmith's door":
					case "bamboo door":
						if ((objectDef.containsOption(0, "Open") || objectDef.containsOption(0, "Unlock")))
							handleDoor(player, object);
						break;
					case "ladder":
						handleLadder(player, object, 1);
						break;
					case "stairs":
					case "staircase":
						handleStaircases(player, object, 1);
						break;
					case "small obelisk":
						if (objectDef.containsOption(0, "renew-points"))
							renewSummoningPoints(player);
						break;
					case "Summoning obelisk":
					case "obelisk":
						if (objectDef.containsOption(0, "Infuse-pouch"))
							Summoning.openInfusionInterface(player, false, false);
						break;
					case "altar":
					case "chaos altar":
					case "gorilla statue":
						if (objectDef.containsOption(0, "Recharge") || objectDef.containsOption(0, "Pray") || objectDef
								.containsOption(0, "Pray-at")) {
							final int maxPrayer = player.getSkills().getLevelForXp(Skills.PRAYER) * 10;
							if (player.getPrayer().getPrayerpoints() < maxPrayer) {
								player.lock(5);
								player.getPackets().sendGameMessage("You pray to the gods...", true);
								player.setNextAnimation(new Animation(645));
								WorldTasksManager.schedule(new WorldTask() {
									@Override
									public void run() {
										player.getPrayer().restorePrayer(maxPrayer);
										player.getPackets().sendGameMessage("...and recharged your prayer.", true);
									}
								}, 2);
							} else
								player.getPackets().sendGameMessage("You already have full prayer.");
							if (id == 6552)
								player.getDialogueManager().startDialogue("AncientAltar");
						}
						break;
					default:
						player.getPackets().sendGameMessage("Nothing interesting happens.");
						break;
					}
				}
				if (Settings.DEBUG)
					Logger.log("ObjectHandler",
							"clicked 1 at object id : " + id + ", " + object.getX() + ", " + object.getY() + ", "
									+ object.getPlane() + ", " + object.getType() + ", " + object.getRotation() + ", "
									+ object.getDefinitions().name + ", " + object.getDefinitions().getSizeX() + ", "
									+ object.getDefinitions().getSizeY());
			}
		}));
	}

	private static boolean getRepeatedTele(Player player, int x1, int y1, int p1, int x2, int y2, int p2) {
		if (player.getX() == x1 && player.getY() == y1) {
			player.useStairs(17803, new WorldTile(x2, y2, p2), 2, 3);
			return true;
		} else if (player.getX() == x2 && player.getY() == y2) {
			player.useStairs(17803, new WorldTile(x1, y1, p1), 2, 3);
			return true;
		}
		return false;
	}

	public static void renewSummoningPoints(Player player) {
		int summonLevel = player.getSkills().getLevelForXp(Skills.SUMMONING);
		if (player.getSkills().getLevel(Skills.SUMMONING) < summonLevel) {
			player.lock(3);
			player.setNextAnimation(new Animation(8502));
			player.getSkills().set(Skills.SUMMONING, summonLevel);
			player.getPackets().sendGameMessage("You have recharged your Summoning points.", true);
		} else
			player.getPackets().sendGameMessage("You already have full Summoning points.");
	}

	private static void handleOption2(final Player player, final WorldObject object) {
		final ObjectConfig objectDef = object.getDefinitions();
		final int id = object.getId();
		if ((object.getId() == 10089 || object.getId() == 10088 || object.getId() == 10087)) {
			player.getPackets().sendGameMessage("Something seems to have scared all the fishes away...");
			return;
		}
		if(object.getId() == 133114) {
			player.faceObject(object);
			UpgradeItemOption.checkAllUpgrades(player);
			return;
		}
		if(object.getId() == 133114) {
			player.faceObject(object);
			UpgradeItemOption.checkAllUpgrades(player);
			return;
		}
		player.setRouteEvent(new RouteEvent(object, new Runnable() {
			@Override
			public void run() {
				player.stopAll();
				if (object.getType() != 0)
					player.faceObject(object);
				if (!player.getControlerManager().processObjectClick2(object))
					return;
				if (player.getTreasureTrailsManager().useObject(object))
					return;
				if (player.getFarmingManager().isFarming(id, null, 2))
					return;
				if (Thieving.handleStalls(player, object))
					return;
				else if (EvilTrees.isTree(object))
					EvilTrees.makeFire(player);
				else if (id == PartyRoom.PARTY_CHEST_OPEN)
					PartyRoom.openPartyChest(player);
				else if (id == 2114)
					player.getCoalTrucksManager().investigate();
				else if (id == 17010)
					player.getDialogueManager().startDialogue("LunarAltar");
				else if (id == 12774)
					player.getDialogueManager().startDialogue("ShantyPassDangerSignD");
				// catacomb
				else if (id == 127785) {
					player.getPackets().sendGameMessage("Kourend is the best.");
				} else if (id == 41902)
					player.useStairs(-1, new WorldTile(2294, 3626, 0), 0, 1);
				else if (id == 62677)
					player.getDominionTower().openRewards();
				else if (id == 28094) // keldagrim TODO make real dialogue in future
					player.useStairs(-1, new WorldTile(2911, 10174, 0), 0, 1);
				else if (id == 2230) // shillo village TODO make real dialogue in future
					player.useStairs(-1, new WorldTile(2832, 2954, 0), 0, 1);
				else if (id == 2491 || id == 16684)
					MiningBase.propect(player, "This rock contains unbound Rune Stone essence.");
				else if (id == 2265)  // cart shillo village -> brim TODO make real dialogue in future
					player.useStairs(-1, new WorldTile(2779, 3212, 0), 0, 1);
				else if (id == 62688)
					player.getDialogueManager().startDialogue("SimpleMessage",
							"You have a Dominion Factor of " + player.getDominionTower().getDominionFactor() + ".");
				else if (id == 68107)
					FightKiln.enterFightKiln(player, true);
				else if (id == 57187)
					player.getDialogueManager().startDialogue("SimpleMessage","You can repair this with a hammer and level "+LavaFlowMine.getBoilerLevel()+" smithing.");
				else if (id == 2693)
					player.getGeManager().openCollectionBox();
				else if (id == 35390)
					GodWars.passGiantBoulder(player, object, false);
				else if (id == 2418)
					PartyRoom.openPartyChest(player);
				else if (id == 29155)
					player.getPackets()
							.sendGameMessage("The fire burns intensively, you wonder when it will burn out.");
				else if (id == 10177 && object.getX() == 2546 && object.getY() == 10143)
					player.useStairs(828, new WorldTile(2544, 3741, 0), 1, 2);
				else if (id == 67051)
					player.getDialogueManager().startDialogue("Marv", true);
				else if (id == 78329)
					DollarContest.info(player);
				else if (id == 129150)
					OccultAltar.setSpellBook(player, 0);
				/*
				 * else if (id == 39660 || id == 39661) { if (!player.isExtremeDonator()) {
				 * player.getPackets().
				 * sendGameMessage("You must be an extreme donator in order to access this area."
				 * ); return; } StealingCreationController.passWall(player, object, true); }
				 */else if (id == 12309) {
					ShopsHandler.openShop(player, 72);
				} else if (id == 6) {
					DwarfMultiCannon.pickupCannon(player, 4, object);
				} else if (id == 78331 || id == 133114) {
				 	UpgradeItemOption.checkAllUpgrades(player);
				} else {
					switch (objectDef.getToObjectName(player).toLowerCase()) {
					case "crashed star":
						if (objectDef.containsOption(1, "Prospect"))
							ShootingStars.prospect(player);
						break;
					case "furnace":
					case "small furnace":
					case "lava furnace":
						player.getDialogueManager().startDialogue("SmeltingD", object);
						break;
					case "cabbage":
						if (objectDef.containsOption(1, "Pick") && player.getInventory().addItem(1965, 1)) {
							player.setNextAnimation(new Animation(827));
							player.lock(2);
							World.removeObjectTemporary(object, 5000);
						}
						break;
					case "potato":
						if (objectDef.containsOption(1, "Pick") && player.getInventory().addItem(1942, 1)) {
							player.setNextAnimation(new Animation(827));
							player.lock(2);
							World.removeObjectTemporary(object, 5000);
						}
						break;
					case "wheat":
						if (objectDef.containsOption(1, "Pick") && player.getInventory().addItem(1947, 1)) {
							player.setNextAnimation(new Animation(827));
							player.lock(2);
							player.getInventory().addItem(1947, 1);
							player.getInventory().addItem(1947, 1);
							World.removeObjectTemporary(object, 5000);
						}
						break;
					case "flax":
						if (objectDef.containsOption(1, "Pick") && player.getInventory().addItem(1779, 1)) {
							player.setNextAnimation(new Animation(827));
							player.lock(2);
							player.getInventory().addItem(1779, 1);
							player.getInventory().addItem(1779, 1);
							World.removeObjectTemporary(object, 5000);
						}
						break;
					case "beehive":
						if (player.getInventory().addItem(12156, 1)) {
							player.setNextAnimation(new Animation(827));
							player.lock(2);
							player.getInventory().addItem(12156, 1);
							player.getInventory().addItem(12156, 1);
						}
						break;
					case "spinning wheel":
						player.getDialogueManager().startDialogue("SpinningD", false);
						break;
					case "bank":
					case "bank chest":
					case "bank table":
					case "bank booth":
					case "counter":
						if (object.getId() == 131725 || objectDef.containsOption(1, "Bank"))
							player.getBank().openBank();
						break;
					case "bank deposit box":
						if (objectDef.containsOption(1, "Deposit-all")) {
							player.getBank().depositAllInventory(false);
							player.getBank().depositAllMoneyPouch(false);
							player.getBank().depositAllEquipment(false);
							player.getBank().depositAllBob(false);
							player.getPackets().sendGameMessage("You deposit all of your items into the deposit box");
						}
						break;
					case "spirit tree":
						SpiritTree.openInterface(player, object.getId() != 68973 && object.getId() != 68974);
						break;
					case "gates":
					case "gate":
					case "metal door":
						if (objectDef.containsOption(1, "Open"))
							handleGate(player, object);
						break;
					case "door":
						if (objectDef.containsOption(1, "Open"))
							handleDoor(player, object);
						break;
					case "ladder":
						handleLadder(player, object, 2);
						break;
					case "stairs":
					case "staircase":
						handleStaircases(player, object, 2);
						break;
					case "summoning obelisk":
					case "obelisk":
						if (objectDef.containsOption(1, "renew-points"))
							renewSummoningPoints(player);
						break;
					default:
						player.getPackets().sendGameMessage("Nothing interesting happens.");
						break;
					}
				}
				if (Settings.DEBUG)
					Logger.log("ObjectHandler", "clicked 2 at object id : " + id + ", " + object.getX() + ", "
							+ object.getY() + ", " + object.getPlane());
			}
		}));
	}

	private static void handleOption3(final Player player, final WorldObject object) {
		final ObjectConfig objectDef = object.getDefinitions();
		final int id = object.getId();
		player.setRouteEvent(new RouteEvent(object, new Runnable() {
			@Override
			public void run() {
				player.stopAll();
				player.faceObject(object);
				if (!player.getControlerManager().processObjectClick3(object))
					return;
				else if (player.getFarmingManager().isFarming(id, null, 3))
					return;
				else if (EvilTrees.isTree(object))
					player.getDialogueManager().startDialogue("EvilTreeInspect");
				else if (id == PartyRoom.PARTY_CHEST_OPEN)
					PartyRoom.closeChest(player, object);
				else if (id == 12309)
					ShopsHandler.openShop(player, 71);
				else if (id == 10177 && object.getX() == 2546 && object.getY() == 10143)
					player.useStairs(828, new WorldTile(1798, 4407, 3), 1, 2);
				else if (id == 129150)
					OccultAltar.setSpellBook(player, 1);
				else {
					switch (objectDef.getToObjectName(player).toLowerCase()) {
					case "bank":
					case "bank chest":
					case "bank table":
					case "bank booth":
					case "counter":
						if (object.getId() == 131725 || objectDef.containsOption(2, "Collect"))
							player.getGeManager().openCollectionBox();
						break;
					case "gate":
					case "metal door":
						if (objectDef.containsOption(2, "Open"))
							handleGate(player, object);
						break;
					case "door":
						if (objectDef.containsOption(2, "Open"))
							handleDoor(player, object);
						break;
					case "ladder":
						handleLadder(player, object, 3);
						break;
					case "stairs":
					case "staircase":
						handleStaircases(player, object, 3);
						break;
					default:
						player.getPackets().sendGameMessage("Nothing interesting happens.");
						break;
					}
				}
				if (Settings.DEBUG)
					Logger.log("ObjectHandler", "cliked 3 at object id : " + id + ", " + object.getX() + ", "
							+ object.getY() + ", " + object.getPlane() + ", ");
			}
		}));
	}

	private static void handleOption4(final Player player, final WorldObject object) {
		final ObjectConfig objectDef = object.getDefinitions();
		final int id = object.getId();
		player.setRouteEvent(new RouteEvent(object, new Runnable() {
			@Override
			public void run() {
				player.stopAll();
				player.faceObject(object);
				if (!player.getControlerManager().processObjectClick4(object))
					return;
				// living rock Caverns
				if (player.getFarmingManager().isFarming(id, null, 4))
					return;
				if (id == 45076)
					MiningBase.propect(player, "This rock contains a large concentration of gold.");
				else if (id == 5999)
					MiningBase.propect(player, "This rock contains a large concentration of coal.");
				else if (id == 129150)
					OccultAltar.setSpellBook(player, 2);
				else {
					switch (objectDef.getToObjectName(player).toLowerCase()) {
						case "bank":
						case "bank chest":
						case "bank table":
						case "bank booth":
							if(player.getPresets().getLastPreset() == -1) {
								player.sendMessage("You have not used any presets recently.");
								player.sendMessage("You can access presets through <col=ffff00>Quest tab -> <col=ffff00>Presets</col>.");
								return;
							}

							player.getPresets().load(player.getPresets().getLastPreset());
							break;
					default:
						player.getPackets().sendGameMessage("Nothing interesting happens.");
						break;
					}
				}
				if (Settings.DEBUG)
					Logger.log("ObjectHandler", "cliked 4 at object id : " + id + ", " + object.getX() + ", "
							+ object.getY() + ", " + object.getPlane() + ", ");
			}
		}));
	}

	private static void handleOption5(final Player player, final WorldObject object) {
		final ObjectConfig objectDef = object.getDefinitions();
		final int id = object.getId();
		if (object.getId() >= HouseConstants.HObject.WOOD_BENCH.getId()
				&& object.getId() <= HouseConstants.HObject.GILDED_BENCH.getId()) {
			player.setRouteEvent(new RouteEvent(object, new Runnable() {
				@Override
				public void run() {
					player.getControlerManager().processObjectClick5(object);
				}
			}, true));
			return;
		}
		player.setRouteEvent(new RouteEvent(object, new Runnable() {
			@Override
			public void run() {
				player.stopAll();
				player.faceObject(object);
				if (!player.getControlerManager().processObjectClick5(object))
					return;
				if (id == -1) {
					// unused
				} else {
					switch (objectDef.getToObjectName(player).toLowerCase()) {
					case "fire":
						if (objectDef.containsOption(4, "Add-logs"))
							Bonfire.addLogs(player, object);
						break;
					case "magical wheat":
						PuroPuro.pushThrough(player, object);
						break;
						case "bank":
						case "bank chest":
						case "bank table":
						case "bank booth":
							player.getBank().openBank(true);
							break;
					default:
						player.getPackets().sendGameMessage("Nothing interesting happens.");
						break;
					}
				}
				if (Settings.DEBUG) {
					String option = objectDef != null ? objectDef.getOption(5) : "null";
					Logger.log("ObjectHandler", "cliked 5 "+ (option.equals("") ? "none" : option) +" at object id : " + id + ", " + object.getX() + ", "
							+ object.getY() + ", " + object.getPlane() + ", ");
				}
			}
		}));
	}

	private static void handleOptionExamine(final Player player, final WorldObject object) {
		if(player.getRights() == 2) {
			ObjectConfig ob = object.getDefinitions();
			if(ob != null) {
				player.sendMessage(ob.name + ": id=" + ob.id + ", type="+object.getType()+", rotation="+object.getRotation()+", sizeX="+ob.getSizeX()+", sizeY="+ob.getSizeX()+" pos=[" + object.getX() + ", " + object.getY() + ", " + object.getPlane() + "]");
				/*if(Settings.DEBUG) {
					System.out.println(ob.name + ": models: " + Arrays.toString(ob.collapseAllIds()));
				}*/
			}
		}
		player.getPackets().sendObjectMessage(0, 15263739, object, ObjectExamines.getExamine(player, object));
		player.getPackets().sendResetMinimapFlag();
		if (Settings.DEBUG)
			Logger.log("ObjectHandler",
					"examined object id : " + object.getId() + ", " + object.getX() + ", " + object.getY() + ", "
							+ object.getPlane() + ", " + object.getType() + ", " + object.getRotation() + ", "
							+ object.getDefinitions().getToObjectName(player) + ", " + object.getDefinitions().name);
	}

	private static void slashWeb(Player player, WorldObject object) {
		if (Utils.random(2) == 0) {
			World.spawnObjectTemporary(new WorldObject(object.getId() + 1, object.getType(), object.getRotation(),
					object.getX(), object.getY(), object.getPlane()), 60000);
			player.getPackets().sendGameMessage("You slash through the web!");
		} else
			player.getPackets().sendGameMessage("You fail to cut through the web.");
	}

	private static boolean handleGate(Player player, WorldObject object) {
		return handleGate(player, object, 60000);
	}

	public static boolean handleGate(Player player, WorldObject object, long delay) {
		if (World.isSpawnedObject(object))
			return false;
		if (object.getRotation() == 0) {
			boolean south = true;
			WorldObject otherDoor = World.getObjectWithType(
					new WorldTile(object.getX(), object.getY() + 1, object.getPlane()), object.getType());
			if (otherDoor == null || otherDoor.getRotation() != object.getRotation()
					|| otherDoor.getType() != object.getType()
					|| !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
				otherDoor = World.getObjectWithType(new WorldTile(object.getX(), object.getY() - 1, object.getPlane()),
						object.getType());
				if (otherDoor == null || otherDoor.getRotation() != object.getRotation()
						|| otherDoor.getType() != object.getType()
						|| !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name))
					return false;
				south = false;
			}
			WorldObject openedDoor1 = new WorldObject(object.getId(), object.getType(), object.getRotation() + 1,
					object.getX(), object.getY(), object.getPlane());
			WorldObject openedDoor2 = new WorldObject(otherDoor.getId(), otherDoor.getType(),
					otherDoor.getRotation() + 1, otherDoor.getX(), otherDoor.getY(), otherDoor.getPlane());
			if (south) {
				openedDoor1.moveLocation(-1, 0, 0);
				openedDoor1.setRotation(3);
				openedDoor2.moveLocation(-1, 0, 0);
			} else {
				openedDoor1.moveLocation(-1, 0, 0);
				openedDoor2.moveLocation(-1, 0, 0);
				openedDoor2.setRotation(3);
			}

			if (World.removeObjectTemporary(object, delay) && World.removeObjectTemporary(otherDoor, delay)) {
				player.faceObject(openedDoor1);
				World.spawnObjectTemporary(openedDoor1, delay);
				World.spawnObjectTemporary(openedDoor2, delay);
				return true;
			}
		} else if (object.getRotation() == 2) {

			boolean south = true;
			WorldObject otherDoor = World.getObjectWithType(
					new WorldTile(object.getX(), object.getY() + 1, object.getPlane()), object.getType());
			if (otherDoor == null || otherDoor.getRotation() != object.getRotation()
					|| otherDoor.getType() != object.getType()
					|| !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
				otherDoor = World.getObjectWithType(new WorldTile(object.getX(), object.getY() - 1, object.getPlane()),
						object.getType());
				if (otherDoor == null || otherDoor.getRotation() != object.getRotation()
						|| otherDoor.getType() != object.getType()
						|| !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name))
					return false;
				south = false;
			}
			WorldObject openedDoor1 = new WorldObject(object.getId(), object.getType(), object.getRotation() + 1,
					object.getX(), object.getY(), object.getPlane());
			WorldObject openedDoor2 = new WorldObject(otherDoor.getId(), otherDoor.getType(),
					otherDoor.getRotation() + 1, otherDoor.getX(), otherDoor.getY(), otherDoor.getPlane());
			if (south) {
				openedDoor1.moveLocation(1, 0, 0);
				openedDoor2.setRotation(1);
				openedDoor2.moveLocation(1, 0, 0);
			} else {
				openedDoor1.moveLocation(1, 0, 0);
				openedDoor1.setRotation(1);
				openedDoor2.moveLocation(1, 0, 0);
			}
			if (World.removeObjectTemporary(object, delay) && World.removeObjectTemporary(otherDoor, delay)) {
				player.faceObject(openedDoor1);

				World.spawnObjectTemporary(openedDoor1, delay);
				World.spawnObjectTemporary(openedDoor2, delay);
				return true;
			}
		} else if (object.getRotation() == 3) {

			boolean right = true;
			WorldObject otherDoor = World.getObjectWithType(
					new WorldTile(object.getX() - 1, object.getY(), object.getPlane()), object.getType());
			if (otherDoor == null || otherDoor.getRotation() != object.getRotation()
					|| otherDoor.getType() != object.getType()
					|| !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
				otherDoor = World.getObjectWithType(new WorldTile(object.getX() + 1, object.getY(), object.getPlane()),
						object.getType());
				if (otherDoor == null || otherDoor.getRotation() != object.getRotation()
						|| otherDoor.getType() != object.getType()
						|| !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name))
					return false;
				right = false;
			}
			WorldObject openedDoor1 = new WorldObject(object.getId(), object.getType(), object.getRotation() + 1,
					object.getX(), object.getY(), object.getPlane());
			WorldObject openedDoor2 = new WorldObject(otherDoor.getId(), otherDoor.getType(),
					otherDoor.getRotation() + 1, otherDoor.getX(), otherDoor.getY(), otherDoor.getPlane());
			if (right) {
				openedDoor1.moveLocation(0, -1, 0);
				openedDoor2.setRotation(0);
				openedDoor1.setRotation(2);
				openedDoor2.moveLocation(0, -1, 0);
			} else {
				openedDoor1.moveLocation(0, -1, 0);
				openedDoor1.setRotation(0);
				openedDoor2.setRotation(2);
				openedDoor2.moveLocation(0, -1, 0);
			}
			if (World.removeObjectTemporary(object, delay) && World.removeObjectTemporary(otherDoor, delay)) {
				player.faceObject(openedDoor1);
				World.spawnObjectTemporary(openedDoor1, delay);
				World.spawnObjectTemporary(openedDoor2, delay);
				return true;
			}
		} else if (object.getRotation() == 1) {

			boolean right = true;
			WorldObject otherDoor = World.getObjectWithType(
					new WorldTile(object.getX() - 1, object.getY(), object.getPlane()), object.getType());
			if (otherDoor == null || otherDoor.getRotation() != object.getRotation()
					|| otherDoor.getType() != object.getType()
					|| !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
				otherDoor = World.getObjectWithType(new WorldTile(object.getX() + 1, object.getY(), object.getPlane()),
						object.getType());
				if (otherDoor == null || otherDoor.getRotation() != object.getRotation()
						|| otherDoor.getType() != object.getType()
						|| !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name))
					return false;
				right = false;
			}
			WorldObject openedDoor1 = new WorldObject(object.getId(), object.getType(), object.getRotation() + 1,
					object.getX(), object.getY(), object.getPlane());
			WorldObject openedDoor2 = new WorldObject(otherDoor.getId(), otherDoor.getType(),
					otherDoor.getRotation() + 1, otherDoor.getX(), otherDoor.getY(), otherDoor.getPlane());
			if (right) {
				openedDoor1.moveLocation(0, 1, 0);
				openedDoor1.setRotation(2);
				openedDoor2.setRotation(0);
				openedDoor2.moveLocation(0, 1, 0);
			} else {
				openedDoor1.moveLocation(0, 1, 0);
				openedDoor1.setRotation(0);
				openedDoor2.setRotation(2);
				openedDoor2.moveLocation(0, 1, 0);
			}
			if (World.removeObjectTemporary(object, delay) && World.removeObjectTemporary(otherDoor, delay)) {
				player.faceObject(openedDoor1);
				World.spawnObjectTemporary(openedDoor1, delay);
				World.spawnObjectTemporary(openedDoor2, delay);
				return true;
			}
		}
		return false;
	}

	public static boolean handleDoor(Player player, WorldObject object, long timer) {
		if (World.isSpawnedObject(object)) {
			// World.removeObject(object);
			return false;
		}
		WorldObject openedDoor = new WorldObject(object.getId(), object.getType(), (object.getRotation() + 1) & 0x3,
				object.getX(), object.getY(), object.getPlane());
		World.spawnObjectTemporary(openedDoor, timer);
		return false;
	}

	private static boolean handleDoor(Player player, WorldObject object) {
		return handleDoor(player, object, 60000);
	}

	public static WorldTile getClimbTile(WorldObject object, boolean up) {
		int x;
		int y;
		if (up) {
			switch (object.getRotation()) {
			case 0:
				x = object.getX() + object.getDefinitions().getSizeX() - 1;
				y = object.getY() + object.getDefinitions().getSizeY();
				break;
			case 1:
				x = object.getX() + object.getDefinitions().getSizeY();
				y = object.getY() + object.getDefinitions().getSizeX() - 1;
				break;
			case 2:
				x = object.getX() + object.getDefinitions().getSizeX() - 1;
				y = object.getY() - 1;
				break;
			case 3:
			default:
				x = object.getX() - 1;
				y = object.getY() + object.getDefinitions().getSizeX() - 1;
				break;
			}
		} else {
			switch (object.getRotation()) {
			case 0:
				x = object.getX() + object.getDefinitions().getSizeX() - 1;
				y = object.getY() - 1;
				break;
			case 1:
				x = object.getX() - object.getDefinitions().getSizeY();
				y = object.getY() - 1;
				break;
			case 2:
				x = object.getX() + object.getDefinitions().getSizeX() - 1;
				y = object.getY() - object.getDefinitions().getSizeY();
				break;
			case 3:
			default:
				x = object.getX() + object.getDefinitions().getSizeY();
				y = object.getY() + object.getDefinitions().getSizeX() - 1;
				break;
			}
		}
		return new WorldTile(x, y, object.getPlane() + (up ? 1 : -1));
	}

	private static boolean handleStaircases(Player player, WorldObject object, int optionId) {
		String option = object.getDefinitions().getOption(optionId);
		if (option.equalsIgnoreCase("Climb-up")) {
			if (player.getPlane() == 3)
				return false;
			player.useStairs(-1, player.transform(0, 0, 1), 0, 1);
		} else if (option.equalsIgnoreCase("Climb-down")) {
			if (player.getPlane() == 0)
				return false;
			player.useStairs(-1, player.transform(0, 0, -1), 0, 1);
		} else if (option.equalsIgnoreCase("Climb")) {
			if (player.getPlane() == 3 || player.getPlane() == 0)
				return false;
			player.getDialogueManager().startDialogue("ClimbNoEmoteStairs", player.transform(0, 0, 1),
					player.transform(0, 0, -1), "Go up the stairs.", "Go down the stairs.");
		} else
			return false;
		return false;
	}

	private static boolean handleLadder(Player player, WorldObject object, int optionId) {
		String option = object.getDefinitions().getOption(optionId);
		if(object.getId() == 33184) {
			// ladders inside ice queen dungeon, doesn't act as a normal ladder
			player.sendMessage("The ladder doesn't lead anywhere.");
			return false;
		}
		if (option.equalsIgnoreCase("Climb-up") || (option.equalsIgnoreCase("Climb") && player.getPlane() == 0)) {
			if (player.getPlane() == 3)
				return false;
			player.useStairs(828, new WorldTile(player.getX(), player.getY(),
					player.getPlane() + (object.getRegionId() == 13200 ? 2 : 1)), 1, 2);
		} else if (option.equalsIgnoreCase("Climb-down")
				|| (option.equalsIgnoreCase("Climb") && player.getPlane() == 3)) {
			if (player.getPlane() == 0)
				return false;
			player.useStairs(828, new WorldTile(player.getX(), player.getY(),
					player.getPlane() - (object.getRegionId() == 13200 ? 2 : 1)), 1, 2);
		} else if (option.equalsIgnoreCase("Climb")) {
			if (player.getPlane() == 3 || player.getPlane() == 0)
				return false;
			player.getDialogueManager().startDialogue("ClimbEmoteStairs",
					new WorldTile(player.getX(), player.getY(), player.getPlane() + 1),
					new WorldTile(player.getX(), player.getY(), player.getPlane() - 1), "Climb up the ladder.",
					"Climb down the ladder.", 828);
		} else
			return false;
		return true;
	}

	public static void handleItemOnObject(final Player player, final WorldObject object, final int interfaceId,
			final int slot, final int itemId) {
		final Item item = player.getInventory().getItem(slot);
		if (item == null || item.getId() != itemId)
			return;
		final ObjectConfig objectDef = object.getDefinitions();
		if (object.getId() == 1996) {
			if (item.getId() != 954)
				return;
			for (int i = 0; i < 7; i++) {
				WorldObject o = new WorldObject(object);
				o.setId(1998);
				o.setLocation(o.getX(), 3475 - i, o.getPlane());
				player.getPackets().sendAddObject(o);
			}
			WorldObject o = new WorldObject(object);
			o.setId(1997);
			player.getPackets().sendAddObject(o);
			player.getAppearence().setRenderEmote(188);
			player.setNextForceMovement(new ForceMovement(object, 8, ForceMovement.SOUTH));
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					for (int i = 0; i < 7; i++) {
						WorldObject o = new WorldObject(object);
						o.setId(1998);
						o.setLocation(o.getX(), 3475 - i, o.getPlane());
						player.getPackets().sendRemoveObject(o);
					}
					WorldObject o = new WorldObject(object);
					o.setId(1996);
					player.getPackets().sendAddObject(o);
					player.getAppearence().setRenderEmote(-1);
					player.setNextWorldTile(new WorldTile(2513, 3468, 0));
				}
			}, 7);
		} else {
			if (FishingFerretRoom.handleFerretThrow(player, object, item))
				return;
		}
		if ((object.getId() == 10089 || object.getId() == 10088 || object.getId() == 10087)
				&& (item.getId() == 6664 || item.getId() == 12633)) {
			if (player.withinDistance(object, 3)) {
				player.getPackets()
						.sendGameMessage("If this thing explodes, I think I should stand a liiiiitle further away...");
				return;
			} else if (!player.withinDistance(object, 7)) {
				player.getPackets().sendGameMessage(
						"You prepare to throw the vial, but notice you are a bit too far to throw with accuracy.");
				return;
			} else if (player.isUnderCombat()) {
				player.getPackets().sendGameMessage("It seems like you're a bit busy right now.");
				return;
			}
			boolean isSuperFishing = item.getId() == 1;
			player.setNextAnimation(new Animation(6600));
			player.setNextFaceWorldTile(object);
			player.getPackets().sendGameMessage("You hurl the shuddering vial into the water...");
			World.sendProjectile(player, object, isSuperFishing ? 51 : 49, 25, 0, 30, 30, 16, 0);
			player.lock();
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					new Mogre(player, Utils.getFreeTile(object, 3), -1, true);
					player.getPackets().sendGameMessage("... and a Mogre appears!");
					player.unlock();
				}
			}, 4);
			return;
		}
		player.setRouteEvent(new RouteEvent(object, new Runnable() {
			@Override
			public void run() {
				if (!player.getControlerManager().handleItemOnObject(object, item))
					return;
				player.faceObject(object);
				if (Ectofuntus.handleItemOnObject(player, itemId, object.getId()))
					return;

				if ((object.getId() == 65371 && object.getX() == 2947 && object.getY() == 3820)
						|| (object.getId() == 65371 && object.getX() == 3729 && object.getY() == 5526)
						|| (object.getId() == 65371 && object.getX() == 3729 && object.getY() == 5516)
						|| object.getId() == 411 || object.getId() == 113199
						|| HouseConstants.Builds.ALTAR.containsObject(object)) {
					Bone bone = Bone.forId(item.getId());
					if (bone != null) {
						player.getActionManager().setAction(new BoneOffering(object, bone, 2, object.getId() == 65371)); // 250%
																															// xp,
																															// if
																															// you
																															// want
						// 350% use your own
						// house
						return;
					}
				}

				if (itemId >= 1438 && itemId <= 1450) {
					for (int index = 0; index < Runecrafting.OBJECTS.length; index++) {
						if (Runecrafting.OBJECTS[index] == object.getId()) {
							Runecrafting.infuseTiara(player, index);
							break;
						}
					}
				} else if (object.getId() == 133307) {
					player.getDialogueManager().startDialogue("CallusShrine", slot);
				} else if (object.getId() == 28715 || object.getId() == 103641 || object.getId() == 26945) {
					player.getDialogueManager().startDialogue("WishCoins", slot);
				} else if (itemId == CoalTrucksManager.COAL && object.getId() == 2114)
					player.getCoalTrucksManager().addCoal();
				else if (object.getId() == 28352 || object.getId() == 28550) {
					Incubator.useEgg(player, itemId);
				} else if (itemId == 1438 && object.getId() == 2452) {
					Runecrafting.enterAirAltar(player);
				} else if (itemId == 1440 && object.getId() == 2455) {
					Runecrafting.enterEarthAltar(player);
				} else if (itemId == 1442 && object.getId() == 2456) {
					Runecrafting.enterFireAltar(player);
				} else if (itemId == 1444 && object.getId() == 2454) {
					Runecrafting.enterWaterAltar(player);
				} else if (itemId == 1446 && object.getId() == 2457) {
					Runecrafting.enterBodyAltar(player);
				} else if (itemId == 1448 && object.getId() == 2453) {
					Runecrafting.enterMindAltar(player);
				} else if (itemId == 1456 && object.getId() == 2462) {
					Runecrafting.enterDeathAltar(player);
				} else if (itemId == 1452 && object.getId() == 2461) {
					Runecrafting.enterChoasAltar(player);
				} else if (itemId == 1450 && object.getId() == 2464) {
					Runecrafting.enterBloodAltar(player);
				} else if (itemId == 1454 && object.getId() == 2458) {
					Runecrafting.enterCosmicAltar(player);
				} else if (itemId == 1462 && object.getId() == 2460) {
					Runecrafting.enterNatureAltar(player);
				} else if (itemId == 1458 && object.getId() == 2459) {
					Runecrafting.enterLawAltar(player);
				} else if (itemId == 43273 && object.getId() == 127029) {
					player.getInventory().deleteItem(43273, 1);
					player.getPackets().sendGameMessage("You throw the unsired into the font of Consumption...");
					int id;
					int roll = Utils.random(100);
					if (roll < 10)
						id = 7979;
					else if (roll < 14)
						id = 43262;
					else if (roll < 34)
						id = 43265;
					else if (roll < 44)
						id = 4151;
					else if (roll < 92)
						id = 43263;
					else
						id = 43277;
					player.getInventory().addItem(id, 1);
				} else if (player.getFarmingManager().isFarming(object.getId(), item, 0)) {
					return;
				} else if (object.getId() == 733 || object.getId() == 64729) {
					player.setNextAnimation(new Animation(PlayerCombat.getWeaponAttackEmote(-1, 0)));
					slashWeb(player, object);
				} else if (object.getId() == 48803 && itemId == 954) {
					if (player.isKalphiteLairSetted())
						return;
					player.getInventory().deleteItem(954, 1);
					player.setKalphiteLair();
				} else if (object.getId() == 48802 && itemId == 954) {
					if (player.isKalphiteLairEntranceSetted())
						return;
					player.getInventory().deleteItem(954, 1);
					player.setKalphiteLairEntrance();
				} else if (object.getId() == 172 && itemId == 989)
					CrystalChest.openChest(player, object);
				else if ((object.getId() == 134662 || object.getId() == 134660) && itemId == 53083)
					BrimstoneChest.openChest(player);
				else if (object.getId() == 17239 && itemId == 9626)
					player.getDialogueManager().startDialogue("SingingBowl", item);
				else if (object.getId() == 129088 && itemId >= 5076 && itemId <= 5078) {
					player.lock(1);
					player.setNextAnimation(new Animation(713));
					player.getInventory().deleteItem(itemId, 1);
					player.getInventory().addItem(5073, 1);
					player.getSkills().addXp(Skills.PRAYER, 100);
				} else if (object.getId() == 36695 || object.getId() == 126782 || object.getId() == 131625) {
					if ((item.getId() >= 1704 && item.getId() <= 1710 && item.getId() % 2 == 0)
							|| (item.getId() >= 10356 && item.getId() <= 10366 && item.getId() % 2 == 0)
							|| (item.getId() == 2572
									|| (item.getId() >= 20653 && item.getId() <= 20657 && item.getId() % 2 != 0))) {
						for (Item item : player.getInventory().getItems().getItems()) {
							if (item == null)
								continue;
							if (item.getId() >= 1704 && item.getId() <= 1710 && item.getId() % 2 == 0) {
								if (object.getId() == 126782 && Utils.random(2500) == 0) {
									player.getPackets().sendGameMessage(
											"The power of the fountain is transferred into an amulet of eternal glory. It will now have unlimited charges.");
									item.setId(49707);
								} else
									item.setId(1712);
							} else if (item.getId() >= 10356 && item.getId() <= 10366 && item.getId() % 2 == 0)
								item.setId(10354);
							else if (item.getId() == 2572
									|| (item.getId() >= 20653 && item.getId() <= 20657 && item.getId() % 2 != 0))
								item.setId(20659);
						}
						player.getInventory().refresh();
						player.getDialogueManager().startDialogue("ItemMessage",
								"Your ring of wealth and amulet of glory have all been recharged.", 1712);
					}
				} else if (object.getId() == 126755 && OdiumWard.makeShield(player, item.getId())) {
					return;
				} else if (object.getId() == 128900 && item.getId() == 6746) {
					if (!player.getInventory().containsItem(49677, 3)) {
						player.getPackets().sendGameMessage("You need 3 ancient crystals to upgrade this item.");
						return;
					}
					player.getInventory().deleteItem(6746, 1);
					player.getInventory().deleteItem(49677, 3);
					player.getInventory().addItem(49675, 1);
				} else if (object.getId() == 78331) {
					Upgrade upgrade = UpgradeItemOption.getUpgrade(item);
					if (upgrade == null) {
						player.getPackets().sendGameMessage("This item can not be upgraded.");
						return;
					}
					player.getDialogueManager().startDialogue("UpgradeItemOption", upgrade, false);
				} else {
					switch (objectDef.getToObjectName(player).toLowerCase()) {
					case "fountain":
					case "well":
					case "sink":
					case "pump and drain":
						if (WaterFilling.isFilling(player, itemId, false))
							return;
						break;
					case "sandpit":
					case "sand pit":
						player.getActionManager().setAction(new SandBucketFillAction());
						break;
					case "anvil":
						if (GodswordCreating.isShard(itemId)) {
							GodswordCreating.joinPieces(player, true);
							return;
						} else if (SpiritshieldCreating.isSigil(item.getId())) {
							SpiritshieldCreating.attachShield(player, item, true);
							return;
						} else if (DragonfireShield.isDragonFireShield(item.getId())) {
							DragonfireShield.joinPieces(player);
							return;
						} else if (DragonSqShieldD.isDragonSqShieldPart(item.getId())) {
							DragonSqShieldD.joinPieces(player);
							return;
						} else if (DragonfireWard.isDragonFireShield(item.getId())) {
							DragonfireWard.joinPieces(player);
							return;
						} else if (SkeletalWyvernShield.isDragonFireShield(item.getId())) {
							SkeletalWyvernShield.joinPieces(player);
							return;
						}

						for (int index = 0; index < Smithing.BARS[0].length; index++) {
							if (Smithing.BARS[0][index] == item.getId()) {
								Smithing.sendForgingInterface(player, index, false);
								break;
							}
						}
						break;
					case "furnace":
					case "small furnace":
					case "lava furnace":
						if (item.getId() == 2353 || item.getId() == 4)
							player.getDialogueManager().startDialogue("SingleSmithingD", object,
									new SmeltingBar[] { SmeltingBar.CANNON_BALLS });
						else if (item.getId() == 1783 || item.getId() == 1781)
							player.getDialogueManager().startDialogue("SingleSmithingD", object,
									new SmeltingBar[] { SmeltingBar.MOLTEN_GLASS });
						else if (item.getId() == 2355)
							player.getDialogueManager().startDialogue("SingleSmithingD", object,
									new SmeltingBar[] { SmeltingBar.HOLY_SYMBOL, SmeltingBar.UNHOLY_SYMBOL,
											SmeltingBar.SILVER_SICKLE, SmeltingBar.UNCHARGED_TIARA });
						else if (item.getId() == 4155) {
							player.getDialogueManager().startDialogue("SingleSmithingD", object,
									new SmeltingBar[] { SmeltingBar.SLAYER_RING });
						} else if (item.getId() == 2357 || GemCutting.isGem(item.getId())) {
							AccessorySmithing.openInterface(player, item.getId() == 2357 || item.getId() == 49493);
						}
						break;
					case "altar":
						if (itemId == SpiritshieldCreating.SPIRIT_SHIELD
								|| itemId == SpiritshieldCreating.HOLY_ELIXIR) {
							SpiritshieldCreating.blessShield(player, true);
							return;
						}
						break;
					case "fire":
					case "firepit":
						if (objectDef.containsOption(4, "Add-logs") && Bonfire.addLog(player, object, item))
							return;
					case "fire pit":
						if (Bonfire.addLog(player, object, item))
							return;
					case "range":
					case "fancy range":
					case "cooking range":
					case "stove":
					case "clay oven":
						Cookables cook = Cooking.isCookingSkill(item);
						if (cook != null) {
							player.getDialogueManager().startDialogue("CookingD", cook, object);
							return;
						}
						player.getDialogueManager().startDialogue("SimpleMessage",
								"You can't cook that on a " + (objectDef.name.equals("Fire") ? "fire" : "range") + ".");
						break;
					default:
						player.getPackets().sendGameMessage("Nothing interesting happens.");
						break;
					}
					if (Settings.DEBUG)
						System.out.println("Item on object: " + object.getId());
				}
			}
		}));
	}
}
