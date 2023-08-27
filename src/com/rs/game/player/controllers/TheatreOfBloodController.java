package com.rs.game.player.controllers;

import com.rs.Settings;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.map.MapInstance.Stages;
import com.rs.game.npc.NPC;
import com.rs.game.npc.theatreOfBlood.TOBBoss;
import com.rs.game.npc.theatreOfBlood.verzikVitur.VerzikVitur;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.raids.TheatreOfBlood;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.WorldPacketsDecoder;
import com.rs.utils.Utils;

/**
 * @author Alex (Dragonkk)
 *
 */
public class TheatreOfBloodController extends Controller {

	private TheatreOfBlood raid;
	private int timer;
	private TOBBoss currentBoss;

	@Override
	public void start() {
		setup(false);
	}

	public void setup(boolean login) {
		if (getArguments() == null || getArguments().length < 2) {
			removeControler();
			return;
		}
		String key = (String) getArguments()[0];
		WorldTile tile = (WorldTile) getArguments()[1];
		if (key == null || tile == null) { // shouldnt happen
			removeControler();
			return;
		}
		TheatreOfBlood raid = TheatreOfBlood.getRaid(key);
		if (raid == null) { // shouldnt happen
			removeControler();
			return;
		}
		this.raid = raid;
		if (login)
			player.useStairs(-1, tile, 0, 2);
		sendInterfaces();
	}

	public void setHPBar(TOBBoss boss) {
		if (currentBoss != boss) {
			currentBoss = boss;
			setBossHPBar();
		}
	}

	private void processHPBar() {
		if (currentBoss == null)
			return;
		if (currentBoss.hasFinished()) {
			currentBoss = null;
			setBossHPBar();
			return;
		}
		refreshHPBar();
	}

	// nicolas
	public void setHPBar(int hp, int maxHP) {
		player.getPackets().sendCSVarInteger(1923, (int) ((double) (maxHP - hp) * (double) 7500 / (double) maxHP));
		player.getPackets().sendHideIComponent(1285, 3, false);
	}

	private void refreshHPBar() { // set hp
		if (currentBoss == null)
			return;
		player.getPackets().sendCSVarInteger(1923,
				(int) ((double) (currentBoss.getMaxHitpoints() - currentBoss.getHitpoints()) * (double) 7500
						/ (double) currentBoss.getMaxHitpoints()));
	}

	public void setBossHPBar() {
		player.getPackets().sendHideIComponent(1285, 3, currentBoss == null);
	}

	@Override
	public void sendInterfaces() {
		player.getInterfaceManager().setOverlay(1285, true);
		setBossHPBar();
		for (int i = 0; i < 3; i++)
			player.getPackets().sendHideIComponent(1285, i, true);
		player.getPackets().sendHideIComponent(1285, 4, true);
		refreshHPBar();
	}

	@Override
	public boolean login() {
		setup(true);
		if (raid != null)
			raid.add(player, true);
		return raid == null;
	}

	@Override
	public boolean logout() {
		if (!isRunning())
			return true;
		WorldTile tile = new WorldTile(player);
		raid.remove(player, TheatreOfBlood.LOGOUT);
		if (getArguments() == null || getArguments().length < 2 || !raid.hasStarted())
			return true;
		getArguments()[1] = tile;
		return false;
	}

