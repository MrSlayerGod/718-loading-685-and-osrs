package com.rs.game.player.controllers;

import java.util.LinkedList;
import java.util.List;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.pet.LuckyPets;
import com.rs.game.player.content.pet.LuckyPets.LuckyPet;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * bad code. simply fixed by dragonkk, but not written by me
 */
public class AerialFishing extends Controller {

	//fish, hunt
	
	private static final int GLOVES = 52817;

	private static enum AerialFish {
		
		BLUEGIL(52826, 43, 11.5, 35, 16.5, 3.5),
		COMMON_TENCH(52829, 56, 40, 51, 45, 10),
		MOTTLED_EEL(52832, 73, 65, 68, 90, 20),
		GREATER_SIREN(52835, 91, 100, 87, 130, 25);
		
		private int fishLevel, hunterLevel;
		private double fishXP, hunterXP, cookingXP;
		private int itemID;
		
		private AerialFish(int itemID, int fishLevel, double fishXP, int hunterLevel, double hunterXP, double cookingXP) {
			this.itemID = itemID;
			this.fishLevel = fishLevel;
			this.fishXP = fishXP;
			this.fishLevel = fishLevel;
			this.hunterLevel = hunterLevel;
			this.hunterXP = hunterXP;
			this.cookingXP = cookingXP;
		}
		
		public boolean hasLevel(Player player) {
			return player.getSkills().getLevel(Skills.FISHING) >= fishLevel &&
					player.getSkills().getLevel(Skills.HUNTER) >= hunterLevel;
		}
		
	}
	
	public AerialFish getFish() {
		List<AerialFish> fishes = new LinkedList<AerialFish>();
		for (AerialFish fish : AerialFish.values()) {
			if (fish.hasLevel(player))
				fishes.add(fish);
		}
		return fishes.isEmpty() ? AerialFish.BLUEGIL : fishes.get(Utils.random(fishes.size()));
	}
	
	public static boolean isCook(Player player, int itemID) {
		for (AerialFish fish : AerialFish.values()) {
			if (fish.itemID == itemID) {
				player.lock(1);
				player.getInventory().deleteItem(itemID, 1);
				player.getInventory().addItem(new Item(52820, 1));
				player.getSkills().addXp(Skills.COOKING, fish.cookingXP);
				player.getPackets().sendGameMessage("You gut the fish.", true);
				return true;
			}
		}
		return false;
		
	}

	public static void enter(Player player) {
		if (player.getEquipment().getItem(3) != null || player.getEquipment().getItem(Equipment.SLOT_SHIELD) != null) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You need both hands free to use a cormorant.");
			return;
		} else if (player.getSkills().getLevel(Skills.FISHING) < 43 && player.getSkills().getLevel(Skills.HUNTER) < 35) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You need a a Fishing level of 43 & a Hunter level of at least 35 to use a cormorant, come back later.");
			return;
		}
		player.setNextAnimation(new Animation(1560));
		player.lock(3);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.getControlerManager().startControler("AerialFishing");
			}
		});
	}

	@Override
	public void start() {
		player.setNextWorldTile(new WorldTile(1368, 3639, 0));
		player.getEquipment().getItems().set(3, new Item(GLOVES, 1));
		player.getEquipment().refresh(3);
		player.getAppearence().generateAppearenceData();
		player.getDialogueManager().startDialogue("SimpleMessage", "Simply click on the target and try your luck.");
	}
	
	public boolean processObjectClick1(WorldObject object) {
		if (object.getId() == 133614) {
			player.setNextAnimation(new Animation(1560));
			player.lock(3);
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					player.setNextWorldTile(new WorldTile(1408, 3612, 0));
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
		return false;
	}
	
	@Override
	public boolean login() {
		return false;
	}

	@Override
	public void forceClose() {
		player.getEquipment().getItems().set(3, null);
		player.getEquipment().refresh(3);
		player.getInventory().deleteItem(GLOVES, Integer.MAX_VALUE);
		player.getAppearence().generateAppearenceData();
	}
	
	@Override
	public boolean processNPCClick1(final NPC npc) {
		if (npc.getId() == 28523) {
			if (!npc.withinDistance(player))
				return false;
			if (player.getInventory().getFreeSlots() == 0) {
				player.getPackets().sendGameMessage("Not enough space in your inventory.");
				return false;
			}
			if (player.getTemporaryAttributtes().get("birdReleased") != null) {
				player.getDialogueManager().startDialogue("SimpleMessage", "You cannot catch a fish without your bird.");
				return false;
			}
				AerialFish fish = getFish();
			if (player.getSkills().getLevel(Skills.FISHING) < fish.fishLevel) {
				player.getDialogueManager().startDialogue("SimpleMessage",
						"You need a Fishing level of " + fish.fishLevel + " to capture this fish.");
				return true;
			}
			if (player.getSkills().getLevel(Skills.HUNTER) < fish.hunterLevel) {
				player.getDialogueManager().startDialogue("SimpleMessage",
						"You need a Hunter level of " + fish.hunterLevel + " to capture this fish.");
				return true;
			}
					player.lock();
					player.getEquipment().getItems().set(3, new Item(10023, 1));
					player.getEquipment().refresh(3);
					player.getAppearence().generateAppearenceData();
					player.getTemporaryAttributtes().put("birdReleased", true);
					
					player.getPackets().sendGameMessage("You send your cormorant to try to catch a fish at sea.");
					int msDelay = World.sendProjectile(player, npc, 918, 41, 16, 31, 35, 16, 0);
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							World.sendGraphics(npc, new Graphics(69), new WorldTile(npc));
							int msDelay = World.sendProjectile(npc, player, 918, 41, 16, 31, 35, 16, 0);
							npc.finish();
							npc.setRespawnTask();
							WorldTasksManager.schedule(new WorldTask() {
								@Override
								public void run() {
									player.getEquipment().getItems().set(3, new Item(GLOVES, 1));
									player.getEquipment().refresh(3);
									player.getAppearence().generateAppearenceData();
									player.getPackets().sendGameMessage("The cormorant returns with its catch.");
									player.getTemporaryAttributtes().remove("birdReleased");
									player.getSkills().addXp(Skills.FISHING, fish.fishXP);
									player.getSkills().addXp(Skills.HUNTER, fish.hunterXP);
									player.getInventory().addItem(fish.itemID, 1);	
									if (Utils.random(5000) == 0 && !player.containsItem(52840)) {
										int piece = 52840;
										player.getPackets().sendGameMessage("You feel your inventory getting heavier.");
										player.getInventory().addItemDrop(piece, 1);
										World.sendNews(player, player.getDisplayName() + " has received <col=ffff00>" + ItemConfig.forID(piece).getName() + "<col=ff8c38> from <col=cc33ff>aerial fishing<col=ff8c38>!", 1);
									}
									LuckyPets.checkPet(player, LuckyPet.HERON);
									player.unlock();
								}
							}, CombatScript.getDelay(msDelay));
						}
					}, CombatScript.getDelay(msDelay));
				
			return false;
		}
		return true;
	}
}
