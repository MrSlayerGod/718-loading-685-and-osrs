package com.rs.game.player;

import java.io.Serializable;
import java.util.TimerTask;

import com.rs.Settings;
import com.rs.executor.GameExecutorManager;
import com.rs.executor.WorldThread;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.minigames.stealingcreation.SCRewards;
import com.rs.game.npc.others.Hati;
import com.rs.game.npc.randomEvent.CombatEventNPC;
import com.rs.game.player.content.DollarContest;
import com.rs.game.player.content.pet.Pets;
import com.rs.utils.Utils;

public final class Skills implements Serializable {

	private static final long serialVersionUID = -7086829989489745985L;

	public static final double MAXIMUM_EXP = Integer.MAX_VALUE; //200000000
	public static final double RANDOM_EVENT_EXP = 100000;
	
	
	public static final int ATTACK = 0, DEFENCE = 1, STRENGTH = 2, HITPOINTS = 3, RANGE = 4, PRAYER = 5, MAGIC = 6, COOKING = 7, WOODCUTTING = 8, FLETCHING = 9, FISHING = 10, FIREMAKING = 11,
			CRAFTING = 12, SMITHING = 13, MINING = 14, HERBLORE = 15, AGILITY = 16, THIEVING = 17, SLAYER = 18, FARMING = 19, RUNECRAFTING = 20, CONSTRUCTION = 22, HUNTER = 21, SUMMONING = 23,
			DUNGEONEERING = 24;
	
	private static final int[] EXP_FOR_LVL = new int[150];
	
	static {
		
	    long points = 0;
	    for (int i = 0; i < EXP_FOR_LVL.length; i++) {
	        int l = i + 1;
	        long step = (int) (l + 300D * Math.pow(2D, l / 7D));
	        points += step;
	        EXP_FOR_LVL[i] = (int) (points / 4);
	       
	    }
	    EXP_FOR_LVL[149] = Integer.MAX_VALUE;
	}
	
	public static final int[] TRIM_CAPES = {
			
			9748, //attack
			9754, //def
			9751,//str
			9769, //hp
			9757, //rng
			9760, //prayer
			9763, //magic
			9802, //cook
			9808, //wc
			9784, //fletch
			9799, //fish
			9805, //fm
			9781, //craft
			9796, //smith
			9793, //mining
			9775, //herb
			9772, //agility
			9778, //thieving
			9787, //slayer
			9811, //farm
			9766, //rc
			9949, //hunter
			9790, //cons
			12170, //summon
			18509, //dung
			
			
	};

	public static final String[] SKILL_NAME =
	{
		"Attack",
		"Defence",
		"Strength",
		"Hitpoints", //Constitution
		"Ranged",
		"Prayer",
		"Magic",
		"Cooking",
		"Woodcutting",
		"Fletching",
		"Fishing",
		"Firemaking",
		"Crafting",
		"Smithing",
		"Mining",
		"Herblore",
		"Agility",
		"Thieving",
		"Slayer",
		"Farming",
		"Runecrafting",
		"Hunter",
		"Construction",
		"Summoning",
		"Dungeoneering" };

	public static final int[] MASTERY_REQUIREMENT = {200000000, 400000000, -1, 50000000};

	private short level[];
	private double xp[];
	private double[] xpTracks;
	private boolean[] trackSkills;
	private byte[] trackSkillsIds;
	private boolean xpDisplay, xpPopup;
	private int elapsedBonusMinutes;
	private double trackXPREvent;
	
	private int[] target, fromTarget;
	private boolean[] levelTarget;
	
	
	private transient double xpBonusTrack;

	private transient int currentCounter;
	private transient double[] temporaryXP;
	private transient Player player;

	public void passLevels(Player p) {
		this.level = p.getSkills().level;
		this.xp = p.getSkills().xp;
	}

	public Skills() {
		level = new short[25];
		xp = new double[25];
		target = new int[25];
		fromTarget = new int[25];
		levelTarget = new boolean[25];
		for (int i = 0; i < level.length; i++) {
			level[i] = 1;
			getXp()[i] = 0;
		}
		level[3] = 10;
		getXp()[3] = 1184;
		xpPopup = true;
		xpTracks = new double[3];
		trackSkills = new boolean[3];
		trackSkillsIds = new byte[3];
		trackSkills[0] = true;
		for (int i = 0; i < trackSkillsIds.length; i++)
			trackSkillsIds[i] = 30;

	}

	public void sendXPDisplay() {
		for (int i = 0; i < trackSkills.length; i++) {
			player.getVarsManager().sendVarBit(10444 + i, trackSkills[i] ? 1 : 0);
			player.getVarsManager().sendVarBit(10440 + i, trackSkillsIds[i] + 1);
			refreshCounterXp(i);
		}
		refreshXpPopup();
	}

	public void setupXPCounter() {
		player.getInterfaceManager().sendXPDisplay(1214);
	}

	public void refreshCurrentCounter() {
		player.getVarsManager().sendVar(2478, currentCounter + 1);
	}