	@Override
	public void process() {
		if (!isRunning() || player.isLocked())
			return;
		processHPBar();
		timer++;
		if (timer % 100 == 0)
			raid.playMusic(player); // so that music doesnt get replaced
		// means kicked or left fc
		if (timer > 10 && (player.getCurrentFriendsChat() == null
				|| !player.getCurrentFriendsChat().getChannel().equalsIgnoreCase(raid.getFCOwner())))
			raid.remove(player, TheatreOfBlood.LEAVE);
		else {
			if (raid.getSotetseg() != null && !raid.getSotetseg().hasFinished()
					&& raid.getSotetseg().isHiddenDimension()) {

				WorldTile minHiddenD = raid.getTile(26, 85, 3);
				WorldTile maxHiddenD = raid.getTile(39, 106, 3);

				WorldTile minHiddenP = raid.getTile(26, 87, 3);
				WorldTile maxHiddenP = raid.getTile(39, 101, 3);

				WorldTile minP = raid.getTile(73, 86);
				WorldTile maxP = raid.getTile(86, 100);

				// private static final Boundary HIDDEN_DIMENSION_PUZZLE = new Boundary(3354,
				// 3367, 4311, 4325);
				// private static final Boundary HIDDEN_DIMENSION_BOUNDARY = new Boundary(3354,
				// 3367, 4309, 4330);
				// private static final Boundary SOTETSEG_PUZZLE = new Boundary(3273, 3286,
				// 4310, 4324);
				if (player.getX() >= minHiddenD.getX() && player.getY() >= minHiddenD.getY()
						&& player.getX() <= maxHiddenD.getX() && player.getY() <= maxHiddenD.getY()) {
					if (player.getX() >= minHiddenP.getX() && player.getY() >= minHiddenP.getY()
							&& player.getX() <= maxHiddenP.getX() && player.getY() <= maxHiddenP.getY()) {
						if (player.getY() >= minHiddenP.getY() + 4 /*4314*/ && !raid.getSotetseg().hasVortex())
							raid.getSotetseg().setHasVortex();
						raid.getSotetseg().setHighlight(new WorldTile(player.getX() + 47, player.getY() - 1, 0));
					}
					player.applyHit(new Hit(raid.getSotetseg(), 30, HitLook.REGULAR_DAMAGE));
				}
				if (player.getX() >= minP.getX() && player.getY() >= minP.getY() && player.getX() <= maxP.getX()
						&& player.getY() <= maxP.getY()) {
					if (!raid.getSotetseg()
							.isSafe(new WorldTile(player.getX() - 47, player.getY() + 1, player.getPlane() + 3)))
						player.applyHit(new Hit(raid.getSotetseg(), Utils.random(100) + 150, HitLook.REGULAR_DAMAGE));
				}
			}
		}
	}

	@Override
	public boolean processNPCClick1(NPC npc) {
		if (!isRunning())
			return false;
		if (npc instanceof VerzikVitur) {
			player.getDialogueManager().startDialogue("VerzikViturD", npc);
			return false;
		}
		return true;
	}

	@Override
	public boolean processObjectClick1(WorldObject object) {
		if (!isRunning())
			return false;
		if (object.getId() == 132996) {
			player.lock();
			FadingScreen.fade(player, 0, new Runnable() {
				@Override
				public void run() {
					player.getPackets().sendGameMessage("<col=16711680>You fought well");
					raid.remove(player, TheatreOfBlood.LEAVE);
				}
			});
			return false;
		 } else if (object.getId() == 133037) {
			 /*if (raid.getSotetseg() != null)
	            raid.getSotetseg().clearObjects();*/
	         player.setNextWorldTile(raid.getTile(80, 101)); //(3273, 4325));
	         return false;
		} else if (object.getId() == 133016 || object.getId() == 132992) {
			raid.lootChest(player, object.getId() == 133016);
			return false;
		} else if (object.getId() == 132741) {
			if (!player.getInventory().hasFreeSlots()) {
				player.getPackets().sendGameMessage("Not enough space in your inventory.");
				return false;
			}
			World.spawnObject(new WorldObject(132742, object.getType(), object.getRotation(), object));
			player.getInventory().addItem(52516, 1);
			player.getPackets()
					.sendGameMessage("You find the Dawnbringer; you feel a pulse of energy burst through it.");
			return false;
		} else if (object.getDefinitions().name.equalsIgnoreCase("barrier")
				&& object.getDefinitions().containsOption(0, "pass")) {
			if (!raid.hasStarted()) {
				if (!raid.getFCOwner().equalsIgnoreCase(player.getUsername())) {
					player.getDialogueManager().startDialogue("SimpleMessage", "Only your leader can start this raid.");
					return false;
				}
				raid.start();
			}
			raid.passBarrier(player, object);
			return false;
		} else if (object.getId() == 132738 || (object.getDefinitions().containsOption(0, "enter")
				&& object.getDefinitions().containsOption(1, "quick-enter"))) {
			raid.enterRoom(player, false);
			return false;
		}
		return true;
	}

	@Override
	public boolean processObjectClick2(WorldObject object) {
		if (object.getDefinitions().containsOption(0, "enter")
				&& object.getDefinitions().containsOption(1, "quick-enter")) {
			raid.enterRoom(player, true);
			return false;
		}
		return true;
	}

	public TheatreOfBlood getRaid() {
		return raid;
	}

	@Override
	public void moved() {
		if (!Settings.HOSTED) {
			int x = player.getX() - (raid.getInstancePos()[0] * 8);
			int y = player.getY() - (raid.getInstancePos()[1] * 8);
			System.out.println(x + ", " + y);
		}
	}

	public boolean isRunning() {
		return raid != null && raid.getStage() == Stages.RUNNING;
	}

	public void magicTeleported(int type) {
		if (!isRunning())
			return;
		raid.remove(player, TheatreOfBlood.TELEPORT);
	}

	public boolean processMagicTeleport(WorldTile toTile) {
		player.getPackets().sendGameMessage("A mysterious force prevents you from teleporting.");
		return false;
	}

