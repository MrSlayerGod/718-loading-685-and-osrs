package com.rs.game.player.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.Achievements.Task;
import com.rs.game.player.actions.Cooking.Cookables;
import com.rs.game.player.content.FishingSpotsHandler;
import com.rs.game.player.content.pet.LuckyPets;
import com.rs.game.player.content.pet.LuckyPets.LuckyPet;
import com.rs.utils.Utils;

public class Fishing extends Action {

	public enum Fish {

		ANCHOVIES(321, 15, 40),

		BASS(363, 46, 100),

		COD(341, 23, 45),

		CAVE_FISH(15264, 85, 300),

		HERRING(345, 10, 30),

		LOBSTER(377, 40, 90),

		MACKEREL(353, 16, 20),

		MANTA(389, 81, 46),

		MONKFISH(7944, 62, 120),

		PIKE(349, 25, 60),

		SALMON(331, 30, 70),

		SARDINES(327, 5, 20),

		SEA_TURTLE(395, 79, 38),

		SEAWEED(401, 30, 0),

		OYSTER(407, 30, 0),

		SHARK(383, 76, 110),

		SHRIMP(317, 1, 10),

		SWORDFISH(371, 50, 100),

		TROUT(335, 20, 50),

		TUNA(359, 35, 80),

		CAVEFISH(15264, 85, 300),

		ROCKTAIL(15270, 90, 385),
		
		DARK_CRAB(41934, 85 , 130),
		
		ANGLERFISH(43439, 82 , 120),
		
		LEAPING_TROUT(11328, 48, 50),
		
		LEAPING_SALMON(11330, 58, 70),
		
		LEAPING_STURGEON(11332, 70, 80),
	
		KARAMBWAN(3142, 65, 50),
		
		KARAMBWANJI(3150, 5, 5),
		;

		private final int id, level;
		private final double xp;

		private Fish(int id, int level, double xp) {
			this.id = id;
			this.level = level;
			this.xp = xp;
		}

		public int getId() {
			return id;
		}

		public int getLevel() {
			return level;
		}

		public double getXp() {
			return xp;
		}

		/**
		 * @author dragonkk(Alex)
		 * Dec 10, 2017
		 * @param player 
		 * @return
		 */
		public boolean hasLevel(Player player) {
			if ((this == Fish.LEAPING_TROUT && (player.getSkills().getLevel(Skills.STRENGTH) < 15 || player.getSkills().getLevel(Skills.AGILITY) < 15))
					|| this == Fish.LEAPING_SALMON && (player.getSkills().getLevel(Skills.STRENGTH) < 30 || player.getSkills().getLevel(Skills.AGILITY) < 30)
					|| this == Fish.LEAPING_STURGEON && (player.getSkills().getLevel(Skills.STRENGTH) < 45 || player.getSkills().getLevel(Skills.AGILITY) < 45))
				return false;
			return player.getSkills().getLevel(Skills.FISHING) >= level;
		}
	}

	public enum FishingSpots {
		
		KARAMBWAN(1177, 1, 3159, 3150, new Animation(621), Fish.KARAMBWAN),
		KARAMBWANJI_NET(1174, 1, 303, -1, new Animation(621), Fish.KARAMBWANJI),
		
		CAVEFISH_SHOAL(8841, 1, 307, 313, new Animation(622), Fish.CAVE_FISH),

		ROCKTAIL_SHOAL(8842, 1, 307, 15263, new Animation(622), Fish.ROCKTAIL),

		NET(327, 1, 303, -1, new Animation(621), Fish.SHRIMP, Fish.ANCHOVIES),
		LURE(328, 1, 309, 314, new Animation(622), Fish.TROUT, Fish.SALMON),
		LURE2(329, 1, 309, 314, new Animation(622), Fish.TROUT, Fish.SALMON),
		LURE3(317, 1, 309, 314, new Animation(622), Fish.TROUT, Fish.SALMON),

		BAIT(328, 2, 307, 313, new Animation(622), Fish.PIKE),

