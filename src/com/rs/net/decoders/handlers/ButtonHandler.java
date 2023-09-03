package com.rs.net.decoders.handlers;

import java.text.DecimalFormat;
import java.util.HashMap;

import com.rs.Settings;
import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Animation;
import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.minigames.CastleWars;
import com.rs.game.minigames.Crucible;
import com.rs.game.minigames.PuroPuro;
import com.rs.game.minigames.Sawmill;
import com.rs.game.minigames.clanwars.FfaZone;
import com.rs.game.minigames.duel.DuelArena;
import com.rs.game.minigames.duel.DuelControler;
import com.rs.game.minigames.pest.CommendationExchange;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.familiar.Familiar.SpecialAttack;
import com.rs.game.npc.others.GraveStone;
import com.rs.game.player.*;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.HomeTeleport;
import com.rs.game.player.actions.PlayerCombat;
import com.rs.game.player.actions.Rest;
import com.rs.game.player.actions.Smithing;
import com.rs.game.player.actions.ViewingOrb;
import com.rs.game.player.content.AccessorySmithing;
import com.rs.game.player.content.Canoes;
import com.rs.game.player.content.CarrierTravel;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.EconomyManager;
import com.rs.game.player.content.Enchanting;
import com.rs.game.player.content.ExtraSettings;
import com.rs.game.player.content.FairyRings;
import com.rs.game.player.content.GameMode;
import com.rs.game.player.content.GnomeGlider;
import com.rs.game.player.content.GraveStoneSelection;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.ItemSets;
import com.rs.game.player.content.ItemTransportation;
import com.rs.game.player.content.LoyaltyProgram;
import com.rs.game.player.content.Magic;
import com.rs.game.player.content.MythGuild;
import com.rs.game.player.content.PlayerLook;
import com.rs.game.player.content.RunePouch;
import com.rs.game.player.content.Runecrafting;
import com.rs.game.player.content.Shop;
import com.rs.game.player.content.SkillCapeCustomizer;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.Slayer.SlayerMaster;
import com.rs.game.player.content.SpiritTree;
import com.rs.game.player.content.StealingCreationShop;
import com.rs.game.player.content.Summoning;
import com.rs.game.player.content.clans.ClansManager;
import com.rs.game.player.content.construction.House;
import com.rs.game.player.content.dungeoneering.DungeonRewardShop;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.content.questTab.QuestTab;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.teleportation.TeleportationInterface;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.controllers.DungeonController;
import com.rs.game.player.controllers.SorceressGarden;
import com.rs.game.player.controllers.events.DeathEvent;
import com.rs.game.player.controllers.partyroom.PartyRoom;
import com.rs.game.player.cutscenes.HomeCutScene3;
import com.rs.game.player.dialogues.impl.BossInstanceD;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.io.InputStream;
import com.rs.net.decoders.WorldPacketsDecoder;
import com.rs.utils.ItemExamines;
import com.rs.utils.Logger;
import com.rs.utils.ShopsHandler;
import com.rs.utils.Utils;

public class ButtonHandler {

	public static void register(int[] componentId, int interfaceId, ButtonAction runnable, int actionIndex) {
		for(Integer i : componentId) {
			register(interfaceId, i, actionIndex, runnable);
		}
	}

	public static void register(int interfaceId, int componentId, int actionIndex, ButtonAction runnable) {
		Integer hash = interfaceId << 16 | componentId;
		actionRepository.putIfAbsent(hash, new ButtonAction[10]);
		ButtonAction[] actionList = actionRepository.get(hash);
		if(actionList[actionIndex] != null)
			System.err.println("Warning: button action " + interfaceId + "[" + componentId + "] is being overwritten!");
		actionList[actionIndex] = runnable;
	}

	public static int getActionIndex(int packet) {
		switch(packet) {
			case WorldPacketsDecoder.ACTION_BUTTON1_PACKET:
				return 1;
			case WorldPacketsDecoder.ACTION_BUTTON2_PACKET:
				return 2;
			case WorldPacketsDecoder.ACTION_BUTTON3_PACKET:
				return 3;
			case WorldPacketsDecoder.ACTION_BUTTON4_PACKET:
				return 4;
			case WorldPacketsDecoder.ACTION_BUTTON5_PACKET:
				return 5;
			case WorldPacketsDecoder.ACTION_BUTTON6_PACKET:
				return 6;
			case WorldPacketsDecoder.ACTION_BUTTON7_PACKET:
				return 7;
			case WorldPacketsDecoder.ACTION_BUTTON8_PACKET:
				return 8;
			case WorldPacketsDecoder.ACTION_BUTTON9_PACKET:
				return 9;
			case WorldPacketsDecoder.ACTION_BUTTON10_PACKET:
				return 10;
			default:
				return 0;
		}
	}

	public static boolean handle(Player player, int interfaceId, int componentId, int slot1, int slot2, int packetId) {
		int action = getActionIndex(packetId);

		if(action < 1)
			return false;
		if(interfaceId < 0 || componentId < 0)
			return false;
		Integer hash = interfaceId << 16 | componentId;
		if (actionRepository.containsKey(hash)) {
			ButtonAction act = actionRepository.get(hash)[action];
			if(act != null) {
				act.handle(player, slot1, slot2, action);
				return true;
			} else return false;
		}

		return false;
	}

	static HashMap<Integer, ButtonAction[]> actionRepository = new HashMap<Integer, ButtonAction[]>();

