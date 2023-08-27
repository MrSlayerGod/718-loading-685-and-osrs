package com.rs.game.player.content;

import java.io.Serializable;
import java.util.List;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.ShopsHandler;
import com.rs.utils.Utils;

public class Slayer {

	public enum SlayerMaster implements Serializable {

		SPRIA(8462, 85, 1, new int[]
		{ 1, 2, 4 }, new int[]
		{ 15, 50 }, SlayerTask.COCKROACH, SlayerTask.CRAB,  SlayerTask.BANSHEE, SlayerTask.BAT, SlayerTask.BEAR, SlayerTask.COW, SlayerTask.BIRD, SlayerTask.CAVE_BUG, SlayerTask.CAVE_SLIME, SlayerTask.DWARF, SlayerTask.CRAWLING_HAND, SlayerTask.DESERT_LIZARD, SlayerTask.DWARF, SlayerTask.GHOST, SlayerTask.GOBLIN, SlayerTask.ICEFIEND, SlayerTask.MINOTAUR, SlayerTask.MONKEY, SlayerTask.SCORPION, SlayerTask.SKELETON, SlayerTask.SPIDER, SlayerTask.WOLF, SlayerTask.ZOMBIE),

		TURAEL(8480, 3, 1, new int[]
		{ 1, 2, 4 }, new int[]
		{ 15, 50 },  SlayerTask.COCKROACH, SlayerTask.CRAB, SlayerTask.BANSHEE, SlayerTask.BAT, SlayerTask.BEAR, SlayerTask.COW, SlayerTask.BIRD, SlayerTask.CAVE_BUG, SlayerTask.CAVE_SLIME, SlayerTask.DWARF, SlayerTask.CRAWLING_HAND, SlayerTask.DESERT_LIZARD, SlayerTask.DWARF, SlayerTask.GHOST, SlayerTask.GOBLIN, SlayerTask.ICEFIEND, SlayerTask.MINOTAUR, SlayerTask.MONKEY, SlayerTask.SCORPION, SlayerTask.SKELETON, SlayerTask.SPIDER, SlayerTask.WOLF, SlayerTask.ZOMBIE),

		MAZCHNA(8481, 20, 1, new int[]
		{ 2, 5, 15 }, new int[]
		{ 40, 85 }, SlayerTask.COCKROACH, SlayerTask.BANSHEE, SlayerTask.BAT, SlayerTask.CATABLEPON, SlayerTask.CAVE_CRAWLER, SlayerTask.COCKATRICE, SlayerTask.CRAWLING_HAND, SlayerTask.CYCLOPS, SlayerTask.DESERT_LIZARD, SlayerTask.DOG, SlayerTask.FLESH_CRAWLER, SlayerTask.GHOUL, SlayerTask.GHOST, SlayerTask.GROTWORM, SlayerTask.HILL_GIANT, SlayerTask.HOBGOBLIN, SlayerTask.ICE_WARRIOR, SlayerTask.KALPHITE, SlayerTask.PYREFIEND, SlayerTask.ROCKSLUG, SlayerTask.SKELETON, SlayerTask.VAMPYRE, SlayerTask.WALL_BEAST, SlayerTask.WOLF, SlayerTask.ZOMBIE),

		ACHTRYN(8465, 20, 1, new int[]
		{ 2, 5, 15 }, new int[]
		{ 40, 85 }, SlayerTask.COCKROACH, SlayerTask.BANSHEE, SlayerTask.BAT, SlayerTask.CATABLEPON, SlayerTask.CAVE_CRAWLER, SlayerTask.COCKATRICE, SlayerTask.CRAWLING_HAND, SlayerTask.CYCLOPS, SlayerTask.DESERT_LIZARD, SlayerTask.DOG, SlayerTask.FLESH_CRAWLER, SlayerTask.GHOUL, SlayerTask.GHOST, SlayerTask.GROTWORM, SlayerTask.HILL_GIANT, SlayerTask.HOBGOBLIN, SlayerTask.ICE_WARRIOR, SlayerTask.KALPHITE, SlayerTask.PYREFIEND, SlayerTask.ROCKSLUG, SlayerTask.SKELETON, SlayerTask.VAMPYRE, SlayerTask.WALL_BEAST, SlayerTask.WOLF, SlayerTask.ZOMBIE),

		VANNAKA(1597, 40, 1, new int[]
		{ 4, 20, 60 }, new int[]
		{ 60, 120 }, SlayerTask.COCKROACH, SlayerTask.ABERRANT_SPECTRE, SlayerTask.ANKOU, SlayerTask.BANSHEE, SlayerTask.BASILISK, SlayerTask.BLOODVELD, SlayerTask.BRINE_RAT, SlayerTask.COCKATRICE, SlayerTask.CROCODILE, SlayerTask.CYCLOPS, SlayerTask.DUST_DEVIL, SlayerTask.EARTH_WARRIOR, SlayerTask.GHOUL, SlayerTask.GREEN_DRAGON, SlayerTask.GROTWORM, SlayerTask.HARPIE_BUG_SWARM, SlayerTask.HILL_GIANT, SlayerTask.ICE_GIANT, SlayerTask.ICE_WARRIOR, SlayerTask.INFERNAL_MAGE, SlayerTask.JELLY, SlayerTask.JUNGLE_HORROR, SlayerTask.LESSER_DEMON, SlayerTask.MOLANISK, SlayerTask.MOSS_GIANT, SlayerTask.OGRE, SlayerTask.MOGRE, SlayerTask.OTHERWORLDLY_BEING, SlayerTask.PYREFIEND, SlayerTask.SHADE, SlayerTask.SHADOW_WARRIOR, SlayerTask.TUROTH, SlayerTask.VAMPYRE, SlayerTask.WEREWOLF),