		BAIT2(317, 2, 307, 313, new Animation(622), Fish.PIKE),
		BAIT3(329, 2, 307, 313, new Animation(622), Fish.PIKE),
		BAIT4(327, 2, 307, 313, new Animation(622), Fish.SARDINES, Fish.HERRING),
		BAIT_ANGLERFISH(7636, 2, 307, 43431, new Animation(622), Fish.ANGLERFISH),

		
		CAGE(6267, 1, 301, -1, new Animation(619), Fish.LOBSTER),
		CAGE_DARK_CRAB(7863, 1, 301, 41940, new Animation(619), Fish.DARK_CRAB),

		CAGE2(312, 1, 301, -1, new Animation(619), Fish.LOBSTER),
		
		CAGE3(324, 1, 301, -1, new Animation(619), Fish.LOBSTER),
		HARPOON4(324, 2, 311, -1, new Animation(618), Fish.TUNA, Fish.SWORDFISH),
		NET_KARANJA(323, 1, 303, -1, new Animation(621), Fish.SHRIMP, Fish.ANCHOVIES),
		BAIT_KARANJA(323, 2, 307, 313, new Animation(622), Fish.SARDINES, Fish.HERRING),
		
		HARPOON(312, 2, 311, -1, new Animation(618), Fish.TUNA, Fish.SWORDFISH),

		BIG_NET(313, 1, 305, -1, new Animation(620), Fish.MACKEREL, Fish.COD, Fish.BASS, Fish.SEAWEED, Fish.OYSTER),

		HARPOON2(313, 2, 311, -1, new Animation(618), Fish.SHARK),
		
		HARPOON3(3848, 1, 311, -1, new Animation(618), Fish.TUNA, Fish.SWORDFISH),
		
		NET3(3848, 2, 303, -1, new Animation(621), Fish.MONKFISH),
		NET4(508, 2, 303, -1, new Animation(621), Fish.MONKFISH),

		NET2(952, 1, 303, -1, new Animation(621), Fish.SHRIMP),
		

		BARB_FISH(2859, 1, 307, -1, new Animation(622), Fish.LEAPING_TROUT, Fish.LEAPING_SALMON, Fish.LEAPING_STURGEON),
		;

		private final Fish[] fish;
		private final int id, option, tool, bait;
		private final Animation animation;

		static final Map<Integer, FishingSpots> spot = new HashMap<Integer, FishingSpots>();

		public static FishingSpots forId(int id) {
			return spot.get(id);
		}

		static {
			for (FishingSpots spots : FishingSpots.values())
				spot.put(spots.id | spots.option << 24, spots);
		}

		private FishingSpots(int id, int option, int tool, int bait, Animation animation, Fish... fish) {
			this.id = id;
			this.tool = tool;
			this.bait = bait;
			this.animation = animation;
			this.fish = fish;
			this.option = option;
		}

		public Fish[] getFish() {
			return fish;
		}

		public int getId() {
			return id;
		}

		public int getOption() {
			return option;
		}

		public int getTool() {
			return tool;
		}

		public int getBait() {
			return bait;
		}

		public Animation getAnimation() {
			return animation;
		}
	}

	private FishingSpots spot;

	private NPC npc;
	private WorldTile tile;
	private int fishId;
	private int count;

	private final int[] BONUS_FISH =
	{ 341, 349, 401, 407 };

	private boolean multipleCatch;

	public Fishing(FishingSpots spot, NPC npc) {
		this.spot = spot;
		this.npc = npc;
		if (npc != null)
			tile = new WorldTile(npc);
	}

	@Override
	public boolean start(Player player) {
		if (!checkAll(player))
			return false;
		fishId = getRandomFish(player);
		player.getPackets().sendGameMessage("You attempt to capture a fish...", true);
		setActionDelay(player, getFishingDelay(player));
		return true;
	}

	@Override
	public boolean process(Player player) {
		player.setNextAnimation(
				
				spot.getAnimation().getIds()[0] == 618 && (player.getInventory().containsOneItem(14101, 14109) || player.getEquipment().containsOneItem(14101, 14109)) ?
						new Animation(10617) : 
				
				spot.getAnimation());
		return checkAll(player);
	}

