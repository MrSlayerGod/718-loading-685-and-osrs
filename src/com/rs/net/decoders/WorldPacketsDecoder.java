package com.rs.net.decoders;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.cache.loaders.QuickChatOptionDefinition;
import com.rs.discord.Bot;
import com.rs.game.*;
import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.item.FloorItem;
import com.rs.game.item.Item;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.minigames.Sawmill;
import com.rs.game.minigames.Sawmill.Plank;
import com.rs.game.minigames.clanwars.ClanWars;
import com.rs.game.minigames.clanwars.FfaZone;
import com.rs.game.minigames.duel.DuelArena;
import com.rs.game.minigames.duel.DuelControler;
import com.rs.game.minigames.stealingcreation.StealingCreationController;
import com.rs.game.minigames.stealingcreation.StealingCreationLobbyController;
import com.rs.game.npc.Drops;
import com.rs.game.npc.NPC;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.familiar.Familiar.SpecialAttack;
import com.rs.game.npc.others.DoorSupport;
import com.rs.game.npc.others.GraveStone;
import com.rs.game.player.*;
import com.rs.game.player.FarmingManager.FarmingSpot;
import com.rs.game.player.FarmingManager.SpotInfo;
import com.rs.game.player.actions.PlayerCombat;
import com.rs.game.player.actions.PlayerFollow;
import com.rs.game.player.actions.Smithing;
import com.rs.game.player.actions.firemaking.Firemaking;
import com.rs.game.player.actions.firemaking.Firemaking.Fire;
import com.rs.game.player.content.*;
import com.rs.game.player.content.Slayer.SlayerMaster;
import com.rs.game.player.content.TicketSystem.TicketEntry;
import com.rs.game.player.content.clans.ClansManager;
import com.rs.game.player.content.commands.Commands;
import com.rs.game.player.content.construction.House;
import com.rs.game.player.content.construction.TabletMaking;
import com.rs.game.player.content.dungeoneering.DungeonRewardShop;
import com.rs.game.player.content.pet.Pets;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.teleportation.TeleportationInterface;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.controllers.DungeonController;
import com.rs.game.player.controllers.SawmillController;
import com.rs.game.player.controllers.partyroom.PartyRoom;
import com.rs.game.player.dialogues.impl.EnchantingOrbsDialogue;
import com.rs.game.player.dialogues.impl.PetShopOwner;
import com.rs.game.player.dialogues.impl.SkillAlchemist;
import com.rs.game.player.dialogues.impl.dungeoneering.CustomSeedD;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.FixedTileStrategy;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.io.InputStream;
import com.rs.net.LoginClientChannelManager;
import com.rs.net.LoginProtocol;
import com.rs.net.Session;
import com.rs.net.decoders.handlers.ButtonHandler;
import com.rs.net.decoders.handlers.InventoryOptionsHandler;
import com.rs.net.decoders.handlers.NPCHandler;
import com.rs.net.decoders.handlers.ObjectHandler;
import com.rs.net.encoders.LoginChannelsPacketEncoder;
import com.rs.utils.*;
import com.rs.utils.huffman.Huffman;

public final class WorldPacketsDecoder extends Decoder {

	private static final byte[] PACKET_SIZES = new byte[104];

	private final static int WALKING_PACKET = 8;
	private final static int MINI_WALKING_PACKET = 58;
	private final static int AFK_PACKET = 16;
	public final static int ACTION_BUTTON1_PACKET = 14;
	public final static int ACTION_BUTTON2_PACKET = 67;
	public final static int ACTION_BUTTON3_PACKET = 5;
	public final static int ACTION_BUTTON4_PACKET = 55;
	public final static int ACTION_BUTTON5_PACKET = 68;
	public final static int ACTION_BUTTON6_PACKET = 90;
	public final static int ACTION_BUTTON7_PACKET = 6;
	public final static int ACTION_BUTTON8_PACKET = 32;
	public final static int ACTION_BUTTON9_PACKET = 27;
	public final static int WORLD_MAP_CLICK = 38;
	public final static int ACTION_BUTTON10_PACKET = 96;
	public final static int RECEIVE_PACKET_COUNT_PACKET = 33;
	private final static int PLAYER_OPTION_3_PACKET = 77;
	private final static int PLAYER_OPTION_4_PACKET = 17;
	private final static int PLAYER_OPTION_6_PACKET = 49;
	private final static int PLAYER_OPTION_7_PACKET = 51;
	private final static int MOVE_CAMERA_PACKET = 103;
	private final static int INTERFACE_ON_OBJECT = 37;
	private final static int CLICK_PACKET = -1;
	private final static int MOUVE_MOUSE_PACKET = -1;
	private final static int KEY_TYPED_PACKET = 1;
	private final static int CLOSE_INTERFACE_PACKET = 54;
	private final static int COMMANDS_PACKET = 60;
	private final static int INTERFACE_ON_INTERFACE = 3;
	private final static int IN_OUT_SCREEN_PACKET = -1;
	private final static int DONE_LOADING_REGION_PACKET = 30;
	private final static int PING_PACKET = 21;
	private final static int SCREEN_PACKET = 98;
	private final static int CHAT_TYPE_PACKET = 83;
	private final static int CHAT_PACKET = 53;
	private final static int PUBLIC_QUICK_CHAT_PACKET = 86;
	private final static int ADD_FRIEND_PACKET = 89;
	private final static int ADD_IGNORE_PACKET = 4;
	private final static int REMOVE_IGNORE_PACKET = 73;
	private final static int JOIN_FRIEND_CHAT_PACKET = 36;
	private final static int CHANGE_FRIEND_CHAT_PACKET = 22;
	private final static int KICK_FRIEND_CHAT_PACKET = 74;
	private final static int KICK_CLAN_CHAT_PACKET = 92;
	private final static int REMOVE_FRIEND_PACKET = 24;
	private final static int SEND_FRIEND_MESSAGE_PACKET = 82;
	private final static int SEND_FRIEND_QUICK_CHAT_PACKET = 0;
	private final static int OBJECT_CLICK1_PACKET = 26;
	private final static int OBJECT_CLICK2_PACKET = 59;
	private final static int OBJECT_CLICK3_PACKET = 40;
	private final static int OBJECT_CLICK4_PACKET = 23;
	private final static int OBJECT_CLICK5_PACKET = 80;
	private final static int OBJECT_EXAMINE_PACKET = 25;
	private final static int NPC_CLICK1_PACKET = 31;
	private final static int NPC_CLICK2_PACKET = 101;
	private final static int NPC_CLICK3_PACKET = 34;
	private final static int NPC_CLICK4_PACKET = 65;
	public final static int ATTACK_NPC = 20;
	private final static int PLAYER_OPTION_1_PACKET = 42;
	private final static int PLAYER_OPTION_2_PACKET = 46;
	private final static int PLAYER_OPTION_5_PACKET = 88;
	private final static int PLAYER_OPTION_9_PACKET = 56;
	private final static int ITEM_GROUND_OPTION_3 = 57;
	private final static int ITEM_GROUND_OPTION_4 = 62;
	private final static int GROUND_ITEM_OPTION_EXAMINE = 102;
	private final static int DIALOGUE_CONTINUE_PACKET = 72;
	private final static int ENTER_INTEGER_PACKET = 81;
	private final static int ENTER_NAME_PACKET = 29;
	private final static int ENTER_LONG_TEXT_PACKET = 48;
	private final static int SWITCH_INTERFACE_COMPONENTS_PACKET = 76;
	private final static int INTERFACE_ON_PLAYER = 50;
	private final static int INTERFACE_ON_NPC = 66;
	private final static int COLOR_ID_PACKET = 97;
	private static final int NPC_EXAMINE_PACKET = 9;
	private static final int FORUM_THREAD_ID_PACKET = 18;
	private final static int OPEN_URL_PACKET = 91;
	private final static int REPORT_ABUSE_PACKET = 11;
	private final static int GRAND_EXCHANGE_ITEM_SELECT_PACKET = 71;
	private final static int WORLD_LIST_UPDATE = 87;
	private final static int UPDATE_GAMEBAR_PACKET = 79;
	private final static int MUSIC_PACKET = 39;

	static {
		loadPacketSizes();
	}

	public static void loadPacketSizes() {
		PACKET_SIZES[0] = -1;
		PACKET_SIZES[1] = -2;
		PACKET_SIZES[2] = -1;
		PACKET_SIZES[3] = 16;
		PACKET_SIZES[4] = -1;
		PACKET_SIZES[5] = 8;
		PACKET_SIZES[6] = 8;
		PACKET_SIZES[7] = 3;
		PACKET_SIZES[8] = -1;
		PACKET_SIZES[9] = 3;
		PACKET_SIZES[10] = -1;
		PACKET_SIZES[11] = -1;
		PACKET_SIZES[12] = -1;
		PACKET_SIZES[13] = 7;
		PACKET_SIZES[14] = 8;
		PACKET_SIZES[15] = 6;
		PACKET_SIZES[16] = 2;
		PACKET_SIZES[17] = 3;
		PACKET_SIZES[18] = -1;
		PACKET_SIZES[19] = -2;
		PACKET_SIZES[20] = 3;
		PACKET_SIZES[21] = 0;
		PACKET_SIZES[22] = -1;
		PACKET_SIZES[23] = 9;
		PACKET_SIZES[24] = -1;
		PACKET_SIZES[25] = 9;
		PACKET_SIZES[26] = 9;
		PACKET_SIZES[27] = 8;
		PACKET_SIZES[28] = 4;
		PACKET_SIZES[29] = -1;
		PACKET_SIZES[30] = 0;
		PACKET_SIZES[31] = 3;
		PACKET_SIZES[32] = 8;
		PACKET_SIZES[33] = 4;
		PACKET_SIZES[34] = 3;
		PACKET_SIZES[35] = -1;
		PACKET_SIZES[36] = -1;
		PACKET_SIZES[37] = 17;
		PACKET_SIZES[38] = 4;
		PACKET_SIZES[39] = 4;
		PACKET_SIZES[40] = 9;
		PACKET_SIZES[41] = -1;
		PACKET_SIZES[42] = 3;
		PACKET_SIZES[43] = 7;
		PACKET_SIZES[44] = -2;
		PACKET_SIZES[45] = 7;
		PACKET_SIZES[46] = 3;
		PACKET_SIZES[47] = 4;
		PACKET_SIZES[48] = -1;
		PACKET_SIZES[49] = 3;
		PACKET_SIZES[50] = 11;
		PACKET_SIZES[51] = 3;
		PACKET_SIZES[52] = -1;
		PACKET_SIZES[53] = -1;
		PACKET_SIZES[54] = 0;
		PACKET_SIZES[55] = 8;
		PACKET_SIZES[56] = 3;
		PACKET_SIZES[57] = 7;
		PACKET_SIZES[58] = -1;
		PACKET_SIZES[59] = 9;
		PACKET_SIZES[60] = -1;
		PACKET_SIZES[61] = 7;
		PACKET_SIZES[62] = 7;
		PACKET_SIZES[63] = 12;
		PACKET_SIZES[64] = 4;
		PACKET_SIZES[65] = 3;
		PACKET_SIZES[66] = 11;
		PACKET_SIZES[67] = 8;
		PACKET_SIZES[68] = 8;
		PACKET_SIZES[69] = 15;
		PACKET_SIZES[70] = 1;
		PACKET_SIZES[71] = 2;
		PACKET_SIZES[72] = 6;
		PACKET_SIZES[73] = -1;
		PACKET_SIZES[74] = -1;
		PACKET_SIZES[75] = -2;
		PACKET_SIZES[76] = 16;
		PACKET_SIZES[77] = 3;
		PACKET_SIZES[78] = 1;
		PACKET_SIZES[79] = 3;
		PACKET_SIZES[80] = 9;
		PACKET_SIZES[81] = 4;
		PACKET_SIZES[82] = -2;
		PACKET_SIZES[83] = 1;
		PACKET_SIZES[84] = 1;
		PACKET_SIZES[85] = 3;
		PACKET_SIZES[86] = -1;
		PACKET_SIZES[87] = 4;
		PACKET_SIZES[88] = 3;
		PACKET_SIZES[89] = -1;
		PACKET_SIZES[90] = 8;
		PACKET_SIZES[91] = -2;
		PACKET_SIZES[92] = -1;
		PACKET_SIZES[93] = -1;
		PACKET_SIZES[94] = 9;
		PACKET_SIZES[95] = -2;
		PACKET_SIZES[96] = 8;
		PACKET_SIZES[97] = 2;
		PACKET_SIZES[98] = 7; //SCREEN_PACKET 6
		PACKET_SIZES[99] = 2;
		PACKET_SIZES[100] = -2;
		PACKET_SIZES[101] = 3;
		PACKET_SIZES[102] = 7;
		PACKET_SIZES[103] = 4;
	}


	private Player player;
	private int chatType;

	// temp spam protection
	private int[] pthrotlecounter = new int[256];
	private long[] pthrotletimer = new long[256];

	// temp spam protection

	public WorldPacketsDecoder(Session session, Player player) {
		super(session);
		this.player = player;
	}

