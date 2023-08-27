package com.rs.game.player.content;

import com.rs.game.npc.others.YellowWizard;
import com.rs.game.player.Player;
import com.rs.game.player.QuestManager;
import com.rs.game.player.QuestManager.Quests;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Fishing;
import com.rs.game.player.content.pet.LuckyPets;
import com.rs.game.player.controllers.SawmillController;
import com.rs.game.player.controllers.SorceressGarden;
import com.rs.utils.BossKillsScore;
import com.rs.utils.Utils;

public final class AdventurersLog {

	public static final int TRIM_COUNT = 4, ELITE_COUNT = 4;
	
	public static String[] REQUIREMENTS =
	{
		"Level 120 in Dungeoneering",
		"Level 99 in all other skills",
		"Kill Every Boss 1x (::bosskc)",
		"Kill Every Slayer Monster 20x (::slayerkc)",
		"Complete all quests",
		"Win one game of Stealing Creation",
		"Win one game of Fight Pits",
		"Captured Flag at Castle Wars",
		"Win a Staked Duel in the Duel Arena",
		"Obtain a set of Elite void",
		"Obtain a Dragon defender from Warrior Guild",
		"Sell Mandrith a Statuette",
		"Mine a Shooting Star",
		"Cut an Evil Tree",
		"Unlock 300 music tracks",
		"Solve 10 Treasure Trails",
		"Obtain Golden Mining set from lava flow mining",
		"Obtain Lumberjack set from sawmill",
		"Obtain Black ibis set from sorceress's garden",
		"Obtain Runecrafter from yellow mage",
		"Obtain Fishing set from aerial fishing",
		"Win a Quick Reaction challenge",
		"Finish 30 Boss tasks",
		"200 Dominion Tower Kills",
		"Win 3 Trivia sessions",
		"Host a 10M Drop Party",
		"Successfully upgrade an item",
		"Give away 500m to the wishing well",
		"Complete achievement diary",
		"[Trim] Obtain a Boss or Skilling pet",
		"[Trim] Kill Every Boss 3x",
		"[Trim] 500 Dominion Tower Kills",
		"[Trim] VLevel 110 in Every skill",
		"[Trim] Win 10 Trivia sessions",
		"[Elite] N1 in a boss record (kills)",
		"[Elite] VLevel 112 in Every skill",
		"[Elite] Complete The Horde",
		"[Elite] Give away 2b to the wishing well!",
	};

	public static boolean hasRequirement(Player player, int idx) {
		switch (idx) {
		case 0: //120 dung
			if (player.getSkills().getLevelForXp(Skills.DUNGEONEERING) < 120)
				return false;
			return true;
		case 1: //99 all skills
			for (int skill = 0; skill < Skills.SKILL_NAME.length; skill++) {
				if (player.getSkills().getLevelForXp(skill) < 99)
					return false;
			}
			return true;
		case 2: //killl every boss x1
			return NPCKillLog.hasBossKills(player,  player.isSuperFast() ? 2 : 1);
		case 3: //kill every slayer npc x20
			return NPCKillLog.hasSlayerKills(player, player.isSuperFast() ? 50 : 20);
		case 4: //Defeat Nomad
			for (Quests quest : QuestManager.REQUIRED_QUESTS)
				if (!player.getQuestManager().completedQuest(quest))
					return false;
			return true;
			//return player.getQuestManager().completedQuest(Quests.NOMADS_REQUIEM);
		case 5: //Win one game of Stealing Creation
			return player.isCompletedStealingCreation();
		case 6: //Win one game of Fight Pits
			return player.isWonFightPits();
		case 7: //Captured flag at castle wars
			return player.isCapturedCastleWarsFlag();
		case 8:// Win a Staked Duel in the Duel Arena
			return player.isWonStackedDuel();
		case 9: //Obtain a set of Elite void
			return player.containsItem(19785) && player.containsItem(19786);
		case 10: //Obtain a Dragon defender
			return player.containsItem(20072) || player.containsItem(52322);
		case 11: //Sell Mandrith a Statuette
			return player.isSellMandrithStatuete();
		case 12: //Mine a Shooting Star
			return player.getLastStarSprite() != 0;
		case 13: //Cut an evil tree
			return player.getLastEvilTree() != 0;
		case 14: //"Unlock 300 music track
			return player.getMusicsManager().getUnlockedMusicsCount() >= 300;
		case 15: //Solve a Treasure Trail
			return player.getTreasureTrailsManager().getCluesCompleted() >= 10;
		case 16: //Obtain Golden Mining Set
			for (int i = 20787; i <= 20791; i++)
				if (!player.containsItem(i))
					return false;
			return true;
		case 17: //Obtain Golden Mining Set
			for (int i : SawmillController.PIECES)
				if (!player.containsItem(i))
					return false;
			return true;
		case 18:
			for (int i : SorceressGarden.PIECES)
				if (!player.containsItem(i))
					return false;
			return true;
		case 19:
			for (int i : YellowWizard.PIECES)
				if (!player.containsItem(i))
					return false;
			return true;
		case 20:
			for (int i : Fishing.PIECES)
				if (!player.containsItem(i))
					return false;
			return true;
		case 21:
			return true;//player.isWonReaction();
		case 22:
			return player.getBossTasksCompleted() >= (player.isSuperFast() ? 50 : 30);
		case 23:
			return player.getDominionTower().getKilledBossesCount() >= 200;
		case 24:
			return player.getWonTrivias() >= 3;
		case 25:
			return player.getDropPartyValue() >= 10000000  || player.isIronman() || player.isUltimateIronman()  || player.isHCIronman();
		//	return player.isGambledPartyhat() || player.isIronman() || player.isHCIronman();
		case 26:
			return player.isUgradedItem();
		case 27:
			return player.getThrownWishingCoins() >= 500000000;
		case 28:
			return player.getAchievements().isCompleted();
		case 29: //[Trim] Obtain either a Boss or Skilling pet
			return LuckyPets.hasOneLuckyPet(player);
		case 30: //[Trim] Kill Every Boss 3x
			return NPCKillLog.hasBossKills(player, 3);
		case 31: //[Trim] 500 Dominion Tower Kills
			return player.getDominionTower().getKilledBossesCount() >= 500;
		case 32: //[Trim] VLevel 120 in Every skill
			for (int skill = 0; skill < Skills.SKILL_NAME.length; skill++) {
				if (player.getSkills().getLevelForXp(skill, 120) < (110))
					return false;
			}
			return true;
		case 33:
			return player.getWonTrivias() >= 10;
			//elite here
		case 34: //boss record
			return BossKillsScore.hasRecord(player);
		case 35: //vlvl 120
			for (int skill = 0; skill < Skills.SKILL_NAME.length; skill++) {
				if (player.getSkills().getLevelForXp(skill, 120) < (112))
					return false;
			}
			return true;
		case 36: //the horde
			return player.isCompletedHorde();
		case 37: //give away 2b
			return player.getThrownWishingCoins() >= 2000000000;
		case 38:
			return player.getOsrsChambersCompletions() > 0;
		case 39:
			return player.getOsrsChambersCompletions() > 2;
		}
		return true;
	}


