package com.rs.game.player.actions.thieving;

import java.util.List;

import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.Achievements.Task;
import com.rs.game.player.content.pet.LuckyPets;
import com.rs.game.player.content.pet.LuckyPets.LuckyPet;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.ObjectHandler;
import com.rs.utils.Utils;

public class Thieving {

	public enum Stalls {
		
		//custom stalls
		
		GEM_STALL1(78324, 1, new int[]
				{ 25448 }, 1, 0.5, 12, 34381),
		GEM_STALL2(78325, 25, new int[]
				{ 25449 }, 1, 0.5, 26, 34381),
		GEM_STALL3(78326, 50, new int[]
				{ 25450 }, 1, 0.5, 84, 34381),
		GEM_STALL4(78327, 75, new int[]
				{ 25451 }, 1, 0.5, 198, 34381),
		GEM_STALL5(78328, 90, new int[]
				{ 25452 }, 1, 0.5, 556, 34381),
		GEM_STALL6(78330, 99, new int[]
				{ 25473 }, 1, 0.5, 600, 34381),
		
		VEGETABAL(0, 2, new int[]
		{ 1957, 1965, 1942, 1982, 1550 }, 1, 2, 10, 34381),
		
		CAKE(34384, 5, new int[]
		{ 1891, 1897, 2309 }, 1, 2.5, 16, 34381),
		
		CAKE_O(111730, 1, new int[]
				{ 1891, 1897, 2309 }, 1, 2.5, 16, 34381),
		
		CAKE_K(6163, 5, new int[]
				{ 1891, 1897, 2309 }, 1, 2.5, 16, 6984),
		
		CAKE_H(106945, 5, new int[]
				{ 1891, 1897, 2309 }, 1, 2.5, 16, 106944),

		CRAFTING(4874, 5, new int[]
		{ 1755, 1592, 1597 }, 1, 7, 16, 34381),
		
		CRAFTING_K(6166, 5, new int[]
				{ 1755, 1592, 1597 }, 1, 7, 16, 6984),

		MONKEY_FOOD(4875, 5, new int[]
		{ 1963 }, 1, 7, 16, 34381),

		MONKEY_GENERAL(4876, 5, new int[]
		{ 1931, 2347, 590 }, 1, 7, 16, 34381),

		TEA_STALL(635, 5, new int[]
		{ 712 }, 1, 7, 16, 34381),
		
		TEA_STALL_O(100635, 5, new int[]
				{ 712 }, 1, 7, 16, 34381),

		SILK_STALL(34383, 20, new int[]
		{ 950 }, 1, 8, 24, 34381),
		SILK_STALL_K(6165, 20, new int[]
				{ 950 }, 1, 8, 24, 6984),
		SILK_STALL_P(136569, 20, new int[]
				{ 950 }, 1, 8, 24, 136568),

		WINE_STALL(14011, 22, new int[]
		{ 1937, 1993, 1987, 1935, 7919 }, 1, 16, 27, 2046),

		FRUIT_STALL(128823, 25, new int[]
		{ 1955, 1963, 5504, 1951, 247, 464, 2120, 2102, 2114, 5972 }, 1, 16, 28.6, 127538),
		
		SEED_STALL(7053, 27, new int[]
		{ 5096, 5097, 5098, 5099, 5100, 5101, 5102, 5103, 5105 }, 30, 11, 10, 2047),

		FUR_STALL(34387, 35, new int[]
		{ 6814, 958 }, 1, 15, 36, 34381),

		FISH_STALL(4277, 42, new int[]
		{ 331, 359, 377 }, 1, 16, 42, 34381),

		CROSSBOW_STALL(0, 49, new int[]
		{ 877, 9420, 9440 }, 1, 11, 52, 34381),
		
		CROSSBOW_STALL_K(17031, 49, new int[]
				{ 877, 9420, 9440 }, 1, 11, 52, 6984),

		SILVER_STALL(34382, 50, new int[]
		{ 442 }, 1, 30, 54, 34381),
		