	@Override
	public int decode(InputStream stream) {
		while (stream.getRemaining() > 0 && session.getChannel() != null && session.getChannel().isActive() && !player.hasFinished()) {
			int start = stream.getOffset();
			int packetId = stream.readPacket(player);
			if (packetId >= PACKET_SIZES.length || packetId < 0) {
				if (Settings.DEBUG)
					System.out.println("PacketId " + packetId + " has fake packet id.");
				return -1; 
			}
			int length = PACKET_SIZES[packetId];
			if ((length == -1 && stream.getRemaining() < 1) || (length == -2 && stream.getRemaining() < 2)) 
				return start;
			
			
			if (length == -1)
				length = stream.readUnsignedByte();
			else if (length == -2)
				length = stream.readUnsignedShort();
			else if (length == -3)
				length = stream.readInt();
			else if (length == -4) {
				length = stream.getRemaining();
				if (Settings.DEBUG)
					System.out.println("Invalid size for PacketId " + packetId + ". Size guessed to be " + length);
			}
			if (length > stream.getRemaining()) {
				/*if (length > 1024) { //shouldnt happen
					if (Settings.DEBUG)
						System.out.println("PacketId " + packetId + " has fake size. - expected size " + length+", "+PACKET_SIZES[packetId]);
					return stream.getRemaining() + stream.getOffset(); //skip
				}*/
			/*	length = stream.getRemaining();
				if (Settings.DEBUG)
					System.out.println("PacketId " + packetId + " has fake size. - expected size " + length+", "+PACKET_SIZES[packetId]);
				// break;*;*/
				return start;
			}
			byte[] data = new byte[length];
			stream.readBytes(data);
			try {
				processPackets(packetId, new InputStream(data), length);
			} catch (Throwable e) {
				Logger.handle(e);
			}
		}
		return stream.getOffset();
	}

	private static int[] REFUSE_UNTIL_PIN = {
		WALKING_PACKET, NPC_CLICK1_PACKET,NPC_CLICK2_PACKET,NPC_CLICK3_PACKET,NPC_CLICK4_PACKET,
			OBJECT_CLICK1_PACKET,OBJECT_CLICK2_PACKET,OBJECT_CLICK3_PACKET,OBJECT_CLICK4_PACKET,OBJECT_CLICK5_PACKET,
			INTERFACE_ON_NPC, INTERFACE_ON_OBJECT, INTERFACE_ON_PLAYER,ITEM_GROUND_OPTION_3, ITEM_GROUND_OPTION_4,

	};