	public void setCurrentCounter(int counter) {
		if (counter != currentCounter) {
			currentCounter = counter;
			refreshCurrentCounter();
		}
	}

	public void switchTrackCounter() {
		trackSkills[currentCounter] = !trackSkills[currentCounter];
		player.getVarsManager().sendVarBit(10444 + currentCounter, trackSkills[currentCounter] ? 1 : 0);
	}

	public void resetCounterXP() {
		xpTracks[currentCounter] = 0;
		refreshCounterXp(currentCounter);
	}

	public void setCounterSkill(int skill) {
		xpTracks[currentCounter] = 0;
		trackSkillsIds[currentCounter] = (byte) skill;
		player.getVarsManager().sendVarBit(10440 + currentCounter, trackSkillsIds[currentCounter] + 1);
		refreshCounterXp(currentCounter);
	}

	public void refreshCounterXp(int counter) {
		player.getVarsManager().sendVar(counter == 0 ? 1801 : 2474 + counter, (int) (xpTracks[counter] * 10));
	}

	public void handleSetupXPCounter(int componentId) {
		if (componentId == 18)
			player.getInterfaceManager().sendXPDisplay();
		else if (componentId >= 22 && componentId <= 24)
			setCurrentCounter(componentId - 22);
		else if (componentId == 27)
			switchTrackCounter();
		else if (componentId == 61)
			resetCounterXP();
		else if (componentId >= 31 && componentId <= 57)
			if (componentId == 33)
				setCounterSkill(4);
			else if (componentId == 34)
				setCounterSkill(2);
			else if (componentId == 35)
				setCounterSkill(3);
			else if (componentId == 42)
				setCounterSkill(18);
			else if (componentId == 49)
				setCounterSkill(11);
			else
				setCounterSkill(componentId >= 56 ? componentId - 27 : componentId - 31);

	}

	public void sendInterfaces() {
		if (xpDisplay)
			player.getInterfaceManager().sendXPDisplay();
	}

	public void refreshXpPopup() {
		player.getVarsManager().sendVarBit(10443, xpPopup ? 0 : 1);
	}

	public void switchXPDisplay() {
		xpDisplay = !xpDisplay;
		if (xpDisplay) {
			player.getInterfaceManager().sendXPDisplay();
		} else
			player.getInterfaceManager().closeXPDisplay();
	}

	public void switchXPPopup() {
		xpPopup = !xpPopup;
		refreshXpPopup();
		player.getPackets().sendGameMessage("XP pop-ups are now " + (xpPopup ? "en" : "dis") + "abled.");
	}

	public void restoreSkills() {
		for (int skill = 0; skill < level.length; skill++) {
			level[skill] = (short) getLevelForXp(skill);
			refresh(skill);
		}
	}

	public void setPlayer(Player player) {
		this.player = player;
		// temporary
		if (xpTracks == null) {
			xpPopup = true;
			xpTracks = new double[3];
			trackSkills = new boolean[3];
			trackSkillsIds = new byte[3];
			trackSkills[0] = true;
			for (int i = 0; i < trackSkillsIds.length; i++)
				trackSkillsIds[i] = 30;
		}
		if (fromTarget == null) {
			target = new int[25];
			fromTarget = new int[25];
			levelTarget = new boolean[25];
		}
	}

	public short[] getLevels() {
		return level;
	}

	public double[] getXp() {
		return temporaryXP != null ? temporaryXP : xp;
	}

	public int getLevel(int skill) {
		return level[skill];
	}

	public double getXp(int skill) {
		return getXp()[skill];
	}

	public boolean hasRequiriments(int... skills) {
		for (int i = 0; i < skills.length; i += 2) {
			int skillId = skills[i];
			if (skillId == DUNGEONEERING)
				continue;
			int skillLevel = skills[i + 1];
			if (getLevelForXp(skillId) < skillLevel)
				return false;

		}
		return true;
	}
	
	public int getTotalLevel() {
		return getTotalLevel(99);
	}

	public int getTotalLevel(int virtualLevel) {
		int level = 0;
		for (int i = 0; i < getXp().length; i++)
			level += getLevelForXp(i, virtualLevel);
		return level;
	}

	public long getTotalXp() {
		long xp = 0;
		for (int i = 0; i < this.getXp().length; i++)
			xp += this.getXp()[i];//Math.min(this.getXp()[i], 200000000);
		return xp;
	}

	public int getCombatLevel() {
		int attack = getLevelForXp(0);
		int defence = getLevelForXp(1);
		int strength = getLevelForXp(2);
		int hitpoints = getLevelForXp(3);
		int prayer = getLevelForXp(5);
		int ranged = getLevelForXp(4);
		int magic = getLevelForXp(6);
		double combatLevel = (defence + hitpoints + Math.floor(prayer / 2)) * 0.25;
		double warrior = (attack + strength) * 0.325;
		double ranger = ranged * 0.4875;
		double mage = magic * 0.4875;
		combatLevel += Math.max(warrior, Math.max(ranger, mage));
		return (int) combatLevel;
	}