	private int getFishingDelay(Player player) {
		int playerLevel = player.getSkills().getLevel(Skills.FISHING);
		int fishLevel = spot.getFish()[fishId].getLevel();
		int modifier = spot.getFish()[fishId].getLevel();
		int randomAmt = Utils.random(4);
		double cycleCount = 1, otherBonus = 0;
		if (player.getFamiliar() != null)
			otherBonus = getSpecialFamiliarBonus(player.getFamiliar().getId());
		cycleCount = Math.ceil(((fishLevel + otherBonus) * 50 - playerLevel * 10) / modifier * 0.25 - randomAmt * 4);
		if (cycleCount < 1)
			cycleCount = 1;
		int delay = (int) cycleCount + 1;
		delay /= player.getAuraManager().getFishingAccurayMultiplier();
		
		if (spot.getAnimation().getIds()[0] == 618 && (player.getEquipment().getWeaponId() == 51028 || player.getEquipment().getWeaponId() == 51031))
			delay *= 0.8;
		else if (spot.getAnimation().getIds()[0] == 618 && (player.getEquipment().getWeaponId() == 53762))
			delay *= 0.65;
		
		return delay;

	}

	private int getSpecialFamiliarBonus(int id) {
		switch (id) {
		case 6796:
		case 6795:// rock crab
			return 1;
		}
		return -1;
	}

	private int getRandomFish(Player player) {
		int random = 0;
		if (spot.getFish().length > 1) { 
			List<Integer> possibleIDs = new ArrayList<Integer>();
			for (int i = 0; i < spot.getFish().length; i++)
				if (spot.getFish()[i].hasLevel(player))
					possibleIDs.add(i);
			if (possibleIDs.size() > 0)
				random = possibleIDs.get(Utils.random(possibleIDs.size()));
		}
	//player.getSkills().getLevel(Skills.FISHING) < spot.getFish()[fishId].getLevel()
	
		/*int difference = player.getSkills().getLevel(Skills.FISHING) - spot.getFish()[random].getLevel();
		if (difference < -1)
			return random = 0;
		if (random < -1)
			return random = 0;*/
		return random;
	}

	@Override
	public int processWithDelay(Player player) {
		addFish(player);
		if (count++ >= 50) //to prevent exploit karambju
			return -1;
		return getFishingDelay(player);
	}

	private void addFish(Player player) {
		
		if (spot.getFish()[fishId] == Fish.SHRIMP)
			player.getAchievements().add(Task.FISH_SHRIMP);
		else	if (spot.getFish()[fishId] == Fish.SHARK)
			player.getAchievements().add(Task.FISH_SHARK);
			
		
		
		if (spot.getFish()[fishId] == Fish.TUNA || spot.getFish()[fishId] == Fish.SHARK || spot.getFish()[fishId] == Fish.SWORDFISH) {
			multipleCatch = false;
			if (Utils.random(50) <= 5) {
				if (player.getSkills().getLevel(Skills.AGILITY) >= spot.getFish()[fishId].getLevel())
					multipleCatch = true;
			}
		}
		Item fish = new Item(spot.getFish()[fishId].getId(), 
				
				
				spot.getFish()[fishId] == Fish.KARAMBWANJI ? ((player.getSkills().getLevelForXp(Skills.FISHING) / 5) + 1) : multipleCatch ? 2 : 1);
		player.getPackets().sendGameMessage(getMessage(fish), true);
		player.getInventory().deleteItem(spot.getBait(), 1);
		double totalXp = spot.getFish()[fishId].getXp();
		double multiplier = 1;
		if (hasFishingSuit(player))
			multiplier += 0.01;
		if (player.getEquipment().getHatId() == 24427)
			multiplier += 0.01;
		if (player.getEquipment().getChestId() == 24428)
			multiplier += 0.01;
		if (player.getEquipment().getLegsId() == 24429)
			multiplier += 0.01;
		if (player.getEquipment().getBootsId() == 24430)
			multiplier += 0.01;
		totalXp *= multiplier;
		
		
		player.getSkills().addXp(Skills.FISHING, totalXp);
		if (spot.getFish()[fishId] == Fish.LEAPING_TROUT || spot.getFish()[fishId] == Fish.LEAPING_SALMON || spot.getFish()[fishId] == Fish.LEAPING_STURGEON) {
			int exp = spot.getFish()[fishId] == Fish.LEAPING_TROUT ? 5 : spot.getFish()[fishId] == Fish.LEAPING_SALMON ? 6 : 7;
			player.getSkills().addXp(Skills.STRENGTH, exp);
			player.getSkills().addXp(Skills.AGILITY, exp);
		}
		
		if (spot.getAnimation().getIds()[0] == 618 && player.getEquipment().getWeaponId() == 51031 && Utils.random(3) == 0) {
			Cookables cooking = Cooking.isCookingSkill(fish);
			player.getSkills().addXp(Skills.COOKING, cooking != null ? (cooking.getXp()/2) : (spot.getFish()[fishId].level / 5));
		} else
			player.getInventory().addItem(fish);
		if (player.getFamiliar() != null) {
			if (Utils.random(50) < 5 && getSpecialFamiliarBonus(player.getFamiliar().getId()) > 0) {
				player.getInventory().addItem(new Item(BONUS_FISH[Utils.random(BONUS_FISH.length)]));
				player.getSkills().addXp(Skills.FISHING, 5.5);
			}
		}
		fishId = getRandomFish(player);
		if (Utils.random(50) == 0 && FishingSpotsHandler.moveSpot(npc))
			player.setNextAnimation(new Animation(-1));
		LuckyPets.checkPet(player, LuckyPet.HERON);
		/*if (Utils.random(500) == 0)
			dropSet(player);*/
	}

