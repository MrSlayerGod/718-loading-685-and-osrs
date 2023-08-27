package com.rs.net.encoders;

import java.util.Arrays;
import java.util.Collections;

import com.rs.Settings;
import com.rs.game.Animation;
import com.rs.game.DynamicRegion;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.FloorItem;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.item.ItemsContainerNew;
import com.rs.game.npc.NPC;
import com.rs.game.player.ChatMessage;
import com.rs.game.player.HintIcon;
import com.rs.game.player.Player;
import com.rs.game.player.PublicChatMessage;
import com.rs.game.player.QuickChatMessage;
import com.rs.game.player.content.clans.ClansManager;
import com.rs.game.player.content.custom.CustomNPCs;
import com.rs.game.player.content.grandExchange.Offer;
import com.rs.io.OutputStream;
import com.rs.login.WorldInformation;
import com.rs.net.Session;
import com.rs.utils.MapArchiveKeys;
import com.rs.utils.Utils;
import com.rs.utils.huffman.Huffman;

import io.netty.channel.ChannelFuture;

public class WorldPacketsEncoder extends Encoder {

	private Player player;

	public WorldPacketsEncoder(Session session, Player player) {
		super(session);
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

	public void sendMinimapFlag(int x, int y) {
		OutputStream stream = new OutputStream(3);
		stream.writePacket(player, 13);
		stream.writeByte128(y);
		stream.writeByte128(x);
		session.write(stream);
	}

	public void sendResetMinimapFlag() {
		sendMinimapFlag(255, 255);
	}

	public void sendPing() {
		OutputStream packet = new OutputStream(1);
		packet.writePacket(player, 153);
		session.write(packet);
	}

	public void sendWorldList(int clientChecksum, int[] online) {
		int lowestWorldId = Integer.MAX_VALUE;
		int highestWorldId = Integer.MIN_VALUE;
		int ourChecksum = Settings.WORLDS_INFORMATION.length;
		for (int i = 0; i < Settings.WORLDS_INFORMATION.length; i++) {
			WorldInformation world = Settings.WORLDS_INFORMATION[i];
			ourChecksum += (ourChecksum * world.hashCode());
			if (world.getId() < lowestWorldId)
				lowestWorldId = world.getId();
			if (world.getId() > highestWorldId)
				highestWorldId = world.getId();
		}

		OutputStream packet = new OutputStream();
		packet.writePacketVarShort(player, 11);
		packet.writeByte(1); // 0 - buffer only, 1 - parse
		packet.writeByte(2); // list version
		packet.writeByte(ourChecksum != clientChecksum ? 1 : 0);
		if (ourChecksum != clientChecksum) {
			packet.writeSmart(Settings.WORLDS_INFORMATION.length); // number of locations
			for (int i = 0; i < Settings.WORLDS_INFORMATION.length; i++) {
				WorldInformation world = Settings.WORLDS_INFORMATION[i];
				packet.writeSmart(world.getCountryFlagID());
				packet.writeVersionedString(world.getCountryName());

			}

			packet.writeSmart(lowestWorldId);
			packet.writeSmart(highestWorldId);
			packet.writeSmart(Settings.WORLDS_INFORMATION.length); // number of worlds
			for (int i = 0; i < Settings.WORLDS_INFORMATION.length; i++) {
				WorldInformation world = Settings.WORLDS_INFORMATION[i];
				packet.writeSmart(world.getId() - lowestWorldId);
				packet.writeByte(i); // world location index
				packet.writeInt(world.getFlags());
				packet.writeVersionedString(world.getActivity());
				packet.writeVersionedString(world.getIp());
			}
			packet.writeInt(ourChecksum);
		}

		for (int i = 0; i < Settings.WORLDS_INFORMATION.length; i++) {
			WorldInformation world = Settings.WORLDS_INFORMATION[i];
			packet.writeSmart(world.getId() - lowestWorldId);
			if (online[i] == -1)
				packet.writeShort(65535);
			else
				packet.writeShort(online[i]);
		}

		packet.endPacketVarShort();
		session.write(packet);
	}

	public void sendPlayerUnderNPCPriority(boolean priority) {
		OutputStream stream = new OutputStream(2);
		stream.writePacket(player, 6);
		stream.writeByteC(priority ? 1 : 0);
		session.write(stream);
	}
	
	public void sendPlayerAttackOptionPriority(boolean priority) {
		OutputStream stream = new OutputStream(2);
		stream.writePacket(player, 147);
		stream.writeByteC(priority ? 0 : 1);
		session.write(stream);
	}

	public void sendHintIcon(HintIcon icon) {
		OutputStream stream = new OutputStream(15);
		stream.writePacket(player, 79);
		stream.writeByte((icon.getTargetType() & 0x1f) | (icon.getIndex() << 5));
		if (icon.getTargetType() == 0)
			stream.skip(13);
		else {
			stream.writeByte(icon.getArrowType());
			if (icon.getTargetType() == 1 || icon.getTargetType() == 10) {
				stream.writeShort(icon.getTargetIndex());
				stream.writeShort(2500); // how often the arrow flashes, 2500
				// ideal, 0 never
				stream.skip(4);
			} else if ((icon.getTargetType() >= 2 && icon.getTargetType() <= 6)) { // directions
				stream.writeByte(icon.getPlane()); // unknown
				stream.writeShort(icon.getCoordX());
				stream.writeShort(icon.getCoordY());
				stream.writeByte(icon.getDistanceFromFloor() * 4 >> 2);
				stream.writeShort(-1); // distance to start showing on minimap,
				// 0 doesnt show, -1 infinite
			}
			stream.writeInt(icon.getModelId());
		}
		session.write(stream);

	}

	public void sendCameraShake(int slotId, int b, int c, int d, int e) {
		OutputStream stream = new OutputStream(7);
		stream.writePacket(player, 44);
		stream.writeByte128(b);
		stream.writeByte128(slotId);
		stream.writeByte128(d);
		stream.writeByte128(c);
		stream.writeShortLE(e);
		session.write(stream);
	}

	public void sendStopCameraShake() {
		OutputStream stream = new OutputStream(1);
		stream.writePacket(player, 131);
		session.write(stream);
	}

	public void sendIComponentScrollSizeHeight(int interfaceId, int containerId, int scrollbarcontainerId, int height, boolean addToHeight) {
		sendExecuteScript(1208, new Object[] {addToHeight == true ? 1 : 0, height, interfaceId << 16 | containerId, interfaceId << 16 | scrollbarcontainerId});
	}

	public void sendIComponentModel(int interfaceId, int componentId, int modelId) {
		OutputStream stream = new OutputStream(9);
		stream.writePacket(player, 102);
		stream.writeIntV1(modelId);
		stream.writeIntV1(interfaceId << 16 | componentId);
		session.write(stream);
	}

	public void sendGrandExchangeOffer(Offer offer) {
		OutputStream stream = new OutputStream(21);
		stream.writePacket(player, 53);
		stream.writeByte(offer.getSlot());
		stream.writeByte(offer.getStage());
		if (offer.forceRemove())
			stream.skip(18);
		else {
			stream.writeShort(offer.getId());
			stream.writeInt(offer.getPrice());
			stream.writeInt(offer.getAmount());
			stream.writeInt(offer.getTotalAmmountSoFar());
			stream.writeInt(offer.getTotalPriceSoFar());
		}
		session.write(stream);
	}

	public void sendIComponentSprite(int interfaceId, int componentId, int spriteId) {
		OutputStream stream = new OutputStream(11);
		stream.writePacket(player, 121);
		stream.writeInt(spriteId);
		stream.writeIntV2(interfaceId << 16 | componentId);
		session.write(stream);
	}

	public void sendHideIComponent(int interfaceId, int componentId, boolean hidden) {
		OutputStream stream = new OutputStream(6);
		stream.writePacket(player, 112);
		stream.writeIntV2(interfaceId << 16 | componentId);
		stream.writeByte(hidden ? 1 : 0);
		session.write(stream);
	}

	public void sendRemoveGroundItem(FloorItem item) {
		OutputStream stream = createChunkInSceneStream(item.getTile());
		stream.writePacket(player, 108);
		stream.writeShortLE(item.getId());
		stream.write128Byte((item.getTile().getXInChunk() << 4) | item.getTile().getYInChunk());
		session.write(stream);

	}

	public void sendGroundItem(FloorItem item) {
		OutputStream stream = createChunkInSceneStream(item.getTile());
		stream.writePacket(player, 125);
		stream.writeByte128((item.getTile().getXInChunk() << 4) | item.getTile().getYInChunk());
		stream.writeShortLE128(Math.min(item.getAmount(), 65535));
		stream.writeShortLE(item.getId());
		session.write(stream);
	}

	@Deprecated
	public void sendProjectile(Entity receiver, WorldTile startTile, WorldTile endTile, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset, int creatorSize) {
		/*fuck the idiot who made this refactoring
		 * OutputStream stream = createWorldTileStream(startTile);
		stream.writePacket(player, 20);
		int localX = startTile.getLocalX(player.getLastLoadedMapRegionTile(), player.getMapSize());
		int localY = startTile.getLocalY(player.getLastLoadedMapRegionTile(), player.getMapSize());
		int offsetX = localX - ((localX >> 3) << 3);
		int offsetY = localY - ((localY >> 3) << 3);
		stream.writeByte((offsetX << 3) | offsetY);
		stream.writeByte(endTile.getX() - startTile.getX());
		stream.writeByte(endTile.getY() - startTile.getY());
		stream.writeShort(receiver == null ? 0 : (receiver instanceof Player ? -(receiver.getIndex() + 1) : receiver.getIndex() + 1));
		stream.writeShort(gfxId);
		stream.writeByte(startHeight);
		stream.writeByte(endHeight);
		stream.writeShort(delay);
		int duration = (Utils.getDistance(startTile.getX(), startTile.getY(), endTile.getX(), endTile.getY()) * 30 / ((speed / 10) < 1 ? 1 : (speed / 10))) + delay;
		stream.writeShort(duration);
		stream.writeByte(curve);
		stream.writeShort(creatorSize * 64 + startDistanceOffset * 64);
		session.write(stream);*/

		sendProjectileProper(startTile, creatorSize, creatorSize, endTile, receiver != null ? receiver.getSize() : 1, receiver != null ? receiver.getSize() : 1, receiver, gfxId, startHeight, endHeight, delay, 
				
				
				Math.max(1, Utils.getProjectileTime(startTile, endTile, startHeight, endHeight, speed / 10, delay, curve, startDistanceOffset) / 20)
				/*(Utils.getDistance(startTile.getX(), startTile.getY(), endTile.getX(), endTile.getY()) * 30 / ((speed / 10) < 1 ? 1 : (speed / 10)))1*/, startDistanceOffset, curve);

	}

	/*
	 * 	public void sendProjectile(Projectile projectile) {
		Tile src = projectile.getSrcTile();
		Tile to = projectile.getDestTile();
		sendTile(src);
		player.getOutStream().createFrame(117);
		player.getOutStream().writeByte(0); //0 since chunkpacket is edited.
		player.getOutStream().writeByte(to.getX() - src.getX() + (projectile.getTarget().getSize() / 2));
		player.getOutStream().writeByte(to.getY() - src.getY() + (projectile.getTarget().getSize() / 2));
		player.getOutStream().writeWord(projectile.getTarget() == null ? 0 : (projectile.getTarget() instanceof Client ? -(projectile.getTarget().getIndex() + 1) : projectile.getTarget().getIndex() + 1));
		player.getOutStream().writeWord(projectile.getID());
		player.getOutStream().writeByte(projectile.getStartHeight());
		player.getOutStream().writeByte(projectile.getEndHeight());
		player.getOutStream().writeWord(projectile.getStartDelay());
		player.getOutStream().writeWord(projectile.getDelay());
		player.getOutStream().writeByte(projectile.getAngle());
		player.getOutStream().writeByte((int) ((64 * projectile.getSource().getSize()) + (32 * projectile.getStartDistance())));
		player.flushOutStream();
	}
	 */
	
	
	public void sendProjectileProper(WorldTile from, int fromSizeX, int fromSizeY, WorldTile to, int toSizeX, int toSizeY, Entity lockOn, int gfxId, int startHeight, int endHeight, int delay, int speed, int slope, int angle) {
		WorldTile src = new WorldTile(from.getX() + fromSizeX/2, from.getY() + fromSizeY/2, from.getPlane());
//		WorldTile dst = new WorldTile(from.getX() + fromSizeX/2, from.getY() + fromSizeY/2, to.getPlane());
		OutputStream stream = createChunkInSceneStream(src);
		stream.writePacket(player, 20);
		stream.writeByte((src.getXInChunk() << 3) | src.getYInChunk());
		stream.writeByte(to.getX() - src.getX() + toSizeX/2);
		stream.writeByte(to.getY() - src.getY() + toSizeY/2);
		stream.writeShort(lockOn == null ? 0 : (lockOn instanceof Player ? -(lockOn.getIndex() + 1) : lockOn.getIndex() + 1));
		stream.writeShort(player.isOldNPCLooks() && from instanceof NPC ? CustomNPCs.getGFX(((NPC)from).getId(), gfxId) : gfxId);
		stream.writeByte(startHeight);
		stream.writeByte(endHeight);
		stream.writeShort(delay);
		stream.writeShort(/*delay + */speed);
		stream.writeByte(angle);
		stream.writeShort(slope);
		session.write(stream);
	}

	public void sendUnlockIComponentOptionSlots(int interfaceId, int componentId, int fromSlot, int toSlot, int... optionsSlots) {
		int settingsHash = 0;
		for (int slot : optionsSlots)
			settingsHash |= 2 << slot;
		sendIComponentSettings(interfaceId, componentId, fromSlot, toSlot, settingsHash);
	}

	public void sendUnlockIComponentOptionSlots(int interfaceId, int componentId, int fromSlot, int toSlot, boolean unlockEvent, int... optionsSlots) {
		int settingsHash = unlockEvent ? 1 : 0;
		for (int slot : optionsSlots)
			settingsHash |= 2 << slot;
		sendIComponentSettings(interfaceId, componentId, fromSlot, toSlot, settingsHash);
	}

	public void sendIComponentSettings(int interfaceId, int componentId, int fromSlot, int toSlot, int settingsHash) {
		OutputStream stream = new OutputStream(13);
		stream.writePacket(player, 40);
		stream.writeIntV2(settingsHash);
		stream.writeInt(interfaceId << 16 | componentId);
		stream.writeShort128(fromSlot);
		stream.writeShortLE(toSlot);
		session.write(stream);
	}

	public void sendInterFlashScript(int interfaceId, int componentId, int width, int height, int slot) {
		Object[] parameters = new Object[4];
		int index = 0;
		parameters[index++] = slot;
		parameters[index++] = height;
		parameters[index++] = width;
		parameters[index++] = interfaceId << 16 | componentId;
		sendExecuteScript(143, parameters);
	}

	public void sendInterSetItemsOptionsScript(int interfaceId, int componentId, int key, int width, int height, String... options) {
		sendInterSetItemsOptionsScript(interfaceId, componentId, key, false, width, height, options);
	}

	public void sendInterSetItemsOptionsScript(int interfaceId, int componentId, int key, boolean negativeKey, int width, int height, String... options) {
		Object[] parameters = new Object[6 + options.length];
		int index = 0;
		for (int count = options.length - 1; count >= 0; count--)
			parameters[index++] = options[count];
		parameters[index++] = -1; // dunno but always this
		parameters[index++] = 0;// dunno but always this, maybe startslot?
		parameters[index++] = height;
		parameters[index++] = width;
		parameters[index++] = key;
		parameters[index++] = interfaceId << 16 | componentId;
		sendExecuteScript(negativeKey ? 695 : 150, parameters); // scriptid 150 does
		// that the method
		// name says*/
	}

	public void sendPouchInfusionOptionsScript(boolean dung, int interfaceId, int componentId, int slotLength, int width, int height, String... options) {
		Object[] parameters = new Object[5 + options.length];
		int index = 0;
		if (dung) {
			parameters[index++] = 1159;
			parameters[index++] = 1100;
		} else {
			parameters[index++] = slotLength;
			parameters[index++] = 1;
		}
		parameters[index++] = height;
		parameters[index++] = width;
		parameters[index++] = interfaceId << 16 | componentId;
		for (int count = options.length - 1; count >= 0; count--)
			parameters[index++] = options[count];
		sendExecuteScript(757, parameters);
	}

	public void sendScrollInfusionOptionsScript(boolean dung, int interfaceId, int componentId, int slotLength, int width, int height, String... options) {
		Object[] parameters = new Object[5 + options.length];
		int index = 0;
		if (dung) {
			parameters[index++] = 1159;
			parameters[index++] = 1100;
		} else {
			parameters[index++] = slotLength;
			parameters[index++] = 1;
		}
		parameters[index++] = height;
		parameters[index++] = width;
		parameters[index++] = interfaceId << 16 | componentId;
		for (int count = options.length - 1; count >= 0; count--)
			parameters[index++] = options[count];
		sendExecuteScript(763, parameters);
	}

	public void sendInputNameScript(String message) {
		sendExecuteScript(109, new Object[]
		{ message });
	}

	public void sendInputIntegerScript(String message) {
		sendExecuteScript(108, new Object[]
		{ message });
	}

	public void sendInputLongTextScript(String message) {
		sendExecuteScript(110, new Object[]
		{ message });
	}

	public void sendExecuteScriptReverse(int scriptId, Object... params) {
		Collections.reverse(Arrays.asList(params));
		sendExecuteScript(scriptId, params);
	}
	
	public void sendExecuteScript(int scriptId, Object... params) { // who was the idiot, who made this script send parameters in reverse order?!?!?!?!?!?!?!?!?!?!?!??!?!?!?!?!?!?!?!? 
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 119);
		String parameterTypes = "";
		if (params != null) {
			for (int count = params.length - 1; count >= 0; count--) {
				if (params[count] instanceof String)
					parameterTypes += "s"; // string
				else
					parameterTypes += "i"; // integer
			}
		}
		stream.writeString(parameterTypes);
		if (params != null) {
			int index = 0;
			for (int count = parameterTypes.length() - 1; count >= 0; count--) {
				if (parameterTypes.charAt(count) == 's')
					stream.writeString((String) params[index++]);
				else
					stream.writeInt((Integer) params[index++]);
			}
		}
		stream.writeInt(scriptId);
		stream.endPacketVarShort();
		session.write(stream);
	}