		CHAELDAR(1598, 70, 1, new int[]
		{ 10, 50, 100 }, new int[]
		{ 110, 170 }, SlayerTask.KRAKEN, SlayerTask.FOSSIL_ISLAND_WYVERN, SlayerTask.LIZARDMAN, SlayerTask.ABERRANT_SPECTRE, SlayerTask.BANSHEE, SlayerTask.BASILISK, SlayerTask.BLOODVELD, SlayerTask.BLUE_DRAGON, SlayerTask.BRINE_RAT, SlayerTask.BRONZE_DRAGON, SlayerTask.CAVE_CRAWLER, SlayerTask.CAVE_HORROR, SlayerTask.CRAWLING_HAND, SlayerTask.DAGANNOTH, SlayerTask.DUST_DEVIL, SlayerTask.ELF_WARRIOR, SlayerTask.FEVER_SPIDER, SlayerTask.FIRE_GIANT, SlayerTask.FUNGAL_MAGE, SlayerTask.GARGOYLE, SlayerTask.GRIFOLAPINE, SlayerTask.GRIFOLAROO, SlayerTask.GROTWORM, SlayerTask.HARPIE_BUG_SWARM, SlayerTask.JUNGLE_STRYKEWYRM, SlayerTask.INFERNAL_MAGE, SlayerTask.JELLY, SlayerTask.JUNGLE_HORROR, SlayerTask.KALPHITE, SlayerTask.KALPHITE, SlayerTask.KURASK, SlayerTask.LESSER_DEMON, SlayerTask.ZYGOMITE, SlayerTask.SHADOW_WARRIOR, SlayerTask.TUROTH, SlayerTask.VYREWATCH, SlayerTask.WARPED_TORTOISE),

		SUMONA(7780, 85, 35, new int[]
		{ 12, 60, 180 }, new int[]
		{ 120, 185 }, SlayerTask.ABERRANT_SPECTRE, SlayerTask.ABYSSAL_DEMON, SlayerTask.AQUANITE, SlayerTask.BANSHEE, SlayerTask.BASILISK, SlayerTask.BLACK_DEMON, SlayerTask.BLOODVELD, SlayerTask.BLUE_DRAGON, SlayerTask.CAVE_CRAWLER, SlayerTask.CAVE_HORROR, SlayerTask.DAGANNOTH, SlayerTask.DESERT_STRYKEWYRM, SlayerTask.DUST_DEVIL, SlayerTask.ELF_WARRIOR, SlayerTask.FUNGAL_MAGE, SlayerTask.GARGOYLE, SlayerTask.GREATER_DEMON, SlayerTask.GRIFOLAPINE, SlayerTask.GRIFOLAROO, SlayerTask.GROTWORM, SlayerTask.HELLHOUND, SlayerTask.IRON_DRAGON, SlayerTask.JUNGLE_STRYKEWYRM, SlayerTask.KALPHITE, SlayerTask.KURASK, SlayerTask.JADINKO, SlayerTask.NECHRYAEL, SlayerTask.RED_DRAGON, SlayerTask.SCABARITE, SlayerTask.SPIRITUAL_MAGE, SlayerTask.SPIRITUAL_WARRIOR, SlayerTask.TERROR_DOG, SlayerTask.TROLL, SlayerTask.TUROTH, SlayerTask.VYREWATCH),
			
		KRYSTILIA(27663, 3, 1, new int[]
				{ 25, 125, 375}, new int[]
				{ 60, 120}, SlayerTask.BATTLE_MAGE, SlayerTask.GHOST, SlayerTask.ANKOU, SlayerTask.AVIANSIE, SlayerTask.BANDIT, SlayerTask.BEAR, SlayerTask.BLACK_DEMON, SlayerTask.BLACK_DRAGON, SlayerTask.DARK_WARRIOR, SlayerTask.EARTH_WARRIOR, SlayerTask.ENT, SlayerTask.FIRE_GIANT, SlayerTask.GREATER_DEMON, SlayerTask.GREEN_DRAGON, SlayerTask.HELLHOUND, SlayerTask.ICE_GIANT, SlayerTask.ICE_WARRIOR, SlayerTask.LAVA_DRAGON, SlayerTask.MAGIC_AXE, SlayerTask.MAMMOTH, SlayerTask.ROGUE, SlayerTask.SCORPION, SlayerTask.SKELETON, SlayerTask.SPIDER, SlayerTask.SPIRITUAL_MAGE, SlayerTask.WILDERNESS_DEMIBOSSES, SlayerTask.WILDERNESS_BOSSES),
			
		
		DURADEL(8466, 100, 50, new int[]
		{ 15, 75, 225 }, new int[]
		{ 130, 200 }, SlayerTask.FOSSIL_ISLAND_WYVERN, SlayerTask.LIZARDMAN, SlayerTask.KRAKEN, SlayerTask.SMOKE_DEVIL, SlayerTask.ABERRANT_SPECTRE, SlayerTask.ABYSSAL_DEMON, SlayerTask.AQUANITE, SlayerTask.BLACK_DEMON, SlayerTask.BLACK_DRAGON, SlayerTask.BLOODVELD, SlayerTask.DAGANNOTH, SlayerTask.DARK_BEAST, SlayerTask.DESERT_STRYKEWYRM, SlayerTask.DUST_DEVIL, SlayerTask.FIRE_GIANT, SlayerTask.FUNGAL_MAGE, SlayerTask.GANODERMIC, SlayerTask.GARGOYLE, SlayerTask.GORAK, SlayerTask.GREATER_DEMON, SlayerTask.GRIFOLAPINE, SlayerTask.GRIFOLAROO, SlayerTask.GROTWORM, SlayerTask.HELLHOUND, SlayerTask.ICE_STRYKEWYRM, SlayerTask.IRON_DRAGON, SlayerTask.JUNGLE_STRYKEWYRM, SlayerTask.KALPHITE, SlayerTask.MITHRIL_DRAGON, SlayerTask.JADINKO, SlayerTask.NECHRYAEL, SlayerTask.SCABARITE, SlayerTask.SKELETAL_WYVERN, SlayerTask.SPIRITUAL_MAGE, SlayerTask.STEEL_DRAGON, SlayerTask.SUQAH, SlayerTask.VYREWATCH, SlayerTask.WARPED_TERRORBIRD, SlayerTask.WATERFIEND),

		LAPALOK(8467, 100, 50, new int[]
		{ 15, 75, 225 }, new int[]
		{ 130, 200 }, SlayerTask.FOSSIL_ISLAND_WYVERN, SlayerTask.LIZARDMAN,SlayerTask.KRAKEN, SlayerTask.SMOKE_DEVIL, SlayerTask.ABERRANT_SPECTRE, SlayerTask.ABYSSAL_DEMON, SlayerTask.AQUANITE, SlayerTask.BLACK_DEMON, SlayerTask.BLACK_DRAGON, SlayerTask.BLOODVELD, SlayerTask.DAGANNOTH, SlayerTask.DARK_BEAST, SlayerTask.DESERT_STRYKEWYRM, SlayerTask.DUST_DEVIL, SlayerTask.FIRE_GIANT, SlayerTask.FUNGAL_MAGE, SlayerTask.GANODERMIC, SlayerTask.GARGOYLE, SlayerTask.GORAK, SlayerTask.GREATER_DEMON, SlayerTask.GRIFOLAPINE, SlayerTask.GRIFOLAROO, SlayerTask.GROTWORM, SlayerTask.HELLHOUND, SlayerTask.ICE_STRYKEWYRM, SlayerTask.IRON_DRAGON, SlayerTask.JUNGLE_STRYKEWYRM, SlayerTask.KALPHITE, SlayerTask.MITHRIL_DRAGON, SlayerTask.JADINKO, SlayerTask.NECHRYAEL, SlayerTask.SCABARITE, SlayerTask.SKELETAL_WYVERN, SlayerTask.SPIRITUAL_MAGE, SlayerTask.STEEL_DRAGON, SlayerTask.SUQAH, SlayerTask.VYREWATCH, SlayerTask.WARPED_TERRORBIRD, SlayerTask.WATERFIEND),
		
		