	public int getCombatLevelWithSummoning() {
		return getCombatLevel() + getSummoningCombatLevel();
	}

	public int getSummoningCombatLevel() {
		double summon = Math.floor(getLevelForXp(Skills.SUMMONING) / 2) * 0.25;
		return (int) summon;
	}

	public void set(int skill, int newLevel) {
		level[skill] = (short) newLevel;
		refresh(skill);
	}

	public int drainLevel(int skill, int drain) {
		int drainLeft = drain - level[skill];
		if (drainLeft < 0) {
			drainLeft = 0;
		}
		level[skill] -= drain;
		if (level[skill] < 0) {
			level[skill] = 0;
		}
		refresh(skill);
		return drainLeft;
	}

	public void drainSummoning(int amt) {
		int level = getLevel(Skills.SUMMONING);
		if (level == 0)
			return;
		set(Skills.SUMMONING, amt > level ? 0 : level - amt);
	}

	public static int getXPForLevel(int level) {
		int points = 0;
		int output = 0;
		for (int lvl = 1; lvl <= level; lvl++) {
			points += Math.floor(lvl + 300.0 * Math.pow(2.0, lvl / 7.0));
			if (lvl >= level) {
				return output + 1;
			}
			output = (int) Math.floor(points / 4);
		}
		return 0;
	}

	public int getLevelForXp(int skill) {
		return getLevelForXp(skill, skill == DUNGEONEERING ? 120 : 99);
	}
	
	public int getLevelForXp(int skill, int levelCap) {
		double exp = getXp()[skill];
		for (int lvl = 0; lvl < levelCap - 1; lvl++) {
			if (EXP_FOR_LVL[lvl] >= exp) 
				return lvl + 1;
		}
		return levelCap;
	}

	public int getHighestSkillLevel() {
		int maxLevel = 1;
		for (int skill = 0; skill < level.length; skill++) {
			int level = getLevelForXp(skill);
			if (level > maxLevel)
				maxLevel = level;
		}
		return maxLevel;
	}

	public void init() {
	/*	for (int skill = 0; skill < level.length; skill++)
			getXp()[skill] = Math.min(getXp()[skill], 200000000);*/
		
		refreshSkills();
		refreshTargets();
		sendXPDisplay();
		if (!Settings.XP_BONUS_ENABLED)
			elapsedBonusMinutes = 0;
		else
			refreshXpBonus();
	}
	
	public void refreshSkills() {
		for (int skill = 0; skill < level.length; skill++)
			refresh(skill);
	}

	private double getXpBonusMultiplier() {
	/*	if (elapsedBonusMinutes >= 600)
			return 1.1;
		double hours = elapsedBonusMinutes / 60;
		return Math.pow((hours - 10) / 7.5, 2) + 1.1;*/
		return 1.5;
	}

	public void refreshBonusXp() {
		player.getVarsManager().sendVar(1878, (int) (xpBonusTrack * 10));
	}

	public void refreshXpBonus() {
		player.getVarsManager().sendVarBit(7232, 1);
		refreshElapsedBonusMinutes();
		refreshBonusXp();
	}

	public void increaseElapsedBonusMinues() {
		elapsedBonusMinutes++;
		refreshElapsedBonusMinutes();
	}

	public void refreshElapsedBonusMinutes() {
		player.getVarsManager().sendVarBit(7233, elapsedBonusMinutes);
	}

	public void refresh(int skill) {
		player.getPackets().sendSkillLevel(skill);
	}
	
	public void setTarget(int skill, int target, boolean level) {
		if (level && target >= 150) //120
			target = 150; //120
		/*else if (!level && target >= 200000000)
			target = (int) 200000000;*/
		this.target[skill] = target;
		levelTarget[skill] = level;
		fromTarget[skill] = level ? getLevelForXp(skill) : (int) getXp()[skill];
		refreshTargets();
	}
	
	public void resetTarget(int skill) {
		setTarget(skill, 0, false);
	}
	
	public void refreshTargets() {
		int mult = 0;
		int mult2 = 0;
		for (int i = 0; i < Skills.SKILL_NAME.length; i++) {
			if (target[i] > 0) {
				mult += 2 << getCounterSkill(i);
				if (levelTarget[i])
					mult2 += 2 << getCounterSkill(i);
				player.getVarsManager().sendVar(1969+getCounterSkill(i), target[i]);
				player.getVarsManager().sendVar(1994+getCounterSkill(i), fromTarget[i]);
			}
		}
		player.getVarsManager().sendVar(1966, mult);
		player.getVarsManager().sendVar(1968, mult2);
	}

	/*
	 * if(componentId == 33) setCounterSkill(4); else if(componentId == 34)
	 * setCounterSkill(2); else if(componentId == 35) setCounterSkill(3); else
	 * if(componentId == 42) setCounterSkill(18); else if(componentId == 49)
	 * setCounterSkill(11);
	 */

