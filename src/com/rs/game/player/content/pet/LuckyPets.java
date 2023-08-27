/**
 * 
 */
package com.rs.game.player.content.pet;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.zalcano.Zalcano;
import com.rs.game.player.Player;
import com.rs.game.player.content.collectionlog.CategoryType;
import com.rs.game.player.content.collectionlog.CollectionLog;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Oct 29, 2017
 */
public class LuckyPets {

	private static final int PET_DROP_RATE = 2; //2x rs
	
	public static enum BossPet {
		HORROR_ARM_1(LuckyPet.HORROR_1, -1),
		SS_PET(LuckyPet.SS_PET, -1),
		GALVEK(LuckyPet.GALVEK, 28097),
		ONYX(LuckyPet.ONYX, 15185),
		//CALLUS(LuckyPet.CALLUS, 21212),
		NEX1(LuckyPet.NEXTERMINATOR, 13447),
		NEX2(LuckyPet.NEXTERMINATOR, 13448),
		NEX3(LuckyPet.NEXTERMINATOR, 13449),
		NEX4(LuckyPet.NEXTERMINATOR, 13450),
		NEX5(LuckyPet.NEXTERMINATOR, 15776),
		GIANT_MOLE(LuckyPet.BABY_MOLE, 3340),
		KALPHITE_QUEEN(LuckyPet.KALHPITE_PRINCESS, 1160),
		CHAOS_ELEMENTAL(LuckyPet.PET_CHAOS_ELEMENTAL, 3200),
		DAGANNOTH_SUPREME(LuckyPet.PET_DAGANNOTH_SUPREME, 2881),
		DAGANNOTH_PRIME(LuckyPet.PET_DAGANNOTH_PRIME, 2882),
		DAGANNOTH_REX(LuckyPet.PET_DAGANNOTH_REX, 2883),
		CORPOREAL_BEAST(LuckyPet.PET_DARK_CORE, 8133),
		GRAARDOR(LuckyPet.PET_GENERAL_GRARDOR, 6260),
		KRIL_TSU(LuckyPet.PET_KRIL_TSUTAROTH, 6203),
		KREEARRA(LuckyPet.PET_KREE_ARRA, 6222),
		ZILYANA(LuckyPet.PET_ZILYANA, 6247),
		KBD(LuckyPet.PRINCE_BLACK_DRAGON, 50),
		ZALCANO(LuckyPet.SMOLCANO, Zalcano.ZALCANO_ID),
		CHAOS_FANATIC(LuckyPet.PET_CHAOS_ELEMENTAL_FANNATIC, 26619),
		VETION(LuckyPet.VETION_JR, 26612),
		VENENATIS(LuckyPet.VENENATIS_SPIDERLING, 26504),
		CALLISTO(LuckyPet.CALLISTO_CUB, 26503),
		SCORPIA(LuckyPet.SCORPIA_OFFSPRING, 26615),
		ZULRAH(LuckyPet.PET_SNAKELING, 22042),
		ZULRAH1(LuckyPet.PET_SNAKELING, 22043),
		ZULRAH2(LuckyPet.PET_SNAKELING, 22044),
		THERMONUCLEAR_SMOKE_DEVIL(LuckyPet.PET_SMOKE_DEVIL, 20499),
		KRAKEN(LuckyPet.PET_KRAKEN, 20494),
		SIRE(LuckyPet.ABYSSAL_ORPHAN, 25908),
		CERBERUS(LuckyPet.HELLPUPPY, 25862),
		SKOTIZO(LuckyPet.SKOTOS, 27286),
		VORKATH(LuckyPet.VORKI, 28061),
		DUSK(LuckyPet.NOON, 27889),
		HYDRA(LuckyPet.HYDRA, 28622),
		NOMAD(LuckyPet.NOMAD, 8528),
		PHOENIX(LuckyPet.PHOENIX_COMBAT, 8549),
		CRAWLING_HAND_1(LuckyPet.CREEPING_HAND, 1648),
		CRAWLING_HAND_2(LuckyPet.CREEPING_HAND, 1649),
		CRAWLING_HAND_3(LuckyPet.CREEPING_HAND, 1650),
		CRAWLING_HAND_4(LuckyPet.CREEPING_HAND, 1651),
		CRAWLING_HAND_5(LuckyPet.CREEPING_HAND, 1652),
		CRAWLING_HAND_6(LuckyPet.CREEPING_HAND, 1653),
		CRAWLING_HAND_7(LuckyPet.CREEPING_HAND, 1654),
		CRAWLING_HAND_8(LuckyPet.CREEPING_HAND, 1655),
		CRAWLING_HAND_9(LuckyPet.CREEPING_HAND, 1656),
		CRAWLING_HAND_10(LuckyPet.CREEPING_HAND, 1657),
		MINITRICE(LuckyPet.MINITRICE, 1620),
		BASILISK(LuckyPet.BASILISK, 1616),
		BASILISK_2(LuckyPet.BASILISK, 1617),
		KURASK(LuckyPet.KURASK, 1608),
		KURASK_2(LuckyPet.KURASK, 1609),
		ABYSSAL_MINION(LuckyPet.ABYSSAL_MINION, 1615),
		ABYSSAL_MINION_2(LuckyPet.ABYSSAL_MINION, 27241),
		AQUANITE(LuckyPet.BABY_AQUANITE, 9172),
		ICE_STR(LuckyPet.FREEZY, 9463),
		