		KONAR_QUO_MATEN(28623, 75, 1, new int[]
				{ 10, 50, 100 }, new int[]
				{ 110, 170 }, SlayerTask.BASILISK, SlayerTask.WYRM, SlayerTask.DRAKE, SlayerTask.HYDRA, SlayerTask.FROST_DRAGON, SlayerTask.FOSSIL_ISLAND_WYVERN, SlayerTask.LIZARDMAN, SlayerTask.GANODERMIC, SlayerTask.WATERFIEND, SlayerTask.AQUANITE, SlayerTask.BATTLE_MAGE, SlayerTask.ENT, SlayerTask.KRAKEN, SlayerTask.SMOKE_DEVIL, SlayerTask.ABERRANT_SPECTRE, SlayerTask.ABYSSAL_DEMON, SlayerTask.ANKOU, SlayerTask.AVIANSIE, SlayerTask.BLACK_DEMON, SlayerTask.BLACK_DRAGON, SlayerTask.BLOODVELD, SlayerTask.BLUE_DRAGON, SlayerTask.BRINE_RAT, SlayerTask.CAVE_HORROR, SlayerTask.DAGANNOTH, SlayerTask.DARK_BEAST, SlayerTask.DUST_DEVIL, SlayerTask.ELF_WARRIOR,  SlayerTask.FIRE_GIANT, SlayerTask.GARGOYLE, SlayerTask.GREATER_DEMON, SlayerTask.HELLHOUND, SlayerTask.IRON_DRAGON, SlayerTask.KALPHITE, SlayerTask.KURASK, SlayerTask.MITHRIL_DRAGON, SlayerTask.NECHRYAEL, SlayerTask.RED_DRAGON, SlayerTask.SCABARITE, SlayerTask.SKELETAL_WYVERN, SlayerTask.SPIRITUAL_MAGE, SlayerTask.STEEL_DRAGON, SlayerTask.SUQAH, SlayerTask.TROLL, SlayerTask.TUROTH, SlayerTask.TZHAAR, SlayerTask.ZYGOMITE, SlayerTask.TORMENTED_DEMON, SlayerTask.GLACOR, SlayerTask.JUNGLE_STRYKEWYRM, SlayerTask.DESERT_STRYKEWYRM, SlayerTask.ICE_STRYKEWYRM, SlayerTask.JADINKO, SlayerTask.LIVING_ROCK ),

		
		NIEVE(26797, 85, 1, new int[]
				{ 12, 60, 180 }, new int[]
				{ 120, 185 }, SlayerTask.BASILISK, SlayerTask.WOLPERTINGER, SlayerTask.FROST_DRAGON, SlayerTask.FOSSIL_ISLAND_WYVERN, SlayerTask.LIZARDMAN, SlayerTask.GANODERMIC, SlayerTask.WATERFIEND, SlayerTask.AQUANITE, SlayerTask.BATTLE_MAGE, SlayerTask.ENT, SlayerTask.KRAKEN, SlayerTask.SMOKE_DEVIL, SlayerTask.ABERRANT_SPECTRE, SlayerTask.ABYSSAL_DEMON, SlayerTask.ANKOU, SlayerTask.AVIANSIE, SlayerTask.BLACK_DEMON, SlayerTask.BLACK_DRAGON, SlayerTask.BLOODVELD, SlayerTask.BLUE_DRAGON, SlayerTask.BRINE_RAT, SlayerTask.CAVE_HORROR, SlayerTask.DAGANNOTH, SlayerTask.DARK_BEAST, SlayerTask.DUST_DEVIL, SlayerTask.ELF_WARRIOR,  SlayerTask.FIRE_GIANT, SlayerTask.GARGOYLE, SlayerTask.GREATER_DEMON, SlayerTask.HELLHOUND, SlayerTask.IRON_DRAGON, SlayerTask.KALPHITE, SlayerTask.KURASK, SlayerTask.MITHRIL_DRAGON, SlayerTask.NECHRYAEL, SlayerTask.RED_DRAGON, SlayerTask.SCABARITE, SlayerTask.SKELETAL_WYVERN, SlayerTask.SPIRITUAL_MAGE, SlayerTask.STEEL_DRAGON, SlayerTask.SUQAH, SlayerTask.TROLL, SlayerTask.TUROTH, SlayerTask.TZHAAR, SlayerTask.ZYGOMITE, SlayerTask.TORMENTED_DEMON, SlayerTask.GLACOR, SlayerTask.JUNGLE_STRYKEWYRM, SlayerTask.DESERT_STRYKEWYRM, SlayerTask.ICE_STRYKEWYRM, SlayerTask.JADINKO, SlayerTask.LIVING_ROCK ),

		
		KURADAL(9085, 110, 75, new int[]
		{ 18, 90, 270 }, new int[]
		{ 120, 250 }, SlayerTask.BASILISK, SlayerTask.WOLPERTINGER, SlayerTask.WYRM, SlayerTask.DRAKE,SlayerTask.HYDRA, SlayerTask.FROST_DRAGON, SlayerTask.FOSSIL_ISLAND_WYVERN, SlayerTask.LIZARDMAN,  SlayerTask.AQUANITE, SlayerTask.BATTLE_MAGE, SlayerTask.RUNE_DRAGON, SlayerTask.ADAMANT_DRAGON, SlayerTask.TORMENTED_DEMON, SlayerTask.GLACOR, SlayerTask.KRAKEN, SlayerTask.SMOKE_DEVIL, SlayerTask.ABERRANT_SPECTRE, SlayerTask.ABYSSAL_DEMON, SlayerTask.BLACK_DEMON, SlayerTask.BLACK_DRAGON, SlayerTask.BLOODVELD, SlayerTask.BLUE_DRAGON, SlayerTask.DAGANNOTH, SlayerTask.DARK_BEAST, SlayerTask.JUNGLE_STRYKEWYRM, SlayerTask.DESERT_STRYKEWYRM, SlayerTask.ICE_STRYKEWYRM, SlayerTask.DUST_DEVIL, SlayerTask.FIRE_GIANT, SlayerTask.GANODERMIC, SlayerTask.GARGOYLE, SlayerTask.GRIFOLAPINE, SlayerTask.GRIFOLAROO, SlayerTask.GROTWORM, SlayerTask.HELLHOUND, SlayerTask.IRON_DRAGON, SlayerTask.KALPHITE, SlayerTask.LIVING_ROCK, SlayerTask.MITHRIL_DRAGON, SlayerTask.JADINKO, SlayerTask.NECHRYAEL, SlayerTask.SKELETAL_WYVERN, SlayerTask.SPIRITUAL_MAGE, SlayerTask.STEEL_DRAGON, SlayerTask.SUQAH, SlayerTask.TERROR_DOG, SlayerTask.TZHAAR, SlayerTask.TZHAAR, SlayerTask.VYREWATCH, SlayerTask.WARPED_TORTOISE, SlayerTask.WATERFIEND),
		/*MORVRAN(20112, 120, 85, new int[]
		{ 20, 100, 300 }, new int[]
		{ 150, 320 }, SlayerTask.ABYSSAL_DEMON, SlayerTask.KING_BLACK_DRAGON, SlayerTask.QUEEN_BLACK_DRAGON, SlayerTask.GIANT_MOLE, SlayerTask.GLACOR, SlayerTask.SKELETAL_WYVERN, SlayerTask.GANODERMIC, SlayerTask.DARK_BEAST, SlayerTask.JADINKO, SlayerTask.STEEL_DRAGON, SlayerTask.MITHRIL_DRAGON, SlayerTask.DARK_BEAST, SlayerTask.CORPOREAL_BEAST, SlayerTask.MEN, SlayerTask.UNICORN, SlayerTask.NEX, SlayerTask.GENERAL_GRAARDOR, SlayerTask.COMMANDER_ZILYANA, SlayerTask.KREE, SlayerTask.TSUTSAROTH, SlayerTask.CHAOS_ELEMENTAL, SlayerTask.TZTOK, SlayerTask.WILDYWYRM);
*/
		
