package com.rs.game.player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.rs.game.player.controllers.NomadsRequiem;
import com.rs.utils.Utils;

public final class QuestManager implements Serializable {

	private static final long serialVersionUID = -8085932531253271252L;

	public static Quests[] REQUIRED_QUESTS = {Quests.DRAGON_SLAYER, Quests.THE_HUNT_FOR_SUROK, Quests.THE_GREAT_BRAIN_ROBBERY, Quests.IN_PYRE_NEED, Quests.HEROES_QUEST_2, Quests.NOMADS_REQUIEM};
	
	public static enum Quests {
		THE_TALE_OF_THE_MUSPAH,
		PERIL_OF_ICE_MONTAINS,
		VOID_STARES_BACK,
		KINGS_RANSOM,
		ELEMENTAL_WORKSHOP_I,
		IN_SEARCH_OF_THE_MYREQUE,
		LEGENDS_QUEST,
		THE_SLUG_MENACE,
		LUNAR_DIPLOMACY,
		FAIRY_RING_III,
		FAMILY_CREST,
		THE_EYES_OF_GLOUPHIE,
		AS_A_FIRST_RESORT,
		RECIPE_FOR_DISASTER,
		EASTER_2014,
		//quests done properly
		DRAGON_SLAYER,
		THE_GREAT_BRAIN_ROBBERY,
		IN_PYRE_NEED,
		THE_HUNT_FOR_SUROK,
		NOMADS_REQUIEM,
		HEROES_QUEST_2,
		//not used. null
		HEROES_QUEST, // remove if you ever reset accs
		;
	}

	private transient Player player;
	private List<Quests> completedQuests;
	private HashMap<Quests, Integer> questStages;

	public QuestManager() {
		completedQuests = new ArrayList<Quests>();
	}

	public void setPlayer(Player player) {
		this.player = player;
		if (questStages == null)
			questStages = new HashMap<Quests, Integer>();
	}

	public int getQuestStage(Quests quest) {
		if (completedQuests.contains(quest))
			return -1;
		Integer stage = questStages.get(quest);
		return stage == null ? -2 : stage;
	}

	public void setQuestStageAndRefresh(Quests quest, int stage) {
		setQuestStage(quest, stage);
		sendStageData(quest, stage);
	}

	public void setQuestStage(Quests quest, int stage) {
		if (completedQuests.contains(quest))
			return;
		questStages.put(quest, stage);
	}

	public void init() {
		checkCompleted(); // temporary
		for (Quests quest : completedQuests)
			sendCompletedQuestsData(quest);
		for (Quests quest : questStages.keySet())
			sendStageData(quest, questStages.get(quest));
	}