		SILVER_STALL_K(6164, 50, new int[]
				{ 442 }, 1, 30, 54, 6984),
		SILVER_STALL_P(136570, 50, new int[]
				{ 442 }, 1, 30, 54, 136568),
		
		
		COIN_STALL(129006, 60, new int[]
		{ 9040, 9028, 9034 }, 1, 0, 54, 127754),

		SPICE_STALL(34386, 65, new int[]
		{ 2007 }, 1, 80, 81, 34381),
		
		SPICE_STALL_P(136572, 65, new int[]
				{ 2007 }, 1, 80, 81, 136568),
		
		
		MAGIC_STALL(4877, 65, new int[]
		{ 556, 557, 554, 555, 563 }, 30, 80, 100, 34381),

		SCIMITAR_STALL(4878, 65, new int[]
		{ 1323 }, 1, 80, 100, 34381),

		GEM_STALL(34385, 75, new int[]
		{ 1623, 1621, 1619, 1617, 1631 }, 1, 160, 16, 34381),
		
		GEM_STALL_O(111731, 75, new int[]
				{ 1623, 1621, 1619, 1617, 1631 }, 1, 160, 16, 34381),
				
		
		GEM_STALL_K(6162, 75, new int[]
				{ 1623, 1621, 1619, 1617, 1631 }, 1, 160, 16, 6984),
		
		GEM_STALL_P(136571, 75, new int[]
				{ 1623, 1621, 1619, 1617, 1631 }, 1, 160, 16, 136568),
		;
		

		private int[] item;
		private int level;
		private int amount;
		private int objectId;
		private int replaceObject;
		private double experience;
		private double seconds;

		Stalls(int objectId, int level, int[] item, int amount, double seconds, double experience, int replaceObject) {
			this.objectId = objectId;
			this.level = level;
			this.item = item;
			this.amount = amount;
			this.seconds = seconds;
			this.experience = experience;
			this.replaceObject = replaceObject;
		}

		public int getReplaceObject() {
			return replaceObject;
		}

		public int getObjectId() {
			return objectId;
		}

		public int getItem(int count) {
			return item[count];
		}

		public int getAmount() {
			return amount;
		}

		public int getLevel() {
			return level;
		}

		public double getTime() {
			return seconds;
		}

		public double getExperience() {
			return experience;
		}
	}

	public static boolean handleStalls(final Player player, final WorldObject object) {
		for (final Stalls stall : Stalls.values()) {
			if (stall.getObjectId() == object.getId()) {
				if (player.getAttackedBy() != null && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
					player.getPackets().sendGameMessage("You can't do this while you're under combat.");
					return true;
				}
				final WorldObject emptyStall = new WorldObject(stall.getReplaceObject(), 10, object.getRotation(), object.getX(), object.getY(), object.getPlane());
				if (player.getSkills().getLevel(Skills.THIEVING) < stall.getLevel()) {
					player.getPackets().sendGameMessage("You need a thieving level of " + stall.getLevel() + " to steal from this.", true);
					return true;
				}
				if (player.getInventory().getFreeSlots() <= 0) {
					player.getPackets().sendGameMessage("Not enough space in your inventory.", true);
					return true;
				}

				player.setNextAnimation(new Animation(881));
				player.lock(2);
				WorldTasksManager.schedule(new WorldTask() {
					boolean gaveItems;

					@Override
					public void run() {
						if (!gaveItems) {
							player.getInventory().addItemMoneyPouch(new Item(stall.getItem(Utils.getRandom(stall.item.length - 1)), Utils.getRandom(stall.getAmount())));
						
							if (stall == Stalls.CAKE
									|| stall == Stalls.CAKE_O
									|| stall == Stalls.CAKE_K
									|| stall == Stalls.CAKE_H)
								player.getAchievements().add(Task.STEAL_FROM_BAKERS_STALL);
							else if (stall == Stalls.SILK_STALL
									|| stall == Stalls.SILK_STALL_K
									|| stall == Stalls.SILK_STALL_P)
								player.getAchievements().add(Task.STEAL_FROM_SILK_STALL);
						/*	else if (stall == Stalls.GEM_STALL
									|| stall == Stalls.GEM_STALL_K
									|| stall == Stalls.GEM_STALL_O
									|| stall == Stalls.GEM_STALL_P)
								player.getAchievements().add(Task.STEAL_FROM_GEM_STALL);*/
							
							player.getSkills().addXp(Skills.THIEVING, stall.getExperience() * getSetBonus(player));
							LuckyPets.checkPet(player, LuckyPet.ROCKY);
							gaveItems = true;
							checkGuards(player);
							if (stall.getTime() == 0) 
								stop();
						} else {
							if (!World.containsObjectWithId(object, object.getId())) {
								stop();
								return;
							}
							World.spawnObjectTemporary(emptyStall, (int) (1500 * stall.getTime()), false, true);
							stop();
						}
					}
				}, 0, 0);
				return true;
			}
		}
		return false;
	}
	