		;

		private SlayerTask[] task;
		private int[] tasksRange, pointsRange;
		private int requriedCombatLevel, requiredSlayerLevel, npcId;

		private SlayerMaster(int npcId, int requriedCombatLevel, int requiredSlayerLevel, int[] pointsRange, int[] tasksRange, SlayerTask... task) {
			this.npcId = npcId;
			this.requriedCombatLevel = requriedCombatLevel;
			this.requiredSlayerLevel = requiredSlayerLevel;
			this.pointsRange = pointsRange;
			this.tasksRange = tasksRange;
			/*for (int i = 0; i < tasksRange.length; i++)
				tasksRange[i] /= 1.5;*/
			this.task = task;
		}

		public int getNPCId() {
			return npcId;
		}

		public int getRequiredCombatLevel() {
			return 1; //requriedCombatLevel;
		}

		public int getRequiredSlayerLevel() {
			return requiredSlayerLevel;
		}

		public SlayerTask[] getTask() {
			return task;
		}

		public int[] getTasksRange() {
			return tasksRange;
		}

		public int[] getPointsRange() {
			return pointsRange;
		}

		public static boolean startInteractionForId(Player player, int npcId, int option) {
			for (SlayerMaster master : SlayerMaster.values()) {
				if (master.getNPCId() == npcId) {
					if (option == 1)
						player.getDialogueManager().startDialogue("SlayerMasterD", npcId, master);
					else if (option == 2)
						player.getDialogueManager().startDialogue("QuickTaskD", master);
					else if (option == 3)
						ShopsHandler.openShop(player, 29);
					else if (option == 4) {
						player.getDialogueManager().startDialogue("SlayerRewardsD");
					//	player.getSlayerManager().sendSlayerInterface(SlayerManager.BUY_INTERFACE);
					}
					else if (option == 5)
						player.getDialogueManager().startDialogue("BossTaskD", master);
					return true;
				}
			}
			return false;
		}
	}

	public enum SlayerTask implements Serializable {// 79 matches out of 117

		//loc added
		MIGHTY_BANSHEE(37, new String[]
		{ "Banshees are fearsome creatures with a mighty scream.", " You need something to cover your ears", "Beware of their scream.", "Banshees are found in the Slayer Tower." }),

		// finished //loc added
		BANSHEE(new WorldTile(3437, 3561, 0), 15, new String[]
		{ "Banshees are fearsome creatures with a mighty scream.", " You need something to cover your ears", "Beware of their scream.", "Banshees are found in the Slayer Tower." }, MIGHTY_BANSHEE),

		// finished //loc added
		BAT(new WorldTile(3356, 3486, 0), 1, new String[] {}),

		//loc added
		BATTLE_MAGE(new WorldTile(2539, 4715, 0), 1, new String[] {}),
		
		//loc added
		KREE(1, 0.25D, new String[] {}),
		//loc added
		WINGMAN(1, new String[] {}),
		//loc added
		FLOCKLEADER(1, new String[] {}),
		//loc added
		FLIGHT(1, new String[] {}),
		//loc added
		AVIANSIE(new WorldTile(2908, 3707, 0), 1, 0.8, new String[] {}, KREE, WINGMAN, FLOCKLEADER, FLIGHT),

		COCKROACH(new WorldTile(3076, 3461, 0), 1, new String[] {}),
		
		//loc added
		// finished
		CHICKEN(1, new String[] {}),

		
		//loc added
		CHOMPY_BIRD(1, new String[] {}),

		//loc added
		DUCK(1, new String[] {}),

		//loc added
		BIRD(new WorldTile(3231, 3297, 0), 1, new String[]
		{
			"Birds are a type of species found throughout Matrix in different forms.",
			"It's recomended that you bring range weapons to fight these monsters.",
			"Avansies are the strongest and most widely known type of bird.",
			"Chickens are great for a fast task." }, AVIANSIE, CHICKEN, CHOMPY_BIRD, DUCK),

		//loc added
		CALLISTO(1, 0.136, new String[] {}),
		//loc added
		BEAR(new WorldTile(3230, 3503, 0), 1, new String[] {}, CALLISTO),

		//loc added
		CAVE_BUG(new WorldTile(3168, 3171, 0), 7, new String[] {}),

