
package com.rs.net.decoders.handlers;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.cache.loaders.NPCConfig;
import com.rs.cache.loaders.StanceConfig;
import com.rs.game.*;
import com.rs.game.Hit.HitLook;
import com.rs.game.item.Item;
import com.rs.game.minigames.CastleWars;
import com.rs.game.minigames.pktournament.PkTournament;
import com.rs.game.minigames.PuroPuro;
import com.rs.game.minigames.Sawmill;
import com.rs.game.minigames.pest.CommendationExchange;
import com.rs.game.npc.Drops;
import com.rs.game.npc.NPC;
import com.rs.game.npc.cox.impl.VespulaGrub;
import com.rs.game.npc.holiday.EvilSanta;
import com.rs.game.npc.others.Brazier;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.familiar.Pyrelord;
import com.rs.game.npc.others.*;
import com.rs.game.npc.others.zalcano.Zalcano;
import com.rs.game.npc.slayer.AlchemicalHydra;
import com.rs.game.npc.slayer.Kraken;
import com.rs.game.player.FarmingManager.ProductInfo;
import com.rs.game.player.LogicPacket;
import com.rs.game.player.Player;
import com.rs.game.player.QuestManager.Quests;
import com.rs.game.player.RouteEvent;
import com.rs.game.player.actions.Fishing;
import com.rs.game.player.actions.Fishing.FishingSpots;
import com.rs.game.player.actions.Herblore;
import com.rs.game.player.actions.PlayerCombat;
import com.rs.game.player.actions.Rest;
import com.rs.game.player.actions.mining.LivingMineralMining;
import com.rs.game.player.actions.mining.MiningBase;
import com.rs.game.player.actions.runecrafting.SiphonActionCreatures;
import com.rs.game.player.actions.thieving.PickPocketAction;
import com.rs.game.player.actions.thieving.PickPocketableNPC;
import com.rs.game.player.actions.woodcutting.EntWoodcutting;
import com.rs.game.player.content.AbbysObsticals;
import com.rs.game.player.content.AccessorySmithing;
import com.rs.game.player.content.CarrierTravel;
import com.rs.game.player.content.CarrierTravel.Carrier;
import com.rs.game.player.content.CureYakHide;
import com.rs.game.player.content.Drinkables;
import com.rs.game.player.content.EconomyManager;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.content.FlyingEntityHunter;
import com.rs.game.player.content.GamblerKing;
import com.rs.game.player.content.GnomeGlider;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.ItemSets;
import com.rs.game.player.content.LightCreature;
import com.rs.game.player.content.PlayerLook;
import com.rs.game.player.content.SheepShearing;
import com.rs.game.player.content.Slayer.SlayerMaster;
import com.rs.game.player.content.SpiritshieldCreating;
import com.rs.game.player.content.StealingCreationShop;
import com.rs.game.player.content.Summoning.Pouch;
import com.rs.game.player.content.dungeoneering.DungeonRewardShop;
import com.rs.game.player.content.dungeoneering.rooms.puzzles.ColouredRecessRoom.Block;
import com.rs.game.player.content.dungeoneering.rooms.puzzles.SlidingTilesRoom;
import com.rs.game.player.content.pet.Pets;
import com.rs.game.player.content.raids.cox.chamber.impl.VespulaChamber;
import com.rs.game.player.content.teleportation.TeleportationInterface;
import com.rs.game.player.controllers.Falconry;
import com.rs.game.player.controllers.RuneEssenceController;
import com.rs.game.player.controllers.SorceressGarden;
import com.rs.game.player.controllers.TheNightmareInstance;
import com.rs.game.player.controllers.VorkathLair;
import com.rs.game.player.dialogues.impl.BoatingDialouge;
import com.rs.game.player.dialogues.impl.FremennikShipmaster;
import com.rs.game.player.dialogues.impl.MakeoverD;
import com.rs.game.player.dialogues.impl.PetShopOwner;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.io.InputStream;
import com.rs.net.decoders.WorldPacketsDecoder;
import com.rs.utils.Logger;
import com.rs.utils.NPCDrops;
import com.rs.utils.NPCExamines;
import com.rs.utils.ShopsHandler;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class NPCHandler {

	public static void register(int[] id, int actionIndex, NPCAction runnable) {
		for(int i : id) {
			register(i, actionIndex, runnable);
		}
	}

	public static void register(int id, int actionIndex, NPCAction runnable) {
		actionRepository.putIfAbsent(id, new NPCAction[10]);
		NPCAction[] actionList = actionRepository.get(id);
		if(actionList[actionIndex] != null)
			System.err.println("Warning: " + id + " action " + actionIndex + " is being overwritten!");
		actionList[actionIndex] = runnable;
	}

	public static boolean handle(Player player, NPC object, int action) {
		if(action < 1)
			return false;
		if (actionRepository.containsKey(object.getId())) {
			//System.out.println("A " + object.getId() + " act " + action);
			NPCAction act = actionRepository.get(object.getId())[action];
			if(act != null) {

				player.setRouteEvent(new RouteEvent(object, new Runnable() {
					@Override
					public void run() {
						player.faceEntity(object);
						if (Utils.getDistance(player, object) > 2)
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

	static HashMap<Integer, NPCAction[]> actionRepository = new HashMap<Integer, NPCAction[]>();

	public static void handleExamine(final Player player, InputStream stream) {
		int npcIndex = stream.readUnsignedShort128();
		boolean forceRun = stream.read128Byte() == 1;
		if (forceRun)
			player.setRun(forceRun);
		final NPC npc = World.getNPCs().get(npcIndex);
		if (npc == null || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId()))
			return;
		if(player.getRights() == 2) {
			if(Settings.DEBUG) {
				System.out.println("id=" + npc.getId() + ", models=" + Arrays.toString(npc.getDefinitions().models));
			}
			NPCConfig n = npc.getDefinitions();
			int anim = n.walkAnimation > 0 ? n.standAnimation : n.renderEmote > 0 ? StanceConfig.forID(n.renderEmote).standAnimation : -1;

			player.sendMessage(npc.getName() + ": id=" + npc.getId() + " size="+npc.getSize()+", standAnim=" + anim + " pos=[" + npc.getX() + ", " + npc.getY() + ", " + npc.getPlane() + "]");
		}
		player.getPackets().sendNPCMessage(0, 15263739, npc, NPCExamines.getExamine(npc));
		player.getPackets().sendResetMinimapFlag();
		int id = npc.getId();
		if(Arrays.stream(AlchemicalHydra.IDS).anyMatch(hydra -> npc.getId() == hydra)) {
			id = AlchemicalHydra.ENRANGE_DYING_ID;
		}
		if (npc.getName().startsWith("Enraged "))
			id = -id;
		Drops drops = NPCDrops.getDrops(id);
		if (drops != null)
			drops.viewDrops(player, npc); //25% hidden boost
		if (Settings.DEBUG)
			Logger.log("NPCHandler", "examined npc: " + npcIndex + ", " + npc.getId());
	}

	public static void handleOption1(final Player player, final InputStream stream) {
		int npcIndex = stream.readUnsignedShort128();
		boolean forceRun = stream.read128Byte() == 1;
		final NPC npc = World.getNPCs().get(npcIndex);
		if (npc == null || (npc.isCantInteract() && !Zalcano.isZalcanoNPC(npc)) || npc.isDead() || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId()) || player.isLocked())
			return;

		if (npc.getId() == 25888 || npc.getId() == 21203 || npc.getId() == 21204) { //abyssal sire exeption
			stream.setOffset(0);
			WorldPacketsDecoder.decodeLogicPacket(player, new LogicPacket(WorldPacketsDecoder.ATTACK_NPC, 3, stream));
			return;
		}

		player.stopAll();

		if (forceRun)
			player.setRun(forceRun);

		if(handle(player, npc, 1))
			return;

		if(npc.getId() == Zalcano.ZALCANO_ID || npc.getId() == Zalcano.ZALCANO_DOWNED_ID || npc.getId() == Zalcano.GOLEM_ID) {
			// must call before route event
			Zalcano.attack(player, npc);
			return;
		}

		if (npc.getId() == 14 && npc instanceof LiquidGoldNymph) {
			player.setRouteEvent(new RouteEvent(npc, new Runnable() {
				@Override
				public void run() {
					player.faceEntity(npc);
					if (!player.withinDistance(npc, 2))
						return;
					npc.faceEntity(player);
					player.getDialogueManager().startDialogue("NymphD", npc);
					return;
				}
			}, true));
			return;
		}
		
		if (npc.getDefinitions().name.toLowerCase().equals("grand exchange clerk")) {
			player.setRouteEvent(new RouteEvent(npc, new Runnable() {
				@Override
				public void run() {
					player.faceEntity(npc);
					if (!player.withinDistance(npc, 2))
						return;
					npc.faceEntity(player);
					//player.getGeManager().openGrandExchange();
					player.getDialogueManager().startDialogue("ViewGETransactions");
					return;
				}
			}, true));
			return;
		}
		if(npc.getId() == 8536) {
			// infernal imp
			EvilSanta.douseImp(player, npc);
			return;
		}
		if (npc.getId() == 14707 || npc.getId() == 4296 || npc.getDefinitions().name.toLowerCase().contains("banker")) {
			player.setRouteEvent(new RouteEvent(npc, new Runnable() {
				@Override
				public void run() {
					player.faceEntity(npc);
					if (!player.withinDistance(npc, 2))
						return;
					npc.faceEntity(player);
					player.getDialogueManager().startDialogue("Banker", npc.getId());
					return;
				}
			}, true));
			return;
		}
		if (npc instanceof VespulaGrub) {
			player.setRouteEvent(new RouteEvent(npc, new Runnable() {
				@Override
				public void run() {
					player.faceEntity(npc);
					if (!player.withinDistance(npc, npc.getSize())) {
						player.sendMessage("I can't reach that!");
						return;
					}
					VespulaChamber.feedGrub(player, npc);
				}
			}, true));
			return;
		}


		if (npc.getId() == 736 || npc.getId() == 3217 || npc.getName().equalsIgnoreCase("bartender")) {
			player.setRouteEvent(new RouteEvent(npc, new Runnable() {
				@Override
				public void run() {
					player.faceEntity(npc);
					if (!player.withinDistance(npc, 5))
						return;
					npc.faceEntity(player);
					if (player.getTreasureTrailsManager().useNPC(npc))
						return;
					ShopsHandler.openShop(player, 226);
					return;
				}
			}, true));
			return;
		}
		if (npc.getId() == 1923 || npc.getId() == 1925 || npc.getName().equalsIgnoreCase("Eblis")) {
			player.setRouteEvent(new RouteEvent(npc, new Runnable() {
				@Override
				public void run() {
					player.faceEntity(npc);
					if (!player.withinDistance(npc, 5))
						return;
					npc.faceEntity(player);
					ShopsHandler.openShop(player, 228);
					return;
				}
			}, true));
			return;
		}
		if (npc.getId() == 2961 || npc.getId() == 1862 || npc.getName().equalsIgnoreCase("Ali Morrisane")) {
			player.setRouteEvent(new RouteEvent(npc, new Runnable() {
				@Override
				public void run() {
					player.faceEntity(npc);
					if (!player.withinDistance(npc, 5))
						return;
					npc.faceEntity(player);
					ShopsHandler.openShop(player, 242);
					return;
				}
			}, true));
			return;
		}
		
		if (npc.getId() == 4250) {
			player.setRouteEvent(new RouteEvent(npc, new Runnable() {
				@Override
				public void run() {
					player.faceEntity(npc);
					if (!player.withinDistance(npc, 2))
						return;
					npc.faceEntity(player);
					player.getDialogueManager().startDialogue("SawmillOperator", npc.getId());
					return;
				}
			}, true));
			return;
		}

		if(npc.getId() == Brazier.UNLIT) {
			player.getActionManager().setAction(new PlayerCombat(npc));
			player.sendMessage("NPC attack" );
			return;
		}

		if (SlidingTilesRoom.handleSlidingBlock(player, npc))
			return;

		if (SiphonActionCreatures.siphon(player, npc))
			return;
		if ((npc.getId() >= 5098 && npc.getId() <= 5100 && player.getControlerManager().getControler() instanceof Falconry)
				|| npc.getId() == 28523) {
			player.faceEntity(npc);
			player.getControlerManager().processNPCClick1(npc);
			return;
		}
		if (npc.getId() >= 6747 && npc.getId() <= 6749) {
			if (player.getEquipment().getWeaponId() == 10501) {
				PlayerCombat.useSnowBall(player, npc);
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						if (npc.isDead())
							return;
						npc.applyHit(new Hit(player, Utils.random(10000), HitLook.REGULAR_DAMAGE));
						npc.setTarget(player);
					}
					
				}, 1);
				return;
			}
			player.getPackets().sendGameMessage("You need to wield snowballs to do this.");
			return;
		}
		
		FishingSpots spot = FishingSpots.forId(npc.getId() | 1 << 24);
		if (spot != null) {
			player.setRouteEvent(new RouteEvent(npc, new Runnable() {
				@Override
				public void run() {
					player.faceEntity(npc);
					if (!Utils.isOnRange(player, npc, 1))
						return;
					if (!player.getControlerManager().processNPCClick1(npc))
						return;
					player.getActionManager().setAction(new Fishing(spot, npc));
					return;
				}
			}, true));
			return;
		}
		player.setRouteEvent(new RouteEvent(npc, new Runnable() {
			@Override
			public void run() {
				npc.setStopRandomWalk();
				npc.resetWalkSteps();
				player.faceEntity(npc);
				if (!player.getControlerManager().processNPCClick1(npc))
					return;
				if (npc.getId() >= 8837 && npc.getId() <= 8839) {
					player.getActionManager().setAction(new LivingMineralMining((LivingRock) npc));
					return;
				}
				if (npc.getId() == 26595) {
					player.getActionManager().setAction(new EntWoodcutting((Ent) npc));
					return;
				}
				if (npc instanceof GraveStone) {
					GraveStone grave = (GraveStone) npc;
					grave.sendGraveInscription(player);
					return;
				}
				if (npc instanceof Familiar) {
					Familiar familiar = (Familiar) npc;
					if (player.getFamiliar() != familiar) {
						player.getPackets().sendGameMessage("That isn't your familiar.");
						return;
					} else if (familiar.getDefinitions().hasOption("interact")) {
						Object[] paramaters = new Object[2];
						Pouch pouch = player.getFamiliar().getPouch();
						if (pouch == Pouch.SPIRIT_GRAAHK) {
							paramaters[0] = "Karamja's Hunter Area";
							paramaters[1] = new WorldTile(2787, 3000, 0);
						} else if (pouch == Pouch.SPIRIT_KYATT) {
							paramaters[0] = "Piscatorius Hunter Area";
							paramaters[1] = new WorldTile(2339, 3636, 0);
						} else if (pouch == Pouch.SPIRIT_LARUPIA) {
							paramaters[0] = "Feldip Hills Hunter Area";
							paramaters[1] = new WorldTile(2557, 2913, 0);
						} else if (pouch == Pouch.ARCTIC_BEAR) {
							paramaters[0] = "Rellekka Hills Hunter Area";
							paramaters[1] = new WorldTile(2721, 3779, 0);
						} else if (pouch == Pouch.LAVA_TITAN) {
							paramaters[0] = "Lava Maze - *Deep Wilderness*";
							paramaters[1] = new WorldTile(3028, 3840, 0);
						} else {
							player.getDialogueManager().startDialogue("SimpleNPCMessage", npc.getId(), "I will kill you. I will kill him. I will kill them. I will destroy everything muahaha!");
							return;
						}
						player.getDialogueManager().startDialogue("FamiliarInspection", paramaters[0], paramaters[1]);
					}
					return;
				}
				if (npc instanceof Pet) {
					((Pet) npc).interact(player, 0);
					return;
				}
				if (npc.getId() >= TheNightmareInstance.NIGHTMARE_LOBBY_EMPTY && npc.getId() <= TheNightmareInstance.NIGHTMARE_LOBBY_PHASE_3) {
					TheNightmareInstance.interact(player);
					return;
				}

				npc.faceEntity(player);

				if (player.getTreasureTrailsManager().useNPC(npc))
					return;
				Object[] shipAttributes = BoatingDialouge.getBoatForShip(npc.getId());
				if (shipAttributes != null)
					player.getDialogueManager().startDialogue("BoatingDialouge", npc.getId());
				else if (npc.getId() == 3709)
					player.getDialogueManager().startDialogue("CostumesD", npc.getId());
				else if (npc.getId() == PkTournament.NPC_ID) {
					player.getDialogueManager().startDialogue("PkTournamentD", npc.getId(), 0);
				}
				else if (npc.getId() == 3403)
					ShopsHandler.openShop(player, 274);
				else if (npc.getId() == 1001)
					ShopsHandler.openShop(player, 272);
				else if (npc.getId() == 5141)
					player.getDialogueManager().startDialogue("UgiDialouge", npc);
				else if (npc.getId() == 15513 || npc.getId() >= 11303 && npc.getId() <= 11307)
					player.getDialogueManager().startDialogue("ServantDialogue", npc.getId());
				else if (npc.getId() == 2290)
					player.getDialogueManager().startDialogue("SirTiffyCashien", npc.getId());
				else if (npc.getId() == 4511)
					player.getDialogueManager().startDialogue("Oneiromancer", npc.getId());
				else if (npc.getId() == 8171 || npc.getId() == 8172)
					player.getDialogueManager().startDialogue("Dimintheis", npc.getId());
				else if (npc.getId() == 8266)
					player.getDialogueManager().startDialogue("Ghommel");
				else if (npc.getId() == 5530)
					player.getDialogueManager().startDialogue("MawnisBurowger");
				else if (npc.getId() == 5532)
					player.getDialogueManager().startDialogue("SorceressGardenNPCs", npc);
				else if (npc.getId() == 5563)
					player.getDialogueManager().startDialogue("SorceressGardenNPCs", npc);
				else if (npc.getId() == 6892 || npc.getId() == 6893)
					player.getDialogueManager().startDialogue("PetShopOwner", npc.getId());
				else if (npc.getId() == 780)
					player.getDialogueManager().startDialogue("Gertrude", npc.getId());
				else if (npc.getId() == 15907)
					player.getDialogueManager().startDialogue("OsmanDialogue", npc.getId());
				else if (npc.getId() == 837)
					player.getDialogueManager().startDialogue("ShantyGuardD", npc.getId());
				else if (npc.getId() == 9712)
					player.getDialogueManager().startDialogue("DungeoneeringTutor");
				else if (npc.getId() == 836)
					player.getDialogueManager().startDialogue("ShantyD");
				else if (npc.getId() == 2301)
					player.getDialogueManager().startDialogue("ShantyMonkeyD");
				else if (npc.getId() == 13651)
					player.getDialogueManager().startDialogue("EasterBunny2018");
				else if (npc.getId() == 5915)
					player.getDialogueManager().startDialogue("ClaimClanItem", npc.getId(), 20709);
				else if (npc.getId() == 14872)
					player.getDialogueManager().startDialogue("MiladeDeathD");
				else if (npc.getId() == 3037)
					player.getDialogueManager().startDialogue("Zahur", npc.getId());
				else if (npc.getId() == 4247 || npc.getId() == 6715)
					player.getDialogueManager().startDialogue("EstateAgentD", npc.getId());
				else if (npc.getId() == 1526)
					player.getDialogueManager().startDialogue("Lanthus", npc.getId());
				else if (npc.getId() == 13633)
					player.getDialogueManager().startDialogue("ClaimClanItem", npc.getId(), 20708);
				else if ((npc.getId() >= 2291 && npc.getId() <= 2294) || npc.getId() == 2296 || npc.getId() == 2298)
					player.getDialogueManager().startDialogue("RugMerchantD", false, 0);
				else if (npc.getId() == 171)
					player.getDialogueManager().startDialogue("Brimstail", npc);
				else if (npc.getId() == 28491)
					player.getDialogueManager().startDialogue("TheCollector", npc.getId());
				else if (npc.getId() == 250)
					player.getDialogueManager().startDialogue("LadyOfTheLake");
				else if (npc.getId() == 3705)
					player.getDialogueManager().startDialogue("Max");
				else if (npc.getId() == 6524)
					player.getDialogueManager().startDialogue("BobBarterD");
				else if (npc.getId() == 8091)
					player.getDialogueManager().startDialogue("StarSpriteD");
				else if (EconomyManager.isEconomyManagerNpc(npc.getId()))
					EconomyManager.processManagerNpcClick(player, npc.getId(), false);
				else if (npc.getId() == 15451 && npc instanceof FireSpirit) {
					FireSpirit spirit = (FireSpirit) npc;
					spirit.giveReward(player);
				} else if (npc.getId() == 1204 || npc.getId() == 1206 || npc.getId() == 4649) {
					boolean onDuty = Utils.random(2) == 0;
					player.getDialogueManager().startDialogue("SimpleNPCMessage", npc.getId(), onDuty ? "I'm on duty, this isn't the time to be talking to strangers" : "It isn't safe here, its best for you to leave now...");
					player.getPackets().sendGameMessage("After all I've been through I think I can handle myself...");
				} else if (npc.getId() == 398 || npc.getId() == 399)
					player.getDialogueManager().startDialogue("SimpleNPCMessage", npc.getId(), "Welcome. I hope you enjoy your time in the Legends' Guild.");
				else if (npc.getId() >= 1 && npc.getId() <= 6 || npc.getId() >= 7875 && npc.getId() <= 7884)
					player.getDialogueManager().startDialogue("Man", npc.getId());
				else if (npc.getId() == 16018)
					player.getDialogueManager().startDialogue("GamblerKingD");
				else if (npc.getId() == 198)
					player.getDialogueManager().startDialogue("Guildmaster", npc.getId());
				else if (npc.getId() == 20013)
						player.getDialogueManager().startDialogue("SimpleNPCMessage", npc.getId(), "Use the item you'd like to un/note on me in exchange for 50 coins each.");
				else if (npc.getId() == 36) {
					int moleSkinCount = player.getInventory().getAmountOf(7418);
					int moleClawCount = player.getInventory().getAmountOf(7416);
					int moleNoseCount = player.getInventory().getAmountOf(19769);
					if (moleSkinCount == 0 && moleClawCount == 0 && moleNoseCount == 0) {
						player.getDialogueManager().startDialogue("SimpleNPCMessage", npc.getId(), "Come back once you have some mole skin, claws or nose.");
						return;
					}
					player.getInventory().deleteItem(7418, moleSkinCount);
					player.getInventory().deleteItem(7416, moleClawCount);
					player.getInventory().deleteItem(19769, moleNoseCount);
					for (int i = 0; i < moleSkinCount + moleClawCount + moleNoseCount; i++) {
						int type = Utils.random(3);
						player.getInventory().addItem(type == 0 ? 5075 : type == 1 ? 5073 : 7413, 1);
					}
					player.getDialogueManager().startDialogue("SimpleNPCMessage", npc.getId(), "Thanks!");
				}else if (npc.getId() == 11509)
					player.getDialogueManager().startDialogue("CommodreTyr", npc.getId());
				else if (npc.getId() == 9462 || npc.getId() == 9464 || npc.getId() == 9466)
					Strykewyrm.handleStomping(player, npc);
				else if (npc.getId() == 2417)
					WildyWyrm.handleInspect(player, npc);
				else if (npc.getId() == 1208)
					player.getDialogueManager().startDialogue("QuartsMaster");
				else if (npc.getId() == 9707)
					player.getDialogueManager().startDialogue("FremennikShipmaster", npc.getId(), true);
				else if (npc.getId() == 9708)
					player.getDialogueManager().startDialogue("FremennikShipmaster", npc.getId(), false);
				else if (npc.getId() == 456)
					player.getDialogueManager().startDialogue("FatherAereck", npc.getId());
				else if (npc.getId() == 1793)
					player.getDialogueManager().startDialogue("CraftingMaster", npc.getId());
				else if (npc.getId() == 3344 || npc.getId() == 3345)
					MutatedZygomites.transform(player, npc);
				else if (npc.getId() == 5585)
					player.getDialogueManager().startDialogue("SkillAlchemist");
				else if (npc.getId() == 11270)
					ShopsHandler.openShop(player, 19);
				else if (npc.getId() == 1163 || npc.getId() == 2483)
					ShopsHandler.openShop(player, 267);
				else if (npc.getId() == 25513)
					player.getDialogueManager().startDialogue("UpgradeVoidKnightD");
				else if (npc.getId() == 1679)
					ShopsHandler.openShop(player, 244);
				else if (npc.getId() == 1680)
					ShopsHandler.openShop(player, 245);
				else if (npc.getId() == 2713)
					ShopsHandler.openShop(player, 246);
				else if (npc.getId() == 562)// 107
					player.getDialogueManager().startDialogue("CandleMaker");
				else if (npc.getId() == 576)
					player.getDialogueManager().startDialogue("Harry");
				else if (npc.getId() == 563)
					player.getDialogueManager().startDialogue("Arhein");
				else if (npc.getId() == 575)
					player.getDialogueManager().startDialogue("Hickton");
				else if (npc.getId() == 2305)
					player.getDialogueManager().startDialogue("Vannesa");
				else if (npc.getId() == 8527) // nomad capes
					player.getDialogueManager().startDialogue("SimpleNPCMessage", 8527, player.getQuestManager().completedQuest(Quests.NOMADS_REQUIEM) ? "Right click me to see my rewards brother!" : "You must kill nomad inside the tent there to see my rewards.");
				else if (npc.getId() == 6537 || npc.getId() == 6539)
					player.getDialogueManager().startDialogue("Mandrith_Nastroth", npc.getId());
				else if (npc.getId() == 2676)
					player.getDialogueManager().startDialogue("MakeOverMage", npc.getId(), 0);
				else if (npc.getId() == 598)
					player.getDialogueManager().startDialogue("Hairdresser", npc.getId());
				else if (npc.getId() == 548)
					player.getDialogueManager().startDialogue("Thessalia", npc.getId());
				else if (npc.getId() == 659)
					player.getDialogueManager().startDialogue("PartyPete");
				else if (npc.getId() == 579)
					player.getDialogueManager().startDialogue("DrogoDwarf", npc.getId());
				else if (npc.getId() == 3799) // void general store
					player.getDialogueManager().startDialogue("GeneralStore", npc.getId(), 122);
				else if (npc.getId() == 471) // tree gnome village general store
					player.getDialogueManager().startDialogue("GeneralStore", npc.getId(), 124);
				else if (npc.getId() == 582) // dwarves general store
					player.getDialogueManager().startDialogue("GeneralStore", npc.getId(), 31);
				else if (npc.getId() == 1917)
					player.getDialogueManager().startDialogue("GeneralStore", npc.getId(), 106);
				else if (npc.getId() == 932)
					player.getDialogueManager().startDialogue("GeneralStore", npc.getId(), 113);
				else if (npc.getId() == 1040)
					player.getDialogueManager().startDialogue("GeneralStore", npc.getId(), 81);
				else if (npc.getId() == 209) // cannon shop
					player.getDialogueManager().startDialogue("Nulodion", npc.getId());
				else if (npc.getId() == 15099)
					player.getDialogueManager().startDialogue("Freda");
				else if (npc.getId() == 4358)
					player.getDialogueManager().startDialogue("CaveyDavey");
				else if (npc.getId() == 15087)
					player.getDialogueManager().startDialogue("DeathPlatueSoldier");
				else if (npc.getId() == 1334)
					player.getDialogueManager().startDialogue("Jossik", npc.getId());
				else if (npc.getId() == 904)
					player.getDialogueManager().startDialogue("ChamberGaurdian", npc.getId());
				else if (npc.getId() == 2259)
					player.getDialogueManager().startDialogue("MageOfZamorak");
				else if (npc.getId() == 903)
					player.getDialogueManager().startDialogue("Lundail", npc.getId());
				else if (npc.getId() == 528 || npc.getId() == 529) // edge
					player.getDialogueManager().startDialogue("GeneralStore", npc.getId(), 1);
				else if (npc.getId() == 522 || npc.getId() == 523) // varrock
					player.getDialogueManager().startDialogue("GeneralStore", npc.getId(), 8);
				else if (npc.getId() == 520 || npc.getId() == 521) // lumbridge
					player.getDialogueManager().startDialogue("GeneralStore", npc.getId(), 4);
				else if (npc.getId() == 2825 || npc.getId() == 2826)
					player.getDialogueManager().startDialogue("PiratePete", npc);
				else if (npc.getId() == 1301)
					player.getDialogueManager().startDialogue("Yrsa");
				else if (npc.getId() == 594)
					player.getDialogueManager().startDialogue("Nurmof", npc.getId());
				else if (npc.getId() == 665)
					player.getDialogueManager().startDialogue("BootDwarf", npc.getId());
				else if (npc.getId() == 5913) // Aubury
					player.getDialogueManager().startDialogue("Aubury", npc);
				else if (npc.getId() == 382 || npc.getId() == 3294 || npc.getId() == 4316)
					player.getDialogueManager().startDialogue("MiningGuildDwarf", npc.getId(), false);
				else if (npc.getId() == 3295)
					player.getDialogueManager().startDialogue("MiningGuildDwarf", npc.getId(), true);
				else if (npc.getId() == 537)
					player.getDialogueManager().startDialogue("Scavvo", npc.getId());
				else if (npc.getId() == 536)
					player.getDialogueManager().startDialogue("GeneralStore", npc.getId(), 17);
				else if (npc.getId() == 4563) // Crossbow Shop
					player.getDialogueManager().startDialogue("Hura", npc.getId());
				else if (npc.getId() == 2617)
					player.getDialogueManager().startDialogue("TzHaarMejJal", npc.getId());
				else if (npc.getId() == 3802 || npc.getId() == 6140 || npc.getId() == 6141)
					player.getDialogueManager().startDialogue("LanderSquire", npc.getId());
				else if (npc.getId() == 2618)
					player.getDialogueManager().startDialogue("TzHaarMejKah", npc.getId());
				else if (npc.getId() == 1595)
					player.getDialogueManager().startDialogue("SaniBoch", false);
				else if (npc.getId() == 15149)
					player.getDialogueManager().startDialogue("MasterOfFear", 0);
				else if (SlayerMaster.startInteractionForId(player, npc.getId(), 1))
					return;
				else if (npc.getId() == 11519)
					player.getDialogueManager().startDialogue("Mariah");
				else if (npc.getId() == 11516)
					player.getDialogueManager().startDialogue("TerryGord");
				else if (npc.getId() == 11517)
					player.getDialogueManager().startDialogue("MrsGord");
				else if (npc.getId() == 519 || npc.getId() == 3797 || npc.getId() == 9711 || npc.getId() == 944 || npc.getId() == 2024)
					player.getDialogueManager().startDialogue("RepairSquire", npc.getId());
				else if (npc.getId() == 3790 || npc.getId() == 3791 || npc.getId() == 3792)
					player.getDialogueManager().startDialogue("VoidKnightExchange", npc.getId());
				else if (npc.getId() == 741)
					ShopsHandler.openShop(player, 222);
				else if (npc.getId() == 488)
					ShopsHandler.openShop(player, 223);
				else if (npc.getId() == 883)
					ShopsHandler.openShop(player, 243);
				else if (npc.getName().toLowerCase().contains("impling")) {
					FlyingEntityHunter.captureFlyingEntity(player, npc);
				} else {
					switch (npc.getDefinitions().getToNPCName(player).toLowerCase()) {
					case "void knight":
						player.getDialogueManager().startDialogue("VoidKnightExchange", npc.getId());
						break;
					case "sheep":
						SheepShearing.shearAttempt(player, npc);
						break;
					case "musician":
					case "drummer":
						if (player.isResting()) {
							player.stopAll();
							return;
						}
						if (player.getEmotesManager().isDoingEmote()) {
							player.getPackets().sendGameMessage("You can't rest while perfoming an emote.");
							return;
						}
						if (player.isLocked()) {
							player.getPackets().sendGameMessage("You can't rest while perfoming an action.");
							return;
						}
						player.stopAll();
						player.getActionManager().setAction(new Rest(true));
						break;
					default:
						if (npc.getDefinitions().hasOption("Trade") || npc.getId() == 7065 || npc.getId() == 7066) 
							player.getDialogueManager().startDialogue("DefaultTradeDialouge", npc.getId(), stream);
						else if (npc.getDefinitions().toNPC(player).hasOption("Talk-to"))
								player.getDialogueManager().startDialogue("SimpleNPCMessage", npc.getId(), "Have a nice time playing "+Settings.SERVER_NAME+"!");
						else {
							player.getPackets().sendGameMessage("Nothing interesting happens.");
							if (Settings.DEBUG)
								System.out.println("cliked 1 at npc[" + npc.getIndex() + "] id : " + npc.getId() + ", " + npc.getX() + ", " + npc.getY() + ", " + npc.getPlane());
						}
					break;
					}
				}
			}
		}));
	}

	public static void handleOption2(final Player player, InputStream stream) {
		int npcIndex = stream.readUnsignedShort128();
		boolean forceRun = stream.read128Byte() == 1;
		final NPC npc = World.getNPCs().get(npcIndex);
		if (npc == null || npc.isCantInteract() || npc.isDead() || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId()) || player.isLocked())
			return;
		player.stopAll();
		if (forceRun)
			player.setRun(forceRun);

		if(handle(player, npc, 2))
			return;

		if (npc.getId() == 14707 || npc.getId() == 4296 || npc.getDefinitions().name.contains("Banker") || npc.getDefinitions().name.contains("banker")) {
			player.setRouteEvent(new RouteEvent(npc, new Runnable() {
				@Override
				public void run() {
					player.faceEntity(npc);
					if (!player.withinDistance(npc, 2))
						return;
					npc.faceEntity(player);
					player.getBank().openBank();
					return;
				}
			}, true));
			return;
		}
		if (npc.getId() == 4250) {
			player.setRouteEvent(new RouteEvent(npc, new Runnable() {
				@Override
				public void run() {
					player.faceEntity(npc);
					if (!player.withinDistance(npc, 2))
						return;
					npc.faceEntity(player);
					Sawmill.openPlanksConverter(player);
					return;
				}
			}, true));
			return;
		}
		if (npc.getDefinitions().name.toLowerCase().equals("grand exchange clerk")) {
			player.setRouteEvent(new RouteEvent(npc, new Runnable() {
				@Override
				public void run() {
					player.faceEntity(npc);
					if (!player.withinDistance(npc, 2))
						return;
					npc.faceEntity(player);
					player.getGeManager().openGrandExchange();
					return;
				}
			}, true));
			return;
		}
		FishingSpots spot = FishingSpots.forId(npc.getId() | 2 << 24);
		if (spot != null) {
			player.setRouteEvent(new RouteEvent(npc, new Runnable() {
				@Override
				public void run() {
					player.faceEntity(npc);
					if (!Utils.isOnRange(player, npc, 1))
						return;
					player.getActionManager().setAction(new Fishing(spot, npc));
					return;
				}
			}, true));
			return;
		}
		player.setRouteEvent(new RouteEvent(npc, new Runnable() {
			@Override
			public void run() {
				npc.setStopRandomWalk();
				npc.resetWalkSteps();
				player.faceEntity(npc);
				if (player.getTreasureTrailsManager().useNPC(npc))
					return;
				if (npc instanceof Pet) {
					((Pet) npc).interact(player, 2);
					return;
				}
				if (npc.getId() >= TheNightmareInstance.NIGHTMARE_LOBBY_EMPTY && npc.getId() <= TheNightmareInstance.NIGHTMARE_LOBBY_PHASE_3) {
					TheNightmareInstance.inspect(player);
					return;
				}
				PickPocketableNPC pocket = PickPocketableNPC.get(npc.getId());
				if (pocket != null) {
					player.getActionManager().setAction(new PickPocketAction(npc, pocket));
					return;
				}
				npc.faceEntity(player);
				if (!player.getControlerManager().processNPCClick2(npc))
					return;
				if (npc instanceof Familiar) {
					Familiar familiar = (Familiar) npc;
					if (player.getFamiliar() != familiar) {
						player.getPackets().sendGameMessage("That isn't your familiar.");
						return;
					}
					if (familiar.getDefinitions().hasOption("store") || npc.getDefinitions().hasOption("withdraw")) {
						player.getFamiliar().store();
					} else if (familiar.getDefinitions().hasOption("cure")) {
						if (!player.getPoison().isPoisoned()) {
							player.getPackets().sendGameMessage("Your arent poisoned or diseased.");
							return;
						} else {
							player.getFamiliar().drainSpecial(2);
							player.addPoisonImmune(120);
						}
					} else if (familiar.getDefinitions().hasOption("interact")) {
						Object[] paramaters = new Object[2];
						Pouch pouch = player.getFamiliar().getPouch();
						if (pouch == Pouch.SPIRIT_GRAAHK) {
							paramaters[0] = "Karamja's Hunter Area";
							paramaters[1] = new WorldTile(2787, 3000, 0);
						} else if (pouch == Pouch.SPIRIT_KYATT) {
							paramaters[0] = "Piscatorius Hunter Area";
							paramaters[1] = new WorldTile(2339, 3636, 0);
						} else if (pouch == Pouch.SPIRIT_LARUPIA) {
							paramaters[0] = "Feldip Hills Hunter Area";
							paramaters[1] = new WorldTile(2557, 2913, 0);
						} else if (pouch == Pouch.ARCTIC_BEAR) {
							paramaters[0] = "Rellekka Hills Hunter Area";
							paramaters[1] = new WorldTile(2721, 3779, 0);
						} else if (pouch == Pouch.LAVA_TITAN) {
							paramaters[0] = "Lava Maze - *Deep Wilderness*";
							paramaters[1] = new WorldTile(3028, 3840, 0);
						} else
							return;
						player.getDialogueManager().startDialogue("FamiliarInspection", paramaters[0], paramaters[1]);
					}
					return;
				} else if (npc instanceof GraveStone) {
					GraveStone grave = (GraveStone) npc;
					grave.repair(player, false);
					return;
				}
				switch (npc.getDefinitions().name.toLowerCase()) {
				case "void knight":
					CommendationExchange.openExchangeShop(player);
					return;
				}
				Object[] shipAttributes = BoatingDialouge.getBoatForShip(npc.getId());
				if (shipAttributes != null) {
					CarrierTravel.sendCarrier(player, (Carrier) shipAttributes[0], (boolean) shipAttributes[1]);
				} else if (npc.getId() == 9707)
					FremennikShipmaster.sail(player, true);
				else if (npc.getId() == 9708)
					FremennikShipmaster.sail(player, false);
				else if (npc.getId() == 943)
					ShopsHandler.openShop(player, player.getRegionId() == 13393 ? 902 : 913);
				else if (npc.getId() == 6537)
					ShopsHandler.openShop(player, 906);
				else if (npc.getId() == 28531)
					ShopsHandler.openShop(player, 281);
				else if (npc.getId() == 28530)
					ShopsHandler.openShop(player, 280);
				else if (npc.getId() == 706)
					ShopsHandler.openShop(player, 278);
				else if (npc.getId() == 28532)
					ShopsHandler.openShop(player, 276);
				else if (npc.getId() == 16020)
					ShopsHandler.openShop(player, 912);
				else if (npc.getId() == 1694)
					ShopsHandler.openShop(player, 911);
				else if (npc.getId() == 27953)
					ShopsHandler.openShop(player, 270);
				else if (npc.getId() == 28325)
					ShopsHandler.openShop(player, 275);
				else if (npc.getId() == 28037)
					ShopsHandler.openShop(player, 268);
				else if (npc.getId() == 28036)
					ShopsHandler.openShop(player, 271);
				else if (npc.getId() == 2253 || npc.getId() == 946)
					ShopsHandler.openShop(player, 900);
				else if (npc.getId() == 947)
					ShopsHandler.openShop(player, 914);
				else if (npc.getId() == 28521)
					ShopsHandler.openShop(player, 277);
				else if (npc.getId() == 27952)
					ShopsHandler.openShop(player, 269);
				else if (npc.getId() == 944)
					ShopsHandler.openShop(player, 903);
				else if (npc.getId() == 945)
					ShopsHandler.openShop(player, 907);
				else if (npc.getId() == 16006)
					ShopsHandler.openShop(player, 908);
				else if (npc.getId() == 13727)
					ShopsHandler.openShop(player, 909);
				else if (npc.getId() == 949)
					ShopsHandler.openShop(player, 904);
				else if (npc.getId() == 954)
					ShopsHandler.openShop(player, 905);
				else if (npc.getId() == 2024)
					ShopsHandler.openShop(player, 910);
				else if (npc.getId() == 1835)
					ShopsHandler.openShop(player, 915);
				else if (npc.getId() == 1918)
					ShopsHandler.openShop(player, 916);
				else if (npc.getId() == 3709)
					ShopsHandler.openShop(player, 917);
				else if (npc.getId() == 1687)
					ShopsHandler.openShop(player, 282);
				else if (npc.getId() == 29271) 
					player.useStairs(-1, new WorldTile(2473, 3995, 0), 0, 1);
				else if (npc.getId() == 5668 || npc.getId() == 5667) {
					player.lock();
					FadingScreen.fade(player, 0, new Runnable() {
						@Override
						public void run() {
							boolean mos = npc.getId() == 5667;
							player.getPackets().sendGameMessage("You board the boat and travel to the "+(mos ? "Harmony Island" : ("docks of Mos Le'Harmless."))+".");
							player.useStairs(-1, mos ? new WorldTile(3784, 2824, 0) : new WorldTile(3684, 2953, 0), 0, 1);
						}
					});
				} else if (npc.getId() == 510) //shillo village TODO make real dialogue in future
					player.useStairs(-1, new WorldTile(2832, 2954, 0), 0, 1);
				else if (npc.getId() == 962) {
					player.getPackets().sendGameMessage("The nurse restores your hitpoints.");
					player.setHitpoints(player.getMaxHitpoints());
				} else if (npc.getId() == 5506)
					CureYakHide.cure(player);
				else if (SlayerMaster.startInteractionForId(player, npc.getId(), 2))
					return;
				else if (npc.getId() == 2619 || npc.getId() == 13455 || npc.getId() == 2617 || npc.getId() == 2618 || npc.getId() == 15194)
					player.getBank().openBank();
				else if ((npc.getId() == 14849 || npc.getId() == 1610) && npc instanceof ConditionalDeath)
					((ConditionalDeath) npc).useHammer(player);
				else if (npc.getId() == 28132) 
					VorkathLair.travel(player);
				else if (npc.getId() == 528 || npc.getId() == 529)
					ShopsHandler.openShop(player, 1);
				else if (npc.getId() == 5198) 
					ShopsHandler.openShop(player, 224);
				else if (npc.getId() == 4251) //Garden Centre
					ShopsHandler.openShop(player, 130);
				else if (npc.getId() == 3799)
					ShopsHandler.openShop(player, 122);
				else if (npc.getId() == 27056) //zeah
					ShopsHandler.openShop(player, 249);
				else if (npc.getId() == 27203) 
					ShopsHandler.openShop(player, 250);
				else if (npc.getId() == 27204) 
					ShopsHandler.openShop(player, 248);
				else if (npc.getId() == 26953) 
					ShopsHandler.openShop(player, 251);
				else if (npc.getId() == 26943) 
					ShopsHandler.openShop(player, 252);
				else if (npc.getId() == 26945) 
					ShopsHandler.openShop(player, 253);
				else if (npc.getId() == 26944) 
					ShopsHandler.openShop(player, 254);
				else if (npc.getId() == 27240) 
					ShopsHandler.openShop(player, 255);
				else if (npc.getId() == 27090)
					ShopsHandler.openShop(player, 256);
				else if (npc.getId() == 27088)
					ShopsHandler.openShop(player, 257);
				else if (npc.getId() == 27071)
					ShopsHandler.openShop(player, 258);
				else if (npc.getId() == 27208)
					ShopsHandler.openShop(player, 259);
				else if (npc.getId() == 27201)
					ShopsHandler.openShop(player, 260);
				else if (npc.getId() == 20305)
					ShopsHandler.openShop(player, 262);
				else if (npc.getId() == 26963)
					ShopsHandler.openShop(player, 263);
				else if (npc.getId() == 27202)
					ShopsHandler.openShop(player, 264);
				else if (npc.getId() == 26986)
					ShopsHandler.openShop(player, 265);
				else if (npc.getId() == 26964)
					ShopsHandler.openShop(player, 266);
				else if (npc.getId() == 471) // tree gnome village general store
					ShopsHandler.openShop(player, 124);
				else if (npc.getId() == 9159)
					ShopsHandler.openShop(player, 123);
				else if ((npc.getId() >= 2291 && npc.getId() <= 2294) || npc.getId() == 2296 || npc.getId() == 2298)
					player.getDialogueManager().startDialogue("RugMerchantD", true, 0);
				else if (npc.getId() == 519)
					ShopsHandler.openShop(player, 2);
				else if (npc.getId() == 520 || npc.getId() == 521)
					ShopsHandler.openShop(player, 4);
				else if (npc.getId() == 538)
					ShopsHandler.openShop(player, 6);
				else if (npc.getId() == 522 || npc.getId() == 523)
					ShopsHandler.openShop(player, 8);
				else if (npc.getId() == 797)
					ShopsHandler.openShop(player, 115);
				else if (npc.getId() == 546)
					ShopsHandler.openShop(player, 10);
				else if (npc.getId() == 4293)
					ShopsHandler.openShop(player, 120);
				else if (npc.getId() == 516)
					ShopsHandler.openShop(player, 125);
				else if (npc.getId() == 517)
					ShopsHandler.openShop(player, 126);
				else if (npc.getId() == 4294)
					ShopsHandler.openShop(player, 121);
				else if (npc.getId() == 4295)
					ShopsHandler.openShop(player, 119);
				else if (npc.getId() == 14620)
					ShopsHandler.openShop(player, 118);
				else if (npc.getId() == 9711)
					DungeonRewardShop.openRewardShop(player);
				else if (npc.getId() == 5915)
					player.getDialogueManager().startDialogue("ClaimClanItem", npc.getId(), 20709);
				else if (npc.getId() == 13633)
					player.getDialogueManager().startDialogue("ClaimClanItem", npc.getId(), 20708);
				else if (npc.getId() == 2824 || npc.getId() == 1041)
					player.getDialogueManager().startDialogue("TanningD", npc.getId());
				else if (npc.getId() == 535 || npc.getId() == 534)
					ShopsHandler.openShop(player, 55);
				else if (npc.getId() == 836)
					ShopsHandler.openShop(player, 92);
				else if (npc.getId() == 2352)
					ShopsHandler.openShop(player, 74);
				else if (npc.getId() == 2259)
					ShopsHandler.openShop(player, 112);
				else if (npc.getId() == 1917)
					ShopsHandler.openShop(player, 106);
				else if (npc.getId() == 932)
					ShopsHandler.openShop(player, 113);
				else if (npc.getId() == 933)
					ShopsHandler.openShop(player, 114);
				else if (npc.getId() == 747)
					ShopsHandler.openShop(player, 7);
				else if (npc.getId() == 2353)
					ShopsHandler.openShop(player, 75);
				else if (npc.getId() == 2356)
					ShopsHandler.openShop(player, 76);
				else if (npc.getId() == 3166)
					ShopsHandler.openShop(player, 89);
				else if (npc.getId() == 3161)
					ShopsHandler.openShop(player, 90);
				else if (npc.getId() == 2154)
					ShopsHandler.openShop(player, 105);
				else if (npc.getId() == 2160)
					ShopsHandler.openShop(player, 99);
				else if (npc.getId() == 2151)
					ShopsHandler.openShop(player, 104);
				else if (npc.getId() == 563)
					ShopsHandler.openShop(player, 108);
				else if (npc.getId() == 576)
					ShopsHandler.openShop(player, 110);
				else if (npc.getId() == 562)
					ShopsHandler.openShop(player, 107);
				else if (npc.getId() == 575)
					ShopsHandler.openShop(player, 109);
				else if (npc.getId() == 2305)
					ShopsHandler.openShop(player, 111);
				else if (npc.getId() == 2161)
					ShopsHandler.openShop(player, 103);
				else if (npc.getId() == 2152)
					ShopsHandler.openShop(player, 100);
				else if (npc.getId() == 2153)
					ShopsHandler.openShop(player, 101);
				else if (npc.getId() == 4248)
					ShopsHandler.openShop(player, 102);
				else if (npc.getId() == 3162)
					ShopsHandler.openShop(player, 91);
				else if (npc.getId() == 564)
					ShopsHandler.openShop(player, 53);
				else if (npc.getId() == 4516)
					ShopsHandler.openShop(player, 70);
				else if (npc.getId() == 4518)
					ShopsHandler.openShop(player, 66);
				else if (npc.getId() == 566)
					ShopsHandler.openShop(player, 52);
				else if (npc.getId() == 540)
					ShopsHandler.openShop(player, 61);
				else if (npc.getId() == 541)
					ShopsHandler.openShop(player, 62);
				else if (npc.getId() == 542)
					ShopsHandler.openShop(player, 63);
				else if (npc.getId() == 544)
					ShopsHandler.openShop(player, 64);
				else if (npc.getId() == 2620) // TzHaar-Hur-Tel's Equipment
					// Store
					ShopsHandler.openShop(player, 77);
				else if (npc.getId() == 2622) // TzHaar-Hur-Lek's Ore and Gem
					// Store
					ShopsHandler.openShop(player, 78);
				else if (npc.getId() == 2623) // TzHaar-Mej-Roh's Rune Store
					ShopsHandler.openShop(player, 79);
				else if (npc.getId() == 583) // Betty's Magic Emporium
					ShopsHandler.openShop(player, 67);
				else if (npc.getId() == 587) // Jatix's Herblore Shop
					ShopsHandler.openShop(player, 69);
				else if (npc.getId() == 659)
					ShopsHandler.openShop(player, 273);
				else if (npc.getId() == 545)
					ShopsHandler.openShop(player, 65);
				else if (npc.getId() == 5510)
					ShopsHandler.openShop(player, 56);
				else if (npc.getId() == 6892 || npc.getId() == 6893)
					PetShopOwner.openShop(player);
				else if (npc.getId() == 11475)
					ShopsHandler.openShop(player, 9);
				else if (npc.getId() == 1658)
					ShopsHandler.openShop(player, 58);
				else if (npc.getId() == 461)
					ShopsHandler.openShop(player, 59);
				else if (npc.getId() == 8228)
					StealingCreationShop.openInterface(player);
				else if (npc.getId() == 593)
					ShopsHandler.openShop(player, 60);
				else if (npc.getId() == 2039) // Uglug nar's shop
					ShopsHandler.openShop(player, 283);
				else if (npc.getId() == 5113) // Hunter Expert's shop
					ShopsHandler.openShop(player, 22);
				else if (npc.getId() == 588) // Davon's Amulet Store
					ShopsHandler.openShop(player, 133);
				else if (npc.getId() == 1860) // Brian's Archery Supplies
					ShopsHandler.openShop(player, 134);
				else if (npc.getId() == 683) // Dargaud's Bows and Arrows
					ShopsHandler.openShop(player, 135);
				else if (npc.getId() == 682) // Archery Appendages
					ShopsHandler.openShop(player, 136);
				else if (npc.getId() == 4558) // Crossbow Shop by Hirko
					ShopsHandler.openShop(player, 33);
				else if (npc.getId() == 4559) // Crossbow Shop by Holoy
					ShopsHandler.openShop(player, 33);
				else if (npc.getId() == 581) // Wayne's chains
					ShopsHandler.openShop(player, 137);
				else if (npc.getId() == 554) // Fancy Clothes Store
					ShopsHandler.openShop(player, 138);
				else if (npc.getId() == 601) // Rometti's Fine Fashions
					ShopsHandler.openShop(player, 139);
				else if (npc.getId() == 3921) // Miscellanian Clothes Shop
					ShopsHandler.openShop(player, 140);
				else if (npc.getId() == 3205) // Pie Shop (Varrock mill)
					ShopsHandler.openShop(player, 141);
				else if (npc.getId() == 600) // Grand Tree Groceries
					ShopsHandler.openShop(player, 142);
				else if (npc.getId() == 603) // Funch's Fine Groceries
					ShopsHandler.openShop(player, 143);
				else if (npc.getId() == 585) // Rommik's Crafty Supplies
					ShopsHandler.openShop(player, 144);
				else if (npc.getId() == 5268) // Jamila's Craft Stall
					ShopsHandler.openShop(player, 145);
				else if (npc.getId() == 1437) // Hamab's Crafting Emporium
					ShopsHandler.openShop(player, 146);
				else if (npc.getId() == 2307) // Alice's Farming Shop
					ShopsHandler.openShop(player, 147);
				else if (npc.getId() == 14860) // Head Farmer Jones's Farming shop
					ShopsHandler.openShop(player, 148);
				else if (npc.getId() == 2306) // Richard's Farming Shop
					ShopsHandler.openShop(player, 149);
				else if (npc.getId() == 2304) // Sarah's Farming Shop
					ShopsHandler.openShop(player, 150);
				else if (npc.getId() == 8864) // Lumbridge Fishing Supplies
					ShopsHandler.openShop(player, 151);
				else if (npc.getId() == 14879) // Nicholas Angle's Fishing Shop
					ShopsHandler.openShop(player, 152);
				else if (npc.getId() == 592) // Fishing Guild Shop
					ShopsHandler.openShop(player, 153);
				else if (npc.getId() == 1393) // Island Fishmonger
					ShopsHandler.openShop(player, 155);
				else if (npc.getId() == 1369) // Island Fishmonger (Etcetria)
					ShopsHandler.openShop(player, 156);
				else if (npc.getId() == 3824) // Arnold's Eclectic Supplies
					ShopsHandler.openShop(player, 157);
				else if (npc.getId() == 571) // Bakery Stall
					ShopsHandler.openShop(player, 158);
				else if (npc.getId() == 7054) // Fresh Meat
					ShopsHandler.openShop(player, 159);
				else if (npc.getId() == 851) // Gianne's Restaurant
					ShopsHandler.openShop(player, 160);
				else if (npc.getId() == 5487) // Keepa Kettilon's Store
					ShopsHandler.openShop(player, 161);
				else if (npc.getId() == 3923) // Miscellanian Food Shop
					ShopsHandler.openShop(player, 162);
				else if (npc.getId() == 5264) // Nathifa's Bake Stall
					ShopsHandler.openShop(player, 163);
				else if (npc.getId() == 793) // The Shrimp and Parrot
					ShopsHandler.openShop(player, 164);
				else if (npc.getId() == 1433) // Solihib's Food Stall
					ShopsHandler.openShop(player, 165);
				else if (npc.getId() == 596) // Tony's Pizza Bases
					ShopsHandler.openShop(player, 166);
				else if (npc.getId() == 584) // Tony's Pizza Bases
					ShopsHandler.openShop(player, 167);
				else if (npc.getId() == 570) // Ardougne Gem Stall
					ShopsHandler.openShop(player, 168);
				else if (npc.getId() == 578) // Frincos's Fabulous Herb Store
					ShopsHandler.openShop(player, 169);
				else if (npc.getId() == 874) // Grud's Herblore Stall
					ShopsHandler.openShop(player, 170);
				else if (npc.getId() == 5109) // Nardah Hunter Shop
					ShopsHandler.openShop(player, 171);
				else if (npc.getId() == 14864) // Ayleth Beaststalker's Hunting Supplies Shop
					ShopsHandler.openShop(player, 172);
				else if (npc.getId() == 2198) // Kjut's Kebabs
					ShopsHandler.openShop(player, 173);
				else if (npc.getId() == 580) // Flynn's Mace Market
					ShopsHandler.openShop(player, 174);
				else if (npc.getId() == 14906) // Carwen Essencebinder Magical Runes Shop
					ShopsHandler.openShop(player, 175);
				else if (npc.getId() == 4513) // Baba Yaga's Magic Shop
					ShopsHandler.openShop(player, 176);
				else if (npc.getId() == 1435) // Tutab's Magical Market
					ShopsHandler.openShop(player, 177);
				else if (npc.getId() == 589) // Zenesha's Platebody Shop
					ShopsHandler.openShop(player, 178);
				else if (npc.getId() == 3038) // Seddu's Adventurers' Store
					ShopsHandler.openShop(player, 179);
				else if (npc.getId() == 1434) // Daga's Scimitar Smithy
					ShopsHandler.openShop(player, 180);
				else if (npc.getId() == 577) // Cassie's Shield Shop
					ShopsHandler.openShop(player, 181);
				else if (npc.getId() == 569) // Ardougne Silver Stall
					ShopsHandler.openShop(player, 182);
				else if (npc.getId() == 2159) // Silver Cog Silver Stall
					ShopsHandler.openShop(player, 183);
				else if (npc.getId() == 1980) // The Spice is Right
					ShopsHandler.openShop(player, 184);
				else if (npc.getId() == 4472) // summoning supplies
					ShopsHandler.openShop(player, 185);
				else if (npc.getId() == 5266) // Blades by Urbi
					ShopsHandler.openShop(player, 186);
				else if (npc.getId() == 586) // Gaius's Two-Handed Shop
					ShopsHandler.openShop(player, 187);
				else if (npc.getId() == 602) // Gulluck and Sons
					ShopsHandler.openShop(player, 188);
				else if (npc.getId() == 692) // Authentic Throwing Weapons
					ShopsHandler.openShop(player, 189);
				else if (npc.getId() == 4312) // Nardok's Bone Weapons
					ShopsHandler.openShop(player, 190);
				else if (npc.getId() == 1167) // Tamayu's Spear Stall
					ShopsHandler.openShop(player, 191);
				else if (npc.getId() == 5486) // Weapons Galore
					ShopsHandler.openShop(player, 192);
				else if (npc.getId() == 1370 || npc.getId() == 1394) // Vegetable stall
					ShopsHandler.openShop(player, 193);
				else if (npc.getId() == 524 || npc.getId() == 525) // Al Kharid General Store
					ShopsHandler.openShop(player, 194);
				else if (npc.getId() == 1436) // Ape Atoll General Store
					ShopsHandler.openShop(player, 195);
				else if (npc.getId() == 590 || npc.getId() == 591) // East Ardougne General Store
					ShopsHandler.openShop(player, 196);
				else if (npc.getId() == 971) // West ardougne General Store
					ShopsHandler.openShop(player, 197);
				else if (npc.getId() == 597) // Bandit Duty Free General Store
					ShopsHandler.openShop(player, 198);
				else if (npc.getId() == 3541) // Aurel's Supplies General Store
					ShopsHandler.openShop(player, 199);
				else if (npc.getId() == 5798) // Dorgesh-Kaan General Supplies General Store
					ShopsHandler.openShop(player, 200);
				else if (npc.getId() == 526 || npc.getId() == 527) // Falador General Store
					ShopsHandler.openShop(player, 201);
				else if (npc.getId() == 11674 || npc.getId() == 11678) // Karamja General Storee
					ShopsHandler.openShop(player, 202);
				else if (npc.getId() == 1334) // The Lighthouse Store General Store
					ShopsHandler.openShop(player, 203);
				else if (npc.getId() == 1334) // Trader Sven's Black Market Goods
					ShopsHandler.openShop(player, 204);
				else if (npc.getId() == 1254) // Razmire General Store
					ShopsHandler.openShop(player, 205);
				else if (npc.getId() == 2086) // Nardah General Store
					ShopsHandler.openShop(player, 206);
				else if (npc.getId() == 1866) // Pollnivneach General Store
					ShopsHandler.openShop(player, 207);
				else if (npc.getId() == 605) // White Knight Master Armoury
					ShopsHandler.openShop(player, 208);
				else if (npc.getId() == 573) // Fur Trader
					ShopsHandler.openShop(player, 209);
				else if (npc.getId() == 572) // Ardougne Spice Stall
					ShopsHandler.openShop(player, 210);
				else if (npc.getId() == 14862) // Alfred Stonemason's Construction Shop
					ShopsHandler.openShop(player, 211);
				else if (npc.getId() == 14858) // Alison Elmshaper's Flying Arrow Fletching Shop
					ShopsHandler.openShop(player, 212);
				else if (npc.getId() == 14885) // Will Oakfeller's Woodcutting Supplies Shop
					ShopsHandler.openShop(player, 213);
				else if (npc.getId() == 14883) // Marcus Everburn's Firemaking Shop
					ShopsHandler.openShop(player, 214);
				else if (npc.getId() == 14874) // Martin Steelweaver's Smithing Supplies Shop
					ShopsHandler.openShop(player, 215);
				else if (npc.getId() == 14870) // Tobias Bronzearms's Mining Supplies Shop
					ShopsHandler.openShop(player, 216);
				else if (npc.getId() == 14877) // Jack Oval's crafting Shop
					ShopsHandler.openShop(player, 217);
				else if (npc.getId() == 555) // Khazard General Store
					ShopsHandler.openShop(player, 218);
				else if (npc.getId() == 1699) // Port Phasmatys General Store
					ShopsHandler.openShop(player, 219);
				else if (npc.getId() == 531 || npc.getId() == 530) // Rimmington General Store
					ShopsHandler.openShop(player, 220);
				else if (npc.getId() == 560) // Jiminua's Jungle Store
					ShopsHandler.openShop(player, 221);
				else if (npc.getId() == 4946) // Ignatius's Hot Deals
					ShopsHandler.openShop(player, 227);
				else if (npc.getId() == 2270) // Martin Thwait's Lost and Found
					ShopsHandler.openShop(player, 229);
				else if (npc.getId() == 14868) // Jacquelyn Manslaughter
					ShopsHandler.openShop(player, 29);
				else if (npc.getId() == 1778) // Team capes
					ShopsHandler.openShop(player, 232);
				else if (npc.getId() == 1779) // Team capes
					ShopsHandler.openShop(player, 233);
				else if (npc.getId() == 1780) // Team capes
					ShopsHandler.openShop(player, 234);
				else if (npc.getId() == 1781) // Team capes
					ShopsHandler.openShop(player, 235);
				else if (npc.getId() == 1782) // Team capes
					ShopsHandler.openShop(player, 236);
				else if (npc.getId() == 1783) // Team capes
					ShopsHandler.openShop(player, 237);
				else if (npc.getId() == 1784) // Team capes
					ShopsHandler.openShop(player, 238);
				else if (npc.getId() == 1785) // Team capes
					ShopsHandler.openShop(player, 239);
				else if (npc.getId() == 1786) // Team capes
					ShopsHandler.openShop(player, 240);
				else if (npc.getId() == 1787) // Team capes
					ShopsHandler.openShop(player, 241);
				else if (npc.getId() == 2961) // Ali's Discount Wares
					ShopsHandler.openShop(player, 242);
				else if (npc.getId() == 8527) {// nomad capes
					if (player.getQuestManager().completedQuest(Quests.NOMADS_REQUIEM))
						ShopsHandler.openShop(player, 51);
					else
						player.getDialogueManager().startDialogue("SimpleNPCMessage", 8527, "You must kill nomad inside the tent there to see my rewards.");
				} else if (npc.getId() >= 6747 && npc.getId() <= 6749) 
					player.getDialogueManager().startDialogue("SimpleNPCMessage", npc.getId(), "Merry Christmas!!");
				else if (npc.getId() == 1595)
					player.getDialogueManager().startDialogue("SaniBoch", false);
				else if (npc.getId() == 3810)
					GnomeGlider.openInterface(player, 1);
				else if (npc.getId() == 3809)
					GnomeGlider.openInterface(player, 3);
				else if (npc.getId() == 3812)
					GnomeGlider.openInterface(player, 4);
				else if (npc.getId() == 1800)
					GnomeGlider.openInterface(player, 5);
				else if (npc.getId() == 3811)
					GnomeGlider.openInterface(player, 0);
				else if (npc.getId() == 1301)
					ShopsHandler.openShop(player, 50);
				else if (npc.getId() == 551 || npc.getId() == 552)
					ShopsHandler.openShop(player, 13);
				else if (npc.getId() == 550)
					ShopsHandler.openShop(player, 14);
				else if (npc.getId() == 549)
					ShopsHandler.openShop(player, 15);
				else if (npc.getId() == 548)
					ShopsHandler.openShop(player, 18); // thesalia
				else if (npc.getId() == 2233 || npc.getId() == 3671)
					ShopsHandler.openShop(player, 20);
				else if (npc.getId() == 970)
					ShopsHandler.openShop(player, 21);
				else if (npc.getId() == 579) // Drogo's mining Emporium
					ShopsHandler.openShop(player, 30);
				else if (npc.getId() == 582) // dwarves general store
					ShopsHandler.openShop(player, 31);
				else if (npc.getId() == 1040)
					ShopsHandler.openShop(player, 81);
				else if (npc.getId() == 1039)
					ShopsHandler.openShop(player, 82);
				else if (npc.getId() == 1038)
					ShopsHandler.openShop(player, 83);
				else if (npc.getId() == 558) // Gerrant's Fishy Business
					ShopsHandler.openShop(player, 84);
				else if (npc.getId() == 556) // Grum's Gold Exchange
					ShopsHandler.openShop(player, 85);
				else if (npc.getId() == 559) // Brian's Battleaxe Bazaar
					ShopsHandler.openShop(player, 86);
				else if (npc.getId() == 557) // Wydin's Food Store
					ShopsHandler.openShop(player, 87);
				else if (npc.getId() >= 4650 && npc.getId() <= 4656 || npc.getId() == 7065 || npc.getId() == 7066) // Trader
					// Stan's
					// Trading
					// Post
					ShopsHandler.openShop(player, 88);
				else if (npc.getId() == 209) // cannon shop
					ShopsHandler.openShop(player, 34);
				else if (npc.getId() == 1334) // The Lighthouse Store
					ShopsHandler.openShop(player, 36);
				else if (npc.getId() == 594) // Nurmof's Pickaxe Shop
					ShopsHandler.openShop(player, 32);
				else if (npc.getId() == 537) // Scavvo's Rune Store
					ShopsHandler.openShop(player, 12);
				else if (npc.getId() == 536) // Valaine's Shop of Champions
					ShopsHandler.openShop(player, 17);
				else if (npc.getId() == 4563) // Crossbow Shop
					ShopsHandler.openShop(player, 33);
				else if (npc.getId() == 6070)
					ShopsHandler.openShop(player, 54);
				else if (npc.getId() == 904)
					ShopsHandler.openShop(player, 37);
				else if (npc.getId() == 1303)
					ShopsHandler.openShop(player, 42);
				else if (npc.getId() == 903)
					ShopsHandler.openShop(player, 38);
				else if (npc.getId() == 6988)
					ShopsHandler.openShop(player, 39);
				else if (npc.getId() == 1316)
					ShopsHandler.openShop(player, 43);
				else if (npc.getId() == 1315 || npc.getId() == 1315 || npc.getId() == 1315)
					ShopsHandler.openShop(player, 45);
				else if (npc.getId() == 5485)
					ShopsHandler.openShop(player, 47);
				else if (npc.getId() == 5483)
					ShopsHandler.openShop(player, 46);
				else if (npc.getId() == 5509)
					ShopsHandler.openShop(player, 48);
				else if (npc.getId() == 3798)
					ShopsHandler.openShop(player, 40);
				else if (npc.getId() == 3796)
					ShopsHandler.openShop(player, 41);
				else if (npc.getId() == 15149)
					player.getDialogueManager().startDialogue("MasterOfFear", 3);
				else if (npc.getId() == 462 || npc.getId() == 844 || npc.getId() == 300 ) // Aubury
					RuneEssenceController.teleport(player, npc);
				else if (npc.getId() == 2676)
					player.getDialogueManager().startDialogue(new MakeoverD());
					//PlayerLook.openCharacterCustomizing(player);
				else if (npc.getId() == 598)
					PlayerLook.openHairdresserSalon(player);
				else if (npc.getId() == 28491) {
					player.getDialogueManager().startDialogue("TheCollector", -1);
				} else if (npc.getId() == 1282)
					ShopsHandler.openShop(player, 44);
				else if (npc.getId() == 171) // Brimstail
					RuneEssenceController.teleport(player, npc);
				else if (npc instanceof Pet) {
					if (npc != player.getPet()) {
						player.getPackets().sendGameMessage("This isn't your pet!");
						return;
					}
					Pet pet = player.getPet();
					player.getPackets().sendMessage(99, "Pet [id=" + pet.getId() + ", hunger=" + pet.getDetails().getHunger() + ", growth=" + pet.getDetails().getGrowth() + ", stage=" + pet.getDetails().getStage() + "].", player);
				} else {
					switch (npc.getDefinitions().getName().toLowerCase()) {
					case "musician":
					case "drummer":
						player.getDialogueManager().startDialogue("SimpleNPCMessage", npc.getId(), "Listen to my music my friend!");
						break;
					default:
						player.getPackets().sendGameMessage("Nothing interesting happens.");
						if (Settings.DEBUG)
							System.out.println("cliked 2 at npc id : " + npc.getId() + ", " + npc.getX() + ", " + npc.getY() + ", " + npc.getPlane());
						break;
					}
			}
			}
		}));
	}

	public static void handleOption3(final Player player, InputStream stream) {
		int npcIndex = stream.readUnsignedShort128();
		boolean forceRun = stream.read128Byte() == 1;
		final NPC npc = World.getNPCs().get(npcIndex);
		if (npc == null || npc.isCantInteract() || npc.isDead() || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId()) || player.isLocked())
			return;
		player.stopAll(false);
		if (forceRun)
			player.setRun(forceRun);

		if(handle(player, npc, 3))
			return;

		if (npc.getId() == 14707 || npc.getId() == 4296 || npc.getDefinitions().name.toLowerCase().contains("banker")) {
			player.setRouteEvent(new RouteEvent(npc, new Runnable() {
				@Override
				public void run() {
					player.faceEntity(npc);
					if (!player.withinDistance(npc, 2))
						return;
					npc.faceEntity(player);
					player.getGeManager().openCollectionBox();
					return;
				}
			}, true));
			return;
		}
		if (npc.getId() == 4250) {
			player.setRouteEvent(new RouteEvent(npc, new Runnable() {
				@Override
				public void run() {
					player.faceEntity(npc);
					if (!player.withinDistance(npc, 2))
						return;
					npc.faceEntity(player);
					ShopsHandler.openShop(player, 128);
					return;
				}
			}, true));
			return;
		}
		if (npc.getDefinitions().name.toLowerCase().equals("grand exchange clerk")) {
			player.setRouteEvent(new RouteEvent(npc, new Runnable() {
				@Override
				public void run() {
					player.faceEntity(npc);
					if (!player.withinDistance(npc, 2))
						return;
					npc.faceEntity(player);
					player.getGeManager().openHistory();
					return;
				}
			}, true));
			return;
		}
		player.setRouteEvent(new RouteEvent(npc, new Runnable() {
			@Override
			public void run() {
				npc.setStopRandomWalk();
				npc.resetWalkSteps();
				if (!player.getControlerManager().processNPCClick3(npc))
					return;
				player.faceEntity(npc);
				if (npc.getId() >= 8837 && npc.getId() <= 8839) {
					MiningBase.propect(player, "You examine the remains...", "The remains contain traces of living minerals.");
					return;
				}
				if (npc instanceof GraveStone) {
					GraveStone grave = (GraveStone) npc;
					grave.repair(player, true);
					return;
				}
				if (npc instanceof Pet) {
					((Pet) npc).interact(player, 3);
					return;
				}
				if (npc.getId() >= TheNightmareInstance.NIGHTMARE_LOBBY_EMPTY && npc.getId() <= TheNightmareInstance.NIGHTMARE_LOBBY_PHASE_3) {
					TheNightmareInstance.spectate(player);
					return;
				}
				npc.faceEntity(player);
				if (SlayerMaster.startInteractionForId(player, npc.getId(), 3))
					ShopsHandler.openShop(player, 29);
				else if (npc.getId() == 4856) // Lovecraft's Tackle
					ShopsHandler.openShop(player, 154);
				else if (npc.getId() == 3824)
					player.getBank().openBank();
				else if (npc.getId() == 1923) // eblis shop for ancient staff
					ShopsHandler.openShop(player, 228);
				else if (npc.getId() == 3705)
					ShopsHandler.openShop(player, 901);
				else if (npc.getId() == 5198) 
					ShopsHandler.openShop(player, 225);
				else if (npc.getId() == 946) {
					TeleportationInterface.openInterface(player);
					player.getTemporaryAttributtes().put(TemporaryAtributtes.Key.SEARCH_TELEPORT, Boolean.TRUE);
					player.getPackets().sendInputLongTextScript("Search teleport:");
				} else if (npc.getId() == 6070)
					PuroPuro.openPuroInterface(player);
				else if (npc.getId() == 2619)
					player.getGeManager().openCollectionBox();
				else if (npc.getId() == 9711)
					player.getDialogueManager().startDialogue("RepairSquire", npc.getId());
				else if (npc.getId() == 209)
					player.getDialogueManager().startDialogue("ReplaceCannon");
				else if (npc.getId() >= 4650 && npc.getId() <= 4656 || npc.getId() == 7065 || npc.getId() == 7066) // Trader
					CarrierTravel.openCharterInterface(player);
				else if (npc.getId() == 1526)
					CastleWars.openCastleWarsTicketExchange(player);
				else if (npc.getId() == 14877)
					player.getDialogueManager().startDialogue("TanningD", npc.getId());
				else if (npc.getId() == 548)
					PlayerLook.openThessaliasMakeOver(player);
				else if (npc.getId() == 1301)
					PlayerLook.openYrsaShop(player);
				else if (npc.getId() == 28491)
					player.getCutscenesManager().play("TrollCutscene");
				else if (npc.getId() == 6892 || npc.getId() == 6893)
					PetShopOwner.sellShards(player);
				else if (npc.getId() == 6524) {
					player.getDialogueManager().startDialogue("SimpleNPCMessage", npc.getId(), "Her ya go chap.");
					Drinkables.decantPotsInv(player);
				} else if (npc.getId() == 2259)
					AbbysObsticals.teleport(player, npc);
				else if (npc.getId() == 4287)
					player.getDialogueManager().startDialogue("GamfriedShield");
				else if (npc.getId() == 359)
					ShopsHandler.openShop(player, 127);
				else if (npc.getId() == 27201)
					ShopsHandler.openShop(player, 261);
				else if (npc.getId() == 5532)
					SorceressGarden.teleportToSorceressGardenNPC(npc, player);
				else if (npc.getId() == 1334) // Book Shop
					ShopsHandler.openShop(player, 35);
				else if (npc.getId() == 5913) // Aubury
					ShopsHandler.openShop(player, 11);
				else
					player.getPackets().sendGameMessage("Nothing interesting happens.");
			}
		}));
		if (Settings.DEBUG)
			System.out.println("cliked 3 at npc id : " + npc.getId() + ", " + npc.getX() + ", " + npc.getY() + ", " + npc.getPlane());
	}

	public static void handleOption4(final Player player, InputStream stream) {
		int npcIndex = stream.readUnsignedShort128();
		boolean forceRun = stream.read128Byte() == 1;
		final NPC npc = World.getNPCs().get(npcIndex);
		if (npc == null || npc.isCantInteract() || npc.isDead() || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId()) || player.isLocked())
			return;
		player.stopAll(false);
		if (forceRun)
			player.setRun(forceRun);

		if(handle(player, npc, 4))
			return;
		if (npc.getDefinitions().name.toLowerCase().equals("grand exchange clerk")) {
			player.setRouteEvent(new RouteEvent(npc, new Runnable() {
				@Override
				public void run() {
					player.faceEntity(npc);
					if (!player.withinDistance(npc, 2))
						return;
					npc.faceEntity(player);
					ItemSets.openSets(player);
					return;
				}
			}, true));
			return;
		}
		if (npc.getId() == 14707 || npc.getId() == 4296 || npc.getDefinitions().name.toLowerCase().contains("banker")) {
			player.setRouteEvent(new RouteEvent(npc, new Runnable() {
				@Override
				public void run() {
					player.faceEntity(npc);
					if (!player.withinDistance(npc, 2))
						return;
					npc.faceEntity(player);
					player.getBank().openBank(true);
					return;
				}
			}, true));
			return;
		}
		player.setRouteEvent(new RouteEvent(npc, new Runnable() {
			@Override
			public void run() {
				npc.setStopRandomWalk();
				npc.resetWalkSteps();
				if (!player.getControlerManager().processNPCClick4(npc))
					return;
				player.faceEntity(npc);
				if (npc instanceof GraveStone) {
					GraveStone grave = (GraveStone) npc;
					grave.demolish(player);
					return;
				}
				npc.faceEntity(player);
				if (npc.getId() == 14866)
					ShopsHandler.openShop(player, 39);
				else if (npc.getId() == 14864) // Ayleth Beaststalker's Hunting Supplies Shop
					ShopsHandler.openShop(player, 172);
				else if (npc.getId() == 3824)
					player.getGeManager().openCollectionBox();
				else if (npc.getId() == 5109) // Nardah Hunter Shop
					ShopsHandler.openShop(player, 171);
				else if (npc.getId() == 5913) // Aubury
					RuneEssenceController.teleport(player, npc);
				else if (npc.getId() == 5111) // leon
					ShopsHandler.openShop(player, 230);
				else if (npc.getId() == 14872)
					player.getDialogueManager().startDialogue("KillingQuickD");
				else if (npc.getId() == 5110) // Aleck's Hunter Emporium
					ShopsHandler.openShop(player, 56);
				else if (npc.getId() == 14854) // Poletax's Herblore Shop
					ShopsHandler.openShop(player, 68);
				else if (npc.getId() == 6524) {
					player.getDialogueManager().startDialogue("SimpleNPCMessage", npc.getId(), "Her ya go chap.");
					Drinkables.decantPotsInv(player);
				}  else if (SlayerMaster.startInteractionForId(player, npc.getId(), 4))
					return;
					//player.getSlayerManager().sendSlayerInterface(SlayerManager.BUY_INTERFACE);
				else
					player.getPackets().sendGameMessage("Nothing interesting happens.");
			}
		}));
		if (Settings.DEBUG)
			System.out.println("cliked 4 at npc id : " + npc.getId() + ", " + npc.getX() + ", " + npc.getY() + ", " + npc.getPlane());

	}

	public static void handleItemOnNPC(final Player player, final NPC npc, final int slot, final Item item) {
		if (item == null)
			return;
		if (!player.getInventory().containsItem(item.getId(), item.getAmount()))
			return;
		if (npc == null || npc.isDead() || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId()))
			return;
		if (npc.getId() == 2021 && item.getId() == 4702 && npc.withinDistance(player)) {
			LightCreature.teleport(npc, player);
			return;
		}
		if (item.getId() == 6660 && npc instanceof Kraken) {
			if (!player.getCombatDefinitions().isDistancedStyle()) {
				player.getPackets().sendGameMessage("You can't reach this npc with melee.");
				return;
			}
			((Kraken)npc).forceWakeUP();
			player.getInventory().deleteItem(6660, 1);
			player.getActionManager().setAction(new PlayerCombat(npc));
			return;
		}
		player.setRouteEvent(new RouteEvent(npc, new Runnable() {
			@Override
			public void run() {
				if(npc.getId() == Pets.OLMLET.getBabyNpcId()) {
					player.sendMessage("You toss the metamorphic dust over your Olmlet..");
					player.anim(6600);

					WorldTasksManager.schedule(() -> npc.gfx(1368));
					WorldTasksManager.schedule(() -> {
						player.sendMessage("It can now metamorphosis into many different creatures!");
						player.getInventory().deleteItem(52386, 1);
						npc.setNextNPCTransformation(Pets.DUSTED_OLMLET.getBabyNpcId());
						player.getPet().setNextNPCTransformation(Pets.DUSTED_OLMLET.getBabyNpcId()); // should be same
						player.getPet().setPet(Pets.DUSTED_OLMLET);
					}, 2);
					return;
				}
				if (npc instanceof Block) {
					Block block = (Block) npc;
					if (!block.useItem(player, item)) {
						return;
					}
				}
				if (npc instanceof Familiar) {
					Familiar familiar = (Familiar) npc;
					if (familiar.getBob() != null) {
						familiar.getBob().addItem(slot, item.getAmount());
					} else if (npc.getId() == 7378 || npc.getId() == 7377) {
						if (Pyrelord.lightLog(familiar, item))
							return;
					} else if (npc.getId() == 7339 || npc.getId() == 7339) {
						if ((item.getId() >= 1704 && item.getId() <= 1710 && item.getId() % 2 == 0) || (item.getId() >= 10356 && item.getId() <= 10366 && item.getId() % 2 == 0) || (item.getId() == 2572 || (item.getId() >= 20653 && item.getId() <= 20657 && item.getId() % 2 != 0))) {
							for (Item item : player.getInventory().getItems().getItems()) {
								if (item == null)
									continue;
								if (item.getId() >= 1704 && item.getId() <= 1710 && item.getId() % 2 == 0)
									item.setId(1712);
								else if (item.getId() >= 10356 && item.getId() <= 10366 && item.getId() % 2 == 0)
									item.setId(10354);
								else if (item.getId() == 2572 || (item.getId() >= 20653 && item.getId() <= 20657 && item.getId() % 2 != 0))
									item.setId(20659);
							}
							player.getInventory().refresh();
							player.getDialogueManager().startDialogue("ItemMessage", "Your ring of wealth and amulet of glory have all been recharged.", 1712);
						}
					}
				} else if (npc instanceof Pet) {
					player.faceEntity(npc);
					player.getPetManager().eat(item.getId(), (Pet) npc);
					return;
				} else if (npc instanceof ConditionalDeath)
					((ConditionalDeath) npc).useHammer(player);
				else if ((npc.getId() == 1526/* && object.getX() == 3298 && object.getY() == 5270 */)) {
					if (!AccessorySmithing.isEmptyRing(item.getId())) {
						player.getPackets().sendGameMessage("Lanthus can not imbue this item."); 
						return;
					};
					player.getDialogueManager().startDialogue("ImbueingDialouge", item.getId());
				}
				else if (item.getId() == 22444)
					PolyporeCreature.sprinkleOil(player, npc);
				else if (item.getId() == SpiritshieldCreating.SPIRIT_SHIELD || item.getId() == SpiritshieldCreating.HOLY_ELIXIR && npc.getId() == 802) {
					SpiritshieldCreating.blessShield(player, false);
				} else if (npc.getId() == 7729 && SpiritshieldCreating.isSigil(item.getId()))
					SpiritshieldCreating.attachShield(player, item, false);
				else if ((npc.getId() == 519 || npc.getId() == 3797 || npc.getId() == 944 || npc.getId() == 2024) && ItemConstants.repairItem(player, player.getInventory().getItems().getThisItemSlot(item), false))
					return;
				else if (npc.getId() == 9711 && ItemConstants.repairItem(player, player.getInventory().getItems().getThisItemSlot(item), true))
					return;
				else if (npc.getId() == 2725 && item.getId() == 11716) {
					if  (player.getInventory().getCoinsAmount() < 300000) {
						player.getPackets().sendGameMessage("You need 300k coins to upgrade this spear.");
						return;
					}
					player.getInventory().removeItemMoneyPouch(new Item(995, 300000));
					player.getInventory().deleteItem(item);
					player.getInventory().addItem(41889, 1);
				} else if (npc.getId() == 2725 && item.getId() == 41889) {
					player.getInventory().deleteItem(item);
					player.getInventory().addItem(11716, 1);
				} else if (npc.getId() == 16018) {
					GamblerKing.gamble(npc, player, item);
				} else if (npc.getId() == 20013) {
					if (/*item.getDefinitions().isNoted() || */item.getDefinitions().getCertId() == -1) {
						player.getPackets().sendGameMessage("Pile can not switch this item."); ///*only note items*
						return;
					}
					int amount = player.getInventory().getAmountOf(item.getId());
					if (item.getDefinitions().isNoted() && !ItemConfig.forID(item.getDefinitions().getCertId()).isStackable()) {
						int slots = player.getInventory().getFreeSlots();
						if (amount > slots)
							amount = slots;
					}
					int price = amount * 50;
					if (player.getInventory().getCoinsAmount() < price) {
						player.getPackets().sendGameMessage("Piles charges 50 coins per noted item.");
						return;
					}
					player.getInventory().removeItemMoneyPouch(new Item(995, price));
					player.getInventory().deleteItem(item.getId(), amount);
					player.getInventory().addItem(item.getDefinitions().getCertId(), amount);
					player.getPackets().sendGameMessage("Piles switches your items for you.");
				}else if (npc.getName().toString().equals("Tool leprechaun") && (Herblore.isIngredient(item) || ProductInfo.isProduct(item)) && !item.getDefinitions().isNoted() && item.getDefinitions().getCertId() != -1) {
					int quantity = player.getInventory().getAmountOf(item.getId());
					player.getInventory().deleteItem(item.getId(), quantity);
					player.getInventory().addItem(item.getDefinitions().getCertId(), quantity);
					player.getDialogueManager().startDialogue("ItemMessage", "The leprechaun exchanges your items for banknotes.", item.getId());
				} else
					player.getPackets().sendGameMessage("Nothing interesting happens.");
			}
		}));
	}
}