	public static final int[] PIECES = {24427, 24428, 24429, 24430};
	private void dropSet(Player player) {
		List<Integer> pieces = new ArrayList<Integer>();
		for (int i : PIECES)
			if (!player.containsItem(i))
				pieces.add(i);
		if (pieces.isEmpty())
			return;
		int piece = pieces.get(Utils.random(pieces.size()));
		player.getPackets().sendGameMessage("You feel your inventory getting heavier.");
		player.getInventory().addItemDrop(piece, 1);
		World.sendNews(player, player.getDisplayName() + " has received <col=ffff00>" + ItemConfig.forID(piece).getName() + "<col=ff8c38> from <col=cc33ff>fishing<col=ff8c38>!", 1);

	}
	
	
	private boolean hasFishingSuit(Player player) {
		if (player.getEquipment().getHatId() == 24427 && player.getEquipment().getChestId() == 24428 && player.getEquipment().getLegsId() == 24429 && player.getEquipment().getBootsId() == 24430)
			return true;
		return false;
	}

	private String getMessage(Item fish) {
		if (spot.getFish()[fishId] == Fish.ANCHOVIES || spot.getFish()[fishId] == Fish.SHRIMP)
			return "You manage to catch some " + fish.getDefinitions().getName().toLowerCase() + ".";
		else if (multipleCatch)
			return "Your quick reactions allow you to catch two " + fish.getDefinitions().getName().toLowerCase() + ".";
		else
			return "You manage to catch a " + fish.getDefinitions().getName().toLowerCase() + ".";
	}

	private boolean checkAll(Player player) {
		if (/*player.getSkills().getLevel(Skills.FISHING) < spot.getFish()[fishId].getLevel()*/
				!spot.getFish()[fishId].hasLevel(player)) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You need a fishing level of " + spot.getFish()[fishId].getLevel() + " to fish here.");
			return false;
		}
		if (!player.getInventory().containsItemToolBelt(spot.getTool())
				&& !(spot.getTool() == 311 && 
				(player.getInventory().containsOneItem(14101, 14109) || player.getEquipment().containsOneItem(14101, 14109)))) {
			player.getPackets().sendGameMessage("You need a " + new Item(spot.getTool()).getDefinitions().getName().toLowerCase() + " to fish here.");
			return false;
		}
		if (spot.getBait() != -1 && !player.getInventory().containsOneItem(spot.getBait())) {
			player.getPackets().sendGameMessage("You don't have " + new Item(spot.getBait()).getDefinitions().getName().toLowerCase() + " to fish here.");
			return false;
		}
		if (!player.getInventory().hasFreeSlots()) {
			player.setNextAnimation(new Animation(-1));
			player.getDialogueManager().startDialogue("SimpleMessage", "You don't have enough inventory space.");
			return false;
		}
		if (npc != null && (tile.getX() != npc.getX() || tile.getY() != npc.getY()))
			return false;
		return true;
	}

	@Override
	public void stop(final Player player) {
		setActionDelay(player, 3);
	}
}