	private AdventurersLog() {

	}

	public static void open(Player player) {
		/*player.getInterfaceManager().sendInterface(623);
		player.getPackets().sendIComponentText(623, 66, "Completionist Cape Requirements");
		int value = 0;
		for (int component = 3; component < 41; component += 2) {
			int idx = (component - 3) / 2;
			String req = REQUIREMENTS[idx];
			if (req == "")
				player.getPackets().sendHideIComponent(623, component - 1, true);
			else {
				if (!hasRequirement(player, idx)) {
					req = "<col=FF0000>" + req;
					value += (int) Math.pow(2, idx + (idx > 10 ? 2 : 1));
				}
			}
			player.getPackets().sendIComponentText(623, component, req);
		}*/
		String[] lines = new String[REQUIREMENTS.length];
		for (int i = 0; i < REQUIREMENTS.length; i++) 
			lines[i] = (i+1)+". <col="+(hasRequirement(player, i) ? "00FF00" : "FF0000")+">"+REQUIREMENTS[i];
		NPCKillLog.sendQuestTab(player, "Completionist Cape Requirements", lines);
		player.getPackets().sendGameMessage("<col=FFBF00>" + player.getDominionTower().getKilledBossesCount() + "/500 dominion tower boss kills.");
		player.getPackets().sendGameMessage("<col=FFBF00>" + player.getBossTasksCompleted() + "/30 boss tasks.");
		player.getPackets().sendGameMessage("<col=FFBF00>" + player.getTreasureTrailsManager().getCluesCompleted()+ "/10 treasure trails.");
		player.getPackets().sendGameMessage("<col=FFBF00>" + player.getWonTrivias() + "/10 trivia sessions.");
		player.getPackets().sendGameMessage("<col=FFBF00>" + Utils.getFormattedNumber(player.getThrownWishingCoins()) + "/500.000.000 coins given away.");
		player.getPackets().sendGameMessage("<col=FFBF00>" + Utils.getFormattedNumber(player.getDropPartyValue()) + "/10.000.000 hosted on a drop party.");
		//player.getVarsManager().forceSendVar(2396, value);
/*		player.getPackets().sendGameMessage("<col=FE2EF7>You have: <col>");
		player.getPackets().sendGameMessage("<col=FFBF00>" + player.getKillCount() + "/" + TRIMMED_PVP_KILLS + " PvP kills.");
		player.getPackets().sendGameMessage("<col=FFBF00>" + player.getDominionTower().getKilledBossesCount() + "/" + DOM_KILLS + " dominion tower boss kills.");
		player.getPackets().sendGameMessage("<col=FFBF00>" + player.getFinishedCastleWars() + "/" + CASTLE_WARS_GAMES + " Castle Wars games completed.");
		player.getPackets().sendGameMessage("<col=FFBF00>" + player.getFinishedStealingCreations() + "/" + STEALING_CREATION_GAMES + " Stealing Creation games completed.");
	*/}
}