	public static void decodeLogicPacket(final Player player, LogicPacket packet) {
		int packetId = packet.getId();
		InputStream stream = new InputStream(packet.getData());

		if(!player.checkBankPin()) {
			// refuse until bank pin
			for(int i : REFUSE_UNTIL_PIN) {
				if(i == packetId) {
					return;
				}
			}
		}

		if (packetId == WALKING_PACKET || packetId == MINI_WALKING_PACKET) {
			if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead())
				return;
			if (player.isLocked() || player.isCantWalk())
				return;
			if (player.getFreezeDelay() >= Utils.currentTimeMillis()) {
				player.getPackets().sendGameMessage("A magical force prevents you from moving.");
				return;
			}
			if (!player.checkBankPin())
	            return;
			if (!player.getBank().hasVerified(10))
	            return;
			int length = stream.getLength();
			int baseX = stream.readUnsignedShort128();
			boolean forceRun = stream.readUnsigned128Byte() == 1;
			int baseY = stream.readUnsignedShort128();
			int steps = (length - 5) / 2;
			if (steps > 25)
				steps = 25;
			player.stopAll();
			if (forceRun)
				player.setRun(forceRun);

			if (steps > 0) {
				int x = 0, y = 0;
				for (int step = 0; step < steps; step++) {
					x = baseX + stream.readUnsignedByte();
					y = baseY + stream.readUnsignedByte();
				}

				steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, player.getX(), player.getY(), player.getPlane(), player.getSize(), new FixedTileStrategy(x, y), true);
				int[] bufferX = RouteFinder.getLastPathBufferX();
				int[] bufferY = RouteFinder.getLastPathBufferY();
				int last = -1;
				for (int i = steps - 1; i >= 0; i--) {
					if (!player.addWalkSteps(bufferX[i], bufferY[i], 25, true))
						break;
					last = i;
				}

				if (last != -1) {
					WorldTile tile = new WorldTile(bufferX[last], bufferY[last], player.getPlane());
					player.getPackets().sendMinimapFlag(tile.getLocalX(player.getLastLoadedMapRegionTile(), player.getMapSize()), tile.getLocalY(player.getLastLoadedMapRegionTile(), player.getMapSize()));
				} else {
					player.getPackets().sendResetMinimapFlag();
				}
			}
		} else if (packetId == INTERFACE_ON_PLAYER) {
			if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead())
				return;
			if (player.isLocked() || player.getEmotesManager().isDoingEmote())
				return;
			final int itemId = stream.readUnsignedShort();
			int playerIndex = stream.readUnsignedShort();
			int interfaceHash = stream.readIntV2();
			int interfaceSlot = stream.readUnsignedShortLE128();
			final boolean forceRun = stream.read128Byte() == 1;
			int interfaceId = interfaceHash >> 16;
			int componentId = interfaceHash & 0xFF;
			if (Utils.getInterfaceDefinitionsSize() <= interfaceId)
				return;
			if (!player.getInterfaceManager().containsInterface(interfaceId))
				return;
			if (componentId == 65535)
				componentId = -1;
			if (componentId != -1 && Utils.getInterfaceDefinitionsComponentsSize(interfaceId) <= componentId)
				return;
			final Player p2 = World.getPlayers().get(playerIndex);
			if (p2 == null || p2 == player || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId()))
				return;
			if (interfaceId != 662 && interfaceId != 747)
				player.stopAll();
			if (forceRun)
				player.setRun(forceRun);
			switch (interfaceId) {
			case 1110:
				if (componentId == 87)
					ClansManager.invite(player, p2);
				break;
			case Inventory.INVENTORY_INTERFACE:
				final Item item = player.getInventory().getItem(interfaceSlot);
				if (item == null || item.getId() != itemId)
					return;
				player.setRouteEvent(new RouteEvent(p2, new Runnable() {
					@Override
					public void run() {
						if (!player.getControlerManager().processItemOnPlayer(p2, item))
							return;
						if (itemId == 4155 || itemId >= 13281 && itemId <= 13288)
							player.getSlayerManager().invitePlayer(p2);
						else if ((item.getId() >= 25425 && item.getId() <= 25429 || item.getId() == 25493)
								|| (item.getId() >= 25437 && item.getId() <= 25440 || item.getId() == 25494
								|| item.getId() == 25472)) {
							if (!p2.getInventory().hasFreeSlots()) {
								player.getPackets().sendGameMessage(p2.getName()+" has no free inventory space.");
								return;
							}
							player.getInventory().deleteItem(item);
							p2.getInventory().addItem(item);
							p2.getPackets().sendGameMessage(p2.getName()+" gave you a "+item.getName()+"!");
							player.getPackets().sendGameMessage("You give "+p2.getName()+" a "+item.getName()+"!");
							Bot.sendLog(Bot.TRADE_STACK_CANNEL, "[type=GIVE][name="+player.getUsername()+"][to="+p2.getUsername()+"][item="+item.getName()+"("+item.getId()+")x"+Utils.getFormattedNumber(item.getAmount())+"]");
						}
					}
				}));
				break;
			case 662:
			case 747:
				if (player.getFamiliar() == null)
					return;
				player.resetWalkSteps();
				if ((interfaceId == 747 && componentId == 15) || (interfaceId == 662 && componentId == 65) || (interfaceId == 662 && componentId == 74) || interfaceId == 747 && componentId == 18) {
					if ((interfaceId == 662 && componentId == 74 || interfaceId == 747 && componentId == 24 || interfaceId == 747 && componentId == 18)) {
						if (player.getFamiliar().getSpecialAttack() != SpecialAttack.ENTITY)
							return;
					}
					if (!player.isCanPvp() || !p2.isCanPvp()) {
						player.getPackets().sendGameMessage("You can only attack players in a player-vs-player area.");
						return;
					}
					if (!player.getFamiliar().canAttack(p2)) {
						player.getPackets().sendGameMessage("You can only use your familiar in a multi-zone area.");
						return;
					} else {
						player.getFamiliar().setSpecial(interfaceId == 662 && componentId == 74 || interfaceId == 747 && componentId == 18);
						player.getFamiliar().setTarget(p2);
					}
				}
				break;
			case 193:
				switch (componentId) {
				case 28:
				case 32:
				case 24:
				case 20:
				case 30:
				case 34:
				case 26:
				case 22:
				case 29:
				case 33:
				case 25:
				case 21:
				case 31:
				case 35:
				case 27:
				case 23:
					if (Magic.checkCombatSpell(player, componentId, 1, false)) {
						player.setNextFaceWorldTile(new WorldTile(p2.getCoordFaceX(p2.getSize()), p2.getCoordFaceY(p2.getSize()), p2.getPlane()));
						if (!player.getControlerManager().canAttack(p2))
							return;
						if (!player.isCanPvp() || !p2.isCanPvp()) {
							player.getPackets().sendGameMessage("You can only attack players in a player-vs-player area.");
							return;
						}
						if (!p2.isAtMultiArea() || !player.isAtMultiArea()) {
							if (player.getAttackedBy() != p2 && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
								player.getPackets().sendGameMessage("That " + (player.getAttackedBy() instanceof Player ? "player" : "npc") + " is already in combat.");
								return;
							}
							if (p2.getAttackedBy() != player && p2.getAttackedByDelay() > Utils.currentTimeMillis()) {
								if (p2.getAttackedBy() instanceof NPC) {
									p2.setAttackedBy(player);// changes
									// enemy
									// to player,
									// player has
									// priority over
									// npc on single
									// areas
								} else {
									player.getPackets().sendGameMessage("That player is already in combat.");
									return;
								}
							}
						}
						player.getActionManager().setAction(new PlayerCombat(p2));
					}
					break;
				}
			case 950:
				switch (componentId) {
				case 52://cure other
					if (!Magic.checkSpellLevel(player, 68))
						return;
					else if (!((Player) p2).isAcceptingAid()) {
						player.getPackets().sendGameMessage(((Player) p2).getDisplayName() + " is not accepting aid");
						return;
					} else if (!Magic.checkRunes(player, true, true, 17790, 1, 17792, 1, 17782, 10))
						return;
					player.setNextAnimation(new Animation(4411));
					p2.setNextGraphics(new Graphics(736, 0, 150));
					p2.getPoison().reset();
					p2.getPackets().sendGameMessage("You have been healed by " + player.getDisplayName() + "!");
					break;
				case 64://veng other
					if (player.getSkills().getLevel(Skills.MAGIC) < 93) {
						player.getPackets().sendGameMessage("Your Magic level is not high enough for this spell.");
						return;
					}
					Long lastVeng = (Long) player.getTemporaryAttributtes().get("LAST_VENG");
					if (lastVeng != null && lastVeng + 30000 > Utils.currentTimeMillis()) {
						player.getPackets().sendGameMessage("Players may only cast vengeance once every 30 seconds.");
						return;
					}
					if (!((Player) p2).isAcceptingAid()) {
						player.getPackets().sendGameMessage(((Player) p2).getDisplayName() + " is not accepting aid");
						return;
					}
					if (!Magic.checkRunes(player, true, true, 17790, 3, 17786, 2, 17782, 10))
						return;
					player.setNextAnimation(new Animation(4411));
					player.getTemporaryAttributtes().put("LAST_VENG", Utils.currentTimeMillis());
					player.getPackets().sendGameMessage("You cast a vengeance.");
					((Player) p2).setNextGraphics(new Graphics(725, 0, 100));
					((Player) p2).setCastVeng(true);
					((Player) p2).getPackets().sendGameMessage("You have the power of vengeance!");
					break;
				}
				break;
			case 192:
				switch (componentId) {
				case 98: //air rush
				case 25:// air strike
				case 28:// water strike
				case 30:// earth strike
				case 32:// fire strike
				case 34:// air bolt
				case 39:// water bolt
				case 42:// earth bolt
				case 45:// fire bolt
				case 49:// air blast
				case 47: //crumble dead
				case 54: //iban blast
				case 56: //magic dart
				case 52:// water blast
				case 58:// earth blast
				case 63:// fire blast
				case 70:// air wave
				case 73:// water wave
				case 77:// earth wave
				case 80:// fire wave
				case 86:// teleblock
				case 84:// air surge
				case 87:// water surge
				case 89:// earth surge
				case 91:// fire surge
				case 99:// storm of armadyl
				case 36:// bind
				case 66:// Sara Strike
				case 67:// Guthix Claws
				case 68:// Flame of Zammy
				case 55:// snare
				case 81:// entangle
					if (Magic.checkCombatSpell(player, componentId, 1, false)) {
						player.setNextFaceWorldTile(new WorldTile(p2.getCoordFaceX(p2.getSize()), p2.getCoordFaceY(p2.getSize()), p2.getPlane()));
						if (!player.getControlerManager().canAttack(p2))
							return;
						if (!player.isCanPvp() || !p2.isCanPvp()) {
							player.getPackets().sendGameMessage("You can only attack players in a player-vs-player area.");
							return;
						}
						if (!p2.isAtMultiArea() || !player.isAtMultiArea()) {
							if (player.getAttackedBy() != p2 && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
								player.getPackets().sendGameMessage("That " + (player.getAttackedBy() instanceof Player ? "player" : "npc") + " is already in combat.");
								return;
							}
							if (p2.getAttackedBy() != player && p2.getAttackedByDelay() > Utils.currentTimeMillis()) {
								if (p2.getAttackedBy() instanceof NPC) {
									p2.setAttackedBy(player);// changes
									// enemy
									// to player,
									// player has
									// priority over
									// npc on single
									// areas
								} else {
									player.getPackets().sendGameMessage("That player is already in combat.");
									return;
								}
							}
						}
						player.getActionManager().setAction(new PlayerCombat(p2));
					}
					break;
				}
				break;
			case 430:
				Magic.processLunarSpell(player, componentId, p2);
				break;
			}
			if (Settings.DEBUG)
				System.out.println("Spell:" + componentId);
		} else if (packetId == INTERFACE_ON_NPC) {
			if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead())
				return;
			if (player.isLocked() || player.getEmotesManager().isDoingEmote())
				return;
			boolean forceRun = stream.readByte() == 1;
			int interfaceHash = stream.readInt();
			int npcIndex = stream.readUnsignedShortLE();
			int interfaceSlot = stream.readUnsignedShortLE128();
			int itemId = stream.readUnsignedShortLE();
			int interfaceId = interfaceHash >> 16;
			int componentId = interfaceHash - (interfaceId << 16);
			if (Utils.getInterfaceDefinitionsSize() <= interfaceId)
				return;
			if (!player.getInterfaceManager().containsInterface(interfaceId))
				return;
			if (componentId == 65535)
				componentId = -1;
			if (componentId != -1 && Utils.getInterfaceDefinitionsComponentsSize(interfaceId) <= componentId)
				return;
			NPC npc = World.getNPCs().get(npcIndex);
			if (npc == null || npc.isDead() || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId()))
				return;
			if (interfaceId != 662 && interfaceId != 747)
				player.stopAll();
			if (forceRun)
				player.setRun(forceRun);
			if (interfaceId != Inventory.INVENTORY_INTERFACE) {
				if (!npc.getDefinitions().hasAttackOption()) {
					player.getPackets().sendGameMessage("You can't attack this npc.");
					return;
				}
			}
			switch (interfaceId) {
			case Inventory.INVENTORY_INTERFACE:
				Item item = player.getInventory().getItem(interfaceSlot);
				if (item == null || item.getId() != itemId || !player.getControlerManager().processItemOnNPC(npc, item))
					return;
				else if (npc instanceof Familiar) {
					Familiar familiar = (Familiar) npc;
					if (familiar != player.getFamiliar()) {
						player.getPackets().sendGameMessage("This is not your familiar!");
						return;
					}
				}
				NPCHandler.handleItemOnNPC(player, npc, interfaceSlot, item);
				break;
			case 1165:
				/* if (componentId == 3) {
				if (!player.getControlerManager().canAttack(npc)) {
				    player.getInterfaceManager().closeInventory();
				    return;
				} else if (player.getAttackedBy() == null) {
				    player.getPackets().sendGameMessage("You need to have a target in order to deploy a dreadnip.");
				    player.getInterfaceManager().closeInventory();
				    return;
				}
				player.getInventory().deleteItem(22370, 1);
				Dreadnip dread = new Dreadnip(player, Utils.getFreeTile(player, 2), -1, true);
				dread.getCombat().setTarget(dread.getTarget().getAttackedBy());
				}*/
				break;
			case 662:
			case 747:
				if (player.getFamiliar() == null)
					return;
				player.resetWalkSteps();
				if ((interfaceId == 747 && componentId == 15) || (interfaceId == 662 && componentId == 65) || (interfaceId == 662 && componentId == 74) || interfaceId == 747 && componentId == 18 || interfaceId == 747 && componentId == 24) {
					if ((interfaceId == 662 && componentId == 74 || interfaceId == 747 && componentId == 18)) {
						if (player.getFamiliar().getSpecialAttack() != SpecialAttack.ENTITY)
							return;
					}
					if (npc instanceof Familiar) {
						Familiar familiar = (Familiar) npc;
						if (familiar == player.getFamiliar()) {
							player.getPackets().sendGameMessage("You can't attack your own familiar.");
							return;
						}
						if (!player.getFamiliar().canAttack(familiar.getOwner())) {
							player.getPackets().sendGameMessage("You can only attack players in a player-vs-player area.");
							return;
						}
					}
					if (!player.getFamiliar().canAttack(npc)) {
						player.getPackets().sendGameMessage("You can only use your familiar in a multi-zone area.");
						return;
					} else {
						player.getFamiliar().setSpecial(interfaceId == 662 && componentId == 74 || interfaceId == 747 && componentId == 18);
						player.getFamiliar().setTarget(npc);
					}
				}
				break;
			case 950:
				switch (componentId) {
				case 51:
					if (!Magic.checkSpellLevel(player, 66))
						return;
					else if (!npc.getDefinitions().hasAttackOption()) {
						player.getPackets().sendGameMessage("That NPC cannot be examined.");
						return;
					} else if (!Magic.checkRunes(player, true, true, 17790, 1, 17789, 1, 17784, 1))
						return;
					player.getInterfaceManager().sendInventoryInterface(522);
					player.getPackets().sendIComponentText(522, 0, "Monster Name: " + npc.getName());
					player.getPackets().sendIComponentText(522, 1, "Combat Level: " + npc.getCombatLevel());
					player.getPackets().sendIComponentText(522, 2, "Life Points: " + npc.getHitpoints());
					player.getPackets().sendIComponentText(522, 3, "Creature's Max Hit: " + npc.getMaxHit());
					player.getPackets().sendIComponentText(522, 4, (player.getSlayerManager().isValidTask(npc.getName()) ? "Valid Slayer Task" : ""));
					player.setNextAnimation(new Animation(4413));
					player.setNextGraphics(new Graphics(1061, 0, 150));
					break;
				case 25:
				case 27:
				case 28:
				case 30:
				case 32:// air bolt
				case 36:// water bolt
				case 37:// earth bolt
				case 41:// fire bolt
				case 42:// air blast
				case 43:// water blast
				case 45:// earth blast
				case 47:// fire blast
				case 48:// air wave
				case 49:// water wave
				case 54:// earth wave
				case 58:// fire wave
				case 61:// air surge
				case 62:// water surge
				case 63:// earth surge
				case 67:// fire surge
				case 34:// bind
				case 44:// snare
				case 59:// entangle
					if (Magic.checkCombatSpell(player, componentId, 1, false)) {
						player.setNextFaceWorldTile(new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane()));
						if (!player.getControlerManager().canAttack(npc))
							return;
						if (npc instanceof Familiar) {
							Familiar familiar = (Familiar) npc;
							if (familiar == player.getFamiliar()) {
								player.getPackets().sendGameMessage("You can't attack your own familiar.");
								return;
							}
							if (!familiar.canAttack(player)) {
								player.getPackets().sendGameMessage("You can't attack this npc.");
								return;
							}
						} else if (!npc.isForceMultiAttacked()) {
							if (!npc.isAtMultiArea() || !player.isAtMultiArea()) {
								if (player.getAttackedBy() != null && !player.getAttackedBy().isDead() && player.getAttackedBy() != npc && player.getAttackedBy() != npc && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
									player.getPackets().sendGameMessage("You are already in combat.");
									return;
								}
								if (npc.getAttackedBy() != player && npc.getAttackedByDelay() > Utils.currentTimeMillis()) {
									player.getPackets().sendGameMessage("This npc is already in combat.");
									return;
								}
							}
						}
						player.getActionManager().setAction(new PlayerCombat(npc));
					}
					break;
				}
				break;
			case 193:
				switch (componentId) {
				case 28:
				case 32:
				case 24:
				case 20:
				case 30:
				case 34:
				case 26:
				case 22:
				case 29:
				case 33:
				case 25:
				case 21:
				case 31:
				case 35:
				case 27:
				case 23:
					if (Magic.checkCombatSpell(player, componentId, 1, false)) {
						player.setNextFaceWorldTile(new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane()));
						if (!player.getControlerManager().canAttack(npc))
							return;
						if (npc instanceof Familiar) {
							Familiar familiar = (Familiar) npc;
							if (familiar == player.getFamiliar()) {
								player.getPackets().sendGameMessage("You can't attack your own familiar.");
								return;
							}
							if (!familiar.canAttack(player)) {
								player.getPackets().sendGameMessage("You can't attack this npc.");
								return;
							}
						} else if (!npc.isForceMultiAttacked()) {
							if (!npc.isAtMultiArea() || !player.isAtMultiArea()) {
								if (player.getAttackedBy() != null && !player.getAttackedBy().isDead() && player.getAttackedBy() != npc && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
									player.getPackets().sendGameMessage("You are already in combat.");
									return;
								}
								if (npc.getAttackedBy() != player && npc.getAttackedByDelay() > Utils.currentTimeMillis()) {
									player.getPackets().sendGameMessage("This npc is already in combat.");
									return;
								}
							}
						}
						player.getActionManager().setAction(new PlayerCombat(npc));
					}
					break;
				}
			case 192:
				switch (componentId) {
				case 98: //air rush
				case 25:// air strike
				case 28:// water strike
				case 30:// earth strike
				case 32:// fire strike
				case 34:// air bolt
				case 39:// water bolt
				case 42:// earth bolt
				case 45:// fire bolt
				case 49:// air blast
				case 47: //crumble dead
				case 54: //iban blast
				case 56: //magic dart
				case 52:// water blast
				case 58:// earth blast
				case 63:// fire blast
				case 70:// air wave
				case 73:// water wave
				case 77:// earth wave
				case 80:// fire wave
				case 84:// air surge
				case 87:// water surge
				case 89:// earth surge
				case 66:// Sara Strike
				case 67:// Guthix Claws
				case 68:// Flame of Zammy
				case 93:
				case 91:// fire surge
				case 99:// storm of Armadyl
				case 36:// bind
				case 55:// snare
				case 81:// entangle
					if (Magic.checkCombatSpell(player, componentId, 1, false)) {
						player.setNextFaceWorldTile(new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane()));
						if (!player.getControlerManager().canAttack(npc))
							return;
						if (npc instanceof Familiar) {
							Familiar familiar = (Familiar) npc;
							if (familiar == player.getFamiliar()) {
								player.getPackets().sendGameMessage("You can't attack your own familiar.");
								return;
							}
							if (!familiar.canAttack(player)) {
								player.getPackets().sendGameMessage("You can't attack this npc.");
								return;
							}
						} else if (!npc.isForceMultiAttacked()) {
							if (!npc.isAtMultiArea() || !player.isAtMultiArea()) {
								if (player.getAttackedBy() != null && !player.getAttackedBy().isDead() && player.getAttackedBy() != npc && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
									player.getPackets().sendGameMessage("You are already in combat.");
									return;
								}
								if (npc.getAttackedBy() != player && npc.getAttackedByDelay() > Utils.currentTimeMillis()) {
									player.getPackets().sendGameMessage("This npc is already in combat.");
									return;
								}
							}
						}
						player.getActionManager().setAction(new PlayerCombat(npc));
					}
					break;
				}
				break;
			case 430:
				Magic.processLunarSpell(player, componentId, npc);
				break;
			}
			if (Settings.DEBUG)
				System.out.println("Spell:" + componentId);
		} else if (packetId == INTERFACE_ON_OBJECT) {
			boolean forceRun = stream.readByte128() == 1;
			int itemId = stream.readUnsignedShortLE128();
			int y = stream.readShortLE128();
			int objectId = stream.readIntV2();
			int interfaceHash = stream.readInt();
			final int interfaceId = interfaceHash >> 16;
			int slot = stream.readShortLE();
			int x = stream.readShort128();
			int componentId = interfaceHash - (interfaceId << 16);
			if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead())
				return;
			if (player.isLocked() || player.getEmotesManager().isDoingEmote())
				return;
			final WorldTile tile = new WorldTile(x, y, player.getPlane());
			int regionId = tile.getRegionId();
			if (!player.getMapRegionsIds().contains(regionId))
				return;
			WorldObject mapObject = World.getObjectWithId(tile, objectId);
			if (mapObject == null || mapObject.getId() != objectId)
				return;
			final WorldObject object = mapObject;
			if (player.isDead() || Utils.getInterfaceDefinitionsSize() <= interfaceId)
				return;
			if (player.isLocked())
				return;
			if (!player.getInterfaceManager().containsInterface(interfaceId))
				return;
			player.stopAll();
			if (forceRun)
				player.setRun(forceRun);
			switch (interfaceId) {
			case Inventory.INVENTORY_INTERFACE:// inventory
				ObjectHandler.handleItemOnObject(player, object, interfaceId, slot, itemId);
				break;
			case 430://lunars
				switch (componentId) {
				case 55:
					if (player.getSkills().getLevel(Skills.MAGIC) < 66) {
						player.getPackets().sendGameMessage("You need a level of 65 in order to cast Cure Plant.");
						return;
					}
					if (!Magic.checkRunes(player, true, Magic.ASTRAL_RUNE, 1, Magic.EARTH_RUNE, 8))
						return;
					final FarmingSpot spot = player.getFarmingManager().getSpot(SpotInfo.getInfo(object.getId()));
					if (spot == null || spot.isDead()) {
						player.getPackets().sendGameMessage("This cannot be cured.");
						return;
					} else if (!spot.isDiseased()) {
						player.getPackets().sendGameMessage("Your patch is not diseased.");
						return;
					}
					player.lock(3);
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							spot.setDiseased(false);
							spot.refresh();
						}
					}, 2);
					player.getSkills().addXp(Skills.MAGIC, 60);
					player.setNextGraphics(new Graphics(742, 0, 150));
					player.setNextAnimation(new Animation(4409));
					player.getPackets().sendGameMessage("You cast the spell and your patch is in perfect health.");
					break;
				}
				break;
			case 192://regular spellbook
				switch (componentId) {
				case 60:// water charge
				case 64:// earth charge
				case 71:// fire charge
				case 74:// air charge
					for (int index = 0; index < 4; index++) {
						if (EnchantingOrbsDialogue.COMPONENTS[index] == componentId) {
							if (!Magic.checkRunes(player, false, EnchantingOrbsDialogue.REQUIRED_RUNES[index]))
								break;
							else if (!Magic.checkSpellLevel(player, EnchantingOrbsDialogue.LEVELS[index]))
								break;
							else {
								if (object.getId() == EnchantingOrbsDialogue.OBJECTS[index]) {
									player.faceObject(object);
									player.getDialogueManager().startDialogue("EnchantingOrbsDialogue", index);
								}
							}
						}
					}
					break;
				}
				break;
			}
		} else if (packetId == PLAYER_OPTION_1_PACKET) {
			if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead())
				return;
			boolean forceRun = stream.readUnsignedByte() == 1;
			int playerIndex = stream.readUnsignedShortLE128();
			Player p2 = World.getPlayers().get(playerIndex);
			if (forceRun)
				player.setRun(forceRun);
			player.stopAll();

			if (p2 == null || p2 == player || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId()))
				return;
			if (player.isLocked() || player.getEmotesManager().isDoingEmote() || !player.getControlerManager().canPlayerOption1(p2))
				return;
			if (player.getEquipment().getWeaponId() == 10501) {
				PlayerCombat.useSnowBall(player, p2);
				return;
			}
			if (!player.isCanPvp())
				return;
			if (!player.getControlerManager().canAttack(p2))
				return;
			if (!player.isCanPvp() || !p2.isCanPvp()) {
				player.getPackets().sendGameMessage("You can only attack players in a player-vs-player area.");
				return;
			}
			if (!p2.isAtMultiArea() || !player.isAtMultiArea()) {
				if (player.getAttackedBy() != p2 && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
					player.getPackets().sendGameMessage("You are already in combat.");
					return;
				}
				if (p2.getAttackedBy() != player && p2.getAttackedByDelay() > Utils.currentTimeMillis()) {
					if (p2.getAttackedBy() instanceof NPC) {
						p2.setAttackedBy(player);// changes enemy to player,
						// player has priority over
						// npc on single areas
					} else {
						player.getPackets().sendGameMessage("That player is already in combat.");
						return;
					}
				}
			}

			player.getActionManager().setAction(new PlayerCombat(p2));
		} else if (packetId == PLAYER_OPTION_2_PACKET) {
			if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead())
				return;
			boolean forceRun = stream.readUnsignedByte() == 1;
			int playerIndex = stream.readUnsignedShortLE128();
			Player p2 = World.getPlayers().get(playerIndex);
			if (p2 == null || p2 == player || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId()))
				return;
			if (player.isLocked() || player.isCantWalk())
				return;
			if (!player.getControlerManager().canPlayerOption2(p2))
				return;
			if (forceRun)
				player.setRun(forceRun);
			player.stopAll();
			player.getActionManager().setAction(new PlayerFollow(p2));
		} else if (packetId == PLAYER_OPTION_3_PACKET) {
			final boolean forceRun = stream.readUnsignedByte() == 1;
			int playerIndex = stream.readUnsignedShortLE128();
			final Player p2 = World.getPlayers().get(playerIndex);
			if (p2 == null || p2 == player || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId()))
				return;
			if (player.isLocked())
				return;
			if (forceRun)
				player.setRun(forceRun);
			player.stopAll();
			player.setRouteEvent(new RouteEvent(p2, new Runnable() {
				@Override
				public void run() {
					if (!player.getControlerManager().canPlayerOption3(p2))
						return;
				}
			}));
		} else if (packetId == PLAYER_OPTION_5_PACKET) {
			boolean forceRun = stream.readUnsignedByte() == 1;
			int playerIndex = stream.readUnsignedShortLE128();
			Player p2 = World.getPlayers().get(playerIndex);
			if (p2 == null || p2 == player || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId()))
				return;
			if (player.isLocked())
				return;
			if (forceRun)
				player.setRun(forceRun);
			player.stopAll();
			player.setRouteEvent(new RouteEvent(p2, new Runnable() {
				@Override
				public void run() {
					if (!player.getControlerManager().canPlayerOption5(p2))
						return;
				}
			}));
		} else if (packetId == PLAYER_OPTION_4_PACKET) {
			boolean forceRun = stream.readUnsignedByte() == 1;
			int playerIndex = stream.readUnsignedShortLE128();
			final Player p2 = World.getPlayers().get(playerIndex);
			if (p2 == null || p2 == player || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId()))
				return;
			if (player.isLocked())
				return;
			if (forceRun)
				player.setRun(forceRun);
			player.stopAll();
			player.setRouteEvent(new RouteEvent(p2, new Runnable() {
				@Override
				public void run() {
					if (!player.getControlerManager().canPlayerOption4(p2))
						return;
					player.stopAll();
					if (player.isIronman() && player.getHcPartner() == null) {
						if (!p2.isIronman()) {
							player.getPackets().sendGameMessage("This player is not a ironman.");
							return;
						}
						if (p2.getHcPartner() != null) {
							player.getPackets().sendGameMessage("This player already has a partner.");
							return;
						}
						if (player.isCantTrade() || player.getControlerManager().getControler() != null && player.getControlerManager().getControler() instanceof StealingCreationLobbyController) {
							player.getPackets().sendGameMessage("You are busy.");
							return;
						}
						if (p2.getInterfaceManager().containsScreenInter() || p2.isCantTrade() || p2.getControlerManager().getControler() != null && p2.getControlerManager().getControler() instanceof StealingCreationLobbyController || p2.isLocked()) {
							player.getPackets().sendGameMessage("The other player is busy.");
							return;
						}
						if (!p2.withinDistance(player, 14)) {
							player.getPackets().sendGameMessage("Unable to find target " + p2.getDisplayName());
							return;
						}
						if (!player.getBank().hasVerified(10)) {
							return;
						}
						if (p2.getTemporaryAttributtes().get("TradeTarget") == player) {
							p2.getTemporaryAttributtes().remove("TradeTarget");
							player.setHcPartner(p2.getUsername());
							p2.setHcPartner(player.getUsername());
							return;
						}
						player.getTemporaryAttributtes().put("TradeTarget", p2);
						player.getPackets().sendGameMessage("Sending " + p2.getDisplayName() + " a request...");
						player.getPackets().sendGameMessage("Warning if the other player accepts, this can not be reversed.");
						p2.getPackets().sendPartnerRequestMessage(player);
						return;
					}
					if (player.isUltimateIronman() && player.getHcPartner() == null) {
						if (!p2.isUltimateIronman()) {
							player.getPackets().sendGameMessage("This player is not an Ultimate ironman.");
							return;
						}
						if (p2.getHcPartner() != null) {
							player.getPackets().sendGameMessage("This player already has a partner.");
							return;
						}
						if (player.isCantTrade() || player.getControlerManager().getControler() != null && player.getControlerManager().getControler() instanceof StealingCreationLobbyController) {
							player.getPackets().sendGameMessage("You are busy.");
							return;
						}
						if (p2.getInterfaceManager().containsScreenInter() || p2.isCantTrade() || p2.getControlerManager().getControler() != null && p2.getControlerManager().getControler() instanceof StealingCreationLobbyController || p2.isLocked()) {
							player.getPackets().sendGameMessage("The other player is busy.");
							return;
						}
						if (!p2.withinDistance(player, 14)) {
							player.getPackets().sendGameMessage("Unable to find target " + p2.getDisplayName());
							return;
						}
						if (!player.getBank().hasVerified(10)) {
							return;
						}
						if (p2.getTemporaryAttributtes().get("TradeTarget") == player) {
							p2.getTemporaryAttributtes().remove("TradeTarget");
							player.setHcPartner(p2.getUsername());
							p2.setHcPartner(player.getUsername());
							return;
						}
						player.getTemporaryAttributtes().put("TradeTarget", p2);
						player.getPackets().sendGameMessage("Sending " + p2.getDisplayName() + " a request...");
						player.getPackets().sendGameMessage("Warning if the other player accepts, this can not be reversed.");
						p2.getPackets().sendPartnerRequestMessage(player);
						return;
					}
					if (player.isHCIronman() && player.getHcPartner() == null) {
						if (!p2.isHCIronman()) {
							player.getPackets().sendGameMessage("This player is not a HC ironman.");
							return;
						}
						if (p2.getHcPartner() != null) {
							player.getPackets().sendGameMessage("This player already has a partner.");
							return;
						}
						if (player.isCantTrade() || player.getControlerManager().getControler() != null && player.getControlerManager().getControler() instanceof StealingCreationLobbyController) {
							player.getPackets().sendGameMessage("You are busy.");
							return;
						}
						if (p2.getInterfaceManager().containsScreenInter() || p2.isCantTrade() || p2.getControlerManager().getControler() != null && p2.getControlerManager().getControler() instanceof StealingCreationLobbyController || p2.isLocked()) {
							player.getPackets().sendGameMessage("The other player is busy.");
							return;
						}
						if (!p2.withinDistance(player, 14)) {
							player.getPackets().sendGameMessage("Unable to find target " + p2.getDisplayName());
							return;
						}
						if (!player.getBank().hasVerified(10)) {
							return;
						}
						if (p2.getTemporaryAttributtes().get("TradeTarget") == player) {
							p2.getTemporaryAttributtes().remove("TradeTarget");
							player.setHcPartner(p2.getUsername());
							p2.setHcPartner(player.getUsername());
							return;
						}
						player.getTemporaryAttributtes().put("TradeTarget", p2);
						player.getPackets().sendGameMessage("Sending " + p2.getDisplayName() + " a request...");
						player.getPackets().sendGameMessage("Warning if the other player accepts, this can not be reversed.");
						p2.getPackets().sendPartnerRequestMessage(player);
						return;
					}
					if (player.isBeginningAccount()) {
						player.getPackets().sendGameMessage("Starter accounts cannot trade for the first hour of playing time.");
						return;
					}
				/*	if (player.isIronman()) {
						player.getPackets().sendGameMessage("You can't use this feature as an ironman.");
						return;
					}*/
					if (player.isHCIronman() && (player.getHcPartner() == null || !p2.getUsername().equalsIgnoreCase(player.getHcPartner()))) {
						player.getPackets().sendGameMessage("You can only trade your partner as a duo hc ironman.");
						return;
					}
					if (player.isUltimateIronman() && (player.getHcPartner() == null || !p2.getUsername().equalsIgnoreCase(player.getHcPartner()))) {
						player.getPackets().sendGameMessage("You can only trade your partner as a duo ultimate ironman.");
						return;
					}
					if (player.isIronman() && (player.getHcPartner() == null || !p2.getUsername().equalsIgnoreCase(player.getHcPartner()))) {
						player.getPackets().sendGameMessage("You can only trade your partner as a duo ironman.");
						return;
					}
					if ((player.isHCIronman() || player.isUltimateIronman() || player.isIronman()) != (p2.isHCIronman() || p2.isUltimateIronman() || p2.isIronman())) {
						player.getPackets().sendGameMessage("You and your partner need to be ironman in order to trade.");
						return;
					}
					/*if (player.isExtreme() != p2.isExtreme()) {
						player.getPackets().sendGameMessage("Both users need to be extreme mode in order to trade.");
						return;
					}*/
					if (player.isDungeoneer() && !player.getDungManager().isInside()) {
						player.getPackets().sendGameMessage("You can't use this feature outside dungeons as an dungeoneer.");
						return;
					}
					if (player.tournamentResetRequired() || player.isCantTrade() || player.getControlerManager().getControler() != null && player.getControlerManager().getControler() instanceof StealingCreationLobbyController) {
						player.getPackets().sendGameMessage("You are busy.");
						return;
					}
					if (p2.isBeginningAccount()) {
						player.getPackets().sendGameMessage("Your target is a starter account, which cannot trade for the first hour of playing time.");
						return;
					}
					if (p2.getInterfaceManager().containsScreenInter() || p2.isCantTrade() || p2.getControlerManager().getControler() != null && p2.getControlerManager().getControler() instanceof StealingCreationLobbyController || p2.isLocked()) {
						player.getPackets().sendGameMessage("The other player is busy.");
						return;
					}
					if (!p2.withinDistance(player, 14)) {
						player.getPackets().sendGameMessage("Unable to find target " + p2.getDisplayName());
						return;
					}
					if (!player.getBank().hasVerified(10)) {
						return;
					}
					if (p2.getTemporaryAttributtes().get("TradeTarget") == player) {
						p2.getTemporaryAttributtes().remove("TradeTarget");
						player.getTrade().openTrade(p2);
						p2.getTrade().openTrade(player);
						return;
					}
					player.getTemporaryAttributtes().put("TradeTarget", p2);
					player.getPackets().sendGameMessage("Sending " + p2.getDisplayName() + " a request...");
					p2.getPackets().sendTradeRequestMessage(player);
				}
			}));
		} else if (packetId == PLAYER_OPTION_6_PACKET) {
			if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead())
				return;
			boolean forceRun = stream.readUnsignedByte() == 1;
			int playerIndex = stream.readUnsignedShortLE128();
			Player p2 = World.getPlayers().get(playerIndex);
			if (p2 == null || p2 == player || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId()))
				return;
			if (player.isLocked())
				return;
			if (forceRun)
				player.setRun(forceRun);
			player.stopAll();
			if (player.getRights() >= 1 && player.getRights() <= 3)
				Commands.rightClickPunish(player, p2);
				//player.getDialogueManager().startDialogue("AddOffenceD", p2.getDisplayName());
			else
				ReportAbuse.report(player, p2.getDisplayName());
		} else if (packetId == PLAYER_OPTION_7_PACKET) {
			if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead())
				return;
			boolean forceRun = stream.readUnsignedByte() == 1;
			int playerIndex = stream.readUnsignedShortLE128();
			Player p2 = World.getPlayers().get(playerIndex);
			if (p2 == null || p2 == player || p2.isDead() || p2.hasFinished()
					|| !player.getMapRegionsIds().contains(p2.getRegionId()))
				return;
			if (player.isLocked())
				return;
			if (forceRun)
				player.setRun(forceRun);
			player.stopAll();
			PlayerExamine.examine(player, p2);
		} else if (packetId == PLAYER_OPTION_9_PACKET) {
			boolean forceRun = stream.readUnsignedByte() == 1;
			int playerIndex = stream.readUnsignedShortLE128();
			Player p2 = World.getPlayers().get(playerIndex);
			if (p2 == null || p2 == player || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId()))
				return;
			if (player.isLocked())
				return;
			if (forceRun)
				player.setRun(forceRun);
			player.stopAll();
			if (ClansManager.viewInvite(player, p2))
				return;
			if (p2.getTemporaryAttributtes().get("social_request") == player)
				player.getSlayerManager().invitePlayer(p2);
		} else if (packetId == ATTACK_NPC) {
			if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead())
				return;
			int npcIndex = stream.readUnsignedShort128();
			boolean forceRun = stream.read128Byte() == 1;
			player.stopAll();
			if (forceRun) //you scrwed up cutscenes
				player.setRun(forceRun);
			NPC npc = World.getNPCs().get(npcIndex);
			if (npc == null || npc.isDead() || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId()))
				return;
			if (player.isLocked() || player.getEmotesManager().isDoingEmote())
				return;
			if (npc.getId() != 9085 && !(npc instanceof GraveStone) && (!npc.getDefinitions().hasAttackOption() || !player.getControlerManager().canAttack(npc)))
				return;
			if (npc.getId() == 9085) {
				player.setRouteEvent(new RouteEvent(npc, new Runnable() {
					@Override
					public void run() {
						player.faceEntity(npc);
						SlayerMaster.startInteractionForId(player, npc.getId(), 5);
					}
				}));
				return;
			} else if (npc instanceof GraveStone) { //exception cuz using attack option as 5th option
				player.setRouteEvent(new RouteEvent(npc, new Runnable() {
					@Override
					public void run() {
						player.faceEntity(npc);
						GraveStone grave = (GraveStone) npc;
						grave.lootAll(player);
					}
				}));
				return;
			} else if (npc instanceof Familiar) {
				Familiar familiar = (Familiar) npc;
				if (familiar == player.getFamiliar()) {
					player.getPackets().sendGameMessage("You can't attack your own familiar.");
					return;
				}
				if (!familiar.canAttack(player)) {
					player.getPackets().sendGameMessage("You can't attack this npc.");
					return;
				}
			} else if (npc instanceof DoorSupport) {
				if (!((DoorSupport) npc).canDestroy(player)) {
					player.getPackets().sendGameMessage("You cannot see a way to open this door...");
					return;
				}
			} else if (!npc.isForceMultiAttacked()) {
				if (!npc.isAtMultiArea() || !player.isAtMultiArea()) {
					if (player.getAttackedBy() != null && !player.getAttackedBy().isDead() && player.getAttackedBy() != npc && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
						player.getPackets().sendGameMessage("You are already in combat.");
						return;
					}
					if (npc.getAttackedBy() != player && npc.getAttackedByDelay() > Utils.currentTimeMillis()) {
						player.getPackets().sendGameMessage("This npc is already in combat.");
						return;
					}
				}
			}
			player.getActionManager().setAction(new PlayerCombat(npc));
		} else if (packetId == NPC_CLICK1_PACKET)
			NPCHandler.handleOption1(player, stream);
		else if (packetId == NPC_CLICK2_PACKET)
			NPCHandler.handleOption2(player, stream);
		else if (packetId == NPC_CLICK3_PACKET)
			NPCHandler.handleOption3(player, stream);
		else if (packetId == NPC_CLICK4_PACKET)
			NPCHandler.handleOption4(player, stream);
		else if (packetId == OBJECT_CLICK1_PACKET)
			ObjectHandler.handleOption(player, stream, 1);
		else if (packetId == OBJECT_CLICK2_PACKET)
			ObjectHandler.handleOption(player, stream, 2);
		else if (packetId == OBJECT_CLICK3_PACKET)
			ObjectHandler.handleOption(player, stream, 3);
		else if (packetId == OBJECT_CLICK4_PACKET)
			ObjectHandler.handleOption(player, stream, 4);
		else if (packetId == OBJECT_CLICK5_PACKET)
			ObjectHandler.handleOption(player, stream, 5);
		else if (packetId == ITEM_GROUND_OPTION_3) {
			if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead())
				return;
			if (player.isLocked())
				return;
			int y = stream.readUnsignedShort();
			int x = stream.readUnsignedShortLE();
			final int id = stream.readUnsignedShort();
			boolean forceRun = stream.readUnsigned128Byte() == 1;
			final WorldTile tile = new WorldTile(x, y, player.getPlane());
			final int regionId = tile.getRegionId();
			if (!player.getMapRegionsIds().contains(regionId))
				return;
			final FloorItem item = World.getRegion(regionId).getGroundItem(id, tile, player);
			if (item == null)
				return;
			if (forceRun)
				player.setRun(forceRun);
			player.stopAll();
			player.setRouteEvent(new RouteEvent(item, new Runnable() {
				@Override
				public void run() {
					final FloorItem item = World.getRegion(regionId).getGroundItem(id, tile, player);
					if (item == null || !player.getControlerManager().canTakeItem(item))
						return;
					
					if ((player.isIronman() || player.isUltimateIronman()) && player.getRegionId() == 12084) {
						player.getPackets().sendGameMessage("You can't pickup other people items as an ironman.");
						return;
					}
					if (World.removeGroundItem(player, item)) {
						if (!World.isFloorFree(tile.getPlane(), tile.getX(), tile.getY())) {
							player.setNextFaceWorldTile(tile);
							player.setNextAnimation(new Animation(833));
						}
						Logger.globalLog(player.getUsername(), player.getSession().getIP(), new String(" has picked up item [ id: " + item.getId() + ", amount: " + item.getAmount() + " ] originally owned to " + (item.getOwner() == null ? "no owner" : item.getOwner()) + "."));
						Bot.sendLog(Bot.PICKUP_DROP_CHANNEL, "[type=PICKUP][name="+player.getUsername()+"][from="+item.getOwner()+"][item="+item.getName()+"("+item.getId()+")x"+Utils.getFormattedNumber(item.getAmount())+"]");
					}
				}
			}));
		} else if (packetId == ITEM_GROUND_OPTION_4) {
			if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead())
				return;
			if (player.isLocked())
				return;
			int y = stream.readUnsignedShort();
			int x = stream.readUnsignedShortLE();
			final int id = stream.readUnsignedShort();
			boolean forceRun = stream.read128Byte() == 1;
			final WorldTile tile = new WorldTile(x, y, player.getPlane());
			final int regionId = tile.getRegionId();
			if (!player.getMapRegionsIds().contains(regionId))
				return;
			final FloorItem item = World.getRegion(regionId).getGroundItem(id, tile, player);
			if (item == null)
				return;
			if (forceRun)
				player.setRun(forceRun);
			player.stopAll();
			player.setRouteEvent(new RouteEvent(item, new Runnable() {
				@Override
				public void run() {
					final FloorItem item = World.getRegion(regionId).getGroundItem(id, tile, player);
					if (item == null)
						return;
					for (Fire fire : Fire.values()) {
						if (item.getId() == fire.getLogId()) {
							player.getActionManager().setAction(new Firemaking(fire, true));
							return;
						}
					}
				}
			}));
		}
	}

	public void processPackets(final int packetId, InputStream stream, int length) {
		player.setLastPing();
		if (packetId >= 0 && packetId < 256) {
			long ctime = System.nanoTime();
			if ((ctime - pthrotletimer[packetId]) > (1000000 * 600)) {
				pthrotlecounter[packetId] = 0;
				pthrotletimer[packetId] = ctime;
			}

			if (++pthrotlecounter[packetId] > 10) {
				pthrotletimer[packetId] = ctime;// reset timer to completly mitigate ddos
				return;
			}
		}
		if (packetId != AFK_PACKET && packetId != PING_PACKET
				&& packetId != RECEIVE_PACKET_COUNT_PACKET && packetId != MUSIC_PACKET
				&& packetId != KEY_TYPED_PACKET) 
			player.setLastActive();
		
		//System.out.println("packet: "+packetId +", "+ Thread.currentThread().getName());
		if (packetId == PING_PACKET) {
			player.getPackets().sendPing();
		} else if (packetId == MUSIC_PACKET) {
			int lastMusicIndex = stream.readInt();
			player.getMusicsManager().replayMusic(lastMusicIndex);
		} else if (packetId == WORLD_LIST_UPDATE) {
			int checksum = stream.readInt();
			LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodePlayerWorldListStatusRequest(player.getUsername(), checksum).trim());
		} else if (packetId == MOUVE_MOUSE_PACKET) {
			// USELESS PACKET
		} else if (packetId == KEY_TYPED_PACKET) {
			for (int i = 0; i < length / 4; i++) {
				int key = stream.readUnsignedByte();
				int timeSinceLastKey = stream.read24BitInt();
			}
		} else if (packetId == RECEIVE_PACKET_COUNT_PACKET) {
			// interface packets
			stream.readInt();
		} else if (packetId == INTERFACE_ON_INTERFACE) {
			InventoryOptionsHandler.handleInterfaceOnInterface(player, stream);
		} else if (packetId == AFK_PACKET) {
			int v = stream.readUnsignedShort();
		} else if (packetId == CLOSE_INTERFACE_PACKET) {
			if (player.hasStarted() && !player.hasFinished() && !player.isRunning()) {// used
				// for
				// old
				// welcome
				// screen
				player.run();
				return;
			}
			player.stopAll(true, true, false);
		} else if (packetId == MOVE_CAMERA_PACKET) {
			// not using it atm
			stream.readUnsignedShort();
			stream.readUnsignedShort();
		} else if (packetId == IN_OUT_SCREEN_PACKET) {
			// not using this check because not 100% efficient
			@SuppressWarnings("unused")
			boolean inScreen = stream.readByte() == 1;
		} else if (packetId == SCREEN_PACKET) {
			int displayMode = stream.readUnsignedByte();
			int graphicMode = stream.readUnsignedByte();
			player.setScreenWidth(stream.readUnsignedShort());
			player.setScreenHeight(stream.readUnsignedShort());
			player.setGraphicMode(graphicMode);
			@SuppressWarnings("unused")
			boolean switchScreenMode = stream.readUnsignedByte() == 1;
			if (!player.hasStarted() || player.hasFinished() || displayMode == player.getDisplayMode() || !player.getInterfaceManager().containsInterface(742))
				return;
			player.setDisplayMode(displayMode);
			if (player.isOsrsGameframe())//fixes orbs
				player.resetGameframe();
			player.getInterfaceManager().removeAll();
			player.getInterfaceManager().sendInterfaces();
			player.getInterfaceManager().sendInterface(742);
		} else if (packetId == CLICK_PACKET) {
			int mouseHash = stream.readShortLE128();
			int mouseButton = mouseHash >> 15;
			int time = mouseHash - (mouseButton << 15);// time
			int positionHash = stream.readIntV1();
			int y = positionHash >> 16;// y;
			int x = positionHash - (y << 16);// x
			@SuppressWarnings("unused")
			boolean clicked;
			// mass click or stupid autoclicker, lets stop lagg
			if (time <= 1 || x < 0 || x > player.getScreenWidth() || y < 0 || y > player.getScreenHeight()) {
				// player.getSession().getChannel().close();
				clicked = false;
				return;
			}
			clicked = true;
		} else if (packetId == DIALOGUE_CONTINUE_PACKET) {
			int interfaceHash = stream.readInt();
			int junk = stream.readShort128();
			int interfaceId = interfaceHash >> 16;
			int buttonId = (interfaceHash & 0xFF);
			if (Utils.getInterfaceDefinitionsSize() <= interfaceId) {
				// hack, or server error or client error
				// player.getSession().getChannel().close();
				return;
			}
			if (!player.isRunning() || !player.getInterfaceManager().containsInterface(interfaceId))
				return;
			
			//compatibliy with old system
			if (interfaceId >= 228 && interfaceId <= 235) { 
				interfaceId = 1188;
				int option = (buttonId - 1);
				buttonId = (option > 1 ? 11 : 10) + option;
			} else if ((interfaceId >= 64 && interfaceId <= 67)) {
				interfaceId = 1191;
				buttonId = 18;
			} else if (interfaceId >= 241 && interfaceId <= 244) {//done
				interfaceId = 1184;
				buttonId = 18;
			} else if (interfaceId == 210) {//done
				interfaceId = 1186;
				buttonId = 7;
			}
				
			
			if (Settings.DEBUG)
				Logger.log(this, "Dialogue: " + interfaceId + ", " + buttonId + ", " + junk);
			
			player.getDialogueManager().continueDialogue(interfaceId, buttonId);
		} else if (packetId == WORLD_MAP_CLICK) {
			int coordinateHash = stream.readInt();
			int x = coordinateHash >> 14;
			int y = coordinateHash & 0x3fff;
			int plane = coordinateHash >> 28;
			Integer hash = (Integer) player.getTemporaryAttributtes().get("worldHash");
			if (hash == null || coordinateHash != hash)
				player.getTemporaryAttributtes().put("worldHash", coordinateHash);
			else {
				player.getTemporaryAttributtes().remove("worldHash");
				player.getHintIconsManager().addHintIcon(x, y, plane, 20, 0, 2, -1, true);
				player.getVarsManager().sendVar(1159, coordinateHash);
			}
		} else if (packetId == ACTION_BUTTON1_PACKET || packetId == ACTION_BUTTON2_PACKET || packetId == ACTION_BUTTON4_PACKET || packetId == ACTION_BUTTON5_PACKET || packetId == ACTION_BUTTON6_PACKET || packetId == ACTION_BUTTON7_PACKET || packetId == ACTION_BUTTON8_PACKET || packetId == ACTION_BUTTON3_PACKET || packetId == ACTION_BUTTON9_PACKET || packetId == ACTION_BUTTON10_PACKET) {
			ButtonHandler.handleButtons(player, stream, packetId);
		} else if (packetId == ENTER_NAME_PACKET) {
			if (!player.isRunning() || player.isDead())
				return;
			String value = stream.readString();
			if (value.equals("")) {
				return;
			}
			if (player.getInterfaceManager().containsInterface(1108))
				player.getFriendsIgnores().setChatPrefix(value);
			else if (player.getTemporaryAttributtes().remove("setclan") != null)
				ClansManager.createClan(player, value);
			else if (player.getTemporaryAttributtes().remove("joinguestclan") != null)
				ClansManager.connectToClan(player, value, true);
			else if (player.getTemporaryAttributtes().remove("banclanplayer") != null)
				ClansManager.banPlayer(player, value);
			else if (player.getTemporaryAttributtes().remove("unbanclanplayer") != null)
				ClansManager.unbanPlayer(player, value);
			else if (player.getTemporaryAttributtes().remove(Key.DUNGEON_INVITE) != null)
				player.getDungManager().invite(value);
			else if (player.getTemporaryAttributtes().remove(Key.CLAN_WARS_VIEW) != null)
				ClanWars.enter(player, value);
			else if (player.getTemporaryAttributtes().remove("enterhouse") != null)
				House.enterHouse(player, value);
			else if (player.getTemporaryAttributtes().remove(Key.REFERRAL_NAME) != null) 
				ReferralSystem.addRef(player, value);
			else if (player.getTemporaryAttributtes().get(Key.MAKE_PRESET) != null) 
				player.getDialogueManager().startDialogue("MakePresetD", player.getTemporaryAttributtes().remove(Key.MAKE_PRESET), value);
			else{
				Boss boss = (Boss) player.getTemporaryAttributtes().remove(Key.JOIN_BOSS_INSTANCE);
				if(boss != null)
					BossInstanceHandler.joinInstance(player, boss, value.toLowerCase(), false);
			}
		} else if (packetId == ENTER_LONG_TEXT_PACKET) {
			if (!player.isRunning() || player.isDead())
				return;
			String value = stream.readString();
			if (value.equals(""))
				return;
			if (player.getTemporaryAttributtes().remove("entering_note") == Boolean.TRUE)
				player.getNotes().add(value);
			else if (player.getTemporaryAttributtes().remove("editing_note") == Boolean.TRUE)
				player.getNotes().edit(value);
			else if (player.getTemporaryAttributtes().remove("ticket_other") != null) {
				TicketSystem.addTicket(player, new TicketEntry(player, value));
				player.getDialogueManager().startDialogue("SimpleMessage", "Your ticket has been submitted.");
			} else if (player.getTemporaryAttributtes().remove("forum_authuserinput") == Boolean.TRUE) {
				player.getTemporaryAttributtes().put("forum_authuser", value);
				player.getTemporaryAttributtes().put("forum_authpasswordinput", true);
				player.getPackets().sendInputLongTextScript("Enter your forum password:");
			} else if (player.getTemporaryAttributtes().remove("forum_authpasswordinput") == Boolean.TRUE) {
				String authuser = (String) player.getTemporaryAttributtes().get("forum_authuser");
				String authpassword = value;
				if (authuser == null || authpassword == null)
					return;
				LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodeAccountVarUpdate(player.getUsername(), LoginProtocol.VAR_TYPE_AUTH, authuser + "@AUTHSPLIT@" + authpassword).trim());
				player.getTemporaryAttributtes().remove("forum_authuser");
				//player.getPackets().sendGameMessage("Feature disabled due to rework.");
			} else if (player.getTemporaryAttributtes().remove(Key.CHANGE_PASSWORD) == Boolean.TRUE) {
				if (value.length() > 15) {
					player.getPackets().sendGameMessage("Your password is too large.");
					return;
				}
				LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodeAccountVarUpdate(player.getUsername(), LoginProtocol.VAR_TYPE_PASSWORD, value).trim());
				player.getPackets().sendGameMessage("Your new password is: "+value+". Please relog to confirm.");
			} else if (player.getTemporaryAttributtes().remove(Key.SEARCH_TELEPORT) == Boolean.TRUE
					&& player.getInterfaceManager().containsInterface(TeleportationInterface.ID)) {
				//Dialogue d = player.getDialogueManager().getLast();
				//if (d instanceof EconomyManager)
					//((EconomyManager)d).searchTeleport(value);
				TeleportationInterface.search(player, value);
			} else if (player.getTemporaryAttributtes().remove(Key.SEARCH_NPC_DROP) == Boolean.TRUE
					&& player.getInterfaceManager().containsInterface(Drops.DROP_INTERFACE_ID)) {
				Drops.search(player, value);
			} else if (player.getTemporaryAttributtes().remove("change_troll_name") == Boolean.TRUE) {
				value = Utils.formatPlayerNameForDisplay(value);
				if (value.length() < 3 || value.length() > 14) {
					player.getPackets().sendGameMessage("You can't use a name shorter than 3 or longer than 14 characters.");
					return;
				}
				if (value.equalsIgnoreCase("none")) {
					player.getPetManager().setTrollBabyName(null);
				} else {
					player.getPetManager().setTrollBabyName(value);
					if (player.getPet() != null && player.getPet().getId() == Pets.TROLL_BABY.getBabyNpcId()) {
						player.getPet().setName(value);
					}
				}
			} else if (player.getTemporaryAttributtes().remove("yellcolor") == Boolean.TRUE) {
				if (value.length() != 6) {
					player.getPackets().sendGameMessage("The HEX yell color you wanted to pick cannot be longer and shorter then 6.");
				} else if (Utils.containsInvalidCharacter(value) || value.contains("_")) {
					player.getPackets().sendGameMessage("The requested yell color can only contain numeric and regular characters.");
				} else {
					player.setYellColor(value);
					player.getPackets().sendGameMessage("Your yell color has been changed to <col=" + player.getYellColor() + ">" + player.getYellColor() + "</col>.");
				}
			} else if (player.getTemporaryAttributtes().remove("setdisplay") == Boolean.TRUE) {
				if (Utils.invalidAccountName(Utils.formatPlayerNameForProtocol(value))) {
					player.getPackets().sendGameMessage("Name contains invalid characters or is too short/long.");
					return;
				}
				if (World.oldBotNames.contains(value.toLowerCase())) {
					player.getPackets().sendGameMessage("This name appears to be taken.");
					return;
				}
				LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodeAccountVarUpdate(player.getUsername(), LoginProtocol.VAR_TYPE_DISPLAY_NAME, Utils.formatPlayerNameForDisplay(value)).trim());
				//player.getPackets().sendGameMessage("Feature disabled due to rework.");
			} else if (player.getInterfaceManager().containsInterface(1103))
				ClansManager.setClanMottoInterface(player, value);
		} else if (packetId == ENTER_INTEGER_PACKET) {
			if (!player.isRunning() || player.isDead())
				return;
			int value = stream.readInt();
			if (value < 0)
				return;

			if (player.getTemporaryAttributtes().get(Key.GAMBLING) != null && player.getControlerManager().getControler() instanceof DuelControler) {
				Player target = (Player) player.getTemporaryAttributtes().get(Key.GAMBLING);
				player.getTemporaryAttributtes().put(Key.GAMBLING_AMOUNT, value);
				player.getPackets().sendGameMessage("Sending " + target.getDisplayName() + " a request...");
				target.getPackets().sendGambleChallengeRequestMessage(player, value);

			}

			if (player.getTemporaryAttributtes().get(Key.SPAWN_ITEM) instanceof Integer && player.getControlerManager().getControler() instanceof FfaZone && !player.isCanPvp()) {
				Integer id = (Integer) player.getTemporaryAttributtes().remove(Key.SPAWN_ITEM);
				player.getInventory().addItem(id, value);
			}

			if (player.getDialogueManager().getLast() instanceof CustomSeedD) {
				if (value >= 1000000)
					((CustomSeedD) player.getDialogueManager().getLast()).redo();
				else {
					if (player.getDungManager().getParty() != null)
						player.getDungManager().getParty().setCustomSeed((long) value);
				}
			} else if ((player.getInterfaceManager().containsInterface(762) && player.getInterfaceManager().containsInterface(763)) || player.getInterfaceManager().containsInterface(11)) {
				Integer bank_item_X_Slot = (Integer) player.getTemporaryAttributtes().remove("bank_item_X_Slot");
				if (bank_item_X_Slot == null)
					return;
				player.getBank().setLastX(value);
				player.getBank().refreshLastX();
				if (player.getTemporaryAttributtes().remove("bank_isWithdraw") != null)
					player.getBank().withdrawItem(bank_item_X_Slot, value);
				else
					player.getBank().depositItem(bank_item_X_Slot, value, player.getInterfaceManager().containsInterface(11) ? false : true);
			} else if (player.getInterfaceManager().containsInterface(631) && player.getTemporaryAttributtes().get(Key.DUEL_COIN_WITHDRAWL) != null) {
				DuelArena arena = (DuelArena) player.getTemporaryAttributtes().get(Key.DUEL_COIN_WITHDRAWL);
				Controller control = player.getControlerManager().getControler();
				if (control == null || control != arena)
					return;
				long coinsAmount = player.getInventory().getCoinsAmount();
				if (coinsAmount == 0)
					return;
				else if (value >= coinsAmount)
					value = (int) coinsAmount;
				arena.addItem(new Item(995, value));
			} else if (player.getInterfaceManager().containsInterface(DungeonRewardShop.REWARD_SHOP) && player.getTemporaryAttributtes().get(Key.PURCHASE_TOKEN_AMOUNT) != null) {
				player.getDialogueManager().startDialogue("DungExperiencePurchase", value);
			} else if (player.getInterfaceManager().containsInterface(300) && player.getTemporaryAttributtes().get(Key.FORGE_X) != null) {
				Integer index = (Integer) player.getTemporaryAttributtes().remove(Key.FORGE_X);
				if (index == null)
					return;
				boolean dungeoneering = false;
				if (index > 100) {
					index -= 100;
					dungeoneering = true;
				}
				player.closeInterfaces();
				player.getActionManager().setAction(new Smithing(index, value, dungeoneering));
			} else if (player.getInterfaceManager().containsInterface(AccessorySmithing.ACCESSORY_INTERFACE) && player.getTemporaryAttributtes().get(Key.JEWLERY_SMITH_COMP) != null) {
				AccessorySmithing.handleButtonClick(player, (int) player.getTemporaryAttributtes().get(Key.JEWLERY_SMITH_COMP), value);
			} else if (player.getInterfaceManager().containsInterface(Summoning.POUCHES_INTERFACE) && player.getTemporaryAttributtes().get(Key.INFUSE_X) != null) {
				boolean dungeoneering = (boolean) player.getTemporaryAttributtes().remove(Key.INFUSE_X);
				int item = (int) player.getTemporaryAttributtes().remove(Key.INFUSE_ITEM);
				Summoning.handlePouchInfusion(player, item >> 16, item & 0xFF, value, dungeoneering);
			} else if (player.getTemporaryAttributtes().get(Key.X_DIALOG) != null) {
				int depositItem = (int) player.getTemporaryAttributtes().remove(Key.X_DIALOG);
				int fromInterface = (int) player.getTemporaryAttributtes().remove(Key.X_DIALOG_INTERFACE);
				ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
				if (fromInterface == PartyRoom.CHEST_INV_INTERFACE)
					PartyRoom.deposit(player, depositItem, value);
				else if(fromInterface == ChambersOfXeric.SHARED_STORAGE_INTERFACE) {
					if(raid != null)
						raid.sharedWithdraw(player, depositItem, value);
				} else if(fromInterface == ChambersOfXeric.PRIVATE_STORAGE_INTERFACE) {
					ChambersOfXeric.privateWithdraw(player, depositItem, value);
				} else if(fromInterface == -ChambersOfXeric.PRIVATE_STORAGE_INTERFACE) {
					//private inv x deposit
					if(raid == null) {
						player.sendMessage("You cannot deposit items out here.");
					} else {
						raid.privateDeposit(player, depositItem, value, true);
					}
				} else if(fromInterface == -ChambersOfXeric.SHARED_STORAGE_INTERFACE) {
					//public inv x deposit
					if (raid != null)
						raid.sharedDeposit(player, depositItem, value);
				}
			} else if (player.getInterfaceManager().containsInterface(206) && player.getInterfaceManager().containsInterface(207)) {
				Integer pc_item_X_Slot = (Integer) player.getTemporaryAttributtes().remove("pc_item_X_Slot");
				if (pc_item_X_Slot == null)
					return;
				if (player.getTemporaryAttributtes().remove("pc_isRemove") != null)
					player.getPriceCheckManager().removeItem(pc_item_X_Slot, value);
				else
					player.getPriceCheckManager().addItem(pc_item_X_Slot, value);
			} else if (player.getInterfaceManager().containsInterface(400)) {
				Integer create_tab_X_component = (Integer) player.getTemporaryAttributtes().remove("create_tab_X_component");
				if (create_tab_X_component == null)
					return;
				TabletMaking.handleTabletCreation(player, create_tab_X_component, value);
			} else if (player.getInterfaceManager().containsInterface(671) && player.getInterfaceManager().containsInterface(665)) {
				if (player.getFamiliar() == null || player.getFamiliar().getBob() == null)
					return;
				Integer bob_item_X_Slot = (Integer) player.getTemporaryAttributtes().remove("bob_item_X_Slot");
				if (bob_item_X_Slot == null)
					return;
				if (player.getTemporaryAttributtes().remove("bob_isRemove") != null)
					player.getFamiliar().getBob().removeItem(bob_item_X_Slot, value);
				else
					player.getFamiliar().getBob().addItem(bob_item_X_Slot, value);
			} else if (player.getInterfaceManager().containsInterface(335) && player.getInterfaceManager().containsInterface(336)) {
				if (player.getTemporaryAttributtes().remove(Key.TRADE_COIN_WITHDRAWL) != null) {
					long coinsAmount = player.getInventory().getCoinsAmount();
					if (coinsAmount == 0)
						return;
					else if (value >= coinsAmount)
						value = (int) coinsAmount;
					Item item = new Item(995, value);
					int tradeCoins = player.getTrade().getItems().getNumberOf(995);
					long totalCoins = (long) value + (long) tradeCoins;
					if(totalCoins > Integer.MAX_VALUE) {
						int coinsAllowed = Integer.MAX_VALUE - tradeCoins;
						item.setAmount(coinsAllowed);
						if(coinsAllowed <= 0) {
							player.sendMessage("You cannot add any more coins to the trade.");
							return;
						}
					}

					if(tradeCoins == 0 && player.getTrade().getItems().getFreeSlots() == 0) {
						player.sendMessage("The trade is full.");
						return;
					}

					// only delete coins if add succeeds
					if(player.getTrade().addItem(item))
						player.getInventory().removeItemMoneyPouch(item);

					return;
				}
				Integer trade_item_X_Slot = (Integer) player.getTemporaryAttributtes().remove("trade_item_X_Slot");
				if (trade_item_X_Slot == null)
					return;
				if (player.getTemporaryAttributtes().remove("trade_isRemove") != null)
					player.getTrade().removeItem(trade_item_X_Slot, value);
				else
					player.getTrade().addItem(trade_item_X_Slot, value);
			} else if (player.getInterfaceManager().containsInterface(403) && player.getTemporaryAttributtes().get("PlanksConvert") != null) {
				Sawmill.convertPlanks(player, (Plank) player.getTemporaryAttributtes().remove("PlanksConvert"), value);
			} else if (player.getInterfaceManager().containsInterface(902) && player.getTemporaryAttributtes().get("PlankMake") != null) {
				Integer type = (Integer) player.getTemporaryAttributtes().remove("PlankMake");
				if (player.getControlerManager().getControler() instanceof SawmillController)
					((SawmillController) player.getControlerManager().getControler()).cutPlank(type, value);
			} else if (player.getInterfaceManager().containsInterface(903) && player.getTemporaryAttributtes().get("PlankWithdraw") != null) {
				Integer type = (Integer) player.getTemporaryAttributtes().remove("PlankWithdraw");
				if (player.getControlerManager().getControler() instanceof SawmillController)
					((SawmillController) player.getControlerManager().getControler()).withdrawFromCart(type, value);
			} else if (player.getInterfaceManager().containsInterface(105) && player.getTemporaryAttributtes().remove("GEPRICESET") != null) {
				player.getGeManager().modifyPricePerItem(value);
			} else if (player.getInterfaceManager().containsInterface(105) && player.getTemporaryAttributtes().remove("GEQUANTITYSET") != null) {
				player.getGeManager().modifyAmount(value);
			} else if (player.getTemporaryAttributtes().remove("withdrawingPouch") == Boolean.TRUE) {
				if (player.getTemporaryAttributtes().remove(Key.WITHDRAW_PLATINUM_TOKEN) == Boolean.TRUE) 
					player.getMoneyPouch().setPlatinumToken(value, true);
				else
					player.getMoneyPouch().sendDynamicInteraction(value, true, MoneyPouch.TYPE_POUCH_INVENTORY);
			} else if (player.getControlerManager().getControler() != null && player.getTemporaryAttributtes().get(Key.SERVANT_REQUEST_ITEM) != null) {
				Integer type = (Integer) player.getTemporaryAttributtes().remove(Key.SERVANT_REQUEST_TYPE);
				Integer item = (Integer) player.getTemporaryAttributtes().remove(Key.SERVANT_REQUEST_ITEM);
				if (!player.getHouse().isLoaded() || !player.getHouse().getPlayers().contains(player) || type == null || item == null)
					return;
				player.getHouse().getServantInstance().requestType(item, value, type.byteValue());
			} else if (player.getTemporaryAttributtes().remove("xformring") == Boolean.TRUE)
				player.getAppearence().transformIntoNPC(value);
			else if (player.getTemporaryAttributtes().remove(Key.SELL_SPIRIT_SHARDS) != null)
				PetShopOwner.sellShards(player, value);
			else if (player.getTemporaryAttributtes().get("selected_neg") != null) {
				int selectedSkill = (int) player.getTemporaryAttributtes().get("selected_neg");
				int skillLevel = player.getSkills().getLevelForXp(selectedSkill);
				if (value >= skillLevel) {
					if (skillLevel == 1)
						value = 1;
					else
						value = skillLevel - 1;
				} else if (value == 0)
					value = 1;
				int skillOffset = skillLevel - value;
				if (skillOffset <= 0)
					skillOffset = 0;
				int price = SkillAlchemist.calculatePrice(player, skillOffset);
				player.getDialogueManager().finishDialogue();
				if (player.getInventory().getCoinsAmount() < price) {
					player.getDialogueManager().startDialogue("SimpleNPCMessage", 5585, "You need " + price + " amount of coins, in order to reduce your level by " + skillOffset + ".");
					return;
				} else if (player.getEquipment().wearingArmour()) {
					player.getDialogueManager().startDialogue("SimpleNPCMessage", 5585, "Please remove any equipment you have equipped, the tranmutation possibly could damage other metals and fabrics.");
					return;
				} else {
					if (player.getFamiliar() != null)
						player.getFamiliar().dissmissFamiliar(false);
					player.getPrayer().closeAllPrayers();
					player.getSkills().set(selectedSkill, value);
					player.getSkills().setXp(selectedSkill, Skills.getXPForLevel(value));
					player.getAppearence().generateAppearenceData();
					player.getInventory().removeItemMoneyPouch(new Item(995, price));
					player.getDialogueManager().startDialogue("SimpleMessage", "As your coins transmute, you begin feel like your forgetting something...");
					/*if (player.isExtremeDonator())
						player.getPackets().sendGameMessage("You notice that your gold is still the same quantity as before....");*/
				}
			} else if (player.getTemporaryAttributtes().remove("kilnX") != null) {
				int componentId = (Integer) player.getTemporaryAttributtes().get("sc_component");
				if (player.getControlerManager().getControler() instanceof StealingCreationController) {
					StealingCreationController controller = (StealingCreationController) player.getControlerManager().getControler();
					player.getTemporaryAttributtes().put("sc_amount_making", value);
					controller.processKilnExchange(componentId, 50);
				}
			} else if (player.getTemporaryAttributtes().get("sc_request") != null) {
				int requestedId = (int) player.getTemporaryAttributtes().get("sc_request");
				WorldTile tile = (WorldTile) player.getTemporaryAttributtes().get("sc_object");
				if (ItemConfig.forID(requestedId).isStackable()) {
					FloorItem item = World.getRegion(player.getRegionId()).getGroundItem(requestedId, tile, player);
					if (item == null)
						return;
					if (item.getAmount() > value) {
						World.addGroundItem(new Item(requestedId, item.getAmount() - value), tile);
						item.setAmount(value);
					}
					if (player.getControlerManager().canTakeItem(item))
						World.removeGroundItem(player, item);
				} else {
					if (value > 28)
						value = 28;
					for (int i = 0; i < value; i++) {
						FloorItem item = World.getRegion(player.getRegionId()).getGroundItem(requestedId, tile, player);
						if (item == null || !player.getControlerManager().canTakeItem(item))
							break;
						World.removeGroundItem(player, item);
					}
				}
			} else if (player.getTemporaryAttributtes().get("skillId") != null) {
				Integer skill = (Integer) player.getTemporaryAttributtes().remove("skillId");
				player.getDialogueManager().finishDialogue();
				if (value > 99) {
					player.getPackets().sendGameMessage("Please choose a valid level.");
					return;
				}
				player.getSkills().set(skill, value);
				player.getSkills().setXp(skill, Skills.getXPForLevel(value));
				player.getPrayer().closeAllPrayers();
				player.getAppearence().generateAppearenceData();
			} else if (player.getTemporaryAttributtes().get(Key.SET_LEVEL_TARGET) != null) {
				Integer skill = (Integer) player.getTemporaryAttributtes().remove(Key.SET_LEVEL_TARGET);
				player.getSkills().setTarget(skill, value, true);
			} else if (player.getTemporaryAttributtes().get(Key.SET_XP_TARGET) != null) {
				Integer skill = (Integer) player.getTemporaryAttributtes().remove(Key.SET_XP_TARGET);
				player.getSkills().setTarget(skill, value, false);
			}
		} else if (packetId == SWITCH_INTERFACE_COMPONENTS_PACKET) {
			stream.readShortLE128();
			int fromInterfaceHash = stream.readIntV1();
			int toInterfaceHash = stream.readInt();
			int fromSlot = stream.readUnsignedShort();
			int toSlot = stream.readUnsignedShortLE128();
			stream.readUnsignedShortLE();

			int toInterfaceId = toInterfaceHash >> 16;
			int toComponentId = toInterfaceHash - (toInterfaceId << 16);
			int fromInterfaceId = fromInterfaceHash >> 16;
			int fromComponentId = fromInterfaceHash - (fromInterfaceId << 16);

			if (Utils.getInterfaceDefinitionsSize() <= fromInterfaceId || Utils.getInterfaceDefinitionsSize() <= toInterfaceId)
				return;
			if (!player.getInterfaceManager().containsInterface(fromInterfaceId) || !player.getInterfaceManager().containsInterface(toInterfaceId))
				return;
			if (fromComponentId != -1 && Utils.getInterfaceDefinitionsComponentsSize(fromInterfaceId) <= fromComponentId)
				return;
			if (toComponentId != -1 && Utils.getInterfaceDefinitionsComponentsSize(toInterfaceId) <= toComponentId)
				return;
			if (fromInterfaceId == Inventory.INVENTORY_INTERFACE && fromComponentId == 0 && toInterfaceId == Inventory.INVENTORY_INTERFACE && toComponentId == 0) {
				toSlot -= 28;
				if (toSlot < 0 || toSlot >= player.getInventory().getItemsContainerSize() || fromSlot >= player.getInventory().getItemsContainerSize())
					return;
				player.getInventory().switchItem(fromSlot, toSlot);
			} else if (fromInterfaceId == 763 && fromComponentId == 0 && toInterfaceId == 763 && toComponentId == 0) {
				if (toSlot >= player.getInventory().getItemsContainerSize() || fromSlot >= player.getInventory().getItemsContainerSize())
					return;
				player.getInventory().switchItem(fromSlot, toSlot);
			} else if (fromInterfaceId == 762 && toInterfaceId == 762) {
				if (player.getBank().isInsertItems() && toSlot != 65535) {
					player.getBank().insertItem(fromSlot, toSlot, fromComponentId, toComponentId);
				} else
					player.getBank().switchItem(fromSlot, toSlot, fromComponentId, toComponentId);
			} else if (fromInterfaceId == 1265 && toInterfaceId == 1266 && player.getTemporaryAttributtes().get("is_buying") != null) {
				if ((boolean) player.getTemporaryAttributtes().get("is_buying") == true) {
					Shop shop = (Shop) player.getTemporaryAttributtes().get("shop_instance");
					if (shop == null)
						return;
					// shop.buyItem(player, fromSlot, 1);
				}
			} else if (fromInterfaceId == 34 && toInterfaceId == 34)
				player.getNotes().switchNotes(fromSlot, toSlot);
			if (Settings.DEBUG)
				System.out.println("Switch item " + fromInterfaceId + ", " + fromSlot + ", " + toSlot);
		} else if (packetId == GROUND_ITEM_OPTION_EXAMINE) {
			if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead())
				return;
			if (player.isLocked())
				return;
			int y = stream.readUnsignedShort();
			int x = stream.readUnsignedShortLE();
			final int id = stream.readUnsignedShort();
			boolean forceRun = stream.read128Byte() == 1;
			final WorldTile tile = new WorldTile(x, y, player.getPlane());
			final int regionId = tile.getRegionId();
			if (!player.getMapRegionsIds().contains(regionId))
				return;
			final FloorItem item = World.getRegion(regionId).getGroundItem(id, tile, player);
			if (item == null)
				return;
			if (forceRun)
				player.setRun(forceRun);
			player.getPackets().sendTileMessage(0, 15263739, item, ItemExamines.getExamine(item));
			player.getPackets().sendGameMessage(ItemExamines.getExamine(item));
		} else if (packetId == DONE_LOADING_REGION_PACKET) {
			/*
			 * if(!player.clientHasLoadedMapRegion()) { //load objects and items
			 * here player.setClientHasLoadedMapRegion(); }
			 * //player.refreshSpawnedObjects(); //player.refreshSpawnedItems();
			 */
			if (!player.clientHasLoadedMapRegion()) {
				// load objects and items here
				player.setClientHasLoadedMapRegion();
				player.refreshSpawnedObjects();
				player.refreshSpawnedItems();
			}
		} else if (packetId == WALKING_PACKET || packetId == MINI_WALKING_PACKET || packetId == ITEM_GROUND_OPTION_4 || packetId == ITEM_GROUND_OPTION_3 || packetId == PLAYER_OPTION_2_PACKET || packetId == PLAYER_OPTION_3_PACKET || packetId == PLAYER_OPTION_4_PACKET || packetId == PLAYER_OPTION_5_PACKET || packetId == PLAYER_OPTION_6_PACKET || packetId == PLAYER_OPTION_7_PACKET ||packetId == PLAYER_OPTION_9_PACKET || packetId == PLAYER_OPTION_1_PACKET || packetId == ATTACK_NPC || packetId == INTERFACE_ON_PLAYER || packetId == INTERFACE_ON_NPC || packetId == NPC_CLICK1_PACKET || packetId == NPC_CLICK2_PACKET || packetId == NPC_CLICK3_PACKET || packetId == NPC_CLICK4_PACKET || packetId == OBJECT_CLICK1_PACKET || packetId == SWITCH_INTERFACE_COMPONENTS_PACKET || packetId == OBJECT_CLICK2_PACKET || packetId == OBJECT_CLICK3_PACKET || packetId == OBJECT_CLICK4_PACKET || packetId == OBJECT_CLICK5_PACKET || packetId == INTERFACE_ON_OBJECT) {
			if (!player.isRunning())
				return;
			player.addLogicPacketToQueue(new LogicPacket(packetId, length, stream));
		} else if (packetId == OBJECT_EXAMINE_PACKET) {
			ObjectHandler.handleOption(player, stream, -1);
		} else if (packetId == NPC_EXAMINE_PACKET) {
			NPCHandler.handleExamine(player, stream);
		} else if (packetId == JOIN_FRIEND_CHAT_PACKET) {
			if (!player.hasStarted())
				return;
			String str = length == 0 ? null : stream.readString();
			if (str == null)
				FriendsChat.requestLeave(player);
			else
				FriendsChat.requestJoin(player, Utils.formatPlayerNameForDisplay(str));
		} else if (packetId == KICK_FRIEND_CHAT_PACKET) {
			if (!player.hasStarted())
				return;
			if (player.getCurrentFriendsChat() != null)
				player.getCurrentFriendsChat().kickMember(player, Utils.formatPlayerNameForDisplay(stream.readString()));
		} else if (packetId == KICK_CLAN_CHAT_PACKET) {
			if (!player.hasStarted())
				return;
			boolean guest = stream.readByte() == 1;
			if (!guest)
				return;
			stream.readUnsignedShort();
			player.kickPlayerFromClanChannel(stream.readString());
		} else if (packetId == CHANGE_FRIEND_CHAT_PACKET) {
			if (!player.hasStarted() || !player.getInterfaceManager().containsInterface(1108))
				return;
			player.getFriendsIgnores().changeRank(Utils.formatPlayerNameForDisplay(stream.readString()), stream.readUnsignedByte128());
		} else if (packetId == UPDATE_GAMEBAR_PACKET) {
			if (!player.hasStarted())
				return;
			int public_ = stream.readUnsignedByte();
			int private_ = stream.readUnsignedByte();
			int trade = stream.readUnsignedByte();
			if (!player.isLobby()) {
				player.setPublicStatus(public_);
				player.setTradeStatus(trade);
			}
			player.getFriendsIgnores().setPmStatus(private_, true);
		} else if (packetId == ADD_FRIEND_PACKET) {
			if (!player.hasStarted())
				return;
			player.getFriendsIgnores().addFriend(Utils.formatPlayerNameForDisplay(stream.readString()));
		} else if (packetId == REMOVE_FRIEND_PACKET) {
			if (!player.hasStarted())
				return;
			player.getFriendsIgnores().removeFriend(Utils.formatPlayerNameForDisplay(stream.readString()));
		} else if (packetId == ADD_IGNORE_PACKET) {
			if (!player.hasStarted())
				return;
			player.getFriendsIgnores().addIgnore(Utils.formatPlayerNameForDisplay(stream.readString()), stream.readUnsignedByte() == 1);
		} else if (packetId == REMOVE_IGNORE_PACKET) {
			if (!player.hasStarted())
				return;
			player.getFriendsIgnores().removeIgnore(Utils.formatPlayerNameForDisplay(stream.readString()));
		} else if (packetId == SEND_FRIEND_MESSAGE_PACKET) {
			if (!player.hasStarted())
				return;
			String target = stream.readString();
			String message = Huffman.decodeString(150, stream);
			if (message.contains("0hdr2ufufl9ljlzlyla") || message.contains("0hdr") || message.contains("1hdrvx6bk81v7h"))
				return;
			player.getFriendsIgnores().sendPrivateMessage(target, message);
			player.resetAntibot();
		} else if (packetId == SEND_FRIEND_QUICK_CHAT_PACKET) {
			if (!player.hasStarted() || player.isMuted())
				return;
			String target = stream.readString();
			int qcFileId = stream.readUnsignedShort();
			long[] data = null;
			QuickChatOptionDefinition option = QuickChatOptionDefinition.loadOption(qcFileId);
			if (option.dynamicDataTypes != null) {
				data = new long[option.dynamicDataTypes.length];
				for (int i = 0; i < option.dynamicDataTypes.length; i++) {
					if (option.getType(i).clientToServerBytes > 0) {
						data[i] = stream.readDynamic(option.getType(i).clientToServerBytes);
					}

				}
			}
			player.getFriendsIgnores().sendPrivateMessage(target, option, data);
		} else if (packetId == PUBLIC_QUICK_CHAT_PACKET) {
			if (!player.hasStarted()|| player.isMuted())
				return;
			if (player.getLastPublicMessage() > Utils.currentTimeMillis())
				return;
			player.setLastPublicMessage(Utils.currentTimeMillis() + 300);

			int quickChatType = stream.readByte();//quickchat does not use chattype as it's only temporary!!!

			int qcFileId = stream.readUnsignedShort();

			long[] data = null;
			QuickChatOptionDefinition option = QuickChatOptionDefinition.loadOption(qcFileId);
			if (option.dynamicDataTypes != null) {
				data = new long[option.dynamicDataTypes.length];
				for (int i = 0; i < option.dynamicDataTypes.length; i++) {
					if (option.getType(i).clientToServerBytes > 0) {
						data[i] = stream.readDynamic(option.getType(i).clientToServerBytes);
					}

				}
			}
			if (quickChatType == 0)
				player.sendPublicChatMessage(new QuickChatMessage(player, option, data));
			else if (quickChatType == 1) {
				if (player.getCurrentFriendsChat() != null)
					player.getCurrentFriendsChat().sendMessage(player, option, data);
			} else if (quickChatType == 2)
				player.sendClanChannelQuickMessage(new QuickChatMessage(player, option, data));
			else if (quickChatType == 3)
				player.sendGuestClanChannelQuickMessage(new QuickChatMessage(player, option, data));
			else if (Settings.DEBUG)
				Logger.log(this, "Unknown chat type: " + quickChatType);
			player.resetAntibot();
		} else if (packetId == CHAT_TYPE_PACKET) {
			chatType = stream.readUnsignedByte();
		} else if (packetId == CHAT_PACKET) {
			if (!player.hasStarted())
				return;
			if (player.getLastPublicMessage() > Utils.currentTimeMillis())
				return;
			player.setLastPublicMessage(Utils.currentTimeMillis() + 300);
			int colorEffect = stream.readUnsignedByte();
			int moveEffect = stream.readUnsignedByte();
			String message = Huffman.decodeString(200, stream);
			if (message == null || message.replaceAll(" ", "").equals("") || message.length() >= 200)
				return;
			if (message.contains("^^") || message.contains("0hdr2ufufl9ljlzlyla") || message.contains("0hdr") || message.contains("1hdrvx6bk81v7h"))
				return;
			if (message.startsWith("::") || message.startsWith(";;")) {
				// if command exists and processed wont send message as public
				// message
				Commands.processCommand(player, message.replace("::", "").replace(";;", ""), false, false);
				return;
			}
			if (player.isMuted()) {
				LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodePunishmentLengthRequest(player.getUsername()).trim());
				return;
			}
			int effects = (colorEffect << 8) | (moveEffect & 0xff);
			if ((effects & 0x8000) != 0)
				return;//someone trying to crash server using qc as chat effect in normal chat
			if (chatType == 1) {
				if (player.getCurrentFriendsChat() != null)
					player.getCurrentFriendsChat().sendMessage(player, message);
			} else if (chatType == 2)
				player.sendClanChannelMessage(new ChatMessage(message));
			else if (chatType == 3)
				player.sendGuestClanChannelMessage(new ChatMessage(message));
			else {
				//Think i also fixed the large view scene thing, but just incase, spoof message
				if (player.getControlerManager().getControler() instanceof DungeonController) {
					for (Player party : player.getDungManager().getParty().getTeam()) {
						/*if (player.getLocalPlayerUpdate().getLocalPlayers()[party.getIndex()] == null || party.getLocalPlayerUpdate().getLocalPlayers()[player.getIndex()] == null) {
							party.getPackets().sendGameMessage(player.getDisplayName() + ":<col=7fa9ff> " + message);
						}*/
						party.getPackets().sendPublicMessage(player, new PublicChatMessage(message, effects));
					}
				} else
					player.sendPublicChatMessage(new PublicChatMessage(message, effects));
			}
			if (Settings.DEBUG)
				Logger.log(this, "Chat type: " + chatType);
			player.resetAntibot();
		} else if (packetId == COMMANDS_PACKET) {
			if (!player.isRunning())
				return;
			boolean clientCommand = stream.readUnsignedByte() == 1;
			@SuppressWarnings("unused")
			boolean unknown = stream.readUnsignedByte() == 1;
			String command = stream.readString();
			if (!Commands.processCommand(player, command, true, clientCommand) && Settings.DEBUG)
				Logger.log(this, "Command: " + command);
		} else if (packetId == COLOR_ID_PACKET) {
			if (!player.hasStarted())
				return;
			int colorId = stream.readUnsignedShort();
			if (player.getTemporaryAttributtes().get("SkillcapeCustomize") != null)
				SkillCapeCustomizer.handleSkillCapeCustomizerColor(player, colorId);
			else if (player.getTemporaryAttributtes().get("MottifCustomize") != null)
				ClansManager.setMottifColor(player, colorId);
			else if (player.getTemporaryAttributtes().remove(Key.COSTUME_COLOR_CUSTOMIZE) != null)
				SkillCapeCustomizer.handleCostumeColor(player, colorId);
		} else if (packetId == REPORT_ABUSE_PACKET) {
			if (!player.hasStarted())
				return;
			String displayName = stream.readString();
			int type = stream.readUnsignedByte();
			boolean mute = stream.readUnsignedByte() == 1;
			@SuppressWarnings("unused")
			String unknown2 = stream.readString();
			ReportAbuse.report(player, displayName, type, mute);
		} else if (packetId == FORUM_THREAD_ID_PACKET) {
			String threadId = stream.readString();
			if (player.getInterfaceManager().containsInterface(1100))
				ClansManager.setThreadIdInterface(player, threadId);
			else if (Settings.DEBUG)
				Logger.log(this, "Called FORUM_THREAD_ID_PACKET: " + threadId);
		} else if (packetId == OPEN_URL_PACKET) {
			String type = stream.readString();
			String path = stream.readString();
			String unknown = stream.readString();
			int flag = stream.readUnsignedByte();
			if (Settings.DEBUG)
				Logger.log(WorldPacketsDecoder.class, "openUrl(" + type + "," + path + "," + unknown + "," + flag + ")");
			if (type.equals("clan-forum"))
				player.getPackets().sendOpenURL(Settings.SHOWTHREAD_LINK + path.replace("threads.ws?threadid=", ""));
			else if (path.contains("messages"))
				player.getPackets().sendOpenURL(Settings.VOTE_LINK);
			else if (path.contains("set_members_dob") || path.contains("userdetails"))
				player.getPackets().sendOpenURL(Settings.STORE_LINK);
			else if (path.contains("recoveries"))
				player.getPackets().sendOpenURL(Settings.OFFENCES_LINK);
			else if (path.contains("mod=email"))
				player.getPackets().sendOpenURL(Settings.EMAIL_LINK);
			else if (path.contains("title.ws") || path.toLowerCase().contains("squeal"))
				player.getPackets().sendOpenURL(Settings.FORUMS_LINK);
		} else if (packetId == GRAND_EXCHANGE_ITEM_SELECT_PACKET) {
			int itemId = stream.readUnsignedShort();
			if (player.getTemporaryAttributtes().remove(Key.SEARCH_ITEM_DROP) == Boolean.TRUE) 
				NPCDrops.showItem(player, itemId);
			else if (player.getTemporaryAttributtes().get(Key.SPAWN_ITEM) == Boolean.TRUE && player.getControlerManager().getControler() instanceof FfaZone && !player.isCanPvp()) {
				/*if (ItemConfig.forID(itemId).isDungItem()) {
					player.getPackets().sendGameMessage("You can not spawn dungeoneering items!");
					return;
				}
				if (ItemConfig.forID(itemId).isSCItem()) {
					player.getPackets().sendGameMessage("You can not spawn stealing creation items!");
					return;
				}
				if (ItemConfig.forID(itemId).isCustomItem()) {
					player.getPackets().sendGameMessage("You can not spawn custom items!");
					return;
				}*/
				if (!Commands.canSpawnItem(player, itemId, 1))
					return;
				player.getTemporaryAttributtes().put(Key.SPAWN_ITEM, itemId);
				player.getPackets().sendInputIntegerScript("Select amount:");
				Bot.sendLog(Bot.COMMAND_CHANNEL, "[type=COMMAND][name=" + player.getUsername() + "][message=::item" + itemId + "]");
			}
			else
				player.getGeManager().chooseItem(itemId);
		} else {
			if (Settings.DEBUG)
				Logger.log(this, "Missing packet " + packetId + ", expected size: " + length + ", actual size: " + PACKET_SIZES[packetId]);
		}
	}
}