	/**
	 * return can teleport
	 */
	public boolean processItemTeleport(WorldTile toTile) {
		player.getPackets().sendGameMessage("A mysterious force prevents you from teleporting.");
		return false;
	}

	/**
	 * return can teleport
	 */
	/*
	 * public boolean processObjectTeleport(WorldTile toTile) { player.getPackets().
	 * sendGameMessage("A mysterious force prevents you from teleporting."); return
	 * false; }
	 */
	public void forceRemove() {

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
					player.reset();
					player.setNextAnimation(new Animation(-1));
					// raid.remove(player, ChamberOfXeric.LEAVE);
					if (isRunning())
						raid.jail(player, false);
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
	public boolean processButtonClick(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		if (!isRunning())
			return false;
		switch (interfaceId) {
		case 1284:
			switch (componentId) {
			case 8:
				int space = player.getBank().addItems(raid.getRewards(player).toArray(), false);
				if (space == 0) {
					player.getPackets().sendGameMessage("Not enough space in your bank.");
					break;
				}
				raid.getRewards(player).clear();
				player.getPackets().sendGameMessage("All the items were moved to your bank.");
				break;
			case 9:
				raid.getRewards(player).clear();
				player.getPackets().sendGameMessage("All the items were removed from the chest.");
				break;
			case 10:
				for (int slot = 0; slot < raid.getRewards(player).toArray().length; slot++) {
					Item item = raid.getRewards(player).get(slot);
					if (item == null) {
						continue;
					}
					boolean added = true;
					if (item.getDefinitions().isStackable() || item.getAmount() < 2) {
						added = player.getInventory().addItem(item);
						if (added) {
							raid.getRewards(player).toArray()[slot] = null;
						}
					} else {
						for (int i = 0; i < item.getAmount(); i++) {
							Item single = new Item(item.getId());
							if (!player.getInventory().addItem(single)) {
								added = false;
								break;
							}
							raid.getRewards(player).remove(single);
						}
					}
					if (!added) {
						player.getPackets().sendGameMessage(
								"You only had enough space in your inventory to accept some of the items.");
						break;
					}
				}
				break;
			case 7:
				Item item = raid.getRewards(player).get(slotId);
				if (item == null) {
					return true;
				}
				switch (packetId) {
				case WorldPacketsDecoder.ACTION_BUTTON4_PACKET:
					player.getPackets().sendGameMessage("It's a " + item.getDefinitions().getName());
					return false;
				case WorldPacketsDecoder.ACTION_BUTTON3_PACKET:
					raid.getRewards(player).toArray()[slotId] = null;
					break;
				case WorldPacketsDecoder.ACTION_BUTTON2_PACKET:
					player.getBank().addItems(new Item[] { raid.getRewards(player).toArray()[slotId] }, false);
					raid.getRewards(player).toArray()[slotId] = null;
					break;
				case WorldPacketsDecoder.ACTION_BUTTON1_PACKET:
					boolean added = true;
					if (item.getDefinitions().isStackable() || item.getAmount() < 2) {
						added = player.getInventory().addItem(item);
						if (added) {
							raid.getRewards(player).toArray()[slotId] = null;
						}
					} else {
						for (int i = 0; i < item.getAmount(); i++) {
							Item single = new Item(item.getId());
							if (!player.getInventory().addItem(single)) {
								added = false;
								break;
							}
							raid.getRewards(player).remove(single);
						}
					}
					if (!added) {
						player.getPackets().sendGameMessage(
								"You only had enough space in your inventory to accept some of the items.");
						break;
					}
					break;
				default:
					return true;
				}
				break;
			default:
				return true;
			}
			raid.sendChestItems(player);
			return false;
		}
		return true;
	}

	@Override
	public boolean canDropItem(Item item) {
		if (!ItemConstants.isTradeable(item))
			return true;
		player.getPackets().sendSound(2739, 0, 1); // global drop
		player.stopAll(false);
		player.getInventory().deleteItem(item);
		World.addGroundItem(item, new WorldTile(player), item.getId() == 52516 ? null : player, false, -1, 2, -1);
		return false;
	}

	@Override
	public boolean canAttack(Entity target) {
		return isRunning() && !raid.isJailed(player);
	}
	
	@Override
	public boolean keepCombating(Entity target) {
		return canAttack(target);
	}
	
	@Override
	public boolean canHit(Entity target) {
		return canAttack(target);
	}
	
	@Override
	public void processIncommingHit(Hit hit, Entity target) {
		if (isRunning())
			raid.addDamage(player, hit.getDamage());
	}
}