	public static int getCounterSkill(int skill) {
		switch (skill) {
		case ATTACK:
			return 0;
		case STRENGTH:
			return 1;
		case DEFENCE:
			return 4;
		case RANGE:
			return 2;
		case HITPOINTS:
			return 5;
		case PRAYER:
			return 6;
		case AGILITY:
			return 7;
		case HERBLORE:
			return 8;
		case THIEVING:
			return 9;
		case CRAFTING:
			return 10;
		case MINING:
			return 12;
		case SMITHING:
			return 13;
		case FISHING:
			return 14;
		case COOKING:
			return 15;
		case FIREMAKING:
			return 16;
		case WOODCUTTING:
			return 17;
		case SLAYER:
			return 19;
		case FARMING:
			return 20;
		case CONSTRUCTION:
			return 22;
		case HUNTER:
			return 21;
		case SUMMONING:
			return 23;
		case DUNGEONEERING:
			return 24;
		case MAGIC:
			return 3;
		case FLETCHING:
			return 18;
		case RUNECRAFTING:
			return 11;
		default:
			return -1;
		}

	}

	public double addXp(int skill, double exp) {
		return addXp(skill, exp, false);
	}

	public double getXPRate(boolean combatSkill) {
		double xpRate = player.isExpert() ? (combatSkill ? 10 : 3) : player.isHCIronman() || player.isUltimateIronman() ? (combatSkill ? 20 : 5) : player.isIronman() ? (combatSkill ? 40 : 10) :
				player.isDeadman() ? (combatSkill ? 1000 : 60) :
				(combatSkill ? 400 : 20);
		xpRate *= (1 +  (double)player.getDonator()/ 10);
		xpRate *= 1 + (player.hasVotedInLast24Hours() ? 0.1 : 0) + (World.isWishingWellActive() ? 0.15 : 0);
		/*if (player.getClanManager() != null)
			xpRate *= 1.01;*/
		if (player.getAuraManager().usingWisdom())
			xpRate *= 1.025;
		int hatID = player.getEquipment().getHatId();
		if (hatID >= 1038 && hatID <= 1048)
			xpRate *= 1.05;
		else if (hatID >= 1053 && hatID <= 1057)
			xpRate *= 1.025;
		else if (hatID == 41862 || hatID == 25643 || hatID == 25644)
			xpRate *= 1.1;
		if (player.getEquipment().getRingId() == 53943)
			xpRate *= 1.03;
		return xpRate;
	}
	
	private transient long lastBonusDropCycle;
	private transient int bonusXPDrop;
	