	public static void handleButtons(final Player player, InputStream stream, final int packetId) {
		int interfaceHash = stream.readIntV2();
		final int interfaceId = interfaceHash >> 16;
		if (Utils.getInterfaceDefinitionsSize() <= interfaceId) {
			// hack, or server error or client error
			// player.getSession().getChannel().close();
			if (Settings.DEBUG) {
				System.out.println("BLOCK 1 " + packetId + "," + interfaceId + "," + (interfaceHash & 0xFFFF));
			}
			return;
		}
		final int componentId = interfaceHash - (interfaceId << 16);
		//cant use inter while locked, temporarly
		if (player.isDead() || player.isLocked() || !player.getInterfaceManager().containsInterface(interfaceId)) {
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET
					&& interfaceId == 375 && componentId == 3 && player.getCutscenesManager().getCurrent() instanceof HomeCutScene3) {
				player.getCutscenesManager().stop();
				return;
			}
			if (Settings.DEBUG) {
				System.out.println("BLOCK 2 " + packetId + "," + interfaceId + "," + (interfaceHash & 0xFFFF));
			}
			return;
		}
		if (componentId != 65535 && Utils.getInterfaceDefinitionsComponentsSize(interfaceId) <= componentId) {
			// hack, or server error or client error
			// player.getSession().getChannel().close();
			if (Settings.DEBUG) {
				System.out.println("BLOCK 3 " + packetId + "," + interfaceId + "," + componentId);
			}
			return;
		}
		final int slotId2 = stream.readUnsignedShort128();
		final int slotId = stream.readUnsignedShortLE128();
		if (Settings.DEBUG) {
			System.out.println(packetId + "," + interfaceId + "," + componentId + "," + slotId + "," + slotId2);
		}
		if (!player.getControlerManager().processButtonClick(interfaceId, componentId, slotId, slotId2, packetId))
			return;
		if(handle(player, interfaceId, componentId, slotId, slotId2, packetId))
			return;
		if (interfaceId == 548 || interfaceId == 746) {
			if ((interfaceId == 548 && componentId == 148) || (interfaceId == 746 && componentId == 199)) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
					if (player.getInterfaceManager().containsScreenInter() || player.getInterfaceManager().containsInventoryInter()) {
						// TODO cant open sound
						player.getPackets().sendGameMessage("Please finish what you're doing before opening the world map.");
						return;
					} else if (player.getAttackedByDelay() >= Utils.currentTimeMillis()) {
						player.getPackets().sendGameMessage("You cannot be in combat while opening the world map.");
						return;
					}
					// world map open
					player.getPackets().sendRootInterface(755, 0);
					player.getPackets().sendCSVarInteger(622, player.getTileHash()); // center
					player.getPackets().sendCSVarInteger(674, player.getTileHash()); // player
					// position
				} else {
					player.getHintIconsManager().removeAll();
					player.getVarsManager().sendVar(1159, 1);
				}
			} else if ((interfaceId == 548 && componentId == 194) || (interfaceId == 746 && componentId == 204)) {
				player.getMoneyPouch().switchPouch();
			} else if ((interfaceId == 548 && componentId == 17) || (interfaceId == 746 && componentId == 54)) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					player.getSkills().switchXPDisplay();
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.getSkills().switchXPPopup();
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					player.getSkills().setupXPCounter();
			} else if ((interfaceId == 746 && componentId == 207) || (interfaceId == 548 && componentId == 159)) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					player.getMoneyPouch().switchPouch();
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.getMoneyPouch().withdrawPouch();
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					player.getMoneyPouch().examinePouch();
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					if (player.getInterfaceManager().containsScreenInter() || player.isLocked()) {
						player.getPackets().sendGameMessage("Please finish what you're doing before opening the price checker.");
						return;
					}
					player.stopAll();
					player.getPriceCheckManager().openPriceCheck();
				}
			} else if (interfaceId == 548 && (componentId >= 98 && componentId <= 105))
				player.getInterfaceManager().setCurrentTab(componentId - 98);
			 else if (interfaceId == 548 && (componentId >= 68 && componentId <= 75))
					player.getInterfaceManager().setCurrentTab(componentId - 68 + 8);
			 else if (interfaceId == 746 && (componentId >= 74 && componentId <= 89))
					player.getInterfaceManager().setCurrentTab(componentId - 74);

		} else if (interfaceId == 748) {
			if (componentId == 2)
				player.getPoison().healPoison();
		} else if (interfaceId == 3040) {
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
				Keybinds.buttonClick(player, componentId);
			}
		} else if (interfaceId == 3031) {
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
				
				if (componentId == 45) {
					player.getPackets().sendGameMessage(ItemExamines.getExamine(new Item(slotId2)));
					return;
				}
				
				player.getCollectionLog().buttonClick(player, componentId);
			}
		} else if (interfaceId == 759) {
			if (componentId >= 4 && componentId <= 40)
				player.getBank().sendNext(componentId, false);
		} else if (interfaceId == 939) {
			if (componentId == 112)
				player.getDungManager().closePartyInterface();
			else if (componentId >= 59 && componentId <= 72) {
				int playerIndex = (componentId - 59) / 3;
				if ((componentId & 0x3) != 0)
					player.getDungManager().pressOption(playerIndex, packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET ? 0 : packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET ? 1 : 2);
				else
					player.getDungManager().pressOption(playerIndex, 3);
			} else if (componentId == 45)
				player.getDungManager().formParty();
			else if (componentId == 33 || componentId == 36)
				player.getDungManager().checkLeaveParty();
			else if (componentId == 43)
				player.getDungManager().invite();
			else if (componentId == 102)
				player.getDungManager().changeComplexity();
			else if (componentId == 108)
				player.getDungManager().changeFloor();
			else if (componentId == 87)
				player.getDungManager().openResetProgress();
			else if (componentId == 94)
				player.getDungManager().switchGuideMode();
		} else if (interfaceId == 949) {
			if (componentId == 65)
				player.getDungManager().acceptInvite();
			else if (componentId == 61 || componentId == 63)
				player.closeInterfaces();
		} else if (interfaceId == 938) {
			if (componentId >= 56 && componentId <= 81)
				player.getDungManager().selectComplexity((componentId - 56) / 5 + 1);
			else if (componentId == 39)
				player.getDungManager().confirmComplexity();
		} else if (interfaceId == 947) {
			if (componentId >= 48 && componentId <= 107)
				player.getDungManager().selectFloor((componentId - 48) + 1);
			else if (componentId == 766)
				player.getDungManager().confirmFloor();
		} else if (interfaceId == 375) {
			player.getActionManager().forceStop();
		} else if (interfaceId == 1015) {
			if (componentId >= 144 && componentId <= 156)
				GameMode.selectOption(player, (componentId - 144) / 2);
		} else if (interfaceId == 363) {
			if (componentId == 4)
				player.getTreasureTrailsManager().movePuzzlePeice(slotId);
		} else if (interfaceId == 1253 || interfaceId == 1252 || interfaceId == 1139) {
			player.getSquealOfFortune().processClick(packetId, interfaceId, componentId, slotId, slotId2);
		} else if (interfaceId == 1312 || interfaceId == 668 || interfaceId == 737) {
			player.getDialogueManager().continueDialogue(interfaceId, componentId);
		} else if (interfaceId == 34) {// notes interface
			switch (componentId) {
			case 35:
			case 37:
			case 39:
			case 41:
				player.getNotes().colour((componentId - 35) / 2);
				player.getPackets().sendHideIComponent(34, 16, true);
				break;
			case 3:
				player.getPackets().sendInputLongTextScript("Add note:");
				player.getTemporaryAttributtes().put("entering_note", Boolean.TRUE);
				break;
			case 9:
				switch (packetId) {
				case WorldPacketsDecoder.ACTION_BUTTON1_PACKET:
					if (player.getNotes().getCurrentNote() == slotId)
						player.getNotes().removeCurrentNote();
					else
						player.getNotes().setCurrentNote(slotId);
					break;
				case WorldPacketsDecoder.ACTION_BUTTON2_PACKET:
					player.getPackets().sendInputLongTextScript("Edit note:");
					player.getNotes().setCurrentNote(slotId);
					player.getTemporaryAttributtes().put("editing_note", Boolean.TRUE);
					break;
				case WorldPacketsDecoder.ACTION_BUTTON3_PACKET:
					player.getNotes().setCurrentNote(slotId);
					player.getPackets().sendHideIComponent(34, 16, false);
					break;
				case WorldPacketsDecoder.ACTION_BUTTON4_PACKET:
					player.getNotes().delete(slotId);
					break;
				}
				break;
			case 8:
			case 11:
				switch (packetId) {
				case WorldPacketsDecoder.ACTION_BUTTON1_PACKET:
					player.getNotes().delete();
					break;
				case WorldPacketsDecoder.ACTION_BUTTON2_PACKET:
					player.getNotes().deleteAll();
					break;
				}
				break;
			}
		} else if (interfaceId == 675) {
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET && slotId != 65535) {
				player.getPackets().sendInputIntegerScript("Enter amount:");
				player.getTemporaryAttributtes().put(Key.JEWLERY_SMITH_COMP, componentId);
				return;
			}
			AccessorySmithing.handleButtonClick(player, componentId, packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET ? 1 : packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET ? 5 : packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET ? 10 : 1);
		} else if (interfaceId == 432) {
			final int index = Enchanting.getComponentIndex(componentId);
			if (index == -1)
				return;
			Enchanting.processBoltEnchantSpell(player, index, packetId == 14 ? 1 : packetId == 67 ? 5 : 10);
		} else if (interfaceId == 182) {
			if (player.getInterfaceManager().containsInventoryInter())
				return;
			if (componentId == 6 || componentId == 13)
				player.logout(componentId == 6);
		} else if (interfaceId == 164 || interfaceId == 161 || interfaceId == 378) {
			player.getSlayerManager().handleRewardButtons(interfaceId, componentId);
		} else if (interfaceId == 647) {
			if(packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
				if(componentId == 21) {
					PartyRoom.acceptDepositItems(player);
				} else if(componentId == PartyRoom.TO_DROP_CONTAINER || componentId == PartyRoom.DROPPING_CONTAINER) {
					player.getPackets().sendGameMessage(ItemExamines.getExamine(new Item(slotId2)));
				} else if (componentId == PartyRoom.TO_DEPO_CONTAINER) {
					PartyRoom.withdrawItem(player, slotId);
				}
			}
		} else if (interfaceId == 3086) {
			BossInstanceD.handleInstanceSystem(player, componentId);
		} else if (interfaceId == 1310) {
			if (componentId == 0) {
				player.getSlayerManager().createSocialGroup(true);
				player.setCloseInterfacesEvent(null);
			}
			player.closeInterfaces();
		} else if (interfaceId == 1011) {
			CommendationExchange.handleButtonOptions(player, componentId);
		} else if (interfaceId == 1309) {
			if (componentId == 20)
				player.getPackets().sendGameMessage("Use your enchanted stone ring onto the player that you would like to invite.", true);
			else if (componentId == 22) {
				Player p2 = player.getSlayerManager().getSocialPlayer();
				if (p2 == null)
					player.getPackets().sendGameMessage("You have no slayer group, invite a player to start one.");
				else
					player.getPackets().sendGameMessage("Your current slayer group consists of you and " + p2.getDisplayName() + ".");
			} else if (componentId == 24)
				player.getSlayerManager().resetSocialGroup(true);
			player.closeInterfaces();
		} else if (interfaceId == 1165) {
			// if (componentId == 22)
			// Summoning.closeDreadnipInterface(player);
		} else if (interfaceId == 1128) {
			int index = -1;
			if (componentId == 98 || componentId == 4)
				index = 0;
			else if (componentId == 128 || componentId == 106)
				index = 1;
			else if (componentId == 144 || componentId == 166)
				index = 2;
			else if (componentId == 203 || componentId == 181)
				index = 3;
			else if (componentId == 240 || componentId == 218)
				index = 4;
			else if (componentId == 277 || componentId == 255)
				index = 5;
			else if (componentId == 292 || componentId == 314)
				index = 6;
			if (index != -1)
				StealingCreationShop.select(player, index);
			else if (componentId == 45)
				StealingCreationShop.purchase(player);
		} else if (interfaceId == 1263) {
			player.getDialogueManager().continueDialogue(interfaceId, componentId);
		} else if (interfaceId == 880) {
			if (componentId >= 7 && componentId <= 19)
				Familiar.setLeftclickOption(player, (componentId - 7) / 2);
			else if (componentId == 21)
				Familiar.confirmLeftOption(player);
			else if (componentId == 25)
				Familiar.setLeftclickOption(player, 7);
		} else if (interfaceId == 662) {
			player.closeInterfaces();
			if (player.getFamiliar() == null) {
				if (player.getPet() == null) {
					return;
				}
				if (componentId == 49)
					player.getPet().call();
				else if (componentId == 51)
					player.getDialogueManager().startDialogue("DismissD");
				return;
			}
			if (componentId == 49)
				player.getFamiliar().call();
			else if (componentId == 51)
				player.getDialogueManager().startDialogue("DismissD");
			else if (componentId == 67)
				player.getFamiliar().takeBob();
			else if (componentId == 69)
				player.getFamiliar().renewFamiliar();
			else if (componentId == 74) {
				if (player.getFamiliar().getSpecialAttack() == SpecialAttack.CLICK)
					player.getFamiliar().setSpecial(true);
				if (player.getFamiliar().hasSpecialOn())
					player.getFamiliar().submitSpecial(player);
			}
		} else if (interfaceId == 60)
			CastleWars.handleInterfaces(player, interfaceId, componentId, packetId);
		else if (interfaceId == 652) {
			if (componentId == 31)
				GraveStoneSelection.handleSelectionInterface(player, slotId / 6);
			else if (componentId == 34)
				GraveStoneSelection.confirmSelection(player);
		} else if (interfaceId == 864) {
			SpiritTree.handleSpiritTree(player, slotId);
		} else if (interfaceId == 747) {
			player.closeInterfaces();
			if (componentId == 8) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					submitSpecialRequest(player);
				else
					Familiar.selectLeftOption(player);
			} else if (player.getPet() != null) {
				if (componentId == 11 || componentId == 20) {
					player.getPet().call();
				} else if (componentId == 12 || componentId == 21) {
					player.getDialogueManager().startDialogue("DismissD");
				} else if (componentId == 10 || componentId == 19) {
					player.getPet().sendFollowerDetails();
				}
			} else if (player.getFamiliar() != null) {
				if (componentId == 11 || componentId == 20)
					player.getFamiliar().call();
				else if (componentId == 12 || componentId == 21)
					player.getDialogueManager().startDialogue("DismissD");
				else if (componentId == 13 || componentId == 22)
					player.getFamiliar().takeBob();
				else if (componentId == 14 || componentId == 23)
					player.getFamiliar().renewFamiliar();
				else if (componentId == 19 || componentId == 10)
					player.getFamiliar().sendFollowerDetails();
				else if (componentId == 18) {
					if (player.getFamiliar().getSpecialAttack() == SpecialAttack.CLICK)
						player.getFamiliar().setSpecial(true);
					if (player.getFamiliar().hasSpecialOn())
						player.getFamiliar().submitSpecial(player);
				}
			}
		} else if (interfaceId == 540) {
			if (componentId == 69)
				PuroPuro.confirmPuroSelection(player);
			else if (componentId == 71)
				ShopsHandler.openShop(player, 54);
			else
				PuroPuro.handlePuroInterface(player, componentId);
		} else if (interfaceId == 3002) {
			QuestTab.handleInterface(player, componentId);
		} else if (interfaceId == 138) {
			int selectedComponent = componentId - 23;
			if (selectedComponent == 0 || player.getTemporaryAttributtes().get("using_carrier") != null)
				return;
			if (componentId == 22)
				selectedComponent = 4;
			else if (componentId == 27)
				selectedComponent = 5;
			GnomeGlider.sendGlider(player, selectedComponent, false);
		} else if (interfaceId == 734) {
			if (componentId == 21)
				FairyRings.confirmRingHash(player);
			else
				FairyRings.handleDialButtons(player, componentId);
		} else if (interfaceId == 728)
			PlayerLook.handleYrsaShoes(player, componentId, slotId);
		else if (interfaceId == 52) {
			if (componentId >= 30 && componentId <= 34) {
				player.getTemporaryAttributtes().put("selected_canoe", componentId - 30);
				Canoes.createShapedCanoe(player);
			}
		} else if (interfaceId == 53) {
			int selectedArea = -1;
			if (componentId == 47)
				selectedArea = 0;
			else if (componentId == 48)
				selectedArea = 1;
			else if (componentId == 3)
				selectedArea = 2;
			else if (componentId == 6)
				selectedArea = 3;
			else if (componentId == 49)
				selectedArea = 4;
			if (selectedArea != -1)
				Canoes.deportCanoeStation(player, selectedArea);
		} else if (interfaceId == 735) {
			if (componentId >= 14 && componentId <= 14 + 64)
				FairyRings.sendRingTeleport(player, componentId - 14);
		} else if (interfaceId == 95) {
			if (componentId >= 23 && componentId <= 33)
				CarrierTravel.handleCharterOptions(player, componentId);
		} else if (interfaceId == 309)
			PlayerLook.handleHairdresserSalonButtons(player, componentId, slotId);
		else if (interfaceId == 729)
			PlayerLook.handleThessaliasMakeOverButtons(player, componentId, slotId);
		else if (interfaceId == 365)
			player.getTreasureTrailsManager().handleSextant(componentId);
		else if (interfaceId == 364) {
			if (componentId == 4)
				player.getPackets().sendGameMessage(ItemExamines.getExamine(new Item(slotId2)));
		} else if (interfaceId == 187) {
			if (componentId == 1) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					player.getMusicsManager().playAnotherMusic(slotId / 2);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.getMusicsManager().sendHint(slotId / 2);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					player.getMusicsManager().addToPlayList(slotId / 2);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					player.getMusicsManager().removeFromPlayList(slotId / 2);
			} else if (componentId == 4)
				player.getMusicsManager().addPlayingMusicToPlayList();
			else if (componentId == 9) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					player.getMusicsManager().playAnotherMusicFromPlayListByIndex(slotId);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.getMusicsManager().removeFromPlayListByIndex(slotId);

			} else if (componentId == 11)
				player.getMusicsManager().switchPlayListOn();
			else if (componentId == 12)
				player.getMusicsManager().clearPlayList();
			else if (componentId == 14)
				player.getMusicsManager().switchShuffleOn();
		} else if ((interfaceId == 590 && componentId == 8) || interfaceId == 464) {
			player.getEmotesManager().useBookEmote(interfaceId == 464 ? componentId : EmotesManager.getId(slotId, packetId));
		} else if (interfaceId == 192) {
			if (componentId == 2)
				player.getCombatDefinitions().switchDefensiveCasting();
			else if (componentId == 7)
				player.getCombatDefinitions().switchShowCombatSpells();
			else if (componentId == 9)
				player.getCombatDefinitions().switchShowTeleportSkillSpells();
			else if (componentId == 11)
				player.getCombatDefinitions().switchShowMiscallaneousSpells();
			else if (componentId == 13)
				player.getCombatDefinitions().switchShowSkillSpells();
			else if (componentId >= 15 & componentId <= 17)
				player.getCombatDefinitions().setSortSpellBook(componentId - 15);
			else
				Magic.processNormalSpell(player, componentId, packetId);
		} else if (interfaceId == 645) {
			if (componentId == 16) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					ItemSets.sendComponents(player, slotId2);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					ItemSets.exchangeSet(player, slotId2);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					ItemSets.examineSet(player, slotId2);
			}
		} else if (interfaceId == 644) {
			if (componentId == 0) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					ItemSets.sendComponentsBySlot(player, slotId, slotId2);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					ItemSets.exchangeSet(player, slotId, slotId2);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					player.getInventory().sendExamine(slotId);
			}
		} else if (interfaceId == 334) {
			if (componentId == 22)
				player.closeInterfaces();
			else if (componentId == 21)
				player.getTrade().accept(false);
		} else if (interfaceId == 335) {
			if (componentId == 18)
				player.getTrade().accept(true);
			else if (componentId == 20)
				player.closeInterfaces();
			else if (componentId == 53) {
				player.getPackets().sendInputIntegerScript("Enter amount:");
				player.getTemporaryAttributtes().put(Key.TRADE_COIN_WITHDRAWL, true);
			} else if (componentId == 32) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					player.getTrade().removeItem(slotId, 1);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.getTrade().removeItem(slotId, 5);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					player.getTrade().removeItem(slotId, 10);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					player.getTrade().removeItem(slotId, Integer.MAX_VALUE);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getTemporaryAttributtes().put("trade_item_X_Slot", slotId);
					player.getTemporaryAttributtes().put("trade_isRemove", Boolean.TRUE);
					player.getPackets().sendExecuteScript(108, new Object[]
							{ "Enter Amount:" });
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET)
					player.getTrade().sendValue(slotId, false);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
					player.getTrade().sendExamine(slotId, false);
			} else if (componentId == 35) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					player.getTrade().sendValue(slotId, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
					player.getTrade().sendExamine(slotId, true);
			}
		} else if (interfaceId == 336) {
			if (componentId == 0) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					player.getTrade().addItem(slotId, 1);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.getTrade().addItem(slotId, 5);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					player.getTrade().addItem(slotId, 10);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					player.getTrade().addItem(slotId, Integer.MAX_VALUE);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getTemporaryAttributtes().put("trade_item_X_Slot", slotId);
					player.getTemporaryAttributtes().remove("trade_isRemove");
					player.getPackets().sendExecuteScript(108, new Object[]
							{ "Enter Amount:" });
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET)
					player.getTrade().sendValue(slotId);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
					player.getInventory().sendExamine(slotId);
			}
		} else if (interfaceId == PartyRoom.CHEST_INV_INTERFACE
				|| interfaceId == ChambersOfXeric.PRIVATE_STORAGE_INTERFACE
				|| interfaceId == ChambersOfXeric.SHARED_STORAGE_INTERFACE) {
			if(interfaceId == PartyRoom.CHEST_INV_INTERFACE
					&& player.getInterfaceManager().containsInterface(ChambersOfXeric.SHARED_STORAGE_INTERFACE)) {
				player.getTemporaryAttributtes().put(Key.X_DIALOG_INTERFACE, -ChambersOfXeric.SHARED_STORAGE_INTERFACE);
			} else if(interfaceId == PartyRoom.CHEST_INV_INTERFACE
					&& player.getInterfaceManager().containsInterface(ChambersOfXeric.PRIVATE_STORAGE_INTERFACE)) {
				player.getTemporaryAttributtes().put(Key.X_DIALOG_INTERFACE, -ChambersOfXeric.PRIVATE_STORAGE_INTERFACE);
			} else {
				player.getTemporaryAttributtes().put(Key.X_DIALOG_INTERFACE, interfaceId);
			}

			// use -(main interface id) for reusable inv
			int overwriteId = (int) player.getTemporaryAttributtes().get(Key.X_DIALOG_INTERFACE);

			ChambersOfXeric raid = ChambersOfXeric.getRaid(player);

			ItemsContainer<Item> items =
					raid != null && overwriteId == ChambersOfXeric.SHARED_STORAGE_INTERFACE ? raid.getSharedItems()
			:		overwriteId == ChambersOfXeric.PRIVATE_STORAGE_INTERFACE ? player.getPrivateItems()
			: player.getInventory().getItems();

			if(slotId >= items.getSize()) {
				return;
			}

			Item item = items.get(slotId);
			if(item == null) {
				return;
			}

			int amt = 1;
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
				amt = 1;
			} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
				amt = 5;
			} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
				amt = 10;
			} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
				amt = items.getNumberOf(item.getId());
			} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
				player.getTemporaryAttributtes().put(Key.X_DIALOG, item.getId());
				player.getPackets().sendExecuteScript(108, new Object[]
						{"Enter Amount:"});
				return;
			}

			raid = ChambersOfXeric.getRaid(player);
			switch(overwriteId) {
				case ChambersOfXeric.SHARED_STORAGE_INTERFACE:
				case -ChambersOfXeric.SHARED_STORAGE_INTERFACE:
					if(raid != null) {
						if(overwriteId < 0)
							raid.sharedDeposit(player, item.getId(), amt);
						else
							raid.sharedWithdraw(player, item.getId(), amt);
					}
					break;
				case ChambersOfXeric.PRIVATE_STORAGE_INTERFACE:
				case -ChambersOfXeric.PRIVATE_STORAGE_INTERFACE:
					if(overwriteId < 0) {
						if(raid != null) {
							raid.privateDeposit(player, item.getId(), amt, true);
						}
					} else {
						ChambersOfXeric.privateWithdraw(player, item.getId(), amt);
					}

					break;
				case PartyRoom.CHEST_INV_INTERFACE:
					PartyRoom.deposit(player, item.getId(), amt);
					break;
			}
		} else if (interfaceId == 300) {
			for (int option = 3; option < 7; option++) {
				for (int index = 0; index < Smithing.COMPONENTS[0].length; index++) {
					if (Smithing.COMPONENTS[0][index] + option == componentId) {
						int cycles = option == 3 ? 28 : option == 4 ? -1 : option == 5 ? 5 : option == 6 ? 1 : -1;
						if (cycles == -1) {
							player.getPackets().sendInputIntegerScript("How many would you like to make: ");
							player.getTemporaryAttributtes().put(Key.FORGE_X, index);
						} else {
							player.closeInterfaces();
							player.getActionManager().setAction(new Smithing(index, cycles, false));
						}
						break;
					}
				}
			}
		} else if (interfaceId == 934) {
			for (int index = 0; index < Smithing.COMPONENTS[1].length; index++) {
				if (componentId == (index == 0 ? 22 : index == 1 ? 23 : (14 + index * 5))) {
					int cycles = packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET ? 1 : packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET ? 5 : packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET ? -1 : 28;
					if (cycles == -1) {
						player.getPackets().sendInputIntegerScript("How many would you like to make: ");
						player.getTemporaryAttributtes().put(Key.FORGE_X, index + 100);
					} else {
						player.closeInterfaces();
						player.getActionManager().setAction(new Smithing(index, cycles, true));
					}
				}
			}
		} else if (interfaceId == DungeonRewardShop.REWARD_SHOP) {
			if (componentId == 2) {
				if (slotId % 5 == 0) {
					DungeonRewardShop.select(player, slotId);
				}
			} else if (componentId == 64)
				DungeonRewardShop.sendConfirmationPurchase(player);
			else if (componentId == 48)
				DungeonRewardShop.purchase(player);
			else if (componentId == 50)
				DungeonRewardShop.removeConfirmationPurchase(player);
		} else if (interfaceId == LoyaltyProgram.LOYALTY_INTERFACE) {
			LoyaltyProgram.handleButtonClick(player, componentId, slotId);
		} else if (interfaceId == 206) {
			if (componentId == 15 && (player.getTemporaryAttributtes().get(Key.LOOTING_BAG) == null)) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					player.getPriceCheckManager().removeItem(slotId, 1);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.getPriceCheckManager().removeItem(slotId, 5);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					player.getPriceCheckManager().removeItem(slotId, 10);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					player.getPriceCheckManager().removeItem(slotId, Integer.MAX_VALUE);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getTemporaryAttributtes().put("pc_item_X_Slot", slotId);
					player.getTemporaryAttributtes().put("pc_isRemove", Boolean.TRUE);
					player.getPackets().sendExecuteScript(108, new Object[]
							{ "Enter Amount:" });
				}
			}
		} else if (interfaceId == 79) {
			Summoning.handleInfusionOptions(player, packetId, componentId, slotId, slotId2, false);
		} else if (interfaceId == 207) {
			if (componentId == 0) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					player.getPriceCheckManager().addItem(slotId, 1);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.getPriceCheckManager().addItem(slotId, 5);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					player.getPriceCheckManager().addItem(slotId, 10);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					player.getPriceCheckManager().addItem(slotId, Integer.MAX_VALUE);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getTemporaryAttributtes().put("pc_item_X_Slot", slotId);
					player.getTemporaryAttributtes().remove("pc_isRemove");
					player.getPackets().sendExecuteScript(108, new Object[]
							{ "Enter Amount:" });
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET)
					player.getInventory().sendExamine(slotId);
			}
		} else if (interfaceId == 665) {
			if (player.getFamiliar() == null || player.getFamiliar().getBob() == null)
				return;
			if (componentId == 0) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					player.getFamiliar().getBob().addItem(slotId, 1);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.getFamiliar().getBob().addItem(slotId, 5);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					player.getFamiliar().getBob().addItem(slotId, 10);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					player.getFamiliar().getBob().addItem(slotId, Integer.MAX_VALUE);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getTemporaryAttributtes().put("bob_item_X_Slot", slotId);
					player.getTemporaryAttributtes().remove("bob_isRemove");
					player.getPackets().sendExecuteScript(108, new Object[]
							{ "Enter Amount:" });
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET)
					player.getInventory().sendExamine(slotId);
			}
		} else if (interfaceId == 275) {
			if (componentId == 7 && player.getInterfaceManager().containsScreenInterface())
				player.getInterfaceManager().removeScreenInterfaceBG();
		} else if (interfaceId == 671) {
			if (player.getFamiliar() == null || player.getFamiliar().getBob() == null)
				return;
			if (componentId == 27) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					player.getFamiliar().getBob().removeItem(slotId, 1);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.getFamiliar().getBob().removeItem(slotId, 5);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					player.getFamiliar().getBob().removeItem(slotId, 10);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					player.getFamiliar().getBob().removeItem(slotId, Integer.MAX_VALUE);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getTemporaryAttributtes().put("bob_item_X_Slot", slotId);
					player.getTemporaryAttributtes().put("bob_isRemove", Boolean.TRUE);
					player.getPackets().sendExecuteScript(108, new Object[]
							{ "Enter Amount:" });
				}
			} else if (componentId == 29)
				player.getFamiliar().takeBob();
		} else if (interfaceId == 916) {
			SkillsDialogue.handleSetQuantityButtons(player, componentId);
		} else if (interfaceId == 193) {
			if (componentId == 5)
				player.getCombatDefinitions().switchShowCombatSpells();
			else if (componentId == 7)
				player.getCombatDefinitions().switchShowTeleportSkillSpells();
			else if (componentId >= 9 && componentId <= 11)
				player.getCombatDefinitions().setSortSpellBook(componentId - 9);
			else if (componentId == 18)
				player.getCombatDefinitions().switchDefensiveCasting();
			else
				Magic.processAncientSpell(player, componentId, packetId);
		} else if (interfaceId == 430) {
			if (componentId == 5)
				player.getCombatDefinitions().switchShowCombatSpells();
			else if (componentId == 7)
				player.getCombatDefinitions().switchShowTeleportSkillSpells();
			else if (componentId == 9)
				player.getCombatDefinitions().switchShowMiscallaneousSpells();
			else if (componentId >= 11 & componentId <= 13)
				player.getCombatDefinitions().setSortSpellBook(componentId - 11);
			else if (componentId == 20)
				player.getCombatDefinitions().switchDefensiveCasting();
			else
				Magic.processLunarSpell(player, componentId, packetId);
		} else if (interfaceId == 261) {
			if (player.getInterfaceManager().containsInventoryInter())
				return;
			if (componentId == 22) {
				if (player.getInterfaceManager().containsScreenInter()) {
					player.getPackets().sendGameMessage("Please close the interface you have open before setting your graphic options.");
					return;
				}
				player.stopAll();
				//player.getInterfaceManager().sendInterface(742);
				player.getDialogueManager().startDialogue("GraphicSettingsSelect");
			} else if (componentId == 6)
				player.switchRightClickReporting();
			else if (componentId == 11)
				player.switchProfanityFilter();
			else if (componentId == 12)
				player.switchAllowChatEffects();
			else if (componentId == 13) // chat setup
				player.getInterfaceManager().sendSettings(982);
			else if (componentId == 16) // house options
				player.getInterfaceManager().sendSettings(398);
			else if (componentId == 14)
				player.switchMouseButtons();
			else if (componentId == 15)
				player.switchAcceptAid();
			else if (componentId == 24) // audio options
				player.getInterfaceManager().sendSettings(429);
			else if (componentId == 26)
				//AdventurersLog.open(player);
				player.getKeyBinds().open(player);
			else if (componentId == 32)
				player.getKeyBinds().open(player);
		} else if (interfaceId == 429) {
			if (componentId == 18)
				player.getInterfaceManager().sendSettings();
		} else if (interfaceId == 398) {
			if (componentId == 19)
				player.getInterfaceManager().sendSettings();
			else if (componentId == 15 || componentId == 1) {
				if (!player.getBank().hasVerified(7))
					return;
				player.getHouse().setBuildMode(componentId == 15);
			} else if (componentId == 25 || componentId == 26)
				player.getHouse().setArriveInPortal(componentId == 25);
			else if (componentId == 27)
				player.getHouse().expelGuests();
			else if (componentId == 29)
				House.leaveHouse(player);
		} else if (interfaceId == 402) {
			if (componentId >= 93 && componentId <= 115)
				player.getHouse().createRoom(componentId - 93);
		} else if (interfaceId == 394 || interfaceId == 396) {
			if (componentId == 11)
				player.getHouse().build(slotId);
		} else if (interfaceId == 982) {
			if (componentId == 5)
				player.getInterfaceManager().sendSettings();
			else if (componentId == 41)
				player.setPrivateChatSetup(player.getPrivateChatSetup() == 0 ? 1 : 0);
			else if (componentId >= 17 && componentId <= 36)
				player.setClanChatSetup(componentId - 17);
			else if (componentId >= 49 && componentId <= 66)
				player.setPrivateChatSetup(componentId - 48);
			else if (componentId >= 72 && componentId <= 91)
				player.setFriendChatSetup(componentId - 72);
			else if (componentId >= 97 && componentId <= 116)
				player.setGuestChatSetup(componentId - 97);
		} else if (interfaceId == 271) {
			/*WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					if (player.isDead())
						return;*/
					if (componentId == 8 || componentId == 42) {
						player.getPrayer().switchPrayer(slotId);
					} else if (componentId == 43 && player.getPrayer().isUsingQuickPrayer())
						player.getPrayer().switchSettingQuickPrayer();
				/*}
			});*/

		} else if (interfaceId == 320) {
			int lvlupSkill = -1;
			int skillMenu = -1;
			switch (componentId) {
			case 150: // Attack
				skillMenu = 1;
				lvlupSkill = 0;
				if (player.getTemporaryAttributtes().remove("leveledUp[0]") != Boolean.TRUE) {
					player.getVarsManager().sendVar(965, 1);
				} else {
					player.getVarsManager().sendVar(1230, 10);
				}
				break;
			case 9: // Strength
				skillMenu = 2;
				lvlupSkill = 2;
				if (player.getTemporaryAttributtes().remove("leveledUp[2]") != Boolean.TRUE) {
					player.getVarsManager().sendVar(965, 2);
				} else {

					player.getVarsManager().sendVar(1230, 20);
				}
				break;
			case 22: // Defence
				skillMenu = 5;
				lvlupSkill = 1;
				if (player.getTemporaryAttributtes().remove("leveledUp[1]") != Boolean.TRUE) {
					player.getVarsManager().sendVar(965, 5);
				} else {
					player.getVarsManager().sendVar(1230, 40);
				}
				break;
			case 40: // Ranged
				skillMenu = 3;
				lvlupSkill = 4;
				if (player.getTemporaryAttributtes().remove("leveledUp[4]") != Boolean.TRUE) {
					player.getVarsManager().sendVar(965, 3);
				} else {
					player.getVarsManager().sendVar(1230, 30);
				}
				break;
			case 58: // Prayer
				skillMenu = 7;
				lvlupSkill = 5;
				if (player.getTemporaryAttributtes().remove("leveledUp[5]") != Boolean.TRUE) {
					player.getVarsManager().sendVar(965, 7);
				} else {
					player.getVarsManager().sendVar(1230, 60);
				}
				break;
			case 71: // Magic
				skillMenu = 4;
				lvlupSkill = 6;
				if (player.getTemporaryAttributtes().remove("leveledUp[6]") != Boolean.TRUE) {
					player.getVarsManager().sendVar(965, 4);
				} else {
					player.getVarsManager().sendVar(1230, 33);
				}
				break;
			case 84: // Runecrafting
				skillMenu = 12;
				lvlupSkill = 20;
				if (player.getTemporaryAttributtes().remove("leveledUp[20]") != Boolean.TRUE) {
					player.getVarsManager().sendVar(965, 12);
				} else {
					player.getVarsManager().sendVar(1230, 100);
				}
				break;
			case 102: // Construction
				skillMenu = 22;
				lvlupSkill = Skills.CONSTRUCTION;
				if (player.getTemporaryAttributtes().remove("leveledUp[22]") != Boolean.TRUE) {
					player.getVarsManager().sendVar(965, 22);
				} else {
					player.getVarsManager().sendVar(1230, 698);
				}
				break;
			case 145: // Hitpoints
				skillMenu = 6;
				lvlupSkill = 3;
				if (player.getTemporaryAttributtes().remove("leveledUp[3]") != Boolean.TRUE) {
					player.getVarsManager().sendVar(965, 6);
				} else {
					player.getVarsManager().sendVar(1230, 50);
				}
				break;
			case 15: // Agility
				skillMenu = 8;
				lvlupSkill = 16;
				if (player.getTemporaryAttributtes().remove("leveledUp[16]") != Boolean.TRUE) {
					player.getVarsManager().sendVar(965, 8);
				} else {
					player.getVarsManager().sendVar(1230, 65);
				}
				break;
			case 28: // Herblore
				skillMenu = 9;
				lvlupSkill = 15;
				if (player.getTemporaryAttributtes().remove("leveledUp[15]") != Boolean.TRUE) {
					player.getVarsManager().sendVar(965, 9);
				} else {
					player.getVarsManager().sendVar(1230, 75);
				}
				break;
			case 46: // Thieving
				skillMenu = 10;
				lvlupSkill = 17;
				if (player.getTemporaryAttributtes().remove("leveledUp[17]") != Boolean.TRUE) {
					player.getVarsManager().sendVar(965, 10);
				} else {
					player.getVarsManager().sendVar(1230, 80);
				}
				break;
			case 64: // Crafting
				skillMenu = 11;
				lvlupSkill = 12;
				if (player.getTemporaryAttributtes().remove("leveledUp[12]") != Boolean.TRUE) {
					player.getVarsManager().sendVar(965, 11);
				} else {
					player.getVarsManager().sendVar(1230, 90);
				}
				break;
			case 77: // Fletching
				skillMenu = 19;
				lvlupSkill = 9;
				if (player.getTemporaryAttributtes().remove("leveledUp[9]") != Boolean.TRUE) {
					player.getVarsManager().sendVar(965, 19);
				} else {
					player.getVarsManager().sendVar(1230, 665);
				}
				break;
			case 90: // Slayer
				skillMenu = 20;
				lvlupSkill = 18;
				if (player.getTemporaryAttributtes().remove("leveledUp[18]") != Boolean.TRUE) {
					player.getVarsManager().sendVar(965, 20);
				} else {
					player.getVarsManager().sendVar(1230, 673);
				}
				break;
			case 108: // Hunter
				skillMenu = 23;
				lvlupSkill = Skills.HUNTER;
				if (player.getTemporaryAttributtes().remove("leveledUp[21]") != Boolean.TRUE) {
					player.getVarsManager().sendVar(965, 23);
				} else {
					player.getVarsManager().sendVar(1230, 689);
				}
				break;
			case 140: // Mining
				skillMenu = 13;
				lvlupSkill = 14;
				if (player.getTemporaryAttributtes().remove("leveledUp[14]") != Boolean.TRUE) {
					player.getVarsManager().sendVar(965, 13);
				} else {
					player.getVarsManager().sendVar(1230, 110);
				}
				break;
			case 135: // Smithing
				skillMenu = 14;
				lvlupSkill = 13;
				if (player.getTemporaryAttributtes().remove("leveledUp[13]") != Boolean.TRUE) {
					player.getVarsManager().sendVar(965, 14);
				} else {
					player.getVarsManager().sendVar(1230, 115);
				}
				break;
			case 34: // Fishing
				skillMenu = 15;
				lvlupSkill = 10;
				if (player.getTemporaryAttributtes().remove("leveledUp[10]") != Boolean.TRUE) {
					player.getVarsManager().sendVar(965, 15);
				} else {
					player.getVarsManager().sendVar(1230, 120);
				}
				break;
			case 52: // Cooking
				skillMenu = 16;
				lvlupSkill = 7;
				if (player.getTemporaryAttributtes().remove("leveledUp[7]") != Boolean.TRUE) {
					player.getVarsManager().sendVar(965, 16);
				} else {
					player.getVarsManager().sendVar(1230, 641);
				}
				break;
			case 130: // Firemaking
				skillMenu = 17;
				lvlupSkill = 11;
				if (player.getTemporaryAttributtes().remove("leveledUp[11]") != Boolean.TRUE) {
					player.getVarsManager().sendVar(965, 17);
				} else {

					player.getVarsManager().sendVar(1230, 649);
				}
				break;
			case 125: // Woodcutting
				skillMenu = 18;
				lvlupSkill = 8;
				if (player.getTemporaryAttributtes().remove("leveledUp[8]") != Boolean.TRUE) {
					player.getVarsManager().sendVar(965, 18);
				} else {
					player.getVarsManager().sendVar(1230, 660);
				}
				break;
			case 96: // Farming
				skillMenu = 21;
				lvlupSkill = 19;
				if (player.getTemporaryAttributtes().remove("leveledUp[19]") != Boolean.TRUE) {
					player.getVarsManager().sendVar(965, 21);
				} else {
					player.getVarsManager().sendVar(1230, 681);
				}
				break;
			case 114: // Summoning
				skillMenu = 24;
				lvlupSkill = 23;
				if (player.getTemporaryAttributtes().remove("leveledUp[23]") != Boolean.TRUE) {
					player.getVarsManager().sendVar(965, 24);
				} else {
					player.getVarsManager().sendVar(1230, 705);
				}
				break;
			case 120: // Dung
				skillMenu = 25;
				lvlupSkill = 24;
				if (player.getTemporaryAttributtes().remove("leveledUp[24]") != Boolean.TRUE) {
					player.getVarsManager().sendVar(965, 25);
				} else {
					player.getVarsManager().sendVar(1230, 705);
				}
				break;
			}
			if (skillMenu != -1) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					player.stopAll();
					Magic.sendCommandTeleportSpell(player, EconomyManager.SKILL_TELES[lvlupSkill]);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
					player.stopAll();
				//	openSkillGuide(player);
					  player.getInterfaceManager().sendInterface( Skills.isFlashOn(player, lvlupSkill) ?
					  741 : 499);
					Skills.switchFlash(player, lvlupSkill, false);
					if (skillMenu != -1)
						player.getTemporaryAttributtes().put("skillMenu", skillMenu);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
					player.getPackets().sendInputIntegerScript("Set level target:");
					player.getTemporaryAttributtes().remove(Key.SET_XP_TARGET);
					player.getTemporaryAttributtes().put(Key.SET_LEVEL_TARGET, lvlupSkill);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					player.getPackets().sendInputIntegerScript("Set xp target:");
					player.getTemporaryAttributtes().remove(Key.SET_LEVEL_TARGET);
					player.getTemporaryAttributtes().put(Key.SET_XP_TARGET, lvlupSkill);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) 
					player.getSkills().resetTarget(lvlupSkill);
				
			}
		} else if (interfaceId == 1218) {
			if ((componentId >= 33 && componentId <= 55) || componentId == 120 || componentId == 151 || componentId == 189)
				player.getInterfaceManager().setInterface(false, 1218, 1, 1217); // seems
			// to
			// fix
		} else if (interfaceId == 499) {
			int skillMenu = -1;
			if (player.getTemporaryAttributtes().get("skillMenu") != null)
				skillMenu = (Integer) player.getTemporaryAttributtes().get("skillMenu");
			if (componentId >= 9 && componentId <= 25)
				player.getVarsManager().sendVar(965, ((componentId - 9) * 1024) + skillMenu);
			else if (componentId == 29)
				// close inter
				player.stopAll();
		} else if (interfaceId == 741) {
			if (componentId == 9)
				player.getPackets().sendOpenURL("https://matrixrsps.io/forums/index.php?/forum/16-guides/");
		} else if (interfaceId == 387) {
			if (player.getInterfaceManager().containsInventoryInter())
				return;
			if(componentId == 30 && packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
				int slot = player.getEquipment().getItems().getThisItemSlot(slotId2);

				if(slotId2 == 25659) {
					player.getEquipment().getItem(slot).setId(25670);
					player.getEquipment().refresh(slot);
					player.getAppearence().generateAppearenceData();
					player.sendMessage("The boots transform!");
					return;
				}
				if(slotId2 == 25670) {
					player.getEquipment().getItem(slot).setId(25671);
					player.getEquipment().refresh(slot);
					player.getAppearence().generateAppearenceData();
					player.sendMessage("The boots transform!");
					return;
				}
				if(slotId2 == 25671) {
					player.getEquipment().getItem(slot).setId(25659);
					player.getEquipment().refresh(slot);
					player.getAppearence().generateAppearenceData();
					player.sendMessage("The boots transform!");
					return;
				}
			}
			if (componentId == 6) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					int hatId = player.getEquipment().getHatId();
					if (hatId == 24437 || hatId == 24439 || hatId == 24440 || hatId == 24441) {
						player.getDialogueManager().startDialogue("FlamingSkull", player.getEquipment().getItem(Equipment.SLOT_HAT), -1);
						return;
					}
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					ButtonHandler.sendRemove(player, Equipment.SLOT_HAT);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
					player.getEquipment().sendExamine(Equipment.SLOT_HAT);
			} else if (componentId == 9) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					int capeId = player.getEquipment().getCapeId();
					if (capeId == 25528 || capeId == 20769 || capeId == 20771 || capeId == 32152 || capeId == 32153)
						SkillCapeCustomizer.startCustomizing(player, capeId);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					int capeId = player.getEquipment().getCapeId();
					if (capeId == 20767 || capeId == 32151)
						SkillCapeCustomizer.startCustomizing(player, capeId);
					else if (capeId == 52114)
						MythGuild.teleport(player);
					else if (capeId == 20763 || capeId == 24709)
						player.getEmotesManager().useBookEmote(39);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					ButtonHandler.sendRemove(player, Equipment.SLOT_CAPE);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
					player.getEquipment().sendExamine(Equipment.SLOT_CAPE);
			} else if (componentId == 12) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					ItemTransportation.sendTeleport(player, player.getEquipment().getItem(Equipment.SLOT_AMULET), 0, true);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
					ItemTransportation.sendTeleport(player, player.getEquipment().getItem(Equipment.SLOT_AMULET), 1, true);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					ItemTransportation.sendTeleport(player, player.getEquipment().getItem(Equipment.SLOT_AMULET), 2, true);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					ItemTransportation.sendTeleport(player, player.getEquipment().getItem(Equipment.SLOT_AMULET), 3, true);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					ButtonHandler.sendRemove(player, Equipment.SLOT_AMULET);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
					player.getEquipment().sendExamine(Equipment.SLOT_AMULET);
			} else if (componentId == 15) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					int weaponId = player.getEquipment().getWeaponId();
					if (weaponId == 15484)
						player.getInterfaceManager().gazeOrbOfOculus();
					else if (weaponId == 14057) // broomstick
						player.setNextAnimation(new Animation(10532));
					else if (weaponId == 18349 || weaponId == 18351 || weaponId == 18353 || weaponId == 18355 || weaponId == 18357 || weaponId == 18359)//chaotics
						player.getCharges().checkPercentage("There is " + ChargesManager.REPLACE + "% of charge remaining.", weaponId, false);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
					int weaponId = player.getEquipment().getWeaponId();
					if (weaponId == 14057) // broomstick
						SorceressGarden.teleportToSocreressGarden(player, true);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					ButtonHandler.sendRemove(player, Equipment.SLOT_WEAPON);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
					player.getEquipment().sendExamine(Equipment.SLOT_WEAPON);
			} else if (componentId == 18)
				ButtonHandler.sendRemove(player, Equipment.SLOT_CHEST);
			else if (componentId == 21) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					int shieldId = player.getEquipment().getShieldId();
					if (shieldId == 11283 || shieldId == 52002 || shieldId == 51633) {
						if (player.getDFSDelay() >= Utils.currentTimeMillis()) {
							player.getPackets().sendGameMessage("You must wait two minutes before performing this attack once more.");
							return;
						}
						player.getTemporaryAttributtes().put("dfs_shield_active", true);
					} else if (shieldId == 11284  || shieldId == 52003 || shieldId == 51634)
						player.getPackets().sendGameMessage("You don't have any charges in your shield.");
				} else {
					ButtonHandler.sendRemove(player, Equipment.SLOT_SHIELD);
				}
			} else if (componentId == 24)
				ButtonHandler.sendRemove(player, Equipment.SLOT_LEGS);
			else if (componentId == 27) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					int glovesId = player.getEquipment().getGlovesId();
					if ((glovesId >= 24450 && glovesId <= 24454) || (glovesId >= 22358 && glovesId <= 22369))
						player.getCharges().checkPercentage("The gloves are " + ChargesManager.REPLACE + "% degraded.", glovesId, true);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					ButtonHandler.sendRemove(player, Equipment.SLOT_HANDS);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
					player.getEquipment().sendExamine(Equipment.SLOT_HANDS);
			} else if (componentId == 30) {
				ButtonHandler.sendRemove(player, Equipment.SLOT_FEET);
			} else if (componentId == 33) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					int ringId = player.getEquipment().getRingId();
					if (ringId == 15707) {
						player.getDungManager().openPartyInterface();
						return;
					} else if (ringId == 13281) {
						ItemTransportation.transportationDialogue(player, player.getEquipment().getItem(Equipment.SLOT_RING), true, true);
						return;
					}
					ItemTransportation.sendTeleport(player, player.getEquipment().getItem(Equipment.SLOT_RING), 0, true);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
					int ringId = player.getEquipment().getRingId();
					if (ringId == 15707) {
						Magic.sendTeleportSpell(player, 13652, 13654, 2602, 2603, 1, 0, new WorldTile(3447, 3694, 0), 10, true, Magic.ITEM_TELEPORT);
						return;
					} else if (ringId == 13281) {
						player.getSlayerManager().checkKillsLeft();
						return;
					}
					ItemTransportation.sendTeleport(player, player.getEquipment().getItem(Equipment.SLOT_RING), 1, true);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					int ringId = player.getEquipment().getRingId();
					if (ringId == 13281) {
						//player.getSlayerManager().checkKillsLeft();
						return;
					}
					ItemTransportation.sendTeleport(player, player.getEquipment().getItem(Equipment.SLOT_RING), 2, true);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					ItemTransportation.sendTeleport(player, player.getEquipment().getItem(Equipment.SLOT_RING), 3, true);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					ButtonHandler.sendRemove(player, Equipment.SLOT_RING);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
					player.getEquipment().sendExamine(Equipment.SLOT_RING);
			} else if (componentId == 36)
				ButtonHandler.sendRemove(player, Equipment.SLOT_ARROWS);
			else if (componentId == 45) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					ButtonHandler.sendRemove(player, Equipment.SLOT_AURA);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
					player.getEquipment().sendExamine(Equipment.SLOT_AURA);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.getAuraManager().activate();
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					player.getAuraManager().sendAuraRemainingTime();
			} else if (componentId == 40) {
				player.stopAll();
				openItemsKeptOnDeath(player);
			} else if (componentId == 41) {
				player.stopAll();
				player.getInterfaceManager().sendInterface(1178);
			} else if (componentId == 37) {
				openEquipmentBonuses(player, false);
			}
		} else if (interfaceId == 1297) {
			if (componentId >= 53 && componentId <= 63)
				ExtraSettings.switchOption(player, componentId - 53);
			else if (componentId == 271)
				ExtraSettings.setPage(player, 0);
			else if (componentId == 264)
				ExtraSettings.setPage(player, 1);
			else if (componentId == 257)
				ExtraSettings.setPage(player, 2);
		} else if (interfaceId == 17) {
			if (componentId == 28)
				sendItemsKeptOnDeath(player, player.getVarsManager().getBitValue(9226) == 0);
		} else if (interfaceId == 1265) {
			Shop shop = (Shop) player.getTemporaryAttributtes().get("Shop");
			if (shop == null)
				return;
			if (componentId == 49 || componentId == 50)
				player.setVerboseShopDisplayMode(componentId == 50);
			else if (componentId == 28 || componentId == 29)
				Shop.setBuying(player, componentId == 28);
			else if (componentId == 171)
				shop.stats(player);
			else if (componentId == 20) {
				boolean buying = Shop.isBuying(player);
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					shop.sendInfo(player, slotId, !buying);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					if (buying)
						shop.buy(player, slotId, 1);
					else
						shop.sell(player, slotId, 1);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
					if (buying)
						shop.buy(player, slotId, 5);
					else
						shop.sell(player, slotId, 5);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					if (buying)
						shop.buy(player, slotId, 10);
					else
						shop.sell(player, slotId, 10);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					if (buying)
						shop.buy(player, slotId, 50);
					else
						shop.sell(player, slotId, 50);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET) {
					if (buying)
						shop.buy(player, slotId, 500);
					else
						shop.sell(player, slotId, 500);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON6_PACKET) {
					if (buying)
						shop.buyAll(player, slotId);
				}
			} else if (componentId == 220)
				shop.setTransaction(player, 1);
			else if (componentId == 217)
				shop.increaseTransaction(player, -5);
			else if (componentId == 214)
				shop.increaseTransaction(player, -1);
			else if (componentId == 15)
				shop.increaseTransaction(player, 1);
			else if (componentId == 208)
				shop.increaseTransaction(player, 5);
			else if (componentId == 211)
				shop.setTransaction(player, Integer.MAX_VALUE);
			else if (componentId == 201)
				shop.pay(player);
		} else if (interfaceId == 1266) {
			if (componentId == 0) {
				Shop shop = (Shop) player.getTemporaryAttributtes().get("Shop");
				if (shop == null)
					return;
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					shop.sendInfo(player, slotId, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					shop.sell(player, slotId, 1);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					shop.sell(player, slotId, 5);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					shop.sell(player, slotId, 10);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET)
					shop.sell(player, slotId, 50);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET)
					player.getInventory().sendExamine(slotId);
			}
		} else if (interfaceId == 640) {
			if (componentId == 18 || componentId == 22) {
				player.getTemporaryAttributtes().put("WillDuelFriendly", true);
				player.getVarsManager().sendVar(283, 67108864);
			} else if (componentId == 19 || componentId == 21) {
				player.getTemporaryAttributtes().put("WillDuelFriendly", false);
				player.getVarsManager().sendVar(283, 134217728);
			} else if (componentId == 20) {
				DuelControler.challenge(player);
			}
		} else if (interfaceId == 650) {
			if (componentId == 15) {
				player.stopAll();
			/*	player.setNextWorldTile(new WorldTile(2974, 4384, player.getPlane()));
				player.getControlerManager().startControler("CorpBeastControler");*/
				BossInstanceHandler.enterInstance(player, Boss.Corporeal_Beast);
			} else if (componentId == 16)
				player.closeInterfaces();
		} else if (interfaceId == 667) {
			if (componentId == 9) {
				if (slotId >= 15)
					return;
				Item item = player.getEquipment().getItem(slotId);
				if (item == null)
					return;
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
					player.getPackets().sendGameMessage(ItemExamines.getExamine(item));
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
					sendRemove(player, slotId);
					ButtonHandler.refreshEquipBonuses(player);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON10_PACKET)
					ButtonHandler.sendStats(player, item);
			} else if (componentId == 46 && player.getTemporaryAttributtes().remove("Banking") != null) {
				player.getBank().openBank();
			}
		} else if (interfaceId == 670) {
			if (player.getTemporaryAttributtes().get("runepouch") != null) {
				if (componentId == 0) {
					if (slotId >= player.getInventory().getItemsContainerSize())
						return;
					Item item = player.getInventory().getItem(slotId);
					if (item == null)
						return;
					if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
						RunePouch.storeRunePouch(player, item, 1);
						return;
					} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
						RunePouch.storeRunePouch(player, item, 10);
						return;
					} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
						RunePouch.storeRunePouch(player, item, 100);
						return;
					} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
						RunePouch.storeRunePouch(player, item, 16000);
					return;
				}
			} else {
				if (componentId == 0) {
					if (slotId >= player.getInventory().getItemsContainerSize())
						return;
					Item item = player.getInventory().getItem(slotId);
					if (item == null)
						return;
					if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {

						Item[] copy = player.getInventory().getItems().getItemsCopy();
						if (sendWear2(player, slotId, item.getId())) {
							ButtonHandler.refreshEquipBonuses(player);
							player.getInventory().refreshItems(copy);
							player.getAppearence().generateAppearenceData();
							player.getPackets().sendSound(2240, 0, 1);
						}
					} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
						player.getInventory().sendExamine(slotId);
					else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
						ButtonHandler.sendStats(player, item);
					else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
						ButtonHandler.sendCompare(player, item);
			}
			}
		} else if (interfaceId == Inventory.INVENTORY_INTERFACE) { // inventory
			if (componentId == 0) {
				if (slotId > 27 || player.getInterfaceManager().containsInventoryInter())
					return;
				Item item = player.getInventory().getItem(slotId);
				if (item == null || item.getId() != slotId2)
					return;
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					InventoryOptionsHandler.handleItemOption1(player, slotId, slotId2, item);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					InventoryOptionsHandler.handleItemOption2(player, slotId, slotId2, item);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					InventoryOptionsHandler.handleItemOption3(player, slotId, slotId2, item);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					InventoryOptionsHandler.handleItemOption4(player, slotId, slotId2, item);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET)
					InventoryOptionsHandler.handleItemOption5(player, slotId, slotId2, item);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON6_PACKET)
					InventoryOptionsHandler.handleItemOption6(player, slotId, slotId2, item);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON7_PACKET)
					InventoryOptionsHandler.handleItemOption7(player, slotId, slotId2, item);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
					InventoryOptionsHandler.handleItemOption8(player, slotId, slotId2, item);
			}
		} else if (interfaceId == 403)
			Sawmill.handlePlanksConvertButtons(player, componentId, packetId);
		else if (interfaceId == 749) {
			if (componentId == 4) {
				/*WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						if (player.isDead())
							return;*/
						if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) // activate
							player.getPrayer().switchQuickPrayers();
						else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) // switch
							player.getPrayer().switchSettingQuickPrayer();
				/*	}
				});*/
			}
		} else if (interfaceId == 750) {
			if (componentId == 4) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
					player.toogleRun(player.isResting() ? false : true);
					if (player.isResting())
						player.stopAll();
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					if (player.isResting()) {
						player.stopAll();
						return;
					}
					if (player.getEmotesManager().isDoingEmote()) {
						player.getPackets().sendGameMessage("You can't rest while perfoming an emote.");
						return;
					} else if (player.isLocked()) {
						player.getPackets().sendGameMessage("You can't rest while perfoming an action.");
						return;
					}
					Action action = player.getActionManager().getAction();
					if (action != null && !(action instanceof Rest)) {
						player.getPackets().sendGameMessage("Please finish what you are doing before resting.");
						return;
					}
					player.stopAll();

					player.getActionManager().setAction(new Rest(false));
				}
			}
		} else if (interfaceId == 11) {
			if (componentId == 17) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					player.getBank().depositItem(slotId, 1, false);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.getBank().depositItem(slotId, 5, false);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					player.getBank().depositItem(slotId, 10, false);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					player.getBank().depositItem(slotId, Integer.MAX_VALUE, false);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getTemporaryAttributtes().put("bank_item_X_Slot", slotId);
					player.getTemporaryAttributtes().remove("bank_isWithdraw");
					player.getPackets().sendExecuteScript(108, new Object[]
							{ "Enter Amount:" });
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET)
					player.getInventory().sendExamine(slotId);
			} else if (componentId == 18)
				player.getBank().depositAllInventory(false);
			else if (componentId == 20)
				player.getBank().depositAllMoneyPouch(false);
			else if (componentId == 22)
				player.getBank().depositAllEquipment(false);
			else if (componentId == 24)
				player.getBank().depositAllBob(false);
		} else if (interfaceId == 762) {
			if (!player.getInterfaceManager().containsInterface(763))
				return;
			if (componentId == 15)
				player.getBank().switchInsertItems();
			else if (componentId == 17)
				player.getPackets().sendCSVarInteger(190, 1);
			else if (componentId == 19)
				player.getBank().switchWithdrawNotes();
			else if (componentId == 33)
				player.getBank().depositAllInventory(true);
			else if (componentId == 35)
				player.getBank().depositAllMoneyPouch(true);
			else if (componentId == 37)
				player.getBank().depositAllEquipment(true);
			else if (componentId == 39)
				player.getBank().depositAllBob(true);
			else if (componentId == 46)
				player.getBank().openHelpInterface();
			else if (componentId == 124)
				player.getBank().switchPlaceHolders();
			else if (componentId == 17) {
				player.getPackets().sendCSVarInteger(11, -1);
			} else if (componentId == 46) {
				player.closeInterfaces();
				player.getInterfaceManager().sendInterface(767);
				player.setCloseInterfacesEvent(new Runnable() {
					@Override
					public void run() {
						player.getBank().openBank();
					}
				});
			} else if (componentId >= 46 && componentId <= 64) {
				int tabId = 9 - ((componentId - 46) / 2);
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					player.getBank().setCurrentTab(tabId);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.getBank().collapse(tabId);
			} else if (componentId == 95) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					player.getBank().withdrawItem(slotId, 1);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.getBank().withdrawItem(slotId, 5);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					player.getBank().withdrawItem(slotId, 10);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					player.getBank().withdrawLastAmount(slotId);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getTemporaryAttributtes().put("bank_item_X_Slot", slotId);
					player.getTemporaryAttributtes().put("bank_isWithdraw", Boolean.TRUE);
					player.getPackets().sendExecuteScript(108, new Object[]
							{ "Enter Amount:" });
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET)
					player.getBank().withdrawItem(slotId, Integer.MAX_VALUE);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON6_PACKET)
					player.getBank().withdrawItemButOne(slotId);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
					player.getBank().sendExamine(slotId);

			} else if (componentId == 119) {
				openEquipmentBonuses(player, true);
			}
		} else if (interfaceId == 763) {
			if (componentId == 0) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					player.getBank().depositItem(slotId, 1, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.getBank().depositItem(slotId, 5, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					player.getBank().depositItem(slotId, 10, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					player.getBank().depositLastAmount(slotId);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getTemporaryAttributtes().put("bank_item_X_Slot", slotId);
					player.getTemporaryAttributtes().remove("bank_isWithdraw");
					player.getPackets().sendExecuteScript(108, new Object[]
							{ "Enter Amount:" });
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET)
					player.getBank().depositItem(slotId, Integer.MAX_VALUE, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
					player.getInventory().sendExamine(slotId);
			}
		} else if (interfaceId == 1218) {
			if (componentId == 73)
				player.closeInterfaces();
		} else if (interfaceId == 767) {
			if (componentId == 10)
				player.getBank().openBank();
		} else if (interfaceId == 884) {
			if (componentId == 4) {
				submitSpecialRequest(player);
			} else if (componentId >= 7 && componentId <= 10)
				player.getCombatDefinitions().setAttackStyle(componentId - 7);
			else if (componentId == 11)
				player.getCombatDefinitions().switchAutoRelatie();
		} else if (interfaceId == 755) {
			if (componentId == 44)
				player.getPackets().sendRootInterface(player.getInterfaceManager().hasRezizableScreen() ? 746 : 548, 2);
			else if (componentId == 42) {
				player.getHintIconsManager().removeAll();
				player.getVarsManager().sendVar(1159, 1);
			}
		} else if (interfaceId == 20)
			SkillCapeCustomizer.handleSkillCapeCustomizer(player, componentId);
		else if (interfaceId == 1056) {
			if (componentId == 173) {
				QuestTab.set(player, 2);
				player.getInterfaceManager().openGameTab(3);
				//player.getInterfaceManager().sendInterface(917);
			}
		} else if (interfaceId == 925) {
			if (componentId == 70)
				player.getDeals().claim1();
			else if (componentId == 42)
				player.getDeals().claim2();
		} else if (interfaceId == 751) {
			if (componentId == 14) {
				if (player.isMuted()) {
					player.getPackets().sendGameMessage("You can't submit ticket when you are muted.");
					return;
				}
				player.stopAll();
				player.getDialogueManager().startDialogue("TicketDialouge");
				//ReportAbuse.report(player);
			}
			else if (componentId == 32) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.setFilterGame(false);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					player.setFilterGame(true);
			} else if (componentId == 0) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.setFriendsChatStatus(0);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					player.setFriendsChatStatus(1);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					player.setFriendsChatStatus(2);
			} else if (componentId == 23) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.setClanStatus(0);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					player.setClanStatus(1);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					player.setClanStatus(2);
			} else if (componentId == 17) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.setAssistStatus(0);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					player.setAssistStatus(1);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					player.setAssistStatus(2);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET) {
					// ASSIST XP Earned/Time
				}
			}
		} else if (interfaceId == 329) {
			if (componentId == 0) {
				player.getPackets().sendOpenURL(Settings.DONATE_LINK);
			} else if (componentId == 1) {
				player.getPackets().sendOpenURL(Settings.EMAIL_LINK);
			} else if (componentId == 8)
				player.getInterfaceManager().removeScreenInterfaceBG();
		} else if (interfaceId == 1163 || interfaceId == 1164 || interfaceId == 1168 || interfaceId == 1170 || interfaceId == 1171 || interfaceId == 1173)
			player.getDominionTower().handleButtons(interfaceId, componentId, slotId, packetId);
		else if (interfaceId == 900)
			PlayerLook.handleMageMakeOverButtons(player, componentId);
		else if (interfaceId == 1028)
			PlayerLook.handleCharacterCustomizingButtons(player, componentId, slotId);
		else if (interfaceId == 1108 || interfaceId == 1109)
			player.getFriendsIgnores().handleFriendChatButtons(interfaceId, componentId, packetId);
		else if (interfaceId == 1330) {
			if (componentId == 12) {
				if (!player.isLegendaryDonator()) {
					player.getPackets().sendGameMessage("You must be a legendary donator in order to access this area.");
					player.getPackets().sendGameMessage("If you would like to subscribe and become a legendary donator, please do the command ::donate to learn how.");
					return;
				}
				Controller c = player.getControlerManager().getControler();
				if (c != null && c instanceof DungeonController) { //becaus dung can tp
					player.getPackets().sendGameMessage("A magical force prevents you from leaving this area.");
					return;
				}
				player.stopAll();
				player.getActionManager().setAction(new HomeTeleport(HomeTeleport.VIP_ZONE));
				player.getPackets().sendGameMessage("Training at vip zone grants a 10% xp bonus.");
			} else if (componentId == 10) {
				if (!player.isDonator()) {
					player.getPackets().sendGameMessage("You must be a donator in order to access this area.");
					player.getPackets().sendGameMessage("If you would like to subscribe and become a donator, please do the command ::donate to learn how.");
					return;
				}
				Controller c = player.getControlerManager().getControler();
				if (c != null && c instanceof DungeonController) { //becaus dung can tp
					player.getPackets().sendGameMessage("A magical force prevents you from leaving this area.");
					return;
				}
				player.stopAll();
				player.getActionManager().setAction(new HomeTeleport(HomeTeleport.DONATOR_ZONE));
				player.getPackets().sendGameMessage("Training at donator zone grants a 3% xp bonus.");
				return;
			} else if (componentId == 8) {
				if (player.getSlayerManager().getCurrentTask() == null) {
					player.getPackets().sendGameMessage("You currently don't have a task.");
					return;
				}
				if (player.getSlayerManager().getCurrentMaster() == SlayerMaster.KRYSTILIA) {
					player.getPackets().sendGameMessage("You are currently doing a wilderness slayer task. ::task won't work therefore..");
					return;
				}
				
				/*if (player.getSlayerManager().getCurrentMaster() == SlayerMaster.DURADEL) {
					if (!player.isSuperDonator()) {
						player.getPackets().sendGameMessage("You do not have the privileges to use this command with this slayer master. Upgrade your rank to emerald donator.");
						return;
					}
				} else if (!player.isExtremeDonator()
						&& ((player.getSlayerManager().getCurrentMaster() != SlayerMaster.TURAEL
						&&  player.getSlayerManager().getCurrentMaster() != SlayerMaster.VANNAKA
						&& player.getSlayerManager().getCurrentMaster() != SlayerMaster.CHAELDAR))) {
					player.getPackets().sendGameMessage("You do not have the privileges to use this command with this slayer master. Upgrade your rank to ruby donator.");
					return;
				}*/
				if (player.getSlayerManager().getCurrentTask().getTile() == null) {
					player.getPackets().sendGameMessage("You can not teleport to this slayer task.");
					return;
				}
				Magic.sendCommandTeleportSpell(player, player.getSlayerManager().getCurrentTask().getTile());
				return;
			} else if (componentId == 6) {
			/*	if (!player.isDiamondDonator()) {
					player.getPackets().sendGameMessage("You must be a diamond donator in order to use this command.");
					player.getPackets().sendGameMessage("If you would like to subscribe and become a diamond donator, please do the command ::donate to learn how.");
					return;
				}
				if (!player.canSpawn()) {
					player.getPackets().sendGameMessage("You can't use this command while in a dangerous area.");
					return;
				}
				int book = player.getCombatDefinitions().getSpellBook();
				player.getCombatDefinitions().setSpellBook(book == 192 ? 1 : book == 193 ? 2 : 0);
				player.getPackets().sendGameMessage("You've toggled your spellbook.");*/
				player.stopAll();
				//EconomyManager.openTPS(player);
				TeleportationInterface.openInterface(player);
			} else if (componentId == 4) {
			/*	if (!player.isDiamondDonator()) {
					player.getPackets().sendGameMessage("You must be a diamond donator in order to use this command.");
					player.getPackets().sendGameMessage("If you would like to subscribe and become a diamond donator, please do the command ::donate to learn how.");
					return;
				}
				if (!player.canSpawn()) {
					player.getPackets().sendGameMessage("You can't use this command while in a dangerous area.");
					return;
				}
				player.getPrayer().setPrayerBook(!player.getPrayer().isAncientCurses());
				player.getPackets().sendGameMessage("You have switched your prayer book.");*/
				Controller c = player.getControlerManager().getControler();
				if (c != null && c instanceof DungeonController) { //becaus dung can tp
					player.getPackets().sendGameMessage("A magical force prevents you from leaving this area.");
					return;
				}
				player.stopAll();
				player.getActionManager().setAction(new HomeTeleport(HomeTeleport.HOME_LODE_STONE));
			}
		} else if (interfaceId == 3207) {
			if (componentId == 21) {
				QuestTab.set(player, 2);
				player.getInterfaceManager().openGameTab(3);
			}
		} else if (interfaceId == 3206) {
				if (componentId == 19) {
					FfaZone.enter(player);
				} else if (componentId == 9) {
					if (!player.isLegendaryDonator()) {
						player.getPackets().sendGameMessage("You must be a legendary donator in order to access this area.");
						player.getPackets().sendGameMessage("If you would like to subscribe and become a legendary donator, please do the command ::donate to learn how.");
						return;
					}
					Controller c = player.getControlerManager().getControler();
					if (c != null && c instanceof DungeonController) { //becaus dung can tp
						player.getPackets().sendGameMessage("A magical force prevents you from leaving this area.");
						return;
					}
					player.stopAll();
					player.getActionManager().setAction(new HomeTeleport(HomeTeleport.VIP_ZONE));
					player.getPackets().sendGameMessage("Training at vip zone grants a 10% xp bonus.");
				} else if (componentId == 7) {
					if (!player.isDonator()) {
						player.getPackets().sendGameMessage("You must be a donator in order to access this area.");
						player.getPackets().sendGameMessage("If you would like to subscribe and become a donator, please do the command ::donate to learn how.");
						return;
					}
					Controller c = player.getControlerManager().getControler();
					if (c != null && c instanceof DungeonController) { //becaus dung can tp
						player.getPackets().sendGameMessage("A magical force prevents you from leaving this area.");
						return;
					}
					player.stopAll();
					player.getActionManager().setAction(new HomeTeleport(HomeTeleport.DONATOR_ZONE));
					player.getPackets().sendGameMessage("Training at donator zone grants a 3% xp bonus.");
					return;
				} else if (componentId == 5) {
					if (player.getSlayerManager().getCurrentTask() == null) {
						player.getPackets().sendGameMessage("You currently don't have a task.");
						return;
					}
					if (player.getSlayerManager().getCurrentMaster() == SlayerMaster.KRYSTILIA) {
						player.getPackets().sendGameMessage("You are currently doing a wilderness slayer task. ::task won't work therefore..");
						return;
					}

				/*if (player.getSlayerManager().getCurrentMaster() == SlayerMaster.DURADEL) {
					if (!player.isSuperDonator()) {
						player.getPackets().sendGameMessage("You do not have the privileges to use this command with this slayer master. Upgrade your rank to emerald donator.");
						return;
					}
				} else if (!player.isExtremeDonator()
						&& ((player.getSlayerManager().getCurrentMaster() != SlayerMaster.TURAEL
						&&  player.getSlayerManager().getCurrentMaster() != SlayerMaster.VANNAKA
						&& player.getSlayerManager().getCurrentMaster() != SlayerMaster.CHAELDAR))) {
					player.getPackets().sendGameMessage("You do not have the privileges to use this command with this slayer master. Upgrade your rank to ruby donator.");
					return;
				}*/
					if (player.getSlayerManager().getCurrentTask().getTile() == null) {
						player.getPackets().sendGameMessage("You can not teleport to this slayer task.");
						return;
					}
					Magic.sendCommandTeleportSpell(player, player.getSlayerManager().getCurrentTask().getTile());
					return;
				} else if (componentId == 3) {
			/*	if (!player.isDiamondDonator()) {
					player.getPackets().sendGameMessage("You must be a diamond donator in order to use this command.");
					player.getPackets().sendGameMessage("If you would like to subscribe and become a diamond donator, please do the command ::donate to learn how.");
					return;
				}
				if (!player.canSpawn()) {
					player.getPackets().sendGameMessage("You can't use this command while in a dangerous area.");
					return;
				}
				int book = player.getCombatDefinitions().getSpellBook();
				player.getCombatDefinitions().setSpellBook(book == 192 ? 1 : book == 193 ? 2 : 0);
				player.getPackets().sendGameMessage("You've toggled your spellbook.");*/
					player.stopAll();
					//EconomyManager.openTPS(player);
					TeleportationInterface.openInterface(player);
				} else if (componentId == 1) {
			/*	if (!player.isDiamondDonator()) {
					player.getPackets().sendGameMessage("You must be a diamond donator in order to use this command.");
					player.getPackets().sendGameMessage("If you would like to subscribe and become a diamond donator, please do the command ::donate to learn how.");
					return;
				}
				if (!player.canSpawn()) {
					player.getPackets().sendGameMessage("You can't use this command while in a dangerous area.");
					return;
				}
				player.getPrayer().setPrayerBook(!player.getPrayer().isAncientCurses());
				player.getPackets().sendGameMessage("You have switched your prayer book.");*/
					Controller c = player.getControlerManager().getControler();
					if (c != null && c instanceof DungeonController) { //becaus dung can tp
						player.getPackets().sendGameMessage("A magical force prevents you from leaving this area.");
						return;
					}
					player.stopAll();
					player.getActionManager().setAction(new HomeTeleport(HomeTeleport.HOME_LODE_STONE));
				}
		} else if (interfaceId == 1089) {
			if (componentId == 30)
				player.getTemporaryAttributtes().put("clanflagselection", slotId);
			else if (componentId == 26) {
				Integer flag = (Integer) player.getTemporaryAttributtes().remove("clanflagselection");
				player.stopAll();
				if (flag != null)
					ClansManager.setClanFlagInterface(player, flag);
			}
		} else if (interfaceId == 1096) {
			if (componentId == 41)
				ClansManager.viewClammateDetails(player, slotId);
			else if (componentId == 94)
				ClansManager.switchGuestsInChatCanEnterInterface(player);
			else if (componentId == 95)
				ClansManager.switchGuestsInChatCanTalkInterface(player);
			else if (componentId == 96)
				ClansManager.switchRecruitingInterface(player);
			else if (componentId == 97)
				ClansManager.switchClanTimeInterface(player);
			else if (componentId == 124)
				ClansManager.openClanMottifInterface(player);
			else if (componentId == 131)
				ClansManager.openClanMottoInterface(player);
			else if (componentId == 240)
				ClansManager.setTimeZoneInterface(player, -720 + slotId * 10);
			else if (componentId == 262)
				player.getTemporaryAttributtes().put("editclanmatejob", slotId);
			else if (componentId == 276)
				player.getTemporaryAttributtes().put("editclanmaterank", slotId);
			else if (componentId == 309)
				ClansManager.kickClanmate(player);
			else if (componentId == 318)
				ClansManager.saveClanmateDetails(player);
			else if (componentId == 290)
				ClansManager.setWorldIdInterface(player, slotId);
			else if (componentId == 297)
				ClansManager.openForumThreadInterface(player);
			else if (componentId == 346)
				ClansManager.openNationalFlagInterface(player);
			else if (componentId == 113)
				ClansManager.showClanSettingsClanMates(player);
			else if (componentId == 120)
				ClansManager.showClanSettingsSettings(player);
			else if (componentId == 386)
				ClansManager.showClanSettingsPermissions(player);
			else if (componentId >= 395 && componentId <= 475) {
				int selectedRank = (componentId - 395) / 8;
				if (selectedRank == 10)
					selectedRank = 125;
				else if (selectedRank > 5)
					selectedRank = 100 + selectedRank - 6;
				ClansManager.selectPermissionRank(player, selectedRank);
			} else if (componentId == 489)
				ClansManager.selectPermissionTab(player, 1);
			else if (componentId == 498)
				ClansManager.selectPermissionTab(player, 2);
			else if (componentId == 506)
				ClansManager.selectPermissionTab(player, 3);
			else if (componentId == 514)
				ClansManager.selectPermissionTab(player, 4);
			else if (componentId == 522)
				ClansManager.selectPermissionTab(player, 5);
		} else if (interfaceId == 1105) {
			if (componentId == 63 || componentId == 66)
				ClansManager.setClanMottifTextureInterface(player, componentId == 66, slotId);
			else if (componentId == 35)
				ClansManager.openSetMottifColor(player, 0);
			else if (componentId == 80)
				ClansManager.openSetMottifColor(player, 1);
			else if (componentId == 92)
				ClansManager.openSetMottifColor(player, 2);
			else if (componentId == 104)
				ClansManager.openSetMottifColor(player, 3);
			else if (componentId == 120)
				player.stopAll();
		} else if (interfaceId == 1110) {
			if (componentId == 82)
				ClansManager.joinClanChatChannel(player);
			else if (componentId == 75)
				ClansManager.openClanDetails(player);
			else if (componentId == 78)
				ClansManager.openClanSettings(player);
			else if (componentId == 91)
				ClansManager.joinGuestClanChat(player);
			else if (componentId == 95)
				ClansManager.banPlayer(player);
			else if (componentId == 99)
				ClansManager.unbanPlayer(player);
			else if (componentId == 11)
				ClansManager.unbanPlayer(player, slotId);
			else if (componentId == 109)
				ClansManager.leaveClan(player);
		} else if (interfaceId == 1079)
			player.closeInterfaces();
		else if (interfaceId == 374) {
			if (componentId >= 5 && componentId <= 9) {
				if (player.getActionManager().getAction() instanceof ViewingOrb)
					player.setNextWorldTile(new WorldTile(((ViewingOrb) player.getActionManager().getAction()).getTps()[componentId - 5]));
			} else if (componentId == 15)
				player.stopAll();
		} else if (interfaceId == 105 || interfaceId == 107 || interfaceId == 109 || interfaceId == 449)
			player.getGeManager().handleButtons(interfaceId, componentId, slotId, packetId);
		else if (interfaceId == 1092) {
			HomeTeleport.useLodestone(player, componentId);
		} else if (interfaceId == 1214)
			player.getSkills().handleSetupXPCounter(componentId);
		else if (interfaceId == 1292) {
			if (componentId == 12)
				Crucible.enterArena(player);
			else if (componentId == 13)
				player.closeInterfaces();
		} else if (interfaceId == 1284) {
			if (player.getTemporaryAttributtes().get("runepouch") != null) {
				Item item = player.getRunePouch().get(slotId);
				if (item == null && componentId == 7)
					return;
				switch (componentId) {
				case 7:
					if (player.getInventory().getFreeSlots() == 0
							&& !player.getInventory().containsItem(player.getRunePouch().get(slotId).getId(), 1)) {
						player.getPackets().sendGameMessage("You don't have enough inventory space.");
						return;
					}
					switch (packetId) {
					case 55:
						RunePouch.withdrawRunePouch(player, slotId, item, 16000);
						break;
					case 5:
						RunePouch.withdrawRunePouch(player, slotId, item, 100);
						break;
					case 67:
						RunePouch.withdrawRunePouch(player, slotId, item, 10);
						break;
					case 14:
						RunePouch.withdrawRunePouch(player, slotId, item, 1);
						break;
					}
					break;
				case 10:
					switch (packetId) {
					case 14:
						for (Item items : player.getRunePouch().getItems()) {
							if (items == null)
								continue;
							if (!player.getInventory().hasFreeSlots()
									&& !player.getInventory().containsItem(items.getId(), 1)) {
								player.getPackets()
										.sendGameMessage("You don't have enough inventory space to withdraw the "
												+ items.getName() + "s.");
								continue;
							}
							player.getInventory().addItem(items);
							player.getRunePouch().remove(items);
							player.getRunePouch().shift();
							player.getPackets().sendGameMessage(
									"You withdraw " + items.getAmount() + " x " + items.getName() + "s.");
						}
						RunePouch.refreshRunePouch(player);
						break;
					}
					break;
				}
			}
		}
		if (Settings.DEBUG)
			Logger.log("ButtonHandler", "InterfaceId " + interfaceId + ", componentId " + componentId + ", slotId " + slotId + ", slotId2 " + slotId2 + ", PacketId: " + packetId);
	}

	public static boolean sendRemove(Player player, int slotId) {
		if (slotId >= 15)
			return false;
		player.stopAll(false, false);
		Item item = player.getEquipment().getItem(slotId);
		if (item == null)
			return true;
		else if (!player.getControlerManager().canRemoveEquip(slotId, item.getId()))
			return false;
		else if (!player.getInventory().addItem(item.getId(), item.getAmount()))
			return false;
		player.getEquipment().getItems().set(slotId, null);
		player.getEquipment().refresh(slotId);
		player.getAppearence().generateAppearenceData();
		if (Runecrafting.isTiara(item.getId()))
			player.getVarsManager().sendVar(491, 0);
		if (slotId == Equipment.SLOT_WEAPON) {
			player.getCombatDefinitions().desecreaseSpecialAttack(0);
			player.sendWeaponStance();
		} else if (slotId == Equipment.SLOT_AURA)
			player.getAuraManager().removeAura();
		player.removeWeaponAttackOption(item.getId());
		return true;
	}

	/*public static boolean sendWear(Player player, int slotId, int itemId) {
		player.stopAll(false, false);
		Item item = player.getInventory().getItem(slotId);
		if (item == null || item.getId() != itemId)
			return false;
		if (item.getDefinitions().isNoted() || !item.getDefinitions().isWearItem(player.getAppearence().isMale())) {
			player.getPackets().sendGameMessage("You can't wear that.");
			return true;
		}
		int targetSlot = Equipment.getItemSlot(itemId);
		if (PlayerCombat.hasBalista(player) && PlayerCombat.isJavelin(itemId) && targetSlot == Equipment.SLOT_WEAPON)
			targetSlot = Equipment.SLOT_ARROWS;
		if (targetSlot == -1) {
			player.getPackets().sendGameMessage("You can't wear that.");
			return true;
		}
		if (!ItemConstants.canWear(item, player))
			return true;
		boolean isTwoHandedWeapon = targetSlot == 3 && Equipment.isTwoHandedWeapon(item);
		if (isTwoHandedWeapon && !player.getInventory().hasFreeSlots() && player.getEquipment().hasShield()) {
			player.getPackets().sendGameMessage("Not enough free space in your inventory.");
			return true;
		}
		HashMap<Integer, Integer> requiriments = item.getDefinitions().getWearingSkillRequiriments();
		boolean hasRequiriments = true;
		if (requiriments != null) {
			for (int skillId : requiriments.keySet()) {
				if (skillId > 24 || skillId < 0)
					continue;
				int level = requiriments.get(skillId);
				if (level < 0 || level > 120)
					continue;
				if (player.getSkills().getLevelForXp(skillId, 120) < level) {
					if (hasRequiriments) {
						player.getPackets().sendGameMessage("You are not high enough level to use this item.");
					}
					hasRequiriments = false;
					String name = Skills.SKILL_NAME[skillId].toLowerCase();
					player.getPackets().sendGameMessage("You need to have a" + (name.startsWith("a") ? "n" : "") + " " + name + " level of " + level + ".");
				}

			}
		}
		if (!hasRequiriments)
			return true;
		if (!player.getControlerManager().canEquip(targetSlot, itemId))
			return false;
		player.stopAll(false, false);
		player.getInventory().deleteItem(slotId, item);
		if (targetSlot == 3) {
			Item oldWeapon = player.getEquipment().getItem(3);
			if (oldWeapon != null)
				player.removeWeaponAttackOption(oldWeapon.getId());
			if (isTwoHandedWeapon && player.getEquipment().getItem(5) != null) {
				if (!player.getInventory().addItem(player.getEquipment().getItem(5).getId(), player.getEquipment().getItem(5).getAmount())) {
					player.getInventory().getItems().set(slotId, item);
					player.getInventory().refresh(slotId);
					return true;
				}
				player.getEquipment().getItems().set(5, null);
			}
		} else if (targetSlot == 5) {
			if (player.getEquipment().getItem(3) != null && Equipment.isTwoHandedWeapon(player.getEquipment().getItem(3))) {
				if (!player.getInventory().addItem(player.getEquipment().getItem(3).getId(), player.getEquipment().getItem(3).getAmount())) {
					player.getInventory().getItems().set(slotId, item);
					player.getInventory().refresh(slotId);
					return true;
				}
				player.getEquipment().getItems().set(3, null);
			}

		}
		if (player.getEquipment().getItem(targetSlot) != null && (itemId != player.getEquipment().getItem(targetSlot).getId() || !item.getDefinitions().isStackable())) {
			if (player.getInventory().getItems().get(slotId) == null) {
				player.getInventory().getItems().set(slotId, new Item(player.getEquipment().getItem(targetSlot).getId(), player.getEquipment().getItem(targetSlot).getAmount()));
				player.getInventory().refresh(slotId);
			} else
				player.getInventory().addItem(new Item(player.getEquipment().getItem(targetSlot).getId(), player.getEquipment().getItem(targetSlot).getAmount()));
			player.getEquipment().getItems().set(targetSlot, null);
		}
		if (targetSlot == Equipment.SLOT_AURA)
			player.getAuraManager().removeAura();
		int oldAmt = 0;
		if (player.getEquipment().getItem(targetSlot) != null) {
			oldAmt = player.getEquipment().getItem(targetSlot).getAmount();
		}
		Item item2 = new Item(itemId, oldAmt + item.getAmount());
		player.getEquipment().getItems().set(targetSlot, item2);
		player.getEquipment().refresh(targetSlot, targetSlot == 3 ? 5 : targetSlot == 3 ? 0 : 3);
		player.getAppearence().generateAppearenceData();
		player.getPackets().sendSound(2240, 0, 1);
		if (targetSlot == 3) {
			player.getCombatDefinitions().desecreaseSpecialAttack(0);
			player.sendWeaponStance();
		}
		player.getCharges().wear(targetSlot);
		player.setWeaponAttackOption(itemId);
		if (itemId == 52111)
			player.getPrayer().drainPrayerOnHalf();
		return true;
	}*/

	public static boolean sendWear2(Player player, int slotId, int itemId) {
		if (player.hasFinished() || player.isDead())
			return false;
		player.stopAll(false, false);
		Item item = player.getInventory().getItem(slotId);
		if (item == null || item.getId() != itemId)
			return false;
		if (item.getDefinitions().isNoted() || !item.getDefinitions().isWearItem(player.getAppearence().isMale()) && itemId != 4084 && itemId != 25490) {
			player.getPackets().sendGameMessage("You can't wear that.");
			return false;
		}
		int targetSlot = Equipment.getItemSlot(itemId);
		if (PlayerCombat.hasBalista(player) && PlayerCombat.isJavelin(itemId) && targetSlot == Equipment.SLOT_WEAPON)
			targetSlot = Equipment.SLOT_ARROWS;
		if (itemId == 4084 || itemId == 25490)
			targetSlot = 3;
		if (targetSlot == -1) {
			player.getPackets().sendGameMessage("You can't wear that.");
			return false;
		}
		if (!ItemConstants.canWear(item, player))
			return false;
		itemId = item.getId();//id may switch if converted
		boolean isTwoHandedWeapon = targetSlot == 3 && Equipment.isTwoHandedWeapon(item);
		if (isTwoHandedWeapon && !player.getInventory().hasFreeSlots() && player.getEquipment().hasShield()) {
			player.getPackets().sendGameMessage("Not enough free space in your inventory.");
			return false;
		}
		HashMap<Integer, Integer> requiriments = item.getDefinitions().getWearingSkillRequiriments();
		boolean hasRequiriments = true;
		if (requiriments != null) {
			for (int skillId : requiriments.keySet()) {
				if (skillId > 24 || skillId < 0)
					continue;
				int level = requiriments.get(skillId);
				if (level < 0 || level > 120)
					continue;
				if (!player.tournamentResetRequired() && player.getSkills().getLevelForXp(skillId, 120) < level) {
					if (hasRequiriments)
						player.getPackets().sendGameMessage("You are not high enough level to use this item.");
					hasRequiriments = false;
					String name = Skills.SKILL_NAME[skillId].toLowerCase();
					player.getPackets().sendGameMessage("You need to have a" + (name.startsWith("a") ? "n" : "") + " " + name + " level of " + level + ".");
				}

			}
		}
		if (!hasRequiriments)
			return false;
		if (!player.getControlerManager().canEquip(targetSlot, itemId))
			return false;
		player.getInventory().getItems().remove(slotId, item);
		if (targetSlot == 3) {
			Item oldWeapon = player.getEquipment().getItem(3);
			if (oldWeapon != null)
				player.removeWeaponAttackOption(oldWeapon.getId());
			if (isTwoHandedWeapon && player.getEquipment().getItem(5) != null) {
				if (!player.getInventory().getItems().add(player.getEquipment().getItem(5))) {
					player.getInventory().getItems().set(slotId, item);
					return false;
				}
				player.getEquipment().getItems().set(5, null);
			}
		} else if (targetSlot == 5) {
			if (player.getEquipment().getItem(3) != null && Equipment.isTwoHandedWeapon(player.getEquipment().getItem(3))) {
				if (!player.getInventory().getItems().add(player.getEquipment().getItem(3))) {
					player.getInventory().getItems().set(slotId, item);
					return false;
				}
				player.getEquipment().getItems().set(3, null);
			}

		}
		if (player.getEquipment().getItem(targetSlot) != null && (itemId != player.getEquipment().getItem(targetSlot).getId() || !item.getDefinitions().isStackable())) {
			if (player.getInventory().getItems().get(slotId) == null) {
				player.getInventory().getItems().set(slotId, new Item(player.getEquipment().getItem(targetSlot).getId(), player.getEquipment().getItem(targetSlot).getAmount()));
			} else
				player.getInventory().getItems().add(new Item(player.getEquipment().getItem(targetSlot).getId(), player.getEquipment().getItem(targetSlot).getAmount()));
			player.getEquipment().getItems().set(targetSlot, null);
		}
		if (targetSlot == Equipment.SLOT_AURA)
			player.getAuraManager().removeAura();
		int oldAmt = 0;
		if (player.getEquipment().getItem(targetSlot) != null) {
			oldAmt = player.getEquipment().getItem(targetSlot).getAmount();
		}
		Item item2 = new Item(itemId, oldAmt + item.getAmount());
		player.getEquipment().getItems().set(targetSlot, item2);
		player.getEquipment().refresh(targetSlot, targetSlot == 3 ? 5 : targetSlot == 3 ? 0 : 3);
		if (targetSlot == 3) {
			player.getCombatDefinitions().desecreaseSpecialAttack(0);
			player.sendWeaponStance();
			if (Combat.hasCustomWeaponOnWild(player)) {
				player.getPackets().sendGameMessage("<col=ff0000>Warning: You are wearing a custom weapon.");
				player.getPackets().sendGameMessage("<col=ff0000>Warning: Your pvp damage will be reduced by 50% while doing so.");
			}
		}
		player.getCharges().wear(targetSlot);
		player.setWeaponAttackOption(itemId);
	/*	if (itemId == 52111)
			player.getPrayer().drainPrayerOnHalf();*/
		if (itemId == 1409)
			player.getPackets().sendGameMessage("You equip the staff of Iban.", true);
		if (itemId == 52557)
			player.setWildernessSkull();
		return true;
	}

	public static void submitSpecialRequest(final Player player) {
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				try {
					if (player.isDead())
						return;
					if(player.getControlerManager().getControler() != null && player.getControlerManager().getControler() instanceof DuelArena && player.getDuelRules().getRule(9)) {
						player.sendMessage("Special attacks are disabled for this duel.");
						return;
					}
					int weaponId = player.getEquipment().getWeaponId();
					if (player.hasInstantSpecial(weaponId)) {
						player.performInstantSpecial(weaponId);
						return;
					}
					player.getCombatDefinitions().switchUsingSpecialAttack();
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		});
	}

	public static void sendWear(Player player, int[] slotIds) {
		if (player.hasFinished() || player.isDead())
			return;
		boolean worn = false;
		Item[] copy = player.getInventory().getItems().getItemsCopy();
		for (int slotId : slotIds) {
			Item item = player.getInventory().getItem(slotId);
			if (item == null)
				continue;
			if (sendWear2(player, slotId, item.getId()))
				worn = true;
		}
		player.getInventory().refreshItems(copy);
		if (worn) {
			player.getAppearence().generateAppearenceData();
			player.getPackets().sendSound(2240, 0, 1);
		}
	}

	public static void openItemsKeptOnDeath(Player player) {
		player.getInterfaceManager().sendInterface(17);
		sendItemsKeptOnDeath(player, false);
	}

	public static void sendItemsKeptOnDeath(Player player, boolean wilderness) {
		boolean skulled = player.hasSkull();
		Integer[][] slots = GraveStone.getItemSlotsKeptOnDeath(player, wilderness, skulled, player.getPrayer().isProtectingItem());
		Item[][] items = GraveStone.getItemsKeptOnDeath(player, slots);
		long riskedWealth = 0;
		long carriedWealth = 0;
		for (Item item : items[1])
			carriedWealth = riskedWealth += GrandExchange.getPrice(item.getId()) * item.getAmount();
		for (Item item : items[0])
			carriedWealth += GrandExchange.getPrice(item.getId()) * item.getAmount();
		if (slots[0].length > 0) {
			for (int i = 0; i < slots[0].length; i++)
				player.getVarsManager().sendVarBit(9222 + i, slots[0][i]);
			player.getVarsManager().sendVarBit(9227, slots[0].length);
		} else {
			player.getVarsManager().sendVarBit(9222, -1);
			player.getVarsManager().sendVarBit(9227, 1);
		}
		player.getVarsManager().sendVarBit(9226, wilderness ? 1 : 0);
		player.getVarsManager().sendVarBit(9229, skulled ? 1 : 0);
		StringBuffer text = new StringBuffer();
		text.append("The number of items kept on").append("<br>").append("death is normally 3.").append("<br>").append("<br>").append("<br>");
		if (wilderness) {
			text.append("Your gravestone will not").append("<br>").append("appear.");
		} else {
			int time = GraveStone.getMaximumTicks(player.getGraveStone());
			int seconds = (int) (time * 0.6);
			int minutes = seconds / 60;
			seconds -= minutes * 60;

			text.append("Gravestone:").append("<br>").append(ClientScriptMap.getMap(1099).getStringValue(player.getGraveStone())).append("<br>").append("<br>").append("Initial duration:").append("<br>").append(minutes + ":" + (seconds < 10 ? "0" : "") + seconds).append("<br>");
		}
		text.append("<br>").append("<br>").append("Carried wealth:").append("<br>").append(carriedWealth > Integer.MAX_VALUE ? "Too high!" : Utils.getFormattedNumber((int) carriedWealth)).append("<br>").append("<br>").append("Risked wealth:").append("<br>").append(riskedWealth > Integer.MAX_VALUE ? "Too high!" : Utils.getFormattedNumber((int) riskedWealth)).append("<br>").append("<br>");
		if (wilderness) {
			text.append("Your hub will be set to:").append("<br>").append("Edgeville.");
		} else {
			text.append("Current hub: " + ClientScriptMap.getMap(3792).getStringValue(DeathEvent.getCurrentHub(player)));
		}
		player.getPackets().sendCSVarString(352, text.toString());
	}

	public static void openEquipmentBonuses(final Player player, boolean banking) {
		player.stopAll();
		player.getInterfaceManager().sendInventoryInterface(670);
		player.getInterfaceManager().sendInterface(667);
		player.getPackets().sendHideIComponent(667, 1, false);
		player.getVarsManager().sendVarBit(4894, banking ? 1 : 0);
		player.getVarsManager().sendVarBit(8348, 1);
		player.getPackets().sendExecuteScript(787, 1);
		player.getPackets().sendItems(93, player.getInventory().getItems());
		player.getPackets().sendInterSetItemsOptionsScript(670, 0, 93, 4, 7, "Equip", "Compare", "Stats", "Examine");
		player.getPackets().sendUnlockIComponentOptionSlots(670, 0, 0, 27, 0, 1, 2, 3);
		player.getPackets().sendUnlockIComponentOptionSlots(667, 9, 0, 14, 0, 8, 9);
		refreshEquipBonuses(player);
		if (banking) {
			player.getTemporaryAttributtes().put("Banking", Boolean.TRUE);
			player.setCloseInterfacesEvent(new Runnable() {
				@Override
				public void run() {
					player.getTemporaryAttributtes().remove("Banking");
					player.getVarsManager().sendVarBit(4894, 0);
				}
			});
		}
	}

	private static final String[] BONUS_NAMES =
		{ "Stab", "Slash", "Crush", "Magic", "Ranged", "Stab", "Slash", "Crush", "Magic", "Ranged", "Summoning", "Absorb melee", "Absorb magic", "Absorb ranged", "Melee strength", "Ranged strength", "Prayer","Magic damage" };

	public static void refreshEquipBonuses(Player player) {
		for (int bonus = 0; bonus < BONUS_NAMES.length; bonus++) {
			player.getPackets().sendIComponentText(667, 28 + bonus, getBonus(player, bonus == CombatDefinitions.PRAYER_BONUS ? CombatDefinitions.MAGIC_DAMAGE : bonus == CombatDefinitions.MAGIC_DAMAGE ? CombatDefinitions.PRAYER_BONUS : bonus));
		}
		player.getPackets().sendWeight();
		double effect = PlayerCombat.getUltimateMeleeEffect(player);
		if (effect > 1) 
			player.getPackets().sendGameMessage("Your armour is boosting your melee damage by "+new DecimalFormat("0.##").format(((effect-1d)*100d))+"%!", true);
		effect = PlayerCombat.getUltimateRangeEffect(player);
		if (effect > 1) 
			player.getPackets().sendGameMessage("Your armour is boosting your ranged damage by "+new DecimalFormat("0.##").format(((effect-1d)*100d))+"%!", true);
		effect = PlayerCombat.getUltimateMageEffect(player);
		if (effect > 1) 
			player.getPackets().sendGameMessage("Your armour is boosting your magic damage by "+new DecimalFormat("0.##").format(((effect-1d)*100d))+"%!", true);
		
	}
	
	
	public static void sendCompare(Player player, Item item) {
		if (!item.getDefinitions().isWearItem()) {
			player.getPackets().sendGameMessage("This item can't be worn.");
			return;
		}
		Item equipped = player.getEquipment().getItem(item.getDefinitions().getEquipSlot());
		if (equipped == null) {
			player.getPackets().sendGameMessage("You aren't wearing anything to compare with.");
			return;
		}
		sendStats(player, equipped);
	}
	
	public static void sendStats(Player player, Item item) {
		ItemConfig defs = item.getDefinitions();
		if (!defs.isWearItem()) {
			player.getPackets().sendGameMessage("This item can't be worn.");
			return;
		}
		player.getPackets().sendHideIComponent(667, 49, false);
		player.getPackets().sendIComponentText(667, 61, "<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><col=FFFFFF>"+defs.getName()+"<br>Attack bonuses<br>Stab:<col=ffff00> +" + defs.getStabAttack() + "<br>Slash: <col=ffff00>+" + defs.getSlashAttack() + "<br>Crush:<col=ffff00>+" + defs.getCrushAttack() + "<br>Magic:<col=ffff00>+" + defs.getMagicAttack() + "<br>Ranged:<col=ffff00>+" + defs.getRangeAttack() +
				
				"<br>Defence bonuses<br>Stab:<col=ffff00> +" + defs.getStabDef() + "<br>Slash: <col=ffff00>+" + defs.getSlashDef() + "<br>Crush:<col=ffff00>+" + defs.getCrushDef() + "<br>Magic:<col=ffff00>+" + defs.getMagicDef() + "<br>Ranged:<col=ffff00>+" + defs.getRangeDef() +
				"<br>Other Bonuses" 
		+ "<br>Melee Strength:<col=ffff00>+" +defs.getStrengthBonus()
		+ "<br>Ranged Strength:<col=ffff00>+" +defs.getRangedStrBonus()
		+ "<br>Magic Damage:<col=ffff00>+" +defs.getMagicDamage()+"%"
		+ "<br>Prayer:<col=ffff00>+" +defs.getPrayerBonus());
	}

	private static String getBonus(Player player, int bonus) {
		if (bonus == CombatDefinitions.MAGIC_DAMAGE) {
			double value = player.getCombatDefinitions().getBonuses()[bonus];
			return (BONUS_NAMES[bonus] + " :" + (value >= 0 ? "+" : "-") + "" + value +"%").replace(".0%", "%");
		} else {
			int value = (int)player.getCombatDefinitions().getBonuses()[bonus];
			return BONUS_NAMES[bonus] + " :" + (value >= 0 ? "+" : "-") + "" + value;
		}
	}

	public static void openSkillGuide(final Player player) {
		player.getInterfaceManager().setScreenInterface(317, 1218);
		player.getInterfaceManager().setInterface(false, 1218, 1, 1217); // seems to fix
		player.setCantWalk(true);
		player.setCloseInterfacesEvent(new Runnable() {

			@Override
			public void run() {
				player.getInterfaceManager().removeInterface(317);
				player.getInterfaceManager().removeInterface(1218);
				player.setCantWalk(false);
			}
		});
	}
}
