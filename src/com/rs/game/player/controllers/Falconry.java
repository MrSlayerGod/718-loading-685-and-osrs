package com.rs.game.player.controllers;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.FlyingEntityHunter;
import com.rs.game.player.content.FlyingEntityHunter.DynamicFormula;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * bad code. simply fixed by dragonkk, but not written by me
 */
public class Falconry extends Controller {

	public int[] xp =
	{ 103, 132, 156 };
	public int[] furRewards =
	{ 10125, 10115, 10127 };
	public int[] levels =
	{ 43, 57, 69 };

	public static void beginFalconry(Player player) {
		if (player.getEquipment().getItem(3) != null || player.getEquipment().getItem(Equipment.SLOT_SHIELD) != null) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You need both hands free to use a falcon.");
			return;
		} else if (player.getSkills().getLevel(Skills.HUNTER) < 43) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You need a Hunter level of at least 43 to use a falcon, come back later.");
			return;
		}
		player.setNextAnimation(new Animation(1560));
		player.lock(3);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.getControlerManager().startControler("Falconry");
			}
		});
	}

	@Override
	public void start() {
		player.setNextWorldTile(new WorldTile(2371, 3619, 0));
		player.getEquipment().getItems().set(3, new Item(10024, 1));
		player.getEquipment().refresh(3);
		player.getAppearence().generateAppearenceData();
		player.getDialogueManager().startDialogue("SimpleMessage", "Simply click on the target and try your luck.");
	}
	
	public boolean processObjectClick1(WorldObject object) {
		if (object.getId() == 19222) {
			player.setNextAnimation(new Animation(1560));
			player.lock(3);
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					player.setNextWorldTile(new WorldTile(2371, 3622, 0));
					player.getControlerManager().forceStop();
				}
			});
			return false;
		}
		return true;
	}

	
	
	@Override
	public boolean canRemoveEquip(int slotId, int itemId) {
		if (slotId == 3 || slotId == 5)
			return false;
		return true;
	}
	
	@Override
	public boolean canEquip(int slotId, int itemId) {
		if (slotId == 3 || slotId == 5)
			return false;
		return true;
	}

	@Override
	public void magicTeleported(int type) {
		player.getControlerManager().forceStop();
	}
	
	@Override
	public boolean logout() {
		removeKill();
		return false;
	}
	
	@Override
	public boolean login() {
		return false;
	}

	@Override
	public void forceClose() {
		removeKill();
		player.getEquipment().getItems().set(3, null);
		player.getEquipment().refresh(3);
		player.getInventory().deleteItem(10024, Integer.MAX_VALUE);
		player.getAppearence().generateAppearenceData();
	}

	
	public void removeKill() {
		NPC kill = (NPC) player.getTemporaryAttributtes().remove("ownedFalcon");
		if (kill != null) {
			player.getEquipment().getItems().set(3, new Item(10024, 1));
			kill.setNextNPCTransformation(kill.getId() + 4);
			kill.setRespawnTask();
		}
	}
	
	@Override
	public boolean processNPCClick1(final NPC npc) {
		if (npc.getDefinitions().getName().toLowerCase().contains("kebbit")) {
			if (player.getInventory().getFreeSlots() == 0) {
				player.getPackets().sendGameMessage("Not enough space in your inventory.");
				return false;
			}
			if (player.getTemporaryAttributtes().get("falconReleased") != null) {
				player.getDialogueManager().startDialogue("SimpleMessage", "You cannot catch a kebbit without your falcon.");
				return false;
			}
			int level = levels[(npc.getId() - 5098)];
			if (proccessFalconAttack(npc)) {
				if (player.getSkills().getLevel(Skills.HUNTER) < level) {
					player.getDialogueManager().startDialogue("SimpleMessage", "You need a Hunter level of " + level + " to capture this kebbit.");
					return true;
				} else if (!npc.isLocked() && FlyingEntityHunter.isSuccessful(player, level, new DynamicFormula() {
					@Override
					public int getExtraProbablity(Player player) {
						if (player.getEquipment().getGlovesId() == 10075)
							return 3;
						return 1;
					}
				})) {
					npc.setLocked(true);
					player.lock(3);
					player.getEquipment().getItems().set(3, new Item(10023, 1));
					player.getEquipment().refresh(3);
					player.getAppearence().generateAppearenceData();
					player.getTemporaryAttributtes().put("falconReleased", true);
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							World.sendProjectile(player, npc, 918, 41, 16, 31, 35, 16, 0);
							WorldTasksManager.schedule(new WorldTask() {
								@Override
								public void run() {
									npc.setNextNPCTransformation(npc.getId() - 4);
									player.getTemporaryAttributtes().put("ownedFalcon", npc);
									player.getPackets().sendGameMessage("The falcon successfully swoops down and captures the kebbit.");
									player.getHintIconsManager().addHintIcon(npc, 1, -1, false);
									npc.setLocked(false);
								}
							});
						}
					});
				} else {
					player.getEquipment().getItems().set(3, new Item(10023, 1));
					player.getEquipment().refresh(3);
					player.getAppearence().generateAppearenceData();
					player.getTemporaryAttributtes().put("falconReleased", true);
					player.lock(4);
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							World.sendProjectile(player, npc, 918, 41, 16, 31, 35, 16, 0);
							WorldTasksManager.schedule(new WorldTask() {
								@Override
								public void run() {
									World.sendProjectile(npc, player, 918, 41, 16, 31, 35, 16, 0);
									WorldTasksManager.schedule(new WorldTask() {
										@Override
										public void run() {
											player.getEquipment().getItems().set(3, new Item(10024, 1));
											player.getEquipment().refresh(3);
											player.getAppearence().generateAppearenceData();
											player.getTemporaryAttributtes().remove("falconReleased");
											player.getPackets().sendGameMessage("The falcon swoops down on the kebbit, but just barely misses catching it.");
										}
									});
								}
							}, Utils.getDistance(player, npc) > 3 ? 2 : 1);
						}
					});
				}
			}
			return false;
		} else if (npc.getDefinitions().getName().toLowerCase().contains("gyr falcon")) {
			NPC kill = (NPC) player.getTemporaryAttributtes().get("ownedFalcon");
			if (kill == null)
				return false;
			if (kill != npc) {
				player.getDialogueManager().startDialogue("SimpleMessage", "This isn't your kill!");
				return false;
			}
			player.lock(1);
			player.getInventory().addItem(new Item(furRewards[(npc.getId() - 5094)], 1));
			player.getInventory().addItem(new Item(526, 1));
			player.getSkills().addXp(Skills.HUNTER, xp[(npc.getId() - 5094)]);
			player.getPackets().sendGameMessage("You retreive the falcon as well as the fur of the dead kebbit.");
			player.getHintIconsManager().removeUnsavedHintIcon();
			player.getEquipment().getItems().set(3, new Item(10024, 1));
			player.getEquipment().refresh(3);
			player.getAppearence().generateAppearenceData();
			player.getTemporaryAttributtes().remove("ownedFalcon");
			player.getTemporaryAttributtes().remove("falconReleased");
			npc.setNextNPCTransformation(npc.getId() + 4);
			npc.setRespawnTask();
			return false;
		}
		return true;
	}

	private boolean proccessFalconAttack(NPC target) {
		int distanceX = player.getX() - target.getX();
		int distanceY = player.getY() - target.getY();
		int size = player.getSize();
		int maxDistance = 16;
		player.resetWalkSteps();
		if ((!player.clipedProjectile(target, maxDistance == 0)) || distanceX > size + maxDistance || distanceX < -1 - maxDistance || distanceY > size + maxDistance || distanceY < -1 - maxDistance) {
			if (!player.calcFollow(target, 2, true, true))
				return true;
		}
		return true;
	}
}