		AHRIM(LuckyPet.AHRIM, 2025),
		DHAROK(LuckyPet.DHAROK, 2026),
		GUTHAN(LuckyPet.GUTHAN, 2027),
		KARIL(LuckyPet.KARIL, 2028),
		TORAG(LuckyPet.TORAG, 2029),
		VERAC(LuckyPet.VERAC, 2030),
		
		CHUNGUS(LuckyPet.CHUNGUS, 25491)
		;
		
		;
		
		
		
		private LuckyPet pet;
		public int bossID;
		private BossPet(LuckyPet pet, int bossID) {
			this.pet = pet;
			this.bossID = bossID;
		}

		public static int forBoss(int npc) {
			for(BossPet pet : values()) {
				if(pet.bossID == npc) {
					return pet.pet.getPet().getBabyItemId();
				}
			}
			return -1;
		}
	}
	
	public static enum LuckyPet {
		//bosss
		HORROR_1(Pets.HORROR_LEFT_ARM, 300),
		HWEEN_2020(Pets.DEATH_PET, 50),
		XMAS_2020(Pets.INFERNAL_IMP, 50),
		SS_PET(Pets.SS_PET_1, 200),
		ABYSSAL_ORPHAN(Pets.ABYSSAL_ORPHAN, 5000),
		BABY_MOLE(Pets.BABY_MOLE, 3000),
		CALLISTO_CUB(Pets.CALLISTO_CUB, 2000),
		HELLPUPPY(Pets.HELLPUPPY, 3000),
		JAL_NIB_REK(Pets.JAL_NIB_REK, 100),
		SHRIMPY(Pets.SHRIMPY, 200),
		GALVEK(Pets.GALVEK, 200),
		ONYX(Pets.ONYX_1, 300),
		//CALLUS(Pets.CALLUS_1, 500),
		MIMIC(Pets.MIMI, 200),
		LILZIK(Pets.LILZIK, 650),
		//1 in 65 unique drops in rs. i think 1 in 200 which makes 1 in 100 is better
		NEXTERMINATOR(Pets.NEXTERMINATOR, 2000),
		QBD(Pets.QBD, 2500),
		KALHPITE_PRINCESS(Pets.KALHPITE_PRINCESS, 3000),
		OLMLET(Pets.OLMLET, 35),
		PET_CHAOS_ELEMENTAL(Pets.PET_CHAOS_ELEMENTAL, 300),
		PET_CHAOS_ELEMENTAL_FANNATIC(Pets.PET_CHAOS_ELEMENTAL, 1000),
		PET_DAGANNOTH_PRIME(Pets.PET_DAGANNOTH_PRIME, 5000),
		PET_DAGANNOTH_REX(Pets.PET_DAGANNOTH_REX, 5000),
		PET_DAGANNOTH_SUPREME(Pets.PET_DAGANNOTH_SUPREME, 5000),
		PET_DARK_CORE(Pets.PET_DARK_CORE, 5000),
		PET_GENERAL_GRARDOR(Pets.PET_GENERAL_GRAARDOR, 5000), 
		PET_KRIL_TSUTAROTH(Pets.PET_KRIL_TSUTAROTH, 5000), 
		PET_KRAKEN(Pets.PET_KRAKEN, 3000),
		PET_KREE_ARRA(Pets.PET_KREE_ARRA, 5000),
		PET_SMOKE_DEVIL(Pets.PET_SMOKE_DEVIL, 3000),
		PET_SNAKELING(Pets.PET_SNAKELING, 4000),
		PET_ZILYANA(Pets.PET_ZILYANA, 5000),
		PRINCE_BLACK_DRAGON(Pets.PRINCE_BLACK_DRAGON, 3000),
		SCORPIA_OFFSPRING(Pets.SCORPIA_OFFSPRING, 2000),
		SMOLCANO(Pets.SMOLCANO, 2250),
		HORDE(Pets.ZIO_THE_SLAVE, 30),
		SKOTOS(Pets.SKOTOS, /*65*/2000), //too easy otherwie due to unlimited entries
		TZREK_JAD(Pets.TZREK_JAD, 200),
		VENENATIS_SPIDERLING(Pets.VENENATIS_SPIDERLING, 2000),
		VETION_JR(Pets.VETION_JR, 2000),
		VORKI(Pets.VORKI, 3000),
		NOON(Pets.NOON, 3000),
		HYDRA(Pets.HYDRA, 3000),
		NIGHTMARE(Pets.LITTLE_NIGHTMARE, 4000),
		
		
		NOMAD(Pets.NOMAD, 2000),
		AHRIM(Pets.AHRIM, 6000),
		DHAROK(Pets.DHAROK, 6000),
		GUTHAN(Pets.GUTHAN, 6000),
		KARIL(Pets.KARIL, 6000),
		TORAG(Pets.TORAG, 6000),
		VERAC(Pets.VERAC, 6000),
		CHUNGUS(Pets.CHUNGUS, 5000),
		//slayer
		PHOENIX_COMBAT(Pets.MEAN_PHOENIX_EGGLING, 3000),
		CREEPING_HAND(Pets.CREEPING_HAND, 6000),
		MINITRICE(Pets.MINITRICE, 6000),
		BASILISK(Pets.BABY_BASILISK, 6000),
		KURASK(Pets.BABY_KURASK, 6000),
		ABYSSAL_MINION(Pets.ABYSSAL_MINION, 6000),
		FREEZY(Pets.FREEZY, 6000),
		BABY_AQUANITE(Pets.BABY_AQUANITE, 6000),
		//skilling
		BABY_CHINCHOMPA(Pets.BABY_CHINCHOMPA, 100000),
		BEAVER(Pets.BEAVER, 100000),
		GIANT_SQUIRREL(Pets.GIANT_SQUIRREL, 30000),
		HERON(Pets.HERON, 100000),
		RIFT_GUARDIAN(Pets.RIFT_GUARDIAN, 30000),
		RIFT_GUARDIAN_RUNESPAN(Pets.RIFT_GUARDIAN, 200000),
		ROCK_GOLEM(Pets.ROCK_GOLEM, 150000),
		ROCKY(Pets.ROCKY, 100000),
		TANGLEROOT(Pets.TANGLEROOT, 30000),
		PHOENIX_SKILL(Pets.CUTE_PHOENIX_EGGLING, 100000),
		COINS(Pets.CASH, 150),
		//clues
		BLOOHOUND(Pets.BLOODHOUND, 1000);
		;
		