		//loc added
		CAVE_SLIME(new WorldTile(3168, 3171, 0), 17, new String[] {}),

		//loc added
		COW(new WorldTile(1821, 3503, 0), 1, new String[] {}),

		//loc added
		ZOMBIE_HAND(5, new String[] {}),

		//loc added
		SKELETAL_HAND(5, new String[] {}),

		// finished
		CRAWLING_HAND(new WorldTile(3416, 3558, 0), 5, new String[] {}, ZOMBIE_HAND, SKELETAL_HAND),

		//loc added
		DWARF(new WorldTile(3016, 3448, 0), 1, new String[] {}),
		
		
		SULPHUR_LIZARD(44, new String[] {}),
		
		//loc added, fix
		LIZARD(1, null),
		
		//loc added
		DESERT_LIZARD(new WorldTile(3383, 3018, 0), 22, new String[] {}, LIZARD, SULPHUR_LIZARD),

		//loc added
		REVENANT(1, 0.65, new String[] {}),
		TORMENTED_WRAITH(1, 0.65, new String[] {}),
		//loc added
		GHOST(new WorldTile(2322, 5227, 0), 1, new String[] {}, REVENANT, TORMENTED_WRAITH),

		//loc added
		GOBLIN(new WorldTile(3247, 3242, 0), 1, new String[] {}),

		//loc added
		ICEFIEND(new WorldTile(3008, 3477, 0), 1, new String[] {}),

		//loc added
		MINOTAUR(new WorldTile(1892, 5193, 0), 1, new String[] {}),

		GORILLA(1, new String[] {}),
		
		//loc added
		MONKEY(new WorldTile(2769, 2789, 0), 1, new String[] {}, GORILLA),

		//loc added
		SCORPIA(1, 0.136, new String[] {}),
		
		//loc added
		SCORPION(new WorldTile(3299, 3297, 0), 1, new String[] {}, SCORPIA),

		//loc added
		VENENATIS(1, 0.136, new String[] {}),
		//loc added
		VETION(1, 0.136, new String[] {}),
		
		//loc added
		SKELETON(new WorldTile(2832, 9656, 0), 1, new String[] {}, SKELETAL_HAND, VETION),

		//loc added
		SPIDER(new WorldTile(2839, 9582, 0), 1, new String[] {}, VENENATIS),

		//loc added
		WOLF(new WorldTile(2845, 3492, 0), 1, new String[] {}),

		//loc added
		ZOMBIE(new WorldTile(2146, 5075, 0), 1, new String[] {}),

		//loc added
		CATABLEPON(new WorldTile(2121, 5297, 0), 1, new String[] {}),

		//loc added
		CAVE_CRAWLER(new WorldTile(2787, 9998, 0), 10, new String[] {}),


		
		//loc added
		DOG(new WorldTile(2667, 9523, 0), 1, new String[] {} ),

		//loc added
		FLESH_CRAWLER(new WorldTile(1993, 5238, 0), 1, new String[] {}),

		//loc added
		HOBGOBLIN(new WorldTile(3124, 9875, 0), 1, new String[] {}),

		//loc added
		KALPHITE(new WorldTile(3228, 3106, 0), 1, new String[] {}),

		//loc added
		ROCKSLUG(new WorldTile(2799, 10018, 0), 20, new String[] {}),

		//loc added
		ROCK_SLUG(20, new String[] {}, ROCKSLUG),

		//loc added
		HOLE_IN_THE_WALL(35, new String[] {}),

		//loc added
		WALL_BEAST(new WorldTile(3168, 3171, 0), 35, new String[] {}, HOLE_IN_THE_WALL),

		//loc added
		DEVIANT_SPECTRE(60, new String[] {}),
		ABERRANT_SPECTRE(new WorldTile(3437, 3548, 1), 60, new String[] {}, DEVIANT_SPECTRE),

		//loc added
		ANKOU(new WorldTile(2360, 5240, 0), 1, new String[] {}),

		BASILISK_KNIGHT(60, new String[] {}),
		
		//loc added
		BASILISK(new WorldTile(2744, 10011, 0), 40, new String[] {}, BASILISK_KNIGHT),

		//loc added
		BLOODVELD(new WorldTile(3420, 3563, 1), 50, new String[] {}),

		//loc added
		BRINE_RAT(new WorldTile(2708, 10132, 0), 47, new String[] {}),

		//loc added
		COCKATRICE(new WorldTile(2793, 10035, 0), 25, new String[] {}),

		//loc added
		CROCODILE(new WorldTile(3303, 2913, 0), 1, 0.5, new String[] {}),

		//loc added
		CYCLOPS(new WorldTile(1650, 10021, 0), 1, new String[] {}),

		//loc added
		CYCLOPSE(1, new String[] {}, CYCLOPS),

		//loc added
		DUST_DEVIL(new WorldTile(1712, 10016, 0), 65, new String[] {}),
		
		//loc added
		SMOKE_DEVIL(new WorldTile(2412, 3054, 0), 93, new String[] {}),
		
		//loc added
		KRAKEN(new WorldTile(2279, 3612, 0), 87, new String[] {}),

		//loc added
		EARTH_WARRIOR(new WorldTile(3131, 9914, 0), 1, 0.5, new String[] {}),

		//loc added
		GHOUL(new WorldTile(3433, 3461, 0), 1, new String[] {}),

		//loc added
		GREEN_DRAGON(new WorldTile(1919, 9012, 1), 1, new String[] {}),

		//loc added
		GROTWORM(new WorldTile(2990, 3237, 0), 1, 0.64, new String[] {}),

		//loc added
		HARPIE_BUG_SWARM(new WorldTile(2867, 3106, 0), 33, new String[] {}),

		//loc added
		HILL_GIANT(new WorldTile(3116, 9836, 0), 1, new String[] {}),

		//loc added
		ICE_GIANT(new WorldTile(3053, 9579, 0), 1, new String[] {}),

		//loc added
		ICE_WARRIOR(new WorldTile(3053, 9579, 0), 1, new String[] {}),

		//loc added
		INFERNAL_MAGE(new WorldTile(3440, 3560, 1), 45, new String[] {}),

		//loc added
		JELLY(new WorldTile(2705, 10027, 0), 52, new String[] {}),

		//loc added
		JUNGLE_HORROR(new WorldTile(3704, 3012, 0), 1, 0.64, new String[] {}),

		//loc added
		LESSER_DEMON(new WorldTile(2835, 9561, 0), 1, new String[] {}),