	public static double getSetBonus(Player player) {
		double xpBoost = 1.00;
		if (player.getEquipment().getChestId() == 21480)
			xpBoost += 0.01;
		if (player.getEquipment().getLegsId() == 21481)
			xpBoost += 0.01;
		if (player.getEquipment().getHatId() == 21482)
			xpBoost += 0.01;
		if (player.getEquipment().getBootsId() == 21483)
			xpBoost += 0.01;
		if (player.getEquipment().getChestId() == 21480 && player.getEquipment().getLegsId() == 21481 && player.getEquipment().getHatId() == 21482 && player.getEquipment().getBootsId() == 21483)
			xpBoost += 0.01;
		return xpBoost;
	}

	public static void checkGuards(Player player) {
		NPC guard = null;
		int lastDistance = -1;
		for (int regionId : player.getMapRegionsIds()) {
			List<Integer> npcIndexes = World.getRegion(regionId).getNPCsIndexes();
			if (npcIndexes == null)
				continue;
			for (int npcIndex : npcIndexes) {
				NPC npc = World.getNPCs().get(npcIndex);
				if (npc == null)
					continue;
				if (!npc.getName().toLowerCase().contains("guard") || npc.isUnderCombat() || npc.isDead() || !npc.withinDistance(player, 4) || !npc.clipedProjectile(player, true))
					continue;
				int distance = Utils.getDistance(npc.getX(), npc.getY(), player.getX(), player.getY());
				if (lastDistance == -1 || lastDistance > distance) {
					guard = npc;
					lastDistance = distance;
				}
			}
		}
		if (guard != null) {
			guard.setNextForceTalk(new ForceTalk(guard.getName().toLowerCase().contains("dog") ? "Woof!" : "Hey, what do you think you are doing!"));
			guard.setTarget(player);
		}
	}

	public static boolean pickDoor(Player player, WorldObject object) {
		/*if (player.getTemporaryAttributtes().get("numbFingers") == null)
			player.getTemporaryAttributtes().put("numbFingers", 0);
		int thievingLevel = player.getSkills().getLevel(Skills.THIEVING);
		int increasedChance = getIncreasedChance(player);
		int decreasedChance = (Integer) player.getTemporaryAttributtes().get("numbFingers");
		int level = Utils.getRandom(thievingLevel + (increasedChance - decreasedChance)) + 1;
		double ratio = level / (Utils.getRandom(45 + 5) + 1);*/
		if (/*Math.round(ratio * thievingLevel) < (player.getAttackedByDelay() > 0 ? 50 : 40) / player.getAuraManager().getThievingAccurayMultiplier()*/
				Utils.random(2) == 0) {
			player.getPackets().sendGameMessage("You fail to unlock the door and your hands begin to numb down.");
			//player.getTemporaryAttributtes().put("numbFingers", decreasedChance + 1);
			return false;
		}
		player.getPackets().sendGameMessage("You successfully unlock the door.");
		ObjectHandler.handleDoor(player, object, 3000 + Utils.getRandom(1000));
		return true;
	}

	private static int getIncreasedChance(Player player) {
		int chance = 0;
		if (Equipment.getItemSlot(Equipment.SLOT_HANDS) == 10075)
			chance += 12;
		player.getEquipment();
		if (Equipment.getItemSlot(Equipment.SLOT_CAPE) == 15349)
			chance += 15;
		return chance;
	}

}
