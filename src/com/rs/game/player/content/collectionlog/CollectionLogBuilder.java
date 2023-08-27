package com.rs.game.player.content.collectionlog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.IntStream;

import com.rs.game.item.Item;
import com.rs.game.minigames.CastleWars;
import com.rs.game.minigames.LavaFlowMine;
import com.rs.game.minigames.PuroPuro;
import com.rs.game.minigames.pest.CommendationExchange;
import com.rs.game.npc.Drop;
import com.rs.game.npc.Drops;
import com.rs.game.npc.others.Mimic;
import com.rs.game.npc.others.YellowWizard;
import com.rs.game.npc.others.zalcano.Zalcano;
import com.rs.game.player.Player;
import com.rs.game.player.SuperiorSlayer;
import com.rs.game.player.TreasureTrailsManager;
import com.rs.game.player.content.pet.LuckyPets;
import com.rs.game.player.content.pet.LuckyPets.BossPet;
import com.rs.game.player.content.pet.Pets;
import com.rs.game.player.content.raids.TheatreOfBlood;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.ChambersRewards;
import com.rs.game.player.controllers.Barrows;
import com.rs.game.player.controllers.FightKiln;
import com.rs.game.player.controllers.Inferno;
import com.rs.game.player.controllers.SawmillController;
import com.rs.game.player.controllers.SorceressGarden;
import com.rs.game.player.controllers.TheHorde;
import com.rs.utils.NPCDrops;
import com.rs.utils.Utils;

/**
 * This class builds the MASTER instance of CollectionLog.
 * To be referenced by Player CollectionLog instance.
 *
 * @author Simplex
 * @since May 08, 2020
 */
public class CollectionLogBuilder {
    public static ArrayList<Integer> pets = new ArrayList<Integer>();
    public static ArrayList<ArrayList<Integer>> bosses = new ArrayList<ArrayList<Integer>>();

    static {
        Arrays.stream(LuckyPets.LuckyPet.values()).forEach(p -> pets.add(p.getPet().getBabyItemId()));
    }

    public static void build(CollectionLog log) {
        buildBosses(log.getBosses());
        buildClues(log.getClues());
        buildOthers(log.getOthers());
        buildMinigames(log.getMinigames());
        buildRaids(log.getRaids());
    }

    private static void buildBosses(LogCategory cat) {
        cat.init("Kree'arra", getNPCDrops(6222));
        cat.init("Commander Zilyana", getNPCDrops(6247));
        cat.init("General Graardor", getNPCDrops(6260));
        cat.init("K'ril Tsutsaroth", getNPCDrops(6203));
        cat.init("Nex", getNPCDrops(13447));
        cat.init("Dagannoth Rex", getNPCDrops(2883));
        cat.init("Dagannoth Prime", getNPCDrops(2882));
        cat.init("Dagannoth Supreme", getNPCDrops(2881));
        cat.init("Grotesque Guardians", getNPCDrops(27889));
        cat.init("Giant Mole", getNPCDrops(3340));
        cat.init("Kalphite Queen", getNPCDrops(1158));
        cat.init("King Black Dragon", getNPCDrops(50));
        cat.init("Queen Black Dragon", getNPCDrops(15454));
        cat.init("Callisto", getNPCDrops(26503));
        cat.init("Venenatis", getNPCDrops(26504));
        cat.init("Vet'ion Reborn", getNPCDrops(26612));
        cat.init("Chaos Elemental", getNPCDrops(3200));
        cat.init("Chaos Fanatic", getNPCDrops(26619));
        cat.init("Crazy archaeologist", getNPCDrops(26618));
        cat.init("Scorpia", getNPCDrops(26615));
        cat.init("Corporeal Beast", getNPCDrops(8133));
        cat.init("Zulrah", getNPCDrops(22042));
        cat.init("Cerberus", getNPCDrops(25862));
        cat.init("Kraken", getNPCDrops(20494));
        cat.init("Mimic", Utils.concatenate(new Integer[] {Pets.MIMI.getBabyItemId()}, Mimic.dropGenerator.getCat(Mimic.VERY_RARE_TABLE).dropListToIntArr()));
        cat.init("Thermonuclear smoke devil", getNPCDrops(20499));
        cat.init("Abyssal Sire", getNPCDrops(25908));
        cat.init("Skotizo", getNPCDrops(27286));
        cat.init("Skeletal horror", new int[] {54034, 54037, 54040, 54043});
        cat.init("Deranged archaeologist", getNPCDrops(27806));
        cat.init("Vorkath", getNPCDrops(28061));
        cat.init("Bork", getNPCDrops(7133));
        cat.init("WildyWyrm", getNPCDrops(3334));
        cat.init("Barrelchest", getNPCDrops(5666));
        cat.init("Phoenix", getNPCDrops(8549));
        cat.init("Galvek", getNPCDrops(28097));
        cat.init("Hati", getNPCDrops(13460));
        cat.init("Sköll", getNPCDrops(14836));
        cat.init("Onyx", getNPCDrops(15186));
        cat.init("Alchemical Hydra", getNPCDrops(28622));
        cat.init("Nomad", getNPCDrops(8528));
        cat.init("Corrupted Wolpertinger", getNPCDrops(16025));
        cat.init("The Nightmare", getNPCDrops(29464));
        cat.init("Zalcano", new int[] {Zalcano.CRYSTAL_TOOL_SEED, 53908, 6571, 53760});
    }
    