		//loc added
		MOLANISK(new WorldTile(2412, 4434, 0), 39, new String[] {}),

		//loc added
		MOSS_GIANT(new WorldTile(3158, 9904, 0), 1, new String[] {}),

		//loc added
		MOGRE(new WorldTile(2986, 3109, 0), 31, new String[] {}),
		
		//loc added
		OGRE(new WorldTile(2512, 3086, 0), 1, new String[] {}, MOGRE),

		//loc added
		OTHERWORLDLY_BEING(new WorldTile(2387, 4427, 0), 1, new String[] {}),

		//loc added
		PYREFIEND(new WorldTile(2762, 10005, 0), 30, new String[] {}),

		//loc added
		SHADE(new WorldTile(2360, 5210, 0), 1, new String[] {}),

		//loc added
		SHADOW_WARRIOR(new WorldTile(2728, 3347, 0), 1, new String[] {}),

		//loc added
		TUROTH(new WorldTile(2723, 10006, 0), 55, new String[] {}),

		//loc added
		VAMPYRE(new WorldTile(3456, 3234, 0), 1, new String[] {}),

		//loc added
		WEREWOLF(new WorldTile(3497, 3476, 0), 1, new String[] {}),

		VORKATH(1, new String[] {}),
		
		//loc added
		BLUE_DRAGON(new WorldTile(2908, 9803, 0), 1, 0.8, new String[] {}, VORKATH),

		//loc added
		BRONZE_DRAGON(new WorldTile(2734, 9489, 0), 1, 0.35, new String[] {}),

		//loc added
		CAVE_HORROR(new WorldTile(3749, 2973, 0), 58, new String[] {}),

		//loc added
		DAGANNOTH(new WorldTile(2510, 3644, 0), 1, 0.96, new String[] {}),

		//loc added
		ELF_WARRIOR(new WorldTile(2203, 3253, 0), 1, 0.49, new String[] {}),

		//loc added
		FEVER_SPIDER(new WorldTile(2149, 5097, 0), 42, new String[] {}),

		//loc added
		FIRE_GIANT(new WorldTile(2511, 3463, 0), 1, new String[] {}),

		//loc added
		FUNGAL_MAGE(new WorldTile(3408, 3327, 0), 1, new String[] {}),

		//loc added
		DAWN(75, new String[] {}),
		DUSK(75, new String[] {}),
		GARGOYLE(new WorldTile(3442, 3548, 2), 75, new String[] {}, DAWN, DUSK),

		//loc added
		GRIFOLAPINE(new WorldTile(3408, 3327, 0), 88, 0.316, new String[] {}),

		//loc added
		GRIFOLAROO(new WorldTile(3408, 3327, 0), 82, 0.316, new String[] {}),

		//loc added
		JUNGLE_STRYKEWYRM(new WorldTile(2451, 2902, 0), 73, new String[] {}),

		//loc added
		KURASK(new WorldTile(2701, 9999, 0), 70, new String[] {}),

		//loc added
		FUNGI(57, 0.81, new String[] {}),

		//loc added
		ZYGOMITE(new WorldTile(2415, 4470, 0), 57, new String[] {}, FUNGI),

		//loc added
		VYRELORD(31, 0.68, new String[] {}),
		VYRELADY(31, 0.68, new String[] {}),
		VYREWATCH(new WorldTile(3626, 3365, 0), 31, 0.68, new String[] {}, VYRELORD, VYRELADY),

		//loc added
		WARPED_TORTOISE(new WorldTile(2321, 3100, 0), 56, new String[] {}),

		//loc added
		ABYSSAL_SIRE(85, new String[] {}),
		ABYSSAL_DEMON(new WorldTile(3419, 3571, 2), 85, new String[] {}, ABYSSAL_SIRE),

		//loc added
		AQUANITE(new WorldTile(2724, 9975, 0), 78, 0.96, new String[] {}),


		//loc added
		DEMONIC(1, new String[] {}),
		
		SKOTIZO(1, new String[] {}),

		TSUTSAROTH(1, 0.25D, new String[] {}),
		
		KARLAK(1,new String[] {}),
		
		ZAKL(1, new String[] {}),
		
		BALFRUG(1, new String[] {}),

		//loc added
		BLACK_DEMON(new WorldTile(2710, 9486, 0), 1, new String[] {}, DEMONIC, SKOTIZO, ZAKL, BALFRUG),

		//loc added
		DESERT_STRYKEWYRM(new WorldTile(3371, 3162, 0), 77, 0.64, new String[] {}),

		//loc added
		GREATER_DEMON(new WorldTile(2637, 9507, 2), 1, new String[] {}, SKOTIZO, TSUTSAROTH, KARLAK),

		//loc added
		CERBERUS(91, new String[] {}),
		//loc added
		HELLHOUND(new WorldTile(2860, 9840, 0), 1, 0.92, new String[] {}, CERBERUS),

		//loc added
		IRON_DRAGON(new WorldTile(2721, 9446, 0), 1, 0.468, new String[] {}),
		
		//loc added
		MUTATED_JADINKO_MALE(91, 0.88, new String[] {}),
		MUTATED_JADINKO_GAURD(86, 0.88, new String[] {}),
		MUTATED_JADINKO_BABY(80, 0.88, new String[] {}),

		JADINKO(new WorldTile(2948, 2956, 0), 80, 0.88, new String[] {}, MUTATED_JADINKO_MALE, MUTATED_JADINKO_GAURD, MUTATED_JADINKO_BABY),
		
		//loc added
		NECHRYAEL(new WorldTile(3438, 3570, 2), 80, 0.88, new String[] {}),

		//loc added
		RED_DRAGON(new WorldTile(2712, 9509, 0), 1, 0.43, new String[] {}),

		
		LOCUST(1, 0.4, new String[] {}),
		SCABARAS(1, 0.4, new String[] {}),
		SCARAB(1, 0.4, new String[] {}),
		SCABARITE(new WorldTile(2800, 5164, 0), 1, 0.4, new String[] {}, LOCUST, SCABARAS, SCARAB),

		//loc added
		SPIRITUAL_MAGE(new WorldTile(2917, 3738, 0), 83, 0.96, new String[] {}),

		//loc added
		SPIRITUAL_WARRIOR(new WorldTile(2917, 3738, 0),68, new String[] {}),

		//loc added
		TERROR_DOG(new WorldTile(3149, 4666, 0), 40, 0.28, new String[] {}),