	public void checkCompleted() {
		if (player.getQuestManager().getQuestStage(Quests.NOMADS_REQUIEM) == -2) // for
			player.getQuestManager().setQuestStageAndRefresh(Quests.NOMADS_REQUIEM, 0);
		//note: these are all in cache
		if (!completedQuests.contains(Quests.PERIL_OF_ICE_MONTAINS) && player.getSkills().hasRequiriments(Skills.CONSTRUCTION, 10, Skills.FARMING, 10, Skills.HUNTER, 10, Skills.THIEVING, 11))
			completeQuest(Quests.PERIL_OF_ICE_MONTAINS);
		if (!completedQuests.contains(Quests.VOID_STARES_BACK) && player.getSkills().hasRequiriments(Skills.MAGIC, 80, Skills.ATTACK, 78, Skills.STRENGTH, 78, Skills.FIREMAKING, 71, Skills.CONSTRUCTION, 70, Skills.CRAFTING, 70, Skills.SMITHING, 70, Skills.SUMMONING, 55, Skills.DEFENCE, 10))
			completeQuest(Quests.VOID_STARES_BACK);
		if (!completedQuests.contains(Quests.KINGS_RANSOM) && player.getSkills().hasRequiriments(Skills.MAGIC, 45, Skills.DEFENCE, 65))
			completeQuest(Quests.KINGS_RANSOM);
		if (!completedQuests.contains(Quests.THE_TALE_OF_THE_MUSPAH) && player.getSkills().hasRequiriments(Skills.FIREMAKING, 6, Skills.MINING, 8, Skills.MAGIC, 10, Skills.WOODCUTTING, 10))
			completeQuest(Quests.THE_TALE_OF_THE_MUSPAH);
		if (!completedQuests.contains(Quests.ELEMENTAL_WORKSHOP_I) && player.getSkills().hasRequiriments(Skills.MINING, 20, Skills.SMITHING, 20, Skills.CRAFTING, 20))
			completeQuest(Quests.ELEMENTAL_WORKSHOP_I);
		if (!completedQuests.contains(Quests.IN_SEARCH_OF_THE_MYREQUE) && player.getSkills().hasRequiriments(Skills.AGILITY, 25))
			completeQuest(Quests.IN_SEARCH_OF_THE_MYREQUE);
		if (!completedQuests.contains(Quests.LEGENDS_QUEST) && player.getSkills().hasRequiriments(Skills.AGILITY, 50, Skills.CRAFTING, 50, Skills.HERBLORE, 45, Skills.MAGIC, 56, Skills.MINING, 52, Skills.PRAYER, 42, Skills.SMITHING, 50, Skills.STRENGTH, 50, Skills.THIEVING, 50, Skills.WOODCUTTING, 50))
			completeQuest(Quests.LEGENDS_QUEST);
		/*if (!completedQuests.contains(Quests.HEROES_QUEST_2) && player.getSkills().hasRequiriments(Skills.COOKING, 53, Skills.FISHING, 53, Skills.HERBLORE, 25, Skills.MINING, 50))
			completeQuest(Quests.HEROES_QUEST_2);*/
		if (!completedQuests.contains(Quests.THE_SLUG_MENACE) && player.getSkills().hasRequiriments(Skills.CRAFTING, 30, Skills.RUNECRAFTING, 30, Skills.SLAYER, 30, Skills.THIEVING, 30))
			completeQuest(Quests.THE_SLUG_MENACE);
		if (!completedQuests.contains(Quests.LUNAR_DIPLOMACY) && player.getSkills().hasRequiriments(Skills.CRAFTING, 61, Skills.DEFENCE, 40, Skills.FIREMAKING, 49, Skills.MINING, 60, Skills.HERBLORE, 5, Skills.MAGIC, 65, Skills.WOODCUTTING, 55))
			completeQuest(Quests.LUNAR_DIPLOMACY);
		if (!completedQuests.contains(Quests.FAIRY_RING_III) && player.getSkills().hasRequiriments(Skills.MAGIC, 59, Skills.FARMING, 54, Skills.THIEVING, 51, Skills.SUMMONING, 37, Skills.CRAFTING, 36))
			completeQuest(Quests.FAIRY_RING_III);
	/*	if (!completedQuests.contains(Quests.FAMILY_CREST) && player.getSkills().hasRequiriments(Skills.CRAFTING, 40, Skills.SMITHING, 40, Skills.MINING, 40, Skills.MAGIC, 59))
			completeQuest(Quests.FAMILY_CREST);*/
		if (!completedQuests.contains(Quests.THE_EYES_OF_GLOUPHIE) && player.getSkills().hasRequiriments(Skills.CONSTRUCTION, 5, Skills.MAGIC, 46))
			completeQuest(Quests.THE_EYES_OF_GLOUPHIE);
		if (!completedQuests.contains(Quests.AS_A_FIRST_RESORT) && player.getSkills().hasRequiriments(Skills.HUNTER, 41, Skills.FIREMAKING, 51, Skills.WOODCUTTING, 58))
			completeQuest(Quests.AS_A_FIRST_RESORT);
		if (!completedQuests.contains(Quests.RECIPE_FOR_DISASTER) && player.getSkills().hasRequiriments(Skills.COOKING, 70, Skills.MAGIC, 59, Skills.THIEVING, 53, Skills.FISHING, 53, Skills.MINING, 50, Skills.CRAFTING, 40, Skills.FIREMAKING, 50, Skills.WOODCUTTING, 36, Skills.AGILITY, 48, Skills.RANGE, 40, Skills.HERBLORE, 25, Skills.FLETCHING, 10, Skills.SLAYER, 10, Skills.SMITHING, 40))
			completeQuest(Quests.RECIPE_FOR_DISASTER);
	}
	
	public String getQuest(String name, Quests quest) {
		return "<col="+(completedQuest(quest) ? "00FF00" : questStages.containsKey(quest) ? "ffff00" : "FF0040")+">"+name;
		
	}