	public double addXp(int skill, double exp, boolean forceRSXp) {
		if (exp == 0)
			return 0;
		player.getControlerManager().trackXP(skill, (int) exp);
		boolean combatSkill = skill == SUMMONING || (skill >= ATTACK && skill <= MAGIC);
		if (player.isXpLocked() && combatSkill)
			return 0;
		/*if (player.isSafePk())
			forceRSXp = true;*/
		if (player.getAuraManager().usingWisdom())
			exp *= 1.025;
		int hatID = player.getEquipment().getHatId();
		if (hatID >= 1038 && hatID <= 1048)
			exp *= 1.05;
		else if (hatID == 41862 || hatID == 25643 || hatID == 25644)
			exp *= 1.1;
		if (player.getEquipment().getRingId() == 53943)
			exp *= 1.03;

		if(!forceRSXp && exp < RANDOM_EVENT_EXP && (!combatSkill || player.isUltimateIronman() || player.isHCIronman() || player.isExpert()) && CombatEventNPC.canRandomEvent(player)) {
			trackXPREvent += exp;
			if(trackXPREvent >= RANDOM_EVENT_EXP) {
				trackXPREvent = 0;
			/*	if(Utils.random(3) == 0) //random factor
					CombatEventNPC.startRandomEvent(player, skill);*/
			}
		}
		if ((combatSkill && getXp()[skill] >= 280681210/*200000000*//*13034431*/) || /*getXp()[skill] >= 200000000 || */player.isDungeoneer())
			forceRSXp = true;
		double bonus = 0;
		if (!forceRSXp/* && (!player.isCanPvp() || !combatSkill)*/) {
			if (!combatSkill && Utils.random(100) == 0 && Utils.currentTimeMillis() - player.getLastDollarKeyFragment() > (skill == Skills.SLAYER ? 500000 : 900000)  && DollarContest.winner == null) {
				player.setLastDollarKeyFragment();
				GameExecutorManager.fastExecutor.schedule(new TimerTask() {

					@Override
					public void run() {
						try {
							player.getInventory().addItemDrop(DollarContest.FRAGMENT_ID, 1);
							player.getPackets().sendGameMessage("You receive a pandora key fragment from skilling.");
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
					
				}, 300);
			}
			exp *= player.isExpert() ? (combatSkill ? 10 : 3) : player.isHCIronman() || player.isUltimateIronman() ? (combatSkill ? 20 : 5) : player.isIronman() ? (combatSkill ? 40 : 10) : (combatSkill ? (player.isDeadman() ? 1000 : (skill != SUMMONING && skill != PRAYER ? 400 : 100)) : skill == DUNGEONEERING ? 10 : 20);
			exp *= (1 +  (double)player.getDonator()/ 10);
			/*if ((player.isFast() || player.isSuperFast()) && skill != DUNGEONEERING) {
				exp *= Settings.FAST_MODE_MULTIPLIER;
				if (combatSkill && skill != PRAYER && skill != SUMMONING)
					exp *= player.isSuperFast() ? 16 : 8; //x180 (3),now x240 (4), now 16
				else if (player.isSuperFast() && skill != PRAYER && skill != SUMMONING)
					exp *= 3;
			}*/
			double oldExp = exp;
			//if (!player.isExtreme())
				exp *= 1 + (player.hasVotedInLast24Hours() ? 0.1 : 0) + (World.isWishingWellActive() ? 0.15 : 0);
			if (skill == World.getSkillOfTheDay())
				exp *= 1.05;
			if (player.getRegionId() == 12605)
				exp *= 1.2;
			else if (player.isAtVipZone())
				exp *= 1.10;
			else if (player.isAtDonatorZone())
				exp *= 1.03;
			
			/*if (!player.isExtreme()) {
				if (player.getClanManager() != null)
					exp *= 1.01;
			}*/
			
			exp += SCRewards.addSkillXP(player, skill, (int) oldExp);
			exp += Hati.addSkillXP(player, skill, (int) oldExp);

			if (skill == SLAYER &&
					(player.getPet() != null
					&& (player.getPet().getId() == Pets.PET_SMOKE_DEVIL.getBabyNpcId()
					||  player.getPet().getId() == Pets.PET_KRAKEN.getBabyNpcId()
							)))
				exp *= 1.03;
			else if (skill == SLAYER &&
					(player.getPet() != null
					&& (player.getPet().getId() == Pets.ABYSSAL_ORPHAN.getBabyNpcId()
					||  player.getPet().getId() == Pets.HELLPUPPY.getBabyNpcId()
					||  player.getPet().getId() == Pets.PET_SNAKELING.getBabyNpcId()
					||  player.getPet().getId() == Pets.PET_SNAKELING_2.getBabyNpcId()
					||  player.getPet().getId() == Pets.PET_SNAKELING_3.getBabyNpcId()
					||  player.getPet().getId() == Pets.VORKI.getBabyNpcId()
					||  player.getPet().getId() == Pets.NOON.getBabyNpcId()
					||  player.getPet().getId() == Pets.MIDNIGHT.getBabyNpcId()
							)))
				exp *= 1.05;
			else if (skill == SLAYER &&
					(player.getPet() != null
					&& (player.getPet().getId() == Pets.HYDRA.getBabyNpcId()
					||  player.getPet().getId() == Pets.HYDRA_2.getBabyNpcId()
							||  player.getPet().getId() == Pets.HYDRA_3.getBabyNpcId()
									||  player.getPet().getId() == Pets.HYDRA_4.getBabyNpcId()
							)))
				exp *= 1.07;
			else if (skill == HUNTER &&
					(player.getPet() != null
					&& (player.getPet().getId() == Pets.BABY_CHINCHOMPA.getBabyNpcId()
					|| player.getPet().getId() == Pets.BABY_CHINCHOMPA_2.getBabyNpcId()
					|| player.getPet().getId() == Pets.BABY_CHINCHOMPA_3.getBabyNpcId()
					|| player.getPet().getId() == Pets.BABY_CHINCHOMPA_4.getBabyNpcId())))
					exp *= 1.1;
			else if (skill == WOODCUTTING &&
					player.getPet() != null
					&& player.getPet().getId() == Pets.BEAVER.getBabyNpcId())
					exp *= 1.1;
			else if (skill == AGILITY &&
					player.getPet() != null
					&& player.getPet().getId() == Pets.GIANT_SQUIRREL.getBabyNpcId())
					exp *= 1.1;
			else if (skill == FISHING &&
					player.getPet() != null
					&& player.getPet().getId() == Pets.HERON.getBabyNpcId())
					exp *= 1.1;
			else if (skill == RUNECRAFTING &&
					player.getPet() != null
					&& player.getPet().getId() == Pets.RIFT_GUARDIAN.getBabyNpcId())
					exp *= 1.1;
			else if (skill == MINING &&
					player.getPet() != null
					&& player.getPet().getId() == Pets.ROCK_GOLEM.getBabyNpcId())
					exp *= 1.1;
			else if (skill == THIEVING &&
					player.getPet() != null
					&& player.getPet().getId() == Pets.ROCKY.getBabyNpcId())
					exp *= 1.1;
			else if (skill == FARMING &&
					player.getPet() != null
					&& player.getPet().getId() == Pets.TANGLEROOT.getBabyNpcId())
					exp *= 1.1;
			else if (skill == FARMING &&
					player.getPet() != null
					&& player.getPet().getId() == Pets.TANGLEROOT.getBabyNpcId())
					exp *= 1.1;
			else if (skill == FIREMAKING &&
					(player.getPet() != null
					&& (player.getPet().getId() == Pets.CUTE_PHOENIX_EGGLING.getBabyNpcId()
					|| player.getPet().getId() == Pets.MEAN_PHOENIX_EGGLING.getBabyNpcId())))
					exp *= 1.1;
			else if (player.getPet() != null
					&& player.getPet().getId() == Pets.BLOODHOUND.getBabyNpcId())
					exp *= 1.01;
			else if (player.getPet() != null &&
					(player.getPet().getId() == Pets.HORROR_LEFT_ARM.getBabyNpcId()
					|| player.getPet().getId() == Pets.HORROR_RIGHT_ARM.getBabyNpcId()
					|| player.getPet().getId() == Pets.HORROR_TAIL.getBabyNpcId())) {
				if(skill == PRAYER) {
					exp *= 1.15;
					player.getPet().gfx(2173);
				}
			}
			if (Settings.XP_BONUS_ENABLED)
				exp *= getXpBonusMultiplier();
			
			
			
			if (exp > oldExp) 
				bonus += exp - oldExp;
		}
		if (lastBonusDropCycle != WorldThread.WORLD_CYCLE) {
			lastBonusDropCycle = WorldThread.WORLD_CYCLE;
			bonusXPDrop = 0;
		}
		bonusXPDrop += bonus;
		player.getVarsManager().sendVar(2044, bonusXPDrop * 10);
		
		int oldLevel = getLevelForXp(skill);
		int oldCombatLevel = getCombatLevelWithSummoning();
		double oldExperience = getXp()[skill];
		for (int i = 0; i < trackSkills.length; i++) {
			if (trackSkills[i]) {
				if (trackSkillsIds[i] == 30 || (trackSkillsIds[i] == 29 && (skill == Skills.ATTACK || skill == Skills.DEFENCE || skill == Skills.STRENGTH || skill == Skills.MAGIC || skill == Skills.RANGE || skill == Skills.HITPOINTS)) || trackSkillsIds[i] == getCounterSkill(skill)) {
					xpTracks[i] += exp;
					refreshCounterXp(i);
				}
			}
		}
		player.getTasksManager().checkForProgression(DailyTasksManager.EXPERIENCE, skill, (int)exp);
		if (getXp()[skill] == MAXIMUM_EXP)
			return 0;
		getXp()[skill] += exp;
		if (getXp()[skill] > MAXIMUM_EXP) {
			getXp()[skill] = MAXIMUM_EXP;
		}
		int newLevel = getLevelForXp(skill);
		int levelDiff = newLevel - oldLevel;
		if (newLevel > oldLevel) {
			level[skill] += levelDiff;
			sendLevelUpInterface(skill);
			
			if (!player.isDungeoneer()) {
				if (oldLevel < 99 && newLevel >= 99 && player.getRights() != 2)
					player.getInventory().addItemDrop(TRIM_CAPES[skill], 1);
				if (!combatSkill) {
					int rewardCoins = (getXPForLevel(Math.min(99, newLevel)) - getXPForLevel(oldLevel)) /  (player.isSuperFast() && skill != DUNGEONEERING ? 60 : player.isFast() && skill != DUNGEONEERING ? 20 : 5);
					player.getInventory().addItemMoneyPouch(new Item(995, rewardCoins));
				}
			}
			
			if (combatSkill) {
				player.getAppearence().generateAppearenceData();
				if (skill == HITPOINTS)
					player.heal(levelDiff * 10);
				else if (skill == PRAYER)
					player.getPrayer().restorePrayer(levelDiff * 10);
			}
			player.getQuestManager().checkCompleted();
			player.getControlerManager().trackLevelUp(skill, level[skill]);
		}
		sendNews(skill, newLevel > oldLevel, oldCombatLevel, oldExperience);
		refresh(skill);
		return exp;
	}

	public static int getLevelForXp(double exp, int max) {
		int points = 0;
		int output = 0;
		for (int lvl = 1; lvl <= max; lvl++) {
			points += Math.floor(lvl + 300.0 * Math.pow(2.0, lvl / 7.0));
			output = (int) Math.floor(points / 4);
			if ((output - 1) >= exp) {
				return lvl;
			}
		}
		return max;
	}
	
	@SuppressWarnings("unused")
	public void sendNews(int skill, boolean levelUP, int combatLevelBefore, double oldXp) {
		if (((player.getRights() == 2 || player.isYoutuber()) && Settings.HOSTED) || player.isDungeoneer()
				|| World.BOTS.contains(player))
			return;
		if (!levelUP) {
			if (MAXIMUM_EXP == Integer.MAX_VALUE && getXp()[skill] == MAXIMUM_EXP) {
				World.sendNews(player, player.getDisplayName() + " has achieved legendary skill mastery in the " + Skills.SKILL_NAME[skill] + " skill. <col=ff8c38>("+player.getGameMode().toUpperCase()+")", World.WORLD_NEWS);
			} else if (getXp()[skill] > 50000000) { // 50m
				if (getLevelForXp(oldXp, 120) != 120 && getLevelForXp(getXp()[skill], 120) == 120)
					World.sendNews(player, player.getDisplayName() + " has achieved true skill mastery in the " + Skills.SKILL_NAME[skill] + " skill.<col=ff8c38> ("+player.getGameMode().toUpperCase()+")", World.WORLD_NEWS);
				else {
					int next = (int) (getXp()[skill] / 50000000);
					int xpachievement = next * 50000000;
					if (oldXp < xpachievement && getXp()[skill] >= xpachievement)
						World.sendNews(player, player.getDisplayName() + " has achieved " + (next * 50) + "m " + Skills.SKILL_NAME[skill] + " xp.<col=ff8c38> ("+player.getGameMode().toUpperCase()+")", World.WORLD_NEWS);
				}
			}
		} else {
			boolean combatSkill = skill == SUMMONING || (skill >= ATTACK && skill <= MAGIC);
			if (combatSkill && combatLevelBefore != 138 && getCombatLevelWithSummoning() == 138) {
				World.sendNews(player, player.getDisplayName() + " has achieved level 138 combat. <col=ff8c38> ("+player.getGameMode().toUpperCase()+")", World.WORLD_NEWS);
				return;
			}
			int level = getLevelForXp(skill);
			millestone: if (level % 10 == 0 || level == 99) {
				for (int i = 0; i < Skills.SKILL_NAME.length; i++) {
					if (player.getSkills().getLevelForXp(i) < level)
						break millestone;
				}
				World.sendNews(player, player.getDisplayName() + " has just achieved at least level " + level + " in all skills! <col=ff8c38>("+player.getGameMode().toUpperCase()+")", level == 99 ? World.WORLD_NEWS : World.FRIEND_NEWS);
				return;
			}
			if (level == 99)
				World.sendNews(player, player.getDisplayName() + " has achieved " + level + " " + Skills.SKILL_NAME[skill] + ".<col=ff8c38> ("+player.getGameMode().toUpperCase()+")", World.WORLD_NEWS);
			else if (level == 120)
				World.sendNews(player, player.getDisplayName() + " has achieved true skill mastery in the " + Skills.SKILL_NAME[skill] + " skill. <col=ff8c38>("+player.getGameMode().toUpperCase()+")", World.WORLD_NEWS);
		}
	}

	public static final int[] LEVEL_MUSIC =
	{ 30, 38, 66, 48, 58, 56, 52, 34, 70, 44, 42, 40, 36, 64, 54, 46, 28, 68, 61, 10, 60, 50, 32, 301, 417 };

	private void sendLevelUpInterface(int skill) {
		final boolean resizable = player.getInterfaceManager().hasRezizableScreen();
		int iconValue = getIconValue(skill);
		player.getPackets().sendCSVarInteger(1756, iconValue);
		player.getInterfaceManager().setWindowInterface(resizable ? 40 : 200, 1216);
		int level = player.getSkills().getLevelForXp(skill);
		player.getTemporaryAttributtes().put("leveledUp", skill);
		player.getTemporaryAttributtes().put("leveledUp[" + skill + "]", Boolean.TRUE);
		player.setNextGraphics(new Graphics(199));
		if (level == 99 || level == 120)
			player.setNextGraphics(new Graphics(1765));
		String name = Skills.SKILL_NAME[skill];
		player.getPackets().sendGameMessage("You've just advanced a" + (name.startsWith("A") ? "n" : "") + " " + name + " level! You have reached level " + level + ".");
		player.getVarsManager().sendVarBit(4757, iconValue);
		switchFlash(player, skill, true);
		player.getPackets().sendMusicEffect(LEVEL_MUSIC[skill]);
	}

	public static int getIconValue(int skill) {
		if (skill == Skills.ATTACK)
			return 1;
		if (skill == Skills.STRENGTH)
			return 2;
		if (skill == Skills.RANGE)
			return 3;
		if (skill == Skills.MAGIC)
			return 4;
		if (skill == Skills.DEFENCE)
			return 5;
		if (skill == Skills.HITPOINTS)
			return 6;
		if (skill == Skills.PRAYER)
			return 7;
		if (skill == Skills.AGILITY)
			return 8;
		if (skill == Skills.HERBLORE)
			return 9;
		if (skill == Skills.THIEVING)
			return 10;
		if (skill == Skills.CRAFTING)
			return 11;
		if (skill == Skills.RUNECRAFTING)
			return 12;
		if (skill == Skills.MINING)
			return 13;
		if (skill == Skills.SMITHING)
			return 14;
		if (skill == Skills.FISHING)
			return 15;
		if (skill == Skills.COOKING)
			return 16;
		if (skill == Skills.FIREMAKING)
			return 17;
		if (skill == Skills.WOODCUTTING)
			return 18;
		if (skill == Skills.FLETCHING)
			return 19;
		if (skill == Skills.SLAYER)
			return 20;
		if (skill == Skills.FARMING)
			return 21;
		if (skill == Skills.CONSTRUCTION)
			return 22;
		if (skill == Skills.HUNTER)
			return 23;
		if (skill == Skills.SUMMONING)
			return 24;
		return 25;
	}

	
	public static int getFlashVar(int skill) {
		int id;
		if (skill == Skills.ATTACK)
			id = 4732;
		else if (skill == Skills.STRENGTH)
			id = 4733;
		else if (skill == Skills.DEFENCE)
			id = 4734;
		else if (skill == Skills.RANGE)
			id = 4735;
		else if (skill == Skills.PRAYER)
			id = 4736;
		else if (skill == Skills.MAGIC)
			id = 4737;
		else if (skill == Skills.HITPOINTS)
			id = 4738;
		else if (skill == Skills.AGILITY)
			id = 4739;
		else if (skill == Skills.HERBLORE)
			id = 4740;
		else if (skill == Skills.THIEVING)
			id = 4741;
		else if (skill == Skills.CRAFTING)
			id = 4742;
		else if (skill == Skills.FLETCHING)
			id = 4743;
		else if (skill == Skills.MINING)
			id = 4744;
		else if (skill == Skills.SMITHING)
			id = 4745;
		else if (skill == Skills.FISHING)
			id = 4746;
		else if (skill == Skills.COOKING)
			id = 4747;
		else if (skill == Skills.FIREMAKING)
			id = 4748;
		else if (skill == Skills.WOODCUTTING)
			id = 4749;
		else if (skill == Skills.RUNECRAFTING)
			id = 4750;
		else if (skill == Skills.SLAYER)
			id = 4751;
		else if (skill == Skills.FARMING)
			id = 4752;
		else if (skill == Skills.CONSTRUCTION)
			id = 4753;
		else if (skill == Skills.HUNTER)
			id = 4754;
		else if (skill == Skills.SUMMONING)
			id = 4755;
		else
			id = 7756;
		return id;
	}
	
	public static void switchFlash(Player player, int skill, boolean on) {
		player.getVarsManager().sendVarBit(getFlashVar(skill), on ? 1 : 0);
	}
	
	public static boolean isFlashOn(Player player, int skill) {
		return player.getVarsManager().getBitValue(getFlashVar(skill)) == 1;
	}

	public double addXpStore(int skill, double exp) {
		player.getControlerManager().trackXP(skill, (int) exp);
		int oldLevel = getLevelForXp(skill);
		getXp()[skill] += exp;
		for (int i = 0; i < trackSkills.length; i++) {
			if (trackSkills[i]) {
				if (trackSkillsIds[i] == 30 || (trackSkillsIds[i] == 29 && (skill == Skills.ATTACK || skill == Skills.DEFENCE || skill == Skills.STRENGTH || skill == Skills.MAGIC || skill == Skills.RANGE || skill == Skills.HITPOINTS)) || trackSkillsIds[i] == getCounterSkill(skill)) {
					xpTracks[i] += exp;
					refreshCounterXp(i);
				}
			}
		}

		if (getXp()[skill] > MAXIMUM_EXP) {
			getXp()[skill] = MAXIMUM_EXP;
		}
		int newLevel = getLevelForXp(skill);
		int levelDiff = newLevel - oldLevel;
		if (newLevel > oldLevel) {
			level[skill] += levelDiff;
			player.getDialogueManager().startDialogue("LevelUp", skill);
			if (skill == SUMMONING || (skill >= ATTACK && skill <= MAGIC)) {
				player.getAppearence().generateAppearenceData();
				if (skill == HITPOINTS)
					player.heal(levelDiff * 10);
				else if (skill == PRAYER)
					player.getPrayer().restorePrayer(levelDiff * 10);
			}
			player.getQuestManager().checkCompleted();
		}
		refresh(skill);
		return exp;
	}
	
	public void addSkillXpRefresh(int skill, double xp) {
		getXp()[skill] += xp;
		level[skill] = (short) getLevelForXp(skill);
	}

	public void resetSkillNoRefresh(int skill) {
		getXp()[skill] = 0;
		level[skill] = 1;
	}

	public void setXp(int skill, double exp) {
		getXp()[skill] = exp;
		refresh(skill);
	}

	public boolean canObtainTrimmed() {
		int count99 = 0;
		for (int skill = 0; skill < SKILL_NAME.length; skill++) {
			if (level[skill] == 99)
				count99++;
		}
		return count99 >= 2;
	}

	public void setXp(double[] xp) {
		// TODO remove
		this.xp = xp;
	}
	
	public void setTemporaryXP(double[] xp) {
		this.temporaryXP = xp;
		for (int i = 0; i < this.xp.length; i++)
			level[i] = (short) getLevelForXp(i);
		init(); //reset stats
	}

	public double[] getTemporaryXP() {
		return temporaryXP;
	}
}