		// a stupid troll at death plateau
		ROCK(1, 1.033, new String[] {}),
		//loc added
		TROLL(new WorldTile(2863, 3593, 0), 1, 1.033, new String[] {}, ROCK),

		
		BRUTAL_BLACK_DRAGON(77, new String[] {}),
		//loc added
		BLACK_DRAGON(new WorldTile(2835, 9825, 0), 1, 0.36, new String[] {}, BRUTAL_BLACK_DRAGON),

		
		
		//loc added
		DARK_BEAST(new WorldTile(1993, 4656, 0), 90, new String[] {}),

		//loc added
		GANODERMIC(new WorldTile(3408, 3327, 0), 95, 0.356, new String[] {}),

		//loc added
		GORAK(new WorldTile(3038, 5348, 0), 1, 0.4, new String[] {}),

		//loc added
		ICE_STRYKEWYRM(new WorldTile(3424, 5659, 0), 93, 0.64, new String[] {}),

		//loc added
		MITHRIL_DRAGON(new WorldTile(2512, 3511, 0), 1, 0.136, new String[] {}),

		
		SPITTING_WYVERN(66, new String[] {}),
		LONGTAILED_WYVERN(66, new String[] {}),
		TALONED_WYVERN(66, new String[] {}),
		ANCIENT_WYVERN(82, new String[] {}),
		FOSSIL_ISLAND_WYVERN(new WorldTile(3746, 3779, 0), 66, new String[] {}, TALONED_WYVERN, SPITTING_WYVERN, LONGTAILED_WYVERN, ANCIENT_WYVERN),
		
		//loc added
		SKELETAL_WYVERN(new WorldTile(3036, 9546, 0), 72, 0.36, new String[] {}),

		//loc added
		STEEL_DRAGON(new WorldTile(2721, 9446, 0), 1, 0.432, new String[] {}),
		
		//loc added
		ADAMANT_DRAGON(new WorldTile(1567, 5074, 0), 1, 0.432, new String[] {}),
		
		//loc added
		RUNE_DRAGON(new WorldTile(1567, 5074, 0), 1, 0.432, new String[] {}),
		
		LIZARDMAN(new WorldTile(1310, 3618, 0), 1, new String[] {}),

		//loc added
		SUQAH(new WorldTile(2102, 3870, 0), 1, 0.4, new String[] {}),

		// loc added
		WARPED_TERRORBIRD(new WorldTile(2321, 3100, 0), 56, new String[] {}),

		// loc added
		WATERFIEND(new WorldTile(2512, 3511, 0), 1, new String[] {}),

		// loc added
		LIVING_ROCK(new WorldTile(3655, 5113, 0), 1, 0.74, new String[] {}),

		JAL(1, 1, new String[] {}),
		// loc added
		TZHAAR(new WorldTile(4673, 5127, 0), 1, 0.44, new String[] {}, JAL),
		
		KING_BLACK_DRAGON(1, 0.67D, new String[] {}),
		
		QUEEN_BLACK_DRAGON(1, 0.50D, new String[] {}),
		
		GIANT_MOLE(1, new String[] {}),
		
		TORMENTED_DEMON(1, 0.2D, new String[] {}),
		
		GLACOR(56, 0.2D, new String[] {}),
		
		CORPOREAL_BEAST(1, 0.050D, new String[] {}),
		
		WOMEN(1, 3.50D, new String[] {}),
		
		MEN(1, 3.50D, new String[] {}, WOMEN),
		
		UNICORN(1, 3.50D, new String[] {}),
		
		CHAOS_ELEMENTAL(1, 0.15D, new String[] {}),
		
		WILDYWYRM(1, 0.025D, new String[] {}),
		
		WYRM(new WorldTile(1279, 10191, 0), 62, new String[] {}),
		DRAKE(new WorldTile(1314, 10237, 1), 84, new String[] {}),
		HYDRA(new WorldTile(1314, 10238, 0), 95, new String[] {}),
		
		WOLPERTINGER(new WorldTile(2564, 4936, 0), 97, new String[] {}),
		
		
		GENERAL_GRAARDOR(1, 0.25D, new String[] {}),
		
		COMMANDER_ZILYANA(1, 0.25D, new String[] {}),
		
		NEX(1, 0.050D, new String[] {}),
		
		TZTOK(1,  0.025D, new String[] {}),
		
		BANDIT(new WorldTile(3169, 2983, 0), 1, new String[] {}),
		
		DARK_WARRIOR(1, new String[] {}),
		ENT(new WorldTile(1661, 3506, 0), 1, new String[] {}),
		
		LAVA_DRAGON(1, new String[] {}),
		
		FROST_DRAGON(new WorldTile(3033, 9599, 0), 1, new String[] {}),
		
		MAGIC_AXE(1, new String[] {}),
		
		MAMMOTH(1, new String[] {}),
		
		ROGUE(1, new String[] {}),
		
		CRAB(new WorldTile(3732, 3845, 0), 1, new String[] {}),
		
		CRAZY_ARCHAEOLOGIST(1, 0.136, new String[] {}),
		CHAOS_FANATIC(1, 0.136, new String[] {}),
		WILDERNESS_DEMIBOSSES(1, 0.136, new String[] {}, CRAZY_ARCHAEOLOGIST, CHAOS_FANATIC, SCORPIA),
		WILDERNESS_BOSSES(1, 0.136, new String[] {}, CALLISTO, VENENATIS, VETION);
		;

		private String[] tips;
		private double taskFactor;
		private SlayerTask[] alternatives;
		private int levelRequried;
		private WorldTile tile;
		
		private SlayerTask(WorldTile tile, int levelRequried, double taskFactor, String[] tips, SlayerTask... alternatives) {
			this.levelRequried = levelRequried;
			this.taskFactor = taskFactor;
			this.tips = tips;
			this.tile = tile;
			this.alternatives = alternatives;
		}

		private SlayerTask(int levelRequried, double taskFactor, String[] tips, SlayerTask... alternatives) {
			this.levelRequried = levelRequried;
			this.taskFactor = taskFactor;
			this.tips = tips;
			this.alternatives = alternatives;
		}

		private SlayerTask(WorldTile tile, int levelRequried, String[] tips, SlayerTask... alternatives) {
			this(tile, levelRequried, 1, tips, alternatives);
		}
		
		private SlayerTask(int levelRequried, String[] tips, SlayerTask... alternatives) {
			this(levelRequried, 1, tips, alternatives);
		}

		public String[] getTips() {
			return tips;
		}

		public SlayerTask[] getAlternatives() {
			return alternatives;
		}

		public int getLevelRequried() {
			return levelRequried;
		}

		public String getName() {
			return Utils.formatPlayerNameForDisplay(toString());
		}