		private Pets pet;
		private int chance;
		
		private LuckyPet(Pets pet, int chance) {
			this.pet = pet;
			this.chance = chance / PET_DROP_RATE;
		}

		public Pets getPet() {
			return pet;
		}
	}
	
	public static void checkBossPet(Player player, NPC npc) {
		for (BossPet pet : BossPet.values())
			if (pet.bossID == npc.getId()) {
				checkPet(player, pet.pet, npc.getName());
				break;
			}
	}

	public static boolean hasPet(Player player, int id) {
		for (LuckyPet pet : LuckyPet.values()) {
			if(id == pet.getPet().getBabyItemId()) {
				boolean follower = (player.getPet() != null && player.getPet().getPet() == pet.pet);
				return follower || player.containsItem(pet.getPet().getBabyItemId());
			}
		}

		return false;
	}
	public static boolean hasOneLuckyPet(Player player) {
		for (LuckyPet pet : LuckyPet.values()) {
			int item = pet.pet.getBabyItemId();
			if (player.getPetManager().getItemId() == item || player.containsItem(pet.pet.getBabyItemId()))
				return true;
		}
		return false;
	}
	public static boolean hasLuckyPet(Player player, LuckyPet pet) {
		int item = pet.pet.getBabyItemId();
		if ((player.getPet() != null && player.getPet().getItemId() == item) || player.getPetManager().getItemId() == item || player.containsItem(pet.pet.getBabyItemId()))
			return true;
		return false;
	}
	
	
	public static boolean checkPet(Player player, LuckyPet pet) {
		return checkPet(player, pet, null);
	}
	
	
	public static boolean checkPet(Player player, LuckyPet pet, String name) {
		if (player.getDungManager().isInside() || Utils.random(pet.chance) != 0)
			return false;
		int id = pet.pet.getBabyItemId();
		if (player.containsItem(id)
				|| (player.getPet() != null && player.getPet().getPet() == pet.pet))
			return false;
		if (player.getFamiliar() != null || player.getPet() != null) {
			player.getPackets().sendGameMessage("You feel something weird sneaking into your backpack.");
			player.getInventory().addItemDrop(id, 1);
		} else {
			player.getPackets().sendGameMessage("You have a funny feeling like you would have been followed...");
			player.getPetManager().spawnPet(id, false);
		}
		player.getCollectionLog().add(CategoryType.OTHERS, "Pets", new Item(id));
		if (name != null)
			player.getCollectionLog().add(CategoryType.BOSSES, name, new Item(id));
		World.sendNews(player, player.getDisplayName() + " has found a <col=ffff00>" + ItemConfig.forID(id).getName() + "<col=ff8c38>!", 1);
		return true;
	}
}