	public void sendCSVarInteger(int id, int value) {
		if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE)
			sendCSVarInteger2(id, value);
		else
			sendCSVarInteger1(id, value);
	}

	public void sendClientState(int id) {
		OutputStream stream = new OutputStream(3);
		stream.writePacket(player, 150);
		stream.writeShort(id);
		session.write(stream);
	}
	
	public void sendCSVarInteger1(int id, int value) {
		OutputStream stream = new OutputStream(4);
		stream.writePacket(player, 154);
		stream.writeByteC(value);
		stream.writeShort128(id);
		session.write(stream);
	}

	public void sendCSVarInteger2(int id, int value) {
		OutputStream stream = new OutputStream(7);
		stream.writePacket(player, 63);
		stream.writeShort128(id);
		stream.writeInt(value);
		session.write(stream);
	}

	@Deprecated
	public void sendVar(int id, int value) {
		if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE)
			sendVar2(id, value);
		else
			sendVar1(id, value);
	}

	@Deprecated
	public void sendVarBit(int id, int value) {
		if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE)
			sendVarBit2(id, value);
		else
			sendVarBit1(id, value);
	}

	public void sendVar1(int id, int value) {
		OutputStream stream = new OutputStream(4);
		stream.writePacket(player, 110);
		stream.writeShortLE128(id);
		stream.writeByte128(value);
		session.write(stream);
	}

	public void sendVar2(int id, int value) {
		OutputStream stream = new OutputStream(7);
		stream.writePacket(player, 56);
		stream.writeShort128(id);
		stream.writeIntLE(value);
		session.write(stream);
	}

	public void sendVarBit1(int id, int value) {
		OutputStream stream = new OutputStream(4);
		stream.writePacket(player, 111);
		stream.writeShort128(id);
		stream.writeByteC(value);
		session.write(stream);
	}

	public void sendVarBit2(int id, int value) {
		OutputStream stream = new OutputStream(7);
		stream.writePacket(player, 81);
		stream.writeIntV1(value);
		stream.writeShort128(id);
		session.write(stream);
	}

	public void sendRunEnergy() {
		OutputStream stream = new OutputStream(2);
		stream.writePacket(player, 25);
		stream.writeByte(player.getRunEnergy());
		session.write(stream);
	}
	
	public void sendWeight() {
		OutputStream stream = new OutputStream(3);
		stream.writePacket(player, 98);
		stream.writeShort((int) player.getWeight());
		session.write(stream);
	}

	public void sendIComponentText(int interfaceId, int componentId, String text) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 135);
		stream.writeString(text);
		stream.writeInt(interfaceId << 16 | componentId);
		stream.endPacketVarShort();
		session.write(stream);

	}

	public void sendIComponentAnimation(int emoteId, int interfaceId, int componentId) {
		OutputStream stream = new OutputStream(9);
		stream.writePacket(player, 103);
		stream.writeIntV2(emoteId);
		stream.writeInt(interfaceId << 16 | componentId);
		session.write(stream);

	}

	public void sendItemOnIComponent(int interfaceid, int componentId, int id, int amount) {
		OutputStream stream = new OutputStream(11);
		stream.writePacket(player, 152);
		stream.writeShort128(id);
		stream.writeIntV1(amount);
		stream.writeIntV2(interfaceid << 16 | componentId);
		session.write(stream);

	}

	public void sendEntityOnIComponent(boolean isPlayer, int entityId, int interfaceId, int componentId) {
		if (isPlayer)
			sendPlayerOnIComponent(interfaceId, componentId);
		else
			sendNPCOnIComponent(interfaceId, componentId, entityId);
	}

	private OutputStream createChunkInSceneStream(WorldTile tile) {
		OutputStream stream = new OutputStream(4);
		stream.writePacket(player, 158);
		stream.writeByte128(tile.getChunkYInScene(player));
		stream.writeByteC(tile.getPlane());
		stream.write128Byte(tile.getChunkXInScene(player));
		return stream;
	}

	public void sendObjectAnimation(WorldObject object, Animation animation) {
		OutputStream stream = new OutputStream(10);
		stream.writePacket(player, 76);
		stream.writeInt(animation.getIds()[0]);
		stream.writeByteC((object.getType() << 2) + (object.getRotation() & 0x3));
		stream.writeIntLE(object.getTileHash());
		session.write(stream);
	}

	public void sendTileMessage(String message, WorldTile tile, int color) {
		sendTileMessage(message, tile, 5000, 255, color);
	}

	public void sendTileMessage(String message, WorldTile tile, int delay, int height, int color) {
		OutputStream stream = createChunkInSceneStream(tile);
		stream.writePacketVarByte(player, 107);
		stream.skip(1);
		stream.writeByte((tile.getXInChunk() << 4) | tile.getYInChunk());
		stream.writeShort(delay / 30);
		stream.writeByte(height);
		stream.write24BitInteger(color);
		stream.writeString(message);
		stream.endPacketVarByte();
		session.write(stream);
	}

	public void sendAddObject(WorldObject object) {
		OutputStream stream = createChunkInSceneStream(object);
		stream.writePacket(player, 120);
		stream.writeByte((object.getXInChunk() << 4) | object.getYInChunk());
		stream.writeByte((object.getType() << 2) + (object.getRotation() & 0x3));
		stream.writeIntLE(object.getId());
		session.write(stream);

	}

	public void sendRemoveObject(WorldObject object) {
		OutputStream stream = createChunkInSceneStream(object);
		stream.writePacket(player, 45);
		stream.writeByteC((object.getType() << 2) + (object.getRotation() & 0x3));
		stream.writeByte128((object.getXInChunk() << 4) | object.getYInChunk());
		session.write(stream);

	}

	public void sendPlayerOnIComponent(int interfaceId, int componentId) {
		OutputStream stream = new OutputStream(5);
		stream.writePacket(player, 23);
		stream.writeIntV2(interfaceId << 16 | componentId);
		session.write(stream);

	}

	public void sendNPCOnIComponent(int interfaceId, int componentId, int npcId) {
		OutputStream stream = new OutputStream(9);
		stream.writePacket(player, 31);
		stream.writeInt(npcId);
		stream.writeInt(interfaceId << 16 | componentId);
		session.write(stream);

	}

	public void sendRandomOnIComponent(int interfaceId, int componentId, int id) {
		/*
		 * OutputStream stream = new OutputStream(); stream.writePacket(player,
		 * 235); stream.writeShort(id); stream.writeIntV1(interfaceId << 16 |
		 * componentId); stream.writeShort(interPacketsCount++);
		 * session.write(stream);
		 */
	}

	public void sendFaceOnIComponent(int interfaceId, int componentId, int look1, int look2, int look3) {
		/*
		 * OutputStream stream = new OutputStream(); stream.writePacket(player,
		 * 192); stream.writeIntV2(interfaceId << 16 | componentId);
		 * stream.writeShortLE128(interPacketsCount++);
		 * stream.writeShortLE128(look1); stream.writeShortLE128(look2);
		 * stream.writeShort128(look2); session.write(stream);
		 */
	}

	public void sendEmptyFriendsChatChannel() {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 117);
		stream.endPacketVarShort();
		session.write(stream);
	}

	public OutputStream startFriendsChatChannel(String owner, String name, int kickReq, int membersCount) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 117);
		stream.writeString(owner);
		stream.writeByte(0); // not sending extra name string which is not used anywhere in client
		stream.writeLong(Utils.stringToLong(name));
		stream.writeByte(kickReq);
		stream.writeByte(membersCount);
		return stream;
	}

	public void appendFriendsChatMember(OutputStream stream, String displayName, int worldId, int rank, String worldName) {
		stream.writeString(displayName);
		stream.writeByte(0);  // no secondary name (this sync's with second and 4th string in ignore list)
		stream.writeShort(worldId);
		stream.writeByte(rank);
		stream.writeString(worldName);
	}

	public void endFriendsChatChannel(OutputStream stream) {
		stream.endPacketVarShort();
		session.write(stream);
	}

	public void sendClanChannel(ClansManager manager, boolean myClan) {
		OutputStream stream = new OutputStream(manager == null ? 4 : manager.getClanChannelDataBlock().length + 4);
		stream.writePacketVarShort(player, 85);
		stream.writeByte(myClan ? 1 : 0);
		if (manager != null)
			stream.writeBytes(manager.getClanChannelDataBlock());
		stream.endPacketVarShort();
		session.write(stream);
	}

	public void sendClanSettings(ClansManager manager, boolean myClan) {
		OutputStream stream = new OutputStream(manager == null ? 4 : manager.getClanSettingsDataBlock().length + 4);
		stream.writePacketVarShort(player, 133);
		stream.writeByte(myClan ? 1 : 0);
		if (manager != null)
			stream.writeBytes(manager.getClanSettingsDataBlock());
		stream.endPacketVarShort();
		session.write(stream);
	}

	public void sendPlainIgnore(boolean isUpdate, String displayName, String previousDisplayName) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 128);
		stream.writeByte(isUpdate ? 0x1 : 0x0);
		appendIgnore(stream, displayName, previousDisplayName);
		stream.endPacketVarByte();
		session.write(stream);
	}

	public OutputStream startIgnoresPacket() {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 55);
		stream.writeByte(0);
		return stream;
	}

	public void appendIgnore(OutputStream stream, String displayName, String previousDisplayName) {
		// TODO find out real meaning of values
		stream.writeString(displayName);
		stream.writeString("");
		stream.writeString(previousDisplayName == null ? "" : previousDisplayName);
		stream.writeString("");
	}

	public void endIgnoresPacket(OutputStream stream, int ignoresCount) {
		int offset = stream.getOffset();
		stream.setOffset(3);
		stream.writeByte(ignoresCount);
		stream.setOffset(offset);
		stream.endPacketVarShort();
		session.write(stream);
	}

	public OutputStream startFriendsPacket() {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 2);
		return stream;
	}

	public void appendFriend(OutputStream stream, boolean isStatusUpdate, String displayName, String previousDisplayName, int world, int fcRank, String worldName) {
		stream.writeByte(isStatusUpdate ? 0 : 1);
		stream.writeString(displayName);
		stream.writeString(previousDisplayName == null ? "" : previousDisplayName);
		stream.writeShort(world);
		stream.writeByte(fcRank);
		stream.writeByte(0);
		if (world > 0) {
			stream.writeString(worldName);
			stream.writeByte(0);
		}
	}

	public void endFriendsPacket(OutputStream stream) {
		stream.endPacketVarShort();
		session.write(stream);
	}

	public void sendPrivateMessageNew(String target, String message) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 130);
		stream.writeString(target);
		Huffman.encodeString(stream, message);
		stream.endPacketVarShort();
		session.write(stream);
	}

	public void sendPrivateMessageNew(String target, int qcFileId, byte[] qcData) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 30);
		stream.writeString(target);
		stream.writeShort(qcFileId);
		if (qcData != null)
			stream.writeBytes(qcData);
		stream.endPacketVarByte();
		session.write(stream);
	}

	public void receivePrivateMessageNew(String target, long messageUid, int iconId, String message) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 105);
		stream.writeByte(0); // no secondary name (this sync's with second and 4th string in ignore list)
		stream.writeString(target);
		for (int bitpos = 0; bitpos < 40; bitpos += 8)
			stream.writeByte((int) (messageUid >> bitpos));
		stream.writeByte(iconId);
		Huffman.encodeString(stream, message);
		stream.endPacketVarShort();
		session.write(stream);
	}

	public void receivePrivateMessageNew(String target, long messageUid, int iconId, int qcFileId, byte[] qcData) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 104);
		stream.writeByte(0); // no secondary name (this sync's with second and 4th string in ignore list)
		stream.writeString(target);
		for (int bitpos = 0; bitpos < 40; bitpos += 8)
			stream.writeByte((int) (messageUid >> bitpos));
		stream.writeByte(iconId);
		stream.writeShort(qcFileId);
		if (qcData != null)
			stream.writeBytes(qcData);
		stream.endPacketVarByte();
		session.write(stream);
	}

	public void sendPrivateMessage(String username, ChatMessage message) {
		if(player.connectedThroughVPN || getPlayer().hasNewPlayerController())
			return;
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 130);
		stream.writeString(username);
		Huffman.encodeString(stream, message.getMessage(player.isFilteringProfanity()));
		stream.endPacketVarShort();
		session.write(stream);
	}

	public void receivePrivateMessage(String name, String display, int rights, ChatMessage message) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 105);
		stream.writeByte(name.equals(display) ? 0 : 1);
		stream.writeString(display);
		if (!name.equals(display))
			stream.writeString(name);
		for (int i = 0; i < 5; i++)
			stream.writeByte(Utils.getRandom(255));
		stream.writeByte(rights);
		Huffman.encodeString(stream, message.getMessage(player.isFilteringProfanity()));
		stream.endPacketVarShort();
		session.write(stream);
	}

	public void receivePrivateChatQuickMessage(Player sender, String display, int rights, QuickChatMessage message) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 104);
		stream.writeByte(sender.getUsername().equals(display) ? 0 : 1);
		stream.writeString(display);
		if (!sender.getUsername().equals(display))
			stream.writeString(sender.getUsername());
		for (int i = 0; i < 5; i++)
			stream.writeByte(Utils.getRandom(255));
		stream.writeByte(rights);
		stream.writeShort(message.getDefinition().id);
		stream.writeBytes(message.getEncoded());
		stream.endPacketVarByte();
		session.write(stream);
	}

	public void sendPrivateQuickMessageMessage(String username, QuickChatMessage message) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 30);
		stream.writeString(username);
		stream.writeShort(message.getDefinition().id);
		stream.writeBytes(message.getEncoded());
		stream.endPacketVarByte();
		session.write(stream);
	}

	public void sendGameBarStages() {
		player.getVarsManager().sendVar(1054, player.getClanStatus());
		player.getVarsManager().sendVar(1055, player.getAssistStatus());
		player.getVarsManager().sendVarBit(6161, player.isFilterGame() ? 1 : 0);
		player.getVarsManager().sendVar(2159, player.getFriendsChatStatus());
		sendOtherGameBarStages();
	}

	public void sendOtherGameBarStages() {
		OutputStream stream = new OutputStream(3);
		stream.writePacket(player, 89);
		stream.write128Byte(player.getTradeStatus());
		stream.writeByte(player.getPublicStatus());
		session.write(stream);
	}

	public void sendPmStatus() {
		OutputStream stream = new OutputStream(2);
		stream.writePacket(player, 75);
		stream.writeByte(player.getFriendsIgnores().getPmStatus());
		session.write(stream);
	}

	// 131 clan chat quick message

	public void receiveClanChatMessage(boolean myClan, String display, int rights, ChatMessage message) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 3);
		stream.writeByte(myClan ? 1 : 0);
		stream.writeString(display);
		for (int i = 0; i < 5; i++)
			stream.writeByte(Utils.getRandom(255));
		stream.writeByte(rights);
		Huffman.encodeString(stream, message.getMessage(player.isFilteringProfanity()));
		stream.endPacketVarByte();
		session.write(stream);
	}

	public void receiveClanChatQuickMessage(boolean myClan, String display, int rights, QuickChatMessage message) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 1);
		stream.writeByte(myClan ? 1 : 0);
		stream.writeString(display);
		for (int i = 0; i < 5; i++)
			stream.writeByte(Utils.getRandom(255));
		stream.writeByte(rights);
		stream.writeShort(message.getDefinition().id);
		stream.writeBytes(message.getEncoded());
		stream.endPacketVarByte();
		session.write(stream);
	}

	public void receiveFriendChatMessage(String name, String chatName, long messageUid, int iconId, String message) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 139);
		stream.writeByte(0); // no secondary name  (this sync's with second and 4th string in ignore list)
		stream.writeString(name);
		stream.writeLong(Utils.stringToLong(chatName));
		for (int bitpos = 0; bitpos < 40; bitpos += 8)
			stream.writeByte((int) (messageUid >> bitpos));
		stream.writeByte(iconId);
		Huffman.encodeString(stream, message);
		stream.endPacketVarByte();
		session.write(stream);
	}

	public void receiveFriendChatMessage(String name, String chatName, long messageUid, int iconId, int qcFileId, byte[] qcData) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 32);
		stream.writeByte(0); // no secondary name  (this sync's with second and 4th string in ignore list)
		stream.writeString(name);
		stream.writeLong(Utils.stringToLong(chatName));
		for (int bitpos = 0; bitpos < 40; bitpos += 8)
			stream.writeByte((int) (messageUid >> bitpos));
		stream.writeByte(iconId);
		stream.writeShort(qcFileId);
		if (qcData != null)
			stream.writeBytes(qcData);
		stream.endPacketVarByte();
		session.write(stream);
	}

	/*
	 * useless, sending friends unlocks it
	 */
	public void sendUnlockIgnoreList() {
		OutputStream stream = new OutputStream(1);
		stream.writePacket(player, 18);
		session.write(stream);
	}

	/*
	 * dynamic map region
	 */
	public void sendDynamicGameScene(boolean sendLswp) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 144);
		if (sendLswp) // exists on newer protocol, sends all player encoded
			// region ids, afterwards new pupdate protocol is
			// regionbased
			player.getLocalPlayerUpdate().init(stream);
		int middleChunkX = player.getChunkX();
		int middleChunkY = player.getChunkY();
		stream.write128Byte(2); // exists on newer protocol, triggers a
		// gamescene supporting npcs
		stream.writeShortLE(middleChunkY);
		stream.writeShortLE128(middleChunkX);
		stream.write128Byte(player.isForceNextMapLoadRefresh() ? 1 : 0);
		stream.writeByteC(player.getMapSize());
		stream.initBitAccess();
		/*
		 * cene length in chunks. scene tiles length / 16, 8 is a chunk size, 16
		 * because the code behind its signed and goes from middle-length to
		 * middle+length
		 */
		int sceneLength = Settings.MAP_SIZES[player.getMapSize()] >> 4;
		// the regionids(maps files) that will be used to load this scene
		int[] regionIds = new int[4 * sceneLength * sceneLength];
		int newRegionIdsCount = 0;
		for (int plane = 0; plane < 4; plane++) {
			for (int realChunkX = (middleChunkX - sceneLength); realChunkX <= ((middleChunkX + sceneLength)); realChunkX++) {
				int regionX = realChunkX / 8;
				y: for (int realChunkY = (middleChunkY - sceneLength); realChunkY <= ((middleChunkY + sceneLength)); realChunkY++) {
					int regionY = realChunkY / 8;
					// rcx / 8 = rx, rcy / 8 = ry, regionid is encoded region x
					// and y
					int regionId = (regionX << 8) + regionY;
					Region region = World.getRegions().get(regionId);
					int newChunkX;
					int newChunkY;
					int newPlane;
					int rotation;
					if (region instanceof DynamicRegion) { // generated map
						DynamicRegion dynamicRegion = (DynamicRegion) region;
						int[] pallete = dynamicRegion.getRegionCoords()[plane][realChunkX - (regionX * 8)][realChunkY - (regionY * 8)];
						newChunkX = pallete[0];
						newChunkY = pallete[1];
						newPlane = pallete[2];
						rotation = pallete[3];
					} else { // real map
						newChunkX = realChunkX;
						newChunkY = realChunkY;
						newPlane = plane;
						rotation = 0;// no rotation
					}
					// invalid chunk, not built chunk
					if (newChunkX == 0 || newChunkY == 0)
						stream.writeBits(1, 0);
					else {
						stream.writeBits(1, 1);
						// chunk encoding = (x << 14) | (y << 3) | (plane <<
						// 24), theres addition of two more bits for rotation
						stream.writeBits(26, (rotation << 1) | (newPlane << 24) | (newChunkX << 14) | (newChunkY << 3));
						int newRegionId = (((newChunkX / 8) << 8) + (newChunkY / 8));
						for (int index = 0; index < newRegionIdsCount; index++)
							if (regionIds[index] == newRegionId)
								continue y;
						regionIds[newRegionIdsCount++] = newRegionId;
					}

				}
			}
		}
		stream.finishBitAccess();
		for (int index = 0; index < newRegionIdsCount; index++) {
			int[] xteas = MapArchiveKeys.getMapKeys(regionIds[index]);
			if (xteas == null)
				xteas = new int[4];
			for (int keyIndex = 0; keyIndex < 4; keyIndex++)
				stream.writeInt(xteas[keyIndex]);
		}
		stream.endPacketVarShort();
		session.write(stream);
	}

	/*
	 * normal map region
	 */
	public void sendGameScene(boolean sendLswp) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 42);
		if (sendLswp)
			player.getLocalPlayerUpdate().init(stream);
		stream.writeByteC(player.getMapSize());
		stream.writeByte(player.isForceNextMapLoadRefresh() ? 1 : 0);
		stream.writeShort(player.getChunkX());
		stream.writeShort(player.getChunkY());
		for (int regionId : player.getMapRegionsIds()) {
			int[] xteas = MapArchiveKeys.getMapKeys(regionId);
			if (xteas == null)
				xteas = new int[4];
			for (int index = 0; index < 4; index++)
				stream.writeInt(xteas[index]);
		}
		stream.endPacketVarShort();
		session.write(stream);
	}

	public void sendCutscene(int id) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 142);
		stream.writeShort(id);
		stream.writeShort(20); // xteas count
		for (int count = 0; count < 20; count++)
			// xteas
			for (int i = 0; i < 4; i++)
				stream.writeInt(0);
		byte[] appearence = player.getAppearence().getAppeareanceData();
		stream.writeByte(appearence.length);
		stream.writeBytes(appearence);
		stream.endPacketVarShort();
		session.write(stream);
	}

	/*
	 * sets the pane interface
	 */
	public void sendRootInterface(int id, int type) {
		int[] xteas = new int[4];
		player.getInterfaceManager().setWindowsPane(id);
		OutputStream stream = new OutputStream(4);
		stream.writePacket(player, 39);
		stream.write128Byte(type);
		stream.writeShort128(id);
		stream.writeIntLE(xteas[1]);
		stream.writeIntV2(xteas[0]);
		stream.writeInt(xteas[3]);
		stream.writeInt(xteas[2]);
		session.write(stream);
	}

	public void sendPlayerOption(String option, int slot, boolean top) {
		sendPlayerOption(option, slot, top, -1);
	}

	public void sendPublicMessage(Player p, PublicChatMessage message) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 106);
		stream.writeShort(p.getIndex());
		stream.writeShort(message.getEffects());
		stream.writeByte(p.getMessageIcon());
		if (message instanceof QuickChatMessage) {
			QuickChatMessage qcMessage = (QuickChatMessage) message;
			stream.writeShort(qcMessage.getDefinition().id);
			stream.writeBytes(qcMessage.getEncoded());

		} else {
			Huffman.encodeString(stream, message.getMessage(player.isFilteringProfanity()));
		}
		stream.endPacketVarByte();
		session.write(stream);
	}

	public void sendPlayerOption(String option, int slot, boolean top, int cursor) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 118);
		stream.writeByte128(slot);
		stream.writeString(option);
		stream.writeShortLE128(cursor);
		stream.writeByteC(top ? 1 : 0);
		stream.endPacketVarByte();
		session.write(stream);
	}

	/*
	 * sends local players update
	 */
	public void sendLocalPlayersUpdate() {
		session.write(player.getLocalPlayerUpdate().createPacketAndProcess());
	}

	/*
	 * sends local npcs update
	 */
	public void sendLocalNPCsUpdate() {
		session.write(player.getLocalNPCUpdate().createPacketAndProcess());
	}

	public void sendGraphics(Graphics graphics, Object target) {
		OutputStream stream = new OutputStream(13);
		int hash = 0;
		if (target instanceof Player) {
			Player p = (Player) target;
			hash = p.getIndex() & 0xffff | 1 << 28;
		} else if (target instanceof NPC) {
			NPC n = (NPC) target;
			hash = n.getIndex() & 0xffff | 1 << 29;
		} else {
			WorldTile tile = (WorldTile) target;
			hash = tile.getPlane() << 28 | tile.getX() << 14 | tile.getY() & 0x3fff | 1 << 30;
		}
		stream.writePacket(player, 90);
		stream.writeShort(graphics.getId());
		stream.writeByte128(0); // slot id used for entitys
		stream.writeShort(graphics.getSpeed());
		stream.writeByte128(graphics.getSettings2Hash());
		stream.writeShort(graphics.getHeight());
		stream.writeIntLE(hash);
		session.write(stream);
	}

	public void sendDelayedGraphics(Graphics graphics, int delay, WorldTile tile) {

	}

	public void sendNPCInterface(NPC npc, boolean nocliped, int windowId, int windowComponentId, int interfaceId) {
		int[] xteas = new int[4];
		OutputStream stream = new OutputStream(26);
		stream.writePacket(player, 57);
		stream.writeIntV2(xteas[0]);
		stream.writeShortLE128(npc.getIndex());
		stream.writeByte128(nocliped ? 1 : 0);
		stream.writeInt(xteas[3]);
		stream.writeShortLE128(interfaceId);
		stream.writeIntLE(xteas[2]);
		stream.writeIntV2(xteas[1]);
		stream.writeIntV1(windowId << 16 | windowComponentId);
		session.write(stream);
	}

	public void sendObjectInterface(WorldObject object, boolean nocliped, int windowId, int windowComponentId, int interfaceId) {
		int[] xteas = new int[4];
		OutputStream stream = new OutputStream(33);
		stream.writePacket(player, 143);
		stream.writeIntV2(xteas[1]);
		stream.writeByte(nocliped ? 1 : 0);
		stream.writeIntLE(xteas[2]);
		stream.writeIntV1(object.getId());
		stream.writeByte128((object.getType() << 2) | (object.getRotation() & 0x3));
		stream.writeInt((object.getPlane() << 28) | (object.getX() << 14) | object.getY()); // the
		// hash
		// for
		// coords,
		stream.writeIntV2((windowId << 16) | windowComponentId);
		stream.writeShort(interfaceId);
		stream.writeInt(xteas[3]);
		stream.writeInt(xteas[0]);
		session.write(stream);
	}
	
	public void sendWorldTileInterface(FloorItem item, boolean nocliped, int windowId, int windowComponentId, int interfaceId) {
		int[] xteas = new int[4];
		OutputStream stream = new OutputStream(29);
		stream.writePacket(player, 36);
		stream.write128Byte(nocliped ? 1 : 0);
		stream.writeIntV2(xteas[1]);
		stream.writeIntLE(windowId << 16 | windowComponentId);
		stream.writeIntV1(xteas[0]);
		stream.writeShort(item.getId());//maybe item id?
		stream.writeIntV1(xteas[3]);
		stream.writeInt(xteas[2]);
		stream.writeShortLE(interfaceId);
		WorldTile tile = item.getTile();
		stream.writeInt(((tile.getPlane() << 28) | (tile.getX() << 14) | tile.getY()));
		session.write(stream);
	}

	public void closeInterface(int parentUID) {
		OutputStream stream = new OutputStream(5);
		stream.writePacket(player, 5);
		stream.writeIntLE(parentUID);
		session.write(stream);
	}

	public void sendInterface(boolean clickThrought, int parentUID, int interfaceId) {
		int[] xteas = new int[4];
		OutputStream stream = new OutputStream(24);
		stream.writePacket(player, 14);
		stream.writeShort(interfaceId);
		stream.writeInt(xteas[0]);
		stream.writeIntV2(xteas[1]);
		stream.writeIntV1(parentUID);
		stream.writeByte(clickThrought ? 1 : 0);
		stream.writeIntV1(xteas[3]);
		stream.writeIntV2(xteas[2]);
		session.write(stream);
	}

	public void sendSystemUpdate(int delay, boolean isLobby) {
		OutputStream stream = new OutputStream(3);
		stream.writePacket(player, 141);
		if (isLobby)
			stream.writeShort(delay * 20);
		else
			stream.writeShort((int) (delay / 0.6)); //x2 correct.
		session.write(stream);
	}

	public void sendUpdateItems(int key, ItemsContainer<Item> items, int... slots) {
		sendUpdateItems(key, items.getItems(), slots);
	}

	public void sendUpdateItems(int key, Item[] items, int... slots) {
		sendUpdateItems(key, key < 0, items, slots);
	}

	public void sendUpdateItems(int key, boolean negativeKey, Item[] items, int... slots) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 138);
		stream.writeShort(key);
		stream.writeByte(negativeKey ? 1 : 0);
		for (int slotId : slots) {
			if (slotId >= items.length)
				continue;
			stream.writeSmart(slotId);
			int id = -1;
			int amount = 0;
			Item item = items[slotId];
			if (item != null) {
				id = item.getId();
				amount = item.getAmount();
			}
			stream.writeShort(id + 1);
			if (id != -1) {
				stream.writeByte(amount >= 255 ? 255 : amount);
				if (amount >= 255)
					stream.writeInt(amount);
			}
		}
		stream.endPacketVarShort();
		session.write(stream);
	}

	public void sendCSVarString(int id, String string) {
		OutputStream stream = new OutputStream();
		if (string.length() >= 253) {
			stream.writePacketVarShort(player, 34);
			stream.writeString(string);
			stream.writeShort(id);
			stream.endPacketVarShort();
		} else {
			stream.writePacketVarByte(player, 134);
			stream.writeShort(id);
			stream.writeString(string);
			stream.endPacketVarByte();
		}
		session.write(stream);
	}

	public void sendItemsContainer(int key, ItemsContainerNew container) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 77);
		stream.writeShort(key);
		stream.writeByte(key < 0 ? 1 : 0);
		stream.writeShort(container.getSize());
		for (int i = 0; i < container.getSize(); i++) {
			int id = -1;
			int amount = 0;
			Item item = container.get(i);
			if (item != null) {
				id = item.getId();
				amount = item.getAmount();
			}
			stream.writeShortLE128(id + 1);
			stream.writeByte128(amount >= 255 ? 255 : amount);
			if (amount >= 255)
				stream.writeIntV1(amount);
		}
		stream.endPacketVarShort();
		session.write(stream);
	}

	public void sendUpdateItemsContainer(int key, ItemsContainerNew container) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 138);
		stream.writeShort(key);
		stream.writeByte(key < 0 ? 1 : 0);
		for (int i = 0; i < container.getSize(); i++) {
			if (!container.needsUpdating(i))
				continue;
			stream.writeSmart(i);
			int id = -1;
			int amount = 0;
			Item item = container.get(i);
			if (item != null) {
				id = item.getId();
				amount = item.getAmount();
			}
			stream.writeShort(id + 1);
			if (id != -1) {
				stream.writeByte(amount >= 255 ? 255 : amount);
				if (amount >= 255)
					stream.writeInt(amount);
			}
		}
		stream.endPacketVarShort();
		session.write(stream);
	}

	public void sendItems(int key, ItemsContainer<Item> items) {
		sendItems(key, key < 0, items);
	}

	public void sendItems(int key, boolean negativeKey, ItemsContainer<Item> items) {
		sendItems(key, negativeKey, items.getItems());
	}

	public void sendItems(int key, Item[] items) {
		sendItems(key, key < 0, items);
	}

	public void sendItems(int key, boolean negativeKey, Item[] items) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 77);
		stream.writeShort(key); // negativeKey ? -key : key
		stream.writeByte(negativeKey ? 1 : 0);
		stream.writeShort(items.length);
		for (int index = 0; index < items.length; index++) {
			Item item = items[index];
			int id = -1;
			int amount = 0;
			if (item != null) {
				id = item.getId();
				amount = item.getAmount();
			}
			stream.writeShortLE128(id + 1);
			stream.writeByte128(amount >= 255 ? 255 : amount);
			if (amount >= 255)
				stream.writeIntV1(amount);
		}
		stream.endPacketVarShort();
		session.write(stream);
	}

	public ChannelFuture sendLogout(boolean lobby) {
		OutputStream stream = new OutputStream();
		stream.writePacket(player, lobby ? 59 : 60);
		return session.write(stream);
	}

	public void sendInventoryMessage(int border, int slotId, String message) {
		sendGameMessage(message);
		sendExecuteScript(948, border, slotId, message);
	}

	public void sendNPCMessage(int border, int color, NPC npc, String message) {
		sendGameMessage(message);
		sendCSVarString(306, message);
		sendCSVarInteger(1699, color);
		sendCSVarInteger(1700, border);
		sendCSVarInteger(1695, 1);
		sendNPCInterface(npc, true, 746, 0, 1177);
	}

	public void sendObjectMessage(int border, int color, WorldObject object, String message) {
		sendGameMessage(message);
		sendCSVarString(306, message);
		sendCSVarInteger(1699, color);
		sendCSVarInteger(1700, border);
		sendCSVarInteger(1695, 1);
		sendObjectInterface(object, true, 746, 0, 1177);
	}
	
	public void sendTileMessage(int border, int color, FloorItem fItem, String message) {
		//sendGameMessage(message);
		sendCSVarString(306, message);
		sendCSVarInteger(1699, color);
		sendCSVarInteger(1700, border);
		sendCSVarInteger(1695, 1);
		sendWorldTileInterface(fItem, true, 746, 0, 1177);
	}
	
	public void sendGameMessage(String text) {
		sendGameMessage(text, false);
	}

	public void sendGameMessage(String text, boolean filter) {
		sendMessage(filter ? 109 : 0, text, null);
	}

	public void sendPanelBoxMessage(String text) {
		sendMessage(player.getRights() == 2 ? 99 : 0, text, null);
	}

	public void sendTradeRequestMessage(Player p) {
		sendMessage(100, "wishes to trade with you.", p);
	}
	
	public void sendPartnerRequestMessage(Player p) {
		sendMessage(100, "wishes to partner with you.", p);
	}

	public void sendClanWarsRequestMessage(Player p) {
		sendMessage(101, "wishes to challenge your clan to a clan war.", p);
	}

	public void sendClanInviteMessage(Player p) {
		sendMessage(117, p.getDisplayName() + " is inviting you to join their clan.", p);
	}

	public void sendDuelChallengeRequestMessage(Player p, boolean friendly) {
		sendMessage(101, "wishes to duel with you(" + (friendly ? "friendly" : "stake") + ").", p);
	}

	public void sendGambleChallengeRequestMessage(Player p, int amount) {
		sendMessage(101, "wishes to gamble with you. ("+Utils.getFormattedNumber(amount)+" coins)", p);
	}

	public void sendDungeonneringRequestMessage(Player p) {
		sendMessage(111, "has invited you to a dungeon party.", p);
	}

	public void sendMessage(int type, String text, Player p) {
		int maskData = 0;
		if (p != null) {
			maskData |= 0x1;
			maskData |= 0x2;
		}
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 136);
		stream.writeSmart(type);
		stream.writeInt(player.getTileHash()); // junk, not used by client
		stream.writeByte(maskData);
		if ((maskData & 0x1) != 0) {
			stream.writeString(Utils.formatPlayerNameForDisplay(p.getDisplayName()));
			stream.writeString(p.getDisplayName());
		}
		if (text.length() > 200)
			text = text.substring(0, 200);
		stream.writeString(text);
		stream.endPacketVarByte();
		session.write(stream);
	}

	// effect type 1 or 2(index4 or index14 format, index15 format unusused by
	// jagex for now)
	public void sendSound(int id, int delay, int effectType) {
		if (effectType == 1)
			sendIndex14Sound(id, delay);
		else if (effectType == 2)
			sendIndex15Sound(id, delay);
	}

	public void sendVoice(int id) {
		resetSounds();
		sendSound(id, 0, 2);
	}

	public void resetSounds() {
		OutputStream stream = new OutputStream(1);
		stream.writePacket(player, 145);
		session.write(stream);
	}

	public void sendIndex14Sound(int id, int delay) {
		OutputStream stream = new OutputStream(9);
		stream.writePacket(player, 26);
		stream.writeShort(id);
		stream.writeByte(1);// repeated amount
		stream.writeShort(delay);
		stream.writeByte(255);
		stream.writeShort(256);
		session.write(stream);
	}

	public void sendIndex15Sound(int id, int delay) {
		OutputStream stream = new OutputStream(7);
		stream.writePacket(player, 70);
		stream.writeShort(id);
		stream.writeByte(1); // amt of times it repeats
		stream.writeShort(delay);
		stream.writeByte(255); // volume
		session.write(stream);
	}

	public void sendMusicEffect(int id) {
		sendMusicEffect(id, 0, 255);
	}

	public void sendMusicEffect(int id, int delay, int volume) {
		OutputStream stream = new OutputStream(7);
		stream.writePacket(player, 9);
		stream.write128Byte(volume); // volume
		stream.write24BitIntegerV2(delay);
		stream.writeShort(id);
		session.write(stream);
	}

	public void sendMusic(int id) {
		sendMusic(id, 100, 255);
	}

	public void sendMusic(int id, int delay, int volume) {
		OutputStream stream = new OutputStream(5);
		stream.writePacket(player, 129);
		stream.writeByte(delay);
		stream.writeShortLE128(id);
		stream.writeByte128(volume);
		session.write(stream);
	}

	public void sendSkillLevel(int skill) {
		OutputStream stream = new OutputStream(7);
		stream.writePacket(player, 146);
		stream.write128Byte(skill);
		stream.writeInt((int) player.getSkills().getXp(skill));
		
		int level = player.getSkills().getLevel(skill);
		
		if (player.isVirtualLevels() && level == player.getSkills().getLevelForXp(skill))
			level = player.getSkills().getLevelForXp(skill, 150);
		
		stream.writeByte128(level);
		session.write(stream);
	}

	// CUTSCENE PACKETS START

	/**
	 * This will blackout specified area.
	 * 
	 * @param byte area = area which will be blackout (0 = unblackout; 1 =
	 *        blackout orb; 2 = blackout map; 5 = blackout orb and map)
	 */
	public void sendBlackOut(int area) {
		OutputStream out = new OutputStream(2);
		out.writePacket(player, 69);
		out.writeByte(area);
		session.write(out);
	}

	// instant
	public void sendCameraLook(int viewLocalX, int viewLocalY, int viewZ) {
		sendCameraLook(viewLocalX, viewLocalY, viewZ, -1, -1);
	}

	public void sendCameraLook(int viewLocalX, int viewLocalY, int viewZ, int speed1, int speed2) {
		OutputStream stream = new OutputStream(7);
		stream.writePacket(player, 116);
		stream.writeByte128(viewLocalY);
		stream.writeByte(speed1);
		stream.writeByteC(viewLocalX);
		stream.writeByte(speed2);
		stream.writeShort128(viewZ >> 2);
		session.write(stream);
	}

	public void sendResetCamera() {
		OutputStream stream = new OutputStream(1);
		stream.writePacket(player, 95);
		session.write(stream);
	}

	public void sendCameraRotation(int x, int y) {
		OutputStream stream = new OutputStream(5);
		stream.writePacket(player, 123);
		stream.writeShort(x);
		stream.writeShortLE(y);
		session.write(stream);
	}

	public void sendCameraPos(int moveLocalX, int moveLocalY, int moveZ) {
		sendCameraPos(moveLocalX, moveLocalY, moveZ, -1, -1);
	}

	public void sendClientConsoleCommand(String command) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 61);
		stream.writeString(command);
		stream.endPacketVarByte();
	}

	public void sendOpenURL(String url) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 17);
		stream.writeByte(0);
		stream.writeString(url);
		stream.endPacketVarShort();
		session.write(stream);
	}

	public void sendSetMouse(String walkHereReplace, int cursor) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 10);
		stream.writeString(walkHereReplace);
		stream.writeShort(cursor);
		stream.endPacketVarByte();
		session.write(stream);
	}
	
	public void sendGroundItemNames() {
		sendExecuteScript(-12, player.isDisableGroundItemNames() ? 0 : 1);
	}

	public void sendHideAttackOption() {
		sendExecuteScript(-11, player.isHideAttackOption() ? 1 : 0);
	}
	
	public void sendVirtualLevels() {
		sendExecuteScript(-2, player.isVirtualLevels() ? 1 : 0);
	}
	
	public void sendHitLook() {
		sendExecuteScript(-5, player.isOldHitLook() ? 1 : 0);
	}
	
	public void sendNPCLooks() {
		sendExecuteScript(-19, player.isOldNPCLooks() ? 1 : 0);
	}
	
	public void resetLocalNPCUpdate() {
		sendExecuteScript(-20);
		player.getLocalNPCUpdate().reset();
	}
	
	
	public void sendItemsLook() {
		// currently disabled
		OutputStream stream = new OutputStream(2);
		stream.writePacket(player, 159);
		stream.writeByte(player.isOldItemsLook() ? 1 : 0);
		session.write(stream);
	}

	public void sendCameraPos(int moveLocalX, int moveLocalY, int moveZ, int speed1, int speed2) {
		OutputStream stream = new OutputStream(7);
		stream.writePacket(player, 74);
		stream.writeByte128(speed2);
		stream.writeByte128(speed1);
		stream.writeByte(moveLocalY);
		stream.writeShort(moveZ >> 2);
		stream.writeByte(moveLocalX);
		session.write(stream);
	}

	public void sendLogReq(String data) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 132);
		stream.writeInt(data.hashCode());
		stream.writeInt(data.length());
		stream.endPacketVarShort();
		session.write(stream);
	}
	
	public void sendHitboxName() {
		Entity lastTarget = player.getLastTarget();
		sendExecuteScript(-4, lastTarget == null ? "None" : lastTarget.getName());
	}
	
	public void setWidgetSize(int id, int componentID, int x, int y) {
		sendExecuteScript(-6, id << 16 | componentID, x, y);
	}
	
	//time in miliseconds
	public void setTimer(int type, int id, int timeMS) {
		if (player.isDisablePotionTimersPlugin())
			return;
		sendExecuteScript(-7, type, id, timeMS);
	}
	
	public void resetTimers() {
		sendExecuteScript(-8);
	}
	
	public void setShopPrices(Object... items) {
		sendExecuteScript(-9, items);
	}
	
	public void sendNotification(String title, String text) {
		sendExecuteScript(-10, title, text);
	}
	
	public void sendRefreshHitbox() {
		Entity lastTarget = player.getLastTarget();
		if (lastTarget == null)
			sendExecuteScript(-3, 0);
		else
			sendExecuteScript(-3, lastTarget.getHitpoints(), lastTarget.getMaxHitpoints());
	}

}