		public double getTaskFactor() {
			return taskFactor;
		}
		
		public WorldTile getTile() {
			return tile;
		}
	}

	public static int getLevelRequirement(String name) {
		for (SlayerTask task : SlayerTask.values()) {
			if (name.replace("'", "").replace("-", "").toLowerCase().contains(task.toString().replace("_", " ").toLowerCase()))
				return task.getLevelRequried();
		}
		return 1;
	}

	public static boolean hasNosepeg(Entity target) {
		if (!(target instanceof Player))
			return true;
		Player targetPlayer = (Player) target;
		int hat = targetPlayer.getEquipment().getHatId();
		return hat == 4168 || hasSlayerHelmet(target);
	}

	public static boolean hasEarmuffs(Entity target) {
		if (!(target instanceof Player))
			return true;
		Player targetPlayer = (Player) target;
		int hat = targetPlayer.getEquipment().getHatId();
		return hat == 4166 || hat == 13277 || hasSlayerHelmet(target);
	}

	public static boolean hasMask(Entity target) {
		if (!(target instanceof Player))
			return true;
		Player targetPlayer = (Player) target;
		int hat = targetPlayer.getEquipment().getHatId();
		return hat == 1506 || hat == 4164 || hat == 13277 || hasSlayerHelmet(target);
	}

	public static boolean hasWitchWoodIcon(Entity target) {
		if (!(target instanceof Player))
			return true;
		Player targetPlayer = (Player) target;
		int hat = targetPlayer.getEquipment().getAmuletId();
		return hat == 8923;
	}

	public static boolean hasSlayerHelmet(Entity target) {
		if (!(target instanceof Player))
			return true;
		Player targetPlayer = (Player) target;
		int hat = targetPlayer.getEquipment().getHatId();
		return hat == 13263 || hat == 14636 || hat == 14637 || hasFullSlayerHelmet(target);
	}

	public static boolean hasFullSlayerHelmet(Entity target) {
		if (!(target instanceof Player))
			return true;
		Player targetPlayer = (Player) target;
		int hat = targetPlayer.getEquipment().getHatId();
		return hat == 25499 || hat == 15492 || hat == 15496 || hat == 15497 || (hat >= 22528 && hat <= 22550);
	}

	public static boolean hasReflectiveEquipment(Entity target) {
		if (!(target instanceof Player))
			return true;
		Player targetPlayer = (Player) target;
		int shieldId = targetPlayer.getEquipment().getShieldId();
		return shieldId == 4156;
	}

	public static boolean hasSpinyHelmet(Entity target) {
		if (!(target instanceof Player))
			return true;
		Player targetPlayer = (Player) target;
		int hat = targetPlayer.getEquipment().getHatId();
		return hat == 4551 || hat == 13105 || hasSlayerHelmet(target);
	}

	public static boolean isUsingBell(final Player player) {
		player.lock(3);
		player.setNextAnimation(new Animation(6083));
		List<WorldObject> objects = World.getRegion(player.getRegionId()).getAllObjects();
		if (objects == null)
			return false;
		for (final WorldObject object : objects) {
			if (!object.withinDistance(player, 3) || object.getId() != 22545)
				continue;
			player.getPackets().sendGameMessage("The bell re-sounds loudly throughout the cavern.");
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					NPC npc = World.spawnNPC(5751, player, -1, true);
					npc.getCombat().setTarget(player);
					WorldObject o = new WorldObject(object);
					o.setId(22544);
					World.spawnObjectTemporary(o, 30000);
				}
			}, 1);
			return true;
		}
		return false;
	}

	public static boolean isBlackMask(int requestedId) {
		return requestedId >= 8901 && requestedId <= 8920;
	}

	private static final int[] SLAYER_HELMET_PARTS =
	{ 8921, 4166, 4164, 4551, 4168 };
	private static final int[] FULL_SLAYER_HELMET_PARTS =
	{ 13263, 15490, 15488 };

	public static boolean createSlayerHelmet(Player player, int itemUsed, int itemUsedWith) {
		if (itemUsed == itemUsedWith)
			return false;
		boolean firstCycle = false, secondCycle = false, full = false;
		for (int parts : SLAYER_HELMET_PARTS) {
			if (itemUsed == parts)
				firstCycle = true;
			if (itemUsedWith == parts)
				secondCycle = true;
		}
		if (!firstCycle || !secondCycle) {
			firstCycle = false;
			secondCycle = false;
			for (int parts : FULL_SLAYER_HELMET_PARTS) {
				if (itemUsed == parts)
					firstCycle = true;
				if (itemUsedWith == parts)
					secondCycle = true;
			}
			full = true;
		}
		if (firstCycle && secondCycle) {
			if (!player.getSlayerManager().hasLearnedSlayerHelmet()) {
				player.getPackets().sendGameMessage("You don't know what to do with these parts. You should talk to an expert, perhaps they know how to assemble these parts.");
				return true;
			} else if (player.getSkills().getLevel(Skills.CRAFTING) < 55) {
				player.getPackets().sendGameMessage("You need a Crafting level of 55 in order to assemble a slayer helmet.");
				return true;
			}
			for (int parts : (full ? FULL_SLAYER_HELMET_PARTS : SLAYER_HELMET_PARTS))
				if (!player.getInventory().containsItem(parts, 1))
					return false;
			for (int parts : (full ? FULL_SLAYER_HELMET_PARTS : SLAYER_HELMET_PARTS))
				player.getInventory().deleteItem(parts, 1);
			player.getInventory().addItem(new Item(full ? 15492 : FULL_SLAYER_HELMET_PARTS[0], 1));
			player.getPackets().sendGameMessage(full ? "You attach two parts to your slayer helmet." : "You combine all parts of the helmet.");
			return true;
		}
		return false;
	}

	public static void dissasembleSlayerHelmet(Player player, boolean full) {
		if (!(player.getInventory().getFreeSlots() >= (full ? 2 : 4))) {
			player.getPackets().sendGameMessage("You don't have enough space in your inventory to dissassemble the helmet.");
			return;
		}
		player.getInventory().deleteItem(full ? 15492 : 13263, 1);
		if (full) {
			for (int parts : FULL_SLAYER_HELMET_PARTS)
				player.getInventory().addItemDrop(parts, 1);
		} else {
			for (int parts : SLAYER_HELMET_PARTS)
				player.getInventory().addItemDrop(parts, 1);
		}
	}

	public static boolean isSlayerHelmet(Item item) {
		return item.getName().toLowerCase().contains("slayer helm");
	}
}