	public void completeQuest(Quests quest) {
		if (completedQuests.contains(quest))
			return;
		completedQuests.add(quest);
		questStages.remove(quest);
		sendCompletedQuestsData(quest);
		player.getPackets().sendGameMessage("<col=ff0000>You have completed quest: " + Utils.formatPlayerNameForDisplay(quest.toString()) + ".");
		// message completed quest
		if (quest == Quests.NOMADS_REQUIEM) {
			player.getSkills().addXp(Skills.PRAYER, 302400, true);
			player.getSkills().addXp(Skills.SLAYER, 91350, true);
			player.getInventory().addItemDrop(15432 + Utils.random(2), 1, NomadsRequiem.OUTSIDE);
			sendQuestInterface("Nomad's Requiem", "302,400 prayer XP<br>91,350 Slayer XP<br>Soul wars cape");
		} else if (quest == Quests.THE_HUNT_FOR_SUROK) {
			player.getSkills().addXp(Skills.SLAYER, 5000, true);
			player.getInventory().addItemDrop(14497, 1);
			player.getInventory().addItemDrop(14499, 1);
			player.getInventory().addItemDrop(14501, 1);
			sendQuestInterface("The Hunt for Surok", "5000 Slayer XP<br>Dagon'hai robes<br>Ability to fight bork daily");
		} else if (quest == Quests.DRAGON_SLAYER) {
			player.getSkills().addXp(Skills.STRENGTH, 18650, true);
			player.getSkills().addXp(Skills.DEFENCE, 18650, true);
			player.getInventory().addItemDrop(1127, 1);
			player.getInventory().addItemDrop(1135, 1);
			sendQuestInterface("Dragon slayer", "18,650 Strength XP<br>18,650 Defence XP<br>Rune platebody & Green d'hide body<br>Ability to equip Dragon Platebodies<br>Ability to use equipment that<br>protects agaisnt dragonfire");
		} else if (quest == Quests.THE_GREAT_BRAIN_ROBBERY) {
			player.getSkills().addXp(Skills.PRAYER, 6000, true);
			player.getSkills().addXp(Skills.CRAFTING, 3000, true);
			player.getSkills().addXp(Skills.CONSTRUCTION, 2000, true);
			player.getInventory().addItemDrop(10887, 1);
			sendQuestInterface("The Great Brain Robbery", "6,000 Prayer XP<br>3,000 Crafting XP<br>2,000 Construction XP<br>Barrelchest anchor");
		} else if (quest == Quests.IN_PYRE_NEED) {
			player.getSkills().addXp(Skills.FIREMAKING, 14400, true);
			player.getSkills().addXp(Skills.FLETCHING, 12500, true);
			player.getSkills().addXp(Skills.CRAFTING, 11556, true);
			player.getInventory().addItemDrop(7583, 1);
			sendQuestInterface("In Pyre Need", "14,400 Firemaking XP<br>12,500 Fletching XP<br>11,556 Crafting XP<br>Hell-kitten");
		} else if (quest == Quests.HEROES_QUEST_2) {
			player.getSkills().addXp(Skills.ATTACK, 3075, true);
			player.getSkills().addXp(Skills.DEFENCE, 3075, true);
			player.getSkills().addXp(Skills.STRENGTH, 3075, true);
			player.getSkills().addXp(Skills.HITPOINTS, 3075, true);
			player.getSkills().addXp(Skills.RANGE, 2075, true);
			player.getSkills().addXp(Skills.FISHING, 2725, true);
			player.getSkills().addXp(Skills.COOKING, 2825, true);
			player.getSkills().addXp(Skills.WOODCUTTING, 1575, true);
			player.getSkills().addXp(Skills.FIREMAKING, 1575, true);
			player.getSkills().addXp(Skills.SMITHING, 2257, true);
			player.getSkills().addXp(Skills.MINING, 2575, true);
			player.getSkills().addXp(Skills.HERBLORE, 1325, true);
			player.getInventory().addItemDrop(1377, 1);
			player.getInventory().addItemDrop(20659, 1);
			sendQuestInterface("Heroes' Quest", "Access to the Heroes' Guild<br>A total of 29,232, XP spread over twelve skills<br>Dragon battleaxe<br>Ring of wealth(4)");
		}
	}
	
	public void sendQuestInterface(String name, String rewards) {
		player.getInterfaceManager().sendInterface(1244);
		player.getPackets().sendIComponentText(1244, 25, "Congratulations! You have completed "+name+"!");
		player.getPackets().sendIComponentText(1244, 26, "You are awarded: <br>"+rewards);
		player.getPackets().sendMusicEffect(160);
	}

	public void sendCompletedQuestsData(Quests quest) {
		switch (quest) {
		case PERIL_OF_ICE_MONTAINS:
			player.getVarsManager().sendVarBit(4684, 150);
			break;
		case NOMADS_REQUIEM:
			player.getVarsManager().sendVarBit(6982, 1);//
			player.getVarsManager().sendVarBit(6962, 3);//enable tent
			break;
		case KINGS_RANSOM:
			player.getVarsManager().sendVarBit(3910, 1);
			break;
		case FAIRY_RING_III:
			player.getVarsManager().sendVarBit(7856, 1);
			player.getVarsManager().sendVarBit(7857, 1);
			break;
		case THE_EYES_OF_GLOUPHIE:
			player.getVarsManager().sendVarBit(2503, 1);
			break;
		case AS_A_FIRST_RESORT:
			/*if (player.getRedStoneDelay() >= Utils.currentTimeMillis())
				player.getVarsManager().sendVarBit(10133, 26);
			else if (player.getRedStoneCount() >= 50)
				player.getVarsManager().sendVarBit(10133, 25);
			player.getVarsManager().sendVarBit(4322, 1);*/
			break;
		default:
			break;
		}
	}

	private void sendStageData(Quests quest, int stage) {
		switch (quest) {
		case NOMADS_REQUIEM:
				player.getVarsManager().sendVarBit(6962, 3);
			break;
		case EASTER_2014:
			if (stage == 0) {
				//player.getCutscenesManager().play(EasterCutScene.class);
			}
			break;
		default:
			break;
		}
	}

	public boolean completedQuest(Quests quest) {
		return completedQuests.contains(quest);
	}
}