    private static Integer[] getNPCDrops(int id) {
    	Integer[] drops = 
    			
    			id == 29464 ?  new Integer[] {54419, 54420, 54421, 54422, 54417, 
    					54511, 54514, 54517, 54491} :
    						
    			id == 15454 ? new Integer[] {24365, 24352, 24338, 11286, 25445} :
    			NPCDrops.getDrops(id).dropsToIntArray(id != 15186);
    	int pet = BossPet.forBoss(id);
    	if (pet != -1) {
    		drops = Arrays.copyOf(drops, drops.length+1);
    		drops[drops.length-1] = pet;
    	}
    	return drops;
    }

    private static void buildMinigames(LogCategory cat) {
        cat.init("Barrows", Barrows.BARROW_REWARDS);
        cat.init("Castle Wars", CastleWars.rewards_int.toArray(new Integer[CastleWars.rewards_int.size()]));
        cat.init("Crucible", IntStream.range(24450, 24457).toArray());
        cat.init("Dominion Tower",  IntStream.range(22358, 22369).toArray());
        cat.init("Fight Caves", new int[] {6570, Pets.TZREK_JAD.getBabyItemId()});
        cat.init("Fight Kiln", new int[] {FightKiln.TOKHAAR_KAL, Pets.SHRIMPY.getBabyItemId(), 6571});
        cat.init("The Inferno", new int[] {Inferno.INFERNAL_CAPE, Pets.JAL_NIB_REK.getBabyItemId()});
        cat.init("The Horde", new int[] {TheHorde.AVAS_BLESSING, TheHorde.HORDE_BLESSING_AURA});
        cat.init("Lava Flow Mine", LavaFlowMine.MINING_SUIT);
        cat.init("Pest Control", CommendationExchange.VOID);
        cat.init("Puro-Puro", PuroPuro.REWARD);
        cat.init("Runespan", Utils.concatenate(new Integer[]{13642}, YellowWizard.PIECES));
        cat.init("Sorceress Garden", SorceressGarden.PIECES);
        cat.init("Sawmill", SawmillController.PIECES);
    }

    private static void buildOthers(LogCategory cat) {
        cat.init("Pets", pets.toArray(new Integer[pets.size()]));
    }

    private static void buildClues(LogCategory cat) {
        cat.init("Easy Clues", TreasureTrailsManager.EASY_RARE_REWARDS);
        cat.init("Medium Clues", TreasureTrailsManager.MEDIUM_RARE_REWARDS);
        cat.init("Hard Clues", TreasureTrailsManager.HARD_RARE_REWARDS);
        cat.init("Elite Clues", TreasureTrailsManager.ELITE_RARE_REWARDS);
        cat.init("Superior Slayer", Utils.concatenate(new Integer[] {Pets.SS_PET_1.getBabyItemId()}, SuperiorSlayer.superiorDropTable.getCat("Very Rare").dropListToIntArr()));
    }

    private static void buildRaids(LogCategory cat) {
        cat.init("Theatre of Blood",  TheatreOfBlood.UNIQUE_REWARDS);
        cat.init("Chambers of Xeric", Utils.concatenate(ChambersRewards.uniqueTable.getCollectionLog(), ChambersRewards.megaRare.getCollectionLog()));
    }
}
